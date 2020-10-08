package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductFlag extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "2", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "3", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "4", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "5", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "7", name="processTurnFlag", type="Column", dataType="String", initial="", history="")
	private String processTurnFlag;
	
	@CTORMTemplate(seq = "8", name="turnOverFlag", type="Column", dataType="String", initial="", history="")
	private String turnOverFlag;
		
	@CTORMTemplate(seq = "10", name="elaQTimeFlag", type="Column", dataType="String", initial="", history="")
	private String elaQTimeFlag;
	
	@CTORMTemplate(seq = "11", name="trackFlag", type="Column", dataType="String", initial="", history="")
	private String trackFlag;
	
	//instantiation
	public ProductFlag()
	{
		
	}

	public ProductFlag(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getProcessTurnFlag() {
		return processTurnFlag;
	}

	public void setProcessTurnFlag(String processTurnFlag) {
		this.processTurnFlag = processTurnFlag;
	}

	public String getTurnOverFlag() {
		return turnOverFlag;
	}

	public void setTurnOverFlag(String turnOverFlag) {
		this.turnOverFlag = turnOverFlag;
	}

	public String getelaQTimeFlag() {
		return elaQTimeFlag;
	}

	public void setelaQTimeFlag(String elaQTimeFlag) {
		this.elaQTimeFlag = elaQTimeFlag;
	}

	public String getTrackFlag() {
		return trackFlag;
	}

	public void setTrackFlag(String trackFlag) {
		this.trackFlag = trackFlag;
	}
	
	
	
	
}