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
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class StockInMaterial extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event  =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StockIn", getEventUser(), getEventComment(), null, null);
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
					String usedQty = SMessageUtil.getChildText(materialE, "USEDQUANTITY", false);
					
					/*============= Set manufactureDate as currentTime(stockInTime) =============*/
					// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
					Consumable materialData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));

					Map<String, String> materialUdfs = materialData.getUdfs();
					
					materialUdfs.put("MANUFACTUREDATE", TimeUtils.getCurrentTime());
					materialData.setUdfs(materialUdfs);
					
					/*============= Validation consumableState, TransportState =============*/
					if(!materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available)|| !materialUdfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK))
					{
						throw new CustomException("MATERIAL-2002",GenericServiceProxy.getConstantMap().Cons_Available,GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
					}
					
					/*============= Stock In material  =============*/
					stockInConsumableData(eventInfo, materialData, usedQty);
				}
			}		
		}
		return doc;
	}
	
	/**
	* Name : stockInConsumableData
	* Desc : stock in material (consumable)
	* Author : aim system
	* Date : 2016.08.01
	*/
	private void stockInConsumableData(EventInfo eventInfo, Consumable materialData, String usedQty) throws CustomException
	{
		/*============= Set TransportState =============*/
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
		materialData.setUdfs(udfs);
		
		ConsumableServiceProxy.getConsumableService().update(materialData);
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
		
		if(Double.valueOf(usedQty) != 0)
		{
			/*============= Decrement Consume Qty from existing Qty =============*/
			
			eventInfo.setEventName("ConsumeQuantity");
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
														eventInfo.getEventTimeKey(), Double.parseDouble(usedQty), udfs);
			
			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
					                                                              (DecrementQuantityInfo) transitionInfo, eventInfo);
			
			/*============= After Decrement Consume Qty, if quantity == 0, Make NotAvaliable =============*/
			materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
			if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
			{
				eventInfo.setEventName("ChangeState");
				
				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
				makeNotAvailableInfo.setUdfs(materialData.getUdfs());
				
				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
			}
		}
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
	
	//20170308 by zhanghao
	private double getDurationUsed(String lastUsedTime) throws CustomException
	{

		/*============= Calculate durationUsed (KitTime ~ UnKitTime) =============*/
		String currentTime = TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		
		try 
		{
			Date lastUsedTimeDate = sdf.parse(lastUsedTime);
			Date currentTimeDate = sdf.parse(currentTime);
				
			long longLastUsedTime = lastUsedTimeDate.getTime();
			long longCurrentTime = currentTimeDate.getTime();
		
			double temp = 1000*60*60*24;
			long durationUsed = longCurrentTime-longLastUsedTime;
			
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
	
	/**
	* Name : stockInDurableData
	* Desc : stock in material (durable)
	* Author : aim system
	* Date : 2016.08.01
	*/
	
	/*============= No resourceType('Durable') column in consumableSpec table =============*/
	/*private void stockInDurableData(EventInfo eventInfo, Durable materialData, String unfreezeStartTime, String expirationDate) throws CustomException
	{
		Map<String, String> udfs = materialData.getUdfs();
		
		//durableSpec 
		DurableSpec durableSpecData = CommonUtil.getDurableSpecByDurableName(materialData.getKey().getDurableName());
		
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);		
		materialData.setUdfs(udfs);
		
		int durationUsed = (int)materialData.getDurationUsed() + getDurationUsed(unfreezeStartTime);
		
		//initialize material durationUsed
		materialData.setDurationUsed(durationUsed);
		
		DurableServiceProxy.getDurableService().update(materialData);
		
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfo, eventInfo);
		
		if(StringUtil.equals(durableSpecData.getUdfs().get("REQUIREDEVENT"),"UNFROZEN") 
				|| StringUtil.equals(durableSpecData.getUdfs().get("REQUIREDEVENT"),"UNFROZENAndMIXING"))
		{
			if(String.valueOf(durableSpecData.getDurationUsedLimit())==null || StringUtil.isEmpty(String.valueOf(durableSpecData.getDurationUsedLimit())))
				throw new CustomException("MATERIAL-0020");
			
			if(durableSpecData.getUdfs().get("UNFREEZETIMEUSED")==null || StringUtil.isEmpty(durableSpecData.getUdfs().get("UNFREEZETIMEUSED")))
				throw new CustomException("MATERIAL-0022");
			
			if(Integer.parseInt(materialData.getUdfs().get("UNFREEZECOUNT")) > Integer.parseInt(durableSpecData.getUdfs().get("UNFREEZETIMEUSED"))
					|| durationUsed > (int)durableSpecData.getDurationUsedLimit() ||
					Long.parseLong(expirationDate) < Long.parseLong(TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT)))
			{
				//Hold 
				materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialData.getKey().getDurableName());
				this.HoldDurable(eventInfo, materialData);
			}
		}
		else
		{
			if(Long.parseLong(expirationDate) < Long.parseLong(TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT)))
			{
				//Hold 
				materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialData.getKey().getDurableName());
				this.HoldDurable(eventInfo, materialData);
			}
		}
	}*/
	
	/**
	* Name : getDurationUsed
	* Desc : getDurationUsed
	* Author : aim system
	* Date : 2016.10.19
	*/
	/*============= No resourceType('Durable') column in consumableSpec table =============*/
	/*private int getDurationUsed(String unfreezeStartTime)
			throws CustomException
	{
		int stockOutY = 0;
		int stockOutM = 0;
		int stockOutD = 0;
		int stockOutH = 0;
		
		if (!unfreezeStartTime.isEmpty())		
		{
			stockOutY = Integer.parseInt(unfreezeStartTime.substring(0, 4));
			stockOutM = Integer.parseInt(unfreezeStartTime.substring(4, 6));
			stockOutD = Integer.parseInt(unfreezeStartTime.substring(6, 8));
			stockOutH = Integer.parseInt(unfreezeStartTime.substring(8, 10));
		}
		
		String currentTime = TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
		
		int currentY = Integer.parseInt(currentTime.substring(0, 4));
		int currentM = Integer.parseInt(currentTime.substring(4, 6));
		int currentD = Integer.parseInt(currentTime.substring(6, 8));
		int currentH = Integer.parseInt(currentTime.substring(8, 10));
		
		int monthCnt = MonthDayCount(stockOutY, stockOutM, currentM, stockOutD);
		int yearCnt = MonthYearCount(stockOutY, currentY);
		
		int unfreezeDuration = 24 * yearCnt
					     		+ 24 * monthCnt
					     		+ 24 * (currentD - stockOutD)
					     		+ (currentH - stockOutH);

		return unfreezeDuration;
	}*/
	
	/*============= No resourceType('Durable') column in consumableSpec table =============*/
	/*private int MonthDayCount(int year, int month, int limitMonth, int day)
    {
        int tempDay = 0;
        
        for(int i=month;i<limitMonth;i++)
        {
        	switch (i)
            {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    { tempDay += 31; break; }

                case 4:
                case 6:
                case 9:
                case 11:
                    { tempDay += 30; break; }


                case 2:
                    {
                        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                        {
                            tempDay += 29; break;
                        }
                        else
                        {
                            tempDay += 28; break;
                        }
                    }
            }
        } 

        return tempDay;
    }*/
	
	/*============= No resourceType('Durable') column in consumableSpec table =============*/
	/*private int MonthYearCount(int year, int limitYear)
    {
        int tempDay = 0;
        
        for(int i=year;i<limitYear;i++)
        {
        	if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
            {
                tempDay += 366; break;
            }
            else
            {
                tempDay += 365; break;
            }
        } 

        return tempDay;
    }*/
	
	/*============= No resourceType('Durable') column in consumableSpec table =============*/
	/*private void HoldDurable(EventInfo eventInfo, Durable materialData) 
			throws CustomException
	{
		eventInfo.setEventName("Hold");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("DURABLEHOLDSTATE", "Y");
		
		materialData.setUdfs(udfs);
		
		DurableServiceProxy.getDurableService().update(materialData);
		
		SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(materialData.getUdfs());
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfo, eventInfo);
	}*/
	
}

