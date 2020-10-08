package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleLotState extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name = "sampleProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowName;
	
	@CTORMTemplate(seq = "3", name = "sampleProcessFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "4", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "5", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name = "ecCode", type = "Column", dataType = "String", initial = "", history = "")
	private String ecCode;
	
	@CTORMTemplate(seq = "7", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	
	@CTORMTemplate(seq = "8", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "9", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	
	@CTORMTemplate(seq = "10", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "11", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	
	@CTORMTemplate(seq = "12", name = "samplePriority", type = "Column", dataType = "Long", initial = "", history = "")
	private long samplePriority;
	
	@CTORMTemplate(seq = "13", name = "sampleState", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleState;
	
	public SampleLotState()
	{
		
	}

	public SampleLotState(String lotName, String sampleProcessFlowName, String sampleProcessFlowVersion) {
		super();
		this.lotName = lotName;
		this.sampleProcessFlowName = sampleProcessFlowName;
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
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

	public long getSamplePriority() {
		return samplePriority;
	}

	public void setSamplePriority(long samplePriority) {
		this.samplePriority = samplePriority;
	}

	public String getSampleState() {
		return sampleState;
	}

	public void setSampleState(String sampleState) {
		this.sampleState = sampleState;
	}
}
