package com.seally.trans.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author dnc
 * 2017年11月30日
 * 数据转移源表映射
 */
public interface SourceTableMapper {
	
	/**
	 * 根据指定id范围统计实际需要转移的记录数
	 * @param startId
	 * @param endId
	 * @return
	 */
	Integer countRealNumByBetween(@Param("startId") Integer startId,@Param("endId") Integer endId,@Param("tableName") String tableName);
	
}
