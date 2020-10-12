package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeIdleTimeLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "3", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "4", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "5", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "6", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	@CTORMTemplate(seq = "7", name="lastRunTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastRunTime;
	
	@CTORMTemplate(seq = "8", name="firstLotFlag", type="Column", dataType="String", initial="", history="")
	private String firstLotFlag;
	
	@CTORMTemplate(seq = "9", name="firstCstID", type="Column", dataType="String", initial="", history="")
	private String firstCstID;
	
	@CTORMTemplate(seq = "10", name="firstLotID", type="Column", dataType="String", initial="", history="")
	private String firstLotID;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "12", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	
	public String getmachineName() {
		return machineName;
	}
	public void setmachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getrecipeName() {
		return recipeName;
	}
	public void setrecipeName(String recipeName) {
		this.recipeName = recipeName;
	}
	
	public String getproductSpecName() {
		return productSpecName;
	}
	public void setproductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	
	public String getprocessOperationName() {
		return processOperationName;
	}
	public void setprocessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	
	public String getfactoryName() {
		return factoryName;
	}
	public void setfactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getareaName() {
		return areaName;
	}
	public void setareaName(String areaName) {
		this.areaName = areaName;
	}
	
	public Timestamp getlastRunTime() {
		return lastRunTime;
	}
	public void setlastRunTime(Timestamp lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	
	public String getfirstLotFlag() {
		return firstLotFlag;
	}
	public void setfirstLotFlag(String firstLotFlag) {
		this.firstLotFlag = firstLotFlag;
	}
	
	public String getfirstLotID() {
		return firstLotID;
	}
	public void setfirstLotID(String firstLotID) {
		this.firstLotID = firstLotID;
	}
	
	public String getfirstCstID() {
		return firstCstID;
	}
	public void setfirstCstID(String firstCstID) {
		this.firstCstID = firstCstID;
	}
	
	public String getlastEventUser() {
		return lastEventUser;
	}
	public void setlastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	public Timestamp getlastEventTime() {
		return lastEventTime;
	}
	public void setlastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	
	public String getlastEventTimekey() {
		return lastEventTimekey;
	}
	public void setlastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	
	public String getlastEventName() {
		return lastEventName;
	}
	public void setlastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getlastEventComment() {
		return lastEventComment;
	}
	public void setlastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	//instantiation
	public RecipeIdleTimeLot()
	{
		
	}
	
	public RecipeIdleTimeLot(String machineName, String recipeName)
	{
		setmachineName(machineName);
		setrecipeName(recipeName);
	}
}
