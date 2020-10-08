package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspReserveLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="carrierName", type="Key", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "3", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "4", name="reserveState", type="Column", dataType="String", initial="", history="")
	private String reserveState;
	
	@CTORMTemplate(seq = "5", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "6", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "8", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public DspReserveLot()
	{
		
	}
	public DspReserveLot(String machineName, String carrierName)
	{
		this.setMachineName(machineName);
		this.setCarrierName(carrierName);
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getReserveState() {
		return reserveState;
	}
	public void setReserveState(String reserveState) {
		this.reserveState = reserveState;
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
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
