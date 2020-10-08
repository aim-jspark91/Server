package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspAlternativeStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateAlternativeStocker extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		
		List<Element> eleToStockerList = SMessageUtil.getBodySequenceItemList(doc, "TOSTOCKERLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		for(Element eleData : eleToStockerList)
		{
			String toStockerName = SMessageUtil.getChildText(eleData, "TOSTOCKERNAME", true);		
			List<DspAlternativeStocker> stockerList = null;
			long position = 0;
			
			try
			{			
				String condition = "WHERE STOCKERNAME = ? ORDER BY POSITION DESC ";

				Object[] bindSet = new Object[]{stockerName};
				
				stockerList = ExtendedObjectProxy.getDspAlternativeStockerService().select(condition, bindSet);
				
				if(stockerList != null)
				{
					position = stockerList.get(0).getPosition();
					position++;
				}
			}
			catch(Exception ex)
			{
				
			}
			
			DspAlternativeStocker alternativeStockerData = new DspAlternativeStocker();
			alternativeStockerData.setStockerName(stockerName);
			alternativeStockerData.setToStockerName(toStockerName);
			alternativeStockerData.setPosition(position);
			
			alternativeStockerData.setLastEventUser(eventInfo.getEventUser());
			alternativeStockerData.setLastEventComment(eventInfo.getEventComment());
			alternativeStockerData.setLastEventTime(eventInfo.getEventTime());
			alternativeStockerData.setLastEventTimekey(eventInfo.getEventTimeKey());
			alternativeStockerData.setLastEventName(eventInfo.getEventName());	
			
			try
			{
				ExtendedObjectProxy.getDspAlternativeStockerService().create(eventInfo, alternativeStockerData);
			}
			catch(Exception ex)
			{
				throw new CustomException("", "");
			}
		}
		
		return doc;
	}

}
