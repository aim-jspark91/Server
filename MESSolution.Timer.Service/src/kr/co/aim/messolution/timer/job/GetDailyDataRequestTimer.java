package kr.co.aim.messolution.timer.job;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

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
/**
 * @author Administrator
 *
 */
public class GetDailyDataRequestTimer implements Job, InitializingBean, ApplicationContextAware
{
	//Equal Factory QTimer
    private static Log log = LogFactory.getLog(GetDailyDataRequestTimer.class);
    
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
//	        try {
//	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//	        	doRequestDailyData();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				log.error(e);
//			}
//	        
//	        // Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//	     	scheduleJobFactory.endScheduleJob(this);
//		}
		// Modified by smkang on 2019.04.24 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
    	//									  For avoid duplication of schedule job, running information should be recorded.
    	try {
    		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
    		ScheduleJob scheduleJob = scheduleJobFactory.startScheduleJob(arg0);
        				
	        try {
	        	log.info(String.format("Job[%s] START", this.getClass().getName()));
				
	        	doRequestDailyData();
				
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
    
    private void doRequestDailyData() throws Exception {
    	List<Map<String, Object>> machineList = getDailyDataSchedule();
        
        if(machineList != null && machineList.size() > 0)
        {
            Machine machineData = null;
            String machineName = StringUtil.EMPTY;
            String unitName = StringUtil.EMPTY;
            String subUnitName = StringUtil.EMPTY;
            String targetSubjectName = StringUtil.EMPTY;
            String originalSourceSubjectName = StringUtil.EMPTY;
            
            Calendar now    = Calendar.getInstance(); 
            int year      = now.get(Calendar.YEAR); 
            int month     = now.get(Calendar.MONTH) + 1; 
            int nowdate   = now.get(Calendar.DATE); 
            int dayofweek = now.get(Calendar.DAY_OF_WEEK); 
            int hour      = now.get(Calendar.HOUR_OF_DAY); 
            int min       = now.get(Calendar.MINUTE); 
            
            int currentMinute = (hour * 60) + min;
            
            for (int idx=0; idx < machineList.size(); idx++)
            {         
                int scheduleMinute = Integer.parseInt(machineList.get(idx).get("TIME").toString());
                if(currentMinute % scheduleMinute == 0)
                {                    
                    machineName = machineList.get(idx).get("MACHINENAME").toString();
                    unitName = machineList.get(idx).get("UNITNAME") == null ? StringUtil.EMPTY : machineList.get(idx).get("UNITNAME").toString();
                                            
                    machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
                    targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
                    originalSourceSubjectName =GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");
                    
                    if(StringUtil.isNotEmpty(targetSubjectName))
                    {
                        generateGetDailyDataTemplate(machineName, unitName, originalSourceSubjectName, targetSubjectName);      
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @Name     generateGetDailyDataTemplate
     * @since    2018. 8. 7.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param unitName
     * @param originalSourceSubjectName
     * @param targetSubjectName
     * @throws CustomException
     */
    private void generateGetDailyDataTemplate(String machineName, String unitName, String originalSourceSubjectName, String targetSubjectName)
            throws CustomException
    {
        Element elementBody = new Element(SMessageUtil.Body_Tag);
        
        Element elementMachineName = new Element("MACHINENAME");
        elementMachineName.setText(machineName);
        elementBody.addContent(elementMachineName);

        Element elementUnitName = new Element("UNITNAME");
        elementUnitName.setText(unitName);
        elementBody.addContent(elementUnitName);            

        try
        {   
            Document requestDoc = SMessageUtil.createXmlDocumentWithOutLanguage(elementBody, "A_GetDailyDataRequest",
                    originalSourceSubjectName,
                    targetSubjectName,
                    "MES",
                    "Get Daily Data Requst");
            
            String      SUCCESS = "0";
            SMessageUtil.setResultItemValue(requestDoc, SMessageUtil.Result_ReturnCode, SUCCESS);
            
            GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, requestDoc, "EISSender");
            /* 20190314, hhlee, Record MessageLog */
            //GenericServiceProxy.getESBServive().recordMessagelogAftersendBySender(targetSubjectName, requestDoc, "EISSender");
        }
        catch (Exception ex)
        {
        }
    }
    
    /**
     * 
     * @Name     getDailyDataSchedule
     * @since    2018. 8. 7.
     * @author   hhlee
     * @contents 
     *           
     * @return
     * @throws Exception
     */
    private List<Map<String, Object>> getDailyDataSchedule()
    {
        Map<String, Object> bindMap = new HashMap<String, Object>();
        String strSql = "" ;
        strSql = strSql + " SELECT MSQ.MACHINENAME   AS MACHINENAME,                               \n"
                        + "        MSQ.UNITNAME      AS UNITNAME,                                  \n"
                        + "        MSQ.TIME          AS TIME,                                      \n"
                        + "        MSQ.MCSUBJECTNAME AS MCSUBJECTNAME                              \n"
                        + "   FROM (SELECT MDC.MACHINENAME   AS MACHINENAME,                       \n"
                        + "                MS.MACHINENAME    AS UNITNAME,                          \n"
                        + "                MDC.TIME          AS TIME,                              \n"
                        + "                MDC.MCSUBJECTNAME AS MCSUBJECTNAME                      \n"
                        + "           FROM MACHINESPEC MS,                                         \n"
                        + "                (SELECT DC.MACHINENAME  AS MACHINENAME,                 \n"
                        + "                        DC.TIME         AS TIME,                        \n"
                        + "                        M.MCSUBJECTNAME AS MCSUBJECTNAME                \n"
                        + "                   FROM CT_DAILYCHECK DC,                               \n"
                        + "                        MACHINE       M                                 \n"
                        + "                  WHERE 1 = 1                                           \n"
                        + "                    AND DC.DETAILMACHINETYPE = 'MAIN'                   \n"
                        + "                    AND DC.MACHINENAME = M.MACHINENAME                  \n"
                        + "                ) MDC                                                   \n"
                        + "          WHERE 1 = 1                                                   \n"
                        + "            AND MS.SUPERMACHINENAME = MDC.MACHINENAME                   \n"
                        + "            AND MS.MACHINETYPE = 'ProductionMachine'                    \n"
                        + "        ) MSQ                                                           \n"
                        + "  WHERE NOT EXISTS (SELECT 1                                            \n"
                        + "                      FROM (SELECT DC.SUPERMACHINENAME AS MACHINENAME,  \n"
                        + "                                   DC.MACHINENAME      AS UNITNAME,     \n"
                        + "                                   DC.TIME             AS TIME,         \n"
                        + "                                   M.MCSUBJECTNAME     AS MCSUBJECTNAME \n"
                        + "                              FROM CT_DAILYCHECK DC,                    \n"
                        + "                                   MACHINE       M                      \n"
                        + "                             WHERE 1 = 1                                \n"
                        + "                               AND DC.DETAILMACHINETYPE = 'UNIT'        \n"
                        + "                               AND M.MACHINENAME = DC.SUPERMACHINENAME  \n"
                        + "                           ) USQ                                        \n"
                        + "                     WHERE MSQ.MACHINENAME = USQ.MACHINENAME            \n"
                        + "                       AND MSQ.UNITNAME = USQ.UNITNAME                  \n"
                        + "                   )                                                    \n"
                        + " UNION                                                                  \n"
                        + " SELECT DC.SUPERMACHINENAME AS MACHINENAME,                             \n"
                        + "        DC.MACHINENAME      AS UNITNAME,                                \n"
                        + "        DC.TIME             AS TIME,                                    \n"
                        + "        M.MCSUBJECTNAME     AS MCSUBJECTNAME                            \n"
                        + "   FROM CT_DAILYCHECK DC,                                               \n"
                        + "        MACHINE       M                                                 \n"
                        + "  WHERE 1 = 1                                                           \n"
                        + "    AND DC.DETAILMACHINETYPE = 'UNIT'                                   \n"
                        + "    AND M.MACHINENAME = DC.SUPERMACHINENAME                             \n";
        
        List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
        
        return sqlResult;
    }     
}