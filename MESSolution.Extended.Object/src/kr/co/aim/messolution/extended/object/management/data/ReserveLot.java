package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "4", name="reserveState", type="Column", dataType="String", initial="", history="")
	private String reserveState;
	
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "7", name="reserveTimeKey", type="Column", dataType="String", initial="", history="")
	private String reserveTimeKey;
	
	@CTORMTemplate(seq = "8", name="inputTimeKey", type="Column", dataType="String", initial="", history="")
	private String inputTimeKey;
	
	@CTORMTemplate(seq = "9", name="completeTimeKey", type="Column", dataType="String", initial="", history="")
	private String completeTimeKey;
	
	@CTORMTemplate(seq = "10", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "11", name="planReleasedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planReleasedTime;
	
	@CTORMTemplate(seq = "12", name="reserveUser", type="Column", dataType="String", initial="", history="")
	private String reserveUser;
	
	@CTORMTemplate(seq = "13", name="holdFlag", type="Column", dataType="String", initial="", history="")
	private String holdFlag;

	@CTORMTemplate(seq = "14", name="departmentName", type="Column", dataType="String", initial="", history="")
	private String departmentName;

	
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	}
	public String getReserveState() {
		return reserveState;
	}
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationName()
	{
		return processOperationName;
	}
	public String getReserveTimeKey() {
		return reserveTimeKey;
	}
	public void setReserveTimeKey(String reserveTimeKey) {
		this.reserveTimeKey = reserveTimeKey;
	}

	public String getInputTimeKey() {
		return inputTimeKey;
	}
	public void setInputTimeKey(String inputTimeKey) {
		this.inputTimeKey = inputTimeKey;
	}
	public String getCompleteTimeKey() {
		return completeTimeKey;
	}
	public void setCompleteTimeKey(String completeTimeKey) {
		this.completeTimeKey = completeTimeKey;
	}
	public String getInputFlag() {
		return reserveState;
	}
	public void setReserveState(String reserveState) {
		this.reserveState = reserveState;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getProductRequestName() {
		return productRequestName;
	}
	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}
	public Timestamp getPlanReleasedTime() {
		return planReleasedTime;
	}
	public void setPlanReleasedTime(Timestamp planReleasedTime) {
		this.planReleasedTime = planReleasedTime;
	}
	public String getReserveUser() {
		return reserveUser;
	}
	public void setReserveUser(String reserveUser) {
		this.reserveUser = reserveUser;
	}

	public String getHoldFlag() {
		return holdFlag;
	}
	public void setHoldFlag(String holdFlag) {
		this.holdFlag = holdFlag;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	//instantiation
	public ReserveLot()
	{
		
	}
	
	public ReserveLot(String machineName, String lotName)
	{
		setMachineName(machineName);
		setLotName(lotName);
	}
}
