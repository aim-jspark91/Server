package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class UnKitMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event  =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unmount", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				
				// Modified by smkang on 2018.09.22 - Change logic as below.
//				ConsumableState = 'Available',
//		        AreaName = NULL,
//		        MaterialLocationName = NULL,
//		        MachineName = NULL,
//		        UnitName = NULL,
//		        MaterialPosition = NULL,
//		        TransportState = 'OUTSTK'로 업데이트
				
				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
				
				Map<String, String> udfs = consumableData.getUdfs();
				
				if((!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Mount) && 
					!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse)) || 
					!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP)) {
					
					throw new CustomException("MATERIAL-2002");
				}
				
				consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
				consumableData.setAreaName("");
				consumableData.setMaterialLocationName("");
				udfs.put("MACHINENAME", "");
				udfs.put("UNITNAME", "");
				udfs.put("MATERIALPOSITION", "");
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
				
				ConsumableServiceProxy.getConsumableService().update(consumableData);
				
				SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
			}
		}
		
		return doc;
	}
}