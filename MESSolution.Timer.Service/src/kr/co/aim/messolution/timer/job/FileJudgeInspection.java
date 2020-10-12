package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.extended.object.management.data.LotMultiHold;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
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
public class FileJudgeInspection implements Job, InitializingBean, ApplicationContextAware
{
	//Equal Factory QTimer
    private static Log log = LogFactory.getLog(FileJudgeInspection.class);
    private EventInfo trackOutEventInfo = new EventInfo();
    
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
//			try {
//				log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//				doFileJudgeInspection();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				log.error(e);
//			}
//	    	
//	    	// Added by smkang on 2019.04.08 - For avoid duplication of schedule job.
//	    	scheduleJobFactory.endScheduleJob(this);
//		}
    	// Modified by smkang on 2019.04.24 - According to Liu Hongwei's request, SCHsvr should be executed AP1 and AP2.
    	//									  For avoid duplication of schedule job, running information should be recorded.
    	try {
    		ScheduleJobFactory scheduleJobFactory = (ScheduleJobFactory) applicationContext.getBean(ScheduleJobFactory.class.getSimpleName());
        	ScheduleJob scheduleJob = scheduleJobFactory.startScheduleJob(arg0);

    		try {
    			log.info(String.format("Job[%s] START", this.getClass().getName()));
    			
    			doFileJudgeInspection();
    			
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
    
    private void doFileJudgeInspection() throws Exception {
		List<Product> ProductList = null;
		
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sqlResult =null;
        
        List<Lot> LotList=null;
    	try {
    		String LotNameList ="";
        	// Find Lot
        	// Table ProcessoperationSpec.DETAILPROCESSOPERATIONTYPE == ''DUMMY" && Table Lot.LOTPROCESSSTATE == "WAIT" and Has GHLD HOLD
    		LotList = LotServiceProxy.getLotService().select("WHERE PROCESSOPERATIONNAME IN (SELECT PROCESSOPERATIONNAME FROM PROCESSOPERATIONSPEC WHERE DETAILPROCESSOPERATIONTYPE = ?) AND LOTPROCESSSTATE = ? AND LOTNAME IN (SELECT LOTNAME FROM CT_LOTMULTIHOLD A WHERE A.REASONCODE = ?)",new Object[]{"DUMMY","WAIT","GHLD"});

    		if(LotList!=null && LotList.size()>0){
    			for(Lot l :LotList){
    				LotNameList+= l.getKey().getLotName()+" ";
    			}
        		log.info("Select lot List [" + LotNameList + " ] lotList Size : "+ LotList.size());
    		}

		} catch (Exception e) {
			return;
		}

    	try
        {
    		log.info("LotList for Logic Start");
        	for(Lot lot : LotList){
        		//2019.01.19_hsryu_requested by hongwai. if OtherHold except 'GHOLD' is exist, not execute Logic. 
        		if(!this.checkOtherHold(lot, "GHLD"))
        		{
        			try{
            			ProductList=null;
    					// getProductList processinginfo == P
    	        		ProductList = ProductServiceProxy.getProductService().select(" WHERE LOTNAME= ? AND PROCESSINGINFO= ?  ", new Object[]{lot.getKey().getLotName(),"P"});
        			}
        			catch(Throwable e){
        				log.info("ProcessingInfo 'P' ProductList is null.");
        			}
        			
	        		// 2019.05.05_hsryu_Modify Logic. Mantis 0003761.
        			try {
        				sqlResult=null;

        				String sql =  "SELECT DISTINCT PRODUCTNAME " +
    	        				"  FROM PRODUCT P " +
    	        				" 	WHERE PROCESSINGINFO = :PROCESSINGINFO " +
    	        				" 	AND LOTNAME = :LOTNAME " +
    	        				" 	AND PRODUCTNAME NOT IN ( " +
    	        				"       SELECT DISTINCT A.GLASSNAME " +
    	        				"       	FROM CT_PANELJUDGE A " +
		        				"       		WHERE A.GLASSNAME IN (SELECT PRODUCTNAME " +
					        	"       		FROM PRODUCT " +
					        	"       			WHERE LOTNAME = :LOTNAME " +
					        	"       			AND PROCESSINGINFO = :PROCESSINGINFO) " +
		        				"       AND A.MACHINENAME = :MACHINENAME " +
        						"       AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ) " ;

    		             Map<String, String> bindMap = new HashMap<String, String>();
    		             bindMap.put("LOTNAME", lot.getKey().getLotName());
    		             bindMap.put("PROCESSINGINFO", "P");
    		             bindMap.put("MACHINENAME", lot.getUdfs().get("LASTLOGGEDOUTMACHINE"));
    		             bindMap.put("PROCESSOPERATIONNAME", lot.getUdfs().get("BEFOREOPERATIONNAME"));
    		             
    		             sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
        			}
        			catch (Throwable e){
        				log.warn("Fail Search Result of Inspection. Lot : " + lot.getKey().getLotName());
        			}
        			
            		List<LotMultiHold> lotMultiHoldList = null;
            		try {
            			lotMultiHoldList = ExtendedObjectProxy.getLotMultiHoldService().select(" where resetflag = ? and lotName= ? ", new Object[]{"Y", lot.getKey().getLotName()});
        			} catch (Exception e) {
        				log.info(lot.getKey().getLotName() + " don't have ResetFlag");
        			}
            		
            		try {
            			//ProductList == sqlReslut -> receive All Data
            			//if( (lotMultiHoldList!=null && StringUtils.equals(lotMultiHoldList.get(0).getResetFlag(), "Y")) ||  (ProductList!=null && sqlResult!=null && ProductList.size()>0 && sqlResult.size()>0 && ProductList.size()==sqlResult.size())  ){
                		if( (lotMultiHoldList!=null && StringUtils.equals(lotMultiHoldList.get(0).getResetFlag(), "Y")) || sqlResult.size() == 0 ){
                			
                			if(sqlResult.size() == 0){
                				log.info("all receive Inspection Result or 'P' ProcessingInfo is not exist. ");
                			}
            				if(lotMultiHoldList!=null && StringUtils.equals(lotMultiHoldList.get(0).getResetFlag(), "Y")){
            					log.info("lotName : "+lot.getKey().getLotName()+ " Reset Flag "+lotMultiHoldList.get(0).getResetFlag()+"\n");
            				}
            				GenericServiceProxy.getTxDataSourceManager().beginTransaction();
            				
            				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            				lot = MESLotServiceProxy.getLotInfoUtil().getLotData(lot.getKey().getLotName());
            				lot = LotServiceProxy.getLotService().selectByKeyForUpdate(lot.getKey());
            				
            				if(!IsDummyOperation(lot)){
            					throw new CustomException("COMMON-0001","This Lot is Not Dummy Operation!");
            				}            				
            				
                    		String note="";
                    		// 2019.03.20_hsryu_Insert Logic. For TrackOut Priority Logic. 
                    		String beforeProcessFlowName = lot.getProcessFlowName();
                    		String beforeProcessOperationName = lot.getProcessOperationName();
                    		
                			//if NextOperation is Nothing Release, TrackIn, TrackOut,SetNote
                			if(checkEndOperation(lot)){
                				log.info("Before ReleaseHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                				log.info("ReleaseHold Start\n");
                    			lot = Release(lot,"GHLD");
                				log.info("ReleaseHold End\n");
                				log.info("Before TrackIn LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			log.info("TrackIn Start\n");
                    			lot = TrackIn(lot);
                    			log.info("TrackIn End\n");
                    			log.info("Before TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			log.info("TrackOut Start\n");
                    			
                    			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                    			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", "SYS", "Timer TrackOut", null, null);
                    	    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
                    	    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
                    	    	/*********************************************************/
                    	    	
                    	    	// 2019.05.06_hsryu_Insert pass EventInfo. 
                    			lot = TrackOut(lot, beforeProcessFlowName, beforeProcessOperationName, eventInfo);
                    			log.info("TrackOut End\n");
                    			log.info("After TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			note = "Next Sampling Operation is Nothing";
                    			lot = LotServiceProxy.getLotService().selectByKey(lot.getKey());
                    			setNote(lot,note);
                    			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                    			this.setProcessFlag(lot, eventInfo);
                    	        log.info("After setProcessFlag LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                	            
                	            // 2019.05.31_hsryu_Add Logic. Check BHold ! 
                    	        lot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lot.getKey().getLotName(), eventInfo);
                    	        log.info("After checkBHoldAndOperHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                			}
                			// Next Operation ForceRepairFlag == Y -> Release, TrackIn, Track Out, SetNote, HoldLot
                			else if(getForceRepairFlagIsY(lot))
                			{
                				log.info("Before ReleaseHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                				log.info("ReleaseHold Start\n");
                				lot = Release(lot,"GHLD");
                				log.info("ReleaseHold End\n");
                				log.info("Before TrackIn LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			log.info("TrackIn Start\n");
                    			lot = TrackIn(lot);
                    			log.info("TrackIn End\n");
                    			log.info("Before TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			log.info("TrackOut Start\n");
                    			
                    			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                    			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", "SYS", "Timer TrackOut", null, null);
                    	    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
                    	    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
                    	    	/*********************************************************/
                    	    	
                    			lot = TrackOut(lot, beforeProcessFlowName, beforeProcessOperationName, eventInfo);
                    			log.info("TrackOut End\n");
                    			log.info("After TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    			note+="Noskip [ ";
                    			// note + ProductNames 샘플링한 모든 ProductNames
                    			EventInfo changeGradeEventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", "SYS", "ChangeGrade", "", "");
                    			// 2019.05.06_hsryu_Delete setBehavior. record History.
                    			//changeGradeEventInfo.setBehaviorName("ARRAY");
                    			for(Product product : ProductList){
                    				
                					Product validationProduct = ProductServiceProxy.getProductService().selectByKey(new ProductKey(product.getKey().getProductName()));
                					
                					if(!validationProduct.getProductGrade().equals("S"))
                					{
                        				note+=product.getKey().getProductName()+" ";
                        				ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();
                        				changeGradeInfo.setProductGrade("P");
                        				changeGradeInfo.setPosition(product.getPosition());
                        				changeGradeInfo.setSubProductQuantity1(product.getSubProductQuantity1());
                        				changeGradeInfo.setSubProductQuantity2(product.getSubProductQuantity2());
                        				ProductServiceProxy.getProductService().changeGrade(product.getKey(), changeGradeEventInfo, changeGradeInfo);
                					}
                    				
                    			}
                    			note += " ] ForceRepairFlag == Y";
                    			lot = LotServiceProxy.getLotService().selectByKey(lot.getKey());
                    			setNote(lot,note);
                    			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                    			this.setProcessFlag(lot, eventInfo);
                    			log.info("After setProcessFlag LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                	            
                    			// 2019.05.31_hsryu_Add Logic. Check BHold ! 
                    	        lot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lot.getKey().getLotName(), eventInfo);
                    	        log.info("After checkBHoldAndOperHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                			}
                			else
                			{
            					// lot의 FileJudge == nextoperation.RepariGradeFlag
            					// Release,TrackIn,TrackOut, SetNote, HoldLot
                				if(isSameFileJudgeAndReparGrade(lot))
                				{
                					log.info("Before ReleaseHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    				log.info("ReleaseHold Start\n");
                    				lot = Release(lot,"GHLD");
                    				log.info("ReleaseHold End\n");
                    				log.info("Before TrackIn LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                        			log.info("TrackIn Start\n");
                        			lot = TrackIn(lot);
                        			log.info("TrackIn End\n");
                        			log.info("Before TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                        			log.info("TrackOut Start\n");
                        			
                        			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                        			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", "SYS", "Timer TrackOut", null, null);
                        	    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
                        	    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
                        	    	/*********************************************************/

                        			lot = TrackOut(lot, beforeProcessFlowName, beforeProcessOperationName, eventInfo);
                        			log.info("TrackOut End\n");
                        			log.info("After TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
        	            			note+="Noskip [ ";

        	            			ProcessOperationSpec OperationData = CommonUtil.getProcessOperationSpec(lot.getFactoryName(), lot.getProcessOperationName());
        	            			FileJudgeSetting fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {lot.getFactoryName() ,lot.getProcessFlowName(),lot.getProcessFlowVersion(), OperationData.getKey().getProcessOperationName(),OperationData.getKey().getProcessOperationVersion() });	
                        			// note + ProductNames nextoperation.RepariGradeFlag == Product.FileJudge 인 모든 ProductNames
                        			EventInfo changeGradeEventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", "SYS", "ChangeGrade", "", "");
                        			// 2019.05.06_hsryu_Delete setBehavior. record History.
                        			//changeGradeEventInfo.setBehaviorName("ARRAY");
                        			for(Product product : ProductList){
                        				if(StringUtil.equals(product.getUdfs().get("FILEJUDGE"), fileJudgeSetting.getRepairGradeFlag()))
                        				{
                        					Product validationProduct = ProductServiceProxy.getProductService().selectByKey(new ProductKey(product.getKey().getProductName()));
                        					
                        					if(!validationProduct.getProductGrade().equals("S"))
                        					{
                            					note+=product.getKey().getProductName()+" ";
                                				ChangeGradeInfo changeGradeInfo = new ChangeGradeInfo();
                                				changeGradeInfo.setProductGrade("P");
                                				changeGradeInfo.setPosition(product.getPosition());
                                				changeGradeInfo.setSubProductQuantity1(product.getSubProductQuantity1());
                                				changeGradeInfo.setSubProductQuantity2(product.getSubProductQuantity2());
                                				ProductServiceProxy.getProductService().changeGrade(product.getKey(), changeGradeEventInfo, changeGradeInfo);
                        					}
                        				}
                        			}
                        			note += " ] ForceRepairFlag == N And RepairGrade == Product.FileJudge";
                        			lot = LotServiceProxy.getLotService().selectByKey(lot.getKey());
        	            			setNote(lot,note);
        	            			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                        			this.setProcessFlag(lot, eventInfo);
                        			log.info("After setProcessFlag LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                        			
                        			// 2019.05.31_hsryu_Add Logic. Check BHold ! 
                        	        lot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lot.getKey().getLotName(), eventInfo);
                        	        log.info("After checkBHoldAndOperHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                				}
                				else
                				{
                					// 위의 모든 경우의 수가 아니라면
                					log.info("Before ReleaseHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                    				log.info("ReleaseHold Start\n");
                    				lot = Release(lot,"GHLD");
                    				log.info("ReleaseHold End\n");
                    				log.info("Before TrackIn LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                        			log.info("TrackIn Start\n");
                        			lot = TrackIn(lot);
                        			log.info("TrackIn End\n");
                        			log.info("Before TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                        			log.info("TrackOut Start\n");
                        			
                        			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                        			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", "SYS", "Timer TrackOut", null, null);
                        	    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
                        	    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
                        	    	/*********************************************************/

                        			lot = TrackOut(lot, beforeProcessFlowName, beforeProcessOperationName, eventInfo);
                        			log.info("TrackOut End\n");
                        			log.info("After TrackOut LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
        	            			note +="Skip Repair, No Sheet Grade meet the Repair Grade. And ResetFlag is Y";
        	            			lot = changeNextOperation(lot);
        	            			log.info("After ChangeOperation LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
        	            			EventInfo Info = EventInfoUtil.makeEventInfo("HoldLot", "SYS", "", "", "");
        	                        lot = MESLotServiceProxy.getLotServiceUtil().executeHold(Info, lot, "HoldLot", "GHLD");
        	                        lot = MESLotServiceProxy.getLotServiceUtil().executeMultiHold(Info, lot, "GHLD", "SYS","Y");
        	                        log.info("After Hold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
        	                        lot = LotServiceProxy.getLotService().selectByKey(lot.getKey());
                					setNote(lot,note);
        	            			// 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. 
                        			this.setProcessFlag(lot, eventInfo);
                        			log.info("After setProcessFlag LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");

                        			// 2019.05.31_hsryu_Add Logic. Check BHold ! 
                        	        lot = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lot.getKey().getLotName(), eventInfo);
                        	        log.info("After checkBHoldAndOperHold LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                				}
                			}
                			 // Remove MachineName And OperationName
                			log.info("Remove Start MachineName OperationName PanelJudge");
                			RemoveMachineNameAndOperationName(lot);
                			log.info("After RemoveMachineNameAndOperationName LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                			log.info("Remove End MachineName OperationName PanelJudge");
                			
                			lot = LotServiceProxy.getLotService().selectByKey(lot.getKey());
                			log.info("Empty Start ProductList Note");
                			setNoteEmptyProductList(lot);
                			log.info("After setNoteEmptyProductList LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                			log.info("Empty End ProductList Note");
                			
                			log.info("Last Lot LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
                			log.info("End Commit Lot LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
                		}
    				}
            		catch (Exception e) {
    					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
    					log.error(lot.getKey().getLotName() + " Timer Logic Fail");
    				}
            		
            		log.info("End if Lot LotName ["+lot.getKey().getLotName()+"] LotHoldState ["+lot.getLotHoldState() + "] LotProcessFlow [" +lot.getProcessFlowName()+"] LotProcessOperationName ["+lot.getProcessOperationName()+"]");
        		}
        	}
        }
        catch (Exception e) {
            throw new JobExecutionException("Filejudge Timer Error");
        }
    }
    
    private boolean IsDummyOperation(Lot lotData) {
    	try {
			ProcessOperationSpec processOperation = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
			
			if(StringUtils.equals("DUMMY", processOperation.getDetailProcessOperationType())){
				log.info("this Lot["+lotData.getKey().getLotName()+"] is Dummy Operation");
				return true;
			}else{
				log.info("this Lot["+lotData.getKey().getLotName()+"] is not Dummy Operation");
				return false;
			}
		} catch (CustomException e) {
			log.info("this Lot["+lotData.getKey().getLotName()+"] is not Dummy Operation");
			return false;
		}
    }
    
	private Lot changeNextOperation(Lot lotData) throws CustomException{

		try {
			String targetOperationName = getNextOperationName(lotData);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "SYS", "", "", "");
	    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
	    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
			if (StringUtil.equals(lotData.getLotProcessState(), "RUN"))
				throw new CustomException("LOT-0008", lotData.getKey().getLotName());

			if(lotData.getProcessOperationName().equals(targetOperationName))
			{
				throw new CustomException("LOT-0217", lotData.getKey().getLotName());
			}

			eventInfo.setEventName("OperationSkip");
			Map<String, String> lotUdfs = lotData.getUdfs();
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
			
			//Operation Changed, Update Product ProcessingInfo to N
			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			
			String areaName = lotData.getAreaName();
			String factoryName = lotData.getFactoryName();
			String lotHoldState = lotData.getLotHoldState();
			String lotProcessState = lotData.getLotProcessState();
			String lotState = lotData.getLotState();
			String processFlowName = lotData.getProcessFlowName();
			String processFlowVersion = lotData.getProcessFlowVersion();
			String processOperationName = lotData.getProcessOperationName();
			String processOperationVersion = lotData.getProcessOperationVersion();
			String productionType = lotData.getProductionType();
			String productSpec2Name = lotData.getProductSpec2Name();
			String productSpec2Version = lotData.getProductSpec2Version();
			String productSpecName = lotData.getProductSpecName();
			String productSpecVersion = lotData.getProductSpecVersion();
			long priority = lotData.getPriority();
			Timestamp dueDate = lotData.getDueDate();
			double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
			double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();


	 		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(), productionType, productSpecName,
					productSpecVersion, productSpec2Name, productSpec2Version, "", subProductUnitQuantity1, subProductUnitQuantity2,
					dueDate, priority, factoryName, areaName, lotState, lotProcessState, lotHoldState, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, processFlowName, targetOperationName, "", "", lotData.getNodeStack(), lotUdfs,
					productUdfs, true);
			
			eventInfo.setBehaviorName("ARRAY");

			return lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			
			//Note clear - YJYU
//			Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//			Map<String, String> udfs_note = lotData_Note.getUdfs();
//			udfs_note.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData_Note);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Change Operation Error");
		}
	}
	
	// Modified by smkang on 2019.06.19 - Performance Tuning.
//	private void RemoveMachineNameAndOperationName(Lot lotData) throws CustomException
//	{
//		List<Map<String, Object>> sqlResult = new ArrayList<Map<String,Object>>();
//				
//		String sql =  "SELECT DISTINCT A.GLASSNAME" +
//					  "  FROM CT_PANELJUDGE A" +
//					  " WHERE A.GLASSNAME IN (SELECT PRODUCTNAME" +
//					  "                         FROM PRODUCT" +
//					  "                        WHERE LOTNAME = :LOTNAME)";
//     
//         Map<String, String> bindMap = new HashMap<String, String>();
//         bindMap.put("LOTNAME", lotData.getKey().getLotName());
//         
//		try {
//			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//			
//			for(Map<String, Object> obj : sqlResult)
//			{
//				String productName = obj.get("GLASSNAME").toString();
//				
//				List<PanelJudge> panelJudgeList = ExtendedObjectProxy.getPanelJudgeService().select("WHERE GLASSNAME = ?", new Object[] {productName});
//				
//				for(PanelJudge panelJudge : panelJudgeList)
//				{
//					panelJudge.setMachineName("");
//					panelJudge.setProcessOperationName("");
//					ExtendedObjectProxy.getPanelJudgeService().update(panelJudge);
//				}
//			}
//		} catch (Exception e) {
//			log.error("Error Remove Machine and OperationName");
//			writeErrorNote(lotData, "Error Remove Machine and OperationName");
//			throw new CustomException("COMMON-0001","Error Remove Machine and OperationName");
//		}
//	}
	private void RemoveMachineNameAndOperationName(Lot lotData) throws CustomException
	{
		try {
			String sql = "UPDATE CT_PANELJUDGE SET MACHINENAME = ?, PROCESSOPERATIONNAME = ?" +
						 " WHERE GLASSNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = ?) AND MACHINENAME IS NOT NULL";
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, new Object[] {"", "", lotData.getKey().getLotName()});
		} catch (Exception e) {
			log.error("Error Remove Machine and OperationName");
			writeErrorNote(lotData, "Error Remove Machine and OperationName");
			throw new CustomException("COMMON-0001","Error Remove Machine and OperationName");
		}
	}

	private void setNote(Lot lotData,String note) throws CustomException {	
		try {
			LotHistoryKey lotHistoryKey = new LotHistoryKey();
			lotHistoryKey.setLotName(lotData.getKey().getLotName());
			lotHistoryKey.setTimeKey(trackOutEventInfo.getEventTimeKey());
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey);
			LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);

			lotHistory.getUdfs().put("NOTE", note);
	        LotServiceProxy.getLotHistoryService().update(lotHistory);
	        
	        log.info("After setNote LotName ["+lotData.getKey().getLotName()+"] LotHoldState ["+lotData.getLotHoldState() + "] LotProcessFlow [" +lotData.getProcessFlowName()+"] LotProcessOperationName ["+lotData.getProcessOperationName()+"]");
		} catch (Exception e) {
			log.info("setNote Error");
			throw new CustomException("COMMON-0001","setNote Error");
		}
	}
	
	private boolean checkEndOperation(Lot lotData) {
		// if next operation is End -> return ture
		// else return false
        String tempNodeStack = lotData.getNodeStack();
        String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
        int count = arrNodeStack.length;
        
		boolean endOperFlag = false;

		String checkSql = "SELECT NODEID, UPPER(NODETYPE) NODETYPE FROM NODE WHERE NODEID = ( "
				+ " SELECT A.TONODEID FROM NODE N, ARC A "
				+ " WHERE N.NODEID = A.FROMNODEID "
				+ " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME "
				+ " AND N.PROCESSFLOWVERSION = A.PROCESSFLOWVERSION "
				+ " AND N.NODEID = :NODEID "
				+ " AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) ";

		Map<String, Object> bindSet = new HashMap<String, Object>();
		bindSet.put("NODEID", arrNodeStack[count-1]);
		bindSet.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindSet.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		List<Map<String, Object>> sqlResult=null;
		try {
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSql, bindSet);
		} catch (Exception e) {
			return endOperFlag;
		}
		
		if (sqlResult!=null &&sqlResult.size() > 0 )
		{
			String nodeType = (String) sqlResult.get(0).get("NODETYPE");
			if ( nodeType.toUpperCase().equals("END") )
			{
				endOperFlag = true;
			}
		}
		return endOperFlag;
    }
	
    private boolean isSameFileJudgeAndReparGrade(Lot lotData) throws CustomException{
    	// 다음 공정 정보를 가져와서
    	// FileJudgeSetting 기준 정보 찾고
    	// 다음공정의 RepariGradeFlag == lotData의 FileJudge와 같다면 true 틀리면 false
    	try {
			ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			
			FileJudgeSetting fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {lotData.getFactoryName() ,lotData.getProcessFlowName(),lotData.getProcessFlowVersion(), nextOperationData.getKey().getProcessOperationName(),nextOperationData.getKey().getProcessOperationVersion() });
			
			if(StringUtils.equals(fileJudgeSetting.getRepairGradeFlag(), lotData.getUdfs().get("FILEJUDGE"))){
				return true;
			}
			else{
				return false;
			}
			
		} catch (CustomException e) {
			throw new CustomException("COMMON-0001","Error isSameFileJudgeAndReparGrade");
		}
    }
    
    private String getNextOperationName( Lot lotData ) throws CustomException
    {	
    	try {
    		ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
    		return nextOperationData.getKey().getProcessOperationName();
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Error getNextNextOperationName");
		}
    }
    
    private boolean getForceRepairFlagIsY(Lot lotData) throws CustomException{
    	// 다음 공정 정보를 가져와서
    	// FileJudgeSetting 기준 정보 찾고
    	// 만일 ForceRepairFlag ==Y true;
    	try {
			ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			FileJudgeSetting fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] { lotData.getFactoryName(),lotData.getProcessFlowName(),lotData.getProcessFlowVersion(), nextOperationData.getKey().getProcessOperationName(),nextOperationData.getKey().getProcessOperationVersion() });
			
			if(StringUtils.equals(fileJudgeSetting.getForceRepairFlag(), "Y")){
				return true;
			}
			else{
				return false;
			}
			
		} catch (CustomException e) {
			throw new CustomException("COMMON-0001","Error getForceRepairFlagIsY");
		}
    }
    
    private Lot Release(Lot lot, String reasonCode) throws CustomException
    {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", "SYS", "", "", "");
    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
		
		try
		{
			// 2019.04.25_hsryu_Modify Logic. Add select "HOLDTIMEKEY". Requested by Report.
			String strSql = 
				"    SELECT A.LOTNAME  " +
				"    , A.PROCESSOPERATIONNAME  "+ 
				"    , A.CARRIERNAME  "+
				"    , A.PRODUCTQUANTITY  "+ 
				"    , A.LOTHOLDSTATE  "+
				"    , A.LOTSTATE  "+
				"    , A.PRODUCTSPECNAME  "+ 
				"    , A.ECCODE  "+
				"    , A.PROCESSFLOWNAME  "+ 
				"    , A.PRODUCTREQUESTNAME  "+ 
				"    , TO_CHAR(B.EVENTTIME,'YYYY-MM-DD HH24:MI:SS') HOLDTIME  "+ 
				"    , B.EVENTUSER HOLDUSER  "+ 
				"    , B.SEQ "+ 
				"    , B.EVENTNAME HOLDEVENTNAME  "+ 
				"    , B.REASONCODE HOLDREASONCODE  "+ 
				"    , B.DEPARTMENT HOLDDEPT    "+
				"    , B.EVENTCOMMENT HOLDNOTE  "+ 
				"    , B.EVENTTIMEKEY HOLDTIMEKEY  "+
				"  FROM  LOT A  "+
				"   INNER JOIN CT_LOTMULTIHOLD B ON A.LOTNAME = B.LOTNAME   "+ 
				" WHERE A.LOTNAME = :LOTNAME  "+
				"  AND B.REASONCODE = :REASONCODE  "; 
				   
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lot.getKey().getLotName());
			bindMap.put("REASONCODE", reasonCode);
	
			List<Map<String, Object>> HoldList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
			
			if(HoldList != null && HoldList.size() > 0)
			{
				String  sLOTNAME = lot.getKey().getLotName();  
				String  sSeq = HoldList.get(0).get("SEQ").toString();
				String  sProcessoperationName = (String)HoldList.get(0).get("PROCESSOPERATIONNAME");
				String  sReleaseUser = eventInfo.getEventUser();
				String  sReleaseDept = "SYS";
				String  sCarrierName = (String)HoldList.get(0).get("CARRIERNAME"); 
				String  sProductQuantity = HoldList.get(0).get("PRODUCTQUANTITY").toString(); 
				
				String  sLotholdState = (String)HoldList.get(0).get("LOTHOLDSTATE");  
				String  sLotState = (String)HoldList.get(0).get("LOTSTATE");
				String  sProductspecName = (String)HoldList.get(0).get("PRODUCTSPECNAME");
				String  sEcCode = (String)HoldList.get(0).get("ECCODE"); 
				String  sProcessflowName = (String)HoldList.get(0).get("PROCESSFLOWNAME");
				String  sProductrequestName = (String)HoldList.get(0).get("PRODUCTREQUESTNAME");

				String  sReleaseNote = "";
				String  sHoldTime = (String)HoldList.get(0).get("HOLDTIME") +".000";
				
				String  sHoldUser = (String)HoldList.get(0).get("HOLDUSER"); 
				String  sHoldeventName = (String)HoldList.get(0).get("HOLDEVENTNAME");
				String  sHoldreasonCode = reasonCode;  
				String  sHoldDept = (String)HoldList.get(0).get("HOLDDEPT");
				String  sHoldNote = (String)HoldList.get(0).get("HOLDNOTE");
				String  sHoldTimeKey = (String)HoldList.get(0).get("HOLDTIMEKEY");
				
				eventInfo.setEventComment(sHoldNote);

				String sql = "INSERT INTO CT_LOTMULTIHOLDRELEASE "
						+ " (LOTNAME, SEQ, PROCESSOPERATIONNAME, RELEASETIME, RELEASEUSER, RELEASEDEPT, CARRIERNAME, PRODUCTQUANTITY,HOLDTIME"
						+ "  ,LOTHOLDSTATE, LOTSTATE, PRODUCTSPECNAME, ECCODE, PROCESSFLOWNAME, PRODUCTREQUESTNAME"
						+ "  ,RELEASENOTE, HOLDUSER, HOLDEVENTNAME, HOLDREASONCODE, HOLDDEPT, HOLDNOTE "
						+ "  ,LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,LASTEVENTCOMMENT, HOLDTIMEKEY "
						+ "  )"
 						+ " VALUES "
						+ " (:lotName, :seq, :processoperationName, :releaseTime, :releaseUser, :releaseDept, :carrierName, :productQuantity, :holdTime"
						+ "  ,:lotholdState, :lotState, :productspecName, :ecCode, :processflowName, :productrequestName "
						+ "  ,:releaseNote, :holdUser, :holdeventName, :holdreasonCode, :holdDept, :holdNote "
						+ "  ,:lasteventName, :lasteventTimekey, :lasteventTime, :lasteventUser, :lasteventCommet, :holdTimeKey "
						+ "  ) "; 
 				
				Map<String,Object> InsertbindMap = new HashMap<String,Object>();
	
				InsertbindMap.put("lotName", sLOTNAME);
				
				InsertbindMap.put("seq", sSeq);
				InsertbindMap.put("processoperationName", sProcessoperationName);
				InsertbindMap.put("releaseTime", eventInfo.getEventTime());
				InsertbindMap.put("releaseUser", sReleaseUser);
				InsertbindMap.put("releaseDept", sReleaseDept);
				InsertbindMap.put("carrierName", sCarrierName);
				InsertbindMap.put("productQuantity", sProductQuantity);
				InsertbindMap.put("holdTime", TimeStampUtil.getTimestamp(sHoldTime ));
				
				InsertbindMap.put("lotholdState", sLotholdState);
				InsertbindMap.put("lotState", sLotState);
				InsertbindMap.put("productspecName", sProductspecName);
				InsertbindMap.put("ecCode", sEcCode);
				InsertbindMap.put("processflowName", sProcessflowName);
				InsertbindMap.put("productrequestName", sProductrequestName);
				
				InsertbindMap.put("releaseNote", sReleaseNote);	
				InsertbindMap.put("holdUser", sHoldUser);
				InsertbindMap.put("holdeventName", sHoldeventName);
				InsertbindMap.put("holdreasonCode", sHoldreasonCode);
				InsertbindMap.put("holdDept", sHoldDept);
				InsertbindMap.put("holdNote", sHoldNote);
	
				InsertbindMap.put("lasteventName", eventInfo.getEventName());
				InsertbindMap.put("lasteventTimekey", eventInfo.getEventTimeKey());
				InsertbindMap.put("lasteventUser", eventInfo.getEventUser()) ;
				InsertbindMap.put("lasteventCommet", eventInfo.getEventComment());
				InsertbindMap.put("lasteventTime", eventInfo.getEventTime());
				InsertbindMap.put("holdTimeKey", sHoldTimeKey);
						
				GenericServiceProxy.getSqlMesTemplate().update(sql, InsertbindMap);
			}
		}
		catch (Exception ex)
		{
			log.error("Error Insert CT_LOTMULTIHOLDRELEASE");
			writeErrorNote(lot, "Error Insert CT_LOTMULTIHOLDRELEASE");
			throw new CustomException("COMMON-0001","Insert CT_lotmultiholdRelease Error");
		}
		
    	try {
    		
    		MESLotServiceProxy.getLotServiceImpl().removeMultiHoldLot(lot.getKey().getLotName(), reasonCode, "SYS","1", eventInfo);
			// delete product multi hold data
			List <Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lot.getKey().getLotName());
			for(Product eleProduct : productList) {
				try	{
					MESProductServiceProxy.getProductServiceImpl().removeMultiHoldProduct(eleProduct.getKey().getProductName(), reasonCode, "SYS", "1",eventInfo);
				} catch(Exception ex) {
					//throw new CustomException("COMMON-0001","remove CT_lotmultihold Error");
					log.info(eleProduct.getKey().getProductName() +"removeMultiHoldProduct Error");
				}
			}
			
			// 2019.04.10_hsryu_Add Logic. Requested by Report.
			String HoldDuration = GetLotHoldDuration(eventInfo,lot.getKey().getLotName());
			
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lot);
			
			Map<String, String> udfs = new HashMap<String,String>();
			udfs.put("NOTE", "");
			// 2019.04.10_hsryu_Add Logic. Requested by Report. Delete HoldTime & Insert HoldDuration.
			udfs.put("HOLDTIME",StringUtils.EMPTY);
			udfs.put("HOLDDURATION", HoldDuration);
			udfs.put("HOLDRELEASETIME",eventInfo.getEventTime().toString());
		
			eventInfo.setEventComment(eventInfo.getEventComment() + "/ lotName : " + lot.getKey().getLotName()+" reasonCode : " +reasonCode+" department : " +" "+" holdUser : " + "SYS");
			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lot, productUSequence, udfs);
			
			return lot = LotServiceProxy.getLotService().makeNotOnHold(lot.getKey(),	eventInfo, makeNotOnHoldInfo);
			//lot.setLotHoldState("N");
			//LotServiceProxy.getLotService().update(lot);
			
			// 2019.04.10_hsryu_Insert Logic. Requested by Report. 
//			lot = LotServiceProxy.getLotService().selectByKeyForUpdate(lot.getKey());
//			lot.getUdfs().put("HOLDDURATION", "");
//			lot.getUdfs().put("HOLDRELEASETIME", "");
//			LotServiceProxy.getLotService().update(lot);
		}
    	catch (Exception e) {
    		log.error("removeMultiHoldLot ~ makeNotOnHold Error");
    		log.error(lot.getKey().getLotName() + "Release Hold Error");
    		writeErrorNote(lot, lot.getKey().getLotName() + "Release Hold Error");
    		throw new CustomException("COMMON-0001","Error removeMultiHoldLot");
		}	
    }
    
    private void getMachineNameByTPEFOM(Lot lotData) throws CustomException
    {
		String strSql = 
				" SELECT CNT, " +
						"         MACHINENAME, " +
						"         MACHINERECIPENAME RECIPENAME, " +
						"         PHOTOMASK MASKNAME " +
						"    FROM (SELECT 1 CNT, " +
						"                 T.MACHINENAME, " +
						"                 P.MACHINERECIPENAME, " +
						"                 P.PHOTOMASK, " +
						"                 FACTORYNAME, " +
						"                 PRODUCTSPECNAME, " +
						"                 PRODUCTSPECVERSION, " +
						"                 ECCODE, " +
						"                 PROCESSFLOWNAME, " +
						"                 PROCESSFLOWVERSION, " +
						"                 PROCESSOPERATIONNAME, " +
						"                 PROCESSOPERATIONVERSION " +
						"            FROM TPEFOMPOLICY T " +
						"	            INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID  " +
						"           WHERE     1 = 1 " +
						"                 AND ( (T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*')) " +
						"                 AND (   (T.PRODUCTSPECNAME = :PRODUCTSPECNAME) " +
						"                      OR (T.PRODUCTSPECNAME = '*')) " +
						"                 AND (   (T.PRODUCTSPECVERSION = '00001') " +
						"                      OR (T.PRODUCTSPECVERSION = '*')) " +
						"                 AND (   (T.PROCESSFLOWNAME = :PROCESSFLOWNAME) " +
						"                      OR (T.PROCESSFLOWNAME = '*')) " +
						"                 AND (   (T.PROCESSFLOWVERSION = '00001') " +
						"                      OR (T.PROCESSFLOWVERSION = '*')) " +
						"                 AND (   (T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) " +
						"                      OR (T.PROCESSOPERATIONNAME = '*')) " +
						"                 AND (   (T.PROCESSOPERATIONVERSION = '00001') " +
						"                      OR (T.PROCESSOPERATIONVERSION = '*')) " +
						"                 AND ( (T.ECCODE = :ECCODE) OR (T.ECCODE = '*')) " +
						"                 AND ( (T.MACHINENAME IN " +
						"                           (SELECT MACHINENAME " +
						"                              FROM CT_MACHINEGROUPMACHINE " +
						"                             WHERE MACHINEGROUPNAME = " +
						"                                      (SELECT MACHINEGROUPNAME " +
						"                                         FROM PROCESSOPERATIONSPEC " +
						"                                        WHERE PROCESSOPERATIONNAME = " +
						"                                                 :PROCESSOPERATIONNAME)))) " +
						"          UNION " +
						"          SELECT 9999 CNT, " +
						"                 M.MACHINENAME, " +
						"                 P.MACHINERECIPENAME, " +
						"                 P.PHOTOMASK, " +
						"                 FACTORYNAME, " +
						"                 PRODUCTSPECNAME, " +
						"                 PRODUCTSPECVERSION, " +
						"                 ECCODE, " +
						"                 PROCESSFLOWNAME, " +
						"                 PROCESSFLOWVERSION, " +
						"                 PROCESSOPERATIONNAME, " +
						"                 PROCESSOPERATIONVERSION " +
						"            FROM TPEFOMPOLICY T " +
						"            	INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID, " +
						"                 ((SELECT MACHINENAME " +
						"                     FROM CT_MACHINEGROUPMACHINE " +
						"                    WHERE MACHINEGROUPNAME = " +
						"                             (SELECT MACHINEGROUPNAME " +
						"                                FROM PROCESSOPERATIONSPEC " +
						"                               WHERE PROCESSOPERATIONNAME = " +
						"                                        :PROCESSOPERATIONNAME))) M " +
						"           WHERE     1 = 1 " +
						"                 AND ( (T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*')) " +
						"                 AND (   (T.PRODUCTSPECNAME = :PRODUCTSPECNAME) " +
						"                      OR (T.PRODUCTSPECNAME = '*')) " +
						"                 AND (   (T.PRODUCTSPECVERSION = '00001') " +
						"                      OR (T.PRODUCTSPECVERSION = '*')) " +
						"                 AND (   (T.PROCESSFLOWNAME = :PROCESSFLOWNAME) " +
						"                      OR (T.PROCESSFLOWNAME = '*')) " +
						"                 AND (   (T.PROCESSFLOWVERSION = '00001') " +
						"                      OR (T.PROCESSFLOWVERSION = '*')) " +
						"                 AND (   (T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) " +
						"                      OR (T.PROCESSOPERATIONNAME = '*')) " +
						"                 AND (   (T.PROCESSOPERATIONVERSION = '00001') " +
						"                      OR (T.PROCESSOPERATIONVERSION = '*')) " +
						"                 AND ( (T.ECCODE = :ECCODE) OR (T.ECCODE = '*')) " +
						"                 AND (T.MACHINENAME = '*')) " +
						"ORDER BY CNT, " +
						"         DECODE (FACTORYNAME, '*', 9999, 0), " +
						"         DECODE (PRODUCTSPECNAME, '*', 9999, 0), " +
						"         DECODE (PRODUCTSPECVERSION, '*', 9999, 0), " +
						"         DECODE (ECCODE, '*', 9999, 0), " +
						"         DECODE (PROCESSFLOWNAME, '*', 9999, 0), " +
						"         DECODE (PROCESSFLOWVERSION, '*', 9999, 0), " +
						"         DECODE (PROCESSOPERATIONNAME, '*', 9999, 0), " +
						"         DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0), " +
						"         DECODE (MACHINENAME, '*', 9999, 0), " +
						"         MACHINENAME " ; 
				   
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("ECCODE", lotData.getUdfs().get("ECCODE"));
		List<Map<String, Object>> sqlResult=null;
		try {
			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Error setRecipe");
		}

		if(sqlResult != null && sqlResult.size() > 0)
		{
			lotData.setMachineRecipeName((String)sqlResult.get(0).get("RECIPENAME"));
			lotData.setMachineName((String)sqlResult.get(0).get("MACHINENAME"));
		}
    }
    

    
    private String getMachineNameByMachineGroup(Lot lotData) throws CustomException{
    	
    	String strSql = "SELECT MG.MACHINENAME " +
    			"  FROM CT_MACHINEGROUPMACHINE MG " +
    			" WHERE MG.MACHINEGROUPNAME = " +
    			"          (SELECT MACHINEGROUPNAME " +
    			"             FROM PROCESSOPERATIONSPEC O " +
    			"            WHERE O.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) " ;
				   
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

		List<Map<String, Object>> sqlResult=null;
		try {
			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		} catch (Exception e) {
			throw new CustomException("COMMON-0001","Error setRecipe");
		}

		if(sqlResult != null && sqlResult.size() > 0)
		{
			return (String)sqlResult.get(0).get("MACHINENAME");
		}
    	
		return "A2DUM010";
    }
    
    private Lot TrackOut(Lot lotData, String beforeProcessFlowName, String beforeProcessOperationName, EventInfo eventInfo) throws CustomException {
    	Document doc= createProductList(lotData);
    	List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(lotData.getMachineName(), "1");
    	
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOutForOPI(lotData, productList);
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
   		boolean aHoldFlag = false;
   		
   		aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(lotData.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);
		
		Lot afterTrackOutLot = null;
		try {
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, lotData, portData, lotData.getCarrierName(), "", lotData.getMachineName(), "",productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, "", aHoldFlag,null);
		
			// 2019.05.06_hsryu_Move Logic. 
			// 2019.03.11_add Logic. set ProcessFlag. 
	        /* Array 20180807, Add [Process Flag Update] ==>> */            
//	        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, afterTrackOutLot, logicalSlotMap, true);
	        /* <<== Array 20180807, Add [Process Flag Update] */

		} catch (Exception e) {
			log.error( lotData.getKey().getLotName() +" TrackOut Error ");
			writeErrorNote(lotData, lotData.getKey().getLotName() +" TrackOut Error ");
			throw new CustomException("COMMON-0001",e.toString());
		}
 		return afterTrackOutLot;
    }
    private Lot TrackIn(Lot lotData) throws CustomException{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", "SYS","Timer TrackIn", null, null);
    	trackOutEventInfo.setEventTime(eventInfo.getEventTime());
    	trackOutEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

		getMachineNameByTPEFOM(lotData);
   	
    	String machineName = lotData.getMachineName();
    	String machineRecipeName =lotData.getMachineRecipeName();
    	if(StringUtils.isEmpty(machineName)){
    		machineName = getMachineNameByMachineGroup(lotData);
    	}
    	
    	List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotData.getKey().getLotName());
		
    	Map<String, String> lotUdfs = new HashMap();
    	Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, "1");
    	
		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
		lotUdfs.put("NOTE", "");
		lotUdfs.put("HOLDDURATION", "");
		lotUdfs.put("HOLDRELEASETIME", "");

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, machineRecipeName, productCSequence,lotUdfs);
		Lot afterTrackInLot;
		try {
			afterTrackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLotForOPI(eventInfo, lotData, makeLoggedInInfo);
		} catch (CustomException e) {
			log.error(e.toString());
			log.error( lotData.getKey().getLotName() +" TrackIn Error ");
			writeErrorNote(lotData, lotData.getKey().getLotName() +" TrackIn Error ");
			throw new CustomException("COMMON-0001",e.toString());
		}

    	return afterTrackInLot;
    }
    private Document createProductList(Lot lotData) throws CustomException{
    	Document doc = new Document();
    	
    	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    	List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
    	List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
    	
    	try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "CreateProductRequest", "", "", "MES", "Auto Create Product Request");
			
			Element element1 = new Element("PRODUCTLIST");
			
			for (Product product : productList) 
			{
				Element tproduct = new Element("PRODUCT");
				
				Element elementS1 = new Element("PRODUCTNAME");
				elementS1.setText(product.getKey().getProductName());
				tproduct.addContent(elementS1);
				
				Element elementS2 = new Element("POSITION");
				elementS2.setText(String.valueOf( product.getPosition() ) );
				tproduct.addContent(elementS2);
				
				Element elementS3 = new Element("PRODUCTJUDGE");
				elementS3.setText("");
				tproduct.addContent(elementS3);
				
				Element elementS4 = new Element("SECONDARYGRADE");
				elementS4.setText("");
				tproduct.addContent(elementS4);
				
				Element elementS5 = new Element("VCRPRODUCTNAME");
				elementS5.setText("");
				tproduct.addContent(elementS5);
				
				Element elementS6 = new Element("PROCESSINGINFO");
				if( StringUtil.equals(product.getUdfs().get("PROCESSINGINFO"), "P") ){
					product.getUdfs().put("PROCESSFLAG", "Y");
					elementS6.setText("P");
				}
				else
				{
					product.getUdfs().put("PROCESSFLAG", "N");
					elementS6.setText("W");
				}
				
				tproduct.addContent(elementS6);
				
				element1.addContent(tproduct);
				ProductServiceProxy.getProductService().update(product);
			}
    		
			eleBodyTemp.addContent(element1);

    		} catch (Exception ex) {
    			log.error("Create Doc Error");
    			writeErrorNote(lotData, "Create Doc Error");
    			throw new CustomException("Create Doc Error");
    		}
    	
    	return doc;
    }
    private void writeErrorNote(Lot lot,String note){
    	if(!StringUtils.equals(note, lot.getUdfs().get("NOTE"))){    		
    		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ErrorNote", "SYS","", null, null);
    		kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
    		setEventInfo.getUdfs().put("NOTE", note);
    		LotServiceProxy.getLotService().setEvent(lot.getKey(), eventInfo, setEventInfo);
    	}
    }

    private void setNoteEmptyProductList(Lot lotData){
    	// TrackOut 후에 Lot의 ProductList의 Note Set Empty
    	// InspectionResult시 note에 Error 를 기록
    	// TrackOut시 Note 컬럼 삭제
    	// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    	List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
    	List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
		
    	// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//    	for(Product product : productList){
//			product.getUdfs().put("NOTE", "");
//			ProductServiceProxy.getProductService().update(product);
//		}
    	Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		for(Product product : productList){
			ProductServiceProxy.getProductService().update(product);
		}
    }
    
    //2019.01.19_hsryu_Requested by hongwei.
    private boolean checkOtherHold(Lot lot, String reasonCode)
    {
    	boolean otherHoldFlag = false;
    	
    	String strSql = 
    			"    SELECT  LOTNAME, REASONCODE, DEPARTMENT, EVENTCOMMENT  " +
    					"   FROM CT_LOTMULTIHOLD   "+ 
    					" WHERE LOTNAME = :LOTNAME  "+
    					"  AND REASONCODE <> :REASONCODE  "; 

    	Map<String, Object> bindMap = new HashMap<String, Object>();
    	bindMap.put("LOTNAME", lot.getKey().getLotName());
    	bindMap.put("REASONCODE", reasonCode);

    	List<Map<String, Object>> HoldList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

    	if(HoldList != null && HoldList.size() > 0)
    	{
    		log.info("Exist OtherHold! not execute Logic. LotName : " + lot.getKey().getLotName());

    		otherHoldFlag = true;
    	}
    	return otherHoldFlag;
    }
    
    private void setProcessFlag(Lot lotData, EventInfo eventInfo) throws CustomException {
		
    	/**** 2019.05.06_hsryu_Insert Logic. not in TrackOut Function. ****/
		Durable carrierData = null;
		String logicalSlotMap = "";

		try{
			carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		}
		catch(Throwable e){
			throw new CustomException("COMMON-0001",e.toString());
		}
		
		try{
	        logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMapInfo(carrierData);
		}
		catch(Throwable e){
			log.warn("Fail LogicalSlotMap!");
		}
		
		// 2019.03.11_add Logic. set ProcessFlag. 
        /* Array 20180807, Add [Process Flag Update] ==>> */            
        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, lotData, logicalSlotMap, true);
        /* <<== Array 20180807, Add [Process Flag Update] */
        /******************************************************************/
    }
    
	private String GetLotHoldDuration(EventInfo eventInfo, String lotName) 
	{
		try
		{
 			String sql = "SELECT ROUND((TO_DATE(:EVENTTIME, :EVENTTIMEFORMAT) - HOLDTIME)*24*60*60,2) HOLDDURATION"
					   + "  FROM LOT"
					   + " WHERE LOTNAME = :LOTNAME"; 
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);
			bindMap.put("EVENTTIME",  eventInfo.getEventTime().toString().substring(0,19));
			bindMap.put("EVENTTIMEFORMAT",  "yyyy-MM-dd HH24:mi:ss");

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			String sDuration = "";
				
			if(sqlResult != null && sqlResult.size() > 0)
			{
				sDuration = sqlResult.get(0).get("HOLDDURATION").toString();
			}
			else
			{
				sDuration = "0";
			}
			
			return sDuration;
		}
		catch (Exception ex)
		{
			log.warn("GetLotHoldDuration ERROR");		
			return "0";
		}
	}
}
