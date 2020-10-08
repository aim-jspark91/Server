package kr.co.aim.messolution.extended.object.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PlanQuantity  extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="OperationName", type="Key", dataType="String", initial="", history="")
	private String OperationName;
	
   @CTORMTemplate(seq = "2", name="DayQuantity", type="Column", dataType="Number", initial="", history="")
	private long DayQuantity;
	
	@CTORMTemplate(seq = "3", name="LightQuantity", type="Column", dataType="Number", initial="", history="")
	private long LightQuantity;
	
	@CTORMTemplate(seq = "4", name="PlanQuantity", type="Column", dataType="Number", initial="", history="")
	private long PlanQuantity;
	
	@CTORMTemplate(seq = "5", name="CreateUser", type="Column", dataType="String", initial="", history="")
	private String CreateUser;
	
	@CTORMTemplate(seq = "6", name="CreateTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp CreateTime;
	
	@CTORMTemplate(seq = "7", name="Reason", type="Column", dataType="String", initial="", history="")
	private String reason;
	
	@CTORMTemplate(seq = "8", name="ActualQuantity", type="Column", dataType="Number", initial="", history="")
	private long actualQuantity;
	
	
	public PlanQuantity()
	{
		
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public long getActualQuantity() {
		return actualQuantity;
	}

	public void setActualQuantity(long actualQuantity) {
		this.actualQuantity = actualQuantity;
	}

	public String getOperationName() {
		return OperationName;
	}

	public void setOperationName(String operationName) {
		OperationName = operationName;
	}

	public long getPlanQuantity() {
		return PlanQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		PlanQuantity = planQuantity;
	}

	public long getDayQuantity() {
		return DayQuantity;
	}

	public void setDayQuantity(long dayQuantity) {
		DayQuantity = dayQuantity;
	}

	public long getLightQuantity() {
		return LightQuantity;
	}

	public void setLightQuantity(long lightQuantity) {
		LightQuantity = lightQuantity;
	}

	public String getCreateUser() {
		return CreateUser;
	}

	public void setCreateUser(String createUser) {
		CreateUser = createUser;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}

 
}
