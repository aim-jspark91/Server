package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @since 2018.08.11
 * @author smkang
 * @see For management of equipment idle time.
 */
public class ChamberIdleTime extends UdfAccessor{
    @CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
    private String machineName;
    
    @CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
    private String unitName;
    
    @CTORMTemplate(seq = "3", name="subUnitName", type="Key", dataType="String", initial="", history="")
    private String subUnitName;
    
    @CTORMTemplate(seq = "4", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
    private String machineRecipeName;
    
    @CTORMTemplate(seq = "5", name="settingIdleTime", type="Column", dataType="Long", initial="", history="")
    private long settingIdleTime;
    
    @CTORMTemplate(seq = "6", name="idleTimeResult", type="Column", dataType="String", initial="", history="")
    private String idleTimeResult;
    
    @CTORMTemplate(seq = "7", name="mqcQuantity", type="Column", dataType="Long", initial="", history="")
    private long mqcQuantity;
        
    @CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
    private String lastEventName;
    
    @CTORMTemplate(seq = "9", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
    private String lastEventTimekey;
    
    @CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
    private Timestamp lastEventTime;
    
    @CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
    private String lastEventUser;
    
    @CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
    private String lastEventComment;
    
    public ChamberIdleTime() {
    }
    
    public ChamberIdleTime(String machineName, String unitName, String subUnitName) {
        this.setMachineName(machineName);
        this.setUnitName(unitName);
        this.setSubUnitName(subUnitName);
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getSubUnitName() {
        return subUnitName;
    }

    public void setSubUnitName(String subUnitName) {
        this.subUnitName = subUnitName;
    }
    
    public String getMachineRecipeName() {
        return machineRecipeName;
    }

    public void setMachineRecipeName(String machineRecipeName) {
        this.machineRecipeName = machineRecipeName;
    }
    
    public long getSettingIdleTime() {
        return settingIdleTime;
    }

    public void setSettingIdleTime(long settingIdleTime) {
        this.settingIdleTime = settingIdleTime;
    }
    
    public String getIdleTimeResult() {
        return idleTimeResult;
    }

    public void setIdleTimeResult(String idleTimeResult) {
        this.idleTimeResult = idleTimeResult;
    }

    public long getMqcQuantity() {
        return mqcQuantity;
    }

    public void setMqcQuantity(long mqcQuantity) {
        this.mqcQuantity = mqcQuantity;
    }

    public String getLastEventName() {
        return lastEventName;
    }

    public void setLastEventName(String lastEventName) {
        this.lastEventName = lastEventName;
    }

    public String getLastEventTimekey() {
        return lastEventTimekey;
    }

    public void setLastEventTimekey(String lastEventTimekey) {
        this.lastEventTimekey = lastEventTimekey;
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