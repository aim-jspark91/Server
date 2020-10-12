package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspMachineDispatch extends UdfAccessor {
	
	// Modified by smkang on 2018.09.04 - Design of this table is changed.
//	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
//	private String machineName;
//	
//	@CTORMTemplate(seq = "2", name="plDispatchFlag", type="Column", dataType="String", initial="", history="")
//	private String plDispatchFlag;
//	
//	@CTORMTemplate(seq = "3", name="plWaitTimee2e", type="Column", dataType="Number", initial="", history="")
//	private long plWaitTimee2e;
//	
//	@CTORMTemplate(seq = "4", name="plWaitTimePush", type="Column", dataType="Number", initial="", history="")
//	private long plWaitTimePush;
//	
//	@CTORMTemplate(seq = "5", name="plPullFlag", type="Column", dataType="String", initial="", history="")
//	private String plPullFlag;
//	
//	@CTORMTemplate(seq = "6", name="puDispatchFlag", type="Column", dataType="String", initial="", history="")
//	private String puDispatchFlag;
//	
//	@CTORMTemplate(seq = "7", name="puWaitTimee2e", type="Column", dataType="Number", initial="", history="")
//	private long puWaitTimee2e;
//	
//	@CTORMTemplate(seq = "8", name="puWaitTimePush", type="Column", dataType="Number", initial="", history="")
//	private long puWaitTimePush;
//	
//	@CTORMTemplate(seq = "9", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
//	private String lastEventName;
//	
//	@CTORMTemplate(seq = "10", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
//	private String lastEventTimekey;
//
//	@CTORMTemplate(seq = "11", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
//	private Timestamp lastEventTime;
//	
//	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
//	private String lastEventUser;
//	
//	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
//	private String lastEventComment;
//
//	public DspMachineDispatch()
//	{
//		
//	}
//	public DspMachineDispatch(String machineName)
//	{
//		this.setMachineName(machineName);
//	}
//	
//	public String getMachineName() {
//		return machineName;
//	}
//
//	public void setMachineName(String machineName) {
//		this.machineName = machineName;
//	}
//
//	public String getPlDispatchFlag() {
//		return plDispatchFlag;
//	}
//
//	public void setPlDispatchFlag(String plDispatchFlag) {
//		this.plDispatchFlag = plDispatchFlag;
//	}
//
//	public long getPlWaitTimee2e() {
//		return plWaitTimee2e;
//	}
//
//	public void setPlWaitTimee2e(long plWaitTimee2e) {
//		this.plWaitTimee2e = plWaitTimee2e;
//	}
//
//	public long getPlWaitTimePush() {
//		return plWaitTimePush;
//	}
//
//	public void setPlWaitTimePush(long plWaitTimePush) {
//		this.plWaitTimePush = plWaitTimePush;
//	}
//
//	public String getPlPullFlag() {
//		return plPullFlag;
//	}
//
//	public void setPlPullFlag(String plPullFlag) {
//		this.plPullFlag = plPullFlag;
//	}
//
//	public String getPuDispatchFlag() {
//		return puDispatchFlag;
//	}
//
//	public void setPuDispatchFlag(String puDispatchFlag) {
//		this.puDispatchFlag = puDispatchFlag;
//	}
//
//	public long getPuWaitTimee2e() {
//		return puWaitTimee2e;
//	}
//
//	public void setPuWaitTimee2e(long puWaitTimee2e) {
//		this.puWaitTimee2e = puWaitTimee2e;
//	}
//
//	public long getPuWaitTimePush() {
//		return puWaitTimePush;
//	}
//
//	public void setPuWaitTimePush(long puWaitTimePush) {
//		this.puWaitTimePush = puWaitTimePush;
//	}
//
//	public String getLastEventName() {
//		return lastEventName;
//	}
//
//	public void setLastEventName(String lastEventName) {
//		this.lastEventName = lastEventName;
//	}
//
//	public String getLastEventTimekey() {
//		return lastEventTimekey;
//	}
//
//	public void setLastEventTimekey(String lastEventTimekey) {
//		this.lastEventTimekey = lastEventTimekey;
//	}
//
//	public Timestamp getLastEventTime() {
//		return lastEventTime;
//	}
//
//	public void setLastEventTime(Timestamp lastEventTime) {
//		this.lastEventTime = lastEventTime;
//	}
//
//	public String getLastEventUser() {
//		return lastEventUser;
//	}
//
//	public void setLastEventUser(String lastEventUser) {
//		this.lastEventUser = lastEventUser;
//	}
//
//	public String getLastEventComment() {
//		return lastEventComment;
//	}
//
//	public void setLastEventComment(String lastEventComment) {
//		this.lastEventComment = lastEventComment;
//	}
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="plDispatchFlagLR", type="Column", dataType="String", initial="", history="")
	private String plDispatchFlagLR;
	
	@CTORMTemplate(seq = "3", name="plDispatchFlagUR", type="Column", dataType="String", initial="", history="")
	private String plDispatchFlagUR;
	
	@CTORMTemplate(seq = "4", name="plWaitTimee2e", type="Column", dataType="Number", initial="", history="")
	private long plWaitTimee2e;
	
	@CTORMTemplate(seq = "5", name="plWaitTimePush", type="Column", dataType="Number", initial="", history="")
	private long plWaitTimePush;
	
	@CTORMTemplate(seq = "6", name="plPullFlag", type="Column", dataType="String", initial="", history="")
	private String plPullFlag;
	
	@CTORMTemplate(seq = "7", name="puDispatchFlagLR", type="Column", dataType="String", initial="", history="")
	private String puDispatchFlagLR;
	
	@CTORMTemplate(seq = "8", name="puDispatchFlagUR", type="Column", dataType="String", initial="", history="")
	private String puDispatchFlagUR;
	
	@CTORMTemplate(seq = "9", name="puWaitTimee2e", type="Column", dataType="Number", initial="", history="")
	private long puWaitTimee2e;
	
	@CTORMTemplate(seq = "10", name="puWaitTimePush", type="Column", dataType="Number", initial="", history="")
	private long puWaitTimePush;
	
	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "12", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "13", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public DspMachineDispatch()
	{
		
	}
	public DspMachineDispatch(String machineName)
	{
		this.setMachineName(machineName);
	}
	
	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getPlDispatchFlagLR() {
		return plDispatchFlagLR;
	}

	public void setPlDispatchFlagLR(String plDispatchFlagLR) {
		this.plDispatchFlagLR = plDispatchFlagLR;
	}
	
	public String getPlDispatchFlagUR() {
		return plDispatchFlagUR;
	}

	public void setPlDispatchFlagUR(String plDispatchFlagUR) {
		this.plDispatchFlagUR = plDispatchFlagUR;
	}

	public long getPlWaitTimee2e() {
		return plWaitTimee2e;
	}

	public void setPlWaitTimee2e(long plWaitTimee2e) {
		this.plWaitTimee2e = plWaitTimee2e;
	}

	public long getPlWaitTimePush() {
		return plWaitTimePush;
	}

	public void setPlWaitTimePush(long plWaitTimePush) {
		this.plWaitTimePush = plWaitTimePush;
	}

	public String getPlPullFlag() {
		return plPullFlag;
	}

	public void setPlPullFlag(String plPullFlag) {
		this.plPullFlag = plPullFlag;
	}

	public String getPuDispatchFlagLR() {
		return puDispatchFlagLR;
	}

	public void setPuDispatchFlagLR(String puDispatchFlagLR) {
		this.puDispatchFlagLR = puDispatchFlagLR;
	}
	
	public String getPuDispatchFlagUR() {
		return puDispatchFlagUR;
	}

	public void setPuDispatchFlagUR(String puDispatchFlagUR) {
		this.puDispatchFlagUR = puDispatchFlagUR;
	}

	public long getPuWaitTimee2e() {
		return puWaitTimee2e;
	}

	public void setPuWaitTimee2e(long puWaitTimee2e) {
		this.puWaitTimee2e = puWaitTimee2e;
	}

	public long getPuWaitTimePush() {
		return puWaitTimePush;
	}

	public void setPuWaitTimePush(long puWaitTimePush) {
		this.puWaitTimePush = puWaitTimePush;
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