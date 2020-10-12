package kr.co.aim.messolution.pms.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "PMS", divider="_")
public class BM extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="bmID", type="Key", dataType="String", initial="", history="")
	private String bmID;

	@CTORMTemplate(seq = "2", name="bmType", type="Column", dataType="String", initial="", history="")
	private String bmType;
	
	@CTORMTemplate(seq = "3", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "4", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "5", name="shift", type="Column", dataType="String", initial="", history="")
	private String shift;

	@CTORMTemplate(seq = "6", name="bmEndTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp bmEndTime;
	
	@CTORMTemplate(seq = "7", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "10", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "11", name="bmGrade", type="Column", dataType="String", initial="", history="")
	private String bmGrade;
	
	@CTORMTemplate(seq = "12", name="repairTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp repairTime;
	
	@CTORMTemplate(seq = "13", name="processPassFlag", type="Column", dataType="String", initial="", history="")
	private String processPassFlag;
	
	@CTORMTemplate(seq = "14", name="executeResult", type="Column", dataType="String", initial="", history="")
	private String executeResult;
	
	@CTORMTemplate(seq = "15", name="bmCause", type="Column", dataType="String", initial="", history="")
	private String bmCause;
	
	@CTORMTemplate(seq = "16", name="bmSolution", type="Column", dataType="String", initial="", history="")
	private String bmSolution;
	
	@CTORMTemplate(seq = "17", name="productionEffectFlag", type="Column", dataType="String", initial="", history="")
	private String productionEffectFlag;
	
	@CTORMTemplate(seq = "18", name="bmState", type="Column", dataType="String", initial="", history="")
	private String bmState;
	
	@CTORMTemplate(seq = "19", name="evaluationComment", type="Column", dataType="String", initial="", history="")
	private String evaluationComment;

	@CTORMTemplate(seq = "20", name="cumulativeTime", type="Column", dataType="String", initial="", history="")
	private String cumulativeTime;
	
	@CTORMTemplate(seq = "21", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "22", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "23", name="NGReason", type="Column", dataType="String", initial="", history="")
	private String NGReason;
	@CTORMTemplate(seq = "24", name="QAEvaluationComment", type="Column", dataType="String", initial="", history="")
	private String QAEvaluationComment;
	@CTORMTemplate(seq = "25", name="BreakDownConfirmer", type="Column", dataType="String", initial="", history="")
	private String BreakDownConfirmer;
	
	public BM()
	{
		
	}
	
	public String getSubUnitName() {
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}
	
	public BM(String bmID )
	{
		setBmID(bmID);
	}

	public String getBmID() {
		return bmID;
	}

	public void setBmID(String bmID) {
		this.bmID = bmID;
	}

	public String getBmType() {
		return bmType;
	}

	public void setBmType(String bmType) {
		this.bmType = bmType;
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

	public String getShift() {
		return shift;
	}

	public void setShift(String shift) {
		this.shift = shift;
	}

	public Timestamp getBmEndTime() {
		return bmEndTime;
	}

	public void setBmEndTime(Timestamp bmEndTime) {
		this.bmEndTime = bmEndTime;
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

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getBmGrade() {
		return bmGrade;
	}

	public void setBmGrade(String bmGrade) {
		this.bmGrade = bmGrade;
	}

	public Timestamp getRepairTime() {
		return repairTime;
	}

	public void setRepairTime(Timestamp repairTime) {
		this.repairTime = repairTime;
	}

	public String getProcessPassFlag() {
		return processPassFlag;
	}

	public void setProcessPassFlag(String processPassFlag) {
		this.processPassFlag = processPassFlag;
	}

	public String getExecuteResult() {
		return executeResult;
	}

	public void setExecuteResult(String executeResult) {
		this.executeResult = executeResult;
	}

	public String getBmCause() {
		return bmCause;
	}

	public void setBmCause(String bmCause) {
		this.bmCause = bmCause;
	}

	public String getBmSolution() {
		return bmSolution;
	}

	public void setBmSolution(String bmSolution) {
		this.bmSolution = bmSolution;
	}

	public String getProductionEffectFlag() {
		return productionEffectFlag;
	}

	public void setProductionEffectFlag(String productionEffectFlag) {
		this.productionEffectFlag = productionEffectFlag;
	}
	
	public String getBmState() {
		return bmState;
	}

	public void setBmState(String bmState) {
		this.bmState = bmState;
	}
	
	public String getEvaluationComment() {
		return evaluationComment;
	}

	public void setEvaluationComment(String evaluationComment) {
		this.evaluationComment = evaluationComment;
	}
	
	public String getCumulativeTime() {
		return cumulativeTime;
	}

	public void setCumulativeTime(String cumulativeTime) {
		this.cumulativeTime = cumulativeTime;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getNGReason() {
		return NGReason;
	}

	public void setNGReason(String NGReason) {
		this.NGReason = NGReason;
	}
	
	public String getQAEvaluationComment() {
		return QAEvaluationComment;
	}

	public void setQAEvaluationComment(String QAEvaluationComment) {
		this.QAEvaluationComment = QAEvaluationComment;
	}
	public String getBreakDownConfirmer() {
		return BreakDownConfirmer;
	}

	public void setBreakDownConfirmer(String BreakDownConfirmer) {
		this.BreakDownConfirmer = BreakDownConfirmer;
	}
	
}
