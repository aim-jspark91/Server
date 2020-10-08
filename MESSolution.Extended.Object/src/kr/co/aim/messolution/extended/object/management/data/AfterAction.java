package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AfterAction extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="ProductSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "2", name="ProductSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "3", name="HoldFlag", type="Column", dataType="String", initial="", history="")
	private String holdFlag;

	@CTORMTemplate(seq = "4", name="MailFlag", type="Column", dataType="String", initial="", history="")
	private String mailFlag;
	
	@CTORMTemplate(seq = "5", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "6", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	
	public AfterAction()
	{
		
	}

	public AfterAction(String productSpecName,String productSpecVersion)
	{
		this.setProductSpecName(productSpecName);
		this.setProductSpecVersion(productSpecVersion);
	}

	public String getProductSpecName() {
		return productSpecName;
	}


	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}


	public String getProductSpecVersion() {
		return productSpecVersion;
	}


	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}


	public String getHoldFlag() {
		return holdFlag;
	}


	public void setHoldFlag(String holdFlag) {
		this.holdFlag = holdFlag;
	}


	public String getMailFlag() {
		return mailFlag;
	}


	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}


	public String getLastEventUser() {
		return lastEventUser;
	}


	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
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


	public String getLastEventComment() {
		return lastEventComment;
	}


	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}


	
	


}
