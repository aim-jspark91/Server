package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class CreateCustomQuery extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sQueryID = SMessageUtil.getBodyItemValue(doc, "QUERYID", true);
		String sVersion = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
		String sOpiName = SMessageUtil.getBodyItemValue(doc, "OPINAME", false);
	    String sDescription = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    String sQueryString = SMessageUtil.getBodyItemValue(doc, "QUERYSTRING", false);
	    
	    String insertSql = " INSERT INTO CT_CUSTOMQUERY						"
	    		+ " (QUERYID, VERSION, OPINAME, DESCRIPTION)		"
	    		+ " VALUES														"
	    		+ " (:QUERYID, :VERSION, :OPINAME,:DESCRIPTION )	";
	    
	    Map<String, Object> insertBindMap = new HashMap<String, Object>();
	    insertBindMap.put("QUERYID", sQueryID);
	    insertBindMap.put("VERSION", sVersion);
	    insertBindMap.put("OPINAME", sOpiName);
	    insertBindMap.put("DESCRIPTION", sDescription);
	    insertBindMap.put("QUERYSTRING", sQueryString);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(insertSql, insertBindMap);
	    GenericServiceProxy.getSqlMesTemplate().update(insertSql, insertBindMap);
	    
		return doc;
	}

}
