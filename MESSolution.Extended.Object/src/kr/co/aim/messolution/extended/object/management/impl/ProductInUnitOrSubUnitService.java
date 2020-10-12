package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductInUnitOrSubUnit;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class ProductInUnitOrSubUnitService extends CTORMService<ProductInUnitOrSubUnit> {

    public static Log logger = LogFactory.getLog(ProductInUnitOrSubUnitService.class);

    private final String historyEntity = "";

    public List<ProductInUnitOrSubUnit> select(String condition, Object[] bindSet)
            throws CustomException {

        List<ProductInUnitOrSubUnit> result = super.select(condition, bindSet,
                ProductInUnitOrSubUnit.class);

        return result;
    }

    public ProductInUnitOrSubUnit selectByKey(boolean isLock, Object[] keySet)
            throws CustomException, greenFrameDBErrorSignal {
        try
        {
            return super.selectByKey(ProductInUnitOrSubUnit.class, isLock, keySet);
        }
        catch(greenFrameDBErrorSignal ns)
        {
            throw ns;
        }

    }

    public ProductInUnitOrSubUnit create(EventInfo eventInfo, ProductInUnitOrSubUnit dataInfo)
            throws CustomException {

        super.insert(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }

    public void remove(EventInfo eventInfo, ProductInUnitOrSubUnit dataInfo)
            throws CustomException {

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        super.delete(dataInfo);

    }

    public ProductInUnitOrSubUnit modify(EventInfo eventInfo, ProductInUnitOrSubUnit dataInfo)
            throws CustomException {
        super.update(dataInfo);

        super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

        return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());

    }


    public boolean setProductProcessUnitOrSubUnit(EventInfo eventInfo, Machine machine, Element product, String carrierName) throws CustomException
    {
        String productName = StringUtil.EMPTY;
        String processedUnitName = StringUtil.EMPTY;
        String processedSubUnitName = StringUtil.EMPTY;
        String lotName = StringUtil.EMPTY;
        String productRecipeName = StringUtil.EMPTY;
        String processOperationName = StringUtil.EMPTY;
        String productSpecName = StringUtil.EMPTY;
        int iSeq = 0;
        /* Used by OLED ==>> */
        /*
        String maskName = StringUtil.EMPTY;
        String sourceName = StringUtil.EMPTY;
        String maskSide = StringUtil.EMPTY;
        String offSetX = StringUtil.EMPTY;
        String offSetY = StringUtil.EMPTY;
        String offSetT = StringUtil.EMPTY;
        */
        /* <<== Used by OLED */

        List<Element> processedUnitElement = null;
        List<Element> processedSubUnitElement = null;
        //EventInfo eventInfoProcessUnit = null;
        EventInfo eventInfoProcessUnit = eventInfo;

        boolean isResult = true;

        try
        {
            productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
            lotName =  SMessageUtil.getChildText(product, "LOTNAME", false);
            productRecipeName = SMessageUtil.getChildText(product, "PRODUCTRECIPE", false);
            processOperationName = SMessageUtil.getChildText(product, "PROCESSOPERATIONNAME", false);
            productSpecName = SMessageUtil.getChildText(product, "PRODUCTSPECNAME", false);

            //Get Lot Data
            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

            processedUnitElement = SMessageUtil.getSubSequenceItemList(product, "PROCESSEDUNITLIST", false);

            if(processedUnitElement.size() > 0)
            {
                ProductInUnitOrSubUnit pProductInUnitOrSubUnit = new ProductInUnitOrSubUnit();
                //eventInfoProcessUnit = EventInfoUtil.makeEventInfo("LotProcessEnd", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

                pProductInUnitOrSubUnit.setProductName(productName);
                pProductInUnitOrSubUnit.setMachineName(machine.getKey().getMachineName());
                pProductInUnitOrSubUnit.setMachineRecipeName(lotData.getMachineRecipeName());
                pProductInUnitOrSubUnit.setCarrierName(carrierName);
                pProductInUnitOrSubUnit.setFactoryName(machine.getFactoryName());
                pProductInUnitOrSubUnit.setLotName(lotName);
                pProductInUnitOrSubUnit.setProductRecipeName(productRecipeName);
                pProductInUnitOrSubUnit.setProcessOperationName(processOperationName);
                pProductInUnitOrSubUnit.setProductSpecName(productSpecName);

                pProductInUnitOrSubUnit.setTimekey(eventInfoProcessUnit.getEventTimeKey());
                pProductInUnitOrSubUnit.setCreateTime(eventInfoProcessUnit.getEventTime());
                pProductInUnitOrSubUnit.setEvnetName(eventInfoProcessUnit.getEventName());
                pProductInUnitOrSubUnit.setEvnetUser(eventInfoProcessUnit.getEventUser());

                iSeq = 1;
                for (Element processUnit : processedUnitElement )
                {
                    processedUnitName = SMessageUtil.getChildText(processUnit, "PROCESSEDUNITNAME", false);

                    pProductInUnitOrSubUnit.setUnitName(processedUnitName);

                    processedSubUnitElement = SMessageUtil.getSubSequenceItemList(processUnit, "PROCESSEDSUBUNITLIST", false);
                    if(processedSubUnitElement.size() > 0)
                    {
                        for (Element processSubUnit : processedSubUnitElement )
                        {
                            /* Used by OLED ==>> */
                            /*
                            maskName = SMessageUtil.getChildText(processSubUnit, "MASKNAME", false);
                            sourceName = SMessageUtil.getChildText(processSubUnit, "SOURCENAME", false);
                            maskSide = SMessageUtil.getChildText(processSubUnit, "MASKSIDE", false);
                            offSetX = SMessageUtil.getChildText(processSubUnit, "OFFSETX", false);
                            offSetY = SMessageUtil.getChildText(processSubUnit, "OFFSETY", false);
                            offSetT = SMessageUtil.getChildText(processSubUnit, "OFFSETT", false);
                            pProductInUnitOrSubUnit.setMaskName(maskName);
                            pProductInUnitOrSubUnit.setSourceName(sourceName);
                            pProductInUnitOrSubUnit.setMaskSide(maskSide);
                            pProductInUnitOrSubUnit.setOffSetX(offSetX);
                            pProductInUnitOrSubUnit.setOffSetY(offSetY);
                            pProductInUnitOrSubUnit.setOffSetT(offSetT);
                            */
                            /* <<== Used by OLED */

                            pProductInUnitOrSubUnit.setSeq(String.valueOf(iSeq));
                            iSeq += 1;

                            processedSubUnitName = SMessageUtil.getChildText(processSubUnit, "PROCESSEDSUBUNITNAME", false);
                            pProductInUnitOrSubUnit.setSubUnitName(processedSubUnitName);

                            pProductInUnitOrSubUnit = ExtendedObjectProxy.getProductInUnitOrSubUnitService().create(eventInfoProcessUnit, pProductInUnitOrSubUnit);

                        }
                    }
                    else
                    {
                        pProductInUnitOrSubUnit.setSeq(String.valueOf(iSeq));
                        iSeq += 1;
                        pProductInUnitOrSubUnit = ExtendedObjectProxy.getProductInUnitOrSubUnitService().create(eventInfoProcessUnit, pProductInUnitOrSubUnit);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.warn(String.format("Set LastExposureFeedBackData failed.[%s - %s - %s] ", productName, lotName, productSpecName));
            isResult = false;
        }

        return isResult;
    }
}