package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of equipment idle time.
 */
public class MQCCondition extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="mqcProductSpecName", type="Key", dataType="String", initial="", history="")
	private String mqcProductSpecName;
	
	@CTORMTemplate(seq = "4", name="mqcPlanCount", type="Column", dataType="Number", initial="", history="")
	private long mqcPlanCount;
	
	@CTORMTemplate(seq = "5", name="mqcRunCount", type="Column", dataType="Number", initial="", history="")
	private long mqcRunCount;
	
	@CTORMTemplate(seq = "6", name="mqcPreRunCount", type="Column", dataType="Number", initial="", history="")
	private long mqcPreRunCount;
	
	@CTORMTemplate(seq = "7", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	public MQCCondition() {
	}
	
	public MQCCondition(String machineName, String unitName, String mqcProductSpecName) {
		this.setMachineName(machineName);
		this.setUnitName(unitName);
		this.setMqcProductSpecName(mqcProductSpecName);
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

	public String getMqcProductSpecName() {
		return mqcProductSpecName;
	}

	public void setMqcProductSpecName(String mqcProductSpecName) {
		this.mqcProductSpecName = mqcProductSpecName;
	}

	public long getMqcPlanCount() {
		return mqcPlanCount;
	}

	public void setMqcPlanCount(long mqcPlanCount) {
		this.mqcPlanCount = mqcPlanCount;
	}

	public long getMqcRunCount() {
		return mqcRunCount;
	}

	public void setMqcRunCount(long mqcRunCount) {
		this.mqcRunCount = mqcRunCount;
	}

	public long getMqcPreRunCount() {
		return mqcPreRunCount;
	}
	
	public void setMqcPreRunCount(long mqcPreRunCount) {
		this.mqcPreRunCount = mqcPreRunCount;
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