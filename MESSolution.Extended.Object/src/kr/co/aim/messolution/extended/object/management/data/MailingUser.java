package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MailingUser extends UdfAccessor {
    
    @CTORMTemplate(seq = "1", name="system", type="Key", dataType="String", initial="", history="")
    private String system;
    
    @CTORMTemplate(seq = "2", name="alarmCode", type="Key", dataType="String", initial="", history="")
    private String alarmCode;   
    
    @CTORMTemplate(seq = "3", name="userID", type="Key", dataType="String", initial="", history="")
    private String userID;
    
    @CTORMTemplate(seq = "4", name="userGroupName", type="Key", dataType="String", initial="", history="")
    private String userGroupName;
    
    @CTORMTemplate(seq = "5", name="mailCat", type="Column", dataType="String", initial="", history="")
    private String mailCat;
    
    @CTORMTemplate(seq = "6", name="machineName", type="Column", dataType="String", initial="", history="")
    private String machineName; 
    
    @CTORMTemplate(seq = "7", name="sendType", type="Column", dataType="String", initial="", history="")
    private String sendType;
    
    @CTORMTemplate(seq = "8", name="mailAddr", type="Column", dataType="String", initial="", history="")
    private String mailAddr;
    
    //instantiation
    public MailingUser()
    {
        
    }
    
    public MailingUser(String system, String alarmCode, String userID, String userGroupName)
    {
        setSystem(system);
        setAlarmCode(alarmCode);
        setUserID(userID);
        setUserGroupName(userGroupName);
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(String alarmCode) {
        this.alarmCode = alarmCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMailCat() {
        return mailCat;
    }

    public void setMailCat(String mailCat) {
        this.mailCat = mailCat;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getMailAddr() {
        return mailAddr;
    }

    public void setMailAddr(String mailAddr) {
        this.mailAddr = mailAddr;
    }

	public String getUserGroupName() {
		return userGroupName;
	}

	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}
    
    
}
