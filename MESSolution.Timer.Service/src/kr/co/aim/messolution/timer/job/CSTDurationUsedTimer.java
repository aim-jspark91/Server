package kr.co.aim.messolution.timer.job;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
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

public class CSTDurationUsedTimer implements Job, InitializingBean, ApplicationContextAware
{
	private static Log log = LogFactory.getLog(CSTDurationUsedTimer.class);
	
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
	
	/*
	* Name : execute
	* Desc : This function is execute
	* Author : AIM Systems, Inc
	* Date : 2011.01.03
	*/
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
//			try
//			{
//				log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//				monitorCSTDurationUsedTime();
//
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			}
//			catch (CustomException e)
//			{
//				if (log.isDebugEnabled())
//					log.error(e.errorDef.getLoc_errorMessage());
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
				
				monitorCSTDurationUsedTime();

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
	
	/**
	 * real-time CSTDurationUsed time monitor
	 * @author hwlee89
	 * @since 2016.03.18
	 * @throws CustomException
	 */
	public void monitorCSTDurationUsedTime() throws CustomException
	{
		//
		StringBuffer sqlBuffer = new StringBuffer()
								.append("SELECT D.DURABLENAME,                                                                                                       \n") 
								.append("       D.DURABLETYPE,																										 \n") 
								.append("       D.TIMEUSED,                                                                                                          \n") 
								.append("       DS.TIMEUSEDLIMIT,																									 \n") 
								.append("       (SYSDATE - DECODE(D.LASTCLEANTIME, NULL, D.CREATETIME, D.LASTCLEANTIME)) DURATIONUSED,								 \n") 
								.append("       DS.DURATIONUSEDLIMIT,                                                                                                 \n") 
								.append("       D.CAPACITY,                                                                                                          \n") 
								.append("       D.LOTQUANTITY,                                                                                                       \n") 
								.append("       D.FACTORYNAME,                                                                                                       \n") 
								.append("       D.DURABLESTATE,                                                                                                      \n") 
								.append("       D.DURABLECLEANSTATE,                                                                                                 \n") 
								.append("       D.LASTEVENTNAME,                                                                                                     \n") 
								.append("       D.LASTEVENTTIMEKEY,                                                                                                  \n") 
								.append("       D.LASTEVENTTIME,                                                                                                     \n") 
								.append("       D.LASTEVENTUSER,                                                                                                     \n") 
								.append("       D.LASTEVENTCOMMENT,                                                                                                  \n") 
								.append("       D.LASTEVENTFLAG,                                                                                                     \n") 
								.append("       D.CREATETIME,                                                                                                        \n") 
								.append("       D.CREATEUSER,                                                                                                        \n") 
								.append("       D.DURABLEHOLDSTATE,                                                                                                  \n") 
								.append("       D.TRANSPORTLOCKFLAG,                                                                                                 \n") 
								.append("       D.MACHINENAME,                                                                                                       \n") 
								.append("       D.PORTNAME,                                                                                                          \n") 
								.append("       D.LASTCLEANTIME,                                                                                                     \n") 
								.append("       CASE                                                                                                                 \n") 
								.append("          WHEN (SYSDATE - DECODE(D.LASTCLEANTIME, NULL, D.CREATETIME, D.LASTCLEANTIME)) > DS.DURATIONUSEDLIMIT			 	 \n") 
								.append("          THEN ?                                                                                                     \n")
								// 2019.05.08_hsryu_Delete Logic. when increment TimeUsed, Compare TimeUsedLimit to TimeUsed and Change 'Dirty'. Mantis 0003800. 
								//.append("          WHEN D.TIMEUSED > DS.TIMEUSEDLIMIT 																					 \n") 
								//.append("          THEN ?                                                                                                      \n")
								.append("          ELSE ?                                                                                                      \n") 
								.append("       END AS DIFFDURATIONUSED                                                                                              \n") 
								.append("  FROM DURABLE D ,DURABLESPEC DS                                                                                                           \n") 
								.append(" WHERE     1 = 1                                                                                                            \n")
								.append("       AND D.DURABLETYPE = ? AND D.DURABLESPECNAME = DS.DURABLESPECNAME		\n")
								.append("       AND D.DURABLESTATE IN (?,?)                                                                        \n")
								.append("       AND D.DURABLECLEANSTATE = ?                                                                                   \n")
								.append("       AND D.OWNER = ?                                                                                   \n");

		Object[] bindArray = new Object[] {GenericServiceProxy.getConstantMap().Dur_Dirty,
				// 2019.05.08_hsryu_Delete Logic. Mantis 0003800. 
				//GenericServiceProxy.getConstantMap().Dur_Dirty ,
				GenericServiceProxy.getConstantMap().Dur_Clean,
				"GlassCST",
				GenericServiceProxy.getConstantMap().Dur_Available,
				GenericServiceProxy.getConstantMap().Dur_InUse,
				GenericServiceProxy.getConstantMap().Dur_Clean,
				GenericServiceProxy.getConstantMap().DEFAULT_FACTORY};
		
		List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindArray);

		if(result != null && result.size() > 0)
		{
			for (ListOrderedMap row : result)
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Dirty", "SYS", "CarrierDurationUsedTime&TimeUsed Monitoring System", null, null);

				String carrierName = CommonUtil.getValue(row, "DURABLENAME");
				String diffDurationUsed = CommonUtil.getValue(row, "DIFFDURATIONUSED");

				if(diffDurationUsed.equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
				{
					changeDirty(carrierName, eventInfo);
				}
			}
		}
	}
	
	/**
	 * Change CarrierCleanState Dirty
	 * @author hwlee89
	 * @since 2016.03.18
	 * @throws CustomException
	 */
	public void changeDirty(String carrierName, EventInfo eventInfo) throws CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		if(durableData.getDurableCleanState().equals(GenericServiceProxy.getConstantMap().Dur_Clean))
		{
			//Dirty
			DirtyInfo dirtyInfo = new DirtyInfo();
			dirtyInfo.setUdfs(durableData.getUdfs());
			
			MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
			
			// For synchronization of a carrier information, common method will be invoked.
            try {
				Element bodyElement = new Element(SMessageUtil.Body_Tag);
				bodyElement.addContent(new Element("DURABLENAME").setText(durableData.getKey().getDurableName()));
				bodyElement.addContent(new Element("DURABLECLEANSTATE").setText(GenericServiceProxy.getConstantMap().Dur_Dirty));
				
				// EventName will be recorded triggered EventName.
				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
				
				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, durableData.getKey().getDurableName());
            } catch (CustomException e) {
				if (log.isDebugEnabled())
	                log.error(e.errorDef.getLoc_errorMessage());
			} catch (Exception e1) {
            	log.warn(e1);
            }
		}
	}
}
