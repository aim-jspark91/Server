package kr.co.aim.messolution.generic.util;

import java.io.StringReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.lob.LobHandler;

public class MessageHistoryUtil {
	private static Log log = LogFactory.getLog(MessageHistoryUtil.class);

	private LobHandler lobHandler = greenFrameServiceProxy.getLobHandler();

	/*
	 * Name : setMessageHistory Desc : This function is setMessageHistory Author
	 * : AIM Systems, Inc Date : 2011.01.19
	 */
	public void setMessageHistory(String serverName, Document xml) {
		String messageName = "";
		// String transactionId = "";
		// String replySubjectName = "";

		String lotName = "";
		String carrierName = "";
		String machineName = "";
		String portName = "";
		String productSpecName = "";
		String processOperationName = "";
		String eventUser = "";

		// watch out upon heap
		String fullMessage = JdomUtils.toString(xml);

		byte[] byteStr = fullMessage.getBytes();
		if (byteStr.length > 4000) {
			fullMessage = new String(byteStr, 0, 3900);
		}

		try {
			Element root = xml.getDocument().getRootElement();

			Element elementHeader = root.getChild(SMessageUtil.Header_Tag);
			Element elementBody = root.getChild(SMessageUtil.Body_Tag);
			if (elementHeader != null) {
				if (elementHeader.getChild(SMessageUtil.MessageName_Tag) != null) {
					messageName = elementHeader.getChild(
							SMessageUtil.MessageName_Tag).getText();
				}

				if (elementBody != null) {
					if (elementBody.getChild("LOTNAME") != null) {
						lotName = elementBody.getChild("LOTNAME").getText();
					}
					if (elementBody.getChild("CARRIERNAME") != null) {
						carrierName = elementBody.getChild("CARRIERNAME")
								.getText();
					}
					if (elementBody.getChild("MACHINENAME") != null) {
						machineName = elementBody.getChild("MACHINENAME")
								.getText();
					}
					if (elementBody.getChild("PORTNAME") != null) {
						portName = elementBody.getChild("PORTNAME").getText();
					}
					if (elementBody.getChild("PRODUCTSPECNAME") != null) {
						productSpecName = elementBody.getChild(
								"PRODUCTSPECNAME").getText();
					}
					if (elementBody.getChild("PROCESSOPERATIONNAME") != null) {
						processOperationName = elementBody.getChild(
								"PROCESSOPERATIONNAME").getText();
					}
					if (elementBody.getChild("EVENTUSER") != null) {
						eventUser = elementBody.getChild("EVENTUSER").getText();
					}
				}
			} else {
				return;
			}

			try {

				String sql = "SELECT RECEIVEFLAG, EXPLICITCOMMIT FROM CT_MESSAGEDEF WHERE SERVERNAME = ? AND MESSAGENAME = ?";
				String[] bindSet = new String[] { serverName, messageName };

				String[][] sqlResult = greenFrameServiceProxy.getSqlTemplate()
						.queryForStringArray(sql, bindSet);

				if (sqlResult.length == 0 || sqlResult[0][0].equals("Y")) {
					String expliciCommit = "";
					if (sqlResult.length > 0) {
						expliciCommit = sqlResult[0][1].toString();
					}

					sql = "INSERT INTO CT_MESSAGEHISTORY (SERVERNAME, MESSAGENAME, TIMEKEY, LOTNAME, CARRIERNAME, MACHINENAME, PORTNAME, PRODUCTSPECNAME, PROCESSOPERATIONNAME, EVENTUSER, FULLMESSAGE) "
							+ "VALUES ( ?, ?, TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'), ?, ?, ?, ?, ?, ?, ?, ?)";
					/*
					 * JdbcTemplate jdbcTemplate =
					 * greenFrameServiceProxy.getSqlTemplate().getJdbcTemplate();
					 * jdbcTemplate =
					 * greenFrameServiceProxy.getSqlTemplate().getJdbcTemplate();
					 * 
					 * Object[] objects = new Object[] {serverName, messageName,
					 * lotName, carrierName, machineName, portName,
					 * productSpecName, processOperationName, eventUser,
					 * fullMessage};
					 * 
					 * jdbcTemplate.update(sql, objects);
					 */
//					JdbcTemplate jdbcTemplate = greenFrameServiceProxy
//							.getSqlTemplate().getJdbcTemplate();
					
					JdbcTemplate jdbcTemplate = GenericServiceProxy.getSqlMesTemplate().getJdbcTemplate();
					
					SqlUpdate sqlUpdate = new SqlUpdate(
							jdbcTemplate.getDataSource(), sql);
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.CLOB));
					Object objFullMessage = new SqlLobValue(new StringReader(
							fullMessage), fullMessage.length(), lobHandler);
					sqlUpdate.compile();

					Object[] objects = new Object[] { serverName, messageName,
							lotName, carrierName, machineName, portName,
							productSpecName, processOperationName, eventUser,
							objFullMessage };

					sqlUpdate.update(objects);
					if (!expliciCommit.equals("N")) {
						jdbcTemplate.update("commit");
					}
				}
			} catch (Exception e) {
				// log.error("ERP Interface Error : " + e.getMessage());
				log.error("Error MessageHistory : " + e.getMessage());

			}
		} catch (Exception e) {

		}
	}
	
	/**
	 * message record
	 * need to prevent leak by much thread using with thread-pool
	 * @author swcho
	 * @since 2014.04.07
	 * @param xml
	 * @param elapse
	 * @param error
	 */
//	public void recordEventResult(Document xml, long elapse, Exception error) {
//		// whenever called, generate orphan as inner resource
//		if (getWorkFlag().equals(MHU_TASK_WATCHER) || getWorkFlag().equals(MHU_TASK_ALL))
//			new Watcher(xml, elapse, error).start();
//	}
	
	/**
	 * message record
	 * need to prevent leak by much thread using with thread-pool
	 * @author swcho
	 * @since 2014.04.07
	 * @param xml
	 * @param elapse
	 * @param error
	 */
	public void recordErrorMessageLog(Document xml, Exception error, String emptyFlag) {
		if (!getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_NONE)
				&& getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_ERROR))
			//new ErrorMessage(xml, error, emptyFlag).start();
			GenericServiceProxy.getDBLogWriter().writErrorMessageLog(xml, error, emptyFlag);
	}
	
	/**
	 * business logic result
	 * need to prevent leak by much thread using with thread-pool
	 * @author swcho
	 * @since 2014.04.07
	 * @param xml
	 */
	public void recordMessageLog(Document xml, String messageType) {
		// whenever called, generate orphan as inner resource
		// if watchLever contains "MSG", insert CT_MESSAGELOG
		if (!getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_NONE)
				&& getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_MESSAGE))
		{
			//new MessageLog(xml, messageType).start();
		    
		    /* 20190319, hhlee, add, */
            Document xmlclone = (Document)xml.clone();
			GenericServiceProxy.getDBLogWriter().writeMessageLog(xmlclone, messageType);
		}
		else// add by GJJ 如果不记录message Log 记server log 20204020
		{
			log.info(JdomUtils.toString(xml));
		}
	}
	
	public void recordTranscationLog(Document xml, long elapse, boolean result) {
		// whenever called, generate orphan as inner resource
		// if watchLever contains "TRX", insert CT_TRANSACTIONLOG
		if (!getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_NONE)
				&& getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_TRANSATION))
		{
			//new TransactionLog(xml, elapse, result).start();
			GenericServiceProxy.getDBLogWriter().writeTransaction(xml, elapse, result);
		}
	}

	class MessageLog extends Thread {
		private Document xml;
		private String messageType;

		public MessageLog(Document xml, String messageType) {
			super();

			this.xml = xml;
			this.messageType = messageType;
		}

		@Override
        public void run() {
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			try {
				Element root = xml.getDocument().getRootElement();

				// Set Variable
				String serverName = getServerName();
				String eventName = root.getChild("Header").getChildText("MESSAGENAME");
				String eventUser = root.getChild("Header").getChildText("EVENTUSER");
				String timeKey = TimeUtils.getCurrentEventTimeKey();
				String transactionId = root.getChild("Header").getChildText("TRANSACTIONID");
				String originalSourceSubjectName = SMessageUtil.getHeaderItemValue(xml, "ORIGINALSOURCESUBJECTNAME", false);
				String targetSubjectName = SMessageUtil.getHeaderItemValue(xml, "TARGETSUBJECTNAME", false);
				String fullMessage = JdomUtils.toString(xml);

				// Get DEFAULTFLAG
				StringBuilder sql = new StringBuilder();
				Map<String, String> bindMap = new HashMap<String, String>();

				try {
					sql.setLength(0);
					sql.append("INSERT INTO CT_MESSAGELOG(SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, ");
					sql.append("TRANSACTIONID, IP, ORIGINALSOURCESUBJECTNAME, TARGETSUBJECTNAME, MESSAGELOG, MESSAGETYPE) ");
					sql.append("VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, ");
					sql.append(":ip, :originalSourceSubjectName, :targetSubjectName, :messageLog, :messageType)");

					bindMap.clear();

					bindMap.put("serverName", new StringBuilder(serverName).append(getProcessSequence()).toString());
					bindMap.put("eventName", eventName);
					bindMap.put("eventUser", eventUser);
					bindMap.put("timeKey", timeKey);
					bindMap.put("transactionId", transactionId);
					bindMap.put("ip", CommonUtil.getIp());
					bindMap.put("originalSourceSubjectName", originalSourceSubjectName);
					bindMap.put("targetSubjectName", targetSubjectName);
					bindMap.put("messageLog", fullMessage);
					bindMap.put("messageType", messageType);

					//greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
				} catch (Exception e) {

				}
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception ex) {
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				log.error(ex);
			}
		}
	}

	class ErrorMessage extends Thread {
		private Document xml;
		private Exception error;
		private String emptyFlag;

		public ErrorMessage(Document xml, Exception error, String emptyFlag) {
			super();

			this.xml = xml;
			this.error = error;
			this.emptyFlag = emptyFlag;
		}

		@Override
        public void run() {
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(
					PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			try {
				String serverName = getServerName();
				String eventName = SMessageUtil.getHeaderItemValue(xml, "MESSAGENAME", false);
				String eventUser = SMessageUtil.getHeaderItemValue(xml, "EVENTUSER", false);
				String timeKey = TimeUtils.getCurrentEventTimeKey();
				String transactionId = SMessageUtil.getHeaderItemValue(xml, "TRANSACTIONID", false);
				
				// uncontrollable if not standardized form
				String errorMessage = StringUtils.EMPTY;
				if (error != null) {
					if (error instanceof CustomException)
						errorMessage = ((CustomException) error).errorDef
								.getLoc_errorMessage();
					else
						errorMessage = error.getMessage();
//						errorMessage = error.getCause().getMessage();
				}

				try {
					StringBuilder sql = new StringBuilder();
					Map<String, String> bindMap = new HashMap<String, String>();
					sql.setLength(0);
					sql.append("INSERT INTO CT_ERRORMESSAGELOG (SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, TRANSACTIONID, ");
					sql.append("IP, ERRORMESSAGE, EMPTYFLAG) ");
					sql.append("VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, :ip, :errorMessage, :emptyFlag) ");

					bindMap.clear();
					bindMap.put("serverName", new StringBuilder(serverName).append(getProcessSequence()).toString());
					bindMap.put("eventName", eventName);
					bindMap.put("eventUser", eventUser);
					bindMap.put("timeKey", timeKey);
					bindMap.put("transactionId", transactionId);
					bindMap.put("ip", CommonUtil.getIp());
					bindMap.put("errorMessage", errorMessage);
					bindMap.put("emptyFlag", emptyFlag);
					
					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
					//greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
				} 
				catch (Exception e) {

				}
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			} catch (Exception ex) {
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				log.error(ex);
			}
		}
	}
	
	class TransactionLog extends Thread {
		private Document xml;
		private long elapse;
		private boolean result;
		
		public TransactionLog(Document xml, long elapse, boolean result) {
			super();

			this.xml = xml;
			this.elapse = elapse;
			this.result = result;
		}

		@Override
        public void run() {
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			try {
				Element root = xml.getDocument().getRootElement();

				// Set Variable
				String serverName = getServerName();
				String eventName = root.getChild("Header").getChildText("MESSAGENAME");
				String eventUser = root.getChild("Header").getChildText("EVENTUSER");
				String timeKey = TimeUtils.getCurrentEventTimeKey();
				String transactionId = root.getChild("Header").getChildText("TRANSACTIONID");
				String elapsedTime = Long.toString(elapse);
				String resultValue = GenericServiceProxy.getConstantMap().FLAG_Y;
				if(!result)
				{
					resultValue = GenericServiceProxy.getConstantMap().FLAG_N;
				}

				// Get DEFAULTFLAG
				StringBuilder sql = new StringBuilder();
				Map<String, String> bindMap = new HashMap<String, String>();

				try {
					sql.setLength(0);
					sql.append("INSERT INTO CT_TRANSACTIONLOG(SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, TRANSACTIONID, IP, ELAPSEDTIME, RESULT) ");
					sql.append("VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, :ip, :elapsedTime, :result)");

					bindMap.clear();
					bindMap.put("serverName", new StringBuilder(serverName).append(getProcessSequence()).toString());
					bindMap.put("eventName", eventName);
					bindMap.put("eventUser", eventUser);
					bindMap.put("timeKey", timeKey);
					bindMap.put("transactionId", transactionId);
					bindMap.put("ip", CommonUtil.getIp());
					bindMap.put("elapsedTime", elapsedTime);
					bindMap.put("result", resultValue);

					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
					//greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
				} catch (Exception e) {

				}
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			} catch (Exception ex) {
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				log.error(ex);
			}
		}
	}
	
	/**
	 * get server process ID
	 * @author swcho
	 * @since 2014.04.07
	 * @return
	 */
	private String getServerName()
	{
		String serverName = "";
		
		try
		{
			if (System.getProperty("svr") != null)
				serverName = new StringBuilder().append(System.getProperty("svr")).toString();
		}
		catch (Exception ex)
		{
			log.warn("NO subject for process");
		}
		
		return serverName;
	}
	
	/**
	 * get server key
	 * @author swcho
	 * @since 2014.04.07
	 * @return
	 */
	private String getProcessSequence()
	{
		try
		{
			if (System.getProperty("Seq") != null)
				return System.getProperty("Seq");
		}
		catch (Exception ex)
		{
			log.warn("NO subject for process");
		}
		
		return "";
	}
	
	/**
	 * decide logger task
	 * @author swcho
	 * @since 2014.04.07
	 * @return
	 */
	private String getWorkFlag()
	{
		try
		{
			if (System.getProperty("watchLevel") != null)
				return System.getProperty("watchLevel");
		}
		catch (Exception ex)
		{
			
		}
		
		return "";
	}
	
	/**
	 * decide logger task
	 * @author xzquan
	 * @since 2015.09.06
	 * @return
	 */
	private List<String> getInsertMessageFlagList()
	{
		List<String> list = new ArrayList<String>();
		
		try
		{
			if (System.getProperty("watchLevel") != null)
			{
				String[] watchLevelArg = System.getProperty("watchLevel").split("\\/");
				
				for(String i : watchLevelArg)
				{
					list.add(i);
				}
			}
		}
		catch (Exception ex)
		{
			
		}
		
		return list;
	}
}