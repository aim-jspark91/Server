package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TimeSchedule extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="serverName", type="Key", dataType="String", initial="", history="")
	private String serverName;
	
	@CTORMTemplate(seq = "2", name="messageName", type="Key", dataType="String", initial="", history="")
	private String messageName;
	
	@CTORMTemplate(seq = "3", name="messageBody", type="Column", dataType="String", initial="", history="")
	private String messageBody;
	
	@CTORMTemplate(seq = "4", name="startTime", type="Column", dataType="String", initial="", history="")
	private String startTime;
	
	@CTORMTemplate(seq = "5", name="timeSchedule", type="Column", dataType="String", initial="", history="")
	private String timeSchedule;
	
	@CTORMTemplate(seq = "6", name="runningServer", type="Column", dataType="String", initial="", history="N")
	private String runningServer;

	@CTORMTemplate(seq = "7", name="cronExpression", type="Column", dataType="String", initial="", history="N")
	private String cronExpression;
	
	@CTORMTemplate(seq = "8", name="active", type="Column", dataType="String", initial="", history="N")
	private String active;
	
	//instantiation
	public TimeSchedule()
	{
		
	}
	
	public TimeSchedule(String serverName, String messageName)
	{
		this.setServerName(serverName);
		this.setMessageName(messageName);
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startName) {
		this.startTime = startName;
	}

	public String getTimeSchedule() {
		return timeSchedule;
	}

	public void setTimeSchedule(String timeSchedule) {
		this.timeSchedule = timeSchedule;
	}

	public String getRunningServer() {
		return runningServer;
	}

	public void setRunningServer(String runningServer) {
		this.runningServer = runningServer;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	
}
