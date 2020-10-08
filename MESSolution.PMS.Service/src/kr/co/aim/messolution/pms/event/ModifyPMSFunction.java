package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyPMSFunction extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
      
		String EnumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);           //ENUMNAME
        String ButtonTagname = SMessageUtil.getBodyItemValue(doc, "BUTTONTAGNAME", true); //ENUMVALUE
        String ToButtonTagname = SMessageUtil.getBodyItemValue(doc, "TOBUTTONTAGNAME", true);
        String FormName =  SMessageUtil.getBodyItemValue(doc, "FORMNAME", true);          //TAG
        String Description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);     //DESC
        
      //Check existence ButtonTagname
        
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
		
		//Check existence ToButtonTagname
		
        String sqlcheck = "SELECT ENUMNAME, ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumname AND ENUMVALUE = :enumvalue ";
		
        bindMap.clear();
		bindMap.put("enumname", EnumName);
		bindMap.put("enumvalue", ToButtonTagname);
		
		List<Map<String, Object>> sqlResultCheck = null ;
		
		try
		{			
			sqlResultCheck = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}	
					
		if(sqlResultCheck.size() > 0)
		{
			throw new CustomException("PMSFUCTION-0003", EnumName, ButtonTagname);
		}
		
		
		//Update ToButtonTagname
		
		String sqlUpdate = "UPDATE ENUMDEFVALUE E SET E.ENUMVALUE = :enumvalueto WHERE E.ENUMNAME = :enumname AND E.ENUMVALUE = :enumvalue AND E.TAG = :tag ";
		
		bindMap.clear();
		bindMap.put("enumname", EnumName);
		bindMap.put("enumvalue", ButtonTagname);
		bindMap.put("enumvalueto", ToButtonTagname);	
		bindMap.put("description", Description);
		bindMap.put("tag", FormName);
			
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sqlUpdate, bindMap);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSFUCTION-0004", EnumName, ButtonTagname);
		}	

		return doc;
	}
	
}
