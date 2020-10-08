package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspStockerZoneEmptyCST extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="stockerName", type="Key", dataType="String", initial="", history="")
	private String stockerName;
	
	@CTORMTemplate(seq = "2", name="zoneName", type="Key", dataType="String", initial="", history="")
	private String zoneName;
	
	@CTORMTemplate(seq = "3", name="fromStockerName", type="Key", dataType="String", initial="", history="")
	private String fromStockerName;
	
	@CTORMTemplate(seq = "4", name="fromZoneName", type="Key", dataType="String", initial="", history="")
	private String fromZoneName;
	
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

	public DspStockerZoneEmptyCST()
	{
		
	}
	public DspStockerZoneEmptyCST(String stockerName, String zoneName, String fromStockerName, String fromZoneName)
	{
		this.setStockerName(stockerName);
		this.setZoneName(zoneName);
		this.setFromStockerName(fromStockerName);
		this.setFromZoneName(fromZoneName);
	}
	
	public String getStockerName() {
		return stockerName;
	}
	public void setStockerName(String stockerName) {
		this.stockerName = stockerName;
	}
	public String getZoneName() {
		return zoneName;
	}
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	public String getFromStockerName() {
		return fromStockerName;
	}
	public void setFromStockerName(String fromStockerName) {
		this.fromStockerName = fromStockerName;
	}
	public String getFromZoneName() {
		return fromZoneName;
	}
	public void setFromZoneName(String fromZoneName) {
		this.fromZoneName = fromZoneName;
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
