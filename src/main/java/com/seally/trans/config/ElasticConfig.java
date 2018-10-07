package com.seally.trans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import com.seally.trans.util.ElasticDataSource;

@Configuration
public class ElasticConfig {
	
	private Logger logger = LoggerFactory.getLogger(ElasticConfig.class);  
    
	@Value("${esHost:000}")
    private String host; 
	
	@Value("${elastic.server.host}")
    private String defaultHost; 
	
	@Value("${esPort:000}")
    private Integer port;  
	
	@Value("${elastic.server.port}")
    private Integer defaultPort;  
      
	@Value("${esClusterName:000}")
    private String clusterName;
	
	@Value("${elastic.server.clusterName}")
    private String defaultClusterName;
    
    @Value("${elastic.client.pool.initialSize}")  
    private Integer initialSize;  
      
    @Value("${elastic.client.pool.maxSize}")  
    private Integer maxSize;  
      
    @Value("${elastic.client.pool.minSize}")  
    private Integer minSize;
    
    @Value("${elastic.client.pool.maxWait}")  
    private Integer maxWait;
    
	@Bean     //声明其为Bean实例  
    @Primary  //在同样的DataSource中，首先使用被标注的DataSource  
    public ElasticDataSource EsDataSource() throws Exception{  
    	
		ElasticDataSource dataSource = new ElasticDataSource();
		
    	dataSource.setHost("000".equals(host)?defaultHost:host);
    	dataSource.setClusterName("000".equals(clusterName)?defaultClusterName:clusterName);
    	dataSource.setPort(port.intValue() == 0?defaultPort:port);
    	dataSource.setInitialSize(initialSize);
    	dataSource.setMaxSize(maxSize);
    	dataSource.setMinSize(minSize);
    	dataSource.setMaxWait(maxWait);
    	logger.info("开始初始化es连接池...");
    	dataSource.initDataSource();
    	logger.info("初始化es连接池结束！");
		return dataSource;
    }
    
}
