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

public class ChangeMaterialProperties extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaterialProperties", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String consumableState = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESTATE", true);
		String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String productionInputType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONINPUTTYPE", true);
		String expirationDate = SMessageUtil.getBodyItemValue(doc, "EXPIRATIONDATE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String materialLocationName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOCATIONNAME", false);
//		String consumableHoldState = SMessageUtil.getBodyItemValue(doc, "CONSUMABLEHOLDSTATE", false);
//		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
//		String reasonCodeType = "";
//		if(consumableHoldState.equals(GenericServiceProxy.getConstantMap().Flag_Y) && !consumableState.equals("SCRAPPED"))
//			reasonCodeType = "HoldMaterial";
//		else if(consumableState.equals("SCRAPPED"))
//			reasonCodeType = "ScrapMaterial";
			
		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(consumableName);
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(consumableKey);
		consumableData.setConsumableState(consumableState);
//		consumableData.setReasonCode(reasonCode);
//		consumableData.setReasonCodeType(reasonCodeType);
		consumableData.setMaterialLocationName(materialLocationName);
		consumableData.setQuantity(Integer.parseInt(quantity));
		
		Map<String, String> udfs = consumableData.getUdfs();
		udfs.put("MACHINENAME", machineName);
		udfs.put("EXPIRATIONDATE", expirationDate);
		udfs.put("PRODUCTIONINPUTTYPE", productionInputType);
//		udfs.put("CONSUMABLEHOLDSTATE", consumableHoldState);
		consumableData.setUdfs(udfs);
		
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
