package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJobCarrier extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="carrierName", type="Key", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "3", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "4", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "5", name="portName", type="Column", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "6", name="transferDirection", type="Column", dataType="String", initial="", history="")
	private String transferDirection;
	
	@CTORMTemplate(seq = "7", name="loadFlag", type="Column", dataType="String", initial="", history="")
	private String loadFlag;
	
	@CTORMTemplate(seq = "8", name="loadTimeKey", type="Column", dataType="String", initial="", history="")
	private String loadTimeKey;
	
	@CTORMTemplate(seq = "9", name="trackflag", type="Column", dataType="String", initial="", history="")
	private String trackflag;
	
	@CTORMTemplate(seq = "10", name="outholdflag", type="Column", dataType="String", initial="", history="")
	private String outholdflag;
	//instantiation
	public SortJobCarrier()
	{
		
	}
	
	
	public SortJobCarrier(String jobName, String carrierName)
	{
		setJobName(jobName);
		setCarrierName(carrierName);
	}

	public String getJobTrackFlag() {
		return trackflag;
	}

	public void setJobTrackFlag(String trackflag) {
		this.trackflag = trackflag;
	}
	
	public String getJobName() {
		return jobName;
	}


	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	public String getCarrierName() {
		return carrierName;
	}


	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}


	public String getLotName() {
		return lotName;
	}


	public void setLotName(String lotName) {
		this.lotName = lotName;
	}


	public String getMachineName() {
		return machineName;
	}


	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}


	public String getPortName() {
		return portName;
	}


	public void setPortName(String portName) {
		this.portName = portName;
	}


	public String getTransferDirection() {
		return transferDirection;
	}


	public void setTransferDirection(String transferDirection) {
		this.transferDirection = transferDirection;
	}


	public String getLoadFlag() {
		return loadFlag;
	}


	public void setLoadFlag(String loadFlag) {
		this.loadFlag = loadFlag;
	}


	public String getLoadTimeKey() {
		return loadTimeKey;
	}


	public void setLoadTimeKey(String loadTimeKey) {
		this.loadTimeKey = loadTimeKey;
	}
	
	public String getoutholdflag() {
		return outholdflag;
	}


	public void setoutholdflag(String outholdflag) {
		this.outholdflag = outholdflag;
	}
	
	public String gettrackflag() {
		return trackflag;
	}


	public void settrackflag(String trackflag) {
		this.trackflag = trackflag;
	}
}
