package com.seally.trans.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seally.trans.model.Translog;

/**
 * @author dnc
 * 2017年11月30日
 *
 * 数据迁移记录mapper
 *
 */
public interface TranslogMapper {
	
	Integer addTranslog(Translog log);
	
	Integer deleteTranslog(Translog log);

	Integer updateTranslogById(Translog log);
	
	Integer updateTranslogByJobName(Translog log);
	
	Translog getTranslogById(@Param("id") Integer id);
	
	List<Translog> getTranslogList(Translog log);

	int clearTranslog();
	
}
