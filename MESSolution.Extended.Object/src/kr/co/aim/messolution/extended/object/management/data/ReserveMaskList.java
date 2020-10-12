package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveMaskList extends UdfAccessor {
// key : conditionID

	@CTORMTemplate(seq = "1", name="conditionID", type="Key", dataType="String", initial="", history="")
	private String conditionID;

	@CTORMTemplate(seq = "2", name="maskName", type="Column", dataType="String", initial="", history="")
	private String maskName;
	
	@CTORMTemplate(seq = "3", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "4", name="portName", type="Column", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "5", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "6", name="positionName", type="Column", dataType="String", initial="", history="")
	private String positionName;

	@CTORMTemplate(seq = "7", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "8", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "9", name="maskCleanRecipe", type="Column", dataType="String", initial="", history="")
	private String maskCleanRecipe;
	
	@CTORMTemplate(seq = "10", name="offSetPre", type="Column", dataType="String", initial="", history="")
	private String offSetPre;

	@CTORMTemplate(seq = "11", name="offSetMark", type="Column", dataType="String", initial="", history="")
	private String offSetMark;

	@CTORMTemplate(seq = "12", name="state", type="Column", dataType="String", initial="", history="")
	private String state;

	@CTORMTemplate(seq = "13", name="carrierType", type="Column", dataType="String", initial="", history="")
	private String carrierType;

	@CTORMTemplate(seq = "14", name="workFlow", type="Column", dataType="String", initial="", history="")
	private String workFlow;

	public ReserveMaskList()
	{
		
	}
	
	public ReserveMaskList(String conditionID)
	{
		setConditionID(conditionID);
	}
	
	public String getConditionID() {
		return conditionID;
	}
	public void setConditionID(String conditionID) {
		this.conditionID = conditionID;
	}
	
	public String getMaskName() {
		return maskName;
	}
	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}
	
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getPortName() {
		return portName;
	}
	public void setPortname(String portname) {
		this.portName = portname;
	} 
	
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
	public String getPosition() {
		return positionName;
	}
	public void setPosition(String positionName) {
		this.positionName = positionName;
	}

	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getSubUnitName() {
		return subUnitName;
	}
	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}
	
	public String getMaskCleanRecipe() {
		return maskCleanRecipe;
	}
	public void setMaskCleanRecipe(String maskCleanRecipe) {
		this.maskCleanRecipe = maskCleanRecipe;
	}
	
	public String getOffSetPre() {
		return offSetPre;
	}
	public void setOffSetPre(String offSetPre) {
		this.offSetPre = offSetPre;
	}
	
	public String getOffSetMark() {
		return offSetMark;
	}
	public void setOffSetMark(String offSetMark) {
		this.offSetMark = offSetMark;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public String getCarrierType() {
		return carrierType;
	}
	public void setCarrierType(String carrierType) {
		this.carrierType = carrierType;
	}
	
	public String getWorkFlow() {
		return workFlow;
	}
	public void setWorkFlow(String workFlow) {
		this.workFlow = workFlow;
	}
}
