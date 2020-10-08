package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductInUnitOrSubUnit extends UdfAccessor {

    @CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
    private String timeKey;

    @CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
    private String productName;

    @CTORMTemplate(seq = "3", name="seQ", type="Key", dataType="String", initial="", history="")
    private String seQ;

    @CTORMTemplate(seq = "4", name="factoryName", type="Column", dataType="String", initial="", history="")
    private String factoryName;

    @CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
    private String machineName;

    @CTORMTemplate(seq = "6", name="unitName", type="Column", dataType="String", initial="", history="")
    private String unitName;

    @CTORMTemplate(seq = "7", name="subUnitName", type="Column", dataType="String", initial="", history="")
    private String subUnitName;

    @CTORMTemplate(seq = "8", name="lotName", type="Column", dataType="String", initial="", history="")
    private String lotName;

    @CTORMTemplate(seq = "9", name="carrierName", type="Column", dataType="String", initial="", history="")
    private String carrierName;

    @CTORMTemplate(seq = "10", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
    private String machineRecipeName;

    @CTORMTemplate(seq = "11", name="productRecipeName", type="Column", dataType="String", initial="", history="")
    private String productRecipeName;

    @CTORMTemplate(seq = "12", name="processOperationName", type="Column", dataType="String", initial="", history="")
    private String processOperationName;

    @CTORMTemplate(seq = "13", name="productSpecName", type="Column", dataType="String", initial="", history="")
    private String productSpecName;

    @CTORMTemplate(seq = "14", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
    private Timestamp createTime;

    @CTORMTemplate(seq = "15", name="eventName", type="Column", dataType="String", initial="", history="")
    private String eventName;

    @CTORMTemplate(seq = "16", name="eventUser", type="Column", dataType="String", initial="", history="")
    private String eventUser;

    /* Add OLED Process Unit / SubUnit ==>> */
//    @CTORMTemplate(seq = "13", name="productSpecName", type="Column", dataType="String", initial="", history="")
//    private String productSpecName;
//
//    @CTORMTemplate(seq = "14", name="maskName", type="Column", dataType="String", initial="", history="")
//    private String maskName;
//
//    @CTORMTemplate(seq = "15", name="sourceName", type="Column", dataType="String", initial="", history="")
//    private String sourceName;
//
//    @CTORMTemplate(seq = "16", name="maskSide", type="Column", dataType="String", initial="", history="")
//    private String maskSide;
//
//    @CTORMTemplate(seq = "17", name="offSetX", type="Column", dataType="String", initial="", history="")
//    private String offSetX;
//
//    @CTORMTemplate(seq = "18", name="offSetY", type="Column", dataType="String", initial="", history="")
//    private String offSetY;
//
//    @CTORMTemplate(seq = "19", name="offSetT", type="Column", dataType="String", initial="", history="")
//    private String offSetT;
//
//    @CTORMTemplate(seq = "20", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
//    private Timestamp createTime;
//
//    @CTORMTemplate(seq = "21", name="eventName", type="Column", dataType="String", initial="", history="")
//    private String eventName;
//
//    @CTORMTemplate(seq = "22", name="eventUser", type="Column", dataType="String", initial="", history="")
//    private String eventUser;
    /* <<== Add OLED Process Unit / SubUnit */
    //instantiation
    public ProductInUnitOrSubUnit()
    {
    }

    public ProductInUnitOrSubUnit(String timeKey, String productName, String seQ)
    {
        this.timeKey = timeKey;
        this.productName = productName;
        this.seQ = seQ;
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

    public String getSeq() {
        return seQ;
    }

    public void setSeq(String seQ) {
        this.seQ = seQ;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
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

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
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

    public String getProductRecipeName() {
        return productRecipeName;
    }

    public void setProductRecipeName(String productRecipeName) {
        this.productRecipeName = productRecipeName;
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

    /* Add OLED Process Unit / SubUnit ==>> */
//    public String getMaskName() {
//        return maskName;
//    }
//
//    public void setMaskName(String maskName) {
//        this.maskName = maskName;
//    }
//
//    public String getSourceName() {
//        return sourceName;
//    }
//
//    public void setSourceName(String sourceName) {
//        this.sourceName = sourceName;
//    }
//
//    public String getMaskSide() {
//        return maskSide;
//    }
//
//    public void setMaskSide(String maskSide) {
//        this.maskSide = maskSide;
//    }
//
//    public String getOffSetX() {
//        return offSetX;
//    }
//
//    public void setOffSetX(String offSetX) {
//        this.offSetX = offSetX;
//    }
//
//    public String getOffSetY() {
//        return offSetY;
//    }
//
//    public void setOffSetY(String offSetY) {
//        this.offSetY = offSetY;
//    }
//
//    public String getOffSetT() {
//        return offSetT;
//    }
//
//    public void setOffSetT(String offSetT) {
//        this.offSetT = offSetT;
//    }
    /* <<== Add OLED Process Unit / SubUnit */

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

    public String getEvnetUser() {
        return eventUser;
    }

    public void setEvnetUser(String eventUser) {
        this.eventUser = eventUser;
    }


}
