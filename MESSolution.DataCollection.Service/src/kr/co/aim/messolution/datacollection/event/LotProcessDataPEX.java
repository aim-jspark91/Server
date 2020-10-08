package kr.co.aim.messolution.datacollection.event;

import kr.co.aim.messolution.datacollection.MESEDCServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class LotProcessDataPEX extends AsyncHandler {
 
    @Override
    public void doWorks(Document doc) throws CustomException  
    {
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
        ITEMLIST*/

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("LotProcessData", getEventUser(), getEventComment(), null, null);
        
        Document spcDoc = (Document) doc.clone();
        
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
        
        Element itemList = SMessageUtil.getBodySequenceItem(spcDoc, "ITEMLIST", false);
        
        /* 20181130, hhlee, add Machine Validation */
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        /* 20190311, hhlee, add validation  */
        MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        
        try
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

            /////////////////////////////////////
            // 1. Get Data & Validation        //
            /////////////////////////////////////
            String factoryName = machineData.getFactoryName();
            String processFlowName = StringUtil.EMPTY;
            String processOperationType = StringUtil.EMPTY;
            
            String woType = StringUtil.EMPTY;
            String downLoadID = StringUtil.EMPTY;
            
            /* 20190423, hhlee, Added ECCODE Item */
            String ecCode = StringUtil.EMPTY;
            
            /* 20190311, hhlee, add Cassette Cleaner validation  */
            if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
            {
                lotName = carrierName;
            }
            
            /* 20190311, hhlee, add validation  */
            if(StringUtil.isNotEmpty(lotName) &&
                    !StringUtil.equals(carrierName, lotName))
            {
                try
                {
                    Lot lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                    factoryName = lotData.getFactoryName();
                    processFlowName = lotData.getProcessFlowName();
                    processOperationName = lotData.getProcessOperationName();
                    
                    ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName()); 
                    processOperationType = processOperationSpecData.getProcessOperationType();  
                    
                    /* 20190410, hhlee, modify, add inquery productspecname */
                    productSpecName = lotData.getProductSpecName();
                    
                    downLoadID = CommonUtil.getValue(processOperationSpecData.getUdfs(), "DOWNLOADID");
                    
                    if(!StringUtil.equals(lotData.getProductRequestName(), StringUtil.EMPTY))
                    {
                        woType = CommonUtil.getWorkOrderType(lotData);
                    }
                    
                    /* 20190423, hhlee, Added ECCODE Item */
                    ecCode = CommonUtil.getValue(lotData.getUdfs(), "ECCODE");
                }
                catch (Exception ex)
                {
                    eventLog.warn(ex.getMessage());
                }
            }
            
            /* 1. SPC Message Setting */
            SMessageUtil.setHeaderItemValue(spcDoc, "MESSAGENAME", "LotProcessData");
            /* 20190423, hhlee, Added ECCODE Item */
            MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcLotProcessDataByReceiveData(SMessageUtil.getBodyElement(spcDoc), machineName, unitName, 
                    subUnitName, lotName, carrierName, machineRecipeName, processFlowName, processOperationName, 
                    processOperationType, factoryName, productSpecName, woType, ecCode, itemList);
            
            /* 1-1. SPC Message Processtime Setting */
            SMessageUtil.setBodyItemValue(spcDoc, "PROCESSTIME", eventInfo.getEventTime().toString());
            
            /* 2. PROCLIST Message Setting (When Sampling) */
            /* 2019620, hhlee, add, DCTYPE */
            if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            {
            	/* 2-1. SPC Message DCTYPE Setting */
                SMessageUtil.setBodyItemValue(spcDoc, "DCTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
            }
            
            /* 2019620, hhlee, modify, deleted validation ProcessOperationType */
            //if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
            if(StringUtil.isNotEmpty(downLoadID))
            {
                /* 2-1. SPC Message DCTYPE Setting */
            	/* 2019620, hhlee, delete, DCTYPE */
                //SMessageUtil.setBodyItemValue(spcDoc, "DCTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
                
                MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcLotProcessDataProcList_V2(eventInfo, 
                        SMessageUtil.getBodyElement(spcDoc), lotName ,downLoadID);
            }
            else
            {
                //PROCLIST
                Element bodyElement = SMessageUtil.getBodyElement(spcDoc);
                Element procListElement = new Element("PROCLIST");
                procListElement.setText("");
                bodyElement.addContent(procListElement);
            }
            
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), spcDoc, "EDCSender");
        }
        catch (Exception ex)
        {
            eventLog.error(ex.getMessage());            
        }
        
        /* 20190226, hhlee, Before SPC Send Logic change ==>> */
        ///*MACHINENAME
        //UNITNAME
        //SUBUNITNAME
        //LOTNAME
        //CARRIERNAME
        //PRODUCTNAME 
        //MACHINERECIPENAME
        //PROCESSOPERATIONNAME
        //PRODUCTSPECNAME
        //PROCESSDATATYPE
        //ITEMLIST*/
        //
        //EventInfo eventInfo = EventInfoUtil.makeEventInfo("LotProcessData", getEventUser(), getEventComment(), null, null);
        //
        //Document cloneDoc = (Document) doc.clone();
        //
        //String machineName          = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        //String unitName          = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
        //String subUnitName          = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
        //String lotName          = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        //String carrierName              = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
        //String productName          = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
        //String machineRecipeName    = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        //String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
        //String productSpecName      = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
        //String processDataType      = SMessageUtil.getBodyItemValue(doc, "PROCESSDATATYPE", false);
        //
        //Element itemList = SMessageUtil.getBodySequenceItem(cloneDoc, "ITEMLIST", false);
        //
        ///* 20181130, hhlee, add Machine Validation */
        //Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        //
        //try
        //{
        //    /*MACHINENAME
        //    UNITNAME
        //    SUBUNITNAME
        //    LOTNAME
        //    CARRIERNAME
        //    PRODUCTNAME 
        //    MACHINERECIPENAME
        //    PROCESSFLOWNAME --
        //    PROCESSOPERATIONNAME
        //    PROCESSOPERATIONTYPE --
        //    FACTORYNAME --
        //    PRODUCTSPECNAME
        //    ITEMLIST*/
        //
        //    /////////////////////////////////////
        //    // 1. Get Data & Validation        //
        //    /////////////////////////////////////
        //    /* 20181130, hhlee, add DefaultFactory */
        //    String factoryName = machineData.getFactoryName();
        //    String processFlowName = StringUtil.EMPTY;
        //    String processOperationType = StringUtil.EMPTY;
        //    
        //    /* 20181206, hhlee, add, change SPC send message spec */
        //    String woType = StringUtil.EMPTY;
        //    
        //    if(StringUtil.isNotEmpty(lotName))
        //    {
        //        Lot lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
        //        factoryName = lotData.getFactoryName();
        //        processFlowName = lotData.getProcessOperationName();
        //        
        //        ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName()); 
        //        processOperationType = processOperationSpecData.getProcessOperationType();  
        //        
        //        /* 20190211, hhlee, add, MQC Product is not exist ProductRequestName, validataion added  */
        //        if(!StringUtil.equals(lotData.getProductRequestName(), StringUtil.EMPTY))
        //        {
        //            /* 20181206, hhlee, add, change SPC send message spec */
        //            //ProductRequest pData = CommonUtil.getProductRequestData(lotData.getProductRequestName());                
        //            //woType = pData.getProductRequestType();
        //            
        //            //2019.02.25_hsryu_getWorkOrderType by Product. Mantis 0002757.
        //            woType = CommonUtil.getWorkOrderType(lotData);
        //        }
        //    }
        //    
        //    /* 20181206, hhlee, add, change SPC send message spec */
        //    /* 1. SPC Message Setting */
        //    SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "LotProcessData");
        //    MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcLotProcessDataByReceiveData(SMessageUtil.getBodyElement(cloneDoc), machineName, unitName, 
        //            subUnitName, lotName, carrierName, machineRecipeName, processFlowName, processOperationName, 
        //            processOperationType, factoryName, productSpecName, woType, itemList);
        //    
        //    /* 20181206, hhlee, add, change SPC send message spec */
        //    /* 1-1. SPC Message Processtime Setting */
        //    SMessageUtil.setBodyItemValue(cloneDoc, "PROCESSTIME", eventInfo.getEventTime().toString());
        //    
        //    /* 2. PROCLIST Message Setting (When Sampling) */
        //    if(StringUtil.equals(processOperationType, GenericServiceProxy.getConstantMap().Pos_Inspection))
        //    {
        //        /* 20181206, hhlee, add, change SPC send message spec */
        //        /* 2-1. SPC Message DCTYPE Setting */
        //        SMessageUtil.setBodyItemValue(cloneDoc, "DCTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
        //        
        //        MESEDCServiceProxy.getDataCollectionServiceUtil().generateSpcProductProcessDataProcList(eventInfo, SMessageUtil.getBodyElement(cloneDoc), factoryName, 
        //                productSpecName, processFlowName,  processOperationName, lotName, productName, machineName, unitName, machineRecipeName);
        //    }
        //    else
        //    {
        //        //PROCLIST
        //        Element bodyElement = SMessageUtil.getBodyElement(cloneDoc);
        //        Element procListElement = new Element("PROCLIST");
        //        procListElement.setText("");
        //        bodyElement.addContent(procListElement);
        //    }
        //    
        //    /* 20181206, hhlee, delete, change SPC send message spec */
        //    ///* 3. TimeStamp Setting */
        //    //Element bodyElement = SMessageUtil.getBodyElement(cloneDoc);
        //    //Element elementTimeStamp = new Element("TIMESTAMP");
        //    //elementTimeStamp.setText(eventInfo.getEventTime().toString());
        //    //bodyElement.addContent(elementTimeStamp);
        //    
        //    eventLog.info(cloneDoc.toString());
        //    
        //    GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("SPCsvr"), cloneDoc, "EDCSender");
        //}
        //catch (Exception ex)
        //{
        //    eventLog.error(ex.getMessage());            
        //}
        /* <<== 20190226, hhlee, Before SPC Send Logic change */ 
    }
    
//    private Element generateSpcProductProcessDataByReceiveData(Element elementBodyTemplate, String machineName, String unitName, String subUnitName,
//            String lotName, String carrierName, String productName, String machineRecipeName, String processFlowName, 
//            String processOperationName, String processOperationType, String factoryName, String productSpecName, Element itemList)
//            throws CustomException
//    {
//        
//        /*MACHINENAME
//        UNITNAME
//        SUBUNITNAME
//        LOTNAME
//        CARRIERNAME
//        PRODUCTNAME 
//        MACHINERECIPENAME
//        PROCESSFLOWNAME --
//        PROCESSOPERATIONNAME
//        PROCESSOPERATIONTYPE --
//        FACTORYNAME --
//        PRODUCTSPECNAME
//        ITEMLIST*/
//        
//        //SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "SPC_ProductProcessData");
//        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
//        
//        //Element elementBodyTemplate = SMessageUtil.getBodyElement(doc);
//        elementBodyTemplate.removeContent();
//        
//        //boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//        //Element elementBodyTemplate = new Element(SMessageUtil.Body_Tag);
//        
//        Element elementMachineName = new Element("MACHINENAME");
//        elementMachineName.setText(machineName);
//        elementBodyTemplate.addContent(elementMachineName);
//        
//        Element elementUnitName = new Element("UNITNAME");
//        elementUnitName.setText(unitName);
//        elementBodyTemplate.addContent(elementUnitName);
//        
//        Element elementSubUnitName = new Element("SUBUNITNAME");
//        elementSubUnitName.setText(subUnitName);
//        elementBodyTemplate.addContent(elementSubUnitName);
//        
//        Element elementLotName = new Element("LOTNAME");
//        elementLotName.setText(lotName);
//        elementBodyTemplate.addContent(elementLotName);
//        
//        Element elementCarrierName = new Element("CARRIERNAME");
//        elementCarrierName.setText(carrierName);
//        elementBodyTemplate.addContent(elementCarrierName);
//        
//        Element elementProductName = new Element("PRODUCTNAME");
//        elementProductName.setText(productName);
//        elementBodyTemplate.addContent(elementProductName);
//        
//        Element elementMachineRecipeName = new Element("MACHINERECIPENAME");
//        elementMachineRecipeName.setText(machineRecipeName);
//        elementBodyTemplate.addContent(elementMachineRecipeName);
//        
//        Element elementProcessFlowName = new Element("PROCESSFLOWNAME");
//        elementProcessFlowName.setText(processFlowName);
//        elementBodyTemplate.addContent(elementProcessFlowName);
//        
//        Element elementProcessOperationName = new Element("PROCESSOPERATIONNAME");
//        elementProcessOperationName.setText(processOperationName);
//        elementBodyTemplate.addContent(elementProcessOperationName);
//        
//        Element elementProcessOperationType = new Element("PROCESSOPERATIONTYPE");
//        elementProcessOperationType.setText(processOperationType);
//        elementBodyTemplate.addContent(elementProcessOperationType);
//        
//        Element elementFactoryName= new Element("FACTORYNAME");
//        elementFactoryName.setText(factoryName);
//        elementBodyTemplate.addContent(elementFactoryName);
//        
//        Element elementProductSpecName = new Element("PRODUCTSPECNAME");
//        elementProductSpecName.setText(productSpecName);
//        elementBodyTemplate.addContent(elementProductSpecName);        
//                
//        //ItemList
//        Element itemListElement =itemList;
//        elementBodyTemplate.addContent(itemListElement);
//        
//        //itemListElement = itemList;
//        //
//        //itemListElement.addContent(itemList);
//        
//        //eventLog.info(doc.toString());
//        
//        //elementBodyTemplate.addContent(itemList);
//        
//        ////overwrite
//        //doc.getRootElement().addContent(elementBodyTemplate);
//        
//        return elementBodyTemplate;    
//    }
    
//    /**
//     * 
//     * @Name     generateSpcProductProcessDataProcList
//     * @since    2018. 10. 9.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param bodyElement
//     * @param factoryName
//     * @param productSpecName
//     * @param processFlowName
//     * @param processOperationName
//     * @param lotName
//     * @param productName
//     * @param machineName
//     * @param unitName
//     * @param machineRecipeName
//     * @return
//     * @throws CustomException
//     */
//    private Element generateSpcProductProcessDataProcList(EventInfo eventInfo, Element bodyElement, String factoryName, 
//            String productSpecName, String processFlowName,  String processOperationName, String lotName, String productName, 
//            String machineName, String unitName, String machineRecipeName) throws CustomException
//    {
//     
//        //Element bodyElement = SMessageUtil.getBodyElement(doc);
//        
//        /*FACTORYNAME
//        PRODUCTSPECNAME
//        PROCESSFLOWNAME
//        PROCESSOPERATIONNAME
//        MACHINENAME
//        MACHINERECIPENAME
//        PROCUNITLIST
//        PROCUNITTYPE
//        UNITRECIPELIST
//        LOTNAME
//        EVENTTIMEKEY*/
//        
//        //PROCLIST
//        Element procListElement = new Element("PROCLIST");
//        
//        List<Map<String, Object>> processOperationData = ExtendedObjectProxy.getProcessedOperationService().getProcessedOperationList(productName);
//        
//        if(processOperationData != null &&
//                processOperationData.size() > 0)
//        {        
//            String elementFactoryName = StringUtil.EMPTY;
//            String elementProductSpecName = StringUtil.EMPTY;
//            String elementProcessFlowName = StringUtil.EMPTY;
//            String elementProcessOperationName = StringUtil.EMPTY;
//            String elementMachineName = StringUtil.EMPTY;
//            String elementMachineRecipeName = StringUtil.EMPTY;
//            String elementUnitNameList = StringUtil.EMPTY;
//            String elementUnitRecipeList = StringUtil.EMPTY;
//            String elementUnitTypeName = StringUtil.EMPTY;
//            String elementLotName = StringUtil.EMPTY;
//            String elementEventdTimeKey = StringUtil.EMPTY;            
//                        
//            for(int i = 0; i < processOperationData.size(); i++ )
//            {                  
//                elementFactoryName = processOperationData.get(i).get("FACTORYNAME").toString().trim();
//                elementProductSpecName = processOperationData.get(i).get("PRODUCTSPECNAME").toString().trim();
//                elementProcessFlowName = processOperationData.get(i).get("PROCESSFLOWNAME").toString().trim();
//                elementProcessOperationName = processOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim();
//                elementMachineName = processOperationData.get(i).get("PROCESSMACHINENAME").toString().trim();
//                elementMachineRecipeName = processOperationData.get(i).get("MACHINERECIPENAME").toString().trim();
//                elementUnitNameList = processOperationData.get(i).get("UNITNAMELIST").toString().trim();
//                elementUnitRecipeList = processOperationData.get(i).get("UNITRECIPELIST").toString().trim();
//                elementLotName = processOperationData.get(i).get("LOTNAME").toString().trim();
//                elementEventdTimeKey = processOperationData.get(i).get("EVENTTIMEKEY").toString().trim();
//                                
//                Element procElement = new Element("PROC");
//                
//                Element factoryNameElement = new Element("FACTORYNAME");
//                factoryNameElement.setText(elementFactoryName);
//                procElement.addContent(factoryNameElement);
//                
//                Element productSpecNameElement = new Element("PRODUCTSPECNAME");
//                productSpecNameElement.setText(elementProductSpecName);
//                procElement.addContent(productSpecNameElement);
//                
//                Element processFlowNameElement = new Element("PROCESSFLOWNAME");
//                processFlowNameElement.setText(elementProcessFlowName);
//                procElement.addContent(processFlowNameElement);
//                
//                Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
//                processOperationNameElement.setText(elementProcessOperationName);
//                procElement.addContent(processOperationNameElement);
//                
//                Element machineNameElement = new Element("MACHINENAME");
//                machineNameElement.setText(elementMachineName);
//                procElement.addContent(machineNameElement);
//                
//                Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
//                machineRecipeNameElement.setText(elementMachineRecipeName);
//                procElement.addContent(machineRecipeNameElement);
//                
//                procElement.addContent(setProcUnitList(elementUnitNameList));
//                
//                procElement.addContent(setUnitRecipeList(elementUnitRecipeList)); 
//                
//                Element lotNameElement = new Element("LOTNAME");
//                lotNameElement.setText(elementLotName);
//                procElement.addContent(lotNameElement);        
//                
//                Element eventTimeKeyElement = new Element("EVENTTIMEKEY");
//                eventTimeKeyElement.setText(elementEventdTimeKey);
//                procElement.addContent(eventTimeKeyElement);
//                
//                procListElement.addContent(procElement);
//            }
//        }
//        bodyElement.addContent(procListElement);        
//        
//        return bodyElement;
//    }
    
//    private Element generateSpcProductProcessDataProcList(EventInfo eventInfo, Element bodyElement, String factoryName, 
//            String productSpecName, String processFlowName,  String processOperationName, String lotName, String machineName, 
//            String unitName, String machineRecipeName) throws CustomException
//    {
//     
//        //Element bodyElement = SMessageUtil.getBodyElement(doc);
//        
//        /*FACTORYNAME
//        PRODUCTSPECNAME
//        PROCESSFLOWNAME
//        PROCESSOPERATIONNAME
//        MACHINENAME
//        MACHINERECIPENAME
//        PROCUNITLIST
//        PROCUNITTYPE
//        UNITRECIPELIST
//        LOTNAME
//        EVENTTIMEKEY*/
//        
//        //PROCLIST
//        Element procListElement = new Element("PROCLIST");
//        Element procElement = new Element("PROC");
//        
//        Element factoryNameElement = new Element("FACTORYNAME");
//        factoryNameElement.setText(factoryName);
//        procElement.addContent(factoryNameElement);
//        
//        Element productSpecNameElement = new Element("PRODUCTSPECNAME");
//        productSpecNameElement.setText(productSpecName);
//        procElement.addContent(productSpecNameElement);
//        
//        Element processFlowNameElement = new Element("PROCESSFLOWNAME");
//        processFlowNameElement.setText(processFlowName);
//        procElement.addContent(processFlowNameElement);
//        
//        Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
//        processOperationNameElement.setText(processOperationName);
//        procElement.addContent(processOperationNameElement);
//        
//        Element machineNameElement = new Element("MACHINENAME");
//        machineNameElement.setText(machineName);
//        procElement.addContent(machineNameElement);
//        
//        Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
//        machineRecipeNameElement.setText(machineRecipeName);
//        procElement.addContent(machineRecipeNameElement);
//        
//        String procUnitType = StringUtil.EMPTY;
//        if(StringUtil.isNotEmpty(unitName))
//        {
//            MachineSpec unitSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
//            procUnitType = unitSpecData.getMachineType();
//        }        
//        
//        Element procUnitListElement = new Element("PROCUNITLIST");
//        procUnitListElement.setText(unitName);
//        procElement.addContent(procUnitListElement);
//        
//        Element procUnitTypeElement = new Element("PROCUNITTYPE");
//        procUnitTypeElement.setText(procUnitType);
//        procElement.addContent(procUnitTypeElement);
//        
//        Element unitRecipeListElement = new Element("UNITRECIPELIST");
//        unitRecipeListElement.setText(StringUtil.EMPTY);
//        procElement.addContent(unitRecipeListElement);
//        
//        Element lotNameElement = new Element("LOTNAME");
//        lotNameElement.setText(lotName);
//        procElement.addContent(lotNameElement);        
//        
//        Element eventTimeKeyElement = new Element("EVENTTIMEKEY");
//        eventTimeKeyElement.setText(eventInfo.getEventTimeKey());
//        procElement.addContent(eventTimeKeyElement);
//        
//        procListElement.addContent(procElement);        
//        bodyElement.addContent(procListElement);
//        
//        Element elementTimeStamp = new Element("TIMESTAMP");
//        elementTimeStamp.setText(eventInfo.getEventTime().toString());
//        bodyElement.addContent(elementTimeStamp);
//        
//        return bodyElement;
//    }
    
//    /**
//     * 
//     * @Name     setProcUnitList
//     * @since    2018. 10. 9.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param elementUnitNameList
//     * @return
//     * @throws CustomException
//     */
//    private Element setProcUnitList(String elementUnitNameList) throws CustomException
//    {
//        String procUnitType = StringUtil.EMPTY;
//        Element procUnitListElement = new Element("PROCUNITLIST");
//        if(StringUtil.isNotEmpty(elementUnitNameList))
//        {
//            //procUnitType = MESMachineServiceProxy.getMachineServiceUtil().getMachineTypeList(elementUnitNameList);                    
//            //Element procUnitListElement = new Element("PROCUNITLIST");
//            //procUnitListElement.setText(elementUnitNameList);
//            //procElement.addContent(procUnitListElement);
//            
//            //Element procUnitTypeElement = new Element("PROCUNITTYPE");
//            //procUnitTypeElement.setText(procUnitType);
//            //procElement.addContent(procUnitTypeElement);
//            
//            String[] splitUnitName = StringUtil.split(elementUnitNameList , "|");
//            String[] splitUnitType = StringUtil.split(procUnitType , "|");
//            MachineSpec machineSpecData = null;
//            for(int j = 0; j < splitUnitName.length; j++ )
//            {
//                
//                Element procUnitElement = new Element("PROCUNIT");
//                
//                Element unitNameElement = new Element("UNITNAME");
//                unitNameElement.setText(splitUnitName[j]);
//                procUnitElement.addContent(unitNameElement);
//                
//                procUnitType = StringUtil.EMPTY; 
//                try
//                {
//                    //procUnitType = MESMachineServiceProxy.getMachineServiceUtil().getMachineTypeList(splitUnitName[j]);
//                    machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(splitUnitName[j]);
//                    procUnitType = machineSpecData.getMachineType();  
//                }
//                catch(Exception ex)
//                {                    
//                }                
//                  
//                Element unitTypeElement = new Element("UNITTYPE");
//                unitTypeElement.setText(procUnitType);
//                //if(j <= splitUnitType.length - 1)
//                //{                            
//                //    unitTypeElement.setText(splitUnitType[j]);                           
//                //}
//                //else
//                //{
//                //    unitTypeElement.setText(StringUtil.EMPTY);
//                //}
//                procUnitElement.addContent(unitTypeElement);
//                procUnitListElement.addContent(procUnitElement);
//            }                             
//        }
//        else
//        {                 
//            //Element procUnitListElement = new Element("PROCUNITLIST");
//            //procUnitListElement.setText(StringUtil.EMPTY);
//            //procElement.addContent(procUnitListElement);
//            
//            //Element procUnitTypeElement = new Element("PROCUNITTYPE");
//            //procUnitTypeElement.setText(StringUtil.EMPTY);
//            //procElement.addContent(procUnitTypeElement);
//                        
//            procUnitListElement.setText(StringUtil.EMPTY);        }
//        
//        return procUnitListElement;        
//    }
//    
//    /**
//     * 
//     * @Name     setUnitRecipeList
//     * @since    2018. 10. 9.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param elementUnitRecipeList
//     * @return
//     */
//    private Element setUnitRecipeList(String elementUnitRecipeList)
//    {
//        Element UnitRecipeListElement = new Element("UNITRECIPELIST");
//        if(StringUtil.isNotEmpty(elementUnitRecipeList))
//        {
//            //Element unitRecipeListElement = new Element("UNITRECIPELIST");
//            //unitRecipeListElement.setText(elementUnitRecipeList);
//            //procElement.addContent(unitRecipeListElement);
//            
//            String[] splitUnitRecipeName = StringUtil.split(elementUnitRecipeList, "|");
//                        
//            for(int j = 0; j < splitUnitRecipeName.length; j++ )
//            {
//                Element UnitRecipeElement = new Element("UNITRECIPE");
//                
//                Element unitRecipeNameElement = new Element("UNITRECIPENAME");
//                unitRecipeNameElement.setText(splitUnitRecipeName[j]);
//                
//                UnitRecipeElement.addContent(unitRecipeNameElement);    
//                
//                UnitRecipeListElement.addContent(UnitRecipeElement);
//            }                          
//        }
//        else
//        {
//            UnitRecipeListElement.setText(StringUtil.EMPTY);            
//        }
//        
//        return UnitRecipeListElement;
//    }
}
