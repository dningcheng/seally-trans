package com.seally.trans.util;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.seally.trans.annotation.EsDocument;
import com.seally.trans.annotation.EsField;

/**
 * @Date 2018年1月12日
 * @author dnc
 * @Description Elastic相关工具类
 */
public class ElasticUtil {
	
	private static Client client = null;
	
	public static int searchMaxNum = 10000;//es常规查询最大能够查询的条数，超过后会抛出异常
	
	public static void initClient(){
		// 设置集群名字
    	Settings settings = Settings.builder()
				.put("cluster.name", "escluster")
				.put("client.transport.sniff", true)//开启自动嗅探机制，可以自动链接集群中的其他节点
				//.put("client.transport.ignore_cluster_name", true)//客户端连接时是否验证集群名称
				.put("client.transport.ping_timeout", "5s")//ping节点的超时时间
				.put("client.transport.nodes_sampler_interval", "5s")//节点的超时时间
				.build();
	    try {
	    	// 读取的ip列表是以逗号分隔的
	    	client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.174.128"), 9300));
	    } catch (UnknownHostException e) {
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 验证索引是否存在
	 * @param client
	 * @param index
	 * @return
	 */
	public static boolean existIndex(Client client,String index){
		return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
	} 
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 删除索引
	 * @param indices
	 * @return
	 */
	public static boolean delIndex(Client client,String... indices){
		DeleteIndexResponse deleteIndexResponse = client
				.admin()
				.indices()
				.prepareDelete(indices)
				.get();
		return deleteIndexResponse.isAcknowledged();
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 插入文档
	 * @param client
	 * @param index 索引库
	 * @param type 类型
	 * @param mapDocument 文档内容
	 * @return
	 */
	public static boolean insertDocument(Client client,String index,String type,Map<String,Object> mapDocument){
		IndexResponse response = client.prepareIndex(index, type)
		        .setSource(mapDocument)
		        .get();
		return response==null?false:response.status().getStatus()==201;
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 插入文档
	 * @param client
	 * @param index 索引库
	 * @param type 类型
	 * @param jsonDocument 文档内容
	 * @return
	 */
	public static boolean insertDocument(Client client,String index,String type,String jsonDocument){
		IndexResponse response = client.prepareIndex(index, type)
		        .setSource(jsonDocument,XContentType.JSON)
		        .get();
		return response==null?false:response.status().getStatus()==201;
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 插入文档
	 * @param client
	 * @param obj 需要注解EsDocument 用来获取文档存储的索引库和类型参数
	 */
	public static boolean insertDocument(Client client,Object obj){
		Class<?> clazz = obj.getClass();
		EsDocument classAnnotation  = clazz.getDeclaredAnnotation(EsDocument.class);
		//获取索引和类型
		if(classAnnotation == null)	return false;
		String index = classAnnotation.index();
		String type = classAnnotation.type();
		if(index == null || type == null)	return false;
		return insertDocument(client,index,type,obj);
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 插入文档
	 * @param client
	 * @param index
	 * @param type
	 * @param obj
	 * @return
	 */
	public static boolean insertDocument(Client client,String index,String type,Object obj){
		try {
			Map<String,Object> map = new HashMap<>();
			Field[] clazzFields = obj.getClass().getDeclaredFields();
			for(Field field : clazzFields){
				field.setAccessible(true);
				EsField fieldAnnotation = field.getDeclaredAnnotation(EsField.class);
				
				if(fieldAnnotation == null)	continue;
				
				//实体bean的真实字段类型名
				String objFieldTypeName = field.getType().getSimpleName();
				
				//实体bean的真实字段名
				String objFieldName = field.getName();
				//es中对应的映射属性名称
				String esFieldName = fieldAnnotation.value();
				
				if("".equals(esFieldName)){
					esFieldName = objFieldName;
				}
				//获取实体bean属性值
				Object object = field.get(obj);//获取属性值
				
				//对于日期，保存时间戳
				if(objFieldTypeName.equals("Date") && object != null){
					map.put(esFieldName,((Date)object).getTime());
				}else{
					map.put(esFieldName,object);
				}
			}
			return insertDocument(client,index,type,map);
		} catch (Exception e) {
			System.out.println("文档保存异常："+e.getMessage());
			return false;
		}
	}
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 删除文档
	 * @param client
	 * @param index 索引
	 * @param type 类型
	 * @param id 文档id
	 * @return
	 */
	public static boolean delDocumentById(Client client,String index,String type,String id){
		DeleteResponse response = client.prepareDelete(index, type, id)
				.get();
		return response==null?false:response.status().getStatus()==200;
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 更新文档 （只会更新文档中字段匹配的文档内容）
	 * @param client
	 * @param index
	 * @param type
	 * @param id
	 * @param mapDocument
	 * @return
	 */
	public static UpdateResponse updateDocument(Client client,String index,String type,String id,Map<String,Object> mapDocument){
		
		UpdateResponse updateResponse=null;
		try {
			UpdateRequest updateRequest = new UpdateRequest()
					.index(index)
					.type(type)
					.id(id)
					.doc(mapDocument);
			updateResponse = client.update(updateRequest).get();
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("更新文档异常："+e.getMessage());
		}
		return updateResponse;
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 修改或新增文档，存在则更新，不存在则增加
	 * @param client
	 * @param index
	 * @param type
	 * @param id
	 * @param mapDocument
	 * @return
	 */
	public static UpdateResponse updateOrInsertDocument(Client client,String index,String type,String id,Map<String,Object> mapDocument){
		UpdateResponse updateResponse =null;
		IndexRequest indexRequest = new IndexRequest(index, type, id).source(mapDocument);
		UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(mapDocument)
		        .upsert(indexRequest);              
		try {
			updateResponse = client.update(updateRequest).get();
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("更新文档异常："+e.getMessage());
		}
		return updateResponse;
	}
	
	/**
	 * @Date 2018年1月17日
	 * @author dnc
	 * @Description 单字段精确查询
	 * @param client
	 * @param index
	 * @param type
	 * @param from
	 * @param size
	 * @param sortMap
	 * @param queryMap 
	 * @return
	 */
	public static SearchResponse termSearch(Client client,String index,String type,int from,int size,Map<String,Integer> sortMap,Map<String,Object> queryMap){
		SearchRequestBuilder searchBuilder = client.prepareSearch().setIndices(index).setTypes(type);
		
		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		for(Entry<String, Object> entry:queryMap.entrySet()){
			if(entry.getKey()!=null && !"".equals(entry.getKey())){
				bool.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
			}
		}
		searchBuilder.setQuery(bool);
		return searchBuilder.get();
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 布尔全文搜索
	 * @param client
	 * @param index 索引库
	 * @param type 类型
	 * @param from 起始页码
	 * @param size 单页条数
	 * @param text 检索词
	 * @param sortMap 排序字段  其值   1用于升序排序  其它 用于降序排序
	 * @param fields 需要检索的字段
	 * @return
	 */
	public static SearchResponse multiMatchSearch(Client client,String index,String type,int from,int size,Map<String,Integer> sortMap,String text,String... fields){
		SearchRequestBuilder searchBuilder = initMatchSearch(client,index,type,null,from,size,sortMap,text,fields);
		return searchBuilder.get();
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 大数据量甚至是全库滚动检索
	 * @param client
	 * @param index
	 * @param type
	 * @param size 单次返回数量
	 * @param seconds 游标单次有效期
	 * @param sortMap 排序字段
	 * @param text 检索词
	 * @param fields 检索目标字段
	 * @return
	 */
	public static SearchResponse multiMatchScrollSearch(Client client,String index,String type,int size,Long seconds,Map<String,Integer> sortMap,String text,String... fields){
		SearchRequestBuilder searchBuilder = initMatchSearch(client,index,type,seconds,0,size,sortMap,text,fields);
		SearchResponse searchResponse = searchBuilder.get();
		return searchResponse;
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 多值查询请求体构建
	 * @param client
	 * @param index
	 * @param type
	 * @param seconds 如果为游标查询则设置此参数，否则请务必设置为null
	 * @param from 如果from为null则默认为0
	 * @param size 单次/每页返回的条数
	 * @param sortMap 排序字段
	 * @param text 检索关键词   null/""等价于无条件检索
	 * @param fields 需要匹配的目标字段集
	 * @return
	 */
	public static SearchRequestBuilder initMatchSearch(Client client,String index,String type,Long seconds,Integer from,int size,Map<String,Integer> sortMap,String text,String... fields){
		SearchRequestBuilder searchBuilder = client.prepareSearch().setIndices(index).setTypes(type);
		//查询体
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		
		if(text==null || "".equals(text)){
			MatchAllQueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
			boolQuery.filter(matchAllQuery);//boolQuery.must(matchAllQuery);
		}else{
			//包装一个多字段匹配查询
			MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(text, fields);
			multiMatchQuery.type(Type.MOST_FIELDS);
			multiMatchQuery.minimumShouldMatch("70%");
			boolQuery.filter(multiMatchQuery);//boolQuery.must(multiMatchQuery);
		}
		
		searchBuilder.setPostFilter(boolQuery);//searchBuilder.setQuery(boolQuery);
		
		//分页
		if(seconds == null){
			searchBuilder.setFrom(from==null?0:from).setSize(size);
		}else{
			searchBuilder.setSize(size);
			searchBuilder.setScroll(new Scroll(TimeValue.timeValueSeconds(seconds)));
		}
		
		//多级排序
		for(Entry<String, Integer> entry:sortMap.entrySet()){
			if(entry.getKey()!=null){
				searchBuilder.addSort(entry.getKey(),entry.getValue()==1?SortOrder.ASC:SortOrder.DESC);
			}
		}
		
		return searchBuilder;
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 滚动查询，用于支持深度分页甚至是滚动查询全部文档,受游标有效期限制,只有未失效才可使用,但可以支持大数据量甚至是全部文档
	 * @param client
	 * @param scrollId 游标id
	 * @param seconds 游标有效期   传递null则默认60
	 * @return
	 */
	public static SearchResponse scrollSearch(Client client,String scrollId,Long seconds){
		
		//设定查询最新游标
		SearchScrollRequestBuilder searchScrollBuilder = client.prepareSearchScroll(scrollId);
		
		//重新设定保持查询上下文时间
		searchScrollBuilder.setScroll(TimeValue.timeValueSeconds(seconds==null?60:seconds));
		SearchResponse searchResponse = searchScrollBuilder.get();
		
		//已经查询完,自动清除游标节省资源
		if(searchResponse.getHits().getHits().length==0){
			clearScroll(client,searchResponse.getScrollId());
		}
		
		return searchResponse;
	}
	
	/**
	 * @Date 2018年1月16日
	 * @author dnc
	 * @Description 清除游标
	 * @param client
	 * @param scrollId
	 */
	public static void clearScroll(Client client,String scrollId){
		ClearScrollRequest request = new ClearScrollRequest();
		request.addScrollId(scrollId);
		client.clearScroll(request);
	}
	
	
	
	/**
	 * @Date 2018年1月14日
	 * @author dnc
	 * @Description 从es结果集获取实体集合
	 * @param hits
	 * @param clazz
	 * @return
	 */
	public static List<Object> getDataListByHits(SearchHit[] hits,Class<?> clazz){
		List<Object> list  =new ArrayList<>();
		for(SearchHit hit:hits){//遍历获取每条记录
			//获取源文档
			Map<String, Object> source = hit.getSource();
			try {
				Object newInstance = clazz.newInstance();
				if(newInstance == null) continue;
				boolean addFlag = false;
				//获取该类所有属性
				Field[] logFields = clazz.getDeclaredFields();
				for(Field field : logFields){
					field.setAccessible(true);
					//尝试获取该字段所添加的es字段注解EsField
					EsField fieldAnnotation = field.getDeclaredAnnotation(EsField.class);
					
					//如果没有被注解，忽略类的该属性处理
					if(fieldAnnotation == null) continue;
					
					//获取实体类该属性的真实类型
					String beamFieldTypeName = field.getType().getSimpleName();
					
					//获取实体类该属性对应es中的映射字段名称
					String annotationFieldName = fieldAnnotation.value();
					String esFieldName = "".equals(annotationFieldName)?field.getName():annotationFieldName;
					
					//根据注解的es映射字段名获取结果中查出来的字段值
					Object object = source.get(esFieldName);
					
					//字段值为空，不对类的该字段进行处理
					if(object==null) continue;
					
					//处理日期格式的字段
					if(beamFieldTypeName.equals("Date")){
						String value = object.toString();
						String pattern = "[0-9]*";
						if(Pattern.matches(pattern, value)){
							field.set(newInstance, new Date(Long.valueOf(value)));
						}else if(value!=null && value.length()>0){
							try {
								field.set(newInstance, new SimpleDateFormat(fieldAnnotation.format()).parse(value));
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("日期转化异常："+e.getMessage());
							}
						}
					}else{
						field.set(newInstance, object);
					}
					addFlag = true;
				}
				if(addFlag)	list.add(newInstance);//加入集合
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("es结果集到实体封装异常："+e.getMessage());
			}
		}
		return list;
	}
}
