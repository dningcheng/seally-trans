package com.seally.trans.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seally.trans.mapper.TranslogMapper;
import com.seally.trans.model.EchartsModel;
import com.seally.trans.model.Translog;
import com.seally.trans.service.TranslogService;

/**
*
*@author dnc
*@version 创建时间：2017年11月30日 上午11:18:26 
*
*
*/
@Service
public class TranslogServiceImpl implements TranslogService {
	
	@Autowired
	TranslogMapper translogMapper;

	@Override
	public Integer addTranslog(Translog log) {
		return translogMapper.addTranslog(log);
	}

	@Override
	public Integer deleteTranslog(Translog log) {
		return translogMapper.deleteTranslog(log);
	}

	@Override
	public Integer updateTranslogById(Translog log) {
		return translogMapper.updateTranslogById(log);
	}

	@Override
	public Integer updateTranslogByJobName(Translog log) {
		return translogMapper.updateTranslogByJobName(log);
	}
	
	@Override
	public Translog getTranslogById(Integer id) {
		return translogMapper.getTranslogById(id);
	}

	@Override
	public List<Translog> getTranslogList(Translog log) {
		return translogMapper.getTranslogList(log);
	}

	/**
	 * @author dnc
	 * @since 2017年12月1日 上午6:12:16
	 * 获取当前转移结果存储为Echarts报表需要刷新的局部数据
	 */
	@Override
	public EchartsModel getCurTransEchartsOption() {
		
		EchartsModel optionData = new EchartsModel();
		
		List<Translog> logs = getTranslogList(null);
		if(logs == null || logs.size() == 0){
			return optionData;
		}
		
		logs.forEach(log -> {
			optionData.putyAxisData(log.getTransName());
			optionData.putSeriesDataTotal(log.getAllCount());
			optionData.putSeriesDataSuccess(log.getSucCount());
			optionData.putSeriesDataFail(-(log.getAllCount()-log.getSucCount()));
		});
		
		return optionData;
	}

	@Override
	public int clearTranslog() {
		return translogMapper.clearTranslog();
	}
	
}
