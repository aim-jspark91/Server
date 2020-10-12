package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class StartCSTInfoCheckRequest extends SyncHandler 
{ 
    @Override 
    public Object doWorks(Document doc) throws CustomException 
    {
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_StartCSTInfoCheckReply");
        
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
        /* 20190110, hhlee, add validation ==>> */
        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
        /* <<== 20190110, hhlee, add validation */
        
        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        // Added by smkang on 2019.07.11 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
        try {
        	eventLog.debug("Lot will be locked to be prevented concurrent executing.");        	
        	LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
        	MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
        	eventLog.debug("Lot is locked to be prevented concurrent executing.");
        } catch (Exception e) {
        	eventLog.debug(e);
        }
        // ------------------------------------------------------------------------------------------------------------------------------------------------------
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
        
        /* 20190514, hhlee, add, BC MachineRecipeName Validation ==>> */
        String receivedMachineRecipeName  = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        /* <<== 20190514, hhlee, add, BC MachineRecipeName Validation */
        
        SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);    
        SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
         
        /* 20181107, hhlee, add, try{}catch{} ==>> */     
        Machine machineData = null;
        MachineSpec machineSpecData = null;
        Port portData = null;
        Durable durableData = null;
        Lot lotData = null;
        //start add by jhying on20200303 mantis:5435,PUT NOTE RECIPE
        lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
        String note = String.format("EQP Report RecipeID:[%s]", receivedMachineRecipeName);
		lotData.getUdfs().put("NOTE", note);
		LotServiceProxy.getLotService().update(lotData);
		//end add by jhying on20200303 mantis:5435,PUT NOTE RECIPE
        try
        {        
            //existence validation
            machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
            portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);                    
            durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
            
            /* 20190401, hhlee, add, update TransferState = 'Processing' ==>> */
            if(!StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "PORTNAME") , portName))
            {
                /* 20190422, hhlee, modify, ErrorCode Description change ==>> */
                //throw new CustomException("CST-9004", carrierName, CommonUtil.getValue(durableData.getUdfs(), "PORTNAME"), portName);
                throw new CustomException("CST-9004", carrierName, portName);
                /* <<== 20190422, hhlee, modify, ErrorCode Description change */
            }
            /* <<== 20190401, hhlee, add, update TransferState = 'Processing' */
        }
        catch (CustomException ce)
        {
            /* 20180616, hhlee, Modify ==>> */
            eventInfo.setEventName("Hold");
            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
            /* 20190424, hhlee, modify, changed function ==>> */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL",""); 
            /* 20190426, hhlee, modify, add variable(setFutureHold) */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
            try {
            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
            	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
            	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception e) {
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}
            
            /* <<== 20190424, hhlee, modify, changed function */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
            return doc;
            /* <<== 20180616, hhlee, Modify */            
        }
        /* <<== 20181107, hhlee, add, try{}catch{} */
    
        if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO")) 
        {
            ////ProbeCard
            //if(StringUtil.equals(machineName, "A2TST010"))
            //  probeCardCheck(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
            
            /* 20190114, hhlee, modify , Change Validation Logic(Mantis:0002402) ==>> */
            //List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);  
            List<Lot> lotList = null; 
            /* <<== 20190114, hhlee, modify , Change Validation Logic(Mantis:0002402) */
            
            /* 20180616, hhlee, Modify ==>> */
            try
            {
                /* 20190110, hhlee, add validation ==>> */
                 lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                /* <<== 20190110, hhlee, add validation */
                
                /* 20190114, hhlee, modify , Change Validation Logic(Mantis:0002402) ==>> */
                lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);  
                
                if(!StringUtil.equals(lotData.getCarrierName(), carrierName))
                {
                    throw new CustomException("LOT-9044", lotData.getCarrierName(), lotName, carrierName);
                }                
                /* <<== 20190114, hhlee, modify , Change Validation Logic(Mantis:0002402) */
                
                
                if (lotList.get(0) == null) 
                {
                    //throw new CustomException("LOT-0054", carrierName);
                    /* 20180616, hhlee, Modify ==>> */
                    throw new CustomException("LOT-0054", carrierName);
                    //eventInfo.setEventName("Hold");
                    //eventInfo.setEventComment(String.format("No Lot in Carrier[%s]", carrierName));
                    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "LOT-0054");
                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("No Lot in Carrier[%s]", carrierName));
                    //return doc;
                    /* <<== 20180616, hhlee, Modify */
                }                
                
                /* 20181220, hhlee, add, LotGrade Validation ==>> */
                if(StringUtil.equals(lotList.get(0).getLotGrade() ,GenericServiceProxy.getConstantMap().LotGrade_S))
                {
                    throw new CustomException("LOT-9043", carrierName, lotList.get(0).getKey().getLotName());
                }
                /* <<== 20181220, hhlee, add, LotGrade Validation */
                
                //DurableSpec durableSpecData
                //check Durable Hold State
                CommonValidation.CheckDurableHoldState(durableData);
        
                //check MachineState
                CommonValidation.checkMachineState(machineData);
        
                //check MachineOperationMode
                CommonValidation.checkMachineOperationModeExistence(machineData);
                
                /* 20181020, hhlee, add, Validate ProdutList ==>> */
                //available Product list
                try
                {
                    List<Product> productDataList = LotServiceProxy.getLotService().allUnScrappedProducts(lotList.get(0).getKey().getLotName());
                }
                catch (Exception ex)
                {
                    throw new CustomException("SYS-9999", "Product", "No Product to process");
                }            
                /* <<== 20181020, hhlee, add, Validate ProdutList */
                
            }
            catch (CustomException ce)
            {                
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                if(StringUtil.equals(ce.errorDef.getErrorCode(), "CST-0005") || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0008")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0011") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-0054")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9043")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9000") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-0048")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9044"))
                {
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                    //String reasonCodeType, String reasonCode, String eventComment, String errorComment
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
                		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    /* <<== 20190424, hhlee, modify, changed function */
                }
                else
                {
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
                		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    /* <<== 20190424, hhlee, modify, changed function */
                }                
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                return doc;
            }
            /* <<== 20180616, hhlee, Modify */
                    
            /* 20180616, hhlee, Modify ==>> */
            String machineRecipeName = StringUtil.EMPTY;
            ProcessFlow processFlowData = null;
            try
            {
                /* 20180930, Add, Operation Validation ==>> */
                //String machineGroup = machineData.getMachineGroupName();
                ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName());
                String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
                String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup);
                //if(!StringUtil.equals(machineGroup, operationMachineGroup))
                /* <<== 20180930, Add, Operation Validation */
                
                //check LotState/ProcessState/HoldLotState
                CommonValidation.checkLotState(lotList.get(0));
                CommonValidation.checkLotProcessState(lotList.get(0));
                CommonValidation.checkLotHoldState(lotList.get(0));   
                
                try
                {
                    /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
                    //ProcessFlow Validation
                    ProcessFlowKey processFlowKey = new ProcessFlowKey();
                    processFlowKey.setFactoryName(lotList.get(0).getFactoryName());
                    processFlowKey.setProcessFlowName(lotList.get(0).getProcessFlowName());
                    processFlowKey.setProcessFlowVersion(lotList.get(0).getProcessFlowVersion());
                    processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
                }
                catch (Exception ex)
                {
                    throw new CustomException("SYS-9999", "Product", "No ProcessFlowData to process");
                }
                
                //20180504, kyjung, QTime
                MESProductServiceProxy.getProductServiceImpl().checkQTime(lotList.get(0).getKey().getLotName());
    
                //20180525, kyjung, MQC
                MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotList.get(0).getKey().getLotName());
                
                /* 20181025, hhlee, add, Check MQC Machine ==>> */
                if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
                {
                    if(StringUtil.isEmpty(MESProductServiceProxy.getProductServiceImpl().checkMQCMachine(lotList.get(0), machineName)))
                    {
                        throw new CustomException("MQC-0049", machineName, StringUtil.EMPTY);
                    }
                }                
                /* <<== 20181025, hhlee, add, Check MQC Machine */
                
//                /* 20181013, hhlee, Modify ==>> */
//                //Recipe
//                //String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
//                //        lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));    
//                //String machineRecipeName = StringUtil.EMPTY;
//                if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
//                {
//                    //checkOperationMode
//                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE")); 
//                    
//                    if(StringUtil.isEmpty(CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME")))
//                    {
//                        throw new CustomException("PORT-9006",machineName );
//                    }
//                    
//                    String unitName = CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME");
//                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
//                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //if(toMachineAndOperationName.size() > 0)
//                    //{
//                    //    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
//                    //}                    
//                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
//                    
//                    String toMachineName = StringUtil.EMPTY;        
//                    
//                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
//                    //String toMachineName = MESLotServiceProxy.getLotServiceUtil().getToMachineName(lotData.getProcessOperationName(),CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //Only INDP has toMachineName
//                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationNameByUnitName(lotList, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    if(toMachineAndOperationName.size() > 0)
//                    {
//                        toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
//                    }
//                    /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//                    //PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
//                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
//                    
//                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                    //Recipe
//                    //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                    //        lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
//                            lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), toMachineName, lotList.get(0).getUdfs().get("ECCODE"));                    
//                    /* <<== 20181011, hhlee, Delete (Move Postion) */
//                }
//                else
//                {
//                    /* 20181024, hhlee, add, Validate Normal Mode Condition ==>> */
//                    //checkOperationMode
//                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20181024, hhlee, add, Validate Normal Mode Condition */
//                    
//                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
//                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, StringUtil.EMPTY, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
//                    
//                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
//                    /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//                    PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
//                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
//                    
//                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                    //Recipe
//                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
//                            lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));
//                    /* <<== 20181011, hhlee, Delete (Move Postion) */
//                }
//                /* <<== 20181013, hhlee, Modify */
                
                /* 20181101, hhlee, [toMachineName(Changed TOPolicy)] ==>> */
                /* PRD 적용 시 위의 IF 문을 막고 아래의 IF문의 주석을 풀고 적용함.*/
                /* 20181013, hhlee, Modify ==>> */
                ////Recipe
                //String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                //        lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));    
                //String machineRecipeName = StringUtil.EMPTY;
                
                /* 20190104, hhlee, add ==> */
                String toMachineName = StringUtil.EMPTY;
                /* <<== 20190104, hhlee, add */
                if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
                {
                    //checkOperationMode
                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE")); 
                    
                    if(StringUtil.isEmpty(CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME")))
                    {
                        throw new CustomException("PORT-9006",machineName );
                    }
                    
                    String unitName = CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME");
                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    //if(toMachineAndOperationName.size() > 0)
                    //{
                    //    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
                    //}                    
                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
                    
                    toMachineName = unitName;        
                    
                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
                    ////String toMachineName = MESLotServiceProxy.getLotServiceUtil().getToMachineName(lotData.getProcessOperationName(),CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    ////Only INDP has toMachineName
                    ////List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationNameByUnitName(lotList, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    //if(toMachineAndOperationName.size() > 0)
                    //{
                    //    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
                    //}
                    ///* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
                    ////PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    //PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    ///* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
                    
                    /* 20181101, hhlee, add, Change TPEFOMPolicy Condition(not use unitname) ==>> */
                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
                    /* 20190520, hhlee, modify, added machine communicationState Validate ==>> */
                    /* 20190524, hhlee, delete, added machine communicationState Validate ==>> */
                    //if(StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
                    //{      
                    //Recipe
                    
                    // start modify by jhiying on20191123 mantis:5449
                   /* machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                              lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE")); */
                    
                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getINDPMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                    		lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"),portName);
                    // END modify by jhiying on20191123 mantis:5449
                    //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                    //        lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), toMachineName, lotList.get(0).getUdfs().get("ECCODE"));                    
                    //}
                    /* <<== 20190524, hhlee, delete, added machine communicationState Validate */
                    /* <<== 20190520, hhlee, modify, added machine communicationState Validate */
                    /* <<== 20181011, hhlee, Delete (Move Postion) */
                    /* <<== 20181101, hhlee, add, Change TPEFOMPolicy Condition(not use unitname) */
                }
                else
                {
                    /* 20181024, hhlee, add, Validate Normal Mode Condition ==>> */
                    //checkOperationMode
                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    /* <<== 20181024, hhlee, add, Validate Normal Mode Condition */
                    
                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, StringUtil.EMPTY, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
                    
                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
                    ///* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
                    //PolicyUtil.checkMachineOperationByTOPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
                    ///* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
                    
                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
                    /* 20190520, hhlee, modify, added machine communicationState Validate ==>> */
                    /* 20190524, hhlee, delete, added machine communicationState Validate ==>> */
                    //if(StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
                    //{      
                    //Recipe
                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                            lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));
                    /* <<== 20181011, hhlee, Delete (Move Postion) */
                    //}
                    /* <<== 20190524, hhlee, delete, added machine communicationState Validate */
                    /* <<== 20190520, hhlee, modify, added machine communicationState Validate */
                }
                ///* <<== 20181013, hhlee, Modify */
                /* PRD 적용 시 여기까지의 IF문의 주석을 풀고 적용함.*/
                /* <<== 20181101, hhlee, [toMachineName(Changed TOPolicy)] */
                
                /* 20190514, hhlee, add, EAP Send Recipe Validation ==>> */
                /* 20190520, hhlee, modify, added machine communicationState Validate ==>> */
                if(StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineLocal))
                {    
                	//MESRecipeServiceProxy.getRecipeServiceUtil().validateReceivedMachineRecipeFromMachine(eventInfo, machineSpecData, machineName, machineRecipeName, receivedMachineRecipeName);
                    MESRecipeServiceProxy.getRecipeServiceUtil().validateReceivedMachineRecipeByOnLineLocal(eventInfo, machineSpecData, machineName, receivedMachineRecipeName);
                    /* 20190522, hhlee, add */
                    /* 20190524, hhlee, delete, added machine communicationState Validate */
                    //machineRecipeName = receivedMachineRecipeName;
                    
                	//start modify by jhiiying on20200228 maintis :5435
                    String localRunExceptionControlFlag = MESLotServiceProxy.getLotServiceUtil().getLocalRunExceptionflag("LocalRunExceptionSwitch", "LocalRunExceptionControl") ;
                    if(localRunExceptionControlFlag.equals("Y"))
                    {
                	  LocalRunException  localRunExceptionData = new LocalRunException(lotName,lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(),machineName);
                	
                	    if(StringUtil.equals(lotList.get(0).getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_PROD) &&
                			(StringUtil.equals(CommonUtil.getWorkOrderType(lotData),"ME")
            				|| StringUtil.equals(CommonUtil.getWorkOrderType(lotData),"MP")))
                	   {
                		MESLotServiceProxy.getLotServiceImpl().checkLocalRunExceptionCondition(lotData,  machineName , toMachineName, receivedMachineRecipeName,eventInfo);
                		
                	    }

                    }
                    
                  //end modify by jhiiying on20200228 maintis :5435
                }
                /* <<== 20190520, hhlee, modify, added machine communicationState Validate */
                /* <<== 20190514, hhlee, add, EAP Send Recipe Validation */
                
                /* 20181025, hhlee, delete, ProcessFlow Validation ==>> */
                ///* 20181020, hhlee, add, ProcessFlow Validation ==>> */
                ////ProcessFlow Validation
                //ProcessFlowKey processFlowKey = new ProcessFlowKey();
                //processFlowKey.setFactoryName(lotList.get(0).getFactoryName());
                //processFlowKey.setProcessFlowName(lotList.get(0).getProcessFlowName());
                //processFlowKey.setProcessFlowVersion(lotList.get(0).getProcessFlowVersion());
                //ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
                ///* <<== 20181020, hhlee, add, ProcessFlow Validation */
                /* <<== 20181025, hhlee, delete, ProcessFlow Validation */
                
                // 20180612, kyjung, Recipe Idle Time
                //MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(machineName, machineRecipeName, lotList.get(0).getProductSpecName(), lotList.get(0).getProcessOperationName());
                
                /* 20190412, hhlee, delete, MachineIdleTimeOver validation logic(Mantis:0003497) ==>> */
                ///* 20190115, hhlee, add, add logic Check Machine Idle Time ==>> */
                /* 20190717, park Jeong Su  Add Logic(Mantis:0004367)*/
                MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOver(machineName, lotList.get(0), portName);
                /* 20190717, park Jeong Su  Add Logic(Mantis:0004367)*/
                ///* <<== 20190115, hhlee, add, add logic Check Machine Idle Time */
                /* <<== 20190412, hhlee, delete, MachineIdleTimeOver validation logic(Mantis:0003497) */
                
        		//2018.11.17_hsryu_check FirstLotFlag.
                //deleted by wghuang 20190121, requested by haiying
        		//MESLotServiceProxy.getLotServiceUtil().checkFirstLotFlagByRecipeIdleTime(machineName, machineRecipeName);

                /* 20181001, hhlee, modify, location move, lotdata change ==>> */
                //Added by jjyoo on 2018.9.20 - Check Inhibit Condition
                //this logic must be existed the end of LotInfoDownloadRequestNew.
                /* 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName ==>> */
        		//MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName);
        		MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  
                        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, toMachineName);
                //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
                //        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), toMachineName);
                /* <<== 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName */
                /* <<== 20181001, hhlee, modify, location move, lotdata change */
            }
            catch (CustomException ce)
            {
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                
                /* 20190514, hhlee, delete ==>> */
                //if(StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-0016") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9015")
                //        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9027") || StringUtil.equals(ce.errorDef.getErrorCode(), "PROCESSOPERATION-9001")
                //        || StringUtil.equals(ce.errorDef.getErrorCode(), "INHIBIT-0005") || StringUtil.equals(ce.errorDef.getErrorCode(), "RECIPE-0012")
                //        || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0102") || StringUtil.equals(ce.errorDef.getErrorCode(), "MQC-0044")
                //        || StringUtil.equals(ce.errorDef.getErrorCode(), "QUEUE-0005"))
                //{
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                //    /* 20190424, hhlee, modify, changed function ==>> */
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                //    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
                //    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
                //    /* <<== 20190424, hhlee, modify, changed function */
                //}
                ///* 20190424, hhlee, delete, change function ==>> */
                /////* 20190220, hhlee, add, added futherHold(validation NG LotProcessState = 'RUN') */
                ////else if(StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9046"))
                ////{
                ////    MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotList.get(0), machineName, "AHOLD", "VRHL");
                ////}
                ///* <<== 20190424, hhlee, delete, change function */
                //else
                //{
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                //    /* 20190424, hhlee, modify, changed function ==>> */
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                //    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
                //    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
                //    /* <<== 20190424, hhlee, modify, changed function */
                //} 
                /* <<== 20190514, hhlee, delete */
                if(StringUtil.equals(ce.errorDef.getErrorCode(), "LocalRunExcp-0001")){
                	
                	try {
    					GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    					MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LREH","", true, true, "", machineSpecData.getUdfs().get("DEPARTMENT"));
    					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
    				} catch (Exception e) {
    					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    				}
                	
                }
                else if(StringUtil.equals(ce.errorDef.getErrorCode(), "LocalRunExcp-0002")){
                	
                	try {
    					GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    					MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LREH","", true, true, "", "INT");
    					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
    				} catch (Exception e) {
    					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    				}
                	
                }
                else
                {	
                  try {
                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				  } catch (Exception e) {
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				 }
                }
                
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                return doc;
            }
            /* <<== 20180616, hhlee, Modify */  
                         
            try
            {
                /* 20181016, hhlee, delete, not used ==>> */
                //String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
                //        lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));
                /* <<== 20181016, hhlee, delete, not used */
                
                //ProbeCard
                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
                {
                    eventLog.info("ProbeCard check Started.");
    
                    String pbCardType = "";
    
                    if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
                    {
                        //get one PBCardType by mahcine
                         //pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(machineName).get(0).getDurableSpecName();
                         pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(machineName);
                         
                         /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
                         if(StringUtil.isEmpty(pbCardType))
                         {
                             throw new CustomException("PROBECARD-0008", machineName);
                         }                            
                         /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
                         
                         PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(lotList.get(0).getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
                    }
                    else
                    {
                        //INDP
                        List<String>unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(machineName);
    
                        for(String unit : unitList)
                        {
                        	eventLog.info("UnitName : ["+unit+"], Linked Unit Name ["+portData.getUdfs().get("LINKEDUNITNAME")+"]");//20190820 yu.cui
                            //pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(unit).get(0).getDurableSpecName();
                        	
                        	if(StringUtil.equals(unit, portData.getUdfs().get("LINKEDUNITNAME"))) //20190820 yu.cui
                        	{
                        		pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit);
                        		                            /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
	                            if(StringUtil.isEmpty(pbCardType))
	                            {
	                                throw new CustomException("PROBECARD-0008", machineName);
	                            }                            
	                            /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
	                            
	                            PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(lotList.get(0).getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
                        	}
                        }
                    }
    
                    eventLog.info("ProbeCard check Ended.");
                }
    
                /* 20190411, hhlee, delete duplicate Inquery ==>> */
                List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotList.get(0).getKey().getLotName());
                
                //PhotoMask
                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK"))
                {
                    eventLog.info("PhotoMask check Started.");
    
                    //also check mask state
                    List<Durable> PhotoMaskList = MESDurableServiceProxy.getDurableServiceUtil().getPhotoMaskNameByMachineName(machineName);
    
                    if(PhotoMaskList.size() <= 0)
                    {
                        throw new CustomException("MASK-0098", machineName);
                        
                        //eventInfo.setEventName("Hold");
                        //eventInfo.setEventComment(String.format("There is no mounted PhotoMask in this machine[%s]", machineName));
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MASK-0098");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("There is no mounted PhotoMask in this machine[%s]", machineName));
                        //return doc;
                    }
                            			
        			/* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
                    //PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(lotList.get(0).getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
                    String photoMaskName = PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, CommonUtil.getValue(lotList.get(0).getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
        			eventLog.info("PhotoMask check Ended .");
                    
        			/* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
                    if(!StringUtils.equals(photoMaskName, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME))
                    {
                        if(StringUtil.equals(machineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
                        {
                            /* 20190524, hhlee, add, check validation OnlineLocal Mode PhotoMask ==>> */
                            if(StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineLocal))
                            {
                                machineRecipeName = receivedMachineRecipeName;
                            }
                            /* <<== 20190524, hhlee, add, check validation OnlineLocal Mode PhotoMask */
                            
                            MESRecipeServiceProxy.getRecipeServiceUtil().validateRecipeParameterPhotoMaskName(eventInfo, machineName, machineRecipeName, photoMaskName);
                        }
                    }
                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
        			
                    //check Product ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.
        			eventLog.info("Check ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.");
        			
        			/* 20190411, hhlee, delete duplicate Inquery */
        			//List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotList.get(0).getKey().getLotName());
        			
        			List<ProductFlag> productFlagList = new ArrayList<ProductFlag>();
        			
        			for(Product productData : productList)
        			{
                        String condition = "WHERE productName = ? ";
                        Object[] bindSet = new Object[]{ productData.getKey().getProductName()};
                        
                        try
                        {
                			productFlagList = ExtendedObjectProxy.getProductFlagService().select(condition, bindSet);
                        }
                        catch(Throwable e)
                        {
                        	eventLog.error("Product [" + productData.getKey().getProductName() + " [ProductFlag is not exist");
                        }
                        
                        if(productFlagList.size()>0)
                        {
                        	for(ProductFlag productFlag : productFlagList)
                        	{
                            	if(StringUtils.equals(productFlag.getTurnOverFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                            	{
                                    throw new CustomException("PRODUCT-0034", productData.getKey().getProductName());
                            	}
                            	
                            	if(StringUtils.equals(productFlag.getProcessTurnFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                            	{
                                    throw new CustomException("PRODUCT-0035", productData.getKey().getProductName());
                            	}
                        	}
                        }
        			}
        			eventLog.info("PhotoMask check Ended .");
                }
                
                /* 20181019, hhlee, delete, LotInfoDownLoadRequest Sync ==>> */
                //this.checkProcessOperation(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(), lotList.get(0).getUdfs().get("ECCODE"), 
                //                                                     lotList.get(0).getProcessFlowName(),lotList.get(0).getProcessOperationName(), machineName);  
                /* <<== 20181019, hhlee, delete, LotInfoDownLoadRequest Sync */
                
                /* 20190104, hhlee, delete */
                ////Added by jjyoo on 2018.9.20 - Check Inhibit Condition
                //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName);
                
                // PHOTO TRCKFLAG MANAGEMENT            
                boolean checkTrackFlag = false;
                
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                String productGradeNList = StringUtil.EMPTY;
                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                
                /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added ==>> */
                String compareNodeStack = StringUtil.EMPTY;        
                String compareLotName = StringUtil.EMPTY;
                /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added */
                
                /* 20190411, hhlee, delete duplicate Inquery */
                //List<Product> sProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotList.get(0).getKey().getLotName());
                //for (Product sProductInfo : sProductList) /* <<== 20190411, hhlee, delete duplicate Inquery */
                for (Product sProductInfo : productList) /* <<== 20190411, hhlee, delete duplicate Inquery */
                {
                    /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                    if(StringUtil.equals(sProductInfo.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_N))
                    {                        
                        productGradeNList += sProductInfo.getKey().getProductName() + ",";                        
                    }                    
                    /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                    
                    /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
                    if(!StringUtil.equals(lotList.get(0).getNodeStack(), sProductInfo.getNodeStack()))
                    {
                        if(!StringUtil.equals(compareLotName, lotList.get(0).getKey().getLotName()))
                        {
                            compareNodeStack += "LOTNAME: " + lotList.get(0).getKey().getLotName() + " - PRODUCTNAME: ";
                            compareLotName = lotList.get(0).getKey().getLotName();
                        }
                        compareNodeStack += sProductInfo.getKey().getProductName() + ",";
                    }
                    /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
                    
                    /*String trackFlag = sProductInfo.getUdfs().get("TRACKFLAG");
                    //if (StringUtil.equals(trackFlag, "N") || StringUtil.isEmpty(trackFlag))
                    if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                    {
                        checkTrackFlag = true;
                        break;
                    }
                    else
                    {
                        continue;
                    }*/
                	
                	//Modify 20181022 hsryu, TrackFlag column move to PRODUCT -> CT_PRODUCTFLAG
                	ProductFlag productFlag = new ProductFlag();
        			
        			try
        			{
        				productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {sProductInfo.getKey().getProductName()});
        			}
        			catch(Throwable e)
        			{
        				eventLog.error(sProductInfo.getKey().getProductName() + " is not exist ProductFlag Info.");
        			}
        			
        			if(productFlag!=null)
        			{
        			    /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
        			    //String trackFlag = productFlag.getTrackFlag();
        				//if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
        				//{
        				//	checkTrackFlag = true;
        				//	break;
        				//}
        				//else
        				//{
        				//	continue;
        				//}
        			    String trackFlag = productFlag.getTrackFlag();
                        if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                        {
                            checkTrackFlag = true;                           
                        }
                        /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
        			}
                }
                
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                if(StringUtil.isNotEmpty(productGradeNList))
                {
                    productGradeNList = StringUtil.substring(productGradeNList, 0, productGradeNList.length() -1);
                    throw new CustomException("PRODUCT-9008", productGradeNList);
                }
                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                
                /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
                if(StringUtil.isNotEmpty(compareNodeStack))
                {
                    compareNodeStack = StringUtil.substring(compareNodeStack, 0, compareNodeStack.length() -1);
                    throw new CustomException("LOT-9047", compareNodeStack);
                }
                /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
                
                //String trackFlag = lotData.getUdfs().get("TRACKFLAG");
                //if (StringUtil.equals(trackFlag, "N") || StringUtil.isEmpty(trackFlag))
                //{
                //    checkTrackFlag = false;
                //}
                
                if ( checkTrackFlag == true )
                {
                    /* 20180714, Add, TrackFlag Validation ==>> */
                    if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH) || 
                        StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
                    {
                        if ( StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
                                StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) )
                        {
                            // Nothing : Normal
                        }
                        else
                        {
                            throw new CustomException("MACHINE-1001", machineData.getKey().getMachineName());
                            /* 20180616, hhlee, Modify ==>> */
                            ////// Abnormal
                            ////throw new CustomException("MACHINE-1001", 
                            ////      machineData.getKey().getMachineName(), macSpecData.getUdfs().get("TRACKFLAG"), lotData.getUdfs().get("TRCKFLAG"));
                            //eventInfo.setEventName("Hold");
                            //eventInfo.setEventComment(String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                            //        machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                            ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                            ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MACHINE-1001");
                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                            //        machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                            //return doc;
                            /* <<== 20180616, hhlee, Modify */
                        }
                    }    
                    /* <<== 20180714, Add, TrackFlag Validation */
                }
                else
                {
                    //eventInfo.setEventComment(String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                    //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MACHINE-1001");
                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                    //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                    //return doc;
                }             
            }
            catch (CustomException ce)
            {
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return(PRODUCT-9008) */
                /* 20190411, hhlee, CompareNodeStack , Error Return(LOT-9047) */
                if(StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-9001") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0015") 
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(), "INHIBIT-0005")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0098") || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-1001")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0034") || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0035")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0102") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0001")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0026") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0027")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0016") || StringUtil.equals(ce.errorDef.getErrorCode(),"PRODUCT-9008")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9047") || StringUtil.equals(ce.errorDef.getErrorCode(),"PROBECARD-0008"))
                {
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
                		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    /* <<== 20190424, hhlee, modify, changed function */
                }
                else
                {
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
                		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    /* <<== 20190424, hhlee, modify, changed function */
                } 
                
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                return doc;
            }
        }

        // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
        //								   So after StartCSTInfoCheckRequest is succeeded, StartCheckResult is changed to Y.
        eventInfo.setBehaviorName("ARRAY");
        eventInfo.setEventName(SMessageUtil.getMessageName(doc));
        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
        
        SetEventInfo setEventInfo = new SetEventInfo();
        setEventInfo.getUdfs().put("STARTCHECKRESULT", "Y");
  
//        //2019.09.02 Add By Park Jeong Su Mantis 4691
//        setEventInfo.getUdfs().put("FIRSTCHECKRESULT", "N");
        LotServiceProxy.getLotService().setEvent(new LotKey(lotName), eventInfo, setEventInfo);
        // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
       //start add by jhying on20200303 mantis:5435
        Map<String, String> updateUdfs = new HashMap<String, String>();
 		updateUdfs.put("NOTE", "");
 		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
 		  //end add by jhying on20200303 mantis:5435
        return doc;
    }
    
    private void checkProcessOperation(String factoryName, String productSpecName, String ecCode, String processFlowName, 
            String processOperationName, String machineName ) throws CustomException
    {
      //String strSql = "SELECT DISTINCT PROCESSOPERATIONNAME " +
        //      "  FROM TPEFOMPOLICY " +
        //      " WHERE     FACTORYNAME = :FACTORYNAME " +
        //      "       AND PRODUCTSPECNAME = :PRODUCTSPECNAME " +
        //      "       AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
        //      "       AND ECCODE = :ECCODE " +
        //      "       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
        //      "       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
        //      "       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +        
        //      "       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
        //      "       AND MACHINENAME = :MACHINENAME " ;

        String strSql= StringUtil.EMPTY;
        strSql = strSql + "  SELECT DISTINCT T.FACTORYNAME, T.PRODUCTSPECNAME, T.PRODUCTSPECVERSION, T.ECCODE, T.PROCESSFLOWNAME, \n";
        strSql = strSql + "         T.PROCESSFLOWVERSION, T.PROCESSOPERATIONNAME, T.PROCESSOPERATIONVERSION, T.MACHINENAME        \n";
        strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
        strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                           \n";
        strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
        strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*'))                                     \n";
        strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = '*'))                         \n";
        strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = '*'))                \n";
        strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = '*'))                                                    \n";
        strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = '*'))                         \n";
        strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = '*'))                \n";
        strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = '*'))          \n";
        strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = '*')) \n";
        strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = '*'))                                     \n";
        strSql = strSql + " ORDER BY DECODE (T.FACTORYNAME, '*', 9999, 0),                                                        \n";
        strSql = strSql + "          DECODE (T.PRODUCTSPECNAME, '*', 9999, 0),                                                    \n";
        strSql = strSql + "          DECODE (T.PRODUCTSPECVERSION, '*', 9999, 0),                                                 \n";
        strSql = strSql + "          DECODE (T.ECCODE, '*', 9999, 0),                                                             \n";
        strSql = strSql + "          DECODE (T.PROCESSFLOWNAME, '*', 9999, 0),                                                    \n";
        strSql = strSql + "          DECODE (T.PROCESSFLOWVERSION, '*', 9999, 0),                                                 \n";
        strSql = strSql + "          DECODE (T.PROCESSOPERATIONNAME, '*', 9999, 0),                                               \n";
        strSql = strSql + "          DECODE (T.PROCESSOPERATIONVERSION, '*', 9999, 0),                                            \n";
        strSql = strSql + "          DECODE (T.MACHINENAME, '*', 9999, 0)                                                         \n";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", factoryName);
        bindMap.put("PRODUCTSPECNAME", productSpecName);
        bindMap.put("PRODUCTSPECVERSION", "00001");
        bindMap.put("ECCODE", ecCode);
        bindMap.put("PROCESSFLOWNAME", processFlowName);
        bindMap.put("PROCESSFLOWVERSION", "00001");
        bindMap.put("PROCESSOPERATIONNAME", processOperationName);
        bindMap.put("PROCESSOPERATIONVERSION", "00001");
        bindMap.put("MACHINENAME", machineName);
        
        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if ( tpefomPolicyData.size() < 0 )
        {
            throw new CustomException("POLICY-0001", "");
        }
    }
    
    /**
     * probeCardCheck
     * @author wghuang
     * @since 2018.05.08
     * @param machineName
     * @param operationMode
     * @return
     * @throws CustomException
     */
    private void probeCardCheck(String machineName, String operationMode) throws CustomException
    {
        int unitCount = 0 ;
        int pUnitCoutn = 0 ;
        
        if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
        {
            if(getUnitListByMachine(machineName).size()<=0)
                throw new CustomException("MACHINE-9001", machineName);
            
            unitCount = getUnitListByMachine(machineName).size();
            
            pUnitCoutn = getPUnitListByUnitList(getUnitListByMachine(machineName)).size();
            
            if(unitCount != pUnitCoutn)
                throw new CustomException("PROBECARD-0001", machineName);
            else
            {
                String probeCardType ="";
                for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
                {
                    String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");
                    
                    if(StringUtil.isEmpty(probeCardType))
                    {
                        probeCardType = tempprobeCardType;
                    }
                    else
                    {
                        if(!StringUtil.equals(probeCardType,tempprobeCardType))
                            throw new CustomException("PROBECARD-0002", machineName, probeCard.getKey().getDurableName(), probeCardType, probeCard.getKey().getDurableName(), tempprobeCardType);
                    }                   
                }
            }
        }
        else if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
        {
            if(pUnitCoutn < 0)
                throw new CustomException("PROBECARD-0005", machineName);
            else if(pUnitCoutn > unitCount)
                throw new CustomException("PROBECARD-0004", machineName);
            
            /*if(unitCount == pUnitCoutn)
            {
                String probeCardType ="";
                for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
                {
                    String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");
                    
                    if(StringUtil.isEmpty(probeCardType))
                    {
                        probeCardType = tempprobeCardType;
                    }
                    else
                    {
                        if(StringUtil.equals(probeCardType,tempprobeCardType))
                            throw new CustomException("PROBECARD-0003", machineName);
                    }                   
                }
            }   
            else if(unitCount < pUnitCoutn)
                throw new CustomException("PROBECARD-0004", machineName);*/
        }
    }
    
    private List<Machine> getUnitListByMachine(String machineName) throws CustomException
    {
        String condition = "WHERE SUPERMACHINENAME = ? AND MACHINETYPE = ?";
        
        Object[] bindSet = new Object[]{machineName,"ProductionMachine"};
        
        List<Machine>unitList ;
        try
        {
            unitList = MachineServiceProxy.getMachineService().select(condition, bindSet);
        }
        catch(NotFoundSignal ne)
        {
            unitList = new ArrayList<Machine>();
        }
        
        return unitList;
    }
    
    private List<Durable> getPUnitListByUnitList(List<Machine> machineList)
    {
        String unitlistName = "";
        List<Durable> probeCardList;
        
        for(Machine unit : machineList)
        {
            if(StringUtil.isEmpty(unitlistName))
                unitlistName = "'" + unit.getKey().getMachineName() + "'";
            
            unitlistName += ",'" + unit.getKey().getMachineName() + "'";
        }
        
        try
        {
            probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
        }
        catch(NotFoundSignal ne)
        {
            probeCardList = new ArrayList<Durable>();
        }
                
        return probeCardList;
    }
}