package com.seally.trans.util;
/**
 * @Date 2017年12月1日
 * @author dnc
 * 
 * 常量类
 */
public interface Constant {
	
	//客户端命令回复code常量
	int CODE_SUCCESS = 200;//处理成功
	int CODE_ERR     = 500;//处理失败或服务器错误
	int CODE_UNKNOW  = 404;//命令不识别
	
	//转移记录状态常量
	int NONE_TRANS  = 0;//没有转移任何数据(无数据可转移)
	int SUCCE_TRANS = 1;//转移成功
	
	//数据迁移任务处理状态常量
	int STATE_TRANS_UNSTART  = 0;//任务转移状态 未开始
	int STATE_TRANS_STARTING = 1;//任务转移状态 进行中
	int STATE_TRANS_FINISHED = 2;//任务转移状态 已完成
	
	//客户端主动命令请求常量
	int CMD_TRANS_START_GLOBAL= 1;//开始全局整体任务转换
	int CMD_TRANS_RESTART_SUB = 2;//手动重新开始单个子任务启动迁移（用于自动处理失败后的情况）
	int CMD_TRANS_STOP_GLOBAL = 3;//停止转换
	int CMD_TRANS_STOP_SUB    = 4;//停止转换
	int CMD_GET_TRANS_RESULT  = 5;//获取处理结果数据（用于刷新报表）
	int CMD_DELETE_TRANS_RESULT  = 6;//清空转移记录表数据
	
	//服务器端推命令常量
	int CMD_PUSH_FINISHED_TRANS_TABLE_RECORDS = 100;//全局整体任务转换记录表生成结束
	int CMD_PUSH_FINISHED_TRANS_GLOBAL        = 101;//全局整体任务转换已经完成
	int CMD_PUSH_FINISHED_TRANS_SUB           = 102;//局部手动启动子任务转换完成
	
}
