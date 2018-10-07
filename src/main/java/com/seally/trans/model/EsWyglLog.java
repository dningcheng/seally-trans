package com.seally.trans.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.seally.trans.annotation.EsDocument;
import com.seally.trans.annotation.EsField;

//import org.apache.log4j.Logger;

@EsDocument(index="wyglsystemlog",type="pblog")
public class EsWyglLog implements Serializable {

	//protected static Logger logger = Logger.getLogger(EsWyglLog.class);
	 
	private static final long serialVersionUID = -5785242575346145247L;
	
	@EsField
	private Integer orgId; // 组织ID
	
	@EsField
	private Integer userId; // 当前用户登录ID
	
	@EsField
	private Integer unitId; // 小区ID
	
	@EsField
	private String moduleCode; // 模块编码
	
	@EsField
	private String apiCode; // 接口编码
	
	@EsField
	private String userAccount; // 用户名
	
	@EsField
	private String unitName; // 小区名称
	
	@EsField
	private String opMethod; // 操作方法
	
	@EsField
	private String opContent; // 操作内容
	
	@EsField
	private String opResult; // 操作状态
	
	@EsField
	private Date opTime; // 操作时间
	
	@EsField("moduleParkPlate")
	private String plate;
	
	@EsField
	private Integer id;//id
	
	//附加查询参数
	private Date beginDate;
	private Date endDate;
	
	private int currentPage=0;//当前页
	private int pageSize=10;//每页条数
	
	private List<String> lists;//存放string类型的集合条件
	
	private String subStr(String str) {
		if (str != null && str.length() > 255) {
			return str.substring(0, 255) + "...";
		} else {
			return str;
		}
	}

	public EsWyglLog() {
	
	}

	public EsWyglLog(List<String> lists) {
		this.lists=lists;
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

	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<String> getLists() {
		return lists;
	}

	public void setLists(List<String> lists) {
		this.lists = lists;
	}
	
}