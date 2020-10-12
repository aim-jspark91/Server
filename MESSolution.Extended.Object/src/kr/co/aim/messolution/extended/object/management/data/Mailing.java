package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Mailing extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="mailID", type="Key", dataType="String", initial="", history="")
	private String mailID;
	
	@CTORMTemplate(seq = "2", name="system", type="Column", dataType="String", initial="", history="")
	private String system;
	
	@CTORMTemplate(seq = "3", name="mailCat", type="Column", dataType="String", initial="", history="")
	private String mailCat;
	
	@CTORMTemplate(seq = "4", name="alarmCode", type="Column", dataType="String", initial="", history="")
	private String alarmCode;	
	
	@CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;	
	
	@CTORMTemplate(seq = "6", name="toUser", type="Column", dataType="String", initial="", history="")
	private String toUser;
	
	@CTORMTemplate(seq = "7", name="ccUser", type="Column", dataType="String", initial="", history="")
	private String ccUser;
	
	@CTORMTemplate(seq = "8", name="subject", type="Column", dataType="String", initial="", history="")
	private String subject;
	
	@CTORMTemplate(seq = "9", name="contents", type="Column", dataType="String", initial="", history="")
	private String contents;
	
	@CTORMTemplate(seq = "10", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "11", name="sendTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp sendTime;
	
	@CTORMTemplate(seq = "12", name="sendFlag", type="Column", dataType="String", initial="", history="N")
	private String sendFlag;

	//instantiation
	public Mailing()
	{
		
	}
	
	public Mailing(String mailID)
	{
		setMailID(mailID);
	}

	public String getMailID() {
		return mailID;
	}

	public void setMailID(String mailID) {
		this.mailID = mailID;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getMailCat() {
		return mailCat;
	}

	public void setMailCat(String mailCat) {
		this.mailCat = mailCat;
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public String getCcUser() {
		return ccUser;
	}

	public void setCcUser(String ccUser) {
		this.ccUser = ccUser;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getSendTime() {
		return sendTime;
	}

	public void setSendTime(Timestamp sendTime) {
		this.sendTime = sendTime;
	}

	public String getSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(String sendFlag) {
		this.sendFlag = sendFlag;
	}
}
