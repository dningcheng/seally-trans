package com.seally.trans.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.seally.trans.model.Translog;
import com.seally.trans.service.TranslogService;

@Controller
public class ComController {
	
	@Autowired
	TranslogService translogService;
	
	@RequestMapping("/login")
	public String login(Map<String,Object> map,String userName,String passWord){
		
		if("ad".equals(userName) && "min".equals(passWord)){
			List<Translog> logs = translogService.getTranslogList(null);
			
			map.put("logs", logs);
			
			return "/index";
			
		}else{
			map.put("msg", "用户名密码错误！");
			return "/login";
		}
	}
	
}
