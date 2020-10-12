package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJob extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="jobState", type="Column", dataType="String", initial="", history="")
	private String jobState;
	
	@CTORMTemplate(seq = "3", name="jobType", type="Column", dataType="String", initial="", history="")
	private String jobType;
	
	@CTORMTemplate(seq = "4", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "5", name="seq", type="Column", dataType="int", initial="", history="")
	private int seq;
	
	@CTORMTemplate(seq = "6", name="timeKey", type="Column", dataType="String", initial="", history="N")
	private String timeKey;
	
	@CTORMTemplate(seq = "7", name="eventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "8", name="eventName", type="Column", dataType="String", initial="", history="N")
	private String eventName;
	
	@CTORMTemplate(seq = "9", name="eventUser", type="Column", dataType="String", initial="", history="N")
	private String eventUser;
	
	@CTORMTemplate(seq = "10", name="eventComment", type="Column", dataType="String", initial="", history="N")
	private String eventComment;
	
	@CTORMTemplate(seq = "11", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "12", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "13", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;

	@CTORMTemplate(seq = "14", name="detailJobType", type="Column", dataType="String", initial="", history="")
	private String detailJobType;
	
	@CTORMTemplate(seq = "15", name="note", type="Column", dataType="String", initial="", history="")
	private String note;
	
	//instantiation
	public SortJob()
	{
		
	}
	
	
	public SortJob(String jobName)
	{
		setJobName(jobName);
	}


	public String getJobName() {
		return jobName;
	}


	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	public String getJobState() {
		return jobState;
	}


	public void setJobState(String jobState) {
		this.jobState = jobState;
	}


	public String getJobType() {
		return jobType;
	}


	public void setJobType(String jobType) {
		this.jobType = jobType;
	}


	public String getMachineName() {
		return machineName;
	}


	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}


	public int getSeq() {
		return seq;
	}


	public void setSeq(int seq) {
		this.seq = seq;
	}


	public String getTimeKey() {
		return timeKey;
	}


	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}


	public Timestamp getEventTime() {
		return eventTime;
	}


	public void setEventTime(Timestamp timestamp) {
		this.eventTime = timestamp;
	}


	public String getEventName() {
		return eventName;
	}


	public void setEventName(String eventName) {
		this.eventName = eventName;
	}


	public String getEventUser() {
		return eventUser;
	}


	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}


	public String getEventComment() {
		return eventComment;
	}


	public void setEventComment(String eventComment) {
		this.eventComment = eventComment;
	}


	public Timestamp getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
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

	public String getDetailJobType() {
		return detailJobType;
	}


	public void setDetailJobType(String detailJobType) {
		this.detailJobType = detailJobType;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}
	


}
