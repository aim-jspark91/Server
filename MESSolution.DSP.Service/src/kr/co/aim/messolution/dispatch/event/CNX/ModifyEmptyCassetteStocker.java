package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZoneEmptyCST;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyEmptyCassetteStocker extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "ZONENAME", true);
		String fromStockerName = SMessageUtil.getBodyItemValue(doc, "FROMSTOCKERNAME", true);
		String fromZoneName = SMessageUtil.getBodyItemValue(doc, "FROMZONENAME", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		
		String newFromStockerName = SMessageUtil.getBodyItemValue(doc, "NEWFROMSTOCKERNAME", true);
		String newFromZoneName = SMessageUtil.getBodyItemValue(doc, "NEWFROMZONENAME", true);
		String newPosition = SMessageUtil.getBodyItemValue(doc, "NEWPOSITION", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
			
		DspStockerZoneEmptyCST stockerData = null;
		
		Object[] keySet = new Object[]{stockerName, zoneName, fromStockerName, fromZoneName};
		
		try
		{
			stockerData = ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().selectByKey(false, keySet);
			
			if (stockerData != null) 
			{
				ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().remove(eventInfo, stockerData);
			}		
			
		}
		catch (Exception ex)
		{
			stockerData = null;
		}
		
		if(stockerData == null)
		{
			throw new CustomException("IDLE-0006", "");
		}
			
		stockerData.setFromStockerName(newFromStockerName);
		stockerData.setFromZoneName(newFromZoneName);
		stockerData.setPosition(Long.parseLong(newPosition));
		
		stockerData.setLastEventUser(eventInfo.getEventUser());
		stockerData.setLastEventComment(eventInfo.getEventComment());
		stockerData.setLastEventTime(eventInfo.getEventTime());
		stockerData.setLastEventTimekey(eventInfo.getEventTimeKey());
		stockerData.setLastEventName(eventInfo.getEventName());
		
		try
		{
			ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().create(eventInfo, stockerData);
		}
		catch(Exception ex)
		{
			throw new CustomException("", "");
		}
		
		return doc;
	}
}
