package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class RemoveAlarmActionDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		
		AlarmActionDef actionData = ExtendedObjectProxy.getAlarmActionDefService().selectByKey(false, new Object[] {alarmCode, actionName});
		
		//history trace
		actionData.setLastEventName(eventInfo.getEventName());
		actionData.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		actionData.setLastEventUser(eventInfo.getEventUser());
		actionData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getAlarmActionDefService().remove(eventInfo, actionData);
		
		return doc;
	}
}
