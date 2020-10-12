package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DefectResult extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="DefectResultID", type="Key", dataType="Number", initial="", history="")
	private long DefectResultID;
	
	@CTORMTemplate(seq = "2", name="FactoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="ProductSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "4", name="ProductSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "5", name="LotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "6", name="CarrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "7", name="ProductName", type="Column", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "8", name="PanelName", type="Column", dataType="String", initial="", history="")
	private String panelName;
	
	@CTORMTemplate(seq = "9", name="CutNumber", type="Column", dataType="String", initial="", history="")
	private String cutNumber;
	
	@CTORMTemplate(seq = "10", name="CoordiNate_x", type="Column", dataType="String", initial="", history="")
	private String coordiNate_x;
	
	@CTORMTemplate(seq = "11", name="CoordiNate_y", type="Column", dataType="String", initial="", history="")
	private String coordiNate_y;
	
	@CTORMTemplate(seq = "12", name="OriginLayer", type="Column", dataType="String", initial="", history="")
	private String originLayer;

	@CTORMTemplate(seq = "13", name="ProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;

	@CTORMTemplate(seq = "14", name="ProcessOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "15", name="MachineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	

	@CTORMTemplate(seq = "16", name="DefectCode", type="Column", dataType="String", initial="", history="")
	private String defectCode;
	
	@CTORMTemplate(seq = "17", name="DefectSize", type="Column", dataType="String", initial="", history="")
	private String defectSize;
	
	@CTORMTemplate(seq = "18", name="AutoJudgeFlag", type="Column", dataType="String", initial="", history="")
	private String autoJudgeFlag;
	
	@CTORMTemplate(seq = "19", name="Note", type="Column", dataType="String", initial="", history="")
	private String note;
	
	@CTORMTemplate(seq = "20", name="InspectionTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp inspectionTime;
	
	@CTORMTemplate(seq = "21", name="holdFlag", type="Column", dataType="String", initial="", history="")
	private String holdFlag;
	
	@CTORMTemplate(seq = "22", name="mailFlag", type="Column", dataType="String", initial="", history="")
	private String mailFlag;
	
	@CTORMTemplate(seq = "23", name="userGroupName", type="Column", dataType="String", initial="", history="")
	private String userGroupName;
	
	@CTORMTemplate(seq = "24", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "25", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "26", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "27", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "28", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public DefectResult()
	{
		
	}
	
	public DefectResult(long defectResultID)
	{
		this.setDefectResultID(defectResultID);
	}

	public long getDefectResultID() {
		return DefectResultID;
	}

	public void setDefectResultID(long defectResultID) {
		DefectResultID = defectResultID;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductSpecVersion() {
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public String getCutNumber() {
		return cutNumber;
	}

	public void setCutNumber(String cutNumber) {
		this.cutNumber = cutNumber;
	}

	public String getCoordiNate_x() {
		return coordiNate_x;
	}

	public void setCoordiNate_x(String coordiNate_x) {
		this.coordiNate_x = coordiNate_x;
	}

	public String getCoordiNate_y() {
		return coordiNate_y;
	}

	public void setCoordiNate_y(String coordiNate_y) {
		this.coordiNate_y = coordiNate_y;
	}

	public String getOriginLayer() {
		return originLayer;
	}

	public void setOriginLayer(String originLayer) {
		this.originLayer = originLayer;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getProcessOperationVersion() {
		return processOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getDefectCode() {
		return defectCode;
	}

	public void setDefectCode(String defectCode) {
		this.defectCode = defectCode;
	}

	public String getDefectSize() {
		return defectSize;
	}

	public void setDefectSize(String defectSize) {
		this.defectSize = defectSize;
	}

	public String getAutoJudgeFlag() {
		return autoJudgeFlag;
	}

	public void setAutoJudgeFlag(String autoJudgeFlag) {
		this.autoJudgeFlag = autoJudgeFlag;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Timestamp getInspectionTime() {
		return inspectionTime;
	}

	public void setInspectionTime(Timestamp inspectionTime) {
		this.inspectionTime = inspectionTime;
	}

	public String getHoldFlag() {
		return holdFlag;
	}

	public void setHoldFlag(String holdFlag) {
		this.holdFlag = holdFlag;
	}

	public String getMailFlag() {
		return mailFlag;
	}

	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}


	
}
