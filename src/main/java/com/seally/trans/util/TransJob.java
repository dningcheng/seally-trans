package com.seally.trans.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.seally.trans.model.SystemLog;
import com.seally.trans.model.Translog;
import com.seally.trans.service.SourceTableService;
import com.seally.trans.service.TranslogService;

/**
 * @author dnc
 * @since 2017年11月19日 下午7:30:25
 * 具体数据迁移执行类
 */
public class TransJob implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(TransJob.class);  
	
	private DataSource dataSource;
	
	private ElasticDataSource esSource;
	
	private TranslogService translogService;
	
	private SourceTableService sourceTableService;
	
	private String index;//提交es集群中索引库索引
	
	private String type;//提交es集群中索引库类型
	
	private Integer bulkSize;//每批提交数目
	
	private Integer fetchIdMin;//任务抓取表主键id下限值（包含fetchIdMin）
	
	private Integer fetchIdMax;//任务抓取表主键id上限值（不包含fetchIdMax）
	
	private Integer fetchSize;//每次抓取表记录数
	
	private String fetchTableName;//抓取表名
	
	private Integer translogId;//转移执行记录主键id,根据此值进行转移记录
	
	private boolean firstTrans;//是否首次进行转移 true： 是        false： 否
	
	public TransJob(){};
	
	public TransJob(DataSource dataSource, ElasticDataSource esSource, String index, String type,
			Integer bulkSize, Integer fetchIdMin, Integer fetchIdMax, Integer fetchSize, String fetchTableName,
			Integer translogId,TranslogService translogService,SourceTableService sourceTableService,boolean firstTrans) {
		this.dataSource = dataSource;
		this.esSource = esSource;
		this.index = index;
		this.type = type;
		this.bulkSize = bulkSize;
		this.fetchIdMin = fetchIdMin;
		this.fetchIdMax = fetchIdMax;
		this.fetchSize = fetchSize;
		this.fetchTableName = fetchTableName;
		this.translogId = translogId;
		this.translogService = translogService;
		this.sourceTableService = sourceTableService;
		this.firstTrans = firstTrans;
	}
	/**
	 * @Transactional 注解用于配置事务（propagation事务传播行为、isolation事务隔离级别）
	 * @param beginId
	 * @param endId
	 * @param connection
	 * @param translogId
	 */
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_COMMITTED)
	public void beginTrans(Integer beginId,Integer endId,Connection connection,Integer translogId){
		List<SystemLog> logs = new ArrayList<SystemLog>();
    	logs.add(new SystemLog());//占位
		
    	//--------------------mysql读取记录开始--------------------
    	try{
			PreparedStatement prepareStatement = connection.prepareStatement(String.format("select * from %s where id >= ? and id < ?",fetchTableName));
			prepareStatement.setInt(1,beginId);
			prepareStatement.setInt(2,endId);
			ResultSet executeQuery = prepareStatement.executeQuery();
	    	while (executeQuery.next()) {
	    		SystemLog log = new SystemLog();
	    		log.setLogId(executeQuery.getInt("id"));
	    		log.setOrgId(executeQuery.getInt("org_id"));
	    		log.setUserId(executeQuery.getInt("user_id"));
	    		log.setUnitId(executeQuery.getInt("unit_id"));
	    		log.setModuleCode(executeQuery.getString("module_code"));
	    		log.setApiCode(executeQuery.getString("api_code"));
	    		log.setUserAccount(executeQuery.getString("user_account"));
	    		log.setUnitName(executeQuery.getString("unit_name"));
	    		log.setOpMethod(executeQuery.getString("op_method"));
	    		log.setOpContent(executeQuery.getString("op_content"));
	    		log.setOpResult(executeQuery.getString("op_result"));
	    		Date optime = (Date)executeQuery.getObject("op_time");
	    		log.setOpTime(optime);
	    		log.setModuleParkPlate(executeQuery.getString("module_park_plate"));
	    		logs.add(log);
			}
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("mysql拉取数据失败:", e);
			return ;
		}
    	
    	logger.debug(Thread.currentThread().getName()+"线程处理信息： 获取id区间为：[ "+beginId+" , "+endId+" ) 的数据 [ "+(logs.size()-1)+" ] 条!");
    	if(logs.size()<=1){//没有实际数据
    		updateTranslog(beginId,endId,null,Constant.NONE_TRANS,translogId);//更新成功记录
    		return ;
    	}
    	
    	//--------------------es转移开始--------------------
    	Integer estransIdMin = logs.get(1).getLogId();//用于记录批量插入的开始id
    	Integer estransIdMax = logs.get(logs.size()-1).getLogId()+1;//用于记录批量插入的结束id
    	int count = 0;//计数器
    	try{
    		Client client = esSource.getClient();
        	BulkRequestBuilder bulkRequest = client.prepareBulk();
        	for (int i = 1; i < logs.size(); i++) {
        		count++;
        	    bulkRequest.add(client.prepareIndex(index, type).setSource(JSON.toJSONString(logs.get(i)), XContentType.JSON));
        	    // 每bulkSize条提交一次
        	    if (i % bulkSize == 0 || i == (logs.size()-1)) {
        	        bulkRequest.execute().actionGet();
        	        logger.debug(Thread.currentThread().getName()+"线程处理信息： 提交id区间为：[ "+beginId+" , "+endId+" ) 的数据 [ "+(logs.size()-1)+" ] 条!");
        	        estransIdMax = logs.get(i).getLogId()+1;//最大id作为本次截止记录
        	        updateTranslog(estransIdMin.intValue(),estransIdMax.intValue(),count,Constant.SUCCE_TRANS,translogId);//更新成功记录
        	        bulkRequest = client.prepareBulk();//新开一个批次
        	        estransIdMin = estransIdMax ; //最后一次成功提交作为下一次开始记录
        	    	count = 0;//计数器归零
        	    }
        	}
        	esSource.releaseClient(client);
    	}catch (Exception e) {
    		e.printStackTrace();
    		logger.error("es服务存储数据失败:", e);
		}
		
	}
	
	
	/**
	 * 根据无数据、成功、失败 记录转移结果
	 * 
	 * @author dnc
	 * @Date 2017-11-30
	 * 
	 * @param beginId id开始记录
	 * @param endId   id结束记录
	 * @param count   实际数据量（对于成功才有用）
	 * @param updateType 0：无数据记录   1：成功记录 
	 * @param translogId 记录表主键id值
	 */
	private void updateTranslog(Integer beginId,Integer endId,Integer count,Integer updateType,Integer translogId){
		
		Translog translog = translogService.getTranslogById(translogId);
		
		String oldSucBetween = translog.getSucBetween();
		String nowBetween = beginId+"-"+endId;
		if(updateType==Constant.NONE_TRANS){//没有数据的记录
			String oldNonBetween = translog.getNoneBetween();
			if(StringUtils.hasText(oldNonBetween)){
				if(oldNonBetween.contains(nowBetween)){//不用更新
					return ;
				}
				nowBetween += ("*"+oldNonBetween);
			}
			translog.setNoneBetween(nowBetween);
		}else if(updateType==Constant.SUCCE_TRANS){//成功记录
			if(StringUtils.hasText(oldSucBetween)){
				nowBetween += ("*"+oldSucBetween);
			}
			translog.setSucBetween(nowBetween);
			translog.setSucCount(translog.getSucCount()+count);
		}
		translogService.updateTranslogById(translog);
	}
	
	@Override
	public void run() {
		try {
			Connection connection = dataSource.getConnection();
			if(!firstTrans){ //对已存在的进行再次转移
				//根据总区间以及成功区间计算出失败区间再次执行
				Translog translog = translogService.getTranslogById(translogId);
				if(translog == null){
					return ;
				}
				String allBetween = translog.getAllBetween();//总共转移的区间集合   元素格式：1-20000
				if(!StringUtils.hasText(allBetween)){
					return ;
				}
				List<String> needTransBetween = new ArrayList<String>();//需要重新转移的区间集合   元素格式：1000-2000
				String[] allSplit = allBetween.split("-");
				Integer allBeginId = Integer.valueOf(allSplit[0]);
				Integer allEndId = Integer.valueOf(allSplit[1]);
				
				String sucBetween = translog.getSucBetween();//已经成功转移的区间字符串      格式：  1000-2000*2000-3000
				if(StringUtils.hasText(sucBetween)){//有曾经成功过的记录
					List<Integer> sucBenginIDPoints = new ArrayList<>();//记录成功开始端点数据
					List<Integer> sucEndIDPoints = new ArrayList<>();//记录成功结束端点数据
					String[] split = sucBetween.split("\\*");
					for(String between:split){
						if(StringUtils.hasText(between)){
							String[] split2 = between.split("-");
							sucBenginIDPoints.add(Integer.valueOf(split2[0]));
							sucEndIDPoints.add(Integer.valueOf(split2[1]));
						}
					}
					//排序成功记录切入点
					Collections.sort(sucBenginIDPoints);
					Collections.sort(sucEndIDPoints);
					//计算并重新规划失败区间
					Integer newBegin = allBeginId;
					for(int id=allBeginId;id<=allEndId;id++){
						if(sucBenginIDPoints.contains(id)){//碰到成功起始点
							if(newBegin != id){
								needTransBetween.add(newBegin.intValue()+"-"+id);//记录该区间
							}
							int indexOf = sucBenginIDPoints.indexOf(id);//查询起始点索引位置
							newBegin = sucEndIDPoints.get(indexOf);//修改起始点位置到原本转移成功的结束位置
						}
						if(id==allEndId && id != newBegin.intValue()){
							needTransBetween.add(newBegin.intValue()+"-"+id);//记录该区间
						}
					}
				}else{
					needTransBetween.add(allBetween);
				}
				
				//对最终结果进行重新转移
				needTransBetween.forEach(between ->{
					if(StringUtils.hasText(between)){
						String[] split = between.split("-");
						int begin = Integer.valueOf(split[0]);
						int end = Integer.valueOf(split[1]);
						//分配任务并执行
						calcTrans(begin,end,translogId,connection);
					}
				});
			}else{//首次创建任务并转移
				calcTrans(fetchIdMin,fetchIdMax,translogId,connection);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 根据上下限参数计算并执行转移
	 * @param minId
	 * @param MaxId
	 * @param logId
	 * @param connection
	 */
	private void calcTrans(Integer minId,Integer MaxId,Integer logId,Connection connection){
		if(minId > MaxId ) return;
		Integer num = (MaxId-minId)%fetchSize==0?(MaxId-minId)/fetchSize:(MaxId-minId)/fetchSize+1;
		for(int i=0;i<num;i++){//自动计算区间
			int beginId = i*fetchSize;
			int endId = beginId+fetchSize;
			if(i!=(num-1)){
				beginTrans(minId+beginId,minId+endId,connection,logId);
			}else{
				beginTrans(minId+beginId,MaxId,connection,logId);
			}
		}
	}
	
}
