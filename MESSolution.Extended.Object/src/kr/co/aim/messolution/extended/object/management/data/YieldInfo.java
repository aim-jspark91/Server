package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class YieldInfo extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "2", name="ecCode", type="Key", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "3", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "4", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "5", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "6", name="sheetFlag", type="Column", dataType="String", initial="", history="")
	private String sheetFlag;
	
	@CTORMTemplate(seq = "7", name="sheetYield", type="Column", dataType="Number", initial="", history="")
	private double sheetYield;
	
	@CTORMTemplate(seq = "8", name="lotFlag", type="Column", dataType="String", initial="", history="")
	private String lotFlag;

	@CTORMTemplate(seq = "9", name="lotYield", type="Column", dataType="Number", initial="", history="")
	private double lotYield;

	@CTORMTemplate(seq = "10", name="addInfo1", type="Column", dataType="String", initial="", history="")
	private String addInfo1;

	@CTORMTemplate(seq = "11", name="addInfo2", type="Column", dataType="String", initial="", history="")
	private String addInfo2;
	
	@CTORMTemplate(seq = "12", name="addInfo3", type="Column", dataType="String", initial="", history="")
	private String addInfo3;
	
	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "16", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "17", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public YieldInfo()
	{
		
	}
	public YieldInfo(String productSpecName, String ecCode,String processFlowName,String processFlowVersion,String processOperationName)
	{
		this.setProductSpecName(productSpecName);
		this.setEcCode(ecCode);
		this.setProcessFlowName(processFlowName);
		this.setProcessFlowVersion(processFlowVersion);
		this.setProcessOperationName(processOperationName);
	}


	public String getProductSpecName() {
		return productSpecName;
	}


	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}


	public String getEcCode() {
		return ecCode;
	}


	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}


	public String getProcessFlowName() {
		return processFlowName;
	}


	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}


	public String getProcessFlowVersion() {
		return processFlowVersion;
	}


	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}


	public String getProcessOperationName() {
		return processOperationName;
	}


	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}


	public String getSheetFlag() {
		return sheetFlag;
	}


	public void setSheetFlag(String sheetFlag) {
		this.sheetFlag = sheetFlag;
	}


	public double getSheetYield() {
		return sheetYield;
	}


	public void setSheetYield(double sheetYield) {
		this.sheetYield = sheetYield;
	}


	public String getLotFlag() {
		return lotFlag;
	}


	public void setLotFlag(String lotFlag) {
		this.lotFlag = lotFlag;
	}


	public double getLotYield() {
		return lotYield;
	}


	public void setLotYield(double lotYield) {
		this.lotYield = lotYield;
	}


	public String getAddInfo1() {
		return addInfo1;
	}


	public void setAddInfo1(String addInfo1) {
		this.addInfo1 = addInfo1;
	}


	public String getAddInfo2() {
		return addInfo2;
	}


	public void setAddInfo2(String addInfo2) {
		this.addInfo2 = addInfo2;
	}


	public String getAddInfo3() {
		return addInfo3;
	}


	public void setAddInfo3(String addInfo3) {
		this.addInfo3 = addInfo3;
	}


	public String getLastEventName() {
		return lastEventName;
	}


	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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


	public String getLastEventComment() {
		return lastEventComment;
	}


	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	

}
