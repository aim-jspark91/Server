package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeIdleTime extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;
	
	@CTORMTemplate(seq = "5", name="idleTime", type="Column", dataType="Long", initial="", history="")
	private long idleTime;
	
	@CTORMTemplate(seq = "6", name="validFlag", type="Column", dataType="String", initial="", history="")
	private String validFlag;
	
	@CTORMTemplate(seq = "7", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	
	public String getmachineName() {
		return machineName;
	}
	public void setmachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getrecipeName() {
		return recipeName;
	}
	public void setrecipeName(String recipeName) {
		this.recipeName = recipeName;
	}
	
	public String getfactoryName() {
		return factoryName;
	}
	public void setfactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getareaName() {
		return areaName;
	}
	public void setareaName(String areaName) {
		this.areaName = areaName;
	}
	
	public long getidleTime() {
		return idleTime;
	}
	public void setidleTime(long idleTime) {
		this.idleTime = idleTime;
	}
	
	public String getvalidFlag() {
		return validFlag;
	}
	public void setvalidFlag(String validFlag) {
		this.validFlag = validFlag;
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
	public RecipeIdleTime()
	{
		
	}
	
	public RecipeIdleTime(String machineName, String recipeName)
	{
		setmachineName(machineName);
		setrecipeName(recipeName);
	}
}
