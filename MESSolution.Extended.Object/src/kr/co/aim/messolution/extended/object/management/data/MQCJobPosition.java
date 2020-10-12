package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCJobPosition extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="mqcJobName", type="Key", dataType="String", initial="", history="")
	private String mqcJobName;
	
	@CTORMTemplate(seq = "2", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "3", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "4", name="position", type="Key", dataType="Long", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "5", name="productName", type="Column", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "6", name="recipeName", type="Column", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "7", name="mqcCountUp", type="Column", dataType="Long", initial="", history="")
	private long mqcCountUp;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	public String getmqcJobName() {
		return mqcJobName;
	}

	public void setmqcJobName(String mqcJobName) {
		this.mqcJobName = mqcJobName;
	}

	public String getprocessOperationName() {
		return processOperationName;
	}

	public void setprocessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public long getposition() {
		return position;
	}

	public void setposition(long position) {
		this.position = position;
	}
	
	public String getprocessOperationVersion() {
		return processOperationVersion;
	}

	public void setprocessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getproductName() {
		return productName;
	}

	public void setproductName(String productName) {
		this.productName = productName;
	}

	public String getrecipeName() {
		return recipeName;
	}

	public void setrecipeName(String recipeName) {
		this.recipeName = recipeName;
	}
	
	public long getmqcCountUp() {
		return mqcCountUp;
	}

	public void setmqcCountUp(long mqcCountUp) {
		this.mqcCountUp = mqcCountUp;
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
	
	//instantiation
	public MQCJobPosition()
	{
		
	}
	
	public MQCJobPosition(String mqcJobName, String processOperationName, String processOperationVersion, long position)
	{
		setmqcJobName(mqcJobName);
		setprocessOperationName(processOperationName);
		setprocessOperationVersion(processOperationVersion);
		setposition(position);
	}
}
