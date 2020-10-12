package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZoneEmptyCST;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateEmptyCassetteStocker extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "ZONENAME", true);
		String fromStockerName = SMessageUtil.getBodyItemValue(doc, "FROMSTOCKERNAME", true);
		String fromZoneName = SMessageUtil.getBodyItemValue(doc, "FROMZONENAME", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					
		DspStockerZoneEmptyCST stockerZoneEmptyCSTData = new DspStockerZoneEmptyCST();
		stockerZoneEmptyCSTData.setStockerName(stockerName);
		stockerZoneEmptyCSTData.setZoneName(zoneName);
		stockerZoneEmptyCSTData.setFromStockerName(fromStockerName);
		stockerZoneEmptyCSTData.setFromZoneName(fromZoneName);
		stockerZoneEmptyCSTData.setPosition(Long.parseLong(position));
			
		stockerZoneEmptyCSTData.setLastEventUser(eventInfo.getEventUser());
		stockerZoneEmptyCSTData.setLastEventComment(eventInfo.getEventComment());
		stockerZoneEmptyCSTData.setLastEventTime(eventInfo.getEventTime());
		stockerZoneEmptyCSTData.setLastEventTimekey(eventInfo.getEventTimeKey());
		stockerZoneEmptyCSTData.setLastEventName(eventInfo.getEventName());	
			
		try
		{
			ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().create(eventInfo, stockerZoneEmptyCSTData);
		}
		catch(Exception ex)
		{
			throw new CustomException("", "");
		}
		
		return doc;
	}

}
