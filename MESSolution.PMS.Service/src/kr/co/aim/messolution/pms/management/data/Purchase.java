package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class Purchase extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="poCode", type="Key", dataType="String", initial="", history="")
	private String poCode;

	@CTORMTemplate(seq = "2", name="purchaseType", type="Column", dataType="String", initial="", history="")
	private String purchaseType;
	
	@CTORMTemplate(seq = "3", name="partID", type="Column", dataType="String", initial="", history="")
	private String partID;
	
	@CTORMTemplate(seq = "4", name="availablePeriod", type="Column", dataType="String", initial="", history="")
	private String availablePeriod;
	
	@CTORMTemplate(seq = "5", name="inRequestDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp inRequestDate;
	
	@CTORMTemplate(seq = "6", name="purchaseQuantity", type="Column", dataType="Number", initial="", history="")
	private Number purchaseQuantity;
	
	@CTORMTemplate(seq = "7", name="purchaseUnit", type="Column", dataType="String", initial="", history="")
	private String purchaseUnit;
	
	@CTORMTemplate(seq = "8", name="purchaseReason", type="Column", dataType="String", initial="", history="")
	private String purchaseReason;
	
	@CTORMTemplate(seq = "9", name="purchaseStatus", type="Column", dataType="String", initial="", history="")
	private String purchaseStatus;
	
	@CTORMTemplate(seq = "10", name="inExpectdate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp inExpectdate;
	
	@CTORMTemplate(seq = "11", name="phoneNumber", type="Column", dataType="String", initial="", history="")
	private String phoneNumber;
	
	@CTORMTemplate(seq = "12", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "13", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "14", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "15", name="cancelComment", type="Column", dataType="String", initial="", history="")
	private String cancelComment;
	
	@CTORMTemplate(seq = "16", name="cancelFlag", type="Column", dataType="String", initial="", history="")
	private String cancelFlag;
	
	public Purchase()
	{
		
	}
	
	public Purchase(String poCode)
	{
		setPoCode(poCode);
	}

	public String getPoCode() {
		return poCode;
	}

	public void setPoCode(String poCode) {
		this.poCode = poCode;
	}

	public String getPurchaseType() {
		return purchaseType;
	}

	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public String getAvailablePeriod() {
		return availablePeriod;
	}

	public void setAvailablePeriod(String availablePeriod) {
		this.availablePeriod = availablePeriod;
	}

	public Timestamp getInRequestDate() {
		return inRequestDate;
	}

	public void setInRequestDate(Timestamp inRequestDate) {
		this.inRequestDate = inRequestDate;
	}

	public Number getPurchaseQuantity() {
		return purchaseQuantity;
	}

	public void setPurchaseQuantity(Number purchaseQuantity) {
		this.purchaseQuantity = purchaseQuantity;
	}

	public String getPurchaseUnit() {
		return purchaseUnit;
	}

	public void setPurchaseUnit(String purchaseUnit) {
		this.purchaseUnit = purchaseUnit;
	}

	public String getPurchaseReason() {
		return purchaseReason;
	}

	public void setPurchaseReason(String purchaseReason) {
		this.purchaseReason = purchaseReason;
	}

	public String getPurchaseStatus() {
		return purchaseStatus;
	}

	public void setPurchaseStatus(String purchaseStatus) {
		this.purchaseStatus = purchaseStatus;
	}

	public Timestamp getInExpectdate() {
		return inExpectdate;
	}

	public void setInExpectdate(Timestamp inExceptdate) {
		this.inExpectdate = inExceptdate;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getCancelComment() {
		return cancelComment;
	}

	public void setCancelComment(String cancelComment) {
		this.cancelComment = cancelComment;
	}

	public String getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(String cancelFlag) {
		this.cancelFlag = cancelFlag;
	}
}
