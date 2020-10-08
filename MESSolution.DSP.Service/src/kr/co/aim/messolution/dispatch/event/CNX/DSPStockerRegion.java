package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerRegion;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DSPStockerRegion extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element regionList = SMessageUtil.getBodySequenceItem(doc, "REGIONLIST", true);
		
		if(regionList != null)
		{
			for(Object obj : regionList.getChildren())
			{
				Element element = (Element)obj;
				String stockerName = SMessageUtil.getChildText(element, "STOCKERNAME", true);
				String stockerRegionType = SMessageUtil.getChildText(element, "STOCKERREGIONTYPE", true);
				String setCount = SMessageUtil.getChildText(element, "SETCOUNT", true);
				String thresholdCount = SMessageUtil.getChildText(element, "THRESHOLDCOUNT", true);
				String gabageTime = SMessageUtil.getChildText(element, "GABAGETIME", true);
				
				DspStockerRegion stockerRegionData = null;
				
				try
				{
					stockerRegionData = ExtendedObjectProxy.getDspStockerRegionService().selectByKey(false, new Object[] {stockerName, stockerRegionType});
				}
				catch (Exception ex)
				{
					stockerRegionData = null;
				}
				
				if(stockerRegionData == null)
				{
					// Create
					EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
					
					stockerRegionData = new DspStockerRegion(stockerName, stockerRegionType);
					stockerRegionData.setSetCount(Long.parseLong(setCount));
					stockerRegionData.setThresHoldCount(Long.parseLong(thresholdCount));
					stockerRegionData.setGabageTime(Long.parseLong(gabageTime));
					stockerRegionData.setLastEventUser(eventInfo1.getEventUser());
					stockerRegionData.setLastEventComment(eventInfo1.getEventComment());
					stockerRegionData.setLastEventTime(eventInfo1.getEventTime());
					stockerRegionData.setLastEventTimekey(eventInfo1.getEventTimeKey());
					stockerRegionData.setLastEventName(eventInfo1.getEventName());
					
					ExtendedObjectProxy.getDspStockerRegionService().create(eventInfo1, stockerRegionData);
				}
				else
				{
					// Modify
					EventInfo eventInfo2 = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
					
					stockerRegionData = new DspStockerRegion(stockerName, stockerRegionType);
					stockerRegionData.setSetCount(Long.parseLong(setCount));
					stockerRegionData.setThresHoldCount(Long.parseLong(thresholdCount));
					stockerRegionData.setGabageTime(Long.parseLong(gabageTime));
					stockerRegionData.setLastEventUser(eventInfo2.getEventUser());
					stockerRegionData.setLastEventComment(eventInfo2.getEventComment());
					stockerRegionData.setLastEventTime(eventInfo2.getEventTime());
					stockerRegionData.setLastEventTimekey(eventInfo2.getEventTimeKey());
					stockerRegionData.setLastEventName(eventInfo2.getEventName());
					
					ExtendedObjectProxy.getDspStockerRegionService().modify(eventInfo2, stockerRegionData);
				}
			}
		}
		
		return doc;
	}
}
