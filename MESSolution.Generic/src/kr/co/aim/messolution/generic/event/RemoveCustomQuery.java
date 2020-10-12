package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class RemoveCustomQuery extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sQueryID = SMessageUtil.getBodyItemValue(doc, "QUERYID", true);
		String sVersion = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
		String sOiName = SMessageUtil.getBodyItemValue(doc, "OINAME", false);
	    String sDescription = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    //String sQueryString = SMessageUtil.getBodyItemValue(doc, "QUERYSTRING", false);
	    String sEventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
	    
	    String deleteSql = " DELETE FROM CT_CUSTOMQUERY "
	    		+ " WHERE 1=1 "
	    		+ " AND QUERYID = :QUERYID "
	    		+ " AND VERSION = :VERSION ";
	    
	    Map<String, Object> deleteBindMap = new HashMap<String, Object>();
	    deleteBindMap.put("QUERYID", sQueryID);
	    deleteBindMap.put("VERSION", sVersion);
	    deleteBindMap.put("OINAME", sOiName);
	    deleteBindMap.put("DESCRIPTION", sDescription);
	    //deleteBindMap.put("QUERYSTRING", sQueryString);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(deleteSql, deleteBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(deleteSql, deleteBindMap);
	    
		return doc;
	}

}
