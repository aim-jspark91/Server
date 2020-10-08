package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class InhibitException extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name="inhibitID", type="Key", dataType="String", initial="", history="")
	private String inhibitID;
	
	@CTORMTemplate(seq = "3", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "4", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "5", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "6", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "7", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "8", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "9", name="createUser", type="Column", dataType="String", initial="", history="N")
	private String createUser;

	public InhibitException()
	{
		
	}
	
	public InhibitException(String lotName, String inhibitID)
	{
		setLotName(lotName);
		setInhibitID(inhibitID);
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getInhibitID() {
		return inhibitID;
	}

	public void setInhibitID(String inhibitID) {
		this.inhibitID = inhibitID;
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

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
