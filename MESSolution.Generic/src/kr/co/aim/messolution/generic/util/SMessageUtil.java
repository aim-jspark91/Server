package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.greenframe.util.msg.MessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

public class SMessageUtil {

	private static 	Log 								log = LogFactory.getLog(MessageUtil.class);
	
	public static final String 							BPELName_Tag = "bpelname";
	public static final String 							Message_Tag = "Message";
	public static final String 							MessageName_Tag = "MESSAGENAME";
	public static final String 							Header_Tag = "Header";
	public static final String                          Listener_Tag = "listener";
	public static final String 							Body_Tag = "Body";
	public static final String 							Return_Tag = "Return";
	public static final String 							Result_Name_Tag				= "Return";	        
	public static final String							Result_ReturnCode			= "RETURNCODE";
	public static final String							Result_ErrorMessage			= "RETURNMESSAGE";
	public static final String                          TransactionId_Tag 	  = "TRANSACTIONID";
	

	public static final String	Result_Tag						= "RESULT";
	public static final String	Result_Desc						= "RESULTDESCRPITION";
	public static final String	Return_ReturnCode_Tag			= "RETURNCODE";
	public static final String	Return_ErrorMessage_Tag			= "RETURNMESSAGE";
	public static final String	InBoxName_Tag					= "INBOXNAME";
	public static final String	Data							= "DATA";
	
	
	// EventInfo Relation
	public static final String							EventUser					= "EVENTUSER";
	public static final String							EventName					= "EVENTNAME";
	public static final String							EventComment				= "EVENTCOMMENT";
	public static final String							Language					= "LANGUAGE";
	public static final String							ReasonCodeType				= "REASONCODETYPE";
	public static final String							ReasonCode					= "REASONCODE";
	public static final String							TimeKey					    = "TIMEKEY";
	
	private static final String 						ReplySubjectName_Tag = "ORIGINALSOURCESUBJECTNAME";
	/*
	* Name : getListenerBpelName
	* Desc : This function is getListenerBpelName
	* Author : AIM Systems, Inc
	* Date : 2011.01.04
	*/
	public static String getListenerBpelName(Document doc)                                    
	{                   
		String Listener = null;
		try {           
			Listener = JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + Listener_Tag);
			if (Listener != null && Listener.length() > 0) {
				if (!Listener.toLowerCase().endsWith(".bpel"))
					Listener = Listener + ".bpel";
			}
		} catch (Exception e) {                                                                 
		}                                                                                       
		return Listener;                                                                              
	} 
	/*
	* Name : getBpelName
	* Desc : This function is getBpelName
	* Author : AIM Systems, Inc
	* Date : 2011.01.04
	*/
	public static String getBpelName(Document doc)                                    
	{                   
		String BpelName = null;
		try {           
			BpelName = JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + MessageName_Tag);
			if (BpelName != null && BpelName.length() > 0) {
				if (!BpelName.toLowerCase().endsWith(".bpel"))
					BpelName = BpelName + ".bpel";
			}
		} catch (Exception e) {                                                                 
		}                                                                                       
		return BpelName;                                                                              
	}      
	/*
	* Name : getDocumentFromTibrvMsg
	* Desc : This function is getDocumentFromTibrvMsg
	* Author : AIM Systems, Inc
	* Date : 2011.01.04
	*/
	public static Document getDocumentFromTibrvMsg(TibrvMsg data, String dataFieldTag)
	{
		
		Document document = null;    
		String strMsg = "";
		String replySubjectName = "";
		
		if (data instanceof TibrvMsg)
		{
			TibrvMsg TibrvMsg = data;
			try
			{
				strMsg = (String)TibrvMsg.get(dataFieldTag);
				replySubjectName = TibrvMsg.getReplySubject();
				if (replySubjectName == null) replySubjectName = "";
			}
			catch (TibrvException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//convert DOM object with SAX
		try
		{
			document = JdomUtils.loadText(strMsg);			
		}
		catch (Exception e) 
		{
			try {
				document = createXmlDocument(strMsg);
			} catch (Exception ex) {
				log.error(ex, ex);
			}
		}
		
		//150305 by swcho : original source subject management
		String existingSourceSubjectName = getElement("//" + Message_Tag + "/" + Header_Tag + "/", document, ReplySubjectName_Tag);
		
		// Deleted by smkang on 2019.03.27 - Before logging of SMessageUtil.getDocumentFromTibrvMsg, MES.MSGNAME and MES.TRXID should be set.
		//									 So this logic is removed from SMessageUtil.getDocumentFromTibrvMsg.
		//log.debug("existing original source subject : " + existingSourceSubjectName);
		
		//store reply subject name for sendrequest style
		if ( replySubjectName.length() > 0 && existingSourceSubjectName.isEmpty())
		{
			setItemValue(document, Header_Tag, ReplySubjectName_Tag, replySubjectName);
			//GenericServiceProxy.getESBServive().setReplySubject(replySubjectName);
		}
		
		return document;
	}
	/*
	* Name : replyXMLMessage
	* Desc : This function is replyXMLMessage
	* Author : AIM Systems, Inc
	* Date : 2011.01.04
	*/
	public static String replyXMLMessage(String ReturnCode)
	{
		Document doc = JdomUtils.createDocument(Message_Tag);
		SMessageUtil.setResultItemValue(doc, Result_ReturnCode, ReturnCode);
		SMessageUtil.setResultItemValue(doc, Result_ErrorMessage, "");
		String replyMSG = JdomUtils.toString(doc);
		return replyMSG;
	}
	
	/*
	 * Name : getElementValue Desc : This function is getElementValue Author :
	 * AIM Systems, Inc Date : 2013.10.30
	 */
	public static String getElementValueByName( Element element, String name )
	{
		if ( element != null && element.getChild( name ) != null )
		{
			return element.getChild( name ).getText();
		}
		else
		{
			return "";
		}
	}
	
	/*
	* Name : setElement
	* Desc : This function is setElement
	* Author : AIM Systems, Inc
	* Date : 2011.01.04
	*/
	public static void setElement(String nodePath, Document doc, String nodeName, String nodeValue)
	{
		if (nodeValue == null) nodeValue = "";
		try {
			Element element = JdomUtils.getNode(doc, nodePath + nodeName);
			if (element != null) {
				//element.setContent(new CDATA(nodeValue));
				//element.setContent(new Element(nodeValue));
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				JdomUtils.setNodeText(doc, nodePath, nodeName, nodeValue);
			}
			else {
				//Element createElement = JdomUtils.createElement(nodeName, valueKey);
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				Element parentElement = JdomUtils.getNode(doc, nodePath);
				Element newElement = JdomUtils.addElement(parentElement, nodeName, nodeValue);
				//newElement.setContent(new CDATA(nodeValue));
			}
		} catch (Exception e) {                                                                 
			e.printStackTrace();                                                                
		}                                                                                                                                                                  
	}
	
	/**
	 * get target node value
	 * @author swcho
	 * @since 2013.09.17
	 * @param nodePath
	 * @param doc
	 * @param nodeName
	 * @return
	 */
	public static String getElement(String nodePath, Document doc, String nodeName)
	{
		String nodeValue = "";
		
		try {
			Element element = JdomUtils.getNode(doc, nodePath + nodeName);
			
			if (element != null)
			{
				//element.setContent(new CDATA(nodeValue));
				//element.setContent(new Element(nodeValue));
				if (nodePath.substring(nodePath.length()-1 , nodePath.length()).equalsIgnoreCase("/"))
					nodePath = nodePath.substring(0, nodePath.length()-1);
				
				//JdomUtils.setNodeText(doc, nodePath, nodeName, nodeValue);
				nodeValue = JdomUtils.getNodeText(doc, nodePath + "/" + nodeName);
			}
			
		} catch (Exception e) {                                                                 
			e.printStackTrace();
			nodeValue = "";
		}
		
		return nodeValue;
	}
	
	/*
	* Name : setResultItemValue
	* Desc : This function is setResultItemValue
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static void setResultItemValue(Document doc, String nodeName, String nodeValue)                                    
	{
		if (doc.getRootElement().getChild(Result_Name_Tag) == null)
			doc.getRootElement().addContent(new Element(Result_Name_Tag));
		
		String nodePath = "//" + Message_Tag + "/" + Result_Name_Tag + "/";
		setElement(nodePath, doc, nodeName, nodeValue);
		
		if (nodeName.equalsIgnoreCase(SMessageUtil.Result_ReturnCode) && !nodeValue.equalsIgnoreCase("0"))
			log.info(nodeName + " : " + nodeValue);
//		else if (nodeName.equalsIgnoreCase(SMessageUtil.Result_ReturnCode) && nodeValue.equalsIgnoreCase("0"))
//			log.info(MessageUtil.Return_Code + " : 0");
		else if (nodeName.equalsIgnoreCase(SMessageUtil.Result_ErrorMessage) && nodeValue.length() > 0)
			log.info(nodeName + " : " + nodeValue);
		else if  (nodeName.equalsIgnoreCase(SMessageUtil.Result_ErrorMessage) && nodeValue.length() == 0)
			log.info(nodeName + " : ");
		
		//log.debug("[" + nodeName + "] = " + nodeValue);
	}
	
	/*
	 * Name : addReturnBodyElement Desc : This function is addReturnBodyElement
	 * Author : AIM Systems, Inc Date : 2019.07.01
	 */
	public static Document addReturnBodyElement( Document doc, List<Map<String, Object>> appendReturnData )
	{
		String XPathBody = "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag;
		JdomUtils.addElement( doc, XPathBody, "DATALIST", "" );
		for ( int i = 0; i < appendReturnData.size(); i++ )
		{
			ListOrderedMap orderMap = (ListOrderedMap) appendReturnData.get( i );
			MapIterator map = orderMap.mapIterator();

			Element Snode = null;

			Snode = new Element( "DATA" );

			while ( map.hasNext() )
			{
				String Key = (String) map.next();
				String value = null;

				if ( map.getValue() == null )
				{
					value = "";
				}
				else if ( map.getValue() instanceof String )
				{
					value = (String) map.getValue();
				}
				else
				{
					value = String.valueOf( map.getValue() );
				}

				Element Vnode = null;

				Vnode = new Element( Key );
				Vnode.setContent( new org.jdom.CDATA( value ) );

				Snode.addContent( Vnode );
			}

			doc.getRootElement().getChild( SMessageUtil.Body_Tag ).getChild( "DATALIST" ).addContent( Snode );
		}

		return doc;
	}
	
	/*
	* Name : setResultItemValue
	* Desc : This function is setResultItemValue
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static void setHeaderItemValue(Document doc, String nodeName, String nodeValue)                                    
	{
		if (doc.getRootElement().getChild("Header") == null)
			doc.getRootElement().addContent(new Element("Header"));
		
		String nodePath = "//" + Message_Tag + "/" + "Header" + "/";
		setElement(nodePath, doc, nodeName, nodeValue);
	}
	/*
	* Name : createXmlDocument
	* Desc : This function is createXmlDocument
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	public static Document createXmlDocument(String receivedData) throws Exception      
	{
		Element message = new Element( Message_Tag );                                                                                                            
		Document document = new Document( message );    

		
		int idx = receivedData.indexOf(" ");
		String commandName = receivedData.substring(0, idx).trim();
		HashMap<String, String> messageMap = null;
		messageMap = parsingStringMessage(receivedData);
		
		Element header = new Element( Header_Tag );       	
		
		Element subElement = new Element( BPELName_Tag );    
		subElement.setText(commandName);
		header.addContent(subElement);
		
		subElement = new Element( MessageName_Tag );    
		subElement.setText(commandName);
		header.addContent(subElement);
		message.addContent(header);
		Element body = new Element( Body_Tag );		                                                                                                               
		Element ele = null;
		while (messageMap.keySet().iterator().hasNext())
		{
			String keyName = messageMap.keySet().iterator().next();
			String keyValue = messageMap.remove(keyName);
			ele = new Element( keyName );                                                                                                               
			ele.setText( keyValue );                                                                                                                            
			body.addContent( ele );       			
		}
		message.addContent(body);
		messageMap.clear();
//		log.debug(JdomUtils.toString(document));
		return document;
	}
	
	/*
	* Name : setItemValue
	* Desc : This function is setItemValue
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
    public static void setItemValue(Document doc, String elementName, String nodeName, String nodeValue)                                    
	{                                                                                         
		try {
			String nodePath = "//" + Message_Tag + "/" + elementName + "/" + nodeName;
			Element element = JdomUtils.getNode(doc, nodePath);
			if (element != null)
				JdomUtils.setNodeText(doc, nodePath, nodeValue);     
			else {
				nodePath = "//" + Message_Tag + "/" + elementName + "/";
 				if (nodePath.endsWith("/")) 
					nodePath = nodePath.substring(0, nodePath.length()-1);
				Element headerElement = JdomUtils.getNode(doc, nodePath);
				JdomUtils.addElement(headerElement, nodeName, nodeValue);
			}
		} catch (Exception e) {                                                                 
			log.error(e, e);
		}                                                                                                                                                                  	
	}    
    /*
	* Name : parsingStringMessage
	* Desc : This function is parsingStringMessage
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	private static HashMap<String, String> parsingStringMessage(String msg)
	{
		String delimeter = "[{X*X}]";  
		
		String msg2 = msg;
		while(true)
		{
			int idx1 = msg2.indexOf("=[");
			int idx2 = msg2.indexOf("]", idx1);
			if (idx1 > 0 && idx2 > 0)
			{
				String a = msg2.substring(idx1+2, idx2);
				if (a.length() == 0)
				{
					msg2 = msg2.substring(idx2+1, msg2.length()); continue;
				}
				String b = org.springframework.util.StringUtils.replace(a, "=", "($%^)");
				msg = org.springframework.util.StringUtils.replace(msg, a, b);				
				msg2 = msg2.substring(idx2, msg2.length());
			}
			else break;
		}
		
		String [] messageSplit = msg.split("=");
		HashMap<String, String> params = new HashMap<String, String>();
		StringBuffer keyNameBuffer = new StringBuffer();
		keyNameBuffer.append("Command").append(delimeter);
		StringBuffer valueBuffer = new StringBuffer();
		for (int i=0; i<messageSplit.length; i++)
		{
			if (getKeyName(messageSplit[i]).length() > 0) {
				keyNameBuffer.append(getKeyName(messageSplit[i])).append(delimeter);
				valueBuffer.append(getValue(messageSplit[i])).append(delimeter);
			}
			else {
				valueBuffer.append(messageSplit[i]).append(delimeter);
			}
		}

		String[] keyNames = org.springframework.util.StringUtils.delimitedListToStringArray(keyNameBuffer.toString(), delimeter);
		String[] keyvalues = org.springframework.util.StringUtils.delimitedListToStringArray(valueBuffer.toString(), delimeter);
		
		//if (keyNames.length == (keyvalues.length+1))
			
			
		for (int i=1; i<keyNames.length; i++)
		{
			if (keyNames[i] == null || keyNames[i].trim().length() == 0) continue;
			try {
				keyvalues[i] = keyvalues[i].trim();
				keyvalues[i] = org.springframework.util.StringUtils.replace(keyvalues[i], "($%^)", "=");
				if (keyvalues[i].startsWith("[") && keyvalues[i].endsWith("]"))
					keyvalues[i] = keyvalues[i].substring(1, keyvalues[i].length()-1);
				params.put(keyNames[i], keyvalues[i].trim());
			} catch (Exception e) {
				params.put(keyNames[i], "");
				log.debug("Value of last data is empty");
			}
		}
		return params;
	}	
	/*
	* Name : getKeyName
	* Desc : This function is getKeyName
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	private static String getKeyName(String partialMsg)
	{
		if (partialMsg.startsWith("[") && partialMsg.endsWith("]"))
			return "";
		String character = "";
		String value = "";
		for (int i=partialMsg.length(); i>0; i--)
		{
			character = partialMsg. substring(i-1, i);
			if (character.equalsIgnoreCase(" "))
			{
				return StringUtils.reverse(value);
			}
			else {
				value += character;
			}
		}
		return "";
	}
	/*
	* Name : getValue
	* Desc : This function is getValue
	* Author : AIM Systems, Inc
	* Date : 2011.01.19
	*/
	private static String getValue(String partialMsg)
	{
		partialMsg = StringUtils.removeEnd(partialMsg, getKeyName(partialMsg));
//		try {
//			if (partialMsg.trim().length() == 0)
//				return "";
//		} catch (Exception e) {}
		return partialMsg;
	}
	
	/**
	 * to deliver generic inquiry message
	 * @author swcho
	 * @since 2013.09.23
	 * @param messageName
	 * @param paramMap
	 * @param sOriginalSourceSubjectName
	 * @param sEventUser
	 * @param sEventName
	 * @return
	 */
	public static Document generateQueryMessage(String messageName, HashMap<String, Object> paramMap, HashMap<String, Object> bindMap,
												String sOriginalSourceSubjectName, String sEventUser, String sEventComment)
	{
		String sTimeKey = TimeUtils.getCurrentEventTimeKey();
		
		//envelopement
		Element message = new Element(Message_Tag);
		{
			//header
			Element header = new Element(Header_Tag);
			{
				Element messagename = new Element(MessageName_Tag);
				messagename.setText(messageName);
				header.addContent(messagename);
				
				Element transactionId = new Element("TRANSACTIONID");
				transactionId.setText(sTimeKey);
				header.addContent(transactionId);
				
				Element originalSourceSubjectName = new Element("ORIGINALSOURCESUBJECTNAME");
				originalSourceSubjectName.setText(sOriginalSourceSubjectName);
				header.addContent(originalSourceSubjectName);
				
				Element eventUser = new Element("EVENTUSER");
				eventUser.setText(sEventUser);
				header.addContent(eventUser);
				
				Element eventComment = new Element("EVENTCOMMENT");
				eventComment.setText(sEventComment);
				header.addContent(eventComment);
			}
			message.addContent(header);
			
			//body
			Element bodyElement = new Element(Body_Tag);
			{
				for (String keyName : paramMap.keySet())
				{
					Element paramElement = new Element(keyName);
					paramElement.setText(paramMap.get(keyName).toString());
					bodyElement.addContent(paramElement);
				}
				
				//Element bindMapElement = new Element("BINDV");
				
				for (String keyName : bindMap.keySet())
				{
					Element bindElement = new Element(keyName);
					bindElement.setText(bindMap.get(keyName).toString());
					bodyElement.addContent(bindElement);
				}
				
				//bodyElement.addContent(bindMapElement);
			}
			message.addContent(bodyElement);
			
			//result
			Element returnElement = new Element(Result_Name_Tag);
			{
				Element returnCode = new Element(Result_ReturnCode);
				returnCode.setText(ESBService.SUCCESS);
				returnElement.addContent(returnCode);
				
				Element returnMessage = new Element(Result_ErrorMessage);
				returnMessage.setText("");
				returnElement.addContent(returnMessage);
			}
			message.addContent(returnElement);
		}

		//converting element to message
		Document replyDoc = new Document(message);
		
		return replyDoc;
	}
	
	
	/**
	 * @param doc
	 	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getMessageName
	 * @category GVO
	 * @param itemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static String getMessageName(Document doc) throws CustomException                            
	{  
		String messageName = getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "MESSAGENAME");
		
		return messageName;
	}
	
	/**
	 * @param doc
	 	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getMessageName
	 * @category GVO
	 * @param itemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static Element getBodyElement(Document doc) throws CustomException                            
	{  
		Element bodyElement = null;
		
		try 
		{
			bodyElement = XmlUtil.getNode(doc, new StringBuilder("//").append(SMessageUtil.Message_Tag)
													.append("/").append(SMessageUtil.Body_Tag)
													.toString());
		} catch (Exception e) {
			throw new CustomException("SYS-0001", SMessageUtil.Body_Tag);
		}
		
		return bodyElement;
	}
	
	/**
	 * @param doc
	 	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getHeaderItemValue
	 * @category GVO
	 * @param itemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static String getHeaderItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Header_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Header_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	
	/**
	 * @param doc
	 	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getBodyItemValue
	 * @category GVO
	 * @param itemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static String getBodyItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	/**
	 * 150309 modified by swcho
	 * @author hykim
	 * @since 2014.1.10
	 * @param doc
	 * @param itemName
	 * @param itemValue
	 * @throws CustomException
	 */
	public static void setBodyItemValue(Document doc, String itemName, String itemValue)
		throws CustomException                            
	{
		try
		{
			Element element = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + itemName);
			
			if (element != null)
			{
				JdomUtils.setNodeText(doc, "//" + Message_Tag + "/" + Body_Tag, itemName, itemValue);
			}
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", itemName);
		}
	}
	
	
	/**
	 * 180309 modified by wghuang
	 * @author swcho
	 * @since 2015.3.9
	 * @param doc
	 * @param itemName
	 * @param itemValue
	 * @param required
	 * @throws CustomException
	 */
	public static void setBodyItemValue(Document doc, String itemName, String itemValue, boolean required)
		throws CustomException                            
	{
		try
		{
			Element element = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + itemName);
			
			if (element != null)
			{
				JdomUtils.setNodeText(doc, "//" + Message_Tag + "/" + Body_Tag, itemName, itemValue);
			}
			else
			{
				if(required)
				{
					JdomUtils.setNodeText(doc, "//" + Message_Tag + "/" + Body_Tag, itemName, itemValue);
				}
			}
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", itemName);
		}
	}
	
	
	/**
	 * 
	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getBodySequenceItem
	 * @category GVO
	 * @param doc
	 * @param listItemName
	 * @param required
	 * @return Element
	 * @throws CustomException
	 */
	public static Element getBodySequenceItem(Document doc, String listItemName, boolean required) throws CustomException                                    
	{	
        Element listItem = null;
        
		try 
		{	
			listItem = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + listItemName);			
		} 
		catch (Exception e) 
		{
		}
		
		if(required == true)
		{	
			String itemName = "";
			//if (StringUtil.isEmpty(doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName()))
			//	throw new CustomException("SYS-0001", Body_Tag + listItemName);		
			
			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", listItemName);
			}
			
			itemName = listItemName.substring(0, listItemName.indexOf("LIST"));
			
			if(listItem.getChildren().size() == 0)
				throw new CustomException("SYS-0001", itemName);
			
			for ( Iterator iterator = listItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element item = (Element) iterator.next();
				
				if (!StringUtil.equals(item.getName(), itemName))
					throw new CustomException("SYS-0001", itemName);
			}			
			//log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
		}
		
		return listItem;                                                                              
	}
	
	/**
	 * 
	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getBodySequenceItemList
	 * @category GVO
	 * @param doc
	 * @param listItemName
	 * @param required
	 * @return List<Element>
	 * @throws CustomException
	 */
	public static List<Element> getBodySequenceItemList(Document doc, String listItemName, boolean required) throws CustomException                                    
	{	
        Element seqItem = null;
        List<Element> seqItemList = new ArrayList<Element>();
        
		try 
		{	
			seqItem = JdomUtils.getNode(doc, "//" + Message_Tag + "/" + Body_Tag + "/" + listItemName);	
			
			for ( Iterator iterator = seqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element item = (Element) iterator.next();	
				seqItemList.add(item);
			}
		} 
		catch (Exception e) 
		{
		}
		
		if(required == true)
		{	
			String itemName = "";
			//if (StringUtil.isEmpty(doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName()))
			//	throw new CustomException("SYS-0001", Body_Tag + listItemName);		
			
			try
			{
				doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", listItemName);
			}
			
			itemName = listItemName.substring(0, listItemName.indexOf("LIST"));
			
			if(seqItem.getChildren().size() == 0)
				throw new CustomException("SYS-0001", itemName);
			
			for ( Iterator iterator = seqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element item = (Element) iterator.next();
				
				if (!StringUtil.equals(item.getName(), itemName))
					throw new CustomException("SYS-0001", itemName);
			}			
			//log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
		}
		
		return seqItemList;                                                                              
	}
	
	/**
	 * 
	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getSubSequenceItem
	 * @category GVO
	 * @param doc
	 * @param subListItemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static Element getSubSequenceItem(Element parentE, String subListItemName, boolean required) throws CustomException                                    
	{	
        Element childItemList = null;
        
        try 
		{	
        	childItemList = parentE.getChild(subListItemName);
		} 
		catch (Exception e) 
		{
		}
        
		if(required == true)
		{	
			String childItemName = "";
			//if (StringUtil.isEmpty(doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName()))
			//	throw new CustomException("ITEM-0000", Body_Tag + listItemName);		
			
			try
			{
				parentE.getChild(subListItemName).getName();
			}
			catch(Exception e)
			{
				//throw new CustomException("ITEM-0000", subListItemName);
			    throw new CustomException("SYS-0001", subListItemName);
			}
			
			childItemName = subListItemName.substring(0, subListItemName.indexOf("LIST"));
			
			childItemList = parentE.getChild(subListItemName);
			
			if(childItemList.getChildren().size() == 0)
			{
				//throw new CustomException("ITEM-0000", childItemName);
			    throw new CustomException("SYS-0002", childItemName);
			}
			
			for ( Iterator iterator = childItemList.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();
				
				if (!StringUtil.equals(childItem.getName(), childItemName))
				{
					//throw new CustomException("ITEM-0000", childItemName);
				    throw new CustomException("SYS-0002", childItemName);
				}
			}			
			//log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
		}
		
		return childItemList;                                                                              
	}
	
	/**
	 * 
	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getBodySequenceItemList
	 * @category GVO
	 * @param doc
	 * @param listItemName
	 * @param required
	 * @return List<Element>
	 * @throws CustomException
	 */
	public static List<Element> getSubSequenceItemList(Element parentE, String subListItemName, boolean required) throws CustomException                                    
	{	
        Element childSeqItem = null;
        List<Element> childSeqItemList = new ArrayList<Element>();
        
        try 
		{			
			childSeqItem = parentE.getChild(subListItemName);
			
			for ( Iterator iterator = childSeqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();			
				childSeqItemList.add(childItem);
			}
		} 
		catch (Exception e) 
		{
		}
        
		if(required == true)
		{	
			String childItemName = "";
			//if (StringUtil.isEmpty(doc.getRootElement().getChild(Body_Tag).getChild(listItemName).getName()))
			//	throw new CustomException("ITEM-0000", Body_Tag + listItemName);		
			
			try
			{
				parentE.getChild(subListItemName).getName();
			}
			catch(Exception e)
			{//SYS-0001
				//throw new CustomException("ITEM-0000", subListItemName);
				throw new CustomException("SYS-0001", subListItemName);
			}
			
			childItemName = subListItemName.substring(0, subListItemName.indexOf("LIST"));
			
			childSeqItem = parentE.getChild(subListItemName);
			
			if(childSeqItem.getChildren().size() == 0)
			{
				//throw new CustomException("ITEM-0000", childItemName);
			    throw new CustomException("SYS-0002", childItemName);
			}
			
			for ( Iterator iterator = childSeqItem.getChildren().iterator(); iterator.hasNext(); )
			{
				Element childItem = (Element) iterator.next();
				
				if (!StringUtil.equals(childItem.getName(), childItemName))
				{
					//throw new CustomException("ITEM-0000", childItemName);
				    throw new CustomException("SYS-0002", childItemName);
				}
			}			
			//log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
		}
		
		return childSeqItemList;                                                                              
	}
	
	/**
	 * @param doc
	 	 * @author hykim
	 * @since 2014.1.10
	 * Desc : getReturnItemValue
	 * @category GVO
	 * @param itemName
	 * @param required
	 * @return
	 * @throws CustomException
	 */
	public static String getReturnItemValue(Document doc, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		try
		{
			itemValue = JdomUtils.getNodeText(doc, "//" + Message_Tag + "/" + Return_Tag + "/" + itemName);
		}
		catch(Exception e)
		{
		}

		if(required == true)
		{
			try
			{
				doc.getRootElement().getChild(Return_Tag).getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
				
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	/*
	* Name : getBodyItemValue
	* Desc : This function is getBodyItemValue
	* Author : AIM Systems, Inc
	* Date : 2014.01.10 hykim
	*/
	public static String getChildText(Element element, String itemName, boolean required) throws CustomException                            
	{                   
		String itemValue = "";
		
		try
		{
			itemValue = element.getChildText(itemName);
		}
		catch (Exception ex)
		{
			itemValue = "";
		}
		
		if (itemValue == null)
			itemValue = "";
		
		if(required == true)
		{
			try
			{
				element.getChild(itemName).getName();
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-0001", itemName);
			}
			
			if (StringUtil.isEmpty(itemValue))
				throw new CustomException("SYS-0002", itemName);
		}                                                                                  
		return itemValue;                                                                              
	}
	
	/**
	 * @author hykim
	 * @since 2014-04-16
	 * @param 
	 * @return Document
	 */
	public static Document addItemToBody(Document doc, String itemName, String itemValue) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild("Body");

		// Add LotIdElement
		if (StringUtil.isNotEmpty(itemName)) {
			Element itemE = new Element(itemName);
			itemE.setText(itemValue);
			bodyElement.addContent(itemE);
		}
		else
		{
			throw new CustomException("SYS-0003", itemName, itemValue);
		}
		
		return doc;
	}
	
	/**
	 * @author hykim
	 * @since 2014-04-16
	 * @param 
	 * @return Document
	 */
	public static Document addItemToReturn(Document doc, String itemName, String itemValue) throws CustomException
	{
		Element returnElement = doc.getRootElement().getChild("Return");

		// Add LotIdElement
		if (StringUtil.isNotEmpty(itemName)) {
			Element itemE = new Element(itemName);
			itemE.setText(itemValue);
			returnElement.addContent(itemE);
		}
		else
		{
			throw new CustomException("SYS-0003", itemName, itemValue);
		}
		
		return doc;
	}
	
	/**
	 * @author hykim
	 * @since 2014-04-16
	 * @param 
	 * @return Document
	 */
	public static Document addReturnToMessage(Document doc, String returnCode, String returnMessage) throws CustomException
	{
		// Add LotIdElement
		try
		{
			Element returnElement = doc.getRootElement().getChild("Return");
			
			Element retunCode = returnElement.getChild("RETURNCODE");
			Element retunMessage = returnElement.getChild("RETURNMESSAGE");
		}
		catch(Exception ex)
		{
			Element root = doc.getRootElement();
			
			Element returnE = new Element("Return");
			root.addContent(returnE);
			
			Element retunCodeE = new Element("RETURNCODE");
			retunCodeE.setText(returnCode);
			returnE.addContent(retunCodeE);
			
			Element retunMessageE = new Element("RETURNMESSAGE");
			retunMessageE.setText(returnMessage);
			returnE.addContent(retunMessageE);
		}
		
		return doc;
	}
	
	/**
	 * generate standard XML message format
	 * @author swcho
	 * @since 2014.06.27
	 * @param bodyElement
	 * @param messageName
	 * @param originalSourceSubjectName
	 * @param targetSubjectName
	 * @param eventUser
	 * @param eventComment
	 * @return
	 * @throws Exception
	 */
	public static Document createXmlDocument(Element bodyElement, String messageName, String originalSourceSubjectName, String targetSubjectName, String eventUser, String eventComment)
		throws Exception      
	{
		Element message = new Element( Message_Tag );                                                                                                            
		Document document = new Document( message );
		
		Element header = createHeaderElement(messageName, TimeUtils.getCurrentEventTimeKey(), originalSourceSubjectName, originalSourceSubjectName, targetSubjectName, eventUser, eventComment, "ENG");       	
		message.addContent(header);
		
		//body must be orphan
		if (bodyElement != null && bodyElement.getName().equals(Body_Tag))
			message.addContent(bodyElement);
		
		// Added by smkang on 2019.01.18 - Making Return Element.
		Element returnElement = new Element(Return_Tag);
		returnElement.addContent(new Element(Result_ReturnCode).setText("0"));
		returnElement.addContent(new Element(Result_ErrorMessage));
		
		return document;
	}
	public static Document CreateXmlDocumentForFMC(String messageName) throws Exception{
		Element message = new Element( Message_Tag );                                                                                                            
		Document document = new Document( message );
		
		Element header = new Element( Header_Tag );       	
		Element eleMessageName = new Element( MessageName_Tag );    
		eleMessageName.setText(messageName);
		header.addContent(eleMessageName);
		message.addContent(header);
		
		Element returnElement = new Element(Return_Tag);
		returnElement.addContent(new Element(Result_ReturnCode).setText("0"));
		returnElement.addContent(new Element(Result_ErrorMessage));
		message.addContent(returnElement);
		
		return document;
	}
	// Added by Park Jeong Su on 2019.02.01 - create Simple Document.
	public static Document createXmlDocument(String messageName, String originalSourceSubjectName, String targetSubjectName, EventInfo eventInfo)
			throws Exception      
		{
			Element message = new Element( Message_Tag );                                                                                                            
			Document document = new Document( message );
			
			Element header = createHeaderElement(messageName, eventInfo.getEventTimeKey(), originalSourceSubjectName, originalSourceSubjectName, targetSubjectName, eventInfo.getEventUser(), eventInfo.getEventComment(), "ENG");       	
			message.addContent(header);
			
			Element bodyElement = new Element(Body_Tag);
			message.addContent(bodyElement);
			
			Element returnElement = new Element(Return_Tag);
			returnElement.addContent(new Element(Result_ReturnCode).setText("0"));
			returnElement.addContent(new Element(Result_ErrorMessage));
			message.addContent(returnElement);
			
			//log.debug(JdomUtils.toString(document));
			
			return document;
		}

	
	/**
     * generate standard XML message format
     * @author swcho
     * @since 2014.06.27
     * @param bodyElement
     * @param messageName
     * @param originalSourceSubjectName
     * @param targetSubjectName
     * @param eventUser
     * @param eventComment
     * @return
     * @throws Exception
     */
    public static Document createXmlDocumentWithOutLanguage(Element bodyElement, String messageName, String originalSourceSubjectName, String targetSubjectName, String eventUser, String eventComment)
        throws Exception      
    {
        Element message = new Element( Message_Tag );                                                                                                            
        Document document = new Document( message );
        
        Element header = createHeaderElementWithOutLanguage(messageName, TimeUtils.getCurrentEventTimeKey(), originalSourceSubjectName, originalSourceSubjectName, targetSubjectName, eventUser, eventComment, "ENG");         
        message.addContent(header);
        
        //body must be orphan
        if (bodyElement != null && bodyElement.getName().equals(Body_Tag))
            message.addContent(bodyElement);
        
        return document;
    }
    
	/**
	 * standard XML header format
	 * @author swcho
	 * @since 2014.06.27
	 * @param messageName
	 * @param transactionId
	 * @param originalSourceSubjectName
	 * @param sourceSubjectName
	 * @param targetSubjectName
	 * @param eventUser
	 * @param eventComment
	 * @param language
	 * @return
	 * @throws Exception
	 */
	public static Element createHeaderElement(String messageName, String transactionId,
											String originalSourceSubjectName, String sourceSubjectName, String targetSubjectName,
											String eventUser, String eventComment, String language)
		throws Exception
	{
		Element header = new Element( Header_Tag );       	
		
		Element eleMessageName = new Element( MessageName_Tag );    
		eleMessageName.setText(messageName);
		header.addContent(eleMessageName);
		
		Element eleTrxId = new Element( "TRANSACTIONID" );    
		eleTrxId.setText(transactionId);
		header.addContent(eleTrxId);
		
		Element eleOriginalSRCSubject = new Element( "ORIGINALSOURCESUBJECTNAME" );    
		eleOriginalSRCSubject.setText(originalSourceSubjectName);
		header.addContent(eleOriginalSRCSubject);
		
		//Element eleSRCSubject = new Element( "SOURCESUBJECTNAME" );    
		//eleSRCSubject.setText(sourceSubjectName);
		//header.addContent(eleSRCSubject);
		
		//Element eleTargetSubejct = new Element( "TARGETSUBJECTNAME" );    
		//eleTargetSubejct.setText(targetSubjectName);
		//header.addContent(eleTargetSubejct);
		
		Element eleEventUser = new Element( "EVENTUSER" );    
		eleEventUser.setText(eventUser);
		header.addContent(eleEventUser);
		
		Element eleEventComment = new Element( "EVENTCOMMENT" );    
		eleEventComment.setText(eventComment);
		header.addContent(eleEventComment);
		
		Element eleLanguage = new Element( "LANGUAGE" );    
		eleLanguage.setText(language);
		header.addContent(eleLanguage);
		
		return header;
	}
	
	/**
     * standard XML header format
     * @author swcho
     * @since 2014.06.27
     * @param messageName
     * @param transactionId
     * @param originalSourceSubjectName
     * @param sourceSubjectName
     * @param targetSubjectName
     * @param eventUser
     * @param eventComment
     * @param language
     * @return
     * @throws Exception
     */
    public static Element createHeaderElementWithOutLanguage(String messageName, String transactionId,
                                            String originalSourceSubjectName, String sourceSubjectName, String targetSubjectName,
                                            String eventUser, String eventComment, String language)
        throws Exception
    {
        Element header = new Element( Header_Tag );         
        
        Element eleMessageName = new Element( MessageName_Tag );    
        eleMessageName.setText(messageName);
        header.addContent(eleMessageName);
        
        Element eleTrxId = new Element( "TRANSACTIONID" );    
        eleTrxId.setText(transactionId);
        header.addContent(eleTrxId);
        
        Element eleOriginalSRCSubject = new Element( "ORIGINALSOURCESUBJECTNAME" );    
        eleOriginalSRCSubject.setText(originalSourceSubjectName);
        header.addContent(eleOriginalSRCSubject);
        
        //Element eleSRCSubject = new Element( "SOURCESUBJECTNAME" );    
        //eleSRCSubject.setText(sourceSubjectName);
        //header.addContent(eleSRCSubject);
        
        //Element eleTargetSubejct = new Element( "TARGETSUBJECTNAME" );    
        //eleTargetSubejct.setText(targetSubjectName);
        //header.addContent(eleTargetSubejct);
        
        Element eleEventUser = new Element( "EVENTUSER" );    
        eleEventUser.setText(eventUser);
        header.addContent(eleEventUser);
        
        Element eleEventComment = new Element( "EVENTCOMMENT" );    
        eleEventComment.setText(eventComment);
        header.addContent(eleEventComment);
        
        //Element eleLanguage = new Element( "LANGUAGE" );    
        //eleLanguage.setText(language);
        //header.addContent(eleLanguage);
        
        return header;
    }
}
