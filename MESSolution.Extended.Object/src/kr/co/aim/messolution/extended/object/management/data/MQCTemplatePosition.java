package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCTemplatePosition extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="mqcTemplateName", type="Key", dataType="String", initial="", history="")
	private String mqcTemplateName;
	
	@CTORMTemplate(seq = "2", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "3", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "4", name="position", type="Key", dataType="Long", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "5", name="processflag", type="Column", dataType="String", initial="", history="")
	private String processflag;
	
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
	
	@CTORMTemplate(seq = "11", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	
	
	public String getmqcTemplateName() {
		return mqcTemplateName;
	}
	public void setmqcTemplateName(String mqcTemplateName) {
		this.mqcTemplateName = mqcTemplateName;
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
	
	public long getposition() {
		return position;
	}
	public void setposition(long position) {
		this.position = position;
	}
	
	public String getrecipeName() {
		return recipeName;
	}
	public void setrecipeName(String recipeName) {
		this.recipeName = recipeName;
	}
	
	public String getprocessflag() {
		return processflag;
	}
	public void setprocessflag(String processflag) {
		this.processflag = processflag;
	}
	
	public long getmqcCountUp() {
		return mqcCountUp;
	}
	public void setmqcCountUp(long mqcCountUp) {
		this.mqcCountUp = mqcCountUp;
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
	public MQCTemplatePosition()
	{
		
	}
	
	public MQCTemplatePosition(String mqcTemplateName, String processOperationName, String processOperationVersion, long position)
	{
		setmqcTemplateName(mqcTemplateName);
		setprocessOperationName(processOperationName);
		setprocessOperationVersion(processOperationVersion);
		setposition(position);
	}
}
