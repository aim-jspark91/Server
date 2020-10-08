package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCJob extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="mqcJobName", type="Key", dataType="String", initial="", history="")
	private String mqcJobName;
	
	@CTORMTemplate(seq = "2", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "4", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "5", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "6", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
		
	@CTORMTemplate(seq = "7", name="mqcTemplateName", type="Column", dataType="String", initial="", history="")
	private String mqcTemplateName;
	
	@CTORMTemplate(seq = "8", name="mqcState", type="Column", dataType="String", initial="", history="")
	private String mqcState;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "14", name="mqcuseproductspec", type="Column", dataType="String", initial="", history="")
	private String mqcuseproductspec;
	
	@CTORMTemplate(seq = "15", name="autoMqcFlag", type="Column", dataType="String", initial="", history="")
	private String autoMqcFlag;
	
	public String getmqcJobName() {
		return mqcJobName;
	}

	public void setmqcJobName(String mqcJobName) {
		this.mqcJobName = mqcJobName;
	}

	public String getlotName() {
		return lotName;
	}

	public void setlotName(String lotName) {
		this.lotName = lotName;
	}

	public String getcarrierName() {
		return carrierName;
	}

	public void setcarrierName(String carrierName) {
		this.carrierName = carrierName;
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

	public String getmqcTemplateName() {
		return mqcTemplateName;
	}

	public void setmqcTemplateName(String mqcTemplateName) {
		this.mqcTemplateName = mqcTemplateName;
	}
	
	public String getmqcuseproductspec() {
		return mqcuseproductspec;
	}

	public void setmqcuseproductspec(String mqcuseproductspec) {
		this.mqcuseproductspec = mqcuseproductspec;
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
	
	public String getmqcState() {
		return mqcState;
	}

	public void setmqcState(String mqcState) {
		this.mqcState = mqcState;
	}
	
	
	public String getAutoMqcFlag() {
		return autoMqcFlag;
	}

	public void setAutoMqcFlag(String autoMqcFlag) {
		this.autoMqcFlag = autoMqcFlag;
	}

	//instantiation
	public MQCJob()
	{
		
	}
	
	public MQCJob(String mqcJobName)
	{
		setmqcJobName(mqcJobName);
	}
}
