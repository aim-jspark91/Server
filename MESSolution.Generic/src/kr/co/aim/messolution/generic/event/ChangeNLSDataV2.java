package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ChangeNLSDataV2 extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sNlsName = SMessageUtil.getBodyItemValue(doc, "NLSNAME", true);
		String sNlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);
	    String sEnglish    = SMessageUtil.getBodyItemValue(doc, "ENGLISH", false);
	    String sChinese    = SMessageUtil.getBodyItemValue(doc, "CHINESE", false);
	    
	    Map<String, Object> bindMap = new HashMap<String, Object>();
	    bindMap.put("NLSNAME", sNlsName);
	    bindMap.put("NLSTYPE", sNlsType);
	    bindMap.put("ENGLISH", sEnglish);
	    bindMap.put("CHINESE", sChinese);
	    
	    String selectSql = "SELECT * FROM CT_NLSDATA WHERE NLSNAME=:NLSNAME AND NLSTYPE=:NLSTYPE ";
	    
	    int result = GenericServiceProxy.getSqlMesTemplate().update(selectSql, bindMap);
	    
	    if(result==0){
	    	// Create Case
	    	String insertSql ="INSERT INTO CT_NLSDATA (NLSNAME,NLSTYPE,ENGLISH,CHINESE) VALUES (:NLSNAME,:NLSTYPE,:ENGLISH,:CHINESE) ";
	    	GenericServiceProxy.getSqlMesTemplate().update(insertSql, bindMap);
	    }else{
	    	// Modify Case
	    	String modifySql ="UPDATE CT_NLSDATA " +
	    			"   SET ENGLISH = :ENGLISH, CHINESE = :CHINESE " +
	    			" WHERE NLSNAME = :NLSNAME AND NLSTYPE = :NLSTYPE " ;
	    	GenericServiceProxy.getSqlMesTemplate().update(modifySql, bindMap);
	    }
	    
		return doc;
	}

}
