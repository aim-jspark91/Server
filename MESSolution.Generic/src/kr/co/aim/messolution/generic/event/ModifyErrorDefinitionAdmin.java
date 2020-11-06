package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyErrorDefinitionAdmin extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sErrorCode = SMessageUtil.getBodyItemValue(doc, "ERRORCODE", true);
		String sEngErrorMsg = SMessageUtil.getBodyItemValue(doc, "ENG_ERRORMSG", true);
	    String sChaErrorMsg = SMessageUtil.getBodyItemValue(doc, "CHA_ERRORMSG", true);
	    
	    StringBuilder sql = new StringBuilder();
	    sql.append(" UPDATE CT_ERRORDEFINITION ");
	    sql.append(" SET ");
	    sql.append(" ENG_ERRORMSG = :ENG_ERRORMSG ,");
	    sql.append(" CHA_ERRORMSG = :CHA_ERRORMSG ");
	    sql.append(" WHERE ERRORCODE = :ERRORCODE  ");

	    Map<String, Object> updateBindMap = new HashMap<String, Object>();
	    updateBindMap.put("ERRORCODE", sErrorCode);
	    updateBindMap.put("ENG_ERRORMSG", sEngErrorMsg);
	    updateBindMap.put("CHA_ERRORMSG", sChaErrorMsg);
	    
	    GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), updateBindMap);
	    
		return doc;
	}

}
