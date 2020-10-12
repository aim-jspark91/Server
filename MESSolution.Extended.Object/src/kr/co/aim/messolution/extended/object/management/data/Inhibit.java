package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Inhibit extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="inhibitID", type="Key", dataType="String", initial="", history="")
	private String inhibitID;
	
	@CTORMTemplate(seq = "2", name="ecCode", type="Column", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "3", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "4", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "5", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "6", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "7", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "8", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "9", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "10", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "11", name="recipeName", type="Column", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "12", name="exceptionLotCount", type="Column", dataType="Long", initial="", history="")
	private long exceptionLotCount;
	
	@CTORMTemplate(seq = "13", name="processLotCount", type="Column", dataType="Long", initial="", history="")
	private long processLotCount;
	
	@CTORMTemplate(seq = "14", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "15", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "16", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "17", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "18", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "19", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "20", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "21", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "22", name="createUser", type="Column", dataType="String", initial="", history="N")
	private String createUser;

	public Inhibit()
	{
		
	}
	
	public Inhibit(String inhibitID)
	{
		setInhibitID(inhibitID);
	}
		
	public String getInhibitID() {
		return inhibitID;
	}

	public void setInhibitID(String inhibitID) {
		this.inhibitID = inhibitID;
	}

	public String getEcCode() {
		return ecCode;
	}

	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
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

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
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

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public long getExceptionLotCount() {
		return exceptionLotCount;
	}

	public void setExceptionLotCount(long exceptionLotCount) {
		this.exceptionLotCount = exceptionLotCount;
	}

	public long getProcessLotCount() {
		return processLotCount;
	}

	public void setProcessLotCount(long processLotCount) {
		this.processLotCount = processLotCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
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
