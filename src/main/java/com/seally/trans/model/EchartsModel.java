package com.seally.trans.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dnc
 * @since 2017年12月1日 上午12:05:51
 * Echarts数据模型
 */
public class EchartsModel {
	
	private List<String> yAxisData = new ArrayList<>();//任务名称集合
	
	private List<Integer> seriesDataTotal = new ArrayList<>();//总数集合
	
	private List<Integer> seriesDataSuccess = new ArrayList<>();//成功集合
	
	private List<Integer> seriesDataFail = new ArrayList<>();//失败集合

	public List<String> getyAxisData() {
		return yAxisData;
	}

	public void putyAxisData(String yAxisData) {
		this.yAxisData.add(yAxisData);
	}

	public List<Integer> getSeriesDataTotal() {
		return seriesDataTotal;
	}

	public void putSeriesDataTotal(Integer seriesDataTotal) {
		this.seriesDataTotal.add(seriesDataTotal);
	}

	public List<Integer> getSeriesDataSuccess() {
		return seriesDataSuccess;
	}

	public void putSeriesDataSuccess(Integer seriesDataSuccess) {
		this.seriesDataSuccess.add(seriesDataSuccess);
	}

	public List<Integer> getSeriesDataFail() {
		return seriesDataFail;
	}

	public void putSeriesDataFail(Integer seriesDataFail) {
		this.seriesDataFail.add(seriesDataFail);
	}
}
