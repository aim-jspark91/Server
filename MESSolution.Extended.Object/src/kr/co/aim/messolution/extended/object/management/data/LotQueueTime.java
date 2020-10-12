package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class LotQueueTime extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "3", name="toProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String toProcessOperationName;
	
	@CTORMTemplate(seq = "4", name="toProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String toProcessFlowName;
	
	/*
	@CTORMTemplate(seq = "5", name="queueType", type="Key", dataType="String", initial="", history="")
	private String queueType;
	*/
	
	@CTORMTemplate(seq = "6", name="warningDurationLimit", type="Column", dataType="String", initial="", history="")
	private String warningDurationLimit;
	
	@CTORMTemplate(seq = "7", name="interlockDurationLimit", type="Column", dataType="String", initial="", history="")
	private String interlockDurationLimit;
	
	@CTORMTemplate(seq = "8", name="queueTimeState", type="Column", dataType="String", initial="", history="")
	private String queueTimeState;
	
	@CTORMTemplate(seq = "9", name="enterTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp enterTime;
	
	@CTORMTemplate(seq = "10", name="exitTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp exitTime;
	
	@CTORMTemplate(seq = "11", name="warningTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp warningTime;
	
	@CTORMTemplate(seq = "12", name="interlockTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp interlockTime;
	
	@CTORMTemplate(seq = "13", name="resolveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp resolveTime;
	
	@CTORMTemplate(seq = "14", name="resolveUser", type="Column", dataType="String", initial="", history="")
	private String resolveUser;
	
	

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getToProcessOperationName() {
		return toProcessOperationName;
	}

	public void setToProcessOperationName(String toProcessOperationName) {
		this.toProcessOperationName = toProcessOperationName;
	}

	public String getWarningDurationLimit() {
		return warningDurationLimit;
	}

	public void setWarningDurationLimit(String warningDurationLimit) {
		this.warningDurationLimit = warningDurationLimit;
	}

	public String getInterlockDurationLimit() {
		return interlockDurationLimit;
	}

	public void setInterlockDurationLimit(String interlockDurationLimit) {
		this.interlockDurationLimit = interlockDurationLimit;
	}

	public String getQueueTimeState() {
		return queueTimeState;
	}

	public void setQueueTimeState(String queueTimeState) {
		this.queueTimeState = queueTimeState;
	}

	public Timestamp getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(Timestamp enterTime) {
		this.enterTime = enterTime;
	}

	public Timestamp getExitTime() {
		return exitTime;
	}

	public void setExitTime(Timestamp exitTime) {
		this.exitTime = exitTime;
	}

	public Timestamp getWarningTime() {
		return warningTime;
	}

	public void setWarningTime(Timestamp warningTime) {
		this.warningTime = warningTime;
	}

	public Timestamp getResolveTime() {
		return resolveTime;
	}

	public void setResolveTime(Timestamp resolveTime) {
		this.resolveTime = resolveTime;
	}

	public String getResolveUser() {
		return resolveUser;
	}

	public void setResolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
	}
	
	public Timestamp getInterlockTime() {
		return interlockTime;
	}

	public void setInterlockTime(Timestamp interlockTime) {
		this.interlockTime = interlockTime;
	}
	
	public String gettoProcessFlowName() {
		return toProcessFlowName;
	}

	public void settoProcessFlowName(String toProcessFlowName) {
		this.toProcessFlowName = toProcessFlowName;
	}
	
	/*
	public String getqueueType() {
		return queueType;
	}

	public void setqueueType(String queueType) {
		this.queueType = queueType;
	}
	*/
	
	public LotQueueTime()
	{
		
	}

	public LotQueueTime(String lotName, String processOperationName, String toProcessOperationName,String toProcessFlowName)
	{
		this.lotName = lotName;
		this.processOperationName = processOperationName;
		this.toProcessOperationName = toProcessOperationName;
		this.toProcessFlowName = toProcessFlowName;
	}
}
