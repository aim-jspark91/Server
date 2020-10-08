package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemoveMQCPositionData extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		String strSqlPosition = "SELECT MQCTEMPLATENAME, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION, POSITION,RECIPENAME,MQCCOUNTUP  FROM CT_MQCTEMPLATEPOSITION where MQCTEMPLATENAME NOT IN ( SELECT MQCTEMPLATENAME FROM CT_MQCTEMPLATE )";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> mqcTemplatePositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlPosition, bindMap);
		
		for(int j = 0; j < mqcTemplatePositionList.size(); j++)
		{
			MQCTemplatePosition mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplatePositionList.get(j).get("MQCTEMPLATENAME"), mqcTemplatePositionList.get(j).get("PROCESSOPERATIONNAME"), mqcTemplatePositionList.get(j).get("PROCESSOPERATIONVERSION"), mqcTemplatePositionList.get(j).get("POSITION")});
			ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePosition);
		}
		
		strSqlPosition = "SELECT MQCTEMPLATENAME, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION, POSITION,RECIPENAME,MQCCOUNTUP  FROM CT_MQCTEMPLATEPOSITIONHIST where MQCTEMPLATENAME NOT IN ( SELECT MQCTEMPLATENAME FROM CT_MQCTEMPLATE )";
		List<Map<String, Object>> mqcTemplatePositionListHist = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlPosition, bindMap);
		
		for(int j = 0; j < mqcTemplatePositionListHist.size(); j++)
		{
		
			Object[] bindList = new Object[] {mqcTemplatePositionListHist.get(j).get("MQCTEMPLATENAME"), mqcTemplatePositionListHist.get(j).get("PROCESSOPERATIONNAME"), mqcTemplatePositionListHist.get(j).get("PROCESSOPERATIONVERSION"), mqcTemplatePositionListHist.get(j).get("POSITION")};
			
			String usrSql = "DELETE CT_MQCTEMPLATEPOSITIONHIST WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION AND POSITION = :POSITION";
			
			kr.co.aim.messolution.generic.GenericServiceProxy.getSqlMesTemplate().update(usrSql, bindList);
		}
		
		return doc;
	}
}
