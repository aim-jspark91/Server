package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
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

public class DurableDryTimer implements Job, InitializingBean, ApplicationContextAware
{	
	//Equal Factory QTimer
	private static Log log = LogFactory.getLog(DurableDryTimer.class);
	
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
//				log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//				checkDryDurable();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
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
				
				checkDryDurable();
				
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
	
	private void checkDryDurable() throws Exception 
	{
		String strSql = 
						" SELECT * FROM ( " +
						" SELECT A.DURABLENAME ,CASE WHEN ROUND((SYSDATE - A.LASTCLEANTIME) * 24 *60,2)  > B.DRYWAITTIME     " +
						"        THEN 'DRY'      " +
						" ELSE 'WET'  END FLAG, SYSDATE, A.LASTCLEANTIME, A.DURABLESPECNAME "+
						" FROM DURABLE A "+ 
						" INNER JOIN DURABLESPEC B ON A.DURABLESPECNAME = B.DURABLESPECNAME "+
						" WHERE 1=1 "+
						//	MODIFY 2019.01.23	aim.yunjm	OLED에 있는 GlassCST는 조회되지 않음
						// * Cleanstate = 'Dirty', DryFlag = 'N' 인 CST는  DryFlag가 변경되지 않기 때문에 반송 불가
						" AND B.FACTORYNAME = :FACTORYNAME " +
						" AND A.DRYFLAG = 'N' " + 
						" AND B.DRYWAITTIME IS NOT NULL  ) WHERE FLAG ='DRY' "; 
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");

		List<Map<String, Object>> DurableList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(DurableList != null && DurableList.size() > 0)
		{
			String DurableName =  "";
			
			for(int i = 0; i < DurableList.size(); i++)
			{
				DurableName =(String)DurableList.get(i).get("DURABLENAME");
				changeDry(DurableName);
			}
		}
	}
	
	public void changeDry(String carrierName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDryFlag", "Timer", "Timer", null, null);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> udfs = durableData.getUdfs();
//		udfs.put("DRYFLAG", "Y");
//		
//		SetEventInfo setEventInfo = new SetEventInfo();
//		setEventInfo.setUdfs(udfs);
		
		SetEventInfo setEventInfo = new SetEventInfo();
	    setEventInfo.getUdfs().put("DRYFLAG", "Y");
	    
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
	}
}