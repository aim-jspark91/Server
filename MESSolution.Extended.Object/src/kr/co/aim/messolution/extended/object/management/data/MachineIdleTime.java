package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of equipment idle time.
 */
public class MachineIdleTime extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="mqcType", type="Column", dataType="String", initial="", history="")
	private String mqcType;
	
	@CTORMTemplate(seq = "4", name="resetType", type="Column", dataType="String", initial="", history="")
	private String resetType;
	
	@CTORMTemplate(seq = "5", name="autoResetFlag", type="Column", dataType="String", initial="", history="")
	private String autoResetFlag;
	
	@CTORMTemplate(seq = "6", name="idleTime", type="Column", dataType="Number", initial="", history="")
	private long idleTime;
	
	@CTORMTemplate(seq = "7", name="glassCount", type="Column", dataType="Number", initial="", history="")
	private long glassCount;
	
	@CTORMTemplate(seq = "8", name="lastRunTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastRunTime;
	
	@CTORMTemplate(seq = "9", name="lastGlassCount", type="Column", dataType="Number", initial="", history="")
	private long lastGlassCount;
	
	@CTORMTemplate(seq = "10", name="isIdleTimeOver", type="Column", dataType="String", initial="", history="")
	private String isIdleTimeOver;
	
	@CTORMTemplate(seq = "11", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	public MachineIdleTime() {
	}
	
	public MachineIdleTime(String machineName, String unitName) {
		this.setMachineName(machineName);
		this.setUnitName(unitName);
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getMqcType() {
		return mqcType;
	}

	public void setMqcType(String mqcType) {
		this.mqcType = mqcType;
	}
	
	public String getResetType() {
		return resetType;
	}

	public void setResetType(String resetType) {
		this.resetType = resetType;
	}

	public String getAutoResetFlag() {
		return autoResetFlag;
	}

	public void setAutoResetFlag(String autoResetFlag) {
		this.autoResetFlag = autoResetFlag;
	}

	public long getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(long idleTime) {
		this.idleTime = idleTime;
	}

	public long getGlassCount() {
		return glassCount;
	}

	public void setGlassCount(long glassCount) {
		this.glassCount = glassCount;
	}

	public Timestamp getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(Timestamp lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public long getLastGlassCount() {
		return lastGlassCount;
	}

	public void setLastGlassCount(long lastGlassCount) {
		this.lastGlassCount = lastGlassCount;
	}

	public String getIsIdleTimeOver() {
		return isIdleTimeOver;
	}

	public void setIsIdleTimeOver(String isIdleTimeOver) {
		this.isIdleTimeOver = isIdleTimeOver;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
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