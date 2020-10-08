package kr.co.aim.messolution.consumable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class HoldCrate extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
				
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");
		
		List<Element> crateList = SMessageUtil.getBodySequenceItemList(doc, "CRATELIST", true);
		
		if (crateList != null)
		{
			for(Element crateE : crateList)
			{
				String crateName = SMessageUtil.getChildText(crateE, "CRATENAME", true);
				String crateHoldState = SMessageUtil.getChildText(crateE, "CONSUMABLEHOLDSTATE", true);
				
				//validate hold state
				if (crateHoldState.equals("Y"))
				{
					throw new CustomException("CRATE-0008", crateName);
				}

				// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				Consumable crateData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(crateName));
					
				//Hold 
				this.HoldConsumable(eventInfo, crateData);			
			}
		}
		
		return doc;
	}	
	
	private void HoldConsumable(EventInfo eventInfo, Consumable crateData) 
			throws CustomException
	{
		Map<String, String> udfs = crateData.getUdfs();
		udfs.put("CONSUMABLEHOLDSTATE", "Y");
		
		crateData.setUdfs(udfs);
		
		ConsumableServiceProxy.getConsumableService().update(crateData);
		
		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(crateData, crateData.getAreaName());
		
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(crateData.getKey().getConsumableName(), setEventInfo, eventInfo);
	}
		
}
