package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReworkProduct extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "2", name="lotname", type="Key", dataType="String", initial="", history="")
	private String lotname;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name="processflowName", type="Key", dataType="String", initial="", history="")
	private String processflowName;
	
	@CTORMTemplate(seq = "5", name="processflowVersion", type="Key", dataType="String", initial="", history="")
	private String processflowVersion;
	
	@CTORMTemplate(seq = "6", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "7", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "8", name="reworkProcessflowName", type="Key", dataType="String", initial="", history="")
	private String reworkProcessflowName;
	
	@CTORMTemplate(seq = "9", name="reworkProcessflowVersion", type="Key", dataType="String", initial="", history="")
	private String reworkProcessflowVersion;
	
	@CTORMTemplate(seq = "10", name="reworkProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String reworkProcessOperationName;

	@CTORMTemplate(seq = "11", name="reworkProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String reworkProcessOperationVersion;

	@CTORMTemplate(seq = "12", name="reworkCount", type="Column", dataType="Number", initial="", history="")
	private long reworkCount;

	@CTORMTemplate(seq = "13", name="currentCount", type="Column", dataType="Number", initial="", history="")
	private long currentCount;

	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "16", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "17", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "18", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "19", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "20", name="reworkDepartmentName", type="Column", dataType="String", initial="", history="")
	private String reworkDepartmentName;
	
	@CTORMTemplate(seq = "21", name="originalProductGrade", type="Column", dataType="String", initial="", history="")
	private String originalProductGrade;


	public String getProductName() {
		return productName;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReworkDepartmentName() {
		return reworkDepartmentName;
	}

	public void setReworkDepartmentName(String reworkDepartmentName) {
		this.reworkDepartmentName = reworkDepartmentName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLotname() {
		return lotname;
	}

	public void setLotname(String lotname) {
		this.lotname = lotname;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProcessflowName() {
		return processflowName;
	}

	public void setProcessflowName(String processflowName) {
		this.processflowName = processflowName;
	}

	public String getProcessflowVersion() {
		return processflowVersion;
	}

	public void setProcessflowVersion(String processflowVersion) {
		this.processflowVersion = processflowVersion;
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

	public String getReworkProcessflowName() {
		return reworkProcessflowName;
	}

	public void setReworkProcessflowName(String reworkProcessflowName) {
		this.reworkProcessflowName = reworkProcessflowName;
	}

	public String getReworkProcessflowVersion() {
		return reworkProcessflowVersion;
	}

	public void setReworkProcessflowVersion(String reworkProcessflowVersion) {
		this.reworkProcessflowVersion = reworkProcessflowVersion;
	}

	public String getReworkProcessOperationName() {
		return reworkProcessOperationName;
	}

	public void setReworkProcessOperationName(String reworkProcessOperationName) {
		this.reworkProcessOperationName = reworkProcessOperationName;
	}

	public String getReworkProcessOperationVersion() {
		return reworkProcessOperationVersion;
	}

	public void setReworkProcessOperationVersion(
			String reworkProcessOperationVersion) {
		this.reworkProcessOperationVersion = reworkProcessOperationVersion;
	}

	public long getReworkCount() {
		return reworkCount;
	}

	public void setReworkCount(long reworkCount) {
		this.reworkCount = reworkCount;
	}

	public long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(long currentCount) {
		this.currentCount = currentCount;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
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

	public String getOriginalProductGrade() {
		return originalProductGrade;
	}

	public void setOriginalProductGrade(String originalProductGrade) {
		this.originalProductGrade = originalProductGrade;
	}	
}
