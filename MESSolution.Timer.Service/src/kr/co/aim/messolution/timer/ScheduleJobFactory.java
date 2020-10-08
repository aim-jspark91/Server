package kr.co.aim.messolution.timer;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

/**
 * @author smkang
 * @since 2019.04.08
 * @see For avoid duplication of schedule job.
 */
public class ScheduleJobFactory {
	// Modified by smkang on 2019.04.23 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
	//									  For avoid duplication of schedule job, running information should be recorded.
//	private Map<String, Job> scheduleJobFactory = new ConcurrentHashMap<String, Job>();
//	
//	public void startScheduleJob(Job job) {
//		scheduleJobFactory.put(job.getClass().getName(), job);
//	}
//	
//	public void endScheduleJob(Job job) {
//		if (isRunningScheduleJob(job))
//			scheduleJobFactory.remove(job.getClass().getName());
//	}
//	
//	public boolean isRunningScheduleJob(Job job) {
//		return scheduleJobFactory.containsKey(job.getClass().getName());
//	}
	private static Log log = LogFactory.getLog(ScheduleJobFactory.class);
	
	// Modified by smkang on 2019.07.05 - Management scenario is changed.
//	private boolean isRunningPreviousScheduleJob(ScheduleJob scheduleJob, Timestamp startDatabaseTime) {
//		return (scheduleJob.getJobEndTime() != null && scheduleJob.getJobEndTime().after(startDatabaseTime));
//	}
	private boolean isRunningPreviousScheduleJob(ScheduleJob scheduleJob) {
		return StringUtils.equals(scheduleJob.getIsRunningJob(), "Y"); 
	}
	
	private boolean isNotComeToNextFireTime(ScheduleJob scheduleJob, Timestamp startDatabaseTime) {
		return (scheduleJob.getNextFireTime() != null && scheduleJob.getNextFireTime().after(startDatabaseTime));
	}
	
	public ScheduleJob startScheduleJob(JobExecutionContext job) throws Exception {
		// Modified by smkang on 2019.06.25 - Defense Code.
//		// Added by smkang on 2019.04.28 - Transaction Management.
//		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
//				
//		log.debug("JobName = " + job.getJobDetail().getName() + " begins schedule job.");
//		
//		Timestamp startDatabaseTime = getCurrentDatabaseTime();
//		Timestamp startLocalTime = TimeStampUtil.getCurrentTimestamp();
//		
//		long timeDiff = getTimeDiff(startDatabaseTime, startLocalTime);
//		log.debug("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", StartLocalTime = " + startLocalTime.toString() + ", TimeDiff = " + timeDiff);
//		
//		ScheduleJob scheduleJob = null;
//		try {
//			scheduleJob = ExtendedObjectProxy.getScheduleJobService().selectByKey(true, new Object[] {job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName()});
//		} catch (greenFrameDBErrorSignal e) {
//			scheduleJob = new ScheduleJob(job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName());
//			scheduleJob = setRunningInformation(scheduleJob, job, startDatabaseTime, timeDiff);
//			
//			ExtendedObjectProxy.getScheduleJobService().insert(scheduleJob);
//			
//			return ExtendedObjectProxy.getScheduleJobService().selectByKey(true, new Object[] {job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName()});
//		}
//		
//		try {
//			if (isRunningPreviousScheduleJob(scheduleJob, startDatabaseTime)) {
//				if (scheduleJob.getJobEndTime() != null)
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", PreviousJobEndTime = " + scheduleJob.getJobEndTime().toString());
//				else
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", PreviousJobEndTime = null");
//			} else if (isNotComeToNextFireTime(scheduleJob, startDatabaseTime)) {
//				if (scheduleJob.getNextFireTime() != null)
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", NextJobFireTime = " + scheduleJob.getNextFireTime().toString());
//				else
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", NextJobFireTime = null");
//			}
//		} catch (Exception e) {
//			// Added by smkang on 2019.04.28 - Transaction Management.
//			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
//			
//			throw e;
//		}
//
//		return setRunningInformation(scheduleJob, job, startDatabaseTime, timeDiff);
		// Added by smkang on 2019.04.28 - Transaction Management.
		GenericServiceProxy.getTxDataSourceManager().beginTransaction();
		
		try {
			log.debug("JobName = " + job.getJobDetail().getName() + " begins schedule job.");
			
			Timestamp startDatabaseTime = getCurrentDatabaseTime();
			Timestamp startLocalTime = TimeStampUtil.getCurrentTimestamp();
			
			long timeDiff = getTimeDiff(startDatabaseTime, startLocalTime);
			log.debug("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", StartLocalTime = " + startLocalTime.toString() + ", TimeDiff = " + timeDiff);
			
			ScheduleJob scheduleJob = null;
			Timestamp oldCurFireTime = null;
			try {
				scheduleJob = ExtendedObjectProxy.getScheduleJobService().selectByKey(true, new Object[] {job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName()});
				
				oldCurFireTime = scheduleJob.getCurrentFireTime();
			} catch (greenFrameDBErrorSignal e) {
				scheduleJob = new ScheduleJob(job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName());
				scheduleJob = setRunningInformation(scheduleJob, job, startDatabaseTime, timeDiff);
				
				ExtendedObjectProxy.getScheduleJobService().insert(scheduleJob);
				
				// --------------------------------------------------------------------------------
				// Modified by smkang on 2019.07.05 - Management scenario is changed.
//				return ExtendedObjectProxy.getScheduleJobService().selectByKey(true, new Object[] {job.getJobDetail().getName(), job.getJobDetail().getJobClass().getName()});
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				GenericServiceProxy.getTxDataSourceManager().beginTransaction();
				
				return scheduleJob;
				// --------------------------------------------------------------------------------
			}

			// Modified by smkang on 2019.07.05 - Management scenario is changed.
//			if (isRunningPreviousScheduleJob(scheduleJob, startDatabaseTime)) {
//				if (scheduleJob.getJobEndTime() != null)
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", PreviousJobEndTime = " + scheduleJob.getJobEndTime().toString());
//				else
//					throw new Exception("JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", PreviousJobEndTime = null");
			log.debug("Validation is started : " + scheduleJob.toString());
			if (isRunningPreviousScheduleJob(scheduleJob)) {
				// Added by smkang on 2019.07.05 - Management scenario is changed.
				GenericServiceProxy.getTxDataSourceManager().commitAllTransactions();
				
				Timestamp now = new Timestamp(job.getScheduledFireTime().getTime() + timeDiff);
				
				Date nowTime = new Date(now.getTime());
				Date curTime = new Date(oldCurFireTime.getTime());
				long IdleTime = 10;
				
				//2019.08.28 dmlee : After 10min ISRUNNINGJOB Reset
				if(oldCurFireTime != null && ((nowTime.getTime() - curTime.getTime()) / (60 * 1000) > IdleTime))
				{
					log.info("ISRUNNINGJOB is 'Y' after 10min... No Validate ISRUNNINGJOB... ");
				}
				else
				{
					if (StringUtils.isNotEmpty(scheduleJob.getIsRunningJob()))
						throw new Exception("Previous job is still running. JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", IsRunningJob = " + scheduleJob.getIsRunningJob());
					else
						throw new Exception("Previous job is still running. JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", IsRunningJob = null");
				}
			} else if (isNotComeToNextFireTime(scheduleJob, startDatabaseTime)) {
				// Added by smkang on 2019.07.05 - Management scenario is changed.
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
				

				if (scheduleJob.getNextFireTime() != null)
					throw new Exception("NextFireTime is not reached yet. JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", NextJobFireTime = " + scheduleJob.getNextFireTime().toString());
				else
					throw new Exception("NextFireTime is not reached yet. JobName = " + job.getJobDetail().getName() + ", StartDatabaseTime = " + startDatabaseTime.toString() + ", NextJobFireTime = null");
			
				
			}
			
			// --------------------------------------------------------------------------------
			// Modified by smkang on 2019.07.05 - Management scenario is changed.
//			return setRunningInformation(scheduleJob, job, startDatabaseTime, timeDiff);
			scheduleJob = setRunningInformation(scheduleJob, job, startDatabaseTime, timeDiff);
			
			ExtendedObjectProxy.getScheduleJobService().update(scheduleJob);
			log.debug("Updated CT_SCHEDULEJOB : " + scheduleJob.toString());
			
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			
			return scheduleJob;
			// --------------------------------------------------------------------------------
		} catch (Exception e) {
			// Added by smkang on 2019.04.28 - Transaction Management.
			// Modified by smkang on 2019.07.05 - Management scenario is changed.
//			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			
			throw e;
		}
	}
	
	public void endScheduleJob(ScheduleJob scheduleJob) throws Exception {
		if (scheduleJob != null) {
			scheduleJob.setJobEndTime(getCurrentDatabaseTime());
			
			// Added by smkang on 2019.07.05 - Management scenario is changed.
			scheduleJob.setIsRunningJob("N");
			
			ExtendedObjectProxy.getScheduleJobService().update(scheduleJob);
			
			log.debug("JobName = " + scheduleJob.getJobName() + " ends schedule job.");
			
			// Added by smkang on 2019.04.28 - Transaction Management.
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
	}
	
	private ScheduleJob setRunningInformation(ScheduleJob scheduleJob, JobExecutionContext job, Timestamp startDatabaseTime, long timeDiff) {
		scheduleJob.setServerName(System.getProperty("svr") + System.getProperty("Seq"));
		scheduleJob.setJobStartTime(startDatabaseTime);
		
		if (job.getPreviousFireTime() != null)
			scheduleJob.setPreviousFireTime(new Timestamp(job.getPreviousFireTime().getTime() + timeDiff));
		
		if (job.getScheduledFireTime() != null)
			scheduleJob.setCurrentFireTime(new Timestamp(job.getScheduledFireTime().getTime() + timeDiff));
		
		if (job.getNextFireTime() != null)
			scheduleJob.setNextFireTime(new Timestamp(job.getNextFireTime().getTime() + timeDiff));
		
		// Added by smkang on 2019.07.05 - Management scenario is changed.
		scheduleJob.setIsRunningJob("Y");
		
		return scheduleJob;
	}
	
	private Timestamp getCurrentDatabaseTime() {
		List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList("SELECT SYSDATE FROM DUAL", new Object[] {});
		
		if (sqlResult != null && sqlResult.size() > 0)
			return (Timestamp) sqlResult.get(0).get("SYSDATE");
		else
			return null;
	}
	
	private long getTimeDiff(Timestamp databaseTime, Timestamp localTime) {
		if (databaseTime != null && localTime != null)
			return databaseTime.getTime() - localTime.getTime();
		else
			return 0;
	}
}