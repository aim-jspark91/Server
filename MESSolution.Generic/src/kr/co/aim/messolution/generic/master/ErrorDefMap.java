/**
 * 
 */
package kr.co.aim.messolution.generic.master;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.greenframe.greenFrameServiceProxy;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author sjlee
 *
 */
public class ErrorDefMap extends HashMap<String, ErrorDef> implements
		ApplicationContextAware {

	private static Log 		   log = LogFactory.getLog(ErrorDefMap.class);
	/**
	 * @uml.property  name="ac"
	 * @uml.associationEnd  
	 */
	private ApplicationContext ac;

	/**
	 * @uml.property  name="sqlErrorDef"
	 */
	private String SqlErrorDef = "SELECT errorCode, kor_errorMsg, eng_errorMsg, cha_errorMsg, loc_errorMsg FROM CT_ERRORDEFINITION ORDER BY errorCode";

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		ac = arg0;
		load();
	}
	/*
	* Name : load
	* Desc : This function is load
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public void load()
	{
		List resultList = null;
		Object errMsgObject = null;
		
		resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(SqlErrorDef);
		
		if ( resultList == null ) return;
		
		for ( int i = 0 ; i < resultList.size(); i++)
		{
			ListOrderedMap orderMap= (ListOrderedMap)resultList.get(i);
			String errorCode = "";
			String kor_errorMsg = "";
			String eng_errorMsg = "";
			String cha_errorMsg = "";
			String loc_errorMsg = "";
			
			errorCode = orderMap.get("errorCode").toString();
			
			errMsgObject = orderMap.get("kor_errorMsg");
			if(errMsgObject != null)
				kor_errorMsg = errMsgObject.toString();
			
			errMsgObject = orderMap.get("eng_errorMsg");
			if(errMsgObject != null)
				eng_errorMsg = errMsgObject.toString();
			
			errMsgObject = orderMap.get("cha_errorMsg");
			if(errMsgObject != null)
				cha_errorMsg = errMsgObject.toString();
			
			errMsgObject = orderMap.get("loc_errorMsg");
			if(errMsgObject != null)
				loc_errorMsg = errMsgObject.toString();
			
			
			ErrorDef errorDef = new ErrorDef();
			errorDef.setErrorCode(errorCode);
			errorDef.setKor_errorMessage(kor_errorMsg);
			errorDef.setEng_errorMessage(eng_errorMsg);
			errorDef.setCha_errorMessage(cha_errorMsg);
			errorDef.setLoc_errorMessage(loc_errorMsg);
		
			this.put(errorCode, errorDef);
		}
	}
	/*
	* Name : getErrorDef
	* Desc : This function is getErrorDef
	* Author : AIM Systems, Inc
	* Date : 2011.01.17
	*/
	public ErrorDef getErrorDef(String errorCode)
	{
		return this.get(errorCode);
	}
}
