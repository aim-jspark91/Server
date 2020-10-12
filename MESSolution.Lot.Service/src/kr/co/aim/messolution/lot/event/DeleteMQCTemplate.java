package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMQCTemplate extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element mqcTemplateList = SMessageUtil.getBodySequenceItem(doc, "MQCTEMPLATELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		MQCTemplatePosition mqcTemplatePositionData = null;
		
		if(mqcTemplateList != null)
		{
			for(Object obj : mqcTemplateList.getChildren())
			{
				Element element = (Element)obj;
				String mqcTemplateName = SMessageUtil.getChildText(element, "MQCTEMPLATENAME", true);
				
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
					throw new CustomException("MQC-0009", mqcTemplateName);
				}
				
				try
				{
					String strSql = "SELECT MQCTEMPLATENAME, " +
							"       PROCESSOPERATIONNAME, " +
							"       PROCESSOPERATIONVERSION, " +
							"       POSITION " +
							"  FROM CT_MQCTEMPLATEPOSITION " +
							" WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME ";

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("MQCTEMPLATENAME", mqcTemplateName);

					List<Map<String, Object>> mqcTemplatePositionList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
					
					for( int i = 0; i < mqcTemplatePositionList.size(); i++)
					{
						mqcTemplatePositionData = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, 
																													(String)mqcTemplatePositionList.get(i).get("PROCESSOPERATIONNAME"), 
																													(String)mqcTemplatePositionList.get(i).get("PROCESSOPERATIONVERSION"), 
																													(String)mqcTemplatePositionList.get(i).get("POSITION").toString()});
						
						if(mqcTemplatePositionData != null)
						{
							ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePositionData);
						}
					}
				}
				catch (Exception ex)
				{
					mqcTemplatePositionData = null;
				}
				
				ExtendedObjectProxy.getMQCTemplateService().remove(eventInfo, mqcTemplateData);
			}
		}
		
		return doc;
	}
}
