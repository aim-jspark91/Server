package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZoneEmptyCST;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class UpdateEmptyCassetteStocker extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		try
		{
			List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "STOCKERLIST", true);
			
			for(Element eleData : eleList)
			{
				String stockerName = SMessageUtil.getChildText(eleData, "STOCKERNAME", true);
				String zoneName = SMessageUtil.getChildText(eleData, "ZONENAME", true);
				String fromStockerName = SMessageUtil.getChildText(eleData, "FROMSTOCKERNAME", true);
				String fromZoneName = SMessageUtil.getChildText(eleData, "FROMZONENAME", true);
				String position = SMessageUtil.getChildText(eleData, "POSITION", true);
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Update", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				
			    Object[] keySet = new Object[]{stockerName, zoneName, fromStockerName, fromZoneName};
				DspStockerZoneEmptyCST sockerData = ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().selectByKey(false, keySet);
					
				sockerData.setPosition(Long.parseLong(position));
				
				ExtendedObjectProxy.getDspStockerZoneEmptyCSTService().update(sockerData);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("", "");
		}
				
		return doc;
	}
}
