package kr.co.aim.messolution.fgms.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FGMSProductRequest extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "2", name="SEQ", type="Key", dataType="String", initial="", history="")
	private String SEQ;

	@CTORMTemplate(seq = "3", name="planQty", type="Column", dataType="String", initial="", history="")
	private String planQty;
	
	@CTORMTemplate(seq = "4", name="supperAreaName", type="Column", dataType="String", initial="", history="")
	private String supperAreaName;
	
	@CTORMTemplate(seq = "5", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	@CTORMTemplate(seq = "6", name="MTO", type="Column", dataType="String", initial="", history="")
	private String MTO;
	
	@CTORMTemplate(seq = "7", name="infoResultStatus", type="Column", dataType="String", initial="", history="")
	private String infoResultStatus;
	
	@CTORMTemplate(seq = "8", name="infoResultMessage", type="Column", dataType="String", initial="", history="")
	private String infoResultMessage;
	
	@CTORMTemplate(seq = "9", name="writeDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp writeDate;
	
	@CTORMTemplate(seq = "10", name="readDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp readDate;
	
	@CTORMTemplate(seq = "11", name="receiveflag", type="Column", dataType="String", initial="", history="")
	private String receiveflag;
	
	
	
	//instantiation
	public FGMSProductRequest()
	{
		
	}
	
	public FGMSProductRequest(String productRequestName)
	{
		setProductRequestName(productRequestName);
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getPlanQty() {
		return planQty;
	}

	public void setPlanQty(String planQty) {
		this.planQty = planQty;
	}

	public String getSupperAreaName() {
		return supperAreaName;
	}

	public void setSupperAreaName(String supperAreaName) {
		this.supperAreaName = supperAreaName;
	}
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getMTO() {
		return MTO;
	}

	public void setMTO(String MTO) {
		this.MTO = MTO;
	}

	public String getSEQ() {
		return SEQ;
	}

	public void setSEQ(String SEQ) {
		this.SEQ = SEQ;
	}

	public String getInfoResultStatus() {
		return infoResultStatus;
	}

	public void setInfoResultStatus(String infoResultStatus) {
		this.infoResultStatus = infoResultStatus;
	}

	public String getInfoResultMessage() {
		return infoResultMessage;
	}

	public void setInfoResultMessage(String infoResultMessage) {
		this.infoResultMessage = infoResultMessage;
	}

	public Timestamp getWriteDate() {
		return writeDate;
	}

	public void setWriteDate(Timestamp writeDate) {
		this.writeDate = writeDate;
	}

	public Timestamp getReadDate() {
		return readDate;
	}

	public void setReadDate(Timestamp readDate) {
		this.readDate = readDate;
	}

	public String getReceiveflag() {
		return receiveflag;
	}

	public void setReceiveflag(String receiveflag) {
		this.receiveflag = receiveflag;
	}
}
