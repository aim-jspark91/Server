package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.lot.management.sql.SqlStatement;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class LotProcessAbort extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_LotProcessAbortedReply");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
				
        String lotJudge = "";
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
        
        /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */ 
        try
        {
    		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
    
    		if(durableData == null)
    		{
    			throw new CustomException("CST-0001", carrierName);
    		}
    
    		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
    		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
    		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
    
    		//job end in unpacker scenario
            if(CommonUtil.isInitialInput(machineName))
            {
                this.UnpackerProcessAbort(doc);
            }
            else if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
    		{
    			this.sortProcessAbort(doc);
    		}
    
            //20170808 by zhanghao  EVA  PU LotProcessAbort TrackOutGlass
            else if (CommonUtil.isInineType(machineName) && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
            {
                this.GlassProcessEnd(doc);
            }
            else if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PB") || 
                        StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PO"))
    		{
    			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
    
    			/* 20181122, hhlee, add, validate LotProcessState = 'RUN' ==>> */
                try
                {
                    //if(StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_LoggedOut))
                    if(!StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
                    {
                        throw new CustomException("LOT-9003", lotList.get(0).getKey().getLotName() +". Current State is " + lotList.get(0).getLotProcessState());
                    }
                    
                    /* 20181220, hhlee, add, add Lot Hold Check Validation ==>> */
                    CommonValidation.checkLotHoldState(lotList.get(0));
                    /* <<== 20181220, hhlee, add, add Lot Hold Check Validation */
                }
                catch (CustomException ce)
                {
                    eventInfo.setEventName("Hold");
                    eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
                    /* 20181128, hhlee, modify, add messageName */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND",""); // Abnormal End
                    // 2019.04.19_hsryu_Delete Logic. HoldDepartment = Machine Department. Mantis 0003604.
                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, "A_LotProcessAborted", machineSpecData.getUdfs().get("DEPARTMENT"));
                    try {
						GenericServiceProxy.getTxDataSourceManager().beginTransaction();
						MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, false, "A_LotProcessAborted", machineSpecData.getUdfs().get("DEPARTMENT"));
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
                    
                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
                    return doc;        
                }
                /* <<== 20181122, hhlee, add, validate LotProcessState = 'RUN' */
    
    			String machineRecipeName = null;			
    			if( productElement != null)
    			{
    			    //20180504, kyjung, QTime
    				for (Element productEle : productElement )
    				{
    					String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    					Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
    
    					MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
    				}
    
    				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
    				Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
 
    	            /* 20180802, modify, add actualSamplePosition parameter of setProductPGSRCSequence function ==> */
    				////List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotList.get(0).getKey().getLotName(), productElement);
    
    				MESLotServiceProxy.getLotServiceUtil().checkTrackFlag(productElement, machineName, false, eventInfo);
    
    				//List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productElement, machineName, actualSamplePosition);
    				/* <<==  20180802, modify, add actualSamplePosition parameter of setProductPGSRCSequence function */
    				
    				// Decide Sampling
    				String decideSampleNodeStack = "";
    	            // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    	            // Modified by smkang on 2018.07.02 - According to EDO's request, although SuperLotFlag is Y, reserved sampling should be executed.
    				//if ( lotList.get(0).getPriority() != 1 )
                    //{
                    //    // Decide Sampling
                    //    decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, lotList.get(0));
                    //}
    
                    //if ( lotList.get(0).getPriority() != 1 && StringUtil.isNotEmpty(MESLotServiceProxy.getLotServiceUtil().checkMoveToSample(lotList.get(0))))
                    //{
                    //    decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getSampleNodeStack(MESLotServiceProxy.getLotServiceUtil().checkMoveToSample(lotList.get(0)));
                    //}
    				
    				Lot lotData = lotList.get(0);
    	            
    				/* 20190308, hhlee, add ==>> */
                    //waiting step
                    ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
                    /* <<== 20190308, hhlee, add */
    				
                    /* 20190308, hhlee, add ==>> */
                    /* 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update */
                    //List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOut(productElement, operationData.getProcessOperationType());
                    List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOut(productElement, 
                            operationData.getProcessOperationType(), operationData.getDetailProcessOperationType());
                    /* <<== 20190308, hhlee, add */
                    
                    // start modify by jhying on20200318 mantis :5836
                    // Added by smkang on 2019.05.16 - According to Liu Hongwei's request, MachineIdleTime should be reset at TrackOut time.
                    String operationMode = machineData.getUdfs().get("OPERATIONMODE");
                    if (lotData.getProductionType().equals(GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA)){
            			String linkedUnitName = "";
            			
            			if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
            				linkedUnitName = "*";
            			else if(StringUtils.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP)) {
            				linkedUnitName = portData.getUdfs().get("LINKEDUNITNAME");
            				
            				if (StringUtils.isEmpty(linkedUnitName))
            					eventLog.warn("OperationMode is 'INDP'. But LinkedUnitName is Empty!!");
            			}
            			
            			eventLog.info("machineName : " + machineName + ", unitName : " + linkedUnitName);
            			//modify by jhying on 20200318 mantis:5836 add check mqcProductSpec = MQCConditionSetting
            			//MESMachineServiceProxy.getMachineServiceImpl().resetMachineIdleTime(machineName, linkedUnitName, eventInfo);
            			MESMachineServiceProxy.getMachineServiceImpl().resetMachineIdleTime(machineName, linkedUnitName, eventInfo,lotData);
                    }	
                    // end modify by jhying on20200318 mantis :5836
                    
    				/* 20180723, Add , hhlee, Actural Sampling Position Update ==>> */
    	            String actualSamplePosition = StringUtil.EMPTY;
    	            //slot map validation
                    String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
    	            //base flow info
    	            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
    	            
    	            /* 20190308, hhlee, delete ==>> */
    	            ////waiting step
    	            //ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
    	            /* <<== 20190308, hhlee, delete */
    	            
    	            /* 20190226, hhlee, SPC Send Logic change ==>> */
                    String upLoadID = CommonUtil.getValue(operationData.getUdfs(), "UPLOADID");
                    /* <<== 20190226, hhlee, SPC Send Logic change */
                    
                    /* 20190312, hhlee, add Check Validation */
                    //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
                    /* 20190523, hhlee, modify, add check validation SamplingFlow */
                    //if (operationData.getProcessOperationType().equals("Inspection") && 
                    //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
                    //            && !flowData.getProcessFlowType().equals("MQC"))
                    if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                            flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                            !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
    	            {
    	                /* 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter ==>> */
    	                //slot map validation
    	                //logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
    	                /* 20181220, hhlee, modify, modify getActualSample -> product(processflag) ==>> */
    	                //actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap, true);
    	                actualSamplePosition = MESProductServiceProxy.getProductServiceUtil().getActualSlotByProductProcessFlag(lotData.getKey().getLotName());
    	                /* <<== 20181220, hhlee, modify, modify getActualSample -> product(processflag) */
    	                /* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter */
    	            }
    	            /* <<== 20180723, Add , hhlee, Actural Sampling Position Update */
    
    	            /* 20181107, Add , hhlee, Get Slot Selection Info ==>> */
    	            String productSlotSelection = StringUtil.EMPTY;
    	            productSlotSelection = MESProductServiceProxy.getProductServiceUtil().getProductSlotSelection(lotData, durableData, actualSamplePosition);            
    	            /* <<== 20181107, Add , hhlee, Get Slot Selection Info */	           
    	            
    	            /* 2181108, hhlee, add, QTime is the same as LotProcessEnd ==>> */
    	            
    	            /* 20181128, hhlee, EventTime Sync */
    	            //20180504, kyjung, QTime
    	            //eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
    	            eventInfo.setCheckTimekeyValidation(false);
    	            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    	            
    	            EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
    	            List<Map<String, Object>> queuePolicyData = null;
    
    	            Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(lotList.get(0));
    
    	            if(qTimeTPEFOPolicyData != null)
    	            {
    	                queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
    
    	                if(queuePolicyData != null)
    	                {
    	                    for (Element product : productElement )
    	                    {
    	                        String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
    
    	                        MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productName, queuePolicyData);
    	                    }
    	                }
    	            }
    	            /* <<== 2181108, hhlee, add, QTime is the same as LotProcessEnd */
    	            
        			List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

        			// 20180504, kyjung, QTime
        			for (Product productData : productDataList)
        			{
        				MESProductServiceProxy.getProductServiceImpl().closeQTime(eventInfo, productData, "TrackIn");
        			}
    	            
    	            /* 20181011, hhlee, add , change location ==>> */
    	            String elaQtimeFlag = GenericServiceProxy.getConstantMap().Flag_N;  //ELAQTIMEFLAG
    	            String qtimeFlag = StringUtil.EMPTY;
    	            String elaQtimeOverProductName = StringUtil.EMPTY;
    	            
    	            /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
    	            String elaProductNgFlag = GenericServiceProxy.getConstantMap().ProductGrade_G;  //ELAQTIMEFLAG
    	            String ngProductList = StringUtil.EMPTY;
    	            /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
    	            
    	            /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
    	            String productListProcessingInfoW = StringUtil.EMPTY;
    	            String productListProcessingInfoF = StringUtil.EMPTY;
    	            /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
    	            
    	            // LotProcessAbort Note
    	            StringBuilder note = new StringBuilder("");
                    for (Element product : productElement )
                    {
                        String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                        
                        // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                        Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
                        Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
    
                        String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", true);
                        productData.getUdfs().put("PROCESSINGINFO", processingInfo);
                        ProductServiceProxy.getProductService().update(productData);
    
        				/*Get Old ProductFlagData*/
        				ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
        				/*Get Old ProductFlagData*/
                        
                        /* Array Product Flag Upsert ==> */
                        //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                        /* <== Array Product Flag Upsert */
                        
        				/*Get New ProductFlagData*/
        				ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
        				/*Get New ProductFlagData*/
        				
        				/* Get Note */ 
        				ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
        				/* Get Note */
                        
                        /* Array Product Process Unit/SubUnit ==> */
                        ExtendedObjectProxy.getProductInUnitOrSubUnitService().setProductProcessUnitOrSubUnit(eventInfo, machineData, product, carrierName);
                        /* <== Array Product Process Unit/SubUnit */
                        
                        if (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                        {
                            /* 20180911, hhlee, Modify, ==>> */
                            //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
                            MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
                            /* <<== 20180911, hhlee, Modify, */
                        }
                        
                        /* 20190226, hhlee, SPC Send Logic change ==>> */
                        if(StringUtil.equals(operationData.getKey().getProcessOperationName(), upLoadID))
                        {
                            MESProductServiceProxy.getProductServiceImpl().setSpcProcessedOperationData(eventInfo, product, machineName);
                        }               
                        /* <<== 20190226, hhlee, SPC Send Logic change */
                        
                        /* 20181001, hhlee, add machine Recipe Save ==>> */
                        if(StringUtil.isEmpty(machineRecipeName))
                        {
                            machineRecipeName = SMessageUtil.getChildText(product, "PRODUCTRECIPE", false);
                        }
                        /* <<== 20180920, hhlee, add machine Recipe Save */
                        
                        qtimeFlag =  SMessageUtil.getChildText(product, GenericServiceProxy.getConstantMap().PRODUCTFLAG_ELAQTIMEFLAG, false);
                        if(StringUtil.equals(qtimeFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                        {
                            /* 20181227, hhlee, add, change logic(all product record) ==>> */
                            elaQtimeFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                            if(StringUtil.isEmpty(elaQtimeOverProductName))                    {
                                
                                elaQtimeOverProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                            }
                            else
                            {
                                elaQtimeOverProductName += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);;
                            }
                            /* 20181227, hhlee, add, change logic(all product record) ==>> */
                        }
                        
                        /* 20181227, hhlee, add, ProcessInfo == F, all product record ==>> */
                        if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F))
                        {
                            /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
                            if(StringUtil.isEmpty(productListProcessingInfoF))
                            {
                               productListProcessingInfoF = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                            }
                            else
                            {
                                productListProcessingInfoF += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                            }
                            /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
                        }
                        /* <<== 20181227, hhlee, add, ProcessInfo == F, all product record */
                        
                        /* 20181227, hhlee, add, add PRODUCT_PROCESSINGINFO_W ==>> */
                        String position = SMessageUtil.getChildText(product, "POSITION", true);
                        if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W))
                        {
                            if (StringUtil.equals(StringUtil.substring(productSlotSelection, Integer.valueOf(position) - 1, Integer.valueOf(position)),
                                    GenericServiceProxy.getConstantMap().Flag_Y))
                            {
                                /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
                                if(StringUtil.isEmpty(productListProcessingInfoW))
                                {
                                    productListProcessingInfoW = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                                }
                                else
                                {
                                    productListProcessingInfoW += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                                }
                                /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
                            }
                        }
                        /* <<== 20181227, hhlee, add, add PRODUCT_PROCESSINGINFO_W */   
                        
                        /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
                        elaProductNgFlag =  SMessageUtil.getChildText(product,"PRODUCTJUDGE", false);
                        if(StringUtil.equals(elaProductNgFlag, GenericServiceProxy.getConstantMap().ProductGrade_N))
                        {                    
                            if(StringUtil.isEmpty(ngProductList))
                            {
                                ngProductList = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
                            }
                            else
                            {
                                ngProductList = ngProductList + "," + SMessageUtil.getChildText(product, "PRODUCTNAME", true);
                            }
                        }
                        /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
                    }
                    /* <<== 20181011, hhlee, add , change location */
                    
                    /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
                    if(StringUtil.isNotEmpty(ngProductList))
                    {
                        String beforeEventName = eventInfo.getEventName();
                        String beforeEventComment = eventInfo.getEventComment();
                        eventInfo.setEventComment(String.format("NG Product Hold.!! [MachineName : %s, NG Product : %s ].!!", machineName, ngProductList)); 
                        /* 20190320, hhlee, Modify, added Hold Department */
                        //MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotList.get(0), machineName, "HoldLot", "GNHL");
                        MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotData, machineName, "HoldLot", "GNHL", CommonUtil.getValue(machineSpecData.getUdfs(), "DEPARTMENT"));
                        eventInfo.setEventName(beforeEventName);
                        eventInfo.setEventComment(beforeEventComment);
                    }
                    /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
                    
                    /**2018.12.20_hsryu_Modify TrackOutLot Logic. same LotProcessEnd!**/
    //              Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotList.get(0), portData,
    //                        carrierName, lotJudge, machineName, machineRecipeName, "",
    //                        productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, true);
                    
                    /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
                    String beforeProcessFlowName = lotList.get(0).getProcessFlowName();
                    String beforeProcessOperationName = lotList.get(0).getProcessOperationName();
                    /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
                    
        			Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotList.get(0), portData,
    						carrierName, lotJudge, machineName, "",
    						productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, true,note.toString());
                    	            
    			    /* Array 20180807, Add [Process Flag Update] ==>> */            
    	            MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterTrackOutLot, logicalSlotMap, true);
    	            /* <<== Array 20180807, Add [Process Flag Update] */
    	            	            
    	            /* Array 20180619, Add [If ELA Q-time Flag is a Y glass Hold the lot after TrackOut.] ==> */
    	            if(StringUtil.equals(elaQtimeFlag, GenericServiceProxy.getConstantMap().Flag_Y))
    	            {
    	                eventInfo.setEventName("Hold");
    	                eventInfo.setEventComment(String.format("This Lot[%s](ProductName=%s) is ELAQ-time Over.", afterTrackOutLot.getKey().getLotName(), elaQtimeOverProductName));
    	                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, afterTrackOutLot.getCarrierName(),null, "EL","");
    	                /* 20190426, hhlee, modify, add variable(setFutureHold) */
    	                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ELAH","", false, "A_LotProcessAbort", " ");
    	                
    	                try {
    	                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    	                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ELAH","", false, false, "A_LotProcessAbort", "");
    	                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						} catch (Exception e) {
							GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						}
    	                
    	            }
    	            /* <<== Array 20180619, Add [If ELA Q-time Flag is a Y glass Hold the lot after TrackOut.] */
    	            
    	            if(StringUtil.isNotEmpty(productListProcessingInfoF))
    	            {
    	                eventInfo.setEventName("Hold");
    	                eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", 
    	                        afterTrackOutLot.getKey().getLotName(), productListProcessingInfoF, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F));
    	                /* 20190424, hhlee, modify, changed function ==>> */
    	                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","",machineSpecData.getUdfs().get("DEPARTMENT"));
    	                /* 20190426, hhlee, modify, add variable(setFutureHold) */
    	                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","", true, "", machineSpecData.getUdfs().get("DEPARTMENT"));
    	                try {
    	                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    	                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","", true, false, "", machineSpecData.getUdfs().get("DEPARTMENT"));
    	                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						} catch (Exception e) {
							GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						}
    	                
    	                /* <<== 20190424, hhlee, modify, changed function */
    	            }
    	            
    	            if(StringUtil.isNotEmpty(productListProcessingInfoW))
    	            {
    	                eventInfo.setEventName("Hold");
    	                eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", 
    	                        afterTrackOutLot.getKey().getLotName(), productListProcessingInfoW, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W));
    	                /* 20190424, hhlee, modify, changed function ==>> */
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "PWHL", "", machineSpecData.getUdfs().get("DEPARTMENT"));
    	                /* 20190426, hhlee, modify, add variable(setFutureHold) */
    	                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PWHL","", true, "", machineSpecData.getUdfs().get("DEPARTMENT"));
    	                try {
    	                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    	                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PWHL","", true, false, "", machineSpecData.getUdfs().get("DEPARTMENT"));
    	                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						} catch (Exception e) {
							GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						}
    	                
                        /* <<== 20190424, hhlee, modify, changed function */
    	            }
    	            /* <<== 20181225, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
    	            
    	            /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
    	            //20180604, kyjung, MQC
    	            ProcessFlowKey processFlowKey = new ProcessFlowKey();
    	            processFlowKey.setFactoryName(afterTrackOutLot.getFactoryName());
    	            processFlowKey.setProcessFlowName(afterTrackOutLot.getProcessFlowName());
    	            processFlowKey.setProcessFlowVersion(afterTrackOutLot.getProcessFlowVersion());
    	            ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
    	            /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
    	            
    	    		// 2019.05.31_hsryu_Move To Logic. located TrackOutLot Function.
//    	            // if LotProcessState == Completed
//    	            // LotProcessOperationName -> NULL
//    	            if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//    	            {
//    	                afterTrackOutLot.setProcessOperationName("-");
//    	                afterTrackOutLot.setProcessOperationVersion("");
//    	                LotServiceProxy.getLotService().update(afterTrackOutLot);
//    	                
//    	                /* 20190129, hhlee, modify, change history update logic */
//    	                //String condition = "where lotname=?" + " and timekey= ? " ;
//    	                //Object[] bindSet = new Object[]{lotData.getKey().getLotName(),eventInfo.getEventTimeKey()};
////    	                String condition = "where lotname = ? and timekey = (select max(timekey) from lothistory where lotname = ?)" ;
////    	                Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getKey().getLotName()};
////    	                List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
////    	                LotHistory lotHistory = arrayList.get(0);
//    	                LotHistoryKey LotHistoryKey = new LotHistoryKey();
//    	    		    LotHistoryKey.setLotName(afterTrackOutLot.getKey().getLotName());
//    	    		    /* 20190426, hhlee, modify, change TimeKey(eventInfo.getEventTimeKey() -> afterTrackOutLot.getLastEventTimeKey() */
//                        //LotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
//                        LotHistoryKey.setTimeKey(afterTrackOutLot.getLastEventTimeKey());
//    	    		    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
//    	                lotHistory.setProcessOperationName("-");
//    	                lotHistory.setProcessOperationVersion("");
//    	                LotServiceProxy.getLotHistoryService().update(lotHistory);
//    	            }
    	            
    	            /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
    	            if(processFlowData != null)
    	            {
    	                if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
    	                {
        					try {
        						MESLotServiceProxy.getLotServiceImpl().checkMQCOperationListAndProcessFlowOperationListHoldLot(afterTrackOutLot.getKey().getLotName(),beforeProcessFlowName);
    						} catch (Exception e) {
    							eventLog.info("checkMQCOperationListAndProcessFlowOperationListHoldLot Error");
    						}
    	                	
    	                    eventInfo.setEventName("UpdateMQCCount");
    	                    eventInfo.setCheckTimekeyValidation(false);
    	                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
    	                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    	                    MESProductServiceProxy.getProductServiceImpl().updateMQCCountToProduct(afterTrackOutLot, eventInfo, processFlowData, beforeProcessOperationName);
    	                    
    	                    if(StringUtil.equals(afterTrackOutLot.getLotState(), "Completed"))
    	                    {
    	                        eventInfo.setEventName("FinishMQCJob");
    	                        eventInfo.setCheckTimekeyValidation(false);
    	                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
    	                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    	                        MESProductServiceProxy.getProductServiceImpl().checkFinishMQCJob(afterTrackOutLot, eventInfo, processFlowData);
    	                        
    	                        /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
    	                        MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLotMQC(afterTrackOutLot, eventInfo);                     
    	                        /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
    	                    }
    	                }
    	                else /* 20190129, hhlee, add, add lot complete */
    	                {
    	                    MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLot(afterTrackOutLot, eventInfo); 
    	                }
    	            }
    	            /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
    	            
    	            /* 20190105, hhlee, add, add Inhibit toMachine ==>> */         
    	            String toMachineName = StringUtil.EMPTY;
    	            if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),
    	                    GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
    	            {
    	                toMachineName = CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME");
    	            }
    	            /* <<== 20190105, hhlee, add, add Inhibit toMachine */
    	            
    	            /* 20181001, hhlee, modify, location move, lotdata change ==>> */
    	            //Added by jjyoo on 2018.9.20 - Check Inhibit Condition
    	            //this logic must be existed the end of LotInfoDownloadRequestNew.
    	            /* 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName ==>> */
    	            //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName, GenericServiceProxy.getConstantMap().Flag_Y);
    	            /* 20190422, hhlee, modify, check ExceptionLot in TrackOut(try ~~ catch ~~, After TrackOut, LotHold) ==>> */
                    try
                    {
                        MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName, 
                                GenericServiceProxy.getConstantMap().Flag_Y, toMachineName);
                    }
                    catch (Exception ex) 
                    {
                        eventInfo.setEventName("Hold");
                        eventInfo.setEventComment(MESLotServiceProxy.getLotServiceImpl().getReturnErrorMessage(ex));
                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, 
                        //        "HoldLot", "ABND", StringUtil.EMPTY, false, "A_LotProcessAborted", StringUtil.EMPTY);
                        try {
                        	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, 
                                    "HoldLot", "ABND", StringUtil.EMPTY, false, false, "A_LotProcessAborted", StringUtil.EMPTY);
                            GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						} catch (Exception e) {
							GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						}

                    }
                    /* <<== 20190422, hhlee, modify, check ExceptionLot in TrackOut(try ~~ catch ~~, After TrackOut, LotHold) */
    	            /* <<== 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName */
                    /* 20181001, hhlee, modify, location move, lotdata change ==>> */
    	            
    	            
    	            eventInfo.setEventName("Hold");
    			    eventInfo.setEventComment("Lot Process Abort Hold");
    			    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, afterTrackOutLot.getCarrierName(),null, "LA","");
    			    /* 20190424, hhlee, modify, changed function ==>> */
    	            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
    			    /* 20190426, hhlee, modify, add variable(setFutureHold) */
    			    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
    			    try {
    			    	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    			    	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
    			    	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					} catch (Exception e) {
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
    			    
    	            /* <<== 20190424, hhlee, modify, changed function */

    			    // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
    			    afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(afterTrackOutLot, note.toString(), eventInfo);

    			    // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//    			    //Port clear - YJYU
//    		        List<Lot> lotList_Port = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
//    		        Lot lotData_Port = lotList_Port.get(0);
//    		        Map<String, String> udfs_note = lotData_Port.getUdfs();
//    		        udfs_note.put("PORTNAME", "");
//    		        LotServiceProxy.getLotService().update(lotData_Port);
    			    List<Lot> portLotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
    			    Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("PORTNAME", "");
					
    			    for (Lot portLotData : portLotDataList) {
    			    	MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(portLotData, updateUdfs);
					}
    			}
    		}
    		else
    		{
    			String lotName = MESLotServiceProxy.getLotServiceUtil().getLotNamefromProductElements(productElement);
    			
	            // LotProcessAbort Note
	            StringBuilder note = new StringBuilder("");
	            
    			List<Element> newProductList = new ArrayList<Element>();
    
    			if( productElement != null)
    			{
    				for (Element product : productElement )
    				{
    					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
    					String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
    
    					if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
    					{
    						newProductList.add(product);
    					}
    					/*Get Old ProductFlagData*/
    					ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
    					/*Get Old ProductFlagData*/
    
    					/* Array 20180529, Add ==> */
    					/* Array Product Flag Upsert ==> */
    					//ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
    					ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
    					
    					/*Get New ProductFlagData*/
    					ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
    					/*Get New ProductFlagData*/
    					
    					/* Get Note */ 
    					ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
    					/* Get Note */
    					
                        /* <== Array Product Flag Upsert */
    					/* Array Product Process Unit/SubUnit ==> */
                        ExtendedObjectProxy.getProductInUnitOrSubUnitService().setProductProcessUnitOrSubUnit(eventInfo, machineData, product, carrierName);
                        /* <== Array Product Process Unit/SubUnit */
    					/* <<== Array 20180529, Add */
    
    					if (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
    	                {
    	                    /* 20180911, hhlee, Modify, ==>> */
    	                    //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
    	                    MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
    	                    /* <<== 20180911, hhlee, Modify, */
    	                }
    				}

    				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    				Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
    
    				/* 20181128, hhlee, EventTime Sync */
    				eventInfo.setCheckTimekeyValidation(false);
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    				//eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
                    eventInfo.setEventName("Split");
                    
                    Lot cancelTrackInLot = null;
    
    				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
                    Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
                    
    				List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotData.getKey().getLotName(), newProductList);
    
    				if (lotData.getProductQuantity() > productPGSRCSequence.size() && productPGSRCSequence.size() > 0)
    				{
    					cancelTrackInLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence);
    
    					/* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
                        //FutureAction
    					MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallSplit(lotData, cancelTrackInLot, eventInfo);
                        /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
    
                        /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
                        //Lot Hold Copy(CT_LOTMULTIHOLD)
                        MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, cancelTrackInLot, eventInfo, 
                                GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
                        /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
                        
    					//20180504, kyjung, QTime
    					for (Element productEle : newProductList )
    					{
    						String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    						Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
    
    						MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
    					}
    
    					/* 20181128, hhlee, EventTime Sync */
    	                eventInfo.setCheckTimekeyValidation(false);
    	                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
    	                //eventInfo = EventInfoUtil.makeEventInfo("TrackInCancel", getEventUser(), getEventComment(), "", "");
    	                eventInfo.setEventName("TrackInCancel");
    										
    					/*
    					String recipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProductSpecName(),
    							cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessOperationName(), machineName, false, cancelTrackInLot.getUdfs().get("ECCODE"));
    					
    					//20180612, kyjung, Recipe Idle Time
    					MESProductServiceProxy.getProductServiceImpl().cancelTIRecipeIdleTimeLot(machineName, recipeName, cancelTrackInLot.getProductSpecName(), cancelTrackInLot.getProcessOperationName(), eventInfo);
    					*/
    					
    					this.TrackInCancel(lotData, durableData.getKey().getDurableName(), eventInfo,note.toString());
    					
    					eventInfo.setEventName("Hold");
    					//holdLot Requested by EDO 20180504
    					//MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LA","");
    					eventInfo.setEventComment("Lot Process Abort Hold");
    	                
    	                /* 20190424, hhlee, modify, changed function ==>> */
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
    					/* 20190426, hhlee, modify, add variable(setFutureHold) */
                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", false, "", " ");
    					try {
    						GenericServiceProxy.getTxDataSourceManager().beginTransaction();
    						MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", false, false, "", "");
    						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						} catch (Exception e) {
							GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						}
                        
                        /* <<== 20190424, hhlee, modify, changed function */
    				
        			    // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
                        lotData = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(lotData, note.toString(), eventInfo);

    				}
    				
    				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//    			    //Port clear - YJYU
//    		        List<Lot> lotList_Port = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
//    		        Lot lotData_Port = lotList_Port.get(0);
//    		        Map<String, String> udfs_note = lotData_Port.getUdfs();
//    		        udfs_note.put("PORTNAME", "");
//    		        LotServiceProxy.getLotService().update(lotData_Port);
    			    List<Lot> portLotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
    			    Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("PORTNAME", "");
					
    			    for (Lot portLotData : portLotDataList) {
    			    	MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(portLotData, updateUdfs);
					}
    			}
    		}
            //start by jhying on20200303 mantis:5435
            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
	       
	        List<LocalRunException> localRunExceptionList = ExtendedObjectProxy.getLocalRunExceptionService().select("WHERE LOTNAME = ? AND PROCESSFLOWNAME = ? AND  PROCESSOPERATIONNAME = ? AND  MACHINENAME = ?"
	                ,new Object[]{lotList.get(0).getKey().getLotName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(),machineName});
	        if(localRunExceptionList != null && localRunExceptionList.size()>0 ){
	      
	             if(MESLotServiceProxy.getLotServiceImpl().isExistLocalRunException(lotList.get(0).getKey().getLotName(), lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(),machineName ))
	             {
	            	 localRunExceptionList.get(0).setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_EXECUTED);

				   ExtendedObjectProxy.getLocalRunExceptionService().remove(eventInfo, localRunExceptionList.get(0));
	            }
	        }
	        
	      //end by jhying on20200303 mantis:5435
    		try
    		{
    			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
    		}
    		catch(Exception ex)
    		{
    			eventLog.warn("FMC Report Failed!");
    		}
    
    		// 2018-11-21 Park Jeong Su Add for ClearName And Update LastRuntime
            MESLotServiceProxy.getLotServiceUtil().clearCarrerNameOnAutoMQCSetting(eventInfo, carrierName,machineName);
        }
        /* 20190410, hhlee, modify, LotProcessEnd fail ==>> */
        catch (Exception ex) 
        {
            /* EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName, 
             * boolean setLotHold, boolean setCarrierHold, boolean carrierStart
             */
            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
            /* 20190425, hhlee, modify, change variable(carrierEnd delete) */
            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
            //        carrierName, StringUtil.EMPTY, false, false, false, true); 
            /* 20190425, hhlee, modify, add variable(setFutureHold) */
            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
            //        carrierName, StringUtil.EMPTY, true, false, "TrackOutFail");
            /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
            //        carrierName, StringUtil.EMPTY, false, false, false, "TrackOutFail");
            
            try {
            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
            	doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc,carrierName, StringUtil.EMPTY, true, false, false, false, "TrackOutFail"); 
                GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception e) {
				eventLog.info("Error setReturnMessageByTrackOut!");
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}

        }
              
		return doc;
	}

	private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		eventLog.info("Split Lot for TK out");

		eventInfo.setEventName("Create");
		Lot targetLot = this.createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			ProductP productP = new ProductP();
			productP.setProductName(productPGSRC.getProductName());
			productP.setPosition(productPGSRC.getPosition());
			productP.setUdfs(productPGSRC.getUdfs());
			productPSequence.add(productP);
		}

		TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
														targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());

		//do splits
		eventInfo.setEventName("Split");
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

		targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());

		eventLog.info(String.format("Lot[%s] is separated", targetLot.getKey().getLotName()));

		return targetLot;
	}

	public Lot createWithParentLot(EventInfo eventInfo, Lot parentlotData, String newCarrierName, boolean deassignFlag,
			Map<String, String> assignCarrierUdfs, Map<String, String> udfs)
					throws CustomException
	{
		String newLotName = "";
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(
				parentlotData.getFactoryName(), parentlotData.getProductSpecName(),GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		//nameRuleAttrMap.put("PRODUCTIONTYPE", srcLotData.getProductionType());
		//nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));
		nameRuleAttrMap.put("FACTORYNAME", productSpecData.getKey().getFactoryName());
		nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());

		try
		{
			//20171221, kyjung
			String lotName = StringUtil.substring(parentlotData.getKey().getLotName(), 0, 8);
			List<String> argSeq = new ArrayList<String>();
			argSeq.add(lotName);
			List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);

			//List<String> lstName = CommonUtil.generateNameByNamingRule("LotNaming", nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(
				parentlotData.getAreaName(),
				//"Y" , assignCarrierUdfs, newCarrierName,
				deassignFlag?"N":"Y", assignCarrierUdfs, deassignFlag?"":newCarrierName,
						parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
						parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(),
						parentlotData.getLotHoldState(), newLotName, parentlotData.getLotProcessState(), parentlotData.getLotState(),
						parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
						parentlotData.getOriginalLotName(), parentlotData.getPriority(),
						parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
						parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), parentlotData.getProductionType(),
						new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
						parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(),
						parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
						parentlotData.getReworkCount(), "", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(),
						parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
						parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(),
						udfs, parentlotData);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	private void copyFutureAction(Lot parent, Lot child)
	{
		//Copy  FutureAction from parent to child
		// Find future Action
		Object[] bindSet =
				new Object[] {
				parent.getKey().getLotName(),
				parent.getFactoryName(),
				parent.getProcessFlowName(),
				parent.getProcessFlowVersion(),
				parent.getProcessOperationName(),
				parent.getProcessOperationVersion() };

		try
		{
			List<LotFutureAction> lotFutureActionList = LotServiceProxy.getLotFutureActionService().select(SqlStatement.LotFutureActionKey, bindSet);
			if(lotFutureActionList.size() > 0)
			{
				Object[] bindSetChild =
						new Object[] {
						child.getKey().getLotName(),
						child.getFactoryName(),
						child.getProcessFlowName(),
						child.getProcessFlowVersion(),
						child.getProcessOperationName(),
						child.getProcessOperationVersion() };

				String condition = "";

				for(LotFutureAction action : lotFutureActionList)
				{
					LotServiceProxy.getLotFutureActionService().update(action, condition, bindSetChild);
				}
			}
		}
		catch (NotFoundSignal ne)
		{
			return;
		}
		catch(Exception e)
		{
			return;
		}
	}

	private void TrackInCancel(Lot lotData, String carrierName, EventInfo eventInfo,String note) throws CustomException
	{
		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();
		makeLoggedOutInfo.setAreaName(lotData.getAreaName());
		makeLoggedOutInfo.setMachineName(lotData.getMachineName());
		//makeLoggedOutInfo.setMachineRecipeName(lotData.getMachineRecipeName());
		makeLoggedOutInfo.setProcessFlowName(lotData.getProcessFlowName());
		makeLoggedOutInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		makeLoggedOutInfo.setProcessOperationName(lotData.getProcessOperationName());
		makeLoggedOutInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
		makeLoggedOutInfo.setNodeStack(lotData.getNodeStack());
		
		Durable durableData = null;
		if (StringUtils.isNotEmpty(carrierName)) {
			makeLoggedOutInfo.setCarrierName(carrierName);
			
			// Added by smkang on 2018.10.27 - If a carrier state is changed from Available to InUse, synchronization should be executed.
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		}
		
		if(note.length()>3500){
			note = note.substring(0, 3499);
		}
		// 2019.06.03_hsryu_Delete Logic. Mantis 0003934. Not Memory CancelTrackIn Event. Momofy NewEvent. 
		//makeLoggedOutInfo.getUdfs().put("NOTE", note);

		Lot cancelTrackInLotData = LotServiceProxy.getLotService().makeLoggedOut(lotData.getKey(), eventInfo, makeLoggedOutInfo);
		
		// Added by smkang on 2019.03.04 - TrackOut 또는 CancelTrackIn 후 Lot의 MachineName과 PortName을 삭제하는 코드 임시 적용
		cancelTrackInLotData.setMachineName("");
		cancelTrackInLotData.getUdfs().put("PORTNAME", "");
        LotServiceProxy.getLotService().update(cancelTrackInLotData);
		
	/*	// Added by smkang on 2018.10.27 - For synchronization of a carrier state and lot quantity, common method will be invoked.
        try {
        	if (durableData != null && durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
				Element bodyElement = new Element(SMessageUtil.Body_Tag);
				bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
				bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_InUse));
				
				// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
				
				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
        	}
        } catch (Exception e) {
        	eventLog.warn(e);
        }*/
	}

    /**
     * @Name     UnpackerProcessAbort
     * @since    2018. 5. 20.
     * @author   hhlee
     * @contents Process for Abort in the PU Port of Unpacker
     * @param doc
     * @throws CustomException
     */
	public void UnpackerProcessAbort(Document doc)  throws CustomException
    {
	    String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", true);
		String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
		String sLotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", true);
		String sLotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String lotJudge = "";

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");

		//Machine Data
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));

		/* 20180713, Add, CST Validation ==>> */
		CommonValidation.checkEmptyCst(carrierName);
		Lot  validateLotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
		if (validateLotData != null)
        {
            throw new CustomException("CST-0006", carrierName);
        }
		/* <<== 20180713, Add, CST Validation */
		 
		try
        {
            if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals("NG"))
            {
                if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
                {
                    eventLog.info("Unpacker MGV unloader case");

                    boolean doit = false;

                    List<Element> eProductListElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

                    for(Element ePruduct : eProductListElement)
                    {
                        String sProductName = SMessageUtil.getChildText(ePruduct, "PRODUCTNAME", true);
                        String sPosition = SMessageUtil.getChildText(ePruduct, "POSITION", true);

                        VirtualGlass productData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {sProductName});

                        productData.setCarrier(durableData.getKey().getDurableName());
                        productData.setPosition(Long.parseLong(sPosition));

                        eventInfo.setEventName("Assign");
                        productData = ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, productData);

                        doit = true;
                    }

                    if (doit)
                    {
                        durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
                        durableData.setLotQuantity(1);

                        eventInfo.setEventName("AssignCarrier");
                        SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableData.getUdfs());

                        DurableServiceProxy.getDurableService().update(durableData);
                        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
                    }

                    return;
                }
                else
                {
                    eventLog.info("Unpacker MGV loader case");
                    return;
                }
            }

            eventLog.info("Unpacker AGV unloader case");

            /* 20181106, hhlee, modify, the Lot is not mixed in when Unpacker becomes LotProcessEnd ==>> */
            ///* 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. ==>> */
            String lotName = MESLotServiceProxy.getLotServiceUtil().getLotNamefromProductElements(productElement);
            //String lotName = MESLotServiceProxy.getLotServiceUtil().getFirstLotNamefromProductElements(productElement, machineName, GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
            ///* <<== 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. */
            /* <<== 20181106, hhlee, modify, the Lot is not mixed in when Unpacker becomes LotProcessEnd */
            
            //modified by wghuang 20181105        
            ReserveLot reserveLot = null;
            ProductRequestPlanKey pPlanKey = null;
            ProductRequestPlan pPlanData = null;
            
            //Product Request Key & Data
            ProductRequestKey pKey = new ProductRequestKey();
                
            ProductRequest pData = null;
            
            try
            {
                try
                {
                     //Reserve Lot Data
                    reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});
                }
                catch(Exception ex)
                {                  
                }
                
                if(reserveLot == null)
                    throw new CustomException("LOT-0095",machineName,lotName );
                
                //Product Request Plan Data
                pPlanKey = new ProductRequestPlanKey(reserveLot.getProductRequestName(), machineName, reserveLot.getPlanReleasedTime());
                             
                try
                {
                	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
                	pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKeyForUpdate(pPlanKey);
                }
                catch(Exception ex)
                {                  
                }
                
                if(pPlanData == null)
                    throw new CustomException("PRODUCTREQUEST-0054","");
                
                
                //productRequestData
                pKey.setProductRequestName(pPlanData.getKey().getProductRequestName());
                
                try
                {
                    pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
                }
                catch(Exception ex)
                {                  
                }
                
                if(pData == null)
                    throw new CustomException("PRODUCTREQUEST-0055","");        
            }
            catch(Exception ex)
            {
                throw ex;
            }
            
            //modified by wghuang 20181106
            
            /*     //Reserve Lot Data
            ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});

            //Product Request Plan Data
            ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey(reserveLot.getProductRequestName(), machineName, reserveLot.getPlanReleasedTime());
            ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);

            //Product Request Key & Data
            ProductRequestKey pKey = new ProductRequestKey();
            pKey.setProductRequestName(pPlanData.getKey().getProductRequestName());
            ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);*/

            //1. Release Lot
            //1.1) get lot Data by Lot Name
            Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

            double lotCreateProductQty = lotData.getCreateProductQuantity();

            //Product Spec Data
            ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

            //1.2) get PGSSequance
            List<Element> eProductListElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
            List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

            /*String[] prdname = {"W", "V", "U", "T", "S", "R", "Q", "P", "N", "M",
                    "L", "K", "J", "H", "G", "F", "E", "D", "C", "B",
                    "A", "9", "8", "7", "6", "5", "4", "3", "2", "1"};*/

            String[] prdname = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
                                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
            
            int prdcnt = 0;

            VirtualGlass vGlassData = new VirtualGlass();

            String cutType = specData.getUdfs().get("CUTTYPE");
            List<Object[]> insertArgList = new ArrayList<Object[]>();
            String insertSql = " INSERT INTO CT_PANELJUDGE "
                    + " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
                    + " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
                    + " VALUES "
                    + " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
                    + "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
            
            
            List<Object[]> insertArgListForHQGlass = new ArrayList<Object[]>();
            
            String insertSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGE "
                    + " (HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, GLASSNAME, "
                    + " LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT)"
                    + " VALUES "
                    + " (:HQGLASSNAME, :HQGLASSJUDGE, NVL(:XAXIS,0), NVL(:YAXIS,0), :GLASSNAME, "
                    + " :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT)";

            
            
            for(Element ePruduct : eProductListElement)
            {
                //String sProductName = lotName + prdname[prdcnt++];
                //String sProductName = StringUtil.substring(lotName, 0, 8) + prdname[prdcnt++];

                String sPosition = SMessageUtil.getChildText(ePruduct, "POSITION", true);
                String sVcrProductName = SMessageUtil.getChildText(ePruduct, "VCRPRODUCTNAME", true);
                String sCrateName = SMessageUtil.getChildText(ePruduct, "CRATENAME", false);

                //String sProductName = lotName + prdname[prdcnt++];
                String sProductName = StringUtil.substring(lotName, 0, 8) + prdname[Integer.parseInt(sPosition) - 1];
                
                /* 20180920, hhlee, add Processed Operation Before AOI ==>> */
                String machineRecipeName = SMessageUtil.getChildText(ePruduct, "PRODUCTRECIPE", false);
                /* <<== 20180920, hhlee, add Processed Operation Before AOI */
                
                try
                {
                    VirtualGlass vData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {sVcrProductName});

                    if(sCrateName.isEmpty())
                    {
                        sCrateName = vData.getCrateName().toString();
                    }

                    //Update product name to Virtual Glass Table
                    vData.setProductName(sProductName);
                    vData.setPosition(Long.parseLong(sPosition));
                    vGlassData = ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vData);
                                         
                    /* 20180920, hhlee, add Processed Operation Before AOI ==>> */
                    //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(sProductName, machineName, lotData.getFactoryName(), lotData.getProcessOperationName(), vData.getVirtualGlassName());
                    /* 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) ==>> */
                    //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, sProductName, machineName, lotData, machineRecipeName, vData.getVirtualGlassName());
                    MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, ePruduct, sProductName, machineName, lotData, machineRecipeName, vData.getVirtualGlassName());
                    /* <<== 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) */
                    /* <<== 20180920, hhlee, add Processed Operation Before AOI */
                }
                catch (Exception ex)
                {

                }
                
                ProductPGS productInfo = new ProductPGS();
                productInfo.setPosition(Long.parseLong(sPosition));
                productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
                productInfo.setProductName(sProductName);
                productInfo.setSubProductGrades1("");
                productInfo.setSubProductQuantity1(specData.getSubProductUnitQuantity1());
                productInfo.getUdfs().put("CRATENAME", sCrateName);
                
                
    			Consumable crateData = null;
    			// Start 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
    			try {
    				crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sCrateName);
    				productInfo.getUdfs().put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
				} catch (Exception e) {
					eventLog.info("crateData is Not Found");
				}
    			// End 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
    			
    			// add by GJJ 20200101 mantis:5531
                //CRATESPECNAME
                if (!StringUtils.equalsIgnoreCase(pData.getUdfs().get("crateSpecName"), crateData.getConsumableSpecName())) 
               {
               	 throw new CustomException("PRODUCT-8001",sPosition ,crateData.getConsumableSpecName(),pData.getKey().getProductRequestName(),pData.getUdfs().get("crateSpecName"));
				}
                
             // add by GJJ 20200101 mantis:5531
       
                /* 20181113, hhlee, delete, ==>> */
                ///* 20181023, hhlee, add, productJudge ==>> */
                //productInfo.getUdfs().put("PRODUCTJUDGE", productInfo.getProductGrade());
                ///* <<== 20181023, hhlee, add, productJudge */
                /* <<== 20181113, hhlee, delete, */
                
                productPGSSequence.add(productInfo);
                            
                try
                {
                    if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_HALF) )
                    {
                        int cut1XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1XAXISCOUNT")) : 0;
                        int cut2XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2XAXISCOUNT")) : 0;
                        int cut1YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1YAXISCOUNT")) : 0;
                        int cut2YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2YAXISCOUNT")) : 0;
                        
                        for ( int i = 1; i < 3; i++ )
                        {
                            if(i==1)
                            {
                                for(int x=0; x<cut1XaxisCount; x++)
                                {
                                    for(int y=0; y< cut1YaxisCount; y++)
                                    {
                                        Object[] inbindSet = new Object[15];

                                        inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
                                        inbindSet[1] = "G";
                                        inbindSet[2] = "G";
                                        inbindSet[3] = String.valueOf(cut1XaxisCount);
                                        inbindSet[4] = String.valueOf(cut1YaxisCount);
                                        inbindSet[5] = String.valueOf(cut2XaxisCount);
                                        inbindSet[6] = String.valueOf(cut2YaxisCount);
                                        inbindSet[7] = sProductName;
                                        inbindSet[8] = sProductName + Integer.toString(i);
                                        inbindSet[9] = cutType;
                                        inbindSet[10] = "Created";
                                        inbindSet[11] = eventInfo.getEventUser();
                                        inbindSet[12] = ConvertUtil.getCurrTime();
                                        inbindSet[13] = "Auto Create PanelJudge";
                                        inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
                                        
                                        insertArgList.add(inbindSet);
                                    }
                                }
                            }
                            
                            else if(i==2)
                            {
                                for(int x=0; x<cut2XaxisCount; x++)
                                {
                                    for(int y=cut1YaxisCount; y<cut1YaxisCount+cut2YaxisCount; y++)
                                    {
                                        Object[] inbindSet = new Object[15];

                                        inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
                                        inbindSet[1] = "G";
                                        inbindSet[2] = "G";
                                        inbindSet[3] = String.valueOf(cut1XaxisCount);
                                        inbindSet[4] = String.valueOf(cut1YaxisCount);
                                        inbindSet[5] = String.valueOf(cut2XaxisCount);
                                        inbindSet[6] = String.valueOf(cut2YaxisCount);
                                        inbindSet[7] = sProductName;
                                        inbindSet[8] = sProductName + Integer.toString(i);
                                        inbindSet[9] = cutType;
                                        inbindSet[10] = "Created";
                                        inbindSet[11] = eventInfo.getEventUser();
                                        inbindSet[12] = ConvertUtil.getCurrTime();
                                        inbindSet[13] = "Auto Create PanelJudge";
                                        inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
                                        
                                        insertArgList.add(inbindSet);
                                    }
                                }
                            }

                            // for HQGlassJudge
                            Object[] inbindSetForHQGlass = new Object[9];

                            inbindSetForHQGlass[0] = sProductName + Integer.toString(i);
                            inbindSetForHQGlass[1] = "G";
                            if(i==1)
                            {
                                inbindSetForHQGlass[2] = String.valueOf(cut1XaxisCount);
                                inbindSetForHQGlass[3] = String.valueOf(cut1YaxisCount);
                            }
                            else if(i==2)
                            {
                                inbindSetForHQGlass[2] = String.valueOf(cut2XaxisCount);
                                inbindSetForHQGlass[3] = String.valueOf(cut2YaxisCount);
                            }
                            inbindSetForHQGlass[4] = sProductName;
                            inbindSetForHQGlass[5] = "Created";
                            inbindSetForHQGlass[6] = eventInfo.getEventUser();
                            inbindSetForHQGlass[7] = ConvertUtil.getCurrTime();
                            inbindSetForHQGlass[8] = "Auto Create HQGlassJudge";

                            insertArgListForHQGlass.add(inbindSetForHQGlass);
                        }
                    }
                    else if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_QUARTER) )
                    {
                        for ( int i = 1; i < 5; i++ )
                        {
                            String character = "";
                            if (i == 1)
                            {
                                character = "A";
                            }
                            else if (i == 2)
                            {
                                character = "B";
                            }
                            else if (i == 3)
                            {
                                character = "C";
                            }
                            else if (i == 4)
                            {
                                character = "D";
                            }
                            
                            Object[] inbindSet = new Object[15];
                            inbindSet[0] = sProductName + Integer.toString(i) + character;
                            inbindSet[1] = "G";
                            inbindSet[2] = "G";
                            inbindSet[3] = specData.getUdfs().get("CUT1XAXISCOUNT");
                            inbindSet[4] = specData.getUdfs().get("CUT1YAXISCOUNT");
                            inbindSet[5] = specData.getUdfs().get("CUT2XAXISCOUNT");
                            inbindSet[6] = specData.getUdfs().get("CUT2YAXISCOUNT");
                            inbindSet[7] = sProductName;
                            inbindSet[8] = sProductName + Integer.toString(i);
                            inbindSet[9] = cutType;
                            inbindSet[10] = "Created";
                            inbindSet[11] = eventInfo.getEventUser();
                            inbindSet[12] = ConvertUtil.getCurrTime();
                            inbindSet[13] = "Auto Create PanelJudge";
                            inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
                            
                            insertArgList.add(inbindSet);
                        }
                    }
                }
                catch (Throwable e)
                {
                    eventLog.warn(String.format("BindSet Fail! CT_PANELJUDGE"));
                }
            }

            //1.3)Release Lot
            MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(
                    lotData, machineData.getAreaName(), lotData.getNodeStack(),
                    lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
                    lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
                    lotData.getProductionType(),
                    lotData.getUdfs(), "",
                    lotData.getDueDate(), lotData.getPriority());

            lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);

            /* Array Product Flag Insert ==> */
            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
            List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
            
            if(productList != null)
            {
                ExtendedObjectProxy.getProductFlagService().setCreateProductFlagForUPK(eventInfo, productList);
            }
            /* <== Array Product Flag Insert */
            
            //String crateName = lotData
            //1.4)If Lot Create Product Qty != Product Qty, Future Hold Lot
            if(lotData.getProductQuantity() != lotCreateProductQty)
            {
                eventLog.info("Lot Create Product Quantity and Product Quantity are different");
                //futureHoldLot(lotData, machineName, eventInfo);
            }
            
            ///* hhlee, 20180703, Add Unpacker LotProcessAborted Hold ==>> */
            ////futureHoldLot(lotData, machineName, eventInfo);
            //eventInfo.setEventComment("LotProcessAborted occurred");
            //MESLotServiceProxy.getLotServiceUtil().futureHoldLot(lotData, machineName, eventInfo, "AHOLD", "LAHL");
            ///* <<== hhlee, 20180703, Add Unpacker LotProcessAborted Hold */
            
            //2. increment Product Request
            try
            {
                pPlanData.setPlanQuantity(pPlanData.getPlanQuantity() - ((long)lotCreateProductQty - productPGSSequence.size()));
                ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
                pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
                
                MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(pPlanData.getKey().getProductRequestName(), pPlanData, "R", productPGSSequence.size(), eventInfo);

            }
            catch(Exception e)
            {
                eventLog.error("incrementWorkOrderReleaseQty Failed");
            }

            //3. Reserve Lot State Change
            eventInfo.setEventName("ChangeState");
            reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);

            ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
            
            //5. Auto Track In / Out Unpacker
            String loadPort = "";
            try
            {
                Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(vGlassData.getCrateName().toString());
                loadPort = consumableData.getMaterialLocationName();

                if(consumableData.getMaterialLocationName().isEmpty())
                {
                    loadPort = "1";
                }
            }
            catch (Exception ex)
            {
                loadPort = "1";
            }

            try
            {
                GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
                
                String insHistSql = " INSERT INTO CT_PANELJUDGEHISTORY "
                        + " (TIMEKEY, PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
                        + " GLASSNAME, HQGLASSNAME, CUTTYPE, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTSPECTYPE) "
                        + " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
                        + " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE "
                        + " FROM CT_PANELJUDGE "
                        + " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
                
                Map<String, Object> insHistbindSet = new HashMap<String, Object>();
                insHistbindSet.put("LOTNAME", lotData.getKey().getLotName());
                
                GenericServiceProxy.getSqlMesTemplate().update(insHistSql, insHistbindSet);
            }
            catch (Exception ex)
            {
                eventLog.warn(String.format("Update Fail! CT_PANELJUDGE"));
            }
            
            try
            {
                GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSqlForHQGlass, insertArgListForHQGlass);
                
                String insHistSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGEHIST "
                        + " (TIMEKEY, HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
                        + " GLASSNAME, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT) "
                        + " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
                        + " GLASSNAME, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT "
                        + " FROM CT_HQGLASSJUDGE "
                        + " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
                
                Map<String, Object> insHistbindSetForHQGlass = new HashMap<String, Object>();
                insHistbindSetForHQGlass.put("LOTNAME", lotData.getKey().getLotName());
                
                GenericServiceProxy.getSqlMesTemplate().update(insHistSqlForHQGlass, insHistbindSetForHQGlass);
            }
            catch (Exception ex)
            {
                eventLog.warn(String.format("Update Fail! CT_HQGLASSJUDGE"));
            }
            
            Document docCopy = (Document)doc.clone();

            String [] CrateNameAndPRecipeName = MESLotServiceProxy.getLotServiceUtil().getCrateNameAndMRecipefromProductElements(productElement).split(",");

            Document trackInOutDoc = writeTrackInOutRequest(docCopy, lotName, machineName, carrierName, loadPort, portName,CrateNameAndPRecipeName[0],CrateNameAndPRecipeName[1]);

            this.TrackInOutLot(trackInOutDoc);

            eventInfo.setEventName("Hold");
            eventInfo.setEventComment("Lot Process Abort Hold");
            /* 20190424, hhlee, modify, changed function ==>> */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
            /* 20190426, hhlee, modify, add variable(setFutureHold) */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
            try {
            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
            	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
            	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception e) {
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}
            
            /* <<== 20190424, hhlee, modify, changed function */
                       
            /*String replySubject = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
                    GenericServiceProxy.getESBServive().sendBySender(replySubject, trackInOutDoc, "LocalSender");*/
        }
        catch (CustomException ce)     
        {
           //add by wghuang 20181105
           //modified by wghuang 20181105. requested by guishi.[Release, TrackIn, TrackOut in one Transaction and doing commit and rollback at same time. if one of those got fail than this transaction will be rollbacked.]
           GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
                     
           //Start a new Transaction
           //create a new transaction doing HoldCST
           GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
           
           /* 20180531, Add CST Hold Logic ==>> */
           /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */                           
           eventInfo.setEventComment("TrackOut Fail.!" + ce.errorDef.getLoc_errorMessage());
           MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), eventInfo.getEventComment(), "HoldCST","LAHC");
           SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
           /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */
           SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "TrackOut Fail.!" + ce.errorDef.getLoc_errorMessage());
           /* <<== 20180531, Add CST Hold Logic */
           
         //Commit new Transaction
           GenericServiceProxy.getTxDataSourceManager().commitTransaction();
        }
        /* <<== 20180616, hhlee, Modify */    	
    }

	private void TrackInOutLot(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String loadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
		String unLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		String productRecipeName = SMessageUtil.getBodyItemValue(doc, "PRODUCTRECIPE", false);

		Object result = null;

		Lot lotData =  MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		try
		{
			Port loader = this.searchLoaderPort(machineName);

			String machineRecipeName = StringUtil.EMPTY;

			if(StringUtil.isEmpty(productRecipeName))
			{
				ConsumableKey consumableKey = new ConsumableKey(crateName);
				Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
				String consumableSpec = con.getConsumableSpecName();

				ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
				consumableSpecKey.setConsumableSpecName(consumableSpec);
				consumableSpecKey.setConsumableSpecVersion("00001");
				consumableSpecKey.setFactoryName(con.getFactoryName());
				ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

			    machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");
			}
			else
			{
				machineRecipeName = productRecipeName;
			}

			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), loadPort, machineRecipeName);

			MESLotServiceProxy.getLotServiceUtil().TrackInLotForUPK(doc, eventInfo);
			
			Port unloader = MESLotServiceProxy.getLotServiceUtil().searchUnloaderPort(loader);

			//doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName);
			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName, machineRecipeName);
			
			MESLotServiceProxy.getLotServiceUtil().TrackOutLotForUPK(doc, eventInfo);
		}
		catch (Exception e)
		{
			eventLog.error(e);
			throw e;
		}

		eventLog.debug("end");
	}

	private Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
		//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("CARRIERNAME");
		element4.setText(carrierName);
		eleBodyTemp.addContent(element4);

		Element elementPL = new Element("PRODUCTLIST");
		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product productData : productList)
			{
				Element elementP = new Element("PRODUCT");
				{
					Element elementS1 = new Element("PRODUCTNAME");
					elementS1.setText(productData.getKey().getProductName());
					elementP.addContent(elementS1);

					Element elementS2 = new Element("POSITION");
					elementS2.setText(String.valueOf(productData.getPosition()));
					elementP.addContent(elementS2);

					Element elementS3 = new Element("PRODUCTJUDGE");
					elementS3.setText(productData.getProductGrade());
					elementP.addContent(elementS3);
				}
				elementPL.addContent(elementP);
			}
		}
		catch (NotFoundSignal e)
	    {
	    	throw new CustomException("PRODUCT-9001", "");
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());
	    }
		eleBodyTemp.addContent(elementPL);

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}

	private Port searchUnloaderPort(Port portData) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
		{
			return portData;
		}
		else
		{
			try
			{
				List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?)", new Object[] {portData.getKey().getMachineName(), "PU", "PS"});

				return result.get(0);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PORT-9001", portData.getKey().getMachineName(), "");
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PORT-9999", fe.getMessage());
			}
		}
	}

	private Port searchLoaderPort(String machineName) throws CustomException
	{
		try
		{
			List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?)", new Object[] {machineName, "PB", "PL", "PS"});

			return result.get(0);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, "");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}

	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName)
			throws CustomException
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInLot");
			SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", StringUtil.EMPTY);

			//Element eleBody = SMessageUtil.getBodyElement(doc);

			boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			Element element1 = new Element("LOTNAME");
			element1.setText(lotName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("MACHINENAME");
			element2.setText(machineName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("PORTNAME");
			element3.setText(portName);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("RECIPENAME");
			element4.setText(recipeName);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("AUTOFLAG");
			element5.setText("Y");
			eleBodyTemp.addContent(element5);

			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);

			return doc;
		}

	private Document writeTrackInOutRequest(Document doc, String lotName, String machineName, String carrierName, String loadPort, String unLoadPort, String crateName, String productRecipeName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInOutLot");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", StringUtil.EMPTY);

		//Element eleBody = SMessageUtil.getBodyElement(doc);

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("CARRIERNAME");
		element3.setText(carrierName);
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("LOADPORT");
		element4.setText(loadPort);
		eleBodyTemp.addContent(element4);

		Element element5 = new Element("UNLOADPORT");
		element5.setText(unLoadPort);
		eleBodyTemp.addContent(element5);

		Element element6 = new Element("CRATENAME");
		element6.setText(crateName);
		eleBodyTemp.addContent(element6);

		Element element7 = new Element("PRODUCTRECIPE");
		element7.setText(productRecipeName);
		eleBodyTemp.addContent(element7);

		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}

	/**
	 * @Name     GlassProcessEnd
	 * @since    2018. 5. 20.
	 * @author
	 * @contents Process for Abort on PU Port of EVAP
	 * @param doc
	 * @throws CustomException
	 */
	public void GlassProcessEnd(Document doc) throws CustomException
    {
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

        //for common
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

        HashMap<String, String> assignCarrierUdfs = new HashMap<String, String>();
        HashMap<String, String> deassignCarrierUdfs = new HashMap<String, String>();

        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
        Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

        String lotJudge = "";

        List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotName);

        lotData.getUdfs().put("UNITNAME", "");

        String decideSampleNodeStack = "";

        //TK OUT
        Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotData, null,
                                                lotData.getCarrierName() , lotJudge, machineName, "",
                                                productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, false,null);

        // Repair
        //afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startAlteration(eventInfo, afterTrackOutLot, afterTrackOutLot.getLotGrade());

        //success then report to FMC
        try
        {
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
        }
        catch(Exception ex)
        {
            eventLog.warn("FMC Report Failed!");
        }
    }
	
	private void sortProcessAbort(Document doc) throws CustomException
	{
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		Lot trackOutLot = null;
		
		// LotProcessEnd Note
		StringBuilder note = new StringBuilder("");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		///* Array 20180807, Add [Process Flag Update] ==>> */   
		////slot map validation
        //String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
        ///* <<== Array 20180807, Add [Process Flag Update] */   
        
		CommonValidation.CheckDurableHoldState(durableData);

		//String detailSortJobType = ExtendedObjectProxy.getSortJobService().getSortJobType(carrierName);
		String sortTransferDirection = ExtendedObjectProxy.getSortJobService().getTransferDirection(carrierName);

		/* 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check ==>> */
		//List<SortJob> sortJobList = null;
		List<Map<String, Object>> sortJobList = null;
        String sortJobName = StringUtil.EMPTY;
        String sortJobType = StringUtil.EMPTY;
        String detailSortJobType = StringUtil.EMPTY;
        try
        {                                                                        
            //sortJobList = ExtendedObjectProxy.getSortJobService().select(" WHERE CARRIERNAME = ? AND JOBSTATE = ? ", 
            //        new Object[] {carrierName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED});    
            sortJobList = ExtendedObjectProxy.getSortJobService().getSortJobNameByCarrierName(carrierName);  
            if(sortJobList != null && sortJobList.size() > 0)
            {
                sortJobName = sortJobList.get(0).get("JOBNAME").toString();
                sortJobType = sortJobList.get(0).get("JOBTYPE").toString();
                detailSortJobType = sortJobList.get(0).get("DETAILJOBTYPE").toString();
            }            
        }
        catch (Exception ex)
        {
            eventLog.warn(ex.getStackTrace());            
        }
        
        List<SortJobCarrier> sortJobCarrierList = null;
        String outHoldFlag = GenericServiceProxy.getConstantMap().Flag_N;
        try
        {
            sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(" WHERE JOBNAME = ? AND CARRIERNAME = ? ", 
                                        new Object[] {sortJobName, carrierName});
            
            outHoldFlag = StringUtil.isEmpty(sortJobCarrierList.get(0).getoutholdflag())? GenericServiceProxy.getConstantMap().Flag_N:sortJobCarrierList.get(0).getoutholdflag();
        }
        catch (Exception ex)
        {
            //eventLog.warn(ex.getStackTrace());
            sortJobCarrierList = new ArrayList<SortJobCarrier>() ;
        }               
        /* <<== 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check */
        
		if(StringUtil.isEmpty(detailSortJobType))
		{
			throw new CustomException("SORT-0001", "");
		}

		/* 20180929, hhlee, add, Sort Job TrackIn Validation ==>> */
        List<Lot> carrierLotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
        if(carrierLotList != null && carrierLotList.size() > 0)
        {
            if(StringUtil.equals(carrierLotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
            {
                throw new CustomException("LOT-9003", carrierLotList.get(0).getKey().getLotName() +". Current State is " + carrierLotList.get(0).getLotProcessState());
            }
            
            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
            {
                if(StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
                {
                    if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT))
                    {
                        eventLog.warn(String.format("DetailJobType Missmatch.[Carriername = %s, OriginalDetailJobType = %s, CurrentDetailJobType = %s]", 
                                carrierName, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE));  
                    }
                    detailSortJobType = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE;
                }
                else
                {
                }
            }
            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
        }
        else
        {
            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
            {           
                if(StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
                {
                    if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE))
                    {
                        eventLog.warn(String.format("DetailJobType Missmatch.[Carriername = %s, OriginalDetailJobType = %s, CurrentDetailJobType = %s]", 
                                carrierName, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT));  
                    }
                    detailSortJobType = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT;
                }
                else
                {
                }
            }
            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
        }
        /* <<== 20180929, hhlee, add, Sort Job TrackIn Validation */
        
        /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
        String sourceLotName = StringUtil.EMPTY;
        /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
        
        /* 20181211, hhlee, add, validate product of productElement ==>> */        
        String notValidateProductName = StringUtil.EMPTY;        
        /* <<== 20181211, hhlee, add, validate product of productElement */
        
        /* 20190321, hhlee, add All Glass Scrap Check validation ==>> */
        String allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_N;
        /* <<== 20190321, hhlee, add All Glass Scrap Check validation */
        
		if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT))
		{
		    if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
            {
                String lotName = "";
                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
                List<ProductP> productPSequence = new ArrayList<ProductP>();

                /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
                List<Map<String, Object>> sortJobProductList = null;
                int productCodunt = 0;
                /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
                
                for (Element product : productElement )
                {
                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
                    String position = SMessageUtil.getChildText(product, "POSITION", false);

                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                    {
                        Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);

                        if(StringUtil.isEmpty(lotName))
                        {
                            lotName = productData.getLotName();
                            
                            /* 20181207, hhlee, modify bug , change lot Product list ==>> */
                            /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
                            //sortJobProductList = ExtendedObjectProxy.getSortJobService().getSortJobProductByJobNameandFromLotName(sortJobName, lotName);
                            List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
                            productCodunt = productDataList != null ? productDataList.size() : 0;                           
                            /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
                            /* <<== 20181207, hhlee, modify bug , change lot Product list */
                        }

                        ProductPGSRC productPGSRC = new ProductPGSRC();
                        productPGSRC.setProductName(productName);
                        productPGSRC.setPosition(Long.valueOf(position));

                        String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
                        if (StringUtil.isEmpty(productGrade))
                        {
                            productPGSRC.setProductGrade(productData.getProductGrade());                            
                        }
                        else
                        {
                            productPGSRC.setProductGrade(productGrade);
                        }
                        productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
                        productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
                        productPGSRC.setReworkFlag("N");

                        //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
                        Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
                        productUdfs.put("PROCESSINGINFO", processingInfo);
                        productPGSRC.setUdfs(productUdfs);
                        productPGSRCSequence.add(productPGSRC);
                        
                        ProductP productPData = new ProductP();
                        productPData.setProductName(productData.getKey().getProductName());
                        /* 20180809, Modify, Mismatch Slot Position ==>> */
                        //productPData.setPosition(productData.getPosition());
                        productPData.setPosition(Long.valueOf(position));
                        /* <<== 20180809, Modify, Mismatch Slot Position */
                        productPSequence.add(productPData);
                        
                        /*Get Old ProductFlagData*/
                        ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                        /*Get Old ProductFlagData*/
                        
                        //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                        
                        /*Get New ProductFlagData*/
                        ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                        /*Get New ProductFlagData*/
                            			    
                        /* Get Note */ 
                        ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
                        /* Get Note */
                        
                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                    }
                }

                if(productPGSRCSequence.size() > 0)
                {
                    /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
                    ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
                    //sourceLotName = lotName;
                    ///* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */   
                    /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
                    
                	// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                	Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));

                    /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
                    if(productCodunt == productPGSRCSequence.size())
                    {
                        trackOutLot = this.splitGlassByDeassignCarreir(eventInfo, lotData, productPGSRCSequence, lotData.getCarrierName());                     
                    }
                    else
                    {
                        trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, carrierName);
                    }
                    /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
                    
                    /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
                    //FutureAction
                    //this.copyFutureActionCallSplit(lotData, trackOutLot, eventInfo);
                    MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallSplit(lotData, trackOutLot, eventInfo);
                    /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
                    
                    /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
                    //Lot Hold Copy(CT_LOTMULTIHOLD)
                    MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
                            GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
                    /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
                    
                    if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
                    {
                        eventInfo.setEventName("AssignCarrier");
                        eventInfo.setCheckTimekeyValidation(false);
                        /* 20181128, hhlee, EventTime Sync */
                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                        
                        AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
                        assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
                        assignCarrierInfo.setProductPSequence(productPSequence);
                        
                        Map<String, String> assignCarrierUdfs = durableData.getUdfs();
                        assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);

                        Map<String, String> lotUdfs = trackOutLot.getUdfs();
                        assignCarrierInfo.setUdfs(lotUdfs);
                        
                        // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
                        // trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
                        
                        /* 20181209, hhlee modify, add Inquiry lot data ==>> */
                        trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
                        /* <<== 20181209, hhlee modify, add Inquiry lot data */
                    }
                }
                else  /* <<== hhlee, add, 20180721, else Statemenet */
                {
                    try
                    {
                        CommonValidation.checkEmptyCst(carrierName); 
                    }
                    catch (CustomException ce)
                    {
                        List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                        trackOutLot = lotList.get(0);
                        //List<ProductU> productUSequence =  new ArrayList<ProductU>();                     
                        //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_END);
                        //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
                        //setEventInfo.setProductUSequence(productUSequence);
                        //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
                    }
                }
            }
            else  /* <<== hhlee, add, 20180721, else Statemenet */
            {
                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                trackOutLot = lotList.get(0);
                //List<ProductU> productUSequence =  new ArrayList<ProductU>();             
                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_END);
                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
                //setEventInfo.setProductUSequence(productUSequence);
                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
            }   
		}
		else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE))
		{
		    if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
            {
                List<Product> productList = new ArrayList<Product>();
                String productNameList = "";
                String lotName = "";
                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
                List<ProductP> productPSequence = new ArrayList<ProductP>();

                /* 20180930, hhlee, add All Glass Scrap Check ==>> */
                /* 20190321, hhlee, add All Glass Scrap Check validation ==>> */
                allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                /* <<== 20190321, hhlee, add All Glass Scrap Check validation */
                /* <<== 20180930, hhlee, add All Glass Scrap Check */
                
                for (Element product : productElement )
                {
                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
                    String position = SMessageUtil.getChildText(product, "POSITION", false);
                    String productJudge = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);

                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                    {
                        if(StringUtil.isEmpty(productNameList))
                            productNameList = "'" + productName + "'";
                        else    
                            productNameList += ",'"+ productName + "'" ;
                        
                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                    }

                    // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
                    Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
                    
                    productData.setPosition(Long.valueOf(position));
                    if(!StringUtil.isEmpty(productJudge))
                    {
                        productData.setProductGrade(productJudge);
                    }
                    Map<String,String> udfs = productData.getUdfs();
                    udfs.put("PROCESSINGINFO", processingInfo);
                    productData.setUdfs(udfs);
                    ProductServiceProxy.getProductService().update(productData);
                    
                    ProductP productPData = new ProductP();
                    productPData.setProductName(productData.getKey().getProductName());
                    /* 20180809, Modify, Mismatch Slot Position ==>> */
                    //productPData.setPosition(productData.getPosition());
                    productPData.setPosition(Long.valueOf(position));
                    /* <<== 20180809, Modify, Mismatch Slot Position */
                    productPSequence.add(productPData);
                    
                    /*Get Old ProductFlagData*/
                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get Old ProductFlagData*/
                    
                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    
                    /*Get New ProductFlagData*/
                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get New ProductFlagData*/
                        			    
                    /* Get Note */ 
                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
                    /* Get Note */
                    
                    /* 20180930, hhlee, add All Glass Scrap Check ==>> */
                    if(!StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S) && 
                            StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                    {
                        allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_N;
                    }
                    /* <<== 20180930, hhlee, add All Glass Scrap Check */
                }

                try
                {
                    productList.addAll(ProductServiceProxy.getProductService().select("WHERE PRODUCTNAME IN (" + productNameList + ")" + "ORDER BY LOTNAME" , null));
                }
                catch (Exception ex)
                {
                    productList = null;
                }

                Lot targetLot = null;
                List<Lot> lotList = null;
                Lot lotData = null;
                
                if(productList != null && productList.size() > 0)  /* <<== 20180813, Add, NullValue Error */
                {
                    lotName = productList.get(0).getLotName();
                    
                    /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
                    ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
                    //sourceLotName = lotName;
                    ///* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
                    /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
                    
                    // Added by smkang on 2018.12.20 - For synchronization of a carrier.
            		List<String> sourceCarrierNameList = new ArrayList<String>();
                    
                    //new add by wghuang 20180715
                    for(int i = 0; i < productList.size(); i++ )
                    {               
                        if(StringUtil.equals(lotName,  productList.get(i).getLotName()))
                        {
                        	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                        	lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
                            
                            // Added by smkang on 2018.12.20 - For synchronization of a carrier.
                			if (StringUtils.isNotEmpty(lotData.getCarrierName()) && !sourceCarrierNameList.contains(lotData.getCarrierName()))
                				sourceCarrierNameList.add(lotData.getCarrierName());
                            
                            if(targetLot == null)
                            {
                                try
                                {
                                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                                }
                                catch (Exception ex)
                                {
                                    lotList = null;
                                }
    
                                if(lotList.size() == 0 )
                                {
                                    eventInfo.setEventName("Create");
                                    targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
                                }
                                else
                                {
                                	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                                    targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
                                	targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotList.get(0).getKey().getLotName()));
                                }           
                            }
                            
                            ProductPGSRC productPGSRC = new ProductPGSRC();
                            productPGSRC.setProductName(productList.get(i).getKey().getProductName());
                            productPGSRC.setPosition(productList.get(i).getPosition());
                            productPGSRC.setProductGrade(productList.get(i).getProductGrade());
                            productPGSRC.setSubProductQuantity1(productList.get(i).getSubProductQuantity1());
                            productPGSRC.setSubProductQuantity2(productList.get(i).getSubProductQuantity2());
                            productPGSRC.setReworkFlag("N");
                            //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
                            productPGSRCSequence.add(productPGSRC);
                            
                            if(i == productList.size() -1)
                            {
                                trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
                                /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
                                //FutureAction
                                //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                                MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                                /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
                                
                                /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
                                //Lot Hold Copy(CT_LOTMULTIHOLD)
                                MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
                                        GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
                                /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
                            }
                        }
                        else
                        {
                            trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
                            lotName = productList.get(i).getLotName();
                            
                            /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
                            ///* 2018130, hhlee, add, Split/Merge Source Lot Variable ==>> */
                            //sourceLotName = sourceLotName + "," +lotName;
                            ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
                            /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
                            
                            /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
                            //FutureAction
                            //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                            MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                            /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
                            
                            /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
                            //Lot Hold Copy(CT_LOTMULTIHOLD)
                            MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
                                    GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
                            /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
                            
                            //new
                            productPGSRCSequence = new ArrayList<ProductPGSRC>();
                            
                            ProductPGSRC productPGSRC = new ProductPGSRC();
                            productPGSRC.setProductName(productList.get(i).getKey().getProductName());
                            productPGSRC.setPosition(productList.get(i).getPosition());
                            productPGSRC.setProductGrade(productList.get(i).getProductGrade());
                            productPGSRC.setSubProductQuantity1(productList.get(i).getSubProductQuantity1());
                            productPGSRC.setSubProductQuantity2(productList.get(i).getSubProductQuantity2());
                            productPGSRC.setReworkFlag("N");
                            //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
                            productPGSRCSequence.add(productPGSRC);
                            
                            /* 20190215, hhlee, add, logic bug ==>> */
                            /* 20180929, hhlee, add Get Lot Data ==>> */
                            // Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                            lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
                            /* <<== 20180929, hhlee, add Get Lot Data */
                            /* <<== 20190215, hhlee, add, logic bug */
                            
                            if(i == productList.size() - 1)
                            {
                                /* 20190215, hhlee, delete, logic bug ==>> */
                                ///* 20180929, hhlee, add Get Lot Data ==>> */
                                //lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                                ///* <<== 20180929, hhlee, add Get Lot Data */
                                /* <<== 20190215, hhlee, delete, logic bug */
                                
                                trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
                                
                                /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
                                //FutureAction
                                //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                                MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
                                /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
                                
                                /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
                                //Lot Hold Copy(CT_LOTMULTIHOLD)
                                MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
                                        GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
                                /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
                            }   
                        }
                    }
                    
    
                    if(productPGSRCSequence.size() > 0)
                    {   
                        if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
                        {
                            eventInfo.setEventName("AssignCarrier");
                            eventInfo.setCheckTimekeyValidation(false);
                            /* 20181128, hhlee, EventTime Sync */
                            //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                            
                            AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
                            assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
                            assignCarrierInfo.setProductPSequence(productPSequence);
                            
                            Map<String, String> assignCarrierUdfs = durableData.getUdfs();
                            assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
    
                            Map<String, String> lotUdfs = trackOutLot.getUdfs();
                            assignCarrierInfo.setUdfs(lotUdfs);
                            
                            // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
                            // trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
                            trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
                            
                            /* 20181209, hhlee modify, add Inquiry lot data ==>> */
                            trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
                            /* <<== 20181209, hhlee modify, add Inquiry lot data */
                        }
                    }
                    else  /* <<== hhlee, add, 20180721, else Statemenet */
                    {
                        try
                        {
                            CommonValidation.checkEmptyCst(carrierName); 
                        }
                        catch (CustomException ce)
                        {
                            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                            trackOutLot = lotList.get(0);  
                            //List<ProductU> productUSequence =  new ArrayList<ProductU>();                        
                            //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
                            //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
                            //setEventInfo.setProductUSequence(productUSequence);
                            //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
                        }
                    }
                    
     /*               // Added by smkang on 2018.12.20 - For synchronization of a carrier.
	        		for (String sourceCarrierName : sourceCarrierNameList) {
	        	        try {
	        	        	// After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
	        	        	Durable sourceCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceCarrierName);
	        	        	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, sourceCarrierData.getDurableState()))
	        	        	{
	        					Element bodyElement = new Element(SMessageUtil.Body_Tag);
	        					bodyElement.addContent(new Element("DURABLENAME").setText(sourceCarrierName));
	        					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
	        					
	        					// EventName will be recorded triggered EventName.
	        					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
	        					
	        					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, sourceCarrierName);
	        	        	}
	        	        } catch (Exception e) {
	        	        	eventLog.warn(e);
	        	        }
	        		}*/
                }
                else /* <<== 20180813, Add, NullValue Error */
                {
                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                    trackOutLot = lotList.get(0);   
                }
            }
            else  /* <<== hhlee, add, 20180721, else Statemenet */
            {
                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
                trackOutLot = lotList.get(0);               
                //List<ProductU> productUSequence =  new ArrayList<ProductU>();                
                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
                //setEventInfo.setProductUSequence(productUSequence);
                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);           
            }   
		}
		else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);

			/* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
            sourceLotName = lotList.get(0).getKey().getLotName();
            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
            
			List<ProductU> productUSequence = new ArrayList<ProductU>();

			for (Element product : productElement )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);

				/* 20180810 , Modify,  TurnOver Flag ==>> */
                String turnOverFlag = SMessageUtil.getChildText(product, "TURNOVERFLAG", false);
                /* <<== 20180810 , Modify,  TurnOver Flag */
                
                if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                {
                    /* 20180810 , Modify,  TurnOver Flag ==>> */
                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                  
                    ProductU productU = new ProductU();                 
                    Map<String, String> productUdfs = productData.getUdfs();
                    
                    productU.setUdfs(productUdfs);
                    /* <<== 20180810 , Modify,  TurnOver Flag */
                    
                    productU.setProductName(productName);
                    productUSequence.add(productU);
                    
                    /*Get Old ProductFlagData*/
                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get Old ProductFlagData*/
                    
                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    
                    /*Get New ProductFlagData*/
                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get New ProductFlagData*/
                        			    
                    /* Get Note */ 
                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
                    /* Get Note */
                    
                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                }
			}

			if(productUSequence.size() > 0)
			{
			    /* 20180928, hhlee, add, Lot Note ==>> */
                String turnOverProductList = StringUtil.EMPTY;
                for (ProductU productUData : productUSequence)
                {
                    if(StringUtil.isEmpty(turnOverProductList))
                    {
                        turnOverProductList = productUData.getProductName();
                    }
                    else
                    {
                        turnOverProductList = turnOverProductList + " , " +  productUData.getProductName() ;
                    }
                }
                
                turnOverProductList = "[TurnOver Product] - " + turnOverProductList;
                
                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
                lotUdfs.put("NOTE", turnOverProductList);                 
                lotList.get(0).setUdfs(lotUdfs);  
                LotServiceProxy.getLotService().update(lotList.get(0));
                /* <<== 20180928, hhlee, add, Lot Note */
                
			    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				/* 20180810 , Add,  ProductQuantity ==>> */
                setEventInfo.setProductQuantity(productUSequence.size());
                /* <<== 20180810 , Add,  ProductQuantity */
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  /* <<== hhlee, add, 20180721, else State */
			{			   
			    trackOutLot = lotList.get(0);
			}
		}
		else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);

			/* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
            sourceLotName = lotList.get(0).getKey().getLotName();
            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
            
			List<ProductU> productUSequence = new ArrayList<ProductU>();

			for (Element product : productElement )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
				
				/* 20180810 , Modify,  TurnOver Flag ==>> */
                String turnSideFlag = SMessageUtil.getChildText(product, "PROCESSTURNFLAG", false);
                /* <<== 20180810 , Modify,  TurnOver Flag */
                
                if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                {
                    /* 20180810 , Modify,  TurnOver Flag ==>> */
                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                  
                    ProductU productU = new ProductU();                 
                    Map<String, String> productUdfs = productData.getUdfs();
                           
                    productU.setUdfs(productUdfs);
                    /* <<== 20180810 , Modify,  TurnOver Flag */            
                
                    productU.setProductName(productName);
                    productUSequence.add(productU);
                    
                    /*Get Old ProductFlagData*/
                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get Old ProductFlagData*/
                    
                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
                    
                    /*Get New ProductFlagData*/
                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
                    /*Get New ProductFlagData*/
                        			    
                    /* Get Note */ 
                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
                    /* Get Note */
                    
                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                }
			}

			if(productUSequence.size() > 0)
			{
			    /* 20180928, hhlee, add, Lot Note ==>> */
                String turnSideProductList = StringUtil.EMPTY;
                for (ProductU productUData : productUSequence)
                {
                    if(StringUtil.isEmpty(turnSideProductList))
                    {
                        turnSideProductList = productUData.getProductName();
                    }
                    else
                    {
                        turnSideProductList = turnSideProductList + " , " +  productUData.getProductName() ;
                    }
                }
                
                turnSideProductList = "[TurnSide Product] - " + turnSideProductList;
                
                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
                lotUdfs.put("NOTE", turnSideProductList);                 
                lotList.get(0).setUdfs(lotUdfs); 
                LotServiceProxy.getLotService().update(lotList.get(0));
                /* <<== 20180928, hhlee, add, Lot Note */
                
			    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				/* 20180810 , Add,  ProductQuantity ==>> */
                setEventInfo.setProductQuantity(productUSequence.size());
                /* <<== 20180810 , Add,  ProductQuantity */
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  /* <<== hhlee, add, 20180721, else State */
            {              
			    trackOutLot = lotList.get(0);               
            }
		}
		else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);

			/* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
            sourceLotName = lotList.get(0).getKey().getLotName();
            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
            
			List<ProductU> productUSequence = new ArrayList<ProductU>();

			/* 20180928, hhlee, add, Lot Note ==>> */
            String SlotMapChangeProductList = StringUtil.EMPTY;
            /* <<== 20180928, hhlee, add, Lot Note */
            
			for (Element product : productElement )
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
				String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
				String position = SMessageUtil.getChildText(product, "POSITION", false);

				if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
				{
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
					Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));

					/* 20180928, hhlee, add, Lot Note ==>> */
                    if(StringUtil.isEmpty(SlotMapChangeProductList))
                    {
                        SlotMapChangeProductList = productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
                    }
                    else
                    {
                        SlotMapChangeProductList = SlotMapChangeProductList + " , " +  productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
                    }
                    /* <<== 20180928, hhlee, add, Lot Note */
                    
					productData.setPosition(Long.valueOf(position));
					ProductServiceProxy.getProductService().update(productData);

					ProductU productU = new ProductU();
					productU.setProductName(productName);
					productUSequence.add(productU);
					
					/*Get Old ProductFlagData*/
					ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
					/*Get Old ProductFlagData*/
					
					//ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
					
					/*Get New ProductFlagData*/
					ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
					/*Get New ProductFlagData*/
					    			    
					/* Get Note */ 
					ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
					/* Get Note */
					
					/* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
				}
			}

			if(productUSequence.size() > 0)
			{
			    /* 20180928, hhlee, add, Lot Note ==>> */
                SlotMapChangeProductList = "[SlotMapChange Product] - " + SlotMapChangeProductList;
                
                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
                lotUdfs.put("NOTE", SlotMapChangeProductList);                 
                lotList.get(0).setUdfs(lotUdfs); 
                LotServiceProxy.getLotService().update(lotList.get(0));
                /* <<== 20180928, hhlee, add, Lot Note */
                
				eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				/* 20180810 , Add,  ProductQuantity ==>> */
                setEventInfo.setProductQuantity(productUSequence.size());
                /* <<== 20180810 , Add,  ProductQuantity */
				setEventInfo.setProductUSequence(productUSequence);
				trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
			}
			else  /* <<== hhlee, add, 20180721, else State */
            {              
			    trackOutLot = lotList.get(0);               
            }
		}
		else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);

			/* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
            sourceLotName = lotList.get(0).getKey().getLotName();
            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
            
            /* 20181211, hhlee, add, validate product of productElement ==>> */            
            notValidateProductName = MESProductServiceProxy.getProductServiceUtil().validateProductElement(productElement);            
            /* <<== 20181211, hhlee, add, validate product of productElement */
            
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			
			List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotList.get(0).getKey().getLotName());
            String processingInfo = StringUtil.EMPTY;
            String productName = StringUtil.EMPTY;
            String elementProductName = StringUtil.EMPTY;
            
            /* 20181205, hhlee, add, Set productPGSRCSequence for Split ==>> */
            List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
            /* <<== 20181205, hhlee, add, Set productPGSRCSequence for Split */
            
            /* 20181211, hhlee, add, validate product of productElement ==>> */
            if(StringUtil.isEmpty(notValidateProductName))
            {
                for (Product productData : productDataList )
                {   
                    processingInfo = GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P;
                    productName = productData.getKey().getProductName();
                    elementProductName = StringUtil.EMPTY;
                    
                    for (Element product : productElement )
                    {
                        elementProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                        
                        if(StringUtil.equals(productName, elementProductName))
                        {
                            processingInfo = GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W;
                            break;
                        }    
                    }
                                    
                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
                    {
                        ProductU productU = new ProductU();
                        productU.setProductName(productName);
                        productUSequence.add(productU);     
                        
                        /* 20181205, hhlee, add, Set productPGSRCSequence for Split ==>> */
                        ProductPGSRC productPGSRC = new ProductPGSRC();
                        
                        productPGSRC.setProductName(productName);
                        productPGSRC.setPosition(productData.getPosition());
                        productPGSRC.setProductGrade(productData.getProductGrade());
                        productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
                        productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
                        productPGSRC.setReworkFlag("N");
    
                        //productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
                        Map<String, String> productUdfs = productData.getUdfs();
                        productUdfs.put("PROCESSINGINFO", processingInfo);
                        productPGSRC.setUdfs(productUdfs);
                        productPGSRCSequence.add(productPGSRC);
                        /* <<== 20181205, hhlee, add, Set productPGSRCSequence for Split */
                        
                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
                    }
                }
                /* <<== 20180811, Modify , The BC will not send Scraped Glass. So, look for the scraped glass in the MES. */
            }
            /* <<== 20181211, hhlee, add, validate product of productElement */
            
            String scrapFlag = StringUtil.EMPTY;
            String outStageFlag = StringUtil.EMPTY;
            
            /* 20180928, hhlee, add, Lot Note ==>> */
            String scrapProductList = StringUtil.EMPTY;
            /* <<== 20180928, hhlee, add, Lot Note */
            
			if(productUSequence.size() > 0)
			{
			    /* 20181121, hhlee, add, Record Scrap Product Lot Note ==>> */
                for(ProductU productU : productUSequence)
                {
                    /* 20180928, hhlee, add, Lot Note ==>> */
                    if(StringUtil.isEmpty(scrapProductList))
                    {
                        scrapProductList = productU.getProductName();
                    }
                    else
                    {
                        scrapProductList = scrapProductList + " , " +  productU.getProductName() ;
                    }
                    /* <<== 20180928, hhlee, add, Lot Note */
                }
                /* <<== 20181121, hhlee, add, Record Scrap Product Lot Note */      
                
			    /* 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check ==>> */
                try
                {
                    List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" WHERE JOBNAME = ? AND PRODUCTNAME = ? ", 
                            new Object[] {sortJobName, productUSequence.get(0).getProductName()});
                    scrapFlag = sortJobProductList.get(0).getScrapFlag();
                    outStageFlag = sortJobProductList.get(0).getOutStageFlag();             
                }
                catch (Exception ex)
                {
                    eventLog.warn(ex.getStackTrace());
                }               
                /* <<== 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check */
                
			    /* 20180813. Add, trackOutLot Null Value Error ==>> */
                trackOutLot = lotList.get(0);
                /* <<== 20180813. Add, trackOutLot Null Value Error */
                
                eventInfo.setEventName("DeassignCarrier");
				eventInfo.setCheckTimekeyValidation(false);
				/* 20181128, hhlee, EventTime Sync */
				//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
				
				
				/* 20181205, hhlee, add, add SourceLot Valiable ==>> */
                Lot sourceLotData = null;
                /* <<== 20181205, hhlee, add, add SourceLot Valiable */
                
				if(lotList.get(0).getProductQuantity() == productUSequence.size())
                {                   
                    DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
                    
                    // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
                    // trackOutLot = LotServiceProxy.getLotService().deassignCarrier(lotList.get(0).getKey(), eventInfo, deassignCarrierInfo);
                    trackOutLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotList.get(0), deassignCarrierInfo, eventInfo);
                    
                    /* 20181209, hhlee modify, add Inquiry lot data ==>> */
                    // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                    trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
                    trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(trackOutLot.getKey().getLotName()));
                    /* <<== 20181209, hhlee modify, add Inquiry lot data */
                }
                else
                {
                    /* 20181205, hhlee, add, Add  */
                    sourceLotData = trackOutLot;                                        
                    /* 20181226, hhlee, modify, modify eventName = (OutStageFlag = Y:OutStageSplit, ScrapFlag = Y: ScrapSplit) ==>> */
                    //trackOutLot = this.scrapSplitGlass(eventInfo, trackOutLot, productPGSRCSequence, StringUtil.EMPTY);
                    trackOutLot = this.scrapSplitGlass(eventInfo, trackOutLot, productPGSRCSequence, StringUtil.EMPTY, outStageFlag);
                    /* <<== 20181226, hhlee, modify, modify eventName = (OutStageFlag = Y:OutStageSplit, ScrapFlag = Y: ScrapSplit) */
                }
				
				eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
                if(StringUtil.equals(outStageFlag,GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_OUTSTAGE);
                }               
			    
                /* 20180928, hhlee, add, Lot Note ==>> */
                scrapProductList = "[Scrap Product] - " + scrapProductList;
                
                Map<String,String> lotUdfs = trackOutLot.getUdfs();   
                lotUdfs.put("NOTE", scrapProductList);                 
                trackOutLot.setUdfs(lotUdfs);  
                /* 20190125, hhlee, bug fix, change lotData : lotList.get(0) --> trackOutLot */
                LotServiceProxy.getLotService().update(trackOutLot);
                /* <<== 20180928, hhlee, add, Lot Note */
                
                eventInfo.setCheckTimekeyValidation(false);
                /* 20181128, hhlee, EventTime Sync */
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

                MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
                makeScrappedInfo.setProductQuantity(productUSequence.size());
                makeScrappedInfo.setProductUSequence(productUSequence);
                
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
    			//trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);
                trackOutLot = MESLotServiceProxy.getLotServiceImpl().makeScrapped(eventInfo,trackOutLot,makeScrappedInfo);
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
                
                
				
                /* 20181209, hhlee modify, add Inquiry lot data ==>> */
                trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
                /* <<== 20181209, hhlee modify, add Inquiry lot data */
                
                /* 20181205, hhlee, add, add SourceLot Valiable ==>> */
                if(sourceLotData != null)
                {
                    /* 20190114, hhlee, modify, Change Logic SourceLot TrackOut ==>> */
                    //trackOutLot = sourceLotData;
                    trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(sourceLotData.getKey().getLotName());
                    /* <<== 20190114, hhlee, modify, Change Logic SourceLot TrackOut */
                }
                /* <<== 20181205, hhlee, add, add SourceLot Valiable */                
			}
			else  /* <<== hhlee, add, 20180721, else State */
            {              
			    trackOutLot = lotList.get(0);             
            }
		}
		
		if (trackOutLot != null)
        {
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    		trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
    		trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(trackOutLot.getKey());
    		
    		if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped))
            {
    		    //20180811
                /* TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
            }
            else if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)) 
    		{
 
    		}
    		else
    		{
    		    /* 20190321, hhlee, add All Glass Scrap Check validation */
                if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                {
                    eventInfo.setEventName("TrackOut");
                    eventInfo.setEventComment(ExtendedObjectProxy.getSortJobService().setSortEventComment(carrierName, sortJobName, sortJobType,
                            sortTransferDirection, eventInfo.getEventName(), GenericServiceProxy.getConstantMap().Flag_N));
                    
                    MESProductServiceProxy.getProductServiceImpl().productStateUpdateByAllGlasssScrap(eventInfo, trackOutLot);  
                    trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateByAllGlasssScrap(eventInfo, trackOutLot);
                }
                else
                {
        			String[] nodeStackList = null;
        
        			/* 20190105, hhlee, modify, add logic ==>> */
                    //nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
                    String tempNodeStack =  StringUtil.reverse(StringUtil.substring(StringUtil.reverse(trackOutLot.getNodeStack()), StringUtil.indexOf(StringUtil.reverse(trackOutLot.getNodeStack()), ".") + 1));
                    nodeStackList = StringUtil.split(tempNodeStack, '.');
                    /* <<== 20190104, hhlee, modify, add logic */
        
        			if(nodeStackList.length > 0)
        			{
        				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setAllProductUdfs(trackOutLot.getKey().getLotName());
        
        				/* 20190328, hhlee, delete, TrackOut ProcessingInfo Record */
        				//productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
        
        				/* 20190105, hhlee, modify, add logic ==>> */
                        //Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);  
                        Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[nodeStackList.length - 1]);    
                        /* <<== 20190105, hhlee, modify, add logic */
        				
        				/* 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = "" ==>> */
                        String returnProcessOperationName = nodeData.getNodeAttribute1();
                        String returnProcessOperationVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
                        
                        /* 20190105, hhlee, modify, change logic ==>> */
                        Node nextNode = null;
                        try
                        {
                            //Node nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(nodeStackList[0], "Normal", "");
                            nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(nodeStackList[nodeStackList.length - 1], "Normal", "");
                        }
                        catch(NotFoundSignal ns)
                        {
                            eventLog.warn(String.format("NextNode data not found.[nodeID:%s, arcType:%s, arcAttribute:%s]", nodeStackList[nodeStackList.length - 1], "Normal", ""));
                        }
                        /* <<== 20190105, hhlee, modify, change logic */
                        
                        
                        /* 20190402, hhlee, add, Modify, change logic location ==>> */
                        String beforeProcessFlowName = trackOutLot.getProcessFlowName();
                        String beforeProcessOperationName = trackOutLot.getProcessOperationName();
                        /* 20190329, hhlee, add, endbank check validation */
                        String beforeProcessOperationNameByExtObj = CommonUtil.getValue(trackOutLot.getUdfs(), "BEFOREOPERATIONNAME");
                        /* <<== 20190402, hhlee, add, Modify, change logic location */
                        
                        /* 20190328, hhlee, add, lotData Update ==>> */
                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateBychangeProcessOperation(eventInfo, trackOutLot);
                        /* <<== 20190328, hhlee, add, lotData Update */
                        
                        /* 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  ==>> */
        				//ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
        				//		trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
        				//		"", "", trackOutLot.getProductRequestName(), trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
        				//		trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
        				//		trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
        				//		nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
        				//		"", "", "", "", "", trackOutLot.getUdfs(), productUdfs, false);
        				
                		//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
                        ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
                                trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
                                "", "", "", trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
                                trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
                                trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
                                nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), returnProcessOperationName, returnProcessOperationVersion,
                                "", "", "", "", "", trackOutLot.getUdfs(), productUdfs, false);
        				/* <<== 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  */
        				
                        /* 20190104, hhlee, add, change node stack ==>> */
                        /* 20190320, hhlee, add, change node stack ==>> */
                        changeSpecInfo.setNodeStack(tempNodeStack);
                        /* <<== 20190320, hhlee, add, change node stack */
                        /* <<== 20190104, hhlee, add, change node stack */
                        
                        String trackOutNote;
                        if(note.length()>3500){
                        	trackOutNote=note.substring(3499);
                        }else{
                        	trackOutNote= note.toString();
                        }
                        
                        // 2019.06.04_hsryu_Delete Logic. Not Memory TrackOut Note. Memory New Event. 
                        //changeSpecInfo.getUdfs().put("NOTE", trackOutNote);
                        
        				eventInfo.setEventName("TrackOut");
        				eventInfo.setEventComment(ExtendedObjectProxy.getSortJobService().setSortEventComment(carrierName, sortJobName, sortJobType, 
        				        sortTransferDirection, eventInfo.getEventName(), GenericServiceProxy.getConstantMap().Flag_N));
        				
        				trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
        				trackOutLot.getUdfs().put("NOTE", "");
        				LotServiceProxy.getLotService().update(trackOutLot);
        				
        				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//        				trackOutLot = LotServiceProxy.getLotService().selectByKey(trackOutLot.getKey());
        				trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(trackOutLot.getKey());
        				
        				/* 20190328, hhlee, add, lot and product state update ==>> */
                        // 20190328, hhlee, delete, function change(lotAndProductStateUpdateAfterTrackOut)  
                        //// 2910.03.01
                        //trackOutLot.setMachineName("");
                        //LotServiceProxy.getLotService().update(trackOutLot);
                        
                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotAndProductStateUpdateAfterTrackOut(eventInfo, trackOutLot);
                        /* <<== 20190328, hhlee, add, lot and product state update */
        				
        				///* Array 20180807, Add [Process Flag Update] ==>> */            
                        //MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, trackOutLot, logicalSlotMap);
                        ///* <<== Array 20180807, Add [Process Flag Update] */
                        
        				/* 20190105, hhlee, modify, change logic ==>> */
                        if(nextNode != null)
                        {
            				/* 20181120, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = "" ==>> */
                            /* 20190329, hhlee, add, endbank check validation */
                            if((StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End) && StringUtil.isEmpty(nextNode.getNodeAttribute1()) &&  
                                    StringUtil.isNotEmpty(CommonUtil.getValue(trackOutLot.getUdfs(), "ENDBANK"))) || 
                                    StringUtil.equals(beforeProcessOperationNameByExtObj, GenericServiceProxy.getConstantMap().PROCESSOPERATIONNAME_BAR))
                            {
                                /* 20190212, hhlee, add, lot state update(LotState, ProcessOperationName, ProcessOperationVersion, LotProcessState) ==>> */
                                trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateByLotStateCompleted(eventInfo, trackOutLot);
                                /* <<== 20190212, hhlee, add, lot state update(LotState, ProcessOperationName, ProcessOperationVersion, LotProcessState) */
                                
                                /* 20190212, hhlee, add, Product state update(ProductState, ProcessOperationName, ProcessOperationVersion, ProductProcessState) ==>> */
                                MESProductServiceProxy.getProductServiceImpl().productStateUpdateByLotStateCompleted(eventInfo, trackOutLot);  
                                /* <<== 20190212, hhlee, add, Product state update(ProductState, ProcessOperationName, ProcessOperationVersion, ProductProcessState) */
                            }
                            /* <<== 20181120, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  */
                        }
                        /* <<== 20190105, hhlee, modify, change logic */
                        
                        /* 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) ==>> */
                        if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
                        {                               
                        }
                        /* <<== 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) */
                                                
                        //2019.02.13_hsryu_Insert Logic. 
                        boolean aHoldFlag = false;
                        
                        /* 20181027, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot) ==>> */                
                        /* 20181127, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot), Hold EventName Change(sortJobType -> "Hold") ==>> */
                        //MESLotServiceProxy.getLotServiceImpl().sorterAfterHoldBySourceLot(eventInfo, trackOutLot, sortJobType, sourceLotName);
                        aHoldFlag = MESLotServiceProxy.getLotServiceImpl().sorterAfterHoldBySourceLot(eventInfo, trackOutLot, "Hold", sourceLotName);
                        /* <<== 20181127, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot), Hold EventName Change(sortJobType -> "Hold") */
                        /* <<== 20181027, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot) */                       
                        
                        /* 20180928, Add, OutHoldFlag -> HOLD ==>> */
                        if(StringUtil.equals(outHoldFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                        {
                        	aHoldFlag = true;
                            eventInfo.setEventName("SorterOutHold");
                            eventInfo.setEventComment("Changer has been Finished Please Check !");
                            /* 20190424, hhlee, modify, changed function ==>> */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH",""); 
                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, "", "");
                            try {
								GenericServiceProxy.getTxDataSourceManager().beginTransaction();
								MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, false, "", "");
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							} catch (Exception e) {
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
							}
                            /* <<== 20190424, hhlee, modify, changed function */
                        }                
                        /* <<== 20180928, Add, OutHoldFlag -> HOLD */  
                        
                        //List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(trackOutLot);
                        //MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
                        //LotServiceProxy.getLotService().makeOnHold(trackOutLot.getKey(), eventInfo, makeOnHoldInfo);
                        /* 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) ==>> */
                        if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
                        {
                        	aHoldFlag = true;
                            eventInfo.setEventName("Hold");
                            eventInfo.setEventComment("Sort Lot Process Abort Hold.");
                            /* 20190424, hhlee, modify, changed function ==>> */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL",""); 
                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
                            try {
                            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                            	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
                            	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							} catch (Exception e) {
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
							}
                            
                            /* <<== 20190424, hhlee, modify, changed function */
                        }
                        //}
                        
                        /* 20181211, hhlee, add, validate product of productElement ==>> */
                        if(StringUtil.isNotEmpty(notValidateProductName))
                        {
                        	aHoldFlag = true;
                            eventInfo.setEventName("SorterAbnormalEndHold");
                            eventInfo.setEventComment("Scrap has been Finished Please Check![Receive ProductName : " + notValidateProductName + "]");
                            /* 20190424, hhlee, modify, changed function ==>> */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot", "STOH", "", "MFG");  
                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, "", "MFG");
                            try {
                            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                            	// start modify by jhiying on20191205 mantis:5176
                            //	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, false, "", "MFG");
                            	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, false, "", ""); // end modify by jhiying on20191205 mantis:5176
                            	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							} catch (Exception e) {
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
							}
                            
                            /* <<== 20190424, hhlee, modify, changed function */
                        }
                        /* <<== 20181211, hhlee, add, validate product of productElement */
                        
                        /* 2019.02.13_hsryu_Insert TrackOut Priority Logic */
                        // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                        trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
                        trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(trackOutLot.getKey().getLotName()));
                        
    			    	if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
    			    	{
    			    		if(!aHoldFlag)
        			   		aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);
    
    			        	if(aHoldFlag)
    			        	{
    			        		trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionBySystemHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);
    			        		trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
    			        	}
    			        	else
    			        	{    							
    							// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
    							boolean isCheckSampling = false;
    							isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(), eventInfo);
    							
    							if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo) && !isCheckSampling ){
    								trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
    							}
    			        	}
    			    	}
    			    	
    			    	/* 20190508, hhlee, modify, MQC Job for Scrap ==>> */
                        if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
                        {
                            try
                            {
                                MESLotServiceProxy.getLotServiceUtil().checkMQCFlowAfter(trackOutLot, carrierName, eventInfo);
                            }
                            catch (Exception ex)
                            {
                                eventLog.error(ex.getMessage());
                            }
                        }
                        /* <<== 20190508, hhlee, modify, MQC Job for Scrap */
                        
        			    // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
                        trackOutLot = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(trackOutLot, note.toString(), eventInfo);

        			}
                }
    		}
    		
    		/* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
            try
            {
                SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sortJobName, carrierName});
                sortJobCarrier.settrackflag(GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT);
                ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
            }
            catch (Exception ex)
            {
                eventLog.error(ex.getMessage());
            }
            /* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
        }
		/* 20181205, hhlee, add, From all product of source cassette to target cassette */
        else
        {
            if(productElement.size() <= 0 )  
            {
                List<Product> productDataList = new ArrayList<Product>();
                
                try 
                {
                    productDataList = ProductServiceProxy.getProductService().select("CARRIERNAME = ?", new Object[] {carrierName});
                } 
                catch (Exception e) 
                {
                   eventLog.warn(e);
                }
                
                if(productElement.size() == 0 && productDataList.size() == 0) 
                {
                    try
                    {
                        SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sortJobName, carrierName});
                        sortJobCarrier.settrackflag(GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT);
                        ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
                    }
                    catch (Exception ex)
                    {
                        eventLog.error(ex.getMessage());
                    }         
                }
            }
        }
		
		/* 20190102, hhlee, add, Add Logic SorterJobAbort Validation ==>> */
        try
        {
            sortJobCarrierList = null;
            sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(" WHERE JOBNAME = ? AND TRACKFLAG <> ? ", 
                    new Object[] {sortJobName, GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT});            
        }
        catch (Exception ex)
        {
            eventLog.warn(String.format("This sorterjob[%s] is not aborted.!", sortJobName));
        } 
        
        try
        {
            if(sortJobCarrierList == null || sortJobCarrierList.size() <= 0)
            {
                SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {sortJobName});
                
                sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ABORT);
                sortJob.setEventComment(eventInfo.getEventComment());
                sortJob.setEventName(eventInfo.getEventName());
                sortJob.setEventTime(eventInfo.getEventTime());
                sortJob.setEventUser(eventInfo.getEventUser());
                
                ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
            }   
        }
         catch (Exception ex)
        {
            eventLog.warn(String.format("This sorterjob[%s] is update error.!", sortJobName));
        } 
        /* <<== 20190102, hhlee, add, Add Logic SorterJobAbort Validation */
	}

	/**
     * 
     * @Name     splitGlassByDeassignCarreir
     * @since    2018. 12. 5.
     * @author   hhlee
     * @contents From all product of source cassette to target cassette
     *           
     * @param eventInfo
     * @param lotData
     * @param productPGSRCSequence
     * @param carrierName
     * @return
     * @throws CustomException
     */
    private Lot splitGlassByDeassignCarreir(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
    {
        eventLog.info("Split(DeassignCarrier Lot for TK out");
        
        Lot targetLot = null;
        
        String splitProductList = StringUtil.EMPTY;
        
        List<ProductU> productUSequence = new ArrayList<ProductU>();
        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductU productU = new ProductU();
            productU.setProductName(productPGSRC.getProductName());
            productU.setUdfs(productPGSRC.getUdfs());
            productUSequence.add(productU);
            
            if(StringUtil.isEmpty(splitProductList))
            {
                splitProductList = productPGSRC.getProductName();
            }
            else
            {
                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        splitProductList = "[Change Product] - " + splitProductList;
        
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", splitProductList);                 
        lotData.setUdfs(lotUdfs);      
        LotServiceProxy.getLotService().update(lotData);
        
        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
        eventInfo.setCheckTimekeyValidation(false);
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

        //eventInfo.setEventName("AssignCarrier");
        //eventInfo.setCheckTimekeyValidation(false);
        //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
        
        DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
        deassignCarrierInfo.setProductUSequence(productUSequence);
        
        targetLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
        

        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());

        return targetLot;
    }
    
	private Lot mergeGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, Lot targetLot, String allGlasssScrapFlag) throws CustomException
	{
	    eventLog.info("Merge Lot for TK out");

        List<ProductP> productPSequence = new ArrayList<ProductP>();
        
        /* 20180928, hhlee, add, Lot Note ==>> */
        String mergeProductList = StringUtil.EMPTY;
        
        //for (ProductPGSRC productPGSRC : productPGSRCSequence)
        //{
        //  ProductP productP = new ProductP();
        //  productP.setProductName(productPGSRC.getProductName());
        //  productP.setPosition(productPGSRC.getPosition());
        //  productP.setUdfs(productPGSRC.getUdfs());
        //  productPSequence.add(productP);
        //}

        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(mergeProductList))
            {
                mergeProductList = productPGSRC.getProductName();
            }
            else
            {
                mergeProductList = mergeProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        mergeProductList = "[Change Product] - " + mergeProductList;
        
        //2019.04.23_hsryu_Insert Logic. Mantis 0002757.
        try{
    		String woName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot, productPSequence);
    		
    		if(!StringUtils.isEmpty(woName)) {
    			if(!StringUtils.equals(targetLot.getProductRequestName(), woName)){
    				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
    				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(targetLot.getKey().getLotName()));
    				
    				targetLot.setProductRequestName(woName);
    				LotServiceProxy.getLotService().update(targetLot);
    			}
    		}
        }
        catch(Throwable e){
        	eventLog.warn("Fail update WorkOrder.");
        }
        
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", mergeProductList);                 
        lotData.setUdfs(lotUdfs);  
        LotServiceProxy.getLotService().update(lotData);
        /* <<== 20180928, hhlee, add, Lot Note */
        
        /* 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) ==>> */
        if(lotData.getPriority() < targetLot.getPriority())
        {
            targetLot.setPriority(lotData.getPriority());                
            LotServiceProxy.getLotService().update(targetLot);
        }
        /* <<== 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) */
        
        /* 20180930, hhlee, add All Glass Scrap Check ==>> */
        TransferProductsToLotInfo  transitionInfo = null;
        
        /* 20190313, hhlee,  */
        if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
        {
            eventInfo.setBehaviorName("SPECIAL");
        }
        
        transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
                targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, targetLot.getUdfs(), new HashMap<String, String>());
        
        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
        eventInfo.setCheckTimekeyValidation(false);
        /* 20181128, hhlee, EventTime Sync */
        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
        /* <<== 20190313, hhlee, modify, Mixed ProductSpec */
        
        //2019.04.23_hsryu_Insert Logic. Check Mixed or Not Mixed WO. Mantis 0002757.
        try{
    		if(lotData != null) {
    			if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)){
    				
    				String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);

    				if(!StringUtils.isEmpty(sourceWOName)) {
    					if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)){
    						lotData.setProductRequestName(sourceWOName);
    						LotServiceProxy.getLotService().update(lotData);

    						String condition = "where lotname=?" + " and timekey= ? " ;
    						Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
    						List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
    						LotHistory lotHistory = arrayList.get(0);
    						lotHistory.setProductRequestName(sourceWOName);
    						LotServiceProxy.getLotHistoryService().update(lotHistory);
    					}
    				}
    			}
    		}
        }
        catch(Throwable e){
        	eventLog.warn("Fail update WorkOrder.");
        }
        
        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());

        return targetLot;
	}
	
	private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
	{
		eventLog.info("Split Lot for TK out");

		List<Lot> lotList = null;
		Lot targetLot = null;
		try
		{
			lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
		}
		catch (Exception ex)
		{
			lotList = null;
		}
		
		/* 20180928, hhlee, add, Lot Note ==>> */
		String splitProductList = StringUtil.EMPTY;
		
        if(lotList.size() == 0)
		{
			eventInfo.setEventName("Create");
			targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
		}
		else
		{
			targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(splitProductList))
            {
                splitProductList = productPGSRC.getProductName();
            }
            else
            {
                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        splitProductList = "[Change Product] - " + splitProductList;        
                                 
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", splitProductList);                 
        lotData.setUdfs(lotUdfs);   
        LotServiceProxy.getLotService().update(lotData);
        /* <<== 20180928, hhlee, add, Lot Note */

		TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
														targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());

		eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
		eventInfo.setCheckTimekeyValidation(false);
		/* 20181128, hhlee, EventTime Sync */
		//eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
		
		/**************************** Check Mixed WO Name *********************************/
		//2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
		try{
			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
			
			if(!StringUtils.isEmpty(desWOName)) {
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(targetLot.getKey().getLotName()));
				
				if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
					targetLot.setProductRequestName(desWOName);
					LotServiceProxy.getLotService().update(targetLot);
					
					String condition = "where lotname=?" + " and timekey= ? " ;
					Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					lotHistory.setProductRequestName(desWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
			
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
			
			if(!StringUtils.isEmpty(sourceWOName)) {
				if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
					lotData.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotService().update(lotData);
					
					String condition = "where lotname=?" + " and timekey= ? " ;
					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					lotHistory.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
		}
		catch(Throwable e){
			eventLog.warn("Fail update WorkOrder.");
		}
		/*********************************************************************************/

		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
		targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(targetLot.getKey().getLotName()));

		return targetLot;
	}
    
    /**
     * 
     * @Name     scrapSplitGlass
     * @since    2018. 12. 26.
     * @author   hhlee
     * @contents Split Glass For Scrap 
     *           
     * @param eventInfo
     * @param lotData
     * @param productPGSRCSequence
     * @param carrierName
     * @param outStageFlag
     * @return
     * @throws CustomException
     */
    private Lot scrapSplitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, String outStageFlag) throws CustomException
    {
        eventLog.info("Scrap Split Lot for TK out");

        List<Lot> lotList = null;
        Lot targetLot = null;
        try
        {
            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
        }
        catch (Exception ex)
        {
            lotList = null;
        }
        
        String splitProductList = StringUtil.EMPTY;
        
        if(lotList.size() == 0 )
        {
            eventInfo.setEventName("Create");
            targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
        }
        else
        {
        	// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
        	targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotList.get(0).getKey().getLotName()));
        }
        
        List<ProductP> productPSequence = new ArrayList<ProductP>();
        for (ProductPGSRC productPGSRC : productPGSRCSequence)
        {
            ProductP productP = new ProductP();
            productP.setProductName(productPGSRC.getProductName());
            productP.setPosition(productPGSRC.getPosition());
            productP.setUdfs(productPGSRC.getUdfs());
            productPSequence.add(productP);
            
            if(StringUtil.isEmpty(splitProductList))
            {
                splitProductList = productPGSRC.getProductName();
            }
            else
            {
                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
            }
        }
        
        splitProductList = "[Scrap Product] - " + splitProductList;
        
        Map<String,String> lotUdfs = lotData.getUdfs();   
        lotUdfs.put("NOTE", splitProductList);                 
        lotData.setUdfs(lotUdfs);      
        LotServiceProxy.getLotService().update(lotData);
        /* <<== 20180928, hhlee, add, Lot Note */

        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
   
        eventInfo.setEventName("ScrapSplit");
        if(StringUtil.equals(outStageFlag, GenericServiceProxy.getConstantMap().Flag_Y))
        {
            eventInfo.setEventName("OutStageSplit");
        }
        eventInfo.setCheckTimekeyValidation(false);
        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
        
        /**************************** Check Mixed WO Name *********************************/
		//2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
		try{
			String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
			
			if(!StringUtils.isEmpty(desWOName)) {
				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
				targetLot = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(targetLot.getKey().getLotName()));
				
				if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
					targetLot.setProductRequestName(desWOName);
					LotServiceProxy.getLotService().update(targetLot);
					
					String condition = "where lotname=?" + " and timekey= ? " ;
					Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					lotHistory.setProductRequestName(desWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
			
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
			
			if(!StringUtils.isEmpty(sourceWOName)) {
				if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
					lotData.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotService().update(lotData);
					
					String condition = "where lotname=?" + " and timekey= ? " ;
					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					lotHistory.setProductRequestName(sourceWOName);
					LotServiceProxy.getLotHistoryService().update(lotHistory);
				}
			}
		}
		catch(Throwable e){
			eventLog.warn("Fail update WorkOrder.");
		}
		/*********************************************************************************/

        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());

        return targetLot;
    }
}

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//20190612, backup
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//package kr.co.aim.messolution.lot.event;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
//import kr.co.aim.messolution.durable.MESDurableServiceProxy;
//import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
//import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
//import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
//import kr.co.aim.messolution.extended.object.management.data.SortJob;
//import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
//import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
//import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
//import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.CommonValidation;
//import kr.co.aim.messolution.generic.util.ConvertUtil;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.GradeDefUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.lot.MESLotServiceProxy;
//import kr.co.aim.messolution.machine.MESMachineServiceProxy;
//import kr.co.aim.messolution.port.MESPortServiceProxy;
//import kr.co.aim.messolution.product.MESProductServiceProxy;
//import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
//import kr.co.aim.greenframe.transaction.PropagationBehavior;
//import kr.co.aim.greenframe.util.time.TimeStampUtil;
//import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
//import kr.co.aim.greentrack.consumable.management.data.Consumable;
//import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
//import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
//import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
//import kr.co.aim.greentrack.durable.DurableServiceProxy;
//import kr.co.aim.greentrack.durable.management.data.Durable;
//import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
//import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
//import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.generic.util.TimeUtils;
//import kr.co.aim.greentrack.lot.LotServiceProxy;
//import kr.co.aim.greentrack.lot.management.data.Lot;
//import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
//import kr.co.aim.greentrack.lot.management.data.LotHistory;
//import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
//import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
//import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
//import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
//import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
//import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
//import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
//import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
//import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
//import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
//import kr.co.aim.greentrack.lot.management.sql.SqlStatement;
//import kr.co.aim.greentrack.machine.management.data.Machine;
//import kr.co.aim.greentrack.machine.management.data.MachineSpec;
//import kr.co.aim.greentrack.name.NameServiceProxy;
//import kr.co.aim.greentrack.port.PortServiceProxy;
//import kr.co.aim.greentrack.port.management.data.Port;
//import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
//import kr.co.aim.greentrack.processflow.management.data.Node;
//import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
//import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
//import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
//import kr.co.aim.greentrack.product.ProductServiceProxy;
//import kr.co.aim.greentrack.product.management.data.Product;
//import kr.co.aim.greentrack.product.management.data.ProductSpec;
//import kr.co.aim.greentrack.product.management.info.ext.ProductP;
//import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
//import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
//import kr.co.aim.greentrack.product.management.info.ext.ProductU;
//import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
//import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
//import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
//import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
//import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
//import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
//
//import org.apache.commons.lang.StringUtils;
//import org.jdom.Document;
//import org.jdom.Element;
//
//public class LotProcessAbort extends SyncHandler
//{
//    @Override
//    public Object doWorks(Document doc) throws CustomException
//    {
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_LotProcessAbortedReply");
//
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
//        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
//        String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
//        String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", false);
//        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
//        String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
//        String sLotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
//        String sLotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", false);
//        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
//        List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
//                
//        String lotJudge = "";
//        
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
//        
//        /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */ 
//        try
//        {
//            Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//    
//            if(durableData == null)
//            {
//                throw new CustomException("CST-0001", carrierName);
//            }
//    
//            MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
//            Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//            Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//    
//            /*SetMaterialLocationInfo MaterialLocationInfo=new SetMaterialLocationInfo();
//            EventInfo setMaterialLocationeventInfo = EventInfoUtil.makeEventInfo("setMaterialLocation", getEventUser(), getEventComment(), null, null);
//            for (Element product : productElement )
//            {
//                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
//                String materialChamber = productData.getUdfs().get("MATERIALCHAMBER");
//                if(!StringUtil.isEmpty(productData.getMaterialLocationName()) || !StringUtil.isEmpty(materialChamber))
//                {
//                    MaterialLocationInfo.setMaterialLocationName("");
//                    productData.getUdfs().put("MATERIALCHAMBER","");
//                    ProductServiceProxy.getProductService().update(productData);
//                    MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(setMaterialLocationeventInfo, productData, MaterialLocationInfo);
//                }
//            }*/
//            
//    
//            //job end in unpacker scenario
//            if(CommonUtil.isInitialInput(machineName))
//            {
//                this.UnpackerProcessAbort(doc);
//            }
//            else if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
//            {
//                this.sortProcessAbort(doc);
//            }
//    
//            //20170808 by zhanghao  EVA  PU LotProcessAbort TrackOutGlass
//            else if (CommonUtil.isInineType(machineName) && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                this.GlassProcessEnd(doc);
//            }
//            else if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PB") || 
//                        StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PO"))
//            {
//                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
//    
//                /* 20181122, hhlee, add, validate LotProcessState = 'RUN' ==>> */
//                try
//                {
//                    //if(StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_LoggedOut))
//                    if(!StringUtil.equals(lotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
//                    {
//                        throw new CustomException("LOT-9003", lotList.get(0).getKey().getLotName() +". Current State is " + lotList.get(0).getLotProcessState());
//                    }
//                    
//                    /* 20181220, hhlee, add, add Lot Hold Check Validation ==>> */
//                    CommonValidation.checkLotHoldState(lotList.get(0));
//                    /* <<== 20181220, hhlee, add, add Lot Hold Check Validation */
//                }
//                catch (CustomException ce)
//                {
//                    eventInfo.setEventName("Hold");
//                    eventInfo.setEventComment(ce.errorDef.getLoc_errorMessage());
//                    /* 20181128, hhlee, modify, add messageName */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND",""); // Abnormal End
//                    // 2019.04.19_hsryu_Delete Logic. HoldDepartment = Machine Department. Mantis 0003604.
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, "A_LotProcessAborted", machineSpecData.getUdfs().get("DEPARTMENT"));
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABND","", false, false, "A_LotProcessAborted", machineSpecData.getUdfs().get("DEPARTMENT"));
//                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//                    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ce.errorDef.getLoc_errorMessage());
//                    return doc;        
//                }
//                /* <<== 20181122, hhlee, add, validate LotProcessState = 'RUN' */
//    
//                String machineRecipeName = null;            
//                if( productElement != null)
//                {
//                    /* 20181011, hhlee, delete, change loacation ==>> */
//                    //for (Element product : productElement )
//                    //{
//                    //  String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                    //  Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
//                    //
//                    //  String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", true);
//                    //  productData.getUdfs().put("PROCESSINGINFO", processingInfo);
//                    //  ProductServiceProxy.getProductService().update(productData);
//                    //
//                    //  /* Array Product Flag Upsert ==> */
//                    //  //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    //  ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    //  /* <== Array Product Flag Upsert */
//                    //  
//                    //  if (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                    //    {
//                    //        /* 20180911, hhlee, Modify, ==>> */
//                    //        //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
//                    //        MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
//                    //        /* <<== 20180911, hhlee, Modify, */
//                    //    }
//                    //  /* 20181001, hhlee, add machine Recipe Save ==>> */
//                    //    if(StringUtil.isEmpty(machineRecipeName))
//                    //    {
//                    //        machineRecipeName = SMessageUtil.getChildText(product, "PRODUCTRECIPE", false);
//                    //    }
//                    //    /* <<== 20180920, hhlee, add machine Recipe Save */
//                    //}
//                    /* <<== 20181011, hhlee, delete, change loacation */
//                    
//                    //20180504, kyjung, QTime
//                    for (Element productEle : productElement )
//                    {
//                        String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
//                        Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
//    
//                        MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
//                    }
//    
//                    Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
//                    Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
//    
//    //              /* 20180802, add, Location change(UP) ==> */
//    //              Lot lotData = lotList.get(0);
//    //              /* <<== 20180802, add, Location change(UP) */
//    //              
//    //              /* 20180802, add, Location change(UP) ==> */
//    //              /* 20180723, Add , hhlee, Actural Sampling Position Update ==>> */
//    //              String actualSamplePosition = StringUtil.EMPTY;
//    //              //base flow info
//    //              ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
//    //              //waiting step
//    //              ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//    //              if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
//    //              {
//    //                  //slot map validation
//    //                  String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//    //                  actualSamplePosition = ExtendedObjectProxy.getSampleLotService().setActualSamplePosition(eventInfo, lotData, logicalSlotMap);
//    //              }
//    //              /* <<== 20180723, Add , hhlee, Actural Sampling Position Update */
//    //              /* <<== 20180802, add, Location change(UP) */
//                    
//                    /* 20180802, modify, add actualSamplePosition parameter of setProductPGSRCSequence function ==> */
//                    ////List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotList.get(0).getKey().getLotName(), productElement);
//    
//                    MESLotServiceProxy.getLotServiceUtil().checkTrackFlag(productElement, machineName, false, eventInfo);
//    
//                    /* 20190308, hhlee, delete ==>> */
//                    //List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOut(productElement);
//                    /* <<== 20190308, hhlee, delete */
//                    
//                    //List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productElement, machineName, actualSamplePosition);
//                    /* <<==  20180802, modify, add actualSamplePosition parameter of setProductPGSRCSequence function */
//                    
//                    /* 20181022, hhlee, delete, ==>> */
//                    //// Auto-judge lotGrade
//                    //lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotList.get(0), lotJudge, productPGSRCSequence);
//                    /* <<== 20181022, hhlee, delete, */         
//                    
//                    // Decide Sampling
//                    String decideSampleNodeStack = "";
//                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//                    // Modified by smkang on 2018.07.02 - According to EDO's request, although SuperLotFlag is Y, reserved sampling should be executed.
//                    //if ( lotList.get(0).getPriority() != 1 )
//                    //{
//                    //    // Decide Sampling
//                    //    decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, lotList.get(0));
//                    //}
//    
//                    //if ( lotList.get(0).getPriority() != 1 && StringUtil.isNotEmpty(MESLotServiceProxy.getLotServiceUtil().checkMoveToSample(lotList.get(0))))
//                    //{
//                    //    decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getSampleNodeStack(MESLotServiceProxy.getLotServiceUtil().checkMoveToSample(lotList.get(0)));
//                    //}
//                    
//                    Lot lotData = lotList.get(0);
//                    
//                    /* 20190308, hhlee, add ==>> */
//                    //waiting step
//                    ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                    /* <<== 20190308, hhlee, add */
//                    
//                    /* 20190308, hhlee, add ==>> */
//                    /* 20190320, hhlee, modify, DetatilOperationType = 'REP', ProductGrade 'P' -> 'G' Update */
//                    //List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOut(productElement, operationData.getProcessOperationType());
//                    List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOut(productElement, 
//                            operationData.getProcessOperationType(), operationData.getDetailProcessOperationType());
//                    /* <<== 20190308, hhlee, add */
//                    
//                    /* 20180723, Add , hhlee, Actural Sampling Position Update ==>> */
//                    String actualSamplePosition = StringUtil.EMPTY;
//                    //slot map validation
//                    String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//                    //base flow info
//                    ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
//                    
//                    /* 20190308, hhlee, delete ==>> */
//                    ////waiting step
//                    //ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//                    /* <<== 20190308, hhlee, delete */
//                    
//                    /* 20190226, hhlee, SPC Send Logic change ==>> */
//                    String upLoadID = CommonUtil.getValue(operationData.getUdfs(), "UPLOADID");
//                    /* <<== 20190226, hhlee, SPC Send Logic change */
//                    
//                    /* 20190312, hhlee, add Check Validation */
//                    //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
//                    /* 20190523, hhlee, modify, add check validation SamplingFlow */
//                    //if (operationData.getProcessOperationType().equals("Inspection") && 
//                    //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
//                    //            && !flowData.getProcessFlowType().equals("MQC"))
//                    if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
//                            flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
//                            !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
//                    {
//                        /* 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter ==>> */
//                        //slot map validation
//                        //logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//                        /* 20181220, hhlee, modify, modify getActualSample -> product(processflag) ==>> */
//                        //actualSamplePosition = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(eventInfo, lotData, logicalSlotMap, true);
//                        actualSamplePosition = MESProductServiceProxy.getProductServiceUtil().getActualSlotByProductProcessFlag(lotData.getKey().getLotName());
//                        /* <<== 20181220, hhlee, modify, modify getActualSample -> product(processflag) */
//                        /* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter */
//                    }
//                    /* <<== 20180723, Add , hhlee, Actural Sampling Position Update */
//    
//                    /* 20181107, Add , hhlee, Get Slot Selection Info ==>> */
//                    String productSlotSelection = StringUtil.EMPTY;
//                    productSlotSelection = MESProductServiceProxy.getProductServiceUtil().getProductSlotSelection(lotData, durableData, actualSamplePosition);            
//                    /* <<== 20181107, Add , hhlee, Get Slot Selection Info */              
//                    
//                    /*              
//                    String superLotFlag = lotData.getUdfs().get("SUPERLOTFLAG");
//                    
//                    if (StringUtils.isEmpty(superLotFlag) || !superLotFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
//                        MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, lotData);
//    
//                    String sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkReservedSamplingInfo(lotData.getKey().getLotName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), eventInfo);
//    
//                    if (StringUtils.isEmpty(sampleFlowName) && (StringUtils.isEmpty(superLotFlag) || superLotFlag.equals(GenericServiceProxy.getConstantMap().Flag_N)))
//                        sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkNormalSamplingInfo(lotData.getKey().getLotName(), lotData.getProcessOperationName(), eventInfo);
//    
//                    if (StringUtil.isNotEmpty(sampleFlowName))
//                        decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
//                    */              
//                    
//                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//                    //this.TrackInCancel(lotList.get(0), "", eventInfo);
//    
//                    /* 20180920, hhlee, add, Modify EventName, EventComment ==>> */
//                    
//                    /* <<== 20180920, hhlee, add, Modify EventName, EventComment */
//                    
//                    //Request by EDO, Hold Lot after TK OUT
//                    //Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotList.get(0), portData,
//                    //                                  carrierName, lotJudge, "", "",
//                    //                                  productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, true);
//    
//                    /* 2181108, hhlee, add, QTime is the same as LotProcessEnd ==>> */
//                    
//                    /* 20181128, hhlee, EventTime Sync */
//                    //20180504, kyjung, QTime
//                    //eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//                    eventInfo.setCheckTimekeyValidation(false);
//                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                    
//                    EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//                    List<Map<String, Object>> queuePolicyData = null;
//    
//                    Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(lotList.get(0));
//    
//                    if(qTimeTPEFOPolicyData != null)
//                    {
//                        queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
//    
//                        if(queuePolicyData != null)
//                        {
//                            for (Element product : productElement )
//                            {
//                                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
//    
//                                MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productName, queuePolicyData);
//                            }
//                        }
//                    }
//                    /* <<== 2181108, hhlee, add, QTime is the same as LotProcessEnd */
//                    
//                    List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
//
//                    // 20180504, kyjung, QTime
//                    for (Product productData : productDataList)
//                    {
//                        MESProductServiceProxy.getProductServiceImpl().closeQTime(eventInfo, productData, "TrackIn");
//                    }
//                    
//                    /* 20181011, hhlee, add , change location ==>> */
//                    String elaQtimeFlag = GenericServiceProxy.getConstantMap().Flag_N;  //ELAQTIMEFLAG
//                    String qtimeFlag = StringUtil.EMPTY;
//                    String elaQtimeOverProductName = StringUtil.EMPTY;
//                    
//                    /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
//                    String elaProductNgFlag = GenericServiceProxy.getConstantMap().ProductGrade_G;  //ELAQTIMEFLAG
//                    String ngProductList = StringUtil.EMPTY;
//                    /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
//                    
//                    /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                    String productListProcessingInfoW = StringUtil.EMPTY;
//                    String productListProcessingInfoF = StringUtil.EMPTY;
//                    /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
//                    
//                    // LotProcessAbort Note
//                    StringBuilder note = new StringBuilder("");
//                    for (Element product : productElement )
//                    {
//                        String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                        Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
//    
//                        String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", true);
//                        productData.getUdfs().put("PROCESSINGINFO", processingInfo);
//                        ProductServiceProxy.getProductService().update(productData);
//    
//                        /*Get Old ProductFlagData*/
//                        ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get Old ProductFlagData*/
//                        
//                        /* Array Product Flag Upsert ==> */
//                        //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        /* <== Array Product Flag Upsert */
//                        
//                        /*Get New ProductFlagData*/
//                        ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get New ProductFlagData*/
//                        
//                        /* Get Note */ 
//                        ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                        /* Get Note */
//                        
//                        /* Array Product Process Unit/SubUnit ==> */
//                        ExtendedObjectProxy.getProductInUnitOrSubUnitService().setProductProcessUnitOrSubUnit(eventInfo, machineData, product, carrierName);
//                        /* <== Array Product Process Unit/SubUnit */
//                        
//                        if (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                        {
//                            /* 20180911, hhlee, Modify, ==>> */
//                            //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
//                            MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
//                            /* <<== 20180911, hhlee, Modify, */
//                        }
//                        
//                        /* 20190226, hhlee, SPC Send Logic change ==>> */
//                        if(StringUtil.equals(operationData.getKey().getProcessOperationName(), upLoadID))
//                        {
//                            MESProductServiceProxy.getProductServiceImpl().setSpcProcessedOperationData(eventInfo, product, machineName);
//                        }               
//                        /* <<== 20190226, hhlee, SPC Send Logic change */
//                        
//                        /* 20181001, hhlee, add machine Recipe Save ==>> */
//                        if(StringUtil.isEmpty(machineRecipeName))
//                        {
//                            machineRecipeName = SMessageUtil.getChildText(product, "PRODUCTRECIPE", false);
//                        }
//                        /* <<== 20180920, hhlee, add machine Recipe Save */
//                        
//                        qtimeFlag =  SMessageUtil.getChildText(product, GenericServiceProxy.getConstantMap().PRODUCTFLAG_ELAQTIMEFLAG, false);
//                        if(StringUtil.equals(qtimeFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                        {
//                            /* 20181227, hhlee, delete, change logic(all product record) ==>> */
//                            //if(StringUtil.isEmpty(elaQtimeOverProductName))
//                            //{
//                            //    elaQtimeFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                            //    elaQtimeOverProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                            //}
//                            /* <<== 20181227, hhlee, delete, change logic(all product record) */
//                            
//                            /* 20181227, hhlee, add, change logic(all product record) ==>> */
//                            elaQtimeFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                            if(StringUtil.isEmpty(elaQtimeOverProductName))                    {
//                                
//                                elaQtimeOverProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                            }
//                            else
//                            {
//                                elaQtimeOverProductName += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);;
//                            }
//                            /* 20181227, hhlee, add, change logic(all product record) ==>> */
//                        }
//                        
//                        /* 20181227, hhlee, add, ProcessInfo == F, all product record ==>> */
//                        if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F))
//                        {
//                            /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                            if(StringUtil.isEmpty(productListProcessingInfoF))
//                            {
//                               productListProcessingInfoF = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                            }
//                            else
//                            {
//                                productListProcessingInfoF += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                            }
//                            /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
//                        }
//                        /* <<== 20181227, hhlee, add, ProcessInfo == F, all product record */
//                        
//                        /* 20181227, hhlee, add, add PRODUCT_PROCESSINGINFO_W ==>> */
//                        String position = SMessageUtil.getChildText(product, "POSITION", true);
//                        if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W))
//                        {
//                            if (StringUtil.equals(StringUtil.substring(productSlotSelection, Integer.valueOf(position) - 1, Integer.valueOf(position)),
//                                    GenericServiceProxy.getConstantMap().Flag_Y))
//                            {
//                                /* 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                                if(StringUtil.isEmpty(productListProcessingInfoW))
//                                {
//                                    productListProcessingInfoW = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                                }
//                                else
//                                {
//                                    productListProcessingInfoW += "," + SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                                }
//                                /* <<== 20181227, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
//                            }
//                        }
//                        /* <<== 20181227, hhlee, add, add PRODUCT_PROCESSINGINFO_W */   
//                        
//                        /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
//                        elaProductNgFlag =  SMessageUtil.getChildText(product,"PRODUCTJUDGE", false);
//                        if(StringUtil.equals(elaProductNgFlag, GenericServiceProxy.getConstantMap().ProductGrade_N))
//                        {                    
//                            if(StringUtil.isEmpty(ngProductList))
//                            {
//                                ngProductList = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
//                            }
//                            else
//                            {
//                                ngProductList = ngProductList + "," + SMessageUtil.getChildText(product, "PRODUCTNAME", true);
//                            }
//                        }
//                        /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
//                    }
//                    /* <<== 20181011, hhlee, add , change location */
//                    
//                    /* 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold ==>> */
//                    if(StringUtil.isNotEmpty(ngProductList))
//                    {
//                        String beforeEventName = eventInfo.getEventName();
//                        String beforeEventComment = eventInfo.getEventComment();
//                        eventInfo.setEventComment(String.format("NG Product Hold.!! [MachineName : %s, NG Product : %s ].!!", machineName, ngProductList)); 
//                        /* 20190320, hhlee, Modify, added Hold Department */
//                        //MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotList.get(0), machineName, "HoldLot", "GNHL");
//                        MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotData, machineName, "HoldLot", "GNHL", CommonUtil.getValue(machineSpecData.getUdfs(), "DEPARTMENT"));
//                        eventInfo.setEventName(beforeEventName);
//                        eventInfo.setEventComment(beforeEventComment);
//                    }
//                    /* <<== 20181022, hhlee, add, if ProductJudge is 'N', Lot is hold */
//                    
//                    /**2018.12.20_hsryu_Modify TrackOutLot Logic. same LotProcessEnd!**/
//    //              Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotList.get(0), portData,
//    //                        carrierName, lotJudge, machineName, machineRecipeName, "",
//    //                        productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, true);
//                    
//                    /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
//                    String beforeProcessFlowName = lotList.get(0).getProcessFlowName();
//                    String beforeProcessOperationName = lotList.get(0).getProcessOperationName();
//                    /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
//                    
//                    Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotList.get(0), portData,
//                            carrierName, lotJudge, machineName, "",
//                            productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, true,note.toString());
//                                    
//                    /* Array 20180807, Add [Process Flag Update] ==>> */            
//                    MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterTrackOutLot, logicalSlotMap, true);
//                    /* <<== Array 20180807, Add [Process Flag Update] */
//                                    
//                    /* Array 20180619, Add [If ELA Q-time Flag is a Y glass Hold the lot after TrackOut.] ==> */
//                    if(StringUtil.equals(elaQtimeFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    {
//                        eventInfo.setEventName("Hold");
//                        eventInfo.setEventComment(String.format("This Lot[%s](ProductName=%s) is ELAQ-time Over.", afterTrackOutLot.getKey().getLotName(), elaQtimeOverProductName));
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, afterTrackOutLot.getCarrierName(),null, "EL","");
//                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ELAH","", false, "A_LotProcessAbort", " ");
//                        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ELAH","", false, false, "A_LotProcessAbort", "");
//                    }
//                    /* <<== Array 20180619, Add [If ELA Q-time Flag is a Y glass Hold the lot after TrackOut.] */
//                    
//                    /* 20181225, hhlee, delete, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                    ///* 20181106, hhlee, add, if(ProcessInfo == F Or W) LotHold[request by Guishi] ==>> */
//                    ///* 20181001, hhlee, add, if(ProcessInfo == F) LotHold ==>> */
//                    //for( ProductPGSRC productPgsrcData : productPGSRCSequence)
//                    //{
//                    //    if(StringUtil.equals(productPgsrcData.getUdfs().get("PROCESSINGINFO"), GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F))
//                    //    {
//                    //        eventInfo.setEventName("Hold");
//                    //        eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", afterTrackOutLot.getKey().getLotName(), productPgsrcData.getProductName(), productPgsrcData.getUdfs().get("PROCESSINGINFO")));
//                    //        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","");
//                    //        break;
//                    //    }
//                    //    
//                    //    if(StringUtil.equals(productPgsrcData.getUdfs().get("PROCESSINGINFO"), GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W))
//                    //    {
//                    //      if (StringUtil.equals(StringUtil.substring(productSlotSelection, (int)(productPgsrcData.getPosition() - 1), (int)productPgsrcData.getPosition()),
//                    //              GenericServiceProxy.getConstantMap().Flag_Y))
//                    //      {
//                    //          /* 20181225, hhlee, add, PROCESSINGINFO = 'W' all product record ==>> */
//                    //          String productListProcessingInfoW = StringUtil.EMPTY;
//                    //          
//                    //          for(ProductPGSRC productW : productPGSRCSequence)
//                    //            {
//                    //                if(StringUtil.equals(productW.getUdfs().get("PROCESSINGINFO"), GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W))
//                    //                {
//                    //                    if (StringUtil.equals(StringUtil.substring(productSlotSelection, (int)(productW.getPosition() - 1), (int)productW.getPosition()),
//                    //                            GenericServiceProxy.getConstantMap().Flag_Y))
//                    //                    {
//                    //                        if(StringUtil.isEmpty(productListProcessingInfoW))
//                    //                        {
//                    //                            productListProcessingInfoW = productW.getProductName();
//                    //                        }
//                    //                        else
//                    //                        {
//                    //                            productListProcessingInfoW += "," + productW.getProductName();
//                    //                        }
//                    //                    }
//                    //                }
//                    //            }
//                    //          /* <<== 20181225, hhlee, add, PROCESSINGINFO = 'W' all product record */
//                    //          
//                    //            eventInfo.setEventName("Hold");
//                    //            //modified by wghuang 20181221
//                    //            //eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", afterTrackOutLot.getKey().getLotName(), productPgsrcData.getProductName(), productPgsrcData.getUdfs().get("PROCESSINGINFO")));
//                    //            eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", afterTrackOutLot.getKey().getLotName(), productListProcessingInfoW, productPgsrcData.getUdfs().get("PROCESSINGINFO")));
//                    //            
//                    //            // Modified by smkang on 2018.12.03 - According to Feng Huanyan's request, a department of the machine is recorded as a department of PWHL hold.
//                    //            // MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PWHL","");
//                    //            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "PWHL", "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                    //
//                    //            break;
//                    //        }
//                    //    }
//                    //}
//                    ///* <<== 20181001, hhlee, add, if(ProcessInfo == F) LotHold */
//                    ///* <<== 20181106, hhlee, add, if(ProcessInfo == F Or W) LotHold[request by Guishi] */
//                    /* <<== 20181225, hhlee, delete, change logic if(ProcessInfo == F Or W), all product record */
//                    
//                    /* 20181225, hhlee, add, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                    /* 20181227, hhlee, delete, change logic if(ProcessInfo == F Or W), all product record ==>> */
//                    //String productListProcessingInfoW = StringUtil.EMPTY;
//                    //String productListProcessingInfoF = StringUtil.EMPTY;
//                    //for( ProductPGSRC productPgsrcData : productPGSRCSequence)
//                    //{
//                    //    if(StringUtil.equals(productPgsrcData.getUdfs().get("PROCESSINGINFO"), GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F))
//                    //    {
//                    //        if(StringUtil.isEmpty(productListProcessingInfoF))
//                    //        {
//                    //           productListProcessingInfoF = productPgsrcData.getProductName();
//                    //        }
//                    //        else
//                    //        {
//                    //            productListProcessingInfoF += "," + productPgsrcData.getProductName();
//                    //        }
//                    //    }
//                    //    
//                    //    if(StringUtil.equals(productPgsrcData.getUdfs().get("PROCESSINGINFO"), GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W))
//                    //    {
//                    //        if (StringUtil.equals(StringUtil.substring(productSlotSelection, (int)(productPgsrcData.getPosition() - 1), (int)productPgsrcData.getPosition()),
//                    //                GenericServiceProxy.getConstantMap().Flag_Y))
//                    //        {
//                    //            if(StringUtil.isEmpty(productListProcessingInfoW))
//                    //            {
//                    //                productListProcessingInfoW = productPgsrcData.getProductName();
//                    //            }
//                    //            else
//                    //            {
//                    //                productListProcessingInfoW += "," + productPgsrcData.getProductName();
//                    //            }
//                    //        }
//                    //    }
//                    //}
//                    /* <<== 20181227, hhlee, delete, change logic if(ProcessInfo == F Or W), all product record */
//                    
//                    if(StringUtil.isNotEmpty(productListProcessingInfoF))
//                    {
//                        eventInfo.setEventName("Hold");
//                        eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", 
//                                afterTrackOutLot.getKey().getLotName(), productListProcessingInfoF, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_F));
//                        /* 20190424, hhlee, modify, changed function ==>> */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","",machineSpecData.getUdfs().get("DEPARTMENT"));
//                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","", true, "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PFHL","", true, false, "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                        /* <<== 20190424, hhlee, modify, changed function */
//                    }
//                    
//                    if(StringUtil.isNotEmpty(productListProcessingInfoW))
//                    {
//                        eventInfo.setEventName("Hold");
//                        eventInfo.setEventComment(String.format("LotName[%s](ProductName=%s) PROCESSINGINFO=%s.", 
//                                afterTrackOutLot.getKey().getLotName(), productListProcessingInfoW, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W));
//                        /* 20190424, hhlee, modify, changed function ==>> */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "PWHL", "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PWHL","", true, "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","PWHL","", true, false, "", machineSpecData.getUdfs().get("DEPARTMENT"));
//                        /* <<== 20190424, hhlee, modify, changed function */
//                    }
//                    /* <<== 20181225, hhlee, add, change logic if(ProcessInfo == F Or W), all product record */
//                    
//                    /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
//                    //20180604, kyjung, MQC
//                    ProcessFlowKey processFlowKey = new ProcessFlowKey();
//                    processFlowKey.setFactoryName(afterTrackOutLot.getFactoryName());
//                    processFlowKey.setProcessFlowName(afterTrackOutLot.getProcessFlowName());
//                    processFlowKey.setProcessFlowVersion(afterTrackOutLot.getProcessFlowVersion());
//                    ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
//                    /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
//                    
//                    // 2019.05.31_hsryu_Move To Logic. located TrackOutLot Function.
////                  // if LotProcessState == Completed
////                  // LotProcessOperationName -> NULL
////                  if( StringUtil.equals(afterTrackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
////                  {
////                      afterTrackOutLot.setProcessOperationName("-");
////                      afterTrackOutLot.setProcessOperationVersion("");
////                      LotServiceProxy.getLotService().update(afterTrackOutLot);
////                      
////                      /* 20190129, hhlee, modify, change history update logic */
////                      //String condition = "where lotname=?" + " and timekey= ? " ;
////                      //Object[] bindSet = new Object[]{lotData.getKey().getLotName(),eventInfo.getEventTimeKey()};
//////                        String condition = "where lotname = ? and timekey = (select max(timekey) from lothistory where lotname = ?)" ;
//////                        Object[] bindSet = new Object[]{afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getKey().getLotName()};
//////                        List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//////                        LotHistory lotHistory = arrayList.get(0);
////                      LotHistoryKey LotHistoryKey = new LotHistoryKey();
////                      LotHistoryKey.setLotName(afterTrackOutLot.getKey().getLotName());
////                      /* 20190426, hhlee, modify, change TimeKey(eventInfo.getEventTimeKey() -> afterTrackOutLot.getLastEventTimeKey() */
////                        //LotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
////                        LotHistoryKey.setTimeKey(afterTrackOutLot.getLastEventTimeKey());
////                      LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
////                      lotHistory.setProcessOperationName("-");
////                      lotHistory.setProcessOperationVersion("");
////                      LotServiceProxy.getLotHistoryService().update(lotHistory);
////                  }
//                    
//                    /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
//                    if(processFlowData != null)
//                    {
//                        if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
//                        {
//                            eventInfo.setEventName("UpdateMQCCount");
//                            eventInfo.setCheckTimekeyValidation(false);
//                            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                            MESProductServiceProxy.getProductServiceImpl().updateMQCCountToProduct(afterTrackOutLot, eventInfo, processFlowData, beforeProcessOperationName);
//                            
//                            if(StringUtil.equals(afterTrackOutLot.getLotState(), "Completed"))
//                            {
//                                eventInfo.setEventName("FinishMQCJob");
//                                eventInfo.setCheckTimekeyValidation(false);
//                                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                                MESProductServiceProxy.getProductServiceImpl().checkFinishMQCJob(afterTrackOutLot, eventInfo, processFlowData);
//                                
//                                /* 20190126, hhlee, add, MQC ProductSpec Change ==>> */
//                                MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLotMQC(afterTrackOutLot, eventInfo);                     
//                                /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
//                            }
//                        }
//                        else /* 20190129, hhlee, add, add lot complete */
//                        {
//                            MESLotServiceProxy.getLotServiceImpl().chkAfterCompleteLot(afterTrackOutLot, eventInfo); 
//                        }
//                    }
//                    /* <<== 20190126, hhlee, add, MQC ProductSpec Change */
//                    
//                    /* 20190105, hhlee, add, add Inhibit toMachine ==>> */         
//                    String toMachineName = StringUtil.EMPTY;
//                    if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"),
//                            GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
//                    {
//                        toMachineName = CommonUtil.getValue(portData.getUdfs(), "LINKEDUNITNAME");
//                    }
//                    /* <<== 20190105, hhlee, add, add Inhibit toMachine */
//                    
//                    /* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                    //Added by jjyoo on 2018.9.20 - Check Inhibit Condition
//                    //this logic must be existed the end of LotInfoDownloadRequestNew.
//                    /* 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName ==>> */
//                    //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName, GenericServiceProxy.getConstantMap().Flag_Y);
//                    /* 20190422, hhlee, modify, check ExceptionLot in TrackOut(try ~~ catch ~~, After TrackOut, LotHold) ==>> */
//                    try
//                    {
//                        MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  lotList.get(0),  machineName,  machineRecipeName, 
//                                GenericServiceProxy.getConstantMap().Flag_Y, toMachineName);
//                    }
//                    catch (Exception ex) 
//                    {
//                        eventInfo.setEventName("Hold");
//                        eventInfo.setEventComment(MESLotServiceProxy.getLotServiceImpl().getReturnErrorMessage(ex));
//                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, 
//                        //        "HoldLot", "ABND", StringUtil.EMPTY, false, "A_LotProcessAborted", StringUtil.EMPTY);
//                        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, 
//                                "HoldLot", "ABND", StringUtil.EMPTY, false, false, "A_LotProcessAborted", StringUtil.EMPTY);
//                    }
//                    /* <<== 20190422, hhlee, modify, check ExceptionLot in TrackOut(try ~~ catch ~~, After TrackOut, LotHold) */
//                    /* <<== 20190101, hhlee, modify, add parameter MachineOpereatonMode, UnitName */
//                    /* 20181001, hhlee, modify, location move, lotdata change ==>> */
//                    
//                    
//                    eventInfo.setEventName("Hold");
//                    eventInfo.setEventComment("Lot Process Abort Hold");
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, afterTrackOutLot.getCarrierName(),null, "LA","");
//                    /* 20190424, hhlee, modify, changed function ==>> */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
//                    /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                    //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
//                    MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
//                    /* <<== 20190424, hhlee, modify, changed function */
//
//                    // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
//                    afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(afterTrackOutLot, note.toString(), eventInfo);
//
//                    //eventInfo.setEventName("LotProcessAbort");
//                    //eventInfo.setEventComment("LotProcessAbort");
//                    //eventInfo.setCheckTimekeyValidation(false);
//                    //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                    //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());                
//                    //List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(afterTrackOutLot);
//                    //MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//                    //LotServiceProxy.getLotService().makeOnHold(afterTrackOutLot.getKey(), eventInfo, makeOnHoldInfo);
//
//                    // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
////                  //Port clear - YJYU
////                  List<Lot> lotList_Port = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
////                  Lot lotData_Port = lotList_Port.get(0);
////                  Map<String, String> udfs_note = lotData_Port.getUdfs();
////                  udfs_note.put("PORTNAME", "");
////                  LotServiceProxy.getLotService().update(lotData_Port);
//                    List<Lot> portLotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
//                    Map<String, String> updateUdfs = new HashMap<String, String>();
//                    updateUdfs.put("PORTNAME", "");
//                    
//                    for (Lot portLotData : portLotDataList) {
//                        MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(portLotData, updateUdfs);
//                    }
//                }
//            }
//            else
//            {
//                String lotName = MESLotServiceProxy.getLotServiceUtil().getLotNamefromProductElements(productElement);
//                
//                // LotProcessAbort Note
//                StringBuilder note = new StringBuilder("");
//                
//                List<Element> newProductList = new ArrayList<Element>();
//    
//                if( productElement != null)
//                {
//                    for (Element product : productElement )
//                    {
//                        String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                        String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//    
//                        if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                        {
//                            newProductList.add(product);
//                        }
//                        /*Get Old ProductFlagData*/
//                        ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get Old ProductFlagData*/
//    
//                        /* Array 20180529, Add ==> */
//                        /* Array Product Flag Upsert ==> */
//                        //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        
//                        /*Get New ProductFlagData*/
//                        ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get New ProductFlagData*/
//                        
//                        /* Get Note */ 
//                        ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                        /* Get Note */
//                        
//                        /* <== Array Product Flag Upsert */
//                        /* Array Product Process Unit/SubUnit ==> */
//                        ExtendedObjectProxy.getProductInUnitOrSubUnitService().setProductProcessUnitOrSubUnit(eventInfo, machineData, product, carrierName);
//                        /* <== Array Product Process Unit/SubUnit */
//                        /* <<== Array 20180529, Add */
//    
//                        if (StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                        {
//                            /* 20180911, hhlee, Modify, ==>> */
//                            //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(product, machineName);
//                            MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, product, machineName);
//                            /* <<== 20180911, hhlee, Modify, */
//                        }
//                    }
//    
//                    Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//    
//                    /* 20181128, hhlee, EventTime Sync */
//                    eventInfo.setCheckTimekeyValidation(false);
//                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                    //eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
//                    eventInfo.setEventName("Split");
//                    
//                    Lot cancelTrackInLot = null;
//    
//                    Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
//                    Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
//                    
//                    List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotData.getKey().getLotName(), newProductList);
//    
//                    if (lotData.getProductQuantity() > productPGSRCSequence.size() && productPGSRCSequence.size() > 0)
//                    {
//                        cancelTrackInLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence);
//    
//                        /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
//                        //FutureAction
//                        MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallSplit(lotData, cancelTrackInLot, eventInfo);
//                        /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
//    
//                        /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
//                        //Lot Hold Copy(CT_LOTMULTIHOLD)
//                        MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, cancelTrackInLot, eventInfo, 
//                                GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
//                        /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
//                        
//                        //20180504, kyjung, QTime
//                        for (Element productEle : newProductList )
//                        {
//                            String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
//                            Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
//    
//                            MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
//                        }
//    
//                        /* 20181128, hhlee, EventTime Sync */
//                        eventInfo.setCheckTimekeyValidation(false);
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        //eventInfo = EventInfoUtil.makeEventInfo("TrackInCancel", getEventUser(), getEventComment(), "", "");
//                        eventInfo.setEventName("TrackInCancel");
//                                            
//                        /*
//                        String recipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProductSpecName(),
//                                cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessOperationName(), machineName, false, cancelTrackInLot.getUdfs().get("ECCODE"));
//                        
//                        //20180612, kyjung, Recipe Idle Time
//                        MESProductServiceProxy.getProductServiceImpl().cancelTIRecipeIdleTimeLot(machineName, recipeName, cancelTrackInLot.getProductSpecName(), cancelTrackInLot.getProcessOperationName(), eventInfo);
//                        */
//                        
//                        this.TrackInCancel(lotData, durableData.getKey().getDurableName(), eventInfo,note.toString());
//                        
//                        eventInfo.setEventName("Hold");
//                        //holdLot Requested by EDO 20180504
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LA","");
//                        eventInfo.setEventComment("Lot Process Abort Hold");
//                        
//                        /* 20190424, hhlee, modify, changed function ==>> */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
//                        /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                        //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", false, "", " ");
//                        MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", false, false, "", "");
//                        /* <<== 20190424, hhlee, modify, changed function */
//                    
//                        // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
//                        lotData = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(lotData, note.toString(), eventInfo);
//
//                    }
//                    
//                    // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
////                  //Port clear - YJYU
////                  List<Lot> lotList_Port = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
////                  Lot lotData_Port = lotList_Port.get(0);
////                  Map<String, String> udfs_note = lotData_Port.getUdfs();
////                  udfs_note.put("PORTNAME", "");
////                  LotServiceProxy.getLotService().update(lotData_Port);
//                    List<Lot> portLotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
//                    Map<String, String> updateUdfs = new HashMap<String, String>();
//                    updateUdfs.put("PORTNAME", "");
//                    
//                    for (Lot portLotData : portLotDataList) {
//                        MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(portLotData, updateUdfs);
//                    }
//                }
//            }
//    
//            try
//            {
//                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
//            }
//            catch(Exception ex)
//            {
//                eventLog.warn("FMC Report Failed!");
//            }
//                    
//            //ProductKey productkey = new ProductKey();
//    
//            //20170915 by yudan *Check productSpecName,ProcessOperation,WO*
//            /*String productSpecName = "";
//            String ProcessOp = "";
//            String workOrderName = "";
//            for (Element product : productList )
//            {
//                productkey.setProductName(SMessageUtil.getChildText(product, "PRODUCTNAME", true));
//                Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);
//                Lot lotDate = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
//    
//                if(StringUtil.isEmpty(productSpecName) && StringUtil.isEmpty(ProcessOp) && StringUtil.isEmpty(workOrderName))
//                {
//                    productSpecName = lotDate.getProductSpecName();
//                    ProcessOp = lotDate.getProcessOperationName();
//                    workOrderName = lotDate.getProductRequestName();
//                }
//    
//                if(!ExtendedObjectProxy.getFirstGlassLotService().CheckPilotLot(lotDate.getKey().getLotName()))
//                {
//                    if(!(productSpecName.equals(lotDate.getProductSpecName())
//                            && ProcessOp.equals(lotDate.getProcessOperationName())
//                            && workOrderName.equals(lotDate.getProductRequestName())))
//                    {
//                        eventLog.error("productSpecName,ProcessOperation,WO must be the same");
//                        return;
//                    }
//                }
//            }*/
//    
//            //20160208 by zhanghao  set MaterialLocationInfo is null
//            /*SetMaterialLocationInfo MaterialLocationInfo=new SetMaterialLocationInfo();
//            EventInfo setMaterialLocationeventInfo = EventInfoUtil.makeEventInfo("setMaterialLocation", getEventUser(), getEventComment(), null, null);
//            for (Element product : productList )
//            {
//                productkey.setProductName(SMessageUtil.getChildText(product, "PRODUCTNAME", true));
//                Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);
//                String materialChamber = productData.getUdfs().get("MATERIALCHAMBER");
//                if(!productData.getMaterialLocationName().equals("") || !materialChamber.isEmpty())
//                {
//                    MaterialLocationInfo.setMaterialLocationName("");
//                    productData.getUdfs().put("MATERIALCHAMBER","");
//                    ProductServiceProxy.getProductService().update(productData);
//                    MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(setMaterialLocationeventInfo, productData, MaterialLocationInfo);
//                }
//            }*/
//    
//            /*EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//            List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList);
//    
//            //job end in unpacker scenario
//            if(CommonUtil.isInitialInput(machineName))
//            {
//                this.UnpackerProcessAbort(doc);
//                return;
//            }
//            else if (CommonUtil.getLotListByCarrier(carrierName, false).size() > 1
//                    //161117 by swcho : crash handling both multi-Lot and pilot job
//                    && !ExtendedObjectProxy.getFirstGlassJobService().isOnPilotJob(carrierName)
//                    )
//            {
//                this.GlassProcessAbort(doc);
//                return;
//            }
//            else if(CommonUtil.isBpkType(machineName) && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                this.BoxUnpackProcessEnd(doc);
//                return;
//            }
//            //20170808 by zhanghao  EVA  PU LotProcessAbort TrackOutGlass
//            else if (CommonUtil.isInineType(machineName)&&CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                this.GlassProcessEnd(doc);
//                return;
//            }*/
//    
//            //20170808 by zhanghao Normal PU LotProcessAbort TrackOut Lot
//            /*else if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
//                Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
//    
//                eventInfo.setEventName("TrackOutLot");
//                Lot trackOutLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
//                Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);
//                if (trackOutLot == null)
//    
//                {
//                    eventLog.warn(String.format("Job end without any Product at Machine[%s] Port[%s] PortType[%s] CST[%s]",
//                            machineName, portName, CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), carrierName));
//    
//                    return;
//                }
//                String lotJudge = "";
//                String reworkFlag = "";
//                String lotSecondaryGrade = "";
//                lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(trackOutLot, lotJudge, productPGSRCSequence);
//    
//                ExtendedObjectProxy.getSampleLotService().reserveSampling(eventInfo, trackOutLot);
//                ExtendedObjectProxy.getFlowSampleLotService().reserveSampling(eventInfo, trackOutLot);
//    
//                String decideSampleNodeStack = "";
//    
//                //TK OUT
//                Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, trackOutLot, portData,
//                                                        carrierName, lotJudge, "", "",
//                                                        productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack);
//                  //Get TK OUT beforeOperation
//                String beforeOperation = CommonUtil.getValue(afterTrackOutLot.getUdfs(), "BEFOREOPERATIONNAME");
//                reworkFlag = StringUtil.equals(trackOutLot.getReworkState(), "NotInRework") && lotJudge.equalsIgnoreCase("R")?"Y":reworkFlag;
//                lotSecondaryGrade = MESLotServiceProxy.getLotServiceUtil().decideLotSecondaryJudge(trackOutLot, lotSecondaryGrade, productPGSRCSequence);
//    
//                // Start Rework
//                afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startRework(eventInfo, afterTrackOutLot, reworkFlag, true);
//    
//                // Repair
//                afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startAlteration(eventInfo, afterTrackOutLot, afterTrackOutLot.getLotGrade());
//    
//                // Skip
//                afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, beforeTrackOutLot, afterTrackOutLot);
//    
//                // Ship
//                afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().shipLot(afterTrackOutLot, doc);
//    
//                //Hold
//                if(StringUtil.equals(lotSecondaryGrade, "M"))
//                {
//                    afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, afterTrackOutLot, "HoldLot", "MixLotSecondaryGrade");
//                }
//                else if(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("RUB"))
//                {
//                    if(!StringUtil.equals(lotSecondaryGrade, "") && !StringUtil.equals(lotSecondaryGrade, "A"))
//                    {
//                        afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, afterTrackOutLot, "HoldLot", "LotSecondaryGrade is not A");
//                    }
//                }
//                return;
//            }*/
//    
//            //2017.4.8 zhongsl MES trackOut and Hold for that EAP/EAS dont report productList
//            /*Document copyDocAbnormal = (Document)doc.clone();
//            List<Element> productListAbnoraml = SMessageUtil.getBodySequenceItemList(copyDocAbnormal, "PRODUCTLIST", false);
//            if(productList.size() < 1 && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
//            {
//                List<Product> productListAbnormalEnd = null;
//                Lot lotDataAbnormalEnd = null;
//    
//                if(ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName).isEmpty())
//                {
//                    lotDataAbnormalEnd = CommonUtil.getLotInfoBydurableName(carrierName);
//                }
//                else
//                {
//                    String firstGlassLot = ExtendedObjectProxy.getFirstGlassLotService().getActiveLotNameByCarrier(carrierName);
//                    lotDataAbnormalEnd = CommonUtil.getLotInfoByLotName(firstGlassLot);
//                }
//    
//                productListAbnormalEnd = LotServiceProxy.getLotService().allUnScrappedProducts(lotDataAbnormalEnd.getKey().getLotName());
//                for (Product productDataAbnoraml : productListAbnormalEnd)
//                {
//                    Element productElement = new Element("PRODUCT");
//    
//                    Element lotNameElement = new Element("LOTNAME");
//                    lotNameElement.setText(productDataAbnoraml.getLotName());
//                    productElement.addContent(lotNameElement);
//    
//                    Element productNameElement = new Element("PRODUCTNAME");
//                    productNameElement.setText(productDataAbnoraml.getKey().getProductName());
//                    productElement.addContent(productNameElement);
//    
//                    Element positionElement = new Element("POSITION");
//                    positionElement.setText(String.valueOf(productDataAbnoraml.getPosition()));
//                    productElement.addContent(positionElement);
//    
//                    productList.add(productElement);
//                }
//            }*/
//    
//            //Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, carrierName, productList);
//            //161117 by swcho : pilot job handling
//            //Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
//    
//            /*if (cancelTrackInLot == null)
//            {
//                eventLog.warn(String.format("Job end without any Product at Machine[%s] Port[%s] PortType[%s] CST[%s]",
//                                            machineName, portName, CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), carrierName));
//                return;
//            }
//    
//            //List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList);
//    
//            eventInfo.setEventName("CancelTrackIn");
//            cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, cancelTrackInLot, portData, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
//    
//            //Cancel TrackIn For MQC
//            //MESLotServiceProxy.getMQCLotServiceUtil().CancelTrackInForMQC(eventInfo, cancelTrackInLot);
//    
//            //150117 by swcho : success then report to FMC
//            try
//            {
//                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
//            }
//            catch(Exception ex)
//            {
//                eventLog.warn("FMC Report Failed!");
//            }
//    
//            //2017.4.8 zhongsl MES trackOut and Hold for that EAP/EAS don't report productList
//            if(productListAbnoraml.size() < 1 && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
//            {
//                MESLotServiceProxy.getLotServiceUtil().doAfterHoldForAbnormalEnd(eventInfo, carrierName, lotName, "AE", null, doc);
//            }
//            else
//            {
//                //161013 by swcho : hold on abort
//                MESLotServiceProxy.getLotServiceUtil().doAfterHold(eventInfo, carrierName, lotName, "A", null, doc);
//            }       */
//    
//            // 2018-11-21 Park Jeong Su Add for ClearName And Update LastRuntime
//            MESLotServiceProxy.getLotServiceUtil().clearCarrerNameOnAutoMQCSetting(eventInfo, carrierName,machineName);
//        }
//        /* 20190410, hhlee, modify, LotProcessEnd fail ==>> */
//        catch (Exception ex) 
//        {
//            /* EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName, 
//             * boolean setLotHold, boolean setCarrierHold, boolean carrierStart
//             */
//            GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
//            
//            /* 20190425, hhlee, modify, change variable(carrierEnd delete) */
//            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
//            //        carrierName, StringUtil.EMPTY, false, false, false, true); 
//            /* 20190425, hhlee, modify, add variable(setFutureHold) */
//            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
//            //        carrierName, StringUtil.EMPTY, true, false, "TrackOutFail");
//            /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
//            //doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
//            //        carrierName, StringUtil.EMPTY, false, false, false, "TrackOutFail");
//            doc = MESLotServiceProxy.getLotServiceImpl().setReturnMessageByTrackOut(eventInfo, ex, doc, 
//                      carrierName, StringUtil.EMPTY, true, false, false, false, "TrackOutFail");
//        }
//        //catch (CustomException ce) /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */
//        //{
//        //    GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
//        //    
//        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//        //    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "TrackOut Fail.! - " + ce.errorDef.getLoc_errorMessage());            
//        //}
//        /* <<== 20190410, hhlee, modify, LotProcessEnd fail */        
//        return doc;
//    }
//
//    private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence) throws CustomException
//    {
//        eventLog.info("Split Lot for TK out");
//
//        eventInfo.setEventName("Create");
//        Lot targetLot = this.createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductP productP = new ProductP();
//            productP.setProductName(productPGSRC.getProductName());
//            productP.setPosition(productPGSRC.getPosition());
//            productP.setUdfs(productPGSRC.getUdfs());
//            productPSequence.add(productP);
//        }
//
//        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//
//        //do splits
//        eventInfo.setEventName("Split");
//        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        eventLog.info(String.format("Lot[%s] is separated", targetLot.getKey().getLotName()));
//
//        return targetLot;
//    }
//
//    public Lot createWithParentLot(EventInfo eventInfo, Lot parentlotData, String newCarrierName, boolean deassignFlag,
//            Map<String, String> assignCarrierUdfs, Map<String, String> udfs)
//                    throws CustomException
//    {
//        String newLotName = "";
//        ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(
//                parentlotData.getFactoryName(), parentlotData.getProductSpecName(),GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
//
//        Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
//        //nameRuleAttrMap.put("PRODUCTIONTYPE", srcLotData.getProductionType());
//        //nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));
//        nameRuleAttrMap.put("FACTORYNAME", productSpecData.getKey().getFactoryName());
//        nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecData.getKey().getProductSpecName());
//
//        try
//        {
//            //20171221, kyjung
//            String lotName = StringUtil.substring(parentlotData.getKey().getLotName(), 0, 8);
//            List<String> argSeq = new ArrayList<String>();
//            argSeq.add(lotName);
//            List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
//
//            //List<String> lstName = CommonUtil.generateNameByNamingRule("LotNaming", nameRuleAttrMap, 1);
//            newLotName = lstName.get(0);
//        }
//        catch(Exception ex)
//        {
//            new CustomException("LOT-9011", ex.getMessage());
//        }
//
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(
//                parentlotData.getAreaName(),
//                //"Y" , assignCarrierUdfs, newCarrierName,
//                deassignFlag?"N":"Y", assignCarrierUdfs, deassignFlag?"":newCarrierName,
//                        parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
//                        parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(),
//                        parentlotData.getLotHoldState(), newLotName, parentlotData.getLotProcessState(), parentlotData.getLotState(),
//                        parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
//                        parentlotData.getOriginalLotName(), parentlotData.getPriority(),
//                        parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
//                        parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), parentlotData.getProductionType(),
//                        new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
//                        parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(),
//                        parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
//                        parentlotData.getReworkCount(), "", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(),
//                        parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
//                        parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(),
//                        udfs, parentlotData);
//
//        Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);
//
//        return newLotData;
//    }
//
//    private void copyFutureAction(Lot parent, Lot child)
//    {
//        //Copy  FutureAction from parent to child
//        // Find future Action
//        Object[] bindSet =
//                new Object[] {
//                parent.getKey().getLotName(),
//                parent.getFactoryName(),
//                parent.getProcessFlowName(),
//                parent.getProcessFlowVersion(),
//                parent.getProcessOperationName(),
//                parent.getProcessOperationVersion() };
//
//        try
//        {
//            List<LotFutureAction> lotFutureActionList = LotServiceProxy.getLotFutureActionService().select(SqlStatement.LotFutureActionKey, bindSet);
//            if(lotFutureActionList.size() > 0)
//            {
//                Object[] bindSetChild =
//                        new Object[] {
//                        child.getKey().getLotName(),
//                        child.getFactoryName(),
//                        child.getProcessFlowName(),
//                        child.getProcessFlowVersion(),
//                        child.getProcessOperationName(),
//                        child.getProcessOperationVersion() };
//
//                String condition = "";
//
//                for(LotFutureAction action : lotFutureActionList)
//                {
//                    LotServiceProxy.getLotFutureActionService().update(action, condition, bindSetChild);
//                }
//            }
//        }
//        catch (NotFoundSignal ne)
//        {
//            return;
//        }
//        catch(Exception e)
//        {
//            return;
//        }
//    }
//
//    private void TrackInCancel(Lot lotData, String carrierName, EventInfo eventInfo,String note) throws CustomException
//    {
//        MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();
//        makeLoggedOutInfo.setAreaName(lotData.getAreaName());
//        makeLoggedOutInfo.setMachineName(lotData.getMachineName());
//        //makeLoggedOutInfo.setMachineRecipeName(lotData.getMachineRecipeName());
//        makeLoggedOutInfo.setProcessFlowName(lotData.getProcessFlowName());
//        makeLoggedOutInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
//        makeLoggedOutInfo.setProcessOperationName(lotData.getProcessOperationName());
//        makeLoggedOutInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
//        makeLoggedOutInfo.setNodeStack(lotData.getNodeStack());
//        
//        Durable durableData = null;
//        if (StringUtils.isNotEmpty(carrierName)) {
//            makeLoggedOutInfo.setCarrierName(carrierName);
//            
//            // Added by smkang on 2018.10.27 - If a carrier state is changed from Available to InUse, synchronization should be executed.
//            durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//        }
//        
//        if(note.length()>3500){
//            note = note.substring(0, 3499);
//        }
//        // 2019.06.03_hsryu_Delete Logic. Mantis 0003934. Not Memory CancelTrackIn Event. Momofy NewEvent. 
//        //makeLoggedOutInfo.getUdfs().put("NOTE", note);
//
//        Lot cancelTrackInLotData = LotServiceProxy.getLotService().makeLoggedOut(lotData.getKey(), eventInfo, makeLoggedOutInfo);
//        
//        // Added by smkang on 2019.03.04 - TrackOut 또는 CancelTrackIn 후 Lot의 MachineName과 PortName을 삭제하는 코드 임시 적용
//        cancelTrackInLotData.setMachineName("");
//        cancelTrackInLotData.getUdfs().put("PORTNAME", "");
//        LotServiceProxy.getLotService().update(cancelTrackInLotData);
//        
//        // Added by smkang on 2018.10.27 - For synchronization of a carrier state and lot quantity, common method will be invoked.
//        try {
//            if (durableData != null && durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
//                Element bodyElement = new Element(SMessageUtil.Body_Tag);
//                bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
//                bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_InUse));
//                
//                // Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
////              Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
//                Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
//                
//                MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
//            }
//        } catch (Exception e) {
//            eventLog.warn(e);
//        }
//    }
//
//    /**
//     * @Name     UnpackerProcessAbort
//     * @since    2018. 5. 20.
//     * @author   hhlee
//     * @contents Process for Abort in the PU Port of Unpacker
//     * @param doc
//     * @throws CustomException
//     */
//    public void UnpackerProcessAbort(Document doc)  throws CustomException
//    {
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//        String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", true);
//        String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", true);
//        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
//        String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
//        String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
//        String sLotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", true);
//        String sLotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", true);
//        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
//        List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
//        String lotJudge = "";
//
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");
//
//        //Machine Data
//        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//        Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//        Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//
//        /* 20180713, Add, CST Validation ==>> */
//        CommonValidation.checkEmptyCst(carrierName);
//        Lot  validateLotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
//        if (validateLotData != null)
//        {
//            throw new CustomException("CST-0006", carrierName);
//        }
//        /* <<== 20180713, Add, CST Validation */
//         
//        try
//        {
//            if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals("NG"))
//            {
//                if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//                {
//                    eventLog.info("Unpacker MGV unloader case");
//
//                    boolean doit = false;
//
//                    List<Element> eProductListElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
//
//                    for(Element ePruduct : eProductListElement)
//                    {
//                        String sProductName = SMessageUtil.getChildText(ePruduct, "PRODUCTNAME", true);
//                        String sPosition = SMessageUtil.getChildText(ePruduct, "POSITION", true);
//
//                        VirtualGlass productData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {sProductName});
//
//                        productData.setCarrier(durableData.getKey().getDurableName());
//                        productData.setPosition(Long.parseLong(sPosition));
//
//                        eventInfo.setEventName("Assign");
//                        productData = ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, productData);
//
//                        doit = true;
//                    }
//
//                    if (doit)
//                    {
//                        durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
//                        durableData.setLotQuantity(1);
//
//                        eventInfo.setEventName("AssignCarrier");
//                        SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(durableData.getUdfs());
//
//                        DurableServiceProxy.getDurableService().update(durableData);
//                        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
//                    }
//
//                    return;
//                }
//                else
//                {
//                    eventLog.info("Unpacker MGV loader case");
//                    return;
//                }
//            }
//
//            eventLog.info("Unpacker AGV unloader case");
//
//            /* 20181106, hhlee, modify, the Lot is not mixed in when Unpacker becomes LotProcessEnd ==>> */
//            ///* 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. ==>> */
//            String lotName = MESLotServiceProxy.getLotServiceUtil().getLotNamefromProductElements(productElement);
//            //String lotName = MESLotServiceProxy.getLotServiceUtil().getFirstLotNamefromProductElements(productElement, machineName, GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
//            ///* <<== 20180531, In case the Lot is mixed in when Unpacker becomes LotProcessEnd, bring the lotName of the first started plan. */
//            /* <<== 20181106, hhlee, modify, the Lot is not mixed in when Unpacker becomes LotProcessEnd */
//            
//            //modified by wghuang 20181105        
//            ReserveLot reserveLot = null;
//            ProductRequestPlanKey pPlanKey = null;
//            ProductRequestPlan pPlanData = null;
//            
//            //Product Request Key & Data
//            ProductRequestKey pKey = new ProductRequestKey();
//                
//            ProductRequest pData = null;
//            
//            try
//            {
//                try
//                {
//                     //Reserve Lot Data
//                    reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});
//                }
//                catch(Exception ex)
//                {                  
//                }
//                
//                if(reserveLot == null)
//                    throw new CustomException("LOT-0095",machineName,lotName );
//                
//                //Product Request Plan Data
//                pPlanKey = new ProductRequestPlanKey(reserveLot.getProductRequestName(), machineName, reserveLot.getPlanReleasedTime());
//                             
//                try
//                {
//                    pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
//                }
//                catch(Exception ex)
//                {                  
//                }
//                
//                if(pPlanData == null)
//                    throw new CustomException("PRODUCTREQUEST-0054","");
//                
//                
//                //productRequestData
//                pKey.setProductRequestName(pPlanData.getKey().getProductRequestName());
//                
//                try
//                {
//                    pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//                }
//                catch(Exception ex)
//                {                  
//                }
//                
//                if(pData == null)
//                    throw new CustomException("PRODUCTREQUEST-0055","");        
//            }
//            catch(Exception ex)
//            {
//                throw ex;
//            }
//            
//            //modified by wghuang 20181106
//            
//            /*     //Reserve Lot Data
//            ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {machineName, lotName});
//
//            //Product Request Plan Data
//            ProductRequestPlanKey pPlanKey = new ProductRequestPlanKey(reserveLot.getProductRequestName(), machineName, reserveLot.getPlanReleasedTime());
//            ProductRequestPlan pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
//
//            //Product Request Key & Data
//            ProductRequestKey pKey = new ProductRequestKey();
//            pKey.setProductRequestName(pPlanData.getKey().getProductRequestName());
//            ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);*/
//
//            //1. Release Lot
//            //1.1) get lot Data by Lot Name
//            Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
//
//            double lotCreateProductQty = lotData.getCreateProductQuantity();
//
//            //Product Spec Data
//            ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
//
//            //1.2) get PGSSequance
//            List<Element> eProductListElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
//            List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
//
//            /*String[] prdname = {"W", "V", "U", "T", "S", "R", "Q", "P", "N", "M",
//                    "L", "K", "J", "H", "G", "F", "E", "D", "C", "B",
//                    "A", "9", "8", "7", "6", "5", "4", "3", "2", "1"};*/
//
//            String[] prdname = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
//                                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
//                                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
//            
//            int prdcnt = 0;
//
//            VirtualGlass vGlassData = new VirtualGlass();
//
//            String cutType = specData.getUdfs().get("CUTTYPE");
//            List<Object[]> insertArgList = new ArrayList<Object[]>();
//            String insertSql = " INSERT INTO CT_PANELJUDGE "
//                    + " (PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, "
//                    + " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE)"
//                    + " VALUES "
//                    + " (:PANELNAME, :PANELJUDGE, :PANELGRADE, NVL(:XAXIS1,0), NVL(:YAXIS1,0), NVL(:XAXIS2,0), NVL(:YAXIS2,0), "
//                    + "  :GLASSNAME, :HQGLASSNAME, :CUTTYPE, :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT, :PRODUCTSPECTYPE)";
//            
//            
//            List<Object[]> insertArgListForHQGlass = new ArrayList<Object[]>();
//            
//            String insertSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGE "
//                    + " (HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, GLASSNAME, "
//                    + " LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT)"
//                    + " VALUES "
//                    + " (:HQGLASSNAME, :HQGLASSJUDGE, NVL(:XAXIS,0), NVL(:YAXIS,0), :GLASSNAME, "
//                    + " :LASTEVENTNAME, :LASTEVENTUSER, TO_DATE(:LASTEVENTTIME,'yyyy-MM-dd HH24:mi:ss'), :LASTEVENTCOMMENT)";
//
//            for(Element ePruduct : eProductListElement)
//            {
//                //String sProductName = lotName + prdname[prdcnt++];
//                //String sProductName = StringUtil.substring(lotName, 0, 8) + prdname[prdcnt++];
//
//                String sPosition = SMessageUtil.getChildText(ePruduct, "POSITION", true);
//                String sVcrProductName = SMessageUtil.getChildText(ePruduct, "VCRPRODUCTNAME", true);
//                String sCrateName = SMessageUtil.getChildText(ePruduct, "CRATENAME", false);
//
//                //String sProductName = lotName + prdname[prdcnt++];
//                String sProductName = StringUtil.substring(lotName, 0, 8) + prdname[Integer.parseInt(sPosition) - 1];
//                
//                /* 20180920, hhlee, add Processed Operation Before AOI ==>> */
//                String machineRecipeName = SMessageUtil.getChildText(ePruduct, "PRODUCTRECIPE", false);
//                /* <<== 20180920, hhlee, add Processed Operation Before AOI */
//                
//                try
//                {
//                    VirtualGlass vData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {sVcrProductName});
//
//                    if(sCrateName.isEmpty())
//                    {
//                        sCrateName = vData.getCrateName().toString();
//                    }
//
//                    //Update product name to Virtual Glass Table
//                    vData.setProductName(sProductName);
//                    vData.setPosition(Long.parseLong(sPosition));
//                    vGlassData = ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vData);
//                    
//                    /* 20180920, hhlee, add Processed Operation Before AOI ==>> */
//                    //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(sProductName, machineName, lotData.getFactoryName(), lotData.getProcessOperationName(), vData.getVirtualGlassName());
//                    /* 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) ==>> */
//                    //MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, sProductName, machineName, lotData, machineRecipeName, vData.getVirtualGlassName());
//                    MESProductServiceProxy.getProductServiceImpl().setProcessedOperationBeforeAOI(eventInfo, ePruduct, sProductName, machineName, lotData, machineRecipeName, vData.getVirtualGlassName());
//                    /* <<== 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) */
//                    /* <<== 20180920, hhlee, add Processed Operation Before AOI */
//                }
//                catch (Exception ex)
//                {
//
//                }
//
//                ProductPGS productInfo = new ProductPGS();
//                productInfo.setPosition(Long.parseLong(sPosition));
//                productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
//                productInfo.setProductName(sProductName);
//                productInfo.setSubProductGrades1("");
//                productInfo.setSubProductQuantity1(specData.getSubProductUnitQuantity1());
//                productInfo.getUdfs().put("CRATENAME", sCrateName);
//                
//                /* 20181113, hhlee, delete, ==>> */
//                ///* 20181023, hhlee, add, productJudge ==>> */
//                //productInfo.getUdfs().put("PRODUCTJUDGE", productInfo.getProductGrade());
//                ///* <<== 20181023, hhlee, add, productJudge */
//                /* <<== 20181113, hhlee, delete, */
//                
//                productPGSSequence.add(productInfo);
//                            
//                try
//                {
//                    if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_HALF) )
//                    {
//                        int cut1XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1XAXISCOUNT")) : 0;
//                        int cut2XaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2XAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2XAXISCOUNT")) : 0;
//                        int cut1YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT1YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT1YAXISCOUNT")) : 0;
//                        int cut2YaxisCount = StringUtils.isNumeric(specData.getUdfs().get("CUT2YAXISCOUNT")) ? Integer.parseInt(specData.getUdfs().get("CUT2YAXISCOUNT")) : 0;
//                        
//                        for ( int i = 1; i < 3; i++ )
//                        {
//                            if(i==1)
//                            {
//                                for(int x=0; x<cut1XaxisCount; x++)
//                                {
//                                    for(int y=0; y< cut1YaxisCount; y++)
//                                    {
//                                        Object[] inbindSet = new Object[15];
//
//                                        inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
//                                        inbindSet[1] = "G";
//                                        inbindSet[2] = "G";
//                                        inbindSet[3] = String.valueOf(cut1XaxisCount);
//                                        inbindSet[4] = String.valueOf(cut1YaxisCount);
//                                        inbindSet[5] = String.valueOf(cut2XaxisCount);
//                                        inbindSet[6] = String.valueOf(cut2YaxisCount);
//                                        inbindSet[7] = sProductName;
//                                        inbindSet[8] = sProductName + Integer.toString(i);
//                                        inbindSet[9] = cutType;
//                                        inbindSet[10] = "Created";
//                                        inbindSet[11] = eventInfo.getEventUser();
//                                        inbindSet[12] = ConvertUtil.getCurrTime();
//                                        inbindSet[13] = "Auto Create PanelJudge";
//                                        inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
//                                        
//                                        insertArgList.add(inbindSet);
//                                    }
//                                }
//                            }
//                            
//                            else if(i==2)
//                            {
//                                for(int x=0; x<cut2XaxisCount; x++)
//                                {
//                                    for(int y=cut1YaxisCount; y<cut1YaxisCount+cut2YaxisCount; y++)
//                                    {
//                                        Object[] inbindSet = new Object[15];
//
//                                        inbindSet[0] = sProductName + Integer.toString(i) + StringUtils.leftPad(Integer.toString(x+1), 2, "0") + StringUtils.leftPad(Integer.toString(y+1), 2, "0");
//                                        inbindSet[1] = "G";
//                                        inbindSet[2] = "G";
//                                        inbindSet[3] = String.valueOf(cut1XaxisCount);
//                                        inbindSet[4] = String.valueOf(cut1YaxisCount);
//                                        inbindSet[5] = String.valueOf(cut2XaxisCount);
//                                        inbindSet[6] = String.valueOf(cut2YaxisCount);
//                                        inbindSet[7] = sProductName;
//                                        inbindSet[8] = sProductName + Integer.toString(i);
//                                        inbindSet[9] = cutType;
//                                        inbindSet[10] = "Created";
//                                        inbindSet[11] = eventInfo.getEventUser();
//                                        inbindSet[12] = ConvertUtil.getCurrTime();
//                                        inbindSet[13] = "Auto Create PanelJudge";
//                                        inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
//                                        
//                                        insertArgList.add(inbindSet);
//                                    }
//                                }
//                            }
//
//                            // for HQGlassJudge
//                            Object[] inbindSetForHQGlass = new Object[9];
//
//                            inbindSetForHQGlass[0] = sProductName + Integer.toString(i);
//                            inbindSetForHQGlass[1] = "G";
//                            if(i==1)
//                            {
//                                inbindSetForHQGlass[2] = String.valueOf(cut1XaxisCount);
//                                inbindSetForHQGlass[3] = String.valueOf(cut1YaxisCount);
//                            }
//                            else if(i==2)
//                            {
//                                inbindSetForHQGlass[2] = String.valueOf(cut2XaxisCount);
//                                inbindSetForHQGlass[3] = String.valueOf(cut2YaxisCount);
//                            }
//                            inbindSetForHQGlass[4] = sProductName;
//                            inbindSetForHQGlass[5] = "Created";
//                            inbindSetForHQGlass[6] = eventInfo.getEventUser();
//                            inbindSetForHQGlass[7] = ConvertUtil.getCurrTime();
//                            inbindSetForHQGlass[8] = "Auto Create HQGlassJudge";
//
//                            insertArgListForHQGlass.add(inbindSetForHQGlass);
//                        }
//                    }
//                    else if ( StringUtil.equals(cutType, GenericServiceProxy.getConstantMap().CUTTYPE_QUARTER) )
//                    {
//                        for ( int i = 1; i < 5; i++ )
//                        {
//                            String character = "";
//                            if (i == 1)
//                            {
//                                character = "A";
//                            }
//                            else if (i == 2)
//                            {
//                                character = "B";
//                            }
//                            else if (i == 3)
//                            {
//                                character = "C";
//                            }
//                            else if (i == 4)
//                            {
//                                character = "D";
//                            }
//                            
//                            Object[] inbindSet = new Object[15];
//                            inbindSet[0] = sProductName + Integer.toString(i) + character;
//                            inbindSet[1] = "G";
//                            inbindSet[2] = "G";
//                            inbindSet[3] = specData.getUdfs().get("CUT1XAXISCOUNT");
//                            inbindSet[4] = specData.getUdfs().get("CUT1YAXISCOUNT");
//                            inbindSet[5] = specData.getUdfs().get("CUT2XAXISCOUNT");
//                            inbindSet[6] = specData.getUdfs().get("CUT2YAXISCOUNT");
//                            inbindSet[7] = sProductName;
//                            inbindSet[8] = sProductName + Integer.toString(i);
//                            inbindSet[9] = cutType;
//                            inbindSet[10] = "Created";
//                            inbindSet[11] = eventInfo.getEventUser();
//                            inbindSet[12] = ConvertUtil.getCurrTime();
//                            inbindSet[13] = "Auto Create PanelJudge";
//                            inbindSet[14] = specData.getUdfs().get("PRODUCTSPECTYPE");
//                            
//                            insertArgList.add(inbindSet);
//                        }
//                    }
//                }
//                catch (Throwable e)
//                {
//                    eventLog.warn(String.format("BindSet Fail! CT_PANELJUDGE"));
//                }
//            }
//
//            //1.3)Release Lot
//            MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(
//                    lotData, machineData.getAreaName(), lotData.getNodeStack(),
//                    lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
//                    lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
//                    lotData.getProductionType(),
//                    lotData.getUdfs(), "",
//                    lotData.getDueDate(), lotData.getPriority());
//
//            lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);
//
//            /* Array Product Flag Insert ==> */
//            List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
//            if(productList != null)
//            {
//                ExtendedObjectProxy.getProductFlagService().setCreateProductFlagForUPK(eventInfo, productList);
//            }
//            /* <== Array Product Flag Insert */
//            
//            //String crateName = lotData
//            //1.4)If Lot Create Product Qty != Product Qty, Future Hold Lot
//            if(lotData.getProductQuantity() != lotCreateProductQty)
//            {
//                eventLog.info("Lot Create Product Quantity and Product Quantity are different");
//                //futureHoldLot(lotData, machineName, eventInfo);
//            }
//            
//            ///* hhlee, 20180703, Add Unpacker LotProcessAborted Hold ==>> */
//            ////futureHoldLot(lotData, machineName, eventInfo);
//            //eventInfo.setEventComment("LotProcessAborted occurred");
//            //MESLotServiceProxy.getLotServiceUtil().futureHoldLot(lotData, machineName, eventInfo, "AHOLD", "LAHL");
//            ///* <<== hhlee, 20180703, Add Unpacker LotProcessAborted Hold */
//            
//            //2. increment Product Request
//            try
//            {
//                pPlanData.setPlanQuantity(pPlanData.getPlanQuantity() - ((long)lotCreateProductQty - productPGSSequence.size()));
//                ProductRequestPlanServiceProxy.getProductRequestPlanService().update(pPlanData);
//                pPlanData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(pPlanKey);
//                
//                MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(pPlanData.getKey().getProductRequestName(), pPlanData, "R", productPGSSequence.size(), eventInfo);
//
//            }
//            catch(Exception e)
//            {
//                eventLog.error("incrementWorkOrderReleaseQty Failed");
//            }
//
//            //3. Reserve Lot State Change
//            eventInfo.setEventName("ChangeState");
//            reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
//
//            ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
//            
//            //5. Auto Track In / Out Unpacker
//            String loadPort = "";
//            try
//            {
//                Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(vGlassData.getCrateName().toString());
//                loadPort = consumableData.getMaterialLocationName();
//
//                if(consumableData.getMaterialLocationName().isEmpty())
//                {
//                    loadPort = "1";
//                }
//            }
//            catch (Exception ex)
//            {
//                loadPort = "1";
//            }
//
//            try
//            {
//                GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSql, insertArgList);
//                
//                String insHistSql = " INSERT INTO CT_PANELJUDGEHISTORY "
//                        + " (TIMEKEY, PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
//                        + " GLASSNAME, HQGLASSNAME, CUTTYPE, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTSPECTYPE) "
//                        + " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), PANELNAME, PANELJUDGE, PANELGRADE, XAXIS1, YAXIS1, XAXIS2, YAXIS2, " 
//                        + " GLASSNAME, HQGLASSNAME, CUTTYPE, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PRODUCTSPECTYPE "
//                        + " FROM CT_PANELJUDGE "
//                        + " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
//                
//                Map<String, Object> insHistbindSet = new HashMap<String, Object>();
//                insHistbindSet.put("LOTNAME", lotData.getKey().getLotName());
//                
//                GenericServiceProxy.getSqlMesTemplate().update(insHistSql, insHistbindSet);
//            }
//            catch (Exception ex)
//            {
//                eventLog.warn(String.format("Update Fail! CT_PANELJUDGE"));
//            }
//            
//            try
//            {
//                GenericServiceProxy.getSqlMesTemplate().updateBatch(insertSqlForHQGlass, insertArgListForHQGlass);
//                
//                String insHistSqlForHQGlass = " INSERT INTO CT_HQGLASSJUDGEHIST "
//                        + " (TIMEKEY, HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
//                        + " GLASSNAME, EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT) "
//                        + " SELECT TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISSsss'), HQGLASSNAME, HQGLASSJUDGE, XAXIS, YAXIS, " 
//                        + " GLASSNAME, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT "
//                        + " FROM CT_HQGLASSJUDGE "
//                        + " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) ";
//                
//                Map<String, Object> insHistbindSetForHQGlass = new HashMap<String, Object>();
//                insHistbindSetForHQGlass.put("LOTNAME", lotData.getKey().getLotName());
//                
//                GenericServiceProxy.getSqlMesTemplate().update(insHistSqlForHQGlass, insHistbindSetForHQGlass);
//            }
//            catch (Exception ex)
//            {
//                eventLog.warn(String.format("Update Fail! CT_HQGLASSJUDGE"));
//            }
//            
//            Document docCopy = (Document)doc.clone();
//
//            String [] CrateNameAndPRecipeName = MESLotServiceProxy.getLotServiceUtil().getCrateNameAndMRecipefromProductElements(productElement).split(",");
//
//            Document trackInOutDoc = writeTrackInOutRequest(docCopy, lotName, machineName, carrierName, loadPort, portName,CrateNameAndPRecipeName[0],CrateNameAndPRecipeName[1]);
//
//            this.TrackInOutLot(trackInOutDoc);
//
//            //eventInfo.setEventName("Hold");
//            //eventInfo.setEventComment("Lot Process Abort Hold.");
//            //eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//            //eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
//            //MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, "HOLD", "LAHL");
//            //MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, "LAHL",""); /* add, 20180608 */
//                
//            //Lot holdLot = abortHoldLot(eventInfo, lotData.getKey().getLotName(), "HOLD", "LAHL");
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrierNotBeginTraction(eventInfo, carrierName,null,"LA","");
//            
//            eventInfo.setEventName("Hold");
//            eventInfo.setEventComment("Lot Process Abort Hold");
//            /* 20190424, hhlee, modify, changed function ==>> */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","");
//            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
//            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
//            /* <<== 20190424, hhlee, modify, changed function */
//                       
//            /*String replySubject = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
//                    GenericServiceProxy.getESBServive().sendBySender(replySubject, trackInOutDoc, "LocalSender");*/
//        }
//        catch (CustomException ce)     
//        {
//           //add by wghuang 20181105
//           //modified by wghuang 20181105. requested by guishi.[Release, TrackIn, TrackOut in one Transaction and doing commit and rollback at same time. if one of those got fail than this transaction will be rollbacked.]
//           GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
//                     
//           //Start a new Transaction
//           //create a new transaction doing HoldCST
//           GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
//           
//           /* 20180531, Add CST Hold Logic ==>> */
//           /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */                           
//           eventInfo.setEventComment("TrackOut Fail.!" + ce.errorDef.getLoc_errorMessage());
//           MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), eventInfo.getEventComment(), "HoldCST","LAHC");
//           SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ce.errorDef.getErrorCode());
//           /* 20190402, hhlee, modify, Add "TrackOut Fail.!" Message */
//           SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "TrackOut Fail.!" + ce.errorDef.getLoc_errorMessage());
//           /* <<== 20180531, Add CST Hold Logic */
//           
//         //Commit new Transaction
//           GenericServiceProxy.getTxDataSourceManager().commitTransaction();
//        }
//        /* <<== 20180616, hhlee, Modify */      
//    }
//
//    private void TrackInOutLot(Document doc) throws CustomException
//    {
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
//
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
//        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//        String loadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
//        String unLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
//        String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
//        String productRecipeName = SMessageUtil.getBodyItemValue(doc, "PRODUCTRECIPE", false);
//
//        Object result = null;
//
//        Lot lotData =  MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//
//        try
//        {
//            Port loader = this.searchLoaderPort(machineName);
//
//            String machineRecipeName = StringUtil.EMPTY;
//
//            if(StringUtil.isEmpty(productRecipeName))
//            {
//                ConsumableKey consumableKey = new ConsumableKey(crateName);
//                Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
//                String consumableSpec = con.getConsumableSpecName();
//
//                ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
//                consumableSpecKey.setConsumableSpecName(consumableSpec);
//                consumableSpecKey.setConsumableSpecVersion("00001");
//                consumableSpecKey.setFactoryName(con.getFactoryName());
//                ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
//
//                machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");
//            }
//            else
//            {
//                machineRecipeName = productRecipeName;
//            }
//
//            /*
//            doc = this.writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), loadPort, machineRecipeName);
//
//            result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackInLot.class.getName(), null, null), "execute", new Object[] {doc});
//
//            Port unloader = this.searchUnloaderPort(loader);
//
//            doc = this.writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName);
//
//            result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackOutLotPU.class.getName(), null, null), "execute", new Object[] {doc});
//            */
//            
//            doc = MESLotServiceProxy.getLotServiceUtil().writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), loadPort, machineRecipeName);
//
//            MESLotServiceProxy.getLotServiceUtil().TrackInLotForUPK(doc, eventInfo);
//            
//            Port unloader = MESLotServiceProxy.getLotServiceUtil().searchUnloaderPort(loader);
//
//            //doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName);
//            doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName, machineRecipeName);
//            
//            MESLotServiceProxy.getLotServiceUtil().TrackOutLotForUPK(doc, eventInfo);
//        }
//        catch (Exception e)
//        {
//            eventLog.error(e);
//            throw e;
//        }
//
//        eventLog.debug("end");
//    }
//
//    private Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName)
//            throws CustomException
//    {
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
//        //SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
//
//        boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//
//        Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
//
//        Element element1 = new Element("LOTNAME");
//        element1.setText(lotName);
//        eleBodyTemp.addContent(element1);
//
//        Element element2 = new Element("MACHINENAME");
//        element2.setText(machineName);
//        eleBodyTemp.addContent(element2);
//
//        Element element3 = new Element("PORTNAME");
//        element3.setText(portName);
//        eleBodyTemp.addContent(element3);
//
//        Element element4 = new Element("CARRIERNAME");
//        element4.setText(carrierName);
//        eleBodyTemp.addContent(element4);
//
//        Element elementPL = new Element("PRODUCTLIST");
//        try
//        {
//            List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
//
//            for (Product productData : productList)
//            {
//                Element elementP = new Element("PRODUCT");
//                {
//                    Element elementS1 = new Element("PRODUCTNAME");
//                    elementS1.setText(productData.getKey().getProductName());
//                    elementP.addContent(elementS1);
//
//                    Element elementS2 = new Element("POSITION");
//                    elementS2.setText(String.valueOf(productData.getPosition()));
//                    elementP.addContent(elementS2);
//
//                    Element elementS3 = new Element("PRODUCTJUDGE");
//                    elementS3.setText(productData.getProductGrade());
//                    elementP.addContent(elementS3);
//                }
//                elementPL.addContent(elementP);
//            }
//        }
//        catch (NotFoundSignal e)
//        {
//            throw new CustomException("PRODUCT-9001", "");
//        }
//        catch (FrameworkErrorSignal fe)
//        {
//            throw new CustomException("PRODUCT-9999", fe.getMessage());
//        }
//        eleBodyTemp.addContent(elementPL);
//
//        //overwrite
//        doc.getRootElement().addContent(eleBodyTemp);
//
//        return doc;
//    }
//
//    private Port searchUnloaderPort(Port portData) throws CustomException
//    {
//        if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
//        {
//            return portData;
//        }
//        else
//        {
//            try
//            {
//                List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?)", new Object[] {portData.getKey().getMachineName(), "PU", "PS"});
//
//                return result.get(0);
//            }
//            catch (NotFoundSignal ne)
//            {
//                throw new CustomException("PORT-9001", portData.getKey().getMachineName(), "");
//            }
//            catch (FrameworkErrorSignal fe)
//            {
//                throw new CustomException("PORT-9999", fe.getMessage());
//            }
//        }
//    }
//
//    private Port searchLoaderPort(String machineName) throws CustomException
//    {
//        try
//        {
//            List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?)", new Object[] {machineName, "PB", "PL", "PS"});
//
//            return result.get(0);
//        }
//        catch (NotFoundSignal ne)
//        {
//            throw new CustomException("PORT-9001", machineName, "");
//        }
//        catch (FrameworkErrorSignal fe)
//        {
//            throw new CustomException("PORT-9999", fe.getMessage());
//        }
//    }
//
//    private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName)
//            throws CustomException
//        {
//            SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInLot");
//            SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", StringUtil.EMPTY);
//
//            //Element eleBody = SMessageUtil.getBodyElement(doc);
//
//            boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//
//            Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
//
//            Element element1 = new Element("LOTNAME");
//            element1.setText(lotName);
//            eleBodyTemp.addContent(element1);
//
//            Element element2 = new Element("MACHINENAME");
//            element2.setText(machineName);
//            eleBodyTemp.addContent(element2);
//
//            Element element3 = new Element("PORTNAME");
//            element3.setText(portName);
//            eleBodyTemp.addContent(element3);
//
//            Element element4 = new Element("RECIPENAME");
//            element4.setText(recipeName);
//            eleBodyTemp.addContent(element4);
//
//            Element element5 = new Element("AUTOFLAG");
//            element5.setText("Y");
//            eleBodyTemp.addContent(element5);
//
//            //overwrite
//            doc.getRootElement().addContent(eleBodyTemp);
//
//            return doc;
//        }
//
//    private Document writeTrackInOutRequest(Document doc, String lotName, String machineName, String carrierName, String loadPort, String unLoadPort, String crateName, String productRecipeName)
//            throws CustomException
//    {
//        SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInOutLot");
//        SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", StringUtil.EMPTY);
//
//        //Element eleBody = SMessageUtil.getBodyElement(doc);
//
//        boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//
//        Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
//
//        Element element1 = new Element("LOTNAME");
//        element1.setText(lotName);
//        eleBodyTemp.addContent(element1);
//
//        Element element2 = new Element("MACHINENAME");
//        element2.setText(machineName);
//        eleBodyTemp.addContent(element2);
//
//        Element element3 = new Element("CARRIERNAME");
//        element3.setText(carrierName);
//        eleBodyTemp.addContent(element3);
//
//        Element element4 = new Element("LOADPORT");
//        element4.setText(loadPort);
//        eleBodyTemp.addContent(element4);
//
//        Element element5 = new Element("UNLOADPORT");
//        element5.setText(unLoadPort);
//        eleBodyTemp.addContent(element5);
//
//        Element element6 = new Element("CRATENAME");
//        element6.setText(crateName);
//        eleBodyTemp.addContent(element6);
//
//        Element element7 = new Element("PRODUCTRECIPE");
//        element7.setText(productRecipeName);
//        eleBodyTemp.addContent(element7);
//
//        //overwrite
//        doc.getRootElement().addContent(eleBodyTemp);
//
//        return doc;
//    }
//
//    /**
//     * @Name     GlassProcessEnd
//     * @since    2018. 5. 20.
//     * @author
//     * @contents Process for Abort on PU Port of EVAP
//     * @param doc
//     * @throws CustomException
//     */
//    public void GlassProcessEnd(Document doc) throws CustomException
//    {
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
//        String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
//        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
//        String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
//        String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
//        String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
//        String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", false);
//        String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
//        String productRecipe = SMessageUtil.getBodyItemValue(doc, "PRODUCTRECIPE", false);
//
//        List<Element> processEdUnitList = SMessageUtil.getBodySequenceItemList(doc, "PROCESSEDUNITLIST", false);
//
//        //for common
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//
//        //Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
//        MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
//
//        HashMap<String, String> assignCarrierUdfs = new HashMap<String, String>();
//        HashMap<String, String> deassignCarrierUdfs = new HashMap<String, String>();
//
//        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//
//        String lotJudge = "";
//
//        List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotName);
//
//        //refined Lot logged in
//        //Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(lotData);
//
//
//        // Auto-judge
//        // 2018.11.01, hsryu, Delete DecideLotJudge
//        //lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotData, lotJudge, productPGSRCSequence);
//
//        //udfs
//        lotData.getUdfs().put("UNITNAME", "");
//
//        String decideSampleNodeStack = "";
//
//        //TK OUT
//        Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotData, null,
//                                                lotData.getCarrierName() , lotJudge, machineName, "",
//                                                productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, false,null);
//
//        // Repair
//        //afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startAlteration(eventInfo, afterTrackOutLot, afterTrackOutLot.getLotGrade());
//
//        //success then report to FMC
//        try
//        {
//            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
//        }
//        catch(Exception ex)
//        {
//            eventLog.warn("FMC Report Failed!");
//        }
//    }
//    
//    private void sortProcessAbort(Document doc) throws CustomException
//    {
//        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
//        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
//        String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
//        String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", false);
//        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
//        String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
//        String sLotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
//        String sLotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", false);
//        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
//        List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
//        Lot trackOutLot = null;
//        
//        // LotProcessEnd Note
//        StringBuilder note = new StringBuilder("");
//        
//        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
//
//        Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//
//        ///* Array 20180807, Add [Process Flag Update] ==>> */   
//        ////slot map validation
//        //String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
//        ///* <<== Array 20180807, Add [Process Flag Update] */   
//        
//        CommonValidation.CheckDurableHoldState(durableData);
//
//        //String detailSortJobType = ExtendedObjectProxy.getSortJobService().getSortJobType(carrierName);
//        String sortTransferDirection = ExtendedObjectProxy.getSortJobService().getTransferDirection(carrierName);
//
//        /* 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check ==>> */
//        //List<SortJob> sortJobList = null;
//        List<Map<String, Object>> sortJobList = null;
//        String sortJobName = StringUtil.EMPTY;
//        String sortJobType = StringUtil.EMPTY;
//        String detailSortJobType = StringUtil.EMPTY;
//        try
//        {                                                                        
//            //sortJobList = ExtendedObjectProxy.getSortJobService().select(" WHERE CARRIERNAME = ? AND JOBSTATE = ? ", 
//            //        new Object[] {carrierName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED});    
//            sortJobList = ExtendedObjectProxy.getSortJobService().getSortJobNameByCarrierName(carrierName);  
//            if(sortJobList != null && sortJobList.size() > 0)
//            {
//                sortJobName = sortJobList.get(0).get("JOBNAME").toString();
//                sortJobType = sortJobList.get(0).get("JOBTYPE").toString();
//                detailSortJobType = sortJobList.get(0).get("DETAILJOBTYPE").toString();
//            }            
//        }
//        catch (Exception ex)
//        {
//            eventLog.warn(ex.getStackTrace());            
//        }
//        
//        List<SortJobCarrier> sortJobCarrierList = null;
//        String outHoldFlag = GenericServiceProxy.getConstantMap().Flag_N;
//        try
//        {
//            sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(" WHERE JOBNAME = ? AND CARRIERNAME = ? ", 
//                                        new Object[] {sortJobName, carrierName});
//            
//            outHoldFlag = StringUtil.isEmpty(sortJobCarrierList.get(0).getoutholdflag())? GenericServiceProxy.getConstantMap().Flag_N:sortJobCarrierList.get(0).getoutholdflag();
//        }
//        catch (Exception ex)
//        {
//            //eventLog.warn(ex.getStackTrace());
//            sortJobCarrierList = new ArrayList<SortJobCarrier>() ;
//        }               
//        /* <<== 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check */
//        
//        if(StringUtil.isEmpty(detailSortJobType))
//        {
//            throw new CustomException("SORT-0001", "");
//        }
//
//        /* 20180929, hhlee, add, Sort Job TrackIn Validation ==>> */
//        List<Lot> carrierLotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//        if(carrierLotList != null && carrierLotList.size() > 0)
//        {
//            if(StringUtil.equals(carrierLotList.get(0).getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
//            {
//                throw new CustomException("LOT-9003", carrierLotList.get(0).getKey().getLotName() +". Current State is " + carrierLotList.get(0).getLotProcessState());
//            }
//            
//            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
//            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
//            {
//                if(StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
//                {
//                    if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT))
//                    {
//                        eventLog.warn(String.format("DetailJobType Missmatch.[Carriername = %s, OriginalDetailJobType = %s, CurrentDetailJobType = %s]", 
//                                carrierName, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE));  
//                    }
//                    detailSortJobType = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE;
//                }
//                else
//                {
//                }
//            }
//            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
//        }
//        else
//        {
//            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
//            if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_CHANGE))
//            {           
//                if(StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET))
//                {
//                    if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE))
//                    {
//                        eventLog.warn(String.format("DetailJobType Missmatch.[Carriername = %s, OriginalDetailJobType = %s, CurrentDetailJobType = %s]", 
//                                carrierName, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT));  
//                    }
//                    detailSortJobType = GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT;
//                }
//                else
//                {
//                }
//            }
//            /* 20190214, hhlee, modify, add detailjobtype validation ==>> */
//        }
//        /* <<== 20180929, hhlee, add, Sort Job TrackIn Validation */
//        
//        /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//        String sourceLotName = StringUtil.EMPTY;
//        /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//        
//        /* 20181211, hhlee, add, validate product of productElement ==>> */        
//        String notValidateProductName = StringUtil.EMPTY;        
//        /* <<== 20181211, hhlee, add, validate product of productElement */
//        
//        /* 20190321, hhlee, add All Glass Scrap Check validation ==>> */
//        String allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_N;
//        /* <<== 20190321, hhlee, add All Glass Scrap Check validation */
//        
//        if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT))
//        {
//            if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
//            {
//                String lotName = "";
//                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
//                List<ProductP> productPSequence = new ArrayList<ProductP>();
//
//                /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
//                List<Map<String, Object>> sortJobProductList = null;
//                int productCodunt = 0;
//                /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
//                
//                for (Element product : productElement )
//                {
//                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//                    String position = SMessageUtil.getChildText(product, "POSITION", false);
//
//                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                    {
//                        Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//
//                        if(StringUtil.isEmpty(lotName))
//                        {
//                            lotName = productData.getLotName();
//                            
//                            /* 20181207, hhlee, modify bug , change lot Product list ==>> */
//                            /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
//                            //sortJobProductList = ExtendedObjectProxy.getSortJobService().getSortJobProductByJobNameandFromLotName(sortJobName, lotName);
//                            List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
//                            productCodunt = productDataList != null ? productDataList.size() : 0;                           
//                            /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
//                            /* <<== 20181207, hhlee, modify bug , change lot Product list */
//                        }
//
//                        ProductPGSRC productPGSRC = new ProductPGSRC();
//                        productPGSRC.setProductName(productName);
//                        productPGSRC.setPosition(Long.valueOf(position));
//
//                        String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
//                        if (StringUtil.isEmpty(productGrade))
//                        {
//                            productPGSRC.setProductGrade(productData.getProductGrade());                            
//                        }
//                        else
//                        {
//                            productPGSRC.setProductGrade(productGrade);
//                        }
//                        productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
//                        productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
//                        productPGSRC.setReworkFlag("N");
//
//                        productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
//                        Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
//                        productUdfs.put("PROCESSINGINFO", processingInfo);
//                        productPGSRC.setUdfs(productUdfs);
//                        productPGSRCSequence.add(productPGSRC);
//                        
//                        ProductP productPData = new ProductP();
//                        productPData.setProductName(productData.getKey().getProductName());
//                        /* 20180809, Modify, Mismatch Slot Position ==>> */
//                        //productPData.setPosition(productData.getPosition());
//                        productPData.setPosition(Long.valueOf(position));
//                        /* <<== 20180809, Modify, Mismatch Slot Position */
//                        productPSequence.add(productPData);
//                        
//                        /*Get Old ProductFlagData*/
//                        ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get Old ProductFlagData*/
//                        
//                        //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                        
//                        /*Get New ProductFlagData*/
//                        ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                        /*Get New ProductFlagData*/
//                                            
//                        /* Get Note */ 
//                        ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                        /* Get Note */
//                        
//                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                    }
//                }
//
//                if(productPGSRCSequence.size() > 0)
//                {
//                    /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
//                    ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//                    //sourceLotName = lotName;
//                    ///* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */   
//                    /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
//                    
//                    Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//
//                    /* 20181205, hhlee, add, From all product of source cassette to target cassette ==>> */
//                    if(productCodunt == productPGSRCSequence.size())
//                    {
//                        trackOutLot = this.splitGlassByDeassignCarreir(eventInfo, lotData, productPGSRCSequence, lotData.getCarrierName());                     
//                    }
//                    else
//                    {
//                        trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, carrierName);
//                    }
//                    /* <<== 20181205, hhlee, add, From all product of source cassette to target cassette */
//                    
//                    /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
//                    //FutureAction
//                    //this.copyFutureActionCallSplit(lotData, trackOutLot, eventInfo);
//                    MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallSplit(lotData, trackOutLot, eventInfo);
//                    /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
//                    
//                    /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
//                    //Lot Hold Copy(CT_LOTMULTIHOLD)
//                    MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
//                            GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
//                    /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
//                    
//                    if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
//                    {
//                        eventInfo.setEventName("AssignCarrier");
//                        eventInfo.setCheckTimekeyValidation(false);
//                        /* 20181128, hhlee, EventTime Sync */
//                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
//                        assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
//                        assignCarrierInfo.setProductPSequence(productPSequence);
//                        
//                        Map<String, String> assignCarrierUdfs = durableData.getUdfs();
//                        assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
//
//                        Map<String, String> lotUdfs = trackOutLot.getUdfs();
//                        assignCarrierInfo.setUdfs(lotUdfs);
//                        
//                        // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//                        // trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
//                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
//                        
//                        /* 20181209, hhlee modify, add Inquiry lot data ==>> */
//                        trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//                        /* <<== 20181209, hhlee modify, add Inquiry lot data */
//                    }
//                }
//                else  /* <<== hhlee, add, 20180721, else Statemenet */
//                {
//                    try
//                    {
//                        CommonValidation.checkEmptyCst(carrierName); 
//                    }
//                    catch (CustomException ce)
//                    {
//                        List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                        trackOutLot = lotList.get(0);
//                        //List<ProductU> productUSequence =  new ArrayList<ProductU>();                     
//                        //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_END);
//                        //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                        //setEventInfo.setProductUSequence(productUSequence);
//                        //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
//                    }
//                }
//            }
//            else  /* <<== hhlee, add, 20180721, else Statemenet */
//            {
//                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                trackOutLot = lotList.get(0);
//                //List<ProductU> productUSequence =  new ArrayList<ProductU>();             
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_END);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
//            }   
//        }
//        else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE))
//        {
//            if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
//            {
//                List<Product> productList = new ArrayList<Product>();
//                String productNameList = "";
//                String lotName = "";
//                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
//                List<ProductP> productPSequence = new ArrayList<ProductP>();
//
//                /* 20180930, hhlee, add All Glass Scrap Check ==>> */
//                /* 20190321, hhlee, add All Glass Scrap Check validation ==>> */
//                allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_Y;
//                /* <<== 20190321, hhlee, add All Glass Scrap Check validation */
//                /* <<== 20180930, hhlee, add All Glass Scrap Check */
//                
//                for (Element product : productElement )
//                {
//                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//                    String position = SMessageUtil.getChildText(product, "POSITION", false);
//                    String productJudge = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
//
//                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                    {
//                        if(StringUtil.isEmpty(productNameList))
//                            productNameList = "'" + productName + "'";
//                        else    
//                            productNameList += ",'"+ productName + "'" ;
//                        
//                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                    }
//
//                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//                    productData.setPosition(Long.valueOf(position));
//                    if(!StringUtil.isEmpty(productJudge))
//                    {
//                        productData.setProductGrade(productJudge);
//                    }
//                    Map<String,String> udfs = productData.getUdfs();
//                    udfs.put("PROCESSINGINFO", processingInfo);
//                    productData.setUdfs(udfs);
//                    ProductServiceProxy.getProductService().update(productData);
//                    
//                    ProductP productPData = new ProductP();
//                    productPData.setProductName(productData.getKey().getProductName());
//                    /* 20180809, Modify, Mismatch Slot Position ==>> */
//                    //productPData.setPosition(productData.getPosition());
//                    productPData.setPosition(Long.valueOf(position));
//                    /* <<== 20180809, Modify, Mismatch Slot Position */
//                    productPSequence.add(productPData);
//                    
//                    /*Get Old ProductFlagData*/
//                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get Old ProductFlagData*/
//                    
//                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    
//                    /*Get New ProductFlagData*/
//                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get New ProductFlagData*/
//                                        
//                    /* Get Note */ 
//                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                    /* Get Note */
//                    
//                    /* 20180930, hhlee, add All Glass Scrap Check ==>> */
//                    if(!StringUtil.equals(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S) && 
//                            StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    {
//                        allGlasssScrapFlag = GenericServiceProxy.getConstantMap().Flag_N;
//                    }
//                    /* <<== 20180930, hhlee, add All Glass Scrap Check */
//                }
//
//                try
//                {
//                    productList.addAll(ProductServiceProxy.getProductService().select("WHERE PRODUCTNAME IN (" + productNameList + ")" + "ORDER BY LOTNAME" , null));
//                }
//                catch (Exception ex)
//                {
//                    productList = null;
//                }
//
//                Lot targetLot = null;
//                List<Lot> lotList = null;
//                Lot lotData = null;
//                
//                if(productList != null && productList.size() > 0)  /* <<== 20180813, Add, NullValue Error */
//                {
//                    lotName = productList.get(0).getLotName();
//                    
//                    /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
//                    ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//                    //sourceLotName = lotName;
//                    ///* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//                    /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
//                    
//                    // Added by smkang on 2018.12.20 - For synchronization of a carrier.
//                    List<String> sourceCarrierNameList = new ArrayList<String>();
//                    
//                    //new add by wghuang 20180715
//                    for(int i = 0; i < productList.size(); i++ )
//                    {               
//                        if(StringUtil.equals(lotName,  productList.get(i).getLotName()))
//                        {
//                            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//                            
//                            // Added by smkang on 2018.12.20 - For synchronization of a carrier.
//                            if (StringUtils.isNotEmpty(lotData.getCarrierName()) && !sourceCarrierNameList.contains(lotData.getCarrierName()))
//                                sourceCarrierNameList.add(lotData.getCarrierName());
//                            
//                            if(targetLot == null)
//                            {
//                                try
//                                {
//                                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                                }
//                                catch (Exception ex)
//                                {
//                                    lotList = null;
//                                }
//    
//                                if(lotList.size() == 0 )
//                                {
//                                    eventInfo.setEventName("Create");
//                                    targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//                                }
//                                else
//                                {
//                                    targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
//                                }           
//                            }
//                            
//                            ProductPGSRC productPGSRC = new ProductPGSRC();
//                            productPGSRC.setProductName(productList.get(i).getKey().getProductName());
//                            productPGSRC.setPosition(productList.get(i).getPosition());
//                            productPGSRC.setProductGrade(productList.get(i).getProductGrade());
//                            productPGSRC.setSubProductQuantity1(productList.get(i).getSubProductQuantity1());
//                            productPGSRC.setSubProductQuantity2(productList.get(i).getSubProductQuantity2());
//                            productPGSRC.setReworkFlag("N");
//                            productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
//                            productPGSRCSequence.add(productPGSRC);
//                            
//                            if(i == productList.size() -1)
//                            {
//                                trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
//                                /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
//                                //FutureAction
//                                //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                                MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                                /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
//                                
//                                /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
//                                //Lot Hold Copy(CT_LOTMULTIHOLD)
//                                MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
//                                        GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//                                /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
//                            }
//                        }
//                        else
//                        {
//                            trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
//                            lotName = productList.get(i).getLotName();
//                            
//                            /* 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. ==>> */
//                            ///* 2018130, hhlee, add, Split/Merge Source Lot Variable ==>> */
//                            //sourceLotName = sourceLotName + "," +lotName;
//                            ///* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//                            /* <<== 20181110, hhlee, delete, By being been applied to copyLotHoldCallSplitMerge, it deletes. */
//                            
//                            /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
//                            //FutureAction
//                            //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                            MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                            /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
//                            
//                            /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
//                            //Lot Hold Copy(CT_LOTMULTIHOLD)
//                            MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
//                                    GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//                            /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
//                            
//                            //new
//                            productPGSRCSequence = new ArrayList<ProductPGSRC>();
//                            
//                            ProductPGSRC productPGSRC = new ProductPGSRC();
//                            productPGSRC.setProductName(productList.get(i).getKey().getProductName());
//                            productPGSRC.setPosition(productList.get(i).getPosition());
//                            productPGSRC.setProductGrade(productList.get(i).getProductGrade());
//                            productPGSRC.setSubProductQuantity1(productList.get(i).getSubProductQuantity1());
//                            productPGSRC.setSubProductQuantity2(productList.get(i).getSubProductQuantity2());
//                            productPGSRC.setReworkFlag("N");
//                            productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
//                            productPGSRCSequence.add(productPGSRC);
//                            
//                            /* 20190215, hhlee, add, logic bug ==>> */
//                            /* 20180929, hhlee, add Get Lot Data ==>> */
//                            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//                            /* <<== 20180929, hhlee, add Get Lot Data */
//                            /* <<== 20190215, hhlee, add, logic bug */
//                            
//                            if(i == productList.size() - 1)
//                            {
//                                /* 20190215, hhlee, delete, logic bug ==>> */
//                                ///* 20180929, hhlee, add Get Lot Data ==>> */
//                                //lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//                                ///* <<== 20180929, hhlee, add Get Lot Data */
//                                /* <<== 20190215, hhlee, delete, logic bug */
//                                
//                                trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot, allGlasssScrapFlag);
//                                
//                                /* 20180814, Add, Copy Action(FutureAction, FutureSampling) ==>> */
//                                //FutureAction
//                                //this.copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                                MESLotServiceProxy.getLotServiceImpl().copyFutureActionCallMerge(lotData, trackOutLot, eventInfo);
//                                /* <<== 20180814, Add, Copy Action(FutureAction, FutureSampling) */
//                                
//                                /* 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) ==>> */
//                                //Lot Hold Copy(CT_LOTMULTIHOLD)
//                                MESLotServiceProxy.getLotServiceImpl().copyLotHoldCallSplitMerge(lotData, trackOutLot, eventInfo, 
//                                        GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//                                /* <<== 20181027, Add, Copy LotHold(SourceLot : LotHoldCount --> TargeLot) */
//                            }   
//                        }
//                    }
//                    
//    
//                    if(productPGSRCSequence.size() > 0)
//                    {   
//                        if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
//                        {
//                            eventInfo.setEventName("AssignCarrier");
//                            eventInfo.setCheckTimekeyValidation(false);
//                            /* 20181128, hhlee, EventTime Sync */
//                            //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                            
//                            AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
//                            assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
//                            assignCarrierInfo.setProductPSequence(productPSequence);
//                            
//                            Map<String, String> assignCarrierUdfs = durableData.getUdfs();
//                            assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
//    
//                            Map<String, String> lotUdfs = trackOutLot.getUdfs();
//                            assignCarrierInfo.setUdfs(lotUdfs);
//                            
//                            // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//                            // trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
//                            trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
//                            
//                            /* 20181209, hhlee modify, add Inquiry lot data ==>> */
//                            trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//                            /* <<== 20181209, hhlee modify, add Inquiry lot data */
//                        }
//                    }
//                    else  /* <<== hhlee, add, 20180721, else Statemenet */
//                    {
//                        try
//                        {
//                            CommonValidation.checkEmptyCst(carrierName); 
//                        }
//                        catch (CustomException ce)
//                        {
//                            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                            trackOutLot = lotList.get(0);  
//                            //List<ProductU> productUSequence =  new ArrayList<ProductU>();                        
//                            //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                            //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                            //setEventInfo.setProductUSequence(productUSequence);
//                            //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
//                        }
//                    }
//                    
//                    // Added by smkang on 2018.12.20 - For synchronization of a carrier.
//                    for (String sourceCarrierName : sourceCarrierNameList) {
//                        try {
//                            // After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
//                            Durable sourceCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceCarrierName);
//                            if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, sourceCarrierData.getDurableState()))
//                            {
//                                Element bodyElement = new Element(SMessageUtil.Body_Tag);
//                                bodyElement.addContent(new Element("DURABLENAME").setText(sourceCarrierName));
//                                bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
//                                
//                                // EventName will be recorded triggered EventName.
//                                Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
//                                
//                                MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, sourceCarrierName);
//                            }
//                        } catch (Exception e) {
//                            eventLog.warn(e);
//                        }
//                    }
//                }
//                else /* <<== 20180813, Add, NullValue Error */
//                {
//                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                    trackOutLot = lotList.get(0);   
//                }
//            }
//            else  /* <<== hhlee, add, 20180721, else Statemenet */
//            {
//                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//                trackOutLot = lotList.get(0);               
//                //List<ProductU> productUSequence =  new ArrayList<ProductU>();                
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);           
//            }   
//        }
//        else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
//        {
//            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//
//            /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//            sourceLotName = lotList.get(0).getKey().getLotName();
//            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//            
//            List<ProductU> productUSequence = new ArrayList<ProductU>();
//
//            for (Element product : productElement )
//            {
//                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//
//                /* 20180810 , Modify,  TurnOver Flag ==>> */
//                String turnOverFlag = SMessageUtil.getChildText(product, "TURNOVERFLAG", false);
//                /* <<== 20180810 , Modify,  TurnOver Flag */
//                
//                if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                {
//                    /* 20180810 , Modify,  TurnOver Flag ==>> */
//                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                  
//                    ProductU productU = new ProductU();                 
//                    Map<String, String> productUdfs = productData.getUdfs();
//                    
//                    //if(StringUtil.equals(productUdfs.get("TURNOVERFLAG"), GenericServiceProxy.getConstantMap().Flag_N) &&
//                    //        StringUtil.equals(turnOverFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    //{
//                    //    productUdfs.put("TURNOVERFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
//                    //}
//                    //else if(StringUtil.equals(productUdfs.get("TURNOVERFLAG"), GenericServiceProxy.getConstantMap().Flag_Y) &&
//                    //        StringUtil.equals(turnOverFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    //{
//                    //    productUdfs.put("TURNOVERFLAG", GenericServiceProxy.getConstantMap().Flag_N);
//                    //}
//                    //else
//                    //{        
//                    //    productUdfs.put("TURNOVERFLAG", productUdfs.get("TURNOVERFLAG"));
//                    //}
//                    
//                    productU.setUdfs(productUdfs);
//                    /* <<== 20180810 , Modify,  TurnOver Flag */
//                    
//                    productU.setProductName(productName);
//                    productUSequence.add(productU);
//                    
//                    /*Get Old ProductFlagData*/
//                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get Old ProductFlagData*/
//                    
//                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    
//                    /*Get New ProductFlagData*/
//                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get New ProductFlagData*/
//                                        
//                    /* Get Note */ 
//                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                    /* Get Note */
//                    
//                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                }
//            }
//
//            if(productUSequence.size() > 0)
//            {
//                /* 20180928, hhlee, add, Lot Note ==>> */
//                String turnOverProductList = StringUtil.EMPTY;
//                for (ProductU productUData : productUSequence)
//                {
//                    if(StringUtil.isEmpty(turnOverProductList))
//                    {
//                        turnOverProductList = productUData.getProductName();
//                    }
//                    else
//                    {
//                        turnOverProductList = turnOverProductList + " , " +  productUData.getProductName() ;
//                    }
//                }
//                
//                turnOverProductList = "[TurnOver Product] - " + turnOverProductList;
//                
//                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
//                lotUdfs.put("NOTE", turnOverProductList);                 
//                lotList.get(0).setUdfs(lotUdfs);  
//                LotServiceProxy.getLotService().update(lotList.get(0));
//                /* <<== 20180928, hhlee, add, Lot Note */
//                
//                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER);
//                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                /* 20180810 , Add,  ProductQuantity ==>> */
//                setEventInfo.setProductQuantity(productUSequence.size());
//                /* <<== 20180810 , Add,  ProductQuantity */
//                setEventInfo.setProductUSequence(productUSequence);
//                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
//            }
//            else  /* <<== hhlee, add, 20180721, else State */
//            {              
//                trackOutLot = lotList.get(0);
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
//               
//            }
//        }
//        else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
//        {
//            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//
//            /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//            sourceLotName = lotList.get(0).getKey().getLotName();
//            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//            
//            List<ProductU> productUSequence = new ArrayList<ProductU>();
//
//            for (Element product : productElement )
//            {
//                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//                
//                /* 20180810 , Modify,  TurnOver Flag ==>> */
//                String turnSideFlag = SMessageUtil.getChildText(product, "PROCESSTURNFLAG", false);
//                /* <<== 20180810 , Modify,  TurnOver Flag */
//                
//                if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                {
//                    /* 20180810 , Modify,  TurnOver Flag ==>> */
//                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                  
//                    ProductU productU = new ProductU();                 
//                    Map<String, String> productUdfs = productData.getUdfs();
//                    
//                    //if(StringUtil.equals(productUdfs.get("TURNSIDEFLAG"), GenericServiceProxy.getConstantMap().Flag_N) &&
//                    //        StringUtil.equals(turnSideFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    //{
//                    //    productUdfs.put("TURNSIDEFLAG", GenericServiceProxy.getConstantMap().Flag_Y);
//                    //}
//                    //else if(StringUtil.equals(productUdfs.get("TURNSIDEFLAG"), GenericServiceProxy.getConstantMap().Flag_Y) &&
//                    //        StringUtil.equals(turnSideFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                    //{
//                    //    productUdfs.put("TURNSIDEFLAG", GenericServiceProxy.getConstantMap().Flag_N);
//                    //}
//                    //else
//                    //{        
//                    //    productUdfs.put("TURNSIDEFLAG", productUdfs.get("TURNSIDEFLAG"));
//                    //}
//                                       
//                    productU.setUdfs(productUdfs);
//                    /* <<== 20180810 , Modify,  TurnOver Flag */            
//                
//                    productU.setProductName(productName);
//                    productUSequence.add(productU);
//                    
//                    /*Get Old ProductFlagData*/
//                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get Old ProductFlagData*/
//                    
//                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    
//                    /*Get New ProductFlagData*/
//                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get New ProductFlagData*/
//                                        
//                    /* Get Note */ 
//                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                    /* Get Note */
//                    
//                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                }
//            }
//
//            if(productUSequence.size() > 0)
//            {
//                /* 20180928, hhlee, add, Lot Note ==>> */
//                String turnSideProductList = StringUtil.EMPTY;
//                for (ProductU productUData : productUSequence)
//                {
//                    if(StringUtil.isEmpty(turnSideProductList))
//                    {
//                        turnSideProductList = productUData.getProductName();
//                    }
//                    else
//                    {
//                        turnSideProductList = turnSideProductList + " , " +  productUData.getProductName() ;
//                    }
//                }
//                
//                turnSideProductList = "[TurnSide Product] - " + turnSideProductList;
//                
//                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
//                lotUdfs.put("NOTE", turnSideProductList);                 
//                lotList.get(0).setUdfs(lotUdfs); 
//                LotServiceProxy.getLotService().update(lotList.get(0));
//                /* <<== 20180928, hhlee, add, Lot Note */
//                
//                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE);
//                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                /* 20180810 , Add,  ProductQuantity ==>> */
//                setEventInfo.setProductQuantity(productUSequence.size());
//                /* <<== 20180810 , Add,  ProductQuantity */
//                setEventInfo.setProductUSequence(productUSequence);
//                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
//            }
//            else  /* <<== hhlee, add, 20180721, else State */
//            {              
//                trackOutLot = lotList.get(0);
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
//               
//            }
//        }
//        else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
//        {
//            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//
//            /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//            sourceLotName = lotList.get(0).getKey().getLotName();
//            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//            
//            List<ProductU> productUSequence = new ArrayList<ProductU>();
//
//            /* 20180928, hhlee, add, Lot Note ==>> */
//            String SlotMapChangeProductList = StringUtil.EMPTY;
//            /* <<== 20180928, hhlee, add, Lot Note */
//            
//            for (Element product : productElement )
//            {
//                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//                String position = SMessageUtil.getChildText(product, "POSITION", false);
//
//                if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                {
//                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//
//                    /* 20180928, hhlee, add, Lot Note ==>> */
//                    if(StringUtil.isEmpty(SlotMapChangeProductList))
//                    {
//                        SlotMapChangeProductList = productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
//                    }
//                    else
//                    {
//                        SlotMapChangeProductList = SlotMapChangeProductList + " , " +  productData.getKey().getProductName() + " from " + productData.getPosition() + " to " + position ;
//                    }
//                    /* <<== 20180928, hhlee, add, Lot Note */
//                    
//                    productData.setPosition(Long.valueOf(position));
//                    ProductServiceProxy.getProductService().update(productData);
//
//                    ProductU productU = new ProductU();
//                    productU.setProductName(productName);
//                    productUSequence.add(productU);
//                    
//                    /*Get Old ProductFlagData*/
//                    ProductFlag oldProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get Old ProductFlagData*/
//                    
//                    //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//                    
//                    /*Get New ProductFlagData*/
//                    ProductFlag newProductFlagData = ExtendedObjectProxy.getProductFlagService().getProductFlagByElement(product);
//                    /*Get New ProductFlagData*/
//                                        
//                    /* Get Note */ 
//                    ExtendedObjectProxy.getProductFlagService().getNoteChangedFlag(oldProductFlagData, newProductFlagData, note);
//                    /* Get Note */
//                    
//                    /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                    MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                    /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                }
//            }
//
//            if(productUSequence.size() > 0)
//            {
//                /* 20180928, hhlee, add, Lot Note ==>> */
//                SlotMapChangeProductList = "[SlotMapChange Product] - " + SlotMapChangeProductList;
//                
//                Map<String,String> lotUdfs = lotList.get(0).getUdfs();   
//                lotUdfs.put("NOTE", SlotMapChangeProductList);                 
//                lotList.get(0).setUdfs(lotUdfs); 
//                LotServiceProxy.getLotService().update(lotList.get(0));
//                /* <<== 20180928, hhlee, add, Lot Note */
//                
//                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE);
//                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                /* 20180810 , Add,  ProductQuantity ==>> */
//                setEventInfo.setProductQuantity(productUSequence.size());
//                /* <<== 20180810 , Add,  ProductQuantity */
//                setEventInfo.setProductUSequence(productUSequence);
//                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
//            }
//            else  /* <<== hhlee, add, 20180721, else State */
//            {              
//                trackOutLot = lotList.get(0);
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
//               
//            }
//        }
//        else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
//        {
//            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//
//            /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
//            sourceLotName = lotList.get(0).getKey().getLotName();
//            /* <<== 20181030, hhlee, add, Split/Merge Source Lot Variable */
//            
//            /* 20181211, hhlee, add, validate product of productElement ==>> */            
//            notValidateProductName = MESProductServiceProxy.getProductServiceUtil().validateProductElement(productElement);            
//            /* <<== 20181211, hhlee, add, validate product of productElement */
//            
//            List<ProductU> productUSequence = new ArrayList<ProductU>();
//            
//            /* 20180811, Modify , The BC will not send Scraped Glass. So, look for the scraped glass in the MES. ==>> */
//            /* 20180811-01, delete, The BC will not send Scraped Glass. So, look for the scraped glass in the MES. ==>> */
//            //for (Element product : productElement )
//            //{
//            //  String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//            //  String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
//            //  if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//            //  {
//            //      ProductU productU = new ProductU();
//            //      productU.setProductName(productName);
//            //      productUSequence.add(productU);                 
//            //      ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
//            //  }
//            //}
//            /* <<== 20180811-01, delete, The BC will not send Scraped Glass. So, look for the scraped glass in the MES. */
//
//            List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotList.get(0).getKey().getLotName());
//            String processingInfo = StringUtil.EMPTY;
//            String productName = StringUtil.EMPTY;
//            String elementProductName = StringUtil.EMPTY;
//            
//            /* 20181205, hhlee, add, Set productPGSRCSequence for Split ==>> */
//            List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
//            /* <<== 20181205, hhlee, add, Set productPGSRCSequence for Split */
//            
//            /* 20181211, hhlee, add, validate product of productElement ==>> */
//            if(StringUtil.isEmpty(notValidateProductName))
//            {
//                for (Product productData : productDataList )
//                {   
//                    processingInfo = GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P;
//                    productName = productData.getKey().getProductName();
//                    elementProductName = StringUtil.EMPTY;
//                    
//                    for (Element product : productElement )
//                    {
//                        elementProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
//                        
//                        if(StringUtil.equals(productName, elementProductName))
//                        {
//                            processingInfo = GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W;
//                            break;
//                        }    
//                    }
//                                    
//                    if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
//                    {
//                        ProductU productU = new ProductU();
//                        productU.setProductName(productName);
//                        productUSequence.add(productU);     
//                        
//                        /* 20181205, hhlee, add, Set productPGSRCSequence for Split ==>> */
//                        ProductPGSRC productPGSRC = new ProductPGSRC();
//                        
//                        productPGSRC.setProductName(productName);
//                        productPGSRC.setPosition(productData.getPosition());
//                        productPGSRC.setProductGrade(productData.getProductGrade());
//                        productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
//                        productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
//                        productPGSRC.setReworkFlag("N");
//    
//                        productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
//                        Map<String, String> productUdfs = productData.getUdfs();
//                        productUdfs.put("PROCESSINGINFO", processingInfo);
//                        productPGSRC.setUdfs(productUdfs);
//                        productPGSRCSequence.add(productPGSRC);
//                        /* <<== 20181205, hhlee, add, Set productPGSRCSequence for Split */
//                        
//                        /* 20181205, hhlee, add, update SortProductState of SortJobProduct ==>> */
//                        MESLotServiceProxy.getLotServiceImpl().updateSorterJobProduct(eventInfo, sortJobName, productName, GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_COMPLETED);
//                        /* <<== 20181205, hhlee, add, update SortProductState of SortJobProduct */
//                    }
//                }
//                /* <<== 20180811, Modify , The BC will not send Scraped Glass. So, look for the scraped glass in the MES. */
//            }
//            /* <<== 20181211, hhlee, add, validate product of productElement */
//            
//            String scrapFlag = StringUtil.EMPTY;
//            String outStageFlag = StringUtil.EMPTY;
//            
//            /* 20180928, hhlee, add, Lot Note ==>> */
//            String scrapProductList = StringUtil.EMPTY;
//            /* <<== 20180928, hhlee, add, Lot Note */
//            
//            if(productUSequence.size() > 0)
//            {
//                /* 20181121, hhlee, add, Record Scrap Product Lot Note ==>> */
//                for(ProductU productU : productUSequence)
//                {
//                    /* 20180928, hhlee, add, Lot Note ==>> */
//                    if(StringUtil.isEmpty(scrapProductList))
//                    {
//                        scrapProductList = productU.getProductName();
//                    }
//                    else
//                    {
//                        scrapProductList = scrapProductList + " , " +  productU.getProductName() ;
//                    }
//                    /* <<== 20180928, hhlee, add, Lot Note */
//                }
//                /* <<== 20181121, hhlee, add, Record Scrap Product Lot Note */      
//                
//                /* 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check ==>> */
//                try
//                {
//                    List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" WHERE JOBNAME = ? AND PRODUCTNAME = ? ", 
//                            new Object[] {sortJobName, productUSequence.get(0).getProductName()});
//                    scrapFlag = sortJobProductList.get(0).getScrapFlag();
//                    outStageFlag = sortJobProductList.get(0).getOutStageFlag();             
//                }
//                catch (Exception ex)
//                {
//                    eventLog.warn(ex.getStackTrace());
//                }               
//                /* <<== 20180928, Add, Sorter JobName , OutStageFlag, ScrapFlag Check */
//                
//                /* 20180813. Add, trackOutLot Null Value Error ==>> */
//                trackOutLot = lotList.get(0);
//                /* <<== 20180813. Add, trackOutLot Null Value Error */
//                
//                eventInfo.setEventName("DeassignCarrier");
//                eventInfo.setCheckTimekeyValidation(false);
//                /* 20181128, hhlee, EventTime Sync */
//                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                
//                
//                /* 20181205, hhlee, add, add SourceLot Valiable ==>> */
//                Lot sourceLotData = null;
//                /* <<== 20181205, hhlee, add, add SourceLot Valiable */
//                
//                if(lotList.get(0).getProductQuantity() == productUSequence.size())
//                {                   
//                    DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
//                    
//                    // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//                    // trackOutLot = LotServiceProxy.getLotService().deassignCarrier(lotList.get(0).getKey(), eventInfo, deassignCarrierInfo);
//                    trackOutLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotList.get(0), deassignCarrierInfo, eventInfo);
//                    
//                    /* 20181209, hhlee modify, add Inquiry lot data ==>> */
//                    trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//                    /* <<== 20181209, hhlee modify, add Inquiry lot data */
//                }
//                else
//                {
//                    /* 20181205, hhlee, delete, delete deassignCarrier ==>> */
//                    //for(ProductU productU : productUSequence)
//                    //{
//                        ////DeassignLotAndCarrierInfo deassignLotAndCarrierInfo = new DeassignLotAndCarrierInfo();  
//                        ////String productnameU = productU.getProductName();
//                        ////Product productdataU = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productnameU);
//                        ////Map<String,String> productUdfs = productdataU.getUdfs();
//                        ////deassignLotAndCarrierInfo.setUdfs(productUdfs);
//                        ////ProductServiceProxy.getProductService().deassignLotAndCarrier(productdataU.getKey(), eventInfo, deassignLotAndCarrierInfo);
//                        //                      
//                        //String productnameU = productU.getProductName();
//                        //Product productdataU = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productnameU);
//                        //kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo deassignCarrierInfo = new kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo();
//                        //ProductServiceProxy.getProductService().deassignCarrier(productdataU.getKey(), eventInfo, deassignCarrierInfo);
//                        //
//                        //eventInfo.setCheckTimekeyValidation(false);
//                        ///* 20181128, hhlee, EventTime Sync */
//                        ////eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        //                      
//                        ///* 20181121, hhlee, delete, ==>> */
//                        /////* 20180928, hhlee, add, Lot Note ==>> */
//                        ////if(StringUtil.isEmpty(scrapProductList))
//                        ////{
//                        ////    scrapProductList = productU.getProductName();
//                        ////}
//                        ////else
//                        ////{
//                        ////    scrapProductList = scrapProductList + " , " +  productU.getProductName() ;
//                        ////}
//                        ///* <<== 20181121, hhlee, delete, */                      
//                    //}
//                    /* <<== 20181205, hhlee, delete, delete deassignCarrier */
//                    
//                    /* 20181205, hhlee, add, Add  */
//                    sourceLotData = trackOutLot;                                        
//                    /* 20181226, hhlee, modify, modify eventName = (OutStageFlag = Y:OutStageSplit, ScrapFlag = Y: ScrapSplit) ==>> */
//                    //trackOutLot = this.scrapSplitGlass(eventInfo, trackOutLot, productPGSRCSequence, StringUtil.EMPTY);
//                    trackOutLot = this.scrapSplitGlass(eventInfo, trackOutLot, productPGSRCSequence, StringUtil.EMPTY, outStageFlag);
//                    /* <<== 20181226, hhlee, modify, modify eventName = (OutStageFlag = Y:OutStageSplit, ScrapFlag = Y: ScrapSplit) */
//                }
//                
//                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
//                if(StringUtil.equals(outStageFlag,GenericServiceProxy.getConstantMap().Flag_Y))
//                {
//                    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_OUTSTAGE);
//                }               
//                
//                /* 20180928, hhlee, add, Lot Note ==>> */
//                scrapProductList = "[Scrap Product] - " + scrapProductList;
//                
//                Map<String,String> lotUdfs = trackOutLot.getUdfs();   
//                lotUdfs.put("NOTE", scrapProductList);                 
//                trackOutLot.setUdfs(lotUdfs);  
//                /* 20190125, hhlee, bug fix, change lotData : lotList.get(0) --> trackOutLot */
//                LotServiceProxy.getLotService().update(trackOutLot);
//                /* <<== 20180928, hhlee, add, Lot Note */
//                
//                eventInfo.setCheckTimekeyValidation(false);
//                /* 20181128, hhlee, EventTime Sync */
//                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//                MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
//                makeScrappedInfo.setProductQuantity(productUSequence.size());
//                makeScrappedInfo.setProductUSequence(productUSequence);
//                trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);              
//                
//                /* 20181209, hhlee modify, add Inquiry lot data ==>> */
//                trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//                /* <<== 20181209, hhlee modify, add Inquiry lot data */
//                
//                /* 20181205, hhlee, add, add SourceLot Valiable ==>> */
//                if(sourceLotData != null)
//                {
//                    /* 20190114, hhlee, modify, Change Logic SourceLot TrackOut ==>> */
//                    //trackOutLot = sourceLotData;
//                    trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(sourceLotData.getKey().getLotName());
//                    /* <<== 20190114, hhlee, modify, Change Logic SourceLot TrackOut */
//                }
//                /* <<== 20181205, hhlee, add, add SourceLot Valiable */
//                
//                /* 20180811, Add , If the ProductQuantity of lot is zero, change the state of the lot to Emptied. ==>> */
////              if(lotList.get(0).getProductQuantity() == productUSequence.size())
////                {     
////                  eventInfo.setEventName("LotEmptied");
////                  //productUSequence = new ArrayList<ProductU>();
////                  //HashMap<String, String> deassignCarrierUdfs = new HashMap<String, String>();
////                  //MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, trackOutLot, productUSequence, deassignCarrierUdfs);
////                  
////                  trackOutLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Emptied);
////                  //trackOutLot.setLotHoldState(GenericServiceProxy.getConstantMap().Flag_N);
////                  //trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Run);
////                 
////                  Map<String, String> lotDataUdfs = trackOutLot.getUdfs();
////                  LotServiceProxy.getLotService().update(trackOutLot);
////                  
////                  // Set Event
////                  kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                  setEventInfo.setUdfs(lotDataUdfs);
////                  
////                  LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo,setEventInfo);                     
////                }
//                /* <<== 20180811, Add , If the ProductQuantity of lot is zero, change the state of the lot to Emptied. */
//            }
//            else  /* <<== hhlee, add, 20180721, else State */
//            {              
//                trackOutLot = lotList.get(0);
//                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
//                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//                //setEventInfo.setProductUSequence(productUSequence);
//                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
//            }
//        }
//        /* 20181101, hhlee, delete, SCRAPFORAFTER is not used ==>> */
////      else if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAPFORAFTER))
////      {
////          String lotName = "";
////          List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
////          List<ProductU> productUSequence = new ArrayList<ProductU>();
////          List<ProductP> productPSequence = new ArrayList<ProductP>();
////
////          for (Element product : productElement )
////          {
////              String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////              String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////              String position = SMessageUtil.getChildText(product, "POSITION", false);
////
////              if(StringUtil.equals(processingInfo, GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
////              {
////                  Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
////
////                  if(StringUtil.isEmpty(lotName))
////                  {
////                      lotName = productData.getLotName();
////                  }
////
////                  ProductPGSRC productPGSRC = new ProductPGSRC();
////                  productPGSRC.setProductName(productName);
////                  productPGSRC.setPosition(Long.valueOf(position));
////
////                  String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
////                  if (StringUtil.isEmpty(productGrade))
////                  {
////                      productPGSRC.setProductGrade(productData.getProductGrade());
////                  }
////                  else
////                  {
////                      productPGSRC.setProductGrade(productGrade);
////                  }
////
////                  productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
////                  productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
////                  productPGSRC.setReworkFlag("N");
////
////                  productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
////                  Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
////                  productUdfs.put("PROCESSINGINFO", processingInfo);
////                  productPGSRC.setUdfs(productUdfs);
////                  productPGSRCSequence.add(productPGSRC);
////
////                  ProductU productU = new ProductU();
////                  productU.setProductName(productName);
////                  productUSequence.add(productU);
////                  
////                  ProductP productPData = new ProductP();
////                  productPData.setProductName(productData.getKey().getProductName());
////                  /* 20180809, Modify, Mismatch Slot Position ==>> */
////                    //productPData.setPosition(productData.getPosition());
////                    productPData.setPosition(Long.valueOf(position));
////                    /* <<== 20180809, Modify, Mismatch Slot Position */
////                  productPSequence.add(productPData);
////                  
////                  //ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                  ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlagByTurn(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////              }
////          }
////
////          if(productPGSRCSequence.size() > 0)
////          {
////              /* <<== 20180929, hhlee, add, Split/Merge Source Lot Variable */
////                sourceLotName = lotName;
////                /* 20181030, hhlee, add, Split/Merge Source Lot Variable ==>> */
////                
////              Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////              trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, carrierName);
////              
////              if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
////              {
////                  eventInfo.setEventName("AssignCarrier");
////                  eventInfo.setCheckTimekeyValidation(false);
////                  eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                  eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                  
////                  AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
////                  assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
////                  assignCarrierInfo.setProductPSequence(productPSequence);
////                  
////                  Map<String, String> assignCarrierUdfs = durableData.getUdfs();
////                  assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
////
////                  Map<String, String> lotUdfs = trackOutLot.getUdfs();
////                  assignCarrierInfo.setUdfs(lotUdfs);
////                  
////                  // Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//////                    trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
////                  trackOutLot = MESLotServiceProxy.getLotServiceImpl().assignCarrier(trackOutLot, assignCarrierInfo, eventInfo);
////              }
////
////              eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
////              eventInfo.setCheckTimekeyValidation(false);
////              eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////              eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////
////              MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
////              makeScrappedInfo.setProductQuantity(productUSequence.size());
////              makeScrappedInfo.setProductUSequence(productUSequence);
////              trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);
////          }
////          else  /* <<== hhlee, add, 20180721, else State */
////            {     
////              List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////              trackOutLot = lotList.get(0);
////              //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
////            }
////      }
//        /* <<== 20181101, hhlee, delete, SCRAPFORAFTER is not used */
//
//        if (trackOutLot != null)
//        {
//            trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//            
//            if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Scrapped))
//            {
//                //20180811
//                /* TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
//            }
//            else if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)) 
//            {
//                /* 20180811, Delete, TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. ==>> */
////                String[] nodeStackList = null;
////    
////              nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
////    
////              if(nodeStackList.length > 0)
////              {
////                  Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);
////    
////                  /* 20180811, Add, Change LotProcessState ==>> */
////                    /* TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
////                    String beforeLotState = trackOutLot.getLotState();
////                    
////                    trackOutLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
////                    trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
////                    trackOutLot.setLotHoldState(GenericServiceProxy.getConstantMap().Flag_N);  
////                    LotServiceProxy.getLotService().update(trackOutLot);
////                    //* <<== 20180811, Add, Change LotProcessState */
////                    
////                  /* 20180731, Add, Change LotProcessState ==>> */
////                    trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
////                    /* <<== 20180731, Add, Change LotProcessState */
////                    
////                  ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
////                          trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
////                          "", "", trackOutLot.getProductRequestName(), trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
////                          trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
////                          trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
////                          nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
////                          "", "", "", "", "", trackOutLot.getUdfs(), null, false);
////    
////                  eventInfo.setEventName("TrackOut");
////                  trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
////                  
////                  /* 20180811, Add, Change LotHoldState, LotProcessState ==>> */
////                    /* TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
////                    trackOutLot.setLotState(beforeLotState);
////                    trackOutLot.setLotProcessState(StringUtil.EMPTY);
////                    trackOutLot.setLotHoldState(StringUtil.EMPTY);
////                    LotServiceProxy.getLotService().update(trackOutLot);
////                    
////                    String pCondition = " where lotname=?" + " and timekey=(select max(timekey) from lothistory where lotname=? and eventname = ? ) " ;
////                    Object[] pBindSet = new Object[]{trackOutLot.getKey().getLotName(),trackOutLot.getKey().getLotName(),"TrackOut"};
////                    
////                    List<LotHistory> pArrayList = LotServiceProxy.getLotHistoryService().select(pCondition, pBindSet);
////                    LotHistory lothistory = pArrayList.get(0);
////                    lothistory.setLotState(beforeLotState);
////                    lothistory.setLotProcessState(StringUtil.EMPTY);
////                    lothistory.setLotHoldState(StringUtil.EMPTY);
////                    LotServiceProxy.getLotHistoryService().update(lothistory);
////                    /* <<== 20180811, Add, Change LotProcessState */ 
////                    
////                  if(!StringUtil.isEmpty(trackOutLot.getCarrierName()))
////                  {
////                      eventInfo.setEventName("DeassignCarrier");
////                      eventInfo.setCheckTimekeyValidation(false);
////                      eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                      eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                      
////                      Durable deassignCarrier = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trackOutLot.getCarrierName());
////                      
////                      SetEventInfo setEventInfo = new SetEventInfo();
////                      DurableServiceProxy.getDurableService().setEvent(deassignCarrier.getKey(), eventInfo, setEventInfo);
////                      
////                      kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                      LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo1);
////                      
////                      trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
////                      trackOutLot.setCarrierName("");
////                      LotServiceProxy.getLotService().update(trackOutLot);
////                      
////                      LotHistory lotHData = MESLotServiceProxy.getLotInfoUtil().getLotHistory(trackOutLot.getKey().getLotName());
////                      lotHData.setCarrierName("");
////                      LotServiceProxy.getLotHistoryService().update(lotHData);
////                  }
////              }
//                /* <<== 20180811, Delete, TrackOut/ChangeSpec cannot be performed if the LotState is Scraped and Empted. */
//            }
//            else
//            {
//                /* 20190321, hhlee, add All Glass Scrap Check validation */
//                if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                {
//                    eventInfo.setEventName("TrackOut");
//                    eventInfo.setEventComment(ExtendedObjectProxy.getSortJobService().setSortEventComment(carrierName, sortJobName, sortJobType,
//                            sortTransferDirection, eventInfo.getEventName(), GenericServiceProxy.getConstantMap().Flag_N));
//                    
//                    MESProductServiceProxy.getProductServiceImpl().productStateUpdateByAllGlasssScrap(eventInfo, trackOutLot);  
//                    trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateByAllGlasssScrap(eventInfo, trackOutLot);
//                }
//                else
//                {
//                    String[] nodeStackList = null;
//        
//                    /* 20190105, hhlee, modify, add logic ==>> */
//                    //nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
//                    String tempNodeStack =  StringUtil.reverse(StringUtil.substring(StringUtil.reverse(trackOutLot.getNodeStack()), StringUtil.indexOf(StringUtil.reverse(trackOutLot.getNodeStack()), ".") + 1));
//                    nodeStackList = StringUtil.split(tempNodeStack, '.');
//                    /* <<== 20190104, hhlee, modify, add logic */
//        
//                    if(nodeStackList.length > 0)
//                    {
//                        List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setAllProductUdfs(trackOutLot.getKey().getLotName());
//        
//                        /* 20190328, hhlee, delete, TrackOut ProcessingInfo Record */
//                        //productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
//        
//                        /* 20190105, hhlee, modify, add logic ==>> */
//                        //Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);  
//                        Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[nodeStackList.length - 1]);    
//                        /* <<== 20190105, hhlee, modify, add logic */
//                        
//                        /* 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = "" ==>> */
//                        String returnProcessOperationName = nodeData.getNodeAttribute1();
//                        String returnProcessOperationVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
//                        
//                        /* 20190105, hhlee, modify, change logic ==>> */
//                        Node nextNode = null;
//                        try
//                        {
//                            //Node nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(nodeStackList[0], "Normal", "");
//                            nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(nodeStackList[nodeStackList.length - 1], "Normal", "");
//                        }
//                        catch(NotFoundSignal ns)
//                        {
//                            eventLog.warn(String.format("NextNode data not found.[nodeID:%s, arcType:%s, arcAttribute:%s]", nodeStackList[nodeStackList.length - 1], "Normal", ""));
//                        }
//                        /* <<== 20190105, hhlee, modify, change logic */
//                        
//                        /* 20181120, hhlee, delete, After TrackOut Update ==>> */
//                        //if(StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End) &&
//                        //        StringUtil.isEmpty(nextNode.getNodeAttribute1()))
//                        //{
//                        //    trackOutLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
//                        //    returnProcessOperationName = "-";
//                        //    returnProcessOperationVersion = StringUtil.EMPTY;
//                        //}
//                        /* <<== 20181120, hhlee, delete, After TrackOut Update */
//                        /* <<== 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  */
//                        
//                        /* 20190328, hhlee, delete, Change Function ==>> */
//                        ///* 20180731, Add, Change LotProcessState ==>> */
//                        //trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
//                        //
//                        ///* 20181116, hhlee, add, add setLastLoggedOutTime(Requested by Report) ==>> */
//                        //trackOutLot.setLastLoggedOutTime(eventInfo.getEventTime());
//                        ///* <<== 20181116, hhlee, add, add setLastLoggedOutTime(Requested by Report) */
//                        //
//                        ///* 20181001, hhlee, add, Lot Note ==>> */                         
//                        //Map<String,String> lotUdfs = trackOutLot.getUdfs();   
//                        //lotUdfs.put("NOTE", "");                 
//                        //trackOutLot.setUdfs(lotUdfs);
//                        //LotServiceProxy.getLotService().update(trackOutLot);
//                        ///* <<== 20181001, hhlee, add, Lot Note */
//                        ///* <<== 20180731, Add, Change LotProcessState */
//                        /* <<== 20190328, hhlee, delete, Change Function */
//                        
//                        /* 20190402, hhlee, add, Modify, change logic location ==>> */
//                        String beforeProcessFlowName = trackOutLot.getProcessFlowName();
//                        String beforeProcessOperationName = trackOutLot.getProcessOperationName();
//                        /* 20190329, hhlee, add, endbank check validation */
//                        String beforeProcessOperationNameByExtObj = CommonUtil.getValue(trackOutLot.getUdfs(), "BEFOREOPERATIONNAME");
//                        String beforeNodeStack = trackOutLot.getNodeStack();
//                        /* <<== 20190402, hhlee, add, Modify, change logic location */
//                        
//                        /* 20190328, hhlee, add, lotData Update ==>> */
//                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateBychangeProcessOperation(eventInfo, trackOutLot);
//                        /* <<== 20190328, hhlee, add, lotData Update */
//                        
//                        /* 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  ==>> */
//                        //ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
//                        //      trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
//                        //      "", "", trackOutLot.getProductRequestName(), trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
//                        //      trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
//                        //      trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
//                        //      nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
//                        //      "", "", "", "", "", trackOutLot.getUdfs(), productUdfs, false);
//                        
//                        //2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
//                        ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
//                                trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
//                                "", "", "", trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
//                                trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
//                                trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
//                                nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), returnProcessOperationName, returnProcessOperationVersion,
//                                "", "", "", "", "", trackOutLot.getUdfs(), productUdfs, false);
//                        /* <<== 20181023, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  */
//                        
//                        /* 20190402, hhlee, add, delete, change logic location ==>> */
//                        //String beforeProcessFlowName = trackOutLot.getProcessFlowName();
//                        //String beforeProcessOperationName = trackOutLot.getProcessOperationName();
//                        ///* 20190329, hhlee, add, endbank check validation */
//                        //String beforeProcessOperationNameByExtObj = CommonUtil.getValue(trackOutLot.getUdfs(), "BEFOREOPERATIONNAME");
//                        //String beforeNodeStack = trackOutLot.getNodeStack();
//                        /* <<== 20190402, hhlee, add, delete, change logic location */
//                        
//                        /* 20190104, hhlee, add, change node stack ==>> */
//                        /* 20190320, hhlee, add, change node stack ==>> */
//                        changeSpecInfo.setNodeStack(tempNodeStack);
//                        /* <<== 20190320, hhlee, add, change node stack */
//                        /* <<== 20190104, hhlee, add, change node stack */
//                        
//                        String trackOutNote;
//                        if(note.length()>3500){
//                            trackOutNote=note.substring(3499);
//                        }else{
//                            trackOutNote= note.toString();
//                        }
//                        
//                        // 2019.06.04_hsryu_Delete Logic. Not Memory TrackOut Note. Memory New Event. 
//                        //changeSpecInfo.getUdfs().put("NOTE", trackOutNote);
//                        
//                        eventInfo.setEventName("TrackOut");
//                        eventInfo.setEventComment(ExtendedObjectProxy.getSortJobService().setSortEventComment(carrierName, sortJobName, sortJobType, 
//                                sortTransferDirection, eventInfo.getEventName(), GenericServiceProxy.getConstantMap().Flag_N));
//                        
//                        trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
//                        trackOutLot.getUdfs().put("NOTE", "");
//                        LotServiceProxy.getLotService().update(trackOutLot);
//                        trackOutLot = LotServiceProxy.getLotService().selectByKey(trackOutLot.getKey());
//                        
//                        /* 20190328, hhlee, add, lot and product state update ==>> */
//                        // 20190328, hhlee, delete, function change(lotAndProductStateUpdateAfterTrackOut)  
//                        //// 2910.03.01
//                        //trackOutLot.setMachineName("");
//                        //LotServiceProxy.getLotService().update(trackOutLot);
//                        
//                        trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotAndProductStateUpdateAfterTrackOut(eventInfo, trackOutLot);
//                        /* <<== 20190328, hhlee, add, lot and product state update */
//                        
//                        ///* Array 20180807, Add [Process Flag Update] ==>> */            
//                        //MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, trackOutLot, logicalSlotMap);
//                        ///* <<== Array 20180807, Add [Process Flag Update] */
//                        
//                        /* 20190105, hhlee, modify, change logic ==>> */
//                        if(nextNode != null)
//                        {
//                            /* 20181120, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = "" ==>> */
//                            /* 20190329, hhlee, add, endbank check validation */
//                            if((StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End) && StringUtil.isEmpty(nextNode.getNodeAttribute1()) &&  
//                                    StringUtil.isNotEmpty(CommonUtil.getValue(trackOutLot.getUdfs(), "ENDBANK"))) || 
//                                    StringUtil.equals(beforeProcessOperationNameByExtObj, GenericServiceProxy.getConstantMap().PROCESSOPERATIONNAME_BAR))
//                            {
//                                /* 20190212, hhlee, delete, change logic ==>> */
//                                //trackOutLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
//                                ////returnProcessOperationName = "-";
//                                ////returnProcessOperationVersion = StringUtil.EMPTY;
//                                //trackOutLot.setProcessOperationName("-");
//                                //trackOutLot.setProcessOperationVersion(StringUtil.EMPTY);    
//                                ///* 20190122, hhlee, add, LotProcessState = StringUtil.EMPTY */
//                                //trackOutLot.setLotProcessState(StringUtil.EMPTY); 
//                                //LotServiceProxy.getLotService().update(trackOutLot);
//                                //
//                                //String pCondition = " where lotname=?" + " and timekey=(select max(timekey) from lothistory where lotname=? and eventname = ? ) " ;
//                                //Object[] pBindSet = new Object[]{trackOutLot.getKey().getLotName(),trackOutLot.getKey().getLotName(),"TrackOut"};
//                                //List<LotHistory> pArrayList = LotServiceProxy.getLotHistoryService().select(pCondition, pBindSet);
//                                //LotHistory Lothistory = pArrayList.get(0);
//                                //Lothistory.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
//                                //Lothistory.setProcessOperationName("-");
//                                //Lothistory.setProcessOperationVersion(StringUtil.EMPTY);
//                                ///* 20190122, hhlee, add, LotProcessState = StringUtil.EMPTY */
//                                //Lothistory.setLotProcessState(StringUtil.EMPTY); 
//                                //LotServiceProxy.getLotHistoryService().update(Lothistory);
//                                /* <<== 20190212, hhlee, delete, change logic */
//                                
//                                /* 20190212, hhlee, add, lot state update(LotState, ProcessOperationName, ProcessOperationVersion, LotProcessState) ==>> */
//                                trackOutLot = MESLotServiceProxy.getLotServiceImpl().lotStateUpdateByLotStateCompleted(eventInfo, trackOutLot);
//                                /* <<== 20190212, hhlee, add, lot state update(LotState, ProcessOperationName, ProcessOperationVersion, LotProcessState) */
//                                
//                                /* 20190212, hhlee, add, Product state update(ProductState, ProcessOperationName, ProcessOperationVersion, ProductProcessState) ==>> */
//                                MESProductServiceProxy.getProductServiceImpl().productStateUpdateByLotStateCompleted(eventInfo, trackOutLot);  
//                                /* <<== 20190212, hhlee, add, Product state update(ProductState, ProcessOperationName, ProcessOperationVersion, ProductProcessState) */
//                            }
//                            /* <<== 20181120, hhlee, add, When EndBank Sort, LotState = 'Completed' and ProcessOperationName = '-' and ProcessOperationVersion = ""  */
//                        }
//                        /* <<== 20190105, hhlee, modify, change logic */
//                        
//                        /* 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) ==>> */
//                        if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//                        {                        
//                            /* 20190212, hhlee, delete, change logic ==>> */
//                            ///* 20180810, Add, Update Product - ProductProcessState[Processing -> Idle] ==>> */
//                            //List<Product> productList = null;
//                            //try
//                            //{
//                            //    productList = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
//                            //}
//                            //catch (Exception ex)
//                            //{
//                            //    //throw new CustomException("SYS-9999", "Product", "No Product to process");
//                            //    eventLog.error("[SYS-9999] No Product to process");   
//                            //}
//                            //
//                            //for (Product productData : productList)
//                            //{   
//                            //    productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
//                            //    ProductServiceProxy.getProductService().update(productData);
//                            //    
//                            //    String pCondition = " where productname=?" + " and timekey=(select max(timekey) from producthistory where productname=? and eventname = ? ) " ;
//                            //    Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName(),"TrackOut"};
//                            //    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//                            //    ProductHistory producthistory = pArrayList.get(0);
//                            //    producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
//                            //    ProductServiceProxy.getProductHistoryService().update(producthistory);
//                            //}
//                            /* <<== 20190212, hhlee, delete, change logic */
//                            
//                            /* 20190328, hhlee, delete, function change(lotAndProductStateUpdateAfterTrackOut) ==>> */
//                            ///* 20190212, hhlee, add, Product state update(ProductProcessState) ==>> */
//                            //MESProductServiceProxy.getProductServiceImpl().productStateUpdateBySortEnd(eventInfo, trackOutLot);  
//                            ///* <<== 20190212, hhlee, add, Product state update(ProductProcessState) */
//                            /* <<== 20190328, hhlee, delete, function change(lotAndProductStateUpdateAfterTrackOut) */
//                            
//                        }
//                        /* <<== 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) */
//                        
//                        /* 20190212, hhlee, delete, change logic(unnessesary logic if statement) ==>> */
//                        //if(!StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
//                        //{
//                        //if(!StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
//                        //{
//                        //eventInfo.setEventName("Hold");
//                        //eventInfo.setEventComment("Sort Lot Process Abort Hold.");
//                        //eventInfo.setReasonCodeType("HoldLot");
//                        //eventInfo.setReasonCode("LAHL");                        
//                        //eventInfo.setCheckTimekeyValidation(false);
//                        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                        //2019.02.13_hsryu_Insert Logic. 
//                        boolean aHoldFlag = false;
//                        
//                        /* 20181027, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot) ==>> */                
//                        /* 20181127, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot), Hold EventName Change(sortJobType -> "Hold") ==>> */
//                        //MESLotServiceProxy.getLotServiceImpl().sorterAfterHoldBySourceLot(eventInfo, trackOutLot, sortJobType, sourceLotName);
//                        aHoldFlag = MESLotServiceProxy.getLotServiceImpl().sorterAfterHoldBySourceLot(eventInfo, trackOutLot, "Hold", sourceLotName);
//                        /* <<== 20181127, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot), Hold EventName Change(sortJobType -> "Hold") */
//                        /* <<== 20181027, Add, LotHold(SoureLot(HoldLot), Target(Copy HoldLot) */                       
//                        
//                        /* 20180928, Add, OutHoldFlag -> HOLD ==>> */
//                        if(StringUtil.equals(outHoldFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                        {
//                            aHoldFlag = true;
//                            eventInfo.setEventName("SorterOutHold");
//                            eventInfo.setEventComment("Changer has been Finished Please Check !");
//                            /* 20190424, hhlee, modify, changed function ==>> */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH",""); 
//                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, "", "");
//                            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, false, "", "");
//                            /* <<== 20190424, hhlee, modify, changed function */
//                        }                
//                        /* <<== 20180928, Add, OutHoldFlag -> HOLD */  
//                        
//                        //List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(trackOutLot);
//                        //MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
//                        //LotServiceProxy.getLotService().makeOnHold(trackOutLot.getKey(), eventInfo, makeOnHoldInfo);
//                        /* 20190212, hhlee, add, lot state update(ProductProcessState, add if statement) ==>> */
//                        if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//                        {
//                            aHoldFlag = true;
//                            eventInfo.setEventName("Hold");
//                            eventInfo.setEventComment("Sort Lot Process Abort Hold.");
//                            /* 20190424, hhlee, modify, changed function ==>> */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL",""); 
//                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, "", "");
//                            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","LAHL","", true, false, "", "");
//                            /* <<== 20190424, hhlee, modify, changed function */
//                        }
//                        //}
//                        
//                        /* 20181211, hhlee, add, validate product of productElement ==>> */
//                        if(StringUtil.isNotEmpty(notValidateProductName))
//                        {
//                            aHoldFlag = true;
//                            eventInfo.setEventName("SorterAbnormalEndHold");
//                            eventInfo.setEventComment("Scrap has been Finished Please Check![Receive ProductName : " + notValidateProductName + "]");
//                            /* 20190424, hhlee, modify, changed function ==>> */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot", "STOH", "", "MFG");  
//                            /* 20190426, hhlee, modify, add variable(setFutureHold) */
//                            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, "", "MFG");
//                            MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","STOH","", true, false, "", "MFG");
//                            /* <<== 20190424, hhlee, modify, changed function */
//                        }
//                        /* <<== 20181211, hhlee, add, validate product of productElement */
//                        
//                        /* 2019.02.13_hsryu_Insert TrackOut Priority Logic */
//                        trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
//                        
//                        if(!StringUtils.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
//                        {
//                            if(!aHoldFlag)
//                            aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);
//    
//                            if(aHoldFlag)
//                            {
//                                trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionBySystemHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);
//                                trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionHold(trackOutLot.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo, GenericServiceProxy.getConstantMap().HOLDTYPE_AHOLD);
//                            }
//                            else
//                            {
//                                // 2019.05.30_Delete Logic. 
////                              if(!MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(),eventInfo))
////                              {
////                                  if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo))
////                                  {
////                                      //Reserve Change
////                                      trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
////                                  }
////                              }
//                                
//                                // 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
//                                boolean isCheckSampling = false;
//                                isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(trackOutLot.getKey().getLotName(), eventInfo);
//                                
//                                if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(trackOutLot.getKey().getLotName(), eventInfo) && !isCheckSampling ){
//                                    trackOutLot = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(trackOutLot.getKey().getLotName(), eventInfo);
//                                }
//                            }
//                        }
//                        
//                        /* 20190508, hhlee, modify, MQC Job for Scrap ==>> */
//                        if(StringUtil.equals(detailSortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
//                        {
//                            try
//                            {
//                                MESLotServiceProxy.getLotServiceUtil().checkMQCFlowAfter(trackOutLot, carrierName, eventInfo);
//                            }
//                            catch (Exception ex)
//                            {
//                                eventLog.error(ex.getMessage());
//                            }
//                        }
//                        /* <<== 20190508, hhlee, modify, MQC Job for Scrap */
//                        
//                        // 2019.06.03_hsryu_Memory Note with UpdateProductFlag. Mantis 0003934.
//                        trackOutLot = MESLotServiceProxy.getLotServiceUtil().MomeryNoteUpdateProductFlag(trackOutLot, note.toString(), eventInfo);
//
//                    }
//                }
//            }
//            
//            /* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
//            try
//            {
//                SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sortJobName, carrierName});
//                sortJobCarrier.settrackflag(GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT);
//                ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
//            }
//            catch (Exception ex)
//            {
//                eventLog.error(ex.getMessage());
//            }
//            /* 20180929, Add, SortJobCarrier trackflag update(Requested by shkim) ==>> */
//            
////          /* 20180810, Add, Update Product - ProductProcessState[Processing -> Idle] ==>> */
////            List<Product> productList = null;
////            try
////            {
////                productList = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
////            }
////            catch (Exception ex)
////            {
////                //throw new CustomException("SYS-9999", "Product", "No Product to process");
////                eventLog.error("[SYS-9999] No Product to process");   
////            }
////            
////            for (Product productData : productList)
////            {   
////                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
////                ProductServiceProxy.getProductService().update(productData);
////                
////                String pCondition = " where productname=?" + " and timekey=(select max(timekey) from producthistory where productname=? and eventname = ? ) " ;
////                Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName(),"TrackOut"};
////                List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
////                ProductHistory producthistory = pArrayList.get(0);
////                producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
////                ProductServiceProxy.getProductHistoryService().update(producthistory);                
////            }    
////            /* <<== 20180810, Add, Update Product - ProductProcessState[Processing -> Idle] */
//        }
//        /* 20181205, hhlee, add, From all product of source cassette to target cassette */
//        else
//        {
//            if(productElement.size() <= 0 )  
//            {
//                List<Product> productDataList = new ArrayList<Product>();
//                
//                try 
//                {
//                    productDataList = ProductServiceProxy.getProductService().select("CARRIERNAME = ?", new Object[] {carrierName});
//                } 
//                catch (Exception e) 
//                {
//                   eventLog.warn(e);
//                }
//                
//                if(productElement.size() == 0 && productDataList.size() == 0) 
//                {
//                    try
//                    {
//                        SortJobCarrier sortJobCarrier = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] {sortJobName, carrierName});
//                        sortJobCarrier.settrackflag(GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT);
//                        ExtendedObjectProxy.getSortJobCarrierService().modify(eventInfo, sortJobCarrier);
//                    }
//                    catch (Exception ex)
//                    {
//                        eventLog.error(ex.getMessage());
//                    }         
//                }
//            }
//        }
//        
//        /* 20190102, hhlee, add, Add Logic SorterJobAbort Validation ==>> */
//        try
//        {
//            sortJobCarrierList = null;
//            sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().select(" WHERE JOBNAME = ? AND TRACKFLAG <> ? ", 
//                    new Object[] {sortJobName, GenericServiceProxy.getConstantMap().SORT_JOBCARRIER_TRACKFLAG_OUT});            
//        }
//        catch (Exception ex)
//        {
//            eventLog.warn(String.format("This sorterjob[%s] is not aborted.!", sortJobName));
//        } 
//        
//        try
//        {
//            if(sortJobCarrierList == null || sortJobCarrierList.size() <= 0)
//            {
//                SortJob sortJob = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] {sortJobName});
//                
//                sortJob.setJobState(GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ABORT);
//                sortJob.setEventComment(eventInfo.getEventComment());
//                sortJob.setEventName(eventInfo.getEventName());
//                sortJob.setEventTime(eventInfo.getEventTime());
//                sortJob.setEventUser(eventInfo.getEventUser());
//                
//                ExtendedObjectProxy.getSortJobService().modify(eventInfo, sortJob);
//            }   
//        }
//         catch (Exception ex)
//        {
//            eventLog.warn(String.format("This sorterjob[%s] is update error.!", sortJobName));
//        } 
//        /* <<== 20190102, hhlee, add, Add Logic SorterJobAbort Validation */
//    }
//    
////  private void sortProcessAbort(Document doc) throws CustomException
////    {
////        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
////        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
////        String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
////        String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", false);
////        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
////        String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
////        String sLotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
////        String sLotSel = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", false);
////        String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
////        List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
////        Lot trackOutLot = null;
////
////        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
////
////        Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
////
////        ///* Array 20180807, Add [Process Flag Update] ==>> */   
////        ////slot map validation
////        //String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(durableData);
////        ///* <<== Array 20180807, Add [Process Flag Update] */   
////        
////        CommonValidation.CheckDurableHoldState(durableData);
////
////        String sortJobType = ExtendedObjectProxy.getSortJobService().getSortJobType(carrierName);
////        String sortTransferDirection = ExtendedObjectProxy.getSortJobService().getTransferDirection(carrierName);
////
////        if(StringUtil.isEmpty(sortJobType))
////        {
////            throw new CustomException("SORT-0001", "");
////        }
////
////        if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT))
////        {
////            if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
////            {
////                String lotName = "";
////                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
////                List<ProductP> productPSequence = new ArrayList<ProductP>();
////
////                for (Element product : productElement )
////                {
////                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////                    String position = SMessageUtil.getChildText(product, "POSITION", false);
////
////                    if(StringUtil.equals(processingInfo, "P"))
////                    {
////                        Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
////
////                        if(StringUtil.isEmpty(lotName))
////                        {
////                            lotName = productData.getLotName();
////                        }
////
////                        ProductPGSRC productPGSRC = new ProductPGSRC();
////                        productPGSRC.setProductName(productName);
////                        productPGSRC.setPosition(Long.valueOf(position));
////
////                        String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
////                        if (StringUtil.isEmpty(productGrade))
////                        {
////                            productPGSRC.setProductGrade(productData.getProductGrade());
////                        }
////                        else
////                        {
////                            productPGSRC.setProductGrade(productGrade);
////                        }
////                        productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
////                        productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
////                        productPGSRC.setReworkFlag("N");
////
////                        productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
////                        Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
////                        productUdfs.put("PROCESSINGINFO", processingInfo);
////                        productPGSRC.setUdfs(productUdfs);
////                        productPGSRCSequence.add(productPGSRC);
////                        
////                        ProductP productPData = new ProductP();
////                        productPData.setProductName(productData.getKey().getProductName());
////                        /* 20180809, Modify, Mismatch Slot Position ==>> */
////                        //productPData.setPosition(productData.getPosition());
////                        productPData.setPosition(Long.valueOf(position));
////                        /* <<== 20180809, Modify, Mismatch Slot Position */
////                        productPSequence.add(productPData);
////                        
////                        ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                    }
////                }
////
////                if(productPGSRCSequence.size() > 0)
////                {
////                    Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////                    trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, carrierName);
////                    
////                    if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
////                    {
////                        eventInfo.setEventName("AssignCarrier");
////                        eventInfo.setCheckTimekeyValidation(false);
////                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                        
////                        AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
////                        assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
////                        assignCarrierInfo.setProductPSequence(productPSequence);
////                        
////                        Map<String, String> assignCarrierUdfs = durableData.getUdfs();
////                        assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
////
////                        Map<String, String> lotUdfs = lotData.getUdfs();
////                        assignCarrierInfo.setUdfs(lotUdfs);
////                        
////                        trackOutLot = LotServiceProxy.getLotService().assignCarrier(lotData.getKey(), eventInfo, assignCarrierInfo);
////                    }
////                }
////                else  /* <<== hhlee, add, 20180721, else State */
////                {
////                    try
////                    {
////                        CommonValidation.checkEmptyCst(carrierName); 
////                    }
////                    catch (CustomException ce)
////                    {
////                        List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                        trackOutLot = lotList.get(0);
////                        //List<ProductU> productUSequence =  new ArrayList<ProductU>();                        
////                        //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                        //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                        //setEventInfo.setProductUSequence(productUSequence);
////                        //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
////                    }
////                }                   
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {
////                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                trackOutLot = lotList.get(0);
////                //List<ProductU> productUSequence =  new ArrayList<ProductU>();                
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE))
////        {
////            if(!StringUtil.equals(sortTransferDirection, GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE))
////            {
////                List<Product> productList = new ArrayList<Product>();
////                String productNameList = "";
////                String lotName = "";
////                List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
////                List<ProductP> productPSequence = new ArrayList<ProductP>();
////
////                for (Element product : productElement )
////                {
////                    String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                    String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////                    String position = SMessageUtil.getChildText(product, "POSITION", false);
////                    String productJudge = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
////
////                    if(StringUtil.equals(processingInfo, "P"))
////                    {
////                        if(StringUtil.isEmpty(productNameList))
////                            productNameList = "'" + productName + "'";
////                        else    
////                            productNameList += ",'"+ productName + "'" ;
////                    }
////
////                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
////                    productData.setPosition(Long.valueOf(position));
////                    if(!StringUtil.isEmpty(productJudge))
////                    {
////                        productData.setProductGrade(productJudge);
////                    }
////                    
////                    Map<String,String> udfs = productData.getUdfs();
////                    udfs.put("PROCESSINGINFO", processingInfo);
////                    productData.setUdfs(udfs);
////                    ProductServiceProxy.getProductService().update(productData);
////                    
////                    ProductP productPData = new ProductP();
////                    productPData.setProductName(productData.getKey().getProductName());
////                    /* 20180809, Modify, Mismatch Slot Position ==>> */
////                    //productPData.setPosition(productData.getPosition());
////                    productPData.setPosition(Long.valueOf(position));
////                    /* <<== 20180809, Modify, Mismatch Slot Position */
////                    productPSequence.add(productPData);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////
////                productNameList = productNameList.substring(1);
////
////                try
////                {
////                    productList.addAll(ProductServiceProxy.getProductService().select("WHERE PRODUCTNAME IN (" + productNameList + ")" + "ORDER BY LOTNAME" , null));
////                }
////                catch (Exception ex)
////                {
////                    productList = null;
////                }
////
////                Lot targetLot = null;
////                List<Lot> lotList = null;
////
////                for(Product productData : productList)
////                {
////                    if(!StringUtil.isEmpty(lotName))
////                    {
////                        if(!StringUtil.equals(lotName,  productData.getLotName()))
////                        {
////                            Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////                            if(targetLot == null)
////                            {
////                                try
////                                {
////                                    lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                                }
////                                catch (Exception ex)
////                                {
////                                    lotList = null;
////                                }
////
////                                if(lotList == null)
////                                {
////                                    eventInfo.setEventName("Create");
////                                    targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
////                                }
////                                else
////                                {
////                                    targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
////                                }
////                            }
////
////                            trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot);
////
////                            lotName = productData.getLotName();
////                            productPGSRCSequence = new ArrayList<ProductPGSRC>();
////                        }
////                    }
////                    else
////                    {
////                        lotName = productData.getLotName();
////                    }
////
////                    ProductPGSRC productPGSRC = new ProductPGSRC();
////                    productPGSRC.setProductName(productData.getKey().getProductName());
////                    productPGSRC.setPosition(productData.getPosition());
////                    productPGSRC.setProductGrade(productData.getProductGrade());
////                    productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
////                    productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
////                    productPGSRC.setReworkFlag("N");
////                    productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
////                    productPGSRCSequence.add(productPGSRC);
////                }
////
////                if(productPGSRCSequence.size() > 0)
////                {
////                    if(targetLot == null)
////                    {
////                        try
////                        {
////                            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                        }
////                        catch (Exception ex)
////                        {
////                            lotList = null;
////                        }
////
////                        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////                        if(lotList != null)
////                        {
////                            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
////                        }
////
////                        if(targetLot != null)
////                        {
////                            trackOutLot = this.mergeGlass(eventInfo, lotData, productPGSRCSequence, carrierName, targetLot);
////                        }
////                    }
////                    
////                    if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
////                    {
////                        eventInfo.setEventName("AssignCarrier");
////                        eventInfo.setCheckTimekeyValidation(false);
////                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                        
////                        AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
////                        assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
////                        assignCarrierInfo.setProductPSequence(productPSequence);
////                        
////                        Map<String, String> assignCarrierUdfs = durableData.getUdfs();
////                        assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
////
////                        Map<String, String> lotUdfs = trackOutLot.getUdfs();
////                        assignCarrierInfo.setUdfs(lotUdfs);
////                        
////                        trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
////                    }
////                }
////                else  /* <<== hhlee, add, 20180721, else State */
////                {
////                    try
////                    {
////                        CommonValidation.checkEmptyCst(carrierName); 
////                    }
////                    catch (CustomException ce)
////                    {
////                        lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                        trackOutLot = lotList.get(0);
////                        //List<ProductU> productUSequence =  new ArrayList<ProductU>();                        
////                        //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                        //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                        //setEventInfo.setProductUSequence(productUSequence);
////                        //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
////                    }
////                }
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {
////                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                trackOutLot = lotList.get(0);
////                //List<ProductU> productUSequence =  new ArrayList<ProductU>();                
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER))
////        {
////            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////
////            List<ProductU> productUSequence = new ArrayList<ProductU>();
////
////            for (Element product : productElement )
////            {
////                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////
////                if(StringUtil.equals(processingInfo, "P"))
////                {
////                    ProductU productU = new ProductU();
////                    productU.setProductName(productName);
////                    productUSequence.add(productU);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////            }
////
////            if(productUSequence.size() > 0)
////            {
////                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNOVER);
////                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                setEventInfo.setProductUSequence(productUSequence);
////                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {              
////                trackOutLot = lotList.get(0);
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
////               
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE))
////        {
////            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////
////            List<ProductU> productUSequence = new ArrayList<ProductU>();
////
////            for (Element product : productElement )
////            {
////                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////
////                if(StringUtil.equals(processingInfo, "P"))
////                {
////                    ProductU productU = new ProductU();
////                    productU.setProductName(productName);
////                    productUSequence.add(productU);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////            }
////
////            if(productUSequence.size() > 0)
////            {
////                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_TURNSIDE);
////                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                setEventInfo.setProductUSequence(productUSequence);
////                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {              
////                trackOutLot = lotList.get(0);
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
////               
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE))
////        {
////            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////
////            List<ProductU> productUSequence = new ArrayList<ProductU>();
////
////            for (Element product : productElement )
////            {
////                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////                String position = SMessageUtil.getChildText(product, "POSITION", false);
////
////                if(StringUtil.equals(processingInfo, "P"))
////                {
////                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
////
////                    productData.setPosition(Long.valueOf(position));
////                    ProductServiceProxy.getProductService().update(productData);
////
////                    ProductU productU = new ProductU();
////                    productU.setProductName(productName);
////                    productUSequence.add(productU);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////            }
////
////            if(productUSequence.size() > 0)
////            {
////                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SLOTMAPCHANGE);
////                kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                setEventInfo.setProductUSequence(productUSequence);
////                trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {              
////                trackOutLot = lotList.get(0);
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo); 
////               
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP))
////        {
////            List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////
////            List<ProductU> productUSequence = new ArrayList<ProductU>();
////
////            for (Element product : productElement )
////            {
////                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////
////                if(StringUtil.equals(processingInfo, "P"))
////                {
////                    ProductU productU = new ProductU();
////                    productU.setProductName(productName);
////                    productUSequence.add(productU);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////            }
////
////            if(productUSequence.size() > 0)
////            {
////                if(lotList.get(0).getProductQuantity() == productUSequence.size())
////                {
////                    eventInfo.setEventName("DeassignCarrier");
////                    eventInfo.setCheckTimekeyValidation(false);
////                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////
////                    DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
////                    trackOutLot = LotServiceProxy.getLotService().deassignCarrier(lotList.get(0).getKey(), eventInfo, deassignCarrierInfo);
////                }
////
////                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
////                eventInfo.setCheckTimekeyValidation(false);
////                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////
////                MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
////                makeScrappedInfo.setProductQuantity(productUSequence.size());
////                makeScrappedInfo.setProductUSequence(productUSequence);
////                trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {              
////                trackOutLot = lotList.get(0);
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
////            }
////        }
////        else if(StringUtil.equals(sortJobType, GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAPFORAFTER))
////        {
////            String lotName = "";
////            List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
////            List<ProductU> productUSequence = new ArrayList<ProductU>();
////            List<ProductP> productPSequence = new ArrayList<ProductP>();
////
////            for (Element product : productElement )
////            {
////                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
////                String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
////                String position = SMessageUtil.getChildText(product, "POSITION", false);
////
////                if(StringUtil.equals(processingInfo, "P"))
////                {
////                    Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
////
////                    if(StringUtil.isEmpty(lotName))
////                    {
////                        lotName = productData.getLotName();
////                    }
////
////                    ProductPGSRC productPGSRC = new ProductPGSRC();
////                    productPGSRC.setProductName(productName);
////                    productPGSRC.setPosition(Long.valueOf(position));
////
////                    String productGrade = SMessageUtil.getChildText(product, "PRODUCTJUDGE", false);
////                    if (StringUtil.isEmpty(productGrade))
////                    {
////                        productPGSRC.setProductGrade(productData.getProductGrade());
////                    }
////                    else
////                    {
////                        productPGSRC.setProductGrade(productGrade);
////                    }
////
////                    productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
////                    productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
////                    productPGSRC.setReworkFlag("N");
////
////                    productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
////                    Map<String, String> productUdfs = CommonUtil.setNamedValueSequence(product, "Product");
////                    productUdfs.put("PROCESSINGINFO", processingInfo);
////                    productPGSRC.setUdfs(productUdfs);
////                    productPGSRCSequence.add(productPGSRC);
////
////                    ProductU productU = new ProductU();
////                    productU.setProductName(productName);
////                    productUSequence.add(productU);
////                    
////                    ProductP productPData = new ProductP();
////                    productPData.setProductName(productData.getKey().getProductName());
////                    productPData.setPosition(productData.getPosition());
////                    productPSequence.add(productPData);
////                    
////                    ExtendedObjectProxy.getProductFlagService().setLotInfoReceiveProductFlag(eventInfo, product, GenericServiceProxy.getConstantMap().PRODUCTFLAGUPLOAD);
////                }
////            }
////
////            if(productPGSRCSequence.size() > 0)
////            {
////                Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
////
////                trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence, carrierName);
////                
////                if(StringUtil.isEmpty(trackOutLot.getCarrierName()))
////                {
////                    eventInfo.setEventName("AssignCarrier");
////                    eventInfo.setCheckTimekeyValidation(false);
////                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                    
////                    AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
////                    assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
////                    assignCarrierInfo.setProductPSequence(productPSequence);
////                    
////                    Map<String, String> assignCarrierUdfs = durableData.getUdfs();
////                    assignCarrierInfo.setAssignCarrierUdfs(assignCarrierUdfs);
////
////                    Map<String, String> lotUdfs = trackOutLot.getUdfs();
////                    assignCarrierInfo.setUdfs(lotUdfs);
////                    
////                    trackOutLot = LotServiceProxy.getLotService().assignCarrier(trackOutLot.getKey(), eventInfo, assignCarrierInfo);
////                }
////
////                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SCRAP);
////                eventInfo.setCheckTimekeyValidation(false);
////                eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////
////                MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
////                makeScrappedInfo.setProductQuantity(productUSequence.size());
////                makeScrappedInfo.setProductUSequence(productUSequence);
////                trackOutLot = LotServiceProxy.getLotService().makeScrapped(trackOutLot.getKey(), eventInfo, makeScrappedInfo);
////            }
////            else  /* <<== hhlee, add, 20180721, else State */
////            {     
////                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
////                trackOutLot = lotList.get(0);
////                //eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOB_PROCESS_TYPE_ABORT);
////                //kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                //setEventInfo.setProductUSequence(productUSequence);
////                //trackOutLot = LotServiceProxy.getLotService().setEvent(lotList.get(0).getKey(), eventInfo, setEventInfo);                
////            }
////        }
////
////        if (trackOutLot != null)
////        {
////            trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
////    
////            if(StringUtil.equals(trackOutLot.getLotState(), "Emptied"))
////            {
////                String[] nodeStackList = null;
////    
////                nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
////    
////                if(nodeStackList.length > 0)
////                {
////                    Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);
////    
////                    /* 20180731, Add, Change LotProcessState ==>> */
////                    trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
////                    /* <<== 20180731, Add, Change LotProcessState */
////                    
////                    ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
////                            trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
////                            "", "", trackOutLot.getProductRequestName(), trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
////                            trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
////                            trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
////                            nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
////                            "", "", "", "", "", trackOutLot.getUdfs(), null, false);
////    
////                    eventInfo.setEventName("TrackOut");
////                    trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
////                    
////                    if(!StringUtil.isEmpty(trackOutLot.getCarrierName()))
////                    {
////                        eventInfo.setEventName("DeassignCarrier");
////                        eventInfo.setCheckTimekeyValidation(false);
////                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                        
////                        Durable deassignCarrier = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trackOutLot.getCarrierName());
////                        
////                        SetEventInfo setEventInfo = new SetEventInfo();
////                        DurableServiceProxy.getDurableService().setEvent(deassignCarrier.getKey(), eventInfo, setEventInfo);
////                        
////                        kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
////                        LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo1);
////                        
////                        trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(trackOutLot.getKey().getLotName());
////                        trackOutLot.setCarrierName("");
////                        LotServiceProxy.getLotService().update(trackOutLot);
////                        
////                        LotHistory lotHData = MESLotServiceProxy.getLotInfoUtil().getLotHistory(trackOutLot.getKey().getLotName());
////                        lotHData.setCarrierName("");
////                        LotServiceProxy.getLotHistoryService().update(lotHData);
////                    }
////                }
////            }
////            else
////            {
////                String[] nodeStackList = null;
////    
////                nodeStackList = StringUtil.split(trackOutLot.getNodeStack(), '.');
////    
////                if(nodeStackList.length > 0)
////                {
////                    List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setAllProductUdfs(trackOutLot.getKey().getLotName());
////    
////                    productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
////    
////                    Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[0]);
////    
////                    /* 20180731, Add, Change LotProcessState ==>> */
////                    trackOutLot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
////                    /* <<== 20180731, Add, Change LotProcessState */
////                    
////                    ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(trackOutLot.getKey().getLotName(),
////                            trackOutLot.getProductionType(), trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(),
////                            "", "", trackOutLot.getProductRequestName(), trackOutLot.getSubProductUnitQuantity1(), trackOutLot.getSubProductUnitQuantity2(),
////                            trackOutLot.getDueDate(), trackOutLot.getPriority(), trackOutLot.getFactoryName(), trackOutLot.getAreaName(),
////                            trackOutLot.getLotState(), trackOutLot.getLotProcessState(), trackOutLot.getLotHoldState(),
////                            nodeData.getProcessFlowName(), nodeData.getProcessFlowVersion(), nodeData.getNodeAttribute1(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
////                            "", "", "", "", "", trackOutLot.getUdfs(), productUdfs, false);
////    
////                    eventInfo.setEventName("TrackOut");
////                    trackOutLot = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, trackOutLot, changeSpecInfo);
////    
////                    ///* Array 20180807, Add [Process Flag Update] ==>> */            
////                    //MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, trackOutLot, logicalSlotMap);
////                    ///* <<== Array 20180807, Add [Process Flag Update] */
////                    
////                    if(!StringUtil.equals(trackOutLot.getLotState(), "Scrapped"))
////                    {
////                        eventInfo.setEventName("LotProcessAbort");
////                        eventInfo.setCheckTimekeyValidation(false);
////                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////                        
////                        List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(trackOutLot);
////                        MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
////                        LotServiceProxy.getLotService().makeOnHold(trackOutLot.getKey(), eventInfo, makeOnHoldInfo);
////                    }
////                }
////            }
////        }
////    }
////  
//    /**
//     * 
//     * @Name     splitGlassByDeassignCarreir
//     * @since    2018. 12. 5.
//     * @author   hhlee
//     * @contents From all product of source cassette to target cassette
//     *           
//     * @param eventInfo
//     * @param lotData
//     * @param productPGSRCSequence
//     * @param carrierName
//     * @return
//     * @throws CustomException
//     */
//    private Lot splitGlassByDeassignCarreir(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
//    {
//        eventLog.info("Split(DeassignCarrier Lot for TK out");
//        
//        Lot targetLot = null;
//        
//        String splitProductList = StringUtil.EMPTY;
//        
//        List<ProductU> productUSequence = new ArrayList<ProductU>();
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductU productU = new ProductU();
//            productU.setProductName(productPGSRC.getProductName());
//            productU.setUdfs(productPGSRC.getUdfs());
//            productUSequence.add(productU);
//            
//            if(StringUtil.isEmpty(splitProductList))
//            {
//                splitProductList = productPGSRC.getProductName();
//            }
//            else
//            {
//                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
//            }
//        }
//        
//        splitProductList = "[Change Product] - " + splitProductList;
//        
//        Map<String,String> lotUdfs = lotData.getUdfs();   
//        lotUdfs.put("NOTE", splitProductList);                 
//        lotData.setUdfs(lotUdfs);      
//        LotServiceProxy.getLotService().update(lotData);
//        
//        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
//        eventInfo.setCheckTimekeyValidation(false);
//        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//        //eventInfo.setEventName("AssignCarrier");
//        //eventInfo.setCheckTimekeyValidation(false);
//        //eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//        
//        DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
//        deassignCarrierInfo.setProductUSequence(productUSequence);
//        
//        targetLot = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
//        
//
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        return targetLot;
//    }
//    
//    private Lot mergeGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, Lot targetLot, String allGlasssScrapFlag) throws CustomException
//    {
//        eventLog.info("Merge Lot for TK out");
//
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        
//        /* 20180928, hhlee, add, Lot Note ==>> */
//        String mergeProductList = StringUtil.EMPTY;
//        
//        //for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        //{
//        //  ProductP productP = new ProductP();
//        //  productP.setProductName(productPGSRC.getProductName());
//        //  productP.setPosition(productPGSRC.getPosition());
//        //  productP.setUdfs(productPGSRC.getUdfs());
//        //  productPSequence.add(productP);
//        //}
//
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductP productP = new ProductP();
//            productP.setProductName(productPGSRC.getProductName());
//            productP.setPosition(productPGSRC.getPosition());
//            productP.setUdfs(productPGSRC.getUdfs());
//            productPSequence.add(productP);
//            
//            if(StringUtil.isEmpty(mergeProductList))
//            {
//                mergeProductList = productPGSRC.getProductName();
//            }
//            else
//            {
//                mergeProductList = mergeProductList + " , " +  productPGSRC.getProductName() ;
//            }
//        }
//        
//        mergeProductList = "[Change Product] - " + mergeProductList;
//        
//        //2019.04.23_hsryu_Insert Logic. Mantis 0002757.
//        try{
//            String woName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot, productPSequence);
//            
//            if(!StringUtils.isEmpty(woName)) {
//                if(!StringUtils.equals(targetLot.getProductRequestName(), woName)){
//                    targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//                    targetLot.setProductRequestName(woName);
//                    LotServiceProxy.getLotService().update(targetLot);
//                }
//            }
//        }
//        catch(Throwable e){
//            eventLog.warn("Fail update WorkOrder.");
//        }
//        
//        Map<String,String> lotUdfs = lotData.getUdfs();   
//        lotUdfs.put("NOTE", mergeProductList);                 
//        lotData.setUdfs(lotUdfs);  
//        LotServiceProxy.getLotService().update(lotData);
//        /* <<== 20180928, hhlee, add, Lot Note */
//        
//        /* 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) ==>> */
//        if(lotData.getPriority() < targetLot.getPriority())
//        {
//            targetLot.setPriority(lotData.getPriority());                
//            LotServiceProxy.getLotService().update(targetLot);
//        }
//        /* <<== 20181030, hhlee, add, LotPriority Copy(Source Lot Priority < Target Lot Priority) */
//        
//        /* 20180930, hhlee, add All Glass Scrap Check ==>> */
//        TransferProductsToLotInfo  transitionInfo = null;
//        
//        /* 20190313, hhlee, modify, Mixed ProductSpec ==>> */
//        //if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//        //{
//        //    //transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfoWithoutValidation(
//        //    //        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//        //    
//        //    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//        //    eventInfo.setCheckTimekeyValidation(false);
//        //    /* 20181128, hhlee, EventTime Sync */
//        //    //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        //    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//        //    
//        //    //for(ProductP productP : productPSequence)
//        //    //{
//        //    //    String productnameP = productP.getProductName();
//        //    //    Product productdataP = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productnameP);
//        //    //    kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo deassignCarrierInfo = new kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo();
//        //    //    ProductServiceProxy.getProductService().deassignCarrier(productdataP.getKey(), eventInfo, deassignCarrierInfo);
//        //    //    
//        //    //    eventInfo.setCheckTimekeyValidation(false);
//        //    //    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        //    //    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());                
//        //    //}
//        //    
//        //    DeassignProductsInfo deassignProductsInfo = new DeassignProductsInfo();
//        //    deassignProductsInfo.setProductPSequence(productPSequence);
//        //    deassignProductsInfo.setEmptyFlag("Y");
//        //    deassignProductsInfo.setProductQuantity(productPSequence.size());
//        //    
//        //    lotData = LotServiceProxy.getLotService().deassignProducts(lotData.getKey(), eventInfo, deassignProductsInfo);
//        //    
//        //    /* 20181128, hhlee, EventTime Sync */
//        //    //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        //    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//        //    
//        //    /* 20180930, hhlee, add, Lot Note ==>> */
//        //    Map<String,String> targetLotUdfs = targetLot.getUdfs();   
//        //    targetLotUdfs.put("NOTE", mergeProductList);                 
//        //    targetLot.setUdfs(targetLotUdfs);  
//        //    LotServiceProxy.getLotService().update(targetLot);
//        //    /* <<== 20180930, hhlee, add, Lot Note */
//        //    
//        //    AssignProductsInfo assignProductsInfo = new AssignProductsInfo();
//        //    assignProductsInfo.setProductPSequence(productPSequence);
//        //    assignProductsInfo.setProductQuantity(productPSequence.size());
//        //    
//        //    targetLot = LotServiceProxy.getLotService().assignProducts(targetLot.getKey(), eventInfo, assignProductsInfo);
//        //}
//        //else
//        //{
//        //    transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//        //            targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//        //    
//        //    eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//        //    eventInfo.setCheckTimekeyValidation(false);
//        //    /* 20181128, hhlee, EventTime Sync */
//        //    //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        //    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//        //
//        //    lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//        //}
//        ///* <<== 20180930, hhlee, add All Glass Scrap Check */
//        
//        /* 20190313, hhlee,  */
//        if(StringUtil.equals(allGlasssScrapFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//        {
//            eventInfo.setBehaviorName("SPECIAL");
//        }
//        
//        transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, targetLot.getUdfs(), new HashMap<String, String>());
//        
//        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
//        eventInfo.setCheckTimekeyValidation(false);
//        /* 20181128, hhlee, EventTime Sync */
//        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//        /* <<== 20190313, hhlee, modify, Mixed ProductSpec */
//        
//        //2019.04.23_hsryu_Insert Logic. Check Mixed or Not Mixed WO. Mantis 0002757.
//        try{
//            if(lotData != null) {
//                if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied)){
//                    
//                    String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
//
//                    if(!StringUtils.isEmpty(sourceWOName)) {
//                        if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)){
//                            lotData.setProductRequestName(sourceWOName);
//                            LotServiceProxy.getLotService().update(lotData);
//
//                            String condition = "where lotname=?" + " and timekey= ? " ;
//                            Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//                            List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//                            LotHistory lotHistory = arrayList.get(0);
//                            lotHistory.setProductRequestName(sourceWOName);
//                            LotServiceProxy.getLotHistoryService().update(lotHistory);
//                        }
//                    }
//                }
//            }
//        }
//        catch(Throwable e){
//            eventLog.warn("Fail update WorkOrder.");
//        }
//        
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        return targetLot;
//    }
//    
//    private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
//    {
//        eventLog.info("Split Lot for TK out");
//
//        List<Lot> lotList = null;
//        Lot targetLot = null;
//        try
//        {
//            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//        }
//        catch (Exception ex)
//        {
//            lotList = null;
//        }
//        
//        /* 20180928, hhlee, add, Lot Note ==>> */
//        String splitProductList = StringUtil.EMPTY;
//        
//        //List<ProductP> productPSequence = new ArrayList<ProductP>();
//        //for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        //{
//        //    ProductP productP = new ProductP();
//        //    productP.setProductName(productPGSRC.getProductName());
//        //    productP.setPosition(productPGSRC.getPosition());
//        //    productP.setUdfs(productPGSRC.getUdfs());
//        //    productPSequence.add(productP);
//        //    
//        //    if(StringUtil.isEmpty(splitProductList))
//        //    {
//        //        splitProductList = productPGSRC.getProductName();
//        //    }
//        //    else
//        //    {
//        //        splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
//        //    }
//        //}
//        //
//        //splitProductList = "[Change Product] - " + splitProductList;
//        //                           
//        //Map<String,String> lotUdfs = lotData.getUdfs();   
//        //lotUdfs.put("NOTE", splitProductList);                 
//        //lotData.setUdfs(lotUdfs); 
//        //LotServiceProxy.getLotService().update(lotData);
//        /* <<== 20180928, hhlee, add, Lot Note */
//        
//        if(lotList.size() == 0)
//        {
//            eventInfo.setEventName("Create");
//            targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//        }
//        else
//        {
//            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
//        }
//
//        /* 20180928, hhlee, add, Lot Note ==>> */
//        //List<ProductP> productPSequence = new ArrayList<ProductP>();
//        //for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        //{
//        //  ProductP productP = new ProductP();
//        //  productP.setProductName(productPGSRC.getProductName());
//        //  productP.setPosition(productPGSRC.getPosition());
//        //  productP.setUdfs(productPGSRC.getUdfs());
//        //  productPSequence.add(productP);
//        //}
//        
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductP productP = new ProductP();
//            productP.setProductName(productPGSRC.getProductName());
//            productP.setPosition(productPGSRC.getPosition());
//            productP.setUdfs(productPGSRC.getUdfs());
//            productPSequence.add(productP);
//            
//            if(StringUtil.isEmpty(splitProductList))
//            {
//                splitProductList = productPGSRC.getProductName();
//            }
//            else
//            {
//                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
//            }
//        }
//        
//        splitProductList = "[Change Product] - " + splitProductList;        
//                                 
//        Map<String,String> lotUdfs = lotData.getUdfs();   
//        lotUdfs.put("NOTE", splitProductList);                 
//        lotData.setUdfs(lotUdfs);   
//        LotServiceProxy.getLotService().update(lotData);
//        /* <<== 20180928, hhlee, add, Lot Note */
//
//        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//
//        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
//        eventInfo.setCheckTimekeyValidation(false);
//        /* 20181128, hhlee, EventTime Sync */
//        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//        
//        /**************************** Check Mixed WO Name *********************************/
//        //2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//        try{
//            String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
//            
//            if(!StringUtils.isEmpty(desWOName)) {
//                targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//                if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
//                    targetLot.setProductRequestName(desWOName);
//                    LotServiceProxy.getLotService().update(targetLot);
//                    
//                    String condition = "where lotname=?" + " and timekey= ? " ;
//                    Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//                    List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//                    LotHistory lotHistory = arrayList.get(0);
//                    lotHistory.setProductRequestName(desWOName);
//                    LotServiceProxy.getLotHistoryService().update(lotHistory);
//                }
//            }
//            
//            //2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//            String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
//            
//            if(!StringUtils.isEmpty(sourceWOName)) {
//                if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
//                    lotData.setProductRequestName(sourceWOName);
//                    LotServiceProxy.getLotService().update(lotData);
//                    
//                    String condition = "where lotname=?" + " and timekey= ? " ;
//                    Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//                    List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//                    LotHistory lotHistory = arrayList.get(0);
//                    lotHistory.setProductRequestName(sourceWOName);
//                    LotServiceProxy.getLotHistoryService().update(lotHistory);
//                }
//            }
//        }
//        catch(Throwable e){
//            eventLog.warn("Fail update WorkOrder.");
//        }
//        /*********************************************************************************/
//
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        return targetLot;
//    }
//    
//    /**
//     * 
//     * @Name     scrapSplitGlass
//     * @since    2018. 12. 5.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param eventInfo
//     * @param lotData
//     * @param productPGSRCSequence
//     * @param carrierName
//     * @return
//     * @throws CustomException
//     */
//    private Lot scrapSplitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName) throws CustomException
//    {
//        eventLog.info("Scrap Split Lot for TK out");
//
//        List<Lot> lotList = null;
//        Lot targetLot = null;
//        try
//        {
//            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//        }
//        catch (Exception ex)
//        {
//            lotList = null;
//        }
//        
//        String splitProductList = StringUtil.EMPTY;
//        
//        if(lotList.size() == 0 )
//        {
//            eventInfo.setEventName("Create");
//            targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//        }
//        else
//        {
//            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
//        }
//        
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductP productP = new ProductP();
//            productP.setProductName(productPGSRC.getProductName());
//            productP.setPosition(productPGSRC.getPosition());
//            productP.setUdfs(productPGSRC.getUdfs());
//            productPSequence.add(productP);
//            
//            if(StringUtil.isEmpty(splitProductList))
//            {
//                splitProductList = productPGSRC.getProductName();
//            }
//            else
//            {
//                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
//            }
//        }
//        
//        splitProductList = "[Scrap Product] - " + splitProductList;
//        
//        Map<String,String> lotUdfs = lotData.getUdfs();   
//        lotUdfs.put("NOTE", splitProductList);                 
//        lotData.setUdfs(lotUdfs);      
//        LotServiceProxy.getLotService().update(lotData);
//        /* <<== 20180928, hhlee, add, Lot Note */
//
//        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//
//        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
//        eventInfo.setCheckTimekeyValidation(false);
//        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        return targetLot;
//    }
//    
//    /**
//     * 
//     * @Name     scrapSplitGlass
//     * @since    2018. 12. 26.
//     * @author   hhlee
//     * @contents Split Glass For Scrap 
//     *           
//     * @param eventInfo
//     * @param lotData
//     * @param productPGSRCSequence
//     * @param carrierName
//     * @param outStageFlag
//     * @return
//     * @throws CustomException
//     */
//    private Lot scrapSplitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, String carrierName, String outStageFlag) throws CustomException
//    {
//        eventLog.info("Scrap Split Lot for TK out");
//
//        List<Lot> lotList = null;
//        Lot targetLot = null;
//        try
//        {
//            lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataByCarrierName(carrierName,false);
//        }
//        catch (Exception ex)
//        {
//            lotList = null;
//        }
//        
//        String splitProductList = StringUtil.EMPTY;
//        
//        if(lotList.size() == 0 )
//        {
//            eventInfo.setEventName("Create");
//            targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
//        }
//        else
//        {
//            targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotList.get(0).getKey().getLotName());
//        }
//        
//        List<ProductP> productPSequence = new ArrayList<ProductP>();
//        for (ProductPGSRC productPGSRC : productPGSRCSequence)
//        {
//            ProductP productP = new ProductP();
//            productP.setProductName(productPGSRC.getProductName());
//            productP.setPosition(productPGSRC.getPosition());
//            productP.setUdfs(productPGSRC.getUdfs());
//            productPSequence.add(productP);
//            
//            if(StringUtil.isEmpty(splitProductList))
//            {
//                splitProductList = productPGSRC.getProductName();
//            }
//            else
//            {
//                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
//            }
//        }
//        
//        splitProductList = "[Scrap Product] - " + splitProductList;
//        
//        Map<String,String> lotUdfs = lotData.getUdfs();   
//        lotUdfs.put("NOTE", splitProductList);                 
//        lotData.setUdfs(lotUdfs);      
//        LotServiceProxy.getLotService().update(lotData);
//        /* <<== 20180928, hhlee, add, Lot Note */
//
//        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
//                                                        targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
//   
//        eventInfo.setEventName("ScrapSplit");
//        if(StringUtil.equals(outStageFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//        {
//            eventInfo.setEventName("OutStageSplit");
//        }
//        eventInfo.setCheckTimekeyValidation(false);
//        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//
//        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
//        
//        /**************************** Check Mixed WO Name *********************************/
//        //2019.04.24_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//        try{
//            String desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(targetLot);
//            
//            if(!StringUtils.isEmpty(desWOName)) {
//                targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//                if(!StringUtils.equals(targetLot.getProductRequestName(), desWOName)) {
//                    targetLot.setProductRequestName(desWOName);
//                    LotServiceProxy.getLotService().update(targetLot);
//                    
//                    String condition = "where lotname=?" + " and timekey= ? " ;
//                    Object[] bindSet = new Object[]{targetLot.getKey().getLotName(), eventInfo.getEventTimeKey()};
//                    List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//                    LotHistory lotHistory = arrayList.get(0);
//                    lotHistory.setProductRequestName(desWOName);
//                    LotServiceProxy.getLotHistoryService().update(lotHistory);
//                }
//            }
//            
//            //2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
//            String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
//            
//            if(!StringUtils.isEmpty(sourceWOName)) {
//                if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
//                    lotData.setProductRequestName(sourceWOName);
//                    LotServiceProxy.getLotService().update(lotData);
//                    
//                    String condition = "where lotname=?" + " and timekey= ? " ;
//                    Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
//                    List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
//                    LotHistory lotHistory = arrayList.get(0);
//                    lotHistory.setProductRequestName(sourceWOName);
//                    LotServiceProxy.getLotHistoryService().update(lotHistory);
//                }
//            }
//        }
//        catch(Throwable e){
//            eventLog.warn("Fail update WorkOrder.");
//        }
//        /*********************************************************************************/
//
//        targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
//
//        return targetLot;
//    }
//    
//    private Lot abortHoldLot(EventInfo eventInfo, String lotName, String reasonCodeType, String reasonCode) throws CustomException
//    {
//        eventInfo.setReasonCode(reasonCode);
//        eventInfo.setReasonCodeType(reasonCodeType);
//        
//        eventInfo.setEventName("Hold");
//        eventInfo.setEventComment("Lot Process Abort Hold.");
//        eventInfo.setCheckTimekeyValidation(false);
//        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//        eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
//        
//        Lot trackOutLot    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//        
//        Map<String, String> udfs = new HashMap<String, String>();
//        List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(trackOutLot);
//        
//        MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
////      String holdDepartment = MESLotServiceProxy.getLotServiceImpl().getHoldDepartment(trackOutLot.getKey().getLotName());
////      makeOnHoldInfo.getUdfs().put("HOLDDEPARTMENT", holdDepartment);
//        LotServiceProxy.getLotService().makeOnHold(trackOutLot.getKey(), eventInfo, makeOnHoldInfo);
//        
//        //Map<String,String> multiHoldudfs = new HashMap<String, String>();
//        //multiHoldudfs.put("eventuserdep", "");
//        
//        //LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//        //multiholdkey.setLotName(lotName);
//        //multiholdkey.setReasonCode(reasonCode);
//        
//        //LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//        //multihold.setUdfs(multiHoldudfs);
//                
//        //LotServiceProxy.getLotMultiHoldService().update(multihold);
//        
//        return trackOutLot;
//    }
//    
////  private void clearCarrerNameOnAutoMQCSetting(String carrerName,String machineName )
////  {
////      EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
////      String condition = " WHERE MACHINENAME = ? AND CARRIERNAME = ? ";
////      Object[] bindSet = new Object[] { machineName ,  carrerName};
////      
////      try {
////          List<AutoMQCSetting> autoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
////          for(AutoMQCSetting autoMQCSetting : autoMQCSettingList)
////          {
////              autoMQCSetting.setCarrierName("");
////              autoMQCSetting.setLastRunTime(eventInfo.getEventTime());
////              
////              ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
////          }
////      } catch (CustomException e) {
////          // TODO Auto-generated catch block
////          e.printStackTrace();
////      }
////  }
//    
////  /**
////     * 
////     * @Name     copyFutureActionCallSplit
////     * @since    2018. 8. 14.
////     * @author   Admin
////     * @contents Copy FutureAction From Parent Lot To Child Lot
////     *           
////     * @param sourceLot
////     * @param destnationLot
////     * @param eventInfo
////     */
////    private void copyFutureActionCallSplit(Lot parentLot, Lot childLot, EventInfo eventInfo)
////    {
////        try
////        {
////            eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
////            eventInfo.setEventComment(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
////            eventInfo.setCheckTimekeyValidation(false);
////            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////            
////            copyFutureActionForSplit(parentLot, childLot, eventInfo);
////        }
////        catch (Throwable e)
////        {
////            return;
////        }
////    }
////    
////    /**
////     * 
////     * @Name     copyFutureActionForSplit
////     * @since    2018. 2. 8
////     * @author   YHU
////     * @contents Copy FutureAction From Parent Lot To Child Lot
////     *           
////     * @param parent
////     * @param child
////     * @param eventInfo
////     */
////    private void copyFutureActionForSplit(Lot parent, Lot child, EventInfo eventInfo)
////    {
////        
////        List<LotAction> sampleActionList = new ArrayList<LotAction>();
////        long lastPosition = 0;
////        
////        String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND actionState = ? ";
////        Object[] bindSet = new Object[]{ parent.getKey().getLotName(), parent.getFactoryName(), "Created" };
////
////        try
////        {
////            sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
////            
////            for(int i=0; i<sampleActionList.size();i++)
////            {
////                LotAction lotaction = new LotAction();
////                lotaction = sampleActionList.get(i);
////                
////                lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(child,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
////                
////                lotaction.setLotName(child.getKey().getLotName());
////                lotaction.setPosition(lastPosition+1);
////                lotaction.setLastEventTime(eventInfo.getEventTime());
////                lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
////                lotaction.setLastEventName(eventInfo.getEventName());
////                lotaction.setLastEventUser(eventInfo.getEventUser());
////                lotaction.setLastEventComment(eventInfo.getEventComment());
////                
////                ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
////
////            }
////        }
////        catch (Throwable e)
////        {
////            return;
////        }
////        
////        
////        /*
////        //Copy  FutureAction from parent to child
////        // Find future Action
////        Object[] bindSet =
////                new Object[] {
////                parent.getKey().getLotName(),
////                parent.getFactoryName()};
////
////        try
////        {
////            List<LotFutureAction> lotFutureActionList = LotServiceProxy.getLotFutureActionService().select(SqlStatement.LotFutureActionKey, bindSet);
////            if(lotFutureActionList.size() > 0)
////            {
////                Object[] bindSetChild =
////                        new Object[] {
////                        child.getKey().getLotName(),
////                        child.getFactoryName(),
////                        child.getProcessFlowName(),
////                        child.getProcessFlowVersion(),
////                        child.getProcessOperationName(),
////                        child.getProcessOperationVersion() };
////                String condition = "";
////                for(LotFutureAction action : lotFutureActionList)
////                {
////                    LotServiceProxy.getLotFutureActionService().update(action, condition, bindSetChild);
////                }
////            }
////        }
////        catch (NotFoundSignal ne)
////        {
////            return;
////        }
////        catch(Exception e)
////        {
////            return;
////        }*/        
////    }
////    
////    /**
////     * 
////     * @Name     copyFutureActionCallMerge
////     * @since    2018. 8. 14.
////     * @author   Admin
////     * @contents Copy FutureAction From Parent Lot To Child Lot
////     *           
////     * @param sourceLot
////     * @param destnationLot
////     * @param eventInfo
////     */
////    private void copyFutureActionCallMerge(Lot sourceLot, Lot destnationLot, EventInfo eventInfo)
////    {
////        try
////        {
////            eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
////            eventInfo.setCheckTimekeyValidation(false);
////            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
////            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
////            
////            copyFutureActionForMerge(sourceLot, destnationLot, eventInfo);
////        }
////        catch (Throwable e)
////        {
////            return;
////        }
////    }
////    
////    /**
////     * 
////     * @Name     copyFutureActionForMerge
////     * @since    2018. 2. 8
////     * @author   YHU
////     * @contents Copy FutureAction From Parent Lot To Child Lot
////     *           
////     * @param sLot
////     * @param dLot
////     * @param eventInfo
////     * @throws CustomException
////     */
////    private void copyFutureActionForMerge(Lot sLot, Lot dLot, EventInfo eventInfo) throws CustomException
////    {
////        
////        List<LotAction> lotActionList = new ArrayList<LotAction>();
////        List<LotAction> lotActionList2 = new ArrayList<LotAction>();
////        long lastPosition= 0;
////
////        String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND actionState = ? ";
////        Object[] bindSet = new Object[]{ sLot.getKey().getLotName(), sLot.getFactoryName(), "Created" };
////
////        try
////        {
////            lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
////            
////            for(int i=0; i<lotActionList.size();i++)
////            {
////                boolean existFlag = false;
////                LotAction lotaction = new LotAction();
////                lotaction = lotActionList.get(i);
////
////                String condition2 = "WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND "
////                        + "processOperationName = ? AND processOperationVersion = ? AND actionState = ? ";
////                
////                Object[] bindSet2 = new Object[]{ dLot.getKey().getLotName(), dLot.getFactoryName() ,lotaction.getProcessFlowName(),
////                        lotaction.getProcessFlowVersion(), lotaction.getProcessOperationName(), lotaction.getProcessOperationVersion(), "Created" };
////                
////                try
////                {
////                    lotActionList2 = ExtendedObjectProxy.getLotActionService().select(condition2, bindSet2);
////                    
////                    for(int j=0; j<lotActionList2.size(); j++)
////                    {
////                        LotAction lotAction2 = new LotAction();
////                        lotAction2 = lotActionList2.get(j);
////                        
////                        if(StringUtil.equals(lotAction2.getActionName(), lotaction.getActionName()))
////                        {
////                            existFlag = true;
////                            break;
////                        }
////                    }
////                    
////                    if(!existFlag)
////                    {
////                        lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(dLot,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
////
////                        lotaction.setLotName(dLot.getKey().getLotName());
////                        lotaction.setPosition(lastPosition+1);
////                        lotaction.setLastEventTime(eventInfo.getEventTime());
////                        lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
////                        lotaction.setLastEventName(eventInfo.getEventName());
////                        lotaction.setLastEventUser(eventInfo.getEventUser());
////                        lotaction.setLastEventComment(eventInfo.getEventComment());
////                        
////                        ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
////                    }
////                }
////                catch(Throwable e)
////                {
////                    lotaction.setLotName(dLot.getKey().getLotName());
////                    lotaction.setLastEventTime(eventInfo.getEventTime());
////                    lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
////                    lotaction.setLastEventName(eventInfo.getEventName());
////                    lotaction.setLastEventUser(eventInfo.getEventUser());
////                    lotaction.setLastEventComment(eventInfo.getEventComment());
////                    
////                    ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
////                }
////            }
////        }
////        catch (Throwable e)
////        {
////            return;
////        }
////    }
//}