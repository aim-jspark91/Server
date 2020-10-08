package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

import org.apache.commons.lang.StringUtils;

public class OperAction extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "4", name="ecCode", type="Key", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "5", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "6", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "7", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "8", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "9", name="position", type="Key", dataType="Long", initial="", history="")
	private long position;
		
	@CTORMTemplate(seq = "10", name="actionName", type="Column", dataType="String", initial="", history="")
	private String actionName;
	
	@CTORMTemplate(seq = "11", name="holdCode", type="Column", dataType="String", initial="", history="")
	private String holdCode;
	
	@CTORMTemplate(seq = "12", name="holdType", type="Column", dataType="String", initial="", history="")
	private String holdType;
	
	@CTORMTemplate(seq = "13", name="changeProductRequestName", type="Column", dataType="String", initial="", history="")
	private String changeProductRequestName;
	
	@CTORMTemplate(seq = "14", name="changeProductSpecName", type="Column", dataType="String", initial="", history="")
	private String changeProductSpecName;
	
	@CTORMTemplate(seq = "15", name="changeProductSpecVersion", type="Column", dataType="String", initial="", history="")
	private String changeProductSpecVersion;
	
	@CTORMTemplate(seq = "16", name="changeECCode", type="Column", dataType="String", initial="", history="")
	private String changeECCode;
	
	@CTORMTemplate(seq = "17", name="changeProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String changeProcessFlowName;
	
	@CTORMTemplate(seq = "18", name="changeProcessFlowVersion", type="Column", dataType="String", initial="", history="")
	private String changeProcessFlowVersion;
	
	@CTORMTemplate(seq = "19", name="changeProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String changeProcessOperationName;
	
	@CTORMTemplate(seq = "20", name="changeProcessOperationVersion", type="Column", dataType="String", initial="", history="")
	private String changeProcessOperationVersion;
	
	@CTORMTemplate(seq = "21", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "22", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "23", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "24", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "25", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "26", name="departmentName", type="Column", dataType="String", initial="", history="")
	private String departmentName;


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

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion() {
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
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

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getHoldCode() {
		return holdCode;
	}

	public void setHoldCode(String holdCode) {
		this.holdCode = holdCode;
	}

	public String getHoldType() {
		return holdType;
	}

	public void setHoldType(String holdType) {
		this.holdType = holdType;
	}

	public String getChangeProductRequestName() {
		return changeProductRequestName;
	}

	public void setChangeProductRequestName(String changeProductRequestName) {
		this.changeProductRequestName = changeProductRequestName;
	}

	public String getChangeProductSpecName() {
		return changeProductSpecName;
	}

	public void setChangeProductSpecName(String changeProductSpecName) {
		this.changeProductSpecName = changeProductSpecName;
	}

	public String getChangeProductSpecVersion() {
		return changeProductSpecVersion;
	}

	public void setChangeProductSpecVersion(String changeProductSpecVersion) {
		this.changeProductSpecVersion = changeProductSpecVersion;
	}

	public String getChangeECCode() {
		return changeECCode;
	}

	public void setChangeECCode(String changeECCode) {
		this.changeECCode = changeECCode;
	}

	public String getChangeProcessFlowName() {
		return changeProcessFlowName;
	}

	public void setChangeProcessFlowName(String changeProcessFlowName) {
		this.changeProcessFlowName = changeProcessFlowName;
	}

	public String getChangeProcessFlowVersion() {
		return changeProcessFlowVersion;
	}

	public void setChangeProcessFlowVersion(String changeProcessFlowVersion) {
		this.changeProcessFlowVersion = changeProcessFlowVersion;
	}

	public String getChangeProcessOperationName() {
		return changeProcessOperationName;
	}

	public void setChangeProcessOperationName(String changeProcessOperationName) {
		this.changeProcessOperationName = changeProcessOperationName;
	}

	public String getChangeProcessOperationVersion() {
		return changeProcessOperationVersion;
	}

	public void setChangeProcessOperationVersion(
			String changeProcessOperationVersion) {
		this.changeProcessOperationVersion = changeProcessOperationVersion;
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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public OperAction()
	{
		
	}

	public OperAction(String factoryName, String productSpecName,
			String productSpecVersion, String ecCode, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, long position) {
		super();
		this.factoryName = factoryName;
		this.productSpecName = productSpecName;
		this.productSpecVersion = productSpecVersion;
		this.ecCode = ecCode;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.position = position;
	}
	
}
