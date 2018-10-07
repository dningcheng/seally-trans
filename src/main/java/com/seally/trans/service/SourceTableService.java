package com.seally.trans.service;

/**
*
*@author dnc
*@version 创建时间：2017年11月30日
*
*
*/
public interface SourceTableService {
	
	Integer countRealNumByBetween(Integer startId,Integer endId,String tableName);
	
}
