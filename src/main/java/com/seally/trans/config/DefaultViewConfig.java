package com.seally.trans.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class DefaultViewConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		//如何是直接的跳转而不需要其它逻辑处理，则可以直接配置在此如登录首页
		registry.addViewController("/").setViewName("/login");
		//registry.addViewController("/").setViewName("forward:/login");//重定向
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		super.addViewControllers(registry);
	}
	
}
