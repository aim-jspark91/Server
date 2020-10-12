package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SetMQCJobGlass extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcJobName = SMessageUtil.getBodyItemValue(doc, "MQCJOBNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMQCJobGlass", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCJobPosition mqcJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[] {mqcJobName, processOperationName, processOperationVersion, position});
			
		if(mqcJobPosition != null)
		{
			MQCJob mqcJob = null;
			try
			{
				mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
			}
			catch (Exception ex)
			{
				mqcJob = null;
			}
			
			if(mqcJob == null)
			{
				throw new CustomException("MQC-0031", mqcJobName);
			}
			
			if(StringUtils.equals(mqcJob.getmqcState(), "Executing"))
			{
				throw new CustomException("MQC-0041", mqcJobName);
			}
			
			if(StringUtils.isEmpty(mqcJobPosition.getrecipeName()))
			{
				if(StringUtils.isEmpty(recipeName))
				{
					throw new CustomException("MQC-0040", "");
				}
			}
			else
			{
				if(StringUtils.equals(mqcJobPosition.getrecipeName(), recipeName))
				{
					throw new CustomException("MQC-0040", "");
				}
			}
			
			mqcJobPosition.setrecipeName(recipeName);
			mqcJobPosition.setLastEventUser(eventInfo.getEventUser());
			mqcJobPosition.setLastEventComment(eventInfo.getEventComment());
			mqcJobPosition.setLastEventTime(eventInfo.getEventTime());
			mqcJobPosition.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mqcJobPosition.setLastEventName(eventInfo.getEventName());
			ExtendedObjectProxy.getMQCJobPositionService().modify(eventInfo, mqcJobPosition);
		}
		
		return doc;
	}
}
