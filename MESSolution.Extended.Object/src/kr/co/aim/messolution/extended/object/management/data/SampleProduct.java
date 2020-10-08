package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleProduct extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	
	@CTORMTemplate(seq = "2", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name = "ecCode", type = "Key", dataType = "String", initial = "", history = "")
	private String ecCode;
	
	@CTORMTemplate(seq = "6", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "8", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	
	@CTORMTemplate(seq = "9", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "10", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	
	@CTORMTemplate(seq = "11", name = "sampleProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowName;
	
	@CTORMTemplate(seq = "12", name = "sampleProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "13", name = "position", type = "Column", dataType = "Long", initial = "", history = "")
	private long position;
	
	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "16", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "17", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "18", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "19", name = "machineRecipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineRecipeName;
	
	
	public SampleProduct()
	{
		
	}


	public SampleProduct(String pRODUCTNAME, String lOTNAME,
			String fACTORYNAME, String pRODUCTSPECNAME, String eCCode,
			String pROCESSFLOWNAME, String processFlowVersion,
			String pROCESSOPERATIONNAME, String processOperationVersion,
			String mACHINENAME, String sampleProcessFlowName,
			String sampleProcessFlowVersion) {
		super();
		this.productName = pRODUCTNAME;
		this.lotName = lOTNAME;
		this.factoryName = fACTORYNAME;
		this.productSpecName = pRODUCTSPECNAME;
		this.ecCode = eCCode;
		this.processFlowName = pROCESSFLOWNAME;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = pROCESSOPERATIONNAME;
		this.processOperationVersion = processOperationVersion;
		this.machineName = mACHINENAME;
		this.sampleProcessFlowName = sampleProcessFlowName;
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
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


	public long getPosition() {
		return position;
	}


	public void setPosition(long position) {
		this.position = position;
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


	public String getMachineRecipeName() {
		return machineRecipeName;
	}


	public void setMachineRecipeName(String machineRecipeName) {
		this.machineRecipeName = machineRecipeName;
	}


	
}
