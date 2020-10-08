/*
 ****************************************************************************
 *
 *  (c) Copyright 2009 AIM Systems, Inc. All rights reserved.
 *
 *  This software is proprietary to and embodies the confidential
 *  technology of AIM Systems, Inc. Possession, use, or copying of this
 *  software and media is authorized only pursuant to a valid written
 *  license from AIM Systems, Inc.
 *
 ****************************************************************************
 */

package kr.co.aim.messolution.query.service;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.SqlCursorItemReader;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.util.StringUtils;

/*
 ****************************************************************************
 *  PACKAGE : kr.co.aim.messolution.query.service
 *  NAME    : QueryServiceProxy.java
 *  TYPE    : JAVA
 *  DESCRIPTION :
 *
 ****************************************************************************
 */

public class QueryServiceUtil
{
	private static Log log = LogFactory.getLog(QueryServiceUtil.class);

	/*
	* Name : createXmlByList
	* Desc : This function is createXmlByList
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public static String createXmlByList(SqlCursorItemReader reader, String messageName,
            							 String sourceSubject,
            							 String targetSubject,
            							 String transactionId,
            							 String queryID, String version) throws Exception {
		
		if(log.isInfoEnabled()){
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
			log.debug("queryId = " + queryID);
			log.debug("version = " + version);
		}
		
        StringBuilder sXmlMsg = new StringBuilder(50000);
        
        sXmlMsg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ").append(SystemPropHelper.CR);
        sXmlMsg.append("  <Message>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Header>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <MESSAGENAME>").append(messageName).append("</MESSAGENAME>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <SOURCESUBJECT>").append(sourceSubject).append("</SOURCESUBJECT>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <TARGETSUBJECT>").append(targetSubject).append("</TARGETSUBJECT>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <TRANSACTIONID>").append(transactionId).append("</TRANSACTIONID>").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Header>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Body>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <QUERYID>").append(queryID).append("</QUERYID>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <VERSION>").append(version).append("</VERSION>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <DATALIST>").append(SystemPropHelper.CR);
        reader.open();
        int dataSize = 0;
		reader.setListMapper();
		reader.setAppendSpace("          ");
		while(true) 
		{
	        try {
	        	ListOrderedMap orderMap = (ListOrderedMap)reader.read();
	        	if (orderMap == null) break;
				MapIterator map = orderMap.mapIterator();
				dataSize++;
		        sXmlMsg.append("        <DATA>").append(SystemPropHelper.CR);
				while (map.hasNext()) {
					String Key = (String) map.next();
					String value = null;
					if (map.getValue() == null) {
						value = "";
					} else if (map.getValue() instanceof String){
						value = (String) map.getValue();
					}else{
						value = map.getValue().toString();
					}
					
					//141201 by swcho : illegal character handler
					//value = replaceIllegalCharacter(value);
					//160324 by swcho : CDATA section handler
					value = wrapUnparsedSection(value);
					
			        sXmlMsg.append("          <").append(Key).append(">").append(value).append("</").append(Key).append(">").append(SystemPropHelper.CR);
				}
		        sXmlMsg.append("        </DATA>").append(SystemPropHelper.CR);
		        orderMap.clear();
	        } catch (Exception e) {
	        	log.error(e);
	        	break;
	        }
		}
        reader.close();
        sXmlMsg.append("      </DATALIST>").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Body>").append(SystemPropHelper.CR);
        sXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
        sXmlMsg.append("      <").append(SMessageUtil.Result_ReturnCode).append(">").append("0").append("</").append(SMessageUtil.Result_ReturnCode).append(">").append(SystemPropHelper.CR);
        sXmlMsg.append("      <").append(SMessageUtil.Result_ErrorMessage).append(">").append("</").append(SMessageUtil.Result_ErrorMessage).append(">").append(SystemPropHelper.CR);
        sXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
        sXmlMsg.append("  </Message>").append(SystemPropHelper.CR);
		log.info(">> Create Message : " + dataSize);	
		log.info("After - createXml");
		return sXmlMsg.toString();
	}
	public static String createXmlByListName(
			List<Map<String, Object>> sqlResult, String messageName,
			String sourceSubject, String targetSubject, String transactionId,
			String queryID, String version) throws Exception {

		Document doc = new Document();
		Element message = new Element(SMessageUtil.Message_Tag);
		Element header = new Element(SMessageUtil.Header_Tag);
		header.addContent(new Element(SMessageUtil.MessageName_Tag).setText(messageName));
		header.addContent(new Element("SOURCESUBJECT").setText(sourceSubject));
		header.addContent(new Element("TARGETSUBJECT").setText(targetSubject));
		header.addContent(new Element("TRANSACTIONID").setText(transactionId));
		message.addContent(header);

		Element body = new Element(SMessageUtil.Result_Tag);
		body.addContent(new Element("QUERYID").setText(queryID));
		body.addContent(new Element("VERSION").setText(version));
		
		String sql = "select t.enumvalue from enumdefvalue t where t.enumname = 'SkipQueryMaxCount' and t.enumvalue = ? ";
		String[] args = new String[] { queryID+version };
		List skipList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, args);
				
		Element dataList = new Element("DATALIST");
		int dataSize = 0;

		for (int i = 0; i < sqlResult.size(); i++) {
			ListOrderedMap list = (ListOrderedMap) sqlResult.get(i);

			MapIterator mapIterator = list.mapIterator();

			try {
				dataSize++;
				Element data = new Element(SMessageUtil.Data);
				while (mapIterator.hasNext()) {
					String Key = (String) mapIterator.next();
					String value = null;

					if (mapIterator.getValue() == null) {
						value = "";
					} else if (mapIterator.getValue() instanceof String) {
						value = (String) mapIterator.getValue();
					} else {
						value = String.valueOf(mapIterator.getValue());
					}
					data.addContent(new Element(Key).setText(value == null ? ""
							: value));
				}
				dataList.addContent(data);
			} catch (Exception e) {
				break;
			}

		}
		//
		body.addContent(dataList);
		message.addContent(body);
		Element returnElement = new Element(SMessageUtil.Return_Tag);
		returnElement
				.addContent(new Element(SMessageUtil.Return_ReturnCode_Tag)
						.setText("0"));
		returnElement.addContent(new Element(
				SMessageUtil.Return_ErrorMessage_Tag));
		message.addContent(returnElement);
		doc.setRootElement(message);
		log.info(">> Create Message : " + dataSize);
		
			
		//ConstantMap constantmap = GenericServiceProxy.getConstantMapExtends();
		// 2019.04.22
//		String maxSize = GenericServiceProxy.getConstantMapExtends().getConstantDefsMap().get("MaxQueryCount").toString();
//		if (skipList.size()==0 && maxSize != null && dataSize > Integer.parseInt(maxSize)) {
//			throw new CustomException("OverSizedOfMaxQueryCount", version+ "," + queryID, maxSize, dataSize);
//		}
			
		log.info("After - createXml");
		String sXmlMsg = JdomUtils.toString(doc);
		return sXmlMsg;
	}

	/**
	 * return a string replace & < > use &amp; &lt; &gt;
	 * @author lvmingtao
	 * @since 2011.01.01
	 * @param value
	 * @return
	 */
	public static String replaceIllegalCharacter(String value) {
		String str = value.replace("&", "&amp;");
		str = str.replace("<", "&lt;");
		str = str.replace(">", "&gt;");
		return str;
	}
	
	/**
	 * keep value as character
	 * @author swcho
	 * @since 2016-03-24
	 * @param value
	 * @return
	 */
	public static String wrapUnparsedSection(String value)
	{
		StringBuffer valueBuffer = new StringBuffer().append("<![CDATA[").append(value).append("]]>");
		
		return valueBuffer.toString();
	}

	/*
	* Name : createStringByList
	* Desc : This function is createStringByList
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public static String createStringByList(SqlCursorItemReader reader, String messageName,
			                                String sourceSubject,
			                                String targetSubject,
			                                String transactionId,
			                                String queryID, String version) throws Exception {

		if(log.isInfoEnabled()){
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
			log.debug("queryId = " + queryID);
			log.debug("version = " + version);
		}
		
        StringBuilder sStrMsg = new StringBuilder(50000);
        
        sStrMsg.append(messageName).append(".REP ");
        sStrMsg.append("HDR(").append(sourceSubject).append(",").append(targetSubject);
        sStrMsg.append(",").append(transactionId).append(") ");
        sStrMsg.append("QUERYID=").append(queryID).append(" ");
        sStrMsg.append("VERSION=").append(version).append(" ");
        sStrMsg.append("DATA=\"");
        
        reader.open();
        int dataSize = 0;
		reader.setListMapper();
		reader.setAppendSpace("          ");
		while(true) 
		{
	        try {
	        	ListOrderedMap orderMap = (ListOrderedMap)reader.read();
	        	if (orderMap == null) break;
				MapIterator map = orderMap.mapIterator();
				dataSize++;
				while (map.hasNext()) {
					String Key = (String) map.next();
					String value = null;
					if (map.getValue() == null) {
						value = "";
					} else if (map.getValue() instanceof String)
						value = (String) map.getValue();
					else
						value = map.getValue().toString();
					sStrMsg.append(value);
					sStrMsg.append(" ");
				}
				sStrMsg.append(SystemPropHelper.CR);
		        orderMap.clear();
	        } catch (Exception e) {
	        	log.error(e);
	        	break;
	        }
		}
		sStrMsg.append("\"");
        reader.close();
		return sStrMsg.toString();
	}	
	
	/**
	 * generate XML message 
	 * @author swcho
	 * @since 2014.04.03
	 * @param result
	 * @param messageName
	 * @param sourceSubject
	 * @param targetSubject
	 * @param transactionId
	 * @param queryID
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public static String createXmlByList(List<Map<String, Object>> result,
											String messageName,
											String sourceSubject,
											String targetSubject,
											String transactionId,
											String queryID, String version) throws Exception
	{
	
		if(log.isInfoEnabled())
		{
			log.debug("messageName = " + messageName);
			log.debug("sourceSubject = " + sourceSubject);
			log.debug("targetSubject = " + targetSubject);
			log.debug("transactionId = " + transactionId);
		//	log.debug("queryId = " + queryID);
		//	log.debug("version = " + version);
		}
		
		StringBuilder sXmlMsg = new StringBuilder(50000);
		
		sXmlMsg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ").append(SystemPropHelper.CR);
		sXmlMsg.append("  <Message>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Header>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <MESSAGENAME>").append(messageName).append("</MESSAGENAME>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <SOURCESUBJECT>").append(sourceSubject).append("</SOURCESUBJECT>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <TARGETSUBJECT>").append(targetSubject).append("</TARGETSUBJECT>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <TRANSACTIONID>").append(transactionId).append("</TRANSACTIONID>").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Header>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Body>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <QUERYID>").append(queryID).append("</QUERYID>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <VERSION>").append(version).append("</VERSION>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <DATALIST>").append(SystemPropHelper.CR);

		int dataSize = 0;
		
		for (Map row : result) 
		{
			try
			{
				ListOrderedMap orderMap = (ListOrderedMap)row;
				if (orderMap == null) break;
				
				MapIterator map = orderMap.mapIterator();
				dataSize++;
				
				sXmlMsg.append("        <DATA>").append(SystemPropHelper.CR);
				
				while (map.hasNext())
				{
					String Key = (String) map.next();
					String value = null;
					
					if (map.getValue() == null)
						value = "";
					else if (map.getValue() instanceof String)
						value = (String) map.getValue();
					else
						value = map.getValue().toString();
					
					//141201 by swcho : illegal character handler
					//value = replaceIllegalCharacter(value);
					//160324 by swcho : CDATA section handler
					value = wrapUnparsedSection(value);
					
					sXmlMsg.append("          <").append(Key).append(">").append(value).append("</").append(Key).append(">").append(SystemPropHelper.CR);
				}
				
				sXmlMsg.append("        </DATA>").append(SystemPropHelper.CR);
				orderMap.clear();
			}
			catch (Exception e)
			{
				log.error(e);
				break;
			}
		}
		
		
		sXmlMsg.append("      </DATALIST>").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Body>").append(SystemPropHelper.CR);
		sXmlMsg.append("    <Return>").append(SystemPropHelper.CR);
		sXmlMsg.append("      <").append(SMessageUtil.Result_ReturnCode).append(">").append("0").append("</").append(SMessageUtil.Result_ReturnCode).append(">").append(SystemPropHelper.CR);
		sXmlMsg.append("      <").append(SMessageUtil.Result_ErrorMessage).append(">").append("</").append(SMessageUtil.Result_ErrorMessage).append(">").append(SystemPropHelper.CR);
		sXmlMsg.append("    </Return>").append(SystemPropHelper.CR);
		sXmlMsg.append("  </Message>").append(SystemPropHelper.CR);
		log.info(">> Create Message : " + dataSize);	
		log.info("After - createXml");
		return sXmlMsg.toString();
	}
	public static String fillParameter(String sql,Map bindMap) throws CustomException
	{
		String fullSql=sql;
		for(Object key :bindMap.keySet() )
		{
			String keyStr=key.toString();
			if(bindMap.get(key) instanceof List)
			{
				List<String> item = (List<String>) bindMap.get(key);
				fullSql=fullSql.replaceAll(":"+keyStr, StringUtils.concat(item, "," ,"'", "'"));
			}
			else
			{
				fullSql=fullSql.replaceAll(":"+keyStr, "'"+bindMap.get(key).toString()+"'");
			}
		}
		return fullSql;
	}
	
}
