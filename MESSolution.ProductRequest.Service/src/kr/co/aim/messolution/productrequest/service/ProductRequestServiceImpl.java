package kr.co.aim.messolution.productrequest.service;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestHistory;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlanHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.DecrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;
import kr.co.aim.greentrack.productrequestplan.management.info.IncrementQuantityByInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.27
 */

public class ProductRequestServiceImpl implements ApplicationContextAware  {
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog("ProductRequestServiceImpl");
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
	throws BeansException
	{
		applicationContext = arg0;
	}

	/*
	* Name : create
	* Desc : This function is create
	* Author : AIM Systems, Inc
	* Date : 2013.05.16
	*/
	public void create(EventInfo eventInfo,
			           CreateInfo createInfo,
					   String productRequestName)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{	
		log.info("Execute ProductRequest create.");
		
		if(log.isInfoEnabled())
		{
			log.info("productRequestName = " + productRequestName);	
		}

		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfo);
		
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}
	
	/*
	* Name : create
	* Desc : This function is create
	* Author : AIM Systems, Inc
	* Date : 2013.05.16
	*/
	public void createPlan(EventInfo eventInfo,
			           kr.co.aim.greentrack.productrequestplan.management.info.CreateInfo createInfo,
					   String productRequestName, String assignedMachineName, Timestamp planReleasedTime)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{	
		log.info("Execute ProductRequestPlan create.");
		
		ProductRequestPlanKey planKey = new ProductRequestPlanKey(productRequestName, assignedMachineName, planReleasedTime);

		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().create(planKey, eventInfo, createInfo);
		
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData, eventInfo);
	}

	/*
	* Name : changeSpec
	* Desc : This function is greenTrack API Call changeSpec
	* Author : AIM Systems, Inc
	* Date : 2013.05.20
	*/
	public ProductRequest changeSpec(ProductRequest productRequestData, ChangeSpecInfo changeSpecInfo,
			EventInfo eventInfo, String productRequestName) throws Exception {
		
		log.info("Execute changeSpec");
		
		if(log.isInfoEnabled()){
			log.info("productRequestName = " + productRequestName);
		}
				
		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);
		
		ProductRequest requestData = ProductRequestServiceProxy.getProductRequestService().changeSpec(productRequestKey, eventInfo,
				changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey"
				+ eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(requestData, eventInfo);
		
		return requestData;
	}

	/*
	* Name : decrementScrappedQuantityBy
	* Desc : This function is greenTrack API Call decrementScrappedQuantityBy
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void decrementScrappedQuantityBy( ProductRequest productRequestData,
							  DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().decrementScrappedQuantityBy(productRequestData.getKey(), eventInfo, decrementScrappedQuantityByInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}

	/*
	* Name : incrementFinishedQuantityBy
	* Desc : This function is greenTrack API Call incrementFinishedQuantityBy
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void incrementFinishedQuantityBy( ProductRequest productRequestData,
			IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo,
			  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().incrementFinishedQuantityBy(productRequestData.getKey(), eventInfo, incrementFinishedQuantityByInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}

	/*
	* Name : incrementReleasedQuantityBy
	* Desc : This function is greenTrack API Call incrementReleasedQuantityBy
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void incrementReleasedQuantityBy( ProductRequest productRequestData,
											 IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo,
											 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().incrementReleasedQuantityBy(productRequestData.getKey(), eventInfo, incrementReleasedQuantityByInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}
	
	/*
	* Name : incrementPlanReleasedQuantityBy
	* Desc : This function is greenTrack API Call incrementPlanReleasedQuantityBy
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void incrementPlanQuantityBy( ProductRequestPlan productRequestPlanData,
											 IncrementQuantityByInfo incrementQuantityByInfo,
											 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().incrementQuantityBy(productRequestPlanData.getKey(), eventInfo, incrementQuantityByInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData, eventInfo);
	}

	/*
	* Name : incrementScrappedQuantityBy
	* Desc : This function is greenTrack API Call incrementScrappedQuantityBy
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void incrementScrappedQuantityBy( ProductRequest productRequestData,
											 IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo,
											 EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().incrementScrappedQuantityBy(productRequestData.getKey(), eventInfo, incrementScrappedQuantityByInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}

	/*
	* Name : makeCompleted
	* Desc : This function is greenTrack API Call makeCompleted
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeCompleted( ProductRequest productRequestData,
							   MakeCompletedInfo makeCompletedInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().makeCompleted(productRequestData.getKey(), eventInfo, makeCompletedInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}
	
	/*
	* Name : makePlanCompleted
	* Desc : This function is greenTrack API Call makePlanCompleted
	* Author : dmlee
	* Date : 2018.03.30
	*/
	public void makeCompletedPlan( ProductRequestPlan productRequestPlanData,
							   kr.co.aim.greentrack.productrequestplan.management.info.MakeCompletedInfo makeCompletedInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		
		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().makeCompleted(productRequestPlanData.getKey(), eventInfo, makeCompletedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData, eventInfo);
	}
	
	/*
	* Name : makeFinished
	* Desc : This function is update Data makeFinished (Request EDO)
	* Author : dmlee
	* Date : 2011.01.23
	*/
	public void makeFinished( ProductRequest productRequestData,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Finished);
		ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
		
		ProductRequest resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestData.getKey().getProductRequestName());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}

	/*
	* Name : makeNotOnHold
	* Desc : This function is greenTrack API Call makeNotOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeNotOnHold( ProductRequest productRequestData,
							   MakeNotOnHoldInfo makeNotOnHoldInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		String oldState = productRequestData.getProductRequestState();
		
		if(!productRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released))
		{
			productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
			ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
		}
		
		eventInfo.setEventName("ReleasedHold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().makeNotOnHold(productRequestData.getKey(), eventInfo, makeNotOnHoldInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//2018.03.30 dmlee : Case Work Order state 'Planned'
		if(resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released))
		{
			resultData.setProductRequestState(oldState);
			ProductRequestServiceProxy.getProductRequestService().update(resultData);
		}
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
	}
	
	/*
	* Name : makeNotOnHoldPlan
	* Desc : This function is greenTrack API Call makeNotOnHoldPlan
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeNotOnHoldPlan( ProductRequestPlan productRequestPlanData,
							   kr.co.aim.greentrack.productrequestplan.management.info.MakeNotOnHoldInfo makeNotOnHoldInfo,
							   EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		Boolean plannedFlag = false;
		
		//2018.03.30 dmlee : Case Work Order state 'Planned'
		if(productRequestPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().Prq_Planned))
		{
			productRequestPlanData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Released);
			ProductRequestPlanServiceProxy.getProductRequestPlanService().update(productRequestPlanData);
			plannedFlag = true;
		}
		
		eventInfo.setEventName("ReleasedHold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().makeNotOnHold(productRequestPlanData.getKey(), eventInfo, makeNotOnHoldInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//2018.03.30 dmlee : Case Work Order state 'Planned'
		if(plannedFlag)
		{
			resultData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Planned);
			ProductRequestPlanServiceProxy.getProductRequestPlanService().update(resultData);
		}
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData, eventInfo);
	}

	/*
	* Name : makeOnHold
	* Desc : This function is greenTrack API Call makeOnHold
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeOnHold( ProductRequest productRequestData,
							MakeOnHoldInfo makeOnHoldInfo,
							EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		
		String oldState = productRequestData.getProductRequestState();
		
		if(!productRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed) && !productRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released) )
		{
			productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
			ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
		}
		
		eventInfo.setEventName("Hold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().makeOnHold(productRequestData.getKey(), eventInfo, makeOnHoldInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		
		if(!productRequestData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed) && resultData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Released))
		{
			resultData.setProductRequestState(oldState);
			ProductRequestServiceProxy.getProductRequestService().update(resultData);
		}
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
		
	}
	
	/*
	* Name : makeOnHoldPlan
	* Desc : This function is greenTrack API Call makeOnHoldPlan
	* Author : dmlee
	* Date : 2018.03.30
	*/
	public void makeOnHoldPlan( ProductRequestPlan productRequestPlanData,
							kr.co.aim.greentrack.productrequestplan.management.info.MakeOnHoldInfo makeOnHoldInfo,
							EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		Boolean plannedFlag = false;
		
		//2018.03.30 dmlee : Case Work Order state 'Planned'
		if(productRequestPlanData.getProductRequestPlanState().equals(GenericServiceProxy.getConstantMap().Prq_Planned))
		{
			productRequestPlanData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Released);
			ProductRequestPlanServiceProxy.getProductRequestPlanService().update(productRequestPlanData);
			plannedFlag = true;
		}
		
		eventInfo.setEventName("Hold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().makeOnHold(productRequestPlanData.getKey(), eventInfo, makeOnHoldInfo );
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		//2018.03.30 dmlee : Case Work Order state 'Planned'
		if(plannedFlag)
		{
			resultData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Planned);
			ProductRequestPlanServiceProxy.getProductRequestPlanService().update(resultData);
		}
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData, eventInfo);
		
	}

	/*
	* Name : makeReleased
	* Desc : This function is greenTrack API Call makeReleased
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeReleased( ProductRequest productRequestData,
							  MakeReleasedInfo makeReleasedInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{	
		productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
		ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
		
		ProductRequest resultData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestData.getKey().getProductRequestName());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData , eventInfo);
	}
	
	/*
	* Name : makeReleasedPlan
	* Desc : This function is greenTrack API Call makeReleasedPlan
	* Author : AIM Systems, Inc
	* Date : 2011.01.23
	*/
	public void makeReleasedPlan( ProductRequestPlan productRequestPlanData,
							  kr.co.aim.greentrack.productrequestplan.management.info.MakeReleasedInfo makeReleasedInfo,
							  EventInfo eventInfo )
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		
		productRequestPlanData.setProductRequestPlanState(GenericServiceProxy.getConstantMap().Prq_Released);
		ProductRequestPlanServiceProxy.getProductRequestPlanService().update(productRequestPlanData);
		
		ProductRequestPlan resultData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(productRequestPlanData.getKey());
		
		//addHistory
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addPlanHistory(resultData , eventInfo);
	}
	
	public void addHistory(ProductRequestHistory historyData) throws CustomException
	{
		ExtendedObjectProxy.getProductRequestHistoryService().insert(historyData);
	}
	
	public void addPlanHistory(ProductRequestPlanHistory historyData) throws CustomException
	{
		ExtendedObjectProxy.getProductRequestPlanHistoryService().insert(historyData);
	}
}