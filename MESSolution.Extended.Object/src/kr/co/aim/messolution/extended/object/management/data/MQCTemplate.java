package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCTemplate extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="mqcTemplateName", type="Key", dataType="String", initial="", history="")
	private String mqcTemplateName;
	
	@CTORMTemplate(seq = "2", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "4", name="productspecname", type="Column", dataType="String", initial="", history="")
	private String productspecname;

	@CTORMTemplate(seq = "5", name="eccode", type="Column", dataType="String", initial="", history="")
	private String eccode;
	
	@CTORMTemplate(seq = "6", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;

	@CTORMTemplate(seq = "8", name="productQuantity", type="Column", dataType="Long", initial="", history="")
	private long productQuantity;
	
	@CTORMTemplate(seq = "9", name="mqcCountLimit", type="Column", dataType="Long", initial="", history="")
	private long mqcCountLimit;
	
	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "12", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	
	
	public String getmqcTemplateName() {
		return mqcTemplateName;
	}
	public void setmqcTemplateName(String mqcTemplateName) {
		this.mqcTemplateName = mqcTemplateName;
	}
	
	public String getdescription() {
		return description;
	}
	public void setdescription(String description) {
		this.description = description;
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
	
	public long getproductQuantity() {
		return productQuantity;
	}
	public void setproductQuantity(long productQuantity) {
		this.productQuantity = productQuantity;
	}
	
	public long getmqcCountLimit() {
		return mqcCountLimit;
	}
	public void setmqcCountLimit(long mqcCountLimit) {
		this.mqcCountLimit = mqcCountLimit;
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
	
	//instantiation
	public MQCTemplate()
	{
		
	}
	
	public MQCTemplate(String mqcTemplateName)
	{
		setmqcTemplateName(mqcTemplateName);
	}
	
	public String getproductspecname() {
		return productspecname;
	}
	public void setproductspecname(String productspecname) {
		this.productspecname = productspecname;
	}
	
	public String geteccode() {
		return eccode;
	}
	public void seteccode(String eccode) {
		this.eccode = eccode;
	}
}
