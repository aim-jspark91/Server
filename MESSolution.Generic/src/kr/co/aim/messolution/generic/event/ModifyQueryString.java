package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyQueryString extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sQueryID = SMessageUtil.getBodyItemValue(doc, "QUERYID", true);
		String sVersion = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
	    String sQueryString = SMessageUtil.getBodyItemValue(doc, "QUERYSTRING", true);
	    
	    String updateSql = " UPDATE CT_CUSTOMQUERY "
	    		+ " SET "
	    		+ " QUERYSTRING = :QUERYSTRING, "
	    		+ " QUERYSTRINGCLOB = :QUERYSTRING "
	    		+ " WHERE QUERYID = :QUERYID "
	    		+ " AND VERSION = :VERSION ";
	    
	    Map<String, Object> updateBindMap = new HashMap<String, Object>();
	    updateBindMap.put("QUERYID", sQueryID);
	    updateBindMap.put("VERSION", sVersion);
	    updateBindMap.put("QUERYSTRING", sQueryString);
	    
	    GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateBindMap);
	    
		return doc;
	}

}
