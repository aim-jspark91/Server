/**
 * 
 */
package kr.co.aim.messolution.datacollection.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetDailyDataReply extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {        
      /*MACHINENAME             
        UNITLIST                
            UNIT            
                UNITNAME        
                LOTNAME     
                PRODUCTNAME     
                CARRIERNAME     
                MACHINERECIPENAME       
                PROCESSOPERATIONNAME        
                PRODUCTSPECNAME     
                ITEMLIST        
                    ITEM    
                        ITEMNAME
                        ITEMVALUE*/

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetDailyDataReply", getEventUser(), getEventComment(), null, null);
        
        Document cloneDoc = (Document) doc.clone();
        
        String machineName          = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        
        /* 20181130, hhlee, add Machine Validation */
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        
        try
        {
        	List<Element> unitList      = SMessageUtil.getBodySequenceItemList(cloneDoc, "UNITLIST", true);
        }
        catch(Exception ex)
        {
        	String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
            String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
            
            if(!StringUtils.equals(returnCode, "0"))
            {
            	throw new CustomException("COMMON-0001", returnMessage);
            }
            else
            {
            	throw ex;
            }
        }
        
        /* 20181206, hhlee, modify, add EDC Send */
        /* 1. SPC Message Setting */
        SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "MachineCheckData");
        
        /* 20181218, hhlee, add, add FACTORYNAME Element (Requsted by SPC) ==>> */
        SMessageUtil.setBodyItemValue(cloneDoc, "FACTORYNAME", machineData.getFactoryName(), true);
        /* <<== 20181218, hhlee, add, add FACTORYNAME Element (Requsted by SPC) */
        
        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), cloneDoc, "EDCSender");    
        
        /* 20181206, hhlee, delete, add EDC Send ==>> */
        //try
        //{
        //    String unitName             = StringUtil.EMPTY;
        //    String subUnitName          = StringUtil.EMPTY;
        //    String lotName              = StringUtil.EMPTY;
        //    String carrierName          = StringUtil.EMPTY;
        //    String productName          = StringUtil.EMPTY;
        //    String machineRecipeName    = StringUtil.EMPTY;
        //    String processOperationName = StringUtil.EMPTY;
        //    String productSpecName      = StringUtil.EMPTY;
        //    
        //    Element itemList            = null;
        //    
        //    //update Product turn result            
        //    for(Element elementUnit : unitList)
        //    {
        //        unitName             = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
        //        lotName              = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        //        carrierName          = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
        //        productName          = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
        //        machineRecipeName    = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        //        processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
        //        productSpecName      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
        //                        
        //        itemList = SMessageUtil.getSubSequenceItem(elementUnit, "ITEMLIST", false);
        //     
        //        /////////////////////////////////////
        //        // 1. Get Data & Validation        //
        //        /////////////////////////////////////
        //        /* 20181130, hhlee, add DefaultFactory */
        //        String factoryName = machineData.getFactoryName();
        //        String processFlowName = StringUtil.EMPTY;
        //        String processOperationType = StringUtil.EMPTY;
        //        
        //        /* 20181206, hhlee, add, change SPC send message spec */
        //        String woType = StringUtil.EMPTY;
        //        
        //        if(StringUtil.isNotEmpty(lotName))
        //        {
        //            Lot lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
        //            factoryName = lotData.getFactoryName();
        //            processFlowName = lotData.getProcessOperationName();
        //            
        //            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName()); 
        //            processOperationType = processOperationSpecData.getProcessOperationType();  
        //            
        //            /* 20181206, hhlee, add, change SPC send message spec */
        //            ProductRequest pData = CommonUtil.getProductRequestData(lotData.getProductRequestName());
        //            woType = pData.getProductRequestType();
        //        }
        //            
        //        /* 20181206, hhlee, add, change SPC send message spec */
        //        /* 1. SPC Message Setting */
        //        SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "MachineCheckData");
        //        MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataByReceiveData(SMessageUtil.getBodyElement(cloneDoc), machineName, 
        //                unitName, subUnitName, lotName, carrierName, productName, machineRecipeName, processFlowName, processOperationName, 
        //                processOperationType, factoryName, productSpecName, woType, itemList);            
        //        
        //        //PROCLIST
        //        Element bodyElement = SMessageUtil.getBodyElement(cloneDoc);
        //        
        //        Element procListElement = new Element("PROCLIST");
        //        procListElement.setText("");
        //        bodyElement.addContent(procListElement);
        //
        //        /* 20181206, hhlee, delete, change SPC send message spec */
        //        ///* 3. TimeStamp Setting */
        //        //Element bodyElement = SMessageUtil.getBodyElement(cloneDoc);
        //        //Element elementTimeStamp = new Element("TIMESTAMP");
        //        //elementTimeStamp.setText(eventInfo.getEventTime().toString());
        //        //bodyElement.addContent(elementTimeStamp);
        //        
        //        eventLog.info(cloneDoc.toString());
        //        
        //        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), cloneDoc, "EDCSender");                 
        //    }
        //}
        //catch (Exception ex)
        //{
        //    eventLog.error(ex.getMessage());            
        //} 
        /* <<== 20181206, hhlee, delete, add EDC Send */
        
        /*MACHINENAME
        UNITNAME
        SUBUNITNAME
        LOTNAME
        CARRIERNAME
        PRODUCTNAME 
        MACHINERECIPENAME
        PROCESSOPERATIONNAME
        PRODUCTSPECNAME
        PROCESSDATATYPE
        ITEMLIST

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetDailyData", getEventUser(), getEventComment(), null, null);
        
        Document cloneDoc = (Document) doc.clone();
        
        String machineName          = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String unitName          = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
        String subUnitName          = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
        String lotName          = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        String carrierName              = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
        String productName          = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
        String machineRecipeName    = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
        String productSpecName      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
        String processDataType      = SMessageUtil.getBodyItemValue(doc, "PROCESSDATATYPE", false);
        
        Element itemList = SMessageUtil.getBodySequenceItem(cloneDoc, "ITEMLIST", false);
        
        try
        {
            MACHINENAME
            UNITNAME
            SUBUNITNAME
            LOTNAME
            CARRIERNAME
            PRODUCTNAME 
            MACHINERECIPENAME
            PROCESSFLOWNAME --
            PROCESSOPERATIONNAME
            PROCESSOPERATIONTYPE --
            FACTORYNAME --
            PRODUCTSPECNAME
            ITEMLIST

            /////////////////////////////////////
            // 1. Get Data & Validation        //
            /////////////////////////////////////
            String factoryName = StringUtil.EMPTY;
            String processFlowName = StringUtil.EMPTY;
            String processOperationType = StringUtil.EMPTY;
            
            if(StringUtil.isNotEmpty(lotName))
            {
                Lot lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                factoryName = lotData.getFactoryName();
                processFlowName = lotData.getProcessOperationName();
                
                ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName()); 
                processOperationType = processOperationSpecData.getProcessOperationType();                
            }
                        
             1. SPC Message Setting 
            SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "SPC_GetDailyData");
            this.generateSpcProductProcessDataByReceiveData(SMessageUtil.getBodyElement(cloneDoc), machineName, unitName, subUnitName, lotName, carrierName, 
                                    productName, machineRecipeName, processFlowName, processOperationName, 
                                    processOperationType, factoryName, productSpecName, itemList);
            
             2. PROCLIST Message Setting 
            this.generateSpcProductProcessDataProcList(eventInfo, SMessageUtil.getBodyElement(cloneDoc), factoryName, 
                    productSpecName, processFlowName,  processOperationName, lotName, machineName, unitName, machineRecipeName);
            
            eventLog.info(cloneDoc.toString());
            
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), cloneDoc, "EDCSender");
        }
        catch (Exception ex)
        {
            eventLog.error(ex.getMessage());            
        }*/
    }
    
    private Element generateSpcProductProcessDataByReceiveData(Element elementBodyTemplate, String machineName, String unitName, String subUnitName,
            String lotName, String carrierName, String productName, String machineRecipeName, String processFlowName, 
            String processOperationName, String processOperationType, String factoryName, String productSpecName, Element itemList)
            throws CustomException
    {
        
        /*MACHINENAME
        UNITNAME
        SUBUNITNAME
        LOTNAME
        CARRIERNAME
        PRODUCTNAME 
        MACHINERECIPENAME
        PROCESSFLOWNAME --
        PROCESSOPERATIONNAME
        PROCESSOPERATIONTYPE --
        FACTORYNAME --
        PRODUCTSPECNAME
        ITEMLIST*/
        
        //SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "SPC_ProductProcessData");
        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
        
        //Element elementBodyTemplate = SMessageUtil.getBodyElement(doc);
        elementBodyTemplate.removeContent();
        
        //boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
        //Element elementBodyTemplate = new Element(SMessageUtil.Body_Tag);
        
        Element elementMachineName = new Element("MACHINENAME");
        elementMachineName.setText(machineName);
        elementBodyTemplate.addContent(elementMachineName);
        
        Element elementUnitName = new Element("UNITNAME");
        elementUnitName.setText(unitName);
        elementBodyTemplate.addContent(elementUnitName);
        
        Element elementSubUnitName = new Element("SUBUNITNAME");
        elementSubUnitName.setText(subUnitName);
        elementBodyTemplate.addContent(elementSubUnitName);
        
        Element elementLotName = new Element("LOTNAME");
        elementLotName.setText(lotName);
        elementBodyTemplate.addContent(elementLotName);
        
        Element elementCarrierName = new Element("CARRIERNAME");
        elementCarrierName.setText(carrierName);
        elementBodyTemplate.addContent(elementCarrierName);
        
        Element elementProductName = new Element("PRODUCTNAME");
        elementProductName.setText(productName);
        elementBodyTemplate.addContent(elementProductName);
        
        Element elementMachineRecipeName = new Element("MACHINERECIPENAME");
        elementMachineRecipeName.setText(machineRecipeName);
        elementBodyTemplate.addContent(elementMachineRecipeName);
        
        Element elementProcessFlowName = new Element("PROCESSFLOWNAME");
        elementProcessFlowName.setText(processFlowName);
        elementBodyTemplate.addContent(elementProcessFlowName);
        
        Element elementProcessOperationName = new Element("PROCESSOPERATIONNAME");
        elementProcessOperationName.setText(processOperationName);
        elementBodyTemplate.addContent(elementProcessOperationName);
        
        Element elementProcessOperationType = new Element("PROCESSOPERATIONTYPE");
        elementProcessOperationType.setText(processOperationType);
        elementBodyTemplate.addContent(elementProcessOperationType);
        
        Element elementFactoryName= new Element("FACTORYNAME");
        elementFactoryName.setText(factoryName);
        elementBodyTemplate.addContent(elementFactoryName);
        
        Element elementProductSpecName = new Element("PRODUCTSPECNAME");
        elementProductSpecName.setText(productSpecName);
        elementBodyTemplate.addContent(elementProductSpecName);        
                
        //ItemList
        Element itemListElement =itemList;
        elementBodyTemplate.addContent(itemListElement);
        
        //itemListElement = itemList;
        //
        //itemListElement.addContent(itemList);
        
        //eventLog.info(doc.toString());
        
        //elementBodyTemplate.addContent(itemList);
        
        ////overwrite
        //doc.getRootElement().addContent(elementBodyTemplate);
        
        return elementBodyTemplate;    
    }
    
    private Element generateSpcProductProcessDataProcList(EventInfo eventInfo, Element bodyElement, String factoryName, 
            String productSpecName, String processFlowName,  String processOperationName, String lotName, String machineName, 
            String unitName, String machineRecipeName) throws CustomException
    {
     
        //Element bodyElement = SMessageUtil.getBodyElement(doc);
        
        /*FACTORYNAME
        PRODUCTSPECNAME
        PROCESSFLOWNAME
        PROCESSOPERATIONNAME
        MACHINENAME
        MACHINERECIPENAME
        PROCUNITLIST
        PROCUNITTYPE
        UNITRECIPELIST
        LOTNAME
        EVENTTIMEKEY*/
        
        //PROCLIST
        Element procListElement = new Element("PROCLIST");
        Element procElement = new Element("PROC");
        
        Element factoryNameElement = new Element("FACTORYNAME");
        factoryNameElement.setText(factoryName);
        procElement.addContent(factoryNameElement);
        
        Element productSpecNameElement = new Element("PRODUCTSPECNAME");
        productSpecNameElement.setText(productSpecName);
        procElement.addContent(productSpecNameElement);
        
        Element processFlowNameElement = new Element("PROCESSFLOWNAME");
        processFlowNameElement.setText(processFlowName);
        procElement.addContent(processFlowNameElement);
        
        Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
        processOperationNameElement.setText(processOperationName);
        procElement.addContent(processOperationNameElement);
        
        Element machineNameElement = new Element("MACHINENAME");
        machineNameElement.setText(machineName);
        procElement.addContent(machineNameElement);
        
        Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
        machineRecipeNameElement.setText(machineRecipeName);
        procElement.addContent(machineRecipeNameElement);
        
        String procUnitType = StringUtil.EMPTY;
        if(StringUtil.isNotEmpty(unitName))
        {
            MachineSpec unitSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
            procUnitType = unitSpecData.getMachineType();
        }        
        
        Element procUnitListElement = new Element("PROCUNITLIST");
        procUnitListElement.setText(unitName);
        procElement.addContent(procUnitListElement);
        
        Element procUnitTypeElement = new Element("PROCUNITTYPE");
        procUnitTypeElement.setText(procUnitType);
        procElement.addContent(procUnitTypeElement);
        
        Element unitRecipeListElement = new Element("UNITRECIPELIST");
        unitRecipeListElement.setText(StringUtil.EMPTY);
        procElement.addContent(unitRecipeListElement);
        
        Element lotNameElement = new Element("LOTNAME");
        lotNameElement.setText(lotName);
        procElement.addContent(lotNameElement);        
        
        Element eventTimeKeyElement = new Element("EVENTTIMEKEY");
        eventTimeKeyElement.setText(eventInfo.getEventTimeKey());
        procElement.addContent(eventTimeKeyElement);
        
        procListElement.addContent(procElement);        
        bodyElement.addContent(procListElement);
        
        Element elementTimeStamp = new Element("TIMESTAMP");
        elementTimeStamp.setText(eventInfo.getEventTime().toString());
        bodyElement.addContent(elementTimeStamp);
        
        return bodyElement;
    }
}
