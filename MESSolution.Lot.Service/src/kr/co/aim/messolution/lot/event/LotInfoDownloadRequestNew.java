package kr.co.aim.messolution.lot.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ExposureFeedBack;
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
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

//import com.sun.xml.internal.ws.policy.jaxws.PolicyUtil;

public class LotInfoDownloadRequestNew extends SyncHandler {

    @Override
    public Object doWorks(Document doc) throws CustomException {
        /* 20181130, hhlee, modify, add MessageName dupulicate prevent ==>> */
        //Document doc = (Document) recvdoc.clone();
        /* <<== 20181130, hhlee, modify, add MessageName dupulicate prevent */
        
        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CSTInfoDownLoadSend");

        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
        String portType    = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
        String portUseType  = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
        String portAccessMode   = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
        String slotMap     = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

        /* 20181107, hhlee, add, try{}catch{} ==>> */     
        Machine machineData = null;
        MachineSpec machineSpecData = null;
        Port portData = null;
        Durable durableData = null;
        
        if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
        }
        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
        }
        else
        {
        }
        
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
        
        /* 20180616, hhlee, Modify ==>> */
        try
        {
            //DurableSpec durableSpecData
            //check Durable Hold State
            CommonValidation.CheckDurableHoldState(durableData);
    
            //check MachineState
            CommonValidation.checkMachineState(machineData);
    
            //check MachineOperationMode
            CommonValidation.checkMachineOperationModeExistence(machineData);
        }
        catch (CustomException ce)
        {            
            eventInfo.setEventName("Hold");
            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
            if(StringUtil.equals(ce.errorDef.getErrorCode(), "CST-0005") || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0008")
                    || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0011"))
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
        
        // Start 2019.09.19 Modify By Park Jeong Su Mantis 4813
        
        //cleaning EQP
        // Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
        // if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CCLN"))
        try {
           	if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
            {
                return downloadCarrierJob(doc, slotMap, durableData, machineData,portName,portType,portUseType, machineSpecData.getDefaultRecipeNameSpaceName());
            }
		} catch (CustomException ce) {
            /* 20180531, Add CST Hold Logic ==>> */
            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
            MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
            return doc;
            /* <<== 20180531, Add CST Hold Logic */             
		}
        
        // End 2019.09.19 Modify By Park Jeong Su Mantis 4813

        boolean isInitialInput = CommonUtil.isInitialInput(machineName);
        Lot lotData = null;

        //Machine UPK
        if(isInitialInput)
        {
            //Unloader
            if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
            {
                eventLog.info("Unpacker unloader Port job download");                
            }
            else
            {
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PORT-1001");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Port[%s] Type NotMismatch !" , portData.getKey().getPortName() + "-" + CommonUtil.getValue(portData.getUdfs(), "PORTTYPE")));
                return doc;
            }
        }
        
        /* 20190605, hhlee, add Sorter transfer direction validation ==>> */
        String transferDirection = StringUtil.EMPTY;
        /* <<== 20190605, hhlee, add Sorter transfer direction validation */
        
        /* 20190125, hhlee, change, PU Port and UNPACKER PU Port are treated with the same logic */
        //else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
        if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO")) 
        {
            //PB,PL Port
            
            /* 20180619, Move Position ==>> */
            //Body generate
            this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
                  durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
            /* <<== 20180619, Move Position */
            
            eventLog.info("Loader Port job download");          
            
            /* 20181011, hhlee, add, Valiable Location Change ==>> */
            String machineRecipeName = StringUtil.EMPTY;
            /* <<== 20181011, hhlee, add, Valiable Location Change */
            
            /* 20181020, hhlee, add, Validate ProdutList ==>> */
            List<Product> productDataList = null;
            ProcessFlow processFlowData = null;
            /* <<== 20181020, hhlee, add, Validate ProdutList */
            
            /* 20181107, hhlee, add, move change logic position ==>> */
            List<Lot> lotList = null;
            //slot map validation
            String logicalSlotMap = StringUtil.EMPTY;
            /* <<== 20181107, hhlee, add, move change logic position */
            
            /* 20180616, hhlee, Modify ==>> */
            try
            {
                /* 20181107, hhlee, add, move change logic position ==>> */
                //lotData = this.getLotData(carrierName);
                lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
                
                if (lotData == null)
                {
                    throw new CustomException("LOT-0054", carrierName);
                }
                /* <<== 20181107, hhlee, add, move change logic position */
                
                /* 20181220, hhlee, add, LotGrade Validation ==>> */
                if(StringUtil.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
                {
                    throw new CustomException("LOT-9043", carrierName, lotData.getKey().getLotName());
                }
                /* <<== 20181220, hhlee, add, LotGrade Validation */
                
                /* 20181031, Add, MachineGroup Validation Logic Move ==>> */
                //String machineGroup = machineData.getMachineGroupName();
                ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
                String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
                /* <<== 20181031, Add, MachineGroup Validation Logic Move */
                
                //check LotState/ProcessState/HoldLotState
                CommonValidation.checkLotState(lotData);
                CommonValidation.checkLotProcessState(lotData);
                CommonValidation.checkLotHoldState(lotData);
                
                /* 20181020, hhlee, add, Validate ProdutList ==>> */
                //available Product list
                try
                {
                    productDataList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
                }
                catch (Exception ex)
                {
                    throw new CustomException("SYS-9999", "Product", "No Product to process");
                }            
                /* <<== 20181020, hhlee, add, Validate ProdutList */
                
                try
                {
                    /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
                    //ProcessFlow Validation
                    ProcessFlowKey processFlowKey = new ProcessFlowKey();
                    processFlowKey.setFactoryName(lotData.getFactoryName());
                    processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
                    processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
                    processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
                }
                catch (Exception ex)
                {
                    throw new CustomException("SYS-9999", "Product", "No ProcessFlowData to process");
                }
                
                //20180504, kyjung, QTime
                MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());    
                
                //20180525, kyjung, MQC
                MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
                
                /* 20181025, hhlee, add, Check MQC Machine ==>> */
                if(StringUtils.equals(processFlowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
                {
                    if(StringUtil.isEmpty(MESProductServiceProxy.getProductServiceImpl().checkMQCMachine(lotData, machineName)))
                    {
                        throw new CustomException("MQC-0049", machineName, StringUtil.EMPTY);
                    }
                }               
                /* <<== 20181025, hhlee, add, Check MQC Machine */
                
                /* 20181107, hhlee, add, move change logic position ==>> */
                //getLotData
                lotList = CommonUtil.getLotListByCarrier(carrierName, true);
                //slot map validation
                logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
                /* <<== 20181107, hhlee, add, move change logic position */
            }
            catch (CustomException ce)
            {               
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                /* 20190424, hhlee, modify, add errorCode : "LOT-9046" */
                if(StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-0016") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9015")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-0054") || StringUtil.equals(ce.errorDef.getErrorCode(), "MQC-0044")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MQC-0049") || StringUtil.equals(ce.errorDef.getErrorCode(), "QUEUE-0005")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0102") || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PROCESSOPERATION-9001") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9027")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9043") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9046"))
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
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, "", " ");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
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
                //if (StringUtil.isNotEmpty(slotMap) && !slotMap.equals(logicalSlotMap))
                if (StringUtil.isNotEmpty(logicalSlotMap) || StringUtil.isNotEmpty(slotMap))
                {
                    if (!slotMap.equals(logicalSlotMap))
                    {       
                        throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
                        //eventInfo.setEventName("Hold");
                        //eventInfo.setEventComment("Different [BCSlotMap = " + slotMap + "] , [MESSlotMap = " + logicalSlotMap + "]");
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
                        ///* 20180601, Slot missmatch  ==>> */
                        ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-0020");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "Different [BCSlotMap = " + slotMap + "] , [MESSlotMap = " + logicalSlotMap + "]");
                        ///* <<== 20180601, Slot missmatch */
                        //return doc;
                    }
                }
            }
            catch (CustomException ce)
            {           
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                /* 20190424, hhlee, modify, changed function ==>> */
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
                /* 20190426, hhlee, modify, add variable(setFutureHold) */
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, "", "");
                try {
                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, true, "", "");
                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				} catch (Exception e) {
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				}
                
                /* <<== 20190424, hhlee, modify, changed function */
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                return doc;             
            }
            
                        
            /* 20181011, hhlee, Delete (Move Postion) ==>> */
            /* hhlee, 20180616, Added TOMACHINE specified logic. ==>> */
            String toMachineName = StringUtil.EMPTY;
            try
            {
                /* 20181101, hhlee, [toMachineName(Changed TOPolicy)] ==>> */
                /* PRD 利侩 矫 困狼 IF 巩阑 阜绊 酒贰狼 IF巩狼 林籍阑 钱绊 利侩窃.*/
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
                    
                    /* 20181031, hhlee, add, Change ToMachine is LinkedUnit ==>> */
                    toMachineName = unitName;
                    /* <<== 20181031, hhlee, add, Change ToMachine is LinkedUnit */
                    
                    /* 20181101, hhlee, add, Change TPEFOMPolicy Condition(not use unitname) ==>> */
                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
                    //Recipe
                       
               // start modify by jhiying on20191123 mantis:5449
                    
                 /* machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
                            lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));*/
                            
                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getINDPMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
                            lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"),portName);
              // end modify by jhiying on20191123 mantis:5449
                   
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
                    
                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
                    //Recipe
                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
                                                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
                    /* <<== 20181011, hhlee, Delete (Move Postion) */
                }
                /* PRD 利侩 矫 咯扁鳖瘤狼 IF巩狼 林籍阑 钱绊 利侩窃.*/
                /* <<== 20181101, hhlee, [toMachineName(Changed TOPolicy)] */
                                
                /* 20190412, hhlee, delete, MachineIdleTimeOver validation logic(Mantis:0003497) ==>> */
                ///* 20190115, hhlee, add, add logic Check Machine Idle Time ==>> */
                /* 20190717, park Jeong Su  Add Logic(Mantis:0004367)*/
                MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOver(machineName, lotData, portName);
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
        		MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
                        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, toMachineName);
        		//MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
                //        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), toMachineName);
        		/* <<== 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName */
                /* 20181001, hhlee, modify, location move, lotdata change ==>> */
                /* <<== 20181011, hhlee, Delete (Move Postion) */
                
        		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
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
            /* <<== hhlee, 20180616, Added TOMACHINE specified logic. */
            
            //add to MachineName
            SMessageUtil.setBodyItemValue(doc, "TOMACHINENAME", toMachineName);
            SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
            SMessageUtil.setBodyItemValue(doc, "PRIORITY", String.valueOf(lotData.getPriority()));
            /* 20190521, hhlee, modify, add validation (super lot flag=Y, Lot priority = 0) ==>> */
            /* 20190528, hhlee, modify, add validation (super lot flag=Y, Lot priority = 1) */
            /* Mantis: 0003939,  */
            if(StringUtil.equals(CommonUtil.getValue(lotData.getUdfs(), "SUPERLOTFLAG"), GenericServiceProxy.getConstantMap().FLAG_Y))
            {
                SMessageUtil.setBodyItemValue(doc, "PRIORITY", "1");
            }    
            /* <<== 20190521, hhlee, modify, add validation (super lot flag=Y, Lot priority = 0) */
            /* <<== 20181011, hhlee, Delete (Move Postion) */            
            
            /* 20180616, hhlee, Modify ==>> */
            //String machineRecipeName = StringUtil.EMPTY;
            try
            {
                //ProbeCard
                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
                {
                    eventLog.info("ProbeCard check Started.");
    
                    String pbCardType = "";
                    String probeCardSpecName = "";
    
                    if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
                    {
                        //get one PBCardType by mahcine
                        //pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(machineName).get(0).getDurableSpecName();
                        pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(machineName);

                        /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
                        if(StringUtil.isEmpty(pbCardType))
                        {
                            throw new CustomException("PROBECARD-0008", machineName);
                        }                            
                        /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
                        
                        PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
                        
                        /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
                        probeCardSpecName = pbCardType;
                        /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
                    }
                    else
                    {
                        //INDP
                        List<String>unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(machineName);
    
                        for(String unit : unitList)
                        {
                        	eventLog.info("UnitName : ["+unit+"], Linked Unit Name ["+portData.getUdfs().get("LINKEDUNITNAME")+"]");
                        	
                        	//2019.08.15 cy
                        	if(StringUtil.equals(unit, portData.getUdfs().get("LINKEDUNITNAME")))
                        	{
                        		//pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit).get(0).getDurableSpecName();
                                pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit);
                                
                                /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
                                if(StringUtil.isEmpty(pbCardType))
                                {
                                    throw new CustomException("PROBECARD-0008", machineName);
                                }                            
                                /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
                                
                                PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
                                
                                /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
                                if(StringUtil.equals(unit, toMachineName))
                                {
                                    probeCardSpecName = pbCardType;
                                }
                                /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
                        	}
                        }
                    }
    
                    eventLog.info("ProbeCard check Ended.");
                    
                    /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
                    SMessageUtil.setBodyItemValue(doc, "PROBECARDNAME", probeCardSpecName);
                    /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
                }
    
                //PhotoMask
                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_PHOTO))
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
        			//PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
                    String photoMaskName = PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
                    eventLog.info("PhotoMask check Ended .");
                    
                    /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
                    if(!StringUtils.equals(photoMaskName, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME))
                    {
                        if(StringUtil.equals(machineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
                        {
                            MESRecipeServiceProxy.getRecipeServiceUtil().validateRecipeParameterPhotoMaskName(eventInfo, machineName, machineRecipeName, photoMaskName);
                        }
                    }
                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
                                        
                    //check Product ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.
        			eventLog.info("Check ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.");
        			
        			/* 20190115, hhlee, delete, duplicate Inquery ==>> */
        			//List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
        			/* <<== 20190115, hhlee, delete, duplicate Inquery */
        			List<ProductFlag> productFlagList = new ArrayList<ProductFlag>();
        			
        			for(Product productData : productDataList)
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
            }
            catch (CustomException ce)
            {
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation, Add ErrorCode : MASK-0009, MASK-0100 */
                if(StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-9001") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0015" )
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0098")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0034") || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0035")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0026") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0027")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0016") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0092")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PROBECARD-0008") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0009")
                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0100"))
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
            /* <<== 20180616, hhlee, Modify */
            
            try
            {
                for (Element productElement : this.generateProductListElement(eventInfo, lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), logicalSlotMap))
                {
                    Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
    
                    productListElement.addContent(productElement);
                }
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
            
            List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
                        
            // PHOTO TRCKFLAG MANAGEMENT
            try
            {
                boolean checkTrackFlag = false;
                /* 20190115, hhlee, delete, duplicate Inquery ==>> */
                //List<Product> sProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
                /* <<== 20190115, hhlee, delete, duplicate Inquery */
                
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                String productGradeNList = StringUtil.EMPTY;
                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                
                for (Product sProductInfo : productDataList) 
                {
                	/* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                    if(StringUtil.equals(sProductInfo.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_N))
                    {                        
                        productGradeNList += sProductInfo.getKey().getProductName() + ",";                        
                    }                    
                    /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                    
                    //Modify 20181022 hsryu - TRACKFLAG column move to PRODUCT -> CT_PRODUCTFLAG
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
        				String trackFlag = productFlag.getTrackFlag();
        				if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
        				{
        					checkTrackFlag = true;
        					break;
        				}
        				else
        				{
        					continue;
        				}
        			}
                }
                
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
                if(StringUtil.isNotEmpty(productGradeNList))
                {
                    productGradeNList = StringUtil.substring(productGradeNList, 0, productGradeNList.length() -1);
                    throw new CustomException("PRODUCT-9008", productGradeNList);
                }
                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
                
                //String trackFlag = lotData.getUdfs().get("TRACKFLAG");
                //if (StringUtil.equals(trackFlag, "N") || StringUtil.isEmpty(trackFlag))
                //{
                //    checkTrackFlag = false;
                //}
                
                if ( checkTrackFlag == true )
                {
                    /* 20181212, hhlee, modify, delete Area check logic ==>> */
                    /* 20180714, Add, TrackFlag Validation ==>> */
                    //if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH) || 
                    ///    StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
                    //{
                    if ( StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
                            StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) )
                    {
                        // Nothing : Normal
                    }
                    else
                    {
                        /* 20180616, hhlee, Modify ==>> */
                        //// Abnormal
                        throw new CustomException("MACHINE-1001", machineData.getKey().getMachineName());
                        //eventInfo.setEventName("Hold");
                        //eventInfo.setEventComment(String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                        //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MACHINE-1001");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
                        //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
                        //return doc;
                        /* <<== 20180616, hhlee, Modify */
                    }
                    //}    
                    /* <<== 20180714, Add, TrackFlag Validation */
                    /* <<== 20181212, hhlee, modify, delete Area check logic */
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
                //eventLog.error("TRACKFLAG Fail!");
                //Glass is TRACKFLAG=Y, but Machine[] TRACKFLAG is not DRYF. Please Call INT.!!
                /* 20190320, hhlee, ProductGrade = 'N' , Error Return(PRODUCT-9008) */
                if(StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-1001") || StringUtil.equals(ce.errorDef.getErrorCode(),"PRODUCT-9008"))
                {
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
                }
                else
                {
                    eventLog.error("TRACKFLAG Fail!");
                }
            }
            
            /* BENDING : Not Need : 20180601, ==>> */
            //String bending = MESLotServiceProxy.getLotServiceUtil().getBendingValue(productList);
            String bending = StringUtil.EMPTY;
            SMessageUtil.setBodyItemValue(doc, "BENDING", bending);
            /* <<== BENDING : Not Need : 20180601, */

            /* SLOTBENDING : Not Need : 20180601, ==>> */
            //setBendingValue
            //for (Element prdElement : productList)
            //{
            //  prdElement.getChild("SLOTBENDING").setText(bending);
            //}
            /* <<== SLOTBENDING : Not Need : 20180601,  */

            /* 20180619, Add ELA Q-Time Flag Check ==> */
            /* If the glass is "ELA Q-Time Flag(QTIMEFLAG)" and "Y", the operation is only possible with the ELA machine. */
            try
            {
                if(ExtendedObjectProxy.getProductFlagService().checkProductFlagElaQtime(productList))
                {
                    if(!CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ELA"))
                    {
                        throw new CustomException("PRODUCT-9004", lotList.get(0).getKey().getLotName());
                        //eventInfo.setEventName("Hold");
                        //eventInfo.setEventComment(String.format("This Lot[%s] is only available in ELA machine.", lotList.get(0).getKey().getLotName()));
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"EL","");
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HOLD","ELHL", eventInfo.getEventComment(),"");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-9004");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("This Lot[%s] is only available in ELA machine.", lotList.get(0).getKey().getLotName()));
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot", "VRHL","");
                        //return doc;
                    }      
                }
            }
            catch (CustomException ce)
            {
                /* 20180616, hhlee, Modify ==>> */
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
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
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                return doc;
                /* <<== 20180616, hhlee, Modify */
                
            }
            /* <<== 20180619, Add ELA Q-Time Flag Check */
            
            //set Recipe
            for (Element prdElement : productList)
            {
                String recipeName = SMessageUtil.getChildText(prdElement, "PRODUCTRECIPE", false);

                if(recipeName != null && StringUtil.isNotEmpty(recipeName))
                {
                    for (Element eleTarget : productList)
                    {
                        String targetRecipe = SMessageUtil.getChildText(eleTarget, "PRODUCTRECIPE", false);

                        if(targetRecipe == null || StringUtil.isEmpty(targetRecipe))
                        {
                            eleTarget.getChild("PRODUCTRECIPE").setText(recipeName);
                        }
                    }

                    if(StringUtil.isEmpty(SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false)))
                        SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", recipeName);
                    break;
                }
            }
        }
        else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
        {
            eventLog.info("Sorter Port job download");
            
            /* 20180616, hhlee, Modify ==>> */
            try
            {
                //job validation
                /* 20190605, hhlee, modify, add Sorter TransferDirection ==>> */
                //this.validateSorterJob(machineName, portName, carrierName);
                transferDirection = this.validateSorterJob(machineName, portName, carrierName);
                /* <<== 20190605, hhlee, modify, add Sorter TransferDirection */
    
                //multi-Lot spec
                List<Lot> lotList = CommonUtil.getLotListByCarrier(carrierName, false);
    
                //Recipe
                String machineRecipeName = "";
    
                //Lot validation
                if (lotList.size() > 0)
                {
                    //using CST
                    //slot map validation    20170629 add BPK PL Port don't validation SlotMap
                    String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
                    
                    if(!machineSpecData.getUdfs().get("CONSTRUCTTYPE").equals("BPK")) //Is there type BPK ?
                    {
                        if (StringUtil.isNotEmpty(slotMap) && !slotMap.equals(logicalSlotMap))
                        {
                            throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
                            //eventInfo.setEventName("Hold");
                            //eventInfo.setEventComment("Different (OriSoltMap)" + slotMap + "SlotMap" + logicalSlotMap);
                            ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
                            ///* 20180601, Slot missmatch  ==>> */
                            ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-0020");
                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "Different (OriSoltMap)" + slotMap + "SlotMap" + logicalSlotMap);
                            ///* <<== 20180601, Slot missmatch */
                            //return doc;
                        }
                        //{
                        //  MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null, "SM","");
                        //}
                    }
    
                    /* 20190116, hhlee, modify, add MachineGroup Validation ==>> */
                    //for (Lot subLotData : lotList)
                    //{
                    //    lotData = subLotData;
                    // 
                    //    /* 20181031, Add, MachineGroup Validation Logic Move ==>> */
                    //    //String machineGroup = machineData.getMachineGroupName();
                    //    ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                    //    String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
                    //    String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
                    //    /* <<== 20181031, Add, MachineGroup Validation Logic Move */
                    //    
                    //    CommonValidation.checkLotState(lotData);
                    //    CommonValidation.checkLotProcessState(lotData);
                    //    CommonValidation.checkLotHoldState(lotData);
                    // 
                    //    /* 20181204, hhlee, delete , delete qtime ==>> */
                    //    ////20180504, kyjung, QTime
                    //    //MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());
                    //    /* <<== 20181204, hhlee, delete , delete qtime */
                    //    
                    //    //20180525, kyjung, MQC
                    //    MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
                    //}
                    
                    lotData = lotList.get(0);
                                       
                    CommonValidation.checkLotState(lotData);
                    CommonValidation.checkLotProcessState(lotData);
                    CommonValidation.checkLotHoldState(lotData);

                    /* 20181204, hhlee, delete , delete qtime ==>> */
                    ////20180504, kyjung, QTime
                    //MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());
                    /* <<== 20181204, hhlee, delete , delete qtime */
                    
                    //20180525, kyjung, MQC
                    MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
                    /* <<== 20190116, hhlee, modify, add MachineGroup Validation */
                    
                    /* 20190116, hhlee, modify, add MachineGroup Validation ==>> */
                    ////waiting step
                    //ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                    ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                    String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
                    String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
                    /* <<== 20190116, hhlee, modify, add MachineGroup Validation */
                    
                    if (!StringUtil.equals(machineOperationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
                    {
                        throw new CustomException("LOT-9041", lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName);
                        //eventInfo.setEventName("Hold");
                        //eventInfo.setEventComment(String.format("The operation[%s] of Lot[%s] to proceed is different from the machine[%s]", lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName));
                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                        ///* 20180601, Slot missmatch  ==>> */
                        ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "LOT-9041");
                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("The operation[%s] of Lot[%s] to proceed is different from the machine[%s]",  lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName));
                        ///* <<== 20180601, Slot missmatch */                       
                        //return doc;                        
                    }
                    
                    //singleLot
                    //representative Recipe
                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
                                            lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
    
                    //New
                    this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
                            durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
    
    
                    SMessageUtil.setBodyItemValue(doc, "CARRIERNAME", carrierName);
                    SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotData.getKey().getLotName());
                    SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
                    // MODIFY BY JHIYING START  BECAUSE SORTmachine PRIORITY IS NULL
                    SMessageUtil.setBodyItemValue(doc, "PRIORITY", String.valueOf(lotData.getPriority()));
                    
                    if(StringUtil.equals(CommonUtil.getValue(lotData.getUdfs(), "SUPERLOTFLAG"), GenericServiceProxy.getConstantMap().FLAG_Y))
                    {
                        SMessageUtil.setBodyItemValue(doc, "PRIORITY", "1");
                    }    
                 // MODIFY BY JHIYING END  BECAUSE SORTmachine PRIORITY IS NULL
                    /* 20180807, hhlee, Modify ==>> */
                    //for (Element productElement : this.generateProductListElement(lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE")))
                    for (Element productElement : this.generateProductListElement(eventInfo, lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), logicalSlotMap))
                    {
                        Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
    
                        productListElement.addContent(productElement);
                    }
                    /* <<== 20180807, hhlee, Modify */
                }
                else
                {
                    //empty CST
                    //slot map validation
                    if (StringUtil.isNotEmpty(slotMap))
                    {
                        /* 20181214, hhlee, modify, ==>> */
                        //CommonValidation.checkCstSlot(slotMap);
                        CommonValidation.checkCstSlot(carrierName, slotMap);
                        /* 20181214, hhlee, modify, ==>> */
                    }
                    
                    //this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), "", "", "", "", "", "", durableData.getDurableState(), durableData.getDurableType());
    
                    this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
                            durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
    
                    SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
                }
            }
            catch (CustomException ce)
            {              
                //eventInfo.setEventName("Hold");
                //eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
                ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                //return doc;
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                if(StringUtil.equals(ce.errorDef.getErrorCode(),"PRODUCT-0020"))
                {
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, "", "");
                	try {
                		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                		MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, true, "", "");
                		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    /* <<== 20190424, hhlee, modify, changed function */
                }
                else if(StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9041") || StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-0102") ||
                        StringUtil.equals(ce.errorDef.getErrorCode(),"SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(),"PROCESSOPERATION-9001") ||
                        StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9027") || StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9049"))
                {
                    /* 20190424, hhlee, modify, changed function ==>> */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null,"HoldLot","VRHL","");
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
                //20181214, hhlee, add
                else if(StringUtil.equals(ce.errorDef.getErrorCode(),"CST-0042")) //20181214, hhlee, add
                {
                    /* 20180531, Add CST Hold Logic ==>> */
                    MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
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
            /* <<== 20180616, hhlee, Modify */
        }
        else
        {
            //PU Port
            eventLog.info("Unloader Port job download");
            /* 20180616, hhlee, Modify ==>> */
            try
            {
                CommonValidation.checkEmptyCst(carrierName);
    
                /* 20190124, hhlee, add empty cassette validation ==>> */
                lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
                if (lotData != null)
                {
                    throw new CustomException("CST-0006", carrierName);
                }
                /* <<== 20190124, hhlee, add empty cassette validation */
                
                //slot map validation
                if (StringUtil.isNotEmpty(slotMap))
                {
                    /* 20181214, hhlee, modify, ==>> */
                    //CommonValidation.checkCstSlot(slotMap);
                    CommonValidation.checkCstSlot(carrierName, slotMap);
                    /* 20181214, hhlee, modify, ==>> */
                }
                
                //generateBodyTemplate
                this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
                        durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
    
                SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
                SMessageUtil.setBodyItemValue(doc, "SLOTSEL", this.slotMapTransfer(slotMap));
            }
            catch (CustomException ce)
            {               
                /* 20180531, Add CST Hold Logic ==>> */
                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                /* <<== 20180531, Add CST Hold Logic */             
                return doc;
            }
            /* <<== 20180616, hhlee, Modify */
        }
        
        // 2019.05.21_hsryu_Add Logic. return SlotSel and Memory Note. Mantis 0003959.
        String slotSel = "";

        try
        {
            //generate selection map
        	slotSel = this.doGlassSelection(doc, lotData, durableData, slotMap);        	
        	
        	/* 20190605, hhlee, add, SlotSel All "N" Validation ==>> */
        	Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);        	
        	if(productListElement != null)
        	{
        	    if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
                        CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS")) 
        	    {
            	    if(StringUtil.indexOf(slotSel, "Y") < 0 &&
            	            !StringUtil.equals(transferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
            	    {
            	        throw new CustomException("LOT-9051", durableData.getKey().getDurableName(), slotSel);
            	    }
        	    } 
        	    else
        	    {        	        
        	    }
        	}
        	/* <<== 20190605, hhlee, add, SlotSel All "N" Validation */
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

        try
        {
            // 2019.05.21_hsryu_Add Logic. return SlotSel and Memory Note. Mantis 0003959.
            this.MemorySlotSelNoteForSampling(lotData, slotSel, eventInfo);
        }
        catch (Throwable e)
        {
        	eventLog.warn("Error Memory Sampling SlotSel.");
        }
        
//        //2019.09.02 Add By Park Jeong Su Mantis 4691
//		try {
//			if(lotData!=null){
//				MESLotServiceProxy.getLotServiceImpl().modifyLotFirstCheckResultY(lotData.getKey().getLotName());
//			}
//		} catch (CustomException ce) {
//			eventLog.info("Change First Check Result Error.");
//		}
        
        return doc;
    }



    private String createSlotMapforUPK(Durable durableData) throws CustomException
    {
        String SlotMap = "";

        long durableCapacity = durableData.getCapacity();

        for(int i = 0; i < durableCapacity; i++)
            SlotMap +=SlotMap;

        return SlotMap;
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
                            throw new CustomException("PROBECARD-0002", machineName, probeCard.getKey().getDurableName(), probeCardType, 
                                    probeCard.getKey().getDurableName(), tempprobeCardType);
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
        String condition = "WHERE SUPERMACHINENAME = ? AND MACHINENAME <> ?";

        Object[] bindSet = new Object[]{machineName,"A2TST011"};

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

    /**
     * append one by one to LotInfoDownloadRequest
     * @author wghuang
     * @since 2018.03.29
     * @param bodyElement
     * @param machineName
     * @param carrierName
     * @param durableState
     * @param durableType
     * @param portName
     * @param portType
     * @param portUseType
     * @param slotMap
     * @param processOperationName
     * @return
     * @throws CustomException
     */
    private Element generateEDOBodyTemplate(Element bodyElement,
                Machine machineData, String carrierName,String durableState,String durableType,String portName,
                String portType,String portUseType,String slotMap, String processOperationName)
        throws CustomException
    {
        //remove Body Content
        bodyElement.removeContent();

        Element machineNameE = new Element("MACHINENAME");
        machineNameE.setText(machineData.getKey().getMachineName());
        bodyElement.addContent(machineNameE);

        Element carrierNameE = new Element("CARRIERNAME");
        carrierNameE.setText(carrierName);
        bodyElement.addContent(carrierNameE);

        Element carrierStateE = new Element("CARRIERSTATE");
        carrierStateE.setText(durableState);
        bodyElement.addContent(carrierStateE);

        Element carrierTypeE = new Element("CARRIERTYPE");
        carrierTypeE.setText(durableType);
        bodyElement.addContent(carrierTypeE);

        Element portNameE = new Element("PORTNAME");
        portNameE.setText(portName);
        bodyElement.addContent(portNameE);

        Element portTypeE = new Element("PORTTYPE");
        portTypeE.setText(portType);
        bodyElement.addContent(portTypeE);

        Element portUseTypeE = new Element("PORTUSETYPE");
        portUseTypeE.setText(portUseType);
        bodyElement.addContent(portUseTypeE);

        Element slotMapE = new Element("SLOTMAP");
        slotMapE.setText(slotMap);
        bodyElement.addContent(slotMapE);


        //need fill out side
        Element slotSelE = new Element("SLOTSEL");
        slotSelE.setText("");
        bodyElement.addContent(slotSelE);

        Element machineRecipeName = new Element("MACHINERECIPENAME");
        machineRecipeName.setText("");
        bodyElement.addContent(machineRecipeName);

        Element probeCardNameE = new Element("PROBECARDNAME");
        probeCardNameE.setText("");
        bodyElement.addContent(probeCardNameE);

        Element operationModeNameE = new Element("OPERATIONMODENAME");
        operationModeNameE.setText(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
        bodyElement.addContent(operationModeNameE);

        //need fill out side
        Element toMachineNameE = new Element("TOMACHINENAME");
        toMachineNameE.setText("");
        bodyElement.addContent(toMachineNameE);

        Element bendingE = new Element("BENDING");
        bendingE.setText("");
        bodyElement.addContent(bendingE);

        Element priorityE = new Element("PRIORITY");
        priorityE.setText("");
        bodyElement.addContent(priorityE);

        //DCRReadFlag
        Element DCRReadFlagE = new Element("DCRREADFLAG");
        DCRReadFlagE.setText("");
        bodyElement.addContent(DCRReadFlagE);

        Element productListE = new Element("PRODUCTLIST");
        productListE.setText("");
        bodyElement.addContent(productListE);

        return bodyElement;
    }
    
    /**
     * write down processing material info
     * @author swcho
     * @since 2015.03.09
     * @param lotList
     * @param machineData
     * @param portData
     * @param durableData
     * @param machineRecipeName
     * @return
     * @throws CustomException
     */
    private List<Element> generateProductListElement(EventInfo eventInfo, List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
            String machineRecipeName, String machineConstructType, String logicalSlotMap)
        throws CustomException
    {
        //final return
        List<Element> productListElement = new ArrayList<Element>();
        
        String chamberGroupName = StringUtil.EMPTY;
        String machineGroupName = StringUtil.EMPTY;
        
        /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added ==>> */
        String compareNodeStack = StringUtil.EMPTY;        
        String compareLotName = StringUtil.EMPTY;
        /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added */
        
        for (Lot lotData : lotList)
        {
            //avail Product list
            List<Product> productList;
            try
            {
                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            }
            catch (Exception ex)
            {
                throw new CustomException("SYS-9999", "Product", "No Product to process");
            }

            //base Product info
            ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

            //base flow info
            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

            //waiting step
            ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

            //get Sorter job List
            List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
            
            /* 20180809, Add, Sorter Job Product List ==>> */
            //if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
            if (StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
            /* <<== 20180809, Add, Sorter Job Product List */
            {
                sortJobList = ExtendedObjectProxy.getSortJobService().getSortJobList(durableData, lotData, machineData, portData);
            }

            /* hhlee, 20180616, Add SampleLot import ==>> */
            String actualSamplePosition = StringUtil.EMPTY;
            /* 20190312, hhlee, add Check Validation */
            //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
            /* 20190522, hhlee, modify, add check validation SamplingFlow */
            //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
            //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
            //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
            if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                    flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                    !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
            {
                /* 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter ==>> */
                //actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap);
                actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap, false);
                /* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter */
                
                /* 20190517, hhlee, add, Sampling Position Validation ==>> */
                if(StringUtil.isEmpty(actualSamplePosition))
                {
                    throw new CustomException("LOT-9049", lotData.getKey().getLotName(), lotData.getProcessOperationName()); 
                }
                /* <<== 20190517, hhlee, add, Sampling Position Validation */
            }            
            /* <<== hhlee, 20180616, Add SampleLot import */
            
            /* 20180620, hhlee, Add Set ChamberGroup ==>> */
            machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
            chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(
                    machineData.getKey().getMachineName(), machineGroupName, "3");
            /* <<== 20180620, hhlee, Add Set ChamberGroup */
            
            /* 20190117, hhlee, add, change productRecipeName ==>> */
            ///* 20181020, hhlee, add, MQC RecipeName valiable ==>> */
            //String mqcProductRecipeName = StringUtil.EMPTY;
            ///* <<== 20181020, hhlee, add, MQC RecipeName valiable */
            String productRecipeName = StringUtil.EMPTY;
            /* <<== 20190117, hhlee, add, change productRecipeName */            
            
            for (Product productData : productList)
            {
                if(!StringUtil.equals(productData.getProductState(), GenericServiceProxy.getConstantMap().Prod_InProduction) ||
                     !StringUtil.equals(productData.getProductProcessState(), GenericServiceProxy.getConstantMap().Prod_Idle) ||
                        !StringUtil.equals(productData.getProductHoldState(), GenericServiceProxy.getConstantMap().Prq_NotOnHold) )
                {
                    throw new CustomException("PRODUCT-9006", productData.getKey().getProductName(),
                            productData.getProductState(), productData.getProductProcessState(),productData.getProductHoldState());
                }
                
                /* 20200901, mgkang, add, check Double Run ==>> */
                checkDoubleRun(productData.getKey().getProductName(), productData.getProcessFlowName(), productData.getProcessOperationName());
                
                /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
                if(!StringUtil.equals(lotData.getNodeStack(), productData.getNodeStack()))
                {
                	//2019.07.22 dmlee : Request by Only Validation CIM Lot Grade 'S'
                	if(!lotData.getLotGrade().equals("S"))
                	{
                		if(!StringUtil.equals(compareLotName, lotData.getKey().getLotName()))
                        {
                            compareNodeStack += "LOTNAME: " + lotData.getKey().getLotName() + " - PRODUCTNAME: ";
                            compareLotName = lotData.getKey().getLotName();
                        }
                        compareNodeStack += productData.getKey().getProductName() + ",";
                	}
                    
                }
                /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
                
                String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");
                // Start 2019.09.11 Add By Park Jeong Su Mantis 4706
                
                String consumableSpecName = CommonUtil.getValue(productData.getUdfs(), "CONSUMABLESPECNAME");
                ConsumableSpec consumableSpecData=null;
                if(StringUtils.isNotEmpty(consumableSpecName)){
                	consumableSpecData = GenericServiceProxy.getSpecUtil().getConsumableSpec(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,consumableSpecName,GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
                }
                else if(StringUtils.isNotEmpty(crateName)){
                	consumableSpecData = CommonUtil.getConsumableSpec(crateName);
                }
                else{
                	// consumableSpecName && crateName All Empty
                	eventLog.info("crateName :[ " + crateName+"] consumableSpecName : ["+consumableSpecName+"] All Empty !");
                	throw new CustomException("CONS-0006");
                }
                
                // Start 2019.09.11 Delete By Park Jeong Su Mantis 4706
//                //DP box data
//                ConsumableSpec consumableSpecData=null;
//                // 2019.09.05 Modify By Park Jeong Su Mantis 4706
//                try {
//                	consumableSpecData = CommonUtil.getConsumableSpec(crateName);
//				} catch (Exception e) {
//					throw new CustomException("CONS-0006");
//				}
                // End 2019.09.11 Delete By Park Jeong Su Mantis 4706
                
                
                // End 2019.09.11 Add By Park Jeong Su Mantis 4706
                
                String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
                //String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
                String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
                String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
                String samplingFlag = "";
                
                /* 20190117, hhlee, add, change productRecipeName ==>> */
                productRecipeName = StringUtil.EMPTY;
                /* <<== 20190117, hhlee, add, change productRecipeName */ 
                
                //Glass selection
                //161228 by swcho : additional sampling
                //if (operationData.getProcessOperationType().equals("Inspection") && 
                //        && !flowData.getProcessFlowType().equals("MQC"))
                /* 20190312, hhlee, add Check Validation */
                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
                /* 20190522, hhlee, modify, add check validation SamplingFlow */
                //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
                //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                        !StringUtil.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
                {
                    /* 20180602, Need Sampling Logic ==>> */
                    /* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
                    Set SlotSel to " Y " for the entire operation of Glass. */
                    /* 20190517, hhlee, modify, default = 'N' */
                    //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
                    if(!StringUtil.isEmpty(actualSamplePosition))
                    {
                        /* 20190517, hhlee, modify, default = 'N' */
                        //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
                        
                        String[] actualsamplepostion = actualSamplePosition.trim().split(",");
                        for(int i = 0; i < actualsamplepostion.length; i++ )
                        {
                            if(productData.getPosition() == Long.parseLong(actualsamplepostion[i]))
                            {
                                samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
                                break;
                            }
                        }     
                    }
                    /* <<== 20180602, Need Sampling Logic */
                }
                else
                {
                    /* 20180809, Add, Sorter Job Product List ==>> */
                    //sort case
                    //if (StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
                    if (StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
                    /* <<== 20180809, Add, Sorter Job Product List */
                    {
                        samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
                    }
                    //repair case
                    /* 20190522, hhlee, modify, add check validation SamplingFlow */
                    //else if (StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
                    else if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) && 
                            StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
                    {
                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        else
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                    }
                    //rework case
                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK))
                    {
                        /* 20190515, hhlee, add, Rework (SlotSel)Logic  */
                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK);   
                    }
                    //MQC SlotMap
                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
                    {
                        /* 20190117, hhlee, modify, MQC Positon validation */
                        /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
                        //mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
                        //productRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);                        
                        /* 20190117, hhlee, modify, MQC Positon */
                        List<Map<String, Object>> slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProduct(productData);
                        String slotPostion = StringUtil.EMPTY;
                        if(slotPostionInfoList != null)
                        {
                            /* 20190124, hhlee, modify , add logic null value check */
                            //slotPostion = slotPostionInfoList.get(0).get("POSITION").toString();
                            slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
                                    slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);
                            /* 20190124, hhlee, modify , add logic null value check */
                            //productRecipeName = slotPostionInfoList.get(0).get("RECIPENAME").toString(); 
                            productRecipeName = (slotPostionInfoList.get(0).get("RECIPENAME") != null ? 
                                    slotPostionInfoList.get(0).get("RECIPENAME").toString() : StringUtil.EMPTY);                            
                        }
                        if(StringUtil.isEmpty(slotPostion) || 
                                productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }                        
                        /* <<== 20181020, hhlee, modify, MQC RecipeName valiable */
                    }
                    /* 20190513, hhlee, add, Branch (SlotSel)Logic  */
                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
                    {
                        /* 20190515, hhlee, add, Branch (SlotSel)Logic  */
                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH);  
                    }                    
                    else
                    {
                        /* 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" ==>>  */
                        ////samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                        }
                        else
                        {
                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                        }
                        /* <<== 20180713, Delete */
                        /* <<== 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" */
                    }
                }

                /* 20190117, hhlee, modify, change logic machieRecipename, mqcProductRecipeName -> productRecipeName ==>> */
                ///* 20181020, hhlee, add, MQC RecipeName valiable ==>> */
                //if(StringUtil.isNotEmpty(mqcProductRecipeName))
                //{
                //    machineRecipeName = mqcProductRecipeName;
                //    mqcProductRecipeName= StringUtil.EMPTY;
                //}
                ///* <<== 20181020, hhlee, add, MQC RecipeName valiable */
                if(StringUtil.isEmpty(productRecipeName))
                {
                    productRecipeName = machineRecipeName;
                }
                /* <<== 20190117, hhlee, modify, change logic machieRecipename, mqcProductRecipeName -> productRecipeName */
                
                /* 20190117, hhlee, modify, add parameter ==>> */
                ////machineData
                //Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
                //        productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);
                
                Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
                        productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName,productRecipeName);

                productListElement.add(productElement);
                
                //add by BRLEE for ProductProcLocationHist linkKey(Use SPC).
          		int linkKey =0;
          		if(productData.getUdfs().get( "LINKKEY" ).isEmpty())
          		{
          			linkKey=linkKey+1;
          		}
          		else
          		{
          		   linkKey = Integer.parseInt( productData.getUdfs().get( "LINKKEY" ).toString() )+1;
          		}
          		
          		productData.getUdfs().put( "LINKKEY", String.valueOf( linkKey ) );
          		eventInfo.setEventName( "SetLinkKey" );
          		eventInfo.setEventTime( TimeUtils.getCurrentTimestamp() );
          		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey() );
          		eventInfo.setEventUser( machineData.getKey().getMachineName() );
          		kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
          		setEventInfo.setUdfs(productData.getUdfs() );
          		ProductServiceProxy.getProductService().setEvent( productData.getKey(), eventInfo, setEventInfo );
          		
                /* <<== 20190117, hhlee, modify, add parameter */
                
                /* 20190315, hhlee, Debug Log write */
                eventLog.info(String.format("SLOTSEL - SampleFlag=%s, [ProcessFlowType=%s, ProcessOperationType=%s, DetailProcessOperationType=%s, ProductGrade=%s]", 
                        samplingFlag, flowData.getProcessFlowType(), operationData.getProcessOperationType(), 
                        operationData.getDetailProcessOperationType(), productData.getProductGrade()));
            }
            
        }

        /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
        if(StringUtil.isNotEmpty(compareNodeStack))
        {
            compareNodeStack = StringUtil.substring(compareNodeStack, 0, compareNodeStack.length() -1);
            throw new CustomException("LOT-9047", compareNodeStack);
        }
        /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
        
        return productListElement;
    }

    /**
     * @author wuzhiming
     * @param productData
     * @param consumableSpecData
     * @return 除了O1PPK设备外，其他设备返回实际厚度
     * @throws CustomException
     */
    private String getExactProductThickness(Product productData, ConsumableSpec consumableSpecData, String machineName) throws CustomException
    {
        MachineSpecKey machinespecKey = new MachineSpecKey();
        machinespecKey.setMachineName(machineName);
        MachineSpec machineSpec = MachineServiceProxy.getMachineSpecService().selectByKey(machinespecKey);

        String construcType = CommonUtil.getValue(machineSpec.getUdfs(), "CONSTRUCTTYPE");

        String productThickness = "";
        if(StringUtil.equalsIgnoreCase(construcType, "BPK"))
        {
            productThickness = getProductThickness(productData, consumableSpecData);
        }
        else
        {
            productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
        }
        return productThickness;
    }

    /**
     * @author wuzhiming
     * @param productData
     * @param consumableSpecData
     * @return 玻璃的厚度
     * @throws CustomException
     */
    private String getProductThickness(Product productData, ConsumableSpec consumableSpecData) throws CustomException
    {
        String productThickness = "";
        String thicknessAfterAssemble = getThicknessAfterAssemble(productData);
        if (!StringUtil.isEmpty(thicknessAfterAssemble))
        {
          productThickness = thicknessAfterAssemble;
        }else {
            //2017.04.10 wuzhiming add: 得到pairProduct的glassThickNess
            double pairProductThickness = this.getPairProductThickness(productData) ;
            productThickness = String.valueOf(this.addTwoDouble(Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS")), pairProductThickness));

        }
        return productThickness;
    }

    /**
     * @author wuzhiming
     * @param productData
     * @return modeler中设定的和productspec绑定的贴合后玻璃的厚度
     */
    private String getThicknessAfterAssemble(Product productData)
    {
        ProductSpecKey productspecKey = new ProductSpecKey();
        productspecKey.setProductSpecName(productData.getProductSpecName());
        productspecKey.setFactoryName(productData.getFactoryName());
        productspecKey.setProductSpecVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
        ProductSpec ProductSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productspecKey);

        String thicknessAfterAssembleString = CommonUtil.getValue(ProductSpecData.getUdfs(), "THICKNESSAFTERASSEMBLE");
        return thicknessAfterAssembleString;
    }

    /**
     * @Author wuzhiming
     * @since 2017.03.10 16:46
     * @param productData 主玻璃Data
     * @return pairProductThickness ：贴合玻璃的厚度，若没有贴合玻璃，返回0
     * @throws  CustomException ：consumableSpec 没找到时将抛出异常
     */
    private double getPairProductThickness(Product productData) throws CustomException
    {
        String pairProductName = CommonUtil.getValue(productData.getUdfs(),"PAIRPRODUCTNAME");

        if (StringUtil.isEmpty(pairProductName))
        {
            return 0;
        }else {
            Product pairProductData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(pairProductName));
            String crateName = CommonUtil.getValue(pairProductData.getUdfs(), "CRATENAME");
            ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
            double pairProductThickness = Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS"));

            return pairProductThickness;
        }
    }

    /**
     * @author wuzhiming
     * @since 2017.03.10 16:55
     * @param v1
     * @param v2
     * @return 返回精确的两个double值相加
     */
    public double addTwoDouble(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * @Name     generateEDOProductElement
     * @since    2018. 6. 21.
     * @author   hhlee
     * @contents 
     * @param productData
     * @param productionType
     * @param productThickness
     * @param productSize
     * @param machineRecipeName
     * @param samplingFlag
     * @param Bending
     * @param machineConstructType
     * @param productSpecData
     * @param chamberGroupName
     * @return
     * @throws CustomException
     */
    private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
                                            String productSize, String machineRecipeName, String samplingFlag,
                                            String bending, String machineConstructType, ProductSpec productSpecData,
                                            String chamberGroupName, String productRecipeName)
        throws CustomException
    {
        Element productElement = new Element("PRODUCT");

        //productName
        Element productNameE = new Element("PRODUCTNAME");
        productNameE.setText(productData.getKey().getProductName());
        productElement.addContent(productNameE);

        //position
        Element positionE = new Element("POSITION");
        positionE.setText(String.valueOf(productData.getPosition()));
        productElement.addContent(positionE);

        //lotName
        Element lotNameE = new Element("LOTNAME");
        lotNameE.setText(productData.getLotName());
        productElement.addContent(lotNameE);

        //productSpecName
        Element productSpecNameE = new Element("PRODUCTSPECNAME");
        productSpecNameE.setText(productData.getProductSpecName());
        productElement.addContent(productSpecNameE);

        //productionType
        Element productionTypeE = new Element("PRODUCTIONTYPE");
        productionTypeE.setText(productionType);
        productElement.addContent(productionTypeE);

        //productType
        Element productTypeE = new Element("PRODUCTTYPE");
        productTypeE.setText(productData.getProductType());
        productElement.addContent(productTypeE);

        //processflow
        Element processflowE = new Element("PROCESSFLOWNAME");
        processflowE.setText(productData.getProcessFlowName());
        productElement.addContent(processflowE);

        //processOperationName
        Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
        processOperationNameE.setText(productData.getProcessOperationName());
        productElement.addContent(processOperationNameE);

        //productSize
        Element productSizeE = new Element("PRODUCTSIZE");
        productSizeE.setText(productSize);
        productElement.addContent(productSizeE);

        //workOrder
        Element workOrderE = new Element("WORKORDER");
        workOrderE.setText(productData.getProductRequestName());
        productElement.addContent(workOrderE);

        //reworkCount
        Element reworkCountE = new Element("REWORKCOUNT");
        reworkCountE.setText(String.valueOf(productData.getReworkCount()));
        productElement.addContent(reworkCountE);

        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
        //productJudge
        Element productJudgeE = new Element("PRODUCTJUDGE");
        productJudgeE.setText(productData.getProductGrade());
        //productJudgeE.setText(productData.getUdfs().get("PRODUCTJUDGE"));
        productElement.addContent(productJudgeE);
        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
        
        //PanelProdcutJudges
        Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
        /* 20180722, hhlee, Add, Panel Product Judge ==>> */
        //ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
        // OLED : [ O : Good | X : No good ]
        String panelProductJudge = ExtendedObjectProxy.getPanelJudgeService().getPanelProductJudge(productData.getKey().getProductName(), productData.getProductType(), false);
        panelProductJudgesE.setText(panelProductJudge);        
        productElement.addContent(panelProductJudgesE);

        //productRecipe
        /* 20190117, hhlee, delete, Change MQC Recipe Logic ==>> */
        //Element productRecipeE = new Element("PRODUCTRECIPE");
        //String recipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
        //if(!StringUtil.isEmpty(recipeName))
        //{
        //    productRecipeE.setText(recipeName);
        //}
        //else
        //{
        //    productRecipeE.setText(machineRecipeName);
        //}        
        Element productRecipeE = new Element("PRODUCTRECIPE");
        productRecipeE.setText(productRecipeName);
        productElement.addContent(productRecipeE);
        /* 20190117, hhlee, delete, Change MQC Recipe Logic ==>> */
        
        //crateName
        Element crateNameE = new Element("CRATENAME");
        crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
        productElement.addContent(crateNameE);

        //productThickness
        Element productThicknessE = new Element("PRODUCTTHICKNESS");
        productThicknessE.setText(productThickness);
        productElement.addContent(productThicknessE);

        /* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
        String exposureUnitId = StringUtil.EMPTY;
        String exposureRecipeName = StringUtil.EMPTY;
        String maskName = StringUtil.EMPTY;
        if (machineConstructType.equals("CDME"))
        {
            ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
                    productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
            if(pExposureFeedBackData != null)
            {
                exposureUnitId = pExposureFeedBackData.getUnitName();
                exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
                maskName = pExposureFeedBackData.getMaskName();
            }
        }
        //exeposureUnitId
        Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
        exeposureUnitIdE.setText(exposureUnitId);
        productElement.addContent(exeposureUnitIdE);

        //exeposureRecipeName
        Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
        exeposureRecipeNameE.setText(exposureRecipeName);
        productElement.addContent(exeposureRecipeNameE);

        //MaskName
        Element maskNameE = new Element("MASKNAME");
        maskNameE.setText(maskName);
        productElement.addContent(maskNameE);
        /* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */

        //ChamberID
        Element chamberIDE = new Element("CHAMBERID");
        chamberIDE.setText(chamberGroupName);
        productElement.addContent(chamberIDE);

        //slotPriority
        Element slotPriorityE = new Element("SLOTPRIORITY");
        slotPriorityE.setText("");
        productElement.addContent(slotPriorityE);

        //slotBending
        Element slotBendingE = new Element("SLOTBENDING");
        slotBendingE.setText(bending);
        productElement.addContent(slotBendingE);

        /* 20180531, Get Product Process Data(Last 5 processes) ==>> */
        //ProcessList
        Element processListElement = new Element("PROCESSLIST");
        //Element processElement = new Element("PROCESS");
        //
        //Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
        //processOperationNameLElement.setText("");
        //processElement.addContent(processOperationNameLElement);
        //
        //Element processMachineNameElement = new Element("PROCESSMACHINENAME");
        //processMachineNameElement.setText("");
        //processElement.addContent(processMachineNameElement);
        //
        //processListElement.addContent(processElement);
        if (machineConstructType.equals("AOI"))
        {
            processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
        }

        /* <<== 20180531, Get Product Process Data(Last 5 processes) */
        productElement.addContent(processListElement);


        /* Array Product Flag Setting ==>> */
        productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
        /* <== Array Product Flag Setting */

        ////TurnOverFlag
        //Element turnOverFlagElement = new Element("TURNOVERFLAG");
        //turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
        //productElement.addContent(turnOverFlagElement);

        ////TurnOverFlag
        //Element turnSideFlagElement = new Element("TURNSIDEFLAG");
        //turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
        //productElement.addContent(turnSideFlagElement);

        //SamplingFlag
        Element samplingFlagElement = new Element("SAMPLINGFLAG");
        samplingFlagElement.setText(samplingFlag);
        productElement.addContent(samplingFlagElement);
        
        return productElement;
    }
    
    /**
     * Glass determine going to proceed
     * @author swcho
     * @since 2015.03.10
     * @param doc
     * @param lotData
     * @param durableData
     * @param slotMap
     * @throws CustomException
     */
    private String doGlassSelection(Document doc, Lot lotData, Durable durableData, String slotMap)
        throws CustomException
    {
        StringBuffer slotMapTemp = new StringBuffer();

         for (long i=0; i<durableData.getCapacity(); i++)
         {
             slotMapTemp.append(GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
         }

         if (!slotMap.isEmpty() && (slotMap.length() != slotMapTemp.length()))
             throw new CustomException("CST-2001", durableData.getKey().getDurableName());

        List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

        for (Element eleProduct : lstDownloadProduct)
        {
            String sSelection = SMessageUtil.getChildText(eleProduct, "SAMPLINGFLAG", true);
            String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);

            int position;
            try
            {
                position = Integer.parseInt(sPosition);
            }
            catch (Exception ex)
            {
                position = 0;
            }

            if (sSelection.equalsIgnoreCase(GenericServiceProxy.getConstantMap().Flag_Y))
            {
                slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT);
            }
            else
            {
                slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
            }

            //Glass selection decision
            SMessageUtil.setBodyItemValue(doc, "SLOTSEL", slotMapTemp.toString());

            //modified by wghuang 20180402
            eleProduct.removeChild("SAMPLINGFLAG");
        }

        eventLog.debug("Completed Slot Selection : " + slotMapTemp.toString() );
        
        // 2019.05.21_hsryu_Add Logic. Return SlotSel Note. 
        return slotMapTemp.toString();
    }

    private Document downloadCarrierJob(Document doc, String slotMap, Durable durableData, Machine machineData, String portName, String portType, String portUseType, String defaultRecipeNameSpace)
        throws CustomException
    {
        eventLog.info("CST Cleaner job download");

        try
        {
            //validation
            CommonValidation.checkEmptyCst(durableData.getKey().getDurableName());
        }
        catch (CustomException ce)
        {
            /* 20180531, Add CST Hold Logic ==>> */
            MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-0006");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
            return doc;
            //throw new CustomException("CST-0006", durableData.getKey().getDurableName());
            /* <<== 20180531, Add CST Hold Logic */
        }

        /* 20180531, Add CST Hold Logic ==>> */
        try
        {
            //slot map validation
            if (StringUtil.isNotEmpty(slotMap))
            {
                CommonValidation.checkCstSlot(slotMap);
            }
        }
        catch (CustomException ce)
        {
            MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-0026");
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
            return doc;
            //throw new CustomException("CST-0026");
        }
        /* <<== 20180531, Add CST Hold Logic */

        /* 20181212, hhlee, add, Validate Clean State=Clean ==>> */
        try
        {
            if(StringUtil.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
            {
                throw new CustomException("CST-0041", durableData.getKey().getDurableName());
            }            
        }
        catch (CustomException ce)
        {
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
            return doc;
        }
        /* <<== 20181212, hhlee, add, Validate Clean State=Clean */
        
        //New
        this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
                durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");

        //Recipe how to control?
        //String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(durableData.getFactoryName(), durableData.getDurableSpecName(), durableData.getKey().getDurableName(), machineData.getKey().getMachineName(), false);

        DurableSpecKey keyInfo = new DurableSpecKey();
        keyInfo.setDurableSpecName(durableData.getDurableSpecName());
        keyInfo.setFactoryName(durableData.getFactoryName());
        keyInfo.setDurableSpecVersion("00001");

        DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);
        // Start 2019.09.17 Add By Park Jeong Su Mantis 4813
        String designatedRecipeName = CommonUtil.getValue(durableSpecData.getUdfs(), "CSTCLEANRECIPE");
        SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", designatedRecipeName);
        SMessageUtil.setBodyItemValue(doc, "SLOTSEL", this.slotMapTransfer(slotMap));
        
        eventLog.info("RMS start");
        MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineData.getKey().getMachineName());
        try {
			if(macSpecData.getUdfs().get("RMSFLAG").toString().equals("Y")){
				MESRecipeServiceProxy.getRecipeServiceUtil().checkRMSCompareResult(macSpecData.getKey().getMachineName(),designatedRecipeName,true);
			}
		} catch (CustomException e) {
			eventLog.info(e.errorDef.getEng_errorMessage());
			throw e;
		}
        eventLog.info("RMS End");
        //End 2019.09.17 Add By Park Jeong Su Mantis 4813
        return doc;
    }

    /* 20190605, hhlee, modify ==>> */
    private String validateSorterJob(String machineName, String portName, String carrierName) throws CustomException
    {
        String transferDirection = StringUtil.EMPTY;
        
        StringBuffer sqlBuffer = new StringBuffer().append("SELECT J.JOBNAME, J.JOBSTATE, J.PROCESSFLOWNAME, J.PROCESSOPERATIONNAME,    ").append("\n")
                                                   .append("       C.CARRIERNAME, C.LOTNAME, C.TRANSFERDIRECTION    ").append("\n")
                                                   .append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ").append("\n")
                                                   .append(" WHERE J.JOBNAME = C.JOBNAME ").append("\n")
                                                   .append("   AND C.MACHINENAME = ?  ").append("\n")
                                                   .append("   AND C.CARRIERNAME = ?  ").append("\n")
                                                   .append("   AND C.PORTNAME = ? ").append("\n")
                                                   .append("   AND J.JOBSTATE IN (?, ?)");

        List<ListOrderedMap> result;

        try
        {
            result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {machineName, carrierName, portName, "WAIT", "CONFIRMED"});
        }
        catch (Exception ex)
        {
            result = new ArrayList<ListOrderedMap>();
        }

        if (result.size() < 1)
        {
            throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
        }
        
        transferDirection = result.get(0).get("TRANSFERDIRECTION") != null ? result.get(0).get("TRANSFERDIRECTION").toString() : StringUtil.EMPTY;
        
        return transferDirection;
    }
    /* 20190605, hhlee, modify ==>> */

    private Map<String, Long> getProductNameAndPosition(String lotName, ProductRequestPlan planData)
    {
        Long planCount = planData.getPlanQuantity() - planData.getReleasedQuantity();
        String condition = "WHERE lotName = ? AND ROWNUM <= ? ORDER BY position desc";
        Object[] bindSet = new Object[]{lotName, planCount};

        List<Product> products = ProductServiceProxy.getProductService().select(condition, bindSet);
        LinkedHashMap<String,Long> productNameMap = new LinkedHashMap<String, Long>();

        for (Product product : products)
        {
            productNameMap.put(product.getKey().getProductName(), product.getPosition());
        }
        return productNameMap;
    }

    private List<String> getToProductSpecNameList(Lot lotData) throws CustomException
    {
        String sql = "SELECT F.TOPRODUCTSPECNAME FROM TPPOLICY T, POSFACTORYRELATION F WHERE T.CONDITIONID = F.CONDITIONID AND T.PRODUCTSPECNAME = ?";
        Object[] bindSet = new Object[] {lotData.getProductSpecName()};
        List<String> toProductSpecNameList = new ArrayList<String>();

        try
        {
            List<Map<String, String>> sqlList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
            if (sqlList.size() > 0)
            {
                for (int i=0; i<sqlList.size(); i++)
                {
                    toProductSpecNameList.add(sqlList.get(i).get("TOPRODUCTSPECNAME"));
                }
            }
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("SYS-9999", "ToProductSpecName", fe.getMessage());
        }
        return toProductSpecNameList;
    }

    private String slotMapTransfer(String slotMap) throws CustomException
    {
        StringBuffer slotMapTemp = new StringBuffer();

        for(int i = 0; i<slotMap.length(); i++)
        {
            slotMapTemp.append("N");
        }

        for(int i = 0; i<slotMap.length(); i++)
        {
            if(String.valueOf(slotMap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
                slotMapTemp.append("Y");
        }

        return slotMapTemp.toString();
    }

    //Get realOperationName at OLED
    private String getRealOperationName(String factoryName, String productSpecName, String ecCode, String processFlowName, String machineName, String carrierName )throws CustomException
    {
        String processOperationName = "" ;

        //String strSql = "SELECT DISTINCT PROCESSOPERATIONNAME " +
        //      "  FROM TPEFOMPOLICY " +
        //      " WHERE     FACTORYNAME = :FACTORYNAME " +
        //      "       AND PRODUCTSPECNAME = :PRODUCTSPECNAME " +
        //      "       AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
        //      "       AND ECCODE = :ECCODE " +
        //      "       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
        //      "       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
        //      "       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
        //      "       AND MACHINENAME = :MACHINENAME " ;

        String strSql= StringUtil.EMPTY;
        strSql = strSql + "  SELECT DISTINCT PROCESSOPERATIONNAME                                                                 \n";
        strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
        strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                           \n";
        strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
        strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*'))                                     \n";
        strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = '*'))                         \n";
        strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = '*'))                \n";
        strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = '*'))                                                    \n";
        strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = '*'))                         \n";
        strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = '*'))                \n";
        //strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = '*'))          \n";
        strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = '*')) \n";
        strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = '*'))                                     \n";
        strSql = strSql + " ORDER BY DECODE (FACTORYNAME, '*', 9999, 0),                                                          \n";
        strSql = strSql + "          DECODE (PRODUCTSPECNAME, '*', 9999, 0),                                                      \n";
        strSql = strSql + "          DECODE (PRODUCTSPECVERSION, '*', 9999, 0),                                                   \n";
        strSql = strSql + "          DECODE (ECCODE, '*', 9999, 0),                                                               \n";
        strSql = strSql + "          DECODE (PROCESSFLOWNAME, '*', 9999, 0),                                                      \n";
        strSql = strSql + "          DECODE (PROCESSFLOWVERSION, '*', 9999, 0),                                                   \n";
        strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, '*', 9999, 0),                                                 \n";
        strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0),                                              \n";
        strSql = strSql + "          DECODE (MACHINENAME, '*', 9999, 0)                                                           \n";
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("FACTORYNAME", factoryName);
        bindMap.put("PRODUCTSPECNAME", productSpecName);
        bindMap.put("PRODUCTSPECVERSION", "00001");
        bindMap.put("ECCODE", ecCode);
        bindMap.put("PROCESSFLOWNAME", processFlowName);
        bindMap.put("PROCESSFLOWVERSION", "00001");
        bindMap.put("PROCESSOPERATIONVERSION", "00001");
        bindMap.put("MACHINENAME", machineName);

        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

        if ( tpefomPolicyData.size() != 1 )
        {
            throw new CustomException("POLICY-0001", carrierName);
        }
        else
        {
            processOperationName = (String) tpefomPolicyData.get(0).get("PROCESSOPERATIONNAME");
        }

        return processOperationName ;
    }
    
    /**
     * 
     * @Name     getRealOperationNameList
     * @since    2018. 10. 11.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @return
     * @throws CustomException
     */
    public List<Map<String, Object>> getRealOperationNameList(String machineName )throws CustomException
    {
        String strSql = "SELECT MQ.MACHINENAME,  " +
                        "       OS.PROCESSOPERATIONNAME, " +
                        "       MQ.OPERATIONPRIORITY " +
                        "  FROM CT_MACHINEGROUPMACHINE MGM, " +
                        "       PROCESSOPERATIONSPEC OS, " +
                        "       ( " +
                        "        SELECT M.MACHINENAME,  " +
                        "               SQ.PROCESSOPERATIONNAME,  " +
                        "               SQ.OPERATIONMODE,  " +
                        "               SQ.OPERATIONPRIORITY " +
                        "          FROM MACHINE M, " +
                        "             ( " +
                        "              SELECT TP.CONDITIONID, " +
                        "                     TP.FACTORYNAME, " +
                        "                     TP.PROCESSOPERATIONNAME, " +
                        "                     TP.PROCESSOPERATIONVERSION, " +
                        "                     PS.MACHINENAME, " +
                        "                     PS.OPERATIONMODE, " +
                        "                     PS.DESCRIPTION, " +
                        "                     PS.OPERATIONPRIORITY " +
                        "                FROM POSOPERATIONMODE PS, " +
                        "                     TOPOLICY TP " +
                        "               WHERE 1=1 " +
                        "                 AND PS.CONDITIONID = TP.CONDITIONID     " +
                        "                 ) SQ " +
                        "        WHERE 1=1 " +
                        "           AND M.MACHINENAME = :MACHINENAME " +
                        "           AND M.MACHINENAME = SQ.MACHINENAME " +
                        "           AND M.OPERATIONMODE = SQ.OPERATIONMODE " +
                        "           ) MQ " +
                        "WHERE 1=1 " +
                        "  AND MGM.MACHINENAME = MQ.MACHINENAME " +
                        "  AND MGM.MACHINEGROUPNAME = OS.MACHINEGROUPNAME " +
                        "  AND MQ.PROCESSOPERATIONNAME = OS.PROCESSOPERATIONNAME " +
                        " ORDER BY MQ.OPERATIONPRIORITY  " ;
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("MACHINENAME", machineName);
        
        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        if(tpefomPolicyData.size() <= 0)
            throw new CustomException("POLICY-0022","");
        
        return tpefomPolicyData ;
    }
    
    // 2019.06.12_hsryu_Insert Logic. Mantis 0003959.
    private void MemorySlotSelNoteForSampling(Lot lotData, String slotSel, EventInfo eventInfo) throws CustomException 
    {
        //base flow info
        ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

        //waiting step
        ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

        /* 20190312, hhlee, add Check Validation */
        //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
        /* 20190522, hhlee, modify, add check validation SamplingFlow */
        //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
        //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
        //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
        if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
        {
            EventInfo eventInfoForSlotSelNote = EventInfoUtil.makeEventInfo("SlotSelChange", getEventUser(), "", null, null);
            eventInfoForSlotSelNote.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            eventInfoForSlotSelNote.setEventTime(eventInfo.getEventTime());
            
        	ArrayList<String> sampleProductName = new ArrayList<String>();

    		//avail Product list
            List<Product> productList = null;
            try
            {
                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            }
            catch (Exception ex){
            }
    				
    		if(productList != null && productList.size() > 0)
    		{
    			String slotNum = "";
        		String strSlotAndProductName = "SamplingSlot = ";
        		
    			for(int i=0; i<slotSel.length(); i++){
    				if(String.valueOf(slotSel.charAt(i)).equals(GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT)){
    					for(int j=0; j<=productList.size(); j++)
    					{
    						Product product = productList.get(j);

    						if((i+1)==(product.getPosition()))
    						{
    							sampleProductName.add(product.getKey().getProductName());
    							strSlotAndProductName += "[" + String.valueOf(i+1) + "," + product.getKey().getProductName() + "]";
    							
        						if(StringUtil.isEmpty(slotNum))
        						{
        							slotNum = String.valueOf(i+1);
        						}
        						else
        						{
        							slotNum = slotNum + "," + String.valueOf(i+1);
        						}

    							break;
    						}
    					}
    				}
    			}
    			
    			try {
                    this.checkManualSamplingAndSetNote(slotNum, lotData.getKey().getLotName(), eventInfoForSlotSelNote);
    			}
    			catch(Throwable e){
    				eventLog.error("Fail Memory ReserveSampleSlot Note.");
    			}
    			
    			Map<String, String> updateUdfs = new HashMap<String, String>();
    			updateUdfs.put("NOTE", "");
    			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
    		}
        }
    }
    
    // 20200901 mgkang DoubleRun Check
    private void checkDoubleRun (String productName, String processFlowName, String processOperationName)throws CustomException
    {
    	eventLog.info("Start Function checkDoubleRun");
    	
    	String doubleRunCheckSql = "SELECT PRODUCTNAME FROM CT_PRODUCTPROCESSOPERATION "
    			   + " WHERE 1=1 "
    			   + " AND PRODUCTNAME = :PRODUCTNAME "
    			   + " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
    			   + " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ";
    	
    	Map<String, Object> doubleRunCheckBindSet = new HashMap<String, Object>();
    	doubleRunCheckBindSet.put("PRODUCTNAME", productName);
    	doubleRunCheckBindSet.put("PROCESSFLOWNAME", processFlowName);
    	doubleRunCheckBindSet.put("PROCESSOPERATIONNAME", processOperationName);
		
		List<Map<String, Object>> doubleRunCheckResult = GenericServiceProxy.getSqlMesTemplate().queryForList(doubleRunCheckSql, doubleRunCheckBindSet);

		if ( doubleRunCheckResult.size() > 0 )
		{
			throw new CustomException("PRODUCT-0053", productName, processFlowName, processOperationName); 			
		}    			   
    }
    
    
    
    // 2019.06.12_hsryu_Insert Logic. Mantis 0003959.
    private void checkManualSamplingAndSetNote(String slotNum, String lotName, EventInfo eventInfo) throws CustomException {
    	
    	eventLog.info("Start Function checkManualSamplingAndSetNode");
    	
    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    	
    	String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;
		
		if(count > 1) {

			Map<String, String> beforeFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-2]);

			String beforeFlowName = beforeFlowMap.get("PROCESSFLOWNAME");
			String beforeFlowVersion = beforeFlowMap.get("PROCESSFLOWVERSION");
			String beforeOperationName = beforeFlowMap.get("PROCESSOPERATIONNAME");

			String sampleLotSql = "SELECT LASTEVENTUSER, SAMPLEPOSITION, TYPE, MANUALSAMPLEFLAG "
					//------------------------------------------------------------------------------------------------
					// Search LotAction Sampling
					+ " FROM (SELECT LA.LASTEVENTUSER, LA.SAMPLEPOSITION, :TYPE1 TYPE, :MANUALSAMPLEFLAG MANUALSAMPLEFLAG "
					+ "         FROM CT_LOTACTION LA "
					+ "        WHERE 1 = 1 "
					+ "          AND LA.LOTNAME = :LOTNAME "
					+ "          AND LA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND LA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "          AND LA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
					+ "          AND LA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
					+ "          AND LA.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND LA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND LA.ACTIONSTATE <> :SAMPLESTATE "
					+ "          AND LA.ACTIONNAME = :ACTIONNAME "
					+ "       UNION "
					//------------------------------------------------------------------------------------------------
					// Search CountSampling
					+ "       SELECT SL.LASTEVENTUSER, SL.SYSTEMSAMPLEPOSITION AS SAMPLEPOSITION, :TYPE2 TYPE, SL.MANUALSAMPLEPOSITION "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = SL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = SL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = SL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = SL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION "
					+ "          AND SLC.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.FROMPROCESSOPERATIONNAME = SL.FROMPROCESSOPERATIONNAME "
					+ "          AND SL.LOTNAME = :LOTNAME "
					+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND SL.ECCODE = :ECCODE "
					+ "          AND SL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME"
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND (SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION OR SL.SAMPLEPROCESSFLOWVERSION = :STAR) "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE <> :SAMPLESTATE "
					+ "          AND SL.MANUALSAMPLEFLAG <> :MANUALSAMPLEFLAG "
					//------------------------------------------------------------------------------------------------
					// Search ForceSampling
					+ "       UNION "
					+ "       SELECT SL.LASTEVENTUSER, SL.MANUALSAMPLEPOSITION AS SAMPLEPOSITION, :TYPE4 TYPE, SL.MANUALSAMPLEPOSITION "
					+ "         FROM CT_SAMPLELOT SL "
					+ "        WHERE 1 = 1 "
					+ "          AND SL.LOTNAME = :LOTNAME "
					+ "          AND SL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND SL.ECCODE = :ECCODE "
					+ "          AND SL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION "
					+ "          AND SL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION "
					+ "          AND SL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND SL.SAMPLESTATE <> :SAMPLESTATE "
					+ "          AND SL.MANUALSAMPLEFLAG = :MANUALSAMPLEFLAG "
					//------------------------------------------------------------------------------------------------
					// Search CorresSampling
					+ "       UNION "
					+ "       SELECT CSL.LASTEVENTUSER, CSL.SYSTEMSAMPLEPOSITION AS SAMPLEPOSITION, :TYPE3 TYPE, CSL.MANUALSAMPLEPOSITION "
					+ "         FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL "
					+ "        WHERE 1 = 1 "
					+ "          AND SLC.FACTORYNAME = CSL.FACTORYNAME "
					+ "          AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME "
					+ "          AND SLC.ECCODE = CSL.ECCODE "
					+ "          AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME "
					+ "          AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME "
					+ "          AND SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION "
					+ "          AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME "
					+ "          AND SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION "
					+ "          AND CSL.LOTNAME = :LOTNAME "
					+ "          AND CSL.PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "          AND CSL.ECCODE = :ECCODE "
					+ "          AND CSL.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "          AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME "
					+ "          AND (CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR CSL.FROMPROCESSOPERATIONVERSION = :STAR) "
					+ "          AND CSL.SAMPLEPROCESSFLOWNAME = :SAMPLEPROCESSFLOWNAME "
					+ "          AND (CSL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION OR CSL.SAMPLEPROCESSFLOWVERSION = :STAR) "
					+ "          AND CSL.SAMPLEFLAG = :SAMPLEFLAG "
					+ "          AND CSL.SAMPLESTATE <> :SAMPLESTATE) "
					+ "  ORDER BY DECODE(TYPE, :TYPE4, 0, :TYPE1, 1,2) ";

			Map<String, Object> smapleLotBindSet = new HashMap<String, Object>();
			smapleLotBindSet.put("LOTNAME", lotData.getKey().getLotName());
			smapleLotBindSet.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			smapleLotBindSet.put("ECCODE", lotData.getUdfs().get("ECCODE"));
			smapleLotBindSet.put("PROCESSFLOWNAME", beforeFlowName);
			smapleLotBindSet.put("PROCESSFLOWVERSION", beforeFlowVersion);
			smapleLotBindSet.put("PROCESSOPERATIONNAME", beforeOperationName);
			smapleLotBindSet.put("PROCESSOPERATIONVERSION", "00001");
			//smapleLotBindSet.put("ACTIONSTATE", "Created");
			smapleLotBindSet.put("ACTIONNAME", "Sampling");
			smapleLotBindSet.put("FROMPROCESSOPERATIONNAME", beforeOperationName);
			smapleLotBindSet.put("FROMPROCESSOPERATIONVERSION", "00001");
			smapleLotBindSet.put("SAMPLEPROCESSFLOWNAME", lotData.getProcessFlowName());
			smapleLotBindSet.put("SAMPLEPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			smapleLotBindSet.put("SAMPLEFLAG", "Y");
			//smapleLotBindSet.put("SAMPLESTATE", "Decided");
			smapleLotBindSet.put("SAMPLESTATE", "Completed");
			smapleLotBindSet.put("TYPE1", "RESERVE");
			smapleLotBindSet.put("TYPE2", "AUTO");
			smapleLotBindSet.put("TYPE3", "CORRES");
			smapleLotBindSet.put("TYPE4", "FORCE");
			smapleLotBindSet.put("MANUALSAMPLEFLAG", "ForceSampling");
			smapleLotBindSet.put("STAR", "*");			

			List<Map<String, Object>> sampleLotSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sampleLotSql, smapleLotBindSet);

			String slotNote = "ReserveSamplingBy:{%s} ReserveSlot:{%s}" ;
			
			if ( sampleLotSqlResult.size() > 0 )
			{
				String eventUser = "";
				String actualSlotNum = "";
				String type = "";

	 			if(sampleLotSqlResult.size() == 1)
				{
					eventUser = sampleLotSqlResult.get(0).get("LASTEVENTUSER").toString();
					actualSlotNum = sampleLotSqlResult.get(0).get("SAMPLEPOSITION").toString();
					type = sampleLotSqlResult.get(0).get("TYPE").toString();

	 				if(StringUtils.equals(type, "FORCE") || StringUtils.equals(type, "RESERVE")) {
	 					// Format #1 : ReserveSamplingBy:{%s} ReserveSlot:{%s} && ActualSlot:{%s}
	 					eventInfo.setEventComment(String.format(slotNote, eventUser, actualSlotNum) + String.format(" && ActualSlot{%s}", slotNum));
					}
				}
				else
				{
					boolean existReserveFlag = false;
					
					for( int i=0; i < sampleLotSqlResult.size(); i++ )
					{
						type = sampleLotSqlResult.get(i).get("TYPE").toString();

						if(StringUtils.equals(type, "FORCE") || StringUtils.equals(type, "RESERVE")) 
						{
							existReserveFlag = true;
							eventUser = sampleLotSqlResult.get(i).get("LASTEVENTUSER").toString();
							actualSlotNum = sampleLotSqlResult.get(i).get("SAMPLEPOSITION").toString();
							
							break;
						}
					}
					
					if( existReserveFlag )
					{
						boolean existSystemSampleFlag = false;

						for( int i=0; i < sampleLotSqlResult.size(); i++ )
						{
							type = sampleLotSqlResult.get(i).get("TYPE").toString();
							
							if(StringUtils.equals(type, "AUTO") || StringUtils.equals(type, "CORRES"))
							{
								existSystemSampleFlag = true;
								String systemSlotNum = sampleLotSqlResult.get(i).get("SAMPLEPOSITION").toString();
 
			 					// Format #2 : ReserveSamplingBy:{%s} ReserveSlot:{%s} && SystemSlot{%s} && ActualSlot:{%s}
			 					eventInfo.setEventComment(String.format(slotNote, eventUser, actualSlotNum)
			 							+ String.format(" && SystemSlot{%s}", systemSlotNum)
			 							+ String.format(" && ActualSlot{%s}", slotNum));
							}
						}
						
						// if ReserveSampling & ForceSampling exist. 
						if(!existSystemSampleFlag) {
		 					// Format #1 : ReserveSamplingBy:{%s} ReserveSlot:{%s} && ActualSlot:{%s}
		 					eventInfo.setEventComment(String.format(slotNote, eventUser, actualSlotNum) + String.format(" && ActualSlot{%s}", slotNum));
						}
					}
				}
	 			
	 			// if eventComment is Empty , Not ReserveSampling. 
	 			if(StringUtils.isNotEmpty(eventInfo.getEventComment())) {
	    			SetEventInfo setEventInfo = new SetEventInfo();
	    			setEventInfo.getUdfs().put("NOTE", eventInfo.getEventComment());
	    			
	    			eventLog.info("if eventComment is Empty , Not ReserveSampling");
	    			lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	 			}
			}
		}
		
		eventLog.info("End Function checkManualSamplingAndSetNode");
    }

    /**
     * @Name     setLotInfoDownLoadSendProcessList
     * @since    2018. 6. 1.
     * @author   hhlee
     * @contents Set LotInfoDownLoadSend Process List
     * @param processlistElement
     * @param factoryName
     * @param productName
     * @return
     * @throws CustomException
     */
    private Element setLotInfoDownLoadSendProcessList(String factoryName, String productName) throws CustomException
    {
        Element processListElement = new Element("PROCESSLIST");
        try
        {
            //String strSql = StringUtil.EMPTY;
            //strSql = strSql + " SELECT SQ.TIMEKEY AS TIMEKEY,                                   \n";
            //strSql = strSql + "        SQ.PRODUCTNAME AS PRODUCTNAME,                           \n";
            //strSql = strSql + "        SQ.PROCESSFLOWNAME AS PROCESSFLOWNAME,                   \n";
            //strSql = strSql + "        SQ.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME,         \n";
            //strSql = strSql + "        SQ.MACHINENAME AS MACHINENAME                            \n";
            //strSql = strSql + "   FROM (                                                        \n";
            //strSql = strSql + "         SELECT /*+ INDEX_DESC(PRODUCTHISTORY_PK) */             \n";
            //strSql = strSql + "                PH.TIMEKEY AS TIMEKEY,                           \n";
            //strSql = strSql + "                PH.PRODUCTNAME AS PRODUCTNAME,                   \n";
            //strSql = strSql + "                PF.PROCESSFLOWNAME AS PROCESSFLOWNAME,           \n";
            //strSql = strSql + "                PH.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, \n";
            //strSql = strSql + "                PH.MACHINENAME AS MACHINENAME                    \n";
            //strSql = strSql + "           FROM PRODUCTHISTORY PH, PROCESSFLOW PF                \n";
            //strSql = strSql + "          WHERE 1=1                                              \n";
            //strSql = strSql + "            AND PH.PRODUCTNAME = :PRODUCTNAME                    \n";
            //strSql = strSql + "            AND PH.MACHINENAME IS NOT NULL                       \n";
            //strSql = strSql + "            AND PH.PROCESSFLOWNAME = PF.PROCESSFLOWNAME          \n";
            //strSql = strSql + "            AND PF.PROCESSFLOWTYPE = :PROCESSFLOWTYPE            \n";
            //strSql = strSql + "            AND PF.FACTORYNAME = :FACTORYNAME                    \n";
            //strSql = strSql + "           ORDER BY PH.TIMEKEY DESC                              \n";
            //strSql = strSql + "         )SQ                                                     \n";
            //strSql = strSql + " WHERE 1=1                                                       \n";
            //strSql = strSql + "   AND ROWNUM <= 5                                               \n";

            String strSql = StringUtil.EMPTY;
            strSql = strSql + " SELECT MQ.PRODUCTNAME,                                                                 \n";
            strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,1), ' ') AS PROCESSMACHINENAME,  \n";
            strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,2), ' ') AS PROCESSOPERATIONNAME \n";
            strSql = strSql + "   FROM (                                                                               \n";
            strSql = strSql + "         SELECT T.NO, SQ.PRODUCTNAME,                                                   \n";
            strSql = strSql + "                (CASE WHEN T.NO = 1 THEN SQ.AT1                                         \n";
            strSql = strSql + "                      WHEN T.NO = 2 THEN SQ.AT2                                         \n";
            strSql = strSql + "                      WHEN T.NO = 3 THEN SQ.AT3                                         \n";
            strSql = strSql + "                      WHEN T.NO = 4 THEN SQ.AT4                                         \n";
            strSql = strSql + "                      WHEN T.NO = 5 THEN SQ.AT5                                         \n";
            strSql = strSql + "                      END) AS PROCESSOPERATION                                          \n";
            strSql = strSql + "           FROM (                                                                       \n";
            strSql = strSql + "                 SELECT PO.PRODUCTNAME AS PRODUCTNAME,                                  \n";
            strSql = strSql + "                        PO.ATTRIBUTE1 AS AT1,                                           \n";
            strSql = strSql + "                        PO.ATTRIBUTE2 AS AT2,                                           \n";
            strSql = strSql + "                        PO.ATTRIBUTE3 AS AT3,                                           \n";
            strSql = strSql + "                        PO.ATTRIBUTE4 AS AT4,                                           \n";
            strSql = strSql + "                        PO.ATTRIBUTE5 AS AT5                                            \n";
            strSql = strSql + "                   FROM CT_PROCESSEDOPERATION PO                                        \n";
            strSql = strSql + "                  WHERE 1=1                                                             \n";
            strSql = strSql + "                    AND PO.PRODUCTNAME = :PRODUCTNAME                                   \n";
            strSql = strSql + "                 )SQ,                                                                   \n";
            strSql = strSql + "                 (SELECT LEVEL NO                                                       \n";
            strSql = strSql + "                    FROM DUAL CONNECT BY LEVEL <= 5) T                                  \n";
            strSql = strSql + "         )MQ                                                                            \n";
            strSql = strSql + "  ORDER BY MQ.NO                                                                        \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("PRODUCTNAME", productName);
            //bindMap.put("FACTORYNAME", factoryName);
            //bindMap.put("PROCESSFLOWTYPE", GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN);

            List<Map<String, Object>> ProcessOperationData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

            if ( ProcessOperationData.size() > 0 )
            {
                for(int i = 0; i < ProcessOperationData.size(); i++ )
                {
                    /* If you have to send only what has a value, loosen the IF statement below. */
                    //if (!ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim().isEmpty())
                    {
                        Element processElement = new Element("PROCESS");
    
                        Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
                        processOperationNameLElement.setText(ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim());
                        processElement.addContent(processOperationNameLElement);
    
                        Element processMachineNameElement = new Element("PROCESSMACHINENAME");
                        processMachineNameElement.setText(ProcessOperationData.get(i).get("PROCESSMACHINENAME").toString().trim());
                        processElement.addContent(processMachineNameElement);
    
                        processListElement.addContent(processElement);
                    }
                }
            }
            else
            {
            }
        }
        catch (Exception ex)
        {
            eventLog.warn("[setLotInfoDownLoadSendProcessList] Data Query Failed");;
        }

        return processListElement;
    }   
}

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
// 20190612, backup
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//package kr.co.aim.messolution.lot.event;
//
//import java.math.BigDecimal;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import kr.co.aim.messolution.durable.MESDurableServiceProxy;
//import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
//import kr.co.aim.messolution.extended.object.management.data.ExposureFeedBack;
//import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
//import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
//import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.CommonValidation;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.PolicyUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.lot.MESLotServiceProxy;
//import kr.co.aim.messolution.lot.event.CNX.AssignWorkOrder;
//import kr.co.aim.messolution.lot.event.CNX.AssignWorkOrderV2;
//import kr.co.aim.messolution.machine.MESMachineServiceProxy;
//import kr.co.aim.messolution.port.MESPortServiceProxy;
//import kr.co.aim.messolution.product.MESProductServiceProxy;
//import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
//import kr.co.aim.greenframe.greenFrameServiceProxy;
//import kr.co.aim.greenframe.util.support.InvokeUtils;
//import kr.co.aim.greenframe.util.time.TimeStampUtil;
//import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
//import kr.co.aim.greentrack.durable.DurableServiceProxy;
//import kr.co.aim.greentrack.durable.management.data.Durable;
//import kr.co.aim.greentrack.durable.management.data.DurableSpec;
//import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
//import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
//import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.generic.util.TimeUtils;
//import kr.co.aim.greentrack.generic.util.XmlUtil;
//import kr.co.aim.greentrack.lot.LotServiceProxy;
//import kr.co.aim.greentrack.lot.management.data.Lot;
//import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
//import kr.co.aim.greentrack.machine.MachineServiceProxy;
//import kr.co.aim.greentrack.machine.management.data.Machine;
//import kr.co.aim.greentrack.machine.management.data.MachineSpec;
//import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
//import kr.co.aim.greentrack.port.management.data.Port;
//import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
//import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
//import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
//import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
//import kr.co.aim.greentrack.product.ProductServiceProxy;
//import kr.co.aim.greentrack.product.management.data.Product;
//import kr.co.aim.greentrack.product.management.data.ProductKey;
//import kr.co.aim.greentrack.product.management.data.ProductSpec;
//import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
//import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
//import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
//import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
//
//import org.apache.commons.collections.map.ListOrderedMap;
//import org.apache.commons.lang.StringUtils;
//import org.jdom.Document;
//import org.jdom.Element;
//
////import com.sun.xml.internal.ws.policy.jaxws.PolicyUtil;
//
//public class LotInfoDownloadRequestNew extends SyncHandler {
//
//    @Override
//    public Object doWorks(Document doc) throws CustomException {
//        /* 20181130, hhlee, modify, add MessageName dupulicate prevent ==>> */
//        //Document doc = (Document) recvdoc.clone();
//        /* <<== 20181130, hhlee, modify, add MessageName dupulicate prevent */
//        
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CSTInfoDownLoadSend");
//
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//        String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
//        String portType    = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
//        String portUseType  = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
//        String portAccessMode   = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
//        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//        String slotMap     = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
//
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//
//        /* 20181107, hhlee, add, try{}catch{} ==>> */     
//        Machine machineData = null;
//        MachineSpec machineSpecData = null;
//        Port portData = null;
//        Durable durableData = null;
//        
//        if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
//        {
//            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
//        }
//        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
//        {
//            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
//        }
//        else
//        {
//        }
//        
//        try
//        {
//            //existence validation
//            machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//            machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);            
//            portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);    
//            durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//            
//            /* 20190401, hhlee, add, update TransferState = 'Processing' ==>> */
//            if(!StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "PORTNAME") , portName))
//            {
//                /* 20190422, hhlee, modify, ErrorCode Description change ==>> */
//                //throw new CustomException("CST-9004", carrierName, CommonUtil.getValue(durableData.getUdfs(), "PORTNAME"), portName);
//                throw new CustomException("CST-9004", carrierName, portName);
//                /* <<== 20190422, hhlee, modify, ErrorCode Description change */
//            }
//            /* <<== 20190401, hhlee, add, update TransferState = 'Processing' */
//        }
//        catch (CustomException ce)
//        {
//            /* 20180616, hhlee, Modify ==>> */
//            eventInfo.setEventName("Hold");
//            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//            /* 20190424, hhlee, modify, changed function ==>> */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//            /* <<== 20190424, hhlee, modify, changed function */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//            return doc;
//            /* <<== 20180616, hhlee, Modify */            
//        }
//        /* <<== 20181107, hhlee, add, try{}catch{} */
//        
//        /* 20180616, hhlee, Modify ==>> */
//        try
//        {
//            //DurableSpec durableSpecData
//            //check Durable Hold State
//            CommonValidation.CheckDurableHoldState(durableData);
//    
//            //check MachineState
//            CommonValidation.checkMachineState(machineData);
//    
//            //check MachineOperationMode
//            CommonValidation.checkMachineOperationModeExistence(machineData);
//        }
//        catch (CustomException ce)
//        {            
//            eventInfo.setEventName("Hold");
//            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//            if(StringUtil.equals(ce.errorDef.getErrorCode(), "CST-0005") || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0008")
//                    || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0011"))
//            {
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                //String reasonCodeType, String reasonCode, String eventComment, String errorComment
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//            }
//            else
//            {
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//            }                
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//            return doc;
//        }
//        /* <<== 20180616, hhlee, Modify */
//        
//        //cleaning EQP
//        // Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
////        if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CCLN"))
//        if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
//        {
//            return downloadCarrierJob(doc, slotMap, durableData, machineData,portName,portType,portUseType, machineSpecData.getDefaultRecipeNameSpaceName());
//        }
//
//        boolean isInitialInput = CommonUtil.isInitialInput(machineName);
//        Lot lotData = null;
//
//        /* 20181025, hhlee, delete, Don't use any more ==>> */
//        //try
//        //{
//        //    if(!isInitialInput)
//        //    {
//        //        //lotData = this.getLotData(carrierName);
//        //        lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//        //                                        
//        //        /* 20180930, Add, Operation Validation ==>> */
//        //        //String machineGroup = machineData.getMachineGroupName();
//        //        ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//        //        String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
//        //        String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
//        //    }
//        //}
//        //catch (CustomException ce)
//        //{                
//        //    eventInfo.setEventName("Hold");
//        //    eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());           
//        //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//        //    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");        
//        //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//        //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//        //    return doc;
//        //}
//        /* <<== 20180930, Add, Operation Validation */
//        /* <<== 20181025, hhlee, delete, Don't use any more */
//        
//        //Machine UPK
//        if(isInitialInput)
//        {
//            /* 20190125, hhlee, delete, PU Port and UNPACKER PU Port are treated with the same logic ==>> */
//            ///* 20180619, Move Position ==>> */
//            //this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//            //        durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//            ///* <<== 20180920, Move Position */
//            /* <<== 20190125, hhlee, delete, PU Port and UNPACKER PU Port are treated with the same logic */
//            
//            //Unloader
//            if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                eventLog.info("Unpacker unloader Port job download");
//
//                /* 20190125, hhlee, delete, PU Port and UNPACKER PU Port are treated with the same logic ==>> */
//                ///* 20180920, Move Position Delete ==>> */
//                /////* 20180619, Move Position ==>> */
//                //////String SlotMapLogic =
//                //////appending item NEW
//                ////this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                ////        durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//                /////* <<== 20180619, Move Position */
//                ///* <<== 20180920, Move Position Delete */
//                //
//                //CommonValidation.checkEmptyCst(carrierName);
//                //
//                ///* 20180712, Add , Empty CST validation ==>> */
//                //lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//                //if (lotData != null)
//                //{
//                //    throw new CustomException("CST-0006", carrierName);
//                //}
//                ///* <<== 20180712, Add , Empty CST validation */
//                //
//                ///* 20190124, hhlee, add empty cassette validation ==>> */
//                ////slot map validation
//                //if (StringUtil.isNotEmpty(slotMap))
//                //{
//                //    /* 20181214, hhlee, modify, ==>> */
//                //    //CommonValidation.checkCstSlot(slotMap);
//                //    CommonValidation.checkCstSlot(carrierName, slotMap);
//                //    /* 20181214, hhlee, modify, ==>> */
//                //}
//                ///* <<== 20190124, hhlee, add empty cassette validation */
//                //
//                ////String machineRecipeName = "";
//                //
//                ///* 20180619, Delete, Move Position ==>> */
//                //////String SlotMapLogic =
//                //////appending item NEW
//                ////this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                ////      durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//                ///* <<== 20180619, Delete, Move Position */
//                //
//                ///* 20181011, hhlee, delete, Dead Code ==>> */
//                ////SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", StringUtil.EMPTY);
//                ///* <<== 20181011, hhlee, delete, Dead Code */
//                /* <<== 20190125, hhlee, delete, PU Port and UNPACKER PU Port are treated with the same logic */
//            }
//            else
//            {
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PORT-1001");
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Port[%s] Type NotMismatch !" , portData.getKey().getPortName() + "-" + CommonUtil.getValue(portData.getUdfs(), "PORTTYPE")));
//                return doc;
//            }
//        }
//        
//        /* 20190605, hhlee, add Sorter transfer direction validation ==>> */
//        String transferDirection = StringUtil.EMPTY;
//        /* <<== 20190605, hhlee, add Sorter transfer direction validation */
//        
//        /* 20190125, hhlee, change, PU Port and UNPACKER PU Port are treated with the same logic */
//        //else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
//        if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") ||
//                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
//                CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO")) 
//        {
//            
//            /* 20181107, hhlee, delete, move change logic position ==>> */
//            ////lotData = this.getLotData(carrierName);
//            //lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//            //
//            //if (lotData == null)
//            //{
//            //    throw new CustomException("LOT-0054", carrierName);
//            //}
//            /* <<== 20181107, hhlee, delete, move change logic position */
//            
//            /* 20180930, Add, Operation Validation ==>> */
//            /* 20181031, Add, MachineGroup Validation Logic Move ==>> */
//            ////String machineGroup = machineData.getMachineGroupName();
//            //ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//            //String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
//            //String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
//            /* <<== 20181031, Add, MachineGroup Validation Logic Move */
//            
//            //PB,PL Port
//            
//            /* 20180619, Move Position ==>> */
//            //Body generate
//            this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                  durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//            /* <<== 20180619, Move Position */
//            
//            eventLog.info("Loader Port job download");          
//            
//            /* 20181011, hhlee, add, Valiable Location Change ==>> */
//            String machineRecipeName = StringUtil.EMPTY;
//            /* <<== 20181011, hhlee, add, Valiable Location Change */
//            
//            /* 20181020, hhlee, add, Validate ProdutList ==>> */
//            List<Product> productDataList = null;
//            ProcessFlow processFlowData = null;
//            /* <<== 20181020, hhlee, add, Validate ProdutList */
//            
//            /* 20181107, hhlee, add, move change logic position ==>> */
//            List<Lot> lotList = null;
//            //slot map validation
//            String logicalSlotMap = StringUtil.EMPTY;
//            /* <<== 20181107, hhlee, add, move change logic position */
//            
//            /* 20180616, hhlee, Modify ==>> */
//            try
//            {
//                /* 20181107, hhlee, add, move change logic position ==>> */
//                //lotData = this.getLotData(carrierName);
//                lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//                
//                if (lotData == null)
//                {
//                    throw new CustomException("LOT-0054", carrierName);
//                }
//                /* <<== 20181107, hhlee, add, move change logic position */
//                
//                /* 20181220, hhlee, add, LotGrade Validation ==>> */
//                if(StringUtil.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
//                {
//                    throw new CustomException("LOT-9043", carrierName, lotData.getKey().getLotName());
//                }
//                /* <<== 20181220, hhlee, add, LotGrade Validation */
//                
//                /* 20181031, Add, MachineGroup Validation Logic Move ==>> */
//                //String machineGroup = machineData.getMachineGroupName();
//                ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
//                String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
//                /* <<== 20181031, Add, MachineGroup Validation Logic Move */
//                
//                /* 20181010, hhlee, delete, Dead Code ==>> */
//                ////Lot validation
//                //if (lotData == null)
//                //{
//                //    /* 20180616, hhlee, Modify ==>> */
//                //    throw new CustomException("LOT-0054", carrierName);
//                //    //eventInfo.setEventName("Hold");
//                //    //eventInfo.setEventComment(String.format("No Lot in Carrier[%s]", carrierName));
//                //    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                //    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                //    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "LOT-0054");
//                //    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("No Lot in Carrier[%s]", carrierName));
//                //    //return doc;
//                //    /* <<== 20180616, hhlee, Modify */
//                //}
//                /* <<== 20181010, hhlee, delete, Dead Code */
//                
//                //check LotState/ProcessState/HoldLotState
//                CommonValidation.checkLotState(lotData);
//                CommonValidation.checkLotProcessState(lotData);
//                CommonValidation.checkLotHoldState(lotData);
//                
//                /* 20181020, hhlee, add, Validate ProdutList ==>> */
//                //available Product list
//                try
//                {
//                    productDataList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//                }
//                catch (Exception ex)
//                {
//                    throw new CustomException("SYS-9999", "Product", "No Product to process");
//                }            
//                /* <<== 20181020, hhlee, add, Validate ProdutList */
//                
//                try
//                {
//                    /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
//                    //ProcessFlow Validation
//                    ProcessFlowKey processFlowKey = new ProcessFlowKey();
//                    processFlowKey.setFactoryName(lotData.getFactoryName());
//                    processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
//                    processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
//                    processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
//                }
//                catch (Exception ex)
//                {
//                    throw new CustomException("SYS-9999", "Product", "No ProcessFlowData to process");
//                }
//                
//                //20180504, kyjung, QTime
//                MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());    
//                
//                //20180525, kyjung, MQC
//                MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
//                
//                /* 20181025, hhlee, add, Check MQC Machine ==>> */
//                if(StringUtils.equals(processFlowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
//                {
//                    if(StringUtil.isEmpty(MESProductServiceProxy.getProductServiceImpl().checkMQCMachine(lotData, machineName)))
//                    {
//                        throw new CustomException("MQC-0049", machineName, StringUtil.EMPTY);
//                    }
//                }               
//                /* <<== 20181025, hhlee, add, Check MQC Machine */
//                
//                /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                //Recipe
//                //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                //                                                                lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//                //
//                //// 20180612, kyjung, Recipe Idle Time
//                //MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(machineName, machineRecipeName, lotData.getProductSpecName(), lotData.getProcessOperationName());
//                //
//                ///* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                ////Added by jjyoo on 2018.9.20 - Check Inhibit Condition
//                ////this logic must be existed the end of LotInfoDownloadRequestNew.
//                //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
//                //        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N);
//                /* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                /* <<== 20181011, hhlee, Delete (Move Postion) */
//                
//                /* 20181107, hhlee, add, move change logic position ==>> */
//                //getLotData
//                lotList = CommonUtil.getLotListByCarrier(carrierName, true);
//                //slot map validation
//                logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//                /* <<== 20181107, hhlee, add, move change logic position */
//            }
//            catch (CustomException ce)
//            {               
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                /* 20190424, hhlee, modify, add errorCode : "LOT-9046" */
//                if(StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-0016") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9015")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-0054") || StringUtil.equals(ce.errorDef.getErrorCode(), "MQC-0044")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MQC-0049") || StringUtil.equals(ce.errorDef.getErrorCode(), "QUEUE-0005")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MACHINE-0102") || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PROCESSOPERATION-9001") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9027")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9043") || StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9046"))
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                }
//                /* 20190424, hhlee, delete, change function ==>> */
//                ///* 20190220, hhlee, add, added futherHold(validation NG LotProcessState = 'RUN') */
//                //else if(StringUtil.equals(ce.errorDef.getErrorCode(), "LOT-9046"))
//                //{
//                //    MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotList.get(0), machineName, "AHOLD", "VRHL");
//                //}
//                /* <<== 20190424, hhlee, delete, change function */
//                else
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, "", " ");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
//                } 
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;
//            }
//            /* <<== 20180616, hhlee, Modify */          
//            
//            /* 20181107, hhlee, delete, move change logic position ==>> */
//            //List<Lot> lotList = CommonUtil.getLotListByCarrier(carrierName, true);
//            ////slot map validation
//            //String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//            /* <<== 20181107, hhlee, delete, move change logic position */
//            
//            try
//            {               
//                //if (StringUtil.isNotEmpty(slotMap) && !slotMap.equals(logicalSlotMap))
//                if (StringUtil.isNotEmpty(logicalSlotMap) || StringUtil.isNotEmpty(slotMap))
//                {
//                    if (!slotMap.equals(logicalSlotMap))
//                    {       
//                        throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment("Different [BCSlotMap = " + slotMap + "] , [MESSlotMap = " + logicalSlotMap + "]");
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
//                        ///* 20180601, Slot missmatch  ==>> */
//                        ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-0020");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "Different [BCSlotMap = " + slotMap + "] , [MESSlotMap = " + logicalSlotMap + "]");
//                        ///* <<== 20180601, Slot missmatch */
//                        //return doc;
//                    }
//                }
//            }
//            catch (CustomException ce)
//            {           
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;             
//            }
//            
//                        
//            /* 20181011, hhlee, Delete (Move Postion) ==>> */
//            /* hhlee, 20180616, Added TOMACHINE specified logic. ==>> */
//            String toMachineName = StringUtil.EMPTY;
//            try
//            {
////                if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
////                {
////                    //checkOperationMode
////                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE")); 
////                    
////                    if(StringUtil.isEmpty(CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME")))
////                    {
////                        throw new CustomException("PORT-9006",machineName );
////                    }
////                    
////                    String unitName = CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME");
////                    
////                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
////                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    ////if(toMachineAndOperationName.size() > 0)
////                    ////{
////                    ////    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
////                    ////}                    
////                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
////                    
////                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
////                    //String toMachineName = MESLotServiceProxy.getLotServiceUtil().getToMachineName(lotData.getProcessOperationName(),CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    //Only INDP has toMachineName
////                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationNameByUnitName(lotList, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    if(toMachineAndOperationName.size() > 0)
////                    {
////                        toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");
////                    }
////                    /* 20181031, hhlee, add, Change ToMachine is LinkedUnit ==>> */
////                    toMachineName = unitName;
////                    /* <<== 20181031, hhlee, add, Change ToMachine is LinkedUnit */
////                    
////                    /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
////                    //PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
////                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
////                    
////                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
////                    //Recipe
////                    //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
////                    //        lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
////                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName(), toMachineName, lotData.getUdfs().get("ECCODE"));
////                    /* <<== 20181011, hhlee, Delete (Move Postion) */
////                }
////                else
////                {
////                    /* 20181024, hhlee, add, Validate Normal Mode Condition ==>> */
////                    //checkOperationMode
////                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    /* <<== 20181024, hhlee, add, Validate Normal Mode Condition */
////                    
////                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
////                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, StringUtil.EMPTY, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
////                    
////                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
////                    /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
////                    PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
////                    /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
////                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
////                    
////                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
////                    //Recipe
////                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
////                    /* <<== 20181011, hhlee, Delete (Move Postion) */
////                }
//                
//                /* 20181101, hhlee, [toMachineName(Changed TOPolicy)] ==>> */
//                /* PRD 利侩 矫 困狼 IF 巩阑 阜绊 酒贰狼 IF巩狼 林籍阑 钱绊 利侩窃.*/
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
//                    
//                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
//                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //if(toMachineAndOperationName.size() > 0)
//                    //{
//                    //    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
//                    //}                    
//                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
//                    
//                    /* 20181031, hhlee, add, Change ToMachine is LinkedUnit ==>> */
//                    toMachineName = unitName;
//                    /* <<== 20181031, hhlee, add, Change ToMachine is LinkedUnit */
//                    
//                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
//                    ////String toMachineName = MESLotServiceProxy.getLotServiceUtil().getToMachineName(lotData.getProcessOperationName(),CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    ////Only INDP has toMachineName
//                    ////List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationNameByUnitName(lotList, unitName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //if(toMachineAndOperationName.size() > 0)
//                    //{
//                    //    toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");
//                    //}
//                    ///* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//                    ////PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    //PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    ///* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
//                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
//                    
//                    /* 20181101, hhlee, add, Change TPEFOMPolicy Condition(not use unitname) ==>> */
//                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                    //Recipe
//                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                            lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//                    //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                    //                                                                lotData.getProcessFlowName(), lotData.getProcessOperationName(), toMachineName, lotData.getUdfs().get("ECCODE"));
//                    /* <<== 20181011, hhlee, Delete (Move Postion) */
//                    /* <<== 20181101, hhlee, add, Change TPEFOMPolicy Condition(not use unitname) */
//                }
//                else
//                {
//                    /* 20181024, hhlee, add, Validate Normal Mode Condition ==>> */
//                    //checkOperationMode
//                    MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20181024, hhlee, add, Validate Normal Mode Condition */
//                    
//                    /* 20181024, hhlee, add, Change TOPolicy Condition ==>> */
//                    List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getMachineNameAndOperationNameByTOPolicy(lotList, machineName, StringUtil.EMPTY, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    /* <<== 20181024, hhlee, add, Change TOPolicy Condition */
//                    
//                    /* 20181024, hhlee, delete, Change TOPolicy Condition ==>> */
//                    ///* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//                    //PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//                    ///* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
//                    /* <<== 20181024, hhlee, delete, Change TOPolicy Condition */
//                    
//                    /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                    //Recipe
//                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                                                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//                    /* <<== 20181011, hhlee, Delete (Move Postion) */
//                }
//                /* PRD 利侩 矫 咯扁鳖瘤狼 IF巩狼 林籍阑 钱绊 利侩窃.*/
//                /* <<== 20181101, hhlee, [toMachineName(Changed TOPolicy)] */
//                                
//                /* 20190117, hhlee, delete , Change Check Loigc ==>> */
//                ///* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
//                ////ProcessFlow Validation
//                ////ProcessFlowKey processFlowKey = new ProcessFlowKey();
//                ////processFlowKey.setFactoryName(lotData.getFactoryName());
//                ////processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
//                ////processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
//                ////ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);                
//                //if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
//                //{
//                //    String mqcProductRecipeName = StringUtil.EMPTY;
//                //                            
//                //    for (Product productData : productDataList)
//                //    {
//                //        mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
//                //        if(StringUtil.isNotEmpty(mqcProductRecipeName))
//                //        {
//                //            machineRecipeName = mqcProductRecipeName;
//                //            break;
//                //        }
//                //    }
//                //}                
//                /* <<== 20181020, hhlee, modify, MQC RecipeName valiable */
//                /* <<== 20190117, hhlee, delete , Change Check Loigc */
//                
//                /* 20181011, hhlee, Delete (Move Postion) ==>> */
//                // 20180612, kyjung, Recipe Idle Time
//                //MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(machineName, machineRecipeName, lotData.getProductSpecName(), lotData.getProcessOperationName());
//                
//                /* 20190412, hhlee, delete, MachineIdleTimeOver validation logic(Mantis:0003497) ==>> */
//                ///* 20190115, hhlee, add, add logic Check Machine Idle Time ==>> */
//                //MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOver(machineName, lotData, portName);
//                ///* <<== 20190115, hhlee, add, add logic Check Machine Idle Time */
//                /* <<== 20190412, hhlee, delete, MachineIdleTimeOver validation logic(Mantis:0003497) */
//                
//                //2018.11.17_hsryu_check FirstLotFlag.
//                //deleted by wghuang 20190121, requested by haiying
//                //MESLotServiceProxy.getLotServiceUtil().checkFirstLotFlagByRecipeIdleTime(machineName, machineRecipeName);
//
//                /* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                //Added by jjyoo on 2018.9.20 - Check Inhibit Condition
//                //this logic must be existed the end of LotInfoDownloadRequestNew.
//                /* 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName ==>> */
//                MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
//                        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, toMachineName);
//                //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotData,  machineName,  
//                //        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_N, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), toMachineName);
//                /* <<== 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName */
//                /* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                /* <<== 20181011, hhlee, Delete (Move Postion) */
//                
//                // -----------------------------------------------------------------------------------------------------------------------------------------------------------
//            }
//            catch (CustomException ce)
//            {
//                /* 20180616, hhlee, Modify ==>> */
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;
//                /* <<== 20180616, hhlee, Modify */
//                
//            }
//            /* <<== hhlee, 20180616, Added TOMACHINE specified logic. */
//            
//            //add to MachineName
//            SMessageUtil.setBodyItemValue(doc, "TOMACHINENAME", toMachineName);
//            SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
//            SMessageUtil.setBodyItemValue(doc, "PRIORITY", String.valueOf(lotData.getPriority()));
//            /* 20190521, hhlee, modify, add validation (super lot flag=Y, Lot priority = 0) ==>> */
//            /* 20190528, hhlee, modify, add validation (super lot flag=Y, Lot priority = 1) */
//            /* Mantis: 0003939,  */
//            if(StringUtil.equals(CommonUtil.getValue(lotData.getUdfs(), "SUPERLOTFLAG"), GenericServiceProxy.getConstantMap().FLAG_Y))
//            {
//                SMessageUtil.setBodyItemValue(doc, "PRIORITY", "1");
//            }    
//            /* <<== 20190521, hhlee, modify, add validation (super lot flag=Y, Lot priority = 0) */
//            /* <<== 20181011, hhlee, Delete (Move Postion) */            
//            
//            /* 20180616, hhlee, Modify ==>> */
//            //String machineRecipeName = StringUtil.EMPTY;
//            try
//            {
//                /* 20181011, hhlee, delete, Duplicate logic ==>> */
//                ////Recipe
//                //machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                //                                                                lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//                /* <<== 20181011, hhlee, delete, Duplicate logic */
//                
//                /* 20180619, Add RMS Recipe Validation ==>> */
//                //doc = MESLotServiceProxy.getLotServiceUtil().getValidationRmsRecipeCheck(doc, eventInfo, machineSpecData, machineName, machineRecipeName, carrierName);
//                //if(SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, true) != "0")
//                //machineRecipeName = "12345678ABCD";
//                /*
//                if(!MESLotServiceProxy.getLotServiceUtil().getValidationRmsRecipeCheck(doc, eventInfo, machineSpecData, machineName, machineRecipeName, carrierName))
//                {
//                    return doc;
//                }*/
//                
//                /* <<== 20180619, Add RMS Recipe Validation */
//                
//                //ProbeCard
//                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
//                {
//                    eventLog.info("ProbeCard check Started.");
//    
//                    String pbCardType = "";
//                    String probeCardSpecName = "";
//    
//                    if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
//                    {
//                        //get one PBCardType by mahcine
//                        //pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(machineName).get(0).getDurableSpecName();
//                        pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByMachine(machineName);
//
//                        /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
//                        if(StringUtil.isEmpty(pbCardType))
//                        {
//                            throw new CustomException("PROBECARD-0008", machineName);
//                        }                            
//                        /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
//                        
//                        PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
//                        
//                        /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
//                        probeCardSpecName = pbCardType;
//                        /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
//                    }
//                    else
//                    {
//                        //INDP
//                        List<String>unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(machineName);
//    
//                        for(String unit : unitList)
//                        {
//                            //pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit).get(0).getDurableSpecName();
//                            pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListTypeByUnitName(unit);
//                            
//                            /* 20190512, hhlee, modify, add Check Mounted ProbeCard ==>> */
//                            if(StringUtil.isEmpty(pbCardType))
//                            {
//                                throw new CustomException("PROBECARD-0008", machineName);
//                            }                            
//                            /* <<== 20190512, hhlee, modify, add Check Mounted ProbeCard */
//                            
//                            PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
//                            
//                            /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
//                            if(StringUtil.equals(unit, toMachineName))
//                            {
//                                probeCardSpecName = pbCardType;
//                            }
//                            /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
//                        }
//                    }
//    
//                    eventLog.info("ProbeCard check Ended.");
//                    
//                    /* 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" ==>> */
//                    SMessageUtil.setBodyItemValue(doc, "PROBECARDNAME", probeCardSpecName);
//                    /* <<== 20181211, hhlee, add, Set Message Item PROBECARDNAME = "PROBECARDSPECNAME" */
//                }
//    
//                //PhotoMask
//                if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_PHOTO))
//                {
//                    eventLog.info("PhotoMask check Started.");
//    
//                    //also check mask state
//                    List<Durable> PhotoMaskList = MESDurableServiceProxy.getDurableServiceUtil().getPhotoMaskNameByMachineName(machineName);
//    
//                    if(PhotoMaskList.size() <= 0)
//                    {
//                        throw new CustomException("MASK-0098", machineName);
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment(String.format("There is no mounted PhotoMask in this machine[%s]", machineName));
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MASK-0098");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("There is no mounted PhotoMask in this machine[%s]", machineName));
//                        //return doc;
//                    }
//                    
//                    /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
//                    //PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
//                    String photoMaskName = PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
//                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
//                    eventLog.info("PhotoMask check Ended .");
//                    
//                    /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation ==>> */
//                    if(!StringUtils.equals(photoMaskName, GenericServiceProxy.getConstantMap().VIRTUAL_PHOTOMASKNAME))
//                    {
//                        if(StringUtil.equals(machineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
//                        {
//                            MESRecipeServiceProxy.getRecipeServiceUtil().validateRecipeParameterPhotoMaskName(eventInfo, machineName, machineRecipeName, photoMaskName);
//                        }
//                    }
//                    /* <<== 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation */
//                                        
//                    //check Product ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.
//                    eventLog.info("Check ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.");
//                    
//                    /* 20190115, hhlee, delete, duplicate Inquery ==>> */
//                    //List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//                    /* <<== 20190115, hhlee, delete, duplicate Inquery */
//                    List<ProductFlag> productFlagList = new ArrayList<ProductFlag>();
//                    
//                    for(Product productData : productDataList)
//                    {
//                        String condition = "WHERE productName = ? ";
//                        Object[] bindSet = new Object[]{ productData.getKey().getProductName()};
//                        
//                        try
//                        {
//                            productFlagList = ExtendedObjectProxy.getProductFlagService().select(condition, bindSet);
//                        }
//                        catch(Throwable e)
//                        {
//                            eventLog.error("Product [" + productData.getKey().getProductName() + " [ProductFlag is not exist");
//                        }
//                        
//                        if(productFlagList.size()>0)
//                        {
//                            for(ProductFlag productFlag : productFlagList)
//                            {
//                                if(StringUtils.equals(productFlag.getTurnOverFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
//                                {
//                                    throw new CustomException("PRODUCT-0034", productData.getKey().getProductName());
//                                }
//                                
//                                if(StringUtils.equals(productFlag.getProcessTurnFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
//                                {
//                                    throw new CustomException("PRODUCT-0035", productData.getKey().getProductName());
//                                }
//                            }
//                        }
//                    }
//                    eventLog.info("PhotoMask check Ended .");
//                }
//            }
//            catch (CustomException ce)
//            {
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                /* 20190514, hhlee, add validation, PhotoMaskName RecipeParaMeter(Mask Name) Validation, Add ErrorCode : MASK-0009, MASK-0100 */
//                if(StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-9001") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0015" )
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0098")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0034") || StringUtil.equals(ce.errorDef.getErrorCode(), "PRODUCT-0035")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0026") || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0027")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "POLICY-0016") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0092")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "PROBECARD-0008") || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0009")
//                        || StringUtil.equals(ce.errorDef.getErrorCode(), "MASK-0100"))
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                }
//                else
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                } 
//                
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;            
//            }
//            /* <<== 20180616, hhlee, Modify */
//            
//            /* 20180619, Delete (Move Postion) ==>> */
//            ////Body generate
//            //this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//            //      durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//            /* <<== 20180619, Delete (Move Postion) */
//            
//            /* 20181011, hhlee, Delete (Move Postion) ==>> */
//            ///* hhlee, 20180616, Added TOMACHINE specified logic. ==>> */
//            //String toMachineName = StringUtil.EMPTY;
//            //try
//            //{
//            //    if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
//            //    {
//            //        //String toMachineName = MESLotServiceProxy.getLotServiceUtil().getToMachineName(lotData.getProcessOperationName(),CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//            //        //Only INDP has toMachineName
//            //        List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//            //        if(toMachineAndOperationName.size() > 0)
//            //        {
//            //            toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");                                            
//            //        }
//            //        /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//            //        PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//            //        /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */
//            //    }
//            //    else
//            //    {
//            //        /* 20180930, Add, Operation Validation(MachineName, UnitName) ==>> */
//            //        PolicyUtil.checkMachineOperationByTOPolicy(lotData.getFactoryName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//            //        /* <<== 20180930, Add, Operation Validation(MachineName, UnitName) */ 
//            //    }
//            //}
//            //catch (CustomException ce)
//            //{
//            //    /* 20180616, hhlee, Modify ==>> */
//            //    eventInfo.setEventName("Hold");
//            //    eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//            //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//            //    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//            //    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//            //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//            //    return doc;
//            //    /* <<== 20180616, hhlee, Modify */
//            //    
//            //}
//            ///* <<== hhlee, 20180616, Added TOMACHINE specified logic. */
//            //
//            //if(!StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
//            //{
//            //    try
//            //    {
//            //        //checkOperationMode
//            //        MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(toMachineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));                    
//            //    }
//            //    catch (CustomException ce)
//            //    {   
//            //        eventInfo.setEventName("Hold");
//            //        eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//            //        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//            //        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//            //        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//            //        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            //        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());                
//            //        return doc;                   
//            //    }
//            //}
//            ////add to MachineName
//            //SMessageUtil.setBodyItemValue(doc, "TOMACHINENAME", toMachineName);
//            //SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
//            ////SMessageUtil.setBodyItemValue(doc, "PRIORITY", CommonUtil.getValue(lotData.getUdfs(), "PRIORITY"));
//            //SMessageUtil.setBodyItemValue(doc, "PRIORITY", String.valueOf(lotData.getPriority()));
//            /* <<== 20181011, hhlee, Delete (Move Postion) */
//            try
//            {
//                for (Element productElement : this.generateProductListElement(eventInfo, lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), logicalSlotMap))
//                {
//                    Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
//    
//                    productListElement.addContent(productElement);
//                }
//            }
//            catch (CustomException ce)
//            {
//                /* 20180616, hhlee, Modify ==>> */
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;
//                /* <<== 20180616, hhlee, Modify */
//                
//            }
//            
//            List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
//                        
//            // PHOTO TRCKFLAG MANAGEMENT
//            try
//            {
//                boolean checkTrackFlag = false;
//                /* 20190115, hhlee, delete, duplicate Inquery ==>> */
//                //List<Product> sProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//                /* <<== 20190115, hhlee, delete, duplicate Inquery */
//                
//                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
//                String productGradeNList = StringUtil.EMPTY;
//                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
//                
//                for (Product sProductInfo : productDataList) 
//                {
//                    /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
//                    if(StringUtil.equals(sProductInfo.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_N))
//                    {                        
//                        productGradeNList += sProductInfo.getKey().getProductName() + ",";                        
//                    }                    
//                    /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
//                    
//                    //Modify 20181022 hsryu - TRACKFLAG column move to PRODUCT -> CT_PRODUCTFLAG
//                    /*String trackFlag = sProductInfo.getUdfs().get("TRACKFLAG");
//                    //if (StringUtil.equals(trackFlag, "N") || StringUtil.isEmpty(trackFlag))
//                    if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    {
//                        checkTrackFlag = true;
//                        break;
//                    }
//                    else
//                    {
//                        continue;
//                    }*/
//                    
//                    ProductFlag productFlag = new ProductFlag();
//                    
//                    try
//                    {
//                        productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {sProductInfo.getKey().getProductName()});
//                    }
//                    catch(Throwable e)
//                    {
//                        eventLog.error(sProductInfo.getKey().getProductName() + " is not exist ProductFlag Info.");
//                    }
//                    
//                    if(productFlag!=null)
//                    {
//                        String trackFlag = productFlag.getTrackFlag();
//                        if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                        {
//                            checkTrackFlag = true;
//                            break;
//                        }
//                        else
//                        {
//                            continue;
//                        }
//                    }
//                }
//                
//                /* 20190320, hhlee, ProductGrade = 'N' , Error Return ==>> */
//                if(StringUtil.isNotEmpty(productGradeNList))
//                {
//                    productGradeNList = StringUtil.substring(productGradeNList, 0, productGradeNList.length() -1);
//                    throw new CustomException("PRODUCT-9008", productGradeNList);
//                }
//                /* <<== 20190320, hhlee, ProductGrade = 'N' , Error Return */
//                
//                //String trackFlag = lotData.getUdfs().get("TRACKFLAG");
//                //if (StringUtil.equals(trackFlag, "N") || StringUtil.isEmpty(trackFlag))
//                //{
//                //    checkTrackFlag = false;
//                //}
//                
//                if ( checkTrackFlag == true )
//                {
//                    /* 20181212, hhlee, modify, delete Area check logic ==>> */
//                    /* 20180714, Add, TrackFlag Validation ==>> */
//                    //if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH) || 
//                    ///    StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
//                    //{
//                    if ( StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
//                            StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) )
//                    {
//                        // Nothing : Normal
//                    }
//                    else
//                    {
//                        /* 20180616, hhlee, Modify ==>> */
//                        //// Abnormal
//                        throw new CustomException("MACHINE-1001", machineData.getKey().getMachineName());
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment(String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
//                        //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MACHINE-1001");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
//                        //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
//                        //return doc;
//                        /* <<== 20180616, hhlee, Modify */
//                    }
//                    //}    
//                    /* <<== 20180714, Add, TrackFlag Validation */
//                    /* <<== 20181212, hhlee, modify, delete Area check logic */
//                }
//                else
//                {
//                    //eventInfo.setEventComment(String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
//                    //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
//                    ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "MACHINE-1001");
//                    //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("Check Machine TrackFlag! Machine[%s], TrackFlag[%s], TRACKFLAG of Glass", 
//                    //      machineData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG")));
//                    //return doc;
//                }
//            }
//            catch (CustomException ce)
//            {               
//                //eventLog.error("TRACKFLAG Fail!");
//                //Glass is TRACKFLAG=Y, but Machine[] TRACKFLAG is not DRYF. Please Call INT.!!
//                /* 20190320, hhlee, ProductGrade = 'N' , Error Return(PRODUCT-9008) */
//                if(StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-1001") || StringUtil.equals(ce.errorDef.getErrorCode(),"PRODUCT-9008"))
//                {
//                    eventInfo.setEventName("Hold");
//                    eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());                
//                    return doc;
//                }
//                else
//                {
//                    eventLog.error("TRACKFLAG Fail!");
//                }
//            }
//            
//            /* BENDING : Not Need : 20180601, ==>> */
//            //String bending = MESLotServiceProxy.getLotServiceUtil().getBendingValue(productList);
//            String bending = StringUtil.EMPTY;
//            SMessageUtil.setBodyItemValue(doc, "BENDING", bending);
//            /* <<== BENDING : Not Need : 20180601, */
//
//            /* SLOTBENDING : Not Need : 20180601, ==>> */
//            //setBendingValue
//            //for (Element prdElement : productList)
//            //{
//            //  prdElement.getChild("SLOTBENDING").setText(bending);
//            //}
//            /* <<== SLOTBENDING : Not Need : 20180601,  */
//
//            /* 20180619, Add ELA Q-Time Flag Check ==> */
//            /* If the glass is "ELA Q-Time Flag(QTIMEFLAG)" and "Y", the operation is only possible with the ELA machine. */
//            try
//            {
//                if(ExtendedObjectProxy.getProductFlagService().checkProductFlagElaQtime(productList))
//                {
//                    if(!CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ELA"))
//                    {
//                        throw new CustomException("PRODUCT-9004", lotList.get(0).getKey().getLotName());
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment(String.format("This Lot[%s] is only available in ELA machine.", lotList.get(0).getKey().getLotName()));
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"EL","");
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HOLD","ELHL", eventInfo.getEventComment(),"");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-9004");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("This Lot[%s] is only available in ELA machine.", lotList.get(0).getKey().getLotName()));
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot", "VRHL","");
//                        //return doc;
//                    }      
//                }
//            }
//            catch (CustomException ce)
//            {
//                /* 20180616, hhlee, Modify ==>> */
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                /* 20190424, hhlee, modify, changed function ==>> */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//                /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                /* <<== 20190424, hhlee, modify, changed function */
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc;
//                /* <<== 20180616, hhlee, Modify */
//                
//            }
//            /* <<== 20180619, Add ELA Q-Time Flag Check */
//            
//            //set Recipe
//            for (Element prdElement : productList)
//            {
//                String recipeName = SMessageUtil.getChildText(prdElement, "PRODUCTRECIPE", false);
//
//                if(recipeName != null && StringUtil.isNotEmpty(recipeName))
//                {
//                    for (Element eleTarget : productList)
//                    {
//                        String targetRecipe = SMessageUtil.getChildText(eleTarget, "PRODUCTRECIPE", false);
//
//                        if(targetRecipe == null || StringUtil.isEmpty(targetRecipe))
//                        {
//                            eleTarget.getChild("PRODUCTRECIPE").setText(recipeName);
//                        }
//                    }
//
//                    if(StringUtil.isEmpty(SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false)))
//                        SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", recipeName);
//                    break;
//                }
//            }
//        }
//        else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
//        {
//            eventLog.info("Sorter Port job download");
//            
//            /* 20180616, hhlee, Modify ==>> */
//            try
//            {
//                //job validation
//                /* 20190605, hhlee, modify, add Sorter TransferDirection ==>> */
//                //this.validateSorterJob(machineName, portName, carrierName);
//                transferDirection = this.validateSorterJob(machineName, portName, carrierName);
//                /* <<== 20190605, hhlee, modify, add Sorter TransferDirection */
//    
//                //multi-Lot spec
//                List<Lot> lotList = CommonUtil.getLotListByCarrier(carrierName, false);
//    
//                //Recipe
//                String machineRecipeName = "";
//    
//                //Lot validation
//                if (lotList.size() > 0)
//                {
//                    //using CST
//                    //slot map validation    20170629 add BPK PL Port don't validation SlotMap
//                    String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//                    
//                    if(!machineSpecData.getUdfs().get("CONSTRUCTTYPE").equals("BPK")) //Is there type BPK ?
//                    {
//                        if (StringUtil.isNotEmpty(slotMap) && !slotMap.equals(logicalSlotMap))
//                        {
//                            throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
//                            //eventInfo.setEventName("Hold");
//                            //eventInfo.setEventComment("Different (OriSoltMap)" + slotMap + "SlotMap" + logicalSlotMap);
//                            ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
//                            ///* 20180601, Slot missmatch  ==>> */
//                            ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
//                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "PRODUCT-0020");
//                            //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "Different (OriSoltMap)" + slotMap + "SlotMap" + logicalSlotMap);
//                            ///* <<== 20180601, Slot missmatch */
//                            //return doc;
//                        }
//                        //{
//                        //  MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null, "SM","");
//                        //}
//                    }
//    
//                    /* 20190116, hhlee, modify, add MachineGroup Validation ==>> */
//                    //for (Lot subLotData : lotList)
//                    //{
//                    //    lotData = subLotData;
//                    // 
//                    //    /* 20181031, Add, MachineGroup Validation Logic Move ==>> */
//                    //    //String machineGroup = machineData.getMachineGroupName();
//                    //    ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                    //    String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
//                    //    String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
//                    //    /* <<== 20181031, Add, MachineGroup Validation Logic Move */
//                    //    
//                    //    CommonValidation.checkLotState(lotData);
//                    //    CommonValidation.checkLotProcessState(lotData);
//                    //    CommonValidation.checkLotHoldState(lotData);
//                    // 
//                    //    /* 20181204, hhlee, delete , delete qtime ==>> */
//                    //    ////20180504, kyjung, QTime
//                    //    //MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());
//                    //    /* <<== 20181204, hhlee, delete , delete qtime */
//                    //    
//                    //    //20180525, kyjung, MQC
//                    //    MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
//                    //}
//                    
//                    lotData = lotList.get(0);
//                                       
//                    CommonValidation.checkLotState(lotData);
//                    CommonValidation.checkLotProcessState(lotData);
//                    CommonValidation.checkLotHoldState(lotData);
//
//                    /* 20181204, hhlee, delete , delete qtime ==>> */
//                    ////20180504, kyjung, QTime
//                    //MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());
//                    /* <<== 20181204, hhlee, delete , delete qtime */
//                    
//                    //20180525, kyjung, MQC
//                    MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());
//                    /* <<== 20190116, hhlee, modify, add MachineGroup Validation */
//                    
//                    /* 20190116, hhlee, modify, add MachineGroup Validation ==>> */
//                    ////waiting step
//                    //ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                    ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                    String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
//                    String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup); 
//                    /* <<== 20190116, hhlee, modify, add MachineGroup Validation */
//                    
//                    if (!StringUtil.equals(machineOperationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
//                    {
//                        throw new CustomException("LOT-9041", lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName);
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment(String.format("The operation[%s] of Lot[%s] to proceed is different from the machine[%s]", lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName));
//                        ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                        ///* 20180601, Slot missmatch  ==>> */
//                        ////throw new CustomException("PRODUCT-0020", slotMap , logicalSlotMap);
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "LOT-9041");
//                        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, String.format("The operation[%s] of Lot[%s] to proceed is different from the machine[%s]",  lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName));
//                        ///* <<== 20180601, Slot missmatch */                       
//                        //return doc;                        
//                    }
//                    
//                    //singleLot
//                    //representative Recipe
//                    machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
//                                            lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
//    
//                    //New
//                    this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                            durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//    
//    
//                    SMessageUtil.setBodyItemValue(doc, "CARRIERNAME", carrierName);
//                    SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotData.getKey().getLotName());
//                    SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
//    
//                    /* 20180807, hhlee, Modify ==>> */
//                    //for (Element productElement : this.generateProductListElement(lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE")))
//                    for (Element productElement : this.generateProductListElement(eventInfo, lotList, machineData, portData, durableData, machineRecipeName, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), logicalSlotMap))
//                    {
//                        Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
//    
//                        productListElement.addContent(productElement);
//                    }
//                    /* <<== 20180807, hhlee, Modify */
//                }
//                else
//                {
//                    //empty CST
//                    //slot map validation
//                    if (StringUtil.isNotEmpty(slotMap))
//                    {
//                        /* 20181214, hhlee, modify, ==>> */
//                        //CommonValidation.checkCstSlot(slotMap);
//                        CommonValidation.checkCstSlot(carrierName, slotMap);
//                        /* 20181214, hhlee, modify, ==>> */
//                    }
//                    
//                    //this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), "", "", "", "", "", "", durableData.getDurableState(), durableData.getDurableType());
//    
//                    this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                            durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//    
//                    SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
//                }
//            }
//            catch (CustomException ce)
//            {              
//                //eventInfo.setEventName("Hold");
//                //eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                ////MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                //return doc;
//                eventInfo.setEventName("Hold");
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                if(StringUtil.equals(ce.errorDef.getErrorCode(),"PRODUCT-0020"))
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","BCHL","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                }
//                else if(StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9041") || StringUtil.equals(ce.errorDef.getErrorCode(),"MACHINE-0102") ||
//                        StringUtil.equals(ce.errorDef.getErrorCode(),"SYS-9999") || StringUtil.equals(ce.errorDef.getErrorCode(),"PROCESSOPERATION-9001") ||
//                        StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9027") || StringUtil.equals(ce.errorDef.getErrorCode(),"LOT-9049"))
//                {
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null,"HoldLot","VRHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                }
//                //20181214, hhlee, add
//                else if(StringUtil.equals(ce.errorDef.getErrorCode(),"CST-0042")) //20181214, hhlee, add
//                {
//                    /* 20180531, Add CST Hold Logic ==>> */
//                    MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
//                }
//                else
//                {
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", true, true, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//                } 
//                
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                return doc; 
//            }
//            /* <<== 20180616, hhlee, Modify */
//        }
//        else
//        {
//            //PU Port
//            eventLog.info("Unloader Port job download");
//            /* 20180616, hhlee, Modify ==>> */
//            try
//            {
//                CommonValidation.checkEmptyCst(carrierName);
//    
//                /* 20190124, hhlee, add empty cassette validation ==>> */
//                lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//                if (lotData != null)
//                {
//                    throw new CustomException("CST-0006", carrierName);
//                }
//                /* <<== 20190124, hhlee, add empty cassette validation */
//                
//                //slot map validation
//                if (StringUtil.isNotEmpty(slotMap))
//                {
//                    /* 20181214, hhlee, modify, ==>> */
//                    //CommonValidation.checkCstSlot(slotMap);
//                    CommonValidation.checkCstSlot(carrierName, slotMap);
//                    /* 20181214, hhlee, modify, ==>> */
//                }
//                
//                //generateBodyTemplate
//                this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                        durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//    
//                SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
//                SMessageUtil.setBodyItemValue(doc, "SLOTSEL", this.slotMapTransfer(slotMap));
//            }
//            catch (CustomException ce)
//            {               
//                /* 20180531, Add CST Hold Logic ==>> */
//                eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                /* <<== 20180531, Add CST Hold Logic */             
//                return doc;
//            }
//            /* <<== 20180616, hhlee, Modify */
//        }
//        
//        // 2019.05.21_hsryu_Add Logic. return SlotSel and Memory Note. Mantis 0003959.
//        String slotSel = "";
//
//        try
//        {
//            //generate selection map
//            slotSel = this.doGlassSelection(doc, lotData, durableData, slotMap);            
//            
//            /* 20190605, hhlee, add, SlotSel All "N" Validation ==>> */
//            Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);          
//            if(productListElement != null)
//            {
//                if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") ||
//                        CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PO") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS")) 
//                {
//                    if(StringUtil.indexOf(slotSel, "Y") < 0 &&
//                            !StringUtil.equals(transferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
//                    {
//                        throw new CustomException("LOT-9051", durableData.getKey().getDurableName(), slotSel);
//                    }
//                } 
//                else
//                {                   
//                }
//            }
//            /* <<== 20190605, hhlee, add, SlotSel All "N" Validation */
//        }
//        catch (CustomException ce)
//        {
//            /* 20180616, hhlee, Modify ==>> */
//            eventInfo.setEventName("Hold");
//            eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"CD","");
//            /* 20190424, hhlee, modify, changed function ==>> */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","");
//            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, "", "");
//            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","VRHL","", true, true, "", "");
//            /* <<== 20190424, hhlee, modify, changed function */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"SM01","");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//            return doc;
//            /* <<== 20180616, hhlee, Modify */            
//        }
//
//        try
//        {
//            // 2019.05.21_hsryu_Add Logic. return SlotSel and Memory Note. Mantis 0003959.
//            this.MemorySlotSelNoteForSampling(lotData, slotSel, eventInfo);
//        }
//        catch (Throwable e)
//        {
//            eventLog.warn("Error Memory Sampling SlotSel.");
//        }
//        
//        return doc;
//    }
//
//
//
//    private String createSlotMapforUPK(Durable durableData) throws CustomException
//    {
//        String SlotMap = "";
//
//        long durableCapacity = durableData.getCapacity();
//
//        for(int i = 0; i < durableCapacity; i++)
//            SlotMap +=SlotMap;
//
//        return SlotMap;
//    }
//
//    /**
//     * probeCardCheck
//     * @author wghuang
//     * @since 2018.05.08
//     * @param machineName
//     * @param operationMode
//     * @return
//     * @throws CustomException
//     */
//    private void probeCardCheck(String machineName, String operationMode) throws CustomException
//    {
//        int unitCount = 0 ;
//        int pUnitCoutn = 0 ;
//
//        if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
//        {
//            if(getUnitListByMachine(machineName).size()<=0)
//                throw new CustomException("MACHINE-9001", machineName);
//
//            unitCount = getUnitListByMachine(machineName).size();
//
//            pUnitCoutn = getPUnitListByUnitList(getUnitListByMachine(machineName)).size();
//
//            if(unitCount != pUnitCoutn)
//                throw new CustomException("PROBECARD-0001", machineName);
//            else
//            {
//                String probeCardType ="";
//                for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
//                {
//                    String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");
//
//                    if(StringUtil.isEmpty(probeCardType))
//                    {
//                        probeCardType = tempprobeCardType;
//                    }
//                    else
//                    {
//                        if(!StringUtil.equals(probeCardType,tempprobeCardType))
//                            throw new CustomException("PROBECARD-0002", machineName, probeCard.getKey().getDurableName(), probeCardType, 
//                                    probeCard.getKey().getDurableName(), tempprobeCardType);
//                    }
//                }
//            }
//        }
//        else if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
//        {
//            if(pUnitCoutn < 0)
//                throw new CustomException("PROBECARD-0005", machineName);
//            else if(pUnitCoutn > unitCount)
//                throw new CustomException("PROBECARD-0004", machineName);
//
//            /*if(unitCount == pUnitCoutn)
//            {
//                String probeCardType ="";
//                for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
//                {
//                    String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");
//
//                    if(StringUtil.isEmpty(probeCardType))
//                    {
//                        probeCardType = tempprobeCardType;
//                    }
//                    else
//                    {
//                        if(StringUtil.equals(probeCardType,tempprobeCardType))
//                            throw new CustomException("PROBECARD-0003", machineName);
//                    }
//                }
//            }
//            else if(unitCount < pUnitCoutn)
//                throw new CustomException("PROBECARD-0004", machineName);*/
//        }
//    }
//
//    private List<Machine> getUnitListByMachine(String machineName) throws CustomException
//    {
//        String condition = "WHERE SUPERMACHINENAME = ? AND MACHINENAME <> ?";
//
//        Object[] bindSet = new Object[]{machineName,"A2TST011"};
//
//        List<Machine>unitList ;
//        try
//        {
//            unitList = MachineServiceProxy.getMachineService().select(condition, bindSet);
//        }
//        catch(NotFoundSignal ne)
//        {
//            unitList = new ArrayList<Machine>();
//        }
//
//        return unitList;
//    }
//
//    private List<Durable> getPUnitListByUnitList(List<Machine> machineList)
//    {
//        String unitlistName = "";
//        List<Durable> probeCardList;
//
//        for(Machine unit : machineList)
//        {
//            if(StringUtil.isEmpty(unitlistName))
//                unitlistName = "'" + unit.getKey().getMachineName() + "'";
//
//            unitlistName += ",'" + unit.getKey().getMachineName() + "'";
//        }
//
//        try
//        {
//            probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
//        }
//        catch(NotFoundSignal ne)
//        {
//            probeCardList = new ArrayList<Durable>();
//        }
//
//        return probeCardList;
//    }
//
//    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//    //Before Only try{}catch(){} - 20181010 
//    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
//    /* 20180807, hhlee, Delete generateBodyTemplate ==>> */
////  /**
////   * append one by one to LotInfoDownloadRequest
////   * @author swcho
////   * @since 2015.03.09
////   * @param bodyElement
////   * @param lotName
////   * @param carrierName
////   * @param productionType
////   * @param productSpecName
////   * @param processOperationName
////   * @param durableState
////   * @param durableType
////   * @return
////   * @throws CustomException
////   */
////  private Element generateBodyTemplate(Element bodyElement,
////              String lotName, String carrierName,
////              String productionType, String productSpecName, String processOperationName, String lotGrade,
////              String durableState, String durableType)
////      throws CustomException
////  {
////
////      if (bodyElement.getChild("CARRIERNAME") == null)
////      {
////          Element carrierIDElement = new Element("CARRIERNAME");
////          carrierIDElement.setText(carrierName);
////          bodyElement.addContent(carrierIDElement);
////      }
////
////      if (bodyElement.getChild("LOTNAME") == null)
////      {
////          Element lotIDElement = new Element("LOTNAME");
////          lotIDElement.setText(lotName);
////          bodyElement.addContent(lotIDElement);
////      }
////
////      Element carrierStateElement = new Element("CARRIERSTATE");
////      carrierStateElement.setText(durableState);
////      bodyElement.addContent(carrierStateElement);
////
////      Element CarrierTypeElement = new Element("CARRIERTYPE");
////      CarrierTypeElement.setText(durableType);
////      bodyElement.addContent(CarrierTypeElement);
////
////      Element firstProductionTypeElement = new Element("PRODUCTIONTYPE");
////      firstProductionTypeElement.setText(productionType);
////      bodyElement.addContent(firstProductionTypeElement);
////
////      Element firstProductSpecNameElement = new Element("PRODUCTSPECNAME");
////      firstProductSpecNameElement.setText(productSpecName);
////      bodyElement.addContent(firstProductSpecNameElement);
////
////      Element firstWorkorderNameElement = new Element("WORKORDERNAME");
////      firstWorkorderNameElement.setText("");
////      bodyElement.addContent(firstWorkorderNameElement);
////
////      Element partid = new Element("PARTID");
////      partid.setText("");
////      bodyElement.addContent(partid);
////
////      Element firstProcessOperationNameElement = new Element("PROCESSOPERATIONNAME");
////      firstProcessOperationNameElement.setText(processOperationName);
////      bodyElement.addContent(firstProcessOperationNameElement);
////
////      Element lotJudgeElement = new Element("LOTJUDGE");
////      lotJudgeElement.setText(lotGrade);
////      bodyElement.addContent(lotJudgeElement);
////
////      //need to fill outside
////      Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
////      machineRecipeNameElement.setText("");
////      bodyElement.addContent(machineRecipeNameElement);
////
////      Element slotSelElement = new Element("SLOTSEL");
////      slotSelElement.setText("");
////      bodyElement.addContent(slotSelElement);
////
////      Element productQuantityElement = new Element("PRODUCTQUANTITY");
////      productQuantityElement.setText("");
////      bodyElement.addContent(productQuantityElement);
////
////      Element productListElement = new Element("PRODUCTLIST");
////      productListElement.setText("");
////      bodyElement.addContent(productListElement);
////
////      return bodyElement;
////  }
//    /* <<== 20180807, hhlee, Delete generateBodyTemplate */
//
//    /**
//     * append one by one to LotInfoDownloadRequest
//     * @author wghuang
//     * @since 2018.03.29
//     * @param bodyElement
//     * @param machineName
//     * @param carrierName
//     * @param durableState
//     * @param durableType
//     * @param portName
//     * @param portType
//     * @param portUseType
//     * @param slotMap
//     * @param processOperationName
//     * @return
//     * @throws CustomException
//     */
//    private Element generateEDOBodyTemplate(Element bodyElement,
//                Machine machineData, String carrierName,String durableState,String durableType,String portName,
//                String portType,String portUseType,String slotMap, String processOperationName)
//        throws CustomException
//    {
//        //remove Body Content
//        bodyElement.removeContent();
//
//        Element machineNameE = new Element("MACHINENAME");
//        machineNameE.setText(machineData.getKey().getMachineName());
//        bodyElement.addContent(machineNameE);
//
//        Element carrierNameE = new Element("CARRIERNAME");
//        carrierNameE.setText(carrierName);
//        bodyElement.addContent(carrierNameE);
//
//        Element carrierStateE = new Element("CARRIERSTATE");
//        carrierStateE.setText(durableState);
//        bodyElement.addContent(carrierStateE);
//
//        Element carrierTypeE = new Element("CARRIERTYPE");
//        carrierTypeE.setText(durableType);
//        bodyElement.addContent(carrierTypeE);
//
//        Element portNameE = new Element("PORTNAME");
//        portNameE.setText(portName);
//        bodyElement.addContent(portNameE);
//
//        Element portTypeE = new Element("PORTTYPE");
//        portTypeE.setText(portType);
//        bodyElement.addContent(portTypeE);
//
//        Element portUseTypeE = new Element("PORTUSETYPE");
//        portUseTypeE.setText(portUseType);
//        bodyElement.addContent(portUseTypeE);
//
//        Element slotMapE = new Element("SLOTMAP");
//        slotMapE.setText(slotMap);
//        bodyElement.addContent(slotMapE);
//
//
//        //need fill out side
//        Element slotSelE = new Element("SLOTSEL");
//        slotSelE.setText("");
//        bodyElement.addContent(slotSelE);
//
//        Element machineRecipeName = new Element("MACHINERECIPENAME");
//        machineRecipeName.setText("");
//        bodyElement.addContent(machineRecipeName);
//
//        Element probeCardNameE = new Element("PROBECARDNAME");
//        probeCardNameE.setText("");
//        bodyElement.addContent(probeCardNameE);
//
//        Element operationModeNameE = new Element("OPERATIONMODENAME");
//        operationModeNameE.setText(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
//        bodyElement.addContent(operationModeNameE);
//
//        //need fill out side
//        Element toMachineNameE = new Element("TOMACHINENAME");
//        toMachineNameE.setText("");
//        bodyElement.addContent(toMachineNameE);
//
//        Element bendingE = new Element("BENDING");
//        bendingE.setText("");
//        bodyElement.addContent(bendingE);
//
//        Element priorityE = new Element("PRIORITY");
//        priorityE.setText("");
//        bodyElement.addContent(priorityE);
//
//        //DCRReadFlag
//        Element DCRReadFlagE = new Element("DCRREADFLAG");
//        DCRReadFlagE.setText("");
//        bodyElement.addContent(DCRReadFlagE);
//
//        Element productListE = new Element("PRODUCTLIST");
//        productListE.setText("");
//        bodyElement.addContent(productListE);
//
//        return bodyElement;
//    }
//
//    /* 20180807, hhlee, Delete generateProductListElement ==>> */
////  /**
////   * write down processing material info
////   * @author swcho
////   * @since 2015.03.09
////   * @param lotList
////   * @param machineData
////   * @param portData
////   * @param durableData
////   * @param machineRecipeName
////   * @return
////   * @throws CustomException
////   */
////  private List<Element> generateProductListElement(List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
////          String machineRecipeName, String machineConstructType)
////      throws CustomException
////  {
////      //final return
////      List<Element> productListElement = new ArrayList<Element>();        
////      
////      String chamberGroupName = StringUtil.EMPTY;
////        String machineGroupName = StringUtil.EMPTY;
////        
////      for (Lot lotData : lotList)
////      {
////          //avail Product list
////          List<Product> productList;
////          try
////          {
////              productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
////          }
////          catch (Exception ex)
////          {
////              throw new CustomException("SYS-9999", "Product", "No Product to process");
////          }
////
////          //base Product info
////          ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
////
////          //base flow info
////          ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
////
////          //waiting step
////          ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
////
////          //get Sorter job List
////          List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
////          if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
////          {
////              sortJobList = this.getSortJobList(durableData, lotData, machineData, portData);
////          }
////
////          //make sure TK cancel
////          /*
////          boolean abortFlag = false;
////          try
////          {
////              ProductServiceProxy.getProductService().select("lotName = ? AND processingInfo = ? AND productState = ?",
////                      new Object[] {lotData.getKey().getLotName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });
////
////              abortFlag = true;
////          }
////          catch (NotFoundSignal ne)
////          {
////              eventLog.info("retry after TK In Canceled");
////
////              abortFlag = false;
////          }
////            */
////
////          //need re-engineering
////            /*
////          //160801 by swcho : sampling enhanced
////          List<ListOrderedMap> sampleLotListOld = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByLot(lotData.getKey().getLotName(),
////                                                  lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                  lotData.getProcessFlowName(), lotData.getProcessOperationName());
////          List<SampleProduct> sampleProductListOld = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(
////                                                      lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                      lotData.getProcessFlowName(), lotData.getProcessOperationName());
////
////          //161228 by swcho :flow sampling is lower prioirty
////          List<FlowSampleLot> sampleLotListNew = new ArrayList<FlowSampleLot>();
////          List<FlowSampleProduct> sampleProductListNew = new ArrayList<FlowSampleProduct>();
////          if (sampleLotListOld.size() < 1)
////          {
////              sampleLotListNew = ExtendedObjectProxy.getFlowSampleLotService().getSampling(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                              lotData.getProcessFlowName(), lotData.getProcessOperationName());
////              sampleProductListNew = ExtendedObjectProxy.getFlowSampleProductService().getSamplingProduct(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                                          lotData.getProcessFlowName(), lotData.getProcessOperationName());
////          }
////
////          //store sampling parameters
////          int targetCnt = 0;
////          //String targetPositions = "";
////          int selCnt = 0;
////
////          if (sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0 ) //|| partSelLot.size() > 0
////          {
////              //String sTargetCount = CommonUtil.getValue(sampleLotList.get(0), "ACTUALPRODUCTCOUNT");
////              String sTargetCount = "0";
////
////              if (sampleLotListOld.size() > 0)
////              {
////                  sTargetCount = CommonUtil.getValue(sampleLotListOld.get(0), "ACTUALPRODUCTCOUNT");
////              }
////              else if (sampleLotListNew.size() > 0)
////              {
////                  sTargetCount = sampleLotListNew.get(0).getACTUALPRODUCTCOUNT();
////              }
////              else if (partSelLot.size() > 0)
////              {
////                  sTargetCount = partSelLot.get(0).getACTUALPRODUCTCOUNT();
////              }
////
////              try
////              {
////                  targetCnt = Integer.parseInt(sTargetCount);
////              }
////              catch (Exception ex)
////              {
////                  eventLog.error("Sampling target count convert failed");
////              }
////          }
////            */
////          
////          /* 20180620, hhlee, Add Set ChamberGroup ==>> */
////            machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
////            chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(machineData.getKey().getMachineName(), machineGroupName, "3");
////            /* <<== 20180620, hhlee, Add Set ChamberGroup */
////            
////          for (Product productData : productList)
////          {
////              String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");
////
////              //DP box data
////              ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
////              String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
////              //String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
////              String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
////              String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
////              String samplingFlag = "";
////
////              //Glass selection
////              //161228 by swcho : additional sampling
////              if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
////              {
////                  /* 20180602, Need Sampling Logic ==>> */
////                  /* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
////                  Set SlotSel to " Y " for the entire operation of Glass. */
////                  samplingFlag = "Y";
////                  /* <<== 20180602, Need Sampling Logic */
////
////                  /*if (sampleProductListOld != null && sampleProductListOld.size() > 0)
////                  {
////                      samplingFlag = ExtendedObjectProxy.getSampleLotService().getSamplingFlag(productData, sampleProductListOld);
////
////                      machineRecipeName = "";
////
////                      if(StringUtil.equals(samplingFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
////                      {
////                          *//** 20180208 NJPARK
////                          for (SampleProduct sampleProduct : sampleProductListOld)
////                          {
////                              if(!StringUtil.isEmpty(sampleProduct.getMACHINERECIPENAME()) &&
////                                      StringUtil.equals(productData.getKey().getProductName(), sampleProduct.getPRODUCTNAME()))
////                              {
////                                  machineRecipeName = sampleProduct.getMACHINERECIPENAME();
////
////                                  break;
////                              }
////                          }
////                          *//*
////                          selCnt++;
////                      }
////                  }
////                  else if (sampleProductListNew.size() > 0)
////                  {
////                      samplingFlag = ExtendedObjectProxy.getFlowSampleProductService().getSamplingFlag(productData, sampleProductListNew);
////
////                      if (samplingFlag.equals(GenericServiceProxy.getConstantMap().FLAG_Y))
////                          selCnt++;
////                  }*/
////              }
////              else
////              {
////                  //sort case
////                  if (StringUtil.equals(operationData.getDetailProcessOperationType(), "SORT"))
////                  {
////                      samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
////                  }
////                  //repair case
////                  else if (StringUtil.equals(operationData.getDetailProcessOperationType(), "REP"))
////                  {
////                      if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                      else
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                  }
////                  //rework case
////                  else if (StringUtil.equals(flowData.getProcessFlowType(), "Rework"))
////                  {
////                      if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                      else
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                  }
////                  //MQC SlotMap
////                  else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
////                  {
////                      String flag = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
////                        if(!StringUtil.isEmpty(flag))
////                      {
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                      }
////                      else
////                      {
////                          samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                      }
////                  }
////                  else
////                  {
////                      /* 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" ==>>  */
////                        ////samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
////                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                        }
////                        else
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                        }
////                        /* <<== 20180713, Delete */
////                        /* <<== 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" */
////                        
////                        /* 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" ==>>  */
////                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                        }
////                        else
////                        {
////                          if(CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals(GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
////                              samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                          else
////                              samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                          //samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
////                        }
////                      /* <<== 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" */
////                  }
////              }
////
////              //machineData
////              Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
////                      productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);
////
////              productListElement.add(productElement);
////          }
////
////          //160801 by swcho : sampling filling
////          //161228 by swcho : additional sampling
////          /*if ((sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0)
////                  &&  targetCnt > 0 && targetCnt > selCnt)
////          {
////              eventLog.info("Glass selection filling");
////
////              for (Element productElement : productListElement)
////              {
////                  if (SMessageUtil.getChildText(productElement, "SAMPLINGFLAG", false).equals("Y"))
////                  {
////                      continue;
////                  }
////                  else
////                  {
////                      try
////                      {
////                          productElement.getChild("SAMPLINGFLAG").setText("Y");
////                          selCnt++;
////                      }
////                      catch (Exception ex)
////                      {
////                          eventLog.error(ex.getMessage());
////                      }
////                  }
////
////                  if (selCnt >= targetCnt) break;
////              }
////          }*/
////      }
////
////      return productListElement;
////  }
//    /* <<== 20180807, hhlee, Delete generateProductListElement */
//
////  /**
////     * write down processing material info
////     * @author swcho
////     * @since 2015.03.09
////     * @param lotList
////     * @param machineData
////     * @param portData
////     * @param durableData
////     * @param machineRecipeName
////     * @return
////     * @throws CustomException
////     */
////    private List<Element> generateProductListElement(List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
////            String machineRecipeName, String machineConstructType, String logicalSlotMap)
////        throws CustomException
////    {
////        //final return
////        List<Element> productListElement = new ArrayList<Element>();
////        
////        String chamberGroupName = StringUtil.EMPTY;
////        String machineGroupName = StringUtil.EMPTY;
////        
////        for (Lot lotData : lotList)
////        {
////            //avail Product list
////            List<Product> productList;
////            try
////            {
////                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
////            }
////            catch (Exception ex)
////            {
////                throw new CustomException("SYS-9999", "Product", "No Product to process");
////            }
////
////            //base Product info
////            ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
////
////            //base flow info
////            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
////
////            //waiting step
////            ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
////
////            //get Sorter job List
////            List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
////            if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
////            {
////                sortJobList = this.getSortJobList(durableData, lotData, machineData, portData);
////            }
////         
////            //make sure TK cancel
////            /*
////            boolean abortFlag = false;
////            try
////            {
////                ProductServiceProxy.getProductService().select("lotName = ? AND processingInfo = ? AND productState = ?",
////                        new Object[] {lotData.getKey().getLotName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });
////
////                abortFlag = true;
////            }
////            catch (NotFoundSignal ne)
////            {
////                eventLog.info("retry after TK In Canceled");
////
////                abortFlag = false;
////            }
////            */
////
////            //need re-engineering
////            /*
////            //160801 by swcho : sampling enhanced
////            List<ListOrderedMap> sampleLotListOld = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByLot(lotData.getKey().getLotName(),
////                                                    lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName());
////            List<SampleProduct> sampleProductListOld = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(
////                                                        lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                        lotData.getProcessFlowName(), lotData.getProcessOperationName());
////
////            //161228 by swcho :flow sampling is lower prioirty
////            List<FlowSampleLot> sampleLotListNew = new ArrayList<FlowSampleLot>();
////            List<FlowSampleProduct> sampleProductListNew = new ArrayList<FlowSampleProduct>();
////            if (sampleLotListOld.size() < 1)
////            {
////                sampleLotListNew = ExtendedObjectProxy.getFlowSampleLotService().getSampling(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                                lotData.getProcessFlowName(), lotData.getProcessOperationName());
////                sampleProductListNew = ExtendedObjectProxy.getFlowSampleProductService().getSamplingProduct(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
////                                                                                                            lotData.getProcessFlowName(), lotData.getProcessOperationName());
////            }
////
////            //store sampling parameters
////            int targetCnt = 0;
////            //String targetPositions = "";
////            int selCnt = 0;
////
////            if (sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0 ) //|| partSelLot.size() > 0
////            {
////                //String sTargetCount = CommonUtil.getValue(sampleLotList.get(0), "ACTUALPRODUCTCOUNT");
////                String sTargetCount = "0";
////
////                if (sampleLotListOld.size() > 0)
////                {
////                    sTargetCount = CommonUtil.getValue(sampleLotListOld.get(0), "ACTUALPRODUCTCOUNT");
////                }
////                else if (sampleLotListNew.size() > 0)
////                {
////                    sTargetCount = sampleLotListNew.get(0).getACTUALPRODUCTCOUNT();
////                }
////                else if (partSelLot.size() > 0)
////                {
////                    sTargetCount = partSelLot.get(0).getACTUALPRODUCTCOUNT();
////                }
////
////                try
////                {
////                    targetCnt = Integer.parseInt(sTargetCount);
////                }
////                catch (Exception ex)
////                {
////                    eventLog.error("Sampling target count convert failed");
////                }
////            }
////            */
////            
////            /* 20180620, hhlee, Add Set ChamberGroup ==>> */
////            machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
////            chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(machineData.getKey().getMachineName(), machineGroupName, "3");
////            /* <<== 20180620, hhlee, Add Set ChamberGroup */
////            
////            for (Product productData : productList)
////            {
////                String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");
////
////                //DP box data
////                ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
////                String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
////                //String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
////                String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
////                String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
////                String samplingFlag = "";
////
////                //Glass selection
////                //161228 by swcho : additional sampling
////                if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
////                {
////                    /* 20180602, Need Sampling Logic ==>> */
////                    /* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
////                    Set SlotSel to " Y " for the entire operation of Glass. */
////                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
////                    /* <<== 20180602, Need Sampling Logic */
////
////                    /*if (sampleProductListOld != null && sampleProductListOld.size() > 0)
////                    {
////                        samplingFlag = ExtendedObjectProxy.getSampleLotService().getSamplingFlag(productData, sampleProductListOld);
////
////                        machineRecipeName = "";
////
////                        if(StringUtil.equals(samplingFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
////                        {
////                            *//** 20180208 NJPARK
////                            for (SampleProduct sampleProduct : sampleProductListOld)
////                            {
////                                if(!StringUtil.isEmpty(sampleProduct.getMACHINERECIPENAME()) &&
////                                        StringUtil.equals(productData.getKey().getProductName(), sampleProduct.getPRODUCTNAME()))
////                                {
////                                    machineRecipeName = sampleProduct.getMACHINERECIPENAME();
////
////                                    break;
////                                }
////                            }
////                            *//*
////                            selCnt++;
////                        }
////                    }
////                    else if (sampleProductListNew.size() > 0)
////                    {
////                        samplingFlag = ExtendedObjectProxy.getFlowSampleProductService().getSamplingFlag(productData, sampleProductListNew);
////
////                        if (samplingFlag.equals(GenericServiceProxy.getConstantMap().FLAG_Y))
////                            selCnt++;
////                    }*/
////                }
////                else
////                {
////                    //sort case
////                    if (StringUtil.equals(operationData.getDetailProcessOperationType(), "SORT"))
////                    {
////                        samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
////                    }
////                    //repair case
////                    else if (StringUtil.equals(operationData.getDetailProcessOperationType(), "REP"))
////                    {
////                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                        else
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                    }
////                    //rework case
////                    else if (StringUtil.equals(flowData.getProcessFlowType(), "Rework"))
////                    {
////                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                        else
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                    }
////                    //MQC SlotMap
////                    else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
////                    {
////                      String flag = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
////                        if(!StringUtil.isEmpty(flag))
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                        }
////                        else
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                        }
////                    }
////                    else
////                    {
////                        /* 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" ==>>  */
////                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
////                        {
////                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                        }
////                        else
////                        {
////                            if(CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals(GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
////                                samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
////                            else
////                                samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
////                        }
////                        //samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
////                        /* ==>> 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" */
////                    }
////                }
////
////                //machineData
////                Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
////                        productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);
////
////                productListElement.add(productElement);
////            }
////
////            //160801 by swcho : sampling filling
////            //161228 by swcho : additional sampling
////            /*if ((sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0)
////                    &&  targetCnt > 0 && targetCnt > selCnt)
////            {
////                eventLog.info("Glass selection filling");
////
////                for (Element productElement : productListElement)
////                {
////                    if (SMessageUtil.getChildText(productElement, "SAMPLINGFLAG", false).equals("Y"))
////                    {
////                        continue;
////                    }
////                    else
////                    {
////                        try
////                        {
////                            productElement.getChild("SAMPLINGFLAG").setText("Y");
////                            selCnt++;
////                        }
////                        catch (Exception ex)
////                        {
////                            eventLog.error(ex.getMessage());
////                        }
////                    }
////
////                    if (selCnt >= targetCnt) break;
////                }
////            }*/
////        }
////
////        return productListElement;
////    }
//    
//    /**
//     * write down processing material info
//     * @author swcho
//     * @since 2015.03.09
//     * @param lotList
//     * @param machineData
//     * @param portData
//     * @param durableData
//     * @param machineRecipeName
//     * @return
//     * @throws CustomException
//     */
//    private List<Element> generateProductListElement(EventInfo eventInfo, List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
//            String machineRecipeName, String machineConstructType, String logicalSlotMap)
//        throws CustomException
//    {
//        //final return
//        List<Element> productListElement = new ArrayList<Element>();
//        
//        String chamberGroupName = StringUtil.EMPTY;
//        String machineGroupName = StringUtil.EMPTY;
//        
//        /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added ==>> */
//        String compareNodeStack = StringUtil.EMPTY;        
//        String compareLotName = StringUtil.EMPTY;
//        /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack variable added */
//        
//        for (Lot lotData : lotList)
//        {
//            //avail Product list
//            List<Product> productList;
//            try
//            {
//                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//            }
//            catch (Exception ex)
//            {
//                throw new CustomException("SYS-9999", "Product", "No Product to process");
//            }
//
//            //base Product info
//            ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
//
//            //base flow info
//            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
//
//            //waiting step
//            ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//
//            //get Sorter job List
//            List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
//            
//            /* 20180809, Add, Sorter Job Product List ==>> */
//            //if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
//            if (StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
//            /* <<== 20180809, Add, Sorter Job Product List */
//            {
//                sortJobList = ExtendedObjectProxy.getSortJobService().getSortJobList(durableData, lotData, machineData, portData);
//            }
//
//            /* hhlee, 20180616, Add SampleLot import ==>> */
//            String actualSamplePosition = StringUtil.EMPTY;
//            /* 20190312, hhlee, add Check Validation */
//            //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
//            /* 20190522, hhlee, modify, add check validation SamplingFlow */
//            //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//            //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
//            //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
//            if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//                    flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
//                    !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//            {
//                /* 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter ==>> */
//                //actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap);
//                actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap, false);
//                /* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter */
//                
//                /* 20190517, hhlee, add, Sampling Position Validation ==>> */
//                if(StringUtil.isEmpty(actualSamplePosition))
//                {
//                    throw new CustomException("LOT-9049", lotData.getKey().getLotName(), lotData.getProcessOperationName()); 
//                }
//                /* <<== 20190517, hhlee, add, Sampling Position Validation */
//            }            
//            /* <<== hhlee, 20180616, Add SampleLot import */
//            
//            /* 20180620, hhlee, Add Set ChamberGroup ==>> */
//            machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
//            chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(
//                    machineData.getKey().getMachineName(), machineGroupName, "3");
//            /* <<== 20180620, hhlee, Add Set ChamberGroup */
//            
//            /* 20190117, hhlee, add, change productRecipeName ==>> */
//            ///* 20181020, hhlee, add, MQC RecipeName valiable ==>> */
//            //String mqcProductRecipeName = StringUtil.EMPTY;
//            ///* <<== 20181020, hhlee, add, MQC RecipeName valiable */
//            String productRecipeName = StringUtil.EMPTY;
//            /* <<== 20190117, hhlee, add, change productRecipeName */            
//            
//            for (Product productData : productList)
//            {
//                if(!StringUtil.equals(productData.getProductState(), GenericServiceProxy.getConstantMap().Prod_InProduction) ||
//                     !StringUtil.equals(productData.getProductProcessState(), GenericServiceProxy.getConstantMap().Prod_Idle) ||
//                        !StringUtil.equals(productData.getProductHoldState(), GenericServiceProxy.getConstantMap().Prq_NotOnHold) )
//                {
//                    throw new CustomException("PRODUCT-9006", productData.getKey().getProductName(),
//                            productData.getProductState(), productData.getProductProcessState(),productData.getProductHoldState());
//                }
//                
//                /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
//                if(!StringUtil.equals(lotData.getNodeStack(), productData.getNodeStack()))
//                {
//                    if(!StringUtil.equals(compareLotName, lotData.getKey().getLotName()))
//                    {
//                        compareNodeStack += "LOTNAME: " + lotData.getKey().getLotName() + " - PRODUCTNAME: ";
//                        compareLotName = lotData.getKey().getLotName();
//                    }
//                    compareNodeStack += productData.getKey().getProductName() + ",";
//                }
//                /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
//                
//                String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");
//
//                //DP box data
//                ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
//                String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
//                //String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
//                String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
//                String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
//                String samplingFlag = "";
//                
//                /* 20190117, hhlee, add, change productRecipeName ==>> */
//                productRecipeName = StringUtil.EMPTY;
//                /* <<== 20190117, hhlee, add, change productRecipeName */ 
//                
//                //Glass selection
//                //161228 by swcho : additional sampling
//                //if (operationData.getProcessOperationType().equals("Inspection") && 
//                //        && !flowData.getProcessFlowType().equals("MQC"))
//                /* 20190312, hhlee, add Check Validation */
//                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
//                /* 20190522, hhlee, modify, add check validation SamplingFlow */
//                //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
//                //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
//                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
//                        !StringUtil.equals(operationData.getDetailProcessOperationType(),GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//                {
//                    /* 20180602, Need Sampling Logic ==>> */
//                    /* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
//                    Set SlotSel to " Y " for the entire operation of Glass. */
//                    /* 20190517, hhlee, modify, default = 'N' */
//                    //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
//                    samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
//                    if(!StringUtil.isEmpty(actualSamplePosition))
//                    {
//                        /* 20190517, hhlee, modify, default = 'N' */
//                        //samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
//                        
//                        String[] actualsamplepostion = actualSamplePosition.trim().split(",");
//                        for(int i = 0; i < actualsamplepostion.length; i++ )
//                        {
//                            if(productData.getPosition() == Long.parseLong(actualsamplepostion[i]))
//                            {
//                                samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
//                                break;
//                            }
//                        }     
//                    }
//                    /* <<== 20180602, Need Sampling Logic */
//                }
//                else
//                {
//                    /* 20180809, Add, Sorter Job Product List ==>> */
//                    //sort case
//                    //if (StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
//                    if (StringUtil.equals(operationData.getProcessOperationType(), GenericServiceProxy.getConstantMap().Pos_Sorter))
//                    /* <<== 20180809, Add, Sorter Job Product List */
//                    {
//                        samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
//                    }
//                    //repair case
//                    /* 20190522, hhlee, modify, add check validation SamplingFlow */
//                    //else if (StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//                    else if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) && 
//                            StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//                    {
//                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        else
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                    }
//                    //rework case
//                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK))
//                    {
//                        /* 20190515, hhlee, add, Rework (SlotSel)Logic  */
//                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
//                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK);                        
//                        
//                        /* 20190515, hhlee, delete ==>> */
//                        ///* 20190513, hhlee, add, Rework (SlotSel)Logic  */
//                        //if(StringUtil.equals(productData.getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA))
//                        //{
//                        //    List<Map<String, Object>> slotPostionInfoList = 
//                        //            MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData);
//                        //    String slotPostion = StringUtil.EMPTY;
//                        //    if(slotPostionInfoList != null)
//                        //    {
//                        //        slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
//                        //                slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);                                                           
//                        //    }
//                        //    
//                        //    if(StringUtil.isEmpty(slotPostion) || 
//                        //            productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        //    }
//                        //    else
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        //    }
//                        //}
//                        //else
//                        //{
//                        //    if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        //    else
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        //}
//                        /* <<== 20190515, hhlee, delete */
//                    }
//                    //MQC SlotMap
//                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
//                    {
//                        /* 20190117, hhlee, modify, MQC Positon validation */
//                        /* 20181020, hhlee, modify, MQC RecipeName valiable ==>> */
//                        //mqcProductRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
//                        //productRecipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);                        
//                        /* 20190117, hhlee, modify, MQC Positon */
//                        List<Map<String, Object>> slotPostionInfoList = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProduct(productData);
//                        String slotPostion = StringUtil.EMPTY;
//                        if(slotPostionInfoList != null)
//                        {
//                            /* 20190124, hhlee, modify , add logic null value check */
//                            //slotPostion = slotPostionInfoList.get(0).get("POSITION").toString();
//                            slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
//                                    slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);
//                            /* 20190124, hhlee, modify , add logic null value check */
//                            //productRecipeName = slotPostionInfoList.get(0).get("RECIPENAME").toString(); 
//                            productRecipeName = (slotPostionInfoList.get(0).get("RECIPENAME") != null ? 
//                                    slotPostionInfoList.get(0).get("RECIPENAME").toString() : StringUtil.EMPTY);                            
//                        }
//                        if(StringUtil.isEmpty(slotPostion) || 
//                                productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
//                        {
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        }
//                        else
//                        {
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        }                        
//                        /* <<== 20181020, hhlee, modify, MQC RecipeName valiable */
//                    }
//                    /* 20190513, hhlee, add, Branch (SlotSel)Logic  */
//                    else if (StringUtil.equals(flowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
//                    {
//                        /* 20190515, hhlee, add, Branch (SlotSel)Logic  */
//                        samplingFlag = MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData, 
//                                GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH);   
//                        
//                        /* 20190515, hhlee, delete ==>> */
//                        //if(StringUtil.equals(productData.getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA))
//                        //{
//                        //    List<Map<String, Object>> slotPostionInfoList = 
//                        //            MESProductServiceProxy.getProductServiceImpl().slotPositionMQCProductForBranchRework(productData);
//                        //    String slotPostion = StringUtil.EMPTY;
//                        //    if(slotPostionInfoList != null)
//                        //    {
//                        //        slotPostion = ( slotPostionInfoList.get(0).get("POSITION") != null ? 
//                        //                slotPostionInfoList.get(0).get("POSITION").toString() : StringUtil.EMPTY);                               
//                        //    }
//                        //    if(StringUtil.isEmpty(slotPostion) || 
//                        //            productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        //    }
//                        //    else
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        //    }       
//                        //}
//                        //else
//                        //{
//                        //    if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        //    }
//                        //    else
//                        //    {
//                        //        samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        //    }
//                        //}
//                        /* <<== 20190515, hhlee, delete */
//                    }                    
//                    else
//                    {
//                        /* 20180713, Delete ==>> */
//                        //if(CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals(GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                        //    samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        //else
//                        //    samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//
//                        /* 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" ==>>  */
//                        ////samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
//                        if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
//                        {
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                        }
//                        else
//                        {
//                            samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                        }
//                        /* <<== 20180713, Delete */
//                        /* <<== 20180801, Add, When SLOTSEL is send, there is not include ProductGrade "S" */
//                    }
//                }
//
//                /* 20190117, hhlee, modify, change logic machieRecipename, mqcProductRecipeName -> productRecipeName ==>> */
//                ///* 20181020, hhlee, add, MQC RecipeName valiable ==>> */
//                //if(StringUtil.isNotEmpty(mqcProductRecipeName))
//                //{
//                //    machineRecipeName = mqcProductRecipeName;
//                //    mqcProductRecipeName= StringUtil.EMPTY;
//                //}
//                ///* <<== 20181020, hhlee, add, MQC RecipeName valiable */
//                if(StringUtil.isEmpty(productRecipeName))
//                {
//                    productRecipeName = machineRecipeName;
//                }
//                /* <<== 20190117, hhlee, modify, change logic machieRecipename, mqcProductRecipeName -> productRecipeName */
//                
//                /* 20190117, hhlee, modify, add parameter ==>> */
//                ////machineData
//                //Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
//                //        productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);
//                
//                Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
//                        productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName,productRecipeName);
//
//                productListElement.add(productElement);
//                /* <<== 20190117, hhlee, modify, add parameter */
//                
//                /* 20190315, hhlee, Debug Log write */
//                eventLog.info(String.format("SLOTSEL - SampleFlag=%s, [ProcessFlowType=%s, ProcessOperationType=%s, DetailProcessOperationType=%s, ProductGrade=%s]", 
//                        samplingFlag, flowData.getProcessFlowType(), operationData.getProcessOperationType(), 
//                        operationData.getDetailProcessOperationType(), productData.getProductGrade()));
//            }
//        }
//
//        /* 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack ==>> */
//        if(StringUtil.isNotEmpty(compareNodeStack))
//        {
//            compareNodeStack = StringUtil.substring(compareNodeStack, 0, compareNodeStack.length() -1);
//            throw new CustomException("LOT-9047", compareNodeStack);
//        }
//        /* <<== 20190411, hhlee, add, Compare Lot Nodestack and Product Nodestack */
//        
//        return productListElement;
//    }
//    
////  /**
////   * @author wghuang
////   * @param durableData
////   * @param lotData
////   * @param machineData
////   * @param portData
////   * @return SortJobList
////   * @throws CustomException
////   */
////  private List<ListOrderedMap>getSortJobList(Durable durableData, Lot lotData, Machine machineData, Port portData) throws CustomException
////  {
////      List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
////
////      try
////      {
////          StringBuffer sqlBuffer = new StringBuffer();
////          sqlBuffer.append("SELECT J.jobName, J.jobState, C.machineName, C.portName, C.carrierName, P.fromLotName, P.productName, P.fromPosition").append("\n")
////                  .append("       FROM CT_SortJob J, CT_SortJobCarrier C, CT_SortJobProduct P").append("\n")
////                  .append("   WHERE J.jobName = C.jobName").append("\n")
////                  .append("    AND C.jobName = P.jobName").append("\n")
////                  .append("    AND C.machineName = P.machineName").append("\n")
////                  .append("    AND C.carrierName = P.fromCarrierName").append("\n")
////                  .append("    AND C.carrierName = ?").append("\n")
////                  .append("    AND C.lotName = ?").append("\n")
////                  .append("    AND C.machineName = ?").append("\n")
////                  .append("    AND C.portName = ?").append("\n")
////                  .append("    AND J.jobState IN (?,?)");
////
////          Object[] bindList = new Object[] { durableData.getKey().getDurableName(), lotData.getKey().getLotName(),
////                                              machineData.getKey().getMachineName(), portData.getKey().getPortName(),
////                                              GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED};
////
////          sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
////      }
////      catch (Exception ex)
////      {
////          eventLog.debug("No sorter job");
////          throw new CustomException("SYS-9999", "SortJob", "No job for Product");
////      }
////      return sortJobList;
////  }
//
//
//    /**
//     * @author wuzhiming
//     * @param productData
//     * @param consumableSpecData
//     * @return 除了O1PPK设备外，其他设备返回实际厚度
//     * @throws CustomException
//     */
//    private String getExactProductThickness(Product productData, ConsumableSpec consumableSpecData, String machineName) throws CustomException
//    {
//        MachineSpecKey machinespecKey = new MachineSpecKey();
//        machinespecKey.setMachineName(machineName);
//        MachineSpec machineSpec = MachineServiceProxy.getMachineSpecService().selectByKey(machinespecKey);
//
//        String construcType = CommonUtil.getValue(machineSpec.getUdfs(), "CONSTRUCTTYPE");
//
//        String productThickness = "";
//        if(StringUtil.equalsIgnoreCase(construcType, "BPK"))
//        {
//            productThickness = getProductThickness(productData, consumableSpecData);
//        }
//        else
//        {
//            productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
//        }
//        return productThickness;
//    }
//
//    /**
//     * @author wuzhiming
//     * @param productData
//     * @param consumableSpecData
//     * @return 玻璃的厚度
//     * @throws CustomException
//     */
//    private String getProductThickness(Product productData, ConsumableSpec consumableSpecData) throws CustomException
//    {
//        String productThickness = "";
//        String thicknessAfterAssemble = getThicknessAfterAssemble(productData);
//        if (!StringUtil.isEmpty(thicknessAfterAssemble))
//        {
//          productThickness = thicknessAfterAssemble;
//        }else {
//            //2017.04.10 wuzhiming add: 得到pairProduct的glassThickNess
//            double pairProductThickness = this.getPairProductThickness(productData) ;
//            productThickness = String.valueOf(this.addTwoDouble(Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS")), pairProductThickness));
//
//        }
//        return productThickness;
//    }
//
//    /**
//     * @author wuzhiming
//     * @param productData
//     * @return modeler中设定的和productspec绑定的贴合后玻璃的厚度
//     */
//    private String getThicknessAfterAssemble(Product productData)
//    {
//        ProductSpecKey productspecKey = new ProductSpecKey();
//        productspecKey.setProductSpecName(productData.getProductSpecName());
//        productspecKey.setFactoryName(productData.getFactoryName());
//        productspecKey.setProductSpecVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
//        ProductSpec ProductSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productspecKey);
//
//        String thicknessAfterAssembleString = CommonUtil.getValue(ProductSpecData.getUdfs(), "THICKNESSAFTERASSEMBLE");
//        return thicknessAfterAssembleString;
//    }
//
//    /**
//     * @Author wuzhiming
//     * @since 2017.03.10 16:46
//     * @param productData 主玻璃Data
//     * @return pairProductThickness ：贴合玻璃的厚度，若没有贴合玻璃，返回0
//     * @throws  CustomException ：consumableSpec 没找到时将抛出异常
//     */
//    private double getPairProductThickness(Product productData) throws CustomException
//    {
//        String pairProductName = CommonUtil.getValue(productData.getUdfs(),"PAIRPRODUCTNAME");
//
//        if (StringUtil.isEmpty(pairProductName))
//        {
//            return 0;
//        }else {
//            Product pairProductData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(pairProductName));
//            String crateName = CommonUtil.getValue(pairProductData.getUdfs(), "CRATENAME");
//            ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
//            double pairProductThickness = Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS"));
//
//            return pairProductThickness;
//        }
//    }
//
//    /**
//     * @author wuzhiming
//     * @since 2017.03.10 16:55
//     * @param v1
//     * @param v2
//     * @return 返回精确的两个double值相加
//     */
//    public double addTwoDouble(double v1, double v2)
//    {
//        BigDecimal b1 = new BigDecimal(Double.toString(v1));
//        BigDecimal b2 = new BigDecimal(Double.toString(v2));
//        return b1.add(b2).doubleValue();
//    }
//
////  /**
////   * write down virtual material info
////   * @author swcho
////   * @since 2015.04.11
////   * @param requestData
////   * @param productList
////   * @param machineData
////   * @param portData
////   * @param durableData
////   * @param machineRecipeName
////   * @return
////   * @throws CustomException
////   */
////  private List<Element> generateProductListElement(ProductRequest requestData, List<VirtualGlass> productList,
////                                                  Machine machineData, Port portData, Durable durableData,
////                                                  String productionType, String productType, String productSpecName, 
////                                                  String processFlowName, String processOperationName,
////                                                  String machineRecipeName)
////      throws CustomException
////  {
////      String factoryName = requestData.getFactoryName();
////
////      String productVendor = "";
////      String productThickness = "";
////      String productSize = "";
////      String degree = "";
////
////      if (productList.size() > 0)
////      {
////          String crateName = productList.get(0).getCrateName();
////
////          //DP box data
////          ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
////          productVendor = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSVENDOR");
////          productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
////          productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
////      }
////
////      List<Element> productListElement = new ArrayList<Element>();
////      for (VirtualGlass productData : productList)
////      {
////          Element productElementOld = this.generateProductElement(productData, productionType, productType,
////                                                              productSpecName, requestData.getKey().getProductRequestName(), processFlowName, processOperationName,
////                                                              productThickness, productSize, productVendor, machineRecipeName, "Y", degree);
////
////
////          //Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness, productSize, machineRecipeName,"");
////
////          productListElement.add(productElementOld);
////      }
////
////      return productListElement;
////  }
//
//    /**
//     * write down Product item
//     * @author swcho
//     * @since 2014.03.09
//     * @param productData
//     * @param productionType
//     * @param productThickness
//     * @param productSize
//     * @param productVendor
//     * @param machineRecipeName
//     * @param samplingFlag
//     * @return
//     * @throws CustomException
//     */
//    private Element generateProductElement(Product productData, String productionType, String productThickness, 
//                                          String productSize, String productVendor, String machineRecipeName, 
//                                          String samplingFlag, String degree, String TmsFlag, String CsgFlag, 
//                                          String furnaceFlag, String dummyType)
//        throws CustomException
//    {
//        Element productElement = new Element("PRODUCT");
//
//        Element lotNameElement = new Element("LOTNAME");
//        lotNameElement.setText(productData.getLotName());
//        productElement.addContent(lotNameElement);
//
//        Element productNameElement = new Element("PRODUCTNAME");
//        productNameElement.setText(productData.getKey().getProductName());
//        productElement.addContent(productNameElement);
//
//        Element productTypeElement = new Element("PRODUCTTYPE");
//        productTypeElement.setText(productData.getProductType());
//        productElement.addContent(productTypeElement);
//
//        Element processflowElement = new Element("PROCESSFLOWNAME");
//        processflowElement.setText(productData.getProcessFlowName());
//        productElement.addContent(processflowElement);
//
//        Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
//        processOperationNameElement.setText(productData.getProcessOperationName());
//        productElement.addContent(processOperationNameElement);
//
//        Element positionElement = new Element("POSITION");
//        positionElement.setText(String.valueOf(productData.getPosition()));
//        productElement.addContent(positionElement);
//
//        Element productSpecNameElement = new Element("PRODUCTSPECNAME");
//        productSpecNameElement.setText(productData.getProductSpecName());
//        productElement.addContent(productSpecNameElement);
//
//        Element productionTypeElement = new Element("PRODUCTIONTYPE");
//        //productionTypeElement.setText(productData.getProductionType());
//        productionTypeElement.setText(productionType);
//        productElement.addContent(productionTypeElement);
//
//        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
//        Element productJudgeElement = new Element("PRODUCTJUDGE");
//        productJudgeElement.setText(productData.getProductGrade());
//        //productJudgeElement.setText(productData.getUdfs().get("PRODUCTJUDGE"));
//        productElement.addContent(productJudgeElement);
//        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
//        
//        Element productGradeElement = new Element("PRODUCTGRADE");
//        productGradeElement.setText(productData.getProductGrade());
//        productElement.addContent(productGradeElement);
//
//        Element dummyTypeElement = new Element("DUMMYTYPE");
//        dummyTypeElement.setText(dummyType);
//        productElement.addContent(dummyTypeElement);
//
//        Element tmsFlagElement = new Element("TMSFLAG");
//        tmsFlagElement.setText(TmsFlag);
//        productElement.addContent(tmsFlagElement);
//
//        Element csgFlagElement = new Element("CSGFLAG");
//        csgFlagElement.setText(CsgFlag);
//        productElement.addContent(csgFlagElement);
//
//        Element turnFlagElement = new Element("TURNFLAG");
//        turnFlagElement.setText(degree);
//        productElement.addContent(turnFlagElement);
//
//        Element furnaceFlagElement = new Element("FURNACEFLAG");
//        furnaceFlagElement.setText(furnaceFlag);
//        productElement.addContent(furnaceFlagElement);
//
//        Element subProductJudgesElement = new Element("SUBPRODUCTJUDGES");
//        subProductJudgesElement.setText("");
//        productElement.addContent(subProductJudgesElement);
//
//        Element subProductGradesElement = new Element("SUBPRODUCTGRADES");
//        subProductGradesElement.setText("");
//        productElement.addContent(subProductGradesElement);
//
//        Element productThicknessElement = new Element("PRODUCTTHICKNESS");
//        productThicknessElement.setText(productThickness);
//        productElement.addContent(productThicknessElement);
//
//        Element workOrderElement = new Element("WORKORDER");
//        workOrderElement.setText(productData.getProductRequestName());
//        productElement.addContent(workOrderElement);
//
//        String ProductRequestName=productData.getProductRequestName();
//        if(ProductRequestName.length()<=12)
//        {
//            Element partid = new Element("PARTID");
//            partid.setText(ProductRequestName);
//            productElement.addContent(partid);
//        }
//        else
//        {
//            ProductRequestName=ProductRequestName.substring(0,12);
//            Element partid = new Element("PARTID");
//            partid.setText(ProductRequestName);
//            productElement.addContent(partid);
//        }
//
//        Element crateNameElement = new Element("CRATENAME");
//        crateNameElement.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
//        productElement.addContent(crateNameElement);
//
//        Element productSizeElement = new Element("PRODUCTSIZE");
//        productSizeElement.setText(productSize);
//        productElement.addContent(productSizeElement);
//
//        Element glassMakerElement = new Element("GLASSMAKER");
//        glassMakerElement.setText(productVendor);
//        productElement.addContent(glassMakerElement);
//
//        Element productRecipeElement = new Element("PRODUCTRECIPE");
//
//        productRecipeElement.setText(machineRecipeName);
//        productElement.addContent(productRecipeElement);
//
//        Element exeposureUnitId = new Element("EXPOSUREUNITID");
//        exeposureUnitId.setText("");
//        productElement.addContent(exeposureUnitId);
//
//        Element exeposureRecipeName = new Element("EXPOSURERECIPENAME");
//        exeposureRecipeName.setText("");
//        productElement.addContent(exeposureRecipeName);
//
//        Element reworkCountElement = new Element("REWORKCOUNT");
//        reworkCountElement.setText(String.valueOf(productData.getReworkCount()));
//        productElement.addContent(reworkCountElement);
//
//        Element dummyCountElement = new Element("DUMMYUSEDCOUNT");
//        dummyCountElement.setText(CommonUtil.getValue(productData.getUdfs(), "DUMMYUSEDCOUNT"));
//        productElement.addContent(dummyCountElement);
//
//        Element maskNameElement = new Element("MASKNAME");
//        maskNameElement.setText("");
//        productElement.addContent(maskNameElement);
//
//        Element proberNameElement = new Element("PROBERNAME");
//        proberNameElement.setText("");
//        productElement.addContent(proberNameElement);
//
//        Element processingInfoElement = new Element("PROCESSINGINFO");
//        processingInfoElement.setText("");
//        productElement.addContent(processingInfoElement);
//
//        Element samplingFlagElement = new Element("SAMPLINGFLAG");
//        samplingFlagElement.setText(samplingFlag);
//        productElement.addContent(samplingFlagElement);
//
//        Element arrayCutRepairType = new Element("ARRAYCUTREPAIRTYPE");
//        arrayCutRepairType.setText("");
//        productElement.addContent(arrayCutRepairType);
//
//        Element lcvdRepairType = new Element("LCVDREPAIRTYPE");
//        lcvdRepairType.setText("");
//        productElement.addContent(lcvdRepairType);
//
//        Element lastProcessEndTime = new Element("LASTPROCESSENDTIME");
//        lastProcessEndTime.setText(CommonUtil.getValue(productData.getUdfs(), "LASTPROCESSENDTIME"));
//        productElement.addContent(lastProcessEndTime);
//
//        Element ELARecipeName = new Element("ELARECIPENAME");
//        ELARecipeName.setText(CommonUtil.getValue(productData.getUdfs(), "ELARECIPENAME"));
//        productElement.addContent(ELARecipeName);
//
//        Element ELAEnergyUsage = new Element("ELAENERGYUSAGE");
//        ELAEnergyUsage.setText(CommonUtil.getValue(productData.getUdfs(), "ELAENERGYUSAGE"));
//        productElement.addContent(ELAEnergyUsage);
//
//        return productElement;
//    }
//
//    /**
//     * write down Product item
//     * @author wghuang
//     * @since 2014.03.31
//     * @param productData
//     * @param productionType
//     * @param productThickness
//     * @param productSize
//     * @param machineRecipeName
//     * @return
//     * @throws CustomException
//     */
//    private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
//                                            String productSize, String machineRecipeName, String samplingFlag,
//                                            String Bending, String machineConstructType, ProductSpec productSpecData)
//        throws CustomException
//    {
//        Element productElement = new Element("PRODUCT");
//
//        //productName
//        Element productNameE = new Element("PRODUCTNAME");
//        productNameE.setText(productData.getKey().getProductName());
//        productElement.addContent(productNameE);
//
//        //position
//        Element positionE = new Element("POSITION");
//        positionE.setText(String.valueOf(productData.getPosition()));
//        productElement.addContent(positionE);
//
//        //lotName
//        Element lotNameE = new Element("LOTNAME");
//        lotNameE.setText(productData.getLotName());
//        productElement.addContent(lotNameE);
//
//        //productSpecName
//        Element productSpecNameE = new Element("PRODUCTSPECNAME");
//        productSpecNameE.setText(productData.getProductSpecName());
//        productElement.addContent(productSpecNameE);
//
//        //productionType
//        Element productionTypeE = new Element("PRODUCTIONTYPE");
//        productionTypeE.setText(productionType);
//        productElement.addContent(productionTypeE);
//
//        //productType
//        Element productTypeE = new Element("PRODUCTTYPE");
//        productTypeE.setText(productData.getProductType());
//        productElement.addContent(productTypeE);
//
//        //processflow
//        Element processflowE = new Element("PROCESSFLOWNAME");
//        processflowE.setText(productData.getProcessFlowName());
//        productElement.addContent(processflowE);
//
//        //processOperationName
//        Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
//        processOperationNameE.setText(productData.getProcessOperationName());
//        productElement.addContent(processOperationNameE);
//
//        //productSize
//        Element productSizeE = new Element("PRODUCTSIZE");
//        productSizeE.setText(productSize);
//        productElement.addContent(productSizeE);
//
//        //workOrder
//        Element workOrderE = new Element("WORKORDER");
//        workOrderE.setText(productData.getProductRequestName());
//        productElement.addContent(workOrderE);
//
//        //reworkCount
//        Element reworkCountE = new Element("REWORKCOUNT");
//        reworkCountE.setText(String.valueOf(productData.getReworkCount()));
//        productElement.addContent(reworkCountE);
//
//        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
//        //productJudge
//        Element productJudgeE = new Element("PRODUCTJUDGE");
//        productJudgeE.setText(productData.getProductGrade());
//        //productJudgeE.setText(productData.getUdfs().get("PRODUCTJUDGE"));
//        productElement.addContent(productJudgeE);
//        /* <== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
//        
//        //PanelProdcutJudges
//        Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
//        /* 20180722, hhlee, Add, Panel Product Judge ==>> */
//        //ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
//        // OLED : [ O : Good | X : No good ]
//        String panelProductJudge = ExtendedObjectProxy.getPanelJudgeService().getPanelProductJudge(productData.getKey().getProductName(), productData.getProductType(), false);
//        panelProductJudgesE.setText(panelProductJudge);
//        productElement.addContent(panelProductJudgesE);
//        /* <<== 20180722, hhlee, Add, Panel Product Judge */
//        
//        //productRecipe
//        Element productRecipeE = new Element("PRODUCTRECIPE");
//        productRecipeE.setText(machineRecipeName);
//        productElement.addContent(productRecipeE);
//
//        //crateName
//        Element crateNameE = new Element("CRATENAME");
//        crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
//        productElement.addContent(crateNameE);
//
//        //productThickness
//        Element productThicknessE = new Element("PRODUCTTHICKNESS");
//        productThicknessE.setText(productThickness);
//        productElement.addContent(productThicknessE);
//
//        /* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
//        String exposureUnitId = StringUtil.EMPTY;
//        String exposureRecipeName = StringUtil.EMPTY;
//        String maskName = StringUtil.EMPTY;
//        if (machineConstructType.equals("CDME"))
//        {
//            ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
//                    productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
//            if(pExposureFeedBackData != null)
//            {
//                exposureUnitId = pExposureFeedBackData.getUnitName();
//                exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
//                maskName = pExposureFeedBackData.getMaskName();
//            }
//        }
//        //exeposureUnitId
//        Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
//        exeposureUnitIdE.setText(exposureUnitId);
//        productElement.addContent(exeposureUnitIdE);
//
//        //exeposureRecipeName
//        Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
//        exeposureRecipeNameE.setText(exposureRecipeName);
//        productElement.addContent(exeposureRecipeNameE);
//
//        //MaskName
//        Element maskNameE = new Element("MASKNAME");
//        maskNameE.setText(maskName);
//        productElement.addContent(maskNameE);
//        /* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */
//
//        //ChamberID
//        Element chamberIDE = new Element("CHAMBERID");
//        chamberIDE.setText("");
//        productElement.addContent(chamberIDE);
//
//        //slotPriority
//        Element slotPriorityE = new Element("SLOTPRIORITY");
//        slotPriorityE.setText("");
//        productElement.addContent(slotPriorityE);
//
//        //slotBending
//        Element slotBendingE = new Element("SLOTBENDING");
//        slotBendingE.setText("");
//        productElement.addContent(slotBendingE);
//
//        /* 20180531, Get Product Process Data(Last 5 processes) ==>> */
//        //ProcessList
//        Element processListElement = new Element("PROCESSLIST");
//        //Element processElement = new Element("PROCESS");
//        //
//        //Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
//        //processOperationNameLElement.setText("");
//        //processElement.addContent(processOperationNameLElement);
//        //
//        //Element processMachineNameElement = new Element("PROCESSMACHINENAME");
//        //processMachineNameElement.setText("");
//        //processElement.addContent(processMachineNameElement);
//        //
//        //processListElement.addContent(processElement);
//        if (machineConstructType.equals("AOI"))
//        {
//            processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
//        }
//
//        /* <<== 20180531, Get Product Process Data(Last 5 processes) */
//        productElement.addContent(processListElement);
//
//
//        /* Array Product Flag Setting ==>> */
//        productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
//        /* <== Array Product Flag Setting */
//
//        ////TurnOverFlag
//        //Element turnOverFlagElement = new Element("TURNOVERFLAG");
//        //turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
//        //productElement.addContent(turnOverFlagElement);
//
//        ////TurnOverFlag
//        //Element turnSideFlagElement = new Element("TURNSIDEFLAG");
//        //turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
//        //productElement.addContent(turnSideFlagElement);
//
//        //SamplingFlag
//        Element samplingFlagElement = new Element("SAMPLINGFLAG");
//        samplingFlagElement.setText(samplingFlag);
//        productElement.addContent(samplingFlagElement);
//
//        return productElement;
//    }
//
//    /**
//     * @Name     generateEDOProductElement
//     * @since    2018. 6. 21.
//     * @author   hhlee
//     * @contents 
//     * @param productData
//     * @param productionType
//     * @param productThickness
//     * @param productSize
//     * @param machineRecipeName
//     * @param samplingFlag
//     * @param Bending
//     * @param machineConstructType
//     * @param productSpecData
//     * @param chamberGroupName
//     * @return
//     * @throws CustomException
//     */
//    private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
//                                            String productSize, String machineRecipeName, String samplingFlag,
//                                            String bending, String machineConstructType, ProductSpec productSpecData,
//                                            String chamberGroupName, String productRecipeName)
//        throws CustomException
//    {
//        Element productElement = new Element("PRODUCT");
//
//        //productName
//        Element productNameE = new Element("PRODUCTNAME");
//        productNameE.setText(productData.getKey().getProductName());
//        productElement.addContent(productNameE);
//
//        //position
//        Element positionE = new Element("POSITION");
//        positionE.setText(String.valueOf(productData.getPosition()));
//        productElement.addContent(positionE);
//
//        //lotName
//        Element lotNameE = new Element("LOTNAME");
//        lotNameE.setText(productData.getLotName());
//        productElement.addContent(lotNameE);
//
//        //productSpecName
//        Element productSpecNameE = new Element("PRODUCTSPECNAME");
//        productSpecNameE.setText(productData.getProductSpecName());
//        productElement.addContent(productSpecNameE);
//
//        //productionType
//        Element productionTypeE = new Element("PRODUCTIONTYPE");
//        productionTypeE.setText(productionType);
//        productElement.addContent(productionTypeE);
//
//        //productType
//        Element productTypeE = new Element("PRODUCTTYPE");
//        productTypeE.setText(productData.getProductType());
//        productElement.addContent(productTypeE);
//
//        //processflow
//        Element processflowE = new Element("PROCESSFLOWNAME");
//        processflowE.setText(productData.getProcessFlowName());
//        productElement.addContent(processflowE);
//
//        //processOperationName
//        Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
//        processOperationNameE.setText(productData.getProcessOperationName());
//        productElement.addContent(processOperationNameE);
//
//        //productSize
//        Element productSizeE = new Element("PRODUCTSIZE");
//        productSizeE.setText(productSize);
//        productElement.addContent(productSizeE);
//
//        //workOrder
//        Element workOrderE = new Element("WORKORDER");
//        workOrderE.setText(productData.getProductRequestName());
//        productElement.addContent(workOrderE);
//
//        //reworkCount
//        Element reworkCountE = new Element("REWORKCOUNT");
//        reworkCountE.setText(String.valueOf(productData.getReworkCount()));
//        productElement.addContent(reworkCountE);
//
//        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
//        //productJudge
//        Element productJudgeE = new Element("PRODUCTJUDGE");
//        productJudgeE.setText(productData.getProductGrade());
//        //productJudgeE.setText(productData.getUdfs().get("PRODUCTJUDGE"));
//        productElement.addContent(productJudgeE);
//        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
//        
//        //PanelProdcutJudges
//        Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
//        /* 20180722, hhlee, Add, Panel Product Judge ==>> */
//        //ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
//        // OLED : [ O : Good | X : No good ]
//        String panelProductJudge = ExtendedObjectProxy.getPanelJudgeService().getPanelProductJudge(productData.getKey().getProductName(), productData.getProductType(), false);
//        panelProductJudgesE.setText(panelProductJudge);        
//        productElement.addContent(panelProductJudgesE);
//
//        //productRecipe
//        /* 20190117, hhlee, delete, Change MQC Recipe Logic ==>> */
//        //Element productRecipeE = new Element("PRODUCTRECIPE");
//        //String recipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
//        //if(!StringUtil.isEmpty(recipeName))
//        //{
//        //    productRecipeE.setText(recipeName);
//        //}
//        //else
//        //{
//        //    productRecipeE.setText(machineRecipeName);
//        //}        
//        Element productRecipeE = new Element("PRODUCTRECIPE");
//        productRecipeE.setText(productRecipeName);
//        productElement.addContent(productRecipeE);
//        /* 20190117, hhlee, delete, Change MQC Recipe Logic ==>> */
//        
//        //crateName
//        Element crateNameE = new Element("CRATENAME");
//        crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
//        productElement.addContent(crateNameE);
//
//        //productThickness
//        Element productThicknessE = new Element("PRODUCTTHICKNESS");
//        productThicknessE.setText(productThickness);
//        productElement.addContent(productThicknessE);
//
//        /* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
//        String exposureUnitId = StringUtil.EMPTY;
//        String exposureRecipeName = StringUtil.EMPTY;
//        String maskName = StringUtil.EMPTY;
//        if (machineConstructType.equals("CDME"))
//        {
//            ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
//                    productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
//            if(pExposureFeedBackData != null)
//            {
//                exposureUnitId = pExposureFeedBackData.getUnitName();
//                exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
//                maskName = pExposureFeedBackData.getMaskName();
//            }
//        }
//        //exeposureUnitId
//        Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
//        exeposureUnitIdE.setText(exposureUnitId);
//        productElement.addContent(exeposureUnitIdE);
//
//        //exeposureRecipeName
//        Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
//        exeposureRecipeNameE.setText(exposureRecipeName);
//        productElement.addContent(exeposureRecipeNameE);
//
//        //MaskName
//        Element maskNameE = new Element("MASKNAME");
//        maskNameE.setText(maskName);
//        productElement.addContent(maskNameE);
//        /* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */
//
//        //ChamberID
//        Element chamberIDE = new Element("CHAMBERID");
//        chamberIDE.setText(chamberGroupName);
//        productElement.addContent(chamberIDE);
//
//        //slotPriority
//        Element slotPriorityE = new Element("SLOTPRIORITY");
//        slotPriorityE.setText("");
//        productElement.addContent(slotPriorityE);
//
//        //slotBending
//        Element slotBendingE = new Element("SLOTBENDING");
//        slotBendingE.setText(bending);
//        productElement.addContent(slotBendingE);
//
//        /* 20180531, Get Product Process Data(Last 5 processes) ==>> */
//        //ProcessList
//        Element processListElement = new Element("PROCESSLIST");
//        //Element processElement = new Element("PROCESS");
//        //
//        //Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
//        //processOperationNameLElement.setText("");
//        //processElement.addContent(processOperationNameLElement);
//        //
//        //Element processMachineNameElement = new Element("PROCESSMACHINENAME");
//        //processMachineNameElement.setText("");
//        //processElement.addContent(processMachineNameElement);
//        //
//        //processListElement.addContent(processElement);
//        if (machineConstructType.equals("AOI"))
//        {
//            processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
//        }
//
//        /* <<== 20180531, Get Product Process Data(Last 5 processes) */
//        productElement.addContent(processListElement);
//
//
//        /* Array Product Flag Setting ==>> */
//        productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
//        /* <== Array Product Flag Setting */
//
//        ////TurnOverFlag
//        //Element turnOverFlagElement = new Element("TURNOVERFLAG");
//        //turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
//        //productElement.addContent(turnOverFlagElement);
//
//        ////TurnOverFlag
//        //Element turnSideFlagElement = new Element("TURNSIDEFLAG");
//        //turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
//        //productElement.addContent(turnSideFlagElement);
//
//        //SamplingFlag
//        Element samplingFlagElement = new Element("SAMPLINGFLAG");
//        samplingFlagElement.setText(samplingFlag);
//        productElement.addContent(samplingFlagElement);
//
//        return productElement;
//    }
//    
////    /**
////     * @Name     generateEDOProductElement
////     * @since    2018. 6. 21.
////     * @author   hhlee
////     * @contents 
////     * @param productData
////     * @param productionType
////     * @param productThickness
////     * @param productSize
////     * @param machineRecipeName
////     * @param samplingFlag
////     * @param Bending
////     * @param machineConstructType
////     * @param productSpecData
////     * @param chamberGroupName
////     * @return
////     * @throws CustomException
////     */
////    private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
////                                            String productSize, String machineRecipeName, String samplingFlag,
////                                            String bending, String machineConstructType, ProductSpec productSpecData,
////                                            String chamberGroupName)
////        throws CustomException
////    {
////        Element productElement = new Element("PRODUCT");
////
////        //productName
////        Element productNameE = new Element("PRODUCTNAME");
////        productNameE.setText(productData.getKey().getProductName());
////        productElement.addContent(productNameE);
////
////        //position
////        Element positionE = new Element("POSITION");
////        positionE.setText(String.valueOf(productData.getPosition()));
////        productElement.addContent(positionE);
////
////        //lotName
////        Element lotNameE = new Element("LOTNAME");
////        lotNameE.setText(productData.getLotName());
////        productElement.addContent(lotNameE);
////
////        //productSpecName
////        Element productSpecNameE = new Element("PRODUCTSPECNAME");
////        productSpecNameE.setText(productData.getProductSpecName());
////        productElement.addContent(productSpecNameE);
////
////        //productionType
////        Element productionTypeE = new Element("PRODUCTIONTYPE");
////        productionTypeE.setText(productionType);
////        productElement.addContent(productionTypeE);
////
////        //productType
////        Element productTypeE = new Element("PRODUCTTYPE");
////        productTypeE.setText(productData.getProductType());
////        productElement.addContent(productTypeE);
////
////        //processflow
////        Element processflowE = new Element("PROCESSFLOWNAME");
////        processflowE.setText(productData.getProcessFlowName());
////        productElement.addContent(processflowE);
////
////        //processOperationName
////        Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
////        processOperationNameE.setText(productData.getProcessOperationName());
////        productElement.addContent(processOperationNameE);
////
////        //productSize
////        Element productSizeE = new Element("PRODUCTSIZE");
////        productSizeE.setText(productSize);
////        productElement.addContent(productSizeE);
////
////        //workOrder
////        Element workOrderE = new Element("WORKORDER");
////        workOrderE.setText(productData.getProductRequestName());
////        productElement.addContent(workOrderE);
////
////        //reworkCount
////        Element reworkCountE = new Element("REWORKCOUNT");
////        reworkCountE.setText(String.valueOf(productData.getReworkCount()));
////        productElement.addContent(reworkCountE);
////
////        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
////        //productJudge
////        Element productJudgeE = new Element("PRODUCTJUDGE");
////        productJudgeE.setText(productData.getProductGrade());
////        //productJudgeE.setText(productData.getUdfs().get("PRODUCTJUDGE"));
////        productElement.addContent(productJudgeE);
////        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
////        
////        //PanelProdcutJudges
////        Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
////        /* 20180722, hhlee, Add, Panel Product Judge ==>> */
////        //ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
////        // OLED : [ O : Good | X : No good ]
////        String panelProductJudge = ExtendedObjectProxy.getPanelJudgeService().getPanelProductJudge(productData.getKey().getProductName(), productData.getProductType(), false);
////        panelProductJudgesE.setText(panelProductJudge);        
////        productElement.addContent(panelProductJudgesE);
////
////        //productRecipe
////        Element productRecipeE = new Element("PRODUCTRECIPE");
////        String recipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
////        if(!StringUtil.isEmpty(recipeName))
////        {
////            productRecipeE.setText(recipeName);
////        }
////        else
////        {
////            productRecipeE.setText(machineRecipeName);
////        }
////        
////        productElement.addContent(productRecipeE);
////
////        //crateName
////        Element crateNameE = new Element("CRATENAME");
////        crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
////        productElement.addContent(crateNameE);
////
////        //productThickness
////        Element productThicknessE = new Element("PRODUCTTHICKNESS");
////        productThicknessE.setText(productThickness);
////        productElement.addContent(productThicknessE);
////
////        /* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
////        String exposureUnitId = StringUtil.EMPTY;
////        String exposureRecipeName = StringUtil.EMPTY;
////        String maskName = StringUtil.EMPTY;
////        if (machineConstructType.equals("CDME"))
////        {
////            ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
////                    productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
////            if(pExposureFeedBackData != null)
////            {
////                exposureUnitId = pExposureFeedBackData.getUnitName();
////                exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
////                maskName = pExposureFeedBackData.getMaskName();
////            }
////        }
////        //exeposureUnitId
////        Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
////        exeposureUnitIdE.setText(exposureUnitId);
////        productElement.addContent(exeposureUnitIdE);
////
////        //exeposureRecipeName
////        Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
////        exeposureRecipeNameE.setText(exposureRecipeName);
////        productElement.addContent(exeposureRecipeNameE);
////
////        //MaskName
////        Element maskNameE = new Element("MASKNAME");
////        maskNameE.setText(maskName);
////        productElement.addContent(maskNameE);
////        /* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */
////
////        //ChamberID
////        Element chamberIDE = new Element("CHAMBERID");
////        chamberIDE.setText(chamberGroupName);
////        productElement.addContent(chamberIDE);
////
////        //slotPriority
////        Element slotPriorityE = new Element("SLOTPRIORITY");
////        slotPriorityE.setText("");
////        productElement.addContent(slotPriorityE);
////
////        //slotBending
////        Element slotBendingE = new Element("SLOTBENDING");
////        slotBendingE.setText(bending);
////        productElement.addContent(slotBendingE);
////
////        /* 20180531, Get Product Process Data(Last 5 processes) ==>> */
////        //ProcessList
////        Element processListElement = new Element("PROCESSLIST");
////        //Element processElement = new Element("PROCESS");
////        //
////        //Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
////        //processOperationNameLElement.setText("");
////        //processElement.addContent(processOperationNameLElement);
////        //
////        //Element processMachineNameElement = new Element("PROCESSMACHINENAME");
////        //processMachineNameElement.setText("");
////        //processElement.addContent(processMachineNameElement);
////        //
////        //processListElement.addContent(processElement);
////        if (machineConstructType.equals("AOI"))
////        {
////            processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
////        }
////
////        /* <<== 20180531, Get Product Process Data(Last 5 processes) */
////        productElement.addContent(processListElement);
////
////
////        /* Array Product Flag Setting ==>> */
////        productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
////        /* <== Array Product Flag Setting */
////
////        ////TurnOverFlag
////        //Element turnOverFlagElement = new Element("TURNOVERFLAG");
////        //turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
////        //productElement.addContent(turnOverFlagElement);
////
////        ////TurnOverFlag
////        //Element turnSideFlagElement = new Element("TURNSIDEFLAG");
////        //turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
////        //productElement.addContent(turnSideFlagElement);
////
////        //SamplingFlag
////        Element samplingFlagElement = new Element("SAMPLINGFLAG");
////        samplingFlagElement.setText(samplingFlag);
////        productElement.addContent(samplingFlagElement);
////
////        return productElement;
////    }
//    
////    /**
////     * @Name     generateEDOProductElement
////     * @since    2018. 6. 21.
////     * @author   hhlee
////     * @contents 
////     * @param productData
////     * @param productionType
////     * @param productThickness
////     * @param productSize
////     * @param machineRecipeName
////     * @param samplingFlag
////     * @param Bending
////     * @param machineConstructType
////     * @param productSpecData
////     * @param chamberGroupName
////     * @return
////     * @throws CustomException
////     */
////    private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
////                                            String productSize, String machineRecipeName, String samplingFlag,
////                                            String bending, String machineConstructType, ProductSpec productSpecData,
////                                            String chamberGroupName)
////        throws CustomException
////    {
////        Element productElement = new Element("PRODUCT");
////
////        //productName
////        Element productNameE = new Element("PRODUCTNAME");
////        productNameE.setText(productData.getKey().getProductName());
////        productElement.addContent(productNameE);
////
////        //position
////        Element positionE = new Element("POSITION");
////        positionE.setText(String.valueOf(productData.getPosition()));
////        productElement.addContent(positionE);
////
////        //lotName
////        Element lotNameE = new Element("LOTNAME");
////        lotNameE.setText(productData.getLotName());
////        productElement.addContent(lotNameE);
////
////        //productSpecName
////        Element productSpecNameE = new Element("PRODUCTSPECNAME");
////        productSpecNameE.setText(productData.getProductSpecName());
////        productElement.addContent(productSpecNameE);
////
////        //productionType
////        Element productionTypeE = new Element("PRODUCTIONTYPE");
////        productionTypeE.setText(productionType);
////        productElement.addContent(productionTypeE);
////
////        //productType
////        Element productTypeE = new Element("PRODUCTTYPE");
////        productTypeE.setText(productData.getProductType());
////        productElement.addContent(productTypeE);
////
////        //processflow
////        Element processflowE = new Element("PROCESSFLOWNAME");
////        processflowE.setText(productData.getProcessFlowName());
////        productElement.addContent(processflowE);
////
////        //processOperationName
////        Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
////        processOperationNameE.setText(productData.getProcessOperationName());
////        productElement.addContent(processOperationNameE);
////
////        //productSize
////        Element productSizeE = new Element("PRODUCTSIZE");
////        productSizeE.setText(productSize);
////        productElement.addContent(productSizeE);
////
////        //workOrder
////        Element workOrderE = new Element("WORKORDER");
////        workOrderE.setText(productData.getProductRequestName());
////        productElement.addContent(workOrderE);
////
////        //reworkCount
////        Element reworkCountE = new Element("REWORKCOUNT");
////        reworkCountE.setText(String.valueOf(productData.getReworkCount()));
////        productElement.addContent(reworkCountE);
////
////        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
////        //productJudge
////        Element productJudgeE = new Element("PRODUCTJUDGE");
////        productJudgeE.setText(productData.getProductGrade());
////        //productJudgeE.setText(productData.getUdfs().get("PRODUCTJUDGE"));
////        productElement.addContent(productJudgeE);
////        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
////        
////        //PanelProdcutJudges
////        Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
////        /* 20180722, hhlee, Add, Panel Product Judge ==>> */
////        //ARRAY : [ O : Not Inspection | G : Good  | X : No good | R : Rework ]
////        // OLED : [ O : Good | X : No good ]
////        String panelProductJudge = ExtendedObjectProxy.getPanelJudgeService().getPanelProductJudge(productData.getKey().getProductName(), productData.getProductType(), false);
////        panelProductJudgesE.setText(panelProductJudge);        
////        productElement.addContent(panelProductJudgesE);
////
////        //productRecipe
////        Element productRecipeE = new Element("PRODUCTRECIPE");
////        String recipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
////        if(!StringUtil.isEmpty(recipeName))
////        {
////            productRecipeE.setText(recipeName);
////        }
////        else
////        {
////            productRecipeE.setText(machineRecipeName);
////        }
////        
////        productElement.addContent(productRecipeE);
////
////        //crateName
////        Element crateNameE = new Element("CRATENAME");
////        crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
////        productElement.addContent(crateNameE);
////
////        //productThickness
////        Element productThicknessE = new Element("PRODUCTTHICKNESS");
////        productThicknessE.setText(productThickness);
////        productElement.addContent(productThicknessE);
////
////        /* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
////        String exposureUnitId = StringUtil.EMPTY;
////        String exposureRecipeName = StringUtil.EMPTY;
////        String maskName = StringUtil.EMPTY;
////        if (machineConstructType.equals("CDME"))
////        {
////            ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
////                    productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
////            if(pExposureFeedBackData != null)
////            {
////                exposureUnitId = pExposureFeedBackData.getUnitName();
////                exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
////                maskName = pExposureFeedBackData.getMaskName();
////            }
////        }
////        //exeposureUnitId
////        Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
////        exeposureUnitIdE.setText(exposureUnitId);
////        productElement.addContent(exeposureUnitIdE);
////
////        //exeposureRecipeName
////        Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
////        exeposureRecipeNameE.setText(exposureRecipeName);
////        productElement.addContent(exeposureRecipeNameE);
////
////        //MaskName
////        Element maskNameE = new Element("MASKNAME");
////        maskNameE.setText(maskName);
////        productElement.addContent(maskNameE);
////        /* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */
////
////        //ChamberID
////        Element chamberIDE = new Element("CHAMBERID");
////        chamberIDE.setText(chamberGroupName);
////        productElement.addContent(chamberIDE);
////
////        //slotPriority
////        Element slotPriorityE = new Element("SLOTPRIORITY");
////        slotPriorityE.setText("");
////        productElement.addContent(slotPriorityE);
////
////        //slotBending
////        Element slotBendingE = new Element("SLOTBENDING");
////        slotBendingE.setText(bending);
////        productElement.addContent(slotBendingE);
////
////        /* 20180531, Get Product Process Data(Last 5 processes) ==>> */
////        //ProcessList
////        Element processListElement = new Element("PROCESSLIST");
////        //Element processElement = new Element("PROCESS");
////        //
////        //Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
////        //processOperationNameLElement.setText("");
////        //processElement.addContent(processOperationNameLElement);
////        //
////        //Element processMachineNameElement = new Element("PROCESSMACHINENAME");
////        //processMachineNameElement.setText("");
////        //processElement.addContent(processMachineNameElement);
////        //
////        //processListElement.addContent(processElement);
////        if (machineConstructType.equals("AOI"))
////        {
////            processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
////        }
////
////        /* <<== 20180531, Get Product Process Data(Last 5 processes) */
////        productElement.addContent(processListElement);
////
////
////        /* Array Product Flag Setting ==>> */
////        productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
////        /* <== Array Product Flag Setting */
////
////        ////TurnOverFlag
////        //Element turnOverFlagElement = new Element("TURNOVERFLAG");
////        //turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
////        //productElement.addContent(turnOverFlagElement);
////
////        ////TurnOverFlag
////        //Element turnSideFlagElement = new Element("TURNSIDEFLAG");
////        //turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
////        //productElement.addContent(turnSideFlagElement);
////
////        //SamplingFlag
////        Element samplingFlagElement = new Element("SAMPLINGFLAG");
////        samplingFlagElement.setText(samplingFlag);
////        productElement.addContent(samplingFlagElement);
////
////        return productElement;
////    }
//    
//    /**
//     * write down virtual Product item
//     * @since 2016.04.11
//     * @author swcho
//     * @param productData
//     * @param productionType
//     * @param productSpecName
//     * @param processFlowName
//     * @param processOperationName
//     * @param productThickness
//     * @param productSize
//     * @param productVendor
//     * @param machineRecipeName
//     * @param samplingFlag
//     * @return
//     * @throws CustomException
//     */
//    private Element generateProductElement(VirtualGlass productData, String productionType, String productType,
//                                            String productSpecName, String productRequestName, String processFlowName, String processOperationName,
//                                            String productThickness, String productSize, String productVendor,
//                                            String machineRecipeName, String samplingFlag, String degree)
//        throws CustomException
//    {
//        Element productElement = new Element("PRODUCT");
//
//        Element lotNameElement = new Element("LOTNAME");
//        lotNameElement.setText("");
//        productElement.addContent(lotNameElement);
//
//        Element productNameElement = new Element("PRODUCTNAME");
//        productNameElement.setText(productData.getVirtualGlassName());
//        productElement.addContent(productNameElement);
//
//        Element productTypeElement = new Element("PRODUCTTYPE");
//        productTypeElement.setText(productType);
//        productElement.addContent(productTypeElement);
//
//        Element processflowElement = new Element("PROCESSFLOWNAME");
//        processflowElement.setText(processFlowName);
//        productElement.addContent(processflowElement);
//
//        Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
//        processOperationNameElement.setText(processOperationName);
//        productElement.addContent(processOperationNameElement);
//
//        Element positionElement = new Element("POSITION");
//        positionElement.setText(String.valueOf(productData.getPosition()));
//        productElement.addContent(positionElement);
//
//        Element productSpecNameElement = new Element("PRODUCTSPECNAME");
//        productSpecNameElement.setText(productSpecName);
//        productElement.addContent(productSpecNameElement);
//
//        Element productionTypeElement = new Element("PRODUCTIONTYPE");
//        productionTypeElement.setText(productionType);
//        productElement.addContent(productionTypeElement);
//
//        /* 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE ==>> */
//        Element productJudgeElement = new Element("PRODUCTJUDGE");
//        productJudgeElement.setText(productData.getGrade());
//        //productJudgeElement.setText(productData.getUdfs().get("PRODUCTJUDGE"));
//        productElement.addContent(productJudgeElement);
//        /* <<== 20181113, hhlee, modify, PRODUCTJUDGE = PRODUCTGRADE */
//        
//        Element productGradeElement = new Element("PRODUCTGRADE");
//        productGradeElement.setText(productData.getGrade());
//        productElement.addContent(productGradeElement);
//
//        Element dummyTypeElement = new Element("DUMMYTYPE");
//        dummyTypeElement.setText("");
//        productElement.addContent(dummyTypeElement);
//
//        Element tmsFlagElement = new Element("TMSFLAG");
//        tmsFlagElement.setText("");
//        productElement.addContent(tmsFlagElement);
//
//        Element turnFlagElement = new Element("TURNFLAG");
//        turnFlagElement.setText(degree);
//        productElement.addContent(turnFlagElement);
//
//        Element furnaceFlagElement = new Element("FURNACEFLAG");
//        furnaceFlagElement.setText("");
//        productElement.addContent(furnaceFlagElement);
//
//        Element subProductJudgesElement = new Element("SUBPRODUCTJUDGES");
//        subProductJudgesElement.setText("");
//        productElement.addContent(subProductJudgesElement);
//
//        Element subProductGradesElement = new Element("SUBPRODUCTGRADES");
//        subProductGradesElement.setText("");
//        productElement.addContent(subProductGradesElement);
//
//        Element productThicknessElement = new Element("PRODUCTTHICKNESS");
//        productThicknessElement.setText(productThickness);
//        productElement.addContent(productThicknessElement);
//
//        Element workOrderElement = new Element("WORKORDER");
//        workOrderElement.setText(productRequestName);
//        productElement.addContent(workOrderElement);
//
//        Element crateNameElement = new Element("CRATENAME");
//        crateNameElement.setText(productData.getCrateName());
//        productElement.addContent(crateNameElement);
//
//        Element productSizeElement = new Element("PRODUCTSIZE");
//        productSizeElement.setText(productSize);
//        productElement.addContent(productSizeElement);
//
//        Element glassMakerElement = new Element("GLASSMAKER");
//        glassMakerElement.setText(productVendor);
//        productElement.addContent(glassMakerElement);
//
//        Element productRecipeElement = new Element("PRODUCTRECIPE");
//
//        productRecipeElement.setText(machineRecipeName);
//        productElement.addContent(productRecipeElement);
//
//        Element exeposureUnitId = new Element("EXPOSUREUNITID");
//        exeposureUnitId.setText("");
//        productElement.addContent(exeposureUnitId);
//
//        Element exeposureRecipeName = new Element("EXPOSURERECIPENAME");
//        exeposureRecipeName.setText("");
//        productElement.addContent(exeposureRecipeName);
//
//        Element reworkCountElement = new Element("REWORKCOUNT");
//        reworkCountElement.setText("0");
//        productElement.addContent(reworkCountElement);
//
//        Element dummyCountElement = new Element("DUMMYUSEDCOUNT");
//        dummyCountElement.setText("0");
//        productElement.addContent(dummyCountElement);
//
//        Element maskNameElement = new Element("MASKNAME");
//        maskNameElement.setText("");
//        productElement.addContent(maskNameElement);
//
//        Element proberNameElement = new Element("PROBERNAME");
//        proberNameElement.setText("");
//        productElement.addContent(proberNameElement);
//
//        Element processingInfoElement = new Element("PROCESSINGINFO");
//        processingInfoElement.setText("");
//        productElement.addContent(processingInfoElement);
//
//        Element samplingFlagElement = new Element("SAMPLINGFLAG");
//        samplingFlagElement.setText(samplingFlag);
//        productElement.addContent(samplingFlagElement);
//
//        Element arrayCutRepairType = new Element("ARRAYCUTREPAIRTYPE");
//        arrayCutRepairType.setText("");
//        productElement.addContent(arrayCutRepairType);
//
//        Element lcvdRepairType = new Element("LCVDREPAIRTYPE");
//        lcvdRepairType.setText("");
//        productElement.addContent(lcvdRepairType);
//
//        Element lastProcessEndTime = new Element("LASTPROCESSENDTIME");
//        lastProcessEndTime.setText("");
//        productElement.addContent(lastProcessEndTime);
//
//        Element ELARecipeName = new Element("ELARECIPENAME");
//        ELARecipeName.setText("");
//        productElement.addContent(ELARecipeName);
//
//        Element ELAEnergyUsage = new Element("ELAENERGYUSAGE");
//        ELAEnergyUsage.setText("");
//        productElement.addContent(ELAEnergyUsage);
//
//        return productElement;
//    }
//
//    /**
//     * Glass determine going to proceed
//     * @author swcho
//     * @since 2015.03.10
//     * @param doc
//     * @param lotData
//     * @param durableData
//     * @param slotMap
//     * @throws CustomException
//     */
//    private String doGlassSelection(Document doc, Lot lotData, Durable durableData, String slotMap)
//        throws CustomException
//    {
//        StringBuffer slotMapTemp = new StringBuffer();
//
//         for (long i=0; i<durableData.getCapacity(); i++)
//         {
//             slotMapTemp.append(GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
//         }
//
//         if (!slotMap.isEmpty() && (slotMap.length() != slotMapTemp.length()))
//             throw new CustomException("CST-2001", durableData.getKey().getDurableName());
//
//        List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);
//
//        for (Element eleProduct : lstDownloadProduct)
//        {
//            String sSelection = SMessageUtil.getChildText(eleProduct, "SAMPLINGFLAG", true);
//            String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
//
//            int position;
//            try
//            {
//                position = Integer.parseInt(sPosition);
//            }
//            catch (Exception ex)
//            {
//                position = 0;
//            }
//
//            if (sSelection.equalsIgnoreCase(GenericServiceProxy.getConstantMap().Flag_Y))
//            {
//                slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT);
//            }
//            else
//            {
//                slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
//            }
//
//            //Glass selection decision
//            SMessageUtil.setBodyItemValue(doc, "SLOTSEL", slotMapTemp.toString());
//
//            //modified by wghuang 20180402
//            eleProduct.removeChild("SAMPLINGFLAG");
//        }
//
//        eventLog.debug("Completed Slot Selection : " + slotMapTemp.toString() );
//        
//        // 2019.05.21_hsryu_Add Logic. Return SlotSel Note. 
//        return slotMapTemp.toString();
//    }
//
//    private Document downloadCarrierJob(Document doc, String slotMap, Durable durableData, Machine machineData, String portName, String portType, String portUseType, String defaultRecipeNameSpace)
//        throws CustomException
//    {
//        eventLog.info("CST Cleaner job download");
//
//        try
//        {
//            //validation
//            CommonValidation.checkEmptyCst(durableData.getKey().getDurableName());
//        }
//        catch (CustomException ce)
//        {
//            /* 20180531, Add CST Hold Logic ==>> */
//            MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-0006");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
//            return doc;
//            //throw new CustomException("CST-0006", durableData.getKey().getDurableName());
//            /* <<== 20180531, Add CST Hold Logic */
//        }
//
//        /* 20180531, Add CST Hold Logic ==>> */
//        try
//        {
//            //slot map validation
//            if (StringUtil.isNotEmpty(slotMap))
//            {
//                CommonValidation.checkCstSlot(slotMap);
//            }
//        }
//        catch (CustomException ce)
//        {
//            MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VRHC");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-0026");
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
//            return doc;
//            //throw new CustomException("CST-0026");
//        }
//        /* <<== 20180531, Add CST Hold Logic */
//
//        /* 20181212, hhlee, add, Validate Clean State=Clean ==>> */
//        try
//        {
//            if(StringUtil.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
//            {
//                throw new CustomException("CST-0041", durableData.getKey().getDurableName());
//            }            
//        }
//        catch (CustomException ce)
//        {
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//            return doc;
//        }
//        /* <<== 20181212, hhlee, add, Validate Clean State=Clean */
//        
//        //New
//        this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
//                durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");
//
//        //Recipe how to control?
//        //String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(durableData.getFactoryName(), durableData.getDurableSpecName(), durableData.getKey().getDurableName(), machineData.getKey().getMachineName(), false);
//
//        DurableSpecKey keyInfo = new DurableSpecKey();
//        keyInfo.setDurableSpecName(durableData.getDurableSpecName());
//        keyInfo.setFactoryName(durableData.getFactoryName());
//        keyInfo.setDurableSpecVersion("00001");
//
//        DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);
//
//        SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", CommonUtil.getValue(durableSpecData.getUdfs(), "CSTCLEANRECIPE"));
//        SMessageUtil.setBodyItemValue(doc, "SLOTSEL", this.slotMapTransfer(slotMap));
//
//        return doc;
//    }
//
//    /* 20190605, hhlee, delete ==>> */
//    //private void validateSorterJob(String machineName, String portName, String carrierName) throws CustomException
//    //{
//    //    StringBuffer sqlBuffer = new StringBuffer().append("SELECT J.jobName, J.jobState, J.processFlowName, J.processOperationName,    ").append("\n")
//    //                                               .append("       C.carrierName, C.lotName, C.transferDirection    ").append("\n")
//    //                                               .append("  FROM CT_SortJob J, CT_SortJobCarrier C ").append("\n")
//    //                                               .append(" WHERE J.jobName = C.jobName ").append("\n")
//    //                                               .append("   AND C.machineName = ?  ").append("\n")
//    //                                               .append("   AND C.carrierName = ?  ").append("\n")
//    //                                               .append("   AND C.portName = ? ").append("\n")
//    //                                               .append("   AND J.jobState IN (?, ?)");
//    //
//    //    List<ListOrderedMap> result;
//    //
//    //    try
//    //    {
//    //        result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {machineName, carrierName, portName, "WAIT", "CONFIRMED"});
//    //    }
//    //    catch (Exception ex)
//    //    {
//    //        result = new ArrayList<ListOrderedMap>();
//    //    }
//    //
//    //    if (result.size() < 1)
//    //    {
//    //        throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
//    //    }
//    //}
//    /* <<== 20190605, hhlee, delete */
//    
//    /* 20190605, hhlee, modify ==>> */
//    private String validateSorterJob(String machineName, String portName, String carrierName) throws CustomException
//    {
//        String transferDirection = StringUtil.EMPTY;
//        
//        StringBuffer sqlBuffer = new StringBuffer().append("SELECT J.JOBNAME, J.JOBSTATE, J.PROCESSFLOWNAME, J.PROCESSOPERATIONNAME,    ").append("\n")
//                                                   .append("       C.CARRIERNAME, C.LOTNAME, C.TRANSFERDIRECTION    ").append("\n")
//                                                   .append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ").append("\n")
//                                                   .append(" WHERE J.JOBNAME = C.JOBNAME ").append("\n")
//                                                   .append("   AND C.MACHINENAME = ?  ").append("\n")
//                                                   .append("   AND C.CARRIERNAME = ?  ").append("\n")
//                                                   .append("   AND C.PORTNAME = ? ").append("\n")
//                                                   .append("   AND J.JOBSTATE IN (?, ?)");
//
//        List<ListOrderedMap> result;
//
//        try
//        {
//            result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {machineName, carrierName, portName, "WAIT", "CONFIRMED"});
//        }
//        catch (Exception ex)
//        {
//            result = new ArrayList<ListOrderedMap>();
//        }
//
//        if (result.size() < 1)
//        {
//            throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
//        }
//        
//        transferDirection = result.get(0).get("TRANSFERDIRECTION") != null ? result.get(0).get("TRANSFERDIRECTION").toString() : StringUtil.EMPTY;
//        
//        return transferDirection;
//    }
//    /* 20190605, hhlee, modify ==>> */
//
////  private Lot getLotData(String carrierName) throws CustomException
////  {
////      String condition = "WHERE carrierName = ?";
////
////      Object[] bindSet = new Object[] {carrierName};
////
////      List<Lot> lotList;
////
////      try
////      {
////          lotList = LotServiceProxy.getLotService().select(condition, bindSet);
////
////          //check MultiLot
////          if(lotList.size() != 1)
////          {
////              throw new CustomException("LOT-0222", "");
////          }
////      }
////      catch(NotFoundSignal ne)
////      {
////          //empty CST
////          lotList = new ArrayList<Lot>();
////
////          return null;
////      }
////      catch (FrameworkErrorSignal fe)
////      {
////          throw new CustomException("SYS-9999", "Lot", "Lot binding CST incorrect");
////      }
////
////      return lotList.get(0);
////  }
//
//    private Lot assignReleaseLot(Document doc, String factoryName, String lotName, String machineName, String carrierName, String assignedWOName) throws CustomException
//    {
//
//        List<ListOrderedMap> planList = CommonUtil.getProductRequestPlanList(machineName);
//        ProductRequestPlan planData = null;
//        Lot lot = CommonUtil.getLotInfoByLotName(lotName);
//        List<String> toProductSpecNameList = this.getToProductSpecNameList(lot);
//        List<String> firstPlanList = new ArrayList<String>();
//        Boolean bool = true;
//        for (ListOrderedMap prPlanListMap : planList)
//        {
//            firstPlanList.add(CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTNAME"));
//
//            /*ProductRequest pRequest = CommonUtil.getProductRequestData(prPlan.getKey().getProductRequestName());
//            if (toProductSpecNameList.contains(pRequest.getProductSpecName()))
//            {
//                planData = prPlan;
//                break;
//            }   */
//
//            if (CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTNAME").equalsIgnoreCase(assignedWOName)) {
//                if (CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTHOLDSTATE").equalsIgnoreCase(GenericServiceProxy.getConstantMap().Prq_OnHold)) {
//                    throw new CustomException("PRODUCTREQUEST-0041");
//                }
//                bool = Long.parseLong(CommonUtil.getValue(prPlanListMap, "PLANQUANTITY")) - Long.parseLong(CommonUtil.getValue(prPlanListMap, "RELEASEDQUANTITY")) >= lot.getProductQuantity();
//
//                ProductRequestPlanKey planKey = new ProductRequestPlanKey();
//                planKey.setAssignedMachineName(machineName);
//                planKey.setProductRequestName(assignedWOName);
//                Timestamp planReleasedTime = (Timestamp) prPlanListMap.get("PLANRELEASEDTIME");
//                planKey.setPlanReleasedTime(planReleasedTime);
//                planData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(planKey );
//
//            }
//        }
//        if (!firstPlanList.contains(assignedWOName)) {
//            throw new CustomException("PRODUCTREQUEST-0041");
//        }
//
//
//
//        /*if (planData == null || planData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
//        {
//            throw new CustomException("PRODUCTREQUEST-0041");
//        }
//        bool = planData.getPlanQuantity() - planData.getReleasedQuantity() > lot.getProductQuantity();*/
//
//        StringBuffer sqlBuffer =  new StringBuffer().append("   SELECT L.LOTNAME, L.PRODUCTSPECNAME, L.PRODUCTIONTYPE, L.PRODUCTREQUESTNAME,    ").append("\n")
//                                                    .append("            L.LOTSTATE, L.PRODUCTQUANTITY, ").append("\n")
//                                                    .append("            L.FACTORYNAME, L.LASTFACTORYNAME,  ").append("\n")
//                                                    .append("            L.PROCESSFLOWNAME, L.PROCESSOPERATIONNAME  ").append("\n")
//                                                    .append("       FROM TPPOLICY T, POSFACTORYRELATION P, PRODUCTREQUEST W, LOT L, ENUMDEFVALUE E  ").append("\n")
//                                                    .append("      WHERE T.CONDITIONID = P.CONDITIONID  ").append("\n")
//                                                    .append("        AND W.PRODUCTSPECNAME = P.TOPRODUCTSPECNAME    ").append("\n")
//                                                    .append("        AND T.PRODUCTSPECNAME = L.PRODUCTSPECNAME  ").append("\n")
//                                                    .append("        AND L.factoryName = ?  ").append("\n")
//                                                    .append("        AND W.productRequestName = ?   ").append("\n")
//                                                    .append("        AND L.lotState = ? ").append("\n")
//                                                    .append("        AND L.lotName = ?  ").append("\n");
//        if (!factoryName.equals("CELL"))
//        {
//            sqlBuffer.append("       AND E.ENUMVALUE = W.PRODUCTREQUESTTYPE ").append("\n")
//                     .append("       AND L.PRODUCTIONTYPE IN (E.DESCRIPTION) ");
//        }
//
//        try
//        {
//            List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(),
//                                            new Object[] {factoryName, assignedWOName, "Released", lotName});
//
//            if (result.size() < 1)
//                throw new CustomException("SYS-9999", "ProductRequestPlan", "Lot is not available for first reserved Work Order at EQP");
//        }
//        catch (FrameworkErrorSignal fe)
//        {
//            throw new CustomException("SYS-9999", "ProductRequestPlan", fe.getMessage());
//        }
//
//
//        Document cloneDoc = (Document) doc.clone();
//
//        try
//        {
//            if (bool)
//            {
//                SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "AssignReleaseLot");
//            }
//            else
//            {
//                SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "AssignWorkOrderV2");
//            }
//
//            SMessageUtil.setHeaderItemValue(cloneDoc, "ORIGINALSOURCESUBJECTNAME", "");
//            cloneDoc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//
//            Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
//
//            Element element1 = new Element("FACTORYNAME");
//            element1.setText(factoryName);
//            eleBodyTemp.addContent(element1);
//
//            Element element2 = new Element("ASSIGNEDMACHINENAME");
//            element2.setText(machineName);
//            eleBodyTemp.addContent(element2);
//
//            Element element21 = new Element("PRODUCTREQUESTNAME");
//            element21.setText(assignedWOName);
//            eleBodyTemp.addContent(element21);
//
//            Element element3 = new Element("PLANRELEASEDTIME");
//            element3.setText(TimeStampUtil.toTimeString(planData.getKey().getPlanReleasedTime()));
//            eleBodyTemp.addContent(element3);
//
//            Element element5 = new Element("PORTNAME");
//            element5.setText("N");
//            eleBodyTemp.addContent(element5);
//
//            Element element6 = new Element("CHKAUTO");
//            element6.setText("N");
//            eleBodyTemp.addContent(element6);
//
//            if (bool)
//            {
//                Element element7 = new Element("LOTLIST");
//                {
//                    Element eleLot = new Element("LOT");
//                    {
//                        Element eleLotName = new Element("LOTNAME");
//                        eleLotName.setText(lotName);
//                        eleLot.addContent(eleLotName);
//                    }
//                    element7.addContent(eleLot);
//                }
//                eleBodyTemp.addContent(element7);
//            }
//            else
//            {
//                Map<String, Long> productNamePositionMap = this.getProductNameAndPosition(lotName,planData);
//
//                Element element8 = new Element("LOTNAME");
//                element8.setText(lotName);
//                eleBodyTemp.addContent(element8);
//
//                Element element9 = new Element("PRODUCTLIST");
//                {
//                    for (String productName : productNamePositionMap.keySet())
//                    {
//                        Element eProduct = new Element("PRODUCT");
//                        {
//                            Element eleProductName = new Element("PRODUCTNAME");
//                            eleProductName.setText(productName);
//                            eProduct.addContent(eleProductName);
//
//                            Element elePosition = new Element("POSITION");
//                            elePosition.setText(String.valueOf(productNamePositionMap.get(productName)));
//                            eProduct.addContent(elePosition);
//                        }
//                        element9.addContent(eProduct);
//                    }
//                }
//                eleBodyTemp.addContent(element9);
//            }
//
//
//            // overwrite
//            cloneDoc.getRootElement().addContent(eleBodyTemp);
//        }
//        catch (Exception ex)
//        {
//            throw new CustomException("SYS-9999", "Lot", "Message generating failed");
//        }
//
//        try
//        {
//            if (bool)
//            {
//                InvokeUtils.invokeMethod(InvokeUtils.newInstance(AssignWorkOrder.class.getName(), null, null), "execute", new Object[] {cloneDoc});
//            }
//            else
//            {
//                InvokeUtils.invokeMethod(InvokeUtils.newInstance(AssignWorkOrderV2.class.getName(), null, null), "execute", new Object[] {cloneDoc});
//            }
//
//            //return released Lot
//            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
//
//            return lotData;
//        }
//        catch (Exception ex)
//        {
//            throw new CustomException("SYS-9999", "System", "Invocation failed");
//        }
//    }
//
//    private Map<String, Long> getProductNameAndPosition(String lotName, ProductRequestPlan planData)
//    {
//        Long planCount = planData.getPlanQuantity() - planData.getReleasedQuantity();
//        String condition = "WHERE lotName = ? AND ROWNUM <= ? ORDER BY position desc";
//        Object[] bindSet = new Object[]{lotName, planCount};
//
//        List<Product> products = ProductServiceProxy.getProductService().select(condition, bindSet);
//        LinkedHashMap<String,Long> productNameMap = new LinkedHashMap<String, Long>();
//
//        for (Product product : products)
//        {
//            productNameMap.put(product.getKey().getProductName(), product.getPosition());
//        }
//        return productNameMap;
//    }
//
//    private List<String> getToProductSpecNameList(Lot lotData) throws CustomException
//    {
//        String sql = "SELECT F.TOPRODUCTSPECNAME FROM TPPOLICY T, POSFACTORYRELATION F WHERE T.CONDITIONID = F.CONDITIONID AND T.PRODUCTSPECNAME = ?";
//        Object[] bindSet = new Object[] {lotData.getProductSpecName()};
//        List<String> toProductSpecNameList = new ArrayList<String>();
//
//        try
//        {
//            List<Map<String, String>> sqlList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
//            if (sqlList.size() > 0)
//            {
//                for (int i=0; i<sqlList.size(); i++)
//                {
//                    toProductSpecNameList.add(sqlList.get(i).get("TOPRODUCTSPECNAME"));
//                }
//            }
//        }
//        catch (FrameworkErrorSignal fe)
//        {
//            throw new CustomException("SYS-9999", "ToProductSpecName", fe.getMessage());
//        }
//        return toProductSpecNameList;
//    }
//
//    private String slotMapTransfer(String slotMap) throws CustomException
//    {
//        StringBuffer slotMapTemp = new StringBuffer();
//
//        for(int i = 0; i<slotMap.length(); i++)
//        {
//            slotMapTemp.append("N");
//        }
//
//        for(int i = 0; i<slotMap.length(); i++)
//        {
//            if(String.valueOf(slotMap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
//                slotMapTemp.append("Y");
//        }
//
//        return slotMapTemp.toString();
//    }
//
//    //Get realOperationName at OLED
//    private String getRealOperationName(String factoryName, String productSpecName, String ecCode, String processFlowName, String machineName, String carrierName )throws CustomException
//    {
//        String processOperationName = "" ;
//
//        //String strSql = "SELECT DISTINCT PROCESSOPERATIONNAME " +
//        //      "  FROM TPEFOMPOLICY " +
//        //      " WHERE     FACTORYNAME = :FACTORYNAME " +
//        //      "       AND PRODUCTSPECNAME = :PRODUCTSPECNAME " +
//        //      "       AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
//        //      "       AND ECCODE = :ECCODE " +
//        //      "       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
//        //      "       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
//        //      "       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
//        //      "       AND MACHINENAME = :MACHINENAME " ;
//
//        String strSql= StringUtil.EMPTY;
//        strSql = strSql + "  SELECT DISTINCT PROCESSOPERATIONNAME                                                                 \n";
//        strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
//        strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
//        strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*'))                                     \n";
//        strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = '*'))                         \n";
//        strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = '*'))                \n";
//        strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = '*'))                                                    \n";
//        strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = '*'))                         \n";
//        strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = '*'))                \n";
//        //strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = '*'))          \n";
//        strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = '*')) \n";
//        strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = '*'))                                     \n";
//        strSql = strSql + " ORDER BY DECODE (FACTORYNAME, '*', 9999, 0),                                                          \n";
//        strSql = strSql + "          DECODE (PRODUCTSPECNAME, '*', 9999, 0),                                                      \n";
//        strSql = strSql + "          DECODE (PRODUCTSPECVERSION, '*', 9999, 0),                                                   \n";
//        strSql = strSql + "          DECODE (ECCODE, '*', 9999, 0),                                                               \n";
//        strSql = strSql + "          DECODE (PROCESSFLOWNAME, '*', 9999, 0),                                                      \n";
//        strSql = strSql + "          DECODE (PROCESSFLOWVERSION, '*', 9999, 0),                                                   \n";
//        strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, '*', 9999, 0),                                                 \n";
//        strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0),                                              \n";
//        strSql = strSql + "          DECODE (MACHINENAME, '*', 9999, 0)                                                           \n";
//        
//        Map<String, Object> bindMap = new HashMap<String, Object>();
//        bindMap.put("FACTORYNAME", factoryName);
//        bindMap.put("PRODUCTSPECNAME", productSpecName);
//        bindMap.put("PRODUCTSPECVERSION", "00001");
//        bindMap.put("ECCODE", ecCode);
//        bindMap.put("PROCESSFLOWNAME", processFlowName);
//        bindMap.put("PROCESSFLOWVERSION", "00001");
//        bindMap.put("PROCESSOPERATIONVERSION", "00001");
//        bindMap.put("MACHINENAME", machineName);
//
//        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//        if ( tpefomPolicyData.size() != 1 )
//        {
//            throw new CustomException("POLICY-0001", carrierName);
//        }
//        else
//        {
//            processOperationName = (String) tpefomPolicyData.get(0).get("PROCESSOPERATIONNAME");
//        }
//
//        return processOperationName ;
//    }
//    
//    /**
//     * 
//     * @Name     getRealOperationNameList
//     * @since    2018. 10. 11.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param machineName
//     * @return
//     * @throws CustomException
//     */
//    public List<Map<String, Object>> getRealOperationNameList(String machineName )throws CustomException
//    {
//        String strSql = "SELECT MQ.MACHINENAME,  " +
//                        "       OS.PROCESSOPERATIONNAME, " +
//                        "       MQ.OPERATIONPRIORITY " +
//                        "  FROM CT_MACHINEGROUPMACHINE MGM, " +
//                        "       PROCESSOPERATIONSPEC OS, " +
//                        "       ( " +
//                        "        SELECT M.MACHINENAME,  " +
//                        "               SQ.PROCESSOPERATIONNAME,  " +
//                        "               SQ.OPERATIONMODE,  " +
//                        "               SQ.OPERATIONPRIORITY " +
//                        "          FROM MACHINE M, " +
//                        "             ( " +
//                        "              SELECT TP.CONDITIONID, " +
//                        "                     TP.FACTORYNAME, " +
//                        "                     TP.PROCESSOPERATIONNAME, " +
//                        "                     TP.PROCESSOPERATIONVERSION, " +
//                        "                     PS.MACHINENAME, " +
//                        "                     PS.OPERATIONMODE, " +
//                        "                     PS.DESCRIPTION, " +
//                        "                     PS.OPERATIONPRIORITY " +
//                        "                FROM POSOPERATIONMODE PS, " +
//                        "                     TOPOLICY TP " +
//                        "               WHERE 1=1 " +
//                        "                 AND PS.CONDITIONID = TP.CONDITIONID     " +
//                        "                 ) SQ " +
//                        "        WHERE 1=1 " +
//                        "           AND M.MACHINENAME = :MACHINENAME " +
//                        "           AND M.MACHINENAME = SQ.MACHINENAME " +
//                        "           AND M.OPERATIONMODE = SQ.OPERATIONMODE " +
//                        "           ) MQ " +
//                        "WHERE 1=1 " +
//                        "  AND MGM.MACHINENAME = MQ.MACHINENAME " +
//                        "  AND MGM.MACHINEGROUPNAME = OS.MACHINEGROUPNAME " +
//                        "  AND MQ.PROCESSOPERATIONNAME = OS.PROCESSOPERATIONNAME " +
//                        " ORDER BY MQ.OPERATIONPRIORITY  " ;
//        
//        Map<String, Object> bindMap = new HashMap<String, Object>();
//        bindMap.put("MACHINENAME", machineName);
//        
//        List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//        
//        if(tpefomPolicyData.size() <= 0)
//            throw new CustomException("POLICY-0022","");
//        
//        return tpefomPolicyData ;
//    }
//    
//    private void MemorySlotSelNoteForSampling(Lot lotData, String slotSel, EventInfo eventInfo) throws CustomException 
//    {
//        //base flow info
//        ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
//
//        //waiting step
//        ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//
//        /* 20190312, hhlee, add Check Validation */
//        //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
//        /* 20190522, hhlee, modify, add check validation SamplingFlow */
//        //if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//        //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
//        //            && !flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC))
//        if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//                flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
//                !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//        {
//            EventInfo eventInfoForSlotSelNote = EventInfoUtil.makeEventInfo("CSTInfoDownLoad", getEventUser(), "CSTInfoDownLoad", null, null);
//            eventInfoForSlotSelNote.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//            eventInfoForSlotSelNote.setEventTime(eventInfo.getEventTime());
//
//            ArrayList<String> sampleProductName = new ArrayList<String>();
//
//            String strSlotAndProductName = "SamplingSlot = ";
//            
//            //avail Product list
//            List<Product> productList = null;
//            try
//            {
//                productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//            }
//            catch (Exception ex){
//            }
//                    
//            if(productList != null && productList.size() > 0)
//            {
//                for(int i=0; i<slotSel.length(); i++){
//                    if(String.valueOf(slotSel.charAt(i)).equals(GenericServiceProxy.getConstantMap().E_PRODUCT_IN_SLOT)){
//                        for(int j=0; j<=productList.size(); j++)
//                        {
//                            Product product = productList.get(j);
//
//                            if((i+1)==(product.getPosition()))
//                            {
//                                sampleProductName.add(product.getKey().getProductName());
//                                strSlotAndProductName += "[" + String.valueOf(i+1) + "," + product.getKey().getProductName() + "]";
//                                break;
//                            }
//                        }
//                    }
//                }
//                
//                lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//                
//                SetEventInfo setEventInfo = new SetEventInfo();
//                setEventInfo.getUdfs().put("NOTE", strSlotAndProductName);
//
//                lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoForSlotSelNote, setEventInfo);
//                
//                // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
////              Map<String, String> lotUdfs = new HashMap<String, String>();
////              lotData.getUdfs().put("NOTE", "");
////              LotServiceProxy.getLotService().update(lotData);
//                Map<String, String> updateUdfs = new HashMap<String, String>();
//                updateUdfs.put("NOTE", "");
//                MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
//            }
//        }
//    }
//
//    /**
//     * @Name     setLotInfoDownLoadSendProcessList
//     * @since    2018. 6. 1.
//     * @author   hhlee
//     * @contents Set LotInfoDownLoadSend Process List
//     * @param processlistElement
//     * @param factoryName
//     * @param productName
//     * @return
//     * @throws CustomException
//     */
//    private Element setLotInfoDownLoadSendProcessList(String factoryName, String productName) throws CustomException
//    {
//        Element processListElement = new Element("PROCESSLIST");
//        try
//        {
//            //String strSql = StringUtil.EMPTY;
//            //strSql = strSql + " SELECT SQ.TIMEKEY AS TIMEKEY,                                   \n";
//            //strSql = strSql + "        SQ.PRODUCTNAME AS PRODUCTNAME,                           \n";
//            //strSql = strSql + "        SQ.PROCESSFLOWNAME AS PROCESSFLOWNAME,                   \n";
//            //strSql = strSql + "        SQ.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME,         \n";
//            //strSql = strSql + "        SQ.MACHINENAME AS MACHINENAME                            \n";
//            //strSql = strSql + "   FROM (                                                        \n";
//            //strSql = strSql + "         SELECT /*+ INDEX_DESC(PRODUCTHISTORY_PK) */             \n";
//            //strSql = strSql + "                PH.TIMEKEY AS TIMEKEY,                           \n";
//            //strSql = strSql + "                PH.PRODUCTNAME AS PRODUCTNAME,                   \n";
//            //strSql = strSql + "                PF.PROCESSFLOWNAME AS PROCESSFLOWNAME,           \n";
//            //strSql = strSql + "                PH.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, \n";
//            //strSql = strSql + "                PH.MACHINENAME AS MACHINENAME                    \n";
//            //strSql = strSql + "           FROM PRODUCTHISTORY PH, PROCESSFLOW PF                \n";
//            //strSql = strSql + "          WHERE 1=1                                              \n";
//            //strSql = strSql + "            AND PH.PRODUCTNAME = :PRODUCTNAME                    \n";
//            //strSql = strSql + "            AND PH.MACHINENAME IS NOT NULL                       \n";
//            //strSql = strSql + "            AND PH.PROCESSFLOWNAME = PF.PROCESSFLOWNAME          \n";
//            //strSql = strSql + "            AND PF.PROCESSFLOWTYPE = :PROCESSFLOWTYPE            \n";
//            //strSql = strSql + "            AND PF.FACTORYNAME = :FACTORYNAME                    \n";
//            //strSql = strSql + "           ORDER BY PH.TIMEKEY DESC                              \n";
//            //strSql = strSql + "         )SQ                                                     \n";
//            //strSql = strSql + " WHERE 1=1                                                       \n";
//            //strSql = strSql + "   AND ROWNUM <= 5                                               \n";
//
//            String strSql = StringUtil.EMPTY;
//            strSql = strSql + " SELECT MQ.PRODUCTNAME,                                                                 \n";
//            strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,1), ' ') AS PROCESSMACHINENAME,  \n";
//            strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,2), ' ') AS PROCESSOPERATIONNAME \n";
//            strSql = strSql + "   FROM (                                                                               \n";
//            strSql = strSql + "         SELECT T.NO, SQ.PRODUCTNAME,                                                   \n";
//            strSql = strSql + "                (CASE WHEN T.NO = 1 THEN SQ.AT1                                         \n";
//            strSql = strSql + "                      WHEN T.NO = 2 THEN SQ.AT2                                         \n";
//            strSql = strSql + "                      WHEN T.NO = 3 THEN SQ.AT3                                         \n";
//            strSql = strSql + "                      WHEN T.NO = 4 THEN SQ.AT4                                         \n";
//            strSql = strSql + "                      WHEN T.NO = 5 THEN SQ.AT5                                         \n";
//            strSql = strSql + "                      END) AS PROCESSOPERATION                                          \n";
//            strSql = strSql + "           FROM (                                                                       \n";
//            strSql = strSql + "                 SELECT PO.PRODUCTNAME AS PRODUCTNAME,                                  \n";
//            strSql = strSql + "                        PO.ATTRIBUTE1 AS AT1,                                           \n";
//            strSql = strSql + "                        PO.ATTRIBUTE2 AS AT2,                                           \n";
//            strSql = strSql + "                        PO.ATTRIBUTE3 AS AT3,                                           \n";
//            strSql = strSql + "                        PO.ATTRIBUTE4 AS AT4,                                           \n";
//            strSql = strSql + "                        PO.ATTRIBUTE5 AS AT5                                            \n";
//            strSql = strSql + "                   FROM CT_PROCESSEDOPERATION PO                                        \n";
//            strSql = strSql + "                  WHERE 1=1                                                             \n";
//            strSql = strSql + "                    AND PO.PRODUCTNAME = :PRODUCTNAME                                   \n";
//            strSql = strSql + "                 )SQ,                                                                   \n";
//            strSql = strSql + "                 (SELECT LEVEL NO                                                       \n";
//            strSql = strSql + "                    FROM DUAL CONNECT BY LEVEL <= 5) T                                  \n";
//            strSql = strSql + "         )MQ                                                                            \n";
//            strSql = strSql + "  ORDER BY MQ.NO                                                                        \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("PRODUCTNAME", productName);
//            //bindMap.put("FACTORYNAME", factoryName);
//            //bindMap.put("PROCESSFLOWTYPE", GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN);
//
//            List<Map<String, Object>> ProcessOperationData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//
//            if ( ProcessOperationData.size() > 0 )
//            {
//                for(int i = 0; i < ProcessOperationData.size(); i++ )
//                {
//                    /* If you have to send only what has a value, loosen the IF statement below. */
//                    //if (!ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim().isEmpty())
//                    {
//                        Element processElement = new Element("PROCESS");
//    
//                        Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
//                        processOperationNameLElement.setText(ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim());
//                        processElement.addContent(processOperationNameLElement);
//    
//                        Element processMachineNameElement = new Element("PROCESSMACHINENAME");
//                        processMachineNameElement.setText(ProcessOperationData.get(i).get("PROCESSMACHINENAME").toString().trim());
//                        processElement.addContent(processMachineNameElement);
//    
//                        processListElement.addContent(processElement);
//                    }
//                }
//            }
//            else
//            {
//            }
//        }
//        catch (Exception ex)
//        {
//            eventLog.warn("[setLotInfoDownLoadSendProcessList] Data Query Failed");;
//        }
//
//        return processListElement;
//    }   
//    
////  /**
////   * @Name     getActualSamplePosition
////   * @since    2018. 6. 16.
////   * @author   hhlee
////   * @contents 
////   * @param lotData
////   * @param logicalSlotMap
////   * @return
////   */
////  private String getActualSamplePosition(EventInfo eventInfo, Lot lotData, String logicalSlotMap)
////  {
////      String actualSamplePosition = StringUtil.EMPTY;
////      String strSql = StringUtil.EMPTY;
////      String originallogicalSlotMap = logicalSlotMap;
////      try
////        {
//////            strSql = strSql + " SELECT SA.LOTNAME, SA.SAMPLEPROCESSFLOWNAME, SA.SAMPLEPROCESSFLOWVERSION,                        \n";
//////            strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
//////            strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
//////            strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
//////            strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
//////            strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
//////            strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
//////            strSql = strSql + "   FROM CT_SAMPLELOTSTATE SA,                                                                     \n";
//////            strSql = strSql + "        CT_SAMPLELOT SL                                                                           \n";
//////            strSql = strSql + "  WHERE 1=1                                                                                       \n";
//////            strSql = strSql + "    AND SA.LOTNAME = :LOTNAME                                                                     \n";
//////            strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
//////            strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
//////            strSql = strSql + "    AND SA.LOTNAME = SL.LOTNAME                                                                   \n";
//////            strSql = strSql + "    AND SA.FACTORYNAME = SL.FACTORYNAME                                                           \n";
//////            strSql = strSql + "    AND SA.PRODUCTSPECNAME = SL.PRODUCTSPECNAME                                                   \n";
//////            strSql = strSql + "    AND SA.PROCESSFLOWNAME = SL.PROCESSFLOWNAME                                                   \n";
//////            strSql = strSql + "    AND SA.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION                                             \n";
//////            strSql = strSql + "    AND SA.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME                                         \n";
//////            strSql = strSql + "    AND SA.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION                                   \n";
//////            strSql = strSql + "    AND SA.MACHINENAME = SL.MACHINENAME                                                           \n";
//////            strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME                                       \n";
//////            strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION                                 \n";
//////            strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
////          
////          strSql = strSql + " SELECT SL.LOTNAME, SL.SAMPLEPROCESSFLOWNAME, SL.SAMPLEPROCESSFLOWVERSION,                        \n";
////            strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
////            strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
////            strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
////            strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
////            strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
////            strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
////            strSql = strSql + "   FROM CT_SAMPLELOT SL                                                                           \n";
////            strSql = strSql + "  WHERE 1=1                                                                                       \n";
////            strSql = strSql + "    AND SL.LOTNAME = :LOTNAME                                                                     \n";
////            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
////            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
////            strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
////          
////          Map<String, Object> bindMap = new HashMap<String, Object>();
////            bindMap.put("LOTNAME", lotData.getKey().getLotName());
////            bindMap.put("SAMPLEPROCESSFLOWNAME", lotData.getProcessFlowName());
////            bindMap.put("SAMPLEPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
////            bindMap.put("ECCODE", CommonUtil.getValue(lotData.getUdfs(), "ECCODE"));
////    
////            List<Map<String, Object>> sampleLotData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
////          
////            if ( sampleLotData.size() > 0 )
////            {                
////                String[] actualsamplepostion = sampleLotData.get(0).get("ACTUALSAMPLEPOSITION").toString().trim().split(",");
////                int index = 0;
////                for(int i = 0; i < actualsamplepostion.length; i++ )
////                {
////                    index = Integer.parseInt(actualsamplepostion[i]) - 1;
////                    
////                    if(!String.valueOf(logicalSlotMap.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
////                    {
////                        actualsamplepostion[i] = getNewActualSamplePosition(index, logicalSlotMap, actualsamplepostion);
////                    }
////                    
////                    if(StringUtil.isEmpty(actualSamplePosition))
////                    {
////                        actualSamplePosition = actualsamplepostion[i];
////                    }
////                    else
////                    {
////                        actualSamplePosition = actualSamplePosition + "," + actualsamplepostion[i];
////                    }
////                   
////                } 
////                
////                actualSamplePosition = actualSamplePositionSorting(actualSamplePosition);                
////                
//////                if(!originallogicalSlotMap.equals(actualSamplePosition))
//////                {
//////                    eventInfo.setEventName("ChangeActualSamplePosition");
//////                    
//////                    SampleLot sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] 
//////                            {sampleLotData.get(0).get("LOTNAME").toString(), 
//////                            sampleLotData.get(0).get("FACTORYNAME").toString(),
//////                            sampleLotData.get(0).get("PRODUCTSPECNAME").toString(),
//////                            sampleLotData.get(0).get("ECCODE").toString(),
//////                            sampleLotData.get(0).get("PROCESSFLOWNAME").toString(),
//////                            sampleLotData.get(0).get("PROCESSFLOWVERSION").toString(),
//////                            sampleLotData.get(0).get("PROCESSOPERATIONNAME").toString(),
//////                            sampleLotData.get(0).get("PROCESSOPERATIONVERSION").toString(),
//////                            sampleLotData.get(0).get("MACHINENAME").toString(),
//////                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWNAME").toString(),
//////                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWVERSION").toString()
//////                            });
//////        
//////                    //default spec info
//////                    sampleLotInfo.setActualSamplePosition(actualSamplePosition);                   
//////        
//////                    //history trace
//////                    sampleLotInfo.setLastEventName(eventInfo.getEventName());
//////                    sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
//////                    sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
//////                    sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
//////                    sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
//////        
//////                    //ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
//////                }
////            }
////            else
////            {
////            }    
////        }
////        catch (Exception ex)
////        {
////            eventLog.warn("[getActualSamplePosition] Data Query Failed");;
////        }
////      
////      return actualSamplePosition;
////  }
////
//////    /**
//////     * @Name     getActualSamplePosition
//////     * @since    2018. 6. 16.
//////     * @author   hhlee
//////     * @contents 
//////     * @param lotData
//////     * @param logicalSlotMap
//////     * @return
//////     */
//////    private String getActualSamplePosition(EventInfo eventInfo, Lot lotData, String logicalSlotMap)
//////    {
//////        String actualSamplePosition = StringUtil.EMPTY;
//////        String strSql = StringUtil.EMPTY;
//////        String originallogicalSlotMap = logicalSlotMap;
//////        try
//////        {
////////          strSql = strSql + " SELECT SA.LOTNAME, SA.SAMPLEPROCESSFLOWNAME, SA.SAMPLEPROCESSFLOWVERSION,                        \n";
////////          strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
////////          strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
////////          strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
////////          strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
////////          strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
////////          strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
////////          strSql = strSql + "   FROM CT_SAMPLELOTSTATE SA,                                                                     \n";
////////          strSql = strSql + "        CT_SAMPLELOT SL                                                                           \n";
////////          strSql = strSql + "  WHERE 1=1                                                                                       \n";
////////          strSql = strSql + "    AND SA.LOTNAME = :LOTNAME                                                                     \n";
////////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
////////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
////////          strSql = strSql + "    AND SA.LOTNAME = SL.LOTNAME                                                                   \n";
////////          strSql = strSql + "    AND SA.FACTORYNAME = SL.FACTORYNAME                                                           \n";
////////          strSql = strSql + "    AND SA.PRODUCTSPECNAME = SL.PRODUCTSPECNAME                                                   \n";
////////          strSql = strSql + "    AND SA.PROCESSFLOWNAME = SL.PROCESSFLOWNAME                                                   \n";
////////          strSql = strSql + "    AND SA.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION                                             \n";
////////          strSql = strSql + "    AND SA.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME                                         \n";
////////          strSql = strSql + "    AND SA.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION                                   \n";
////////          strSql = strSql + "    AND SA.MACHINENAME = SL.MACHINENAME                                                           \n";
////////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME                                       \n";
////////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION                                 \n";
////////          strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
//////            
//////            strSql = strSql + " SELECT SL.LOTNAME, SL.SAMPLEPROCESSFLOWNAME, SL.SAMPLEPROCESSFLOWVERSION,                        \n";
//////            strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
//////            strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
//////            strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
//////            strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
//////            strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
//////            strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
//////            strSql = strSql + "   FROM CT_SAMPLELOT SL                                                                           \n";
//////            strSql = strSql + "  WHERE 1=1                                                                                       \n";
//////            strSql = strSql + "    AND SL.LOTNAME = :LOTNAME                                                                     \n";
//////            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
//////            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
//////            strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
//////            
//////            Map<String, Object> bindMap = new HashMap<String, Object>();
//////            bindMap.put("LOTNAME", lotData.getKey().getLotName());
//////            bindMap.put("SAMPLEPROCESSFLOWNAME", lotData.getProcessFlowName());
//////            bindMap.put("SAMPLEPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
//////            bindMap.put("ECCODE", CommonUtil.getValue(lotData.getUdfs(), "ECCODE"));
//////    
//////            List<Map<String, Object>> sampleLotData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//////            
//////            if ( sampleLotData.size() > 0 )
//////            {                
//////                String[] actualsamplepostion = sampleLotData.get(0).get("ACTUALSAMPLEPOSITION").toString().trim().split(",");
//////                int index = 0;
//////                for(int i = 0; i < actualsamplepostion.length; i++ )
//////                {
//////                    index = Integer.parseInt(actualsamplepostion[i]) - 1;
//////                    
//////                    if(!String.valueOf(logicalSlotMap.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
//////                    {
//////                        actualsamplepostion[i] = getNewActualSamplePosition(index, logicalSlotMap, actualsamplepostion);
//////                    }
//////                    
//////                    if(StringUtil.isEmpty(actualSamplePosition))
//////                    {
//////                        actualSamplePosition = actualsamplepostion[i];
//////                    }
//////                    else
//////                    {
//////                        actualSamplePosition = actualSamplePosition + "," + actualsamplepostion[i];
//////                    }
//////                   
//////                } 
//////                
//////                actualSamplePosition = actualSamplePositionSorting(actualSamplePosition);                
//////                
//////                if(!originallogicalSlotMap.equals(actualSamplePosition))
//////                {
//////                    eventInfo.setEventName("ChangeActualSamplePosition");
//////                    
//////                    SampleLot sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] 
//////                            {sampleLotData.get(0).get("LOTNAME").toString(), 
//////                            sampleLotData.get(0).get("FACTORYNAME").toString(),
//////                            sampleLotData.get(0).get("PRODUCTSPECNAME").toString(),
//////                            sampleLotData.get(0).get("ECCODE").toString(),
//////                            sampleLotData.get(0).get("PROCESSFLOWNAME").toString(),
//////                            sampleLotData.get(0).get("PROCESSFLOWVERSION").toString(),
//////                            sampleLotData.get(0).get("PROCESSOPERATIONNAME").toString(),
//////                            sampleLotData.get(0).get("PROCESSOPERATIONVERSION").toString(),
//////                            sampleLotData.get(0).get("MACHINENAME").toString(),
//////                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWNAME").toString(),
//////                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWVERSION").toString()
//////                            });
//////        
//////                    //default spec info
//////                    sampleLotInfo.setActualSamplePosition(actualSamplePosition);                   
//////        
//////                    //history trace
//////                    sampleLotInfo.setLastEventName(eventInfo.getEventName());
//////                    sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
//////                    sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
//////                    sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
//////                    sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
//////        
//////                    //ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
//////                }
//////            }
//////            else
//////            {
//////            }    
//////        }
//////        catch (Exception ex)
//////        {
//////            eventLog.warn("[getActualSamplePosition] Data Query Failed");;
//////        }
//////        
//////        return actualSamplePosition;
//////    }
////      
////  /**
////   * @Name     getNewActualSamplePosition
////   * @since    2018. 6. 16.
////   * @author   hhlee
////   * @contents 
////   * @param indexcnt
////   * @param logicalslotmap
////   * @param actualsamplepostiondata
////   * @return
////   */
////  private String getNewActualSamplePosition(int indexcnt, String logicalslotmap , String[] actualsamplepostiondata)
////  {
////      String newpostion = StringUtil.EMPTY;
////      boolean newposition = false;
////      int position = 0;
////      
////      for(int i = indexcnt + 1; i<logicalslotmap.length(); i++) 
////      {
////          position = i + 1;           
////          if(String.valueOf(logicalslotmap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
////          {
////              newposition = false;
////              newpostion = String.valueOf(position);
////              for(int j = 0; j < actualsamplepostiondata.length; j++ )
////                {
////                    if (position ==  Integer.parseInt(actualsamplepostiondata[j]))
////                    {           
////                        newposition = true;
////                        break;
////                    }
////                }
////              
////              if (!newposition)
////              {
////                  break;
////              }               
////          }
////          newpostion = StringUtil.EMPTY;
////      }
////      
////      return newpostion;
////  }
////  
////  /**
////   * @Name     actualSamplePositionSorting
////   * @since    2018. 6. 16.
////   * @author   hhlee
////   * @contents 
////   * @param actualSamplePosition
////   * @return
////   */
////  private String actualSamplePositionSorting(String actualSamplePosition)
////  {
////      String[] actualsamplepostion = actualSamplePosition.trim().split(",");
////      String actualsamplepostionNew = StringUtil.EMPTY;
////      String positiontemp = StringUtil.EMPTY;
////            
////        for(int i = 0; i < actualsamplepostion.length - 1; i++ )
////        {
////            for(int j = 0; j < actualsamplepostion.length - 1; j++ )
////            {
////                if(Long.parseLong(actualsamplepostion[j]) > Long.parseLong(actualsamplepostion[j+1]))
////                {
////                    positiontemp = actualsamplepostion[j];
////                    actualsamplepostion[j] = actualsamplepostion[j+1];
////                    actualsamplepostion[j+1] = positiontemp;
////                }
////            }
////        }
////        for(int i = 0; i < actualsamplepostion.length; i++ )
////        {
////            actualsamplepostionNew = actualsamplepostionNew + "," + actualsamplepostion[i] ;
////        }
////        actualsamplepostionNew = actualsamplepostionNew.substring(1);
////      
////        return actualsamplepostionNew;                
////  }
//}
