package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ScrapProduct extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "6", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "7", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "8", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "9", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "10", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;
	
	@CTORMTemplate(seq = "11", name="productGrade", type="Column", dataType="String", initial="", history="")
	private String productGrade;
	
	@CTORMTemplate(seq = "12", name="dueDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp dueDate;
	
	@CTORMTemplate(seq = "13", name="priority", type="Column", dataType="Number", initial="", history="")
	private long priority;

	@CTORMTemplate(seq = "14", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;

	@CTORMTemplate(seq = "15", name="productState", type="Column", dataType="String", initial="", history="")
	private String productState;

	@CTORMTemplate(seq = "16", name="productProcessState", type="Column", dataType="String", initial="", history="")
	private String productProcessState;

	@CTORMTemplate(seq = "17", name="productHoldState", type="Column", dataType="String", initial="", history="")
	private String productHoldState;

	@CTORMTemplate(seq = "18", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "19", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "20", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "21", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "22", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "23", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "24", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "25", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;

	@CTORMTemplate(seq = "26", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;

	@CTORMTemplate(seq = "27", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;

	@CTORMTemplate(seq = "28", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;

	@CTORMTemplate(seq = "29", name="nodeStack", type="Column", dataType="String", initial="", history="")
	private String nodeStack;

	@CTORMTemplate(seq = "30", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "31", name="ecCode", type="Column", dataType="String", initial="", history="")
	private String ecCode;

	@CTORMTemplate(seq = "32", name="dummyUsedCount", type="Column", dataType="Number", initial="", history="")
	private long dummyUsedCount;

	@CTORMTemplate(seq = "33", name="mqcCount", type="Column", dataType="Number", initial="", history="")
	private long mqcCount;

	@CTORMTemplate(seq = "34", name="totalMQCCount", type="Column", dataType="Number", initial="", history="")
	private long totalMQCCount;

	@CTORMTemplate(seq = "35", name="mqcUSEProductSpec", type="Column", dataType="String", initial="", history="")
	private String mqcUSEProductSpec;

	@CTORMTemplate(seq = "36", name="mqcUSEECCode", type="Column", dataType="String", initial="", history="")
	private String mqcUSEECCode;

	@CTORMTemplate(seq = "37", name="mqcUSEProcessFlow", type="Column", dataType="String", initial="", history="")
	private String mqcUSEProcessFlow;

	@CTORMTemplate(seq = "38", name="scrapDepartmentName", type="Column", dataType="String", initial="", history="")
	private String scrapDepartmentName;

	@CTORMTemplate(seq = "39", name="scrapMachine", type="Column", dataType="String", initial="", history="")
	private String scrapMachine;
	
	@CTORMTemplate(seq = "40", name="note", type="Column", dataType="String", initial="", history="")
	private String note;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
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

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductGrade() {
		return productGrade;
	}

	public void setProductGrade(String productGrade) {
		this.productGrade = productGrade;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	public Timestamp getDueDate() {
		return dueDate;
	}

	public void setDueDate(Timestamp dueDate) {
		this.dueDate = dueDate;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getProductState() {
		return productState;
	}

	public void setProductState(String productState) {
		this.productState = productState;
	}

	public String getProductProcessState() {
		return productProcessState;
	}

	public void setProductProcessState(String productProcessState) {
		this.productProcessState = productProcessState;
	}

	public String getProductHoldState() {
		return productHoldState;
	}

	public void setProductHoldState(String productHoldState) {
		this.productHoldState = productHoldState;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
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

	public String getNodeStack() {
		return nodeStack;
	}

	public void setNodeStack(String nodeStack) {
		this.nodeStack = nodeStack;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getEcCode() {
		return ecCode;
	}

	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}

	public long getDummyUsedCount() {
		return dummyUsedCount;
	}

	public void setDummyUsedCount(long dummyUsedCount) {
		this.dummyUsedCount = dummyUsedCount;
	}

	public long getMqcCount() {
		return mqcCount;
	}

	public void setMqcCount(long mqcCount) {
		this.mqcCount = mqcCount;
	}

	public long getTotalMQCCount() {
		return totalMQCCount;
	}

	public void setTotalMQCCount(long totalMQCCount) {
		this.totalMQCCount = totalMQCCount;
	}

	public String getMqcUSEProductSpec() {
		return mqcUSEProductSpec;
	}

	public void setMqcUSEProductSpec(String mqcUSEProductSpec) {
		this.mqcUSEProductSpec = mqcUSEProductSpec;
	}

	public String getMqcUSEECCode() {
		return mqcUSEECCode;
	}

	public void setMqcUSEECCode(String mqcUSEECCode) {
		this.mqcUSEECCode = mqcUSEECCode;
	}

	public String getMqcUSEProcessFlow() {
		return mqcUSEProcessFlow;
	}

	public void setMqcUSEProcessFlow(String mqcUSEProcessFlow) {
		this.mqcUSEProcessFlow = mqcUSEProcessFlow;
	}

	public String getScrapDepartmentName() {
		return scrapDepartmentName;
	}

	public void setScrapDepartmentName(String scrapDepartmentName) {
		this.scrapDepartmentName = scrapDepartmentName;
	}

	public String getScrapMachine() {
		return scrapMachine;
	}

	public void setScrapMachine(String scrapMachine) {
		this.scrapMachine = scrapMachine;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
