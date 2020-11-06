package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaterialState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaterialState", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String consumableState = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESTATE", true);
		
		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(consumableName);
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(consumableKey);
		consumableData.setConsumableState(consumableState);
		
		
		ConsumableServiceProxy.getConsumableService().update(consumableData);
		SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
	
		
		return doc;
	}
	
	
	/**
	* Name : checkExistConsumable
	* Desc : check existence
	* Author : AIM-jhlee
	* Date : 2016.07.29
	*/
	private void checkExistConsumable(String consumableName) throws CustomException
	{
		String condition = "CONSUMABLENAME = ?";
		
		Object[] bindSet = new Object[] {consumableName};
		try
		{
			List <Consumable> sqlResult = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
			
			if(sqlResult.size() == 0)
			{
				throw new CustomException("MATERIAL-0002", consumableName);
			}
		}
		catch (NotFoundSignal ex)
		{
			 return;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-0001", fe.getMessage());
		}
	}
	
	/**
	* Name : checkExistDurable
	* Desc : check existence
	* Author : AIM-jhlee
	* Date : 2016.07.29
	*/
	
}
