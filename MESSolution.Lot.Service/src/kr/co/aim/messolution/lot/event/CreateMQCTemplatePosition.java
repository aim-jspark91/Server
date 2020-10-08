package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateMQCTemplatePosition extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);
		String mqcCountUp = SMessageUtil.getBodyItemValue(doc, "MQCCOUNTUP", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		MQCTemplatePosition mqcTemplatePosition = null;
		
		try
		{
			mqcTemplateData = ExtendedObjectProxy.getMQCTemplateService().selectByKey(false, new Object[] {mqcTemplateName});
		}
		catch (Exception ex)
		{
			mqcTemplateData = null;
		}
		
		if(mqcTemplateData == null)
		{
			throw new CustomException("MQC-0009", mqcTemplateData.getmqcTemplateName());
		}
		
		try
		{
			mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, processOperationName, processOperationVersion, position});
		}
		catch (Exception ex)
		{
			mqcTemplatePosition = null;
		}
		
		if(mqcTemplatePosition != null)
		{
			throw new CustomException("MQC-0023", position);
		}
		
		mqcTemplatePosition = new MQCTemplatePosition(mqcTemplateName, processOperationName, processOperationVersion, Long.valueOf(position));
		mqcTemplatePosition.setrecipeName(recipeName);
		
		if(StringUtils.isEmpty(mqcCountUp))
		{
			mqcTemplatePosition.setmqcCountUp(0);
		}
		else
		{
			mqcTemplatePosition.setmqcCountUp(Long.valueOf(mqcCountUp));
		}
		
		mqcTemplatePosition.setlastEventUser(eventInfo.getEventUser());
		mqcTemplatePosition.setlastEventComment(eventInfo.getEventComment());
		mqcTemplatePosition.setlastEventTime(eventInfo.getEventTime());
		mqcTemplatePosition.setlastEventTimekey(eventInfo.getEventTimeKey());
		mqcTemplatePosition.setlastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getMQCTemplatePositionService().create(eventInfo, mqcTemplatePosition);
		
		return doc;
	}
}
