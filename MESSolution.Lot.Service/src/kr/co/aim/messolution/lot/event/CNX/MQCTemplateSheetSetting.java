package kr.co.aim.messolution.lot.event.CNX;

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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MQCTemplateSheetSetting extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryname = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		String processflowname = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = "00001";
		String processflag = SMessageUtil.getBodyItemValue(doc, "PROCESSFLAG", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String mqcCountUp = SMessageUtil.getBodyItemValue(doc, "MQCCOUNTUP", false);
		String synopeflag = SMessageUtil.getBodyItemValue(doc, "SYNOPEFLAG", true);
		//String DefaultRecipe = SMessageUtil.getBodyItemValue(doc, "DEFAULTRECIPE", true);
		List<Element> elePositionList = SMessageUtil.getBodySequenceItemList(doc, "POSITIONLIST", true);
		//List<Element> eleRecipeList = SMessageUtil.getBodySequenceItemList(doc, "RECIPELIST", true);
		//String temprecipeName = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		
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
		
		for(Element elePosition : elePositionList)
		{
			String position = SMessageUtil.getChildText(elePosition, "POSITIONNAME", true).trim();

			if(!StringUtil.equals(mqcCountUp, "0"))
			{
				String tsql = "SELECT PROCESSOPERATIONNAME, POSITION, MQCCOUNTUP FROM CT_MQCTEMPLATEPOSITION WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME AND POSITION = :POSITION";
				Map<String, String> checkbindMap = new HashMap<String, String>();
				checkbindMap.put("MQCTEMPLATENAME", mqcTemplateName);
				checkbindMap.put("POSITION", position);
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> checksqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(tsql, checkbindMap);
				if (checksqlResult.size() > 0) 
				{
					for(int i=0; i<checksqlResult.size(); i++)
					{
						if(!StringUtil.equals(checksqlResult.get(i).get("PROCESSOPERATIONNAME").toString(), processOperationName))
						{
							if(!StringUtil.equals(checksqlResult.get(i).get("MQCCOUNTUP").toString(), "0"))
							{
								throw new CustomException("MQC-0047");
							}
						}
					}
				}
			}
					
			if(synopeflag.toUpperCase().equals("Y"))
			{
				String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
				sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
				sql += " FROM ARC A,";
				sql += " NODE N,";
				sql += " PROCESSFLOW PF";
				sql += " WHERE 1 = 1";
				sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
				sql += " AND N.FACTORYNAME = :FACTORYNAME";
				sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
				sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
				sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
				sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
				sql += " AND A.FROMNODEID = N.NODEID)";
				sql += " START WITH NODETYPE = :NODETYPE";
				sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
				Map<String, String> newbindMap = new HashMap<String, String>();
				newbindMap.put("PROCESSFLOWNAME", processflowname);
				newbindMap.put("FACTORYNAME", factoryname);
				newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
				if (newsqlResult.size() > 0) 
				{
					for( int k=1; k< newsqlResult.size(); k++)
					{
						if(!StringUtil.equals(newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), ""))
						{
							
							if(StringUtil.equals(newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), processOperationName))
							{ 			
								boolean result = this.SettingPosition(mqcTemplateName, newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), processOperationVersion, Long.valueOf(position), recipeName, mqcCountUp, eventInfo, processflag);
							}
							else {
								boolean result = this.SettingPosition(mqcTemplateName, newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), processOperationVersion, Long.valueOf(position), StringUtils.EMPTY, "" , eventInfo, processflag);
							}
						}
					}
				}
			}
			else
			{
				boolean result = this.SettingPosition(mqcTemplateName, processOperationName, processOperationVersion, Long.valueOf(position), recipeName, mqcCountUp, eventInfo, processflag);	
			}		
		}
			
		return doc;
	}
	
	private boolean SettingPosition(String mqcTemplateName, String processOperationName, String processOperationVersion, long position, String recipeName, String mqcCountUp, EventInfo eventInfo, String processflag) throws CustomException
	{
		MQCTemplatePosition mqcTemplatePosition = null;
	
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
			// update case
			mqcTemplatePosition = new MQCTemplatePosition(mqcTemplateName, processOperationName, processOperationVersion, Long.valueOf(position));
			mqcTemplatePosition.setrecipeName(recipeName);
			
			if( !StringUtil.equals(mqcCountUp, "") && !StringUtil.equals(mqcCountUp, "0"))
			{
				mqcTemplatePosition.setmqcCountUp(Long.valueOf(mqcCountUp));
			}
			
			mqcTemplatePosition.setprocessflag(processflag);
			mqcTemplatePosition.setlastEventUser(eventInfo.getEventUser());
			mqcTemplatePosition.setlastEventComment(eventInfo.getEventComment());
			mqcTemplatePosition.setlastEventTime(eventInfo.getEventTime());
			mqcTemplatePosition.setlastEventTimekey(eventInfo.getEventTimeKey());
			mqcTemplatePosition.setlastEventName(eventInfo.getEventName());
			ExtendedObjectProxy.getMQCTemplatePositionService().modify(eventInfo, mqcTemplatePosition);		
		}
		else 
		{	
			// create case
			mqcTemplatePosition = new MQCTemplatePosition(mqcTemplateName, processOperationName, processOperationVersion, Long.valueOf(position));
			mqcTemplatePosition.setrecipeName(recipeName);
			if(!StringUtil.equals(mqcCountUp, "") && !StringUtil.equals(mqcCountUp, "0"))
			{
				mqcTemplatePosition.setmqcCountUp(Long.valueOf(mqcCountUp));
			}
			mqcTemplatePosition.setprocessflag(processflag);
			mqcTemplatePosition.setlastEventUser(eventInfo.getEventUser());
			mqcTemplatePosition.setlastEventComment(eventInfo.getEventComment());
			mqcTemplatePosition.setlastEventTime(eventInfo.getEventTime());
			mqcTemplatePosition.setlastEventTimekey(eventInfo.getEventTimeKey());
			mqcTemplatePosition.setlastEventName(eventInfo.getEventName());
			ExtendedObjectProxy.getMQCTemplatePositionService().create(eventInfo, mqcTemplatePosition);			
		}	
		return true;
	}
}
