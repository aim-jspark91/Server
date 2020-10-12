package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class CreateNLSData extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sNlsName = SMessageUtil.getBodyItemValue(doc, "NLSNAME", true);
		String sNlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);
	    String sEnglish    = SMessageUtil.getBodyItemValue(doc, "ENGLISH", false);
	    String sChinese    = SMessageUtil.getBodyItemValue(doc, "CHINESE", false);
	    String sDescription   = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	    
	    String insertSql = " INSERT INTO CT_NLSDATA							"
		    		+ " (NLSNAME, NLSTYPE,ENGLISH,CHINESE)		"
		    		+ " VALUES													"
		    		+ " (:NLSNAME, :NLSTYPE,:ENGLISH,:CHINESE)	";
	   
	    
	    Map<String, Object> insertBindMap = new HashMap<String, Object>();
	    insertBindMap.put("NLSNAME", sNlsName);
	    insertBindMap.put("NLSTYPE", sNlsType);
	    insertBindMap.put("ENGLISH", sEnglish);
	    insertBindMap.put("CHINESE", sChinese);
	    insertBindMap.put("DESCRIPTION", sDescription);

	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(insertSql, insertBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(insertSql, insertBindMap);
	    
		return doc;
	}

}
