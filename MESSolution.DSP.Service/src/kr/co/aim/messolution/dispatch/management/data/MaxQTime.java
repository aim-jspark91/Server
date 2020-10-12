package kr.co.aim.messolution.dispatch.management.data;

import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

@CTORMHeader(tag = "CT", divider="_")
public class MaxQTime extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="RelationId", type="Key", dataType="String", initial="", history="")
	private String RelationId;
	
	@CTORMTemplate(seq = "2", name="FactoryName", type="Key", dataType="String", initial="", history="")
	private String FactoryName;

	@CTORMTemplate(seq = "3", name="FromProductSpecName", type="Key", dataType="String", initial="", history="")
	private String FromProductSpecName;
	
	@CTORMTemplate(seq = "4", name="FromProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String FromProcessFlowName;
	
	@CTORMTemplate(seq = "5", name="FromProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String FromProcessOperationName;
	
	@CTORMTemplate(seq = "6", name="ToProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String ToProcessOperationName;
	
	@CTORMTemplate(seq = "7", name="ToMachineName", type="Key", dataType="String", initial="", history="")
	private String ToMachineName;
	
	@CTORMTemplate(seq = "8", name="MachineCapacity", type="Column", dataType="Number", initial="", history="")
	private Number MachineCapacity;

	public String getRelationId() {
		return RelationId;
	}

	public void setRelationId(String relationId) {
		RelationId = relationId;
	}

	public String getFactoryName() {
		return FactoryName;
	}

	public void setFactoryName(String factoryName) {
		FactoryName = factoryName;
	}

	public String getFromProductSpecName() {
		return FromProductSpecName;
	}

	public void setFromProductSpecName(String fromProductSpecName) {
		FromProductSpecName = fromProductSpecName;
	}

	public String getFromProcessFlowName() {
		return FromProcessFlowName;
	}

	public void setFromProcessFlowName(String fromProcessFlowName) {
		FromProcessFlowName = fromProcessFlowName;
	}

	public String getFromProcessOperationName() {
		return FromProcessOperationName;
	}

	public void setFromProcessOperationName(String fromProcessOperationName) {
		FromProcessOperationName = fromProcessOperationName;
	}

	public String getToProcessOperationName() {
		return ToProcessOperationName;
	}

	public void setToProcessOperationName(String toProcessOperationName) {
		ToProcessOperationName = toProcessOperationName;
	}

	public String getToMachineName() {
		return ToMachineName;
	}

	public void setToMachineName(String toMachineName) {
		ToMachineName = toMachineName;
	}

	public Number getMachineCapacity() {
		return MachineCapacity;
	}

	public void setMachineCapacity(Number machineCapacity) {
		MachineCapacity = machineCapacity;
	}
	
}
