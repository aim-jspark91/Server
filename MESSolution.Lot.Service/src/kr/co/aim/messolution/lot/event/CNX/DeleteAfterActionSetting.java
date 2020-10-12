package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AfterAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteAfterActionSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element afterActionSettingList = SMessageUtil.getBodySequenceItem(doc, "AFTERACTIONSETTINGLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteAfterActionSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		if(afterActionSettingList != null)
		{
			for(Object obj : afterActionSettingList.getChildren())
			{
				Element element = (Element)obj;
				String productSpecName =SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
				String productSpecVersion =SMessageUtil.getChildText(element, "PRODUCTSPECVERSION", true);
				String holdFlag = SMessageUtil.getChildText(element, "HOLDFLAG", true);
				String mailFlag = SMessageUtil.getChildText(element, "MAILFLAG", true);

				
				AfterAction afterAction =null;
				
				try
				{
					afterAction = ExtendedObjectProxy.getAfterActionService().selectByKey(false, new Object[] {productSpecName, productSpecVersion });
				}
				catch (Exception ex)
				{
					afterAction = null;
				}
				
				if(afterAction == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				afterAction.setLastEventComment(eventInfo.getEventComment());
				afterAction.setLastEventName(eventInfo.getEventName());
				afterAction.setLastEventTime(eventInfo.getEventTime());
				afterAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				afterAction.setLastEventUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getAfterActionService().remove(eventInfo, afterAction);
			}
		}
		
		return doc;
	}
}
