package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmDefinition extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;

	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="alarmType", type="Column", dataType="String", initial="", history="")
	private String alarmType;
	
	@CTORMTemplate(seq = "4", name="alarmSeverity", type="Column", dataType="String", initial="", history="")
	private String alarmSeverity;
	
	@CTORMTemplate(seq = "5", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;

	//instantiation
	public AlarmDefinition()
	{
		
	}

	public AlarmDefinition(String alarmCode)
	{
		setAlarmCode(alarmCode);
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmSeverity() {
		return alarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
