package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class PMCode extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="pmCode", type="Key", dataType="String", initial="", history="")
	private String pmCode;

	@CTORMTemplate(seq = "2", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "3", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "4", name="maintenanceName", type="Column", dataType="String", initial="", history="")
	private String maintenanceName;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "6", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "7", name="machineType", type="Column", dataType="String", initial="", history="")
	private String machineType;
	
	@CTORMTemplate(seq = "8", name="maintDesc", type="Column", dataType="String", initial="", history="")
	private String maintDesc;
	
	@CTORMTemplate(seq = "9", name="maintControlDesc", type="Column", dataType="String", initial="", history="")
	private String maintControlDesc;
	
	@CTORMTemplate(seq = "10", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "11", name="maintPurposeDesc", type="Column", dataType="String", initial="", history="")
	private String maintPurposeDesc;
	
	@CTORMTemplate(seq = "12", name="maintFrequency", type="Column", dataType="String", initial="", history="")
	private String maintFrequency;
	
	@CTORMTemplate(seq = "13", name="frequencyValue", type="Column", dataType="String", initial="", history="")
	private String frequencyValue;
	
	@CTORMTemplate(seq = "14", name="maintEarlyValue", type="Column", dataType="String", initial="", history="")
	private String maintEarlyValue;
	
	@CTORMTemplate(seq = "15", name="maintLimitValue", type="Column", dataType="String", initial="", history="")
	private String maintLimitValue;
		
	@CTORMTemplate(seq = "16", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "17", name="maintManPower", type="Column", dataType="Number", initial="", history="")
	private Number maintManPower;
	
	@CTORMTemplate(seq = "18", name="triggerID", type="Column", dataType="String", initial="", history="")
	private String triggerID;
	
	@CTORMTemplate(seq = "19", name="mappingFlag", type="Column", dataType="String", initial="", history="")
	private String mappingFlag;
	
	@CTORMTemplate(seq = "20", name="initializeFlag", type="Column", dataType="String", initial="", history="")
	private String initializeFlag;
	
	@CTORMTemplate(seq = "21", name="codeDesc", type="Column", dataType="String", initial="", history="")
	private String codeDesc;
	
	@CTORMTemplate(seq = "22", name="pmComment", type="Column", dataType="String", initial="", history="")
	private String pmComment;
	
	@CTORMTemplate(seq = "23", name="maintType", type="Column", dataType="String", initial="", history="")
	private String maintType;
	
	@CTORMTemplate(seq = "24", name="startFlag", type="Column", dataType="String", initial="", history="")
	private String startFlag;
	
	@CTORMTemplate(seq = "25", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	public PMCode()
	{
		
	}
	
	public PMCode(String pmCode)
	{
		setPmCode(pmCode);
	}

	public String getPmCode() {
		return pmCode;
	}

	public void setPmCode(String pmCode) {
		this.pmCode = pmCode;
	}

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getMaintenanceName() {
		return maintenanceName;
	}

	public void setMaintenanceName(String maintenanceName) {
		this.maintenanceName = maintenanceName;
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

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public String getMaintDesc() {
		return maintDesc;
	}

	public void setMaintDesc(String maintDesc) {
		this.maintDesc = maintDesc;
	}

	public String getMaintControlDesc() {
		return maintControlDesc;
	}

	public void setMaintControlDesc(String maintControlDesc) {
		this.maintControlDesc = maintControlDesc;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getMaintPurposeDesc() {
		return maintPurposeDesc;
	}

	public void setMaintPurposeDesc(String maintPurposeDesc) {
		this.maintPurposeDesc = maintPurposeDesc;
	}

	public String getMaintFrequency() {
		return maintFrequency;
	}

	public void setMaintFrequency(String maintFrequency) {
		this.maintFrequency = maintFrequency;
	}

	public String getFrequencyValue() {
		return frequencyValue;
	}

	public void setFrequencyValue(String frequencyValue) {
		this.frequencyValue = frequencyValue;
	}

	public String getMaintEarlyValue() {
		return maintEarlyValue;
	}

	public void setMaintEarlyValue(String maintEarlyValue) {
		this.maintEarlyValue = maintEarlyValue;
	}

	public String getMaintLimitValue() {
		return maintLimitValue;
	}

	public void setMaintLimitValue(String maintLimitValue) {
		this.maintLimitValue = maintLimitValue;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public Number getMaintManPower() {
		return maintManPower;
	}

	public void setMaintManPower(Number maintManPower) {
		this.maintManPower = maintManPower;
	}

	public String getTriggerID() {
		return triggerID;
	}

	public void setTriggerID(String triggerID) {
		this.triggerID = triggerID;
	}

	public String getMappingFlag() {
		return mappingFlag;
	}

	public void setMappingFlag(String mappingFlag) {
		this.mappingFlag = mappingFlag;
	}

	public String getInitializeFlag() {
		return initializeFlag;
	}

	public void setInitializeFlag(String initializeFlag) {
		this.initializeFlag = initializeFlag;
	}

	public String getCodeDesc() {
		return codeDesc;
	}

	public void setCodeDesc(String codeDesc) {
		this.codeDesc = codeDesc;
	}

	public String getPmComment() {
		return pmComment;
	}

	public void setPmComment(String pmComment) {
		this.pmComment = pmComment;
	}

	public String getMaintType() {
		return maintType;
	}

	public void setMaintType(String maintType) {
		this.maintType = maintType;
	}

	public String getStartFlag() {
		return startFlag;
	}

	public void setStartFlag(String startFlag) {
		this.startFlag = startFlag;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
}
