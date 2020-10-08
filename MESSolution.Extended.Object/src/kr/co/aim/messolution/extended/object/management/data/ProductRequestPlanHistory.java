package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductRequestPlanHistory extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "2", name="assignedMachineName", type="Key", dataType="String", initial="", history="")
	private String assignedMachineName;
	
	@CTORMTemplate(seq = "3", name="planReleasedTime", type="Key", dataType="Timestamp", initial="", history="")
	private Timestamp planReleasedTime;
	
	@CTORMTemplate(seq = "4", name="timekey", type="Key", dataType="String", initial="", history="")
	private String timekey;
	
	@CTORMTemplate(seq = "5", name="planFinishedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planFinishedTime;
	
	@CTORMTemplate(seq = "6", name="planQuantity", type="Column", dataType="long", initial="", history="")
	private long planQuantity;
	
	@CTORMTemplate(seq = "7", name="releasedQuantity", type="Column", dataType="long", initial="", history="")
	private long releasedQuantity;
	
	@CTORMTemplate(seq = "8", name="finishedQuantity", type="Column", dataType="long", initial="", history="")
	private long finishedQuantity;
	
	@CTORMTemplate(seq = "9", name="productRequestPlanState", type="Column", dataType="String", initial="", history="")
	private String productRequestPlanState;
	
	@CTORMTemplate(seq = "10", name="productRequestPlanHoldState", type="Column", dataType="String", initial="", history="")
	private String productRequestPlanHoldState;
	
	@CTORMTemplate(seq = "11", name="eventName", type="Column", dataType="String", initial="", history="")
	private String eventName;
	
	@CTORMTemplate(seq = "12", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "13", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "14", name="eventComment", type="Column", dataType="String", initial="", history="")
	private String eventComment;
	
	@CTORMTemplate(seq = "15", name="eventFlag", type="Column", dataType="String", initial="", history="")
	private String eventFlag;
	
	@CTORMTemplate(seq = "16", name="position", type="Column", dataType="long", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "17", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "18", name="ECCode", type="Column", dataType="String", initial="", history="")
	private String ECCode;
	
	@CTORMTemplate(seq = "19", name="DepartmentName", type="Column", dataType="String", initial="", history="")
	private String departmentName;
	
	@CTORMTemplate(seq = "20", name="Priority", type="Column", dataType="long", initial="", history="")
	private long priority;
	
	

	//instantiation
	public ProductRequestPlanHistory()
	{
		
	}
	
	public ProductRequestPlanHistory(String productRequestName, String assignedMachineName, Timestamp planReleasedTime, String timeKey)
	{
		setProductRequestName(productRequestName);
		setAssignedMachineName(assignedMachineName);
		setPlanReleasedTime(planReleasedTime);
		setTimekey(timeKey);
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getAssignedMachineName() {
		return assignedMachineName;
	}

	public void setAssignedMachineName(String assignedMachineName) {
		this.assignedMachineName = assignedMachineName;
	}

	public Timestamp getPlanReleasedTime() {
		return planReleasedTime;
	}

	public void setPlanReleasedTime(Timestamp planReleasedTime) {
		this.planReleasedTime = planReleasedTime;
	}

	public String getTimekey() {
		return timekey;
	}

	public void setTimekey(String timekey) {
		this.timekey = timekey;
	}

	public Timestamp getPlanFinishedTime() {
		return planFinishedTime;
	}

	public void setPlanFinishedTime(Timestamp planFinishedTime) {
		this.planFinishedTime = planFinishedTime;
	}

	public long getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		this.planQuantity = planQuantity;
	}

	public long getReleasedQuantity() {
		return releasedQuantity;
	}

	public void setReleasedQuantity(long releasedQuantity) {
		this.releasedQuantity = releasedQuantity;
	}

	public long getFinishedQuantity() {
		return finishedQuantity;
	}

	public void setFinishedQuantity(long finishedQuantity) {
		this.finishedQuantity = finishedQuantity;
	}

	public String getProductRequestPlanState() {
		return productRequestPlanState;
	}

	public void setProductRequestPlanState(String productRequestPlanState) {
		this.productRequestPlanState = productRequestPlanState;
	}

	public String getProductRequestPlanHoldState() {
		return productRequestPlanHoldState;
	}

	public void setProductRequestPlanHoldState(String productRequestPlanHoldState) {
		this.productRequestPlanHoldState = productRequestPlanHoldState;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public String getEventComment() {
		return eventComment;
	}

	public void setEventComment(String eventComment) {
		this.eventComment = eventComment;
	}

	public String getEventFlag() {
		return eventFlag;
	}

	public void setEventFlag(String eventFlag) {
		this.eventFlag = eventFlag;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	
	public String getECCode() {
		return ECCode;
	}

	public void setECCode(String eCCode) {
		ECCode = eCCode;
	}
	
	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	
	
	
}
