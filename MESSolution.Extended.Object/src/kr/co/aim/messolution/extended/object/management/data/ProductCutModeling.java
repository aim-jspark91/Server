package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductCutModeling extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "4", name="no", type="Key", dataType="String", initial="", history="")
	private String no;
	
	@CTORMTemplate(seq = "5", name="productSpecNameAC", type="Column", dataType="String", initial="", history="")
	private String productSpecNameAC;
	
	@CTORMTemplate(seq = "6", name="ecCodeAC", type="Column", dataType="String", initial="", history="")
	private String ecCodeAC;
	
	@CTORMTemplate(seq = "7", name="processFlowNameAC", type="Column", dataType="String", initial="", history="")
	private String processFlowNameAC;
	
	@CTORMTemplate(seq = "8", name="xPosition", type="Column", dataType="String", initial="", history="")
	private String xPosition;
	
	@CTORMTemplate(seq = "9", name="yPosition", type="Column", dataType="String", initial="", history="")
	private String yPosition;
	
	@CTORMTemplate(seq = "10", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	//instantiation
	public ProductCutModeling()
	{
		
	}

	public ProductCutModeling(String factoryName, String productSpecName,
			String productSpecVersion, String no) {
		super();
		this.factoryName = factoryName;
		this.productSpecName = productSpecName;
		this.productSpecVersion = productSpecVersion;
		this.no = no;
	}

	public String getProductSpecNameAC() {
		return productSpecNameAC;
	}

	public void setProductSpecNameAC(String productSpecNameAC) {
		this.productSpecNameAC = productSpecNameAC;
	}

	public String getEcCodeAC() {
		return ecCodeAC;
	}

	public void setEcCodeAC(String ecCodeAC) {
		this.ecCodeAC = ecCodeAC;
	}

	public String getProcessFlowNameAC() {
		return processFlowNameAC;
	}

	public void setProcessFlowNameAC(String processFlowNameAC) {
		this.processFlowNameAC = processFlowNameAC;
	}

	public String getxPosition() {
		return xPosition;
	}

	public void setxPosition(String xPosition) {
		this.xPosition = xPosition;
	}

	public String getyPosition() {
		return yPosition;
	}

	public void setyPosition(String yPosition) {
		this.yPosition = yPosition;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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

	public String getFactoryName() {
		return factoryName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public String getProductSpecVersion() {
		return productSpecVersion;
	}

	public String getNo() {
		return no;
	}
	
	
}
