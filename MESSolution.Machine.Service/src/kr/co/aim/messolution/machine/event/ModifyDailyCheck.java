package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DailyCheck;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyDailyCheck extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String timekey = SMessageUtil.getBodyItemValue(doc, "TIMEKEY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDailyCheck", this.getEventUser(), this.getEventComment(), "", "");
		
		DailyCheck DailyCheckData = null;
		
		try
		{
			DailyCheckData = ExtendedObjectProxy.getDailyCheckService().selectByKey(false, new Object[] {machineName});
		}
		catch (Exception ex)
		{
			DailyCheckData = null;				
		}
		
		DailyCheckData.settime(Integer.parseInt(timekey));
		DailyCheckData.setlasteventuser(eventInfo.getEventUser());
		DailyCheckData.setlasteventcomment(eventInfo.getEventComment());
		DailyCheckData.setlasteventtime(eventInfo.getEventTime());
		DailyCheckData.setlasteventtimekey(eventInfo.getEventTimeKey());
		DailyCheckData.setlasteventname(eventInfo.getEventName());
		
		ExtendedObjectProxy.getDailyCheckService().modify(eventInfo, DailyCheckData);
		
		return doc;
	}
}
