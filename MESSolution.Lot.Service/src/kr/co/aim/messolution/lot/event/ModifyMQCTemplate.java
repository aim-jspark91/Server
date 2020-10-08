package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyMQCTemplate extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		String newProcessFlowName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSFLOWNAME", false);
		String newProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSFLOWVERSION", false);
		//String newProductQuantity = SMessageUtil.getBodyItemValue(doc, "NEWPRODUCTQUANTITY", false);
		//String newMqcCountLimit = SMessageUtil.getBodyItemValue(doc, "NEWMQCCOUNTLIMIT", false);
		String newDescription = SMessageUtil.getBodyItemValue(doc, "NEWDESCRIPTION", false);
		
		String newProductspec = SMessageUtil.getBodyItemValue(doc, "NEWPRODUCTSPEC", false);
		String newEccode = SMessageUtil.getBodyItemValue(doc, "NEWECCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		MQCTemplatePosition mqcTemplatePositionData = null;
		
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

			List<Map<String, Object>> mqcTemplatePositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
			
			for( int i = 0; i < mqcTemplatePositionList.size(); i++)
			{
				mqcTemplatePositionData = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, 
																											(String)mqcTemplatePositionList.get(i).get("PROCESSOPERATIONNAME"), 
																											(String)mqcTemplatePositionList.get(i).get("PROCESSOPERATIONVERSION"), 
																											(String)mqcTemplatePositionList.get(i).get("POSITION")});
				
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
		
		if(!StringUtils.isEmpty(newProductspec))
		{
			mqcTemplateData.setproductspecname(newProductspec);
		}
		if(!StringUtils.isEmpty(newEccode))
		{
			mqcTemplateData.seteccode(newEccode);
		}
	
		if(!StringUtils.isEmpty(newDescription))
		{
			mqcTemplateData.setdescription(newDescription);
		}
		
		if(!StringUtils.isEmpty(newProcessFlowName))
		{
			mqcTemplateData.setprocessFlowName(newProcessFlowName);
		}
		
		if(!StringUtils.isEmpty(newProcessFlowVersion))
		{
			mqcTemplateData.setprocessFlowVersion(newProcessFlowVersion);
		}
		
//		if(!StringUtils.isEmpty(newProductQuantity))
//		{
//			mqcTemplateData.setproductQuantity(Long.valueOf(newProductQuantity));
//		}
//		
//		if(!StringUtils.isEmpty(newMqcCountLimit))
//		{
//			mqcTemplateData.setmqcCountLimit(Long.valueOf(newMqcCountLimit));
//		}
		
		mqcTemplateData.setlastEventUser(eventInfo.getEventUser());
		mqcTemplateData.setlastEventComment(eventInfo.getEventComment());
		mqcTemplateData.setlastEventTime(eventInfo.getEventTime());
		mqcTemplateData.setlastEventTimekey(eventInfo.getEventTimeKey());
		mqcTemplateData.setlastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getMQCTemplateService().modify(eventInfo, mqcTemplateData);
		
		return doc;
	}
}
