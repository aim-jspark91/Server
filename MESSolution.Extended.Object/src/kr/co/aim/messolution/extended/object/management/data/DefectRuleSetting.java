package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DefectRuleSetting extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="FactoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name="ProductSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "4", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "5", name="ProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "6", name="DefectCode", type="Key", dataType="String", initial="", history="")
	private String defectCode;
	
	@CTORMTemplate(seq = "7", name="DefectSize", type="Column", dataType="Number", initial="", history="")
	private double defectSize;
	
	@CTORMTemplate(seq = "8", name="DefectCount", type="Column", dataType="Number", initial="", history="")
	private double defectCount;
	
	@CTORMTemplate(seq = "9", name="holdFlag", type="Column", dataType="String", initial="", history="")
	private String holdFlag;
	
	@CTORMTemplate(seq = "10", name="mailFlag", type="Column", dataType="String", initial="", history="")
	private String mailFlag;
	
	@CTORMTemplate(seq = "11", name="userGroupName", type="Column", dataType="String", initial="", history="")
	private String userGroupName;
	
	@CTORMTemplate(seq = "12", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "14", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "15", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public DefectRuleSetting()
	{
		
	}
	public DefectRuleSetting(String factoryName,String productSpecName,String productSpecVersion,String processOperationName,String processOperationVersion,String defectCode)
	{
		this.setFactoryName(factoryName);
		this.setProductSpecName(productSpecName);
		this.setProductSpecVersion(productSpecVersion);
		this.setProcessOperationName(processOperationName);
		this.setProcessOperationVersion(processOperationVersion);
		this.setDefectCode(defectCode);
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


	public String getProductSpecVersion() {
		return productSpecVersion;
	}


	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}


	public String getProcessOperationName() {
		return processOperationName;
	}


	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}


	public String getProcessOperationVersion() {
		return processOperationVersion;
	}


	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}


	public String getDefectCode() {
		return defectCode;
	}


	public void setDefectCode(String defectCode) {
		this.defectCode = defectCode;
	}


	public double getDefectSize() {
		return defectSize;
	}


	public void setDefectSize(double defectSize) {
		this.defectSize = defectSize;
	}


	public double getDefectCount() {
		return defectCount;
	}


	public void setDefectCount(double defectCount) {
		this.defectCount = defectCount;
	}


	
	
	
	public String getHoldFlag() {
		return holdFlag;
	}
	public void setHoldFlag(String holdFlag) {
		this.holdFlag = holdFlag;
	}
	public String getMailFlag() {
		return mailFlag;
	}
	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}
	public String getUserGroupName() {
		return userGroupName;
	}
	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
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
