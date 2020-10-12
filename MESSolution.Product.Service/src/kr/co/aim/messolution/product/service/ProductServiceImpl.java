package kr.co.aim.messolution.product.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.ProcessedOperation;
import kr.co.aim.messolution.extended.object.management.data.ProductMultiHold;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTimeLot;
import kr.co.aim.messolution.extended.object.management.data.SpcProcessedOperation;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDefKey;
import kr.co.aim.greentrack.name.management.data.NameGeneratorSerialKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotInfo;
import kr.co.aim.greentrack.product.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.info.CreateRawInfo;
import kr.co.aim.greentrack.product.management.info.CreateWithLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.MakeAllocatedInfo;
import kr.co.aim.greentrack.product.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.product.management.info.MakeConsumedInfo;
import kr.co.aim.greentrack.product.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.product.management.info.MakeInProductionInfo;
import kr.co.aim.greentrack.product.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.product.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.product.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.product.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.product.management.info.RecreateInfo;
import kr.co.aim.greentrack.product.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.SetAreaInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.UndoInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ProductServiceImpl implements ApplicationContextAware {
	
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log log = LogFactory.getLog(ProductServiceImpl.class);

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {

	
		applicationContext = arg0;
	}
	 
	/*
	* Name : generateProduct
	* Desc : This function is generateProduct
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	private List<String> generateProduct(String ruleName, String quantity, String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{		
		 List<String> argSeq = new ArrayList<String>();
		 argSeq.add(lotName);
		 
		 List<String> names = null;
		 names = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, Long.valueOf(quantity));
		 
		 NameGeneratorRuleAttrDefKey keyInfo = new NameGeneratorRuleAttrDefKey();
		 keyInfo.setRuleName(ruleName);
		 keyInfo.setPosition(0); // Argument
		 NameGeneratorRuleAttrDef attrDef = NameServiceProxy.getNameGeneratorRuleAttrDefService().selectByKey(keyInfo);
		 int length = Long.valueOf(attrDef.getSectionLength()).intValue();
		  
		 NameGeneratorSerialKey key = new NameGeneratorSerialKey();
		 key.setRuleName(ruleName);
		 key.setPrefix(lotName.substring(0, length));
				 
		 NameServiceProxy.getNameGeneratorSerialService().delete(key);
		 
		 return names;		 
	}

	/*
	* Name : assignCarrier
	* Desc : This function is greenTrack API Call assignCarrier
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void assignCarrier( Product productData, 
			AssignCarrierInfo assignCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignCarrier(productData.getKey(), eventInfo, assignCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : assignLot
	* Desc : This function is greenTrack API Call assignLot
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void assignLot( Product productData, 
			AssignLotInfo assignLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignLot(productData.getKey(), eventInfo, assignLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : assignLotAndCarrier
	* Desc : This function is greenTrack API Call assignLotAndCarrier
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void assignLotAndCarrier( Product productData, 
			AssignLotAndCarrierInfo assignLotAndCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignLotAndCarrier(productData.getKey(), eventInfo, assignLotAndCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : assignProcessGroup
	* Desc : This function is greenTrack API Call assignProcessGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void assignProcessGroup( Product productData, 
			AssignProcessGroupInfo assignProcessGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignProcessGroup(productData.getKey(), eventInfo, assignProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : assignTransportGroup
	* Desc : This function is greenTrack API Call assignTransportGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void assignTransportGroup( Product productData, 
			AssignTransportGroupInfo assignTransportGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignTransportGroup(productData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/**
	 * change ProductGrade in Product
	 * @author swcho
	 * @since 2015.04.22
	 * @param productData
	 * @param changeGradeInfo
	 * @param eventInfo
	 * @return
	 * @throws CustomException
	 */
	public Product changeGrade(Product productData, ChangeGradeInfo changeGradeInfo, EventInfo eventInfo)
		throws CustomException
	{
		try
		{
			productData = ProductServiceProxy.getProductService().changeGrade(productData.getKey(), eventInfo, changeGradeInfo);
			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	    
	    return productData;
	}

	/**
	 * ordinary change spec
	 * @author swcho
	 * @since 2014.08.27
	 * @param eventInfo
	 * @param productData
	 * @param changeSpecInfo
	 * @throws CustomException
	 */
	public void changeSpec(EventInfo eventInfo, Product productData, ChangeSpecInfo changeSpecInfo)
		throws CustomException
	{
		try
		{
			ProductServiceProxy.getProductService().changeSpec(productData.getKey(), eventInfo, changeSpecInfo);
			
			log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	}

	/*
	* Name : create
	* Desc : This function is greenTrack API Call create
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void create( Product productData, 
			CreateInfo createInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().create(productData.getKey(), eventInfo, createInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : createRaw
	* Desc : This function is greenTrack API Call createRaw
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void createRaw( Product productData, 
			CreateRawInfo createRawInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().createRaw(productData.getKey(), eventInfo, createRawInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : createWithLot
	* Desc : This function is greenTrack API Call createWithLot
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void createWithLot( Product productData, 
			CreateWithLotInfo createWithLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().createWithLot(productData.getKey(), eventInfo, createWithLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : consumeMaterials
	* Desc : This function is greenTrack API Call consumeMaterials
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void consumeMaterials( Product productData, 
			ConsumeMaterialsInfo consumeMaterialsInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().consumeMaterials(productData.getKey(), eventInfo, consumeMaterialsInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignCarrier
	* Desc : This function is greenTrack API Call deassignCarrier
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void deassignCarrier( Product productData, 
			DeassignCarrierInfo deassignCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignCarrier(productData.getKey(), eventInfo, deassignCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignLot
	* Desc : This function is greenTrack API Call deassignLot
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void deassignLot( Product productData, 
			DeassignLotInfo deassignLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignLot(productData.getKey(), eventInfo, deassignLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignLotAndCarrier
	* Desc : This function is greenTrack API Call deassignLotAndCarrier
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void deassignLotAndCarrier( Product productData, 
			DeassignLotAndCarrierInfo deassignLotAndCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignLotAndCarrier(productData.getKey(), eventInfo, deassignLotAndCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignProcessGroup
	* Desc : This function is greenTrack API Call deassignProcessGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void deassignProcessGroup( Product productData, 
			DeassignProcessGroupInfo deassignProcessGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignProcessGroup(productData.getKey(), eventInfo, deassignProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : deassignTransportGroup
	* Desc : This function is greenTrack API Call deassignTransportGroup
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void deassignTransportGroup( Product productData, 
			DeassignTransportGroupInfo deassignTransportGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignTransportGroup(productData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeAllocated
	* Desc : This function is greenTrack API Call makeAllocated
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeAllocated( Product productData, 
			MakeAllocatedInfo makeAllocatedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeAllocated(productData.getKey(), eventInfo, makeAllocatedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeCompleted
	* Desc : This function is greenTrack API Call makeCompleted
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeCompleted( Product productData, 
			MakeCompletedInfo makeCompletedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeCompleted(productData.getKey(), eventInfo, makeCompletedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeConsumed
	* Desc : This function is greenTrack API Call makeConsumed
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeConsumed( Product productData, 
			MakeConsumedInfo makeConsumedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeConsumed(productData.getKey(), eventInfo, makeConsumedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeIdle
	* Desc : This function is greenTrack API Call makeIdle
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeIdle( Product productData, 
			MakeIdleInfo makeIdleInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeIdle(productData.getKey(), eventInfo, makeIdleInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeInProduction
	* Desc : This function is greenTrack API Call makeInProduction
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeInProduction( Product productData, 
			MakeInProductionInfo makeInProductionInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeInProduction(productData.getKey(), eventInfo, makeInProductionInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeInRework
	* Desc : This function is greenTrack API Call makeInRework
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeInRework( Product productData, 
			MakeInReworkInfo makeInReworkInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeInRework(productData.getKey(), eventInfo, makeInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeNotOnHold
	* Desc : This function is greenTrack API Call makeNotOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeNotOnHold( Product productData, 
			MakeNotOnHoldInfo makeNotOnHoldInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeNotOnHold(productData.getKey(), eventInfo, makeNotOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeOnHold
	* Desc : This function is greenTrack API Call makeOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeOnHold( Product productData, 
			MakeOnHoldInfo makeOnHoldInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeOnHold(productData.getKey(), eventInfo, makeOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeProcessing
	* Desc : This function is greenTrack API Call makeProcessing
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeProcessing( Product productData, 
			MakeProcessingInfo makeProcessingInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeProcessing(productData.getKey(), eventInfo, makeProcessingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeReceived
	* Desc : This function is greenTrack API Call makeReceived
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeReceived( Product productData, 
			MakeReceivedInfo makeReceivedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeReceived(productData.getKey(), eventInfo, makeReceivedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeScrapped
	* Desc : This function is greenTrack API Call makeScrapped
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeScrapped( Product productData, 
			MakeScrappedInfo makeScrappedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeScrapped(productData.getKey(), eventInfo, makeScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeShipped
	* Desc : This function is greenTrack API Call makeShipped
	* Author : AIM Systems, Inc
	* Date : 2011.01.21
	*/
	public void makeShipped( Product productData, 
			MakeShippedInfo makeShippedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeShipped(productData.getKey(), eventInfo, makeShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeTraveling
	* Desc : This function is greenTrack API Call makeTraveling
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void makeTraveling( Product productData, 
			MakeTravelingInfo makeTravelingInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeTraveling(productData.getKey(), eventInfo, makeTravelingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeUnScrapped
	* Desc : This function is greenTrack API Call makeUnScrapped
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void makeUnScrapped( Product productData, 
			MakeUnScrappedInfo makeUnScrappedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeUnScrapped(productData.getKey(), eventInfo, makeUnScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : makeUnShipped
	* Desc : This function is greenTrack API Call makeUnShipped
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void makeUnShipped( Product productData, 
			MakeUnShippedInfo makeUnShippedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeUnShipped(productData.getKey(), eventInfo, makeUnShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : recreate
	* Desc : This function is greenTrack API Call recreate
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void recreate( Product productData, 
			RecreateInfo recreateInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().recreate(productData.getKey(), eventInfo, recreateInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : separate
	* Desc : This function is greenTrack API Call separate
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void separate( Product productData, 
			SeparateInfo separateInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		
		ProductServiceProxy.getProductService().separate(productData.getKey(), eventInfo, separateInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : setArea
	* Desc : This function is greenTrack API Call setArea
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void setArea( Product productData, 
			SetAreaInfo setAreaInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().setArea(productData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/*
	* Name : setEvent
	* Desc : This function is greenTrack API Call setEvent
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void setEvent( Product productData, 
			SetEventInfo setEventInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	/**
	 * set material location
	 * @author swcho
	 * @since 2014.08.27
	 * @param eventInfo
	 * @param productData
	 * @param setMaterialLocationInfo
	 * @throws CustomException
	 */
	public void setMaterialLocation(EventInfo eventInfo, Product productData, SetMaterialLocationInfo setMaterialLocationInfo)
		throws CustomException
	{
		try
		{
			ProductServiceProxy.getProductService().setMaterialLocation(productData.getKey(), eventInfo, setMaterialLocationInfo);
			
			log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));
			
			// Added by smkang on 2018.10.13 - Although machine state is not changed, if current state is RUN, ReasonCode should be changed according to EDO's rule.
			// Modified by smkang on 2019.06.18 - When a PEX receives A_GlassInIndexer, MaterialLocationName will be deleted, so main machine will be selected at that time.  
			String currentLocationName = setMaterialLocationInfo.getMaterialLocationName();// add by GJJ  20191111 mantis 5083
			//String currentLocationName = StringUtils.isNotEmpty(setMaterialLocationInfo.getMaterialLocationName()) ? setMaterialLocationInfo.getMaterialLocationName() : productData.getMachineName();// modfiy by GJJ  20191111 mantis 5083
						
			if (StringUtils.isNotEmpty(currentLocationName)) {
				Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(currentLocationName);
				
				/* 20181027, hhlee, modify, GlassOutIndexer, GlassInUnit, GlassInSubUnit Message is not checked a BCASECSSFlag ==>> */
				//if (machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN) &&
				//	MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
				//	EventInfo tempEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());
				//	tempEventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(tempEventInfo, currentLocationName, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN);
                //
				//	// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
				//	//if (!tempEventInfo.getReasonCode().equals(machineData.getReasonCode())) {
				//	if (!tempEventInfo.getReasonCode().equals(machineData.getUdfs().get("STATEREASONCODE"))) {
				//		machineData.getUdfs().put("STATEREASONCODE", tempEventInfo.getReasonCode());
				//		
				//		/* 20181026, hhlee, modify, Machine State is not changed(MachineState = RUN ==> MachineState = RUN) ==>> */
				//		kr.co.aim.greentrack.machine.management.info.SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
				//		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, tempEventInfo);
				//		//MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN);
				//		//MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, tempEventInfo);
				//		/* <<== 20181026, hhlee, modify, Machine State is not changed(MachineState = RUN ==> MachineState = RUN) */
				//	}
				//}
				/* 20181027, hhlee, modify, ARRAY는 MACHINE_STATE_RUN, MACHINE_STATE_MQC 일 경우 ReasonCode를 변경 가능하고, OLED는 MACHINE_STATE_RUN 일때 ReasonCode를 변경이 가능하다.*/
				if (machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN) ||
				        machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC))
                {
                  EventInfo tempEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());
                  //tempEventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(tempEventInfo, currentLocationName, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN); modfiy by GJJ 20191111 mantis 5083
                  tempEventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(tempEventInfo, currentLocationName, machineData.getMachineStateName());// add by GJJ  20191111 mantis 5083
                  // Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
                  //if (!tempEventInfo.getReasonCode().equals(machineData.getReasonCode())) {
                  if (!tempEventInfo.getReasonCode().equals(machineData.getUdfs().get("STATEREASONCODE"))) {
                	  // Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
                	  machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
  					
                      machineData.getUdfs().put("STATEREASONCODE", tempEventInfo.getReasonCode());
                      
                      /* 20181026, hhlee, modify, Machine State is not changed(MachineState = RUN ==> MachineState = RUN) ==>> */
                      kr.co.aim.greentrack.machine.management.info.SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
                      /* 20190325, hhlee, modify, setEvent -> setEventByNotUpdateOldStateReasonCode ==>> */
                      //MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, tempEventInfo);
                      MESMachineServiceProxy.getMachineServiceImpl().setEventByNotUpdateOldStateReasonCode(machineData, setEventInfo, tempEventInfo);
                      /* <<== 20190325, hhlee, modify, setEvent -> setEventByNotUpdateOldStateReasonCode */
                      //MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN);
                      //MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, tempEventInfo);
                      /* <<== 20181026, hhlee, modify, Machine State is not changed(MachineState = RUN ==> MachineState = RUN) */
                  }
                }
				/* <<== 20181027, hhlee, modify, GlassOutIndexer, GlassInUnit, GlassInSubUnit Message is not checked a BCASECSSFlag */
			}
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	}

	/*
	* Name : undo
	* Desc : This function is greenTrack API Call undo
	* Author : AIM Systems, Inc
	* Date : 2011.01.22
	*/
	public void undo( Product productData, 
			UndoInfo undoInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().undo(productData.getKey(), eventInfo, undoInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	public Map<String, Object> checkPriorityPolicy(Lot lotData) throws CustomException
	{
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		String strSql = "SELECT FACTORYNAME,    " +
				"         PRODUCTSPECNAME,    " +
				"         PRODUCTSPECVERSION,    " +
				"         ECCODE,    " +
				"         PROCESSFLOWNAME,    " +
				"         PROCESSFLOWVERSION,    " +
				"         PROCESSOPERATIONNAME,    " +
				"         PROCESSOPERATIONVERSION,   " +
				"         GENERATIONTYPE    " +
				"    FROM TPEFOPOLICY T, POSQUEUETIME P    " +
				"   WHERE     T.CONDITIONID = P.CONDITIONID    " +
				"         AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = :STAR))    " +
				"         AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = :STAR))    " +
				"         AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = :STAR))    " +
				"         AND ((T.GENERATIONTYPE = :GENERATIONTYPE) OR (T.GENERATIONTYPE = :STAR))   " +
				"         AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = :STAR))    " +
				"         AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = :STAR))    " +
				"         AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = :STAR))    " +
				"         AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = :STAR))    " +
				"         AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = :STAR))    " +
				"ORDER BY FACTORYNAME,    " +
				"         PRODUCTSPECNAME,    " +
				"         PRODUCTSPECVERSION,    " +
				"         ECCODE,    " +
				"         PROCESSFLOWNAME,    " +
				"         PROCESSFLOWVERSION,    " +
				"         PROCESSOPERATIONNAME,    " +
				"         PROCESSOPERATIONVERSION,  " +
				"         GENERATIONTYPE ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("GENERATIONTYPE", productSpecData.getUdfs().get("GENERATIONTYPE"));
		bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("STAR", "*");

		List<Map<String, Object>> queuePolicyData = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

		
		if(queuePolicyData != null && queuePolicyData.size() > 0)
		{
			return queuePolicyData.get(queuePolicyData.size() - 1);
		}
		else
		{
			return null;
		}
	}
	
	public List<Map<String, Object>> checkQTimePolicy(Map<String, Object> qTimeTPEFOPolicyData)
	{
		String strSql = "SELECT T.FACTORYNAME,     " +
				"       T.PRODUCTSPECNAME,     " +
				"       T.PRODUCTSPECVERSION,   " +
				"       T.GENERATIONTYPE,    " +
				"       T.ECCODE,     " +
				"       T.PROCESSFLOWNAME,     " +
				"       T.PROCESSFLOWVERSION,     " +
				"       T.PROCESSOPERATIONNAME,     " +
				"       T.PROCESSOPERATIONVERSION,     " +
				"       P.TOFACTORYNAME,     " +
				"       P.TOPROCESSFLOWNAME,     " +
				"       P.TOPROCESSFLOWVERSION,     " +
				"       P.TOPROCESSOPERATIONNAME,     " +
				"       P.TOPROCESSOPERATIONVERSION,     " +
				"       P.TOEVENTNAME,     " +
				"       P.QUEUETIMETYPE,     " +
				"       P.WARNINGDURATIONLIMIT,     " +
				"       P.INTERLOCKDURATIONLIMIT,    " +
				"       P.DEPARTMENTNAME    " +
				"  FROM TPEFOPOLICY T, POSQUEUETIME P     " +
				" WHERE     T.CONDITIONID = P.CONDITIONID     " +
				"       AND T.FACTORYNAME = :FACTORYNAME     " +
				"       AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME     " +
				"       AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION     " +
				"       AND T.GENERATIONTYPE = :GENERATIONTYPE   " +
				"       AND T.ECCODE = :ECCODE     " +
				"       AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME     " +
				"       AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION     " +
				"       AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME     " +
				"       AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION  ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", qTimeTPEFOPolicyData.get("FACTORYNAME"));
		bindMap.put("PRODUCTSPECNAME", qTimeTPEFOPolicyData.get("PRODUCTSPECNAME"));
		bindMap.put("PRODUCTSPECVERSION", qTimeTPEFOPolicyData.get("PRODUCTSPECVERSION"));
		bindMap.put("GENERATIONTYPE", qTimeTPEFOPolicyData.get("GENERATIONTYPE"));
		bindMap.put("ECCODE", qTimeTPEFOPolicyData.get("ECCODE"));
		bindMap.put("PROCESSFLOWNAME", qTimeTPEFOPolicyData.get("PROCESSFLOWNAME"));
		bindMap.put("PROCESSFLOWVERSION", qTimeTPEFOPolicyData.get("PROCESSFLOWVERSION"));
		bindMap.put("PROCESSOPERATIONNAME", qTimeTPEFOPolicyData.get("PROCESSOPERATIONNAME"));
		bindMap.put("PROCESSOPERATIONVERSION", qTimeTPEFOPolicyData.get("PROCESSOPERATIONVERSION"));

		List<Map<String, Object>> queuePolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(queuePolicyData != null && queuePolicyData.size() > 0)
		{
			return queuePolicyData;
		}
		else
		{
			return null;
		}
	}
	
	public List<Map<String, Object>> checkQTimeEndPolicy(Map<String, Object> qTimeTPEFOPolicyData)
	{
		String strSql = "SELECT T.FACTORYNAME,     " +
				"       T.PRODUCTSPECNAME,     " +
				"       T.PRODUCTSPECVERSION,   " +
				"       T.GENERATIONTYPE,    " +
				"       T.ECCODE,     " +
				"       T.PROCESSFLOWNAME,     " +
				"       T.PROCESSFLOWVERSION,     " +
				"       T.PROCESSOPERATIONNAME,     " +
				"       T.PROCESSOPERATIONVERSION,     " +
				"       P.TOFACTORYNAME,     " +
				"       P.TOPROCESSFLOWNAME,     " +
				"       P.TOPROCESSFLOWVERSION,     " +
				"       P.TOPROCESSOPERATIONNAME,     " +
				"       P.TOPROCESSOPERATIONVERSION,     " +
				"       P.TOEVENTNAME,     " +
				"       P.QUEUETIMETYPE,     " +
				"       P.WARNINGDURATIONLIMIT,     " +
				"       P.INTERLOCKDURATIONLIMIT,    " +
				"       P.DEPARTMENTNAME    " +
				"  FROM TPEFOPOLICY T, POSQUEUETIME P     " +
				" WHERE     T.CONDITIONID = P.CONDITIONID     " +
				"       AND T.FACTORYNAME = :FACTORYNAME     " +
				"       AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME     " +
				"       AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION     " +
				"       AND T.GENERATIONTYPE = :GENERATIONTYPE   " +
				"       AND T.ECCODE = :ECCODE     " +
				"       AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME     " +
				"       AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION     " +
				"       AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME     ";


		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", qTimeTPEFOPolicyData.get("FACTORYNAME"));
		bindMap.put("PRODUCTSPECNAME", qTimeTPEFOPolicyData.get("PRODUCTSPECNAME"));
		bindMap.put("PRODUCTSPECVERSION", qTimeTPEFOPolicyData.get("PRODUCTSPECVERSION"));
		bindMap.put("GENERATIONTYPE", qTimeTPEFOPolicyData.get("GENERATIONTYPE"));
		bindMap.put("ECCODE", qTimeTPEFOPolicyData.get("ECCODE"));
		bindMap.put("PROCESSFLOWNAME", qTimeTPEFOPolicyData.get("PROCESSFLOWNAME"));
		bindMap.put("PROCESSFLOWVERSION", qTimeTPEFOPolicyData.get("PROCESSFLOWVERSION"));
		bindMap.put("TOPROCESSOPERATIONNAME", qTimeTPEFOPolicyData.get("TOPROCESSOPERATIONNAME"));

		List<Map<String, Object>> queuePolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(queuePolicyData != null && queuePolicyData.size() > 0)
		{
			return queuePolicyData;
		}
		else
		{
			return null;
		}
	}
	
	public String checkMQCMachine(Lot lotData, String MachineName)
	{
		String strSql = " SELECT DISTINCT A.LOTNAME, A.CARRIERNAME, A.PROCESSFLOWNAME,  B.PROCESSOPERATIONNAME, B.MACHINEGROUPNAME, B.MACHINENAME, C.RECIPENAME";
		strSql = strSql + " FROM CT_MQCJOB A JOIN CT_MQCJOBOPER B ON A.MQCJOBNAME = B.MQCJOBNAME JOIN CT_MQCJOBPOSITION C ON B.MQCJOBNAME = C.MQCJOBNAME AND C.PROCESSOPERATIONNAME = B.PROCESSOPERATIONNAME";
		strSql = strSql + " AND A.LOTNAME = :LOTNAME";
		strSql = strSql + " AND A.CARRIERNAME = :CARRIERNAME";
		strSql = strSql + " AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME";
		strSql = strSql + " AND B.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME";
		strSql = strSql + " AND A.MQCSTATE = 'Executing'";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("CARRIERNAME", lotData.getCarrierName());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		
		List<Map<String, Object>> checkMQCData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(checkMQCData == null || checkMQCData.size() == 0)
		{
			return "";
		}
		
		/* 20190116, hhlee, add, change validation logic(MQC Machine may not exist) ==>> */
		if(checkMQCData.size() > 0)
        {
		    /* 20190124, hhlee, modify , add logic null value check */
            if(checkMQCData.get(0).get("MACHINENAME") != null && 
                    StringUtil.isNotEmpty(checkMQCData.get(0).get("MACHINENAME").toString()))
            {
              if(StringUtil.equals(MachineName, checkMQCData.get(0).get("MACHINENAME").toString()))
              {
                  return (String)checkMQCData.get(0).get("LOTNAME");
              } else {
                  return "";
              }
            }
        }
        return checkMQCData.get(0).get("LOTNAME").toString(); // Success;
		
		//if(checkMQCData.size() > 0)
		//{
		//    if(StringUtil.isNotEmpty(checkMQCData.get(0).get("MACHINENAME").toString()))
		//    {
    	//	    if(StringUtil.equals(MachineName, checkMQCData.get(0).get("MACHINENAME").toString()))
    	//		{
    	//			return (String)checkMQCData.get(0).get("RECIPENAME");
    	//		} else {
    	//			return "";
    	//		}
		//    }
		//}
		//return checkMQCData.get(0).get("RECIPENAME").toString(); // Success;
		/* <<== 20190116, hhlee, add, change validation logic(MQC Machine may not exist) */
	}
	
	public boolean checkMQCMachineforTrackin(Lot lotData, String MachineName ) 
	{
		String strSql = " SELECT B.MACHINENAME FROM CT_MQCJOB A JOIN CT_MQCJOBOPER B ON A.MQCJOBNAME = B.MQCJOBNAME ";
		strSql = strSql + " WHERE A.LOTNAME = :LOTNAME AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME AND B.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME AND A.MQCSTATE = 'Executing' ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		
		List<Map<String, Object>> checkMQCData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(checkMQCData != null && checkMQCData.size() > 0)
		{ // If machine name has null value, return true;
			if(checkMQCData.get(0).get("MACHINENAME") != null)
			{
				if(!StringUtils.equals(MachineName, checkMQCData.get(0).get("MACHINENAME").toString()))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void checkMQCLot(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		if(processFlowData != null)
		{
			if(StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				String strSql = "SELECT * " +
						"  FROM CT_MQCJOB " +
						" WHERE     LOTNAME = :LOTNAME " +
						"       AND CARRIERNAME = :CARRIERNAME " +
						"       AND FACTORYNAME = :FACTORYNAME " +
						"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
						"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
						"       AND MQCSTATE = :MQCSTATE ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTNAME", lotData.getKey().getLotName());
				bindMap.put("CARRIERNAME", lotData.getCarrierName());
				bindMap.put("FACTORYNAME", lotData.getFactoryName());
				bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
				bindMap.put("MQCSTATE", "Executing");

				List<Map<String, Object>> checkMQCData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
				
				if(checkMQCData == null || checkMQCData.size() == 0)
				{
					throw new CustomException("MQC-0044", lotData.getKey().getLotName());
				}
			}
		}
	}
	
	public void checkRecipeIdleTime(String machineName, String recipeName, String productSpecName, String processOperationName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName) && !StringUtils.isEmpty(productSpecName) && !StringUtils.isEmpty(processOperationName))
		{
			RecipeIdleTimeLot recipeIdleTimeLotData = null;
			
			try
			{
				recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTimeLotData = null;
			}
			
			if(recipeIdleTimeLotData != null)
			{
				if(StringUtils.equals(recipeIdleTimeLotData.getfirstLotFlag(), "Y"))
				{
					throw new CustomException("RECIPE-0011", "");
				}
			}
		}
	}
	
	public void firstFlagRecipeIdleTimeLot(String lotName, String carrierName, String machineName, String recipeName, String productSpecName, String processOperationName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName) && !StringUtils.isEmpty(productSpecName) && !StringUtils.isEmpty(processOperationName))
		{
			RecipeIdleTimeLot recipeIdleTimeLotData = null;
			
			try
			{
				recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTimeLotData = null;
			}
			
			if(recipeIdleTimeLotData != null)
			{
				if(!StringUtils.equals(recipeIdleTimeLotData.getfirstLotFlag(), "Y"))
				{
					String strSql = "SELECT R.MACHINENAME, R.RECIPENAME, R.IDLETIME " +
							"    FROM CT_RECIPEIDLETIMELOT RL, CT_RECIPEIDLETIME R " +
							"   WHERE     RL.MACHINENAME = :MACHINENAME " +
							"         AND RL.RECIPENAME = :RECIPENAME " +
							"         AND RL.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
							"         AND RL.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
							"         AND RL.MACHINENAME = R.MACHINENAME " +
							"         AND ( (RL.RECIPENAME = R.RECIPENAME) OR (R.RECIPENAME = :STAR)) " +
							"ORDER BY MACHINENAME, RECIPENAME ";
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("MACHINENAME", recipeIdleTimeLotData.getmachineName());
					bindMap.put("RECIPENAME", recipeIdleTimeLotData.getrecipeName());
					bindMap.put("PRODUCTSPECNAME", recipeIdleTimeLotData.getproductSpecName());
					bindMap.put("PROCESSOPERATIONNAME", recipeIdleTimeLotData.getprocessOperationName());
					bindMap.put("STAR", "*");
										
					List<Map<String, Object>> priorIdleTime = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
					
					if(priorIdleTime != null && priorIdleTime.size() > 0)
					{
						strSql = "SELECT * " +
								"  FROM CT_RECIPEIDLETIMELOT  " +
								" WHERE MACHINENAME = :MACHINENAME  " +
								"       AND RECIPENAME = :RECIPENAME  " +
								"       AND PRODUCTSPECNAME = :PRODUCTSPECNAME  " +
								"       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  " +
								"       AND :IDLETIME <= TO_NUMBER ( (SYSDATE - LASTRUNTIME) * 24 * 60)  ";
						
						bindMap.put("IDLETIME", priorIdleTime.get(priorIdleTime.size() - 1).get("IDLETIME").toString());
						
						List<Map<String, Object>> checkFirstFlag = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
						
						if(checkFirstFlag != null && checkFirstFlag.size() > 0)
						{
							recipeIdleTimeLotData.setfirstLotFlag("Y");
							recipeIdleTimeLotData.setfirstCstID(carrierName);
							recipeIdleTimeLotData.setfirstLotID(lotName);
							recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
							recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
							recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
							recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
							
							eventInfo.setEventName("TrackIn");
							ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo, recipeIdleTimeLotData);
						}
					}
				}
			}
		}
	}
	
	public void cancelTIRecipeIdleTimeLot(String machineName, String recipeName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName))
		{
			RecipeIdleTimeLot recipeIdleTimeLotData = null;
			
			try
			{
				recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTimeLotData = null;
			}
			
			if(recipeIdleTimeLotData == null)
			{
				try
				{
					recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, "*"});
				}
				catch (Exception ex)
				{
					recipeIdleTimeLotData = null;
				}
			}
			
			if(recipeIdleTimeLotData != null)
			{
				String strSql = "SELECT MACHINENAME, " +
						"         RECIPENAME, " +
						"         LASTRUNTIME " +
						"    FROM CT_RECIPEIDLETIMELOTHIST " +
						"   WHERE     MACHINENAME = :MACHINENAME " +
						"         AND RECIPENAME = :RECIPENAME " +
						"ORDER BY TIMEKEY DESC ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MACHINENAME", recipeIdleTimeLotData.getmachineName());
				bindMap.put("RECIPENAME", recipeIdleTimeLotData.getrecipeName());

				List<Map<String, Object>> checkFirstFlag = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
				
				if(checkFirstFlag !=null && checkFirstFlag.size()>0)
				{
					if(StringUtil.equals(recipeIdleTimeLotData.getfirstLotFlag(),"Y"))
					{
						recipeIdleTimeLotData.setfirstLotFlag("");
						recipeIdleTimeLotData.setfirstCstID("");
						recipeIdleTimeLotData.setfirstLotID("");
						recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
						recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
						recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
						recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
						recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
						
						eventInfo.setEventName("CancelTrackIn");
						ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo, recipeIdleTimeLotData);
					}
				}
			}
		}
	}
	
	public boolean createRecipeIdleTimeLot(String machineName, String recipeName, String productSpecName, String processOperationName, String areaName, String factoryName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName) && !StringUtils.isEmpty(productSpecName) && !StringUtils.isEmpty(processOperationName))
		{
			RecipeIdleTime recipeIdleTime = null;
			
			try
			{
				recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTime = null;
			}
			
			if(recipeIdleTime == null)
			{
				try
				{
					recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, "*"});
				}
				catch (Exception ex)
				{
					recipeIdleTime = null;
				}
			}
			
			if(recipeIdleTime != null)
			{
				if(StringUtils.equals(recipeIdleTime.getvalidFlag(), "Y"))
				{
					RecipeIdleTimeLot recipeIdleTimeLotData = null;
					
					try
					{
						recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
					}
					catch (Exception ex)
					{
						recipeIdleTimeLotData = null;
					}
					
					if(recipeIdleTimeLotData == null)
					{
						recipeIdleTimeLotData = new RecipeIdleTimeLot(machineName, recipeName);
						recipeIdleTimeLotData.setfactoryName(factoryName);
						recipeIdleTimeLotData.setareaName(areaName);
						recipeIdleTimeLotData.setlastRunTime(eventInfo.getEventTime());
						recipeIdleTimeLotData.setfirstLotFlag("");
						recipeIdleTimeLotData.setfirstCstID("");
						recipeIdleTimeLotData.setfirstLotID("");
						recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
						recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
						recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
						recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
						recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getRecipeIdleTimeLotService().create(eventInfo, recipeIdleTimeLotData);
					}
					
					else
					{
						if(!StringUtils.equals(recipeIdleTimeLotData.getfirstLotFlag(), "Y"))
						{
							recipeIdleTimeLotData.setlastRunTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
							recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
							recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
							recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
							
							eventInfo.setEventName("TrackOut");
							eventInfo.setEventComment("Recipe Idle Time First Lot");
							ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo, recipeIdleTimeLotData);
						}
						else
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public void checkFinishMQCJob(Lot lotData, EventInfo eventInfo, ProcessFlow processFlowData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String strSql = "SELECT MQCJOBNAME " +
				"  FROM CT_MQCJOB  " +
				" WHERE     LOTNAME = :LOTNAME  " +
				"       AND ((:CARRIERNAME IS NULL) OR (CARRIERNAME = :CARRIERNAME))  " +
				"       AND FACTORYNAME = :FACTORYNAME  " +
				"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
				"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
				"       AND MQCSTATE = :MQCSTATE  ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("CARRIERNAME", lotData.getCarrierName());
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", processFlowData.getKey().getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("MQCSTATE", "Executing");

		List<Map<String, Object>> mqcJobList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(mqcJobList == null || mqcJobList.size() > 1)
		{
			throw new CustomException("MQC-0045", "");
		}
		
		MQCJob mqcJob = null;
		
		try
		{
			mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobList.get(0).get("MQCJOBNAME").toString()});
		}
		catch (Exception ex)
		{
			mqcJob = null;
		}
		
		if(mqcJob == null)
		{
			throw new CustomException("MQC-0031", mqcJobList.get(0).get("MQCJOBNAME").toString());
		}
		
		if(!StringUtils.equals(mqcJob.getmqcState(), "Executing"))
		{
			throw new CustomException("MQC-0041", mqcJobList.get(0).get("MQCJOBNAME").toString());
		}
		
		//mqcJob.setmqcCount(mqcJob.getmqcCount() + 1);
		mqcJob.setmqcState("Wait");
		mqcJob.setLastEventUser(eventInfo.getEventUser());
		mqcJob.setLastEventComment(eventInfo.getEventComment());
		mqcJob.setLastEventTime(eventInfo.getEventTime());
		mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
		mqcJob.setLastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getMQCJobService().modify(eventInfo, mqcJob);
	}
	
	public void updateMQCCountToProduct(Lot lotData, EventInfo eventInfo, ProcessFlow processFlowData, String beforeProcessOperationName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String strSql = "SELECT MQCJOBNAME " +
				"  FROM CT_MQCJOB  " +
				" WHERE     LOTNAME = :LOTNAME  " +
				"       AND ((:CARRIERNAME IS NULL) OR (CARRIERNAME = :CARRIERNAME))  " +
				"       AND FACTORYNAME = :FACTORYNAME  " +
				"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
				"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
				"       AND MQCSTATE = :MQCSTATE  ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("CARRIERNAME", lotData.getCarrierName());
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", processFlowData.getKey().getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("MQCSTATE", "Executing");

		List<Map<String, Object>> mqcJobList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(mqcJobList == null || mqcJobList.size() > 1)
		{
			throw new CustomException("MQC-0045", "");
		}
		
		MQCJob mqcJob = null;
		
		try
		{
			mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobList.get(0).get("MQCJOBNAME").toString()});
		}
		catch (Exception ex)
		{
			mqcJob = null;
		}
		
		if(mqcJob == null)
		{
			throw new CustomException("MQC-0031", "NULL");
		}
		
		if(!StringUtils.equals(mqcJob.getmqcState(), "Executing"))
		{
			throw new CustomException("MQC-0041", mqcJobList.get(0).get("MQCJOBNAME").toString());
		}
		
		String strProductSql = "SELECT MQCJOBNAME, " +
				"       PROCESSOPERATIONNAME, " +
				"       PROCESSOPERATIONVERSION, " +
				"       POSITION, " +
				"       PRODUCTNAME, " +
				"       MQCCOUNTUP " +
				"  FROM CT_MQCJOBPOSITION " +
				" WHERE MQCJOBNAME = :MQCJOBNAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ";

		Map<String, Object> bindProductMap = new HashMap<String, Object>();
		bindMap.put("MQCJOBNAME", mqcJob.getmqcJobName());
		bindMap.put("PROCESSOPERATIONNAME", beforeProcessOperationName);

		List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strProductSql, bindMap);
		
		if(mqcJobPositionList != null)
		{
			for(int i = 0; i < mqcJobPositionList.size(); i++)
			{
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(mqcJobPositionList.get(i).get("PRODUCTNAME").toString());

				if(Long.valueOf(mqcJobPositionList.get(i).get("MQCCOUNTUP").toString()) > 0)
				{
					String productMQCCount = "0";
					// 2018.08.13
					String ProductTotalMQCCount = "0";
					
					if(!StringUtil.isEmpty(productData.getUdfs().get("MQCCOUNT")))
					{
						productMQCCount = productData.getUdfs().get("MQCCOUNT").toString();
					}
					long mqcCount = Long.valueOf(productMQCCount) + Long.valueOf(mqcJobPositionList.get(i).get("MQCCOUNTUP").toString());
					
					if(!StringUtil.isEmpty(productData.getUdfs().get("TOTALMQCCOUNT")))
					{
						ProductTotalMQCCount = productData.getUdfs().get("TOTALMQCCOUNT").toString();
					}
					long TotalmqcCount = Long.valueOf(ProductTotalMQCCount) + Long.valueOf(mqcJobPositionList.get(i).get("MQCCOUNTUP").toString());
					
					// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//					Map<String,String> udfs = productData.getUdfs();
//					udfs.put("MQCCOUNT", String.valueOf(mqcCount));
//					udfs.put("TOTALMQCCOUNT", String.valueOf(TotalmqcCount));
//					productData.setUdfs(udfs);
//					ProductServiceProxy.getProductService().update(productData);
					
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("MQCCOUNT", String.valueOf(mqcCount));
					setEventInfo.getUdfs().put("TOTALMQCCOUNT", String.valueOf(TotalmqcCount));
					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
				}
			}
		}
	}
	
	public String slotMQCProduct(Product productData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		
	    String strSql = "SELECT P.RECIPENAME, M.* ,P.PRODUCTNAME " +
	    		"  FROM CT_MQCJOB M, CT_MQCJOBPOSITION P  " +
	    		" WHERE     M.MQCJOBNAME = P.MQCJOBNAME  " +
	    		"       AND M.MQCSTATE = :MQCSTATE  " +
	    		"       AND M.LOTNAME = :LOTNAME  " +
	    		"       AND M.CARRIERNAME = :CARRIERNAME  " +
	    		"       AND M.FACTORYNAME = :FACTORYNAME  " +
	    		"       AND M.PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
	    		"       AND M.PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
	    		"       AND P.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  " +
	    		"       AND P.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION  " +
	    		"       AND P.PRODUCTNAME = :PRODUCTNAME  " +
	    		"       AND P.POSITION = :POSITION  "  ;
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MQCSTATE", "Executing");
		bindMap.put("LOTNAME", productData.getLotName());
		bindMap.put("CARRIERNAME", productData.getCarrierName());
		bindMap.put("FACTORYNAME", productData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", productData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", productData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", productData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", productData.getProcessOperationVersion());
		bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
		bindMap.put("POSITION", productData.getPosition());
		

		List<Map<String, Object>> mqcJobList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(mqcJobList != null && mqcJobList.size() > 0)
		{
			return (String)mqcJobList.get(0).get("RECIPENAME");
		}
		
		return null;
	}
    
	/**
	 * 
	 * @Name     slotPositionMQCProductForBranch
	 * @since    2020. 4. 10.
	 * @author   GJJ
	 * @contents 
	 *           
	 * @param productData
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws CustomException
	 */
	public List<Map<String, Object>> slotPositionMQCProductForBranch(Product productData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    {
	    List<Map<String, Object>> mqcJobPositionList = null;
        try
        {
            String strSql = "SELECT DISTINCT P.POSITION, P.RECIPENAME, M.* " +
                    "  FROM CT_MQCJOB M, CT_MQCJOBPOSITION P " +
                    " WHERE     M.MQCJOBNAME = P.MQCJOBNAME " +
                    "       AND M.MQCSTATE = :MQCSTATE " +
                    "       AND M.LOTNAME = :LOTNAME " +
                    "       AND M.CARRIERNAME = :CARRIERNAME " +
                    "       AND M.FACTORYNAME = :FACTORYNAME " +
                    "       AND P.PRODUCTNAME = :PRODUCTNAME " +
                    "       AND P.POSITION = :POSITION ";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MQCSTATE", "Executing");
            bindMap.put("LOTNAME", productData.getLotName());
            bindMap.put("CARRIERNAME", productData.getCarrierName());
            bindMap.put("FACTORYNAME", productData.getFactoryName());
            bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
            bindMap.put("POSITION", productData.getPosition());
                
            mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
            
            if(mqcJobPositionList == null || mqcJobPositionList.size() <= 0)
            {
                log.warn("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProduct]");
                mqcJobPositionList = null;
            }
        }
        catch(Exception ex)
        {
            log.error("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProduct]");
            mqcJobPositionList = null;
        }
        
        return mqcJobPositionList;
    }
	
	
	
	/**
	 * 
	 * @Name     slotPositionMQCProduct
	 * @since    2019. 1. 17.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productData
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws CustomException
	 */
	public List<Map<String, Object>> slotPositionMQCProduct(Product productData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    {
	    List<Map<String, Object>> mqcJobPositionList = null;
        try
        {
            String strSql = "SELECT P.POSITION, P.RECIPENAME, M.* " +
                    "  FROM CT_MQCJOB M, CT_MQCJOBPOSITION P " +
                    " WHERE     M.MQCJOBNAME = P.MQCJOBNAME " +
                    "       AND M.MQCSTATE = :MQCSTATE " +
                    "       AND M.LOTNAME = :LOTNAME " +
                    "       AND M.CARRIERNAME = :CARRIERNAME " +
                    "       AND M.FACTORYNAME = :FACTORYNAME " +
                    "       AND M.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
                    "       AND M.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
                    "       AND P.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
                    "       AND P.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
                    "       AND P.PRODUCTNAME = :PRODUCTNAME " +
                    "       AND P.POSITION = :POSITION ";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("MQCSTATE", "Executing");
            bindMap.put("LOTNAME", productData.getLotName());
            bindMap.put("CARRIERNAME", productData.getCarrierName());
            bindMap.put("FACTORYNAME", productData.getFactoryName());
            bindMap.put("PROCESSFLOWNAME", productData.getProcessFlowName());
            bindMap.put("PROCESSFLOWVERSION", productData.getProcessFlowVersion());
            bindMap.put("PROCESSOPERATIONNAME", productData.getProcessOperationName());
            bindMap.put("PROCESSOPERATIONVERSION", productData.getProcessOperationVersion());
            bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
            bindMap.put("POSITION", productData.getPosition());
                
            mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
            
            if(mqcJobPositionList == null || mqcJobPositionList.size() <= 0)
            {
                log.warn("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProduct]");
                mqcJobPositionList = null;
            }
        }
        catch(Exception ex)
        {
            log.error("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProduct]");
            mqcJobPositionList = null;
        }
        
        return mqcJobPositionList;
    }
		
	public void checkQTime(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		if(StringUtils.isNotEmpty(lotData.getProcessOperationName()) && !StringUtils.equals(lotData.getProcessOperationName(), "-")){
	        ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
	        
	        //2019.03.15_hsryu_if Lot have Interlocked q-time, can do Sampling.  Mantis 0003082.
	        if (operationData.getProcessOperationType().equals("Inspection")) return;
		}
	        
		String strSql = "SELECT *   " +
				"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P   " +
				" WHERE     PQ.PRODUCTNAME = P.PRODUCTNAME  " +
				"       AND P.LOTNAME = :LOTNAME   " +
				"       AND PQ.QUEUETIMESTATE = :QUEUETIMESTATE  " +
		        "       AND P.PRODUCTSTATE <> :PRODUCTSTATE  " +
		        "       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))  " +
		        "       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))  " ;

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("QUEUETIMESTATE", GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);
		bindMap.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_Scrapped);
		
		bindMap.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("TOPROCESSOPERATIONNAME", lotData.getProcessOperationName());

		List<Map<String, Object>> checkQTimeData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(checkQTimeData != null && checkQTimeData.size() > 0)
		{
			throw new CustomException("QUEUE-0005", "");
		}
	}
	/**
	 * @return 
	 * ProcessFlowName == ToProcessFlowName Return false
	 * ProcessFlow.PROCESSFLOWTYPE != MAIN  And ToProcessFlow.PROCESSFLOWTYPE ==MAIN Return false
	 * ProcessFlow.PROCESSFLOWTYPE == MAIN  And ToProcessFlow.PROCESSFLOWTYPE !=MAIN Return true
	 * 
	 */
	private boolean checkProcessFlow(String productName,String toFactoryName,String toProcessFlowName,String toProcessFlowVersion) {
		try {
			ProductKey productkey = new ProductKey(productName);
			Product product = ProductServiceProxy.getProductService().selectByKey(productkey);
			LotKey lotkey = new LotKey(product.getLotName());
			Lot lotData=LotServiceProxy.getLotService().selectByKey(lotkey);
			ProcessFlowKey processFlowkey =new ProcessFlowKey(lotData.getFactoryName(),lotData.getProcessFlowName(),lotData.getProcessFlowVersion()); 
			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowkey);
			ProcessFlowKey toProcessFlowKey = new ProcessFlowKey(toFactoryName,toProcessFlowName,toProcessFlowVersion);
			ProcessFlow toProcessFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(toProcessFlowKey);
			
			if(StringUtils.equals(toProcessFlowName, lotData.getProcessFlowName())){
				return false;
			}
			// 2019.04.24_hsryu_Insert Logic. Requested by Hongwei.
			else if(StringUtils.equals(processFlow.getProcessFlowType(),"MAIN") && StringUtils.equals(toProcessFlow.getProcessFlowType(),"MAIN"))
			{
				if(!StringUtils.equals(processFlow.getKey().getProcessFlowName(), toProcessFlow.getKey().getProcessFlowName()))
				{
					return true;
				}
			}
			else if(!StringUtils.equals(processFlow.getProcessFlowType(),"MAIN") && StringUtils.equals(toProcessFlow.getProcessFlowType(),"MAIN"))
			{				
				// 2019.04.12_hsryu_Insert Logic. if processFlow is not Main and ToProcessFlow is Main, check exist toProcessFlowName in NodeStack.
				boolean mainFlowExistFlag = false;
				String nodeStack = lotData.getNodeStack();
		        String[] arrNodeStack = StringUtil.split(nodeStack, ".");
		        
		        for(int i=0; i<arrNodeStack.length; i++){
		        	Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[i]);
					String flowName = flowMap.get("PROCESSFLOWNAME");
					
					if(StringUtils.equals(flowName, toProcessFlowName)){
						mainFlowExistFlag = true;
						break;
					}
		        }
		        
		        if(mainFlowExistFlag)
		        	return false;
		        else
		        	return true;

				//return false;
			}
			else if( StringUtils.equals(processFlow.getProcessFlowType(),"MAIN") && !StringUtils.equals(toProcessFlow.getProcessFlowType(),"MAIN") )
			{
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	public void EnteredQTime(EventInfo eventInfo, EventInfo eventInfo1, String productName, List<Map<String, Object>> queuePolicyData) throws CustomException
	{
		ProductQueueTime qTimeData = null;
		boolean createFlag = false;
		eventInfo.setEventName("Entered");
		
		for(int i = 0; i < queuePolicyData.size(); i++)
		{
			// Add By Park Jeong Su For Check ProcessFlow
			if(checkProcessFlow(productName, (String)queuePolicyData.get(i).get("TOFACTORYNAME") ,(String)queuePolicyData.get(i).get("TOPROCESSFLOWNAME"),(String)queuePolicyData.get(i).get("TOPROCESSFLOWVERSION") )){
				continue;
			}
			try
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {productName, 
																												(String)queuePolicyData.get(i).get("FACTORYNAME"), 
																												(String)queuePolicyData.get(i).get("PROCESSFLOWNAME"), 
																												(String)queuePolicyData.get(i).get("PROCESSFLOWVERSION"), 
																												(String)queuePolicyData.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)queuePolicyData.get(i).get("PROCESSOPERATIONVERSION"), 
																												(String)queuePolicyData.get(i).get("TOFACTORYNAME"),
																												(String)queuePolicyData.get(i).get("TOPROCESSFLOWNAME"),
																												(String)queuePolicyData.get(i).get("TOPROCESSFLOWVERSION"),
																												(String)queuePolicyData.get(i).get("TOPROCESSOPERATIONNAME"),
																												(String)queuePolicyData.get(i).get("TOPROCESSOPERATIONVERSION"),
																												(String)queuePolicyData.get(i).get("QUEUETIMETYPE")});
			}
			catch (Exception ex)
			{
				qTimeData = null;
			}
			
			if(qTimeData != null)
			{
				if(StringUtils.equals(qTimeData.getqueueTimeState(), "Resolved"))
				{
				
				}
				// REMOVE BY JHYING ON20200414 MANTIS:6006
//				else if(!StringUtils.equals(qTimeData.getqueueTimeState(), "Exited"))
//				{
//					return;
//				}
				createFlag = false;
			}
			else
			{
				qTimeData = new ProductQueueTime(productName, 
												(String)queuePolicyData.get(i).get("FACTORYNAME"), 
												(String)queuePolicyData.get(i).get("PROCESSFLOWNAME"), 
												(String)queuePolicyData.get(i).get("PROCESSFLOWVERSION"), 
												(String)queuePolicyData.get(i).get("PROCESSOPERATIONNAME"), 
												(String)queuePolicyData.get(i).get("PROCESSOPERATIONVERSION"), 
												(String)queuePolicyData.get(i).get("TOFACTORYNAME"),
												(String)queuePolicyData.get(i).get("TOPROCESSFLOWNAME"),
												(String)queuePolicyData.get(i).get("TOPROCESSFLOWVERSION"),
												(String)queuePolicyData.get(i).get("TOPROCESSOPERATIONNAME"),
												(String)queuePolicyData.get(i).get("TOPROCESSOPERATIONVERSION"),
												(String)queuePolicyData.get(i).get("QUEUETIMETYPE"));
				createFlag = true;
			}
			
			qTimeData.settoEventName((String)queuePolicyData.get(i).get("TOEVENTNAME"));
			qTimeData.setqueueTimeType((String)queuePolicyData.get(i).get("QUEUETIMETYPE"));
			
			// Modified by smkang on 2018.11.20 - Need to validate null or non-numeric value.
//			if(StringUtils.isEmpty(queuePolicyData.get(i).get("WARNINGDURATIONLIMIT").toString()))
			if(queuePolicyData.get(i).get("WARNINGDURATIONLIMIT") == null || !StringUtils.isNumeric(queuePolicyData.get(i).get("WARNINGDURATIONLIMIT").toString()))
				qTimeData.setwarningDurationLimit(0);
			else
				qTimeData.setwarningDurationLimit(Long.valueOf(queuePolicyData.get(i).get("WARNINGDURATIONLIMIT").toString()));
			
			// Modified by smkang on 2018.11.20 - Need to validate null or non-numeric value.
//			if(StringUtils.isEmpty(queuePolicyData.get(i).get("INTERLOCKDURATIONLIMIT").toString()))
			if(queuePolicyData.get(i).get("INTERLOCKDURATIONLIMIT") == null || !StringUtils.isNumeric(queuePolicyData.get(i).get("INTERLOCKDURATIONLIMIT").toString()))
				qTimeData.setinterlockDurationLimit(0);
			else
				qTimeData.setinterlockDurationLimit(Long.valueOf(queuePolicyData.get(i).get("INTERLOCKDURATIONLIMIT").toString()));
			
			qTimeData.setgenerationType((String)queuePolicyData.get(i).get("GENERATIONTYPE"));
			qTimeData.setdepartmentName((String)queuePolicyData.get(i).get("DEPARTMENTNAME"));
			if(StringUtils.equals(qTimeData.getqueueTimeType(), "Min")){
				qTimeData.setqueueTimeState("Interlocked");
			}
			else{
				qTimeData.setqueueTimeState("Entered");
			}
			
			qTimeData.setenterTime(eventInfo.getEventTime());
			qTimeData.setexitTime(null);
			qTimeData.setwarningTime(null);
			qTimeData.setinterlockTime(null);
			qTimeData.setresolveTime(null);
			qTimeData.setresolveUser("");
			qTimeData.setlastEventUser(eventInfo.getEventUser());
			qTimeData.setlastEventTime(eventInfo.getEventTime());
			qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
			qTimeData.setlastEventName(eventInfo.getEventName());
			qTimeData.setlastEventComment(eventInfo.getEventComment());
		    
			Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName).getLotName());
			
			//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			qTimeData.setLotName(lot.getKey().getLotName());
			qTimeData.setCarrierName(lot.getCarrierName());
			
			//2019.02.25_hsryu_lot.getproductrequestname -> product.getproductrequestname. Mantis 0002757.
			qTimeData.setProductRequestName(productData.getProductRequestName());
			
			if(createFlag == true)
			{
				ExtendedObjectProxy.getProductQueueTimeService().create(eventInfo, qTimeData);
			}
			else
			{
				ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
			}
			
			if(StringUtils.equals((String)queuePolicyData.get(i).get("QUEUETIMETYPE"), "Min"))
			{
				InterlockQTime(eventInfo1, qTimeData);
			}
			
			/* 20181204, add, add, duplicate timekey ==>> */
			eventInfo.setCheckTimekeyValidation(false);
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            eventInfo1.setCheckTimekeyValidation(false);
            eventInfo1.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
            /* <<== 20181204, add, add, duplicate timekey */
		}
	}
	

	
	public void WarningQTime(EventInfo eventInfo, ProductQueueTime qTimeData) throws CustomException
	{
		eventInfo.setEventName("Warning");
		
		try
		{
			qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {qTimeData.getproductName(), 
																											qTimeData.getfactoryName(), 
																											qTimeData.getprocessFlowName(), 
																											qTimeData.getprocessFlowVersion(), 
																											qTimeData.getprocessOperationName(), 
																											qTimeData.getprocessOperationVersion(), 
																											qTimeData.gettoFactoryName(),
																											qTimeData.gettoProcessFlowName(),
																											qTimeData.gettoProcessFlowVersion(),
																											qTimeData.gettoProcessOperationName(),
																											qTimeData.gettoProcessOperationVersion(),
																											qTimeData.getqueueTimeType()});
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
		
		if(qTimeData == null)
		{
			return;
		}
		
		if(!StringUtils.equals(qTimeData.getqueueTimeState(), "Entered"))
		{
			return;
		}
		
		qTimeData.setqueueTimeState("Warning");
		qTimeData.setwarningTime(eventInfo.getEventTime());
		qTimeData.setlastEventUser(eventInfo.getEventUser());
		qTimeData.setlastEventTime(eventInfo.getEventTime());
		qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
		qTimeData.setlastEventName(eventInfo.getEventName());
		
		Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName()).getLotName());
		
		//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName());
		
		qTimeData.setLotName(lot.getKey().getLotName());
		qTimeData.setCarrierName(lot.getCarrierName());
		
		//2019.02.25_hsryu_lot.getproductrequestname -> product.getproductrequestname. Mantis 0002757.
		qTimeData.setProductRequestName(productData.getProductRequestName());
		
		ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
	}
	public void closeQTime(EventInfo eventInfo, Product productData, String toEventName) throws CustomException
	{
		ProductQueueTime qTimeData = null;
		eventInfo.setEventName("CloseQueueTime");
		
		try
		{
			String strSql = "SELECT PQ.PRODUCTNAME,     " +
					"       PQ.FACTORYNAME,     " +
					"       PQ.PROCESSFLOWNAME,     " +
					"       PQ.PROCESSFLOWVERSION,     " +
					"       PQ.PROCESSOPERATIONNAME,     " +
					"       PQ.PROCESSOPERATIONVERSION,   " +
					"       PQ.TOFACTORYNAME,   " +
					"       PQ.TOPROCESSFLOWNAME,   " +
					"       PQ.TOPROCESSFLOWVERSION,   " +
					"       PQ.TOPROCESSOPERATIONNAME,   " +
					"       PQ.TOPROCESSOPERATIONVERSION, " +
					"       PQ.QUEUETIMETYPE, " +
					"       PQ.TOEVENTNAME   " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P    " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME     " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))  " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))  " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME    " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME " +
					"       AND PQ.QUEUETIMESTATE='Exited' " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TOFACTORYNAME", productData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", productData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", productData.getProcessOperationName());
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																											(String)qTimeList.get(i).get("FACTORYNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																											(String)qTimeList.get(i).get("TOFACTORYNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																											(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData == null)
				{
					continue;
				}
				ExtendedObjectProxy.getProductQueueTimeService().remove(eventInfo, qTimeData);
			}
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
		
	}
	
	public void closeAllQTime(EventInfo eventInfo, Product productData) throws CustomException
	{
		ProductQueueTime qTimeData = null;
		eventInfo.setEventName("CloseQueueTime");
		
		try
		{
			String strSql = "SELECT PQ.PRODUCTNAME,     " +
					"       PQ.FACTORYNAME,     " +
					"       PQ.PROCESSFLOWNAME,     " +
					"       PQ.PROCESSFLOWVERSION,     " +
					"       PQ.PROCESSOPERATIONNAME,     " +
					"       PQ.PROCESSOPERATIONVERSION,   " +
					"       PQ.TOFACTORYNAME,   " +
					"       PQ.TOPROCESSFLOWNAME,   " +
					"       PQ.TOPROCESSFLOWVERSION,   " +
					"       PQ.TOPROCESSOPERATIONNAME,   " +
					"       PQ.TOPROCESSOPERATIONVERSION, " +
					"       PQ.QUEUETIMETYPE, " +
					"       PQ.TOEVENTNAME   " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P    " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME     " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME    " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME " ;

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TOFACTORYNAME", productData.getFactoryName());
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																											(String)qTimeList.get(i).get("FACTORYNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																											(String)qTimeList.get(i).get("TOFACTORYNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																											(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData == null)
				{
					continue;
				}
				ExtendedObjectProxy.getProductQueueTimeService().remove(eventInfo, qTimeData);
			}
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
		
	}
	
	public void ExitedQTime(EventInfo eventInfo, Product productData, String toEventName) throws CustomException
	{
		ProductQueueTime qTimeData = null;
		eventInfo.setEventName("Exited");
		
		try
		{
			String strSql = "SELECT PQ.PRODUCTNAME,     " +
					"       PQ.FACTORYNAME,     " +
					"       PQ.PROCESSFLOWNAME,     " +
					"       PQ.PROCESSFLOWVERSION,     " +
					"       PQ.PROCESSOPERATIONNAME,     " +
					"       PQ.PROCESSOPERATIONVERSION,   " +
					"       PQ.TOFACTORYNAME,   " +
					"       PQ.TOPROCESSFLOWNAME,   " +
					"       PQ.TOPROCESSFLOWVERSION,   " +
					"       PQ.TOPROCESSOPERATIONNAME,   " +
					"       PQ.TOPROCESSOPERATIONVERSION, " +
					"       PQ.QUEUETIMETYPE, " +
					"       PQ.TOEVENTNAME   " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P    " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME     " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))  " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))  " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME    " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TOFACTORYNAME", productData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", productData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", productData.getProcessOperationName());
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																											(String)qTimeList.get(i).get("FACTORYNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																											(String)qTimeList.get(i).get("TOFACTORYNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																											(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData == null)
				{
					return;
				}
				
				qTimeData.setqueueTimeState("Exited");
				qTimeData.setexitTime(eventInfo.getEventTime());
				qTimeData.setlastEventUser(eventInfo.getEventUser());
				qTimeData.setlastEventTime(eventInfo.getEventTime());
				qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
				qTimeData.setlastEventName(eventInfo.getEventName());
				
				Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
				 
				qTimeData.setLotName(lot.getKey().getLotName());
				qTimeData.setCarrierName(lot.getCarrierName());
				
				//2019.02.25_hsryu_lot.getproductrequestname -> productData.getproductrequestname. Mantis 0002757.
				qTimeData.setProductRequestName(productData.getProductRequestName());
				
				ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
			}
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
	}
	
	public void ExitedCancelQTime(EventInfo eventInfo, Product productData, String toEventName) throws CustomException
	{
		ProductQueueTime qTimeData = null;
		eventInfo.setEventName("ExitedCancel");
		
		try
		{
			String strSql = "SELECT PQ.PRODUCTNAME,      " +
					"       PQ.FACTORYNAME,      " +
					"       PQ.PROCESSFLOWNAME,      " +
					"       PQ.PROCESSFLOWVERSION,      " +
					"       PQ.PROCESSOPERATIONNAME,      " +
					"       PQ.PROCESSOPERATIONVERSION,    " +
					"       PQ.TOFACTORYNAME,    " +
					"       PQ.TOPROCESSFLOWNAME,    " +
					"       PQ.TOPROCESSFLOWVERSION,    " +
					"       PQ.TOPROCESSOPERATIONNAME,    " +
					"       PQ.TOPROCESSOPERATIONVERSION,    " +
					"       PQ.QUEUETIMETYPE    " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P     " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME      " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))         " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))       " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME     " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME     " +
					"       AND PQ.QUEUETIMESTATE = :QUEUETIMESTATE " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME   ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TOFACTORYNAME", productData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", productData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", productData.getProcessOperationName());
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			bindMap.put("QUEUETIMESTATE", "Exited");
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																											(String)qTimeList.get(i).get("FACTORYNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																											(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																											(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																											(String)qTimeList.get(i).get("TOFACTORYNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																											(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																											(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData == null)
				{
					return;
				}
				
				qTimeData.setqueueTimeState("Entered");				
				qTimeData.setexitTime(null);
				qTimeData.setwarningTime(null);
				qTimeData.setinterlockTime(null);
				qTimeData.setresolveTime(null);
				qTimeData.setresolveUser("");
				qTimeData.setlastEventUser(eventInfo.getEventUser());
				qTimeData.setlastEventTime(eventInfo.getEventTime());
				qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
				qTimeData.setlastEventName(eventInfo.getEventName());
				
				Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
				 
				qTimeData.setLotName(lot.getKey().getLotName());
				qTimeData.setCarrierName(lot.getCarrierName());
				
				//2019.02.25_hsryu_lot.getproductrequestname -> productData.getproductrequestname. Mantis 0002757.
				qTimeData.setProductRequestName(productData.getProductRequestName());
				
				// add by jhying on20200410 mantis:6006
				//ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
				
                ProductQueueTime queuePolicyData = ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);

				if(StringUtils.equals((String)queuePolicyData.getqueueTimeType(), "Min"))
				{
					
					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					InterlockQTime(eventInfo, qTimeData);
				}
				// add by jhying on20200410 mantis:6006		
			}
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
	}
	
	public void InterlockQTime(EventInfo eventInfo, ProductQueueTime qTimeData) throws CustomException
	{
		eventInfo.setEventName("Interlocked");
		
		try
		{
			qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {qTimeData.getproductName(), 
																											qTimeData.getfactoryName(), 
																											qTimeData.getprocessFlowName(), 
																											qTimeData.getprocessFlowVersion(), 
																											qTimeData.getprocessOperationName(), 
																											qTimeData.getprocessOperationVersion(), 
																											qTimeData.gettoFactoryName(),
																											qTimeData.gettoProcessFlowName(),
																											qTimeData.gettoProcessFlowVersion(),
																											qTimeData.gettoProcessOperationName(),
																											qTimeData.gettoProcessOperationVersion(),
																											qTimeData.getqueueTimeType()});
		}
		catch (Exception ex)
		{
			qTimeData = null;
		}
		
		if(qTimeData == null)
		{
			return;
		}
		
		if(!StringUtils.equals(qTimeData.getqueueTimeType(), "Min"))
		{
//			long warnDurationTime  = qTimeData.getwarningDurationLimit() != 0 ? qTimeData.getwarningDurationLimit() : 0 ; //add by jhying on202200402 mantis:5910
//			long interlockedTime = qTimeData.getinterlockDurationLimit() != 0 ? qTimeData.getinterlockDurationLimit() :0 ; //add by jhying on202200402 mantis:5910
		  if(qTimeData.getwarningDurationLimit() <= qTimeData.getinterlockDurationLimit() )
		  { //add by jhying on202200402 mantis:5910
			if(!StringUtils.equals(qTimeData.getqueueTimeState(), "Warning"))
			{
				return;
			}
		 }
		}
		
		qTimeData.setqueueTimeState("Interlocked");
		qTimeData.setinterlockTime(eventInfo.getEventTime());
		qTimeData.setlastEventUser(eventInfo.getEventUser());
		qTimeData.setlastEventTime(eventInfo.getEventTime());
		qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
		qTimeData.setlastEventName(eventInfo.getEventName());
		
		Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName()).getLotName());
		
		//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName());
	
		qTimeData.setLotName(lot.getKey().getLotName());
		qTimeData.setCarrierName(lot.getCarrierName());
		
		//2019.02.25_hsryu_lot.getproductrequestname -> productData.getproductrequestname. Mantis 0002757.
		qTimeData.setProductRequestName(productData.getProductRequestName());
		
		ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
	}
	
	public void ResolvedQTime(EventInfo eventInfo, ProductQueueTime qTimeData) throws CustomException
	{
		eventInfo.setEventName("Resolved");
		
		qTimeData.setqueueTimeState("Resolved");
		qTimeData.setresolveTime(eventInfo.getEventTime());
		qTimeData.setresolveUser(eventInfo.getEventUser());
		qTimeData.setlastEventUser(eventInfo.getEventUser());
		qTimeData.setlastEventTime(eventInfo.getEventTime());
		qTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
		qTimeData.setlastEventName(eventInfo.getEventName());
		
		Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName()).getLotName());
		
		//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(qTimeData.getproductName());

		qTimeData.setLotName(lot.getKey().getLotName());
		qTimeData.setCarrierName(lot.getCarrierName());
		
		//2019.02.25_hsryu_lot.getproductrequestname -> productData.getproductrequestname. Mantis 0002757.
		qTimeData.setProductRequestName(productData.getProductRequestName());
		
		ExtendedObjectProxy.getProductQueueTimeService().modify(eventInfo, qTimeData);
	}
	
//	/**
//	 * @Name     setProcessedOperationBeforeAOI
//	 * @since    2018. 9. 11.
//	 * @author   hhlee
//	 * @contents 
//	 *           
//	 * @param productEle
//	 * @param machineName
//	 * @throws CustomException
//	 */
//	public void setProcessedOperationBeforeAOI(Element productEle, String machineName) throws CustomException
//	{
//		try
//		{
//			String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
//			
//			Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//			String operation = aProduct.getProcessOperationName();
//			
//			ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(aProduct.getFactoryName(), operation);
//			String operationType = processOperationSpecData.getProcessOperationType();
//						
//			if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
//			{
//				String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5 "
//						+ " FROM CT_PROCESSEDOPERATION "
//						+ " WHERE PRODUCTNAME = :PRODUCTNAME ";
//				
//				Map<String, Object> checkMap = new HashMap<String, Object>();
//				checkMap.put("PRODUCTNAME", productName);
//
//				List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
//				if ( checkResult.size() > 0 )
//				{
//					String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
//					String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
//					String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
//					String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
//					String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
//					
//					String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
//					String condition = "";
//					
//					Map<String, Object> updateMap = new HashMap<String, Object>();
//					updateMap.put("PRODUCTNAME", productName);
//					
//					if ( StringUtil.isEmpty(attribute2) )
//					{
//						condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
//						updateMap.put("ATTRIBUTE2", machineName+","+operation);
//						
//						updateSql = updateSql + condition; 
//					}
//					else if ( StringUtil.isEmpty(attribute3) )
//					{
//						condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
//						updateMap.put("ATTRIBUTE3", machineName+","+operation);
//						
//						updateSql = updateSql + condition; 
//					}
//					else if ( StringUtil.isEmpty(attribute4) )
//					{
//						condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
//						updateMap.put("ATTRIBUTE4", machineName+","+operation);
//						
//						updateSql = updateSql + condition; 
//					}
//					else if ( StringUtil.isEmpty(attribute5) )
//					{
//						condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
//						updateMap.put("ATTRIBUTE5", machineName+","+operation);
//						
//						updateSql = updateSql + condition; 
//					}
//					else
//					{
//						// All ATTRIBUTE Column is not empty
//						condition = " ATTRIBUTE1 = :ATTRIBUTE1, ATTRIBUTE2 = :ATTRIBUTE2, ATTRIBUTE3 = :ATTRIBUTE3, ATTRIBUTE4 = :ATTRIBUTE4, ATTRIBUTE5 = :ATTRIBUTE5 ";
//						updateMap.put("ATTRIBUTE1", attribute2);
//						updateMap.put("ATTRIBUTE2", attribute3);
//						updateMap.put("ATTRIBUTE3", attribute4);
//						updateMap.put("ATTRIBUTE4", attribute5);
//						updateMap.put("ATTRIBUTE5", machineName+","+operation);
//						
//						updateSql = updateSql + condition; 
//					}
//					
//					String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
//					updateSql = updateSql + where;
//					
//					GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
//				}
//				else
//				{
//					String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
//							+ " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5) "
//							+ " VALUES "
//							+ " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5) ";
//					
//					Map<String, Object> insMap = new HashMap<String, Object>();
//					insMap.put("PRODUCTNAME", productName);
//					insMap.put("ATTRIBUTE1", machineName+","+operation);
//					
//					insMap.put("ATTRIBUTE2", "");
//					insMap.put("ATTRIBUTE3", "");
//					insMap.put("ATTRIBUTE4", "");
//					insMap.put("ATTRIBUTE5", "");
//
//					GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
//				}
//			}
//		}
//		catch (Throwable e)
//		{
//			log.error("Update Fail! [CT_PROCESSEDOPERATION]");
//		}
//	}
	
//	/**
//     * @Name     setProcessedOperationBeforeAOI
//     * @since    2018. 9. 11.
//     * @author   hhlee
//     * @contents 
//     *           
//     * @param productEle
//     * @param machineName
//     * @throws CustomException
//     */
//    public void setProcessedOperationBeforeAOI(String productName, String machineName, String factoryName, String operation, String virtualGlassName) throws CustomException
//    {
//        try
//        {
//            Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//            //String operation = aProduct.getProcessOperationName();
//                        
//            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(factoryName, operation);
//            String operationType = processOperationSpecData.getProcessOperationType();
//                        
//            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
//            {
//                String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5 "
//                        + " FROM CT_PROCESSEDOPERATION "
//                        + " WHERE PRODUCTNAME = :PRODUCTNAME ";
//                
//                Map<String, Object> checkMap = new HashMap<String, Object>();
//                checkMap.put("PRODUCTNAME", productName);
//
//                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
//                if ( checkResult.size() > 0 )
//                {
//                    String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
//                    String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
//                    String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
//                    String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
//                    String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
//                    
//                    String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
//                    String condition = "";
//                    
//                    Map<String, Object> updateMap = new HashMap<String, Object>();
//                    updateMap.put("PRODUCTNAME", productName);
//                    
//                    if ( StringUtil.isEmpty(attribute2) )
//                    {
//                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
//                        updateMap.put("ATTRIBUTE2", machineName+","+operation);
//                        
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute3) )
//                    {
//                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
//                        updateMap.put("ATTRIBUTE3", machineName+","+operation);
//                        
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute4) )
//                    {
//                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
//                        updateMap.put("ATTRIBUTE4", machineName+","+operation);
//                        
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute5) )
//                    {
//                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
//                        updateMap.put("ATTRIBUTE5", machineName+","+operation);
//                        
//                        updateSql = updateSql + condition; 
//                    }
//                    else
//                    {
//                        // All ATTRIBUTE Column is not empty
//                        condition = " ATTRIBUTE1 = :ATTRIBUTE1, ATTRIBUTE2 = :ATTRIBUTE2, ATTRIBUTE3 = :ATTRIBUTE3, ATTRIBUTE4 = :ATTRIBUTE4, ATTRIBUTE5 = :ATTRIBUTE5 ";
//                        updateMap.put("ATTRIBUTE1", attribute2);
//                        updateMap.put("ATTRIBUTE2", attribute3);
//                        updateMap.put("ATTRIBUTE3", attribute4);
//                        updateMap.put("ATTRIBUTE4", attribute5);
//                        updateMap.put("ATTRIBUTE5", machineName+","+operation);
//                        
//                        updateSql = updateSql + condition; 
//                    }
//                    
//                    String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
//                    updateSql = updateSql + where;
//                    
//                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
//                }
//                else
//                {
//                    String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
//                            + " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5) "
//                            + " VALUES "
//                            + " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5) ";
//                    
//                    Map<String, Object> insMap = new HashMap<String, Object>();
//                    insMap.put("PRODUCTNAME", productName);
//                    insMap.put("ATTRIBUTE1", machineName+","+operation);
//                    
//                    insMap.put("ATTRIBUTE2", "");
//                    insMap.put("ATTRIBUTE3", "");
//                    insMap.put("ATTRIBUTE4", "");
//                    insMap.put("ATTRIBUTE5", "");
//
//                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
//                }
//            }
//        }
//        catch (Throwable e)
//        {
//            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
//        }
//    }
    
    /**
     * @Name     setProcessedOperationBeforeAOI
     * @since    2018. 9. 11.
     * @author   hhlee
     * @contents Processed Operation Before AOI
     *           
     * @param eventInfo
     * @param productEle
     * @param machineName
     * @throws CustomException
     */
    public void setProcessedOperationBeforeAOI(EventInfo eventInfo, String productName, 
            String machineName, Lot lotData, String machineRecipeName, String virtualGlassName) throws CustomException
    {
        try
        {
            //String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
            
            //Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
            String operation = lotData.getProcessOperationName();
            
            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), operation);
            String operationType = processOperationSpecData.getProcessOperationType();
            
            String factoryName = lotData.getFactoryName();
            String productSpecName = lotData.getProductSpecName();
            String processFlowName = lotData.getProcessFlowName();
            //String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", true);
            String lotName = lotData.getKey().getLotName();
            String eventTimeKey = eventInfo.getEventTimeKey();
            
            String unitNameList = StringUtil.EMPTY;
            String unitRecipeNameList = StringUtil.EMPTY;
            
            try
            {
                VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {virtualGlassName});
                /* 20180925, hhlee, Add Get Unit List ==>> */
                unitNameList = vGlassData.getUnitNameList();
                /* <<== 20180925, hhlee, Add Ge Unit List */
            }
            catch (Throwable e)
            {
                log.error("Selete VirtualGlassName Fail! [CT_PROCESSEDOPERATION]");
            }
                
            /* 20180925, hhlee, Add Get Unit Recipe List ==>> */
            unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName);
            /* <<== 20180925, hhlee, Add Ge Unit Recipe List */
            
            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
            {
                String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5 "
                        + " FROM CT_PROCESSEDOPERATION "
                        + " WHERE PRODUCTNAME = :PRODUCTNAME ";
                
                Map<String, Object> checkMap = new HashMap<String, Object>();
                checkMap.put("PRODUCTNAME", productName);

                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
                if ( checkResult.size() > 0 )
                {
                    /* 20190123, hhlee, modify, change Type casting ==>> */
                    //String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
                    //String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
                    //String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
                    //String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
                    //String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
                    /* 20190124, hhlee, modify, change Type casting ==>> */
                    String attribute1 = checkResult.get(0).get("ATTRIBUTE1") != null ? checkResult.get(0).get("ATTRIBUTE1").toString() : StringUtil.EMPTY;
                    String attribute2 = checkResult.get(0).get("ATTRIBUTE2") != null ? checkResult.get(0).get("ATTRIBUTE2").toString() : StringUtil.EMPTY;
                    String attribute3 = checkResult.get(0).get("ATTRIBUTE3") != null ? checkResult.get(0).get("ATTRIBUTE3").toString() : StringUtil.EMPTY;
                    String attribute4 = checkResult.get(0).get("ATTRIBUTE4") != null ? checkResult.get(0).get("ATTRIBUTE4").toString() : StringUtil.EMPTY;
                    String attribute5 = checkResult.get(0).get("ATTRIBUTE5") != null ? checkResult.get(0).get("ATTRIBUTE5").toString() : StringUtil.EMPTY;
                    /* <<== 20190124, hhlee, modify, change Type casting */
                    /* <<== 20190123, hhlee, modify, change Type casting */
                                        
                    String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
                    String condition = "";
                    
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("PRODUCTNAME", productName);
                    
                    if ( StringUtil.isEmpty(attribute2) )
                    {
                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
                        updateMap.put("ATTRIBUTE2", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute3) )
                    {
                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
                        updateMap.put("ATTRIBUTE3", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute4) )
                    {
                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
                        updateMap.put("ATTRIBUTE4", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute5) )
                    {
                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
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
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
                        updateSql = updateSql + condition; 
                    }
                    
                    updateSql = updateSql + ", UNITNAMELIST = :UNITNAMELIST, UNITRECIPENAMELIST = :UNITRECIPENAMELIST"; 
                    updateMap.put("UNITNAMELIST", StringUtil.EMPTY);
                    updateMap.put("UNITRECIPENAMELIST", StringUtil.EMPTY);
                    
                    String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
                    updateSql = updateSql + where;
                    
                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
                }
                else
                {
                    String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
                            + " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, UNITNAMELIST, UNITRECIPENAMELIST) "
                            + " VALUES "
                            + " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5, :UNITNAMELIST, :UNITRECIPENAMELIST) ";
                    
                    Map<String, Object> insMap = new HashMap<String, Object>();
                    insMap.put("PRODUCTNAME", productName);
                    insMap.put("ATTRIBUTE1", machineName + "," + operation + "," + factoryName + "," + 
                                             productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                             lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
                    insMap.put("ATTRIBUTE2", "");
                    insMap.put("ATTRIBUTE3", "");
                    insMap.put("ATTRIBUTE4", "");
                    insMap.put("ATTRIBUTE5", "");
                    insMap.put("UNITNAMELIST", "");
                    insMap.put("UNITRECIPENAMELIST", "");

                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
        }
    }
    
    /**
     * @Name     setProcessedOperationBeforeAOI
     * @since    2018. 9. 11.
     * @author   hhlee
     * @contents Processed Operation Before AOI
     *           
     * @param eventInfo
     * @param productEle
     * @param machineName
     * @throws CustomException
     */
    public void setProcessedOperationBeforeAOI(EventInfo eventInfo, Element productEle, String productName, 
            String machineName, Lot lotData, String machineRecipeName, String virtualGlassName) throws CustomException
    {
        try
        {
            //String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
            
            //Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
            String operation = lotData.getProcessOperationName();
            
            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), operation);
            String operationType = processOperationSpecData.getProcessOperationType();
            
            String factoryName = lotData.getFactoryName();
            String productSpecName = lotData.getProductSpecName();
            String processFlowName = lotData.getProcessFlowName();
            //String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", true);
            String lotName = lotData.getKey().getLotName();
            String eventTimeKey = eventInfo.getEventTimeKey();
            
            String unitNameList = StringUtil.EMPTY;
            String unitRecipeNameList = StringUtil.EMPTY;
            
            try
            {
                VirtualGlass vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {virtualGlassName});
                /* 20180925, hhlee, Add Get Unit List ==>> */
                unitNameList = vGlassData.getUnitNameList();
                /* <<== 20180925, hhlee, Add Ge Unit List */
            }
            catch (Throwable e)
            {
                log.error("Selete VirtualGlassName Fail! [CT_PROCESSEDOPERATION]");
            }
                
            /* 20180925, hhlee, Add Get Unit Recipe List ==>> */
            unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName);
            /* <<== 20180925, hhlee, Add Ge Unit Recipe List */
            
            /* 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) ==>> */
            String subUnitNameList = MESProductServiceProxy.getProductServiceUtil().getSubUnitNameListByProductElement(productEle);
            /* <<== 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) */
            
            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
            {
                String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5 "
                        + " FROM CT_PROCESSEDOPERATION "
                        + " WHERE PRODUCTNAME = :PRODUCTNAME ";
                
                Map<String, Object> checkMap = new HashMap<String, Object>();
                checkMap.put("PRODUCTNAME", productName);

                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
                if ( checkResult.size() > 0 )
                {
                    /* 20190123, hhlee, modify, change Type casting ==>> */
                    //String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
                    //String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
                    //String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
                    //String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
                    //String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
                    /* 20190124, hhlee, modify, change Type casting ==>> */
                    String attribute1 = checkResult.get(0).get("ATTRIBUTE1") != null ? checkResult.get(0).get("ATTRIBUTE1").toString() : StringUtil.EMPTY;
                    String attribute2 = checkResult.get(0).get("ATTRIBUTE2") != null ? checkResult.get(0).get("ATTRIBUTE2").toString() : StringUtil.EMPTY;
                    String attribute3 = checkResult.get(0).get("ATTRIBUTE3") != null ? checkResult.get(0).get("ATTRIBUTE3").toString() : StringUtil.EMPTY;
                    String attribute4 = checkResult.get(0).get("ATTRIBUTE4") != null ? checkResult.get(0).get("ATTRIBUTE4").toString() : StringUtil.EMPTY;
                    String attribute5 = checkResult.get(0).get("ATTRIBUTE5") != null ? checkResult.get(0).get("ATTRIBUTE5").toString() : StringUtil.EMPTY;
                    /* <<== 20190124, hhlee, modify, change Type casting */
                    /* <<== 20190123, hhlee, modify, change Type casting */
                                        
                    String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
                    String condition = "";
                    
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("PRODUCTNAME", productName);
                    
                    if ( StringUtil.isEmpty(attribute2) )
                    {
                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
                        updateMap.put("ATTRIBUTE2", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute3) )
                    {
                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
                        updateMap.put("ATTRIBUTE3", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute4) )
                    {
                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
                        updateMap.put("ATTRIBUTE4", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute5) )
                    {
                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
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
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    
                    updateSql = updateSql + ", UNITNAMELIST = :UNITNAMELIST, UNITRECIPENAMELIST = :UNITRECIPENAMELIST"; 
                    
                    /* hhlee, 20190222, delete, add LotProcessEnd Logic ==>> */
                    updateMap.put("UNITNAMELIST", StringUtil.EMPTY);
                    updateMap.put("UNITRECIPENAMELIST", StringUtil.EMPTY);
                    /* <<== hhlee, 20190222, delete, add LotProcessEnd Logic */
                    
                    String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
                    updateSql = updateSql + where;
                    
                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
                }
                else
                {
                    String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
                            + " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, UNITNAMELIST, UNITRECIPENAMELIST) "
                            + " VALUES "
                            + " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5, :UNITNAMELIST, :UNITRECIPENAMELIST) ";
                    
                    Map<String, Object> insMap = new HashMap<String, Object>();
                    insMap.put("PRODUCTNAME", productName);
                    insMap.put("ATTRIBUTE1", machineName + "," + operation + "," + factoryName + "," + 
                                             productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                             lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                    insMap.put("ATTRIBUTE2", "");
                    insMap.put("ATTRIBUTE3", "");
                    insMap.put("ATTRIBUTE4", "");
                    insMap.put("ATTRIBUTE5", "");
                    
                    /* hhlee, 20190222, delete, add LotProcessEnd Logic ==>> */
                    insMap.put("UNITNAMELIST", "");
                    insMap.put("UNITRECIPENAMELIST", "");
                    /* <<== hhlee, 20190222, delete, add LotProcessEnd Logic */

                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
        }
    }
//    /**
//	 * @Name     setProcessedOperationBeforeAOI
//	 * @since    2018. 10. 23.
//	 * @author   jspark
//	 * @contents Processed Operation Before AOI
//	 *           
//	 * @param eventInfo
//	 * @param productEle
//	 * @param machineName
//	 * @throws CustomException
//	 */
//	public void setProcessedOperation(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
//    {
//        try
//        {
//            String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
//            
//            Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
//            String operation = aProduct.getProcessOperationName();
//            
//            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(aProduct.getFactoryName(), operation);
//            String operationType = processOperationSpecData.getProcessOperationType();
//            
//            String factoryName = aProduct.getFactoryName();
//            String productSpecName = aProduct.getProductSpecName();
//            String processFlowName = aProduct.getProcessFlowName();
//            String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", true);
//            String lotName = aProduct.getLotName();
//            String eventTimeKey = eventInfo.getEventTimeKey();
//            
//            String unitNameList = StringUtil.EMPTY;
//            String unitRecipeNameList = StringUtil.EMPTY;
//            
//            /* 20180925, hhlee, Add Get Unit Recipe List ==>> */
//            //unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName);
//            /* <<== 20180925, hhlee, Add Ge Unit Recipe List */
//            
//            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
//            {
//                String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,UNITNAMELIST,UNITRECIPENAMELIST "
//                        + " FROM CT_PROCESSEDOPERATION "
//                        + " WHERE PRODUCTNAME = :PRODUCTNAME ";
//                
//                Map<String, Object> checkMap = new HashMap<String, Object>();
//                checkMap.put("PRODUCTNAME", productName);
//
//                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
//                if ( checkResult.size() > 0 )
//                {
//                    String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
//                    String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
//                    String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
//                    String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
//                    String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
//                    
//                    //unitNameList = (String)checkResult.get(0).get("UNITNAMELIST");
//                    //unitRecipeNameList = (String)checkResult.get(0).get("UNITRECIPENAMELIST");
//                    
//                    String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
//                    String condition = "";
//                    
//                    Map<String, Object> updateMap = new HashMap<String, Object>();
//                    updateMap.put("PRODUCTNAME", productName);
//                    
//                    if ( StringUtil.isEmpty(attribute2) )
//                    {
//                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
//                        updateMap.put("ATTRIBUTE2", machineName + "," + operation + "," + factoryName + "," + 
//                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute3) )
//                    {
//                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
//                        updateMap.put("ATTRIBUTE3", machineName + "," + operation + "," + factoryName + "," + 
//                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute4) )
//                    {
//                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
//                        updateMap.put("ATTRIBUTE4", machineName + "," + operation + "," + factoryName + "," + 
//                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                        updateSql = updateSql + condition; 
//                    }
//                    else if ( StringUtil.isEmpty(attribute5) )
//                    {
//                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
//                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
//                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                        updateSql = updateSql + condition; 
//                    }
//                    else
//                    {
//                        // All ATTRIBUTE Column is not empty
//                        condition = " ATTRIBUTE1 = :ATTRIBUTE1, ATTRIBUTE2 = :ATTRIBUTE2, ATTRIBUTE3 = :ATTRIBUTE3, ATTRIBUTE4 = :ATTRIBUTE4, ATTRIBUTE5 = :ATTRIBUTE5 ";
//                        updateMap.put("ATTRIBUTE1", attribute2);
//                        updateMap.put("ATTRIBUTE2", attribute3);
//                        updateMap.put("ATTRIBUTE3", attribute4);
//                        updateMap.put("ATTRIBUTE4", attribute5);
//                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
//                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                        updateSql = updateSql + condition; 
//                    }
//                    
//                    updateSql = updateSql + ", UNITNAMELIST = :UNITNAMELIST, UNITRECIPENAMELIST = :UNITRECIPENAMELIST"; 
//                    updateMap.put("UNITNAMELIST", StringUtil.EMPTY);
//                    updateMap.put("UNITRECIPENAMELIST", StringUtil.EMPTY);
//                    
//                    String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
//                    updateSql = updateSql + where;
//                    
//                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
//                }
//                else
//                {
//                    String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
//                            + " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, UNITNAMELIST, UNITRECIPENAMELIST) "
//                            + " VALUES "
//                            + " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5, :UNITNAMELIST, :UNITRECIPENAMELIST) ";
//                    
//                    Map<String, Object> insMap = new HashMap<String, Object>();
//                    insMap.put("PRODUCTNAME", productName);
//                    insMap.put("ATTRIBUTE1", machineName + "," + operation + "," + factoryName + "," + 
//                                             productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
//                                             lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList);
//                    insMap.put("ATTRIBUTE2", "");
//                    insMap.put("ATTRIBUTE3", "");
//                    insMap.put("ATTRIBUTE4", "");
//                    insMap.put("ATTRIBUTE5", "");
//                    insMap.put("UNITNAMELIST", "");
//                    insMap.put("UNITRECIPENAMELIST", "");
//
//                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
//                }
//            }
//        }
//        catch (Throwable e)
//        {
//            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
//        }
//    }
    
	/**
	 * @Name     setProcessedOperationBeforeAOI
	 * @since    2018. 9. 11.
	 * @author   hhlee
	 * @contents Processed Operation Before AOI
	 *           
	 * @param eventInfo
	 * @param productEle
	 * @param machineName
	 * @throws CustomException
	 */
	public void setProcessedOperationBeforeAOI(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
    {
        try
        {
            String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
            
            Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);
            /* 20181217, hhlee, delete , CT_PROCESSEDOPERATION ==>> */
            //String operation = aProduct.getProcessOperationName();
            //
            //ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(aProduct.getFactoryName(), operation);
            //String operationType = processOperationSpecData.getProcessOperationType();
            //
            //String factoryName = aProduct.getFactoryName();
            //String productSpecName = aProduct.getProductSpecName();
            //String processFlowName = aProduct.getProcessFlowName();
            //String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", false);
            //String lotName = aProduct.getLotName();
            /* <<== 20181217, hhlee, delete , CT_PROCESSEDOPERATION */
            
            /* 20181217, hhlee, modify , CT_PROCESSEDOPERATION ==>> */
            Lot lotdata = MESLotServiceProxy.getLotInfoUtil().getLotData(aProduct.getLotName());
            
            String operation = lotdata.getProcessOperationName();
            
            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotdata.getFactoryName(), operation);
            String operationType = processOperationSpecData.getProcessOperationType();
            
            String factoryName = lotdata.getFactoryName();
            String productSpecName = lotdata.getProductSpecName();
            String processFlowName = lotdata.getProcessFlowName();
            String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", false);
            String lotName = lotdata.getKey().getLotName();
            
            String ecCode = CommonUtil.getValue(lotdata.getUdfs(), "ECCODE");
            /* <<== 20181217, hhlee, modify , CT_PROCESSEDOPERATION */
            
            String eventTimeKey = eventInfo.getEventTimeKey();
            
            String unitNameList = StringUtil.EMPTY;
            String unitRecipeNameList = StringUtil.EMPTY;
            
            /* 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) ==>> */
            String subUnitNameList = MESProductServiceProxy.getProductServiceUtil().getSubUnitNameListByProductElement(productEle);
            /* <<== 20190219, hhlee, add, subunitnamelist(UNITNAME=SUBUNITNAME|UNITNAME=SUBUNITNAME|.....) */
            
            /* 20181210, hhlee, modify, Get Unit Recipe List ==>> */
            MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);  
            //if(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK") ||
            //        CommonUtil.getValue(machineSpecData.getUdfs(), "RMSUNITRECIPEONLY").equals("Y"))
            if(StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), GenericServiceProxy.getConstantMap().ConstructType_PHOTO) ||
                    StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "RMSUNITRECIPEONLY")  , GenericServiceProxy.getConstantMap().Flag_Y))
            {
                /* 20181217, hhlee, modify , CT_PROCESSEDOPERATION ==>> */
                //Lot lotdata = MESLotServiceProxy.getLotInfoUtil().getLotData(aProduct.getLotName());
                //ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, operation, machineName, 
                //        CommonUtil.getValue(lotdata.getUdfs(), "ECCODE"));
                ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, operation, machineName, ecCode);
                unitRecipeNameList = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));
                log.info("[01.UNIT RECIPE LIST]" + unitRecipeNameList);
                unitRecipeNameList = StringUtil.replace(unitRecipeNameList, "_", "|"); 
                /* <<== 20181217, hhlee, modify , CT_PROCESSEDOPERATION */
            }
            else
            {
                /* 20180925, hhlee, Add Get Unit Recipe List ==>> */
                unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName);
                /* <<== 20180925, hhlee, Add Ge Unit Recipe List */
            }
            /* <<== 20181210, hhlee, modify, Get Unit Recipe List */
            
            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
            {
                String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,UNITNAMELIST,UNITRECIPENAMELIST "
                        + " FROM CT_PROCESSEDOPERATION "
                        + " WHERE PRODUCTNAME = :PRODUCTNAME ";
                
                Map<String, Object> checkMap = new HashMap<String, Object>();
                checkMap.put("PRODUCTNAME", productName);

                List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
                if ( checkResult!=null && checkResult.size() > 0 )
                {
                    /* 20190123, hhlee, modify, change Type casting ==>> */
                    //String attribute1 = (String)checkResult.get(0).get("ATTRIBUTE1");
                    //String attribute2 = (String)checkResult.get(0).get("ATTRIBUTE2");
                    //String attribute3 = (String)checkResult.get(0).get("ATTRIBUTE3");
                    //String attribute4 = (String)checkResult.get(0).get("ATTRIBUTE4");
                    //String attribute5 = (String)checkResult.get(0).get("ATTRIBUTE5");
                    //unitNameList = (String)checkResult.get(0).get("UNITNAMELIST");
                    ////unitRecipeNameList = (String)checkResult.get(0).get("UNITRECIPENAMELIST");
                    
                    /* 20190124, hhlee, modify, change Type casting ==>> */
                    String attribute1 = checkResult.get(0).get("ATTRIBUTE1") != null ? checkResult.get(0).get("ATTRIBUTE1").toString() : StringUtil.EMPTY;
                    String attribute2 = checkResult.get(0).get("ATTRIBUTE2") != null ? checkResult.get(0).get("ATTRIBUTE2").toString() : StringUtil.EMPTY;
                    String attribute3 = checkResult.get(0).get("ATTRIBUTE3") != null ? checkResult.get(0).get("ATTRIBUTE3").toString() : StringUtil.EMPTY;
                    String attribute4 = checkResult.get(0).get("ATTRIBUTE4") != null ? checkResult.get(0).get("ATTRIBUTE4").toString() : StringUtil.EMPTY;
                    String attribute5 = checkResult.get(0).get("ATTRIBUTE5") != null ? checkResult.get(0).get("ATTRIBUTE5").toString() : StringUtil.EMPTY;
                    unitNameList = checkResult.get(0).get("UNITNAMELIST") != null ? checkResult.get(0).get("UNITNAMELIST").toString() : StringUtil.EMPTY;
                    /* <<== 20190124, hhlee, modify, change Type casting */
                    /* <<== 20190123, hhlee, modify, change Type casting */
                    
                    String updateSql = " UPDATE CT_PROCESSEDOPERATION SET ";
                    String condition = "";
                    
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("PRODUCTNAME", productName);
                    
                    if ( StringUtil.isEmpty(attribute2) )
                    {
                        condition = " ATTRIBUTE2 = :ATTRIBUTE2 ";
                        updateMap.put("ATTRIBUTE2", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute3) )
                    {
                        condition = " ATTRIBUTE3 = :ATTRIBUTE3 ";
                        updateMap.put("ATTRIBUTE3", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute4) )
                    {
                        condition = " ATTRIBUTE4 = :ATTRIBUTE4 ";
                        updateMap.put("ATTRIBUTE4", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    else if ( StringUtil.isEmpty(attribute5) )
                    {
                        condition = " ATTRIBUTE5 = :ATTRIBUTE5 ";
                        updateMap.put("ATTRIBUTE5", machineName + "," + operation + "," + factoryName + "," + 
                                                    productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
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
                                                    lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                        updateSql = updateSql + condition; 
                    }
                    
                    updateSql = updateSql + ", UNITNAMELIST = :UNITNAMELIST, UNITRECIPENAMELIST = :UNITRECIPENAMELIST"; 
                    
                    /* hhlee, 20190222, delete, add LotProcessEnd Logic ==>> */
                    updateMap.put("UNITNAMELIST", StringUtil.EMPTY);
                    updateMap.put("UNITRECIPENAMELIST", StringUtil.EMPTY);
                    /* <<== hhlee, 20190222, delete, add LotProcessEnd Logic */
                    
                    String where = " WHERE PRODUCTNAME = :PRODUCTNAME ";
                    updateSql = updateSql + where;
                    
                    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
                }
                else
                {
                    String insSql = " INSERT INTO CT_PROCESSEDOPERATION "
                            + " (PRODUCTNAME, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, UNITNAMELIST, UNITRECIPENAMELIST) "
                            + " VALUES "
                            + " (:PRODUCTNAME, :ATTRIBUTE1, :ATTRIBUTE2, :ATTRIBUTE3, :ATTRIBUTE4, :ATTRIBUTE5, :UNITNAMELIST, :UNITRECIPENAMELIST) ";
                    
                    Map<String, Object> insMap = new HashMap<String, Object>();
                    insMap.put("PRODUCTNAME", productName);
                    insMap.put("ATTRIBUTE1", machineName + "," + operation + "," + factoryName + "," + 
                                             productSpecName + "," + processFlowName + "," + machineRecipeName + "," + 
                                             lotName + "," + eventTimeKey + "," + unitNameList + "," + unitRecipeNameList + "," + subUnitNameList);
                    insMap.put("ATTRIBUTE2", "");
                    insMap.put("ATTRIBUTE3", "");
                    insMap.put("ATTRIBUTE4", "");
                    insMap.put("ATTRIBUTE5", "");
                    
                    /* hhlee, 20190222, delete, add LotProcessEnd Logic ==>> */
                    insMap.put("UNITNAMELIST", "");
                    insMap.put("UNITRECIPENAMELIST", "");
                    /* <<== hhlee, 20190222, delete, add LotProcessEnd Logic */

                    GenericServiceProxy.getSqlMesTemplate().update(insSql, insMap);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
        }
    }
	
	/**
	 * 
	 * @Name     resetProcessedOperationBeforeAOI
	 * @since    2018. 12. 12.
	 * @author   hhlee
	 * @contents Reset UNITNAMELIST, UNITRECIPENAMELIST of ProcessedOperation 
	 *           
	 * @param eventInfo
	 * @param productName
	 * @throws CustomException
	 */
	public void resetProcessedOperationBeforeAOI(EventInfo eventInfo, String productName) throws CustomException
    {
        try
        {            
            String checkSql = "SELECT PRODUCTNAME,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE5,UNITNAMELIST,UNITRECIPENAMELIST "
                    + " FROM CT_PROCESSEDOPERATION "
                    + " WHERE PRODUCTNAME = :PRODUCTNAME ";
            
            Map<String, Object> checkMap = new HashMap<String, Object>();
            checkMap.put("PRODUCTNAME", productName);

            List<Map<String, Object>> checkResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, checkMap);
            
            if ( checkResult!=null && checkResult.size() > 0 )
            {
                String updateSql = " UPDATE CT_PROCESSEDOPERATION SET                \n"
                                 + "        UNITNAMELIST = :UNITNAMELIST,            \n"
                                 + "        UNITRECIPENAMELIST = :UNITRECIPENAMELIST \n"
                                 + "  WHERE PRODUCTNAME = :PRODUCTNAME                 ";
                Map<String, Object> updateMap = new HashMap<String, Object>();
                updateMap.put("UNITNAMELIST", StringUtil.EMPTY);
                updateMap.put("UNITRECIPENAMELIST", StringUtil.EMPTY);
                updateMap.put("PRODUCTNAME", productName);
                                    
                GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateMap);
            }            
        }
        catch (Throwable e)
        {
            log.error("Update Fail! [CT_PROCESSEDOPERATION]");
        }
    }
	
	
	/**
	 * @author smkang
	 * @since 2018.08.13
	 * @param productName
	 * @param reasonCode
	 * @param department
	 * @param eventInfo
	 * @throws CustomException
	 * @see According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
	 */
	public void addMultiHoldProduct(String productName, String reasonCode, String department, String holdType, EventInfo eventInfo) throws CustomException {
		try {
			ProductMultiHold productMultiHoldData = ExtendedObjectProxy.getProductMultiHoldService().selectByKey(false, new Object[] {productName, reasonCode, department, eventInfo.getEventComment()});
			
			productMultiHoldData.setEventTime(eventInfo.getEventTime());
			productMultiHoldData.setEventName(eventInfo.getEventName());
			productMultiHoldData.setEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getProductMultiHoldService().modify(eventInfo, productMultiHoldData);
		} catch (greenFrameDBErrorSignal e) {
			// TODO: handle exception
			ProductMultiHold productMultiHoldData = new ProductMultiHold();
			
			productMultiHoldData.setProductName(productName);
			productMultiHoldData.setReasonCode(reasonCode);
			productMultiHoldData.setDepartment(department);
			productMultiHoldData.setEventComment(eventInfo.getEventComment());
			productMultiHoldData.setEventTime(eventInfo.getEventTime());
			productMultiHoldData.setEventName(eventInfo.getEventName());
			productMultiHoldData.setEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getProductMultiHoldService().create(eventInfo, productMultiHoldData);
		}
	}
	
	/**
	 * @author smkang
	 * @since 2018.08.13
	 * @param productName
	 * @param reasonCode
	 * @param department
	 * @param eventInfo
	 * @return MultiHoldProductCount
	 * @throws CustomException
	 * @see According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
	 */
	public int removeMultiHoldProduct(String productName, String reasonCode, String department, String seq, EventInfo eventInfo) throws CustomException {
		try {

			long position = Long.parseLong(seq);
			
			if(StringUtil.isEmpty(department))
			{
				department = " ";
			}

			try {
				ProductMultiHold productMultiHoldData = ExtendedObjectProxy.getProductMultiHoldService().selectByKey(false, new Object[] {productName, reasonCode, department, eventInfo.getEventComment(), position});
				ExtendedObjectProxy.getProductMultiHoldService().remove(eventInfo, productMultiHoldData);
			} catch (Exception e) {
				log.info("Not exist ProductMultiHold : ProductName :"+productName+" ReasonCode :"+reasonCode+" Department :"+department+ " EventComment :"+eventInfo.getEventComment() + " Seq :" + seq);
			}
			
			List<ProductMultiHold> productMultiHoldList = ExtendedObjectProxy.getProductMultiHoldService().select("PRODUCTNAME = ?", new Object[] {productName});
			
			return (productMultiHoldList != null && productMultiHoldList.size() > 0) ? productMultiHoldList.size() : 0;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public boolean checkRecipeIdleTimeLotFirstFlag(RecipeIdleTimeLot recipeIdleTimeLotData)
	{
		if(recipeIdleTimeLotData!=null)
		{
			String strSql = "SELECT R.MACHINENAME, R.RECIPENAME, R.IDLETIME " +
					"    FROM CT_RECIPEIDLETIMELOT RL, CT_RECIPEIDLETIME R " +
					"   WHERE     RL.MACHINENAME = :MACHINENAME " +
					"         AND RL.RECIPENAME = :RECIPENAME " +
					"         AND RL.MACHINENAME = R.MACHINENAME " +
					"         AND ((RL.RECIPENAME = R.RECIPENAME) OR (R.RECIPENAME = :STAR)) " +
					"ORDER BY MACHINENAME, RECIPENAME ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", recipeIdleTimeLotData.getmachineName());
			bindMap.put("RECIPENAME", recipeIdleTimeLotData.getrecipeName());
			bindMap.put("STAR", "*");

			List<Map<String, Object>> priorIdleTime = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

			if(priorIdleTime != null && priorIdleTime.size() > 0)
			{
				strSql = "SELECT LASTRUNTIME, TO_NUMBER ( (SYSDATE - LASTRUNTIME) * 24 * 60) " +
						"  FROM CT_RECIPEIDLETIMELOT  " +
						" WHERE MACHINENAME = :MACHINENAME  " +
						"       AND RECIPENAME = :RECIPENAME  " +
						"       AND :IDLETIME <= TO_NUMBER ( (SYSDATE - LASTRUNTIME) * 24 * 60)  ";

				bindMap.put("IDLETIME", priorIdleTime.get(priorIdleTime.size() - 1).get("IDLETIME").toString());

				List<Map<String, Object>> checkFirstFlag = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

				if(checkFirstFlag != null && checkFirstFlag.size() > 0)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public void checkRecipeIdleTime(Document doc, String factoryName, String areaName, String lotName, String carrierName, String machineName, String portName, String recipeName,EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{	
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName))
		{
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			RecipeIdleTime recipeIdleTime = null;
			
			try
			{
				recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTime = null;
			}
			
			if(recipeIdleTime == null)
			{
				try
				{
					recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, "*"});
				}
				catch (Exception ex)
				{
					recipeIdleTime = null;
				}
			}
			
			if(recipeIdleTime != null)
			{
				if(StringUtils.equals(recipeIdleTime.getvalidFlag(), "Y"))
				{
					RecipeIdleTimeLot recipeIdleTimeLotData = null;
					
					try
					{
						recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
					}
					catch (Exception ex)
					{
						recipeIdleTimeLotData = null;
					}
					
					if(recipeIdleTimeLotData == null)
					{
						recipeIdleTimeLotData = new RecipeIdleTimeLot(machineName, recipeName);
						recipeIdleTimeLotData.setproductSpecName(lotData.getProductSpecName());
						recipeIdleTimeLotData.setprocessOperationName(lotData.getProcessOperationName());
						recipeIdleTimeLotData.setfactoryName(factoryName);
						recipeIdleTimeLotData.setareaName(areaName);
						recipeIdleTimeLotData.setlastRunTime(recipeIdleTime.getlastEventTime());
						recipeIdleTimeLotData.setfirstLotFlag("");
						recipeIdleTimeLotData.setfirstCstID("");
						recipeIdleTimeLotData.setfirstLotID("");
						recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
						recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
						recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
						recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
						recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getRecipeIdleTimeLotService().create(eventInfo, recipeIdleTimeLotData);
						
					//	if(checkRecipeIdleTimeLotFirstFlag(recipeIdleTimeLotData))
					//	{    //MODIFY BY JHIYING ON20191225 MANTIS :5381 REMOVE IF係숭
							recipeIdleTimeLotData.setfirstLotFlag("Y");
							recipeIdleTimeLotData.setfirstLotID(lotName);
							recipeIdleTimeLotData.setfirstCstID(carrierName);
							
							/* 20190122, hhlee, modify, update RecipeIdleTimeLotHist.[firstLotFlag, firstLotID, firstCstID] ==>> */
							//ExtendedObjectProxy.getRecipeIdleTimeLotService().update(recipeIdleTimeLotData);
							
							//eventInfo.setEventName("SetRecipeIdleTimeLotFirstFlag");
							eventInfo.setCheckTimekeyValidation(false);
				            eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo,recipeIdleTimeLotData);
							//try 
                            //{
                            //    StringBuilder sql = new StringBuilder();
                            //    Map<String, Object> bindMap = new HashMap<String, Object>();
                            //    sql.setLength(0);
                            //    sql.append(" UPDATE CT_RECIPEIDLETIMELOTHIST A                     \n");
                            //    sql.append("    SET A.FIRSTLOTFLAG = :FIRSTLOTFLAG,                \n");
                            //    sql.append("        A.FIRSTCSTID = :FIRSTCSTID,                    \n");
                            //    sql.append("        A.FIRSTLOTID = :FIRSTLOTID                     \n");
                            //    sql.append("  WHERE A.MACHINENAME = :MACHINENAME                   \n");
                            //    sql.append("    AND A.RECIPENAME = :RECIPENAME                     \n");
                            //    sql.append("    AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME           \n");
                            //    sql.append("    AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME \n");
                            //    sql.append("    AND A.TIMEKEY = :TIMEKEY                             ");
                            //    bindMap.put("FIRSTLOTFLAG", recipeIdleTimeLotData.getfirstLotFlag());
                            //    bindMap.put("FIRSTCSTID", recipeIdleTimeLotData.getfirstCstID());
                            //    bindMap.put("FIRSTLOTID", recipeIdleTimeLotData.getfirstLotID());
                            //    bindMap.put("MACHINENAME", recipeIdleTimeLotData.getmachineName());
                            //    bindMap.put("RECIPENAME", recipeIdleTimeLotData.getrecipeName());
                            //    bindMap.put("PRODUCTSPECNAME", recipeIdleTimeLotData.getproductSpecName());
                            //    bindMap.put("PROCESSOPERATIONNAME", recipeIdleTimeLotData.getprocessOperationName());
                            //    bindMap.put("TIMEKEY", recipeIdleTimeLotData.getlastEventTimekey());
                            //    int rows = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
                            //} 
                            //catch (Exception ex) 
                            //{
                            //    log.error(String.format("Update Fail! [CT_RECIPEIDLETIMELOTHIST- "
                            //            + "FIRSTLOTFLAG:%s, FIRSTCSTID:%s, FIRSTLOTID:%s, MACHINENAME:%s, RECIPENAME:%s, "
                            //            + "PRODUCTSPECNAME:%s, PROCESSOPERATIONNAME:%s, TIMEKEY:%s   ]",
                            //            recipeIdleTimeLotData.getfirstLotFlag(), recipeIdleTimeLotData.getfirstCstID(), 
                            //            recipeIdleTimeLotData.getfirstLotID(), recipeIdleTimeLotData.getrecipeName(),
                            //            recipeIdleTimeLotData.getproductSpecName(), recipeIdleTimeLotData.getprocessOperationName(),
                            //            recipeIdleTimeLotData.getlastEventTimekey()));
                            //}
							/* <<== 20190122, hhlee, add, update RecipeIdleTimeLotHist.[firstLotFlag, firstLotID, firstCstID] */
														
							// Modified by smkang on 2018.08.21 - Move cancelOtherPortCarrier method to LotServiceUtil for using RecipeServiceUtil.
							//MESDurableServiceProxy.getDurableServiceUtil().cancelOtherPortCarrier(machineName, portName, recipeName, doc);
							MESLotServiceProxy.getLotServiceUtil().cancelOtherPortCarrier(machineName, portName, recipeName, doc);
					//	}
					}
					else
					{
						if(checkRecipeIdleTimeLotFirstFlag(recipeIdleTimeLotData))
						{
							recipeIdleTimeLotData.setfirstLotFlag("Y");
							recipeIdleTimeLotData.setfirstLotID(lotName);
							recipeIdleTimeLotData.setfirstCstID(carrierName);
							recipeIdleTimeLotData.setproductSpecName(lotData.getProductSpecName());
							recipeIdleTimeLotData.setprocessOperationName(lotData.getProcessOperationName());
							recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
							recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
							recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
							recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());

							ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo,recipeIdleTimeLotData );
							
							// Modified by smkang on 2018.08.21 - Move cancelOtherPortCarrier method to LotServiceUtil for using RecipeServiceUtil.
                            //MESDurableServiceProxy.getDurableServiceUtil().cancelOtherPortCarrier(machineName, portName, recipeName, doc);
							MESLotServiceProxy.getLotServiceUtil().cancelOtherPortCarrier(machineName, portName, recipeName, doc);
						}
					}
					
					RecipeIdleTimeLot CurrentrecipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[]{machineName, recipeName});
					
					if(StringUtil.equals(CurrentrecipeIdleTimeLotData.getfirstLotFlag(), "Y"))
					{
						MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
						
						String plusFirstLotInfoNote = "This is firstLot by RecipeIdleTime. FirstLotName:" + lotData.getKey().getLotName()+", FirstCSTName:" + carrierName;
						EventInfo firstEventInfo = EventInfoUtil.makeEventInfo("FirstLotHold", eventInfo.getEventUser(), plusFirstLotInfoNote, null, "FLHD");
						MESLotServiceProxy.getLotServiceUtil().reserveAHold(lotData, machineSpecData.getUdfs().get("DEPARTMENT"), firstEventInfo);
					}
				}
				
			}
			
		}
	}
	
	
	/**
     * 
     * @Name     recordProcessedOperationOfUnit
     * @since    2018. 9. 22.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param productName
     * @param unitName
     * @param unitRecipeName
     * @throws CustomException
     */
    public void recordProcessedOperationOfUnit(EventInfo eventInfo, String productName, String unitName, String unitRecipeName) throws CustomException
    {
        try
        {
            MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(unitName);
            
            if(!StringUtil.equals(macSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_TransportMachine))
            {
                ProcessedOperation processedOperationData = null;
                try
                {
                    processedOperationData = ExtendedObjectProxy.getProcessedOperationService().selectByKey(false, new Object[] {productName});
                }
                catch (Exception ex)
                {
                    processedOperationData = new ProcessedOperation(productName);
                    processedOperationData = ExtendedObjectProxy.getProcessedOperationService().create(eventInfo, processedOperationData);
                }
                
                String unitNameList = processedOperationData.getUnitNameList();
                if(StringUtil.isEmpty(unitNameList))
                {
                    unitNameList = unitName;
                }
                else
                {
                    if(StringUtil.indexOf(unitNameList, unitName) < 0)
                    {
                        unitNameList = unitNameList + "|" + unitName;
                    }
                }
                
                String unitRecipeNameList = processedOperationData.getUnitRecipeNameList();
                if(StringUtil.isEmpty(unitRecipeNameList))
                {
                    unitRecipeNameList = unitRecipeName;
                }
                else
                {
                    unitRecipeNameList = unitRecipeNameList + "|" + unitRecipeName;
                }
                
                processedOperationData.setUnitNameList(unitNameList);
                processedOperationData.setUnitRecipeNameList(unitRecipeNameList);
                
                ExtendedObjectProxy.getProcessedOperationService().modify(eventInfo, processedOperationData);
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());            
        }
    }
    
    /**
     * 
     * @Name     recordVirtualGlassOfUnit
     * @since    2018. 9. 22.
     * @author   hhlee
     * @contents 
     *           
     * @param virtualGlassData
     * @param unitName
     * @param unitRecipeName
     * @return
     * @throws CustomException
     */
    public VirtualGlass recordVirtualGlassOfUnit(VirtualGlass virtualGlassData, String unitName, String unitRecipeName) throws CustomException
    {
       
        VirtualGlass virtualglassdata = virtualGlassData;
        
        try
        {
            /* 20181114, hhlee, add, modify exclude TransportMachine ==>> */
            MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(unitName);
            
            //if(!StringUtil.equals(macSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_TransportMachine))
            /* <<== 20181114, hhlee, add, modify exclude TransportMachine */
            {
                String unitNameList = virtualglassdata.getUnitNameList();
                if(StringUtil.isEmpty(unitNameList))
                {
                    unitNameList = unitName;
                }
                else
                {
                    /* 20181114, hhlee, add, modify exclude Duplicate unitName ==>> */
                    if(StringUtil.indexOf(unitNameList, unitName) < 0)
                    {
                        unitNameList = unitNameList + "|" + unitName;
                    }  
                    /* <<== 20181114, hhlee, add, modify exclude Duplicate unitName */                 
                }
                
                String unitRecipeNameList = virtualglassdata.getUnitRecipeNameList();
                if(StringUtil.isEmpty(unitRecipeNameList))
                {
                    unitRecipeNameList = unitRecipeName;
                }
                else
                {
                    unitRecipeNameList = unitRecipeNameList + "|" + unitRecipeName;
                }
                
                virtualglassdata.setUnitNameList(unitNameList);
                virtualglassdata.setUnitRecipeNameList(unitRecipeNameList);
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());            
        }
        
        return virtualglassdata;
    }
    
    /**
     * 
     * @Name     productStateUpdateByLotStateCompleted
     * @since    2019. 2. 12.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     */
    public void productStateUpdateByLotStateCompleted(EventInfo eventInfo, Lot lotData)
    {
        List<Product> productList = null;
        
        try
        {
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] No Product to process");   
        }
        
        try
        {
            log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            for (Product productData : productList)
            {   
                productData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
                productData.setProcessOperationName("-");
                productData.setProcessOperationVersion(StringUtil.EMPTY);    
                productData.setProductProcessState(StringUtil.EMPTY); 
                productData.setProductHoldState(StringUtil.EMPTY); 
                ProductServiceProxy.getProductService().update(productData);
                
                /* 20190326, hhlee, modify, changed producthistory inquery ==>> */
                //String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? and eventname = ?)";
                //Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName(),"TrackOut"};
//                String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? )";
//                Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName()};
                ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	            productHistoryKey.setProductName(productData.getKey().getProductName());
	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());

	            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
                /* <<== 20190326, hhlee, modify, changed producthistory inquery */
                
//                List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//                ProductHistory producthistory = pArrayList.get(0);
                producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
                producthistory.setProcessOperationName("-");
                producthistory.setProcessOperationVersion(StringUtil.EMPTY);    
                producthistory.setProductProcessState(StringUtil.EMPTY); 
                producthistory.setProductHoldState(StringUtil.EMPTY); 
                ProductServiceProxy.getProductHistoryService().update(producthistory);
            } 
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] Update Fail Product Data.");   
        }
    }
    
    /**
     * 
     * @Name     productStateUpdateBySortEnd
     * @since    2019. 2. 12.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     */
    public void productStateUpdateBySortEnd(EventInfo eventInfo, Lot lotData)
    {
        List<Product> productList = null;
        try
        {
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] No Product to process");   
        }
        
        try
        {
            log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
            
            for (Product productData : productList)
            {   
                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                ProductServiceProxy.getProductService().update(productData);
                
                /* 20190326, hhlee, modify, changed producthistory inquery ==>> */
                //String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? and eventname = ?)";
                //Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName(),"TrackOut"};
//                String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? )";
//                Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName()};
                /* <<== 20190326, hhlee, modify, changed producthistory inquery */
                ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	            productHistoryKey.setProductName(productData.getKey().getProductName());
	            productHistoryKey.setTimeKey(productData.getLastEventTimeKey());

	            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);              
	            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);              
//                List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//                ProductHistory producthistory = pArrayList.get(0);
                producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                ProductServiceProxy.getProductHistoryService().update(producthistory);
            }
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] Update Fail Product Data.");   
        }
    }
    
    /**
     * 
     * @Name     recordProcessedOperationOfSubUnit
     * @since    2019. 2. 19.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param productName
     * @param unitName
     * @param subUnitName
     * @throws CustomException
     */
    public void recordProcessedOperationOfSubUnit(EventInfo eventInfo, String productName, String unitName, String subUnitName) throws CustomException
    {
        try
        {
            if(StringUtil.isNotEmpty(unitName) && StringUtil.isNotEmpty(subUnitName))
            {
                String unitNameandSubUnitNameCombine = unitName + "=" + subUnitName;
                
                MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(subUnitName);
                
                if(!StringUtil.equals(macSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_TransportMachine))
                {
                    ProcessedOperation processedOperationData = null;
                    try
                    {
                        processedOperationData = ExtendedObjectProxy.getProcessedOperationService().selectByKey(false, new Object[] {productName});
                    }
                    catch (Exception ex)
                    {
                        processedOperationData = new ProcessedOperation(productName);
                        processedOperationData = ExtendedObjectProxy.getProcessedOperationService().create(eventInfo, processedOperationData);
                    }
                    
                    String subUnitNameList = processedOperationData.getUnitNameList();
                    if(StringUtil.isEmpty(subUnitNameList))
                    {
                        subUnitNameList = unitNameandSubUnitNameCombine;
                    }
                    else
                    {
                        if(StringUtil.indexOf(subUnitNameList, unitNameandSubUnitNameCombine) < 0)
                        {
                            subUnitNameList = subUnitNameList + "|" + unitNameandSubUnitNameCombine;
                        }
                    }
                                    
                    processedOperationData.setUnitNameList(subUnitNameList);
                    
                    ExtendedObjectProxy.getProcessedOperationService().modify(eventInfo, processedOperationData);
                }
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());            
        }
    }
    
    /**
     * 
     * @Name     recordVirtualGlassOfSubUnit
     * @since    2019. 2. 19.
     * @author   hhlee
     * @contents 
     *           
     * @param virtualGlassData
     * @param unitName
     * @param subUnit
     * @return
     * @throws CustomException
     */
    public VirtualGlass recordVirtualGlassOfSubUnit(VirtualGlass virtualGlassData, String unitName, String subUnitName) throws CustomException
    {
       
        VirtualGlass virtualglassdata = virtualGlassData;
        
        try
        {
            if(StringUtil.isNotEmpty(unitName) && StringUtil.isNotEmpty(subUnitName))
            {
                String unitNameandSubUnitNameCombine = unitName + "=" + subUnitName;
                
                MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(subUnitName);
                
                if(!StringUtil.equals(macSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_TransportMachine))
                {
                    String subUnitNameList = virtualglassdata.getUnitNameList();
                    if(StringUtil.isEmpty(subUnitNameList))
                    {
                        subUnitNameList = unitNameandSubUnitNameCombine;
                    }
                    else
                    {
                        if(StringUtil.indexOf(subUnitNameList, unitNameandSubUnitNameCombine) < 0)
                        {
                            subUnitNameList = subUnitNameList + "|" + unitNameandSubUnitNameCombine;
                        }              
                    }
                                        
                    virtualglassdata.setUnitNameList(subUnitNameList);
                }
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());            
        }
        
        return virtualglassdata;
    }
    
    /**
     * 
     * @Name     stringInsertDelimitor
     * @since    2019. 2. 20.
     * @author   hhlee
     * @contents 
     *           
     * @param strString
     * @param position
     * @param strDelimitor
     * @param unitNameList
     * @return
     * @throws CustomException
     */
    public String stringInsertDelimitor(String strString, int position, String strDelimitor, String unitNameList) throws CustomException
    {
        String changeString = strString;
        String delimitorString = StringUtil.EMPTY;
                
        while(StringUtil.isNotEmpty(changeString))
        {
            delimitorString += StringUtil.substring(changeString, 0, position) + "|";
            changeString = StringUtil.substring(changeString, position);            
        }
        
        delimitorString = StringUtil.substring(delimitorString, 0, delimitorString.length() - 1);
        
        String[] splitUnitName = StringUtil.split(unitNameList , "|");
        String[] splitUnitRecipeName = StringUtil.split(delimitorString , "|");
        
        //String[] tempUnitRecipeName = splitUnitRecipeName;
        delimitorString = StringUtil.EMPTY;        
        
        for(int i = 0; i < splitUnitName.length; i++ )
        {
            try
            {
                MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(splitUnitName[i]);
                
                /* CONSTRUCTTYPE = 'A', 'B', 'C', 'D' .....*/
                int unitRecipeSeqCode = StringUtil.isEmpty(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE")) ? -1 : 
                    CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").charAt(0) - 'A';
                
                //if(unitRecipeSeqCode >= 0 && unitRecipeSeqCode <= tempUnitRecipeName.length -1)
                //{
                //    tempUnitRecipeName[unitRecipeSeqCode] = splitUnitRecipeName[i];
                //}
                if(unitRecipeSeqCode >= 0 && unitRecipeSeqCode <= splitUnitRecipeName.length -1)
                {
                    delimitorString += splitUnitRecipeName[unitRecipeSeqCode] + "|";
                }
                
            }
            catch (Exception ex)
            {
                //log.error(ex.getMessage());            
            }
        }        
        
        //delimitorString = StringUtil.join(tempUnitRecipeName, "|");
        delimitorString = StringUtil.substring(delimitorString, 0, delimitorString.length() - 1);
        
        log.info("Delimitor : ["+delimitorString+"]");
        
        return delimitorString;
    }
    
    /**
     * 
     * @Name     stringInsertDelimitorByUnitName
     * @since    2019. 2. 25.
     * @author   hhlee
     * @contents 
     *           
     * @param strString
     * @param position
     * @param strDelimitor
     * @param unitName
     * @return
     * @throws CustomException
     */
    public String stringInsertDelimitorByUnitName(String strString, int position, String strDelimitor, String unitName) throws CustomException
    {
        String changeString = strString;
        String delimitorString = StringUtil.EMPTY;
        String unitRecipeName = StringUtil.EMPTY;
                
        while(StringUtil.isNotEmpty(changeString))
        {
            delimitorString += StringUtil.substring(changeString, 0, position) + "|";
            changeString = StringUtil.substring(changeString, position);            
        }
        
        delimitorString = StringUtil.substring(delimitorString, 0, delimitorString.length() - 1);
        
        String[] splitUnitRecipeName = StringUtil.split(delimitorString , "|");
        
        delimitorString = StringUtil.EMPTY;        
        
        for(int i = 0; i < splitUnitRecipeName.length; i++ )
        {
            try
            {
                MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
                
                /* CONSTRUCTTYPE = 'A', 'B', 'C', 'D' .....*/
                int unitRecipeSeqCode = StringUtil.isEmpty(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE")) ? -1 : 
                    CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").charAt(0) - 'A';
                
                unitRecipeName = splitUnitRecipeName[unitRecipeSeqCode];                
            }
            catch (Exception ex)
            {
                //log.error(ex.getMessage());            
            }
        }
        
        log.info("Unit Recipe Name : ["+unitRecipeName+"]");
        
        return unitRecipeName;
    }
    
    /**
     * 
     * @Name     setSpcProcessedOperationData
     * @since    2019. 2. 25.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param productEle
     * @param machineName
     * @throws CustomException
     */
    public void setSpcProcessedOperationData(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
    {
        try
        {
            EventInfo spcEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
                   
            String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
            
            /* 00. Lot, Product Validation */            
            Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                      
            Lot lotdata = MESLotServiceProxy.getLotInfoUtil().getLotData(aProduct.getLotName());
                        
            String factoryName = lotdata.getFactoryName();
            String productSpecName = lotdata.getProductSpecName();
            String processFlowName = lotdata.getProcessFlowName();
            String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", false);
            String lotName = lotdata.getKey().getLotName();
            String ecCode = CommonUtil.getValue(lotdata.getUdfs(), "ECCODE");
            String eventTimeKey = eventInfo.getEventTimeKey();
            String processOperationName = lotdata.getProcessOperationName();
            
            ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotdata.getFactoryName(), processOperationName);
            String operationType = processOperationSpecData.getProcessOperationType();
            
            if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
            {            
                String processedUnitNameList = StringUtil.EMPTY;    
                List<Element> processedUnitElement = SMessageUtil.getSubSequenceItemList(productEle, "PROCESSEDUNITLIST", false);
                
                /* 01. Unit, SubUnit Relation Creation 
                 *     UnitAndSubUnitNameList[UNITNAME01=SUBUNIT01|UNITNAME02=SUBUNIT02|....]
                 *     UnitNameList[UNITNAME01|UNITNAME02|....]
                 * */
                if(processedUnitElement != null && processedUnitElement.size() > 0)
                {
                    List<Element> processedSubUnitElement = null;
                    String processedUnitName = StringUtil.EMPTY;
                    String processedUnitRecipe = StringUtil.EMPTY;
                    String processedUnitType = StringUtil.EMPTY;
                    String processedSubUnitName = StringUtil.EMPTY;
                    
                    String unitRecipeOnlyList = StringUtil.EMPTY;
                    boolean isunitRecipeOnly = false;
                    MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);  
                    
                    if(StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), GenericServiceProxy.getConstantMap().ConstructType_PHOTO) ||
                            StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "RMSUNITRECIPEONLY"), GenericServiceProxy.getConstantMap().Flag_Y))
                    {
                        ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);
                        unitRecipeOnlyList = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));
                        isunitRecipeOnly = true;
                    }
                    else
                    {
                    }
                                        
                    for (Element processUnit : processedUnitElement )
                    {
                        processedUnitName = SMessageUtil.getChildText(processUnit, "PROCESSEDUNITNAME", false);
                        machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(processedUnitName);
                        processedUnitType = machineSpecData.getMachineType();
                                                
                        if(isunitRecipeOnly)
                        {
                            processedUnitRecipe = this.stringInsertDelimitorByUnitName(unitRecipeOnlyList, 4, "|", processedUnitName);
                        }
                        else
                        {
                            processedUnitRecipe =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeListByUnitName(machineName, machineRecipeName, processedUnitName);               
                        }
                                                
                        /* CT_SPCPROCESSEDOPERATION INSERT */
                        spcEventInfo.setCheckTimekeyValidation(false);
                        spcEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                        
                        SpcProcessedOperation spcProcessedOperationData = new SpcProcessedOperation(spcEventInfo.getEventTimeKey(), productName);
                        
                        spcProcessedOperationData.setFactoryName(factoryName);
                        spcProcessedOperationData.setLotName(lotName);
                        spcProcessedOperationData.setProductSpecName(productSpecName);
                        spcProcessedOperationData.setProcessFlowName(processFlowName);
                        spcProcessedOperationData.setProcessOperationName(processOperationName);
                        spcProcessedOperationData.setMachineName(machineName);
                        spcProcessedOperationData.setMachineRecipeName(machineRecipeName);
                        
                        spcProcessedOperationData.setUnitName(processedUnitName);
                        
                        spcProcessedOperationData.setUnitRecipeName(processedUnitRecipe);
                        spcProcessedOperationData.setUnitType(processedUnitType);
                        
                        spcProcessedOperationData.setCreateTime(spcEventInfo.getEventTime());
                        spcProcessedOperationData.setEvnetName(spcEventInfo.getEventName());
                        spcProcessedOperationData.setEventTimekey(eventTimeKey);
                        spcProcessedOperationData.setEvnetUser(spcEventInfo.getEventUser());
                        
                        processedSubUnitElement = SMessageUtil.getSubSequenceItemList(processUnit, "PROCESSEDSUBUNITLIST", false);                        
                        if(processedSubUnitElement != null && processedSubUnitElement.size() > 0)
                        {
                            for (Element processSubUnit : processedSubUnitElement )
                            {
                                processedSubUnitName = SMessageUtil.getChildText(processSubUnit, "PROCESSEDSUBUNITNAME", false); 
                                spcProcessedOperationData.setSubUnitName(processedSubUnitName);
                                
                                spcProcessedOperationData = ExtendedObjectProxy.getSpcProcessedOperationService().create(spcEventInfo, spcProcessedOperationData);
                            }                           
                        }
                        else
                        {  
                            spcProcessedOperationData = ExtendedObjectProxy.getSpcProcessedOperationService().create(spcEventInfo, spcProcessedOperationData);
                        }
                    }                  
                }
            }
        }
        catch (Throwable e)
        {
            log.error("[setSpcProcessedOperationData] Update Fail! [CT_SPCPROCESSEDOPERATION] - " + e.getLocalizedMessage());
        }
    }
    
    /**
     * 
     * @Name     productStateUpdateByAllGlasssScrap
     * @since    2019. 3. 21.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param lotData
     */
    public void productStateUpdateByAllGlasssScrap(EventInfo eventInfo, Lot lotData)
    {
        List<Product> productList = null;
        
        try
        {
        	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
            productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] No Product to process");   
        }
        
        try
        {
            for (Product productData : productList)
            {   
                productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle); 
                ProductServiceProxy.getProductService().update(productData);
                
                SetEventInfo setEventInfo = new SetEventInfo();
                ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);                
            } 
        }
        catch (Exception ex)
        {
            //throw new CustomException("SYS-9999", "Product", "No Product to process");
            log.error("[SYS-9999] Update Fail Product Data.");   
        }
    }
    
    ///**
    // * 
    // * @Name     setSpcProcessedOperationData
    // * @since    2019. 2. 21.
    // * @author   hhlee
    // * @contents 
    // *           
    // * @param eventInfo
    // * @param productEle
    // * @param machineName
    // * @throws CustomException
    // */
    //public void setSpcProcessedOperationData(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
    //{
    //    try
    //    {
    //        EventInfo spcEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
    //               
    //        String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    //        
    //        /* 00. Lot, Product Validation */            
    //        Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                      
    //        Lot lotdata = MESLotServiceProxy.getLotInfoUtil().getLotData(aProduct.getLotName());
    //                    
    //        String factoryName = lotdata.getFactoryName();
    //        String productSpecName = lotdata.getProductSpecName();
    //        String processFlowName = lotdata.getProcessFlowName();
    //        String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", false);
    //        String lotName = lotdata.getKey().getLotName();
    //        String ecCode = CommonUtil.getValue(lotdata.getUdfs(), "ECCODE");
    //        String eventTimeKey = eventInfo.getEventTimeKey();
    //        String processOperationName = lotdata.getProcessOperationName();
    //        
    //        ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotdata.getFactoryName(), processOperationName);
    //        String operationType = processOperationSpecData.getProcessOperationType();
    //        
    //        if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
    //        {            
    //            String processedUnitNameList = StringUtil.EMPTY;    
    //            String processedUnitAndSubUnitNameList = StringUtil.EMPTY;
    //            String tempProcessedUnitAndSubUnitNameList = StringUtil.EMPTY;
    //            List<Element> processedUnitElement = SMessageUtil.getSubSequenceItemList(productEle, "PROCESSEDUNITLIST", false);
    //            
    //            /* 01. Unit, SubUnit Relation Creation 
    //             *     UnitAndSubUnitNameList[UNITNAME01=SUBUNIT01|UNITNAME02=SUBUNIT02|....]
    //             *     UnitNameList[UNITNAME01|UNITNAME02|....]
    //             * */
    //            if(processedUnitElement != null && processedUnitElement.size() > 0)
    //            {
    //                List<Element> processedSubUnitElement = null;
    //                String processedUnitName = StringUtil.EMPTY;
    //                String processedSubUnitName = StringUtil.EMPTY;
    //                
    //                for (Element processUnit : processedUnitElement )
    //                {
    //                    processedUnitName = SMessageUtil.getChildText(processUnit, "PROCESSEDUNITNAME", false);
    //                    if(StringUtil.isNotEmpty(processedUnitName))
    //                    {
    //                        processedUnitNameList += processedUnitName + "|";
    //                        tempProcessedUnitAndSubUnitNameList = processedUnitName + "=";
    //                    }
    //                    
    //                    tempProcessedUnitAndSubUnitNameList = "=" + processedUnitName;
    //                    
    //                    processedSubUnitElement = SMessageUtil.getSubSequenceItemList(processUnit, "PROCESSEDSUBUNITLIST", false);                        
    //                    if(processedSubUnitElement != null && processedSubUnitElement.size() > 0)
    //                    {
    //                        for (Element processSubUnit : processedSubUnitElement )
    //                        {
    //                            processedSubUnitName = SMessageUtil.getChildText(processSubUnit, "PROCESSEDSUBUNITNAME", false);
    //                            processedUnitAndSubUnitNameList += tempProcessedUnitAndSubUnitNameList + processedSubUnitName + "|";                                
    //                        }                           
    //                    }
    //                    else
    //                    {
    //                        processedUnitAndSubUnitNameList += tempProcessedUnitAndSubUnitNameList + "|";
    //                    }                        
    //                }
    //                
    //                processedUnitNameList = StringUtil.substring(processedUnitNameList, 0, processedUnitNameList.length() - 1);
    //                processedUnitAndSubUnitNameList = StringUtil.substring(processedUnitAndSubUnitNameList, 0, processedUnitAndSubUnitNameList.length() - 1);
    //                
    //                /* 02. Get UnitRecipe Information  
    //                 *     - PHOTO/RMSUNITRECIPEONLY='Y', MACHINERECIPE[UNIT01(4)UNIT02(4)UNIT03(4)]] : UnitNameList Sort
    //                 *     - getUnitRecipeList : UnitNameList Sort
    //                 * */
    //                String unitRecipeNameList = StringUtil.EMPTY;
    //                
    //                MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);  
    //                if(StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), GenericServiceProxy.getConstantMap().ConstructType_PHOTO) ||
    //                        StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "RMSUNITRECIPEONLY"), GenericServiceProxy.getConstantMap().Flag_Y))
    //                {
    //                    ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);
    //                    unitRecipeNameList = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));
    //                    unitRecipeNameList = this.stringInsertDelimitor(unitRecipeNameList, 4, "|", processedUnitNameList);
    //                }
    //                else
    //                {
    //                    unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName, processedUnitNameList);                
    //                }
    //                
    //                String[] splitProcUnitAndSubUnitNameList = StringUtil.split(processedUnitAndSubUnitNameList, "|");  
    //                String[] splitProcUnitRecipeNameList = StringUtil.split(unitRecipeNameList, "|"); 
    //                                   
    //                /* 03. Set  SPC ProcessedOperationData 
    //                 *     - UnitAndSubUnitNameList Separeation ['|', '=']
    //                 *     - UnitRecipeNameList Set
    //                 * */
    //                String procUnitName = StringUtil.EMPTY;
    //                String procUnitType = StringUtil.EMPTY;
    //                String procSubUnitName = StringUtil.EMPTY;
    //                String procUnitRecipeName = StringUtil.EMPTY;
    //                for (int i = 0; i < splitProcUnitAndSubUnitNameList.length; i++ )
    //                {   
    //                    if(StringUtil.isNotEmpty(splitProcUnitAndSubUnitNameList[i]))
    //                    {
    //                        String[] procUnitAndSubUnitName = StringUtil.split(splitProcUnitAndSubUnitNameList[i], "=");
    //                                                    
    //                        procUnitName = procUnitAndSubUnitName[0];
    //                        procSubUnitName = procUnitAndSubUnitName[1];
    //                        
    //                        procUnitType = StringUtil.EMPTY;
    //                        if(StringUtil.isNotEmpty(procUnitName))
    //                        {
    //                            machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(procUnitName);
    //                            procUnitType = machineSpecData.getMachineType();
    //                        }
    //                        
    //                        /* Get UnitType Info */
    //                        procUnitRecipeName = StringUtil.EMPTY;
    //                        if(i <= splitProcUnitRecipeNameList.length - 1)
    //                        {
    //                            procUnitRecipeName = splitProcUnitRecipeNameList[i];
    //                        }                            
    //                                                    
    //                        /* CT_SPCPROCESSEDOPERATION INSERT */
    //                        spcEventInfo.setCheckTimekeyValidation(false);
    //                        spcEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
    //                        
    //                        SpcProcessedOperation spcProcessedOperationData = new SpcProcessedOperation(spcEventInfo.getEventTimeKey(), productName);
    //                        
    //                        spcProcessedOperationData.setFactoryName(factoryName);
    //                        spcProcessedOperationData.setLotName(lotName);
    //                        spcProcessedOperationData.setProductSpecName(productSpecName);
    //                        spcProcessedOperationData.setProcessFlowName(processFlowName);
    //                        spcProcessedOperationData.setProcessOperationName(processOperationName);
    //                        spcProcessedOperationData.setMachineName(machineName);
    //                        spcProcessedOperationData.setMachineRecipeName(machineRecipeName);
    //                        
    //                        spcProcessedOperationData.setUnitName(procUnitName);
    //                        spcProcessedOperationData.setUnitRecipeName(procUnitRecipeName);
    //                        spcProcessedOperationData.setUnitType(procUnitType);
    //                        spcProcessedOperationData.setSubUnitName(procSubUnitName);                            
    //                        
    //                        spcProcessedOperationData.setCreateTime(spcEventInfo.getEventTime());
    //                        spcProcessedOperationData.setEvnetName(spcEventInfo.getEventName());
    //                        spcProcessedOperationData.setEventTimekey(eventTimeKey);
    //                        spcProcessedOperationData.setEvnetUser(spcEventInfo.getEventUser());
    //                        
    //                        spcProcessedOperationData = ExtendedObjectProxy.getSpcProcessedOperationService().create(spcEventInfo, spcProcessedOperationData); 
    //                    }
    //                }                        
    //            }
    //        }
    //    }
    //    catch (Throwable e)
    //    {
    //        log.error("[setSpcProcessedOperationData] Update Fail! [CT_PROCESSEDOPERATION] - " + e.getLocalizedMessage());
    //    }
    //}
    
    
    ///**
    // * 
    // * @Name     setSpcProcessedOperationData
    // * @since    2019. 2. 21.
    // * @author   hhlee
    // * @contents 
    // *           
    // * @param eventInfo
    // * @param productEle
    // * @param machineName
    // * @throws CustomException
    // */
    //public void setSpcProcessedOperationData(EventInfo eventInfo, Element productEle, String machineName) throws CustomException
    //{
    //    try
    //    {
    //        EventInfo spcEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
    //        
    //        String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
    //        
    //        Product aProduct = MESProductServiceProxy.getProductServiceUtil().getProductByProductName(productName);                      
    //        Lot lotdata = MESLotServiceProxy.getLotInfoUtil().getLotData(aProduct.getLotName());
    //                    
    //        String factoryName = lotdata.getFactoryName();
    //        String productSpecName = lotdata.getProductSpecName();
    //        String processFlowName = lotdata.getProcessFlowName();
    //        String machineRecipeName = SMessageUtil.getChildText(productEle, "PRODUCTRECIPE", false);
    //        String lotName = lotdata.getKey().getLotName();
    //        String ecCode = CommonUtil.getValue(lotdata.getUdfs(), "ECCODE");
    //        String eventTimeKey = eventInfo.getEventTimeKey();
    //        String operation = lotdata.getProcessOperationName();
    //        
    //        ProcessOperationSpec processOperationSpecData = CommonUtil.getProcessOperationSpec(lotdata.getFactoryName(), operation);
    //        String operationType = processOperationSpecData.getProcessOperationType();
    //        
    //        if (StringUtil.equals(operationType, GenericServiceProxy.getConstantMap().Pos_Production))
    //        {            
    //            /* CT_SPCPROCESSEDOPERATION INSERT */
    //            ExtendedObjectProxy.getSpcProcessedOperationService().setSpcProcessedOperationData(spcEventInfo, lotdata, 
    //                    productName, machineName, machineRecipeName, eventTimeKey);
    //            
    //            String processedUnitNameList = StringUtil.EMPTY;    
    //            String processedUnitAndSubUnitNameList = StringUtil.EMPTY;
    //            String tempProcessedUnitAndSubUnitNameList = StringUtil.EMPTY;
    //            List<Element> processedUnitElement = SMessageUtil.getSubSequenceItemList(productEle, "PROCESSEDUNITLIST", false);
    //            
    //            if(processedUnitElement != null && processedUnitElement.size() > 0)
    //            {
    //                List<Element> processedSubUnitElement = null;
    //                String processedUnitName = StringUtil.EMPTY;
    //                String processedSubUnitName = StringUtil.EMPTY;
    //                
    //                for (Element processUnit : processedUnitElement )
    //                {
    //                    processedUnitName = SMessageUtil.getChildText(processUnit, "PROCESSEDUNITNAME", false);
    //                    if(StringUtil.isNotEmpty(processedUnitName))
    //                    {
    //                        processedUnitNameList += processedUnitName + "|";                            
    //                    }
    //                    tempProcessedUnitAndSubUnitNameList = processedUnitName + "=";
    //                    
    //                    processedSubUnitElement = SMessageUtil.getSubSequenceItemList(processUnit, "PROCESSEDSUBUNITLIST", false);                        
    //                    if(processedSubUnitElement != null && processedSubUnitElement.size() > 0)
    //                    {
    //                        for (Element processSubUnit : processedSubUnitElement )
    //                        {
    //                            processedSubUnitName = SMessageUtil.getChildText(processSubUnit, "PROCESSEDSUBUNITNAME", false);
    //                            processedUnitAndSubUnitNameList += tempProcessedUnitAndSubUnitNameList + processedSubUnitName + "|";                                
    //                        }                           
    //                    }
    //                    else
    //                    {
    //                        processedUnitAndSubUnitNameList += "|";
    //                    }                        
    //                }
    //                
    //                processedUnitNameList = StringUtil.substring(processedUnitNameList, 0, processedUnitNameList.length() - 1);
    //                processedUnitAndSubUnitNameList = StringUtil.substring(processedUnitAndSubUnitNameList, 0, processedUnitAndSubUnitNameList.length() - 1);
    //                
    //                String unitRecipeNameList = StringUtil.EMPTY;
    //                
    //                MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);  
    //                if(StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), GenericServiceProxy.getConstantMap().ConstructType_PHOTO) ||
    //                        StringUtil.equals(CommonUtil.getValue(machineSpecData.getUdfs(), "RMSUNITRECIPEONLY"), GenericServiceProxy.getConstantMap().Flag_Y))
    //                {
    //                    ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, operation, machineName, ecCode);
    //                    unitRecipeNameList = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));
    //                    log.info("[01.UNIT RECIPE LIST]" + unitRecipeNameList);
    //                    unitRecipeNameList = this.stringInsertDelimitor(unitRecipeNameList, 4, "|", processedUnitNameList);
    //                }
    //                else
    //                {
    //                    unitRecipeNameList =  MESRecipeServiceProxy.getRecipeServiceUtil().getUnitRecipeList(machineName, machineRecipeName, processedUnitNameList);                
    //                }
    //                
    //                String[] splitProcUnitAndSubUnitNameList = StringUtil.split(processedUnitAndSubUnitNameList , "|");  
    //                String[] splitProcUnitRecipeNameList = StringUtil.split(unitRecipeNameList , "|"); 
    //                
    //                String procUnitName = StringUtil.EMPTY;
    //                String procUnitType = StringUtil.EMPTY;
    //                String procSubUnitName = StringUtil.EMPTY;
    //                String procUnitRecipeName = StringUtil.EMPTY;
    //                for (int i = 0; i < splitProcUnitAndSubUnitNameList.length; i++ )
    //                {   
    //                    if(StringUtil.isNotEmpty(splitProcUnitAndSubUnitNameList[i]))
    //                    {
    //                        String[] procUnitAndSubUnitName = StringUtil.split(splitProcUnitAndSubUnitNameList[i], "=");
    //                        
    //                        procUnitName = StringUtil.isEmpty(procUnitAndSubUnitName[0]) ? " " : procUnitAndSubUnitName[0];
    //                        procSubUnitName = StringUtil.isEmpty(procUnitAndSubUnitName[1]) ? " " : procUnitAndSubUnitName[0];
    //                        
    //                        procUnitType = StringUtil.EMPTY;
    //                        if(StringUtil.isNotEmpty(procUnitName))
    //                        {
    //                            machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(procUnitName);
    //                            procUnitType = machineSpecData.getMachineType();
    //                        }
    //                        
    //                        procUnitRecipeName = StringUtil.EMPTY;
    //                        if(i <= splitProcUnitRecipeNameList.length - 1)
    //                        {
    //                            procUnitRecipeName = splitProcUnitRecipeNameList[i];
    //                        }                            
    //                        
    //                        ExtendedObjectProxy.getSpcProcessedUnitService().setSpcProcessedUnitData(spcEventInfo, productName, 
    //                                procUnitName, procSubUnitName, procUnitRecipeName, procUnitType);
    //                        
    //                    }
    //                }                        
    //            }
    //        }
    //    }
    //    catch (Throwable e)
    //    {
    //        log.error("Update Fail! [CT_PROCESSEDOPERATION]");
    //    }
    //}
    
	public void checkRecipeIdleTimeFirstFlag(Lot lotData, String carrierName, String machineName, String recipeName, String areaName, String factoryName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if(!StringUtils.isEmpty(machineName) && !StringUtils.isEmpty(recipeName))
		{
			RecipeIdleTime recipeIdleTime = null;
			
			try
			{
				recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
			}
			catch (Exception ex)
			{
				recipeIdleTime = null;
			}
			
			if(recipeIdleTime == null)
			{
				try
				{
					recipeIdleTime = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, "*"});
				}
				catch (Exception ex)
				{
					recipeIdleTime = null;
				}
			}
			
			if(recipeIdleTime != null)
			{
				if(StringUtils.equals(recipeIdleTime.getvalidFlag(), "Y"))
				{
					MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

					RecipeIdleTimeLot recipeIdleTimeLotData = null;
					
					try
					{
						recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
					}
					catch (Exception ex)
					{
						recipeIdleTimeLotData = null;
					}
					
					if(recipeIdleTimeLotData == null)
					{
						recipeIdleTimeLotData = new RecipeIdleTimeLot(machineName, recipeName);
						recipeIdleTimeLotData.setproductSpecName(lotData.getProductSpecName());
						recipeIdleTimeLotData.setprocessOperationName(lotData.getProcessOperationName());
						recipeIdleTimeLotData.setfactoryName(factoryName);
						recipeIdleTimeLotData.setareaName(areaName);
						recipeIdleTimeLotData.setlastRunTime(recipeIdleTime.getlastEventTime());
						recipeIdleTimeLotData.setfirstLotFlag("");
						recipeIdleTimeLotData.setfirstCstID("");
						recipeIdleTimeLotData.setfirstLotID("");
						recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
						recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
						recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
						recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
						recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getRecipeIdleTimeLotService().create(eventInfo, recipeIdleTimeLotData);
					}
					else
					{
						if(!StringUtil.equals(recipeIdleTimeLotData.getfirstLotFlag(), "Y"))
						{
							eventInfo.setEventName("TrackOut");

							recipeIdleTimeLotData.setlastRunTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
							recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
							recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
							recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
							recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());

							ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo, recipeIdleTimeLotData);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @Name     slotPositionMQCProductForBranchRework
	 * @since    2019. 5. 15.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param productData
	 * @param flowType
	 * @return
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws CustomException
	 */
    public String slotPositionMQCProductForBranchRework(Product productData, String flowType) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    {   
        String samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
        
        if(StringUtil.equals(productData.getProductionType(), GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA))
        {
            String slotPostion = StringUtil.EMPTY;
            
            try
            {         
                String strSql = "  SELECT M.MQCJOBNAME,                                        \n"
                              + "         P.PRODUCTNAME, P.POSITION,                           \n"
                              + "         M.LOTNAME, M.CARRIERNAME, M.FACTORYNAME              \n"
                              + "    FROM CT_MQCJOB M, CT_MQCJOBPOSITION P                     \n"
                              + "   WHERE M.LOTNAME = :LOTNAME                                 \n"
                              + "     AND M.CARRIERNAME = :CARRIERNAME                         \n"
                              + "     AND M.FACTORYNAME = :FACTORYNAME                         \n"
                              + "     AND M.MQCSTATE = :MQCSTATE                               \n"
                              + "     AND M.MQCJOBNAME = P.MQCJOBNAME                          \n"
                              + "     AND P.PRODUCTNAME = :PRODUCTNAME                         \n"
                              + "     AND P.POSITION = :POSITION                               \n"
                              + "  GROUP BY M.MQCJOBNAME, P.PRODUCTNAME, P.POSITION,           \n"
                              + "           M.LOTNAME, M.CARRIERNAME, M.FACTORYNAME              ";
                                                 
                
                Map<String, Object> bindMap = new HashMap<String, Object>();
                bindMap.put("LOTNAME", productData.getLotName());
                bindMap.put("CARRIERNAME", productData.getCarrierName());
                bindMap.put("FACTORYNAME", productData.getFactoryName());
                bindMap.put("MQCSTATE", "Executing");                
                bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
                bindMap.put("POSITION", productData.getPosition());
                
                List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
                                
                if(mqcJobPositionList != null && mqcJobPositionList.size() > 0)
                {
                    slotPostion = ( mqcJobPositionList.get(0).get("POSITION") != null ? 
                            mqcJobPositionList.get(0).get("POSITION").toString() : StringUtil.EMPTY);                   
                }
            }
            catch(Exception ex)
            {
                log.error("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProductForBranchRework]");
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
        }
        else
        {
            if(StringUtil.equals(flowType, GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_REWORK))
            {
                if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
                {
                    samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                }
                else
                {
                    samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                }
            }
            else if(StringUtil.equals(flowType, GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
            {
                if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S))
                {
                    samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
                }
                else
                {
                    samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
                }
            }
            else
            {                
            }
        }
        
        return samplingFlag;
    }
    
	///**
	// * 
	// * @Name     slotPositionMQCProductForBranchRework
	// * @since    2019. 5. 13.
	// * @author   hhlee
	// * @contents 
	// *           
	// * @param lotNodeStack
	// * @param productData
	// * @return
	// * @throws FrameworkErrorSignal
	// * @throws NotFoundSignal
	// * @throws CustomException
	// */
	//public List<Map<String, Object>> slotPositionMQCProductForBranchRework(String lotNodeStack, Product productData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
    //{   
	//    List<Map<String, Object>> mqcJobPositionList = null;
    //    try
    //    {
    //        String tempNodeStack =  StringUtil.reverse(StringUtil.substring(StringUtil.reverse(lotNodeStack), StringUtil.indexOf(StringUtil.reverse(lotNodeStack), ".") + 1));
    //        String[] nodeStackList = StringUtil.split(tempNodeStack, '.');
    //        if(nodeStackList.length > 0)
    //        {
    //            
    //            Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackList[nodeStackList.length - 1]);    
    //            
    //            String returnProcessOperationName = nodeData.getNodeAttribute1();
    //            String returnProcessOperationVersion = nodeData.getNodeAttribute2();
    //            
    //            String strSql = "  SELECT M.MQCJOBNAME,                                        \n"
    //                          + "         P.PRODUCTNAME, P.POSITION, P.RECIPENAME,             \n"
    //                          + "         P.PROCESSOPERATIONNAME, P.PROCESSOPERATIONVERSION,   \n"
    //                          + "         M.LOTNAME, M.CARRIERNAME, M.FACTORYNAME,             \n"
    //                          + "         M.MQCSTATE, M.MQCUSEPRODUCTSPEC,                     \n"
    //                          + "         M.AUTOMQCFLAG                                        \n"
    //                          + "    FROM CT_MQCJOB M, CT_MQCJOBPOSITION P                     \n"
    //                          + "   WHERE M.LOTNAME = :LOTNAME                                 \n"
    //                          + "     AND M.CARRIERNAME = :CARRIERNAME                         \n"
    //                          + "     AND M.FACTORYNAME = :FACTORYNAME                         \n"
    //                          + "     AND M.MQCSTATE = :MQCSTATE                               \n"
    //                          + "     AND M.MQCJOBNAME = P.MQCJOBNAME                          \n"
    //                          + "     AND P.PRODUCTNAME = :PRODUCTNAME                         \n"
    //                          + "     AND P.POSITION = :POSITION                               \n"
    //                          + "     AND P.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME       \n"
    //                          + "     AND P.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION   ";
    //            
    //            Map<String, Object> bindMap = new HashMap<String, Object>();
    //            bindMap.put("LOTNAME", productData.getLotName());
    //            bindMap.put("CARRIERNAME", productData.getCarrierName());
    //            bindMap.put("FACTORYNAME", productData.getFactoryName());
    //            bindMap.put("MQCSTATE", "Executing");                
    //            bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
    //            bindMap.put("POSITION", productData.getPosition());
    //            bindMap.put("PROCESSOPERATIONNAME", returnProcessOperationName);
    //            bindMap.put("PROCESSOPERATIONVERSION", returnProcessOperationVersion);
    //                
    //            mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
    //            
    //            if(mqcJobPositionList == null || mqcJobPositionList.size() <= 0)
    //            {
    //                log.warn("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProductForBranchRework]");
    //                mqcJobPositionList = null;
    //            }
    //        }
    //    }
    //    catch(Exception ex)
    //    {
    //        log.error("slotPositionMQCProduct inquery is Fail! [slotPositionMQCProductForBranchRework]");
    //        mqcJobPositionList = null;
    //    }
    //    
    //    return mqcJobPositionList;
    //}
    
    /**
     * @author smkang
     * @since 2019.05.28
     * @param productData
     * @param updateUdfs
     * @see ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
     */
    public synchronized void updateProductWithoutHistory(Product productData, Map<String, String> updateUdfs) {
    	if (productData != null && updateUdfs != null && updateUdfs.size() > 0) {
    		try {
    			String sqlStatement = "";
        		String setStatement = "";    		
        		
        		Set<String> keys = updateUdfs.keySet();
        		for (String key : keys) {
        			productData.getUdfs().put(key, updateUdfs.get(key));
        			setStatement = setStatement.concat(key).concat(" = :").concat(key).concat(", "); 
    			}
        		
        		sqlStatement = sqlStatement.concat("UPDATE PRODUCT SET ").concat(StringUtils.removeEnd(setStatement, ", ")).concat(" WHERE PRODUCTNAME = :PRODUCTNAME");
        		updateUdfs.put("PRODUCTNAME", productData.getKey().getProductName());
        		
        		if (GenericServiceProxy.getSqlMesTemplate().update(sqlStatement, updateUdfs) > 0)
        			log.debug(sqlStatement + " is succeeded to be executed.");
        		else
        			log.debug(sqlStatement + " is failed to be executed.");
			} catch (Exception e) {
				throw e;
			}
    	}
    }
    
	public static void insertProductProcLocationHist(EventInfo eventInfo, String productName, String machineName, String unitName)
			throws CustomException {

		Product productData = CommonUtil.getProductData(productName);

	/*	String machineRecipeName =   MESRecipeServiceProxy.getRecipeServiceUtil().getINDPMachineRecipe(productData.getFactoryName(), productData.getProductSpecName(),
				productData.getProcessFlowName(), productData.getProcessOperationName(), machineName, productData.getUdfs().get("ECCODE"), productData.getUdfs().get("PORTNAME"));*/
		String machineRecipeName =  MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(productData.getFactoryName(), productData.getProductSpecName(),
				productData.getProcessFlowName(), productData.getProcessOperationName(), machineName, productData.getUdfs().get("ECCODE"));
		// Unit or SubUnit Spec
		MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(unitName);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT LINKKEY, PROCUNITLIST, PROCSUBUNITLIST "
				+ "FROM CT_PRODUCTPROCLOCATIONHIST "
				+ "WHERE FACTORYNAME=:FACTORYNAME"
				+ " AND PRODUCTNAME=:PRODUCTNAME"
				+ " AND PRODUCTSPECNAME=:PRODUCTSPECNAME"
				+ " AND PRODUCTSPECVERSION=:PRODUCTSPECVERSION"
				+ " AND PROCESSFLOWNAME=:PROCESSFLOWNAME"
				+ " AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME"
				+ " AND MACHINENAME=:MACHINENAME" + " AND LINKKEY=:LINKKEY");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", productData.getFactoryName());
		args.put("PRODUCTNAME", productData.getKey().getProductName());
		args.put("PRODUCTSPECNAME", productData.getProductSpecName());
		args.put("PRODUCTSPECVERSION", productData.getProductSpecVersion());
		args.put("PROCESSFLOWNAME", productData.getProcessFlowName());
		args.put("PROCESSOPERATIONNAME", productData.getProcessOperationName());
		args.put("MACHINENAME", machineName);
		args.put("LINKKEY", productData.getUdfs().get("LINKKEY").toString());
		List<ListOrderedMap> histList = greenFrameServiceProxy.getSqlTemplate().queryForList(sb.toString(), args);
		
		if (histList.size() > 0) {
			String procunitList = ConvertUtil.getMapValueByName(histList.get(0), "PROCUNITLIST");
			String procSubUnitList = ConvertUtil.getMapValueByName(histList.get(0), "PROCSUBUNITLIST");
			if (machineSpec.getDetailMachineType().equals("UNIT")) 
			{
				if (StringUtil.isEmpty(procunitList)) {
					procunitList = unitName;
				} else {
					procunitList = procunitList + "," + unitName;
				}
			} else {
				if (StringUtil.isEmpty(procSubUnitList)) {
					procSubUnitList = unitName;
				} else {
					procSubUnitList = procSubUnitList + "," + unitName;
				}
			}

			sb = new StringBuilder();
			sb.append("UPDATE CT_PRODUCTPROCLOCATIONHIST SET PROCUNITLIST=:PROCUNITLIST "
					+ ", PROCSUBUNITLIST=:PROCSUBUNITLIST, PROCESSTIME=:PROCESSTIME "
					+ "WHERE FACTORYNAME=:FACTORYNAME"
					+ " AND PRODUCTNAME=:PRODUCTNAME"
					+ " AND PRODUCTSPECNAME=:PRODUCTSPECNAME"
					+ " AND PRODUCTSPECVERSION=:PRODUCTSPECVERSION"
					+ " AND PROCESSFLOWNAME=:PROCESSFLOWNAME"
					+ " AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME"
					+ " AND MACHINENAME=:MACHINENAME" + " AND LINKKEY=:LINKKEY");
			args.put("LINKKEY", histList.get(0).get("LINKKEY").toString());
			args.put("PROCUNITLIST", procunitList);
			args.put("PROCSUBUNITLIST", procSubUnitList);
			args.put("PROCESSTIME", productData.getLastIdleTime());
			greenFrameServiceProxy.getSqlTemplate().update(sb.toString(), args);

		} else {
			sb = new StringBuilder();
			sb.append("INSERT INTO CT_PRODUCTPROCLOCATIONHIST  ");
			sb.append("(LINKKEY,FACTORYNAME,PRODUCTNAME,PRODUCTSPECNAME,PRODUCTSPECVERSION,PROCESSFLOWNAME,PROCESSOPERATIONNAME,MACHINENAME,RECIPENAME,PROCUNITLIST,TRACKINLOTNAME,EVENTTIMEKEY,PROCSUBUNITLIST, PROCESSTIME) ");
			sb.append("VALUES  ");
			sb.append("(:LINKKEY,:FACTORYNAME,:PRODUCTNAME,:PRODUCTSPECNAME,:PRODUCTSPECVERSION,:PROCESSFLOWNAME,:PROCESSOPERATIONNAME,:MACHINENAME,:RECIPENAME,:PROCUNITLIST,:TRACKINLOTNAME,:EVENTTIMEKEY,:PROCSUBUNITLIST, :PROCESSTIME) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LINKKEY", productData.getUdfs().get("LINKKEY")
					.toString());
			bindMap.put("FACTORYNAME", productData.getFactoryName());
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			bindMap.put("PRODUCTSPECNAME", productData.getProductSpecName());
			bindMap.put("PRODUCTSPECVERSION",
					productData.getProductSpecVersion());
			bindMap.put("PROCESSFLOWNAME", productData.getProcessFlowName());
			bindMap.put("PROCESSOPERATIONNAME",
					productData.getProcessOperationName());
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("RECIPENAME", machineRecipeName);
			bindMap.put("PROCESSTIME",  productData.getLastIdleTime());
			if (machineSpec.getDetailMachineType().equals("UNIT")) {
				bindMap.put("PROCUNITLIST", unitName);
				bindMap.put("PROCSUBUNITLIST", "");
			} else {
				bindMap.put("PROCUNITLIST", "");
				bindMap.put("PROCSUBUNITLIST", unitName);
			}
			bindMap.put("TRACKINLOTNAME", productData.getLotName());
			bindMap.put("EVENTTIMEKEY", eventInfo.getEventTimeKey());

			greenFrameServiceProxy.getSqlTemplate().update(sb.toString(), bindMap);
		}

	}
	
	public void ExitedCancelFabQTime(EventInfo eventInfo, Lot lotData, String toEventName, String factoryName) throws CustomException
	{
		eventInfo.setEventName("ExitedCancel");
		
		try
		{
			String strSql = "SELECT PQ.PRODUCTNAME,      " +
					"       PQ.FACTORYNAME,      " +
					"       PQ.PROCESSFLOWNAME,      " +
					"       PQ.PROCESSFLOWVERSION,      " +
					"       PQ.PROCESSOPERATIONNAME,      " +
					"       PQ.PROCESSOPERATIONVERSION,    " +
					"       PQ.TOFACTORYNAME,    " +
					"       PQ.TOPROCESSFLOWNAME,    " +
					"       PQ.TOPROCESSFLOWVERSION,    " +
					"       PQ.TOPROCESSOPERATIONNAME,    " +
					"       PQ.TOPROCESSOPERATIONVERSION    " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P     " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME      " +
					"       AND PQ.FACTORYNAME = :FACTORYNAME     " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))         " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))       " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME     " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME     " +
					"       AND PQ.QUEUETIMESTATE = :QUEUETIMESTATE " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME   ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("TOFACTORYNAME", lotData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", lotData.getProcessOperationName());
			bindMap.put("PRODUCTNAME", lotData.getKey().getLotName());
			bindMap.put("QUEUETIMESTATE", "Exited");
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				
				String updateSql = "UPDATE CT_PRODUCTQUEUETIME   " +
						"   SET QUEUETIMESTATE = 'Entered',   " +
						"       EXITTIME = NULL, " +
						"       WARNINGTIME = NULL, " +
						"       INTERLOCKTIME = NULL, " +
						"       RESOLVETIME = NULL, " +
						"       RESOLVEUSER = NULL,   " +
						"       LASTEVENTUSER = :LASTEVENTUSER,   " +
						"       LASTEVENTTIME = :LASTEVENTTIME,   " +
						"       LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY,   " +
						"       LASTEVENTNAME = :LASTEVENTNAME,  " +
						"       LASTEVENTCOMMENT = :LASTEVENTCOMMENT   " +
						" WHERE     PRODUCTNAME = :PRODUCTNAME   " +
						"       AND FACTORYNAME = :FACTORYNAME   " +
						"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME   " +
						"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION   " +
						"       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME   " +
						"       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION   " +
						"       AND TOFACTORYNAME = :TOFACTORYNAME   " +
						"       AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME   " +
						"       AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION   " +
						"       AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME   " +
						"       AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION   " ;

				Map<String, Object> productQueueTime = new HashMap<String, Object>();
				productQueueTime.put("LASTEVENTUSER", eventInfo.getEventUser());
				productQueueTime.put("LASTEVENTTIME", eventInfo.getEventTime());
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTime.put("LASTEVENTNAME", eventInfo.getEventName());
				productQueueTime.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
				productQueueTime.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTime.put("FACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTime.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTime.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTime.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTime.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTime.put("TOFACTORYNAME", factoryName);
				productQueueTime.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTime.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTime.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTime.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				
				GenericServiceProxy.getSqlMesTemplate().update(updateSql, productQueueTime);
				
				String insertHistSql = "INSERT INTO CT_PRODUCTQUEUETIMEHIST  " +
						"   SELECT PRODUCTNAME,  " +
						"          :TIMEKEY,  " +
						"          FACTORYNAME,  " +
						"          PROCESSFLOWNAME,  " +
						"          PROCESSFLOWVERSION,  " +
						"          PROCESSOPERATIONNAME,  " +
						"          PROCESSOPERATIONVERSION,  " +
						"          TOFACTORYNAME,  " +
						"          TOPROCESSFLOWNAME,  " +
						"          TOPROCESSFLOWVERSION,  " +
						"          TOPROCESSOPERATIONNAME,  " +
						"          TOPROCESSOPERATIONVERSION,  " +
						"          TOEVENTNAME,  " +
						"          QUEUETIMETYPE,  " +
						"          WARNINGDURATIONLIMIT,  " +
						"          INTERLOCKDURATIONLIMIT,  " +
						"          GENERATIONTYPE,  " +
						"          DEPARTMENTNAME,  " +
						"          'Entered',  " +
						"          ENTERTIME,  " +
						"          NULL,  " +
						"          NULL,  " +
						"          NULL,  " +
						"          NULL,  " +
						"          NULL,  " +
						"          :EVENTUSER,  " +
						"          :EVENTTIME,  " +
						"          :EVENTNAME,  " +
						"          :EVENTCOMMENT  " +
						"     FROM CT_PRODUCTQUEUETIME  " +
						"    WHERE     PRODUCTNAME = :PRODUCTNAME  " +
						"          AND FACTORYNAME = :FACTORYNAME  " +
						"          AND PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
						"          AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
						"          AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  " +
						"          AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION  " +
						"          AND TOFACTORYNAME = :TOFACTORYNAME  " +
						"          AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME  " +
						"          AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION  " +
						"          AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME  " +
						"          AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION  ";
				
				Map<String, Object> productQueueTimeHist = new HashMap<String, Object>();
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTimeHist.put("TIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTimeHist.put("TIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTimeHist.put("EVENTUSER", eventInfo.getEventUser());
				productQueueTimeHist.put("EVENTTIME", eventInfo.getEventTime());
				productQueueTimeHist.put("EVENTNAME", eventInfo.getEventName());
				productQueueTimeHist.put("EVENTCOMMENT", eventInfo.getEventComment());
				productQueueTimeHist.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTimeHist.put("FACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTimeHist.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTimeHist.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTimeHist.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTimeHist.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTimeHist.put("TOFACTORYNAME", factoryName);
				productQueueTimeHist.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTimeHist.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTimeHist.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTimeHist.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				
				GenericServiceProxy.getSqlMesTemplate().update(insertHistSql, productQueueTimeHist);
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	
	
	/**
	 * @author smkang
	 * @since 2018.08.15
	 * @param productName
	 * @param reasonCode
	 * @param department
	 * @param eventInfo
	 * @return MultiHoldProductCount
	 * @throws CustomException
	 * @see According to user's requirement, ProductName/ReasonCode/Department/EventComment are necessary to be keys.
	 */
	public int removeMultiHoldProduct(String productName, String reasonCode, String department, EventInfo eventInfo) throws CustomException {
		try {

			if(StringUtil.isEmpty(department))
			{
				department = " ";
			}

			try {
				ProductMultiHold productMultiHoldData = ExtendedObjectProxy.getProductMultiHoldService().selectByKey(false, new Object[] {productName, reasonCode, department, eventInfo.getEventComment()});
				ExtendedObjectProxy.getProductMultiHoldService().remove(eventInfo, productMultiHoldData);
			} catch (Exception e) {
				log.info("Not exist ProductMultiHold : ProductName :"+productName+" ReasonCode :"+reasonCode+" Department :"+department+ " EventComment :"+eventInfo.getEventComment());
			}
			
			List<ProductMultiHold> productMultiHoldList = ExtendedObjectProxy.getProductMultiHoldService().select("PRODUCTNAME = ?", new Object[] {productName});
			
			return (productMultiHoldList != null && productMultiHoldList.size() > 0) ? productMultiHoldList.size() : 0;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public void checkFabQTime(String lotName, String factoryName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{

		String strSql = "SELECT PQ.PRODUCTNAME" +
						"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P" +
						" WHERE PQ.PRODUCTNAME = P.PRODUCTNAME" +
						"   AND P.LOTNAME = :LOTNAME" +
						"   AND P.FACTORYNAME = :FACTORYNAME" +
						"   AND PQ.QUEUETIMESTATE = :QUEUETIMESTATE" +
						"   AND PQ.TOFACTORYNAME = :TOFACTORYNAME" ;
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("QUEUETIMESTATE", GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);
		bindMap.put("TOFACTORYNAME", "OLED");
		
		List<Map<String, Object>> checkQTimeData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(checkQTimeData != null && checkQTimeData.size() > 0)
		{
			throw new CustomException("QUEUE-0005", "");
		}
	}
	
	public void ExitedFabQTime(EventInfo eventInfo, Lot lotData, String toEventName, String factoryName) throws CustomException
	{
		eventInfo.setEventName("Exited");
		
		try
		{
			//2018.09.10 dmlee : Cross Fab Q-time No Oper
			String processOperationName = lotData.getProcessOperationName();
			if(StringUtil.isEmpty(processOperationName))
			{
				processOperationName = "-";
			}
			//------------------------------------------
			
			String strSql = "SELECT PQ.PRODUCTNAME,       " +
					"       PQ.FACTORYNAME,       " +
					"       PQ.PROCESSFLOWNAME,       " +
					"       PQ.PROCESSFLOWVERSION,       " +
					"       PQ.PROCESSOPERATIONNAME,       " +
					"       PQ.PROCESSOPERATIONVERSION,     " +
					"       PQ.TOFACTORYNAME,     " +
					"       PQ.TOPROCESSFLOWNAME,     " +
					"       PQ.TOPROCESSFLOWVERSION,     " +
					"       PQ.TOPROCESSOPERATIONNAME,     " +
					"       PQ.TOPROCESSOPERATIONVERSION,   " +
					"       PQ.TOEVENTNAME, " +
					"       PQ.LASTEVENTTIMEKEY,     " +
					"       PQ.LOTNAME,     " +
					"       PQ.CARRIERNAME,     " +
					"       PQ.PRODUCTREQUESTNAME     " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P      " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME       " +
					"       AND PQ.FACTORYNAME = :FACTORYNAME    " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))    " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))    " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME      " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME   " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME   ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("TOFACTORYNAME", lotData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PRODUCTNAME", lotData.getKey().getLotName());
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				String updateSql = "UPDATE CT_PRODUCTQUEUETIME  " +
						"   SET QUEUETIMESTATE = 'Exited',  " +
						"       EXITTIME = :EXITTIME,  " +
						"       LASTEVENTUSER = :LASTEVENTUSER,  " +
						"       LASTEVENTTIME = :LASTEVENTTIME,  " +
						"       LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY,  " +
						"       LASTEVENTNAME = :LASTEVENTNAME, " +
						"       LASTEVENTCOMMENT = :LASTEVENTCOMMENT  " +
						" WHERE     PRODUCTNAME = :PRODUCTNAME  " +
						"       AND FACTORYNAME = :FACTORYNAME  " +
						"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
						"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
						"       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  " +
						"       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION  " +
						"       AND TOFACTORYNAME = :TOFACTORYNAME  " +
						"       AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME  " +
						"       AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION  " +
						"       AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME  " +
						"       AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION  " ;

				Map<String, Object> productQueueTime = new HashMap<String, Object>();
				productQueueTime.put("EXITTIME", eventInfo.getEventTime());
				productQueueTime.put("LASTEVENTUSER", eventInfo.getEventUser());
				productQueueTime.put("LASTEVENTTIME", eventInfo.getEventTime());
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTime.put("LASTEVENTNAME", eventInfo.getEventName());
				productQueueTime.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
				productQueueTime.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTime.put("FACTORYNAME", qTimeList.get(i).get("FACTORYNAME"));
				productQueueTime.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTime.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTime.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTime.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTime.put("TOFACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTime.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTime.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTime.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTime.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				
				GenericServiceProxy.getSqlMesTemplate().update(updateSql, productQueueTime);
				
				String insertHistSql = "INSERT INTO CT_PRODUCTQUEUETIMEHIST " +
						"   SELECT PRODUCTNAME, " +
						"          :TIMEKEY, " +
						"          FACTORYNAME, " +
						"          PROCESSFLOWNAME, " +
						"          PROCESSFLOWVERSION, " +
						"          PROCESSOPERATIONNAME, " +
						"          PROCESSOPERATIONVERSION, " +
						"          TOFACTORYNAME, " +
						"          TOPROCESSFLOWNAME, " +
						"          TOPROCESSFLOWVERSION, " +
						"          TOPROCESSOPERATIONNAME, " +
						"          TOPROCESSOPERATIONVERSION, " +
						"          TOEVENTNAME, " +
						"          QUEUETIMETYPE, " +
						"          WARNINGDURATIONLIMIT, " +
						"          INTERLOCKDURATIONLIMIT, " +
						"          GENERATIONTYPE, " +
						"          DEPARTMENTNAME, " +
						"          'Exited', " +
						"          ENTERTIME, " +
						"          :EXITTIME, " +
						"          WARNINGTIME, " +
						"          INTERLOCKTIME, " +
						"          RESOLVETIME, " +
						"          RESOLVEUSER, " +
						"          :EVENTUSER, " +
						"          :EVENTTIME, " +
						"          :EVENTNAME, " +
						"          :EVENTCOMMENT, " +
						"          :LOTNAME, " +
						"          :CARRIERNAME, " +
						"          :PRODUCTREQUESTNAME " +
						"     FROM CT_PRODUCTQUEUETIME " +
						"    WHERE     PRODUCTNAME = :PRODUCTNAME " +
						"          AND FACTORYNAME = :FACTORYNAME " +
						"          AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
						"          AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
						"          AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
						"          AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
						"          AND TOFACTORYNAME = :TOFACTORYNAME " +
						"          AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME " +
						"          AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION " +
						"          AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME " +
						"          AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
				
				Map<String, Object> productQueueTimeHist = new HashMap<String, Object>();
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTimeHist.put("TIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTimeHist.put("TIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTimeHist.put("EXITTIME", eventInfo.getEventTime());
				productQueueTimeHist.put("EVENTUSER", eventInfo.getEventUser());
				productQueueTimeHist.put("EVENTTIME", eventInfo.getEventTime());
				productQueueTimeHist.put("EVENTNAME", eventInfo.getEventName());
				productQueueTimeHist.put("EVENTCOMMENT", eventInfo.getEventComment());
				productQueueTimeHist.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTimeHist.put("FACTORYNAME", qTimeList.get(i).get("FACTORYNAME"));
				productQueueTimeHist.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTimeHist.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTimeHist.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTimeHist.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTimeHist.put("TOFACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTimeHist.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTimeHist.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTimeHist.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTimeHist.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				productQueueTimeHist.put("LOTNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("LOTNAME"))?"":(String)qTimeList.get(i).get("LOTNAME"));
				productQueueTimeHist.put("CARRIERNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("CARRIERNAME"))?"":(String)qTimeList.get(i).get("CARRIERNAME"));
				productQueueTimeHist.put("PRODUCTREQUESTNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("PRODUCTREQUESTNAME"))?"":(String)qTimeList.get(i).get("PRODUCTREQUESTNAME"));

				GenericServiceProxy.getSqlMesTemplate().update(insertHistSql, productQueueTimeHist);
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	public void ExitedFabQTime(EventInfo eventInfo, Product productData, String toEventName, String factoryName) throws CustomException
	{
		eventInfo.setEventName("Exited");
		
		try
		{
			//2018.09.10 dmlee : Cross Fab Q-time No Oper
			String processOperationName = productData.getProcessOperationName();
			if(StringUtil.isEmpty(processOperationName))
			{
				processOperationName = "-";
			}
			//------------------------------------------
			
			String strSql = "SELECT PQ.PRODUCTNAME,       " +
					"       PQ.FACTORYNAME,       " +
					"       PQ.PROCESSFLOWNAME,       " +
					"       PQ.PROCESSFLOWVERSION,       " +
					"       PQ.PROCESSOPERATIONNAME,       " +
					"       PQ.PROCESSOPERATIONVERSION,     " +
					"       PQ.TOFACTORYNAME,     " +
					"       PQ.TOPROCESSFLOWNAME,     " +
					"       PQ.TOPROCESSFLOWVERSION,     " +
					"       PQ.TOPROCESSOPERATIONNAME,     " +
					"       PQ.TOPROCESSOPERATIONVERSION,   " +
					"       PQ.TOEVENTNAME, " +
					"       PQ.LASTEVENTTIMEKEY,     " +
					"       PQ.LOTNAME,     " +
					"       PQ.CARRIERNAME,     " +
					"       PQ.PRODUCTREQUESTNAME     " +
					"  FROM CT_PRODUCTQUEUETIME PQ, PRODUCT P      " +
					" WHERE     PQ.TOFACTORYNAME = :TOFACTORYNAME       " +
					"       AND PQ.FACTORYNAME = :FACTORYNAME    " +
					"       AND ((PQ.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME) OR (PQ.TOPROCESSFLOWNAME = '*'))    " +
					"       AND ((PQ.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME) OR (PQ.TOPROCESSOPERATIONNAME = '*'))    " +
					"       AND PQ.PRODUCTNAME = P.PRODUCTNAME      " +
					"       AND P.PRODUCTNAME = :PRODUCTNAME   " +
					"       AND PQ.TOEVENTNAME = :TOEVENTNAME   ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("TOFACTORYNAME", productData.getFactoryName());
			bindMap.put("TOPROCESSFLOWNAME", productData.getProcessFlowName());
			bindMap.put("TOPROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			bindMap.put("TOEVENTNAME", toEventName);

			List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < qTimeList.size(); i++)
			{
				String updateSql = "UPDATE CT_PRODUCTQUEUETIME  " +
						"   SET QUEUETIMESTATE = 'Exited',  " +
						"       EXITTIME = :EXITTIME,  " +
						"       LASTEVENTUSER = :LASTEVENTUSER,  " +
						"       LASTEVENTTIME = :LASTEVENTTIME,  " +
						"       LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY,  " +
						"       LASTEVENTNAME = :LASTEVENTNAME, " +
						"       LASTEVENTCOMMENT = :LASTEVENTCOMMENT  " +
						" WHERE     PRODUCTNAME = :PRODUCTNAME  " +
						"       AND FACTORYNAME = :FACTORYNAME  " +
						"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
						"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION  " +
						"       AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  " +
						"       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION  " +
						"       AND TOFACTORYNAME = :TOFACTORYNAME  " +
						"       AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME  " +
						"       AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION  " +
						"       AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME  " +
						"       AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION  " ;

				Map<String, Object> productQueueTime = new HashMap<String, Object>();
				productQueueTime.put("EXITTIME", eventInfo.getEventTime());
				productQueueTime.put("LASTEVENTUSER", eventInfo.getEventUser());
				productQueueTime.put("LASTEVENTTIME", eventInfo.getEventTime());
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTime.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTime.put("LASTEVENTNAME", eventInfo.getEventName());
				productQueueTime.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
				productQueueTime.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTime.put("FACTORYNAME", qTimeList.get(i).get("FACTORYNAME"));
				productQueueTime.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTime.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTime.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTime.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTime.put("TOFACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTime.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTime.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTime.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTime.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				
				GenericServiceProxy.getSqlMesTemplate().update(updateSql, productQueueTime);
				
				String insertHistSql = "INSERT INTO CT_PRODUCTQUEUETIMEHIST " +
						"   SELECT PRODUCTNAME, " +
						"          :TIMEKEY, " +
						"          FACTORYNAME, " +
						"          PROCESSFLOWNAME, " +
						"          PROCESSFLOWVERSION, " +
						"          PROCESSOPERATIONNAME, " +
						"          PROCESSOPERATIONVERSION, " +
						"          TOFACTORYNAME, " +
						"          TOPROCESSFLOWNAME, " +
						"          TOPROCESSFLOWVERSION, " +
						"          TOPROCESSOPERATIONNAME, " +
						"          TOPROCESSOPERATIONVERSION, " +
						"          TOEVENTNAME, " +
						"          QUEUETIMETYPE, " +
						"          WARNINGDURATIONLIMIT, " +
						"          INTERLOCKDURATIONLIMIT, " +
						"          GENERATIONTYPE, " +
						"          DEPARTMENTNAME, " +
						"          'Exited', " +
						"          ENTERTIME, " +
						"          :EXITTIME, " +
						"          WARNINGTIME, " +
						"          INTERLOCKTIME, " +
						"          RESOLVETIME, " +
						"          RESOLVEUSER, " +
						"          :EVENTUSER, " +
						"          :EVENTTIME, " +
						"          :EVENTNAME, " +
						"          :EVENTCOMMENT, " +
						"          :LOTNAME, " +
						"          :CARRIERNAME, " +
						"          :PRODUCTREQUESTNAME " +
						"     FROM CT_PRODUCTQUEUETIME " +
						"    WHERE     PRODUCTNAME = :PRODUCTNAME " +
						"          AND FACTORYNAME = :FACTORYNAME " +
						"          AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
						"          AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
						"          AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
						"          AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
						"          AND TOFACTORYNAME = :TOFACTORYNAME " +
						"          AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME " +
						"          AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION " +
						"          AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME " +
						"          AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
				
				Map<String, Object> productQueueTimeHist = new HashMap<String, Object>();
				
				// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//				productQueueTimeHist.put("TIMEKEY", eventInfo.getLastEventTimekey());
				productQueueTimeHist.put("TIMEKEY", eventInfo.getEventTimeKey());
				
				productQueueTimeHist.put("EXITTIME", eventInfo.getEventTime());
				productQueueTimeHist.put("EVENTUSER", eventInfo.getEventUser());
				productQueueTimeHist.put("EVENTTIME", eventInfo.getEventTime());
				productQueueTimeHist.put("EVENTNAME", eventInfo.getEventName());
				productQueueTimeHist.put("EVENTCOMMENT", eventInfo.getEventComment());
				productQueueTimeHist.put("PRODUCTNAME", qTimeList.get(i).get("PRODUCTNAME"));
				productQueueTimeHist.put("FACTORYNAME", qTimeList.get(i).get("FACTORYNAME"));
				productQueueTimeHist.put("PROCESSFLOWNAME", qTimeList.get(i).get("PROCESSFLOWNAME"));
				productQueueTimeHist.put("PROCESSFLOWVERSION", qTimeList.get(i).get("PROCESSFLOWVERSION"));
				productQueueTimeHist.put("PROCESSOPERATIONNAME", qTimeList.get(i).get("PROCESSOPERATIONNAME"));
				productQueueTimeHist.put("PROCESSOPERATIONVERSION", qTimeList.get(i).get("PROCESSOPERATIONVERSION"));
				productQueueTimeHist.put("TOFACTORYNAME", qTimeList.get(i).get("TOFACTORYNAME"));
				productQueueTimeHist.put("TOPROCESSFLOWNAME", qTimeList.get(i).get("TOPROCESSFLOWNAME"));
				productQueueTimeHist.put("TOPROCESSFLOWVERSION", qTimeList.get(i).get("TOPROCESSFLOWVERSION"));
				productQueueTimeHist.put("TOPROCESSOPERATIONNAME", qTimeList.get(i).get("TOPROCESSOPERATIONNAME"));
				productQueueTimeHist.put("TOPROCESSOPERATIONVERSION", qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"));
				productQueueTimeHist.put("LOTNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("LOTNAME"))?"":(String)qTimeList.get(i).get("LOTNAME"));
				productQueueTimeHist.put("CARRIERNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("CARRIERNAME"))?"":(String)qTimeList.get(i).get("CARRIERNAME"));
				productQueueTimeHist.put("PRODUCTREQUESTNAME", StringUtils.isEmpty((String)qTimeList.get(i).get("PRODUCTREQUESTNAME"))?"":(String)qTimeList.get(i).get("PRODUCTREQUESTNAME"));

				GenericServiceProxy.getSqlMesTemplate().update(insertHistSql, productQueueTimeHist);
			}
		}
		catch (Exception ex)
		{
		}
	}
}