package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyNLSData extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		// TODO Auto-generated method stub
		String sNlsName = SMessageUtil.getBodyItemValue(doc, "NLSNAME", true);
		String sNlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);
	    String sEnglish    = SMessageUtil.getBodyItemValue(doc, "ENGLISH", false);
	    String sChinese    = SMessageUtil.getBodyItemValue(doc, "CHINESE", false);
	    String sDescription   = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    String sEventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
	    
	    String updateSql = new String();
	    if(sEventName.equals("ModifyNLSEnglish"))
	    {
	    	updateSql = " UPDATE CT_NLSDATA   "
		    		+ " SET "
		    		+ " ENGLISH = :ENGLISH 	 "
		    		+ " WHERE NLSNAME = :NLSNAME  	 "
		    		+ " AND NLSTYPE = :NLSTYPE		 ";
	    	
	    }
	    if(sEventName.equals("ModifyNLSChinese"))
	    {
	    	updateSql = " UPDATE CT_NLSDATA   "
		    		+ " SET "
		    		+ " CHINESE = :CHINESE 	 "
		    		+ " WHERE NLSNAME = :NLSNAME  	 "
		    		+ " AND NLSTYPE = :NLSTYPE		 ";
	    }
	    if(sEventName.equals("ModifyNLSInfo"))
	    {
	    	updateSql = " UPDATE CT_NLSDATA   "
		    		+ " SET "
		    		+ " DESCRIPTION = :DESCRIPTION 	 "
		    		+ " WHERE NLSNAME = :NLSNAME  	 "
		    		+ " AND NLSTYPE = :NLSTYPE		 ";
	    }
	    
	    Map<String, Object> updateBindMap = new HashMap<String, Object>();
	    updateBindMap.put("NLSNAME", sNlsName);
	    updateBindMap.put("NLSTYPE", sNlsType);
	    updateBindMap.put("ENGLISH", sEnglish);
	    updateBindMap.put("CHINESE", sChinese);
	    updateBindMap.put("DESCRIPTION", sDescription);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(updateSql, updateBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateBindMap);
	    
		return doc;
	}

}
