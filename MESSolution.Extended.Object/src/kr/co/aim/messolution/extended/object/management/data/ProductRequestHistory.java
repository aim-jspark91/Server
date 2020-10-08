package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductRequestHistory extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;

	@CTORMTemplate(seq = "2", name="timekey", type="Key", dataType="String", initial="", history="")
	private String timekey;
	
	@CTORMTemplate(seq = "3", name="productRequestType", type="Column", dataType="String", initial="", history="")
	private String productRequestType;
	
	@CTORMTemplate(seq = "4", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name="planReleasedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planReleasedTime;
	
	@CTORMTemplate(seq = "7", name="planFinishedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planFinishedTime;
	
	@CTORMTemplate(seq = "8", name="planQuantity", type="Column", dataType="long", initial="", history="")
	private long planQuantity;
	
	@CTORMTemplate(seq = "9", name="releasedQuantity", type="Column", dataType="long", initial="", history="")
	private long releasedQuantity;
	
	@CTORMTemplate(seq = "10", name="finishedQuantity", type="Column", dataType="long", initial="", history="")
	private long finishedQuantity;
	
	@CTORMTemplate(seq = "11", name="scrappedQuantity", type="Column", dataType="long", initial="", history="")
	private long scrappedQuantity;
	
	@CTORMTemplate(seq = "12", name="productRequestState", type="Column", dataType="String", initial="", history="")
	private String productRequestState;
	
	@CTORMTemplate(seq = "13", name="productRequestHoldState", type="Column", dataType="String", initial="", history="")
	private String productRequestHoldState;
	
	@CTORMTemplate(seq = "14", name="eventName", type="Column", dataType="String", initial="", history="")
	private String eventName;
	
	@CTORMTemplate(seq = "15", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "16", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "17", name="eventComment", type="Column", dataType="String", initial="", history="")
	private String eventComment;
	
	@CTORMTemplate(seq = "18", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "19", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "20", name="releaseTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp releaseTime;
	
	@CTORMTemplate(seq = "21", name="releaseUser", type="Column", dataType="String", initial="", history="")
	private String releaseUser;
	
	@CTORMTemplate(seq = "22", name="completeTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp completeTime;
	
	@CTORMTemplate(seq = "23", name="completeUser", type="Column", dataType="String", initial="", history="")
	private String completeUser;
	
	@CTORMTemplate(seq = "24", name="crateSpecName", type="Column", dataType="String", initial="", history="")
	private String crateSpecName;
	
	@CTORMTemplate(seq = "25", name="changeInQuantity", type="Column", dataType="long", initial="", history="")
	private String changeInQuantity;

	@CTORMTemplate(seq = "26", name="changeOutQuantity", type="Column", dataType="long", initial="", history="")
	private String changeOutQuantity;
	
	@CTORMTemplate(seq = "27", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "28", name="unit", type="Column", dataType="String", initial="", history="")
	private String unit;
	
	@CTORMTemplate(seq = "29", name="sales_Order", type="Column", dataType="String", initial="", history="")
	private String sales_Order;
	
	@CTORMTemplate(seq = "30", name="eng_Name", type="Column", dataType="String", initial="", history="")
	private String eng_Name;
	
	@CTORMTemplate(seq = "31", name="exp_No", type="Column", dataType="String", initial="", history="")
	private String exp_No;
	
	@CTORMTemplate(seq = "32", name="pln_Sht_Cnt", type="Column", dataType="long", initial="", history="")
	private long pln_Sht_Cnt;
	
	@CTORMTemplate(seq = "33", name="clm_User", type="Column", dataType="String", initial="", history="")
	private String clm_User;
	
	@CTORMTemplate(seq = "34", name="pln_Stb_Date", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp pln_Stb_Date;
	
	@CTORMTemplate(seq = "35", name="self_Kind_Name1", type="Column", dataType="String", initial="", history="")
	private String self_Kind_Name1;
	
	@CTORMTemplate(seq = "36", name="self_Kind_Name2", type="Column", dataType="String", initial="", history="")
	private String self_Kind_Name2;
	
	@CTORMTemplate(seq = "37", name="self_Kind_Name3", type="Column", dataType="String", initial="", history="")
	private String self_Kind_Name3;
	
	@CTORMTemplate(seq = "38", name="self_Kind_Name4", type="Column", dataType="String", initial="", history="")
	private String self_Kind_Name4;
	
	@CTORMTemplate(seq = "39", name="self_Kind_Name5", type="Column", dataType="String", initial="", history="")
	private String self_Kind_Name5;
	
	@CTORMTemplate(seq = "40", name="cust_Prod_Id", type="Column", dataType="String", initial="", history="")
	private String cust_Prod_Id;
	
	@CTORMTemplate(seq = "41", name="cust_Name", type="Column", dataType="String", initial="", history="")
	private String cust_Name;
	
	@CTORMTemplate(seq = "42", name="grade", type="Column", dataType="String", initial="", history="")
	private String grade;
	
	@CTORMTemplate(seq = "43", name="makt_Maktx", type="Column", dataType="String", initial="", history="")
	private String makt_Maktx;
	
	@CTORMTemplate(seq = "44", name="mkal_Text1", type="Column", dataType="String", initial="", history="")
	private String mkal_Text1;
	
	@CTORMTemplate(seq = "45", name="afpo_Dwerk", type="Column", dataType="String", initial="", history="")
	private String afpo_Dwerk;
	
	@CTORMTemplate(seq = "46", name="knmt_Postx", type="Column", dataType="String", initial="", history="")
	private String knmt_Postx;
	
	@CTORMTemplate(seq = "47", name="vbkd_Bstkd", type="Column", dataType="String", initial="", history="")
	private String vbkd_Bstkd;
	
	@CTORMTemplate(seq = "48", name="vbak_Kunnr", type="Column", dataType="String", initial="", history="")
	private String vbak_Kunnr;
	
	@CTORMTemplate(seq = "49", name="endBank", type="Column", dataType="String", initial="", history="")
	private String endBank;
	

	//instantiation
	public ProductRequestHistory()
	{
		
	}
	
	public ProductRequestHistory(String productRequestName, String timekey)
	{
		setProductRequestName(productRequestName);
		setTimekey(timekey);
	}
	

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getTimekey() {
		return timekey;
	}

	public void setTimekey(String timekey) {
		this.timekey = timekey;
	}

	public String getProductRequestType() {
		return productRequestType;
	}

	public void setProductRequestType(String productRequestType) {
		this.productRequestType = productRequestType;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public Timestamp getPlanReleasedTime() {
		return planReleasedTime;
	}

	public void setPlanReleasedTime(Timestamp planReleasedTime) {
		this.planReleasedTime = planReleasedTime;
	}

	public Timestamp getPlanFinishedTime() {
		return planFinishedTime;
	}

	public void setPlanFinishedTime(Timestamp planFinishedTime) {
		this.planFinishedTime = planFinishedTime;
	}

	public long getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		this.planQuantity = planQuantity;
	}

	public long getReleasedQuantity() {
		return releasedQuantity;
	}

	public void setReleasedQuantity(long releasedQuantity) {
		this.releasedQuantity = releasedQuantity;
	}

	public long getFinishedQuantity() {
		return finishedQuantity;
	}

	public void setFinishedQuantity(long finishedQuantity) {
		this.finishedQuantity = finishedQuantity;
	}

	public long getScrappedQuantity() {
		return scrappedQuantity;
	}

	public void setScrappedQuantity(long scrappedQuantity) {
		this.scrappedQuantity = scrappedQuantity;
	}

	public String getProductRequestState() {
		return productRequestState;
	}

	public void setProductRequestState(String productRequestState) {
		this.productRequestState = productRequestState;
	}

	public String getProductRequestHoldState() {
		return productRequestHoldState;
	}

	public void setProductRequestHoldState(String productRequestHoldState) {
		this.productRequestHoldState = productRequestHoldState;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public String getEventComment() {
		return eventComment;
	}

	public void setEventComment(String eventComment) {
		this.eventComment = eventComment;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(Timestamp releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getReleaseUser() {
		return releaseUser;
	}

	public void setReleaseUser(String releaseUser) {
		this.releaseUser = releaseUser;
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Timestamp completeTime) {
		this.completeTime = completeTime;
	}

	public String getCompleteUser() {
		return completeUser;
	}

	public void setCompleteUser(String completeUser) {
		this.completeUser = completeUser;
	}
	
	public String getCrateSpecName() {
		return crateSpecName;
	}

	public void setCrateSpecName(String crateSpecName) {
		this.crateSpecName = crateSpecName;
	}

	public String getChangeInQuantity() {
		return changeInQuantity;
	}

	public void setChangeInQuantity(String changeInQuantity) {
		this.changeInQuantity = changeInQuantity;
	}

	public String getChangeOutQuantity() {
		return changeOutQuantity;
	}

	public void setChangeOutQuantity(String changeOutQuantity) {
		this.changeOutQuantity = changeOutQuantity;
	}
	
	public String getdescription() {
		return description;
	}

	public void setdescription(String description) {
		this.description = description;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSales_Order() {
		return sales_Order;
	}

	public void setSales_Order(String sales_Order) {
		this.sales_Order = sales_Order;
	}

	public String getEng_Name() {
		return eng_Name;
	}

	public void setEng_Name(String eng_Name) {
		this.eng_Name = eng_Name;
	}

	public String getExp_No() {
		return exp_No;
	}

	public void setExp_No(String exp_No) {
		this.exp_No = exp_No;
	}

	public long getPln_Sht_Cnt() {
		return pln_Sht_Cnt;
	}

	public void setPln_Sht_Cnt(long pln_Sht_Cnt) {
		this.pln_Sht_Cnt = pln_Sht_Cnt;
	}

	public String getClm_User() {
		return clm_User;
	}

	public void setClm_User(String clm_User) {
		this.clm_User = clm_User;
	}

	public Timestamp getPln_Stb_Date() {
		return pln_Stb_Date;
	}

	public void setPln_Stb_Date(Timestamp pln_Stb_Date) {
		this.pln_Stb_Date = pln_Stb_Date;
	}

	public String getSelf_Kind_Name1() {
		return self_Kind_Name1;
	}

	public void setSelf_Kind_Name1(String self_Kind_Name1) {
		this.self_Kind_Name1 = self_Kind_Name1;
	}

	public String getSelf_Kind_Name2() {
		return self_Kind_Name2;
	}

	public void setSelf_Kind_Name2(String self_Kind_Name2) {
		this.self_Kind_Name2 = self_Kind_Name2;
	}

	public String getSelf_Kind_Name3() {
		return self_Kind_Name3;
	}

	public void setSelf_Kind_Name3(String self_Kind_Name3) {
		this.self_Kind_Name3 = self_Kind_Name3;
	}

	public String getSelf_Kind_Name4() {
		return self_Kind_Name4;
	}

	public void setSelf_Kind_Name4(String self_Kind_Name4) {
		this.self_Kind_Name4 = self_Kind_Name4;
	}

	public String getSelf_Kind_Name5() {
		return self_Kind_Name5;
	}

	public void setSelf_Kind_Name5(String self_Kind_Name5) {
		this.self_Kind_Name5 = self_Kind_Name5;
	}

	public String getCust_Prod_Id() {
		return cust_Prod_Id;
	}

	public void setCust_Prod_Id(String cust_Prod_Id) {
		this.cust_Prod_Id = cust_Prod_Id;
	}

	public String getCust_Name() {
		return cust_Name;
	}

	public void setCust_Name(String cust_Name) {
		this.cust_Name = cust_Name;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getMakt_Maktx() {
		return makt_Maktx;
	}

	public void setMakt_Maktx(String makt_Maktx) {
		this.makt_Maktx = makt_Maktx;
	}

	public String getMkal_Text1() {
		return mkal_Text1;
	}

	public void setMkal_Text1(String mkal_Text1) {
		this.mkal_Text1 = mkal_Text1;
	}

	public String getAfpo_Dwerk() {
		return afpo_Dwerk;
	}

	public void setAfpo_Dwerk(String afpo_Dwerk) {
		this.afpo_Dwerk = afpo_Dwerk;
	}

	public String getKnmt_Postx() {
		return knmt_Postx;
	}

	public void setKnmt_Postx(String knmt_Postx) {
		this.knmt_Postx = knmt_Postx;
	}

	public String getVbkd_Bstkd() {
		return vbkd_Bstkd;
	}

	public void setVbkd_Bstkd(String vbkd_Bstkd) {
		this.vbkd_Bstkd = vbkd_Bstkd;
	}

	public String getVbak_Kunnr() {
		return vbak_Kunnr;
	}

	public void setVbak_Kunnr(String vbak_Kunnr) {
		this.vbak_Kunnr = vbak_Kunnr;
	}

	public String getEndBank() {
		return endBank;
	}

	public void setEndBank(String endBank) {
		this.endBank = endBank;
	}
	
}
