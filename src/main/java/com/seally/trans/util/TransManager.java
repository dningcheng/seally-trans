package com.seally.trans.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.seally.trans.model.Translog;
import com.seally.trans.service.SourceTableService;
import com.seally.trans.service.TranslogService;

/**
 * @author dnc
 * @since 2017年11月19日 下午4:23:07
 * 数据迁移任务管理类
 */
@Component
public class TransManager {
	
	private static final Logger logger = LoggerFactory.getLogger(TransJob.class);
	
	@Autowired
	DataSource dataSource;//需要直接使用dataSource进行jdbc操作因此这里注入过来
	
	@Autowired
	ElasticDataSource esSource;
	
	@Autowired
	TranslogService translogService;
	
	@Autowired
	SourceTableService sourceTableService;
	
	//websocket服务器端发送消息模板，使用此模板给已经建立连接的指定客户端发送消息
	@Autowired
    private SimpMessagingTemplate socketTemplate;
	
	@Value("${esIndex:000}")  
	private String index;//es数据导入索引库
	
	@Value("${elastic.client.import.index}")  
	private String defaultIndex;//es数据导入索引库
	
	@Value("${esType:000}")  
	private String type;//es数据导入类型
	
	@Value("${elastic.client.import.type}")  
	private String defaultType;//es数据导入类型
	
	@Value("${dbTableName:000}")  
	private String tableName;//es数据导入数据来源表
	
	@Value("${trans.datasource.table.name}")  
	private String defaultTableName;//es数据导入数据来源表
	
	@Value("${trans.thread.pool.size}")  
	private Integer threads;//es数据导入批量提交每批数目
	
	@Value("${elastic.client.import.bulkSize}")  
	private Integer bulkSize;//es数据导入批量提交每批数目
	
	@Value("${trans.datasource.table.fetchSize}")  
	private Integer fetchSize;//每次加载到内存中的表中记录数
	
	@Value("${idMin:0}")  
	private Integer idmin;//id最小值
	
	@Value("${idMax:0}")  
	private Integer idmax;//id最大值
	
	//线程池
	private ExecutorService fixedThreadPool = null;
	
	public static Integer transState = 0;//当前任务的处理状态(未用) 0：未执行 1：执行中   2：执行完毕
	
	//数据迁移启动方法
	public CmdUtil startTrans(){
		
		tableName = ("000".equals(tableName)?defaultTableName:tableName);
		
		index = ("000".equals(index)?defaultIndex:index);
		
		type = ("000".equals(type)?defaultType:type);
		
		List<Translog> logs = translogService.getTranslogList(null);
		if((logs != null && logs.size() > 0)){
			return CmdUtil.getError("已经启动过任务，请执行重新启动单个子任务完成余下数据转移！");
		}
		
		if(transState == Constant.STATE_TRANS_STARTING){
			return CmdUtil.getError("转移任务正在进行中，请勿重复提交！");
		}
		
		if(fixedThreadPool == null){
			fixedThreadPool = Executors.newFixedThreadPool(threads);
		}
		//获取数据转移总记录数
		try {
			Connection connection = dataSource.getConnection();
			PreparedStatement prepareStatement = connection.prepareStatement(String.format("select max(id) as maxId,count(*) as totalNum from %s where id > %d and id < %d  ", tableName,idmin,idmax));
			
			ResultSet executeQuery = prepareStatement.executeQuery();
			int maxId = 0;
			int totalNum = 0;
			while (executeQuery.next()) {
				maxId = executeQuery.getInt("maxId");
				totalNum = executeQuery.getInt("totalNum");
			}
			
			logger.info("数据转移主键ID区间为 [ "+1+" , "+(maxId+1)+" ) 目标总数为 [ "+totalNum+" ] 条,共分给 [ "+threads+" ] 个job执行！");
			
			//划分任务
			int jobAvegeCount =  maxId / threads;
			int leaveCount = maxId % threads;
			
			List<TransJob> jobs = new ArrayList<TransJob>();
			final List<Future<?>> futures = new ArrayList<Future<?>>();
			
			for(int i=0;i<threads;i++){
				Integer fetchIdMin = jobAvegeCount*i;
				Integer fetchIdMax = fetchIdMin+jobAvegeCount;
				if(i==(threads-1)){//最后一个job
					fetchIdMax = fetchIdMax+leaveCount+1;
				}
				if(fetchIdMin == 0){//id最小从1开始
					fetchIdMin = 1;
				}
				
				//查询记录是否存在并传递主键
				Integer allCount = sourceTableService.countRealNumByBetween(fetchIdMin, fetchIdMax, tableName);
				Translog translog = new Translog("迁移线程"+i,tableName,fetchIdMin+"-"+fetchIdMax,allCount);
				Integer count = translogService.addTranslog(translog);
				if(count == 0){//保存失败
					//清空记录表
					translogService.clearTranslog();
					//清空任务记录
					jobs.clear();
					return CmdUtil.getError("转移记录表保存记录失败，请检查网络或数据库能否正常连接！");
				}
				TransJob transJob = new TransJob(dataSource, esSource, index, type, bulkSize, fetchIdMin, fetchIdMax, fetchSize, tableName,translog.getId(),translogService,sourceTableService,true);
				jobs.add(transJob);
			}
			//任务记录表记录生成完毕
			socketTemplate.convertAndSend("/myTopic/myCmdInter", CmdUtil.getResponse(Constant.CODE_SUCCESS, Constant.CMD_PUSH_FINISHED_TRANS_TABLE_RECORDS));
			jobs.forEach(job -> futures.add(fixedThreadPool.submit(job)));
			transState = Constant.STATE_TRANS_STARTING;//修改状态为正在执行中
			//统计任务执行状态
			new Thread(new Runnable() {
				@Override
				public void run() {
					futures.forEach(future -> {
						try {
							future.get();
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					transState = Constant.STATE_TRANS_FINISHED;
					//任务执行完毕，推送客户端进行相关处理
					socketTemplate.convertAndSend("/myTopic/myCmdInter", CmdUtil.getResponse(Constant.CODE_SUCCESS, Constant.CMD_PUSH_FINISHED_TRANS_GLOBAL));
				}
			}).start();
			
			return CmdUtil.getSuccess();
		} catch (Exception e) {
			logger.error("数据转移job生成失败："+e);
			return CmdUtil.getError("任务启动失败，请检查网络或数据库能否正常连接！");
		}
	}
	
	//数据迁移启动方法
	public CmdUtil startTrans(Integer translogId){
		
		tableName = ("000".equals(tableName)?defaultTableName:tableName);
		
		index = ("000".equals(index)?defaultIndex:index);
		
		type = ("000".equals(type)?defaultType:type);
		
		
		if(transState == Constant.STATE_TRANS_STARTING){
			return CmdUtil.getError("主任务正在进行中，请勿开启子任务！");
		}
		
		if(fixedThreadPool == null){
			fixedThreadPool = Executors.newFixedThreadPool(threads);
		}
		TransJob transJob = new TransJob(dataSource, esSource, index, type, bulkSize, null, null, fetchSize, tableName,translogId,translogService,sourceTableService,false);
		Future<?> submit = fixedThreadPool.submit(transJob);
		try {
			submit.get();
			//任务执行完毕，推送客户端进行相关处理
			socketTemplate.convertAndSend("/myTopic/myCmdInter", CmdUtil.getResponse(Constant.CODE_SUCCESS, Constant.CMD_PUSH_FINISHED_TRANS_SUB));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return CmdUtil.getSuccess();
	}
}
