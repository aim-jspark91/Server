package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class DeletePMSFunction extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
      
		String EnumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);           //ENUMNAME
        String ButtonTagname = SMessageUtil.getBodyItemValue(doc, "BUTTONTAGNAME", true); //ENUMVALUE
        
        String sql = "SELECT ENUMNAME, ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumname AND ENUMVALUE = :enumvalue ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumname", EnumName);
		bindMap.put("enumvalue", ButtonTagname);
		
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
			throw new CustomException("PMSFUCTION-0003", EnumName, ButtonTagname);
			
		}
		
		String sqlDelete = "DELETE FROM ENUMDEFVALUE E WHERE E.ENUMNAME = :enumname AND E.ENUMVALUE = :enumvalue ";
		
		bindMap.clear();
		bindMap.put("enumname", EnumName);
		bindMap.put("enumvalue", ButtonTagname);
					
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sqlDelete, bindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSFUCTION-0002", EnumName, ButtonTagname);
		}	

		return doc;
	}
	
}
