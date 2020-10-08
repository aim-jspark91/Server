package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyMQCTemplatePosition extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "NEWRECIPENAME", false);
		String mqcCountUp = SMessageUtil.getBodyItemValue(doc, "NEWMQCCOUNTUP", false);
		String newposition = SMessageUtil.getBodyItemValue(doc, "NEWPOSITION", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplatePosition mqcTemplatePosition = null;
		
		try
		{
			mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, processOperationName, processOperationVersion, position});
		}
		catch (Exception ex)
		{
			mqcTemplatePosition = null;
		}
		
		if(mqcTemplatePosition == null)
		{
			throw new CustomException("MQC-0024", position);
		}
		
		if(!StringUtils.isEmpty(recipeName))
		{
			mqcTemplatePosition.setrecipeName(recipeName);
		}
		
		if(!StringUtils.isEmpty(mqcCountUp))
		{
			mqcTemplatePosition.setmqcCountUp(Long.valueOf(mqcCountUp));
		}
		
//		if(!StringUtils.isEmpty(newposition))
//		{
//			mqcTemplatePosition.setposition(Long.valueOf(newposition));
//		}
		
		mqcTemplatePosition.setlastEventUser(eventInfo.getEventUser());
		mqcTemplatePosition.setlastEventComment(eventInfo.getEventComment());
		mqcTemplatePosition.setlastEventTime(eventInfo.getEventTime());
		mqcTemplatePosition.setlastEventTimekey(eventInfo.getEventTimeKey());
		mqcTemplatePosition.setlastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getMQCTemplatePositionService().modify(eventInfo, mqcTemplatePosition);
		
		String sql = "UPDATE CT_MQCTemplatePosition SET POSITION = :NEWPOSITION WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME AND PROCESSOPERATIONNAME  = :PROCESSOPERATIONNAME AND PROCESSOPERATIONVERSION = '00001' AND POSITION = :POSITION";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("NEWPOSITION", newposition);
		bindMap.put("MQCTEMPLATENAME", mqcTemplateName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("POSITION", position);

		@SuppressWarnings("unchecked")
		int temp = GenericServiceProxy.getSqlMesTemplate().update(sql,bindMap);
			
		return doc;
	}
}
