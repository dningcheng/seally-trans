package com.seally.trans.model;

/**
 * @author dnc
 * 2017年11月30日
 * 转移记录实体
 */
public class Translog {
	
	private Integer id;         //主键
	private String transName;   //转移任务名
	private String transTable;  //转移表名
	private String allBetween;  //该任务转移的id总区间
	private Integer allCount;   //该任务转移的实际存在记录数
	private String noneBetween;  //该任务转移没有实际记录的id区间
	private String sucBetween; //该任务转移成功的id区间
	private Integer sucCount;  //该任务转移成功实际记录数
	
	public Translog(){}
	
	
	public Translog(String transName, String transTable, String allBetween,Integer allCount) {
		this.transName = transName;
		this.transTable = transTable;
		this.allBetween = allBetween;
		this.allCount = allCount;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTransName() {
		return transName;
	}
	public void setTransName(String transName) {
		this.transName = transName;
	}
	public String getTransTable() {
		return transTable;
	}
	public void setTransTable(String transTable) {
		this.transTable = transTable;
	}
	public String getAllBetween() {
		return allBetween;
	}
	public void setAllBetween(String allBetween) {
		this.allBetween = allBetween;
	}
	public Integer getAllCount() {
		return allCount;
	}
	public void setAllCount(Integer allCount) {
		this.allCount = allCount;
	}
	public String getNoneBetween() {
		return noneBetween;
	}
	public void setNoneBetween(String noneBetween) {
		this.noneBetween = noneBetween;
	}
	public String getSucBetween() {
		return sucBetween;
	}
	public void setSucBetween(String sucBetween) {
		this.sucBetween = sucBetween;
	}
	public Integer getSucCount() {
		return sucCount;
	}
	public void setSucCount(Integer sucCount) {
		this.sucCount = sucCount;
	}
	
}
