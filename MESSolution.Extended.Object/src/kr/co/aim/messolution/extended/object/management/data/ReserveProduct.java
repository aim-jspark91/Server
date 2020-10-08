package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveProduct extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="processOperationGroupName", type="Key", dataType="String", initial="", history="")
	private String processOperationGroupName;
	
	@CTORMTemplate(seq = "3", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "4", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "6", name="position", type="Column", dataType="String", initial="", history="")
	private String position;
	
	@CTORMTemplate(seq = "7", name="reserveState", type="Column", dataType="String", initial="", history="")
	private String reserveState;
	
	@CTORMTemplate(seq = "8", name="reservedQuantity", type="Column", dataType="String", initial="", history="")
	private String reservedQuantity;
	
	@CTORMTemplate(seq = "9", name="completeQuantity", type="Column", dataType="String", initial="", history="")
	private String completeQuantity;

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getProcessOperationGroupName() {
		return processOperationGroupName;
	}

	public void setProcessOperationGroupName(String processOperationGroupName) {
		this.processOperationGroupName = processOperationGroupName;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
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

	public String getReservedQuantity() {
		return reservedQuantity;
	}

	public void setReservedQuantity(String reservedQuantity) {
		this.reservedQuantity = reservedQuantity;
	}

	public String getCompleteQuantity() {
		return completeQuantity;
	}

	public void setCompleteQuantity(String completeQuantity) {
		this.completeQuantity = completeQuantity;
	}
	
}
