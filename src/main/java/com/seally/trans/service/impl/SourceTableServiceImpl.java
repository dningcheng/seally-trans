package com.seally.trans.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.seally.trans.mapper.SourceTableMapper;
import com.seally.trans.service.SourceTableService;


/**
 * @author dnc
 * 2017年11月30日
 * 数据源业务逻辑处理类
 */
@Service
public class SourceTableServiceImpl implements SourceTableService {
	
	@Autowired
	SourceTableMapper sourceTableMapper;
	
	@Override
	public Integer countRealNumByBetween(Integer startId, Integer endId, String tableName) {
		if(startId == null || endId == null || !StringUtils.hasText(tableName)){
			return 0;
		}
		return sourceTableMapper.countRealNumByBetween(startId, endId, tableName);
	}

}
