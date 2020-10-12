package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskSheet extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="sheetName", type="Key", dataType="String", initial="", history="")
	private String sheetName;
	
	@CTORMTemplate(seq = "2", name="sheetType", type="Column", dataType="String", initial="", history="")
	private String sheetType;
	
	@CTORMTemplate(seq = "3", name="sheetGrade", type="Column", dataType="String", initial="", history="")
	private String sheetGrade;
	
	@CTORMTemplate(seq = "4", name="sheetState", type="Column", dataType="String", initial="", history="")
	private String sheetState;
	
	@CTORMTemplate(seq = "5", name="trayName", type="Column", dataType="String", initial="", history="")
	private String trayName;
	
	@CTORMTemplate(seq = "6", name="trayPosition", type="Column", dataType="Long", initial="", history="")
	private long trayPosition;
	
	@CTORMTemplate(seq = "7", name="maskName", type="Column", dataType="String", initial="", history="")
	private String maskName;
	
	@CTORMTemplate(seq = "8", name="maskPosition", type="Column", dataType="Long", initial="", history="")
	private long maskPosition;
	
	@CTORMTemplate(seq = "9", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "10", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "13", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	public String getsheetName() {
		return sheetName;
	}
	public void setsheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	public String getsheetType() {
		return sheetType;
	}
	public void setsheetType(String sheetType) {
		this.sheetType = sheetType;
	}
	
	public String getsheetGrade() {
		return sheetGrade;
	}
	public void setsheetGrade(String sheetGrade) {
		this.sheetGrade = sheetGrade;
	}
	
	public String getsheetState() {
		return sheetState;
	}
	public void setsheetState(String sheetState) {
		this.sheetState = sheetState;
	}
	
	public String gettrayName() {
		return trayName;
	}
	public void settrayName(String trayName) {
		this.trayName = trayName;
	}
	
	public long gettrayPosition() {
		return trayPosition;
	}
	public void settrayPosition(long trayPosition) {
		this.trayPosition = trayPosition;
	}
	
	public String getmaskName() {
		return maskName;
	}
	public void setmaskName(String maskName) {
		this.maskName = maskName;
	}
	
	public long getmaskPosition() {
		return maskPosition;
	}
	public void setmaskPosition(long maskPosition) {
		this.maskPosition = maskPosition;
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
	public MaskSheet()
	{
		
	}
	
	public MaskSheet(String sheetName)
	{
		setsheetName(sheetName);
	}
}
