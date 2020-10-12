package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class CreateErrorDefinition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub

		String sErrorCode = SMessageUtil.getBodyItemValue(doc, "ERRORCODE", true);
		String sDefErrorMsg = SMessageUtil.getBodyItemValue(doc, "LOC_ERRORMSG", false);
		String sEngErrorMsg = SMessageUtil.getBodyItemValue(doc, "ENG_ERRORMSG", false);
	    String sChaErrorMsg = SMessageUtil.getBodyItemValue(doc, "CHA_ERRORMSG", false);
	    
	    String insertSql = " INSERT INTO CT_ERRORDEFINITION						"
	    		+ " (ERRORCODE)		"
	    		+ " VALUES														"
	    		+ " (:ERRORCODE )	";
	    
	    Map<String, Object> insertBindMap = new HashMap<String, Object>();
	    insertBindMap.put("ERRORCODE", sErrorCode);
	    insertBindMap.put("LOC_ERRORMSG", sDefErrorMsg);
	    insertBindMap.put("ENG_ERRORMSG", sEngErrorMsg);
	    insertBindMap.put("CHA_ERRORMSG", sChaErrorMsg);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(insertSql, insertBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(insertSql, insertBindMap);
	    
		return doc;
	}

}
