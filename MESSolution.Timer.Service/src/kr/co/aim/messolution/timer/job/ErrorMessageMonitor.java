package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ErrorMessageMonitor implements Job, InitializingBean, ApplicationContextAware {
	
	private static Log log = LogFactory.getLog(ErrorMessageMonitor.class);
	
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	private static ApplicationContext applicationContext;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@Override
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
//		if (scheduleJobFactory.isRunningScheduleJob(this)) {
//			log.info("Previous " + this.getClass().getName() + " is still running, so this schedule job is terminated.");
//		} else {
//			log.info("Previous " + this.getClass().getName() + " is not running, so this schedule job is executed.");
//			scheduleJobFactory.startScheduleJob(this);
//
//			try {
//				log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//				monitorErrorMessageLog();
//
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//					log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				log.error(e);
//			}
//			
//			// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//			scheduleJobFactory.endScheduleJob(this);
//		}
		// Modified by smkang on 2019.04.24 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
    	//									  For avoid duplication of schedule job, running information should be recorded.
		try {
			ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
			ScheduleJob scheduleJob = scheduleJobFactory.startScheduleJob(arg0);

			try {
				log.info(String.format("Job[%s] START", this.getClass().getName()));
				
				// 2019.05.07_hsryu_after ReStart, if FIrst Job, not execute. 
				if(arg0.getPreviousFireTime() != null){
					monitorErrorMessageLog(scheduleJob);
					
					//MODIFY BY NSKIM START ON20190907 MANTIS:4845
					//monitorTransactionElapsed(scheduleJob);
					//MODIFY BY NSKIM END ON20190907 MANTIS:4845
					log.info(String.format("Job[%s] END", this.getClass().getName()));
				}
				else{
					log.info(String.format("Job[%s] PASS", this.getClass().getName()));
				}
			} catch (CustomException e) {
				if (log.isDebugEnabled())
					log.error(e.errorDef.getLoc_errorMessage());
				
				log.error(String.format("Job[%s] TERMINATE", this.getClass().getName()));
			} catch (Exception e) {
				log.error(e);
				log.error(String.format("Job[%s] TERMINATE", this.getClass().getName()));
			}
			
			scheduleJobFactory.endScheduleJob(scheduleJob);
		} catch (Exception e) {
			log.info(e);
			log.info(String.format("Job[%s] PASS", this.getClass().getName()));
		}
	}
	
	/**
	 * real-time CSTDurationUsed time monitor
	 * @author hwlee89
	 * @since 2016.03.18
	 * @throws CustomException
	 */
	public void monitorErrorMessageLog(ScheduleJob job) throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT TO_CHAR(SYSDATE-1/24/60, :DATEFORMAT) BEFOREONEMIN, \n");
		sqlBuffer.append("       SUBSTR(A.TIMEKEY,0,14) ERRORTIME, \n");
		sqlBuffer.append("       TO_CHAR(SYSDATE, :DATEFORMAT) CURRENTTIME, \n");
		sqlBuffer.append("       A.SERVERNAME, \n");
		sqlBuffer.append("       A.EVENTNAME, \n");
		sqlBuffer.append("       A.EVENTUSER, \n");
		sqlBuffer.append("       A.TIMEKEY, \n");
		sqlBuffer.append("       A.TRANSACTIONID, \n");
		sqlBuffer.append("       A.IP, \n");
		sqlBuffer.append("       A.ERRORMESSAGE, \n");
		sqlBuffer.append("       A.EMPTYFLAG \n");
		sqlBuffer.append("  FROM CT_ERRORMESSAGELOG A \n");
		// 2019.05.07_hsryu_Change Sysdate -> FireTime.
		//sqlBuffer.append(" WHERE TO_CHAR(SYSDATE-1/24/60, :DATEFORMAT) <= SUBSTR(TIMEKEY,0,14) \n");
		//sqlBuffer.append("   AND SUBSTR(TIMEKEY,0,14) < TO_CHAR(SYSDATE, :DATEFORMAT) \n");
		//2019.09.29 nskim modified inequality sign to BETWEEN [A] AND [B]
//		sqlBuffer.append(" WHERE TO_CHAR(:PREFILETIME, :DATEFORMAT) <= SUBSTR(TIMEKEY,0,14) \n");
//		sqlBuffer.append("   AND SUBSTR(TIMEKEY,0,14) < TO_CHAR(:CURRENTFILETIME, :DATEFORMAT) \n");
		sqlBuffer.append(" WHERE A.TIMEKEY BETWEEN TO_CHAR(:PREFILETIME, :DATEFORMAT) AND  TO_CHAR(:CURRENTFILETIME, :DATEFORMAT)\n");
		sqlBuffer.append(" ORDER BY TIMEKEY DESC");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DATEFORMAT", "YYYYMMDDHH24MISS");
		// 2019.05.07_hsryu_Insert Argument.
		bindMap.put("PREFILETIME", new Timestamp(job.getPreviousFireTime().getTime()));
		bindMap.put("CURRENTFILETIME", new Timestamp(job.getCurrentFireTime().getTime()));

		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);

		if(result != null && result.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ErrorMessageAlarm", "SYS", "", "", "");
			
			for (ListOrderedMap row : result)
			{
				String eventName = CommonUtil.getValue(row, "EVENTNAME");

				if(this.existErrorMessageInAlarmActionDef(eventName))
				{
					String serverName = CommonUtil.getValue(row, "SERVERNAME");
					String eventUser = CommonUtil.getValue(row, "EVENTUSER");
					String timeKey = CommonUtil.getValue(row, "TIMEKEY");
					String transactionID = CommonUtil.getValue(row, "TRANSACTIONID");
					String ip = CommonUtil.getValue(row, "IP");
					String errorMessage = CommonUtil.getValue(row, "ERRORMESSAGE");
					String emptyFlag = CommonUtil.getValue(row, "EMPTYFLAG");

					this.sendEmail(eventInfo, serverName, eventName, eventUser, timeKey, transactionID, ip, errorMessage, emptyFlag);
				}
			}
		}
	}
	
	public void monitorTransactionElapsed(ScheduleJob job) throws CustomException
	{
		log.info("monitorTransactionElapsed Start");
		
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT TO_CHAR(SYSDATE-2/24/60, :DATEFORMAT) BEFOREONEMIN, \n");
		sqlBuffer.append("       SUBSTR(A.TIMEKEY,0,14) ERRORTIME, \n");
		sqlBuffer.append("       TO_CHAR(SYSDATE, :DATEFORMAT) CURRENTTIME, \n");
		sqlBuffer.append("       A.SERVERNAME, \n");
		sqlBuffer.append("       A.EVENTNAME, \n");
		sqlBuffer.append("       A.EVENTUSER, \n");
		sqlBuffer.append("       A.TIMEKEY, \n");
		sqlBuffer.append("       A.TRANSACTIONID, \n");
		sqlBuffer.append("       A.IP, \n");
		sqlBuffer.append("       A.ELAPSEDTIME \n");
		sqlBuffer.append("  FROM CT_TRANSACTIONLOG A \n");
		//2019.09.29 nskim modified inequality sign to BETWEEN [A] AND [B]
//		sqlBuffer.append(" WHERE TO_CHAR(:PREFILETIME, :DATEFORMAT) <= SUBSTR(TIMEKEY,0,14) \n");
//		sqlBuffer.append("   AND SUBSTR(TIMEKEY,0,14) < TO_CHAR(:CURRENTFILETIME, :DATEFORMAT) \n");
		sqlBuffer.append(" WHERE A.TIMEKEY BETWEEN TO_CHAR(:PREFILETIME, :DATEFORMAT) AND  TO_CHAR(:CURRENTFILETIME, :DATEFORMAT)\n");
		sqlBuffer.append("   AND A.SERVERNAME LIKE 'PEX%' \n");
		sqlBuffer.append("   AND A.ELAPSEDTIME >= 60*10*1000 \n");
		sqlBuffer.append(" ORDER BY TIMEKEY DESC");
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DATEFORMAT", "YYYYMMDDHH24MISS");
		bindMap.put("PREFILETIME", new Timestamp(job.getPreviousFireTime().getTime()));
		bindMap.put("CURRENTFILETIME", new Timestamp(job.getCurrentFireTime().getTime()));

		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
		
		log.info(String.format("result count = [%d]",result.size()));
		
		if(result != null && result.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ErrorMessageAlarm", "SYS", "", "", "");
			
			for (ListOrderedMap row : result)
			{
				String eventName = CommonUtil.getValue(row, "EVENTNAME");

				if(this.existErrorMessageInAlarmActionDef())
				{			
					String serverName = CommonUtil.getValue(row, "SERVERNAME");
					String eventUser = CommonUtil.getValue(row, "EVENTUSER");
					String timeKey = CommonUtil.getValue(row, "TIMEKEY");
					String transactionID = CommonUtil.getValue(row, "TRANSACTIONID");
					String ip = CommonUtil.getValue(row, "IP");
					String elapsedTime = CommonUtil.getValue(row, "ELAPSEDTIME");
					
					this.sendEmail_Elapsed(eventInfo,eventName, serverName, eventUser, timeKey, transactionID, ip, elapsedTime);
					log.info("sendEmail_Elapsed Success");
				}
			}
		}
	}
	
	
	public void sendEmail(EventInfo eventInfo, String serverName, String eventName, String eventUser, String timeKey, String transactionID, String ip, String errorMessage, String emptyFlag)
	{
    	try
    	{
    		Document doc = new Document();
    		doc = SMessageUtil.createXmlDocument("ErrorMessageAlarm", "", "", eventInfo);

    		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
			
			Element element1 = new Element("ALARMCODE");
			element1.setText(eventName);
			bodyElement.addContent(element1);
			
			Element element2 = new Element("SERVERNAME");
			element2.setText(serverName);
			bodyElement.addContent(element2);

			Element element3 = new Element("EVENTNAME");
			element3.setText(eventName);
			bodyElement.addContent(element3);
			
			Element element4 = new Element("EVENTUSER");
			element4.setText(eventUser);
			bodyElement.addContent(element4);
			
			Element element5 = new Element("TIMEKEY");
			element5.setText(timeKey);
			bodyElement.addContent(element5);

			Element element6 = new Element("TRANSACTIONID");
			element6.setText(transactionID);
			bodyElement.addContent(element6);

			Element element7 = new Element("IP");
			element7.setText(ip);
			bodyElement.addContent(element7);

			Element element8 = new Element("ERRORMESSAGE");
			element8.setText(errorMessage);
			bodyElement.addContent(element8);

			Element element9 = new Element("EMPTYFLAG");
			element9.setText(emptyFlag);
			bodyElement.addContent(element9);
			
			GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
    	}
    	catch(Exception ex)
    	{
    		log.error(ex);
    		log.error(String.format("E-Mail Send Fail !"));
    	}
	}
	
	public boolean existErrorMessageInAlarmActionDef(String eventName)
	{
		boolean existFlag = false;
		
		AlarmDefinition alarmDef = null;
		
		try
		{
			alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {eventName});
		}
		catch(Throwable e)
		{
			alarmDef = null;
			return existFlag;
		}
		
		if(alarmDef != null)
		{
			AlarmActionDef alarmActionDef = null;
			
			try
			{
				alarmActionDef = ExtendedObjectProxy.getAlarmActionDefService().selectByKey(false, new Object[] {eventName , "Email"});
			}
			catch(Throwable e)
			{
				alarmActionDef = null;
				return existFlag;
			}
			
			if(alarmActionDef != null)
			{
				existFlag = true;
			}
		}
		
		return existFlag;
	}
	
	
	public boolean existErrorMessageInAlarmActionDef()
	{
		boolean existFlag = false;
		
		AlarmDefinition alarmDef = null;
		
		try
		{
			alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {"ElapsedTimeOver"});
		}
		catch(Throwable e)
		{
			alarmDef = null;
			return existFlag;
		}
		
		if(alarmDef != null)
		{
			AlarmActionDef alarmActionDef = null;
			
			try
			{
				alarmActionDef = ExtendedObjectProxy.getAlarmActionDefService().selectByKey(false, new Object[] {"ElapsedTimeOver" , "Email"});
			}
			catch(Throwable e)
			{
				alarmActionDef = null;
				return existFlag;
			}
			
			if(alarmActionDef != null)
			{
				existFlag = true;
			}
		}
		
		return existFlag;
	}
	
	
	public void sendEmail_Elapsed(EventInfo eventInfo,String eventName, String serverName, String eventUser, String timeKey, String transactionID, String ip, String elapsedTime)
	{
    	try
    	{
    		Document doc = new Document();
    		doc = SMessageUtil.createXmlDocument("TransactionElapsedAlarm", "", "", eventInfo);

    		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
    		

			
			Element element1 = new Element("ALARMCODE");
			element1.setText("ElapsedTimeOver");
			bodyElement.addContent(element1);
			
			Element element2 = new Element("SERVERNAME");
			element2.setText(serverName);
			bodyElement.addContent(element2);

			Element element3 = new Element("EVENTNAME");
			element3.setText(eventName);
			bodyElement.addContent(element3);
			
			Element element4 = new Element("EVENTUSER");
			element4.setText(eventUser);
			bodyElement.addContent(element4);
			
			Element element5 = new Element("TIMEKEY");
			element5.setText(timeKey);
			bodyElement.addContent(element5);

			Element element6 = new Element("TRANSACTIONID");
			element6.setText(transactionID);
			bodyElement.addContent(element6);

			Element element7 = new Element("IP");
			element7.setText(ip);
			bodyElement.addContent(element7);

			Element element8 = new Element("ELAPSEDTIME");
			element8.setText(elapsedTime);
			bodyElement.addContent(element8);
			
			GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
    	}
    	catch(Exception ex)
    	{
    		log.error(ex);
    		log.error(String.format("E-Mail Send Fail !"));
    	}
	}
}