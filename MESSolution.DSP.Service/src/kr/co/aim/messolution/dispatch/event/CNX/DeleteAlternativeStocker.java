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

public class DeleteAlternativeStocker extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		
		List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "STOCKERLIST", true);
		
		for(Element eleData : eleList)
		{
			String toStockerName = SMessageUtil.getChildText(eleData, "TOSTOCKERNAME", true);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			
		    Object[] keySet = new Object[]{stockerName, toStockerName};
			DspAlternativeStocker sockerData = ExtendedObjectProxy.getDspAlternativeStockerService().selectByKey(false, keySet);
				
			ExtendedObjectProxy.getDspAlternativeStockerService().remove(eventInfo, sockerData);
		}
		
		List<DspAlternativeStocker> alternativeStockerList = null ;
		
		try
		{		
			String condition = "WHERE STOCKERNAME = ? ORDER BY POSITION ";
			Object[] bindSet = new Object[]{stockerName};
			alternativeStockerList = ExtendedObjectProxy.getDspAlternativeStockerService().select(condition, bindSet);	
			
			if(alternativeStockerList != null)
			{
				long position = 0;
				
				for (DspAlternativeStocker alternativeStockerData : alternativeStockerList)
				{
					alternativeStockerData.setPosition(position);
					ExtendedObjectProxy.getDspAlternativeStockerService().update(alternativeStockerData);
					position++;
				}
			}
		}
		catch(Exception ex)
		{
			
		}
		
		return doc;
	}

}
