package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class MQCClearPosition extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryname = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", false);
		String processflowname = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = "00001";		
		String Synopeflag = SMessageUtil.getBodyItemValue(doc, "SYNOPEFLAG", true);
		
		List<Element> elePositionList = SMessageUtil.getBodySequenceItemList(doc, "POSITIONLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClearPosition", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplatePosition mqcTemplatePosition = null;
		
		for(Element elePosition : elePositionList)
		{
			String position = SMessageUtil.getChildText(elePosition, "POSITIONNAME", true);		
			
			try
			{
				if(StringUtil.equals(Synopeflag, "Y"))
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
								mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString(), processOperationVersion, position});
								
								if(mqcTemplatePosition != null)
								{
									ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePosition);
								}
							}
						}
					}				
				}
				else
				{
					mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, processOperationName, processOperationVersion, position});
					ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePosition);
				}
				
			}
			catch (Exception ex)
			{
				mqcTemplatePosition = null;
			}
			
			if(mqcTemplatePosition == null)
			{
				throw new CustomException("MQC-0024", position);
			}		
		}
		
		return doc;
	}
}
