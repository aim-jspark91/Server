package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeMaterialSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String materialName = SMessageUtil.getBodyItemValue(doc,
				"MATERIALNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME",
				true);
		String materialSpecName = SMessageUtil.getBodyItemValue(doc,
				"MATERIALSPECNAME", true);
		String materialQty = SMessageUtil.getBodyItemValue(doc,
				"CREATEQUANTITY", true);
		String qty = SMessageUtil.getBodyItemValue(doc,
				"QUANTITY", true);
		String materialCode = SMessageUtil.getBodyItemValue(doc,
				"MATERIALCODE", false);
		String expirationDate = SMessageUtil.getBodyItemValue(doc,
				"EXPIRATIONDATE", true);
		String resourceType = SMessageUtil.getBodyItemValue(doc,
				"RESOURCETYPE", true);
		String materialType = SMessageUtil.getBodyItemValue(doc,
				"MATERIALTYPE", false);
		String durationUsedLimit = SMessageUtil.getBodyItemValue(doc,
				"DURATIONUSEDLIMIT", false);
		String unFreezeTimeUsed = SMessageUtil.getBodyItemValue(doc,
				"UNFREEZETIMEUSED", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeInfo",
				getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		if (resourceType.equals("Consumable")) 
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(materialName);

			// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
			Consumable materialData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(consumableKey);
			
			ConsumableSpecKey consumableSpecKey= new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(materialSpecName);
			consumableSpecKey.setFactoryName(factoryName);
			consumableSpecKey.setConsumableSpecVersion("00001");
			ConsumableSpec materialSpecDate=ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			

			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setConsumableSpecName(materialSpecName);
			changeSpecInfo.setConsumableSpecVersion("00001");			
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> consumableUdfs = materialData.getUdfs();
//			consumableUdfs.put("MATERIALCODE", materialCode);
//			consumableUdfs.put("EXPIRATIONDATE", expirationDate);
//			consumableUdfs.put("UNFREEZETIMEUSED", unFreezeTimeUsed);
//			consumableUdfs.put("DURATIONUSEDLIMIT", materialSpecDate.getUdfs().get("DURATIONUSEDLIMIT"));
//			consumableUdfs.put("DURATIONUSEDLIMIT", durationUsedLimit);
//			changeSpecInfo.setUdfs(consumableUdfs);		
			
			changeSpecInfo.getUdfs().put("MATERIALCODE", materialCode);
			changeSpecInfo.getUdfs().put("EXPIRATIONDATE", expirationDate);
			changeSpecInfo.getUdfs().put("UNFREEZETIMEUSED", unFreezeTimeUsed);
			changeSpecInfo.getUdfs().put("DURATIONUSEDLIMIT", durationUsedLimit);

			materialData.setCreateQuantity(Double.valueOf(materialQty));

			materialData.setQuantity(Double.valueOf(qty));

			materialData.setConsumableType(materialType);
			ConsumableServiceProxy.getConsumableService().update(materialData);

			Consumable resultData = ConsumableServiceProxy
					.getConsumableService().changeSpec(consumableKey,
							eventInfo, changeSpecInfo);
		} 
		else if (resourceType.equals("Durable")) 
		{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(materialName);

			// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(durableKey);
			
			DurableSpecKey durableSpecKey = new DurableSpecKey();
			durableSpecKey.setFactoryName(factoryName);
			durableSpecKey.setDurableSpecName(materialSpecName);
			durableSpecKey.setDurableSpecVersion("00001");			
			
			DurableSpec specData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);			

			if (StringUtil.equals(durableData.getDurableState(), "InUse"))
				throw new CustomException("CST-0006", materialName);

			kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo changeSpecInfo1 =  MESDurableServiceProxy
					.getDurableInfoUtil().changeSpecInfo(durableData,
							durableData.getAreaName(),
							specData.getDefaultCapacity(),
							materialSpecName,
							"00001",
							specData.getDurationUsedLimit(),
							factoryName,
							specData.getTimeUsedLimit());			

			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> durableUdfs = durableData.getUdfs();
//			durableUdfs.put("MATERIALCODE", materialCode);
//			durableUdfs.put("EXPIRATIONDATE", expirationDate);			
//			changeSpecInfo1.setUdfs(durableUdfs);
			changeSpecInfo1.getUdfs().put("MATERIALCODE", materialCode);
			changeSpecInfo1.getUdfs().put("EXPIRATIONDATE", expirationDate);

			durableData.setDurableType(specData.getDurableType());	
			DurableServiceProxy.getDurableService().update(durableData);

			MESDurableServiceProxy.getDurableServiceImpl().changeSpec(durableData, changeSpecInfo1, eventInfo);			
		}
		return doc;
	}

}
