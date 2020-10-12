package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspReserveProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name = "unitName", type = "Key", dataType = "String", initial = "", history = "")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="portName", type="Key", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "4", name="reserveName", type="Key", dataType="String", initial="", history="")
	private String reserveName;
	
	@CTORMTemplate(seq = "5", name="position", type="Column", dataType="String", initial="", history="")
	private String position;
	
	@CTORMTemplate(seq = "6", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "7", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "8", name="ecCode", type="Column", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "9", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "10", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "11", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
		
	@CTORMTemplate(seq = "12", name="setCount", type="Column", dataType="Number", initial="", history="")
	private long setCount;
	
	@CTORMTemplate(seq = "13", name="currentCount", type="Column", dataType="Number", initial="", history="")
	private long currentCount;
	
	@CTORMTemplate(seq = "14", name="useFlag", type="Column", dataType="String", initial="", history="")
	private String useFlag;
	
	@CTORMTemplate(seq = "15", name="recycleFlag", type="Column", dataType="String", initial="", history="")
	private String recycleFlag;
	
	@CTORMTemplate(seq = "16", name="skipFlag", type="Column", dataType="String", initial="", history="")
	private String skipFlag;
	
	@CTORMTemplate(seq = "17", name="reserveState", type="Column", dataType="String", initial="", history="")
	private String reserveState;
	
	@CTORMTemplate(seq = "18", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "19", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "20", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "21", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "22", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	//	ADD	2019.01.29	AIM.YUNJM	Mantis : 0002626
	@CTORMTemplate(seq = "23", name="activeFlag", type="Column", dataType="String", initial="", history="")
	private String activeFlag;

	public DspReserveProduct()
	{
		
	}
	public DspReserveProduct(String machineName, String portName, String reserveName,String unitName)
	{
		this.setMachineName(machineName);
		this.setPortName(portName);
		this.setReserveName(reserveName);
		this.setUnitName(unitName);
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
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getReserveName() {
		return reserveName;
	}
	public void setReserveName(String reserveName) {
		this.reserveName = reserveName;
	}
	public String getPositionName() {
		return position;
	}
	public void setPositionName(String position) {
		this.position = position;
	}
	public String getProductionType() {
		return productionType;
	}
	public void setProductionType(String productionType) {
		this.productionType = productionType;
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
	public String getProcessOperationName() {
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	public String getMachineGroupName() {
		return processOperationName;
	}
	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}
	public long getSetCount() {
		return setCount;
	}
	public void setSetCount(long setCount) {
		this.setCount = setCount;
	}
	public long getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(long currentCount) {
		this.currentCount = currentCount;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
	}
	public String getRecycleFlag() {
		return recycleFlag;
	}
	public void setRecycleFlag(String recycleFlag) {
		this.recycleFlag = recycleFlag;
	}
	public String getSkipFlag() {
		return skipFlag;
	}
	public void setSkipFlag(String skipFlag) {
		this.skipFlag = skipFlag;
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
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getReserveState() {
		return reserveState;
	}
	public void setReserveState(String reserveState) {
		this.reserveState = reserveState;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}
	
}
