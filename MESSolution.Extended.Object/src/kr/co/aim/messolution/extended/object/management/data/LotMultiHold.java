package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

import org.apache.commons.lang.StringUtils;

/**
 * @since 2018.08.13
 * @author smkang
 * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
 */
public class LotMultiHold extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name="reasonCode", type="Key", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "3", name="department", type="Key", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "4", name="eventComment", type="Key", dataType="String", initial="", history="")
	private String eventComment;
	
	@CTORMTemplate(seq = "5", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "6", name="holdType", type="Column", dataType="String", initial="", history="")
	private String holdType;
	
	@CTORMTemplate(seq = "7", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "8", name="eventName", type="Column", dataType="String", initial="", history="")
	private String eventName;
	
	@CTORMTemplate(seq = "9", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
		
	@CTORMTemplate(seq = "10", name="resetFlag", type="Column", dataType="String", initial="", history="")
	private String resetFlag;
	
	@CTORMTemplate(seq = "11", name="eventTimeKey", type="Column", dataType="String", initial="", history="")
	private String eventTimeKey;
	
	public LotMultiHold() {
	}
	
	public LotMultiHold(String lotName, String reasonCode, String department, String eventComment) {
		this.setLotName(lotName);
		this.setReasonCode(reasonCode);
		this.setDepartment(department);
		this.setEventComment(eventComment);
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEventComment() {
		return eventComment;
	}

	public void setEventComment(String eventComment) {
		this.eventComment = StringUtils.isNotEmpty(eventComment) ? eventComment.trim() : "";
	}
	
	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getHoldType() {
		return holdType;
	}

	public void setHoldType(String holdType) {
		this.holdType = holdType;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
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

	public String getResetFlag() {
		return resetFlag;
	}

	public void setResetFlag(String resetFlag) {
		this.resetFlag = resetFlag;
	}

	public String getEventTimeKey() {
		return eventTimeKey;
	}

	public void setEventTimeKey(String eventTimeKey) {
		this.eventTimeKey = eventTimeKey;
	}
	
}