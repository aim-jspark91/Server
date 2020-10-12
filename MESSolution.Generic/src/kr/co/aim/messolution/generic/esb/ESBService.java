package kr.co.aim.messolution.generic.esb;


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenflow.exception.BpelException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.esb.GenericSender;
import kr.co.aim.greenframe.esb.IRequester;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ESBService implements ApplicationContextAware {
	
	private static Log log = LogFactory.getLog(ESBService.class);
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext	applicationContext;
	public static final String 		SUCCESS = "0";
	/**
	 * @uml.property  name="sendSubjectMap"
	 * @uml.associationEnd  qualifier="constant:java.lang.String java.lang.String"
	 */
	private Map<String, String> sendSubjectMap = new Hashtable<String, String>();
	
	//global destination
	private ThreadLocal<String> replySubjectName = new ThreadLocal<String>();
	
	/*
	* Name : init
	* Desc : This function is sendSubjectMap init
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void init()
	{
		sendSubjectMap.put("CNXsvr", makeCustomServerLocalSubject("CNXsvr"));
		sendSubjectMap.put("PEXsvr", makeCustomServerLocalSubject("PEXsvr"));
		sendSubjectMap.put("TEXsvr", makeCustomServerLocalSubject("TEXsvr"));
		sendSubjectMap.put("QRYsvr", makeCustomServerLocalSubject("QRYsvr"));
		sendSubjectMap.put("TEMsvr", makeCustomServerLocalSubject("TEMsvr"));
		sendSubjectMap.put("FMCsvr", makeCustomServerLocalSubject("FMCsvr"));
		sendSubjectMap.put("DSPsvr", makeCustomServerLocalSubject("RTD"));
		sendSubjectMap.put("CNMsvr", makeCustomServerLocalSubject("CNMsvr"));
		sendSubjectMap.put("EDCsvr", makeCustomServerLocalSubject("EDCsvr"));
		sendSubjectMap.put("PEMsvr", makeCustomServerLocalSubject("PEMsvr"));
		sendSubjectMap.put("SPCsvr", makeCustomServerLocalSubject("SPCsvr"));
		sendSubjectMap.put("FGMsvr", makeCustomServerLocalSubject("FGMsvr"));
		sendSubjectMap.put("FGXsvr", makeCustomServerLocalSubject("FGXsvr"));
		sendSubjectMap.put("PMMsvr", makeCustomServerLocalSubject("PMMsvr"));
		sendSubjectMap.put("PMXsvr", makeCustomServerLocalSubject("PMXsvr"));
		sendSubjectMap.put("DTRsvr", makeCustomServerLocalSubject("DTRsvr"));
	}
	
	public String getReplySubject()
	{
		String result = this.replySubjectName.get();
		
		if (StringUtil.isEmpty(result))
			return "";
		else
			return result;
	}
	
	public void setReplySubject(String replySubjectName)
	{
		this.replySubjectName.set(replySubjectName);
	}
	
	/*
	* Name : getSendSubject
	* Desc : This function is get SendSubject
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public String getSendSubject(String serverName)
	{
		if(log.isInfoEnabled()){
			log.debug("serverName = " + serverName);
		}
		
		return sendSubjectMap.get(serverName);
	}
	/*
	* Name : makeCustomServerLocalSubject
	* Desc : This function is makeCustomServerLocalSubject
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public String makeCustomServerLocalSubject(String serverName)
	{
		if(log.isInfoEnabled()){
			log.debug("serverName = " + serverName);
		}
		
		StringBuffer serverSubject = new StringBuffer();
		
		if(StringUtils.equals(serverName, "CNXsvr") ||
				StringUtils.equals(serverName, "PEXsvr") ||
				StringUtils.equals(serverName, "TEXsvr") ||
		        StringUtils.equals(serverName, "FGXsvr") ||
		        StringUtils.equals(serverName, "PMXsvr"))
		{//inbox handling
			serverSubject.append("_LOCAL").
						  append("." + System.getProperty("location")).
			              append("." + System.getProperty("factory")).
			              append("." + System.getProperty("cim")).
			              append("." + System.getProperty("mode")).
			              append("." + System.getProperty("shop")).
			              append("." + serverName);
			
		}
		else if (StringUtils.equals(serverName, "TEMsvr"))
		{//common router
			serverSubject.append(System.getProperty("location")).
				          append("." + System.getProperty("factory")).
				          append("." + System.getProperty("cim")).
				          append("." + System.getProperty("mode")).
				          // Modified by smkang o 2018.05.28 - Need to distinguish shop.
//				          append("." + "FAB").
				          append("." + System.getProperty("shop")).
				          append("." + serverName);
		}
		else if (StringUtils.equals(serverName, "FMCsvr"))
		{//common router
			serverSubject.append(System.getProperty("location")).
				          append("." + System.getProperty("factory")).
				          append("." + System.getProperty("cim")).
				          append("." + System.getProperty("mode")).
				          //modified by wghuang
				          //append("." + "FAB").
				          append("." + System.getProperty("shop")).
				          append("." + serverName);
		}
		else if ("MCS".equals(serverName))
		{
			// Modified by smkang on 2018.04.04 - Sometime a subject name of MCS should be checked and modified again.
			//									  Configuration of HIFsvr in a configuration of TEXsvr is necessary to be modified too.
			// Modified by smkang on 2018.05.03 - According to MCS Interface Specification.
//			serverSubject.append("TRULY.F1.MCS.PRD.FAB.MCS");
//			serverSubject.append("KSM.MES.DEV.FAB.MCS");
			serverSubject.append(System.getProperty("location")).
				          append("." + System.getProperty("factory")).
				          append("." + serverName).
				          append("." + System.getProperty("mode")).
				          // Modified by smkang on 2018.05.22 - MCS has one subject name.
//				          append("." + System.getProperty("shop")).
				          append(".FAB").
				          append("." + serverName);
		}
		else if("RTD".equals(serverName))
		{
			serverSubject.append(System.getProperty("location")).
			  append("." + System.getProperty("factory")).
			  append("." + GenericServiceProxy.getConstantMap().Subject_RTD).
			  append("." + System.getProperty("mode")).
			  //modified by wghuang
			  //append("." + "FAB").
			  append("." + System.getProperty("shop")).
			  append("." + "DSPsvr");
		}
		else if("SPCsvr".equals(serverName))
		{
			serverSubject.append(System.getProperty("location")).
				          append("." + System.getProperty("factory")).
				          /* 20181130, hhlee, SPC has fix cim name */
				          //append("." + System.getProperty("cim")).
				          append("." + GenericServiceProxy.getConstantMap().Subject_SPC).
				          append("." + System.getProperty("mode")).
				          /* 20181130, hhlee, delete, SPC has not shop name */
				          //append("." + System.getProperty("shop")).
				          append("." + "EDCsvr");
		}
		else
		{//generic
			serverSubject.append(System.getProperty("location")).
				          append("." + System.getProperty("factory")).
				          append("." + System.getProperty("cim")).
				          append("." + System.getProperty("mode")).
				          append("." + System.getProperty("shop")).
				          append("." + serverName);
		}
				
		log.info("Maked SubjectName=" + serverSubject.toString());
		
		return serverSubject.toString(); 
	}
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
    public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		//this.applicationContext = arg0;
		
		log.debug("Adujusting bundle dependency");
		init();
	}
	/*
	* Name : sendRequest
	* Desc : This function is sendRequest
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public Document sendRequest(String subject, Document doc) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String) greenFrameServiceProxy.getGenericSender().sendRequest(subject, sendMsg);
		Document createDocument = JdomUtils.loadText(reply);
		return createDocument;
	}
	/*
	* Name : sendRequestTimeOut
	* Desc : This function is sendRequestTimeOut
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public Document sendRequestTimeOut(String subject, Document doc, int timeOut) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String) greenFrameServiceProxy.getGenericSender().sendRequest(subject, sendMsg, timeOut);
		Document createDocument = JdomUtils.loadText(reply);
		return createDocument;
	}
	
	
	/**
	 * sendRequest by sender
	 * @author swcho
	 * @since 2013.09.24
	 * @param subject
	 * @param doc
	 * @param senderName
	 * @return
	 * @throws Exception
	 */
	public Document sendRequestBySender(String subject, Document doc, String senderName) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		//String reply = (String) greenFrameServiceProxy.getGenericSender().sendRequest(subject, sendMsg);
		String reply = (String)GenericServiceProxy.getGenericSender(senderName).sendRequest(subject, sendMsg);
		log.debug(reply);
		Document createDocument = JdomUtils.loadText(reply);
		
		return createDocument;
	}
	
	/*
	* Name : send
	* Desc : This function is greenTrack API send
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void send(String subject, Document doc )
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		// add return
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage,
			SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Result_Name_Tag + "/", doc, SMessageUtil.Result_ErrorMessage));
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender("GenericSender").send(subject, sendMsg);
		
		log.debug(" SEND : Subject=" + subject);
	}
	
	/*
	* Name : sendBySender
	* Desc : This function is greenTrack API send
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendBySender(Document doc, String senderName)
	{
		log.debug("senderName = " + senderName);
		
		try {
			String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, returnCode);
		} catch (CustomException e) {
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
		}

		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage,
				SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Result_Name_Tag + "/", doc, SMessageUtil.Result_ErrorMessage));
		
				
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(sendMsg);
		
		log.debug(" SEND : Message=" + sendMsg);
	}
	
	/*
	* Name : sendBySender
	* Desc : This function is Create Return XML greenTrack API send
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendBySender(String subject, Document doc, String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		// add return  
		try {
			String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, returnCode);
		} catch (CustomException e) {
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
		}
		//SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
		//130917 by swcho : get success report
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage,
			SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Result_Name_Tag + "/", doc, SMessageUtil.Result_ErrorMessage));
		//
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);
		
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
	}
	/*
	* Name : sendBySender
	* Desc : This function is Create Return XML greenTrack API send
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendBySender(String subject, Document doc, String senderName, String returnCode, String returnMsg)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		// add return  
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, returnCode);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, returnMsg);
		// 
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
	}
	
	/*
	* Name : sendBySender
	* Desc : This function is greenTrack API send
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendBySender(String subject, String sSendMsg, String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		GenericServiceProxy.getGenericSender(senderName).send(subject, sSendMsg);
		log.debug(" SEND : Subject=" + subject);
		//log.debug(" SEND : Message=" + sSendMsg);
	}
	
	/*
	* Name : send
	* Desc : This function is greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void send(String beanName, String subject, Document doc )
	{
		if(log.isInfoEnabled()){
			log.debug("beanName = " + beanName);
			log.debug("subject = " + subject);
		}
		
		String sendMsg = doc.toString();
		IRequester requester = (IRequester)this.applicationContext.getBean(beanName);
		requester.send(subject, doc.toString());
		
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
		
	}
	/*
	* Name : sendReply
	* Desc : This function is greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendReply( String replySubject )
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
		}
		
		String sReplyMsg = SMessageUtil.replyXMLMessage(SUCCESS );

		greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
		greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);
   
   log.debug(" SERP : Subject=" + replySubject);
   log.debug(" SERP : Message=" + sReplyMsg);
	}
	/*
	* Name : sendReplyBySender
	* Desc : This function is greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendReplyBySender( String replySubject , String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
			log.debug("senderName = " + senderName);
		}
		
	   String sReplyMsg = SMessageUtil.replyXMLMessage(SUCCESS );

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);
	   
	   log.debug(" SERP : Subject=" + replySubject);
	   log.debug(" SERP : Message=" + sReplyMsg);
	}
	/*
	* Name : sendReply
	* Desc : This function is greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendReply( String replySubject, String sReplyMsg )
	{
	   greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
	   greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   //log.debug(" SERP : Message=" + sReplyMsg);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}
	/*
	* Name : sendReplyBySender
	* Desc : This function is greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public void sendReplyBySender( String replySubject, String sReplyMsg , String senderName)
	{
		GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
		GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   //log.debug(" SERP : Message=" + sReplyMsg);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}
	
	/*
	* Name : sendReply
	* Desc : This function is Add XML Return greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	public void sendReply( String replySubject, Document doc ) 
	{
	    String sReplyMsg = null;
	   
	    try
	    {
		    //keep reason code even though failed
		    if (StringUtil.isEmpty(SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, false)))
		   	    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
		   
		    String customComment = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false);
		   
		    if (!StringUtil.isEmpty(customComment))
			    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, customComment);
		    else
			    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	    }
	    catch (CustomException ce)
	    {
		    log.debug(ce.getMessage());
		    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	    }
	   
	    /* 20190211, hhlee, add, Result NG ==>>
         * hhlee, 20190211, Message Body RESULT, RESULTDESCRIPTION Add
         * Only apply messages that require RESULT and RESULTDESCRIPTION.
         *       
         */
	    String returnCode = StringUtil.EMPTY;
        String ErrorMessage = StringUtil.EMPTY; 
        try
        {
            returnCode = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, false);
            ErrorMessage = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false);
            
    	    Element bcResult =  doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild(GenericServiceProxy.getConstantMap().RETURN_BODY_RESULT);
            if (bcResult != null && 
                    StringUtil.isNotEmpty(returnCode) && !StringUtil.equals(returnCode, SUCCESS))
            {                
                bcResult.setText("NG");
                
                Element bcResultDescription =  doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild(GenericServiceProxy.getConstantMap().RETURN_BODY_RESULT_DESCRIPTION);
                if (bcResultDescription != null)
                {
                    bcResultDescription.setText(ErrorMessage);
                }
            }
        }
        catch(Exception ce)
        {       
        }
        /*  <<== 20190211, hhlee, add, Result NG */
	    
	    sReplyMsg = JdomUtils.toString(doc);

	    greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
	    greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);

	    log.debug(" SERP : Subject=" + replySubject);
	    //log.debug(" SERP : Message=" + sReplyMsg);
	    GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	   
	    //add by wghuang 20181213, requested by EDO
	    //just left ErrorMessageLog when ReturnCode is not 0.
	    try
	    {	 
	        if(StringUtil.isEmpty(returnCode))
	        {
	            returnCode = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, false);
	            ErrorMessage = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false);
	        }
	        
	        if(StringUtil.isNotEmpty(returnCode) && !StringUtil.equals(returnCode, SUCCESS))
	        {
	            CustomException ex = new CustomException(returnCode);
	    	    ex.errorDef.setLoc_errorMessage(ErrorMessage);
	    		
	    	    //ErrorMessageLogItems errorMessageLogitem = new ErrorMessageLogItems(doc, ex, StringUtils.EMPTY);	    		  		
			    if (doc != null && !StringUtil.isEmpty(replySubject) && ex != null)
			    {
			        GenericServiceProxy.getMessageTraceService().recordErrorMessageLog(doc, ex, StringUtils.EMPTY);
			    }
	        }	
        }
	    catch(Exception ce)
	    {    	
	    }
	    //add by wghuang 20181213, requested by EDO
	}
	/*
	* Name : sendReplyBySender
	* Desc : This function is Add XML Return greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	public void sendReplyBySender( String replySubject, Document doc , String senderName) 
	{
		//if(log.isInfoEnabled()){
		//	log.debug("replySubject = " + replySubject);
		//	log.debug("senderName = " + senderName);
		//}
		
	   String sReplyMsg = null;
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	   
	   //SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", makeCustomServerLocalSubject(System.getProperty("svr")));
	   //SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", replySubject);
	  
	   sReplyMsg = JdomUtils.toString(doc);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   //log.debug(" SERP : Message=" + sReplyMsg);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}
	
	public void sendReplyBySenderByRMS( String replySubject, Document doc , String senderName) 
	{
		//if(log.isInfoEnabled()){
		//	log.debug("replySubject = " + replySubject);
		//	log.debug("senderName = " + senderName);
		//}
		
		try
		{
		   if(!StringUtil.equals(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false), "NG"))
		   {
			   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
			   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
		   }
		}
		catch(Exception ex)
		{
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
		}
		
	   String sReplyMsg = null;
	  
	   sReplyMsg = JdomUtils.toString(doc);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   //log.debug(" SERP : Message=" + sReplyMsg);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}
	
	/*
	* Name : sendReplyBySender
	* Desc : This function is Add XML Return greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	public void sendReplyBySender( String replySubject, String MsgName, Document doc , String senderName) 
	{
		//if(log.isInfoEnabled()){
		//	log.debug("replySubject = " + replySubject);
		//	log.debug("senderName = " + senderName);
		//}
		
	   String sReplyMsg = null;
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	   
	   SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME",       MsgName);
	   //SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", makeCustomServerLocalSubject(System.getProperty("svr")));
	   //SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", replySubject);
	  
	   sReplyMsg = JdomUtils.toString(doc);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   //log.debug(" SERP : Message=" + sReplyMsg);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}

	/*
	* Name : sendError
	* Desc : This function is sendErsendErrorrorBySender
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	public void sendError( String replySubject, Document doc, String lanuage, Exception e)
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
//			log.debug("lanuage = " + lanuage);
		}
		
		if(e instanceof BpelException){
			e = ((BpelException)e).getNativeException();
		}
	   String sReplyMsg = this.makeSendError(replySubject, doc, lanuage, e);

	   greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
	   if(StringUtils.isNotEmpty(replySubject) == true)
		   greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);

	   log.error(" SERP : Subject=" + replySubject);
	   log.error(" SERP : Message=" + sReplyMsg);
	}
	/*
	* Name : makeSendError
	* Desc : This function is makeSendError
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	private String makeSendError(String replySubject, Document doc, String lanuage, Exception e){
		
		String errorCode = "";
		String korErrorMsg = "";
		String engErrorMsg = "";
		String chaErrorMsg = "";
		String locErrorMsg = "";
		
		//Throwable orgEx = (BpelException)e;
		Throwable orgEx = e;
		
		Throwable targetEx;
		if ( orgEx.getCause() instanceof InvocationTargetException )
			targetEx = ((InvocationTargetException) orgEx.getCause()).getTargetException();
		else if ( orgEx.getCause() instanceof CustomException )
			targetEx = (orgEx.getCause());
		else if ( orgEx.getCause() instanceof NotFoundSignal )
			targetEx = (orgEx.getCause());
		else if ( orgEx.getCause() instanceof DuplicateNameSignal )
			targetEx = (orgEx.getCause());
		else if ( orgEx.getCause() instanceof FrameworkErrorSignal )
			targetEx = (orgEx.getCause());
		else if ( orgEx.getCause() instanceof InvalidStateTransitionSignal )
			targetEx = (orgEx.getCause());
		else if ( orgEx.getCause() instanceof greenFrameErrorSignal )
			targetEx = (orgEx.getCause());
		else
			targetEx = orgEx;
		
		if ( targetEx instanceof NotFoundSignal )
		{
			errorCode = "NotFoundSignal";
			//ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
			korErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			
		}
		else if ( targetEx instanceof DuplicateNameSignal )
		{
			errorCode = "DuplicateNameSignal";
			//ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);

			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof FrameworkErrorSignal)
		{
			errorCode = "FrameworkErrorSignal";
			//ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
		
			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof InvalidStateTransitionSignal)
		{
			errorCode = "InvalidStateTransitionSignal";
			//ErrorDef errorDef = new ErrorDef();
			
			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof CustomException )
		{
			if (((CustomException)targetEx).errorDef != null)
			{
				errorCode = ((CustomException)targetEx).errorDef.getErrorCode();
				ErrorDef errorDef = ((CustomException)targetEx).errorDef;
				
				korErrorMsg = errorDef.getKor_errorMessage();
				engErrorMsg = errorDef.getEng_errorMessage();
				chaErrorMsg = errorDef.getCha_errorMessage();
				locErrorMsg = errorDef.getLoc_errorMessage();
			}
			else
			{
				errorCode = "UndefinedCode";
				//ErrorDef errorDef = ((CustomException)targetEx).errorDef;
				korErrorMsg = targetEx.getMessage();engErrorMsg = targetEx.getMessage();chaErrorMsg = targetEx.getMessage();locErrorMsg = targetEx.getMessage();
			}
		}
		else if ( targetEx instanceof greenFrameErrorSignal )
		{
			errorCode = "greenFrameErrorSignal";
			ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
			
			if (errorDef != null)
			{
				korErrorMsg =
					MessageFormat.format(errorDef.getKor_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				engErrorMsg = 
					MessageFormat.format(errorDef.getEng_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				chaErrorMsg = 
					MessageFormat.format(errorDef.getCha_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				locErrorMsg = 
					MessageFormat.format(errorDef.getLoc_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
			}
		}
		else if (targetEx instanceof NullPointerException)
		{
			errorCode = "NullValue";
			
			korErrorMsg = "System field has null";
			engErrorMsg = "System filed has null";
			chaErrorMsg = "System filed has null";
			locErrorMsg = "System filed has null"; 
		}
		else 
		{
			errorCode = "UndefinedCode";
			ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("UndefinedCode");
			
			if (errorDef != null)
			{
				korErrorMsg =
					MessageFormat.format(errorDef.getKor_errorMessage(), targetEx.getMessage());
				engErrorMsg =
					MessageFormat.format(errorDef.getEng_errorMessage(), targetEx.getMessage());
				chaErrorMsg =
					MessageFormat.format(errorDef.getCha_errorMessage(), targetEx.getMessage());
				locErrorMsg = 
					MessageFormat.format(errorDef.getLoc_errorMessage(), targetEx.getMessage());
			}
		}
		
	    String sReplyMsg = null;
	    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, errorCode);
	   
	    if ( lanuage.equals("Korean") == true )
		    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, korErrorMsg);
	    else if ( lanuage.equals("English") == true )
		    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, engErrorMsg);
	    else if ( lanuage.equals("Chinese") == true )
		    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, chaErrorMsg);
	    else
		    SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, locErrorMsg);
	   
		/* 
		 * hhlee, 20180326, Message Body RESULT, RESULTDESCRIPTION Add
		 * Only apply messages that require RESULT and RESULTDESCRIPTION.
		 * =================================================================>	   
		 */
	    Element bcResult =  doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild(GenericServiceProxy.getConstantMap().RETURN_BODY_RESULT);
	    if (bcResult != null)
	    {
	    	bcResult.setText("NG");
	    }
	    
	    Element bcResultDescription =  doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild(GenericServiceProxy.getConstantMap().RETURN_BODY_RESULT_DESCRIPTION);
	    if (bcResultDescription != null)
	    {
	        if ( lanuage.equals("Korean") == true )
	        {
	 		    bcResultDescription.setText(korErrorMsg);
	        }
	 	    else if ( lanuage.equals("English") == true )
	 	    {
	 		    bcResultDescription.setText(engErrorMsg);
	 	    }
	 	    else if ( lanuage.equals("Chinese") == true )
	 	    {
	 	    	bcResultDescription.setText(chaErrorMsg);
	 	    }
	 	    else
	 	    {
	 	    	bcResultDescription.setText(locErrorMsg);
	 	    }	 	   
	    }
	    /* <===================================================================== */
	   
	   //set description for BC : no need
	   /*try
	   {
		   Element bcDescription = doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("HOSTMESSAGE");
		   
		   if (bcDescription != null)
		   {
			   if ( lanuage.equals("KOR") == true )
				   bcDescription.setText(korErrorMsg);
			   else if ( lanuage.equals("ENG") == true )
				   bcDescription.setText(engErrorMsg);
			   else if ( lanuage.equals("CHA") == true )
				   bcDescription.setText(chaErrorMsg);
			   else
				   bcDescription.setText(locErrorMsg);
		   }
		   else
		   {
			   bcDescription = doc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("DESCRIPTION");
			   
			   if (bcDescription != null)
			   {
				   if ( lanuage.equals("KOR") == true )
					   bcDescription.setText(korErrorMsg);
				   else if ( lanuage.equals("ENG") == true )
					   bcDescription.setText(engErrorMsg);
				   else if ( lanuage.equals("CHA") == true )
					   bcDescription.setText(chaErrorMsg);
				   else
					   bcDescription.setText(locErrorMsg);
			   }
		   }
	   }
	   catch (Exception ex)
	   {
		   //ignore that
	   }*/
	   
	   sReplyMsg = JdomUtils.toString(doc);
	   
	   return sReplyMsg;
	}
	/*
	* Name : sendErrorBySender
	* Desc : This function is sendErrorBySender greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.05.17
	*/
	public void sendErrorBySender( String replySubject, Document doc, String lanuage, Exception e, String senderName)
	{
		//if(log.isInfoEnabled()){
		//	log.debug("replySubject = " + replySubject);
		//	log.debug("lanuage = " + lanuage);
		//	log.debug("senderName = " + senderName);
		//}
		
		if(e instanceof BpelException){
			e = ((BpelException)e).getNativeException();
		}
	   String sReplyMsg = this.makeSendError(replySubject, doc, lanuage, e);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   if(StringUtils.isNotEmpty(replySubject) == true)
		   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.error(" SERP : Subject=" + replySubject);
	   log.error(" SERP : Message=" + sReplyMsg);
	}
	/*
	* Name : sendBySender
	* Desc : This function is Create XML Add ReturnCode, ReturnMessage.  greenTrack API reply
	* Author : AIM Systems, Inc
	* Date : 2011.03.04
	*/
	public Document sendBySenderReturnMessage(String replySubject, Document doc, Element element, String senderName, String returnMsg) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
			log.debug("senderName = " + senderName);
		}
		
		String sReplyMsg = null;
		
		Element message = new Element( SMessageUtil.Message_Tag );  
		
		Element oriheader = doc.getDocument().getRootElement().getChild("Header");
		
		Element header = new Element("Header");
		
		Element messagename = new Element("MESSAGENAME");
		messagename.setText(oriheader.getChildText("MESSAGENAME"));
		header.addContent(messagename);
		
		Element shopName = new Element("SHOPNAME");
		shopName.setText(oriheader.getChildText("SHOPNAME"));
		header.addContent(shopName);
		
		Element machineName = new Element("MACHINENAME");
		machineName.setText(oriheader.getChildText("MACHINENAME"));
		header.addContent(machineName);
		
		Element transactionid = new Element("TRANSACTIONID");
		transactionid.setText(oriheader.getChildText("TRANSACTIONID"));
		header.addContent(transactionid);
		
		Element originalSourceSubjectName = new Element("ORIGINALSOURCESUBJECTNAME");
		originalSourceSubjectName.setText(oriheader.getChildText("ORIGINALSOURCESUBJECTNAME"));
		header.addContent(originalSourceSubjectName);
		
		//Element sourceSubjectName = new Element("SOURCESUBJECTNAME");
		//sourceSubjectName.setText(oriheader.getChildText("SOURCESUBJECTNAME"));
		//header.addContent(sourceSubjectName);
		
		//Element targetSubjectName = new Element("TARGETSUBJECTNAME");
		//targetSubjectName.setText(oriheader.getChildText("TARGETSUBJECTNAME"));
		//header.addContent(targetSubjectName);
		
		Element eventUser = new Element("EVENTUSER");
		eventUser.setText(oriheader.getChildText("EVENTUSER"));
		header.addContent(eventUser);
		
		Element eventComment = new Element("EVENTCOMMENT");
		eventComment.setText(oriheader.getChildText("EVENTCOMMENT"));
		header.addContent(eventComment);
				
		message.addContent(header);
		
		if ( element != null )
		{
			message.addContent(element);
		}

		Element returnElement = new Element("Return");
		
		Element returnCode = new Element("RETURNCODE");
		returnCode.setText(SUCCESS);
		returnElement.addContent(returnCode);
		
		Element returnMessage = new Element("RETURNMESSAGE");
		returnMessage.setText(returnMsg);
		returnElement.addContent(returnMessage);
		
		message.addContent(returnElement);
		
		//
		Document replyDoc = new Document( message ); 
		   
		sReplyMsg = JdomUtils.toString(replyDoc);
		
		GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
		GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

		log.debug(" SEND : Subject=" + replySubject);
		log.debug(" SEND : Message=" + sReplyMsg);

		return replyDoc;
	}
	
	/*
    * Name : recordMessagelogAftersendBySender
    * Desc : This function is Create Return XML greenTrack API send
    * Author : AIM Systems, Inc
    * Date : 2018.09.14
    */
    public void recordMessagelogAftersendBySender(String subject, Document doc, String senderName)
    {
        if(log.isInfoEnabled()){
            log.debug("subject = " + subject);
            log.debug("senderName = " + senderName);
        }
        
        // add return  
        try {
            String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, returnCode);
        } catch (CustomException e) {
            SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
        }
        //SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
        //130917 by swcho : get success report
        SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage,
            SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Result_Name_Tag + "/", doc, SMessageUtil.Result_ErrorMessage));
        //
        
        String sendMsg = JdomUtils.toString(doc);
                
        GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);
                
        
        log.debug(" SEND : Subject=" + subject);
        log.debug(" SEND : Message=" + sendMsg);
        
        GenericServiceProxy.getMessageTraceService().recordMessageLog(doc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
    }   
}
