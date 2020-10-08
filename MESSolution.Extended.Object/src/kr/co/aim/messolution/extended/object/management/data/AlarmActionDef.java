package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmActionDef extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;

	@CTORMTemplate(seq = "2", name="actionName", type="Key", dataType="String", initial="", history="")
	private String actionName;
	
	@CTORMTemplate(seq = "3", name="seq", type="Column", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "4", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "5", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "10", name="department", type="Column", dataType="String", initial="", history="")
	private String department;

	//instantiation
	public AlarmActionDef()
	{
		
	}

	public AlarmActionDef(String alarmCode, String actionName)
	{
		setAlarmCode(alarmCode);
		setActionName(actionName);
	}
	
	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
