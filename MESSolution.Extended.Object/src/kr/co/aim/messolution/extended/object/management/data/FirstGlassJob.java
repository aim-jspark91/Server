package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassJob extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "4", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;	
	
	@CTORMTemplate(seq = "5", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;	
	
	@CTORMTemplate(seq = "6", name="targetOperationName", type="Column", dataType="String", initial="", history="")
	private String targetOperationName;
	
	@CTORMTemplate(seq = "7", name="jobState", type="Column", dataType="String", initial="", history="")
	private String jobState;
	
	@CTORMTemplate(seq = "8", name="jobProcessState", type="Column", dataType="String", initial="", history="")
	private String jobProcessState;
	
	@CTORMTemplate(seq = "9", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "10", name="returnFlowName", type="Column", dataType="String", initial="", history="")
	private String returnFlowName;
	
	@CTORMTemplate(seq = "11", name="returnOperationName", type="Column", dataType="String", initial="", history="")
	private String returnOperationName;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "17", name="judge", type="Column", dataType="String", initial="", history="")
	private String judge;
	
	@CTORMTemplate(seq = "18", name="inspectFlowName", type="Column", dataType="String", initial="", history="")
	private String inspectFlowName;
	
	@CTORMTemplate(seq = "19", name="assignFlag", type="Column", dataType="String", initial="", history="")
	private String assignFlag;

	//instantiation
	public FirstGlassJob()
	{
		
	}
	
	public FirstGlassJob(String jobName)
	{
		setJobName(jobName);
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
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

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getTargetOperationName() {
		return targetOperationName;
	}

	public void setTargetOperationName(String targetOperationName) {
		this.targetOperationName = targetOperationName;
	}

	public String getJobState() {
		return jobState;
	}

	public void setJobState(String jobState) {
		this.jobState = jobState;
	}

	public String getJobProcessState() {
		return jobProcessState;
	}

	public void setJobProcessState(String jobProcessState) {
		this.jobProcessState = jobProcessState;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getReturnFlowName() {
		return returnFlowName;
	}

	public void setReturnFlowName(String returnFlowName) {
		this.returnFlowName = returnFlowName;
	}

	public String getReturnOperationName() {
		return returnOperationName;
	}

	public void setReturnOperationName(String returnOperationName) {
		this.returnOperationName = returnOperationName;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
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

	public String getJudge() {
		return judge;
	}

	public void setJudge(String judge) {
		this.judge = judge;
	}

	public String getInspectFlowName() {
		return inspectFlowName;
	}

	public void setInspectFlowName(String inspectFlowName) {
		this.inspectFlowName = inspectFlowName;
	}

	public String getAssignFlag() {
		return assignFlag;
	}

	public void setAssignFlag(String assignFlag) {
		this.assignFlag = assignFlag;
	}
}
