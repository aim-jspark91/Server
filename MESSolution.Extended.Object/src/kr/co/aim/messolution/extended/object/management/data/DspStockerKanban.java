package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DspStockerKanban extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="stockerName", type="Key", dataType="String", initial="", history="")
	private String stockerName;
	
	@CTORMTemplate(seq = "2", name="zoneName", type="Key", dataType="String", initial="", history="")
	private String zoneName;
	
	@CTORMTemplate(seq = "3", name="kanbanName", type="Key", dataType="String", initial="", history="")
	private String kanbanName;
	
	@CTORMTemplate(seq = "4", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "6", name="ecCode", type="Column", dataType="String", initial="", history="")
	private String ecCode;
	
	@CTORMTemplate(seq = "7", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "8", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "9", name="machineGroupName", type="Column", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "10", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "11", name="setCount", type="Column", dataType="Number", initial="", history="")
	private long setCount;
	
	@CTORMTemplate(seq = "12", name="useFlag", type="Column", dataType="String", initial="", history="")
	private String useFlag;
	
	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "14", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "15", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "16", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "17", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public DspStockerKanban()
	{
		
	}
	public DspStockerKanban(String stockerName, String zoneName, String kanbanName)
	{
		this.setStockerName(stockerName);
		this.setZoneName(zoneName);
		this.setKanbanName(kanbanName);
	}
	public String getStockerName() {
		return stockerName;
	}
	public void setStockerName(String stockerName) {
		this.stockerName = stockerName;
	}
	public String getZoneName() {
		return zoneName;
	}
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	public String getKanbanName() {
		return kanbanName;
	}
	public void setKanbanName(String kanbanName) {
		this.kanbanName = kanbanName;
	}
	public String getProductionType() {
		return productionType;
	}
	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getEcCode() {
		return ecCode;
	}
	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}
	public String getProcessFlowName() {
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
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
	public long getSetCount() {
		return setCount;
	}
	public void setSetCount(long setCount) {
		this.setCount = setCount;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
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
	
}
