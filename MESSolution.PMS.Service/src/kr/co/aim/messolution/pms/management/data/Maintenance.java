package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class Maintenance extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="maintenanceID", type="Key", dataType="String", initial="", history="")
	private String maintenanceID;

	@CTORMTemplate(seq = "2", name="pmCode", type="Column", dataType="String", initial="", history="")
	private String pmCode;
	
	@CTORMTemplate(seq = "3", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "4", name="className", type="Column", dataType="String", initial="", history="")
	private String className;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "6", name="machineType", type="Column", dataType="String", initial="", history="")
	private String machineType;
	
	@CTORMTemplate(seq = "7", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "8", name="maintName", type="Column", dataType="String", initial="", history="")
	private String maintName;
	
	@CTORMTemplate(seq = "9", name="maintType", type="Column", dataType="String", initial="", history="")
	private String maintType;
	
	@CTORMTemplate(seq = "10", name="maintDesc", type="Column", dataType="String", initial="", history="")
	private String maintDesc;
	
	@CTORMTemplate(seq = "11", name="maintControlDesc", type="Column", dataType="String", initial="", history="")
	private String maintControlDesc;
	
	@CTORMTemplate(seq = "12", name="maintPurposeDesc", type="Column", dataType="String", initial="", history="")
	private String maintPurposeDesc;
	
	@CTORMTemplate(seq = "13", name="maintPlanDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maintPlanDate;
	
	@CTORMTemplate(seq = "14", name="maintStatus", type="Column", dataType="String", initial="", history="")
	private String maintStatus;
	
	@CTORMTemplate(seq = "15", name="maintStartDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maintStartDate;
	
	@CTORMTemplate(seq = "16", name="maintEndDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maintEndDate;
	
	@CTORMTemplate(seq = "17", name="maintEarlyDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maintEarlyDate;
	
	@CTORMTemplate(seq = "18", name="maintLimitDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maintLimitDate;
	
	@CTORMTemplate(seq = "19", name="maintElapsedDate", type="Column", dataType="String", initial="", history="")
	private String maintElapsedDate;

	@CTORMTemplate(seq = "20", name="maintDoDesc", type="Column", dataType="String", initial="", history="")
	private String maintDoDesc;
	
	@CTORMTemplate(seq = "21", name="executeUser", type="Column", dataType="String", initial="", history="")
	private String executeUser;
		
	@CTORMTemplate(seq = "22", name="maintHelper", type="Column", dataType="String", initial="", history="")
	private String maintHelper;
	
	@CTORMTemplate(seq = "23", name="checkUser", type="Column", dataType="String", initial="", history="")
	private String checkUser;
	
	@CTORMTemplate(seq = "24", name="checkResult", type="Column", dataType="String", initial="", history="")
	private String checkResult;
	
	@CTORMTemplate(seq = "25", name="checkAction", type="Column", dataType="String", initial="", history="")
	private String checkAction;

	@CTORMTemplate(seq = "26", name="evaluationUser", type="Column", dataType="String", initial="", history="")
	private String evaluationUser;
	
	@CTORMTemplate(seq = "27", name="evaluationTime", type="Column", dataType="String", initial="", history="")
	private String evaluationTime;
		
	@CTORMTemplate(seq = "28", name="evaluationDesc", type="Column", dataType="String", initial="", history="")
	private String evaluationDesc;
	
	@CTORMTemplate(seq = "29", name="cancelFlag", type="Column", dataType="String", initial="", history="")
	private String cancelFlag;
	
	@CTORMTemplate(seq = "30", name="cancelUser", type="Column", dataType="String", initial="", history="")
	private String cancelUser;
	
	@CTORMTemplate(seq = "31", name="cancelTime", type="Column", dataType="String", initial="", history="")
	private String cancelTime;

	@CTORMTemplate(seq = "32", name="remark", type="Column", dataType="String", initial="", history="")
	private String remark;
	
	public Maintenance()
	{
		
	}
	
	public Maintenance(String maintenanceID )
	{
		setMaintenanceID(maintenanceID);
	}

	public String getMaintenanceID() {
		return maintenanceID;
	}

	public void setMaintenanceID(String maintenanceID) {
		this.maintenanceID = maintenanceID;
	}

	public String getPmCode() {
		return pmCode;
	}

	public void setPmCode(String pmCode) {
		this.pmCode = pmCode;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getMaintName() {
		return maintName;
	}

	public void setMaintName(String maintName) {
		this.maintName = maintName;
	}

	public String getMaintType() {
		return maintType;
	}

	public void setMaintType(String maintType) {
		this.maintType = maintType;
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

	public String getMaintPurposeDesc() {
		return maintPurposeDesc;
	}

	public void setMaintPurposeDesc(String maintPurposeDesc) {
		this.maintPurposeDesc = maintPurposeDesc;
	}

	public Timestamp getMaintPlanDate() {
		return maintPlanDate;
	}

	public void setMaintPlanDate(Timestamp maintPlanDate) {
		this.maintPlanDate = maintPlanDate;
	}

	public String getMaintStatus() {
		return maintStatus;
	}

	public void setMaintStatus(String maintStatus) {
		this.maintStatus = maintStatus;
	}

	public Timestamp getMaintStartDate() {
		return maintStartDate;
	}

	public void setMaintStartDate(Timestamp maintStartDate) {
		this.maintStartDate = maintStartDate;
	}

	public Timestamp getMaintEndDate() {
		return maintEndDate;
	}

	public void setMaintEndDate(Timestamp maintEndDate) {
		this.maintEndDate = maintEndDate;
	}

	public Timestamp getMaintEarlyDate() {
		return maintEarlyDate;
	}

	public void setMaintEarlyDate(Timestamp maintEarlyDate) {
		this.maintEarlyDate = maintEarlyDate;
	}

	public Timestamp getMaintLimitDate() {
		return maintLimitDate;
	}

	public void setMaintLimitDate(Timestamp maintLimitDate) {
		this.maintLimitDate = maintLimitDate;
	}

	public String getMaintElapsedDate() {
		return maintElapsedDate;
	}

	public void setMaintElapsedDate(String maintElapsedDate) {
		this.maintElapsedDate = maintElapsedDate;
	}

	public String getMaintDoDesc() {
		return maintDoDesc;
	}

	public void setMaintDoDesc(String maintDoDesc) {
		this.maintDoDesc = maintDoDesc;
	}

	public String getExecuteUser() {
		return executeUser;
	}

	public void setExecuteUser(String executeUser) {
		this.executeUser = executeUser;
	}

	public String getMaintHelper() {
		return maintHelper;
	}

	public void setMaintHelper(String maintHelper) {
		this.maintHelper = maintHelper;
	}

	public String getCheckUser() {
		return checkUser;
	}

	public void setCheckUser(String checkUser) {
		this.checkUser = checkUser;
	}

	public String getCheckResult() {
		return checkResult;
	}

	public void setCheckResult(String checkResult) {
		this.checkResult = checkResult;
	}

	public String getCheckAction() {
		return checkAction;
	}

	public void setCheckAction(String checkAction) {
		this.checkAction = checkAction;
	}

	public String getEvaluationUser() {
		return evaluationUser;
	}

	public void setEvaluationUser(String evaluationUser) {
		this.evaluationUser = evaluationUser;
	}

	public String getEvaluationTime() {
		return evaluationTime;
	}

	public void setEvaluationTime(String evaluationTime) {
		this.evaluationTime = evaluationTime;
	}

	public String getEvaluationDesc() {
		return evaluationDesc;
	}

	public void setEvaluationDesc(String evaluationDesc) {
		this.evaluationDesc = evaluationDesc;
	}

	public String getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(String cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public String getCancelUser() {
		return cancelUser;
	}

	public void setCancelUser(String cancelUser) {
		this.cancelUser = cancelUser;
	}

	public String getCancelTime() {
		return cancelTime;
	}

	public void setCancelTime(String cancelTime) {
		this.cancelTime = cancelTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	
}
