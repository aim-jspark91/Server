package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJobProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "3", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "4", name="fromLotName", type="Column", dataType="String", initial="", history="")
	private String fromLotName;
	
	@CTORMTemplate(seq = "5", name="fromCarrierName", type="Column", dataType="String", initial="", history="")
	private String fromCarrierName;
	
	@CTORMTemplate(seq = "6", name="fromPortName", type="Column", dataType="String", initial="", history="")
	private String fromPortName;
	
	@CTORMTemplate(seq = "7", name="fromPosition", type="Column", dataType="String", initial="", history="")
	private String fromPosition;
	
	@CTORMTemplate(seq = "8", name="toLotName", type="Column", dataType="String", initial="", history="")
	private String toLotName;
	
	@CTORMTemplate(seq = "9", name="toCarrierName", type="Column", dataType="String", initial="", history="")
	private String toCarrierName;
	
	@CTORMTemplate(seq = "10", name="toPortName", type="Column", dataType="String", initial="", history="")
	private String toPortName;
	
	@CTORMTemplate(seq = "11", name="toPosition", type="Column", dataType="String", initial="", history="")
	private String toPosition;
	
	@CTORMTemplate(seq = "12", name="sortProductState", type="Column", dataType="String", initial="", history="")
	private String sortProductState;
	
	@CTORMTemplate(seq = "13", name="turnSideFlag", type="Column", dataType="String", initial="", history="")
	private String turnSideFlag;
	
	@CTORMTemplate(seq = "14", name="scrapFlag", type="Column", dataType="String", initial="", history="")
	private String scrapFlag;
	
	@CTORMTemplate(seq = "15", name="turnOverFlag", type="Column", dataType="String", initial="", history="")
	private String turnOverFlag;
	
	@CTORMTemplate(seq = "16", name="outStageFlag", type="Column", dataType="String", initial="", history="")
	private String outStageFlag;
	//instantiation
	public SortJobProduct()
	{
		
	}
	
	
	public SortJobProduct(String jobName, String productName)
	{
		setJobName(jobName);
		setProductName(productName);
	}


	public String getJobName() {
		return jobName;
	}


	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
	}


	public String getMachineName() {
		return machineName;
	}


	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}


	public String getFromLotName() {
		return fromLotName;
	}


	public void setFromLotName(String fromLotName) {
		this.fromLotName = fromLotName;
	}


	public String getFromCarrierName() {
		return fromCarrierName;
	}


	public void setFromCarrierName(String fromCarrierName) {
		this.fromCarrierName = fromCarrierName;
	}


	public String getFromPortName() {
		return fromPortName;
	}


	public void setFromPortName(String fromPortName) {
		this.fromPortName = fromPortName;
	}


	public String getFromPosition() {
		return fromPosition;
	}


	public void setFromPosition(String fromPosition) {
		this.fromPosition = fromPosition;
	}


	public String getToLotName() {
		return toLotName;
	}


	public void setToLotName(String toLotName) {
		this.toLotName = toLotName;
	}


	public String getToCarrierName() {
		return toCarrierName;
	}


	public void setToCarrierName(String toCarrierName) {
		this.toCarrierName = toCarrierName;
	}


	public String getToPortName() {
		return toPortName;
	}


	public void setToPortName(String toPortName) {
		this.toPortName = toPortName;
	}


	public String getToPosition() {
		return toPosition;
	}


	public void setToPosition(String toPosition) {
		this.toPosition = toPosition;
	}


	public String getSortProductState() {
		return sortProductState;
	}


	public void setSortProductState(String sortProductState) {
		this.sortProductState = sortProductState;
	}


	public String getTurnSideFlag() {
		return turnSideFlag;
	}


	public void setTurnSideFlag(String turnSideFlag) {
		this.turnSideFlag = turnSideFlag;
	}


	public String getScrapFlag() {
		return scrapFlag;
	}


	public void setScrapFlag(String scrapFlag) {
		this.scrapFlag = scrapFlag;
	}
	
	public String getTurnOverFlag() {
		return turnOverFlag;
	}

	public void setTurnOverFlag(String turnOverFlag) {
		this.turnOverFlag = turnOverFlag;
	}

	public String getOutStageFlag() {
		return outStageFlag;
	}

	public void setOutStageFlag(String outStageFlag) {
		this.outStageFlag = outStageFlag;
	}
}
