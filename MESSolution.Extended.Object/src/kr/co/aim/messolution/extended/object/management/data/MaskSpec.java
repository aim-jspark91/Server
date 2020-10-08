package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskSpec extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="maskSpecName", type="Key", dataType="String", initial="", history="")
	private String maskSpecName;
	
	@CTORMTemplate(seq = "3", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "4", name="maskType", type="Column", dataType="String", initial="", history="")
	private String maskType;
	
	@CTORMTemplate(seq = "5", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "6", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	
	public String getfactoryName() {
		return factoryName;
	}
	public void setfactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getmaskSpecName() {
		return maskSpecName;
	}
	public void setmaskSpecName(String maskSpecName) {
		this.maskSpecName = maskSpecName;
	}
	
	public String getdescription() {
		return description;
	}
	public void setdescription(String description) {
		this.description = description;
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
	
	public String getlastEventName() {
		return lastEventName;
	}
	public void setlastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	
	public String getlastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setlastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
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
	
	//instantiation
	public MaskSpec()
	{
		
	}
	
	public MaskSpec(String factoryName, String maskSpecName)
	{
		setfactoryName(factoryName);
		setmaskSpecName(maskSpecName);
	}
}
