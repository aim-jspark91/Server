package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspStockerRegion extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="stockerName", type="Key", dataType="String", initial="", history="")
	private String stockerName;
	
	@CTORMTemplate(seq = "2", name="stockerRegionType", type="Key", dataType="String", initial="", history="")
	private String stockerRegionType;
	
	@CTORMTemplate(seq = "3", name="setCount", type="Column", dataType="Number", initial="", history="")
	private long setCount;
	
	@CTORMTemplate(seq = "4", name="thresHoldCount", type="Column", dataType="Number", initial="", history="")
	private long thresHoldCount;
	
	@CTORMTemplate(seq = "5", name="gabageTime", type="Column", dataType="Number", initial="", history="")
	private long gabageTime;
	
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

	public DspStockerRegion()
	{
		
	}
	public DspStockerRegion(String stockerName, String stockerRegionType)
	{
		this.setStockerName(stockerName);
		this.setStockerRegionType(stockerRegionType);
	}
	public String getStockerName() {
		return stockerName;
	}
	public void setStockerName(String stockerName) {
		this.stockerName = stockerName;
	}
	public String getStockerRegionType() {
		return stockerRegionType;
	}
	public void setStockerRegionType(String stockerRegionType) {
		this.stockerRegionType = stockerRegionType;
	}
	public long getSetCount() {
		return setCount;
	}
	public void setSetCount(long setCount) {
		this.setCount = setCount;
	}
	public long getThresHoldCount() {
		return thresHoldCount;
	}
	public void setThresHoldCount(long thresHoldCount) {
		this.thresHoldCount = thresHoldCount;
	}
	public long getGabageTime() {
		return gabageTime;
	}
	public void setGabageTime(long gabageTime) {
		this.gabageTime = gabageTime;
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
