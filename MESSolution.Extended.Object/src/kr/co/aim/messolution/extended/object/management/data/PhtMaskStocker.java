package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PhtMaskStocker extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="location", type="Key", dataType="Long", initial="", history="")
	private long location;
	
	@CTORMTemplate(seq = "4", name="currentMaskName", type="Column", dataType="String", initial="", history="")
	private String currentMaskName;
	
	@CTORMTemplate(seq = "5", name="currentInTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp currentInTime;
	
	@CTORMTemplate(seq = "6", name="lastOutMaskName", type="Column", dataType="String", initial="", history="")
	private String lastOutMaskName;
	
	@CTORMTemplate(seq = "7", name="lastOutTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastOutTime;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	public String getMachineName() {
		return machineName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public long getLocation() {
		return location;
	}

	public void setLocation(long location) {
		this.location = location;
	}

	public String getCurrentMaskName() {
		return currentMaskName;
	}

	public void setCurrentMaskName(String currentMaskName) {
		this.currentMaskName = currentMaskName;
	}

	public Timestamp getCurrentInTime() {
		return currentInTime;
	}

	public void setCurrentInTime(Timestamp currentInTime) {
		this.currentInTime = currentInTime;
	}

	public String getLastOutMaskName() {
		return lastOutMaskName;
	}

	public void setLastOutMaskName(String lastOutMaskName) {
		this.lastOutMaskName = lastOutMaskName;
	}

	public Timestamp getLastOutTime() {
		return lastOutTime;
	}

	public void setLastOutTime(Timestamp lastOutTime) {
		this.lastOutTime = lastOutTime;
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

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
}
