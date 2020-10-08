package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class CorresSampleLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "4", name = "ecCode", type = "Key", dataType = "String", initial = "", history = "")
	private String ecCode;
	
	@CTORMTemplate(seq = "5", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	
	@CTORMTemplate(seq = "6", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "7", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	
	@CTORMTemplate(seq = "8", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "9", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	
	@CTORMTemplate(seq = "10", name = "sampleProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowName;
	
	@CTORMTemplate(seq = "11", name = "sampleProcessFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "12", name = "fromProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String fromProcessOperationName;
	
	@CTORMTemplate(seq = "13", name = "fromProcessOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String fromProcessOperationVersion;
	
	@CTORMTemplate(seq = "14", name = "sampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleFlag;
	
	@CTORMTemplate(seq = "15", name = "sampleCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long sampleCount;
	
	@CTORMTemplate(seq = "16", name = "currentCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long currentCount;
	
	@CTORMTemplate(seq = "17", name = "totalCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long totalCount;
	
	@CTORMTemplate(seq = "18", name = "systemSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String systemSamplePosition;
	
	@CTORMTemplate(seq = "19", name = "manualSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSamplePosition;
	
	@CTORMTemplate(seq = "20", name = "actualSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSamplePosition;

	@CTORMTemplate(seq = "21", name = "manualSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSampleFlag;
	
	@CTORMTemplate(seq = "22", name = "sampleState", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleState;
	
	@CTORMTemplate(seq = "23", name = "sampleOutHoldFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleOutHoldFlag;
	
	@CTORMTemplate(seq = "24", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "25", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "26", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCode;
	
	@CTORMTemplate(seq = "27", name = "reasonCodeType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "28", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "29", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "30", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	public CorresSampleLot()
	{
		
	}

	public CorresSampleLot(String lotName, String factoryName,
			String productSpecName, String ecCode, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, String machineName,
			String sampleProcessFlowName, String sampleProcessFlowVersion) {
		super();
		this.lotName = lotName;
		this.factoryName = factoryName;
		this.productSpecName = productSpecName;
		this.ecCode = ecCode;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.machineName = machineName;
		this.sampleProcessFlowName = sampleProcessFlowName;
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
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

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getSampleProcessFlowName() {
		return sampleProcessFlowName;
	}

	public void setSampleProcessFlowName(String sampleProcessFlowName) {
		this.sampleProcessFlowName = sampleProcessFlowName;
	}

	public String getSampleProcessFlowVersion() {
		return sampleProcessFlowVersion;
	}

	public void setSampleProcessFlowVersion(String sampleProcessFlowVersion) {
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}

	public String getFromProcessOperationName() {
		return fromProcessOperationName;
	}

	public void setFromProcessOperationName(String fromProcessOperationName) {
		this.fromProcessOperationName = fromProcessOperationName;
	}

	public String getFromProcessOperationVersion() {
		return fromProcessOperationVersion;
	}

	public void setFromProcessOperationVersion(String fromProcessOperationVersion) {
		this.fromProcessOperationVersion = fromProcessOperationVersion;
	}

	public String getSampleFlag() {
		return sampleFlag;
	}

	public void setSampleFlag(String sampleFlag) {
		this.sampleFlag = sampleFlag;
	}

	public long getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(long sampleCount) {
		this.sampleCount = sampleCount;
	}

	public long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(long currentCount) {
		this.currentCount = currentCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public String getSystemSamplePosition() {
		return systemSamplePosition;
	}

	public void setSystemSamplePosition(String systemSamplePosition) {
		this.systemSamplePosition = systemSamplePosition;
	}

	public String getManualSamplePosition() {
		return manualSamplePosition;
	}

	public void setManualSamplePosition(String manualSamplePosition) {
		this.manualSamplePosition = manualSamplePosition;
	}

	public String getActualSamplePosition() {
		return actualSamplePosition;
	}

	public void setActualSamplePosition(String actualSamplePosition) {
		this.actualSamplePosition = actualSamplePosition;
	}

	public String getManualSampleFlag() {
		return manualSampleFlag;
	}

	public void setManualSampleFlag(String manualSampleFlag) {
		this.manualSampleFlag = manualSampleFlag;
	}
	
	public String getSampleState() {
		return sampleState;
	}

	public void setSampleState(String sampleState) {
		this.sampleState = sampleState;
	}
	
	public String getSampleOutHoldFlag() {
		return sampleOutHoldFlag;
	}

	public void setSampleOutHoldFlag(String sampleOutHoldFlag) {
		this.sampleOutHoldFlag = sampleOutHoldFlag;
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

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	
	
}
