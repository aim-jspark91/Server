package kr.co.aim.messolution.consumable.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class StockOutMaterial extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StockOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		int materialHoldCount = 0;
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				String lastUsedTime = SMessageUtil.getChildText(materialE, "LASTUSEDTIME", false);
				String expirationDate = SMessageUtil.getChildText(materialE, "EXPIRATIONDATE", false);
			
				
				/*============= Set newDurationUsed =============*/
				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				Consumable materialData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
				
				String durationUsed = materialData.getUdfs().get("DURATIONUSED");
				double newDurationUsed = 0;
				
				if(Double.parseDouble(durationUsed) > 0)
				{
					newDurationUsed = Double.parseDouble(durationUsed) + getDurationUsed(lastUsedTime);
				}
				else
				{
					newDurationUsed = 0 + getDurationUsed(lastUsedTime);
				}
				
				Map<String, String> materialUdfs = materialData.getUdfs();
				materialUdfs.put("DURATIONUSED", String.valueOf(newDurationUsed));
				materialData.setUdfs(materialUdfs);
				
				ConsumableServiceProxy.getConsumableService().update(materialData);
				
				
				/*======================================== Validation =========================================*/
				/*============= durationUsed, if DurationUsed > DurationUsedLimit , Hold Material =============*/
				/*============= expirationDate, if currentDate > expirationDate , Hold Material   =============*/
				/*=============================================================================================*/
				double durationUsedLimit = Double.parseDouble(materialData.getUdfs().get("DURATIONUSEDLIMIT"));
				
				if(newDurationUsed > durationUsedLimit || 
				   Long.parseLong(expirationDate)< Long.parseLong(TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DAY)))
				{   
					/*============= Hold material  =============*/
					EventInfo holdeventInfo = EventInfoUtil.makeEventInfo("HoldMaterial", getEventUser(), getEventComment(), null, null);
					holdConsumable(holdeventInfo, materialData);
					materialHoldCount++;
				}	
			}
			if(materialHoldCount > 0)
			{
				//XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "HOLDMATERIAL", 1);
				XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "HOLDMATERIAL", "1");
				
				return doc;
			}
			else
			{
				for(Element materialE : materialList)
				{
					String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
			
					/*============= Set manufactureDate as currentTime(stockOutTime) =============*/
					Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);		
					Map<String, String> materialUdfs = materialData.getUdfs();
					
					materialUdfs.put("MANUFACTUREDATE", TimeUtils.getCurrentTime());
					materialData.setUdfs(materialUdfs);
					
				
					/*============= Validation consumableState, transportState =============*/
					if(!materialData.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK)
							|| !materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
					{
						throw new CustomException("MATERIAL-2002", GenericServiceProxy.getConstantMap().Cons_Available, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
					}
					
		
					/*============= Stock Out Material =============*/
					stockOutConsumableData(eventInfo, materialData);
				}
			}
		}
		return doc;
	}
	
	private double getDurationUsed(String lastUsedTime) throws CustomException
	{
		
		/*============= Calculate durationUsed (KitTime ~ UnKitTime) =============*/
		String currentTime = TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		
		try 
		{
			Date lastUsedTimeDate = sdf.parse(lastUsedTime);
			Date currentTimeDate = sdf.parse(currentTime);
				
			long longlastUsedTime = lastUsedTimeDate.getTime();
			long longCurrentTime = currentTimeDate.getTime();
		
			double temp = 1000*60*60*24;
			long durationUsed = longCurrentTime-longlastUsedTime;
			
			double days = (double)durationUsed/temp;
			
			/*============= Change days form 123.12345 --> 123.12 =============*/
			double tempDays = days * 100;
			int intDays = (int)tempDays;
			double finalDays = (double)intDays /(double)100;
			
			return finalDays;
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private void holdConsumable(EventInfo eventInfo, Consumable materialData) 
			throws CustomException
	{
		eventInfo.setEventName("Hold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("CONSUMABLEHOLDSTATE", "Y");
		materialData.setUdfs(udfs);
		
		ConsumableServiceProxy.getConsumableService().update(materialData);
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
	}
	
	/**
	* Name : stockOutConsumableData
	* Desc : consumable stock out event
	* Author :  
	* Date : 2016.07.29
	*/
	private void stockOutConsumableData(EventInfo eventInfo, Consumable materialData) throws CustomException
	{
		/*============= Set transportState, manufactureDate =============*/
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		udfs.put("MANUFACTUREDATE", TimeUtils.getCurrentTime());
		
		materialData.setUdfs(udfs);
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);	
	}
	
	

	
	/**
	* Name : stockOutDurableData
	* Desc : durable stock out event
	* Author : 
	* Date : 2016.07.29
	*/
	/*private void stockOutDurableData(EventInfo eventInfo, Durable materialData) throws CustomException
	{
		DurableSpec durableSpec = CommonUtil.getDurableSpecByDurableName(materialData.getKey().getDurableName());
		
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("MANUFACTUREDATE", TimeUtils.getCurrentTime());
		
		if (isDurUnfreezeRequired(durableSpec))
		{
			eventInfo.setEventName("Unfreezing");
			udfs.put("TRANSPORTSTATE", "UNFREEZING");
		}
		else
		{
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		}
		
		materialData.setUdfs(udfs);
		
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(materialData.getUdfs());
		DurableServiceProxy.getDurableService().update(materialData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfo, eventInfo);
	}*/

	
	/**
	* Name : isDurUnfreezeRequired
	* Desc : check if Unfreeze is required
	* Author : jhlee
	* Date : 2016.07.29
	*/
/*	private boolean isDurUnfreezeRequired(DurableSpec durableSpec)
	{
		if (durableSpec.getUdfs().get("REQUIREDEVENT").equals("UNFROZEN") 
			|| durableSpec.getUdfs().get("REQUIREDEVENT").equals("UNFROZENAndMIXING"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}*/
	
	/**
	* Name : isConsUnfreezeRequired
	* Desc : check if Unfreeze is required
	* Author : jhlee
	* Date : 2016.07.29
	*/

}
