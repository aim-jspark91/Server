package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCJobOper extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="mqcJobName", type="Key", dataType="String", initial="", history="")
	private String mqcJobName;
	
	@CTORMTemplate(seq = "2", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "3", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "4", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "6", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "7", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	public String getmqcJobName() {
		return mqcJobName;
	}

	public void setmqcJobName(String mqcJobName) {
		this.mqcJobName = mqcJobName;
	}

	public String getprocessOperationName() {
		return processOperationName;
	}

	public void setprocessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getprocessOperationVersion() {
		return processOperationVersion;
	}

	public void setprocessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getmachineGroupName() {
		return machineGroupName;
	}

	public void setmachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getmachineName() {
		return machineName;
	}

	public void setmachineName(String machineName) {
		this.machineName = machineName;
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
	
	//instantiation
	public MQCJobOper()
	{
		
	}
	
	public MQCJobOper(String mqcJobName, String processOperationName, String processOperationVersion)
	{
		setmqcJobName(mqcJobName);
		setprocessOperationName(processOperationName);
		setprocessOperationVersion(processOperationVersion);
	}
}
