package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AfterAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateAfterActionSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{

		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String holdFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
		String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", true);

		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateAfterActionSetting", this.getEventUser(), this.getEventComment(), "", "");

		AfterAction afterAction = null;
		
		try
		{
			afterAction = ExtendedObjectProxy.getAfterActionService().selectByKey(false, new Object[] {productSpecName, productSpecVersion });
					
		}
		catch (Exception ex)
		{
			afterAction = null;
			
		}
		
		if(afterAction != null)
		{
			throw new CustomException("IDLE-0005", "");
		}

		afterAction = new AfterAction(productSpecName, productSpecVersion);
		
		afterAction.setHoldFlag(holdFlag);
		afterAction.setMailFlag(mailFlag);
		
		afterAction.setLastEventComment(eventInfo.getEventComment());
		afterAction.setLastEventName(eventInfo.getEventName());
		afterAction.setLastEventTime(eventInfo.getEventTime());
		afterAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
		afterAction.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getAfterActionService().create(eventInfo, afterAction);
		
		return doc;
	}
}
