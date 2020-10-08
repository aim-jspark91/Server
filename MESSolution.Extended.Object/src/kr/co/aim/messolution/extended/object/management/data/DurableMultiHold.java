package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DurableMultiHold extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="DurableName", type="Key", dataType="String", initial="", history="")
	private String DurableName;
	
	@CTORMTemplate(seq = "2", name="DurableType", type="Column", dataType="String", initial="", history="")
	private String DurableType;
	
	@CTORMTemplate(seq = "3", name="ReasonCode", type="Column", dataType="String", initial="", history="")
	private String ReasonCode;
	
	@CTORMTemplate(seq = "4", name="EventTime", type="Column", dataType="Date", initial="", history="")
	private Date EventTime;
	
	@CTORMTemplate(seq = "5", name="EventName", type="Column", dataType="String", initial="", history="")
	private String EventName;
	
	@CTORMTemplate(seq = "6", name="EventUser", type="Column", dataType="String", initial="", history="")
	private String EventUser;
	
	@CTORMTemplate(seq = "7", name="EventComment", type="Column", dataType="String", initial="", history="")
	private String EventComment;
	
	
	
	
	public DurableMultiHold()
	{
		
	}

	public DurableMultiHold(String DurableName)
	{
		setDurableName(DurableName);
		
	}

	public String getDurableName() {
		return DurableName;
	}

	public void setDurableName(String DurableName) {
		this.DurableName = DurableName;
	}

	public String getDurableType() {
		return DurableType;
	}

	public void setDurableType(String DurableType) {
		this.DurableType = DurableType;
	}

	public String getReasonCode() {
		return ReasonCode;
	}

	public void setReasonCode(String ReasonCode) {
		this.ReasonCode= ReasonCode;
	}

	public Date getEventTime() {
		return EventTime;
	}

	public void setEventTime(Date EventTime) {
		this.EventTime = EventTime;
	}

	public String getEventName() {
		return EventName;
	}

	public void setEventName(String EventName) {
		this.EventName = EventName;
	}

	public String getEventUser() {
		return EventUser;
	}

	public void setEventUser(String EventUser) {
		this.EventUser = EventUser;
	}

	public String getEventComment() {
		return EventComment;
	}

	public void setEventComment(String EventComment) {
		this.EventComment = EventComment;
	}


}
