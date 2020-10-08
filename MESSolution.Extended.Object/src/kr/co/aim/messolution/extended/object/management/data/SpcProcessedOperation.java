package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SpcProcessedOperation extends UdfAccessor {

    @CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
    private String timeKey;

    @CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
    private String productName;

    @CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
    private String factoryName;

    @CTORMTemplate(seq = "4", name="lotName", type="Column", dataType="String", initial="", history="")
    private String lotName;
    
    @CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="")
    private String productSpecName;
    
    @CTORMTemplate(seq = "6", name="processFlowName", type="Column", dataType="String", initial="", history="")
    private String processFlowName;
    
    @CTORMTemplate(seq = "7", name="processOperationName", type="Column", dataType="String", initial="", history="")
    private String processOperationName;
    
    @CTORMTemplate(seq = "8", name="machineName", type="Column", dataType="String", initial="", history="")
    private String machineName;

    @CTORMTemplate(seq = "9", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
    private String machineRecipeName;
    
    @CTORMTemplate(seq = "10", name="unitName", type="Column", dataType="String", initial="", history="")
    private String unitName;
    
    @CTORMTemplate(seq = "11", name="unitRecipeName", type="Column", dataType="String", initial="", history="")
    private String unitRecipeName;
    
    @CTORMTemplate(seq = "12", name="unitType", type="Column", dataType="String", initial="", history="")
    private String unitType;
    
    @CTORMTemplate(seq = "13", name="subUnitName", type="Column", dataType="String", initial="", history="")
    private String subUnitName;
    
    @CTORMTemplate(seq = "14", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
    private Timestamp createTime;

    @CTORMTemplate(seq = "15", name="eventName", type="Column", dataType="String", initial="", history="")
    private String eventName;

    @CTORMTemplate(seq = "16", name="eventTimeKey", type="Column", dataType="String", initial="", history="")
    private String eventTimeKey;
    
    @CTORMTemplate(seq = "17", name="eventUser", type="Column", dataType="String", initial="", history="")
    private String eventUser;
    
    //instantiation
    public SpcProcessedOperation()
    {
    }

    public SpcProcessedOperation(String timeKey, String productName)
    {
        this.timeKey = timeKey;
        this.productName = productName;
    }

    public String getTimeKey() {
        return timeKey;
    }

    public void setTimekey(String timeKey) {
        this.timeKey = timeKey;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }
    
    public String getProductSpecName() {
        return productSpecName;
    }

    public void setProductSpecName(String productSpecName) {
        this.productSpecName = productSpecName;
    }
    
    public String getProcessFlowName() {
        return processFlowName;
    }

    public void setProcessFlowName(String processFlowName) {
        this.processFlowName = processFlowName;
    }
    
    public String getProcessOperationName() {
        return processOperationName;
    }

    public void setProcessOperationName(String processOperationName) {
        this.processOperationName = processOperationName;
    }
    
    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
    
    public String getMachineRecipeName() {
        return machineRecipeName;
    }

    public void setMachineRecipeName(String machineRecipeName) {
        this.machineRecipeName = machineRecipeName;
    }
    
    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    public String getUnitRecipeName() {
        return unitRecipeName;
    }

    public void setUnitRecipeName(String unitRecipeName) {
        this.unitRecipeName = unitRecipeName;
    }
    
    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
    
    public String getSubUnitName() {
        return subUnitName;
    }

    public void setSubUnitName(String subUnitName) {
        this.subUnitName = subUnitName;
    }
    
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public String getEvnetName() {
        return eventName;
    }

    public void setEvnetName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getEventTimeKey() {
        return eventTimeKey;
    }

    public void setEventTimekey(String eventTimeKey) {
        this.eventTimeKey = eventTimeKey;
    }
    
    public String getEvnetUser() {
        return eventUser;
    }

    public void setEvnetUser(String eventUser) {
        this.eventUser = eventUser;
    }
}
