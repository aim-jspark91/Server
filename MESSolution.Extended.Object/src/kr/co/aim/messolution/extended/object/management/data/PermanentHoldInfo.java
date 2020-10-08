package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

import org.apache.commons.lang.StringUtils;

public class PermanentHoldInfo extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="lotname", type="Key", dataType="String", initial="", history="")
	private String lotname;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "4", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "5", name="ecCode", type="Key", dataType="String", initial="", history="")
	private String ecCode;

	@CTORMTemplate(seq = "6", name="processflowName", type="Key", dataType="String", initial="", history="")
	private String processflowName;
	
	@CTORMTemplate(seq = "7", name="processflowVersion", type="Key", dataType="String", initial="", history="")
	private String processflowVersion;
	
	@CTORMTemplate(seq = "8", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "9", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "10", name="position", type="Key", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "11", name="actionState", type="Column", dataType="String", initial="", history="")
	private String actionState;
	
	@CTORMTemplate(seq = "12", name="description", type="Column", dataType="String", initial="", history="")
	private String description;

	@CTORMTemplate(seq = "13", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;

	@CTORMTemplate(seq = "14", name="departmentName", type="Column", dataType="String", initial="", history="")
	private String departmentName;

	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "17", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "18", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "19", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

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

	public String getEcCode() {
		return ecCode;
	}

	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
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

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getActionState() {
		return actionState;
	}

	public void setActionState(String actionState) {
		this.actionState = actionState;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
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
		this.lastEventComment = StringUtils.isNotEmpty(lastEventComment) ? lastEventComment.trim() : "";
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
	

	
}
