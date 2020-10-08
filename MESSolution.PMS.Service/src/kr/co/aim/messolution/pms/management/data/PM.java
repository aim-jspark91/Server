package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class PM extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="pmCode", type="Key", dataType="String", initial="", history="")
	private String pmCode;

	@CTORMTemplate(seq = "2", name="executeID", type="Column", dataType="String", initial="", history="")
	private String executeID;
	
	@CTORMTemplate(seq = "3", name="triggerID", type="Column", dataType="String", initial="", history="")
	private String triggerID;
	
	@CTORMTemplate(seq = "4", name="pmName", type="Column", dataType="String", initial="", history="")
	private String pmName;
	
	@CTORMTemplate(seq = "5", name="pmType", type="Column", dataType="String", initial="", history="")
	private String pmType;

	@CTORMTemplate(seq = "6", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "7", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "8", name="maintenanceFreType", type="Column", dataType="String", initial="", history="")
	private String maintenanceFreType;
	
	@CTORMTemplate(seq = "9", name="freValue", type="Column", dataType="String", initial="", history="")
	private String freValue;
	
	@CTORMTemplate(seq = "10", name="shift", type="Column", dataType="String", initial="", history="")
	private String shift;
	
	@CTORMTemplate(seq = "11", name="pmStartTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp pmStartTime;
	
	@CTORMTemplate(seq = "12", name="pmEndTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp pmEndTime;
	
	@CTORMTemplate(seq = "13", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "14", name="pmStatus", type="Column", dataType="String", initial="", history="")
	private String pmStatus;
	
	@CTORMTemplate(seq = "15", name="planManPower", type="Column", dataType="String", initial="", history="")
	private String planManPower;
		
	@CTORMTemplate(seq = "16", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "17", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "18", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "19", name="MaintenanceDesc", type="Column", dataType="String", initial="", history="")
	private String MaintenanceDesc;
	
	@CTORMTemplate(seq = "20", name="InspectResult", type="Column", dataType="String", initial="", history="")
	private String InspectResult;
	
	@CTORMTemplate(seq = "21", name="PmCause", type="Column", dataType="String", initial="", history="")
	private String PmCause;
	
	@CTORMTemplate(seq = "22", name="PmSolution", type="Column", dataType="String", initial="", history="")
	private String PmSolution;
	
	@CTORMTemplate(seq = "23", name="EvaluationComment", type="Column", dataType="String", initial="", history="")
	private String EvaluationComment;
	
	@CTORMTemplate(seq = "24", name="MaintenanceComment", type="Column", dataType="String", initial="", history="")
	private String MaintenanceComment;
	
	public PM()
	{
		
	}
	
	public PM(String pmCode )
	{
		setPmCode(pmCode);
	}

	public String getPmCode() {
		return pmCode;
	}

	public void setPmCode(String pmCode) {
		this.pmCode = pmCode;
	}

	public String getExecuteID() {
		return executeID;
	}

	public void setExecuteID(String executeID) {
		this.executeID = executeID;
	}

	public String getTriggerID() {
		return triggerID;
	}

	public void setTriggerID(String triggerID) {
		this.triggerID = triggerID;
	}

	public String getPmName() {
		return pmName;
	}

	public void setPmName(String pmName) {
		this.pmName = pmName;
	}

	public String getPmType() {
		return pmType;
	}

	public void setPmType(String pmType) {
		this.pmType = pmType;
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

	public String getMaintenanceFreType() {
		return maintenanceFreType;
	}

	public void setMaintenanceFreType(String maintenanceFreType) {
		this.maintenanceFreType = maintenanceFreType;
	}

	public String getFreValue() {
		return freValue;
	}

	public void setFreValue(String freValue) {
		this.freValue = freValue;
	}

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}

	public Timestamp getPmStartTime() {
		return pmStartTime;
	}

	public void setPmStartTime(Timestamp pmStartTime) {
		this.pmStartTime = pmStartTime;
	}

	public Timestamp getPmEndTime() {
		return pmEndTime;
	}

	public void setPmEndTime(Timestamp pmEndTime) {
		this.pmEndTime = pmEndTime;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPmStatus() {
		return pmStatus;
	}

	public void setPmStatus(String pmStatus) {
		this.pmStatus = pmStatus;
	}

	public String getPlanManPower() {
		return planManPower;
	}

	public void setPlanManPower(String planManPower) {
		this.planManPower = planManPower;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getMaintenanceDesc() {
		return MaintenanceDesc;
	}

	public void setMaintenanceDesc(String maintenanceDesc) {
		MaintenanceDesc = maintenanceDesc;
	}

	public String getInspectResult() {
		return InspectResult;
	}

	public void setInspectResult(String inspectResult) {
		InspectResult = inspectResult;
	}

	public String getPmCause() {
		return PmCause;
	}

	public void setPmCause(String pmCause) {
		PmCause = pmCause;
	}

	public String getPmSolution() {
		return PmSolution;
	}

	public void setPmSolution(String pmSolution) {
		PmSolution = pmSolution;
	}

	public String getEvaluationComment() {
		return EvaluationComment;
	}

	public void setEvaluationComment(String evaluationComment) {
		EvaluationComment = evaluationComment;
	}

	public String getMaintenanceComment() {
		return MaintenanceComment;
	}

	public void setMaintenanceComment(String maintenanceComment) {
		MaintenanceComment = maintenanceComment;
	}
}
