package kr.co.aim.messolution.consumable.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class UnfreezeMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unfreeze", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		//String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				String resourceType = SMessageUtil.getChildText(materialE, "RESOURCETYPE", true);
				String unfreezeStartTime = SMessageUtil.getChildText(materialE, "UNFREEZESTART", true);
				
				if (resourceType.equals("Consumable"))
				{
					//consumable
					// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
					Consumable materialData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
					
					Map<String, String> udfs = materialData.getUdfs();
					
					
					//consumableSpec 
					ConsumableSpec consumableSpec = CommonUtil.getConsumableSpec(materialName);
					Map<String, String> specUdfs = consumableSpec.getUdfs();
					
					
					String requiredEvent = CommonUtil.getValue(specUdfs, "REQUIREDEVENT");
					
					//initialize material durationUsed   unfreezeStartTime					
					String durationUsed = materialData.getUdfs().get("DURATIONUSED"); 
					String stockerOutCount = materialData.getUdfs().get("STOCKEROUTCOUNT");		
					
					int durationTime = 0;	
					
					if(stockerOutCount == null || StringUtil.isEmpty(stockerOutCount)||Integer.parseInt(stockerOutCount)<=1) 
						durationTime = 0 + getDurationUsed(unfreezeStartTime);
					else 
						durationTime = Integer.parseInt(durationUsed) + getDurationUsed(unfreezeStartTime) ;
					
					udfs.put("DURATIONUSED", String.valueOf(durationTime));
					udfs.put("MANUFACTUREDATE", TimeUtils.getCurrentTime());
					
					materialData.setUdfs(udfs);
					ConsumableServiceProxy.getConsumableService().update(materialData);
					
					//validate
					if (!materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
						throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
					
					if (!requiredEvent.equals("UNFROZEN") && !requiredEvent.equals("UNFROZENAndMIXING"))
						throw new CustomException("MATERIAL-0009", materialName);
					
					if (!udfs.get("TRANSPORTSTATE").equals("UNFREEZING"))
						throw new CustomException("MATERIAL-0005", "UNFREEZING");
					
					validateUnfreezeTime(materialName, unfreezeStartTime, "C");
					
					// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
					Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
					
					//Unfreeze Material
					this.unfreezeConsumableData(eventInfo, consumableData);
				}
				else if (resourceType.equals("Durable"))
				{
					//durable
					// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Durable materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
					Durable materialData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(materialName));
					
					Map<String, String> udfs = materialData.getUdfs();
					
					//durableSpec 
					DurableSpec durableSpecData = CommonUtil.getDurableSpecByDurableName(materialName);
					Map<String, String> specUdfs = durableSpecData.getUdfs();
					
					String requiredEvent = CommonUtil.getValue(specUdfs, "REQUIREDEVENT");
					
					//validate
					if (!materialData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
						throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Dur_Available);
					
					if (!requiredEvent.equals("UNFROZEN") && !requiredEvent.equals("UNFROZENAndMIXING"))
						throw new CustomException("MATERIAL-0009", materialName);
					
					if (!udfs.get("TRANSPORTSTATE").equals("UNFREEZING"))
						throw new CustomException("MATERIAL-0005", "UNFREEZING");
					
					validateUnfreezeTime(materialName, unfreezeStartTime, "D");
					
					
					//Unfreeze Material
					this.unfreezeDurableData(eventInfo, materialData);
				}
			}
		}
		
		return doc;
	}
	
	/**
	* Name : validateUnfreezeTime
	* Desc : validate minimum unfreezing Time (8 hours)
	* Author : aim system
	* Date : 2016.07.29
	*/
	private void validateUnfreezeTime(String materialName, String unfreezeStartTime, String flag)
			throws CustomException
	{
		int limitTime = 0;
		
		if(StringUtil.equals(flag, "C"))
		{
			ConsumableSpec consumableSpec = CommonUtil.getConsumableSpec(materialName);
			
			if(consumableSpec.getUdfs().get("DURATIONFROZENLIMIT") == null 
					|| StringUtil.isEmpty(consumableSpec.getUdfs().get("DURATIONFROZENLIMIT")))
				throw new CustomException("MATERIAL-0018");
			
			limitTime = Integer.parseInt(consumableSpec.getUdfs().get("DURATIONFROZENLIMIT"));
		}
		else if(StringUtil.equals(flag, "D"))
		{
			DurableSpec durableSpec = CommonUtil.getDurableSpecByDurableName(materialName);
			
			if(durableSpec.getUdfs().get("DURATIONFROZENLIMIT") == null 
					|| StringUtil.isEmpty(durableSpec.getUdfs().get("DURATIONFROZENLIMIT")))
				throw new CustomException("MATERIAL-0018");
			
			limitTime = Integer.parseInt(durableSpec.getUdfs().get("DURATIONFROZENLIMIT"));
		}
		
		int unfreezeStartY = 0;
		int unfreezeStartM = 0;
		int unfreezeStartD = 0;
		int unfreezeStartH = 0;
		
		if (!unfreezeStartTime.isEmpty())		
		{
			unfreezeStartY = Integer.parseInt(unfreezeStartTime.substring(0, 4));
			unfreezeStartM = Integer.parseInt(unfreezeStartTime.substring(4, 6));
			unfreezeStartD = Integer.parseInt(unfreezeStartTime.substring(6, 8));
			unfreezeStartH = Integer.parseInt(unfreezeStartTime.substring(8, 10));
		}
		
		String currentTime = TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
		
		int unfrozenY = Integer.parseInt(currentTime.substring(0, 4));
		int unfrozenM = Integer.parseInt(currentTime.substring(4, 6));
		int unfrozenD = Integer.parseInt(currentTime.substring(6, 8));
		int unfrozenH = Integer.parseInt(currentTime.substring(8, 10));
				
		int monthCnt = MonthDayCount(unfreezeStartY, unfreezeStartM, unfrozenM, unfrozenD);
		int yearCnt = MonthYearCount(unfreezeStartY, unfrozenY);
		
		int unfreezeDuration = 24 * yearCnt
					     		+ 24 * monthCnt
					     		+ 24 * (unfrozenD - unfreezeStartD)
					     		+ (unfrozenH - unfreezeStartH);
		
		if (unfreezeDuration < limitTime)
			throw new CustomException("MATERIAL-0017", materialName, unfreezeDuration, limitTime);
	}
	
	
	private void unfreezeConsumableData(EventInfo eventInfo, Consumable materialData) throws CustomException
	{
		Map<String, String> udfs = materialData.getUdfs();
		
		int unFreezeCnt;
		
		if(materialData.getUdfs().get("UNFREEZECOUNT") == null 
				|| StringUtil.isEmpty(materialData.getUdfs().get("UNFREEZECOUNT")))		
			unFreezeCnt = 0;
		else
			unFreezeCnt = Integer.parseInt(materialData.getUdfs().get("UNFREEZECOUNT"));
		
		udfs.put("TRANSPORTSTATE", "UNFROZEN");
		//udfs.put("UNFREEZECOUNT", String.valueOf(unFreezeCnt+1));
		udfs.put("UNFREEZENTIME", String.valueOf(eventInfo.getEventTime()));
		materialData.setUdfs(udfs);
		
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		ConsumableServiceProxy.getConsumableService().update(materialData);
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
		
	}
	
	private void unfreezeDurableData(EventInfo eventInfo, Durable materialData) throws CustomException
	{
		Map<String, String> udfs = materialData.getUdfs();
		
		int unFreezeCnt;
		
		if(materialData.getUdfs().get("UNFREEZECOUNT") == null 
				|| StringUtil.isEmpty(materialData.getUdfs().get("UNFREEZECOUNT")))		
			unFreezeCnt = 0;
		else
			unFreezeCnt = Integer.parseInt(materialData.getUdfs().get("UNFREEZECOUNT"));
		
		udfs.put("TRANSPORTSTATE", "UNFROZEN");
		udfs.put("UNFREEZECOUNT", String.valueOf(unFreezeCnt+1));
		udfs.put("UNFREEZENTIME", String.valueOf(eventInfo.getEventTime()));
		materialData.setUdfs(udfs);
		
		SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(materialData.getUdfs());
		DurableServiceProxy.getDurableService().update(materialData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfo, eventInfo);
	}
	
	private int MonthDayCount(int year, int month, int limitMonth, int day)
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
    }
	
	private int MonthYearCount(int year, int limitYear)
    {
        if (year < limitYear) {
        	if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                return 366;
            else
                return 365;
        } else {
        	return 0;
        }
    }

	//20170308 by zhanghao
	private int getDurationUsed(String unfreezeStartTime)
			throws CustomException
	{
		String currentTime = TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
		try 
		{
			Date unfreezeStartTimeDate = sdf.parse(unfreezeStartTime);
			Date currentTimeDate = sdf.parse(currentTime);
			long longunfreezeStartTime=unfreezeStartTimeDate.getTime();
			long longcurrentTime=currentTimeDate.getTime();
			long days=(longcurrentTime-longunfreezeStartTime)/(1000*60*60);
			
			return (int)days;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}