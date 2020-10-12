package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyErrorDefinition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String sErrorCode = SMessageUtil.getBodyItemValue(doc, "ERRORCODE", true);
		String sDefErrorMsg = SMessageUtil.getBodyItemValue(doc, "LOC_ERRORMSG", false);
		String sEngErrorMsg = SMessageUtil.getBodyItemValue(doc, "ENG_ERRORMSG", false);
	    String sChaErrorMsg = SMessageUtil.getBodyItemValue(doc, "CHA_ERRORMSG", false);
	    String sEventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
	    
	    String updateSql = new String();
	    if(sEventName.equals("ModifyDefault"))
	    {
	    	updateSql = " UPDATE CT_ERRORDEFINITION "
		    		+ " SET "
		    		+ " LOC_ERRORMSG = :LOC_ERRORMSG "
		    		+ " WHERE ERRORCODE = :ERRORCODE ";
	    }
	    if(sEventName.equals("ModifyEnglish"))
	    {
	    	updateSql = " UPDATE CT_ERRORDEFINITION "
		    		+ " SET "
		    		+ " ENG_ERRORMSG = :ENG_ERRORMSG "
		    		+ " WHERE ERRORCODE = :ERRORCODE ";
	    }
	    if(sEventName.equals("ModifyChinese"))
	    {
	    	updateSql = " UPDATE CT_ERRORDEFINITION "
		    		+ " SET "
		    		+ " CHA_ERRORMSG = :CHA_ERRORMSG "
		    		+ " WHERE ERRORCODE = :ERRORCODE ";
	    }
	    
	    
	    Map<String, Object> updateBindMap = new HashMap<String, Object>();
	    updateBindMap.put("ERRORCODE", sErrorCode);
	    updateBindMap.put("LOC_ERRORMSG", sDefErrorMsg);
	    updateBindMap.put("ENG_ERRORMSG", sEngErrorMsg);
	    updateBindMap.put("CHA_ERRORMSG", sChaErrorMsg);
	    
//	    int result = greenFrameServiceProxy.getSqlTemplate().update(updateSql, updateBindMap);
	    int result = GenericServiceProxy.getSqlMesTemplate().update(updateSql, updateBindMap);
	    
		return doc;
	}

}
