package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassLogM extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="dataType", type="Key", dataType="String", initial="", history="")
	private String dataType;
	
	@CTORMTemplate(seq = "2", name="dataId", type="Key", dataType="String", initial="", history="")
	private String dataId;
	
	@CTORMTemplate(seq = "3", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	
	@CTORMTemplate(seq = "4", name="createTime", type="Key", dataType="String", initial="", history="")
	private String createTime;
	
	@CTORMTemplate(seq = "5", name="createUser", type="Key", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "6", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "7", name="judge", type="Column", dataType="String", initial="", history="")
	private String judge;
	
	@CTORMTemplate(seq = "8", name="operator", type="Column", dataType="String", initial="", history="")
	private String operator;
	
	@CTORMTemplate(seq = "9", name="confirmor", type="Column", dataType="String", initial="", history="")
	private String confirmor;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getTimeKey() {
		return timeKey;
	}

	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getJudge() {
		return judge;
	}

	public void setJudge(String judge) {
		this.judge = judge;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getConfirmor() {
		return confirmor;
	}

	public void setConfirmor(String confirmor) {
		this.confirmor = confirmor;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
}
