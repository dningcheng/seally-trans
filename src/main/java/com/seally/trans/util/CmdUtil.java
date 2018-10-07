package com.seally.trans.util;

/**
 * @Date 2017年11月28日
 * @author dnc
 * 
 * 交互命令类
 */
public class CmdUtil {
	
	private Integer cmd;//客户端请求代码
	
	private Integer code = Constant.CODE_SUCCESS;//服务端响应
	
	private Object data;//交换附加数据

	public CmdUtil(){}
	
	public CmdUtil(Integer code,Integer cmd){
		this.code = code;
		this.cmd = cmd;
	}
	
	public CmdUtil(Object data,Integer cmd){
		this.data = data;
		this.cmd = cmd;
	}
	
	public CmdUtil(Integer code,Object data,Integer cmd){
		this.code = code;
		this.data = data;
		this.cmd = cmd;
	}
	
	public static CmdUtil getResponse(CmdUtil instance,Integer cmd){
		return new CmdUtil(instance.getCode(),instance.getData(),cmd);
	}
	
	public static CmdUtil getResponse(Integer code,Integer cmd){
		return new CmdUtil(code,cmd);
	}
	
	public static CmdUtil getSuccess(){
		return new CmdUtil();
	}
	
	public static CmdUtil getSuccess(Object data,Integer cmd){
		return new CmdUtil(data,cmd);
	}
	
	public static CmdUtil getError(Integer cmd){
		return new CmdUtil(Constant.CODE_ERR,cmd);
	}
	
	public static CmdUtil getError(Object data){
		return getError(data,null);
	}
	
	public static CmdUtil getError(Object data,Integer code,Integer cmd){
		return new CmdUtil(code,data,cmd);
	}
	
	public static CmdUtil getError(Object data,Integer cmd){
		return new CmdUtil(Constant.CODE_ERR,data,cmd);
	}
	
	public Integer getCmd() {
		return cmd;
	}

	public void setCmd(Integer cmd) {
		this.cmd = cmd;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
