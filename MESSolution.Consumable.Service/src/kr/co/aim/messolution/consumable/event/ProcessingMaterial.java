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

import org.jdom.Document;

/**
 * @since 2018.09.22
 * @author smkang
 * @see According to EDO's request, Only one material can change to InUse state in a unit.
 */
public class ProcessingMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event  =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Processing", getEventUser(), getEventComment(), null, null);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		
		// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
		
		if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse)) {
			if (consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Mount)) {
				Map<String, String> udfs = consumableData.getUdfs();
				String machineName = udfs.get("MACHINENAME");
				String unitName = udfs.get("UNITNAME");
				
				try {
					List<Consumable> inUseConsumableDataList = ConsumableServiceProxy.getConsumableService().select("MACHINENAME = ? AND UNITNAME = ? AND CONSUMABLESTATE = ?", new Object[] {machineName, unitName, GenericServiceProxy.getConstantMap().Cons_InUse});
					
					if (inUseConsumableDataList != null && inUseConsumableDataList.size() > 0)
						throw new CustomException("MATERIAL-0032", inUseConsumableDataList.get(0).getKey().getConsumableName(), GenericServiceProxy.getConstantMap().Cons_Mount);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
				
				ConsumableServiceProxy.getConsumableService().update(consumableData);
				
				SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(consumableData, consumableData.getAreaName());
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
			} else {
				throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Mount);
			}
		}
		
		return doc;
	}	
}