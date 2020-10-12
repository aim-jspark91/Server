package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ExposureFeedBack extends UdfAccessor {

    @CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
    private String productName;

    @CTORMTemplate(seq = "2", name="lotName", type="Key", dataType="String", initial="", history="")
    private String lotName;

    @CTORMTemplate(seq = "3", name="machineName", type="Key", dataType="String", initial="", history="")
    private String machineName;

    @CTORMTemplate(seq = "4", name="unitName", type="Key", dataType="String", initial="", history="")
    private String unitName;

    @CTORMTemplate(seq = "5", name="processOperationName", type="Key", dataType="String", initial="", history="")
    private String processOperationName;

    @CTORMTemplate(seq = "6", name="productSpecName", type="Key", dataType="String", initial="", history="")
    private String productSpecName;

    @CTORMTemplate(seq = "7", name="maskName", type="Key", dataType="String", initial="", history="")
    private String maskName;

    @CTORMTemplate(seq = "8", name="carrierName", type="Column", dataType="String", initial="", history="")
    private String carrierName;

    @CTORMTemplate(seq = "9", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
    private String machineRecipeName;

    @CTORMTemplate(seq = "10", name="exposureRecipeName", type="Column", dataType="String", initial="", history="")
    private String exposureRecipeName;

    @CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="N")
    private String lastEventName;

    @CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
    private String lastEventTimeKey;

    @CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
    private Timestamp lastEventTime;

    @CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
    private String lastEventUser;

    @CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
    private String lastEventComment;

    //instantiation
    public ExposureFeedBack()
    {

    }

    public ExposureFeedBack(String productName, String lotName, String machineName, String unitName, String processOperationName, String productSpecName, String maskName)
    {
        this.productName = productName;
        this.lotName = lotName;
        this.machineName = machineName;
        this.unitName = unitName;
        this.processOperationName = processOperationName;
        this.productSpecName = productSpecName;
        this.maskName = maskName;
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

    public String getProcessOperationName() {
        return processOperationName;
    }

    public void setProcessOperationName(String processOperationName) {
        this.processOperationName = processOperationName;
    }
    //--
    public String getProductSpecName() {
        return productSpecName;
    }

    public void setProductSpecName(String productSpecName) {
        this.productSpecName = productSpecName;
    }

    public String getMaskName() {
        return maskName;
    }

    public void setMaskName(String maskName) {
        this.maskName = maskName;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getMachineRecipeName() {
        return machineRecipeName;
    }

    public void setMachineRecipeName(String machineRecipeName) {
        this.machineRecipeName = machineRecipeName;
    }
    //--
    public String getExposureRecipeName() {
        return exposureRecipeName;
    }

    public void setExposureRecipeName(String exposureRecipeName) {
        this.exposureRecipeName = exposureRecipeName;
    }

    public String getLastEvnetName() {
        return lastEventName;
    }

    public void setLastEvnetName(String lastEventName) {
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
