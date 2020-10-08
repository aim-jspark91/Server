package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class DeleteNLSDataV2 extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sNlsName = SMessageUtil.getBodyItemValue(doc, "NLSNAME", true);
		String sNlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);

	    
	    Map<String, Object> bindMap = new HashMap<String, Object>();
	    bindMap.put("NLSNAME", sNlsName);
	    bindMap.put("NLSTYPE", sNlsType);

	    String deleteSql ="DELETE FROM CT_NLSDATA WHERE NLSNAME=:NLSNAME AND NLSTYPE=:NLSTYPE ";
	    
	    int result = GenericServiceProxy.getSqlMesTemplate().update(deleteSql, bindMap);
	    
	    return doc;
	}

}
