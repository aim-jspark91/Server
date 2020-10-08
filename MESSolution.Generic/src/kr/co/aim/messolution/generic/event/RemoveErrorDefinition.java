package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class RemoveErrorDefinition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub

		String sErrorCode = SMessageUtil.getBodyItemValue(doc, "ERRORCODE", true);
		String sDefErrorMsg = SMessageUtil.getBodyItemValue(doc, "LOC_ERRORMSG", false);
		String sEngErrorMsg = SMessageUtil.getBodyItemValue(doc, "ENG_ERRORMSG", false);
	    String sChaErrorMsg = SMessageUtil.getBodyItemValue(doc, "CHA_ERRORMSG", false);
	    String sEventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
	    
	    String deleteSql = " DELETE FROM CT_ERRORDEFINITION "
	    		+ " WHERE 1=1 "
	    		+ " AND ERRORCODE = :ERRORCODE ";
	    
	    Map<String, Object> deleteBindMap = new HashMap<String, Object>();
	    deleteBindMap.put("ERRORCODE", sErrorCode);
	    deleteBindMap.put("LOC_ERRORMSG", sDefErrorMsg);
	    deleteBindMap.put("ENG_ERRORMSG", sEngErrorMsg);
	    deleteBindMap.put("CHA_ERRORMSG", sChaErrorMsg);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(deleteSql, deleteBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(deleteSql, deleteBindMap);
	    
		return doc;
		
	}

}
