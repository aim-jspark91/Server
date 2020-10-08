package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;
	
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "3", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "4", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "5", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "6", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "7", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "8", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	//instantiation
	public FirstGlassProduct()
	{
		
	}
	
	public FirstGlassProduct(String jobName, String productName)
	{
		setJobName(jobName);
		setProductName(productName);
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
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
	
	
}
