package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyCustomQuery extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sQueryID = SMessageUtil.getBodyItemValue(doc, "QUERYID", true);
		String sVersion = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
		String sOiName = SMessageUtil.getBodyItemValue(doc, "OPINAME", false);
	    String sDescription = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    String sQueryString = SMessageUtil.getBodyItemValue(doc, "QUERYSTRING", false);
	    String sEventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
	    
	    StringBuilder sql = new StringBuilder();
	    
	    
	    if( sEventName.equals("ModifyInfo") )
	    {
	    	sql.append(" UPDATE CT_CUSTOMQUERY ");
	    	sql.append(" SET ");
	    	sql.append(" OPINAME = :OPINAME, ");
	    	sql.append(" DESCRIPTION = :DESCRIPTION ");
	    	sql.append(" WHERE QUERYID = :QUERYID ");
	    	sql.append(" AND VERSION = :VERSION ");
	    }
	    if(sEventName.equals("ModifyQuery"))
	    {
	    	if(sQueryString.length()>3999){
	    		sql.append(" UPDATE CT_CUSTOMQUERY ");
	    		sql.append(" SET ");
	    		sql.append(" QUERYSTRINGCLOB = :QUERYSTRING ");
	    		sql.append(" WHERE QUERYID = :QUERYID ");
	    		sql.append(" AND VERSION = :VERSION ");
	    		
	    	}else{
	    		sql.append(" UPDATE CT_CUSTOMQUERY ");
	    		sql.append(" SET ");
	    		sql.append(" QUERYSTRING = :QUERYSTRING, ");
	    		sql.append(" QUERYSTRINGCLOB = :QUERYSTRING ");
	    		sql.append(" WHERE QUERYID = :QUERYID ");
	    		sql.append(" AND VERSION = :VERSION ");
	    	}
	    }
	    
	    Map<String, Object> updateBindMap = new HashMap<String, Object>();
	    updateBindMap.put("QUERYID", sQueryID);
	    updateBindMap.put("VERSION", sVersion);
	    updateBindMap.put("OPINAME", sOiName);
	    updateBindMap.put("DESCRIPTION", sDescription);
	    updateBindMap.put("QUERYSTRING", sQueryString);
	    
	    int result = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), updateBindMap);
	    
		return doc;
	}

}
