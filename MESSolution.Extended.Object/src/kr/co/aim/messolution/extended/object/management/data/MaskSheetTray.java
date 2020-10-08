package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskSheetTray extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="trayName", type="Key", dataType="String", initial="", history="")
	private String trayName;
	
	@CTORMTemplate(seq = "2", name="trayState", type="Column", dataType="String", initial="", history="")
	private String trayState;
	
	@CTORMTemplate(seq = "3", name="sheetCount", type="Column", dataType="Long", initial="", history="")
	private long sheetCount;
	
	@CTORMTemplate(seq = "4", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "5", name="carrierPosition", type="Column", dataType="Long", initial="", history="")
	private long carrierPosition;
	
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	public String gettrayName() {
		return trayName;
	}
	public void settrayName(String trayName) {
		this.trayName = trayName;
	}
	
	public String gettrayState() {
		return trayState;
	}
	public void settrayState(String trayState) {
		this.trayState = trayState;
	}
	
	public long getsheetCount() {
		return sheetCount;
	}
	public void setsheetCount(long sheetCount) {
		this.sheetCount = sheetCount;
	}
	
	public String getcarrierName() {
		return carrierName;
	}
	public void setcarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
	public long getcarrierPosition() {
		return carrierPosition;
	}
	public void setcarrierPosition(long carrierPosition) {
		this.carrierPosition = carrierPosition;
	}
	
	public String getlastEventName() {
		return lastEventName;
	}
	public void setlastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getlastEventTimekey() {
		return lastEventTimekey;
	}
	public void setlastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	
	public Timestamp getlastEventTime() {
		return lastEventTime;
	}
	public void setlastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	
	public String getlastEventUser() {
		return lastEventUser;
	}
	public void setlastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	public String getlastEventComment() {
		return lastEventComment;
	}
	public void setlastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	//instantiation
	public MaskSheetTray()
	{
		
	}
	
	public MaskSheetTray(String trayName)
	{
		settrayName(trayName);
	}
}
