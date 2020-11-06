package kr.co.aim.messolution.consumable.service;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MergeInfo;
import kr.co.aim.greentrack.consumable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.consumable.management.info.SplitInfo;
import kr.co.aim.greentrack.consumable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.16
 */

public class ConsumableServiceImpl implements ApplicationContextAware
{
	/**
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog(ConsumableServiceImpl.class);				
	
	/**
	 * @param arg0
	 * @throws BeansException
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}
	
	//--------------------------------------------------------------------------------------------------------------------------
	// Start DT
	//--------------------------------------------------------------------------------------------------------------------------
  
	/*
	* Name : aassignTransportGroup
	* Desc : This function is Green Track API Call ConsumableassignTransportGroup
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void assignTransportGroup( Consumable consumableData, 
			AssignTransportGroupInfo assignTransportGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().assignTransportGroup(consumableData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	/*
	* Name : changeSpec
	* Desc : This function is Green Track API Call ConsumablechangeSpec
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void changeSpec( Consumable consumableData, 
							ChangeSpecInfo changeSpecInfo,
							EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().changeSpec(consumableData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/**
	 * create Consumable
	 * @param consumableName
	 * @param createInfo
	 * @param eventInfo
	 * @throws InvalidStateTransitionSignal
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws DuplicateNameSignal
	 */
	public void createCrate(EventInfo eventInfo, String consumableName, CreateInfo createInfo)
		throws CustomException
	{		
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);
			
			ConsumableServiceProxy.getConsumableService().create(consumableKey, eventInfo, createInfo);
			
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableName);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableName);
		}
	}
	
	/*
	* Name : deaassignTransportGroup
	* Desc : This function is Green Track API Call ConsumabledeassignTransportGroup
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void deassignTransportGroup( Consumable consumableData, 
										DeassignTransportGroupInfo deassignTransportGroupInfo,
										EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().deassignTransportGroup(consumableData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/**
	 * decrease QTY for consumable
	 * @author swcho
	 * @since 2014.05.08
	 * @param consumableData
	 * @param decrementQuantityInfo
	 * @param eventInfo
	 * @throws InvalidStateTransitionSignal
	 * @throws FrameworkErrorSignal
	 * @throws NotFoundSignal
	 * @throws DuplicateNameSignal
	 */
	public void decrementQuantity( Consumable consumableData, 
									DecrementQuantityInfo decrementQuantityInfo,
									EventInfo eventInfo	)
		throws CustomException
	{
		try
		{
			eventInfo.setBehaviorName("ARRAY");
			ConsumableServiceProxy.getConsumableService().decrementQuantity(consumableData.getKey(), eventInfo, decrementQuantityInfo);
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableData.getKey().getConsumableName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableData.getKey().getConsumableName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableData.getKey().getConsumableName());
		}
	}
	
	/**
	 * increase QTY for consumable
	 * @author swcho
	 * @since 2014.05.13
	 * @param consumableData
	 * @param incrementQuantityInfo
	 * @param eventInfo
	 * @throws CustomException
	 */
	public void incrementQuantity( Consumable consumableData, 
								   IncrementQuantityInfo incrementQuantityInfo,
								   EventInfo eventInfo	)
		throws CustomException
	{		
		try
		{
			ConsumableServiceProxy.getConsumableService().incrementQuantity(consumableData.getKey(), eventInfo, incrementQuantityInfo);
			
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableData.getKey().getConsumableName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableData.getKey().getConsumableName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableData.getKey().getConsumableName());
		}
	}
	/*
	* Name : makeAvailable
	* Desc : This function is Green Track API Call ConsumablemakeAvailable
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void makeAvailable( Consumable consumableData, 
								MakeAvailableInfo makeAvailableInfo,
								EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().makeAvailable(consumableData.getKey(), eventInfo, makeAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	/*
	* Name : makeNotAvailable
	* Desc : This function is Green Track API Call ConsumablemakeNotAvailable
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void makeNotAvailable( Consumable consumableData, 
								  MakeNotAvailableInfo makeNotAvailableInfo,
								  EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().makeNotAvailable(consumableData.getKey(), eventInfo, makeNotAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	/*
	* Name : merge
	* Desc : This function is Green Track API Call Consumablemerge
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void merge( Consumable consumableData, 
					   MergeInfo mergeInfo,
					   EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
			ConsumableServiceProxy.getConsumableService().merge(consumableData.getKey(), eventInfo, mergeInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	/*
	* Name : setArea
	* Desc : This function is Green Track API Call ConsumablesetArea
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void setArea( Consumable consumableData, 
						 SetAreaInfo setAreaInfo,
						 EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().setArea(consumableData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/*
	* Name : setEvent
	* Desc : This function is Green Track API Call ConsumablesetEvent
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void setEvent( String consumableName, 
						  SetEventInfo setEventInfo,
						  EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(consumableName);
		
		ConsumableServiceProxy.getConsumableService().setEvent(consumableKey, eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	/*
	* Name : setMaterialLocation
	* Desc : This function is Green Track API Call ConsumablesetMaterialLocation
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void setMaterialLocation( Consumable consumableData, 
									 SetMaterialLocationInfo setMaterialLocationInfo,
									 EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
			ConsumableServiceProxy.getConsumableService().setMaterialLocation(consumableData.getKey(), eventInfo, setMaterialLocationInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}		
	/*
	* Name : split
	* Desc : This function is Green Track API Call Consumable Split
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void split( Consumable consumableData, 
					   SplitInfo splitInfo,
					   EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
			ConsumableServiceProxy.getConsumableService().split(consumableData.getKey(), eventInfo, splitInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}		
	/*
	* Name : undo
	* Desc : This function is Green Track API Call Consumable undo
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public void undo( Consumable consumableData, 
					  UndoInfo undoInfo,
					  EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{		
		ConsumableServiceProxy.getConsumableService().undo(consumableData.getKey(), eventInfo, undoInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
}
