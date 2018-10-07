package com.seally.trans.util;
/**
 * @Date 2018年3月10日
 * @author dnc
 * @Description 系统请求消息码值枚举类
 */
public enum CodeEnum {
	SUCCESS("操作成功",200),
	ERR("系统错误",500);
	 
    private String message;
    private int code;  
    
    // 构造方法<如果需要给枚举类增加属性，那么必须定义该构造方法用于定义枚举变量SUCCESS等
    private CodeEnum(String message, int code) { 
    	
        this.message = message;  
        this.code = code;  
    }  
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}

	public static void main(String[] args) {
		System.out.println(CodeEnum.ERR.getCode()+CodeEnum.ERR.getMessage());
	}
}
