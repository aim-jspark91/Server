package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMQCTemplatePosition extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element mqcTemplatePositionList = SMessageUtil.getBodySequenceItem(doc, "MQCTEMPLATEPOSITIONLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplatePosition mqcTemplatePosition = null;
		
		if(mqcTemplatePositionList != null)
		{
			for(Object obj : mqcTemplatePositionList.getChildren())
			{
				Element element = (Element)obj;
				String mqcTemplateName = SMessageUtil.getChildText(element, "MQCTEMPLATENAME", true);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				String processOperationVersion = SMessageUtil.getChildText(element, "PROCESSOPERATIONVERSION", true);
				String position = SMessageUtil.getChildText(element, "POSITION", true);
				
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
				
				ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePosition);
			}
		}
		
		return doc;
	}
}
