package kr.co.aim.messolution.consumable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class KitMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event  =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mount", getEventUser(), getEventComment(), null, null);
		
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		if(materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				String machineName = SMessageUtil.getChildText(materialE,"MACHINENAME", true);
				String unitName = SMessageUtil.getChildText(materialE, "UNITNAME", false);
				String materialPosition = SMessageUtil.getChildText(materialE, "MATERIALPOSITION", false);

				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));

				Map<String, String> udfs = consumableData.getUdfs();
				
				if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
					throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
				
				if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
	            {
	                throw new CustomException("MATERIAL-0010", materialName);
	            }
				
				// Modified by smkang on 2018.09.22 - Change logic as below.
//				ConsumableState = 'Mount',
//				AreaName = 현재 Machine의 AreaName,
//		        MaterialLocationName = 현재 Mount Position,
//		        MachineName = 현재 Machine,
//		        UnitName = 현재 Unit,
//		        MaterialPosition = 현재 Mount Position,
//		        TransportState = 'ONEQP',
//		        LastMountTime을 업데이트
				Machine machineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(machineName);
				
				consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Mount);
				consumableData.setAreaName(machineData.getAreaName());
				consumableData.setMaterialLocationName(materialPosition);
				udfs.put("MACHINENAME", machineName);
				udfs.put("UNITNAME", unitName);
				udfs.put("MATERIALPOSITION", materialPosition);
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
				udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());
				
				ConsumableServiceProxy.getConsumableService().update(consumableData);
				
				SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
			}
		}
		
		return doc;
	}	
}