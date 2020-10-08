package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmMailTemplate extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmType", type="Key", dataType="String", initial="", history="")
	private String alarmType;
	
	@CTORMTemplate(seq = "2", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;
	
	@CTORMTemplate(seq = "3", name="title", type="Column", dataType="String", initial="", history="")
	private String title;
	
	@CTORMTemplate(seq = "4", name="comments", type="Column", dataType="String", initial="", history="")
	private String comments;
	
	@CTORMTemplate(seq = "5", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	public AlarmMailTemplate() {
		super();
	}
	
	

	public AlarmMailTemplate(String alarmType, String alarmCode) {
		super();
		this.alarmType = alarmType;
		this.alarmCode = alarmCode;
	}



	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
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

	
	
}
