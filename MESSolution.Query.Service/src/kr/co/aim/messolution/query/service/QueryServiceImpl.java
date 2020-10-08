package kr.co.aim.messolution.query.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.SqlCursorItemReader;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class QueryServiceImpl {
	private static Log log = LogFactory.getLog(QueryServiceImpl.class);

	/*
	* Name : getQueryResult
	* Desc : This function is getQueryResult
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	public String getQueryResult(String messageName,
			                     String sourceSubject,
			                     String targetSubject,
			                     String transactionId,
			                     String queryId, String version, Element bindElement) 
		throws Exception
	{
		if(log.isInfoEnabled())
			log.info(String.format("GetQueryResult [%s %s] prepairing.....", queryId, version));

		//String usrSql = "SELECT queryString FROM CT_CustomQuery WHERE queryId = ? and version = ? ";
		String usrSql = "SELECT queryStringClob FROM CT_CustomQuery WHERE queryId = ? and version = ? ";

		String[] args = new String[] { queryId, version };

		List resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(usrSql, args);

		if (resultList.size() == 0)
			throw new CustomException("CanNotFoundQueryID", queryId, version);

		ListOrderedMap queryMap = (ListOrderedMap) resultList.get(0);
		//String dbSql = queryMap.get("queryString").toString();
		String dbSql = queryMap.get("queryStringClob").toString();
	
		resultList.clear();
	
		String result = "";
		if(StringUtils.indexOf(dbSql, "?") != -1){
			Object[] bind = getBindObjectByElement(bindElement);
			
			//if (bind != null){
			//	log.debug(">> SQL = [" + dbSql + "] : [" + bind.toString() + "]");
			//  log.info(">> BIND INFO : [" + bind.toString() + "]");
			//}
			//else
			//	log.debug(">> SQL = [" + dbSql + "] : []");
			
			SqlCursorItemReader reader = greenFrameServiceProxy.getSqlTemplate().queryByCursor(dbSql, bind);						
			try {
				result = QueryServiceUtil.createXmlByList(reader, messageName, sourceSubject,
																  targetSubject, transactionId, queryId, version);
				
				//if (log.isDebugEnabled())
					//log.debug(String.format(">>> ResultMessage: %s", result.toString()));
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			try {
				Map bindMap = getBindMapByElement(bindElement);
				
				//if (bindMap != null){
				//	log.debug(">> SQL = [" + dbSql + "] : [" + bindMap.toString() + "]");
				//	log.info(">> BIND INFO : [" + bindMap.toString() + "]");
				//}
				//else
				//	log.debug(">> SQL = [" + dbSql + "] : []");
								
				List<Map<String, Object>> sqlResult = 
					greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(dbSql, bindMap);
				//result = new QueryServiceProxy().createXml(resultList, queryID);
				result = QueryServiceUtil.createXmlByList(sqlResult, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
				//log.info("QueryID: " + queryId + ", Version: " + version + ", QueryResult: " + sqlResult.size());
				log.info(String.format("QueryID: %s, Version: %s, QueryResult: %d", queryId, version, sqlResult.size()));
				
				//if (log.isDebugEnabled())
					//log.debug(String.format(">>> ResultMessage: %s", result.toString()));
			} catch (Exception e) {
				log.error(e);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 
	 * @param messageName
	 * @param sourceSubject
	 * @param targetSubject
	 * @param transactionId
	 * @param queryId
	 * @param version
	 * @param condElement
	 * @param bindElement
	 * @return
	 * @throws Exception
	 */
	public String getQueryResult(String messageName,
					            String sourceSubject,
					            String targetSubject,
					            String transactionId,
					            String queryId, String version,
					            Element condElement, Element bindElement) 
					throws Exception
	{
		if(log.isInfoEnabled())
			log.info(String.format("GetQueryResult [%s %s] prepairing.....", queryId, version));
	
		//String usrSql = "SELECT queryString FROM CT_CustomQuery WHERE queryId = ? and version = ? ";
		String usrSql = "SELECT queryStringClob FROM CT_CustomQuery WHERE queryId = ? and version = ? ";
	
		String[] args = new String[] { queryId, version };
	
		List resultList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(usrSql, args);
	
		if (resultList.size() == 0)
			throw new CustomException("SYS-0012", queryId, version);
	
		ListOrderedMap queryMap = (ListOrderedMap) resultList.get(0);
		//String dbSql = queryMap.get("queryString").toString();
		String dbSql = queryMap.get("queryStringClob").toString();
	
		resultList.clear();
	
		String result = "";
		
		//condition statement replacement
		//code is '{CODE}'
		if (condElement != null)
		{
			Map phraseMap = getBindMapByElement(condElement);
			
			if (phraseMap != null)
			{
				for (Object keyValue : phraseMap.keySet())
				{
					String phraseString = (String) phraseMap.get(keyValue.toString());
					
					if (phraseString != null)
						dbSql = StringUtil.replace(dbSql,
													new StringBuilder().append("{").append(keyValue.toString()).append("}").toString(),
													phraseString);
				}
				
				log.info("Dynamic SQL conversion complete");
			}
		}
		
		if(StringUtils.indexOf(dbSql, "?") != -1)
		{
			Object[] bind = getBindObjectByElement(bindElement);
	
			//if (bind != null){
			//	log.debug(">> SQL = [" + dbSql + "] : [" + bind.toString() + "]");
			//  log.info(">> BIND INFO : [" + bind.toString() + "]");
			//}
			//else
			//	log.debug(">> SQL = [" + dbSql + "] : []");
	
			SqlCursorItemReader reader = greenFrameServiceProxy.getSqlTemplate().queryByCursor(dbSql, bind);
			
			try
			{
				result = QueryServiceUtil.createXmlByList(reader, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
	
				//if (log.isDebugEnabled())
				//log.debug(String.format(">>> ResultMessage: %s", result.toString()));
			}
			catch (Exception e)
			{
				log.error(e);
			}
		}
		else 
		{
			try
			{
				Map<String, Object> bindMap = getBindMapByElement(bindElement);
				
				if (log.isInfoEnabled() && bindMap != null)
					log.info(">> BIND INFO : [" + bindMap.toString() + "]");
				
				if (log.isDebugEnabled())
					log.debug(String.format(">> SQL : [%s]", dbSql));
				
				List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(dbSql, bindMap);
				//result = new QueryServiceProxy().createXml(resultList, queryID);
				result = QueryServiceUtil.createXmlByList(sqlResult, messageName, sourceSubject, targetSubject, transactionId, queryId, version);
				//log.info("QueryID: " + queryId + ", Version: " + version + ", QueryResult: " + sqlResult.size());
				log.info(String.format("QueryID: %s, Version: %s, QueryResult: %d", queryId, version, sqlResult.size()));
	
				//if (log.isDebugEnabled())
				//log.debug(String.format(">>> ResultMessage: %s", result.toString()));
			}
			catch (Exception e)
			{
				log.error(e);
			}
		}
		
		//Update useFlag & lastUseTimeKey
		try
		{
			//2019.10.08 dmlee : Request By CIM (No Record Query Count) Start
			//Map<String, Object> bindMap = new HashMap<String, Object>();
			//bindMap.put("queryId", queryId);
			//bindMap.put("version", version);
			//bindMap.put("lastUseTimeKey", TimeUtils.getCurrentEventTimeKey());

			//usrSql = "UPDATE CT_CUSTOMQUERY SET USECOUNT = USECOUNT + 1, LASTUSETIMEKEY = :lastUseTimeKey WHERE QUERYID = :queryId AND VERSION = :version ";
			//greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(usrSql, bindMap);
			//2019.10.08 dmlee : Request By CIM (No Record Query Count) End
			
			//GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e) 
		{
			log.error(e);
		}
		
		return result;
	}
	/**
	 * 
	 * 
	 * @param messageName
	 * @param sourceSubject
	 * @param targetSubject
	 * @param transactionId
	 * @param queryId
	 * @param version
	 * @param condElement
	 * @param bindElement
	 * @return
	 * @throws Exception
	 */
	public String getRequestQueryResult(String messageName,
					            String sourceSubject,
					            String targetSubject,
					            String transactionId,
					            String queryId, String version,
					            Element condElement, Element bindElement) 
					throws Exception
	{
		if ( log.isInfoEnabled() )
		{
			log.info("MESSAGENAME = "   + messageName);
			log.info("SOURCESUBJECT = " + sourceSubject);
			log.info("TARGETSUBJECT = " + targetSubject);
			log.info("TRANSACTIONID = " + transactionId);
			log.info("QUERYID = "       + queryId);
			log.info("VERSION = "       + version);
		}

		String usrSql = "SELECT queryString,queryStringExtend FROM CT_FMB_CustomQuery WHERE queryId = ? and version = ? ";

		String[] args = new String[] { queryId, version };

		List resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(usrSql, args);

		if ( resultList.size() == 0 )
			throw new CustomException("CanNotFoundQueryID", queryId, version);

		ListOrderedMap queryMap = (ListOrderedMap) resultList.get(0);
		String dbSql = "";
		String dbSqlNormal = "";
		String dbSqlExtend = "";
		if(queryMap.get("queryString") != null)
		{
			dbSqlNormal = queryMap.get("queryString").toString();
		}
		if(queryMap.get("queryStringExtend") != null)
		{
			dbSqlExtend = queryMap.get("queryStringExtend").toString();
		}

		dbSql = dbSqlNormal + " " + dbSqlExtend ;

		//log.info(dbSql);

		resultList.clear();
		
		//2015-11-24 hykim
		//condition statement replacement
		//code is '{CODE}'
		if (condElement != null)
		{
			Map phraseMap = getBindMapByElement(condElement);
					
			if (phraseMap != null)
			{
				for (Object keyValue : phraseMap.keySet())
				{
					String phraseString = (String) phraseMap.get(keyValue.toString());
					String condString = "{"+keyValue.toString()+"}";
							
					if (phraseString != null)
					{
						if(dbSql.contains(condString))
							dbSql = dbSql.replace(condString, phraseString);
					}	
				}
						
				log.info("Dynamic SQL conversion complete");
			}
		}
		//2015-11-24 hykim
				
		
		String result = "";
		if ( StringUtils.indexOf(dbSql, "?") != -1 )
		{ 
			try 
			{ 
				Object[] bind = getBindObjectByElement(bindElement);
			
				if ( bind != null )
				{   
					
					String fullSql=QueryServiceUtil.fillParameter(dbSql, CommonUtil.ConverObjectToMap(bind));
						
					log.info(">> SQL = ["+fullSql+"] ["+bind.toString()+"]");
				}
				else
					log.info(">> SQL = [" + dbSql + "] : []");
			
				SqlCursorItemReader reader = greenFrameServiceProxy.getSqlTemplate().queryByCursor(dbSql, bind);	
			
			} 
			catch (Exception e) 
			{
				log.error(e);
			}
		} 
		else 
		{
			try 
			{
				Map bindMap = getBindMapByElement(bindElement);
				
				if ( bindMap != null )
				{
					
				    String 	fullSql=QueryServiceUtil.fillParameter(dbSql, bindMap);
					
					log.info(">> SQL = ["+fullSql+"  ] ["+  bindMap.toString() + "]");
				}
				else
					log.info(">> SQL = [" + dbSql + "] : []");
								
				List<Map<String, Object>> sqlResult = 
					greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(dbSql, bindMap);
				result = QueryServiceUtil.createXmlByListName(sqlResult, messageName, sourceSubject, targetSubject, transactionId, queryId, version); //new QueryServiceProxy().createXml(resultList, queryID);
			} 
			catch (Exception e) 
			{
				log.error(e);
				if(e instanceof CustomException){
					throw e ;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * to get ID according to Naming rule
	 * @author swcho
	 * @since 2013.09.23
	 * @param ruleName
	 * @param quantity
	 * @param nameRuleAttrElement
	 * @param doc
	 * @return
	 * @throws CustomException
	 */
	public Document getName(String ruleName, long quantity, Element nameRuleAttrElement, Document doc)
		throws CustomException
	{
		if (nameRuleAttrElement != null)
		{
			//name parameter is mandatory
			Map bindMap = getBindMapByElement(nameRuleAttrElement);
			
			List<String> lstName = CommonUtil.generateNameByNamingRule(ruleName, bindMap, quantity);
			
			//generate return message
			//XmlUtil.getChild(doc, SMessageUtil.Header_Tag, true).getChild(SMessageUtil.MessageName_Tag).setText("GetNameResultReply");
			
			Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
			//bodyElement.removeChild("BINDV");
			
			Element resultListElement = new Element("DATALIST");
			
			for (String name : lstName)
			{
				Element resultElement = new Element("DATA");
				{
					Element valueElement = new Element("NAMEVALUE");
					valueElement.setText(name);
					resultElement.addContent(valueElement);
				}
				
				resultListElement.addContent(resultElement);
			}
			bodyElement.addContent(resultListElement);
		}
		
		return doc;
	}

	/*
	* Name : getBindObjectByElement
	* Desc : This function is getBindObjectByElement
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	private Object[] getBindObjectByElement(Element bindElement){
		List<Object> returnBindObject = new ArrayList<Object>();
		if(bindElement == null)
			return null;
		List<Element> bindElementList = bindElement.getChildren();
		for(int i = 0;i < bindElementList.size(); i++){
			returnBindObject.add(bindElementList.get(i).getText());
		}		
		return returnBindObject.toArray();
	}

	/*
	* Name : getBindMapByElement
	* Desc : This function is getBindMapByElement
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
	private Map getBindMapByElement(Element bindElement){
		Map<String, String> returnBindMap = new HashMap<String, String>();
		if(bindElement == null)
			return null;
		List<Element> bindElementList = bindElement.getChildren();
		for(int i = 0;i < bindElementList.size(); i++){
			returnBindMap.put(bindElementList.get(i).getName(), bindElementList.get(i).getText());
		}
		return returnBindMap;
	}
}