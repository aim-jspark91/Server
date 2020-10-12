package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class MixMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mix", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				String resourceType = SMessageUtil.getChildText(materialE, "RESOURCETYPE", true);
				
				if(resourceType.equals("Consumable")) 
				{
					//consumable
					Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
					
					//consumableSpec
					ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName);
					Map<String, String> specUdfs = consumableSpecData.getUdfs();
					
					String requiredEvent = CommonUtil.getValue(specUdfs, "REQUIREDEVENT");
					
					//validate
					if(!materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
						throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
					
					if (!requiredEvent.equals("MIXING") && !requiredEvent.equals("UNFROZENAndMIXING"))
						throw new CustomException("MATERIAL-0008", materialName);
					
					if (requiredEvent.equals("UNFROZENAndMIXING") && !materialData.getUdfs().get("TRANSPORTSTATE").equals("UNFROZEN"))
						throw new CustomException("MATERIAL-0007", materialName);
					
					
					//Mix Material
					this.mixConsumableData(eventInfo, materialData);
				}
				else if(resourceType.equals("Durable"))
				{
					//durable
					Durable materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
					
					//durableSpec 
					DurableSpec durableSpecData = CommonUtil.getDurableSpecByDurableName(materialName);
					Map<String, String> specUdfs = durableSpecData.getUdfs();
					
					String requiredEvent = CommonUtil.getValue(specUdfs, "REQUIREDEVENT");
					
					//validate
					if(!materialData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
						throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Dur_Available);
					
					if (!requiredEvent.equals("MIXING") && !requiredEvent.equals("UNFROZENAndMIXING"))
						throw new CustomException("MATERIAL-0008", materialName);
					
					if (requiredEvent.equals("UNFROZENAndMIXING") && !materialData.getUdfs().get("TRANSPORTSTATE").equals("UNFROZEN"))
						throw new CustomException("MATERIAL-0007", materialName);
					
					
					//Mix Material
					this.mixDurableData(eventInfo, materialData);
				}
			}
		}
		
		return doc;
	}
	
	private void mixConsumableData(EventInfo eventInfo, Consumable materialData) throws CustomException
	{
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTSTATE", "MIXING");
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
	}
	
	private void mixDurableData(EventInfo eventInfo, Durable materialData) throws CustomException
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTSTATE", "MIXING");
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfo, eventInfo);
	}
}