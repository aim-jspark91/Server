package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemoveAlarmDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode});
		ExtendedObjectProxy.getAlarmDefinitionService().remove(eventInfo, alarmDef);
		removeCascading(alarmCode);
		
		return doc;
	}
	
	private void removeCascading(String alarmCode)
	{
		//cascading
		try
		{
			List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select(" alarmCode = ? ORDER BY seq DESC", new Object[] {alarmCode});
			
			for (AlarmActionDef action : actionList)
			{
				try
				{
					ExtendedObjectProxy.getAlarmActionDefService().delete(action);
				}
				catch (Exception ce)
				{
					eventLog.error(String.format("AlarmCode[%s] [%d]th action has not removed", action.getAlarmCode(), action.getSeq()));
				}
			}
		}
		catch (Exception ce)
		{
			eventLog.error(String.format("AlarmCode[%s] action set have not removed", alarmCode));
		}
	}
}
