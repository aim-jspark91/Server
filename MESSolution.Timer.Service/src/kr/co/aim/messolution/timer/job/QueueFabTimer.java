package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class QueueFabTimer implements Job, InitializingBean, ApplicationContextAware
{	
	//Different Factory QTimer
	private static Log log = LogFactory.getLog(QueueFabTimer.class);
	
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	private static ApplicationContext applicationContext;
	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@Override
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
//		if (scheduleJobFactory.isRunningScheduleJob(this)) {
//			log.info("Previous " + this.getClass().getName() + " is still running, so this schedule job is terminated.");
//		} else {
//			log.info("Previous " + this.getClass().getName() + " is not running, so this schedule job is executed.");
//			scheduleJobFactory.startScheduleJob(this);
//			
//			try {
//	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//	        	//Max
//				checkWarningQTime();
//				checkInterlockQTime();
//				
//				//Min
//				checkResolveQTime();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
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
				
	        	//Max
				checkWarningQTime();
				checkInterlockQTime();
				
				//Min
				checkResolveQTime();
				
				log.info(String.format("Job[%s] END", this.getClass().getName()));
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
	
	private void checkWarningQTime() throws Exception 
	{
		ProductQueueTime qTimeData = null;
		
		String strSql = "SELECT PRODUCTNAME, " +
				"       FACTORYNAME, " +
				"       PROCESSFLOWNAME, " +
				"       PROCESSFLOWVERSION, " +
				"       PROCESSOPERATIONNAME, " +
				"       PROCESSOPERATIONVERSION, " +
				"       TOFACTORYNAME, " +
				"       TOPROCESSFLOWNAME, " +
				"       TOPROCESSFLOWVERSION, " +
				"       TOPROCESSOPERATIONNAME, " +
				"       TOPROCESSOPERATIONVERSION, " +
				"       QUEUETIMETYPE " +
				"  FROM CT_PRODUCTQUEUETIME " +
				" WHERE     FACTORYNAME = :FACTORYNAME " +
				"       AND (SELECT ENUMNAME " +
				"              FROM ENUMDEF " +
				"             WHERE     USAGE = 'ProductSpecType' " +
				"                   AND DESCRIPTION = PRODUCTSPECTYPE) <= " +
				"              TO_NUMBER ( (SYSDATE - ENTERTIME) * 24 * 60) " +
				"       AND QUEUETIMESTATE = :QUEUETIMESTATE " +
				"       AND QUEUETIMETYPE = 'Max' " +
				"       AND TOFACTORYNAME = :TOFACTORYNAME " ; 
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");
		bindMap.put("QUEUETIMESTATE", "Entered");
		bindMap.put("TOFACTORYNAME", "OLED");

		List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(qTimeList != null && qTimeList.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "SYS", "Timer", null, null);
			
			for(int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																												(String)qTimeList.get(i).get("FACTORYNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																												(String)qTimeList.get(i).get("TOFACTORYNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																												(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData != null)
				{
					MESProductServiceProxy.getProductServiceImpl().WarningQTime(eventInfo, qTimeData);
				}
			}
		}
	}
	
	private void checkInterlockQTime() throws Exception 
	{
		ProductQueueTime qTimeData = null;
		
		String strSql = "SELECT PRODUCTNAME,      " +
				"       FACTORYNAME,      " +
				"       PROCESSFLOWNAME,      " +
				"       PROCESSFLOWVERSION,      " +
				"       PROCESSOPERATIONNAME,      " +
				"       PROCESSOPERATIONVERSION,    " +
				"       TOFACTORYNAME,    " +
				"       TOPROCESSFLOWNAME,    " +
				"       TOPROCESSFLOWVERSION,    " +
				"       TOPROCESSOPERATIONNAME,    " +
				"       TOPROCESSOPERATIONVERSION,    " +
				"       QUEUETIMETYPE    " +
				"  FROM CT_PRODUCTQUEUETIME      " +
				" WHERE     FACTORYNAME = :FACTORYNAME      " +
				"       AND (SELECT ENUMNAME " +
				"              FROM ENUMDEF " +
				"             WHERE     USAGE = 'ProductSpecType' " +
				"                   AND DESCRIPTION = PRODUCTSPECTYPE) <= " +
				"              TO_NUMBER ( (SYSDATE - ENTERTIME) * 24 * 60) " +
				"       AND QUEUETIMESTATE = :QUEUETIMESTATE      " +
				"       AND QUEUETIMETYPE = 'Max'     " +
				"       AND TOFACTORYNAME = :TOFACTORYNAME "; 
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");
		bindMap.put("QUEUETIMESTATE", "Warning");
		bindMap.put("TOFACTORYNAME", "OLED");

		List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(qTimeList != null && qTimeList.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "SYS", "Timer", null, null);
			
			for(int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																												(String)qTimeList.get(i).get("FACTORYNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																												(String)qTimeList.get(i).get("TOFACTORYNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																												(String)qTimeList.get(i).get("QUEUETIMETYPE")});
								
				if(qTimeData != null)
				{
					MESProductServiceProxy.getProductServiceImpl().InterlockQTime(eventInfo, qTimeData);
				}
			}
		}
	}
	
	private void checkResolveQTime() throws Exception 
	{
		ProductQueueTime qTimeData = null;
		
		String strSql = "SELECT PRODUCTNAME,      " +
				"       FACTORYNAME,      " +
				"       PROCESSFLOWNAME,      " +
				"       PROCESSFLOWVERSION,      " +
				"       PROCESSOPERATIONNAME,      " +
				"       PROCESSOPERATIONVERSION,      " +
				"       TOFACTORYNAME,    " +
				"       TOPROCESSFLOWNAME,    " +
				"       TOPROCESSFLOWVERSION,    " +
				"       TOPROCESSOPERATIONNAME,    " +
				"       TOPROCESSOPERATIONVERSION,     " +
				"       QUEUETIMETYPE     " +
				"  FROM CT_PRODUCTQUEUETIME      " +
				" WHERE     FACTORYNAME = :FACTORYNAME        " +
				"       AND (SELECT ENUMNAME " +
				"              FROM ENUMDEF " +
				"             WHERE     USAGE = 'ProductSpecType' " +
				"                   AND DESCRIPTION = PRODUCTSPECTYPE) <= " +
				"              TO_NUMBER ( (SYSDATE - ENTERTIME) * 24 * 60) " +
				"       AND QUEUETIMESTATE = :QUEUETIMESTATE      " +
				"       AND QUEUETIMETYPE = 'Min'  " +
				"       AND TOFACTORYNAME = :TOFACTORYNAME "; 
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");
		bindMap.put("QUEUETIMESTATE", "Interlocked");
		bindMap.put("TOFACTORYNAME", "OLED");

		List<Map<String, Object>> qTimeList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(qTimeList != null && qTimeList.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "SYS", "Timer", null, null);
			
			for(int i = 0; i < qTimeList.size(); i++)
			{
				qTimeData = ExtendedObjectProxy.getProductQueueTimeService().selectByKey(false, new Object[] {(String)qTimeList.get(i).get("PRODUCTNAME"), 
																												(String)qTimeList.get(i).get("FACTORYNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWNAME"), 
																												(String)qTimeList.get(i).get("PROCESSFLOWVERSION"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)qTimeList.get(i).get("PROCESSOPERATIONVERSION"), 
																												(String)qTimeList.get(i).get("TOFACTORYNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSFLOWVERSION"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONNAME"),
																												(String)qTimeList.get(i).get("TOPROCESSOPERATIONVERSION"),
																												(String)qTimeList.get(i).get("QUEUETIMETYPE")});
				
				if(qTimeData != null)
				{
					MESProductServiceProxy.getProductServiceImpl().ResolvedQTime(eventInfo, qTimeData);
				}
			}
		}
	}
}
