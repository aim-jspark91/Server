package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerKanban;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteKanban extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element kanbanList = SMessageUtil.getBodySequenceItem(doc, "KANBANLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		if(kanbanList != null)
		{
			for(Object obj : kanbanList.getChildren())
			{
				Element element = (Element)obj;
				String stockerName = SMessageUtil.getChildText(element, "STOCKERNAME", true);
				String zoneName = SMessageUtil.getChildText(element, "ZONENAME", true);
				String kanbanName = SMessageUtil.getChildText(element, "KANBANNAME", true);

				DspStockerKanban stockerKanbanData = null;

//				try
//				{
//					stockerKanbanData = ExtendedObjectProxy.getDspStockerKanbanService().selectByKey(false, new Object[] {stockerName, kanbanName});
//				}
//				catch (Exception ex)
//				{
//					stockerKanbanData = null;
//				}
//
//				if(stockerKanbanData == null)
//				{
//					throw new CustomException("RECIPE-0009", "");
//				}

				stockerKanbanData = new DspStockerKanban(stockerName, zoneName, kanbanName);
				stockerKanbanData.setLastEventUser(eventInfo.getEventUser());
				stockerKanbanData.setLastEventComment(eventInfo.getEventComment());
				stockerKanbanData.setLastEventTime(eventInfo.getEventTime());
				stockerKanbanData.setLastEventTimekey(eventInfo.getEventTimeKey());
				stockerKanbanData.setLastEventName(eventInfo.getEventName());

				ExtendedObjectProxy.getDspStockerKanbanService().remove(eventInfo, stockerKanbanData);
			}
		}
		
		return doc;
	}
}
