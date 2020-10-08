package kr.co.aim.messolution.fgms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShipRequest extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="inVoiceNo", type="Key", dataType="String", initial="", history="")
	private String inVoiceNo;

	@CTORMTemplate(seq = "2", name="inVoiceType", type="Column", dataType="String", initial="", history="")
	private String inVoiceType;
	
	@CTORMTemplate(seq = "3", name="shipRequestState", type="Column", dataType="String", initial="", history="")
	private String shipRequestState;
	
	@CTORMTemplate(seq = "4", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "5", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "6", name="reserveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp reserveTime;
	
	@CTORMTemplate(seq = "7", name="reserveUser", type="Column", dataType="String", initial="", history="")
	private String reserveUser;
	
	@CTORMTemplate(seq = "8", name="confirmTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp confirmTime;
	
	@CTORMTemplate(seq = "9", name="confirmUser", type="Column", dataType="String", initial="", history="")
	private String confirmUser;
	
	@CTORMTemplate(seq = "10", name="completeTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp completeTime;
	
	@CTORMTemplate(seq = "11", name="completeUser", type="Column", dataType="String", initial="", history="")
	private String completeUser;
	
	@CTORMTemplate(seq = "12", name="cancelTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp cancelTime;
	
	@CTORMTemplate(seq = "13", name="CancelUser", type="Column", dataType="String", initial="", history="")
	private String CancelUser;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "16", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "17", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "18", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "19", name="customerNo", type="Column", dataType="String", initial="", history="")
	private String customerNo;
	
	@CTORMTemplate(seq = "20", name="planShipDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planShipDate;
	
	@CTORMTemplate(seq = "21", name="domesticExport", type="Column", dataType="String", initial="", history="")
	private String domesticExport;

	//instantiation
	public ShipRequest()
	{
		
	}
	
	public ShipRequest(String inVoiceNo)
	{
		setInVoiceNo(inVoiceNo);
	}

	public String getInVoiceNo() {
		return inVoiceNo;
	}

	public void setInVoiceNo(String inVoiceNo) {
		this.inVoiceNo = inVoiceNo;
	}

	public String getInVoiceType() {
		return inVoiceType;
	}

	public void setInVoiceType(String inVoiceType) {
		this.inVoiceType = inVoiceType;
	}

	public String getShipRequestState() {
		return shipRequestState;
	}

	public void setShipRequestState(String shipRequestState) {
		this.shipRequestState = shipRequestState;
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

	public Timestamp getReserveTime() {
		return reserveTime;
	}

	public void setReserveTime(Timestamp reserveTime) {
		this.reserveTime = reserveTime;
	}

	public String getReserveUser() {
		return reserveUser;
	}

	public void setReserveUser(String reserveUser) {
		this.reserveUser = reserveUser;
	}

	public Timestamp getConfirmTime() {
		return confirmTime;
	}

	public void setConfirmTime(Timestamp confirmTime) {
		this.confirmTime = confirmTime;
	}

	public String getConfirmUser() {
		return confirmUser;
	}

	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Timestamp completeTime) {
		this.completeTime = completeTime;
	}

	public String getCompleteUser() {
		return completeUser;
	}

	public void setCompleteUser(String completeUser) {
		this.completeUser = completeUser;
	}

	public Timestamp getCancelTime() {
		return cancelTime;
	}

	public void setCancelTime(Timestamp cancelTime) {
		this.cancelTime = cancelTime;
	}

	public String getCancelUser() {
		return CancelUser;
	}

	public void setCancelUser(String cancelUser) {
		CancelUser = cancelUser;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public Timestamp getPlanShipDate() {
		return planShipDate;
	}

	public void setPlanShipDate(Timestamp planShipDate) {
		this.planShipDate = planShipDate;
	}

	public String getDomesticExport() {
		return domesticExport;
	}

	public void setDomesticExport(String domesticExport) {
		this.domesticExport = domesticExport;
	}

	
	
}
