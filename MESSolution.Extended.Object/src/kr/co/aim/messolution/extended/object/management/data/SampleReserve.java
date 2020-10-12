package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleReserve extends UdfAccessor {
	
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
	
	@CTORMTemplate(seq = "9", name = "decidedMachineName", type = "Column", dataType = "String", initial = "", history = "")
	private String decidedMachineName;
	
	@CTORMTemplate(seq = "10", name = "decidedProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String decidedProcessOperationName;
	
	@CTORMTemplate(seq = "11", name = "sampleProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowName;
	
	@CTORMTemplate(seq = "12", name = "sampleProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "13", name = "samplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String samplePosition;
	
	@CTORMTemplate(seq = "14", name = "samplePriority", type = "Column", dataType = "Long", initial = "", history = "")
	private long samplePriority;
	
	@CTORMTemplate(seq = "15", name = "sampleOutHoldFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleOutHoldFlag;
	
	@CTORMTemplate(seq = "16", name = "actualSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSamplePosition;

	@CTORMTemplate(seq = "17", name = "manualSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSampleFlag;
	
	@CTORMTemplate(seq = "18", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "19", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "20", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "21", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "22", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "23", name = "reasonCodeType", type = "Column", dataType = "String", initial = "", history = "N")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "24", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "N")
	private String reasonCode;
	
	@CTORMTemplate(seq = "25", name = "sampleDepartmentName", type = "Column", dataType = "String", initial = "", history = "N")
	private String sampleDepartmentName; 
	
	public SampleReserve()
	{
		
	}

	public SampleReserve(String lotName, String factoryName,
			String productSpecName, String ecCode, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion) {
		super();
		this.lotName = lotName;
		this.factoryName = factoryName;
		this.productSpecName = productSpecName;
		this.ecCode = ecCode;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
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

	public String getDecidedMachineName() {
		return decidedMachineName;
	}

	public void setDecidedMachineName(String decidedMachineName) {
		this.decidedMachineName = decidedMachineName;
	}

	public String getDecidedProcessOperationName() {
		return decidedProcessOperationName;
	}

	public void setDecidedProcessOperationName(String decidedProcessOperationName) {
		this.decidedProcessOperationName = decidedProcessOperationName;
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

	public String getSamplePosition() {
		return samplePosition;
	}

	public void setSamplePosition(String samplePosition) {
		this.samplePosition = samplePosition;
	}

	public long getSamplePriority() {
		return samplePriority;
	}

	public void setSamplePriority(long samplePriority) {
		this.samplePriority = samplePriority;
	}

	public String getSampleOutHoldFlag() {
		return sampleOutHoldFlag;
	}

	public void setSampleOutHoldFlag(String sampleOutHoldFlag) {
		this.sampleOutHoldFlag = sampleOutHoldFlag;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}
	
	public String getReasonCodeType() {
		return reasonCodeType;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	
	public String getReasonCode() {
		return reasonCode;
	}
	
	public void setSampleDepartmentName(String sampleDepartmentName) {
		this.sampleDepartmentName = sampleDepartmentName;
	}
	
	public String getSampleDepartmentName() {
		return sampleDepartmentName;
	}
}
