package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
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

public class SortArrangePort implements Job, InitializingBean, ApplicationContextAware {

    private static Log log = LogFactory.getLog(FileJudgeInspection.class);
    
    // Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
 	private static ApplicationContext applicationContext;
    
    @Override
    public void afterPropertiesSet() throws Exception 
    {
        log.info(String.format("SorterArrangePort service set completed", getClass().getSimpleName()));
    }
    
    @Override
	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
    
    @Override
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
//	        	doArrangePort();
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
//    		scheduleJobFactory.endScheduleJob(this);
//		}
		// Modified by smkang on 2019.04.24 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
    	//									  For avoid duplication of schedule job, running information should be recorded.
    	try {
    		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
    		ScheduleJob scheduleJob = scheduleJobFactory.startScheduleJob(arg0);
    			
			try {
	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
				
	        	doArrangePort();
				
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
    
    private void doArrangePort() throws Exception {
    	EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignPort", "Timer", "Timer", "", "");	
    	
    	// to find sort flow;
    	String sql = "SELECT PROCESSFLOWNAME FROM PROCESSFLOW WHERE FACTORYNAME = :FACTORYNAME AND PROCESSFLOWTYPE = :PROCESSFLOWTYPE AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION";
    	
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", "ARRAY");
		bindMap.put("PROCESSFLOWTYPE", "Sort");
		bindMap.put("PROCESSFLOWVERSION", "00001");
		
		List<Map<String, Object>> SortFlow = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		if(SortFlow != null && SortFlow.size() > 0)
		{
			// to find sort operation;
			sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
			sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
			sql += " FROM ARC A,";
			sql += " NODE N,";
			sql += " PROCESSFLOW PF";
			sql += " WHERE 1 = 1";
			sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = :FACTORYNAME";
			sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
			sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
			sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
			sql += " AND A.FROMNODEID = N.NODEID)";
			sql += " START WITH NODETYPE = :NODETYPE";
			sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
			
			bindMap.put("PROCESSFLOWNAME", (String)SortFlow.get(0).get("PROCESSFLOWNAME"));
			bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> SortOperation = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if (SortOperation.size() > 0) 
			{				
				// to find Machine List to arrange ports;
				sql = "SELECT DISTINCT MACHINENAME FROM (SELECT MACHINENAME FROM CT_MACHINEGROUPMACHINE WHERE MACHINEGROUPNAME = (SELECT MACHINEGROUPNAME FROM PROCESSOPERATIONSPEC WHERE PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME)";
				sql += " UNION ALL ";
				sql += "SELECT DISTINCT MS.MACHINENAME FROM MACHINESPEC MS, CT_OPERATIONMODE OM WHERE MS.MACHINENAME = OM.MACHINENAME AND MS.FACTORYNAME = :FACTORYNAME AND MS.DETAILMACHINETYPE = :DETAILMACHINETYPE AND OM.OPERATIONMODE = :OPERATIONMODE)";
				
    			bindMap.put("PROCESSOPERATIONNAME", (String)SortOperation.get(1).get("PROCESSOPERATIONNAME"));
    			bindMap.put("FACTORYNAME", "ARRAY");
    			bindMap.put("DETAILMACHINETYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN);
    			bindMap.put("OPERATIONMODE", GenericServiceProxy.getConstantMap().OPERATIONMODE_SORTING);
    			
    			@SuppressWarnings("unchecked")
    			List<Map<String, Object>> SortMachineList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
    			
    			if (SortMachineList.size() > 0) 
    			{
    				for(int i=0; i<SortMachineList.size(); i++)
    				{
    					String MachineName = (String)SortMachineList.get(i).get("MACHINENAME");        					
    					MESLotServiceProxy.getLotInfoUtil().rearrangeSorterPort("", MachineName, eventInfo);       					     					
    				}
    			}  			
			}			
		}
    }
}