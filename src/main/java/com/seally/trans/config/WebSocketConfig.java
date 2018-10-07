package com.seally.trans.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker //注解用于开启使用STOMP协议来传输基于代理（MessageBroker）的消息，这时候控制器（controller）开始支持@MessageMapping,就像是使用@requestMapping一样
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	/**
	 * @author dnc
	 * @Date 2017年11月29日 上午7:20:36
	 * 注册一个Stomp的节点（endpoint）,并指定使用SockJS协议，作为客户端建立连接时使用的url地址:var socket = new SockJS('/myCmdEndpoint');
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/myCmdEndpoint").withSockJS();
	}
	
	/**
	 * @author dnc
	 * @Date 2017年11月29日 上午7:21:08
	 * 配置消息代理，/myTopic 作为消息发送目的地主题前缀
	 */
	@Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/myTopic");
    }
}
