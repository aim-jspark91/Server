package kr.co.aim.messolution.consumable.service;

import java.sql.Timestamp;
import java.util.Map;

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
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
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

public class ConsumableInfoUtil implements ApplicationContextAware
{
	/**
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog(ConsumableInfoUtil.class);				
	
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
	* Name : assignTransportGroupInfo
	* Desc : This function is ConsumableData Assign TransportGroup
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/	
	public  AssignTransportGroupInfo assignTransportGroupInfo( Consumable consumableData, String transportGroupName)
	{
		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();
		assignTransportGroupInfo.setTransportGroupName(transportGroupName);
				
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		assignTransportGroupInfo.setUdfs(consumableUdfs);
		
		return assignTransportGroupInfo;
	}
	/*
	* Name : changeSpecInfo
	* Desc : This function is Make ChangeSpecInfo 
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public  ChangeSpecInfo changeSpecInfo( Consumable consumableData,String areaName, 
												 String consumableSpecName, String consumableSpecVersion, String factoryName )
	{
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setConsumableSpecName(consumableSpecName);
		changeSpecInfo.setConsumableSpecVersion(consumableSpecVersion);
		changeSpecInfo.setFactoryName(factoryName);
					 
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		changeSpecInfo.setUdfs(consumableUdfs);
		
		return changeSpecInfo;
	}
	
	
	/**
	 * create API info
	 * @author swcho
	 * @since 2014.05.13
	 * @param factoryName
	 * @param areaName
	 * @param consumableName
	 * @param consumableSpecName
	 * @param consumableSpecVersion
	 * @param consumableType
	 * @param quantity
	 * @param udfs
	 * @return
	 */
	public CreateInfo createInfo(String factoryName, String areaName, String consumableName, String consumableSpecName, String consumableSpecVersion,
								  String consumableType, double quantity, Map<String, String> udfs)
	{
		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName(areaName);
		createInfo.setConsumableName(consumableName);
		createInfo.setConsumableSpecName(consumableSpecName);
		createInfo.setConsumableSpecVersion(consumableSpecVersion);
		createInfo.setConsumableType(consumableType);
		createInfo.setFactoryName(factoryName);
		createInfo.setQuantity(quantity);
			
		createInfo.setUdfs(udfs);
		
		return createInfo;
	}
	
	/*
	* Name : deassignTransportGroupInfo
	* Desc : This function is ConsumableData Deassign TransportGroup
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public  DeassignTransportGroupInfo deassignTransportGroupInfo( Consumable consumableData)
	{
		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();
		
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		deassignTransportGroupInfo.setUdfs(consumableUdfs);
		
		return deassignTransportGroupInfo;
	}
	
	

	/**
	 * set to decrement quantity
	 * @author swcho
	 * @since 2014.05.08
	 * @param consumerLotName
	 * @param consumerPOName
	 * @param consumerPOVersion
	 * @param consumerProductName
	 * @param consumerTimeKey
	 * @param quantity
	 * @param udf
	 * @return
	 */
	public DecrementQuantityInfo decrementQuantityInfo(String consumerLotName, String consumerPOName, 
			 										   String consumerPOVersion, String consumerProductName, 
			 										   String consumerTimeKey ,double quantity,
			 										   Map udfs)
	{
		DecrementQuantityInfo decrementQuantityInfo = new DecrementQuantityInfo();
		decrementQuantityInfo.setConsumerLotName(consumerLotName);
		decrementQuantityInfo.setConsumerPOName(consumerPOName);
		decrementQuantityInfo.setConsumerPOVersion(consumerPOVersion);
		decrementQuantityInfo.setConsumerProductName(consumerProductName);
		decrementQuantityInfo.setConsumerTimeKey(consumerTimeKey);
		decrementQuantityInfo.setQuantity(quantity);
		
		Map<String,String> consumableUdfs = udfs;
		decrementQuantityInfo.setUdfs(consumableUdfs);
		
		return decrementQuantityInfo;
	}
	
	
	/**
	 * set to increase quantity
	 * @author swcho
	 * @since 2014.05.13
	 * @param quantity
	 * @param udfs
	 * @return
	 */
	public IncrementQuantityInfo incrementQuantityInfo(double quantity, Map<String, String> udfs)
	{
		IncrementQuantityInfo incrementQuantityInfo = new IncrementQuantityInfo();
		incrementQuantityInfo.setQuantity(quantity);
		
		Map<String,String> consumableUdfs = udfs;
		incrementQuantityInfo.setUdfs(consumableUdfs);
		
		return incrementQuantityInfo;
	}
	
	/*
	* Name : makeAvailableInfo
	* Desc : This function is Make MakeAvilableInfo Set UDF
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public MakeAvailableInfo makeAvailableInfo( Consumable consumableData )
	{
		MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
					
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		makeAvailableInfo.setUdfs(consumableUdfs);
		
		return makeAvailableInfo;
	}
	/*
	* Name : makeNotAvailableInfo
	* Desc : This function is Make MakeNotAvailableInfo Set UDF
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public MakeNotAvailableInfo makeNotAvailableInfo( Consumable consumableData )
	{
		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
		
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		makeNotAvailableInfo.setUdfs(consumableUdfs);
		
		return makeNotAvailableInfo;
	}
	/*
	* Name : mergeInfo
	* Desc : This function is Make MergeInfo 
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public MergeInfo mergeInfo( Consumable consumableData, String parentConsumableName, Map<String, String> parentConsumableUdfs )
	{
		MergeInfo mergeInfo = new MergeInfo();
		
		mergeInfo.setParentConsumableName(parentConsumableName);
		mergeInfo.setParentConsumableUdfs(parentConsumableUdfs);
				
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		mergeInfo.setUdfs(consumableUdfs);
		
		return mergeInfo;
	}
	/*
	* Name : setAreaInfo
	* Desc : This function is Make SetAreaInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public SetAreaInfo setAreaInfo( Consumable consumableData, String areaName )
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);
		
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		setAreaInfo.setUdfs(consumableUdfs);
		
		return setAreaInfo;
	}
	/*
	* Name : setEventInfo
	* Desc : This function is Make SetEventInfo Set UDF
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public SetEventInfo setEventInfo( Consumable consumableData, String areaName )
	{
		SetEventInfo setEventInfo = new SetEventInfo();
				
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		setEventInfo.setUdfs(consumableUdfs);
		
		return setEventInfo;
	}
	/*
	* Name : setMaterialLocationInfo
	* Desc : This function is Make SetMaterialLocationInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public SetMaterialLocationInfo setMaterialLocationInfo( Consumable consumableData, String materialLocationName )
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);
				
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		setMaterialLocationInfo.setUdfs(consumableUdfs);
		
		return setMaterialLocationInfo;
	}
	/*
	* Name : setSplitInfo
	* Desc : This function is Make SetSplitInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public SplitInfo splitInfo( Consumable consumableData, String childConsumableName,
									   Map<String, String> childConsumableUdfs, double quantity )
	{
		SplitInfo splitInfo = new SplitInfo();
		splitInfo.setChildConsumableName(childConsumableName);
		splitInfo.setChildConsumableUdfs(childConsumableUdfs);
		splitInfo.setQuantity(quantity);
						
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		splitInfo.setUdfs(consumableUdfs);
		
		return splitInfo;
	}

	/*
	* Name : undoInfo
	* Desc : This function is Make SetUndoInfo
	* Author : AIM Systems, Inc
	* Date : 2010.12.22
	*/
	public UndoInfo undoInfo( Consumable consumableData, String eventName,
			   Timestamp eventTime,  String eventTimeKey, String eventUser, String lastEventTimeKey )
	{
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime( eventTime );
		undoInfo.setEventTimeKey( eventTimeKey );
		undoInfo.setEventUser( eventUser );
		undoInfo.setLastEventTimeKey( lastEventTimeKey );
		
		Map<String,String> consumableUdfs = consumableData.getUdfs();
		undoInfo.setUdfs(consumableUdfs);
		
		return undoInfo;
	}
	
	/**
	 * to get common Consumable data
	 * @author swcho
	 * @since 2014.05.08
	 * @param consumableName
	 * @return
	 * @throws CustomException
	 */
	public Consumable getConsumableData(String consumableName)
		throws CustomException
	{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);
			Consumable consumableData =   ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
			
			return consumableData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
	}
}
