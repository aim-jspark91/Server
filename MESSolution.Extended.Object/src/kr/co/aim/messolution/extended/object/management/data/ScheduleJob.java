package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @since 2019.04.23
 * @author smkang
 * @see According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
 *      For avoid duplication of schedule job, running information should be recorded.
 */
public class ScheduleJob extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="jobClass", type="Key", dataType="String", initial="", history="")
	private String jobClass;
	
	@CTORMTemplate(seq = "3", name="serverName", type="Column", dataType="String", initial="", history="")
	private String serverName;
	
	@CTORMTemplate(seq = "4", name="jobStartTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp jobStartTime;
	
	@CTORMTemplate(seq = "5", name="jobEndTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp jobEndTime;

	@CTORMTemplate(seq = "6", name="previousFireTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp previousFireTime;
	
	@CTORMTemplate(seq = "7", name="currentFireTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp currentFireTime;
	
	@CTORMTemplate(seq = "8", name="nextFireTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp nextFireTime;

	// Added by smkang on 2019.07.05 - Management scenario is changed.
	@CTORMTemplate(seq = "9", name="isRunningJob", type="Column", dataType="String", initial="N", history="")
	private String isRunningJob;
	
	public ScheduleJob() {
	}
	
	public ScheduleJob(String jobName, String jobClass) {
		this.setJobName(jobName);
		this.setJobClass(jobClass);
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Timestamp getJobStartTime() {
		return jobStartTime;
	}

	public void setJobStartTime(Timestamp jobStartTime) {
		this.jobStartTime = jobStartTime;
	}

	public Timestamp getJobEndTime() {
		return jobEndTime;
	}

	public void setJobEndTime(Timestamp jobEndTime) {
		this.jobEndTime = jobEndTime;
	}
	
	public Timestamp getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Timestamp previousFireTime) {
		this.previousFireTime = previousFireTime;
	}
	
	public Timestamp getCurrentFireTime() {
		return currentFireTime;
	}

	public void setCurrentFireTime(Timestamp currentFireTime) {
		this.currentFireTime = currentFireTime;
	}

	public Timestamp getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Timestamp nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
	// Added by smkang on 2019.07.05 - Management scenario is changed.
	public String getIsRunningJob() {
		return isRunningJob;
	}

	// Added by smkang on 2019.07.05 - Management scenario is changed.
	public void setIsRunningJob(String isRunningJob) {
		this.isRunningJob = isRunningJob;
	}
}