package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductQueueTime extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "4", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "5", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "6", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "7", name="toFactoryName", type="Key", dataType="String", initial="", history="")
	private String toFactoryName;
	
	@CTORMTemplate(seq = "8", name="toProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String toProcessFlowName;
	
	@CTORMTemplate(seq = "9", name="toProcessFlowVersion", type="Key", dataType="String", initial="", history="")
	private String toProcessFlowVersion;
	
	@CTORMTemplate(seq = "10", name="toProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String toProcessOperationName;
	
	@CTORMTemplate(seq = "11", name="toProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String toProcessOperationVersion;
	
	@CTORMTemplate(seq = "12", name="queueTimeType", type="Key", dataType="String", initial="", history="")
	private String queueTimeType;
	
	@CTORMTemplate(seq = "13", name="toEventName", type="Column", dataType="String", initial="", history="")
	private String toEventName;
	
	@CTORMTemplate(seq = "14", name="warningDurationLimit", type="Column", dataType="Long", initial="", history="")
	private long warningDurationLimit;
	
	@CTORMTemplate(seq = "15", name="interlockDurationLimit", type="Column", dataType="Long", initial="", history="")
	private long interlockDurationLimit;
	
	@CTORMTemplate(seq = "16", name="generationType", type="Column", dataType="String", initial="", history="")
	private String generationType;
	
	@CTORMTemplate(seq = "17", name="departmentName", type="Column", dataType="String", initial="", history="")
	private String departmentName;
	
	@CTORMTemplate(seq = "18", name="queueTimeState", type="Column", dataType="String", initial="", history="")
	private String queueTimeState;
	
	@CTORMTemplate(seq = "19", name="enterTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp enterTime;
	
	@CTORMTemplate(seq = "20", name="exitTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp exitTime;
	
	@CTORMTemplate(seq = "21", name="warningTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp warningTime;
	
	@CTORMTemplate(seq = "22", name="interlockTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp interlockTime;
	
	@CTORMTemplate(seq = "23", name="resolveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp resolveTime;
	
	@CTORMTemplate(seq = "24", name="resolveUser", type="Column", dataType="String", initial="", history="")
	private String resolveUser;
	
	@CTORMTemplate(seq = "25", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "26", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "27", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "28", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "29", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "30", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "31", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "32", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	
	
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getProductRequestName() {
		return productRequestName;
	}
	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}
	
	public String getproductName() {
		return productName;
	}
	public void setproductName(String productName) {
		this.productName = productName;
	}
	
	public String getfactoryName() {
		return factoryName;
	}
	public void setfactoryName(String factoryName) {
		this.factoryName = factoryName;
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
	
	public String gettoFactoryName() {
		return toFactoryName;
	}
	public void settoFactoryName(String toFactoryName) {
		this.toFactoryName = toFactoryName;
	}
	
	public String gettoProcessFlowName() {
		return toProcessFlowName;
	}
	public void settoProcessFlowName(String toProcessFlowName) {
		this.toProcessFlowName = toProcessFlowName;
	}
	
	public String gettoProcessFlowVersion() {
		return toProcessFlowVersion;
	}
	public void settoProcessFlowVersion(String toProcessFlowVersion) {
		this.toProcessFlowVersion = toProcessFlowVersion;
	}
	
	public String gettoProcessOperationName() {
		return toProcessOperationName;
	}
	public void settoProcessOperationName(String toProcessOperationName) {
		this.toProcessOperationName = toProcessOperationName;
	}
	
	public String gettoProcessOperationVersion() {
		return toProcessOperationVersion;
	}
	public void settoProcessOperationVersion(String toProcessOperationVersion) {
		this.toProcessOperationVersion = toProcessOperationVersion;
	}
	
	public String gettoEventName() {
		return toEventName;
	}
	public void settoEventName(String toEventName) {
		this.toEventName = toEventName;
	}
	
	public String getqueueTimeType() {
		return queueTimeType;
	}
	public void setqueueTimeType(String queueTimeType) {
		this.queueTimeType = queueTimeType;
	}
	
	public long getwarningDurationLimit() {
		return warningDurationLimit;
	}
	public void setwarningDurationLimit(long warningDurationLimit) {
		this.warningDurationLimit = warningDurationLimit;
	}
	
	public long getinterlockDurationLimit() {
		return interlockDurationLimit;
	}
	public void setinterlockDurationLimit(long interlockDurationLimit) {
		this.interlockDurationLimit = interlockDurationLimit;
	}
	
	public String getgenerationType() {
		return generationType;
	}
	public void setgenerationType(String generationType) {
		this.generationType = generationType;
	}
	
	public String getdepartmentName() {
		return departmentName;
	}
	public void setdepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
	public String getqueueTimeState() {
		return queueTimeState;
	}
	public void setqueueTimeState(String queueTimeState) {
		this.queueTimeState = queueTimeState;
	}
	
	public Timestamp getenterTime() {
		return enterTime;
	}
	public void setenterTime(Timestamp enterTime) {
		this.enterTime = enterTime;
	}
	
	public Timestamp getexitTime() {
		return exitTime;
	}
	public void setexitTime(Timestamp exitTime) {
		this.exitTime = exitTime;
	}
	
	public Timestamp getwarningTime() {
		return warningTime;
	}
	public void setwarningTime(Timestamp warningTime) {
		this.warningTime = warningTime;
	}
	
	public Timestamp getinterlockTime() {
		return interlockTime;
	}
	public void setinterlockTime(Timestamp interlockTime) {
		this.interlockTime = interlockTime;
	}
	
	public Timestamp getresolveTime() {
		return resolveTime;
	}
	public void setresolveTime(Timestamp resolveTime) {
		this.resolveTime = resolveTime;
	}
	
	public String getresolveUser() {
		return resolveUser;
	}
	public void setresolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
	}
	
	public String getlastEventUser() {
		return lastEventUser;
	}
	public void setlastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	public Timestamp getlastEventTime() {
		return lastEventTime;
	}
	public void setlastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	
	public String getlastEventTimekey() {
		return lastEventTimekey;
	}
	public void setlastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	
	public String getlastEventName() {
		return lastEventName;
	}
	public void setlastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getlastEventComment() {
		return lastEventComment;
	}
	public void setlastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	//instantiation
	public ProductQueueTime()
	{
		
	}
	
	public ProductQueueTime(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, 
			String toFactoryName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion, String queueTimeType)
	{
		setproductName(productName);
		setfactoryName(factoryName);
		setprocessFlowName(processFlowName);
		setprocessFlowVersion(processFlowVersion);
		setprocessOperationName(processOperationName);
		setprocessOperationVersion(processOperationVersion);
		settoFactoryName(toFactoryName);
		settoProcessFlowName(toProcessFlowName);
		settoProcessFlowVersion(toProcessFlowVersion);
		settoProcessOperationName(toProcessOperationName);
		settoProcessOperationVersion(toProcessOperationVersion);
		setqueueTimeType(queueTimeType);
	}
}
