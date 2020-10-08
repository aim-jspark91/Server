package kr.co.aim.messolution.consumable.event;

import java.util.List;

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
import org.jdom.Element;

public class ScrapMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		/*============= Message Parsing =============*/
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), reasonCodeType, reasonCode);
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				
				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				Consumable materialData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
				
				/*============= Validation ConsumableState =============*/
				if (materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Scrapped)
						|| materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse)
						||materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_NotAvailable))
				{
					throw new CustomException("MATERIAL-0012", materialName, materialData.getConsumableState());
				}
				
				/*============= Scrap Consumable =============*/
				materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Scrapped);
				
				ConsumableServiceProxy.getConsumableService().update(materialData);
				SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
			}
		}
		
		return doc;
	}
}