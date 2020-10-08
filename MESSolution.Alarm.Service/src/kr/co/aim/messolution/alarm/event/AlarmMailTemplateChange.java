package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmMailTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class AlarmMailTemplateChange extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		String alarmType =  SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		String alarmCode =  SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String title =  SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String comments =  SMessageUtil.getBodyItemValue(doc, "COMMENTS", false);
		String type =  SMessageUtil.getBodyItemValue(doc, "TYPE", true);
		
		try
		{
			AlarmMailTemplate mailTemplateData = ExtendedObjectProxy.getAlarmMailTemplateService().selectByKey(false, new Object[]{alarmType, alarmCode});
			
			if(type.equals("CHANGE"))
			{
				eventInfo.setEventName("Change");
				
				mailTemplateData.setTitle(title);
				mailTemplateData.setComments(comments);
				
				mailTemplateData.setLastEventComment(eventInfo.getEventComment());
				mailTemplateData.setLastEventName(eventInfo.getEventName());
				mailTemplateData.setLastEventTime(eventInfo.getEventTime());
				mailTemplateData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				mailTemplateData.setLastEventUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getAlarmMailTemplateService().modify(eventInfo, mailTemplateData);
			}
			//Delete Case
			else
			{
				ExtendedObjectProxy.getAlarmMailTemplateService().remove(eventInfo, mailTemplateData);
			}
		}
		catch(Exception ex)
		{
			//Create
			eventInfo.setEventName("Create");
			
			AlarmMailTemplate mailTemplateData = new AlarmMailTemplate(alarmType, alarmCode);
			
			mailTemplateData.setTitle(title);
			mailTemplateData.setComments(comments);
			
			mailTemplateData.setLastEventComment(eventInfo.getEventComment());
			mailTemplateData.setLastEventName(eventInfo.getEventName());
			mailTemplateData.setLastEventTime(eventInfo.getEventTime());
			mailTemplateData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mailTemplateData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getAlarmMailTemplateService().create(eventInfo, mailTemplateData);
		}
		
		return doc;
	}

}
