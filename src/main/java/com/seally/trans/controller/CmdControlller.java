package com.seally.trans.controller;

import javax.annotation.Resource;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.seally.trans.service.TranslogService;
import com.seally.trans.util.CmdUtil;
import com.seally.trans.util.Constant;
import com.seally.trans.util.TransManager;

@Controller
public class CmdControlller {
	
	@Resource
	TransManager transManager;
	
	@Resource
	TranslogService translogService;
	
	/**
	 * @author dnc
	 * @since 2017年11月29日 上午7:24:05
	 * 消息接受及返回交互方法：
	 * @MessageMapping 注解对应页面发送消息的参数一 stompClient.send("/myCmd", {}, JSON.stringify({'name': name}));发送消息的参数一
	 * @SendTo 注解对应客户端的订阅字符串（注意前缀为WebSocketConfig.registry.enableSimpleBroker("/myTopic");）
	 */
	@MessageMapping("/myCmd") 
    @SendTo("/myTopic/myCmdInter")
    public CmdUtil cmdInter(CmdUtil message) throws Exception {
		if(message.getCmd()==null){
			return CmdUtil.getError(message.getCmd());
		}
		switch (message.getCmd()) {
			case Constant.CMD_TRANS_START_GLOBAL:
				return CmdUtil.getResponse(transManager.startTrans(),message.getCmd());
			case Constant.CMD_TRANS_RESTART_SUB:
				return CmdUtil.getResponse(transManager.startTrans(Integer.valueOf(message.getData().toString())),message.getCmd());
			case Constant.CMD_GET_TRANS_RESULT:
				return CmdUtil.getSuccess(translogService.getCurTransEchartsOption(),message.getCmd());
			case Constant.CMD_DELETE_TRANS_RESULT:
				return CmdUtil.getSuccess(translogService.clearTranslog(),message.getCmd());
			default:
				return CmdUtil.getError("命令未识别",Constant.CODE_UNKNOW,message.getCmd());
		}
    }
	
}
