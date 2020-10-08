package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspStockerZone extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="stockerName", type="Key", dataType="String", initial="", history="")
	private String stockerName;
	
	@CTORMTemplate(seq = "2", name="zoneName", type="Key", dataType="String", initial="", history="")
	private String zoneName;
	
	@CTORMTemplate(seq = "3", name="totalCapacity", type="Column", dataType="Number", initial="", history="")
	private long totalCapacity;
	
	@CTORMTemplate(seq = "4", name="highWaterMark", type="Column", dataType="Number", initial="", history="")
	private long highWaterMark;
	
	@CTORMTemplate(seq = "5", name="prohibitedShelfCount", type="Column", dataType="Number", initial="", history="")
	private long prohibitedShelfCount;
	
	@CTORMTemplate(seq = "6", name="usedShelfCount", type="Column", dataType="Number", initial="", history="")
	private long usedShelfCount;
	
	@CTORMTemplate(seq = "7", name="emptyShelfCount", type="Column", dataType="Number", initial="", history="")
	private long emptyShelfCount;
	
	@CTORMTemplate(seq = "8", name="minEmptyCarrierCount", type="Column", dataType="Number", initial="", history="")
	private long minEmptyCarrierCount;
	
	@CTORMTemplate(seq = "9", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "10", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "11", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public DspStockerZone()
	{
		
	}
	public DspStockerZone(String stockerName, String zoneName)
	{
		this.setStockerName(stockerName);
		this.setZoneName(zoneName);
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
	public long getTotalCapacity() {
		return totalCapacity;
	}
	public void setTotalCapacity(long totalCapacity) {
		this.totalCapacity = totalCapacity;
	}
	public long getHighWaterMark() {
		return highWaterMark;
	}
	public void setHighWaterMark(long highWaterMark) {
		this.highWaterMark = highWaterMark;
	}
	public long getProhibitedShelfCount() {
		return prohibitedShelfCount;
	}
	public void setProhibitedShelfCount(long prohibitedShelfCount) {
		this.prohibitedShelfCount = prohibitedShelfCount;
	}
	public long getUsedShelfCount() {
		return usedShelfCount;
	}
	public void setUsedShelfCount(long usedShelfCount) {
		this.usedShelfCount = usedShelfCount;
	}
	public long getEmptyShelfCount() {
		return emptyShelfCount;
	}
	public void setEmptyShelfCount(long emptyShelfCount) {
		this.emptyShelfCount = emptyShelfCount;
	}
	public long getMinEmptyCarrierCount() {
		return minEmptyCarrierCount;
	}
	public void setMinEmptyCarrierCount(long minEmptyCarrierCount) {
		this.minEmptyCarrierCount = minEmptyCarrierCount;
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
