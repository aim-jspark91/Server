package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
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
import kr.co.aim.greentrack.name.NameServiceProxy;

import org.jdom.Document;
import org.jdom.Element;


public class MQCTemplatePositinSetting extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryname = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", false);
		String mqcflowName = SMessageUtil.getBodyItemValue(doc, "MQCFLOWNAME", false);
		//String PositionList = SMessageUtil.getBodyItemValue(doc, "POSITIONLIST", false);
		List<Element> PositionList = SMessageUtil.getBodySequenceItemList(doc, "POSITIONLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SettingPosition", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		
		try
		{
			mqcTemplateData = ExtendedObjectProxy.getMQCTemplateService().selectByKey(false, new Object[] {mqcTemplateName});
		}
		catch (Exception ex)
		{
			mqcTemplateData = null;
			// return;
		}
		
		List<String> argSeq = new ArrayList<String>();
		List<String> tempmqcTemplateName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("MQCTemplateNaming", argSeq, 1);
		String TempmqcTemplateName = tempmqcTemplateName.get(0);
		
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
		newbindMap.put("PROCESSFLOWNAME", mqcflowName);
		newbindMap.put("FACTORYNAME", factoryname);
		newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
		
		if (newsqlResult.size() > 0) 
		{
			for( int k=1; k< newsqlResult.size(); k++) // Operation List
			{
				if(!StringUtil.equals(newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), ""))
				{
					String Check_Query = "SELECT PROCESSOPERATIONNAME, POSITION, RECIPENAME, MQCCOUNTUP, PROCESSFLAG FROM CT_MQCTEMPLATEPOSITION WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ORDER BY POSITION ASC";
					newbindMap.clear();
					newbindMap.put("MQCTEMPLATENAME", mqcTemplateName);
					newbindMap.put("PROCESSOPERATIONNAME", newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString());
					List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Check_Query, newbindMap);
										
					if (sqlResult.size() > 0) 
					{
						for( int m=0; m< sqlResult.size(); m++)
						{
							for(Element position : PositionList){
								String _position = SMessageUtil.getChildText(position, "POSITION", true);
								
								if(StringUtil.equals(sqlResult.get(m).get("POSITION").toString().trim(),_position))
								{
									if(sqlResult.get(m).get("RECIPENAME") != null)
									{
										boolean result = this.SettingPosition(TempmqcTemplateName, sqlResult.get(m).get("PROCESSOPERATIONNAME").toString().trim(), "00001", Long.valueOf(sqlResult.get(m).get("POSITION").toString().trim()), sqlResult.get(m).get("RECIPENAME").toString(), sqlResult.get(m).get("MQCCOUNTUP").toString(), eventInfo, sqlResult.get(m).get("PROCESSFLAG").toString());
									}
									else
									{
										boolean result = this.SettingPosition(TempmqcTemplateName, sqlResult.get(m).get("PROCESSOPERATIONNAME").toString().trim(), "00001", Long.valueOf(sqlResult.get(m).get("POSITION").toString().trim()), "", sqlResult.get(m).get("MQCCOUNTUP").toString(), eventInfo, sqlResult.get(m).get("PROCESSFLAG").toString());
									}
								}
								
							}
							
						}
					}			
				}
			}
		}
		
		SMessageUtil.addItemToBody(doc, "NEWMQCTEMPLATENAME", TempmqcTemplateName);
		
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
