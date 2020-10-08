package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleLotCount extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name = "ecCode", type = "Key", dataType = "String", initial = "", history = "")
	private String ecCode;
	
	@CTORMTemplate(seq = "4", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	
	@CTORMTemplate(seq = "5", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "6", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	
	@CTORMTemplate(seq = "7", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "8", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	
	@CTORMTemplate(seq = "9", name = "sampleProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowName;
	
	@CTORMTemplate(seq = "10", name = "sampleProcessFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String sampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "11", name = "fromProcessOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String fromProcessOperationName;
	
	@CTORMTemplate(seq = "12", name = "fromProcessOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String fromProcessOperationVersion;
	
	@CTORMTemplate(seq = "13", name = "corresProcessOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String corresProcessOperationName;
	
	@CTORMTemplate(seq = "14", name = "corresProcessOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String corresProcessOperationVersion;
		
	@CTORMTemplate(seq = "15", name = "corresSampleProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String corresSampleProcessFlowName;
	
	@CTORMTemplate(seq = "16", name = "corresSampleProcessFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String corresSampleProcessFlowVersion;
	
	@CTORMTemplate(seq = "17", name = "sampleCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long sampleCount;
	
	@CTORMTemplate(seq = "18", name = "currentCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long currentCount;
	
	@CTORMTemplate(seq = "19", name = "totalCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long totalCount;
	
	@CTORMTemplate(seq = "20", name = "samplePriority", type = "Column", dataType = "Long", initial = "", history = "")
	private long samplePriority;
	
	@CTORMTemplate(seq = "21", name = "corresSampleCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long corresSampleCount;
	
	@CTORMTemplate(seq = "22", name = "corresCurrentCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long corresCurrentCount;
	
	@CTORMTemplate(seq = "23", name = "corresTotalCount", type = "Column", dataType = "Long", initial = "", history = "")
	private long corresTotalCount;
	
	@CTORMTemplate(seq = "24", name = "corresSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String corresSamplePosition;
	
	@CTORMTemplate(seq = "25", name = "corresSamplePriority", type = "Column", dataType = "Long", initial = "", history = "")
	private long corresSamplePriority;

	@CTORMTemplate(seq = "26", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "27", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "28", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "29", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "30", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	public SampleLotCount() {

	}

	public SampleLotCount(String factoryName, String productSpecName,
			String eCCode, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion,
			String machineName, String sampleProcessFlowName,
			String sampleProcessFlowVersion,String fromProcessOperationName, String fromProcessOperationVersion,
			String corresProcessOperationName, String corresProcessOperationVersion, String corresSampleProcessFlowName,
			String corresSampleProcessFlowVersion) {
		super();
		this.factoryName = factoryName;
		this.productSpecName = productSpecName;
		this.ecCode = eCCode;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.machineName = machineName;
		this.sampleProcessFlowName = sampleProcessFlowName;
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
		this.fromProcessOperationName = fromProcessOperationName;
		this.fromProcessOperationVersion = fromProcessOperationVersion;
		this.corresProcessOperationName = corresProcessOperationName;
		this.corresProcessOperationVersion = corresProcessOperationVersion;
		this.corresSampleProcessFlowName = corresSampleProcessFlowName;
		this.corresSampleProcessFlowVersion = corresSampleProcessFlowVersion;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getECCode() {
		return ecCode;
	}

	public void setECCode(String ecCode) {
		this.ecCode = ecCode;
	}

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion() {
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getProcessOperationVersion() {
		return processOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getSampleProcessFlowName() {
		return sampleProcessFlowName;
	}

	public void setSampleProcessFlowName(String sampleProcessFlowName) {
		this.sampleProcessFlowName = sampleProcessFlowName;
	}

	public String getSampleProcessFlowVersion() {
		return sampleProcessFlowVersion;
	}

	public void setSampleProcessFlowVersion(String sampleProcessFlowVersion) {
		this.sampleProcessFlowVersion = sampleProcessFlowVersion;
	}

	public String getFromProcessOperationName() {
		return fromProcessOperationName;
	}

	public void setFromProcessOperationName(String fromProcessOperationName) {
		this.fromProcessOperationName = fromProcessOperationName;
	}

	public String getFromProcessOperationVersion() {
		return fromProcessOperationVersion;
	}

	public void setFromProcessOperationVersion(String fromProcessOperationVersion) {
		this.fromProcessOperationVersion = fromProcessOperationVersion;
	}

	public long getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(long sampleCount) {
		this.sampleCount = sampleCount;
	}

	public long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(long currentCount) {
		this.currentCount = currentCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getSamplePriority() {
		return samplePriority;
	}

	public void setSamplePriority(long samplePriority) {
		this.samplePriority = samplePriority;
	}

	public String getCorresProcessOperationName() {
		return corresProcessOperationName;
	}

	public void setCorresProcessOperationName(String corresProcessOperationName) {
		this.corresProcessOperationName = corresProcessOperationName;
	}

	public String getCorresProcessOperationVersion() {
		return corresProcessOperationVersion;
	}

	public void setCorresProcessOperationVersion(
			String corresProcessOperationVersion) {
		this.corresProcessOperationVersion = corresProcessOperationVersion;
	}

	public String getCorresSampleProcessFlowName() {
		return corresSampleProcessFlowName;
	}

	public void setCorresSampleProcessFlowName(String corresSampleProcessFlowName) {
		this.corresSampleProcessFlowName = corresSampleProcessFlowName;
	}

	public String getCorresSampleProcessFlowVersion() {
		return corresSampleProcessFlowVersion;
	}

	public void setCorresSampleProcessFlowVersion(
			String corresSampleProcessFlowVersion) {
		this.corresSampleProcessFlowVersion = corresSampleProcessFlowVersion;
	}

	public long getCorresSampleCount() {
		return corresSampleCount;
	}

	public void setCorresSampleCount(long corresSampleCount) {
		this.corresSampleCount = corresSampleCount;
	}

	public long getCorresCurrentCount() {
		return corresCurrentCount;
	}

	public void setCorresCurrentCount(long corresCurrentCount) {
		this.corresCurrentCount = corresCurrentCount;
	}

	public long getCorresTotalCount() {
		return corresTotalCount;
	}

	public void setCorresTotalCount(long corresTotalCount) {
		this.corresTotalCount = corresTotalCount;
	}

	public String getCorresSamplePosition() {
		return corresSamplePosition;
	}

	public void setCorresSamplePosition(String corresSamplePosition) {
		this.corresSamplePosition = corresSamplePosition;
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

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public long getCorresSamplePriority() {
		return corresSamplePriority;
	}

	public void setCorresSamplePriority(long corresSamplePriority) {
		this.corresSamplePriority = corresSamplePriority;
	}
	
	

}
