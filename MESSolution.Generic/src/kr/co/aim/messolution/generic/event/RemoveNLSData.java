package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class RemoveNLSData extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sNlsName = SMessageUtil.getBodyItemValue(doc, "NLSNAME", true);
		String sNlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);
	    String sEnglish    = SMessageUtil.getBodyItemValue(doc, "ENGLISH", false);
	    String sChinese    = SMessageUtil.getBodyItemValue(doc, "CHINESE", false);
	    String sDescription   = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    
	    String deleteSql = " DELETE FROM CT_NLSDATA "
	    		+ " WHERE 1=1 "
	    		+ " AND NLSNAME = :NLSNAME "
	    		+ " AND NLSTYPE = :NLSTYPE ";
	    
	    Map<String, Object> deleteBindMap = new HashMap<String, Object>();
	    deleteBindMap.put("NLSNAME", sNlsName);
	    deleteBindMap.put("NLSTYPE", sNlsType);
	    deleteBindMap.put("ENGLISH", sEnglish);
	    deleteBindMap.put("CHINESE", sChinese);
	    deleteBindMap.put("DESCRIPTION", sDescription);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(deleteSql, deleteBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(deleteSql, deleteBindMap);
	    
		return doc;
	}

}
