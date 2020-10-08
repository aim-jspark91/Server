
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
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateAlarmActionDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String sequence = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		//get spec
		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode});
		
		AlarmActionDef actionData = new AlarmActionDef(alarmDef.getAlarmCode(), actionName);
		
		actionData.setReasonCodeType(reasonCodeType);
		actionData.setReasonCode(reasonCode);
		
		//history trace
		actionData.setLastEventName(eventInfo.getEventName());
		actionData.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		actionData.setLastEventUser(eventInfo.getEventUser());
		actionData.setLastEventComment(eventInfo.getEventComment());
		
		//assign index
		try
		{
			actionData.setSeq(Long.parseLong(sequence));
			actionData.setDepartment(department);
		}
		catch (Exception ex) //if there is no sequence
		{	
			try
			{
				List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select(" alarmCode = ? ORDER BY seq DESC", new Object[] {alarmCode});
				
				for (AlarmActionDef action : actionList)
				{
					long lastIndex = action.getSeq();
					
					actionData.setSeq(lastIndex++);
					
					break;
				}
			}
			catch (Exception ce) // if there is not any alarm action assigned to alarm definition
			{
				actionData.setSeq(1);
			}
			
		}
		
		ExtendedObjectProxy.getAlarmActionDefService().create(eventInfo, actionData);
		
		return doc;
	}
}
