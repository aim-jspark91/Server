package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FileJudgeSetting extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="FactoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="ProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "3", name="ProcessFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "4", name="ProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "5", name="ProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "6", name="GradeDataFlag", type="Column", dataType="String", initial="", history="")
	private String gradeDataFlag;
	
	@CTORMTemplate(seq = "7", name="ReJudgeFlag", type="Column", dataType="String", initial="", history="")
	private String reJudgeFlag;

	@CTORMTemplate(seq = "8", name="AutoReviewFlag", type="Column", dataType="String", initial="", history="")
	private String autoReviewFlag;
	
	@CTORMTemplate(seq = "9", name="ForceRepairFlag", type="Column", dataType="String", initial="", history="")
	private String forceRepairFlag;
	
	@CTORMTemplate(seq = "10", name="RepairGradeFlag", type="Column", dataType="String", initial="", history="")
	private String repairGradeFlag;

	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public FileJudgeSetting()
	{
		
	}
	public FileJudgeSetting(String factoryName,String processFlowName, String processFlowVersion,String processOperationName,String processOperationVersion)
	{
		this.setFactoryName(factoryName);
		this.setProcessFlowName(processFlowName);
		this.setProcessFlowVersion(processFlowVersion);
		this.setProcessOperationName(processOperationName);
		this.setProcessOperationVersion(processOperationVersion);
	}


	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getProcessFlowName() {
		return processFlowName;
	}


	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}


	public String getProcessFlowVersion() {
		return this.processFlowVersion;
	}


	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}


	public String getProcessOperationName() {
		return this.processOperationName;
	}


	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}


	public String getProcessOperationVersion() {
		return this.processOperationVersion;
	}


	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}


	public String getGradeDataFlag() {
		return this.gradeDataFlag;
	}


	public void setGradeDataFlag(String gradeDataFlag) {
		this.gradeDataFlag = gradeDataFlag;
	}


	public String getReJudgeFlag() {
		return this.reJudgeFlag;
	}


	public void setReJudgeFlag(String reJudgeFlag) {
		this.reJudgeFlag = reJudgeFlag;
	}


	public String getAutoReviewFlag() {
		return this.autoReviewFlag;
	}


	public void setAutoReviewFlag(String autoReviewFlag) {
		this.autoReviewFlag = autoReviewFlag;
	}

	

	public String getForceRepairFlag() {
		return forceRepairFlag;
	}
	public void setForceRepairFlag(String forceRepairFlag) {
		this.forceRepairFlag = forceRepairFlag;
	}
	public String getRepairGradeFlag() {
		return repairGradeFlag;
	}
	public void setRepairGradeFlag(String repairGradeFlag) {
		this.repairGradeFlag = repairGradeFlag;
	}
	public String getLastEventUser() {
		return this.lastEventUser;
	}


	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}


	public Timestamp getLastEventTime() {
		return this.lastEventTime;
	}


	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}


	public String getLastEventTimeKey() {
		return this.lastEventTimeKey;
	}


	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}


	public String getLastEventName() {
		return this.lastEventName;
	}


	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}


	public String getLastEventComment() {
		return this.lastEventComment;
	}


	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	

}
