package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TransportJobCommand extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="transportJobName", type="Key", dataType="String", initial="", history="")
	private String transportJobName;
	
	@CTORMTemplate(seq = "2", name="transportJobType", type="Column", dataType="String", initial="", history="")
	private String transportJobType;
	
	@CTORMTemplate(seq = "3", name="jobState", type="Column", dataType="String", initial="", history="")
	private String jobState;
	
	@CTORMTemplate(seq = "4", name="cancelState", type="Column", dataType="String", initial="", history="")
	private String cancelState;
	
	@CTORMTemplate(seq = "5", name="changeState", type="Column", dataType="String", initial="", history="")
	private String changeState;
	
	@CTORMTemplate(seq = "6", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "7", name="sourceMachineName", type="Column", dataType="String", initial="", history="")
	private String sourceMachineName;
	
	@CTORMTemplate(seq = "8", name="sourcePositionType", type="Column", dataType="String", initial="", history="")
	private String sourcePositionType;
	
	@CTORMTemplate(seq = "9", name="sourcePositionName", type="Column", dataType="String", initial="", history="")
	private String sourcePositionName;
	
	@CTORMTemplate(seq = "10", name="sourceZoneName", type="Column", dataType="String", initial="", history="")
	private String sourceZoneName;
	
	@CTORMTemplate(seq = "11", name="destinationMachineName", type="Column", dataType="String", initial="", history="")
	private String destinationMachineName;
	
	@CTORMTemplate(seq = "12", name="destinationPositionType", type="Column", dataType="String", initial="", history="")
	private String destinationPositionType;
	
	@CTORMTemplate(seq = "13", name="destinationPositionName", type="Column", dataType="String", initial="", history="")
	private String destinationPositionName;
	
	@CTORMTemplate(seq = "14", name="destinationZoneName", type="Column", dataType="String", initial="", history="")
	private String destinationZoneName;
	
	@CTORMTemplate(seq = "15", name="priority", type="Column", dataType="String", initial="", history="")
	private String priority;
	
	@CTORMTemplate(seq = "16", name="carrierState", type="Column", dataType="String", initial="", history="")
	private String carrierState;
	
	@CTORMTemplate(seq = "17", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	//@CTORMTemplate(seq = "24", name="processOperationName", type="Column", dataType="String", initial="", history="")
	//private String processOperationName;
	
	@CTORMTemplate(seq = "18", name="productQuantity", type="Column", dataType="Long", initial="", history="")
	private long productQuantity;
	
	@CTORMTemplate(seq = "19", name="currentMachineName", type="Column", dataType="String", initial="", history="")
	private String currentMachineName;
	
	@CTORMTemplate(seq = "20", name="currentPositionType", type="Column", dataType="String", initial="", history="")
	private String currentPositionType;
	
	@CTORMTemplate(seq = "21", name="currentPositionName", type="Column", dataType="String", initial="", history="")
	private String currentPositionName;
	
	@CTORMTemplate(seq = "22", name="currentZoneName", type="Column", dataType="String", initial="", history="")
	private String currentZoneName;
	
	@CTORMTemplate(seq = "23", name="transferState", type="Column", dataType="String", initial="", history="")
	private String transferState;
	
	@CTORMTemplate(seq = "24", name="alternateFlag", type="Column", dataType="String", initial="", history="")
	private String alternateFlag;
	
	@CTORMTemplate(seq = "25", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "26", name="reasonMessage", type="Column", dataType="String", initial="", history="")
	private String reasonMessage;
	
	@CTORMTemplate(seq = "27", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "28", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "29", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "30", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "31", name="lastEventResultCode", type="Column", dataType="String", initial="", history="eventResultCode")
	private String lastEventResultCode;
	
	@CTORMTemplate(seq = "32", name="lastEventResultText", type="Column", dataType="String", initial="", history="eventResultText")
	private String lastEventResultText;
	
	@CTORMTemplate(seq = "33", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "34", name="carrierType", type="Column", dataType="String", initial="", history="")
	private String carrierType;
	
	@CTORMTemplate(seq = "35", name="cleanState", type="Column", dataType="String", initial="", history="")
	private String cleanState;
	
	@CTORMTemplate(seq = "36", name="kanban", type="Column", dataType="String", initial="", history="")
	private String kanban;
	
	@CTORMTemplate(seq = "37", name="region", type="Column", dataType="String", initial="", history="")
	private String region;
	
	// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
	@CTORMTemplate(seq = "38", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;

	// Added by aim.yunjm on 2019.01.17
	@CTORMTemplate(seq = "39", name="reserveProductName", type="Column", dataType="String", initial="", history="")
	private String reserveProductName;
	
	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	@CTORMTemplate(seq = "40", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	@CTORMTemplate(seq = "41", name="ecCode", type="Column", dataType="String", initial="", history="")
	private String ecCode;
	
	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	@CTORMTemplate(seq = "42", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;

	// 2019.03.27 by shkim. Mentis 3257
	@CTORMTemplate(seq = "43", name="productRequestType", type="Column", dataType="String", initial="", history="")
	private String productRequestType;
	
	public TransportJobCommand() {		
	}
	
	public TransportJobCommand(String transportJobName)	{
		setTransportJobName(transportJobName);
	}
		
	public String getTransportJobName() {
		return transportJobName;
	}

	public void setTransportJobName(String transportJobName) {
		this.transportJobName = transportJobName;
	}

	public String getTransportJobType() {
		return transportJobType;
	}

	public void setTransportJobType(String transportJobType) {
		this.transportJobType = transportJobType;
	}

	public String getJobState() {
		return jobState;
	}

	public void setJobState(String jobState) {
		this.jobState = jobState;
	}

	public String getCancelState() {
		return cancelState;
	}

	public void setCancelState(String cancelState) {
		this.cancelState = cancelState;
	}

	public String getChangeState() {
		return changeState;
	}

	public void setChangeState(String changeState) {
		this.changeState = changeState;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getSourceMachineName() {
		return sourceMachineName;
	}

	public void setSourceMachineName(String sourceMachineName) {
		this.sourceMachineName = sourceMachineName;
	}

	public String getSourcePositionType() {
		return sourcePositionType;
	}

	public void setSourcePositionType(String sourcePositionType) {
		this.sourcePositionType = sourcePositionType;
	}

	public String getSourcePositionName() {
		return sourcePositionName;
	}

	public void setSourcePositionName(String sourcePositionName) {
		this.sourcePositionName = sourcePositionName;
	}

	public String getSourceZoneName() {
		return sourceZoneName;
	}

	public void setSourceZoneName(String sourceZoneName) {
		this.sourceZoneName = sourceZoneName;
	}

	public String getDestinationMachineName() {
		return destinationMachineName;
	}

	public void setDestinationMachineName(String destinationMachineName) {
		this.destinationMachineName = destinationMachineName;
	}

	public String getDestinationPositionType() {
		return destinationPositionType;
	}

	public void setDestinationPositionType(String destinationPositionType) {
		this.destinationPositionType = destinationPositionType;
	}

	public String getDestinationPositionName() {
		return destinationPositionName;
	}

	public void setDestinationPositionName(String destinationPositionName) {
		this.destinationPositionName = destinationPositionName;
	}

	public String getDestinationZoneName() {
		return destinationZoneName;
	}

	public void setDestinationZoneName(String destinationZoneName) {
		this.destinationZoneName = destinationZoneName;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getCarrierState() {
		return carrierState;
	}

	public void setCarrierState(String carrierState) {
		this.carrierState = carrierState;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public long getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(long productQuantity) {
		this.productQuantity = productQuantity;
	}

	public String getCurrentMachineName() {
		return currentMachineName;
	}

	public void setCurrentMachineName(String currentMachineName) {
		this.currentMachineName = currentMachineName;
	}

	public String getCurrentPositionType() {
		return currentPositionType;
	}

	public void setCurrentPositionType(String currentPositionType) {
		this.currentPositionType = currentPositionType;
	}

	public String getCurrentPositionName() {
		return currentPositionName;
	}

	public void setCurrentPositionName(String currentPositionName) {
		this.currentPositionName = currentPositionName;
	}

	public String getCurrentZoneName() {
		return currentZoneName;
	}

	public void setCurrentZoneName(String currentZoneName) {
		this.currentZoneName = currentZoneName;
	}

	public String getTransferState() {
		return transferState;
	}

	public void setTransferState(String transferState) {
		this.transferState = transferState;
	}

	public String getAlternateFlag() {
		return alternateFlag;
	}

	public void setAlternateFlag(String alternateFlag) {
		this.alternateFlag = alternateFlag;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReasonMessage() {
		return reasonMessage;
	}

	public void setReasonMessage(String reasonMessage) {
		this.reasonMessage = reasonMessage;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getLastEventResultCode() {
		return lastEventResultCode;
	}

	public void setLastEventResultCode(String lastEventResultCode) {
		this.lastEventResultCode = lastEventResultCode;
	}

	public String getLastEventResultText() {
		return lastEventResultText;
	}

	public void setLastEventResultText(String lastEventResultText) {
		this.lastEventResultText = lastEventResultText;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getCarrierType() {
		return carrierType;
	}

	public void setCarrierType(String carrierType) {
		this.carrierType = carrierType;
	}

	public String getCleanState() {
		return cleanState;
	}

	public void setCleanState(String cleanState) {
		this.cleanState = cleanState;
	}

	public String getKanban() {
		return kanban;
	}

	public void setKanban(String kanban) {
		this.kanban = kanban;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
	public String getProcessOperationName() {
		return processOperationName;
	}

	// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	
	// Added by aim.yunjm on 2019.01.17
	public String getReserveProductName() {
		return reserveProductName;
	}

	public void setReserveProductName(String reserveProductName) {
		this.reserveProductName = reserveProductName;
	}
	
	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public String getProductSpecName() {
		return productSpecName;
	}

	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public String getEcCode() {
		return ecCode;
	}

	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public void setEcCode(String ecCode) {
		this.ecCode = ecCode;
	}

	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public String getProcessFlowName() {
		return processFlowName;
	}

	// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	
	public String getProductRequestType() {
		return productRequestType;
	}

	public void setProductRequestType(String productRequestType) {
		this.productRequestType = productRequestType;
	}
}