package com.seally.trans.model;

import java.util.Date;

import com.seally.trans.annotation.EsDocument;
import com.seally.trans.annotation.EsField;

/**
 * @author dnc
 * @since 2017年11月18日 下午10:36:35
 * 系统操作日志模型
 */
@EsDocument(index="logindex",type="systemlog")
public class SystemLog {
	@EsField
	private Integer logId;       	 //主键ID
	@EsField
	private Integer orgId;    		 //机构ID
	@EsField
	private Integer userId;   		 //用户ID
	@EsField
	private Integer unitId;   		 //小区ID
	@EsField
	private String moduleCode;		 //模块描述
	@EsField
	private String apiCode;   		 //接口描述
	@EsField
	private String userAccount;		 //用户账号
	@EsField
	private String unitName;  		 //小区名称
	@EsField
	private String opMethod;   		 //操作方法
	@EsField
	private String opContent;  		 //操作内容
	@EsField
	private String opResult;         //操作结果
	@EsField
	private Date opTime;             //操作时间
	@EsField
	private String moduleParkPlate;  //模块车场附属车牌号
	
	public Integer getLogId() {
		return logId;
	}
	public void setLogId(Integer logId) {
		this.logId = logId;
	}
	public Integer getOrgId() {
		return orgId;
	}
	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getUnitId() {
		return unitId;
	}
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}
	public String getModuleCode() {
		return moduleCode;
	}
	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}
	public String getApiCode() {
		return apiCode;
	}
	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getOpMethod() {
		return opMethod;
	}
	public void setOpMethod(String opMethod) {
		this.opMethod = opMethod;
	}
	public String getOpContent() {
		return opContent;
	}
	public void setOpContent(String opContent) {
		this.opContent = opContent;
	}
	public String getOpResult() {
		return opResult;
	}
	public void setOpResult(String opResult) {
		this.opResult = opResult;
	}
	public Date getOpTime() {
		return opTime;
	}
	public void setOpTime(Date opTime) {
		this.opTime = opTime;
	}
	public String getModuleParkPlate() {
		return moduleParkPlate;
	}
	public void setModuleParkPlate(String moduleParkPlate) {
		this.moduleParkPlate = moduleParkPlate;
	}
	
}
