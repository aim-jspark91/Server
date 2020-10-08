package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyUserGrade extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String bstateType   = SMessageUtil.getBodyItemValue(doc, "BFORMNAME", true);
		String bstateCode   = SMessageUtil.getBodyItemValue(doc, "BSTATECODE", true);
		String brank        = SMessageUtil.getBodyItemValue(doc, "BRANK", true);
		String buserGroup   = SMessageUtil.getBodyItemValue(doc, "BUSERGROUP", true);	
		
		String stateType   = SMessageUtil.getBodyItemValue(doc, "FORMNAME", true);
		String stateCode   = SMessageUtil.getBodyItemValue(doc, "STATECODE", true);
		String rank        = SMessageUtil.getBodyItemValue(doc, "RANK", true);
		String userGroup   = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);	
			
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("ModifyUserGrade", getEventUser(), getEventComment(), null, null);
		
        //Check existence 
        
        String sql = "SELECT STATETYPE, STATECODE, RANK, DEPT FROM PMS_USERGRADE WHERE STATETYPE = :statetype AND STATECODE = :statecode AND RANK = :rank AND DEPT = :dept ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("statetype", bstateType);
		bindMap.put("statecode", bstateCode);
		bindMap.put("rank", brank);
		bindMap.put("dept", buserGroup);
		
		List<Map<String, Object>> sqlResult = null ;
		try
		{			
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}	
		
		if(sqlResult.size() < 0)
		{
			throw new CustomException("PMSUserGrade-0005", bstateType, bstateCode,brank,buserGroup);
		}
			
		//Update 
		String sqlUpdate = "UPDATE PMS_USERGRADE PU SET PU.STATETYPE = :tostatetype, PU.STATECODE = :tostatecode, PU.RANK = :torank, PU.DEPT = :todept  WHERE PU.STATETYPE = :statetype AND PU.STATECODE = :statecode AND PU.RANK = :rank AND PU.DEPT = :dept ";
		
		bindMap.clear();
		bindMap.put("tostatetype", stateType);
		bindMap.put("tostatecode", stateCode);
		bindMap.put("torank", rank);
		bindMap.put("todept", userGroup);
		
		bindMap.put("statetype", bstateType);
		bindMap.put("statecode", bstateCode);
		bindMap.put("rank", brank);
		bindMap.put("dept", buserGroup);
		
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sqlUpdate, bindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSUserGrade-0004", stateType,stateCode,rank,userGroup);
		}		
		return doc;
	}
}
