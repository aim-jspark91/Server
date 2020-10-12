package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Mask extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="maskName", type="Key", dataType="String", initial="", history="")
	private String maskName;
	
	@CTORMTemplate(seq = "2", name="maskType", type="Column", dataType="String", initial="", history="")
	private String maskType;
	
	@CTORMTemplate(seq = "3", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "4", name="maskSpecName", type="Column", dataType="String", initial="", history="")
	private String maskSpecName;
	
	@CTORMTemplate(seq = "5", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "6", name="carrierPosition", type="Column", dataType="Long", initial="", history="")
	private long carrierPosition;
	
	@CTORMTemplate(seq = "7", name="maskGrade", type="Column", dataType="String", initial="", history="")
	private String maskGrade;
	
	@CTORMTemplate(seq = "8", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "9", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	@CTORMTemplate(seq = "10", name="maskState", type="Column", dataType="String", initial="", history="")
	private String maskState;
	
	@CTORMTemplate(seq = "11", name="maskProcessState", type="Column", dataType="String", initial="", history="")
	private String maskProcessState;
	
	@CTORMTemplate(seq = "12", name="maskHoldState", type="Column", dataType="String", initial="", history="")
	private String maskHoldState;
	
	@CTORMTemplate(seq = "13", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "14", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "15", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "16", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "17", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "18", name="nodeStack", type="Column", dataType="String", initial="", history="")
	private String nodeStack;
	
	@CTORMTemplate(seq = "19", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "20", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
	private String machineRecipeName;
	
	@CTORMTemplate(seq = "21", name="reworkState", type="Column", dataType="String", initial="", history="")
	private String reworkState;
	
	@CTORMTemplate(seq = "22", name="reworkCount", type="Column", dataType="Long", initial="", history="")
	private long reworkCount;
	
	@CTORMTemplate(seq = "23", name="transferLockFlag", type="Column", dataType="String", initial="", history="")
	private String transferLockFlag;
	
	@CTORMTemplate(seq = "24", name="transferState", type="Column", dataType="String", initial="", history="")
	private String transferState;
	
	@CTORMTemplate(seq = "25", name="positionType", type="Column", dataType="String", initial="", history="")
	private String positionType;
	
	@CTORMTemplate(seq = "26", name="tMachineName", type="Column", dataType="String", initial="", history="")
	private String tMachineName;
	
	@CTORMTemplate(seq = "27", name="portName", type="Column", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "28", name="positionName", type="Column", dataType="String", initial="", history="")
	private String positionName;
	
	@CTORMTemplate(seq = "29", name="zoneName", type="Column", dataType="String", initial="", history="")
	private String zoneName;
	
	@CTORMTemplate(seq = "30", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "31", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "32", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "33", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "34", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "35", name="createTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "36", name="createUser", type="Column", dataType="String", initial="", history="N")
	private String createUser;
	
	@CTORMTemplate(seq = "37", name="releaseTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp releaseTime;
	
	@CTORMTemplate(seq = "38", name="releaseUser", type="Column", dataType="String", initial="", history="N")
	private String releaseUser;
	
	@CTORMTemplate(seq = "39", name="lastLoggedInTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastLoggedInTime;
	
	@CTORMTemplate(seq = "40", name="lastLoggedInUser", type="Column", dataType="String", initial="", history="N")
	private String lastLoggedInUser;
	
	@CTORMTemplate(seq = "41", name="lastLoggedOutTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastLoggedOutTime;
	
	@CTORMTemplate(seq = "42", name="lastLoggedOutUser", type="Column", dataType="String", initial="", history="N")
	private String lastLoggedOutUser;
	
	
	public String getmaskName() {
		return maskName;
	}
	public void setmaskName(String maskName) {
		this.maskName = maskName;
	}
	
	public String getmaskType() {
		return maskType;
	}
	public void setmaskType(String maskType) {
		this.maskType = maskType;
	}
	
	public String getproductionType() {
		return productionType;
	}
	public void setproductionType(String productionType) {
		this.productionType = productionType;
	}
	
	public String getmaskSpecName() {
		return maskSpecName;
	}
	public void setmaskSpecName(String maskSpecName) {
		this.maskSpecName = maskSpecName;
	}
	
	public String getcarrierName() {
		return carrierName;
	}
	public void setcarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
	public long getcarrierPosition() {
		return carrierPosition;
	}
	public void setcarrierPosition(long carrierPosition) {
		this.carrierPosition = carrierPosition;
	}
	
	public String getmaskGrade() {
		return maskGrade;
	}
	public void setmaskGrade(String maskGrade) {
		this.maskGrade = maskGrade;
	}
	
	public String getfactoryName() {
		return factoryName;
	}
	public void setfactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getareaName() {
		return areaName;
	}
	public void setareaName(String areaName) {
		this.areaName = areaName;
	}
	
	public String getmaskState() {
		return maskState;
	}
	public void setmaskState(String maskState) {
		this.maskState = maskState;
	}
	
	public String getmaskProcessState() {
		return maskProcessState;
	}
	public void setmaskProcessState(String maskProcessState) {
		this.maskProcessState = maskProcessState;
	}
	
	public String getmaskHoldState() {
		return maskHoldState;
	}
	public void setmaskHoldState(String maskHoldState) {
		this.maskHoldState = maskHoldState;
	}
	
	public String getreasonCode() {
		return reasonCode;
	}
	public void setreasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	
	public String getprocessFlowName() {
		return processFlowName;
	}
	public void setprocessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	
	public String getprocessFlowVersion() {
		return processFlowVersion;
	}
	public void setprocessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}
	
	public String getprocessOperationName() {
		return processOperationName;
	}
	public void setprocessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	
	public String getprocessOperationVersion() {
		return processOperationVersion;
	}
	public void setprocessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}
	
	public String getnodeStack() {
		return nodeStack;
	}
	public void setnodeStack(String nodeStack) {
		this.nodeStack = nodeStack;
	}
	
	public String getmachineName() {
		return machineName;
	}
	public void setmachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getmachineRecipeName() {
		return machineRecipeName;
	}
	public void setmachineRecipeName(String machineRecipeName) {
		this.machineRecipeName = machineRecipeName;
	}
	
	public String getreworkState() {
		return reworkState;
	}
	public void setreworkState(String reworkState) {
		this.reworkState = reworkState;
	}
	
	public long getreworkCount() {
		return reworkCount;
	}
	public void setreworkCount(long reworkCount) {
		this.reworkCount = reworkCount;
	}
	
	public String gettransferLockFlag() {
		return transferLockFlag;
	}
	public void settransferLockFlag(String transferLockFlag) {
		this.transferLockFlag = transferLockFlag;
	}
	
	public String gettransferState() {
		return transferState;
	}
	public void settransferState(String transferState) {
		this.transferState = transferState;
	}
	
	public String getpositionType() {
		return positionType;
	}
	public void setpositionType(String positionType) {
		this.positionType = positionType;
	}
	
	public String gettMachineName() {
		return tMachineName;
	}
	public void settMachineName(String tMachineName) {
		this.tMachineName = tMachineName;
	}
	
	public String getportName() {
		return portName;
	}
	public void setportName(String portName) {
		this.portName = portName;
	}
	
	public String getpositionName() {
		return positionName;
	}
	public void setpositionName(String positionName) {
		this.positionName = positionName;
	}
	
	public String getzoneName() {
		return zoneName;
	}
	public void setzoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	
	public String getlastEventName() {
		return lastEventName;
	}
	public void setlastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getlastEventTimekey() {
		return lastEventTimekey;
	}
	public void setlastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	
	public Timestamp getlastEventTime() {
		return lastEventTime;
	}
	public void setlastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	
	public String getlastEventUser() {
		return lastEventUser;
	}
	public void setlastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	public String getlastEventComment() {
		return lastEventComment;
	}
	public void setlastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	public Timestamp getcreateTime() {
		return createTime;
	}
	public void setcreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public String getcreateUser() {
		return createUser;
	}
	public void setcreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	public Timestamp getreleaseTime() {
		return releaseTime;
	}
	public void setreleaseTime(Timestamp releaseTime) {
		this.releaseTime = releaseTime;
	}
	
	public String getreleaseUser() {
		return releaseUser;
	}
	public void setreleaseUser(String releaseUser) {
		this.releaseUser = releaseUser;
	}
	
	public Timestamp getlastLoggedInTime() {
		return lastLoggedInTime;
	}
	public void setlastLoggedInTime(Timestamp lastLoggedInTime) {
		this.lastLoggedInTime = lastLoggedInTime;
	}
	
	public String getlastLoggedInUser() {
		return lastLoggedInUser;
	}
	public void setlastLoggedInUser(String lastLoggedInUser) {
		this.lastLoggedInUser = lastLoggedInUser;
	}
	
	public Timestamp getlastLoggedOutTime() {
		return lastLoggedOutTime;
	}
	public void setlastLoggedOutTime(Timestamp lastLoggedOutTime) {
		this.lastLoggedOutTime = lastLoggedOutTime;
	}
	
	public String getlastLoggedOutUser() {
		return lastLoggedOutUser;
	}
	public void setlastLoggedOutUser(String lastLoggedOutUser) {
		this.lastLoggedOutUser = lastLoggedOutUser;
	}
	
	
	//instantiation
	public Mask()
	{
		
	}
	
	public Mask(String maskName)
	{
		setmaskName(maskName);
	}
}
