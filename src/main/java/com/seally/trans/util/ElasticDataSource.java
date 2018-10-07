package com.seally.trans.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dnc
 * @since 2017年11月19日 上午9:47:27
 * 自定义es客户端连接池
 */
public class ElasticDataSource {
	
	private static Logger logger = LoggerFactory.getLogger(ElasticDataSource.class);  
	
	private  String  host="localhost";           //集群主机
	private  Integer port=9300;                  //端口
	private  String  clusterName="escluster";    //集群名称
	private  Integer initialSize=50;             //初始化大小
	private  Integer maxSize=50;                 //最大数目
	private  Integer minSize=50;                 //最小数目
	private  Integer maxWait=6000;               //最大等待时间 
	
	private static LinkedList<Client> pool = new LinkedList<Client>();
	
	public ElasticDataSource() { }
	
	@SuppressWarnings("resource")
	public void initDataSource() throws Exception{
		Settings  settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true).build();
		boolean checkHealth =false;
		for(int index=0;index<initialSize;index++){
			TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(host), port));
			if(!checkHealth){
				checkHealth=true;
				client.admin().cluster().prepareClusterStats().execute().get().getStatus();
			}
			logger.info("put into espool client"+index);
			ElasticDataSource.pool.add(client);
		}
		logger.info("初始化es连接池:"+ElasticDataSource.pool.size());
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public Integer getMinSize() {
		return minSize;
	}

	public void setMinSize(Integer minSize) {
		this.minSize = minSize;
	}

	public Integer getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Integer maxWait) {
		this.maxWait = maxWait;
	}

	//取出连接池客户端
	public synchronized Client getClient() throws Exception{
		if(pool.size()>0){
			return pool.removeFirst();
		}else{
			if(pool.size()<minSize){
				poolExpend();
			}
		}
		return pool.removeFirst();
	}
	
	//归还客户端给连接池
	public void releaseClient(Client client){
		pool.add(client);
	}
	
	//扩容连接池
	@SuppressWarnings("resource")
	private void poolExpend() throws Exception{
		logger.info("开始扩容es连接池");
		boolean checkService = false;
		Settings  settings = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true).build();
		for(int index=pool.size();index<maxSize;index++){
			TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(host), port));
			if(!checkService){
				checkService = true;
				client.admin().cluster().prepareState().execute().actionGet();
			}
			ElasticDataSource.pool.add(client);
		}
		logger.info("扩容es连接池完成");
	}

}
