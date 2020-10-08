package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DailyCheck;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteDailyCheck extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element DeleteList = SMessageUtil.getBodySequenceItem(doc, "DELETELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDailyCheck", this.getEventUser(), this.getEventComment(), "", "");
		
		if(DeleteList != null)
		{
			for(Object obj : DeleteList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				DailyCheck DailyCheck =null;
				
				try
				{
					DailyCheck = ExtendedObjectProxy.getDailyCheckService().selectByKey(false, new Object[] {machineName});
				}
				catch (Exception ex)
				{
					DailyCheck = null;
				}
				
				if(DailyCheck == null)
				{
					
				}
				
				ExtendedObjectProxy.getDailyCheckService().remove(eventInfo, DailyCheck);
			}
		}
		
		return doc;
	}
}
