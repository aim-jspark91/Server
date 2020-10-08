package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AutoMQCSetting extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "2", name="ecCode", type="Key", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "3", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "4", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "5", name="mqcTemplateName", type="Key", dataType="String", initial="", history="")
	private String mqcTemplateName;
	
	@CTORMTemplate(seq = "6", name="mqcType", type="Key", dataType="String", initial="", history="")
	private String mqcType;
	
	@CTORMTemplate(seq = "7", name="mqcValue", type="Column", dataType="Number", initial="", history="")
	private double mqcValue;
	
	@CTORMTemplate(seq = "8", name="mqcValidFlag", type="Column", dataType="String", initial="", history="")
	private String mqcValidFlag;
	
	@CTORMTemplate(seq = "9", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "10", name="lastRunTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastRunTime;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "12", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public AutoMQCSetting()
	{
		
	}
	public AutoMQCSetting(String productSpecName,String ecCode,String processOperationName,String machineName,String mqcTemplateName,String mqcType)
	{
		this.setProductSpecName(productSpecName);
		this.setEcCode(ecCode);
		this.setProcessOperationName(processOperationName);
		this.setMachineName(machineName);
		this.setMqcTemplateName(mqcTemplateName);
		this.setMqcType(mqcType);
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getEcCode() {
		return ecCode;
	}
	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}
	public String getProcessOperationName() {
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getMqcTemplateName() {
		return mqcTemplateName;
	}
	public void setMqcTemplateName(String mqcTemplateName) {
		this.mqcTemplateName = mqcTemplateName;
	}
	public String getMqcType() {
		return mqcType;
	}
	public void setMqcType(String mqcType) {
		this.mqcType = mqcType;
	}
	public double getMqcValue() {
		return mqcValue;
	}
	public void setMqcValue(double mqcValue) {
		this.mqcValue = mqcValue;
	}
	public String getMqcValidFlag() {
		return mqcValidFlag;
	}
	public void setMqcValidFlag(String mqcValidFlag) {
		this.mqcValidFlag = mqcValidFlag;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public Timestamp getLastRunTime() {
		return lastRunTime;
	}
	public void setLastRunTime(Timestamp lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	

}
