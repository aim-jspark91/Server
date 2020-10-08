package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ModifyAlarmDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode});
		
		alarmDef.setAlarmSeverity(alarmSeverity);
		//alarmDef.setAlarmType(alarmType);
		alarmDef.setDescription(description);
		alarmDef.setFactoryName(factoryName);
		
		//history trace
		alarmDef.setLastEventName(eventInfo.getEventName());
		alarmDef.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		alarmDef.setLastEventUser(eventInfo.getEventUser());
		alarmDef.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getAlarmDefinitionService().modify(eventInfo, alarmDef);
		
		return doc;
	}
}
