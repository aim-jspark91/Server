package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author smkang
 * @since 2018.11.07
 * @see Check MachineIdleTime for update IsIdleTimeOver.
 */
public class MachineIdleTimeOverMonitor implements Job, InitializingBean, ApplicationContextAware {

	private static Log log = LogFactory.getLog(MachineIdleTimeOverMonitor.class);
	
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	private static ApplicationContext applicationContext;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
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
//	        	doMachineIdleTimeOver();
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
				
	        	doMachineIdleTimeOver();
				
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
	
	private void doMachineIdleTimeOver() throws Exception {
		// Search MachineIdleTime.
		String condition = "MQCTYPE = ? AND ISIDLETIMEOVER = ? AND ((SYSDATE - LASTRUNTIME) * 24 * 60) >= IDLETIME";
		Object[] bindSet = new Object[] {"IDLE_TIME", "N"};
		
		List<MachineIdleTime> machineIdleTimeList = null;
		try {
			machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
		} catch (Exception e) {
			log.debug(e);
		}
		
		if (machineIdleTimeList != null && machineIdleTimeList.size() > 0) {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(MachineIdleTimeOverMonitor.class.getSimpleName(), System.getProperty("svr"), MachineIdleTimeOverMonitor.class.getSimpleName(), null, null);
			
			for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
				//2018.11.20_hsryu_add Validation_if MachineState is 'RUN', not update.
				
				Machine machineData = null;
				String unitName = "";
				if(!StringUtils.equals(machineIdleTime.getUnitName(), "*")){
					machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineIdleTime.getUnitName());
				    unitName = machineIdleTime.getUnitName();
				}
				else{
					machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineIdleTime.getMachineName());
				     unitName= "*";
				}
				
				if(!StringUtils.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN)) {
					machineIdleTime.setIsIdleTimeOver("Y");
					machineIdleTime.setLastEventName(eventInfo.getEventName());
					machineIdleTime.setLastEventTimekey(eventInfo.getEventTimeKey());
					machineIdleTime.setLastEventTime(eventInfo.getEventTime());
					machineIdleTime.setLastEventUser(eventInfo.getEventUser());
					machineIdleTime.setLastEventComment(eventInfo.getEventComment());
					
					ExtendedObjectProxy.getMachineIdleTimeService().modify(eventInfo, machineIdleTime);
					MESLotServiceProxy.getSendServiceImpl().SendToFMCIsIdleTimeOver(machineData.getKey().getMachineName());
					
					// STAT ADD BY JHY MANTIS:6049 ON20200423

					//boolean needToResetMachineIdleTime = resetType.equals("MULTI") ? true : false;

					String condition1 = "MACHINENAME = ? AND UNITNAME = ? ";
					Object[] bidSet = new Object[] {machineIdleTime.getMachineName(), unitName};

					List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition1, bidSet);

					int k = mqcConditionList.size();
					
					  String sql="SELECT D.DURABLENAME,D.MACHINENAME,D.PORTNAME ,L.LOTNAME,L.PRODUCTSPECNAME FROM DURABLE D "
					  		+ "LEFT OUTER JOIN LOT L ON D.DURABLENAME = L.CARRIERNAME "
					  		+ "WHERE D.MACHINENAME = :MACHINENAME AND D.TRANSPORTSTATE='ONEQP' AND D.POSITIONTYPE ='PORT' "
					  		+ "AND D.MACHINENAME= L.MACHINENAME "
					  		+ "AND L.LOTPROCESSSTATE = 'RUN' ";	
					   Map<String, Object> bindmap = new HashMap<String, Object>();
					   bindmap.put("MACHINENAME",machineIdleTime.getMachineName());
					   List<Map<String, Object>> cstResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindmap);
						
					 if(cstResult != null && cstResult.size()>0){

							 for (MQCCondition mqcCondition : mqcConditionList) {
								 
								 for(int i=0; i<cstResult.size();i++)	 
								 {
			                          
									if (mqcCondition.getMqcProductSpecName().equals(cstResult.get(i).get("PRODUCTSPECNAME").toString())) {
										
			                                 k--;
			                                 break;

									}
										
							    }
								
						 }
							 if (  k == mqcConditionList.size() ) {
								 for (MQCCondition mqcCondition : mqcConditionList) {
									   if(mqcCondition.getMqcRunCount()!=0 || mqcCondition.getMqcPreRunCount() !=0  )
									   {
										    eventInfo.setEventName("ClearMqcRunCount");
											eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
											eventInfo.setEventComment("Set Mqc Run Count :"+ mqcCondition.getMqcRunCount() +"--> 0 ,Set Mqc Pre Run Count :"+mqcCondition.getMqcPreRunCount()+"--> 0 ");
											mqcCondition.setLastEventComment(eventInfo.getEventComment());
											mqcCondition.setMqcRunCount(0);
											mqcCondition.setMqcPreRunCount(0);
											mqcCondition.setLastEventName(eventInfo.getEventName());
											mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
											mqcCondition.setLastEventTime(eventInfo.getEventTime());
											mqcCondition.setLastEventUser(eventInfo.getEventUser());
											ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
									   }
								 }
							
					     }
						 
				   }
					 else{
						 
						   for (MQCCondition mqcCondition : mqcConditionList) {
								
							   if(mqcCondition.getMqcRunCount()!=0 || mqcCondition.getMqcPreRunCount() !=0  )
							   {
								    eventInfo.setEventName("ClearMqcRunCount");
									eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());	
									eventInfo.setEventComment("Set Mqc Run Count :"+ mqcCondition.getMqcRunCount() +"--> 0 ,Set Mqc Pre Run Count :"+mqcCondition.getMqcPreRunCount()+"--> 0 ");
									mqcCondition.setLastEventComment(eventInfo.getEventComment());
									mqcCondition.setMqcRunCount(0);
									mqcCondition.setMqcPreRunCount(0);
									mqcCondition.setLastEventName(eventInfo.getEventName());
									mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
									mqcCondition.setLastEventTime(eventInfo.getEventTime());
									mqcCondition.setLastEventUser(eventInfo.getEventUser());
									
									ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
							   }
							}
						 
					 }
						// END ADD BY JHY MANTIS:6049 ON20200423

				}	
		
			}
		}		
	}

	
    private void clearMqcRunCount() throws Exception{
    	
    	// Search MachineIdleTime ,ISIDLETIMEOVER = Y.
		String condition = "MQCTYPE = ? AND ISIDLETIMEOVER = ? AND ((SYSDATE - LASTRUNTIME) * 24 * 60) >= IDLETIME";
		Object[] bindSet = new Object[] {"IDLE_TIME", "Y"};
		
		List<MachineIdleTime> machineIdleTimeList = null;
		try {
			machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
		} catch (Exception e) {
			log.debug(e);
		}
		
		if (machineIdleTimeList != null && machineIdleTimeList.size() > 0) {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClearMqcRunCount", System.getProperty("svr"), "ClearMqcRunCount", null, null);
			
			for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
				//2018.11.20_hsryu_add Validation_if MachineState is 'RUN', not update.
				
				Machine machineData = null;
				String unitName = "";
				if(!StringUtils.equals(machineIdleTime.getUnitName(), "*")){
					machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineIdleTime.getUnitName());
				    unitName = machineIdleTime.getUnitName();
				}
				else{
					machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineIdleTime.getMachineName());
				     unitName= "*";
				}
				
				if(!StringUtils.equals(machineData.getMachineStateName(), GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN)) {
					
					// STAT ADD BY JHY MANTIS:6049 ON20200423

					//boolean needToResetMachineIdleTime = resetType.equals("MULTI") ? true : false;

					String condition1 = "MACHINENAME = ? AND UNITNAME = ? ";
					Object[] bidSet = new Object[] {machineIdleTime.getMachineName(), unitName};

					List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition1, bidSet);

					int k = mqcConditionList.size();
					
					  String sql="SELECT D.DURABLENAME,D.MACHINENAME,D.PORTNAME ,L.LOTNAME,L.PRODUCTSPECNAME FROM DURABLE D "
					  		+ "LEFT OUTER JOIN LOT L ON D.DURABLENAME = L.CARRIERNAME "
					  		+ "WHERE D.MACHINENAME = :MACHINENAME AND D.TRANSPORTSTATE='ONEQP' AND D.POSITIONTYPE ='PORT' "
					  		+ "AND D.MACHINENAME= L.MACHINENAME "
					  		+ "AND L.LOTPROCESSSTATE = 'RUN' ";	
					   Map<String, Object> bindmap = new HashMap<String, Object>();
					   bindmap.put("MACHINENAME",machineIdleTime.getMachineName());
					   List<Map<String, Object>> cstResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindmap);
						
					 if(cstResult != null && cstResult.size()>0){

							 for (MQCCondition mqcCondition : mqcConditionList) {
								 
								 for(int i=0; i<cstResult.size();i++)	 
								 {
			                          
									if (mqcCondition.getMqcProductSpecName().equals(cstResult.get(i).get("PRODUCTSPECNAME").toString())) {
										
			                                 k--;
			                                 break;

									}
										
							    }
								
						 }
							 if ( (machineIdleTime.getResetType().equals("MULTI") && k != 0) ||  ( machineIdleTime.getResetType().equals("SINGLE") && k != mqcConditionList.size() )) {
								 for (MQCCondition mqcCondition : mqcConditionList) {
									   if(mqcCondition.getMqcRunCount()!=0 || mqcCondition.getMqcPreRunCount() !=0  )
									   {
											eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
											eventInfo.setEventComment("Set Mqc Run Count :"+ mqcCondition.getMqcRunCount() +"--> 0 ,Set Mqc Pre Run Count :"+mqcCondition.getMqcPreRunCount()+"--> 0 ");
											mqcCondition.setLastEventComment(eventInfo.getEventComment());
											mqcCondition.setMqcRunCount(0);
											mqcCondition.setMqcPreRunCount(0);
											mqcCondition.setLastEventName(eventInfo.getEventName());
											mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
											mqcCondition.setLastEventTime(eventInfo.getEventTime());
											mqcCondition.setLastEventUser(eventInfo.getEventUser());
											ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
									   }
								 }
							
					     }
						 
				   }
					 else{
						 
						   for (MQCCondition mqcCondition : mqcConditionList) {
								
							   if(mqcCondition.getMqcRunCount()!=0 || mqcCondition.getMqcPreRunCount() !=0  )
							   {
									eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());	
									eventInfo.setEventComment("Set Mqc Run Count :"+ mqcCondition.getMqcRunCount() +"--> 0 ,Set Mqc Pre Run Count :"+mqcCondition.getMqcPreRunCount()+"--> 0 ");
									mqcCondition.setLastEventComment(eventInfo.getEventComment());
									mqcCondition.setMqcRunCount(0);
									mqcCondition.setMqcPreRunCount(0);
									mqcCondition.setLastEventName(eventInfo.getEventName());
									mqcCondition.setLastEventTimekey(eventInfo.getEventTimeKey());
									mqcCondition.setLastEventTime(eventInfo.getEventTime());
									mqcCondition.setLastEventUser(eventInfo.getEventUser());
									
									ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcCondition);
							   }
							}
						 
					 }
						// END ADD BY JHY MANTIS:6049 ON20200423
				}

			}	
				
		  }
				
    }	
  
}