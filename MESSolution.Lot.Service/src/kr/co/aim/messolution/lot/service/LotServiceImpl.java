package kr.co.aim.messolution.lot.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.InhibitException;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.extended.object.management.data.YieldInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.AssignNewProductsInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.CreateRawInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeEmptiedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MergeInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateAndCreateAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RecreateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.crypto.provider.ARCFOURCipher;

public class LotServiceImpl implements ApplicationContextAware {

    /**
     * @uml.property name="applicationContext"
     * @uml.associationEnd
     */
    private ApplicationContext applicationContext;
    private static Log log = LogFactory.getLog(LotServiceImpl.class);

    /**
     * @param arg0
     * @throws BeansException
     * @uml.property name="applicationContext"
     */
    @Override
    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        applicationContext = arg0;
    }
    
    public Lot createLot(EventInfo eventInfo, CreateInfo createInfo)
        throws CustomException
    {
        try
        {
            Lot lot = LotServiceProxy.getLotService().create(eventInfo, createInfo);
            
            return lot;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", createInfo.getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", createInfo.getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", createInfo.getLotName());
        }
    }
    
    /**
     * @author hykim
     * @since 2014-04-16
     * @param lotName
     * @return void
     */
    public Lot createRawLot(EventInfo eventInfo, String newLotName, CreateRawInfo createRawInfo)
    throws CustomException
    {
        try
        {
            LotKey newLotKey = new LotKey();
            newLotKey.setLotName(newLotName);
            
            Lot newLotData = LotServiceProxy.getLotService().createRaw(newLotKey, eventInfo, createRawInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
            
            return newLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", newLotName);
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", newLotName);
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", newLotName);
        }
    }
    
    /**
     * 111519 by swcho : modified
     * @author hykim
     * @since 2014-04-16
     * @param eventInfo
     * @param newLotName
     * @param createWithParentLotInfo
     * @return
     * @throws CustomException
     */
    public Lot createWithParentLot(EventInfo eventInfo, String newLotName, CreateWithParentLotInfo createWithParentLotInfo)
            throws CustomException
    {
        try
        {
//            LotKey newLotKey = new LotKey();
//            newLotKey.setLotName(newLotName);
            
        	// 2019.07.22 Park Jeong Su
        	// All Product Grade Is S -> Change LotGrade 'S'
        	try {
                if(createWithParentLotInfo.getProductPSequence()!=null && createWithParentLotInfo.getProductPSequence().size()>0){
                	
                	boolean allScrapGlassFlag = true;
                	
                    for(ProductP productP : createWithParentLotInfo.getProductPSequence()){
                    	Product product = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productP.getProductName()));
                    	if(!StringUtils.equals(GenericServiceProxy.getConstantMap().LotGrade_S, product.getProductGrade())){
                    		allScrapGlassFlag=false;
                    		break;
                    	}
                    }
                    
                    if(allScrapGlassFlag==true){
                    	log.info("All Product of New Lot is S Grade! Change Lot Grade");
                    	createWithParentLotInfo.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
                    }
                }
			} catch (Exception e) {
				
			}

            Lot newLotData = LotServiceProxy.getLotService().createWithParentLot(eventInfo, createWithParentLotInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
            
            return newLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", newLotName);
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", newLotName);
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", newLotName);
        }
    }
    
    /**
     * 151119 by swcho : modified
     * @author hykim
     * @since 2014-04-16
     * @param eventInfo
     * @param lotData
     * @param transitionInfo
     * @throws CustomException
     */
    public Lot transferProductsToLot(EventInfo eventInfo, Lot lotData, TransferProductsToLotInfo transitionInfo) throws CustomException
    {   
        try
        {
            lotData = LotServiceProxy.getLotService().transferProductsToLot(lotData.getKey(), eventInfo, transitionInfo);
            
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
            
            try {
            	// 2019.07.22 Park Jeong Su
            	// All Product Grade Is S -> Change LotGrade 'S'
                Lot targetLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(transitionInfo.getDestinationLotName()));
                List<Product> targetProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByLotName(targetLotData.getKey().getLotName());
                boolean targetLotAllScrapGlassFlag = true;
                for(Product product : targetProductList){
                	if(!StringUtils.equals(GenericServiceProxy.getConstantMap().LotGrade_S, product.getProductGrade())){
                		targetLotAllScrapGlassFlag=false;
                		break;
                	}
                }
                if(targetLotAllScrapGlassFlag==true&&!StringUtils.equals(GenericServiceProxy.getConstantMap().LotGrade_S, targetLotData.getLotGrade())){
                	targetLotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
                	LotServiceProxy.getLotService().update(targetLotData);
                	
                	LotHistoryKey lotHistoryKey = new LotHistoryKey();
                	lotHistoryKey.setLotName(targetLotData.getKey().getLotName());
                	lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
                	LotHistory targetLotHistData = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey);
                	targetLotHistData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
                	LotServiceProxy.getLotHistoryService().update(targetLotHistData);
                	log.info("Target Lot Change S Grade");
                }
                
                lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotData.getKey().getLotName()));
                
                List<Product> sourceProductList =null;
                try {
                	sourceProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
				} catch (Exception e) {
					log.info("sourceProductList not Found!");
				}
                
                
                boolean sourceLotAllScrapGlassFlag = false;
                if(sourceProductList!=null && sourceProductList.size()>0){
                    for(Product product : sourceProductList){
                    	if(StringUtils.equals(GenericServiceProxy.getConstantMap().LotGrade_S, product.getProductGrade())){
                    		sourceLotAllScrapGlassFlag=true;
                    	}
                    	else{
                    		sourceLotAllScrapGlassFlag=false;
                    		break;
                    	}
                    }
                }

                if(sourceProductList!=null && sourceLotAllScrapGlassFlag==true&& !StringUtils.equals(GenericServiceProxy.getConstantMap().LotGrade_S, lotData.getLotGrade())){
                	lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
                	LotServiceProxy.getLotService().update(lotData);
                	
                	LotHistoryKey lotHistoryKey = new LotHistoryKey();
                	lotHistoryKey.setLotName(lotData.getKey().getLotName());
                	lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
                	LotHistory sourceLotHistData = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey);
                	sourceLotHistData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
                	LotServiceProxy.getLotHistoryService().update(sourceLotHistData);
                	log.info("source Lot Change S Grade");
                }
                // Start 2019.09.09 Add By Park Jeong Su Mantis 4721
                try {
                	this.changeNodeStack111111111111111(lotData.getKey().getLotName());
        		} catch (Exception e) {
        			log.info(lotData.getKey().getLotName() + "ChangeNodeStack Error");
        		}
                // End 2019.09.09 Add By Park Jeong Su Mantis 4721
                
			} catch (Exception e) {
				log.info(e.getMessage() + " " + e.getLocalizedMessage());
			}
            
            lotData = LotServiceProxy.getLotService().selectByKey(new LotKey(lotData.getKey().getLotName()));
            
            return lotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * @author hykim
     * @since 2013-04-16
     * @param eventInfo
     * @param lotData
     * @param machineName
     * @param machineRecipeName
     * @param productCSequence
     * @param lotUdfs
     * @return trackInLotData
     */
    public Lot trackInLot(EventInfo eventInfo, Lot lotData, MakeLoggedInInfo makeLoggedInInfo) throws CustomException
    {
        try
        {
            Lot trackInLotData = LotServiceProxy.getLotService().makeLoggedIn(lotData.getKey(), eventInfo, makeLoggedInInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            // Added by smkang on 2018.12.01 - According to Feng Huanyan's request, if a lot track in to another machine, reserved lot information is removed.
    		ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, trackInLotData.getKey().getLotName(), trackInLotData.getCarrierName(), trackInLotData.getMachineName());
            
            return trackInLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            //Abnormal case holdLot Requested by EDO 20180504
            /* 20181220, hhlee, delete, because duplicate hold */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LA",ie.getMessage());
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            /* 20181220, hhlee, delete, because duplicate hold */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",fe.getMessage());
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            /* 20181220, hhlee, delete, because duplicate hold */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",de.getMessage());
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            /* 20181220, hhlee, delete, because duplicate hold */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",ne.getMessage());
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * @author hykim
     * @since 2013-04-16
     * @param eventInfo
     * @param lotData
     * @param machineName
     * @param machineRecipeName
     * @param productCSequence
     * @param lotUdfs
     * @return trackInLotData
     */
    public Lot trackInLotForOPI(EventInfo eventInfo, Lot lotData, MakeLoggedInInfo makeLoggedInInfo) throws CustomException
    {
        try
        {
            Lot trackInLotData = LotServiceProxy.getLotService().makeLoggedIn(lotData.getKey(), eventInfo, makeLoggedInInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            // Added by smkang on 2018.12.01 - According to Feng Huanyan's request, if a lot track in to another machine, reserved lot information is removed.
    		ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, trackInLotData.getKey().getLotName(), trackInLotData.getCarrierName(), trackInLotData.getMachineName());
            
            return trackInLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            //Abnormal case holdLot Requested by EDO 20180504
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LA",ie.getMessage());
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",fe.getMessage());
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",de.getMessage());
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",ne.getMessage());
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }


    public Lot SorttrackInLot(EventInfo eventInfo, Lot lotData, MakeLoggedInInfo makeLoggedInInfo) throws CustomException
    {
        try
        {
            eventInfo.setBehaviorName("ARRAY");
            Lot trackInLotData = LotServiceProxy.getLotService().makeLoggedIn(lotData.getKey(), eventInfo, makeLoggedInInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            // Added by smkang on 2018.12.01 - According to Feng Huanyan's request, if a lot track in to another machine, reserved lot information is removed.
    		ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, trackInLotData.getKey().getLotName(), trackInLotData.getCarrierName(), trackInLotData.getMachineName());
            
            return trackInLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            //Abnormal case holdLot Requested by EDO 20180504
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LA",ie.getMessage());
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",fe.getMessage());
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",de.getMessage());
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LA",ne.getMessage());
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    /**
     * 160311 by swcho : Durable makeInUse API bug evade 
     * @author hykim
     * @since 2013-05-21
     * @param eventInfo
     * @param lotData
     * @param machineName
     * @param machineRecipeName
     * @param productCSequence
     * @param lotUdfs
     * @return trackOutLotData
     */
    public Lot trackOutLot(EventInfo eventInfo, Lot lotData, MakeLoggedOutInfo makeLoggedOutInfo) throws CustomException
    {
        try
        {
			String carrierName = makeLoggedOutInfo.getCarrierName();
			Durable durableData = null;
			if (StringUtils.isNotEmpty(carrierName))
			{
				durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				
				if (!eventInfo.getEventTimeKey().isEmpty())
				{
					if (durableData.getLastEventTimeKey().compareTo(eventInfo.getEventTimeKey()) >= 0)
						eventInfo.setEventTimeKey(durableData.getLastEventTimeKey());
				}
			}
            
            lotData = LotServiceProxy.getLotService().makeLoggedOut(lotData.getKey(), eventInfo, makeLoggedOutInfo);
            
            
            // Added by smkang on 2019.03.04 - TrackOut 肚绰 CancelTrackIn 饶 Lot狼 MachineName苞 PortName阑 昏力窍绰 内靛 烙矫 利侩
            lotData.setMachineName("");
            lotData.getUdfs().put("PORTNAME", "");
            LotServiceProxy.getLotService().update(lotData);
            
         /*   // Added by smkang on 2018.10.27 - For synchronization of a carrier state and lot quantity, common method will be invoked.
            try {
            	//if (durableData != null && durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
            	if(durableData!=null && StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, durableData.getDurableState()))
            	{
					Element bodyElement = new Element(SMessageUtil.Body_Tag);
					bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_InUse));
					
					// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
					
					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
            	}
            } catch (Exception e) {
            	log.warn(e);
            }*/
            
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        }
        catch (InvalidStateTransitionSignal ie)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo,"", lotData, "LE",ie.getMessage());
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "", lotData, "LE",fe.getMessage());
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LE",de.getMessage());
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, "",lotData, "LE",ne.getMessage());
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
        
        return lotData;
    }
    
    /**
    * @author jhyeom
    * @since 2014-04-17
    * @return receiveLotData
    */
    public Lot receiveLot(EventInfo eventInfo, Lot lotData, MakeReceivedInfo makeReceivedInfo)
    {
        Lot receiveLotData = LotServiceProxy.getLotService().makeReceived(lotData.getKey(), eventInfo, makeReceivedInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return receiveLotData;
    }
    
    /**
     * receive WIP in TRULY
     * @author swcho
     * @since 2016.03.12
     * @param eventInfo
     * @param lotData
     * @param changeSpecInfo
     * @return
     */
    public Lot receiveLot(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo) throws CustomException
    {
        try
        {
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
            lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
            
            changeSpecInfo.getUdfs().put("RETURNFLOWNAME", lotData.getProcessFlowName());
            changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", lotData.getProcessOperationName());
            changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
            changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
            changeSpecInfo.getUdfs().put("LASTFACTORYNAME", lotData.getFactoryName());
            
            lotData.setLotState(changeSpecInfo.getLotState());
            lotData.setFactoryName(changeSpecInfo.getFactoryName());
            lotData.setProcessOperationName(changeSpecInfo.getProcessOperationName());
            lotData.setAreaName(changeSpecInfo.getAreaName());
            
            SetEventInfo setEventInfo = new SetEventInfo();
            //setEventInfo.setProductUSequence(changeSpecInfo.getProductUdfs());
            setEventInfo.setProductQuantity(lotData.getProductQuantity());
            setEventInfo.setUdfs(changeSpecInfo.getUdfs());
            
            LotServiceProxy.getLotService().update(lotData);
            lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
            
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            return lotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * cancel Receive WIP in TRULY
     * @author swcho
     * @since 2016.05.41
     * @param eventInfo
     * @param lotData
     * @param changeSpecInfo
     * @return
     */
    public Lot cancelReceiveLot(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo) throws CustomException
    {
        try
        {
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
            lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
            
            changeSpecInfo.getUdfs().put("RETURNFLOWNAME", "");
            changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", "");
            changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
            changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
            changeSpecInfo.getUdfs().put("LASTFACTORYNAME", lotData.getFactoryName());
            
            lotData.setLotState(changeSpecInfo.getLotState());
            lotData.setFactoryName(changeSpecInfo.getFactoryName());
            lotData.setProcessOperationName(changeSpecInfo.getProcessOperationName());
            lotData.setAreaName(changeSpecInfo.getAreaName());
            
            SetEventInfo setEventInfo = new SetEventInfo();
            //setEventInfo.setProductUSequence(changeSpecInfo.getProductUdfs());
            setEventInfo.setProductQuantity(lotData.getProductQuantity());
            setEventInfo.setUdfs(changeSpecInfo.getUdfs());
            
            LotServiceProxy.getLotService().update(lotData);
            lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
            
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            return lotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
    * @author jhyeom
    * @since 2014-04-17
    * @return shipLotData
    */
    public Lot shipLot(EventInfo eventInfo, Lot lotData, MakeShippedInfo makeShippedInfo)
    {
        Lot shipLotData = LotServiceProxy.getLotService().makeShipped(lotData.getKey(), eventInfo, makeShippedInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return shipLotData;
    }
    
    /**
    * @author jhyeom
    * @since 2014-04-17
    * @return unShipLotData
    */
    public Lot unShipLot(EventInfo eventInfo, Lot lotData, MakeUnShippedInfo makeUnShippedInfo)
    {
        Lot unShipLotData = LotServiceProxy.getLotService().makeUnShipped(lotData.getKey(), eventInfo, makeUnShippedInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return unShipLotData;
    }

    /**
     * standard operation movement methodology
     * 150305 by swcho : modified
     * @author jhyeom
     * @since 2014-04-18
     * @param eventInfo
     * @param lotData
     * @param changeSpecInfo
     * @return
     * @throws CustomException
     */
    public Lot changeProcessOperation(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo)
        throws CustomException
    {
        try
        {
            Lot changeOperLotData = LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            return changeOperLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /** 
    * @author jhyeom
    * @since 2014-04-24
    * @return assembleLotData 
    */ 
    public Lot startRework(EventInfo eventInfo, Lot lotData, MakeInReworkInfo makeInReworkInfo)
    { 
        Lot startReworkData = LotServiceProxy.getLotService().makeInRework(lotData.getKey(), eventInfo, makeInReworkInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return startReworkData;  
    }
    
    /** 
    * @author jhyeom
    * @since 2014-04-24
    * @return assembleLotData 
    */ 
    public Lot completeRework(EventInfo eventInfo, Lot lotData, MakeNotInReworkInfo makeNotInReworkInfo)
    { 
        Lot completeReworkData = LotServiceProxy.getLotService().makeNotInRework(lotData.getKey(), eventInfo, makeNotInReworkInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return completeReworkData;  
    }
    
    /** 
    * @author jhyeom
    * @since 2014-04-28 
    * @return assembleLotData  
    */ 
    public Lot ChangeGrade(EventInfo eventInfo, Lot lotData, ChangeGradeInfo changeGradeInfo)
    { 
        Lot changeGradeData = LotServiceProxy.getLotService().changeGrade(lotData.getKey(), eventInfo, changeGradeInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return changeGradeData; 
    }
    
    /**
    *140902 by swcho : formalized with standard exception handler
    * @author jhyeom
    * @since 2014-05-23
    * @return assembleLotData 
    */ 
    public Lot makeScrapped(EventInfo eventInfo, Lot lotData, MakeScrappedInfo makeScrappedInfo)
        throws CustomException
    { 
        try
        {
            Lot glassScrapData = LotServiceProxy.getLotService().makeScrapped(lotData.getKey(), eventInfo, makeScrappedInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            // Start 2019.09.09 Add By Park Jeong Su Mantis 4721
            try {
            	this.changeNodeStack111111111111111(glassScrapData.getKey().getLotName());
    		} catch (Exception e) {
    			log.info(glassScrapData.getKey().getLotName() + "ChangeNodeStack Error");
    		}
            // End 2019.09.09 Add By Park Jeong Su Mantis 4721
            return glassScrapData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * unscrap Lot with Product
     * @author swcho
     * @since 2014.09.01
     * @param eventInfo
     * @param lotData
     * @param makeUnScrappedInfo
     * @return
     * @throws CustomException
     */
    public Lot makeUnScrapped(EventInfo eventInfo, Lot lotData, MakeUnScrappedInfo makeUnScrappedInfo)
        throws CustomException
    {
        try
        {
            Lot result = LotServiceProxy.getLotService().makeUnScrapped(lotData.getKey(), eventInfo, makeUnScrappedInfo);
            log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));
            
            return result;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /** 
    * @author hykim
    * @since 2014-05-23
    * @return splitLot 
    */ 
    public Lot splitLot(EventInfo eventInfo, Lot lotData, SplitInfo splitInfo)
    { 
        Lot splitLot = LotServiceProxy.getLotService().split(lotData.getKey(), eventInfo, splitInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        
        return splitLot;  
    }
    
    /** 
    * @author hykim
    * @since 2014-05-23
    * @return mergeLot 
    */ 
    public Lot mergeLot(EventInfo eventInfo, Lot lotData, MergeInfo mergeInfo)
    { 
        Lot mergeLot = LotServiceProxy.getLotService().merge(lotData.getKey(), eventInfo, mergeInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        // Start 2019.09.09 Add By Park Jeong Su Mantis 4721
        try {
        	this.changeNodeStack111111111111111(mergeLot.getKey().getLotName());
		} catch (Exception e) {
			log.info(mergeLot.getKey().getLotName() + "ChangeNodeStack Error");
		}
        // End 2019.09.09 Add By Park Jeong Su Mantis 4721
        return mergeLot;  
    }
    
    /**
     * make Lot released
     * @author swcho
     * @since 2014.04.22
     * @param eventInfo
     * @param lotData
     * @param makeReleasedInfo
     * @param productPGSSequence
     * @return
     * @throws CustomException
     */
    public Lot releaseLot(EventInfo eventInfo, Lot lotData, MakeReleasedInfo makeReleasedInfo, List<ProductPGS> productPGSSequence)
        throws CustomException
    {
    	try
        {
            //by actual Product amount
            if (productPGSSequence != null && productPGSSequence.size() > 0)
            {
                makeReleasedInfo.setProductPGSSequence(productPGSSequence);
                makeReleasedInfo.setProductQuantity(productPGSSequence.size());
            }
            else
            {
                makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
                makeReleasedInfo.setProductQuantity(0);
            }
          
            Lot result = LotServiceProxy.getLotService().makeReleased(lotData.getKey(), eventInfo, makeReleasedInfo);
            
            return result;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    	    
    }
    
    
    /**
     * create and assign new Product list
     * @author swcho
     * @since 2014.04.22
     * @param eventInfo 
     * @param lotData
     * @param transitionInfo
     * @return
     * @throws CustomException
     */
    public Lot assignNewProducts(EventInfo eventInfo, Lot lotData, AssignNewProductsInfo transitionInfo)
        throws CustomException
    {
        try
        {
            Lot result = LotServiceProxy.getLotService().assignNewProducts(lotData.getKey(), eventInfo, transitionInfo);
            
            return result;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage()); 
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * create and assign Carrier
     * @author swcho
     * @since 2014.04.22
     * @param eventInfo
     * @param lotData
     * @param transitionInfo
     * @return
     * @throws CustomException
     */
    public Lot assignCarrier(Lot lotData, AssignCarrierInfo assignCarrierInfo, EventInfo eventInfo)
        throws CustomException 
    {
		try
		{
			// Added by smkang on 2018.12.20 - For synchronization of a carrier.
    		String sourceCarrierName = lotData.getCarrierName();
    		
    		// Added by smkang on 2018.12.20 - Need to check previous DurableState.
    		Durable assignCarrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(assignCarrierInfo.getCarrierName());
    		String previousDurableState = assignCarrierData.getDurableState();
    		
			lotData = LotServiceProxy.getLotService().assignCarrier(lotData.getKey(), eventInfo, assignCarrierInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			
/*			// Added by smkang on 2018.12.20 - Need to check previous DurableState.
        	if (!previousDurableState.equals(GenericServiceProxy.getConstantMap().Dur_InUse)) {
    			// Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
                try {
                	String carrierName = assignCarrierInfo.getCarrierName();

    				Element bodyElement = new Element(SMessageUtil.Body_Tag);
    				bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
    				bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_InUse));
    				
    				// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//    				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
    				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
    				
    				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
                } catch (Exception e) {
                	log.warn(e);
                }
        	}*/
            
/*            // Added by smkang on 2018.12.20 - For synchronization of a carrier.
    		if (StringUtils.isNotEmpty(sourceCarrierName)) {
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
    	        	log.warn(e);
    	        }
    		}*/
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage()); 
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
		
		return lotData;
	}
    
    /**
     * create and deassign Carrier 
     * @author swcho
     * @since 2014.04.22
     * @param eventInfo
     * @param lotData
     * @param transitionInfo
     * @return
     * @throws CustomException
     */
    public Lot deassignCarrier(Lot lotData, DeassignCarrierInfo deassignCarrierInfo, EventInfo eventInfo) throws CustomException 
    {
        try
        {
        	// Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
        	String carrierName = lotData.getCarrierName();
        	
            //timkey adjustmemt for Durable
        	//if (!carrierName.isEmpty())
        	if(!StringUtil.isEmpty(carrierName))
            {
                Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
                
                // if (!eventInfo.getEventTimeKey().isEmpty())
                if(!StringUtil.isEmpty(eventInfo.getEventTimeKey()))
                {
                    if (durableData.getLastEventTimeKey().compareTo(eventInfo.getEventTimeKey()) >= 0)
                        eventInfo.setEventTimeKey(durableData.getLastEventTimeKey());
                }
            }
            
            lotData = LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
            // 2019.02.28
            MESLotServiceProxy.getLotServiceUtil().checkMQCFlow(lotData, carrierName, eventInfo, deassignCarrierInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            /*// Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
            try {
            	// Added by smkang on 2018.10.25 - After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
            	Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
            	//if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
            	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, durableData.getDurableState()))
            	{
					Element bodyElement = new Element(SMessageUtil.Body_Tag);
					bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
					
					// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
					
					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
            	}
            } catch (Exception e) {
            	log.warn(e);
            }*/
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage()); 
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
        
        return lotData;
    }
    
    public Lot deassignCarrierByScrap(Lot lotData, DeassignCarrierInfo deassignCarrierInfo, EventInfo eventInfo) throws CustomException 
    {
        try
        {
        	// Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
        	String carrierName = lotData.getCarrierName();
        	
            //timkey adjustmemt for Durable
        	//if (!carrierName.isEmpty())
        	if(!StringUtil.isEmpty(carrierName))
            {
                Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
                
                // if (!eventInfo.getEventTimeKey().isEmpty())
                if(!StringUtil.isEmpty(eventInfo.getEventTimeKey()))
                {
                    if (durableData.getLastEventTimeKey().compareTo(eventInfo.getEventTimeKey()) >= 0)
                        eventInfo.setEventTimeKey(durableData.getLastEventTimeKey());
                }
            }
            
            lotData = LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
            // 2019.02.28
            //MESLotServiceProxy.getLotServiceUtil().checkMQCFlow(lotData, carrierName, eventInfo, deassignCarrierInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
          /*  // Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
            try {
            	// Added by smkang on 2018.10.25 - After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
            	Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
            	//if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
            	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, durableData.getDurableState()))
            	{
					Element bodyElement = new Element(SMessageUtil.Body_Tag);
					bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
					
					// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
					
					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
            	}
            } catch (Exception e) {
            	log.warn(e);
            }*/
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage()); 
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
        
        return lotData;
    }
    /**
     * relocateProducts
     * @author swcho
     * @since 2014.04.22
     * @param eventInfo
     * @param lotData
     * @param relocateProductsInfo
     * @param eventInfo
     * @return
     * @throws CustomException
     */
    public void relocateProducts(Lot lotData,RelocateProductsInfo relocateProductsInfo, EventInfo eventInfo)
            throws CustomException
            {
        try
        {
            LotServiceProxy.getLotService().relocateProducts(lotData.getKey(),eventInfo, relocateProductsInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
        }
        catch (FrameworkErrorSignal de)
        {
            throw new CustomException("LOT-9999", de.getMessage());
        }
        catch (Exception e) {
            throw new CustomException("LOT-0001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * deleteLotReturnInformation
     * @author jhyeom
     * @since 2014.09.17
     * @param Lot
     * @return 
     * @throws CustomException
     */
    public void deleteLotReturnInformation (Lot lotData)
    throws CustomException
    {
        try
        {
            String sql = " UPDATE LOT " +
                         "   SET RETURNFLOWNAME = '' , " +
                         "       RETURNOPERATIONNAME = '' " +
                         " WHERE LOTNAME = :lotName ";
             
            Map<String,Object> bindMap = new HashMap<String,Object>();
            bindMap.put("lotName" , lotData.getKey().getLotName());

//          greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
            GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
        }
        catch(Exception e)
        {   
        }
    }
    
    
    
    /*
    * Name : MakeReleased
    * Desc : This function is MakeReleased
    * Author : jhyeom
    * Date : 2014.10.16 
    */
    public void MakeReleased(EventInfo eventInfo, Lot lotData,
            String crateName, List<ProductPGS> productPGSSequence) throws CustomException {
        //productPGSSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence_Released(lotData, crateName);
 
        Map<String, String> assignCarrierUdfs = new HashMap<String, String>();

        MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
        makeReleasedInfo.setCarrierName("");
        makeReleasedInfo.setDueDate(lotData.getDueDate());
        makeReleasedInfo.setPriority(lotData.getPriority());
        makeReleasedInfo.setProcessFlowName(lotData.getProcessFlowName());
        makeReleasedInfo.setProcessOperationName(lotData.getProcessOperationName());
        makeReleasedInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
        makeReleasedInfo.setNodeStack(lotData.getNodeStack()); 
        //makeReleasedInfo.setProductPGSSequence(productPGSSequence);
        makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
        makeReleasedInfo.setAssignCarrierUdfs(assignCarrierUdfs);
         
        makeReleasedInfo.setUdfs(lotData.getUdfs());
        
        //by actual Product amount
        if (productPGSSequence != null && productPGSSequence.size() > 0)
        {
            makeReleasedInfo.setProductPGSSequence(productPGSSequence);
            makeReleasedInfo.setProductQuantity(productPGSSequence.size());
        }
        else
        {
            makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
            makeReleasedInfo.setProductQuantity(0);
        }

        LotServiceProxy.getLotService().makeReleased(lotData.getKey(),eventInfo, makeReleasedInfo);
        log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
    }
    
    /*
    * Name : MakeEmptied
    * Desc : This function is MakeEmptied
    * Author : hykim
    * Date : 2014.10.16 
    */
    public void MakeEmptied(EventInfo eventInfo, Lot lotData, List<ProductU> productUSequence, Map<String,String> deassignCarrierUdfs) 
    throws CustomException 
    {
        MakeEmptiedInfo makeEmptiedInfo = new MakeEmptiedInfo();
        makeEmptiedInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
        //makeEmptiedInfo.setProductUdfs(productUSequence);
        makeEmptiedInfo.setUdfs(lotData.getUdfs());
        
        LotServiceProxy.getLotService().makeEmptied(lotData.getKey(), eventInfo, makeEmptiedInfo);
    }
    
    /**
     * ChangeSpec for Lot
     * 160422 by hwlee89 : created
     * @author hwlee89
     * @since 2016-04-22
     * @param eventInfo
     * @param lotData
     * @param changeSpecInfo
     * @return
     * @throws CustomException
     */
    public Lot changeProductSpec(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo)
        throws CustomException
    {
        try
        {
            Lot changeOperLotData = LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);
            log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
            
            return changeOperLotData;
        }
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }
    }
    
    /**
     * Recreate Lot And All Products
     * @author 170426 add by lszhen
     * @param eventInfo
     * @param lotData
     * @param recreateAndCreateAllProductsInfo
     * @return
     * @throws CustomException
     */
    public Lot recreateAndCreateAllProducts(EventInfo eventInfo, Lot lotData, RecreateAndCreateAllProductsInfo recreateAndCreateAllProductsInfo)
        throws CustomException
    {
        try 
        {
            Lot recreateLot = LotServiceProxy.getLotService().recreateAndCreateAllProducts(lotData.getKey(), eventInfo, recreateAndCreateAllProductsInfo);
            log.info("Event Name= " + eventInfo.getEventName() + ", EventTimeKey= " + eventInfo.getEventTimeKey());
            
            return recreateLot;
        } 
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }               
    }
    
    /**
     * Recreate select products
     * @author 170427 add by lszhen
     * @param eventInfo
     * @param lotData
     * @param recreateProductsInfo
     * @throws CustomException
     */
    public Lot recreateProducts(EventInfo eventInfo, Lot lotData, RecreateProductsInfo recreateProductsInfo) throws CustomException
    {
        try
        {
            Lot recreateProductLot = LotServiceProxy.getLotService().recreateProducts(lotData.getKey(), eventInfo, recreateProductsInfo);
            log.info("Event Name= " + eventInfo.getEventName() + ", EventTimeKey= " + eventInfo.getEventTimeKey());
            
            return recreateProductLot;
        } 
        catch (InvalidStateTransitionSignal ie)
        {
            throw new CustomException("LOT-9003", lotData.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            throw new CustomException("LOT-9999", fe.getMessage());
        }
        catch (DuplicateNameSignal de)
        {
            throw new CustomException("LOT-9002", lotData.getKey().getLotName());
        }
        catch (NotFoundSignal ne)
        {
            throw new CustomException("LOT-9001", lotData.getKey().getLotName());
        }       
    }

    /** 
     * @author hsryu
     * @since 2018-02-22
     * @return assembleLotData 
     */ 
    public Lot cancelRework(EventInfo eventInfo, Lot lotData, MakeNotInReworkInfo makeNotInReworkInfo)
    { 
    	Lot completeReworkData = LotServiceProxy.getLotService().makeNotInRework(lotData.getKey(), eventInfo, makeNotInReworkInfo);
    	log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

    	return completeReworkData;  
    }
    
    public Lot checkYield(String lotName, String productSpecName, String ecCode, String processFlowName, String processFlowVersion, String processOperationName, EventInfo eventInfo) throws CustomException
    {
    	YieldInfo yieldInfo = null;
    	boolean lotYieldHoldFlag = false;
    	String holdComment = "";

    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

    	try
    	{
    		yieldInfo = ExtendedObjectProxy.getYieldInfoService().selectByKey(false,new Object[] {productSpecName, ecCode,processFlowName, processFlowVersion, processOperationName});
    	}
    	catch (Exception ex)
    	{
    		yieldInfo = null;
    	}

    	if(yieldInfo==null)
    	{
    		try
    		{
    			yieldInfo = ExtendedObjectProxy.getYieldInfoService().selectByKey(false,new Object[] {productSpecName, "*" , processFlowName , processFlowVersion, processOperationName});
    		}
    		catch (Exception ex)
    		{
    			yieldInfo = null;
    		}
    	}

    	if(yieldInfo != null)
    	{
    		boolean sheetYieldFlag = false;
    		boolean lotYieldFlag = false;

    		//2019.03.19_hsryu_only check Received FileJudge Glass.
    		//List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);
    		List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().getReceivedFileJudgeProduct(lotName);
    				
    		if(pProductList.size() > 0)
    		{
    			// not use LotYieldFlag !!
//    			if(StringUtil.equals(yieldInfo.getLotFlag(), "Y"))
//    			{
//    				double oNum = 0;
//    				double xNum = 0;
//    				double gNum = 0;
//    				double otherGradeNum = 0;
//
//    				double lotYield = yieldInfo.getLotYield();
//
//    				for(Product productData : pProductList)
//    				{
//    					List<PanelJudge> panelJudgeList = new ArrayList<PanelJudge>();
//
//    					String condition = "WHERE glassName = ? ";
//    					Object[] bindSet = new Object[]{ productData.getKey().getProductName() };
//
//    					try
//    					{
//    						panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);
//
//    						for(int i=0; i<panelJudgeList.size();i++)
//    						{
//    							String panelJudge = panelJudgeList.get(i).getPanelJudge();
//
//    							if(StringUtil.equals(panelJudge, "O"))
//    							{
//    								oNum++;
//    							}
//    							else if(StringUtil.equals(panelJudge, "X"))
//    							{
//    								xNum++;
//    							}
//    							else if(StringUtil.equals(panelJudge, "G"))
//    							{
//    								gNum++;
//    							}
//    							else if((!StringUtil.equals(panelJudge, "G"))&&(!StringUtil.equals(panelJudge, "O")&&(!StringUtil.equals(panelJudge, "X"))))
//    							{
//    								otherGradeNum ++;
//    							}
//    						}
//    					}
//    					catch(Throwable e)
//    					{
//    						log.info("Not Panel in Glass ["+productData.getKey().getProductName()+"]");
//    					}
//    				}
//
//    				if(oNum!=0)
//    				{
//    					if(oNum/(oNum+xNum)<lotYield)
//    					{
//    						lotYieldHoldFlag = true;
//    						holdComment = "Lot Yield Setting : " + lotYield + ", Lot Yield Result : " + (long)oNum/(oNum+otherGradeNum);
//    						log.info("lotYield is Too bad...");
//    					}
//    				}
//    				else
//    				{
//    					log.info("Judge 'O' Quantity is zero.. Calculate by the quantity of 'G' ");
//    					if(gNum/(gNum+xNum)<lotYield)
//    					{
//    						lotYieldHoldFlag = true;
//    						holdComment = "Lot Yield Setting : " + lotYield + ", Lot Yield Result : " + (long)gNum/(gNum+otherGradeNum);
//    						log.info("lotYield is Too bad...");
//    					}
//    				}
//    			}

    			if(StringUtil.equals(yieldInfo.getSheetFlag(), "Y"))
    			{
    				double sheetYield = yieldInfo.getSheetYield();

    				for(Product productData : pProductList)
    				{
    					double oNum = 0;
    					double xNum = 0;
    					double gNum = 0;
    					double otherGradeNum = 0;
        		    	boolean sheetYieldHoldFlag = false;

    					List<PanelJudge> panelJudgeList = new ArrayList<PanelJudge>();

    					String condition = "WHERE glassName = ? ";
    					Object[] bindSet = new Object[]{ productData.getKey().getProductName() };

    					try
    					{
    						panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(condition, bindSet);

    						for(int i=0; i < panelJudgeList.size();i++)
    						{
    							String panelJudge = panelJudgeList.get(i).getPanelJudge();

    							if(StringUtil.equals(panelJudge, "O"))
    							{
    								oNum++;
    							}
    							else if(StringUtil.equals(panelJudge, "X"))
    							{
    								xNum++;
    							}
    							else if(StringUtil.equals(panelJudge, "G"))
    							{
    								gNum++;
    							}
    							else if((!StringUtil.equals(panelJudge, "G"))&&(!StringUtil.equals(panelJudge, "O")&&(!StringUtil.equals(panelJudge, "X"))))
    							{
    								otherGradeNum ++;
    							}
    						}

    						if(oNum!=0)
    						{
    							if(oNum/(oNum+xNum+otherGradeNum)<sheetYield)
    							{
    								sheetYieldHoldFlag = true;
    								
    								// 2019.03.19_hsryu_Modify HoldNote!
    								holdComment = "ProductID:[" + productData.getKey().getProductName() + "],SettingYield：[" + sheetYield + "],Current Yield:[" + (long)oNum/(oNum+xNum+otherGradeNum) + "]";
    								//holdComment = "sheetYieldHoldFlag Yield Setting : " + sheetYield + ", Lot Yield Result : " + (long)oNum/(oNum+otherGradeNum);
    								log.info("sheetYieldHoldFlag is Too bad...");
    							}
    						}
    						else if(gNum != 0) 
    						{
    							log.info("Judge 'O' Quantity is zero.. Calculate by the quantity of 'G' ");
    							if(gNum/(gNum+xNum+otherGradeNum)<sheetYield)
    							{
    								sheetYieldHoldFlag = true;
    								
    								// 2019.03.19_hsryu_Modify HoldNote!
    								holdComment = "ProductID:[" + productData.getKey().getProductName() + "],SettingYield：[" + sheetYield + "],Current Yield:[" + (long)gNum/(gNum+xNum+otherGradeNum) + "]";
    								//holdComment = "sheetYieldHoldFlag Yield Setting : " + sheetYield + ", Lot Yield Result : " + (long)gNum/(gNum+otherGradeNum);
    								log.info("sheetYieldHoldFlag is Too bad...");
    							}
    						}
    						else
    						{
    							log.info(" Judge 'O' & Judge 'G' is zero.. all 'R' or 'X'! ");
								sheetYieldHoldFlag = true;
								// 2019.03.19_hsryu_Modify HoldNote!
								holdComment = "ProductID:[" + productData.getKey().getProductName() + "],SettingYield：[" + sheetYield + "],Current Yield:[0.0]";
								//holdComment = "sheetYieldHoldFlag Yield Setting : " + sheetYield + ", Lot Yield Result : " + (long)gNum/(gNum+otherGradeNum);
								log.info("sheetYieldHoldFlag is Too bad...");
    						}
    						
    						if(sheetYieldHoldFlag){
        						try {
        	        				// 2019.03.19_hsryu_Next Operation(DUMMY) AHold.
        	    					ProcessOperationSpec nextProcessOperation = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());

        	    					if(StringUtils.equals(nextProcessOperation.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_DUMMY)) {
        	    	    				EventInfo yieldEventInfo = EventInfoUtil.makeEventInfo("YieldHold", eventInfo.getEventUser(), holdComment, GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "TSTH");
        								MESLotServiceProxy.getLotServiceUtil().reserveAHoldByOtherOperation(lotData, lotData.getProcessFlowName(), nextProcessOperation.getKey().getProcessOperationName(),"INT", yieldEventInfo);
        	    	    				//MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, "INT", firstEventInfo);
        	    					}
        	    				}
        	    				catch(Throwable e) {
        	    					log.warn("Fail Reserve Yield AHold.");;
        	    				}
    						}
    					}
    					catch(Throwable e)
    					{
    						log.info("Not Panel in Glass ["+productData.getKey().getProductName()+"]");
    					}
    				}
    			}

//    			if(lotYieldHoldFlag||sheetYieldHoldFlag)
//    			{
//    				try {
//        				// 2019.03.19_hsryu_Next Operation(DUMMY) AHold.
//    					ProcessOperationSpec nextProcessOperation = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
//
//    					if(StringUtils.equals(nextProcessOperation.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_DUMMY)) {
//    	    				EventInfo yieldEventInfo = EventInfoUtil.makeEventInfo("YieldHold", eventInfo.getEventUser(), holdComment, GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, "TSTH");
//							MESLotServiceProxy.getLotServiceUtil().reserveAHoldByOtherOperation(lotData, lotData.getProcessFlowName(), nextProcessOperation.getKey().getProcessOperationName(),"INT", yieldEventInfo);
//    	    				//MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, "INT", firstEventInfo);
//    					}
//    				}
//    				catch(Throwable e) {
//    					log.warn("Fail Reserve Yield AHold.");;
//    				}
//					
//
    				/*              eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                    eventInfo.setReasonCode("TSTH");
                    eventInfo.setReasonCodeType("HoldLot");
                    eventInfo.setEventName("Hold");
                    eventInfo.setEventComment(holdComment);

                    if(!lotData.getLotHoldState().equals("Y"))
                    {
                        List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
                        MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo(productUSequence);
                        LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

                        // -------------------------------------------------------------------------------------------------------------------------------------------
                        // Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//                      Map<String,String> multiHoldudfs = new HashMap<String, String>();
//                      //2018.05.09 dmlee : To Be Modify EventUserDep
//                      multiHoldudfs.put("eventuserdep", "INT");
    //
//                      LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//                      multiholdkey.setLotName(lotData.getKey().getLotName());
//                      multiholdkey.setReasonCode("TSTH");
    //
//                      LotMultiHold multihold = LotServiceProxy.getLotMultiHoldService().selectByKey(multiholdkey);
//                      multihold.setUdfs(multiHoldudfs);
    //
//                      LotServiceProxy.getLotMultiHoldService().update(multihold);
                        try {
                            addMultiHoldLot(lotData.getKey().getLotName(), "TSTH", "INT","AHOLD", eventInfo);
                        } catch (Exception e) {
                            log.warn(e);
                        }
                        // -------------------------------------------------------------------------------------------------------------------------------------------
                    }
                    else
                    {
                        SetEventInfo setEventInfo = new SetEventInfo();
                        LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

                        // -------------------------------------------------------------------------------------------------------------------------------------------
                        // Modified by smkang on 2018.08.13 - According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
//                      LotMultiHoldKey multiholdkey = new LotMultiHoldKey();
//                      multiholdkey.setLotName(lotData.getKey().getLotName());
//                      multiholdkey.setReasonCode("TSTH");
    //
//                      LotMultiHold multihold = new LotMultiHold();
    //
//                      Map<String,String> multiHoldudfs = new HashMap<String, String>();
//                      //2018.05.09 dmlee : To Be Modify EventUserDep
//                      multiHoldudfs.put("eventuserdep", "INT");
    //
//                      multihold.setKey(multiholdkey);
//                      multihold.setUdfs(multiHoldudfs);
//                      multihold.setEventTime(eventInfo.getEventTime());
//                      multihold.setEventName(eventInfo.getEventName());
//                      multihold.setEventUser(eventInfo.getEventUser());
//                      LotServiceProxy.getLotMultiHoldService().insert(multihold);
                        try {
                            addMultiHoldLot(lotData.getKey().getLotName(), "TSTH", "INT","AHOLD", eventInfo);
                        } catch (Exception e) {
                            log.warn(e);
                        }
                        // -------------------------------------------------------------------------------------------------------------------------------------------

                        List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

                        for(ProductU product : productUSequence)
                        {
                            Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
                            kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                            ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), eventInfo, setEventInfoP);

                            // -------------------------------------------------------------------------------------------------------------------------------------------
                            // Modified by smkang on 2018.08.13 - According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
//                          ProductMultiHoldKey prdMultiHoldKey = new ProductMultiHoldKey();
//                          prdMultiHoldKey.setProductName(product.getProductName());
//                          prdMultiHoldKey.setReasonCode("TSTH");
    //
//                          ProductMultiHold prdMultiHold = new ProductMultiHold();
    //
//                          prdMultiHold.setKey(prdMultiHoldKey);
//                          prdMultiHold.setEventTime(eventInfo.getEventTime());
//                          prdMultiHold.setEventName(eventInfo.getEventName());
//                          prdMultiHold.setEventUser(eventInfo.getEventUser());
    //
//                          prdMultiHold.setUdfs(multiHoldudfs);
    //
//                          ProductServiceProxy.getProductMultiHoldService().insert(prdMultiHold);
                            try {
                                MESProductServiceProxy.getProductServiceImpl().addMultiHoldProduct(product.getProductName(), "TSTH", "INT","", eventInfo);
                            } catch (Exception e) {
                                log.warn(e);
                            }
                            // -------------------------------------------------------------------------------------------------------------------------------------------
                        }
                    }*/
//    			}
    		}
    	}


    	return lotData;
    }

    /**
     * 
     * @Name     LotProcessStartTrackIn
     * @since    2018. 8. 11.
     * @author   hhlee
     * @contents LotProcessStarted(TrackIn)
     *           
     * @param doc
     * @param eventInfo
     * @return
     * @throws CustomException
     */
    public Lot LotProcessStartTrackIn(org.jdom.Document doc, EventInfo eventInfo) throws CustomException
    {
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
        String lotName = StringUtil.EMPTY;
        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
        String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
        
        /* 20180928, add,  Sorter Job Validation ==>> */
        String sortJobName = SMessageUtil.getBodyItemValue(doc, "SORTJOBNAME", false);
        String sortJobType = SMessageUtil.getBodyItemValue(doc, "SORTJOBTYPE", false);
        String sortTransFerDirection = SMessageUtil.getBodyItemValue(doc, "SORTTRANSFERDIRECTION", false);
        /* <<== 20180928, add,  Sorter Job Validation */
        
        boolean firstLotFlag = false;
        
        lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
        
        //EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

       //Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);   //add by GJJ 20200406, mantis:5968 add rowlock    
		//add by GJJ 20200406, mantis:5968 add rowlockstart
		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);
		Machine machineData = MachineServiceProxy.getMachineService().selectByKeyForUpdate(machineKey);
		//add by GJJ 20200406, mantis:5968 add rowlock end
        
        
        Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

        /* 20181106, hhlee, delete, validata deleted machine state at start [CSTInfoDownLoadSend, StartCSTInfoCheckRequest, request by Guishi] ==>> */
        //CommonValidation.ChekcMachinState(machineData);
        /* <<== 20181106, hhlee, delete, validata deleted machine state at start [CSTInfoDownLoadSend, StartCSTInfoCheckRequest, request by Guishi] */
        
        /* 20190105, hhlee, add, add lot validation ==>> */
        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
        /* <<== 20190105, hhlee, add, add lot validation */
        
        
        /* 20190125, hhlee, add, check LotState/ProcessState/HoldLotState ==>> */
        CommonValidation.checkLotState(lotData);
        CommonValidation.checkLotProcessState(lotData);
        CommonValidation.checkLotHoldState(lotData);
        /* <<== 20190125, hhlee, add, check LotState/ProcessState/HoldLotState */
        
        List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);

        /* 20181220, hhlee, add, add MachineGroup Validation ==>> */        
        ProcessOperationSpec machineOperationData = CommonUtil.getProcessOperationSpec(lotList.get(0).getFactoryName(), lotList.get(0).getProcessOperationName());
        String operationMachineGroup = machineOperationData.getUdfs().get("MACHINEGROUPNAME");
        String machineGroup = MESMachineServiceProxy.getMachineServiceUtil().getMachineOperationGroup(machineData.getKey().getMachineName(), operationMachineGroup);        
        /* <<== 20181220, hhlee, add, add MachineGroup Validation */
        
        if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_SORTING)&&
        		StringUtil.equals("PS", CommonUtil.getValue(portData.getUdfs(), "PORTTYPE")))
        {            
        }
        else
        {
            MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotList.get(0));
        }
        
        /* 20181023, hhlee, delete ==>> */
        //String mesMachineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotList.get(0).getFactoryName(), lotList.get(0).getProductSpecName(),
        //        lotList.get(0).getProcessFlowName(), lotList.get(0).getProcessOperationName(), machineName, lotList.get(0).getUdfs().get("ECCODE"));
        //
        //
        //if (!StringUtil.equals(machineRecipeName, mesMachineRecipeName))
        //{
        //    //throw new CustomException("MACHINE-0103", machineRecipeName, machineName, "");
        //    log.warn(String.format("MachineReciep[%s] and MESMachieRecipe [%s] mismatch!" ,machineRecipeName, mesMachineRecipeName));
        //}
        /* <<== 20181023, hhlee, delete */
        
        List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

        //20180504, kyjung, QTime
        for (Product productData : productDataList )
        {
            MESProductServiceProxy.getProductServiceImpl().ExitedQTime(eventInfo, productData, "TrackIn");
            
            /* 20181212, hhlee, resetProcessedOperationBeforeAOI ==>> */
            MESProductServiceProxy.getProductServiceImpl().resetProcessedOperationBeforeAOI(eventInfo, productData.getKey().getProductName());
            /* <<== 20181212, hhlee, resetProcessedOperationBeforeAOI */
            
        }

        List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
        
        // Added by smkang on 2018.09.03 - According to EDO's request, PortName should be updated.
        for (ProductC productC : productCSequence) {
            productC.getUdfs().put("PORTNAME", portName);
			//2018.12.21_hsryu_after TrackIn, ProcessingInfo is null. Requested by CIM.
			productC.getUdfs().put("PROCESSINGINFO", "");
        }

        Map<String, String> lotUdfs = lotList.get(0).getUdfs();
        lotUdfs.put("PORTNAME", portData.getKey().getPortName());
        lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
        lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
        
        // 2019.04.10_hsryu_Delete Logic. Reqeusted Report.
        /* 20190212, hhlee, add, lot holdtime, holdreleasetime ==>> */
        //lotUdfs.put("HOLDTIME",StringUtil.EMPTY);
        //lotUdfs.put("HOLDRELEASETIME",StringUtil.EMPTY);
        /* <<== 20190212, hhlee, add, lot holdtime, holdreleasetime */
        
        // Added by smkang on 2019.01.23 - According to Liu Hongwei's request, requester of transport job should be recorded in Lot and LotHistory.
 		try {
 			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotList.get(0).getCarrierName());
 			lotUdfs.put("TRANSPORTREQUESTER", durableData.getUdfs().get("TRANSPORTREQUESTER"));
 		} catch (Exception e) {
 			// TODO: handle exception
 			log.info(e);
 		}

        MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, machineRecipeName, productCSequence, lotUdfs);

        /* 20180928, add,  Sorter Job EventName, LotNote ==>> */
        String trackInEventName = "TrackIn";
        String trackInEventComment = StringUtil.EMPTY;
        
        if(StringUtil.isNotEmpty(sortJobName))
        {
            trackInEventComment = ExtendedObjectProxy.getSortJobService().setSortEventComment(carrierName, sortJobName, sortJobType, 
                    sortTransFerDirection, trackInEventName, GenericServiceProxy.getConstantMap().Flag_N);
            eventInfo.setEventComment(trackInEventComment);
            //String sortLotNote = ExtendedObjectProxy.getSortJobService().setSortLotNote(carrierName, sortJobName, sortJobType, sortTransFerDirection, trackInEventName);
            //lotUdfs.put("NOTE", sortLotNote);
        }
        /* <<== 20180928, add,  Sorter Job EventName, LotNote */
        
        eventInfo.setEventName(trackInEventName);       
        
        
        Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotList.get(0), makeLoggedInInfo);
        
        // 20180612, kyjung, Recipe Idle Time
        /*
        if(StringUtil.isEmpty(carrierName))
        {
            MESProductServiceProxy.getProductServiceImpl().firstFlagRecipeIdleTimeLot(trackInLot.getKey().getLotName(), 
                    carrierName, 
                    machineName, 
                    mesMachineRecipeName, 
                    trackInLot.getProductSpecName(), 
                    trackInLot.getProcessOperationName(), 
                    eventInfo);
        }
        */
        
        /** 2018.12.20_hsryu_Insert ExecuteSampleLot Logic! **/
		trackInLot = MESLotServiceProxy.getLotServiceUtil().executeSampleLot(trackInLot);
        
        if(!StringUtil.equals(trackInLot.getProductionType(), "MQCA"))
        {
            if(!StringUtil.isEmpty(carrierName))
            {
                MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(
                        doc,
                        trackInLot.getFactoryName(),
                        machineData.getAreaName(),
                        trackInLot.getKey().getLotName(), 
                        carrierName, 
                        machineName, 
                        portData.getKey().getPortName(),
                        machineRecipeName, 
                        eventInfo);
            }
        }


        if(!StringUtil.isEmpty(trackInLot.getCarrierName()))
        {
            // IncrementTimeUsed For Carrier by hwlee89
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
            Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));

            IncrementTimeUsedInfo incrementTimeUsedInfo = MESDurableServiceProxy.getDurableInfoUtil().incrementTimeUsedInfo(durableData, 1);
            
            eventInfo.setEventName("Use");
            durableData = MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUsed(durableData, incrementTimeUsedInfo, eventInfo);
        }

        if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL") && StringUtils.isNotEmpty(trackInLot.getCarrierName()))
        {
            try
            {
                /* 20181128, hhlee, EventTime Sync */
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                eventInfo.setCheckTimekeyValidation(false);
                deassignCarrier(eventInfo, trackInLot);
            }
            catch (CustomException ce)
            {
                log.error("Deassign failed");
            }
        }
        
        if(!StringUtil.isEmpty(machineRecipeName))
        {
            eventInfo.setEventName("TrackIn");
            Map<String, String> machineUdfs = machineData.getUdfs();
            machineUdfs.put("MACHINERECIPENAME", machineRecipeName);
            kr.co.aim.greentrack.machine.management.info.SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineUdfs);
            
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
        }   
        
        // 2019.03.18_hsryu_Insert Logic. For TrackOutLogic.
        trackInLot = MESLotServiceProxy.getLotServiceUtil().getLotData(trackInLot.getKey().getLotName());
        
		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(trackInLot.getKey().getLotName(), trackInLot.getFactoryName(), trackInLot.getProcessFlowName(),trackInLot.getProcessOperationName(), eventInfo);

        
        /* 20181018, hhlee, delete, this logic must be existed the end of LotInfoDownloadRequestNew/StartInfoCheckRequest ==>> */
        ///* 20181001, hhlee, modify, location move, lotdata change ==>> */
        ////Added by jjyoo on 2018.9.20 - Check Inhibit Condition
        ////this logic must be existed the end of LotInfoDownloadRequestNew.
        //MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition( eventInfo,  trackInLot,  machineName,  
        //        machineRecipeName, GenericServiceProxy.getConstantMap().Flag_Y);
        ///* 20181001, hhlee, modify, location move, lotdata change ==>> */
        /* <<== 20181018, hhlee, delete, this logic must be existed the end of LotInfoDownloadRequestNew/StartInfoCheckRequest */
        
        return trackInLot;
    }
    
    /**
     * 
     * @Name     LotProcessStartTrackInbyEmptyCassette
     * @since    2019. 1. 25.
     * @author   hhlee
     * @contents 
     *           
     * @param doc
     * @param eventInfo
     * @throws CustomException
     */
    public void LotProcessStartTrackInbyEmptyCassette(org.jdom.Document doc, EventInfo eventInfo) throws CustomException
    {
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);

        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
        Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

        /* 20190329, hhlee, delete, chnage function ==>> */
        //if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "CARRIERNAME") , carrierName))
        //{
        //    this.makePortWorking(eventInfo, portData);
        //}
        //else
        //{
        //    throw new CustomException("CST-0017", portName, carrierName);
        //}
        /* <<== 20190329, hhlee, delete, chnage function */
        
        //try
        //{
        //    GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
        //}
        //catch(Exception ex)
        //{
        //    log.warn("FMC Report Failed!");
        //}
    }
    
    /**
     * 
     * @Name     makePortWorking
     * @since    2019. 1. 25.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param portData
     */
    public void makePortWorking(EventInfo eventInfo, Port portData)
    {
        try
        {
            if( !StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_Processing) )
            {
                eventInfo.setEventName("ChangeTransferState");

                MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
                makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
                makeTransferStateInfo.setValidateEventFlag("N");
                makeTransferStateInfo.setUdfs(portData.getUdfs());

                MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
            }
        }
        catch (Exception ex)
        {
            log.error("Port handling is failed");
        }
    }
    
    /**
     * 
     * @Name     deassignCarrier
     * @since    2018. 8. 11.
     * @author   hhlee
     * @contents DeAssign Carrier
     *           
     * @param eventInfo
     * @param lotData
     * @throws CustomException
     */
    private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
    {
        Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

        if(StringUtils.equals(carrierData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
        {
        	String carrierName = lotData.getCarrierName();
        	
            eventInfo.setEventName("DeassignCarrier");

            List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

            DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);
            LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
            
            // 2019.03.04 - JSPARK
            MESLotServiceProxy.getLotServiceUtil().checkMQCFlow(lotData, carrierName, eventInfo, deassignCarrierInfo);
            
            // Added by smkang on 2018.10.23 - For synchronization of a carrier state and lot quantity, common method will be invoked.
/*            try {
            	// Added by smkang on 2018.10.25 - After deassignCarrier is executed, it is necessary to check this carrier is really changed to Available state.
            	Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierData.getKey().getDurableName());
            	//if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
            	if(StringUtil.equals(GenericServiceProxy.getConstantMap().Dur_Available, durableData.getDurableState()))
            	{
					Element bodyElement = new Element(SMessageUtil.Body_Tag);
					bodyElement.addContent(new Element("DURABLENAME").setText(carrierData.getKey().getDurableName()));
					bodyElement.addContent(new Element("DURABLESTATE").setText(GenericServiceProxy.getConstantMap().Dur_Available));
					
					// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
					
					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierData.getKey().getDurableName());
            	}
            } catch (Exception e) {
            	log.warn(e);
            }*/
        }
    }
    
    public Lot setHoldInfo(String lotName, String department,EventInfo eventInfo) throws CustomException {
    	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
    	List<LotMultiHold> lotMultiHoldList = new ArrayList<LotMultiHold>();

    	try
    	{
    		lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(" WHERE lotName = ? ", new Object[] {lotName});
    	}
    	catch(Throwable e)
    	{
    		log.info("Not exist. Start create HoldTime!");
    	}
    	
    	if( lotMultiHoldList==null || lotMultiHoldList.size() == 0 )
    	{
    		try{
    			Map<String, String> lotUdfs = lotData.getUdfs();
    			lotUdfs.put("HOLDTIME", eventInfo.getEventTime().toString());
    			lotData.setUdfs(lotUdfs);

    			LotServiceProxy.getLotService().update(lotData);
    		}
    		catch(Throwable e)
    		{
    			log.info("HoldDuration Error!");
    		}
    	}
    	
    	// 2019.04.10_hsryu_add try&Catch.
    	try {
    		LotHistoryKey keyInfo = new LotHistoryKey();
    		keyInfo.setLotName(lotData.getKey().getLotName());
    		keyInfo.setTimeKey(eventInfo.getEventTimeKey());
    		
    		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    		LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(keyInfo);
    		LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(keyInfo);

    		Map<String, String> hUdfs = lotHistory.getUdfs();
    		hUdfs.put("HOLDDEPARTMENT", department);
        	if( lotMultiHoldList==null || lotMultiHoldList.size() == 0 )
        	{
        		hUdfs.put("HOLDTIME", eventInfo.getEventTime().toString());
        	}
    		LotServiceProxy.getLotHistoryService().update(lotHistory);
    	}
    	catch(Throwable e){
    		log.warn("Fail update HoldDepartment");
    	}
    	
    	return lotData;
    }
    
    /**
     * @author smkang
     * @since 2018.08.13
     * @param lotName
     * @param reasonCode
     * @param department
     * @param eventInfo
     * @throws CustomException
     * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
     */
    public Lot addMultiHoldLot(String lotName, String reasonCode, String department, String holdType, EventInfo eventInfo) throws CustomException {

    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    	
    	//2019.02.19_hsryu_Insert Logic. Mantis 0002775.
    	lotData = this.setHoldInfo(lotName ,department, eventInfo);
    	
    	long lastPosition=  0;

    	if(StringUtil.isEmpty(department))
    	{
    		department = " ";
    	}
    	
    	try {
    		// 2019.03.20_hsryu_Memory HoldEvent in Product History. requested by CIM.
    		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

    		for(ProductU product : productUSequence)
    		{
    			Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
    			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
    			ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), eventInfo, setEventInfoP);
    			// -------------------------------------------------------------------------------------------------------------------------------------------
    		}
    	}
    	catch(Throwable e){
    		log.info("Fail Memory ProductHistory ! ");
    	}

    	List<LotMultiHold> lotMultiHoldList = new ArrayList<LotMultiHold>();

    	String condition = " WHERE lotName = ? AND reasonCode = ? AND department = ? AND eventComment = ? ";
    	Object[] bindSet = new Object[] {lotName, reasonCode, department, eventInfo.getEventComment()};

    	try
    	{
    		lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
    	}
    	catch(Throwable e)
    	{
    		log.info("Not exsit. Start create Hold!");
    	}

    	lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastSeqOfLotAction(lotName, reasonCode, department, eventInfo.getEventComment()));

    	/* 20181212, hhlee, ==>> */
    	log.info("[                 LASTPOSITION] : " + String.valueOf(lastPosition));

    	log.info(lotName + "," + reasonCode + "," + department + "," + String.valueOf(lastPosition + 1) + "," + holdType + "," + eventInfo.getEventComment());

    	if(StringUtil.isEmpty(eventInfo.getEventComment()))
    	{
    		eventInfo.setEventComment("Hold");
    		/* 20181212, hhlee, ==>> */
    		log.info("[    EVENTCOMMENT IS EMPTY]====================================================== ");			    
    	}			
    	if(eventInfo.getEventTime() == null)
    	{
    		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
    		/* 20181212, hhlee, ==>> */
    		log.info("[        EVENTTIME IS NULL]====================================================== ");
    	}

    	// TODO: handle exception
    	LotMultiHold lotMultiHoldData = new LotMultiHold();

    	lotMultiHoldData.setLotName(lotName);
    	lotMultiHoldData.setReasonCode(reasonCode);
    	lotMultiHoldData.setDepartment(department);
    	lotMultiHoldData.setSeq(lastPosition+1);
    	lotMultiHoldData.setHoldType(holdType);
    	lotMultiHoldData.setEventComment(eventInfo.getEventComment());
    	lotMultiHoldData.setEventTime(eventInfo.getEventTime());
    	lotMultiHoldData.setEventName(eventInfo.getEventName());
    	lotMultiHoldData.setEventUser(eventInfo.getEventUser());
    	lotMultiHoldData.setEventTimeKey(eventInfo.getEventTimeKey());

    	/* 20181212, hhlee, ==>> */
    	log.info("[   BEFORE MULTIHOLD CREATE] ===================================================== " );

    	ExtendedObjectProxy.getLotMultiHoldService().create(eventInfo, lotMultiHoldData);

    	/* 20181212, hhlee, ==>> */
    	log.info("[    AFTER MULTIHOLD CREATE] ===================================================== " );
    	
    	return lotData;
    }
    /**
     * @author ParkJeongSu
     * @since 2019.04.08
     * @param lotName
     * @param reasonCode
     * @param department
     * @param eventInfo
     * @param resetFlag
     * @throws CustomException
     * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
     */
    public Lot addMultiHoldLot(String lotName, String reasonCode, String department, String holdType, EventInfo eventInfo,String resetFlag) throws CustomException {

    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    	
    	//2019.02.19_hsryu_Insert Logic. Mantis 0002775.
    	lotData = this.setHoldInfo(lotName ,department, eventInfo);
    	
    	long lastPosition=  0;

    	if(StringUtil.isEmpty(department))
    	{
    		department = " ";
    	}
    	
    	try {
    		// 2019.03.20_hsryu_Memory HoldEvent in Product History. requested by CIM.
    		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

    		for(ProductU product : productUSequence)
    		{
    			Product aProduct =  MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getProductName());
    			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoP = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
    			ProductServiceProxy.getProductService().setEvent(aProduct.getKey(), eventInfo, setEventInfoP);
    			// -------------------------------------------------------------------------------------------------------------------------------------------
    		}
    	}
    	catch(Throwable e){
    		log.info("Fail Memory ProductHistory ! ");
    	}

    	List<LotMultiHold> lotMultiHoldList = new ArrayList<LotMultiHold>();

    	String condition = " WHERE lotName = ? AND reasonCode = ? AND department = ? AND eventComment = ? ";
    	Object[] bindSet = new Object[] {lotName, reasonCode, department, eventInfo.getEventComment()};

    	try
    	{
    		lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
    	}
    	catch(Throwable e)
    	{
    		log.info("Not exsit. Start create Hold!");
    	}

    	lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastSeqOfLotAction(lotName, reasonCode, department, eventInfo.getEventComment()));

    	/* 20181212, hhlee, ==>> */
    	log.info("[                 LASTPOSITION] : " + String.valueOf(lastPosition));

    	log.info(lotName + "," + reasonCode + "," + department + "," + String.valueOf(lastPosition + 1) + "," + holdType + "," + eventInfo.getEventComment());

    	if(StringUtil.isEmpty(eventInfo.getEventComment()))
    	{
    		eventInfo.setEventComment("Hold");
    		/* 20181212, hhlee, ==>> */
    		log.info("[    EVENTCOMMENT IS EMPTY]====================================================== ");			    
    	}			
    	if(eventInfo.getEventTime() == null)
    	{
    		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
    		/* 20181212, hhlee, ==>> */
    		log.info("[        EVENTTIME IS NULL]====================================================== ");
    	}

    	// TODO: handle exception
    	LotMultiHold lotMultiHoldData = new LotMultiHold();

    	lotMultiHoldData.setLotName(lotName);
    	lotMultiHoldData.setReasonCode(reasonCode);
    	lotMultiHoldData.setDepartment(department);
    	lotMultiHoldData.setSeq(lastPosition+1);
    	lotMultiHoldData.setHoldType(holdType);
    	lotMultiHoldData.setEventComment(eventInfo.getEventComment());
    	lotMultiHoldData.setEventTime(eventInfo.getEventTime());
    	lotMultiHoldData.setEventName(eventInfo.getEventName());
    	lotMultiHoldData.setEventUser(eventInfo.getEventUser());
    	lotMultiHoldData.setEventTimeKey(eventInfo.getEventTimeKey());
    	lotMultiHoldData.setResetFlag(resetFlag);

    	/* 20181212, hhlee, ==>> */
    	log.info("[   BEFORE MULTIHOLD CREATE] ===================================================== " );

    	ExtendedObjectProxy.getLotMultiHoldService().create(eventInfo, lotMultiHoldData);

    	/* 20181212, hhlee, ==>> */
    	log.info("[    AFTER MULTIHOLD CREATE] ===================================================== " );
    	
    	return lotData;
    }
    
    
    public String getHoldDepartment (String lotName) throws CustomException 
    {
    	String department="";
    	
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sqlResult =null;

		try
		{
    		String sql =  "  SELECT CT_LOTMULTIHOLD.DEPARTMENT " +
    				"    FROM CT_LOTMULTIHOLD " +
    				"   WHERE 1 = 1 AND CT_LOTMULTIHOLD.LOTNAME = :LOTNAME AND DEPARTMENT != :DEPARTMENT " +
    				"GROUP BY DEPARTMENT "  ;
         
             Map<String, String> bindMap = new HashMap<String, String>();
             bindMap.put("LOTNAME", lotName);
             bindMap.put("DEPARTMENT", " ");

             sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
             
 			for(Map<String, Object> obj : sqlResult)
 			{
 				department += obj.get("DEPARTMENT").toString()+" ";
 			}
 			
// 			for(int i=0;i<sqlResult.size();i++){
// 				if(i==sqlResult.size()-1){
// 					department += sqlResult.get(i).get("DEPARTMENT").toString()+" ,";
// 				}
// 				else if(i<sqlResult.size()-1){
// 					department += sqlResult.get(i).get("DEPARTMENT").toString();
// 				}
// 			}
		}
		catch(Throwable e)
		{
			log.info("Not exsit. Start create Hold!");
			return "";
		}
		
    	return department;
    }
    
    /**
     * @author smkang
     * @since 2018.08.13
     * @param lotName
     * @param reasonCode
     * @param department
     * @param eventInfo
     * @return MultiHoldLotCount
     * @throws CustomException
     * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
     */
    public int removeMultiHoldLot(String lotName, String reasonCode, String department, String seq, EventInfo eventInfo) throws CustomException {
    	try {
    		
    		long position = Long.parseLong(seq);
    		
            if(StringUtil.isEmpty(department))
            {
                department = " ";
            }
            
        	LotMultiHold lotMultiHoldData = ExtendedObjectProxy.getLotMultiHoldService().selectByKey(false, new Object[] {lotName, reasonCode, department, eventInfo.getEventComment(),position});
            ExtendedObjectProxy.getLotMultiHoldService().remove(eventInfo, lotMultiHoldData);
            
		} catch (Exception e) {
			log.info("Not exist LotMultiHold : LotName :"+lotName+" ReasonCode :"+reasonCode+" Department :"+department+ " EventComment :"+eventInfo.getEventComment());
            throw new CustomException("HOLD-0001",lotName,reasonCode,department,eventInfo.getEventComment());   
		}
    	
        try {
            List<LotMultiHold> lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select("LOTNAME = ?", new Object[] {lotName});
            return (lotMultiHoldList != null && lotMultiHoldList.size() > 0) ? lotMultiHoldList.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * @Name     setLotProcessedOperationBeforeAOI
     * @since    2018. 9. 11.
     * @author   Admin
     * @contents 
     *           
     * @param eventInfo
     * @param productEle
     * @param machineName
     * @throws CustomException
     */
    public void setLotProcessedOperationBeforeAOI(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
    {
        try
        {
            String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
            
            Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
            String operation = aProduct.getProcessOperationName();
            
            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(aProduct.getFactoryName(), operation);
            String operationType = processOperationSpecData.getProcessOperationType();
            
            String factoryName = aProduct.getFactoryName();
            String productSpecName = aProduct.getProductSpecName();
            String processFlowName = aProduct.getProcessFlowName();
            String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", true);
            String lotName = aProduct.getLotName();
            String eventTimeKey = eventInfo.getEventTimeKey();
                        
            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
            {
                String checkSql = "SELECT LOTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5 "
                        + " FROM CT_LOTPROCESSEDOPERATION "
                        + " WHERE LOTNAME = :LOTNAME ";
                
                Map<String, Object> checkMap = new HashMap<String, Object>();
                checkMap.put("LOTNAME", lotName);

                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
                if ( checkResult.size() > 0 )
                {
                    String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
                    String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
                    String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
                    String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
                    String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
                    
                    String updateSql = " UPDATE CT_LOTPROCESSEDOPERATION SET ";
                    String condition = "";
                    
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("LOTNAME", lotName);
                    
                    if ( StringUtil.isEmpty(attribute2) )
                    {
                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
                        updateMap.put("ATTRIBUTE2", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute3) )
                    {
                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
                        updateMap.put("ATTRIBUTE3", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute4) )
                    {
                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
                        updateMap.put("ATTRIBUTE4", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute5) )
                    {
                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey);
                        updateSql = updateSql + condition; 
                    }
                    else
                    {
                        // All ATTRIBUTE Column is not empty
                        condition = " ATTRIBUTE1 = :ATTRIBUTE1, ATTRIBUTE2 = :ATTRIBUTE2, ATTRIBUTE3 = :ATTRIBUTE3, ATTRIBUTE4 = :ATTRIBUTE4, ATTRIBUTE5 = :ATTRIBUTE5 ";
                        updateMap.put("ATTRIBUTE1", attribute2);
                        updateMap.put("ATTRIBUTE2", attribute3);
                        updateMap.put("ATTRIBUTE3", attribute4);
                        updateMap.put("ATTRIBUTE4", attribute5);
                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey);
                        updateSql = updateSql + condition; 
                    }
                    
                    String where = " WHERE LOTNAME = :LOTNAME ";
                    updateSql = updateSql + where;
                    
                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
                }
                else
                {
                    String insSql = " INSERT INTO CT_LOTPROCESSEDOPERATION "
                            + " (LOTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5) "
                            + " VALUES "
                            + " (:LOTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5) ";
                    
                    Map<String, Object> insMap = new HashMap<String, Object>();
                    insMap.put("LOTNAME", lotName);
                    insMap.put("ATTRIBUTE1", machineName + "," + operation + "," + factoryName + "," + 
                                             productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                             lotName + "," + eventTimeKey);
                    insMap.put("ATTRIBUTE2", "");
                    insMap.put("ATTRIBUTE3", "");
                    insMap.put("ATTRIBUTE4", "");
                    insMap.put("ATTRIBUTE5", "");

                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Update Fail! [CT_LOTPROCESSEDOPERATION]");
        }
    }
    
    /*
	* Name : insertCT_ProductProcessOperation
	* Desc : This function is insertCT_ProductProcessOperation
	* Author : AIM Systems, Inc
	* Date : 2020.09.02
	*/
    public void insertCT_ProductProcessOperation(List<Element> productElementList, EventInfo eventInfo) throws CustomException 
    {
		for (Element productElement : productElementList) 
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			String processFlowName = productData.getProcessFlowName();
			String processOperationName = productData.getProcessOperationName();

			String sql = " INSERT INTO CT_PRODUCTPROCESSOPERATION "
					+ " (PRODUCTNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTCOMMENT ) "
					+ " VALUES "
					+ " (:PRODUCTNAME, :PROCESSFLOWNAME, :PROCESSOPERATIONNAME, :LASTEVENTTIMEKEY, :LASTEVENTTIME, :LASTEVENTNAME, :LASTEVENTUSER, :LASTEVENTCOMMENT ) ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
	}
    
    public void checkSampleOutHoldFlag(Lot lotData, EventInfo eventInfo) throws CustomException
    {
    	log.info("Check Sample Out Hold Flag Start");
    	
        ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
        
        ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
        
        //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
        /* 20190312, hhlee, add Check Validation */
        //if(StringUtil.equals("Inspection", operationData.getProcessOperationType())  &&  !StringUtil.equals("MQC", flowData.getProcessFlowType())  )
        /* 20190523, hhlee, modify, add check validation SamplingFlow */
        //if (operationData.getProcessOperationType().equals("Inspection") && 
        //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
        //            && !flowData.getProcessFlowType().equals("MQC"))
        if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
        {
            String sampleOutholdFlag = "";
            
            String tempNodeStack = lotData.getNodeStack();
            String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
            int count = arrNodeStack.length;
            
            Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-1]);

            String flowName = flowMap.get("PROCESSFLOWNAME");
            String flowVersion = flowMap.get("PROCESSFLOWVERSION");
            String operationName = flowMap.get("PROCESSOPERATIONNAME");

            if (StringUtil.indexOf(tempNodeStack, ".") > -1)
            {
                boolean endFlag = MESLotServiceProxy.getLotServiceUtil().checkEndOperation(flowName, flowVersion, arrNodeStack[count-1]);

                if ( endFlag == true )
                {
                    if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), flowName, flowVersion).toUpperCase(), "SAMPLING"))
                    {
                    	//2019.03.05_hsryu_move to checkHoldOutFlag fuction. 
                    	//2019.02.27_hsryu_For SampleOutHold EventUser. 
//                 		EventInfo eventInfoForSampleOutHoldFlag = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), 
//                 				"Hold by SampleOutHoldFlag", "HoldLot", "HD-0001");
//                		
//                 		eventInfoForSampleOutHoldFlag.setEventTime(eventInfo.getEventTime());
//                 		eventInfoForSampleOutHoldFlag.setEventTimeKey(eventInfo.getEventTimeKey());

                        sampleOutholdFlag = MESLotServiceProxy.getLotServiceUtil().checkHoldOutFlag(lotData,arrNodeStack[count-1],arrNodeStack[count-2],eventInfo);
                        
                          //2019.03.05_hsryu_move to checkHoldOutFlag fuction. 
//                        if(StringUtil.equals(sampleOutholdFlag, "Y"))
//                        {
//                        	//2019.02.27_hsryu_Remove Logic. change located Logic. For EventUser.
//                            //EventInfo sampleOutHoldEventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), "Hold by SampleOutHoldFlag", "HoldLot", "HD-0001");
//                            MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, " ", eventInfoForSampleOutHoldFlag);
//                        }
                    }
                }
            }
        }
        
        log.info("Check Sample Out Hold Flag End");
    }
//    /**
//     * @Name     checkInhibitCondition
//     * @since    2018. 9. 19
//     * @author   jjyoo
//     * @contents Check inhibit Condition before Track in machine
//     */
//    public void checkInhibitCondition(EventInfo eventInfo, Lot lotData, String machineName, String machineRecipeName, String unitName) throws CustomException
//    {
//        String machineGroupName = "";
//        ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//        machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
//            
//        /*============= Get all inhibit data list if there is even one inhibit table column value matched with lot info =============*/
//
//            List<Inhibit> inhibitList = null; 
//            try{
//            	if(unitName != "")
//            	{
//            		inhibitList = ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
//												                            "OR (PRODUCTSPECNAME = ?) " +
//												                            "OR (ECCODE = ?) " +
//												                            "OR (PROCESSFLOWNAME = ?) " +
//												                            "OR (PROCESSOPERATIONNAME = ?) " +
//												                            "OR (MACHINEGROUPNAME = ?) " +
//												                            "OR (MACHINENAME = ?) " +
//												                            "OR (UNITNAME = ?) " +
//												                            "OR (RECIPENAME = ?) " 
//												                            , new Object[]{lotData.getProductSpecName(),
//												                                           lotData.getUdfs().get("ECCODE"),
//												                                           lotData.getProcessFlowName(),
//												                                           lotData.getProcessOperationName(),
//												                                           machineGroupName,
//												                                           machineName,
//												                                           unitName,
//												                                           machineRecipeName});
//            	}
//            	else
//            	{
//            		inhibitList = ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
//														                            "OR (PRODUCTSPECNAME = ?) " +
//														                            "OR (ECCODE = ?) " +
//														                            "OR (PROCESSFLOWNAME = ?) " +
//														                            "OR (PROCESSOPERATIONNAME = ?) " +
//														                            "OR (MACHINEGROUPNAME = ?) " +
//														                            "OR (MACHINENAME = ?) " +
//														                            "OR (RECIPENAME = ?) " 
//														                            , new Object[]{lotData.getProductSpecName(),
//														                                           lotData.getUdfs().get("ECCODE"),
//														                                           lotData.getProcessFlowName(),
//														                                           lotData.getProcessOperationName(),
//														                                           machineGroupName,
//														                                           machineName,
//														                                           machineRecipeName});	
//            	}
//            
//            }catch(Exception ex){}
//            
//            if(inhibitList != null)
//            {
//            /*============= Get all applied inhibit data list ============*/
//            List<Inhibit> appliedInhibitList = new ArrayList<Inhibit>();
//            
//            for(Inhibit inhibitData : inhibitList)
//            {
//                String ihProductSpecName = inhibitData.getProductSpecName();
//                String ihECCode = inhibitData.getEcCode();
//                String ihProcessFlowName = inhibitData.getProcessFlowName();
//                String ihProcessOperationName = inhibitData.getProcessOperationName();
//                String ihMachineGroupName = inhibitData.getMachineGroupName();
//                String ihMachineName = inhibitData.getMachineName();
//                String ihUnitName = inhibitData.getUnitName();
//                String ihRecipeName = inhibitData.getRecipeName();
//                
//                boolean inhibitExistCheckFlag = true;
//                
//                if(!StringUtil.isEmpty(ihProductSpecName))
//                {
//                    if(StringUtil.equals(lotData.getProductSpecName(), ihProductSpecName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihECCode))
//                {
//                    if(StringUtil.equals(lotData.getUdfs().get("ECCODE"), ihECCode))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihProcessFlowName))
//                {
//                    if(StringUtil.equals(lotData.getProcessFlowName(), ihProcessFlowName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihProcessOperationName))
//                {
//                    if(StringUtil.equals(lotData.getProcessOperationName(), ihProcessOperationName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihMachineGroupName))
//                {
//                    if(StringUtil.equals(machineGroupName, ihMachineGroupName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihMachineName))
//                {
//                    if(StringUtil.equals(machineName, ihMachineName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(unitName != "")
//                {
//	                if(!StringUtil.isEmpty(ihUnitName))
//	                {
//	                    if(StringUtil.equals(unitName, ihUnitName))
//	                    {
//	                        inhibitExistCheckFlag = true;
//	                    }
//	                    else
//	                    {
//	                        inhibitExistCheckFlag = false;
//	                        continue;
//	                    }
//	                }
//                }
//                if(!StringUtil.isEmpty(ihRecipeName))
//                {
//                    if(StringUtil.equals(machineRecipeName, ihRecipeName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(inhibitExistCheckFlag == true)
//                {
//                    appliedInhibitList.add(inhibitData);
//                }
//            }
//            
//            if(appliedInhibitList.size() > 0)
//            {
//                /*============= Get selected inhibit data =============*/
//                Inhibit selectedInhibitData1 = null;
//                Inhibit selectedInhibitData2 = null;
//                Inhibit selectedInhibitData3 = null;
//                Inhibit selectedInhibitData4 = null;
//                Inhibit selectedInhibitData5 = null;
//                
//                for(Inhibit inhibitData : appliedInhibitList)
//                {
//                    String ihProductSpecName = inhibitData.getProductSpecName();
//                    String ihProcessFlowName = inhibitData.getProcessFlowName();
//                    String ihProcessOperationName = inhibitData.getProcessOperationName();
//                    String ihMachineName = inhibitData.getMachineName();
//            
//                    /*============= Inhibit combination priority check =============*/
//                    if(!StringUtil.isEmpty(ihProductSpecName)&&
//                       !StringUtil.isEmpty(ihProcessFlowName)&&
//                       !StringUtil.isEmpty(ihProcessOperationName)&&
//                       !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData1 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProductSpecName)&&
//                            !StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData2 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProcessFlowName)&&
//                            !StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData3 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData4 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else
//                    {
//                        selectedInhibitData5 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                }
//                
//                Inhibit selectedInhibitData = null;
//                
//                if(selectedInhibitData1 != null)
//                {
//                    selectedInhibitData = selectedInhibitData1;
//                }
//                else if(selectedInhibitData2 != null)
//                {
//                    selectedInhibitData = selectedInhibitData2;
//                }
//                else if(selectedInhibitData3 != null)
//                {
//                    selectedInhibitData = selectedInhibitData3;
//                }
//                else if(selectedInhibitData4 != null)
//                {
//                    selectedInhibitData = selectedInhibitData4;
//                }
//                else
//                {
//                    selectedInhibitData = selectedInhibitData5;
//                }
//    
//                long exceptionLotCount = selectedInhibitData.getExceptionLotCount();
//                long processLotCount = selectedInhibitData.getProcessLotCount()+ 1;
//                
//                if(exceptionLotCount == 0)
//				{
//					InhibitException inhibitExceptionData = null;
//					
//					try
//					{
//						inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
//					}
//					catch(greenFrameDBErrorSignal ex)
//					{
//						//inhibit rule applied by inhibit exception
//						throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());
//					}
//				}
//				else if(exceptionLotCount > 0)
//				{					
//					if(processLotCount > exceptionLotCount)
//					{
//						//inhibit rule applied by exception lot count
//						throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());	
//					}	
//				}
//				
//				selectedInhibitData.setProcessLotCount(processLotCount);
//				eventInfo.setEventName("IncreaseProcessLotCount");
//				ExtendedObjectProxy.getInhibitService().modify(eventInfo, selectedInhibitData);
//        	}
//        }
//    }
    
    /**
     * @Name     checkInhibitCondition
     * @since    2018. 9. 19
     * @author   jjyoo
     * @contents Check inhibit Condition before Track in machine
     */
    public void checkInhibitCondition(EventInfo eventInfo, Lot lotData, 
            String machineName, String machineRecipeName, String processLotCountFlag, String unitName) throws CustomException
    {
    	if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
        	boolean inhibitExceptionFlag = true;
            String machineGroupName = "";
            ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
            machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
                
            /*============= Get all inhibit data list if there is even one inhibit table column value matched with lot info =============*/
            List<Inhibit> inhibitList = null;
            try
            {
            	if(unitName != "")
            	{
                    /* 20190108, hhlee, Modify, Inhibit Query Modify 
                     * [NormalMode : UnitName is null, INDPMode : UnitName is not null ] ==>> */
//            		inhibitList 
//            		= ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
//    										                            "OR (PRODUCTSPECNAME = ?) " +
//    										                            "OR (ECCODE = ?) " +
//    										                            "OR (PROCESSFLOWNAME = ?) " +
//    										                            "OR (PROCESSOPERATIONNAME = ?) " +
//    										                            "OR (MACHINEGROUPNAME = ?) " +
//    										                            "OR (MACHINENAME = ?) " +
//    										                            "OR (UNITNAME = ?) " +
//    										                            "OR (RECIPENAME = ?) " 
//    										                            , new Object[]{lotData.getProductSpecName(),
//    										                                           lotData.getUdfs().get("ECCODE"),
//    										                                           lotData.getProcessFlowName(),
//    										                                           lotData.getProcessOperationName(),
//    										                                           machineGroupName,
//    										                                           machineName,
//    										                                           unitName,
//    										                                           machineRecipeName});
            	    inhibitList 
                    = ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
                                                                        " AND (PRODUCTSPECNAME IS NULL OR PRODUCTSPECNAME = ?) " +
                                                                        " AND (ECCODE IS NULL OR ECCODE = ?) " +
                                                                        " AND (PROCESSFLOWNAME IS NULL OR PROCESSFLOWNAME = ?) " +
                                                                        " AND (PROCESSOPERATIONNAME IS NULL OR PROCESSOPERATIONNAME = ?) " +
                                                                        " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) " +
                                                                        " AND (MACHINENAME IS NULL OR MACHINENAME = ?) " +
                                                                        " AND UNITNAME = ? " +
                                                                        " AND (RECIPENAME IS NULL OR RECIPENAME = ?) "                                                                    
                                                                        , new Object[]{lotData.getProductSpecName(),
                                                                                       lotData.getUdfs().get("ECCODE"),
                                                                                       lotData.getProcessFlowName(),
                                                                                       lotData.getProcessOperationName(),
                                                                                       machineGroupName,
                                                                                       machineName,
                                                                                       unitName,
                                                                                       machineRecipeName});
            	}
            	else
            	{
//                  inhibitList 
//                = ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
//                                                                    "OR (PRODUCTSPECNAME = ?) " +
//                                                                    "OR (ECCODE = ?) " +
//                                                                    "OR (PROCESSFLOWNAME = ?) " +
//                                                                    "OR (PROCESSOPERATIONNAME = ?) " +
//                                                                    "OR (MACHINEGROUPNAME = ?) " +
//                                                                    "OR (MACHINENAME = ?) " +
//                                                                    "OR (RECIPENAME = ?) " 
//                                                                    , new Object[]{lotData.getProductSpecName(),
//                                                                                   lotData.getUdfs().get("ECCODE"),
//                                                                                   lotData.getProcessFlowName(),
//                                                                                   lotData.getProcessOperationName(),
//                                                                                   machineGroupName,
//                                                                                   machineName,
//                                                                                   machineRecipeName});
            	    inhibitList 
                    = ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
                                                                        " AND (PRODUCTSPECNAME IS NULL OR PRODUCTSPECNAME = ?) " +
                                                                        " AND (ECCODE IS NULL OR ECCODE = ?) " +
                                                                        " AND (PROCESSFLOWNAME IS NULL OR PROCESSFLOWNAME = ?) " +
                                                                        " AND (PROCESSOPERATIONNAME IS NULL OR PROCESSOPERATIONNAME = ?) " +
                                                                        " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) " +
                                                                        " AND (MACHINENAME IS NULL OR MACHINENAME = ?) " +
                                                                        " AND UNITNAME IS NULL " +
                                                                        " AND (RECIPENAME IS NULL OR RECIPENAME = ?) "                                                                    
                                                                        , new Object[]{lotData.getProductSpecName(),
                                                                                       lotData.getUdfs().get("ECCODE"),
                                                                                       lotData.getProcessFlowName(),
                                                                                       lotData.getProcessOperationName(),
                                                                                       machineGroupName,
                                                                                       machineName,
                                                                                       machineRecipeName});
            	    /* <<== 20190108, hhlee, Modify, Inhibit Query Modify */
            	}
            }
            catch(Exception ex)
            { 
                
            }
            // Start 2019.09.17 Add By Park Jeong Su Mantis 4826 Add Code && inhibitList.size() > 0
            if(inhibitList != null && inhibitList.size() > 0)
            //End 2019.09.17 Add By Park Jeong Su Mantis 4826
            {
                /*============= Get all applied inhibit data list ============*/
                List<Inhibit> appliedInhibitList = new ArrayList<Inhibit>();
                
                for(Inhibit inhibitData : inhibitList)
                {
                    String ihProductSpecName = inhibitData.getProductSpecName();
                    String ihECCode = inhibitData.getEcCode();
                    String ihProcessFlowName = inhibitData.getProcessFlowName();
                    String ihProcessOperationName = inhibitData.getProcessOperationName();
                    String ihMachineGroupName = inhibitData.getMachineGroupName();
                    String ihMachineName = inhibitData.getMachineName();
                    String ihUnitName = inhibitData.getUnitName();
                    String ihRecipeName = inhibitData.getRecipeName();
                    
                    boolean inhibitExistCheckFlag = true;
                    
                    if(!StringUtil.isEmpty(ihProductSpecName))
                    {
                        if(StringUtil.equals(lotData.getProductSpecName(), ihProductSpecName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(!StringUtil.isEmpty(ihECCode))
                    {
                        if(StringUtil.equals(lotData.getUdfs().get("ECCODE"), ihECCode))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(!StringUtil.isEmpty(ihProcessFlowName))
                    {
                        if(StringUtil.equals(lotData.getProcessFlowName(), ihProcessFlowName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(!StringUtil.isEmpty(ihProcessOperationName))
                    {
                        if(StringUtil.equals(lotData.getProcessOperationName(), ihProcessOperationName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(!StringUtil.isEmpty(ihMachineGroupName))
                    {
                        if(StringUtil.equals(machineGroupName, ihMachineGroupName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(!StringUtil.isEmpty(ihMachineName))
                    {
                        if(StringUtil.equals(machineName, ihMachineName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(unitName != "")
                    {
                    	if(!StringUtil.isEmpty(ihUnitName))
                        {
                            if(StringUtil.equals(unitName, ihUnitName))
                            {
                                inhibitExistCheckFlag = true;
                            }
                            else
                            {
                                inhibitExistCheckFlag = false;
                                continue;
                            }
                        }	
                    }
                    if(!StringUtil.isEmpty(ihRecipeName))
                    {
                        if(StringUtil.equals(machineRecipeName, ihRecipeName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }
                    if(inhibitExistCheckFlag == true)
                    {
                        appliedInhibitList.add(inhibitData);
                    }
                }
                
                if(appliedInhibitList.size() > 0)
                {
                    /*============= Get selected inhibit data =============*/
                    Inhibit selectedInhibitData1 = null;
                    Inhibit selectedInhibitData2 = null;
                    Inhibit selectedInhibitData3 = null;
                    Inhibit selectedInhibitData4 = null;
                    Inhibit selectedInhibitData5 = null;
                    for(Inhibit inhibitData : appliedInhibitList)
                    {
                        String ihProductSpecName = inhibitData.getProductSpecName();
                        String ihProcessFlowName = inhibitData.getProcessFlowName();
                        String ihProcessOperationName = inhibitData.getProcessOperationName();
                        String ihMachineName = inhibitData.getMachineName();
                
                        /*============= Inhibit combination priority check =============*/
                        if(!StringUtil.isEmpty(ihProductSpecName)&&
                           !StringUtil.isEmpty(ihProcessFlowName)&&
                           !StringUtil.isEmpty(ihProcessOperationName)&&
                           !StringUtil.isEmpty(ihMachineName))
                        {
                            selectedInhibitData1 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                        }
                        else if(!StringUtil.isEmpty(ihProductSpecName)&&
                                !StringUtil.isEmpty(ihProcessOperationName)&&
                                !StringUtil.isEmpty(ihMachineName))
                        {
                            selectedInhibitData2 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                        }
                        else if(!StringUtil.isEmpty(ihProcessFlowName)&&
                                !StringUtil.isEmpty(ihProcessOperationName)&&
                                !StringUtil.isEmpty(ihMachineName))
                        {
                            selectedInhibitData3 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                        }
                        else if(!StringUtil.isEmpty(ihProcessOperationName)&&
                                !StringUtil.isEmpty(ihMachineName))
                        {
                            selectedInhibitData4 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                        }
                        else
                        {
                            selectedInhibitData5 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                        }
                    }
                    
                    Inhibit selectedInhibitData = null;
                    
                    if(selectedInhibitData1 != null)
                    {
                        selectedInhibitData = selectedInhibitData1;
                    }
                    else if(selectedInhibitData2 != null)
                    {
                        selectedInhibitData = selectedInhibitData2;
                    }
                    else if(selectedInhibitData3 != null)
                    {
                        selectedInhibitData = selectedInhibitData3;
                    }
                    else if(selectedInhibitData4 != null)
                    {
                        selectedInhibitData = selectedInhibitData4;
                    }
                    else
                    {
                        selectedInhibitData = selectedInhibitData5;
                    }
        
                    long exceptionLotCount = selectedInhibitData.getExceptionLotCount();
                    long processLotCount = selectedInhibitData.getProcessLotCount();
                    
                    if(StringUtil.equals(processLotCountFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                    {
                        processLotCount = selectedInhibitData.getProcessLotCount()+ 1;
                        selectedInhibitData.setProcessLotCount(processLotCount);
                        eventInfo.setEventName("IncreaseProcessLotCount");
                        ExtendedObjectProxy.getInhibitService().modify(eventInfo, selectedInhibitData);
                    }
                    else
                    {
                        if(exceptionLotCount > 0)
                        {                   
                            if(processLotCount >= exceptionLotCount)
                            {
                                //inhibit rule applied by exception lot count
                                throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
                            }   
                        }
                        
            
                        InhibitException inhibitExceptionData = null;
                        
                        /* 20181001, hhlee, modify, if ExceptionLotCount == "0"  All Lot Inhit, else Check ProcessCheck ==>> */
                        //try
                        //{
                        //    inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
                        //
                        //    if(inhibitExceptionData != null)
                        //    {
                        //        //inhibit rule applied by inhibit exception
                        //        throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
                        //    }
                        //}catch(greenFrameDBErrorSignal ex){}                 
                        try
                        {
                            inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
                            if(inhibitExceptionData != null)
                            {
                                //inhibit rule applied by inhibit exception
                                //throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
                            }
                        }
                        catch(greenFrameDBErrorSignal ex)
                        {                    
                        } 
                        
                        if(exceptionLotCount == 0 && 
                                inhibitExceptionData == null)
                        {
                            //inhibit rule applied by inhibit exception
                            throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
                        }
                    }
                    /* <<== 20181001, hhlee, modify, if ExceptionLotCount == "0"  All Lot Inhit, else Check ProcessCheck */                
                }
                /* 20190227, hhlee, add, Inhibit Validation */
                else
                {
                    /* 20190422, hhlee, modify, check ExceptionLot in TrackOut ==>> */
                    ///* 20190416, hhlee, modify, Not Used Inhibit TrackOut Check Logic  */
                    //////add by wghuang 20190301, need to test more with EDO chuiyu
                    ////checkExceptionLot(lotData, machineName, inhibitExceptionFlag,machineGroupName);
                    //if(!StringUtil.equals(processLotCountFlag, GenericServiceProxy.getConstantMap().Flag_Y))
                    //{
                    //	  //add by wghuang 20190301, need to test more with EDO chuiyu
                    //    checkExceptionLot(lotData, machineName, inhibitExceptionFlag,machineGroupName);
                    //}                
                    checkExceptionLot(lotData, machineName, inhibitExceptionFlag,machineGroupName);
                    /* 20190422, hhlee, modify, check ExceptionLot in TrackOut ==>> */
                    
                	//deleted by wghuang 20190301, why is this logic here? need to discuss
                    /* List<InhibitException> inhibitExceptionData = null;
                    try
                    {
                        inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().select(" WHERE LOTNAME = ?  ", new Object[]{lotData.getKey().getLotName()});                
                    }
                    catch(greenFrameDBErrorSignal ex)
                    {                    
                    }
                    if(inhibitExceptionData != null && inhibitExceptionData.size() > 0)
                    {
                        throw new CustomException("INHIBIT-0007", lotData.getKey().getLotName());   
                    }*/
                }
            }
            /* 20190227, hhlee, add, Inhibit Validation */
            /* else
            {     
            	//add by wghuang 20190301
                checkExceptionLot(lotData, machineName, inhibitExceptionFlag,machineGroupName);
            }*/
    	}
    }

    //add by wghuang 20190301, requested by EDO chuiyu
	private void checkExceptionLot(Lot lotData, String machineName,boolean inhibitExceptionFlag, String machineGroupName)throws CustomException {
		
		List<InhibitException> inhibitExceptionData = null;
		
		try
		{
		    inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().select(" WHERE LOTNAME = ?  ", new Object[]{lotData.getKey().getLotName()});                
		}
		catch(greenFrameDBErrorSignal ex)
		{                    
		}
		          
		Inhibit checkExceptionInhibit = null;
		
		if(inhibitExceptionData != null && inhibitExceptionData.size() > 0)
		{
			for(InhibitException iexceptionData : inhibitExceptionData)
			{
				try
				{
					checkExceptionInhibit = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{iexceptionData.getInhibitID()});
				}
				catch(Exception ce)
				{   			
				}
				
				if(checkExceptionInhibit != null)
				{
					if(StringUtil.equals(lotData.getProcessOperationName(), checkExceptionInhibit.getProcessOperationName()))
					{
						if(StringUtil.equals(CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), checkExceptionInhibit.getEcCode()) &&
						   StringUtil.equals(lotData.getProductSpecName(), checkExceptionInhibit.getProductSpecName()) &&
						   StringUtil.equals(lotData.getProcessFlowName(), checkExceptionInhibit.getProcessFlowName()) && 				    
						   StringUtil.equals(machineGroupName, checkExceptionInhibit.getMachineGroupName()) &&
						   StringUtil.equals(machineName, checkExceptionInhibit.getMachineName()))
						{
							inhibitExceptionFlag = false;
							break;
						}
					}
					else
					{
						inhibitExceptionFlag = false;
					}
				}
			}
			
			if(inhibitExceptionFlag == true)
				throw new CustomException("INHIBIT-0007", lotData.getKey().getLotName(),checkExceptionInhibit.getInhibitID());               
		}
	}
    
    /**
     * @Name     decreaseInhibitProcessLotCount
     * @since    2018. 9. 19
     * @author   jjyoo
     * @contents When Track in Cancel, if there is inhibit data applied before, procss lot count should be decreased
     */
    public void decreaseInhibitProcessLotCount(EventInfo eventInfo, Lot lotData, String machineName, String unitName) throws CustomException
    {
        String machineGroupName = "";
        ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
        machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
            
        /*============= Get all inhibit data list if there is even one inhibit table column value matched with lot info =============*/
        	
        List<Inhibit> inhibitList = null;
        try{
        	if(unitName != "")
        	{
        		inhibitList 
        		= ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
										                            "OR (PRODUCTSPECNAME = ?) " +
										                            "OR (ECCODE = ?) " +
										                            "OR (PROCESSFLOWNAME = ?) " +
										                            "OR (PROCESSOPERATIONNAME = ?) " +
										                            "OR (MACHINEGROUPNAME = ?) " +
										                            "OR (MACHINENAME = ?) " +
										                            "OR (UNITNAME = ?) " +
										                            "OR (RECIPENAME = ?) " 
										                            , new Object[]{lotData.getProductSpecName(),
										                                           lotData.getUdfs().get("ECCODE"),
										                                           lotData.getProcessFlowName(),
										                                           lotData.getProcessOperationName(),
										                                           machineGroupName,
										                                           machineName,
										                                           unitName,
										                                           lotData.getMachineRecipeName()});
        	}
        	else
        	{
        		inhibitList 
        		= ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
											                        "OR (PRODUCTSPECNAME = ?) " +
											                        "OR (ECCODE = ?) " +
											                        "OR (PROCESSFLOWNAME = ?) " +
											                        "OR (PROCESSOPERATIONNAME = ?) " +
											                        "OR (MACHINEGROUPNAME = ?) " +
											                        "OR (MACHINENAME = ?) " +
											                        "OR (RECIPENAME = ?) " 
											                        , new Object[]{lotData.getProductSpecName(),
											                                       lotData.getUdfs().get("ECCODE"),
											                                       lotData.getProcessFlowName(),
											                                       lotData.getProcessOperationName(),
											                                       machineGroupName,
											                                       machineName,
											                                       lotData.getMachineRecipeName()});
        	}
        }catch(Exception ex){}
        if(inhibitList != null)
        {   
            /*============= Get all applied inhibit data list ============*/
            List<Inhibit> appliedInhibitList = new ArrayList<Inhibit>();
            
            for(Inhibit inhibitData : inhibitList)
            {
                String ihProductSpecName = inhibitData.getProductSpecName();
                String ihECCode = inhibitData.getEcCode();
                String ihProcessFlowName = inhibitData.getProcessFlowName();
                String ihProcessOperationName = inhibitData.getProcessOperationName();
                String ihMachineGroupName = inhibitData.getMachineGroupName();
                String ihMachineName = inhibitData.getMachineName();
                String ihUnitName = inhibitData.getUnitName();
                String ihRecipeName = inhibitData.getRecipeName();
                
                boolean inhibitExistCheckFlag = true;
                
                if(!StringUtil.isEmpty(ihProductSpecName))
                {
                    if(StringUtil.equals(lotData.getProductSpecName(), ihProductSpecName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihECCode))
                {
                    if(StringUtil.equals(lotData.getUdfs().get("ECCODE"), ihECCode))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihProcessFlowName))
                {
                    if(StringUtil.equals(lotData.getProcessFlowName(), ihProcessFlowName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihProcessOperationName))
                {
                    if(StringUtil.equals(lotData.getProcessOperationName(), ihProcessOperationName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihMachineGroupName))
                {
                    if(StringUtil.equals(machineGroupName, ihMachineGroupName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihMachineName))
                {
                    if(StringUtil.equals(machineName, ihMachineName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(unitName != "")
                {
                	if(!StringUtil.isEmpty(ihUnitName))
                    {
                        if(StringUtil.equals(unitName, ihUnitName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }	
                }
                if(!StringUtil.isEmpty(ihRecipeName))
                {
                    if(StringUtil.equals(lotData.getMachineRecipeName(), ihRecipeName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(inhibitExistCheckFlag == true)
                {
                    appliedInhibitList.add(inhibitData);
                }
            }
            
            if(appliedInhibitList.size() > 0)
            {
                /*============= Get selected inhibit data =============*/
                Inhibit selectedInhibitData1 = null;
                Inhibit selectedInhibitData2 = null;
                Inhibit selectedInhibitData3 = null;
                Inhibit selectedInhibitData4 = null;
                Inhibit selectedInhibitData5 = null;
                for(Inhibit inhibitData : appliedInhibitList)
                {
                    String ihProductSpecName = inhibitData.getProductSpecName();
                    String ihProcessFlowName = inhibitData.getProcessFlowName();
                    String ihProcessOperationName = inhibitData.getProcessOperationName();
                    String ihMachineName = inhibitData.getMachineName();
            
                    /*============= Inhibit combination priority check =============*/
                    if(!StringUtil.isEmpty(ihProductSpecName)&&
                       !StringUtil.isEmpty(ihProcessFlowName)&&
                       !StringUtil.isEmpty(ihProcessOperationName)&&
                       !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData1 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProductSpecName)&&
                            !StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData2 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProcessFlowName)&&
                            !StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData3 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData4 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else
                    {
                        selectedInhibitData5 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                }
                
                Inhibit selectedInhibitData = null;
                
                if(selectedInhibitData1 != null)
                {
                    selectedInhibitData = selectedInhibitData1;
                }
                else if(selectedInhibitData2 != null)
                {
                    selectedInhibitData = selectedInhibitData2;
                }
                else if(selectedInhibitData3 != null)
                {
                    selectedInhibitData = selectedInhibitData3;
                }
                else if(selectedInhibitData4 != null)
                {
                    selectedInhibitData = selectedInhibitData4;
                }
                else
                {
                    selectedInhibitData = selectedInhibitData5;
                }
                
                long processLotCount = selectedInhibitData.getProcessLotCount()- 1;
                
                selectedInhibitData.setProcessLotCount(processLotCount);
                eventInfo.setEventName("DecreaseProcessLotCount");
                ExtendedObjectProxy.getInhibitService().modify(eventInfo, selectedInhibitData);
            }
        }
    }
            
    /**
     * @Name     checkInhibitCondition
     * @since    2018. 9. 19
     * @author   jjyoo
     * @contents Check inhibit Condition before Track in machine
     */
    public void checkInhibitConditionForCheckLotValidation(Lot lotData, String machineName, String machineRecipeName, String unitName) throws CustomException
    {
        String machineGroupName = "";
        ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
        machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
            
        /*============= Get all inhibit data list if there is even one inhibit table column value matched with lot info =============*/
            List<Inhibit> inhibitList = null;
            
            try{
            	if(unitName != "")
            	{
            		inhibitList 
            		= ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
    										                            "OR (PRODUCTSPECNAME = ?) " +
    										                            "OR (ECCODE = ?) " +
    										                            "OR (PROCESSFLOWNAME = ?) " +
    										                            "OR (PROCESSOPERATIONNAME = ?) " +
    										                            "OR (MACHINEGROUPNAME = ?) " +
    										                            "OR (MACHINENAME = ?) " +
    										                            "OR (UNITNAME = ?) " +
    										                            "OR (RECIPENAME = ?) " 
    										                            , new Object[]{lotData.getProductSpecName(),
    										                                           lotData.getUdfs().get("ECCODE"),
    										                                           lotData.getProcessFlowName(),
    										                                           lotData.getProcessOperationName(),
    										                                           machineGroupName,
    										                                           machineName,
    										                                           unitName,
    										                                           machineRecipeName});
            	}
            	else
            	{
            		inhibitList 
            		= ExtendedObjectProxy.getInhibitService().select("WHERE 1=1 " +
											                            "OR (PRODUCTSPECNAME = ?) " +
											                            "OR (ECCODE = ?) " +
											                            "OR (PROCESSFLOWNAME = ?) " +
											                            "OR (PROCESSOPERATIONNAME = ?) " +
											                            "OR (MACHINEGROUPNAME = ?) " +
											                            "OR (MACHINENAME = ?) " +
											                            "OR (RECIPENAME = ?) " 
											                            , new Object[]{lotData.getProductSpecName(),
											                                           lotData.getUdfs().get("ECCODE"),
											                                           lotData.getProcessFlowName(),
											                                           lotData.getProcessOperationName(),
											                                           machineGroupName,
											                                           machineName,
											                                           machineRecipeName});
            	}
            }catch(Exception ex){}
            
            if(inhibitList != null)
            {
            /*============= Get all applied inhibit data list ============*/
            List<Inhibit> appliedInhibitList = new ArrayList<Inhibit>();
            
            for(Inhibit inhibitData : inhibitList)
            {
                String ihProductSpecName = inhibitData.getProductSpecName();
                String ihECCode = inhibitData.getEcCode();
                String ihProcessFlowName = inhibitData.getProcessFlowName();
                String ihProcessOperationName = inhibitData.getProcessOperationName();
                String ihMachineGroupName = inhibitData.getMachineGroupName();
                String ihMachineName = inhibitData.getMachineName();
                String ihUnitName = inhibitData.getUnitName();
                String ihRecipeName = inhibitData.getRecipeName();
                
                boolean inhibitExistCheckFlag = true;
                
                if(!StringUtil.isEmpty(ihProductSpecName))
                {
                    if(StringUtil.equals(lotData.getProductSpecName(), ihProductSpecName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihECCode))
                {
                    if(StringUtil.equals(lotData.getUdfs().get("ECCODE"), ihECCode))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihProcessFlowName))
                {
                    if(StringUtil.equals(lotData.getProcessFlowName(), ihProcessFlowName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihProcessOperationName))
                {
                    if(StringUtil.equals(lotData.getProcessOperationName(), ihProcessOperationName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihMachineGroupName))
                {
                    if(StringUtil.equals(machineGroupName, ihMachineGroupName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(!StringUtil.isEmpty(ihMachineName))
                {
                    if(StringUtil.equals(machineName, ihMachineName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(unitName != "")
                {
                	if(!StringUtil.isEmpty(ihUnitName))
                    {
                        if(StringUtil.equals(unitName, ihUnitName))
                        {
                            inhibitExistCheckFlag = true;
                        }
                        else
                        {
                            inhibitExistCheckFlag = false;
                            continue;
                        }
                    }	
                }
                if(!StringUtil.isEmpty(ihRecipeName))
                {
                    if(StringUtil.equals(machineRecipeName, ihRecipeName))
                    {
                        inhibitExistCheckFlag = true;
                    }
                    else
                    {
                        inhibitExistCheckFlag = false;
                        continue;
                    }
                }
                if(inhibitExistCheckFlag == true)
                {
                    appliedInhibitList.add(inhibitData);
                }
            }
            
            if(appliedInhibitList.size() > 0)
            {
                /*============= Get selected inhibit data =============*/
                Inhibit selectedInhibitData1 = null;
                Inhibit selectedInhibitData2 = null;
                Inhibit selectedInhibitData3 = null;
                Inhibit selectedInhibitData4 = null;
                Inhibit selectedInhibitData5 = null;
                
                for(Inhibit inhibitData : appliedInhibitList)
                {
                    String ihProductSpecName = inhibitData.getProductSpecName();
                    String ihProcessFlowName = inhibitData.getProcessFlowName();
                    String ihProcessOperationName = inhibitData.getProcessOperationName();
                    String ihMachineName = inhibitData.getMachineName();
            
                    /*============= Inhibit combination priority check =============*/
                    if(!StringUtil.isEmpty(ihProductSpecName)&&
                       !StringUtil.isEmpty(ihProcessFlowName)&&
                       !StringUtil.isEmpty(ihProcessOperationName)&&
                       !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData1 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProductSpecName)&&
                            !StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData2 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProcessFlowName)&&
                            !StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData3 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else if(!StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName))
                    {
                        selectedInhibitData4 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                    else
                    {
                        selectedInhibitData5 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
                    }
                }
                
                Inhibit selectedInhibitData = null;
                
                if(selectedInhibitData1 != null)
                {
                    selectedInhibitData = selectedInhibitData1;
                }
                else if(selectedInhibitData2 != null)
                {
                    selectedInhibitData = selectedInhibitData2;
                }
                else if(selectedInhibitData3 != null)
                {
                    selectedInhibitData = selectedInhibitData3;
                }
                else if(selectedInhibitData4 != null)
                {
                    selectedInhibitData = selectedInhibitData4;
                }
                else
                {
                    selectedInhibitData = selectedInhibitData5;
                }
    
                long exceptionLotCount = selectedInhibitData.getExceptionLotCount();
                long processLotCount = selectedInhibitData.getProcessLotCount()+ 1;
                
                if(exceptionLotCount == 0)
				{
					InhibitException inhibitExceptionData = null;
					
					try
					{
						inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
					}
					catch(greenFrameDBErrorSignal ex)
					{
						//inhibit rule applied by inhibit exception
						throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());
					}
				}
				else if(exceptionLotCount > 0)
				{					
					if(processLotCount > exceptionLotCount)
					{
						//inhibit rule applied by exception lot count
						throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());	
					}	
				}
            }
        }
    }
    
//    /**
//     * 
//     * @Name     checkInhibitCondition
//     * @since    2018. 9. 19
//     * @author   jjyoo
//     * @contents Check inhibit Condition before Track in machine
//     *           hhlee, 20190101, modify, add MachineOperationMode, UnitName Validation 
//     *           
//     * @param eventInfo
//     * @param lotData
//     * @param machineName
//     * @param machineRecipeName
//     * @param processLotCountFlag
//     * @param machineOperationMode
//     * @param unitName
//     * @throws CustomException
//     */
//    public void checkInhibitCondition(EventInfo eventInfo, Lot lotData, String machineName, String machineRecipeName, 
//            String processLotCountFlag, String machineOperationMode, String unitName) throws CustomException
//    {
//        String machineGroupName = "";
//        ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
//        machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
//            
//        /*============= Get all inhibit data list if there is even one inhibit table column value matched with lot info =============*/
//        List<Inhibit> inhibitList = null;
//        try
//        {
//            Object[] bindSet = null;
//            
//            String strSql = " WHERE 1=1 "
//                          + " AND (PRODUCTSPECNAME IS NULL OR PRODUCTSPECNAME = ?) "
//                          + " AND (ECCODE IS NULL OR ECCODE = ?) "
//                          + " AND (PROCESSFLOWNAME IS NULL OR PROCESSFLOWNAME = ?) "
//                          + " AND (PROCESSOPERATIONNAME IS NULL OR PROCESSOPERATIONNAME = ?) "
//                          + " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) ";                          
//            
//            if(StringUtil.equals(machineOperationMode, 
//                    GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
//            {
//                strSql = strSql + " AND (MACHINENAME IS NULL OR MACHINENAME = ?) "
//                                + " AND (UNITNAME IS NULL OR UNITNAME = ?) "
//                                + " AND (RECIPENAME IS NULL OR RECIPENAME = ?) ";
//                
//                bindSet = new Object[]{ lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), 
//                                        lotData.getProcessFlowName(), lotData.getProcessOperationName(), 
//                                        machineGroupName, machineName, unitName, machineRecipeName};
//            }
//            else
//            {
//                strSql = strSql + " AND (MACHINENAME IS NULL OR MACHINENAME = ?) "
//                                + " AND (RECIPENAME IS NULL OR RECIPENAME = ?) ";
//                
//                bindSet = new Object[]{ lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), 
//                        lotData.getProcessFlowName(), lotData.getProcessOperationName(), 
//                        machineGroupName, machineName, machineRecipeName};
//            }
//           
//            inhibitList = ExtendedObjectProxy.getInhibitService().select(strSql, bindSet);
//        }
//        catch(Exception ex)
//        { 
//            
//        }
//        if(inhibitList != null)
//        {
//            /*============= Get all applied inhibit data list ============*/
//            List<Inhibit> appliedInhibitList = new ArrayList<Inhibit>();
//            
//            for(Inhibit inhibitData : inhibitList)
//            {
//                String ihProductSpecName = inhibitData.getProductSpecName();
//                String ihECCode = inhibitData.getEcCode();
//                String ihProcessFlowName = inhibitData.getProcessFlowName();
//                String ihProcessOperationName = inhibitData.getProcessOperationName();
//                String ihMachineGroupName = inhibitData.getMachineGroupName();
//                String ihMachineName = inhibitData.getMachineName();
//                String ihUnitName = inhibitData.getUnitName();
//                String ihSubUnitName = inhibitData.getSubUnitName();
//                String ihRecipeName = inhibitData.getRecipeName();
//                
//                boolean inhibitExistCheckFlag = true;
//                
//                if(!StringUtil.isEmpty(ihProductSpecName))
//                {
//                    if(StringUtil.equals(lotData.getProductSpecName(), ihProductSpecName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihECCode))
//                {
//                    if(StringUtil.equals(lotData.getUdfs().get("ECCODE"), ihECCode))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihProcessFlowName))
//                {
//                    if(StringUtil.equals(lotData.getProcessFlowName(), ihProcessFlowName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihProcessOperationName))
//                {
//                    if(StringUtil.equals(lotData.getProcessOperationName(), ihProcessOperationName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(!StringUtil.isEmpty(ihMachineGroupName))
//                {
//                    if(StringUtil.equals(machineGroupName, ihMachineGroupName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                
//                if(!StringUtil.isEmpty(ihMachineName))
//                {
//                    if(StringUtil.equals(machineName, ihMachineName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                
//                if(!StringUtil.isEmpty(ihUnitName))
//                {
//                    if(StringUtil.equals(unitName, ihUnitName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                
//                //if(!StringUtil.isEmpty(ihSubUnitName))
//                //{
//                //    if(StringUtil.equals(machineName, ihSubUnitName))
//                //    {
//                //        inhibitExistCheckFlag = true;
//                //    }
//                //    else
//                //    {
//                //        inhibitExistCheckFlag = false;
//                //        continue;
//                //    }
//                //}
//                
//                if(!StringUtil.isEmpty(ihRecipeName))
//                {
//                    if(StringUtil.equals(machineRecipeName, ihRecipeName))
//                    {
//                        inhibitExistCheckFlag = true;
//                    }
//                    else
//                    {
//                        inhibitExistCheckFlag = false;
//                        continue;
//                    }
//                }
//                if(inhibitExistCheckFlag == true)
//                {
//                    appliedInhibitList.add(inhibitData);
//                }
//            }
//            
//            if(appliedInhibitList.size() > 0)
//            {
//                /*============= Get selected inhibit data =============*/
//                Inhibit selectedInhibitData1 = null;
//                Inhibit selectedInhibitData2 = null;
//                Inhibit selectedInhibitData3 = null;
//                Inhibit selectedInhibitData4 = null;
//                Inhibit selectedInhibitData5 = null;
//                for(Inhibit inhibitData : appliedInhibitList)
//                {
//                    String ihProductSpecName = inhibitData.getProductSpecName();
//                    String ihProcessFlowName = inhibitData.getProcessFlowName();
//                    String ihProcessOperationName = inhibitData.getProcessOperationName();
//                    String ihMachineName = inhibitData.getMachineName();
//            
//                    /*============= Inhibit combination priority check =============*/
//                    if(!StringUtil.isEmpty(ihProductSpecName)&&
//                       !StringUtil.isEmpty(ihProcessFlowName)&&
//                       !StringUtil.isEmpty(ihProcessOperationName)&&
//                       !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData1 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProductSpecName)&&
//                            !StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData2 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProcessFlowName)&&
//                            !StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData3 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else if(!StringUtil.isEmpty(ihProcessOperationName)&&
//                            !StringUtil.isEmpty(ihMachineName))
//                    {
//                        selectedInhibitData4 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                    else
//                    {
//                        selectedInhibitData5 = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitData.getInhibitID()});
//                    }
//                }
//                
//                Inhibit selectedInhibitData = null;
//                
//                if(selectedInhibitData1 != null)
//                {
//                    selectedInhibitData = selectedInhibitData1;
//                }
//                else if(selectedInhibitData2 != null)
//                {
//                    selectedInhibitData = selectedInhibitData2;
//                }
//                else if(selectedInhibitData3 != null)
//                {
//                    selectedInhibitData = selectedInhibitData3;
//                }
//                else if(selectedInhibitData4 != null)
//                {
//                    selectedInhibitData = selectedInhibitData4;
//                }
//                else
//                {
//                    selectedInhibitData = selectedInhibitData5;
//                }
//    
//                long exceptionLotCount = selectedInhibitData.getExceptionLotCount();
//                long processLotCount = selectedInhibitData.getProcessLotCount();
//                
//                if(StringUtil.equals(processLotCountFlag, GenericServiceProxy.getConstantMap().Flag_Y))
//                {
//                    processLotCount = selectedInhibitData.getProcessLotCount()+ 1;
//                    selectedInhibitData.setProcessLotCount(processLotCount);
//                    eventInfo.setEventName("IncreaseProcessLotCount");
//                    ExtendedObjectProxy.getInhibitService().modify(eventInfo, selectedInhibitData);
//                }
//                else
//                {
//                    if(exceptionLotCount > 0)
//                    {                   
//                        if(processLotCount >= exceptionLotCount)
//                        {
//                            //inhibit rule applied by exception lot count
//                            throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
//                        }   
//                    }
//                    
//        
//                    InhibitException inhibitExceptionData = null;
//                    
//                    /* 20181001, hhlee, modify, if ExceptionLotCount == "0"  All Lot Inhit, else Check ProcessCheck ==>> */
//                    //try
//                    //{
//                    //    inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
//                    //
//                    //    if(inhibitExceptionData != null)
//                    //    {
//                    //        //inhibit rule applied by inhibit exception
//                    //        throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
//                    //    }
//                    //}catch(greenFrameDBErrorSignal ex){}                 
//                    try
//                    {
//                        inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object []{lotData.getKey().getLotName(),selectedInhibitData.getInhibitID()});
//                        if(inhibitExceptionData != null)
//                        {
//                            //inhibit rule applied by inhibit exception
//                            //throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
//                        }
//                    }
//                    catch(greenFrameDBErrorSignal ex)
//                    {                    
//                    } 
//                    
//                    if(exceptionLotCount == 0 && 
//                            inhibitExceptionData == null)
//                    {
//                        //inhibit rule applied by inhibit exception
//                        throw new CustomException("INHIBIT-0005",selectedInhibitData.getInhibitID());   
//                    }
//                }
//                /* <<== 20181001, hhlee, modify, if ExceptionLotCount == "0"  All Lot Inhit, else Check ProcessCheck */
//                
//            }
//        }        
//    }
    
    
    /**
     * 
     * @Name     copyFutureActionCallSplit
     * @since    2018. 8. 14.
     * @author   Admin
     * @contents Copy FutureAction From Parent Lot To Child Lot
     *           
     * @param sourceLot
     * @param destnationLot
     * @param eventInfo
     */
    public void copyFutureActionCallSplit(Lot parentLot, Lot childLot, EventInfo eventInfo)
    {
        try
        {
            /* 20181205, hhlee, add, From all product of source cassette to target cassette */
            if(!StringUtil.equals(parentLot.getKey().getLotName(), childLot.getKey().getLotName()))
            {            
                eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
                eventInfo.setEventComment(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
                eventInfo.setCheckTimekeyValidation(false);
                /* 20181128, hhlee, EventTime Sync */
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                
                copyFutureActionForSplit(parentLot, childLot, eventInfo);
            }
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    /**
     * 
     * @Name     copyFutureActionForSplit
     * @since    2018. 2. 8
     * @author   YHU
     * @contents Copy FutureAction From Parent Lot To Child Lot
     *           
     * @param parent
     * @param child
     * @param eventInfo
     */
    public void copyFutureActionForSplit(Lot parent, Lot child, EventInfo eventInfo)
    {
        
        List<LotAction> sampleActionList = new ArrayList<LotAction>();
        long lastPosition = 0;
        
        String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
        Object[] bindSet = new Object[]{ parent.getKey().getLotName(), parent.getFactoryName(), "Created" };

        try
        {
            sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
            
            for(int i=0; i<sampleActionList.size();i++)
            {
                LotAction lotaction = new LotAction();
                lotaction = sampleActionList.get(i);
                if(!StringUtil.equals(lotaction.getHoldCode(), "RLRE")){ // START MODIFY BY JHYING ON20200316 MANTIS:5775 split时 register local run的future action不需要继承

                lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(child,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));
                
                lotaction.setLotName(child.getKey().getLotName());
                lotaction.setPosition(lastPosition+1);
                lotaction.setLastEventTime(eventInfo.getEventTime());
                
                // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                
                lotaction.setLastEventName(eventInfo.getEventName());
                /* 20181205, hhlee, delete, No Copy EventUser, EventComment ==>> */
                //lotaction.setLastEventUser(eventInfo.getEventUser());
                //lotaction.setLastEventComment(eventInfo.getEventComment());
                /* <<== 20181205, hhlee, delete, No Copy EventUser, EventComment */
                
                ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                } // END MODIFY BY JHYING ON20200316 MANTIS:5775 split时 register local run的future action不需要继承
            }
        }
        catch (Throwable e)
        {
            return;
        }
        
        
        /*
        //Copy  FutureAction from parent to child
        // Find future Action
        Object[] bindSet =
                new Object[] {
                parent.getKey().getLotName(),
                parent.getFactoryName()};

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
        }*/        
    }
    
    /**
     * 
     * @Name     copyFutureActionCallMerge
     * @since    2018. 8. 14.
     * @author   Admin
     * @contents Copy FutureAction From Parent Lot To Child Lot
     *           
     * @param sourceLot
     * @param destnationLot
     * @param eventInfo
     */
    public void copyFutureActionCallMerge(Lot sourceLot, Lot destnationLot, EventInfo eventInfo)
    {
        try
        {
            eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_MERGE);
            eventInfo.setCheckTimekeyValidation(false);
            /* 20181128, hhlee, EventTime Sync */
            //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            
            copyFutureActionForMerge(sourceLot, destnationLot, eventInfo);
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    /**
     * 
     * @Name     copyFutureActionForMerge
     * @since    2018. 2. 8
     * @author   YHU
     * @contents Copy FutureAction From Parent Lot To Child Lot
     *           
     * @param sLot
     * @param dLot
     * @param eventInfo
     * @throws CustomException
     */
    public void copyFutureActionForMerge(Lot sLot, Lot dLot, EventInfo eventInfo) throws CustomException
    {
        
        List<LotAction> lotActionList = new ArrayList<LotAction>();
        List<LotAction> lotActionList2 = new ArrayList<LotAction>();
        long lastPosition= 0;

        String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
        Object[] bindSet = new Object[]{ sLot.getKey().getLotName(), sLot.getFactoryName(), "Created" };

        try
        {
            lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
            
            for(int i=0; i<lotActionList.size();i++)
            {
                boolean existFlag = false;
                LotAction lotaction = new LotAction();
                lotaction = lotActionList.get(i);

                String condition2 = "WHERE lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND "
                        + "processOperationName = ? AND processOperationVersion = ? AND actionState = ? ";
                
                Object[] bindSet2 = new Object[]{ dLot.getKey().getLotName(), dLot.getFactoryName() ,lotaction.getProcessFlowName(),
                        lotaction.getProcessFlowVersion(), lotaction.getProcessOperationName(), lotaction.getProcessOperationVersion(), "Created" };
                
                try
                {
                    lotActionList2 = ExtendedObjectProxy.getLotActionService().select(condition2, bindSet2);
                    
                    for(int j=0; j<lotActionList2.size(); j++)
                    {
                        LotAction lotAction2 = new LotAction();
                        lotAction2 = lotActionList2.get(j);
                        
                        if(StringUtil.equals(lotAction2.getActionName(), lotaction.getActionName()))
                        {
                            existFlag = true;
                            break;
                        }
                    }
                    
                    if(!existFlag)
                    {
                        lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(dLot,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));

                        lotaction.setLotName(dLot.getKey().getLotName());
                        lotaction.setPosition(lastPosition+1);
                        lotaction.setLastEventTime(eventInfo.getEventTime());
                        
                        // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                        lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                        lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                        
                        lotaction.setLastEventName(eventInfo.getEventName());
                        lotaction.setLastEventUser(eventInfo.getEventUser());
                        lotaction.setLastEventComment(eventInfo.getEventComment());
                        
                        ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                    }
                }
                catch(Throwable e)
                {
                    lotaction.setLotName(dLot.getKey().getLotName());
                    lotaction.setLastEventTime(eventInfo.getEventTime());
                    
                    // Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//                    lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
                    lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
                    
                    lotaction.setLastEventName(eventInfo.getEventName());
                    lotaction.setLastEventUser(eventInfo.getEventUser());
                    lotaction.setLastEventComment(eventInfo.getEventComment());
                    
                    ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
                }
            }
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    /**
     * 
     * @Name     copyLotHoldCallSplitMerge
     * @since    2018. 10. 29.
     * @author   hhlee
     * @contents Lot Split/Merge, Copy Source Lot Hold Info
     *           
     * @param parentLot
     * @param childLot
     * @param eventInfo
     * @param EventName
     */
    public void copyLotHoldCallSplitMerge(Lot parentLot, Lot childLot, EventInfo eventInfo, String EventName)
    {
        try
        {
            /* 20181205, hhlee, add, From all product of source cassette to target cassette */
            if(!StringUtil.equals(parentLot.getKey().getLotName(), childLot.getKey().getLotName()))
            {
                eventInfo.setEventName(EventName);
                //eventInfo.setEventComment(EventName);
                eventInfo.setCheckTimekeyValidation(false);
                /* 20181128, hhlee, EventTime Sync */
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                
                copyLotHoldForSplitMerge(parentLot, childLot, eventInfo);
            }
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    /**
     * 
     * @Name     copyLotHoldForSplitMerge
     * @since    2018. 10. 29.
     * @author   hhlee
     * @contents Lot Split/Merge, Copy Source Lot Hold Info
     *           
     * @param sLot
     * @param dLot
     * @param eventInfo
     * @throws CustomException
     */
    public void copyLotHoldForSplitMerge(Lot sLot, Lot dLot, EventInfo eventInfo) throws CustomException
    {
        
        List<LotMultiHold> lotMultiHoldList = new ArrayList<LotMultiHold>();
        long lastSeq= 0;

        String condition = " WHERE lotName = ? ";
        Object[] bindSet = new Object[]{ sLot.getKey().getLotName() };
        lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
        
        try
        {
            String strSql = " SELECT NVL(MAX(LMH.SEQ), 0) + 1 AS SEQ \n"
                          + "   FROM CT_LOTMULTIHOLD LMH             \n"
                          + "  WHERE 1=1                             \n"
                          + "    AND LMH.LOTNAME = :LOTNAME            ";
            
            Map<String, String> bindMap = new HashMap<String, String>();
            bindMap.put("LOTNAME", dLot.getKey().getLotName());
            
            List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
            
            lastSeq = Long.parseLong(sqlResult.get(0).get("SEQ").toString());
            
            /* 20181110, hhlee, add, add split/merge comment ==>> */
            String eventComment = StringUtil.EMPTY;
            /* <<== 20181110, hhlee, add, add split/merge comment */
            
            for(LotMultiHold lotmultihold : lotMultiHoldList)
            {
                try 
                {
                	// 2019.04.10_hsryu_Change NoteComment. Mantis 0003462.
                	/* 20181110, hhlee, add, add split/merge comment ==>> */
                    //eventComment = lotmultihold.getEventComment() + " - [Sorter Hold SourceLotName : " + sLot.getKey().getLotName() + "]";
                    eventComment = lotmultihold.getEventComment() + ", inherit from lot[" + sLot.getKey().getLotName() + "]";
                    
                    lastSeq = Long.parseLong(MESLotServiceProxy.getLotServiceUtil().getLastSeqOfLotAction(dLot.getKey().getLotName(),
                            lotmultihold.getReasonCode(), lotmultihold.getDepartment(), eventComment));
                    /* <<== 20181110, hhlee, add, add split/merge comment */
                    
                    lotmultihold.setLotName(dLot.getKey().getLotName());
                    /* 20190328, hhlee, modify, SEQ + 1 */
                    //lotmultihold.setSeq(lastSeq);
                    lotmultihold.setSeq(lastSeq + 1);
                    lotmultihold.setEventTime(eventInfo.getEventTime());
                    // 2019.04.10_hsryu_Insert Logic. Mantis 0003462.
                    lotmultihold.setEventComment(eventComment);
                    /* 20190128, hhlee, delete, Source EventName Copy ==>> */
                    //lotmultihold.setEventName(eventInfo.getEventName());
                    /* <<== 20190128, hhlee, delete, Source EventName Copy */
                    
                    /* 20181205, hhlee, delete, No Copy EventUser, EventComment ==>> */
                    //lotmultihold.setEventUser(eventInfo.getEventUser());
                    //lotmultihold.setEventComment(eventComment);
                    /* <<== 20181205, hhlee, delete, No Copy EventUser, EventComment */
                    ExtendedObjectProxy.getLotMultiHoldService().create(eventInfo, lotmultihold);
                    
                    /* 20181110, hhlee, delete, ==>> */
                    //lastSeq += 1;
                    /* <<== 20181110, hhlee, delete, */
                    
                } catch (Exception e) 
                {
                    log.warn(e);
                }
            }
        }
        catch (Throwable e)
        {
            return;
        }
    }
    
    /**
     * 
     * @Name sorterAfterHoldBySourceLot
     * @since 2018. 10. 29.
     * @author hhlee
     * @param eventInfo
     * @param lotData
     * @param EventName
     * @throws CustomException
     */
    public boolean sorterAfterHoldBySourceLot(EventInfo eventInfo, Lot lotData, String eventName, String sourceLotName) throws CustomException
    {
        try
        {
            List<LotMultiHold> lotMultiHoldList = new ArrayList<LotMultiHold>();           
    
            String condition = " WHERE lotName = ? ORDER BY seq DESC ";
            Object[] bindSet = new Object[]{ lotData.getKey().getLotName() };
            
            lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(condition, bindSet);
            
            /* 20181226, hhlee, delete, change EvnetName */
            //eventInfo.setEventName(eventName);
            ////eventInfo.setEventComment(EventName);
            
            /* 20181213, hhlee, modify, Add Lot Multi Hold ==>> */
            for(LotMultiHold lotMultiHoldData : lotMultiHoldList)
            {
                /* 20181226, hhlee, add, change EvnetName ==>> */
                eventInfo.setEventName(lotMultiHoldData.getEventName());
                /* <<== 20181226, hhlee, add, change EvnetName */
                
                eventInfo.setEventComment(lotMultiHoldData.getEventComment());
                eventInfo.setCheckTimekeyValidation(false);
                //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                
                /* 20181212, hhlee, add, Change EventUser ==>> */
                eventInfo.setEventUser(lotMultiHoldData.getEventUser());
                /* <<== 20181212, hhlee, add, Change EventUser */
                
                Map<String,String> lotUdfs = lotData.getUdfs();   
                lotUdfs.put("NOTE", lotMultiHoldData.getEventComment());
                if(StringUtils.isEmpty(lotData.getUdfs().get("HOLDTIME"))){
                	lotUdfs.put("HOLDTIME", eventInfo.getEventTime().toString());
                }
                // 2019.04.10_hsryu_Delete Logic. InherityInfo include EventComment.
//              if(StringUtil.isNotEmpty(sourceLotName))
//              {
//                 lotUdfs.put("NOTE", lotUdfs.get("NOTE") + " - [Sorter Hold SourceLotName : " + sourceLotName + "]");    
//              }
                
                lotData.setUdfs(lotUdfs);
                LotServiceProxy.getLotService().update(lotData);
                
                // 2019.04.10_hsryu_Add Condition. if TargetLot HoldState is 'Y', SetEvent for memory History.
                if(StringUtils.isEmpty(lotData.getLotHoldState()) || StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Flag_N)) {
                    MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, 
                    		GenericServiceProxy.getConstantMap().REASONCODETYPE_HOLDLOT, lotMultiHoldData.getReasonCode());
                }
                else {
                	SetEventInfo setEventInfo = new SetEventInfo();
                	setEventInfo.setUdfs(lotData.getUdfs());
                	LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
                }
                
                // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//                lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//                
//        		// For Clear Note, Add By Park Jeong Su
//                lotData.getUdfs().put("NOTE", "");
//                LotServiceProxy.getLotService().update(lotData);
                Map<String, String> updateUdfs = new HashMap<String, String>();
    			updateUdfs.put("NOTE", "");
    			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
            }
            /* <<== 20181213, hhlee, modify, Add Lot Multi Hold */
            
            if(lotMultiHoldList.size() > 0)
            	return true;
            else
            	return false;
        }
        catch (Exception e)
        {
            //log.warn(e);
        	return false;
        }
    }
    
    /**
     * 
     * @Name     updateSorterJobProduct
     * @since    2018. 12. 5.
     * @author   hhlee
     * @contents update Sorter Job Product
     *           
     * @param eventInfo
     * @param sortJobName
     * @param productName
     * @param sortProductState
     * @throws CustomException
     */
    public void updateSorterJobProduct(EventInfo eventInfo, String sortJobName, String productName, String sortProductState) throws CustomException
    {
        try
        {
            eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            SortJobProduct sortJobProduct = ExtendedObjectProxy.getSortJobProductService().selectByKey(false, new Object[] {sortJobName, productName});
            sortJobProduct.setSortProductState(sortProductState);
            ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, sortJobProduct);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
        }       
    }
    
	public void MoveMQCLot(Lot trackOutLot,EventInfo eventInfo) throws CustomException
	{
        List<Product> pProductList = null;
        try
        {
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
            pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(trackOutLot.getKey().getLotName());
        }
        catch (FrameworkErrorSignal fe)
        {
            log.error("[chkAfterCompleteLotMQC] - FrameworkErrorSignal");
        }
        catch (NotFoundSignal ne)
        {
            log.error("[chkAfterCompleteLotMQC] - NotFoundSignal");
        }
        catch (Exception ex)
        {
            log.error("[chkAfterCompleteLotMQC] - " + ex.getMessage());
        }
        
        if(pProductList != null)
        {
			eventInfo.setCheckTimekeyValidation(false);
			// 2019.06.04_hsryu_Delete SetEventTime Logic. Same before Event.  
			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
            String[] arrNodeStack = StringUtil.split(trackOutLot.getNodeStack(), ".");
            Map<String, String> processFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[0]);
            String processFlowName = processFlowMap.get("PROCESSFLOWNAME");
            String lastNodeID = CommonUtil.getLastNode(trackOutLot.getFactoryName(), processFlowName);

            String nGradeproductList = StringUtil.EMPTY;
            
            for(Product product : pProductList)
            {
                if (StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_N, product.getProductGrade()))
                    nGradeproductList+=product.getKey().getProductName() + " ";
            }
                    
            if(StringUtils.isNotEmpty(nGradeproductList.trim()))
            {
                throw new CustomException("PRODUCT-9005", nGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_N);
            }
            
            for(Product product : pProductList)
            {
            	String OldProductSpecName = product.getProductSpecName();
            	String OldProcessOperationName = product.getProcessOperationName();
            	// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
            	//2019.02.18_hsryu_insert Logic. Mantis 0002606.
                //Map<String, String> udfs = product.getUdfs();
				//udfs.put("COMPLETEDATE", eventInfo.getEventTime().toString());
				//product.setUdfs(udfs);

                eventInfo.setEventName("MoveToEndBank");
                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                // 2019.06.03_Add 'ProcessFlowName','NodeStack' update. if CurrentFlow is 'Sampling' or 'Sort' or 'Rework' or 'Branch', set MainFlow. 
                product.setProcessFlowName(processFlowName);
                product.setProcessOperationName("-");
                product.setProcessOperationVersion("");
                product.setNodeStack(lastNodeID);
                product.setProductSpecName("M-COMMON");
                product.setProductState("Completed");
                product.setProductProcessState("");
                product.setProductHoldState("");
                
                // 2019.06.10_hsryu_Add Logic. if ProductGrade is 'P' or 'R', Change 'G'
                if(StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_P)
                		|| StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_R)) {
                	product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
                }
                
                // 2019.06.10_hsryu_Add Logic. if ReworkState is 'InRework', Change NotInRework.
                if(StringUtils.equals(product.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework)) {
                	product.setReworkState(GenericServiceProxy.getConstantMap().Lot_NotInRework);
                	product.setReworkNodeId("");
                }

                ProductServiceProxy.getProductService().update(product);       
                // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
                setEventInfo1.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
                ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
                
                ProductHistoryKey productHistoryKey = new ProductHistoryKey();
                productHistoryKey.setProductName(product.getKey().getProductName());
                productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());

                // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
                ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
                
                productHistory.setOldProductSpecName(OldProductSpecName);
                productHistory.setOldProcessOperationName(OldProcessOperationName);
                ProductServiceProxy.getProductHistoryService().update(productHistory);	
                
				// Mentis 2659
				ProductFlag productFlag = new ProductFlag();
				productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {product.getKey().getProductName()});
				if(productFlag!=null)
				{
					productFlag.setTrackFlag("");
					productFlag.setLastEventUser(eventInfo.getEventUser());
					productFlag.setLastEventComment(eventInfo.getEventComment());
					productFlag.setLastEventTime(eventInfo.getEventTime());
					
					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//					productFlag.setLastEventTimekey(eventInfo.getLastEventTimekey());
					productFlag.setLastEventTimekey(eventInfo.getEventTimeKey());
					
					ExtendedObjectProxy.getProductFlagService().modify(eventInfo, productFlag);
				}	
            }
            
            String newEndBank = this.insertEndBankMQCA();
            if(StringUtil.isNotEmpty(newEndBank))
            {
            	String OldLotSpecName = trackOutLot.getProductSpecName();
            	String OldLotOperationNamem = trackOutLot.getProcessOperationName();
            	
                eventInfo.setEventName("MoveToEndBank");
                SetEventInfo setEventInfo1 = new SetEventInfo();
                
              // 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//                Map<String, String> udfs = trackOutLot.getUdfs();
//                udfs.put("ENDBANK", newEndBank);                
//                trackOutLot.setUdfs(udfs);
                
                trackOutLot.setLotState("Completed");
                trackOutLot.setLotProcessState("");
                trackOutLot.setProductSpecName("M-COMMON");
                // 2019.06.03_Add 'ProcessFlowName','NodeStack' update. if CurrentFlow is 'Sampling' or 'Sort' or 'Rework' or 'Branch', set MainFlow. 
                trackOutLot.setProcessFlowName(processFlowName);
                trackOutLot.setProcessOperationName("-");
                trackOutLot.setProcessOperationVersion("");
                trackOutLot.setNodeStack(lastNodeID);
                trackOutLot.setLotHoldState("");
                
                // 2019.06.10_hsryu_Add Logic. if LotGrade is 'R', Change 'G'
                if(StringUtils.equals(trackOutLot.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_R)) {
                	trackOutLot.setLotGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
                }
                
                // 2019.06.10_hsryu_Add Logic. if ReworkState is 'InRework', Change NotInRework.
                if(StringUtils.equals(trackOutLot.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework)) {
                	trackOutLot.setReworkState(GenericServiceProxy.getConstantMap().Lot_NotInRework);
                	trackOutLot.setReworkNodeId("");
                }
                
                LotServiceProxy.getLotService().update(trackOutLot);
                
                setEventInfo1.getUdfs().put("ENDBANK", newEndBank);
                LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo1);     
                
                LotHistoryKey LotHistoryKey = new LotHistoryKey();
                LotHistoryKey.setLotName(trackOutLot.getKey().getLotName());
                LotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
                
                // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
                LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);

                LotHistory.setOldProductSpecName(OldLotSpecName);
                LotHistory.setOldProcessOperationName(OldLotOperationNamem);
                LotServiceProxy.getLotHistoryService().update(LotHistory);
            }
        }
	}
	
    /**
     * 
     * @Name     chkAfterCompleteLotMQC
     * @since    2019. 1. 26.
     * @author   hhlee
     * @contents 
     *           
     * @param trackOutLot
     * @param eventInfo
     * @throws CustomException
     */
    public void chkAfterCompleteLotMQC(Lot trackOutLot,EventInfo eventInfo) throws CustomException
    {
        if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
        {
            List<Product> pProductList = null;
            try
            {
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
                pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(trackOutLot.getKey().getLotName());
            }
            catch (FrameworkErrorSignal fe)
            {
                log.error("[chkAfterCompleteLotMQC] - FrameworkErrorSignal");
            }
            catch (NotFoundSignal ne)
            {
                log.error("[chkAfterCompleteLotMQC] - NotFoundSignal");
            }
            catch (Exception ex)
            {
                log.error("[chkAfterCompleteLotMQC] - " + ex.getMessage());
            }
            
            if(pProductList != null)
            {
                eventInfo.setEventName("MoveToEndBank");
				eventInfo.setCheckTimekeyValidation(false);
				// 2019.06.14_hsryu_same EventTime.
				//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
                String nGradeproductList = StringUtil.EMPTY;
                
                for(Product product : pProductList)
                {
                    if (StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_N, product.getProductGrade()) )
                        nGradeproductList+=product.getKey().getProductName() + " ";
                }
                
                if(StringUtils.isNotEmpty(nGradeproductList.trim()))
                {
                    throw new CustomException("PRODUCT-9005", nGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_N);
                }
                
                for(Product product : pProductList)
                {
                    kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                    product.setProductSpecName("M-COMMON");
                    ProductServiceProxy.getProductService().update(product);
                    
                    setEventInfo1.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
                    ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
                    
    				// Mentis 2659
    				ProductFlag productFlag = new ProductFlag();
    				productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {product.getKey().getProductName()});
    				if(productFlag!=null)
    				{
    					productFlag.setTrackFlag("");
    					productFlag.setLastEventUser(eventInfo.getEventUser());
    					productFlag.setLastEventComment(eventInfo.getEventComment());
    					productFlag.setLastEventTime(eventInfo.getEventTime());
    					
    					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//    					productFlag.setLastEventTimekey(eventInfo.getLastEventTimekey());
    					productFlag.setLastEventTimekey(eventInfo.getEventTimeKey());
    					
    					ExtendedObjectProxy.getProductFlagService().modify(eventInfo, productFlag);
    				}	
                }
                
                String newEndBank = this.insertEndBankMQCA();
                if(StringUtil.isNotEmpty(newEndBank))
                {
                    SetEventInfo setEventInfo1 = new SetEventInfo();
                    
                    trackOutLot.setProductSpecName("M-COMMON");
                    LotServiceProxy.getLotService().update(trackOutLot);
                    
                    setEventInfo1.getUdfs().put("ENDBANK", newEndBank);
                    LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo1);                    
                }
            }
        }
    }
    
    /**
     * 
     * @Name     chkAfterCompleteLot
     * @since    2019. 1. 29.
     * @author   hhlee
     * @contents 
     *           
     * @param trackOutLot
     * @param eventInfo
     * @throws CustomException
     */
    public void chkAfterCompleteLot(Lot trackOutLot, EventInfo eventInfo) throws CustomException
    {
        if(StringUtil.equals(trackOutLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
        {
        	// 2019.04.02_jsPark_Check ThresHoldRatio. Mantis 0003310.
        	MESLotServiceProxy.getLotServiceUtil().checkThresHoldRatio(trackOutLot);
    			
            List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(trackOutLot.getKey().getLotName());
            
            String sGradeproductList = "";
            String nGradeproductList = "";
            for(Product product : pProductList)
            {
                if( StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_S, product.getProductGrade()))
                {
                    sGradeproductList+=product.getKey().getProductName() + " ";
                }
                
                if( StringUtil.equals(GenericServiceProxy.getConstantMap().ProductGrade_N, product.getProductGrade()) )
                {
                    nGradeproductList+=product.getKey().getProductName() + " ";
                }
            }
            
            if(StringUtils.isNotEmpty(sGradeproductList.trim()))
                 throw new CustomException("PRODUCT-9005", sGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_S);

            if(StringUtils.isNotEmpty(nGradeproductList.trim()))
                 throw new CustomException("PRODUCT-9005", nGradeproductList, GenericServiceProxy.getConstantMap().ProductGrade_N);

            // 2019.06.14_hsryu_Move to Logic. Requested by Report. same Lot and Product Timekey.
            eventInfo.setEventName("MoveToEndBank");
            eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

            for(Product product : pProductList)
            {
                List<PanelJudge> panelJudgeList = new ArrayList<PanelJudge>();
                
                String conditionForPanelJudge = "WHERE glassName = ? ";
                Object[] bindSet = new Object[]{ product.getKey().getProductName() };
                
                try
                {
                    panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select(conditionForPanelJudge, bindSet);
                }
                catch(Throwable e)
                {
                    log.warn("Not PanelList - GlassName [" + product.getKey().getProductName() + "]");
                }
                
                if(panelJudgeList.size()>0)
                {
                    for(PanelJudge panelJudge : panelJudgeList)
                    {
                        if(StringUtils.equals(panelJudge.getPanelJudge(), "R"))
                        {
                            throw new CustomException("PANEL-0002", panelJudge.getPanelName(), product.getKey().getProductName());
                        }
                    }
                }
                
                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
                
				setEventInfo1.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
                ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
            }
            
            //2019.02.25_hsryu_Modify Logic. productRequestName -> WorkOrderType Pass. Mantis 0002757.
            if(StringUtil.isNotEmpty(insertEndBank(trackOutLot,CommonUtil.getWorkOrderType(trackOutLot),trackOutLot.getProductionType())))
            {
                SetEventInfo setEventInfo1 = new SetEventInfo();
                
                setEventInfo1.getUdfs().put("ENDBANK", insertEndBank(trackOutLot,CommonUtil.getWorkOrderType(trackOutLot), trackOutLot.getProductionType()));
                // 2019.06.14_hsryu_Delete CompelteDate. only need Product CompleteDate. 
                //setEventInfo1.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());

                LotServiceProxy.getLotService().setEvent(trackOutLot.getKey(), eventInfo, setEventInfo1);
                
            }
            
    		//2019.02.22_hsryu_Delete Logic. 
			//MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(trackOutLot.getProductRequestName(), null, "F", (long)trackOutLot.getProductQuantity(), eventInfo);
            
            /**** 2019.02.22_hsryu_adjust WO Quantity. for Mixed WO Glass in a Lot.****/
    		Map<String ,Integer> woHashMap = new HashMap<String, Integer>();
    		
            for(Product productData : pProductList){
            	if(StringUtils.isNotEmpty(productData.getProductRequestName())){
        			if(!woHashMap.containsKey(productData.getProductRequestName()))
        				woHashMap.put(productData.getProductRequestName(), 1);
        			else
        				woHashMap.put(productData.getProductRequestName(), (woHashMap.get(productData.getProductRequestName()))+1);
            	}
    		}
    		
    		for(String key : woHashMap.keySet()){	
    			if (!trackOutLot.getProductionType().equals("MQCA"))
    	            MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(key, null, "F", woHashMap.get(key), eventInfo);
    		}
    		/***************************************************************************/
        }
    }
    
    /**
     * 
     * @Name     insertEndBankMQCA
     * @since    2019. 1. 29.
     * @author   hhlee
     * @contents 
     *           
     * @return
     */
    private String insertEndBankMQCA()
    {
        
        String sql = "SELECT EDV.ENUMVALUE FROM ENUMDEF ED JOIN ENUMDEFVALUE EDV ON ED.ENUMNAME = EDV.ENUMNAME WHERE ED.ENUMNAME = :ENUMNAME AND TAG = :TAG";
        Map<String, String> bindMap = new HashMap<String, String>();
        bindMap.put("ENUMNAME", "EndBank");
        bindMap.put("TAG", "MQCA");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
        String NewEndBank = "";
        if(sqlResult != null && sqlResult.size() > 0)
        {
            NewEndBank = sqlResult.get(0).get("ENUMVALUE").toString();
        }
        
        return NewEndBank;
    }
    
    /**
     * 
     * @Name     insertEndBank
     * @since    2019. 1. 29.
     * @author   hhlee
     * @contents 
     *           
     * @param trackOutLot
     * @param productRequestName
     * @param ProductionType
     * @return
     */
    private String insertEndBank(Lot trackOutLot, String productRequestType, String ProductionType)
    {
        //ProductRequest Key & Data
    	//2019.02.25_hsryu_Delete Logic. Mantis 0002757.
//        ProductRequestKey pKey = new ProductRequestKey();
//        pKey.setProductRequestName(productRequestName);
//        ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
    	
    	if(StringUtils.isNotEmpty(productRequestType)){
    		String sql = "";
            Map<String, Object> bindMap = new HashMap<String, Object>();
            if(StringUtil.equals(ProductionType, "DMQC"))
            {
                sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
                        + "WHERE ENUMNAME = :ENUMNAME AND TAG = :TAG";  
                bindMap.put("ENUMNAME", productRequestType);
                bindMap.put("TAG", ProductionType);
            }
            else {
                sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
                        + "WHERE ENUMNAME = :ENUMNAME ";    
                bindMap.put("ENUMNAME", productRequestType);
            }
            
            List<Map<String, Object>> sqlResult = 
                    GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
            
            return sqlResult.get(0).get("ENUMVALUE").toString();
    	}
    	else
    		return "";
    }
    
    /**
     * 
     * @Name     lotStateUpdateByLotStateCompleted
     * @since    2019. 2. 12.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     * @return
     */
    public Lot lotStateUpdateByLotStateCompleted(EventInfo eventInfo, Lot lotData) throws CustomException
    {
        try
        {
            log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
            lotData.setProcessOperationName("-");
            lotData.setProcessOperationVersion(StringUtil.EMPTY);    
            lotData.setLotProcessState(StringUtil.EMPTY); 
            lotData.setLotHoldState(StringUtil.EMPTY); 
            LotServiceProxy.getLotService().update(lotData);
            
            // 2019.04.19 by shkim
            LotHistoryKey LotHistoryKey = new LotHistoryKey();
	        LotHistoryKey.setLotName(lotData.getKey().getLotName());
	        LotHistoryKey.setTimeKey(lotData.getLastEventTimeKey());
	        
	        // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	        LotHistory Lothistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
	        LotHistory Lothistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
	        
            Lothistory.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
            Lothistory.setProcessOperationName("-");
            Lothistory.setProcessOperationVersion(StringUtil.EMPTY);
            Lothistory.setLotProcessState(StringUtil.EMPTY); 
            Lothistory.setLotHoldState(StringUtil.EMPTY); 
            LotServiceProxy.getLotHistoryService().update(Lothistory);
            
            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] Update Fail Lot Data.");   
        }
        
        return lotData;
    }
    

    /**
     * 
     * @Name     updateSorterJobProduct
     * @since    2019. 3. 13.
     * @author   Administrator
     * @contents 
     *           
     * @param eventInfo
     * @param sortJobName
     * @param carrierName
     * @throws CustomException
     */
    public void updateSorterJobProduct(EventInfo eventInfo, String sortJobName, String carrierName) throws CustomException
    {            
        try
        {
            List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(" JOBNAME = ? AND FROMCARRIERNAME = ? ", new Object[] {sortJobName, carrierName});
            
            if(sortJobProductList != null && sortJobProductList.size() > 0)
            {
                for(SortJobProduct sortJobProduct : sortJobProductList) 
                {
                    try
                    {
                        eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                        sortJobProduct.setSortProductState(GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_EXECUTING);
                        ExtendedObjectProxy.getSortJobProductService().modify(eventInfo, sortJobProduct);
                    }
                    catch (Exception ex)
                    {
                        log.error(ex.getMessage());
                    }  
                }
            }            
        }
        catch (NotFoundSignal ne)
        {
            //sortJobProductList = new ArrayList <SortJobProduct>();
            log.error(ne.getStackTrace());
        }
        catch(Exception e)
        {
            //sortJobProductList = new ArrayList <SortJobProduct>();
            log.error(e.getStackTrace());
        }   
    }
    
    /**
     * 
     * @Name     lotStateUpdateByAllGlasssScrap
     * @since    2019. 3. 21.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     * @return
     * @throws CustomException
     */
    public Lot lotStateUpdateByAllGlasssScrap(EventInfo eventInfo, Lot lotData) throws CustomException
    {
        try
        {
            lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_S);
            lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait); 
            LotServiceProxy.getLotService().update(lotData);
            
            SetEventInfo setEventInfo = new SetEventInfo();
            LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
            
            lotData.setMachineName("");
            LotServiceProxy.getLotService().update(lotData);
            
            lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] Update Fail Lot Data.");   
        }
        
        return lotData;
    }
    
    
    /**
     * 
     * @Name     lotStateUpdateBychangeProcessOperation
     * @since    2019. 3. 28.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     * @return
     * @throws CustomException
     */
    public Lot lotStateUpdateBychangeProcessOperation(EventInfo eventInfo, Lot lotData) throws CustomException
    {
        try
        {
            lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
            lotData.setLastLoggedOutTime(eventInfo.getEventTime());                        
            lotData.setLastLoggedOutUser(eventInfo.getEventUser());
           
            Map<String,String> lotUdfs = lotData.getUdfs();   
            lotUdfs.put("NOTE", "");
            lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
            lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
            lotUdfs.put("LASTLOGGEDOUTMACHINE", eventInfo.getEventUser());
            lotData.setUdfs(lotUdfs);                        
            
            LotServiceProxy.getLotService().update(lotData);
            
            //lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] lotStateUpdateBychangeProcessOperation - Update Fail Lot Data.");   
        }
        
        return lotData;
    }
    
    /**
     * 
     * @Name     lotAndProductStateUpdateAfterTrackOut
     * @since    2019. 3. 28.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     * @return
     */
    public Lot lotAndProductStateUpdateAfterTrackOut(EventInfo eventInfo, Lot lotData)
    {
    	
    	/* 2019.11.27 Request By CIM
        String LeadTime = "";
        String WaitTime = "";
        String ProcTime = "";
        
         Caculation LeadTime, WaitTime, ProcTime 
        try
        {   
            log.info("LeadTime, WaitTime, ProcTime - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            // TK-OUT - IF(BEFORE TK-OUT NULL THEN Release TIME ELSE BEFORE TK-OUT END)  
            String strSql_LeadTime = 
                    "    WITH LOT_LIST AS (" +
                        "                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) BEFORT , LASTLOGGEDOUTTIME ,LOTNAME  " +                   
                    "                    FROM LOTHISTORY  " +
                    "                   WHERE LOTNAME = :LOTNAME " +
                    "                    ORDER BY TIMEKEY )         " +
                    "           SELECT TO_CHAR(ROUND((LASTLOGGEDOUTTIME - BEFORT) * 24 *60 *60,2))  AS LEADTIME, BEFORT, LASTLOGGEDOUTTIME " +
                    "           FROM LOT_LIST " +
                    "           WHERE NO IN ( SELECT MAX(NO) NO FROM LOT_LIST ) " ;     
                                         
            Map<String, Object> bindMap_LeadTime = new HashMap<String, Object>();
            bindMap_LeadTime.put("LOTNAME", lotData.getKey().getLotName());
    
            //List<Map<String, Object>> List_LeadTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_LeadTime, bindMap_LeadTime);
    
            //BEFORE TK-OUT - TK-IN
            String strSql_WaitTime = 
                    " WITH LOT_TKIN AS (      "+    
                    "                   SELECT LASTLOGGEDINTIME EVENTTIME ,LOTNAME  " +                 
                    "                    FROM LOT  " +
                    "                   WHERE  LOTNAME = :LOTNAME ) " +
                    "       ,LOT_TKOUT AS (  "+
                    "                   SELECT ROWNUM NO, LAG(LASTLOGGEDOUTTIME) OVER(ORDER BY TIMEKEY ) EVENTTIME ,LOTNAME  " +                    
                    "                    FROM LOTHISTORY A " +
                    "                   WHERE LOTNAME = :LOTNAME " +
                    "                    ORDER BY TIMEKEY ) " +
                    "      SELECT TO_CHAR(ROUND((A.EVENTTIME - B.EVENTTIME ) * 24 *60 *60,2)) WAITTIME  "+
                    "      FROM  LOT_TKIN A "+
                    "        INNER JOIN LOT_TKOUT B ON A.LOTNAME = B.LOTNAME "+
                    "      WHERE 1=1  "+
                    "         AND B.NO IN (SELECT MAX(NO) NO FROM LOT_TKOUT) " ;     ;
     
            Map<String, Object> bindMap_WaitTime = new HashMap<String, Object>();
            bindMap_WaitTime.put("LOTNAME", lotData.getKey().getLotName());
    
            //List<Map<String, Object>> List_WaitTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_WaitTime, bindMap_WaitTime);
                            
            //TK-OUT - TK-IN
            String strSql_ProcTime =     
                    "      SELECT  TO_CHAR(ROUND((LASTLOGGEDOUTTIME - LASTLOGGEDINTIME ) * 24 *60 *60,2)) PROCTIME  "+
                    "       FROM LOT  "+
                    "      WHERE  LOTNAME = :LOTNAME  ";

                Map<String, Object> bindMap_ProcTime = new HashMap<String, Object>();
                bindMap_ProcTime.put("LOTNAME", lotData.getKey().getLotName());
                //List<Map<String, Object>> List_ProcTime = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql_ProcTime, bindMap_ProcTime);
                

            if(List_LeadTime != null && List_LeadTime.size() > 0)
            {   
                LeadTime = List_LeadTime.get(0).get("LEADTIME").toString();
            }
            
            if(List_WaitTime != null && List_WaitTime.size() > 0)
            {
                WaitTime = List_WaitTime.get(0).get("WAITTIME").toString();
            }
            
            if(List_ProcTime != null && List_ProcTime.size() > 0)
            {
                ProcTime = List_ProcTime.get(0).get("PROCTIME").toString();
            }            
        }
        catch (Exception ex)
        {
            log.error("[SYS-9999] Caculation LeadTime, WaitTime, ProcTime Fail.");   
        }
        */
        
        
        /* Set Lot, LotHistory, Caculation LeadTime, WaitTime, ProcTime */
        try
        {
            log.info("Update LotData - EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            //LotHistoryKey lotHistoryKey = new LotHistoryKey();
            //lotHistoryKey.setLotName(lotData.getKey().getLotName());
            //lotHistoryKey.setTimeKey(lotData.getLastEventTimeKey());
    
            //Lot History Update
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            LotHistory lotHistoryInfo = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey);
            //LotHistory lotHistoryInfo = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);

            //lotHistoryInfo.getUdfs().put("LEADTIME", LeadTime);
            //lotHistoryInfo.getUdfs().put("WAITTIME", WaitTime);
            //lotHistoryInfo.getUdfs().put("PROCTIME", ProcTime);
            
            //LotServiceProxy.getLotHistoryService().update(lotHistoryInfo);
            
            lotData.setMachineName("");
            Map<String, String> udfs_port = lotData.getUdfs();
            udfs_port.put("PORTNAME", "");
            //udfs_port.put("LEADTIME", LeadTime);
            //udfs_port.put("WAITTIME", WaitTime);
            //udfs_port.put("PROCTIME", ProcTime);
            
            LotServiceProxy.getLotService().update(lotData);
        }
        catch (Exception ex)
        {
            log.error("[SYS-9999] Lot data Update Fail Product Data.");   
        }
        
        /* Set Product, ProductHistory, Caculation LeadTime, WaitTime, ProcTime */
        List<Product> productList = null;
        try
        {
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            log.error("[SYS-9999] No Product to process");   
        }
        
        try
        {
            log.info("Update Product EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            for (Product productData : productList)
            {   
                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                productData.setLastIdleTime(lotData.getLastEventTime());
                productData.setLastIdleUser(lotData.getLastEventUser());
                
                if(StringUtil.isEmpty(productData.getUdfs().get("PROCESSINGINFO")))
                {
                    productData.getUdfs().put("PROCESSINGINFO", GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_W);
                }
                
                ProductServiceProxy.getProductService().update(productData);
                
//                String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? )";
//                Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName()};
//                                
//                List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//                ProductHistory producthistory = pArrayList.get(0);
                ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	            productHistoryKey.setProductName(productData.getKey().getProductName());
	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());	            
	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
	            
                producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                
                producthistory.setLastIdleTime(productData.getLastIdleTime());
                producthistory.setLastIdleUser(productData.getLastIdleUser());
                
                producthistory.getUdfs().put("PROCESSINGINFO", productData.getUdfs().get("PROCESSINGINFO"));
                //producthistory.getUdfs().put("LEADTIME", LeadTime);
                //producthistory.getUdfs().put("WAITTIME", WaitTime);
                //producthistory.getUdfs().put("PROCTIME", ProcTime);
                
                ProductServiceProxy.getProductHistoryService().update(producthistory);
            }
        }
        catch (Exception ex)
        {
            log.error("[SYS-9999] Product data Update Fail Product Data.");   
        }
        
        return lotData;
    }
    
    /**
     * 
     * @Name     setReturnMessageAndLotHoldByTrackIn
     * @since    2019. 4. 10.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param exceptionEx
     * @param doc
     * @param carrierName
     * @param setLotHold
     * @param setCarrierHold
     * @return
     */
    /* 20190425, hhlee, modify, change variable(carrierEnd delete) 
     *           change function name (setReturnMessageAndLotHoldByTrackIn->setReturnMessageByTrackIn)
     */
    //public Document setReturnMessageAndLotHoldByTrackIn(EventInfo eventInfo, Exception exceptionEx, Document doc, 
    //        String carrierName, String lotName, boolean setLotHold, boolean setCarrierHold, boolean carrierStart, boolean setEvent)
    /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
    public Document setReturnMessageByTrackIn(EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName,
             String lotName, boolean setLotHold, boolean setCarrierHold, boolean setFutureHold, boolean setEventLog, String recordEventName)
    {
        log.error(exceptionEx);
        
        String errorCode = StringUtil.EMPTY;
        String errorMessage = StringUtil.EMPTY;
        /* 20190424, hhlee, add, Comment */
        String oldEventComment = eventInfo.getEventComment();
        String eventComment = StringUtil.EMPTY;
        
        /* 20190424, hhlee, add, eventName */
        String eventName = eventInfo.getEventName();
        
        /* 20190425, hhlee, delete, Change Logic(switch ~~~ case ~~~) ==>> */
        //if(carrierStart)
        //{
        //    errorMessage = "CarrierStart Fail.! - ";
        //}
        //else
        //{
        //    errorMessage = "TrackIn Fail.! - ";
        //}
        /* <<== 20190425, hhlee, delete, Change Logic(switch ~~~ case ~~~) */
        
        /* 20190516, hhlee, add LotData Inquery ==>> */
        Lot lotData = null;
        try
        {
            if(StringUtil.isNotEmpty(lotName))
            {
                lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
            }
            else
            {
                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
                lotData = lotList.get(0);
            }             
        }
        catch (Exception ex) 
        {
            log.warn(String.format("setReturnMessageByTrackIn - Inquery Lot data fail.[CarrierName:{0}, lotName:{1}]", carrierName, lotName));
        }
        /* <<== 20190516, hhlee, add LotData Inquery */   
        
        /* 20190425, hhlee, add, Change Logic(switch ~~~ case ~~~) ==>> */
        switch (recordEventName)
        {
            case "TrackInFail":
                errorMessage = "TrackIn Fail.! - ";
                break; 
            case "CarrierStartFail":
                errorMessage = "CarrierStart Fail.! - ";
                break; 
            default:
                errorMessage = StringUtil.EMPTY;
                break;            
        }
        /* <<== 20190425, hhlee, add, Change Logic(switch ~~~ case ~~~) */
        
        /* 20190516, hhlee, add LotData ==>> */
        if(lotData != null)
        {
            errorMessage += "[ LotName : " + lotData.getKey().getLotName()  + " ] ";
        }
        /* <<== 20190516, hhlee, add LotData */
        
        /* 20190424, hhlee, add, Comment */
        eventComment += errorMessage;        
        
        if (exceptionEx instanceof CustomException) 
        {
            errorCode = ((CustomException) exceptionEx).errorDef.getErrorCode();
            /* 20190424, hhlee, add, Comment */
            eventComment += errorCode + " : ";
            
            if(StringUtil.equals(errorCode, GenericServiceProxy.getConstantMap().ERRORCODE_UNDEFINECODE))
            {
                errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                        exceptionEx.getClass().getName() + " - " + exceptionEx.getMessage() : 
                            exceptionEx.getClass().getName() + " - " + "Unknown exception is occurred.");
                
                /* 20190424, hhlee, add, Comment */
                eventComment += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                        exceptionEx.getClass().getName() + " - " + exceptionEx.getMessage() : 
                            exceptionEx.getClass().getName() + " - " + "Unknown exception is occurred.");
            }
            else
            {
                errorMessage += ((CustomException) exceptionEx).errorDef.getLoc_errorMessage();
               
                /* 20190424, hhlee, add, Comment */
                eventComment += ((CustomException) exceptionEx).errorDef.getLoc_errorMessage();
            }
        } 
        else 
        {
            errorCode = (exceptionEx != null) ? exceptionEx.getClass().getName() : "SYS-0000";
            /* 20190424, hhlee, add, Comment */
            eventComment += errorCode + " : ";
            
            errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                                exceptionEx.getMessage() : "Unknown exception is occurred.");    
           
            /* 20190424, hhlee, add, Comment */
            eventComment += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                    exceptionEx.getMessage() : "Unknown exception is occurred."); 
        }
        
        /* 20190424, hhlee, add, Comment */
        /* 20190425, hhlee, add, Change variable(boolean setEvent -> String recordEventName) */
        //if(setEvent)
        /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
        //if(StringUtil.isNotEmpty(recordEventName))
        if(setEventLog)
        {
            try
            {
                /* 20190516, hhlee, delete ==>> */
                //Lot lotData = null;
                //if(StringUtil.isNotEmpty(lotName))
                //{
                //    lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                //}
                //else
                //{
                //    List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
                //    lotData = lotList.get(0);
                //} 
                /* <<== 20190516, hhlee, delete */
                
                if(lotData != null)
                {
                    /* 20190425, hhlee, add, Change variable(boolean setEvent -> String recordEventName) */
                    //eventInfo.setEventName("TrackInFail");
                    eventInfo.setEventName(recordEventName);
                    eventInfo.setEventComment(oldEventComment);
                    
                    SetEventInfo  setEventInfoLot = new SetEventInfo();        
                    Map<String,String> lotDataUdfs = lotData.getUdfs();
                    lotDataUdfs.put("NOTE", eventComment);
                    setEventInfoLot.setUdfs(lotDataUdfs);
                    
                    LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfoLot); 
                }
            }
            catch (Exception ex) 
            {
                log.warn(String.format("setReturnMessageByTrackIn - Inquery Lot data fail.[[CarrierName:{0}, lotName:{1}]", carrierName, lotName));
            }
        }
        
        if(setLotHold)
        {
            if(!StringUtil.equals(errorCode, "LOT-9046"))
            {
                eventInfo.setEventName("Hold");
                eventInfo.setEventComment(errorMessage);
                /* 20190424, hhlee, modify, changed function ==>> */
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "ABST", ""); // Abnormal Start
                /* 20190426, hhlee, modify, add variable(setFutureHold) */
                //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABST","", true, "", "");
                try {
                	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
                	log.info("Before doAfterHoldbyCarrier");
                	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName,null,"HoldLot","ABST","", false, setFutureHold, oldEventComment, "");
                	log.info("After doAfterHoldbyCarrier");
                	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				} catch (Exception e) {
					log.info("Error doAfterHoldbyCarrier");
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				}
                
                /* <<== 20190424, hhlee, modify, changed function */
            }
        }
        
        if(setCarrierHold)
        {            
        }
        
        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, errorCode);
        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, errorMessage);
        
        return doc;
    }
    
    /**
     * 
     * @Name     setReturnMessageByTrackOut
     * @since    2019. 4. 10.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param exceptionEx
     * @param doc
     * @param carrierName
     * @param setLotHold
     * @param setCarrierHold
     * @param carrierEnd
     * @return
     */
    /* 20190425, hhlee, modify, change variable(carrierEnd delete) */
    //public Document setReturnMessageByTrackOut(EventInfo eventInfo, Exception exceptionEx, Document doc, 
    //        String carrierName, String lotName, boolean setLotHold, boolean setCarrierHold, boolean carrierEnd, String recordEventName)
    /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
    public Document setReturnMessageByTrackOut(EventInfo eventInfo, Exception exceptionEx, Document doc, String carrierName, 
            String lotName, boolean setLotHold, boolean setCarrierHold, boolean setFutureHold, boolean setEventLog, String recordEventName) throws CustomException
    {
        log.error(exceptionEx);
        
        String errorCode = StringUtil.EMPTY;
        String errorMessage = StringUtil.EMPTY;
        
        String eventName = eventInfo.getEventName();
        
        /* 20190424, hhlee, add, Comment */
        String oldEventComment = eventInfo.getEventComment();
        String eventComment = StringUtil.EMPTY;
        
        /* 20190425, hhlee, delete, Change Logic(switch ~~~ case ~~~) ==>> */
        //if(carrierEnd)
        //{
        //    errorMessage = "CarrierEnd Fail.! - ";
        //}
        //else
        //{
        //    errorMessage = "TrackOut Fail.! - ";
        //}
        /* <<== 20190425, hhlee, delete, Change Logic(switch ~~~ case ~~~) */
        
        /* 20190516, hhlee, add LotData Inquery ==>> */
        Lot lotData = null;
        try
        {
            if(StringUtil.isNotEmpty(lotName))
            {
                lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
            }
            else
            {
                List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
                lotData = lotList.get(0);
            }             
        }
        catch (Exception ex) 
        {
            log.warn(String.format("setReturnMessageByTrackOut - Inquery Lot data fail.[CarrierName:{0}, lotName:{1}]", carrierName, lotName));
        }
        /* <<== 20190516, hhlee, add LotData Inquery */         
        
        /* 20190425, hhlee, add, Change Logic(switch ~~~ case ~~~) ==>> */
        switch (recordEventName)
        {
            case "TrackOutFail":
                errorMessage = "TrackOut Fail.! - ";
                break; 
            case "CarrierEndFail":
                errorMessage = "CarrierEnd Fail.! - ";
                break; 
            case "LotProcessCancel":
                errorMessage = "LotProcess Cancel.! - ";
                break; 
            default:
                errorMessage = StringUtil.EMPTY;
                break;            
        }
        /* <<== 20190425, hhlee, add, Change Logic(switch ~~~ case ~~~) */
        
        /* 20190516, hhlee, add LotData ==>> */
        if(lotData != null)
        {
            errorMessage += "[ LotName : " + lotData.getKey().getLotName()  + " ] ";
        }
        /* <<== 20190516, hhlee, add LotData */
        
        /* 20190424, hhlee, add, Comment */
        eventComment += errorMessage;
        
        if (exceptionEx instanceof CustomException) 
        {
            errorCode = ((CustomException) exceptionEx).errorDef.getErrorCode();
            /* 20190424, hhlee, add, Comment */
            eventComment += errorCode + " : ";
            
            if(StringUtil.equals(errorCode, GenericServiceProxy.getConstantMap().ERRORCODE_UNDEFINECODE))
            {
                errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                        exceptionEx.getClass().getName() + " - " + exceptionEx.getMessage() : 
                            exceptionEx.getClass().getName() + " - " + "Unknown exception is occurred.");
                
                /* 20190424, hhlee, add, Comment */
                eventComment += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                        exceptionEx.getClass().getName() + " - " + exceptionEx.getMessage() : 
                            exceptionEx.getClass().getName() + " - " + "Unknown exception is occurred.");
            }
            else
            {
                errorMessage += ((CustomException) exceptionEx).errorDef.getLoc_errorMessage();
                
                /* 20190424, hhlee, add, Comment */
                eventComment += ((CustomException) exceptionEx).errorDef.getLoc_errorMessage();
            }
        } 
        else
        {
            errorCode = (exceptionEx != null) ? exceptionEx.getClass().getName() : "SYS-0000";
            /* 20190424, hhlee, add, Comment */
            eventComment += errorCode + " : ";
            
            errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                                exceptionEx.getMessage() : "Unknown exception is occurred.");    
            
            /* 20190424, hhlee, add, Comment */
            eventComment += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                    exceptionEx.getMessage() : "Unknown exception is occurred."); 
        }
        
        /* 20190424, hhlee, add, Comment */
        /* 20190425, hhlee, add, Change variable(boolean setEvent -> String recordEventName) */
        //if(setEvent)
        /* 20190426, hhlee, modify, add variable(setFutureHold, setEventLog) */
        //if(StringUtil.isNotEmpty(recordEventName))
        if(setEventLog)
        {
            try
            {
                /* 20190516, hhlee, delete ==>> */
                //Lot lotData = null;
                //if(StringUtil.isNotEmpty(lotName))
                //{
                //    lotData    = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
                //}
                //else
                //{
                //    List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotDataFromCarrier(carrierName);
                //    lotData = lotList.get(0);
                //} 
                /* <<== 20190516, hhlee, delete */
                
                if(lotData != null)
                {
                    /* 20190425, hhlee, add, Change variable(boolean setEvent -> String recordEventName) */
                    //eventInfo.setEventName("TrackOutFail");
                    eventInfo.setEventName(recordEventName);
                    eventInfo.setEventComment(oldEventComment);
                    
                    SetEventInfo  setEventInfoLot = new SetEventInfo();        
                    Map<String,String> lotDataUdfs = lotData.getUdfs();
                    lotDataUdfs.put("NOTE", eventComment);
                    setEventInfoLot.setUdfs(lotDataUdfs);
                    
                    LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfoLot);
                }
            }
            catch (Exception ex) 
            {
                log.warn(String.format("setReturnMessageByTrackOut - Inquery Lot data fail.[CarrierName:{0}, lotName:{0}]", carrierName, lotName));
            }
        }
        
        if(setLotHold)
        {
            eventInfo.setEventName("Hold");
            eventInfo.setEventComment(errorMessage);
            /* 20190426, hhlee, modify, add variable(setFutureHold) */
            //MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "ABND", "", false, eventName, "");
            try {
            	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
            	log.info("Before doAfterHoldbyCarrier");
            	MESLotServiceProxy.getLotServiceUtil().doAfterHoldbyCarrier(eventInfo, carrierName, null, "HoldLot", "ABND", "", false, setFutureHold, oldEventComment, "");
            	log.info("After doAfterHoldbyCarrier");
            	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception e) {
				log.info("Error doAfterHoldbyCarrier");
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}
            
        }
        
        if(setCarrierHold)
        {
        }
        
        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, errorCode);
        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, errorMessage);
        
        return doc;
    }
    
    /**
     * 
     * @Name     getReturnErrorMessage
     * @since    2019. 4. 22.
     * @author   hhlee
     * @contents 
     *           
     * @param exceptionEx
     * @return
     */
    public String getReturnErrorMessage(Exception exceptionEx)
    {
        log.error(exceptionEx);
        
        String errorCode = StringUtil.EMPTY;
        String errorMessage = StringUtil.EMPTY;
        String errorReturnMessage = StringUtil.EMPTY;
        
        if (exceptionEx instanceof CustomException) 
        {
            errorCode = ((CustomException) exceptionEx).errorDef.getErrorCode();
            errorReturnMessage += errorCode + " : ";
            if(StringUtil.equals(errorCode, GenericServiceProxy.getConstantMap().ERRORCODE_UNDEFINECODE))
            {
                errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                        exceptionEx.getClass().getName() + " - " + exceptionEx.getMessage() : 
                            exceptionEx.getClass().getName() + " - " + "Unknown exception is occurred.");
                
                errorReturnMessage += errorMessage;
            }
            else
            {
                errorMessage += ((CustomException) exceptionEx).errorDef.getLoc_errorMessage();
                
                errorReturnMessage += errorMessage;
            }
        } 
        else
        {
            errorCode = (exceptionEx != null) ? exceptionEx.getClass().getName() : "SYS-0000";
            errorReturnMessage += errorCode + " : ";
            errorMessage += ((exceptionEx != null && StringUtils.isNotEmpty(exceptionEx.getMessage())) ? 
                                exceptionEx.getMessage() : "Unknown exception is occurred.");    
            
            errorReturnMessage += errorMessage;     
        }
        
        return errorReturnMessage;
    }
    
    public void checkMQCJobOfStartMQCJob(MQCJob MQCJob) throws CustomException{
    	List<MQCJobOper> MQCJobOperationList = null;
    	try {
	    	String condition = " WHERE MQCJOBNAME= ? ";
	    	Object[] bindSet = new Object[] {MQCJob.getmqcJobName()};
	    	MQCJobOperationList = ExtendedObjectProxy.getMQCJobOperService().select(condition, bindSet);
		} catch (Exception e) {
			log.info("MQCJobOperationList is Null");
		}
    	
    	if(MQCJobOperationList!=null && MQCJobOperationList.size()>0){
    		for(MQCJobOper MQCJobOperation : MQCJobOperationList){
				if(isAllProductScrapOfMQCOperation(MQCJob.getmqcJobName() ,MQCJobOperation.getprocessOperationName()  )){
					throw new CustomException("MQC-0060", MQCJobOperation.getprocessOperationName());
				}
    		}
    	}
    }
    
    /*
     * MQC Job 狼 OperationList 客 Lot 狼 ProcessFlow 狼 OperationList 甫 厚背茄促.
     * OperationList 狼 Count, Order, Name 捞 悼老窍瘤 臼促搁 Create Error
     * 沥荐
     * */
    public void checkMQCJobOperationListAndProcessFlowOperationList(String MQCJobName,String factoryName,String processFlowName) throws CustomException
    {
    	List<Map<String, Object>> MQCOperationList = MESLotServiceProxy.getLotInfoUtil().getMQCJobOperationList(MQCJobName, factoryName, processFlowName);
    	List<Map<String, Object>> operationList = MESLotServiceProxy.getLotInfoUtil().getProcessOperationList(factoryName, processFlowName);
    	
    	if(MQCOperationList.size()!=operationList.size()){
    		// Size different Error
			log.info("MQCOperationList OperationList size Error");
			throw new CustomException("MQC-0061", MQCJobName);
    	}

		for (int i = 0; i < operationList.size(); i++) {
			if(!StringUtils.equals(MQCOperationList.get(i).get("PROCESSOPERATIONNAME").toString(), operationList.get(i).get("PROCESSOPERATIONNAME").toString())){
				// Order And Name different Error
				log.info("MQCOperationList is Different OperationList");
				throw new CustomException("MQC-0061", MQCJobName);
			}
		}
    }
    /*
     * MQC Job 狼 OperationList 客 Lot 狼 ProcessFlow 狼 OperationList 甫 厚背秦辑
     * 鞍栏搁 false
     * 促福搁 true甫 馆券 窍绰 窃荐
     * return 狼 蔼篮 aHoldFlag肺 积阿窍搁 等促.
     * 促福扁 锭巩俊 促澜俊 hold甫 吧绢具 登绊 true 蔼阑 馆券茄促.
     * */
    public boolean getAholdFlagMQCOperationListAndProcessFlowOperationList(String lotName, String processFlowName) throws CustomException{
    	boolean aHoldFlag = false;
    	
    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    	MQCJob MQCJob =ExtendedObjectProxy.getMQCJobService().selectMQCJobWhereJobStateisExecuting(lotData.getFactoryName(), processFlowName, lotName);
    	
    	List<Map<String, Object>> MQCOperationList = MESLotServiceProxy.getLotInfoUtil().getMQCJobOperationList(MQCJob.getmqcJobName(), lotData.getFactoryName(), processFlowName);
    	List<Map<String, Object>> operationList = MESLotServiceProxy.getLotInfoUtil().getProcessOperationList(lotData.getFactoryName(), processFlowName);
    	
    	if(MQCOperationList.size()!=operationList.size())
    	{
    		aHoldFlag= true;
    		log.info("lotName :"+lotName +"MQCJobProcessFlow is different ProcessFlow OperationList"); 
    		return aHoldFlag;
    	}
    	else
    	{
    		for(int i=0; i<MQCOperationList.size();i++){
    			if(!StringUtils.equals(MQCOperationList.get(i).get("PROCESSOPERATIONNAME").toString(), operationList.get(i).get("PROCESSOPERATIONNAME").toString())){
    				aHoldFlag=true;
    	    		log.info("lotName :"+lotName +"MQCJobProcessFlow is different ProcessFlow OperationList"); 
    				return aHoldFlag;
    			}
    			
    		}
    	}
    	return false;
    }
    
    
    /*
     * TrackOut 饶俊 MQC Job 狼 OperationList 客 Lot 狼 ProcessFlow 狼 OperationList 甫 厚背茄促.
     * 父老 Operation List 狼 Count , Order Name捞 促福促搁
     * Lot 阑 圈靛甫 窍绊 note俊 扁废秦霖促.
     * */
    public void checkMQCOperationListAndProcessFlowOperationListHoldLot(String lotName,String processFlowName) throws CustomException{
    	// 泅犁 lot狼 MQC Job 阑 啊廉柯促.
    	// MQCJob 狼 lotname苞 鞍绊 JobState 啊 Executing 牢 Get MQC Job
    	
    	Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
    	MQCJob MQCJob =ExtendedObjectProxy.getMQCJobService().selectMQCJobWhereJobStateisExecuting(lotData.getFactoryName(), processFlowName, lotName);
    	
    	List<Map<String, Object>> MQCOperationList = MESLotServiceProxy.getLotInfoUtil().getMQCJobOperationList(MQCJob.getmqcJobName(), lotData.getFactoryName(), processFlowName);
    	List<Map<String, Object>> operationList = MESLotServiceProxy.getLotInfoUtil().getProcessOperationList(lotData.getFactoryName(), processFlowName);
    	
    	StringBuilder MQCOperation = new StringBuilder("MQC Job OperationList [");
    	StringBuilder operation = new StringBuilder("ProcessFlow OperationList [");
    	
    	boolean isSame = true; 
    	
    	if(MQCOperationList.size()!=operationList.size()){
    		for(Map<String, Object> MQCJobOper : MQCOperationList){
    			MQCOperation.append((String)MQCJobOper.get("PROCESSOPERATIONNAME")+" ");
    		}
    		for(Map<String, Object> Oper : operationList){
    			operation.append((String)Oper.get("PROCESSOPERATIONNAME")+" ");
    		}
    		MQCOperation.append("]");
    		operation.append("]");
    		isSame = false;
    	}
    	else{
    		
    		for(int i=0; i<MQCOperationList.size();i++){
    			MQCOperation.append((String)MQCOperationList.get(i).get("PROCESSOPERATIONNAME")+" ");
    			operation.append((String)operationList.get(i).get("PROCESSOPERATIONNAME")+" ");
    			if(!StringUtils.equals((String)MQCOperationList.get(i).get("PROCESSOPERATIONNAME"), (String)operationList.get(i).get("PROCESSOPERATIONNAME"))){
    				isSame = false;
    			}
    			
    		}
    		MQCOperation.append("]");
    		operation.append("]");
    	}
    	
    	// 鞍瘤 臼阑 版快 Hold 窍绊 困俊辑 累己茄 Note 扁废
    	// 捞固 Hold 啊 吧赴 Lot俊 措秦辑绰 MultiHoldLot俊 眠啊父 窍绊
    	// Hold 啊 酒囱 Lot篮 Hold 甫 吧绊 MultiHoldLot俊 眠啊茄促.
    	if(!isSame){
    		MQCOperation.append("\n");
    		MQCOperation.append(operation);
    		lotData = LotServiceProxy.getLotService().selectByKey(lotData.getKey());
    		lotData.getUdfs().put("NOTE", MQCOperation.toString());
    		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", "SYS", "MQC Flow is Different ProcessFlow", "HoldLot", "ABND");
            MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo, lotData, "HoldLot", "ABND");
            MESLotServiceProxy.getLotServiceUtil().executeMultiHold(eventInfo, lotData, "ABND", " "); 
    		
    	}
    }
    
    
    
    /* checkMQCJobOfScrapLotAll
     * 葛电 Product啊 Scrap 灯阑 锭
     * MQC Job Wait, Executing 狼 版快啊 促福促.
     * Wait 牢 Job 狼 版快 窍唱狼 Product 扼档 Scrap 捞 等促搁, 弊 MQC Job篮 昏力茄促.
     * Executing 狼 版快 菊栏肺 柳青且 傍沥俊 措秦辑 弊 傍沥狼 Product 啊 葛滴 Scrap 灯阑 版快父 Job阑 昏力窍绊, EndBank肺 捞悼茄促.
     * 
     * 父老 Lot狼 葛电 Product 啊 Scrap 灯促搁, EndBank肺 捞悼窍瘤 臼绰促.
     * 
     * 泅犁 MQCJob Start 狼 版快 窍唱狼 Product扼档 Grade啊 'S'扼搁 Start 甫 窍瘤 给茄促.
    */
    public void checkMQCJobOfScrapLot(String lotName,String CarrierName,EventInfo eventInfo) throws CustomException {
    	log.info("checkMQCJobOfScrapLot Start");
    	LotKey keyInfo = new LotKey(lotName);
    	
    	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    	Lot lotData = LotServiceProxy.getLotService().selectByKey(keyInfo);
    	Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(keyInfo);
    	
		if(StringUtil.equals("MQCA",lotData.getProductionType())){
	    	List<MQCJob> MQCJobList=null;
	    	List<MQCJobOper> MQCJobOperationList = null;
	    	List<Map<String, Object>> nextOperationList = null;
	    	List<Product> unScrappedProductList = null;
	    	try {
	    		MQCJobList = ExtendedObjectProxy.getMQCJobService().selectMQCJobListByCarrier(CarrierName);
			} catch (CustomException e) {
				
			}
	    	
            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
            
	    	try {
                String[] arrNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
                Map<String, String> processFlowMap = null;
                String processFlowName = "";
                String processOperationName = "";

                // if CurrentFlow is 'MQC' or 'Main' or 'Sort', get FirstNode.
                if(flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC)
                		|| flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN)
                		|| flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SORT)){

                	processFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[0]);
            		processFlowName = processFlowMap.get("PROCESSFLOWNAME");
            		processOperationName = processFlowMap.get("PROCESSOPERATIONNAME");
                }
                // if CurrentFlow is 'Sampling' or 'Rework' or 'Branch', get NextNode.
                else{
    				String nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(lotData.getFactoryName(), arrNodeStack[0]);
                    processFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(nextNodeID);
            		processFlowName = processFlowMap.get("PROCESSFLOWNAME");
            		processOperationName = processFlowMap.get("PROCESSOPERATIONNAME");
                }
                
                if(StringUtils.isNotEmpty(processFlowName) && StringUtils.isNotEmpty(processOperationName)){
    	    		nextOperationList = CommonUtil.getNextOperationList(lotData.getFactoryName(), processFlowName, processOperationName);
                }
			} catch (Exception e) {
				log.info("nextOperationList is Null");
			}
	    	try {
	    		unScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			} catch (Exception e) {
				log.info("unScrappedProductList is Null");
				log.info("lotName ["+lotName+"] is All Scrap");
			}
			
			if(MQCJobList!=null && MQCJobList.size()>0){
				
				for(MQCJob MQCJob : MQCJobList){
					log.info("MQCJob Check Start MQCJobName ["+MQCJob.getmqcJobName()+"]");
					boolean deleteFlag =false;
					if(StringUtils.equals("Wait", MQCJob.getmqcState())){
						log.info("MQCJobName ["+MQCJob.getmqcJobName()+"] JobState is Wait");
						try {
					    	String condition = " WHERE MQCJOBNAME= ? ";
					    	Object[] bindSet = new Object[] {MQCJob.getmqcJobName()};
					    	MQCJobOperationList = ExtendedObjectProxy.getMQCJobOperService().select(condition, bindSet);
					    	
					    	if(MQCJobOperationList!=null && MQCJobOperationList.size()>0){
					    		for(MQCJobOper MQCJobOperation : MQCJobOperationList){
									if(isProductScrapOfMQCOperation(MQCJob.getmqcJobName() ,MQCJobOperation.getprocessOperationName()  )){
										deleteFlag=true;
										break;
									}
					    		}
					    	}
					    	if(deleteFlag){
					    		log.info("Delete MQCJobName ["+MQCJob.getmqcJobName()+"]");
					    		deleteMQCJobAndOperationListAndPosition(MQCJob.getmqcJobName(),eventInfo);
					    	}
					    	
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					else if(StringUtils.equals("Executing", MQCJob.getmqcState()))
					{
						log.info("MQCJobName ["+MQCJob.getmqcJobName()+"] JobState is Executing");
						try {
					    	
					    	if(nextOperationList!=null && nextOperationList.size()>0){
					    		for(Map<String, Object> processOperation : nextOperationList){
					    			if(isAllProductScrapOfMQCOperation(MQCJob.getmqcJobName(),processOperation.get("PROCESSOPERATIONNAME").toString()  )){
										deleteFlag=true;
										break;
					    			}
					    		}
					    	}
					    	
						} catch (Exception e) {
							// TODO: handle exception
						}
						// 秦寸 Lot狼 葛电 Product啊 Scrap 捞扼搁
						// 葛电 Product啊 Scrap 灯促搁 MQCJob,Oper,Position 阑 昏力窍绊
						// EndBank肺 捞悼窍瘤 臼绰促.
				    	if(deleteFlag){
				    		// All Product Scrap
				    		log.info("Delete MQCJobName ["+MQCJob.getmqcJobName()+"]");
				    		if(unScrappedProductList == null || unScrappedProductList.size()==0  ){
				    			log.info("Don't go to EndBank LotName : ["+lotData.getKey().getLotName()+"]");
					    		deleteMQCJobAndOperationListAndPosition(MQCJob.getmqcJobName(),eventInfo);
				    		}
				    		else{
					    		log.info("go to EndBank LotName : ["+lotData.getKey().getLotName()+"]");
					    		deleteMQCJobAndOperationListAndPosition(MQCJob.getmqcJobName(),eventInfo);
					    		MESLotServiceProxy.getLotServiceImpl().MoveMQCLot(lotData,eventInfo);
				    		}
				    	}
						
					}
					log.info("MQCJob Check End MQCJobName ["+MQCJob.getmqcJobName()+"]");
				}//MQCJob For
			}
		}
		log.info("checkMQCJobOfScrapLot End");
    }
    /*
     * 秦寸 傍沥狼 葛电 Product 啊 Scrap 灯绰啊?
     * 嘎栏搁 true 馆券 酒聪搁 false 馆券
     * */
    public boolean isAllProductScrapOfMQCOperation(String MQCJobName, String processOperationName) throws CustomException{
    	log.info("isProductScrapOfMQCOperation Start");
    	log.info("MQCJobName : ["+MQCJobName+"] ProcessOperationName : ["+processOperationName+"]");
    	String condition = " WHERE MQCJOBNAME= ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ";
    	Object[] bindSet = new Object[] {MQCJobName,processOperationName,"00001"};
    	List<MQCJobPosition> MQCJobPositionList=null;
    	try {
        	MQCJobPositionList = ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
		} catch (Exception e) {
			
		}
    	
    	if(MQCJobPositionList!=null && MQCJobPositionList.size()>0){
        	for(MQCJobPosition position : MQCJobPositionList){
        		ProductKey keyInfo = new ProductKey(position.getproductName());
        		Product product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
        		if(!StringUtils.equals(GenericServiceProxy.getConstantMap().Prod_Scrapped, product.getProductState())){
        			log.info("isAllProductScrapOfMQCOperation End Return False");
        			return false;
        		}
        	}
    	}
    	log.info("isAllProductScrapOfMQCOperation End Return False");
    	return true;
    }
    /*
     * 窍唱扼档 秦寸傍沥狼 Product 啊 Scrap 灯绰啊?
     * 嘎栏搁 true 馆券, 酒聪搁 false 馆券
     * */
    public boolean isProductScrapOfMQCOperation(String MQCJobName, String processOperationName) throws CustomException{
    	log.info("isProductScrapOfMQCOperation Start");
    	log.info("MQCJobName : ["+MQCJobName+"] ProcessOperationName : ["+processOperationName+"]");
    	String condition = " WHERE MQCJOBNAME= ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ";
    	Object[] bindSet = new Object[] {MQCJobName,processOperationName,"00001"};
    	List<MQCJobPosition> MQCJobPositionList=null;
    	try {
        	MQCJobPositionList = ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
		} catch (Exception e) {
			
		}
    	
    	if(MQCJobPositionList!=null && MQCJobPositionList.size()>0){
        	for(MQCJobPosition position : MQCJobPositionList){
        		ProductKey keyInfo = new ProductKey(position.getproductName());
        		Product product = ProductServiceProxy.getProductService().selectByKey(keyInfo);
        		if(StringUtils.equals(GenericServiceProxy.getConstantMap().Prod_Scrapped, product.getProductState())){
        			log.info("isProductScrapOfMQCOperation End Return True");
        			return true;
        		}
        	}
    	}
    	log.info("isProductScrapOfMQCOperation End Return False");
    	return false;
    }
    
    public void deleteMQCJobAndOperationListAndPosition(String MQCJobName,EventInfo eventInfo) throws CustomException{
    	try {
    		log.info("Delete MQC Job Start");
	    	String condition = " WHERE MQCJOBNAME= ? ";
	    	Object[] bindSet = new Object[] {MQCJobName};
    		MQCJob MQCJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[]{MQCJobName});
    		List<MQCJobOper> MQCJobOperationList = ExtendedObjectProxy.getMQCJobOperService().select(condition, bindSet);
    		List<MQCJobPosition>MQCJobPositionList =  ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
    		eventInfo.setEventName("RemoveMqcJob");
    		log.info("JobName : ["+MQCJobName+"] LotName : ["+MQCJob.getlotName()+"]" );
    		for(MQCJobPosition jobPosition : MQCJobPositionList){
    			ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, jobPosition);
    		}
    		for(MQCJobOper jobOperation  : MQCJobOperationList){
    			ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, jobOperation);
    		}
    		ExtendedObjectProxy.getMQCJobService().remove(eventInfo, MQCJob);
    		log.info("Delete MQC Job End");
		} catch (Exception e) {
			// TODO: handle exception
		}
    }

    /**
     * @author smkang
     * @since 2019.05.24
     * @param lotData
     * @param updateUdfs
     * @see LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
     */
    public synchronized void updateLotWithoutHistory(Lot lotData, Map<String, String> updateUdfs) {
    	if (lotData != null && updateUdfs != null && updateUdfs.size() > 0) {
    		try {
    			String sqlStatement = "";
        		String setStatement = "";    		
        		
        		Set<String> keys = updateUdfs.keySet();
        		for (String key : keys) {
        			lotData.getUdfs().put(key, updateUdfs.get(key));
        			setStatement = setStatement.concat(key).concat(" = :").concat(key).concat(", "); 
    			}
        		
        		sqlStatement = sqlStatement.concat("UPDATE LOT SET ").concat(StringUtils.removeEnd(setStatement, ", ")).concat(" WHERE LOTNAME = :LOTNAME");
        		updateUdfs.put("LOTNAME", lotData.getKey().getLotName());
        		
        		if (GenericServiceProxy.getSqlMesTemplate().update(sqlStatement, updateUdfs) > 0)
        			log.debug(sqlStatement + " is succeeded to be executed.");
        		else
        			log.debug(sqlStatement + " is failed to be executed.");
			} catch (Exception e) {
				throw e;
			}
    	}
    }
    /**
     * @Name	checkLotEmptyChangeNodeStack
     * @since	2019. 09. 09
     * @author	jspark
     * @contents Check LotState Empty Change NodeStack, if LotState == Scrapped OR LotState == Emptied Change NodeStack 111111111111111
     */
    public void changeNodeStack111111111111111(String lotName) throws CustomException{
    	try {
    		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
    		if(StringUtils.equals(GenericServiceProxy.getConstantMap().Lot_Emptied, lotData.getLotState()) || StringUtils.equals(GenericServiceProxy.getConstantMap().Lot_Scrapped, lotData.getLotState())){
    			Map<String, String> updateUdfs = new HashMap<String, String>();
    			updateUdfs.put("NODESTACK", "111111111111111");
    			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
    			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotNameForUpdate(lotName);
    			if(productList!=null && productList.size()>0){
    				for(Product product : productList){
    					product.setNodeStack("111111111111111");
    					ProductServiceProxy.getProductService().update(product);
    				}
    			}
    		}
		} catch (Exception e) {
			log.info(lotName + "Change NodeStack Fail");
		}
    }
    /**
     * @Name	checkLocalRunException
     * @since	2020. 03. 02
     * @author	jhying
     * @param String 
     * @param localRunExp
     * @contents Check exist local run exception lot
     */
    public boolean isExistLocalRunException(String lotName, String processFlowName, String processOperationName,String machineName) throws CustomException{
    	log.info("isExistLocalRunException Start");
    	
    	String condition = " WHERE LOTNAME= ? AND PROCESSFLOWNAME = ? AND PROCESSOPERATIONNAME = ?  AND MACHINENAME = ?";
    	Object[] bindSet = new Object[] {lotName,processFlowName,processOperationName,machineName};
    	List<LocalRunException> LocalRunExceptionList=null;
    	try {
    		LocalRunExceptionList = ExtendedObjectProxy.getLocalRunExceptionService().select(condition, bindSet);
		} catch (Exception e) {
			
		}
    	
    	if(LocalRunExceptionList!=null && LocalRunExceptionList.size()>0){
    		log.info("isExistLocalRunException End Return True");
    		return true;
    	}
    	log.info("isExistLocalRunException End Return False");
    	return false;
    }
    
    /**
     * @Name	checkLocalRunException
     * @since	2020. 03. 02
     * @author	jhying
     * @param String 
     * @param localRunExpNote 
     * @contents Check exist local run exception lot
     */
    
    public void checkLocalRunExceptionCondition( Lot lotData, String machineName, String unitName, String machineRecipeName,EventInfo eventInfo) throws CustomException{
    	log.info("check LocalRunExceptionLot Start");
    	log.info("LotName: ["+lotData.getKey()+"]  ProcessFlowName : ["+lotData.getProcessFlowName()+"] ProcessOperationName : ["+lotData.getProcessOperationName()+"] MachineName : ["+machineName+"] MachineRecipeName:["+machineRecipeName+"]");

    	if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed)){
        	boolean localRunExceptionFlag = true;
            String machineGroupName = "";
            ProcessOperationSpec operation_Data = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
        	MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
            machineGroupName = CommonUtil.getValue(operation_Data.getUdfs(), "MACHINEGROUPNAME");
    
            List<LocalRunException> localRunExceptionList = null;
            try
            {
            	localRunExceptionList 
                = ExtendedObjectProxy.getLocalRunExceptionService().select("WHERE 1=1 " +
                                                                    " AND  LOTNAME = ? " +
                                                                    " AND PROCESSFLOWNAME = ? " +
                                                                    " AND  PROCESSOPERATIONNAME = ? " +
                                                                    " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) " +
                                                                    " AND MACHINENAME = ? "                                                                   
                                                                    , new Object[]{lotData.getKey().getLotName(),
                                                                                   lotData.getProcessFlowName(),
                                                                                   lotData.getProcessOperationName(),
                                                                                   machineGroupName,
                                                                                   machineName});
            /*	if(unitName != "")
            	{
                   
            		localRunExceptionList 
                    = ExtendedObjectProxy.getLocalRunExceptionService().select("WHERE 1=1 " +
                                                                        " AND  LOTNAME = ? " +
                                                                        " AND PROCESSFLOWNAME = ? " +
                                                                        " AND  PROCESSOPERATIONNAME = ? " +
                                                                        " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) " +
                                                                        " AND MACHINENAME = ? " +
                                                                        " AND (UNITNAME =IS NULL OR UNITNAME = ?) "                                                                   
                                                                        , new Object[]{lotData.getKey().getLotName(),
                                                                                       lotData.getProcessFlowName(),
                                                                                       lotData.getProcessOperationName(),
                                                                                       machineGroupName,
                                                                                       machineName,
                                                                                       unitName});
            	}
            	else
            	{
            		localRunExceptionList 
                    = ExtendedObjectProxy.getLocalRunExceptionService().select("WHERE 1=1 " +
                                                                        " AND  LOTNAME = ? " +
                                                                        " AND PROCESSFLOWNAME = ? " +
                                                                        " AND  PROCESSOPERATIONNAME = ? " +
                                                                        " AND (MACHINEGROUPNAME IS NULL OR MACHINEGROUPNAME = ?) " +
                                                                        " AND MACHINENAME = ? " +
                                                                        " AND UNITNAME IS NULL "                                                                
                                                                        , new Object[]{lotData.getKey().getLotName(),
                                                                                       lotData.getProcessFlowName(),
                                                                                       lotData.getProcessOperationName(),
                                                                                       machineGroupName,
                                                                                       machineName});
            	 
            	}*/
            }
            catch(Exception ex)
            { 
            	  
            }
          
            if(localRunExceptionList != null && localRunExceptionList.size() > 0)
     
            {
                
             //   List<LocalRunException> appliedLocalRunExceptionList = new ArrayList<LocalRunException>();
                
                for(LocalRunException localRunExceptionData : localRunExceptionList)
                {
                	String ihLotName = localRunExceptionData.getLotName();
                    String ihProcessFlowName = localRunExceptionData.getProcessFlowName();
                    String ihProcessOperationName = localRunExceptionData.getProcessOperationName();
                    String ihMachineGroupName = localRunExceptionData.getMachineGroupName();
                    String ihMachineName = localRunExceptionData.getMachineName();
                    String ihUnitName = localRunExceptionData.getUnitName();
                    String ihRecipeName = localRunExceptionData.getRecipeName();
  
                    if(!StringUtil.isEmpty(ihLotName)&& !StringUtil.isEmpty(ihProcessFlowName)&&
                            !StringUtil.isEmpty(ihProcessOperationName)&&
                            !StringUtil.isEmpty(ihMachineName)){
                         
                    	if(StringUtil.equals(localRunExceptionData.getLotName(),lotData.getKey().getLotName()) &&
     						   StringUtil.equals(lotData.getProcessFlowName(), localRunExceptionData.getProcessFlowName()) && 	
     						  StringUtil.equals(lotData.getProcessOperationName(), localRunExceptionData.getProcessOperationName()) &&
     						   StringUtil.equals(machineGroupName, localRunExceptionData.getMachineGroupName()) &&
     						   StringUtil.equals(machineName, localRunExceptionData.getMachineName()) &&
     						  StringUtil.equals(machineRecipeName,localRunExceptionData.getRecipeName()))
     						{
                    		localRunExceptionFlag = false;
     							break;
     						}
     					}
     					else
     					{
     						localRunExceptionFlag = false;
     					}
                         
                    if(localRunExceptionFlag == true)
                    
                    {
						
            				throw new CustomException("LocalRunExcp-0001", lotData.getKey().getLotName(),localRunExceptionData.getProcessOperationName(),localRunExceptionData.getMachineName(),localRunExceptionData.getRecipeName(),machineRecipeName);  
                    }
					
                }

    	  }
            else{
            	throw new CustomException("LocalRunExcp-0002") ;  
            }

    	}
    }
    
	public static 	Map<String, Object> commonAlterPolicy( Lot lotData, String lotJudge, String conditionValue)
	{
		Map<String, Object> alterProcess = null;
		alterProcess = LotServiceUtil.checkAlterProcessOperation( lotData, lotJudge, conditionValue );
		
		return alterProcess;
	}
	
	/**
	 * @author smkang
	 * @since 2018.08.15
	 * @param lotName
	 * @param reasonCode
	 * @param department
	 * @param eventInfo
	 * @throws CustomException
	 * @see According to user's requirement, LotName/ReasonCode/Department/EventComment are necessary to be keys.
	 */
	public void addMultiHoldLot(String lotName, String reasonCode, String department, EventInfo eventInfo) throws CustomException {
		try {
			
			if(StringUtil.isEmpty(department))
			{
				department = " ";
			}
			
			LotMultiHold lotMultiHoldData = ExtendedObjectProxy.getLotMultiHoldService().selectByKey(false, new Object[] {lotName, reasonCode, department, eventInfo.getEventComment()});
			
			lotMultiHoldData.setEventTime(eventInfo.getEventTime());
			lotMultiHoldData.setEventName(eventInfo.getEventName());
			lotMultiHoldData.setEventUser(eventInfo.getEventUser());
			lotMultiHoldData.setEventTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getLotMultiHoldService().modify(eventInfo, lotMultiHoldData);
		} catch (Exception e) {
			// TODO: handle exception
			LotMultiHold lotMultiHoldData = new LotMultiHold();
			
			lotMultiHoldData.setLotName(lotName);
			lotMultiHoldData.setReasonCode(reasonCode);
			lotMultiHoldData.setDepartment(department);
			lotMultiHoldData.setEventComment(eventInfo.getEventComment());
			lotMultiHoldData.setEventTime(eventInfo.getEventTime());
			lotMultiHoldData.setEventName(eventInfo.getEventName());
			lotMultiHoldData.setEventUser(eventInfo.getEventUser());
			lotMultiHoldData.setEventTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getLotMultiHoldService().create(eventInfo, lotMultiHoldData);
		}
	}

    
}