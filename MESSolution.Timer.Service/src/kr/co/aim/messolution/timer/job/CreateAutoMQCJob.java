package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

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
 * @author Administrator
 *
 */
public class CreateAutoMQCJob implements Job, InitializingBean, ApplicationContextAware
{
	//Equal Factory QTimer
    private static Log log = LogFactory.getLog(CreateAutoMQCJob.class);
    
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
//				doAutoMQCJob();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//	                log.error(e.errorDef.getLoc_errorMessage());
//			} catch (Exception e) {
//				log.warn(e);
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
    			
    			doAutoMQCJob();

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
    
    private void doAutoMQCJob() throws CustomException {
    	EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateAutoMqcJob", "SYS", "", "", "");
		List<AutoMQCSetting> autoMQCSettingList = null;
		
		try {
			autoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(" CARRIERNAME IS NULL ", new Object[]{});	
		} catch (Exception e) {
			log.info(e);
		}
		
		if (autoMQCSettingList != null && autoMQCSettingList.size()> 0) {
			log.info("Create Auto MQC Start");
			for(AutoMQCSetting autoMQCSetting : autoMQCSettingList)
			{
				boolean createFlag =false;
				String processOperationName = autoMQCSetting.getProcessOperationName();
				String machineName = autoMQCSetting.getMachineName();
				String mqcTemplateName = autoMQCSetting.getMqcTemplateName();
				double mqcValue = autoMQCSetting.getMqcValue();
				String mqcValidFlag = autoMQCSetting.getMqcValidFlag();
				
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				// mqcValidFlag==N 이거나 Machine State가 Idle이 아닐 경우
				// continue
				if(StringUtils.equals("N", mqcValidFlag)  || !StringUtils.equals("IDLE",machineData.getMachineStateName() ))
				{
					continue;
				}
				else
				{
					// NowTime -  LastRuntime > autoMQCSetting.getMqcValue()
					// AutoMQC 생성

					// Get Last TrackOutTime
					// 해당 머신이 TrackIn 하거나, TrackOut 할때, LastRunTime을 Update해주는 로직 필요 
					long lastTrackOuttime =autoMQCSetting.getLastRunTime().getTime();
					// Get Now time
					long nowTime = TimeStampUtil.getCurrentTimestamp().getTime();
					long diff = (nowTime - lastTrackOuttime)/1000; // 두 시간 사이의 초 구하기
					
					// Modified by smkang on 2018.12.01 - According to Jiang Haiying's request, time unit is changed hour to minute.
					//long value = ((long)mqcValue) * 60 * 60; // mqcValue(hours)를 초로 환산
					long value = ((long)mqcValue) * 60; // mqcValue(minite)을 초으로 환산

					if(diff >value){
						log.info("Create Auto Value Check ok");
						// Create Auto MQC
						MQCTemplate mqcTemplate = ExtendedObjectProxy.getMQCTemplateService().selectByKey(false, new Object[]{mqcTemplateName});
						
						// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//						String condition = "PRODUCTIONTYPE = ? AND LOTNAME NOT IN (SELECT LOTNAME FROM CT_MQCJOB J WHERE J.MQCSTATE = ?) AND LOTSTATE = ?";
						String condition = "PRODUCTIONTYPE = ? AND LOTNAME NOT IN (SELECT LOTNAME FROM CT_MQCJOB J WHERE J.MQCSTATE = ?) AND LOTSTATE = ? FOR UPDATE";

						Object[] bindSet = new Object[] {"MQCA", "Executing","Completed"};
						List<Lot> lotList = LotServiceProxy.getLotService().select(condition, bindSet);
						
						for(Lot lot : lotList){
							//Validation
							if(validation(mqcTemplate,lot)){
								log.info(lot.getKey().getLotName()+" Validation Ok");
								try {
									createMQCJob(lot,mqcTemplate,eventInfo,processOperationName,machineName,autoMQCSetting);
									log.info(lot.getKey().getLotName()+"Create Auto MQC Sucess");
									createFlag=true;
									break;
								} catch (Exception e) {
									log.info(lot.getKey().getLotName()+"Create Auto MQC Fail");
									continue;
								}
							}//if
						}//for
						if(createFlag==false){
							HashMap<String,String> map = setHashMap(autoMQCSetting);
							LotServiceUtil.sendByCreateAlarm(eventInfo,"AUTOMQC",map);
						}
					}
				}
			}// for 전체 AutoMQCSetting List 
		} // else AutoMQCSetting List가 하나라도 존재한다면,
    }
    
    private HashMap<String, String> setHashMap(AutoMQCSetting autoMQCSetting){
    	HashMap<String,String> returnValue = new HashMap<String, String>();
    	returnValue.put("MACHINENAME", autoMQCSetting.getMachineName());
    	
    	returnValue.put("PROCESSOPERATIONNAME", autoMQCSetting.getProcessOperationName());
    	returnValue.put("PRODUCTSPECNAME", autoMQCSetting.getProductSpecName());
    	returnValue.put("ECCODE", autoMQCSetting.getEcCode());
    	returnValue.put("TEMPLATENAME", autoMQCSetting.getMqcTemplateName());
    	
    	return returnValue;
    }
    
    private boolean validation(MQCTemplate mqcTemplate, Lot lotData)
	{
		List<Product> productList =null;
		try {
			productList= ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
		} catch (Exception e) {
			return false;
		}
		// RequiredQuantity Validation
		// 현재 로직은 mqcTemplate 에 등록된 Position 과 완전히 같은 Product가 존재할경우만 
		// AutoMQC 를 생성한다.
		
		int requiredQuantity=0;
		String sql = "SELECT DISTINCT POSITION " +
				"  FROM CT_MQCTEMPLATEPOSITION " +
				" WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME " ;
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MQCTEMPLATENAME", mqcTemplate.getmqcTemplateName());
		List<Map<String, Object>> positionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		for(int j = 0; j < positionList.size(); j++)
		{
			requiredQuantity++;
		}
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey(lotData.getFactoryName(),lotData.getProcessFlowName(),lotData.getProcessFlowVersion());
		ProcessFlow processFlow =  ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		int processFlowRequiredQuantity;
		try {
			processFlowRequiredQuantity = Integer.parseInt( processFlow.getUdfs().get("MQCREQUIREDSHEETCOUNT") );
		} catch (Exception e) {
			processFlowRequiredQuantity=Integer.MAX_VALUE;
		}
		
		if(processFlowRequiredQuantity>requiredQuantity){
			return false;
		}
		
		List<MQCTemplatePosition> mqcTemplatePositionList = null;
		String condition = " where MQCTEMPLATENAME = ? AND PROCESSFLAG = ? ORDER BY POSITION";
		Object[] bindSet = new Object[] {mqcTemplate.getmqcTemplateName(), "1"};
		
		try {
			mqcTemplatePositionList = ExtendedObjectProxy.getMQCTemplatePositionService().select(condition, bindSet);
		} catch (Exception e) {
			return false;
		}
		
		// product 테이블의 Count 비교
		for(MQCTemplatePosition mqcTemplatePosition: mqcTemplatePositionList)
		{
			try {
				int countUp = (int)mqcTemplatePosition.getmqcCountUp();
				String position = String.valueOf(mqcTemplatePosition.getposition());
				boolean ExistFlag =false;
				for(Product product : productList)
				{
					if(StringUtils.equals(position, String.valueOf( product.getPosition())))
					{
						// if Over MQC Count return false
						if(countUp + Integer.parseInt(product.getUdfs().get("MQCCOUNT")) >Integer.parseInt(product.getUdfs().get("MQCLIMITCOUNT")))
						{
							return false;
						}
						// if Not Equals productSpecName != template.SpecName
						if(!StringUtil.equals(mqcTemplate.getproductspecname(), product.getUdfs().get("MQCUSEPRODUCTSPEC"))){
							return false;
						}
						// if Not Equals ECCODE != template.ECCODE
						if(!StringUtil.equals(mqcTemplate.geteccode(), product.getUdfs().get("MQCUSEECCODE"))){
							return false;
						}
						// if Equals ProductGrade And "S"
						if(StringUtils.equals("S", product.getProductGrade())){
							return false;
						}
						ExistFlag=true;
						break;
					}
				}
				if(ExistFlag==false){
					return false;
				}
				
			}
			catch (Exception e) {
				return false;
			}
			  
		}
		
		return true;
	}
	private void createMQCJob(Lot lotData, MQCTemplate mqcTemplate, EventInfo eventInfo,String processOperationName, String machineName,AutoMQCSetting autoMQCSetting) throws CustomException
	{
		log.info(lotData.getKey().getLotName() + "createMQCJob Start");
		List<MQCTemplatePosition> mqcTemplatePositionList = null;
		String firstProcessOperationName="";
		MQCJob mqcJob = null;
		MQCJobOper mqcJobOper=null;
		String mqcJobName="";
		List<Product> productList =null;
		try {
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			productList= ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
			productList= MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
		} catch (Exception e) {
			throw new CustomException("COMMON-0001", "Not Exist ProductList");
		}
		
		if(StringUtils.isEmpty(lotData.getCarrierName()))
		{
			throw new CustomException("MQC-0028", "");
		}
		
		String strSql = "SELECT DISTINCT MQCTEMPLATENAME,  " +
				"       PROCESSOPERATIONNAME,  " +
				"       PROCESSOPERATIONVERSION " +
				"  FROM CT_MQCTEMPLATEPOSITION  " +
				" WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME  ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MQCTEMPLATENAME", mqcTemplate.getmqcTemplateName());

		List<Map<String, Object>> mqcTemplatePositionInfo = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(mqcTemplatePositionInfo == null || mqcTemplatePositionInfo.size() == 0)
		{
			throw new CustomException("MQC-0027", "");
		}
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotData.getKey().getLotName());
		nameRuleAttrMap.put("CARRIERNAME", lotData.getCarrierName());
		
		List<String> TempmqcJobName = CommonUtil.generateNameByNamingRule("MQCJobNaming", nameRuleAttrMap, 1);
		mqcJobName = TempmqcJobName.get(0);
		
		try
		{
			mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
		}
		catch (Exception ex)
		{
			mqcJob = null;
		}
		
		if(mqcJob != null)
		{
			throw new CustomException("MQC-0029", mqcJobName);
		}
		
		mqcJob = new MQCJob(mqcJobName);
		mqcJob.setlotName(lotData.getKey().getLotName());
		mqcJob.setcarrierName(lotData.getCarrierName());
		mqcJob.setfactoryName(lotData.getFactoryName());
		mqcJob.setprocessFlowName(mqcTemplate.getprocessFlowName());
		mqcJob.setprocessFlowVersion("00001");
		mqcJob.setmqcTemplateName(mqcTemplate.getmqcTemplateName());
		mqcJob.setmqcState("Executing");
		mqcJob.setLastEventUser(eventInfo.getEventUser());
		mqcJob.setLastEventComment(eventInfo.getEventComment());
		mqcJob.setLastEventTime(eventInfo.getEventTime());
		mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
		mqcJob.setLastEventName(eventInfo.getEventName());
		mqcJob.setmqcuseproductspec(mqcTemplate.getproductspecname());
		mqcJob.setAutoMqcFlag("Y");
		
		ExtendedObjectProxy.getMQCJobService().create(eventInfo, mqcJob);
		
		
		String sql = "SELECT   " +
				"A.PROCESSOPERATIONNAME,  " +
				"A.MACHINEGROUPNAME  " +
				"FROM   " +
				"(SELECT NODE.NUM,  " +
				"       PO.FACTORYNAME,  " +
				"       PO.PROCESSOPERATIONNAME,  " +
				"       PO.PROCESSOPERATIONVERSION,  " +
				"       PO.DESCRIPTION PROCESSOPERATIONDESCRIPTION,  " +
				"       PF.PROCESSFLOWTYPE,  " +
				"       PF.PROCESSFLOWNAME,  " +
				"       PF.PROCESSFLOWVERSION,  " +
				"       PO.MACHINEGROUPNAME,  " +
				"       MG.DESCRIPTION MACHINEGROUPDESCRIPTION  " +
				"  FROM PROCESSOPERATIONSPEC PO, PROCESSFLOW PF, MACHINEGROUP MG,  " +
				"       (SELECT ROWNUM NUM, N.NODEID, N.NODETYPE, N.NODEATTRIBUTE1, N.PROCESSFLOWNAME  " +
				"              FROM ARC A, NODE N  " +
				"             WHERE     1 = 1  " +
				"                   AND N.NODEID = A.FROMNODEID  " +
				"                   AND N.PROCESSFLOWNAME IN  " +
				"                   (SELECT PROCESSFLOWNAME  " +
				"                   FROM CT_PRODUCTSPECPOSSIBLEPF   " +
				"                   WHERE FACTORYNAME = :FACTORYNAME  " +
				"                   AND PRODUCTSPECNAME = :PRODUCTSPECNAME  " +
				"                   AND ECCODE = :ECCODE)  " +
				"        START WITH N.NODETYPE = 'Start'  " +
				"        CONNECT BY NOCYCLE     A.FROMNODEID = PRIOR A.TONODEID  " +
				"                           AND A.ARCTYPE != 'Otherwise'  " +
				"                           AND (   A.ARCATTRIBUTE IS NULL  " +
				"                                OR A.ARCATTRIBUTE = 'Y')) NODE  " +
				" WHERE     1 = 1  " +
				"       AND NODE.nodetype = 'ProcessOperation'  " +
				"       AND NODE.NODEATTRIBUTE1 = PO.PROCESSOPERATIONNAME  " +
				"       AND PO.FACTORYNAME = :FACTORYNAME  " +
				"       AND PF.PROCESSFLOWNAME = NODE.PROCESSFLOWNAME  " +
				"       AND PO.MACHINEGROUPNAME = MG.MACHINEGROUPNAME(+)  " +
				"       ORDER BY NODE.NUM) A  " ;
		bindMap.clear();
		bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", mqcTemplate.getfactoryName());
		bindMap.put("PRODUCTSPECNAME", mqcTemplate.getproductspecname());
		bindMap.put("ECCODE", mqcTemplate.geteccode());

		List<Map<String, Object>> mqcTemplateOperationList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		firstProcessOperationName = (String)mqcTemplateOperationList.get(0).get("PROCESSOPERATIONNAME");
		
		if(mqcTemplateOperationList!=null && mqcTemplateOperationList.size()>0)
		{
			for( int i=0; i< mqcTemplateOperationList.size(); i++) // Operation List
			{
				String mqcProcessOperationName = (String)mqcTemplateOperationList.get(i).get("PROCESSOPERATIONNAME");
				mqcJobOper = new MQCJobOper(mqcJobName, mqcProcessOperationName, "00001");
				
				mqcJobOper.setmachineGroupName((String)mqcTemplateOperationList.get(i).get("MACHINEGROUPNAME"));
				if(StringUtil.equals(processOperationName, mqcProcessOperationName)){
					mqcJobOper.setmachineName(machineName);
				}

				mqcJobOper.setLastEventUser(eventInfo.getEventUser());
				mqcJobOper.setLastEventComment(eventInfo.getEventComment());
				mqcJobOper.setLastEventTime(eventInfo.getEventTime());
				mqcJobOper.setLastEventTimeKey(eventInfo.getEventTimeKey());
				mqcJobOper.setLastEventName(eventInfo.getEventName());
				
				ExtendedObjectProxy.getMQCJobOperService().create(eventInfo, mqcJobOper);
				
				String condition = " where MQCTEMPLATENAME=? AND PROCESSOPERATIONNAME = ? ORDER BY POSITION";
				Object[] bindSet = new Object[] {mqcTemplate.getmqcTemplateName(), mqcProcessOperationName};
				
				try {
					mqcTemplatePositionList = ExtendedObjectProxy.getMQCTemplatePositionService().select(condition, bindSet);
				} catch (Exception e) {
					log.info(lotData.getKey().getLotName() + "TemplatePosition Select Error");
					throw new CustomException("COMMON-0001", "TemplatePosition Select Error");
				}
				
				for(MQCTemplatePosition mqcTemplatePosition :  mqcTemplatePositionList)
				{
					String strSqlProduct = "SELECT PRODUCTNAME " +
							"  FROM PRODUCT " +
							" WHERE     LOTNAME = :LOTNAME " +
							"       AND POSITION = :POSITION " +
							"       AND FACTORYNAME = :FACTORYNAME ";
					
					Map<String, Object> bindMapProduct = new HashMap<String, Object>();
					bindMapProduct.put("LOTNAME", lotData.getKey().getLotName());
					bindMapProduct.put("FACTORYNAME", lotData.getFactoryName());
					bindMapProduct.put("POSITION", mqcTemplatePosition.getposition());
					
					List<Map<String, Object>> productData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlProduct, bindMapProduct);
					
					if(productData == null)
					{
						continue;
					}
					
					MQCJobPosition newMQCJobPosition = new MQCJobPosition(mqcJobName, 
							mqcTemplatePosition.getprocessOperationName(), 
							mqcTemplatePosition.getprocessOperationVersion(), 
							mqcTemplatePosition.getposition());
					
					newMQCJobPosition.setproductName(productData.get(0).get("PRODUCTNAME").toString());
					newMQCJobPosition.setrecipeName(mqcTemplatePosition.getrecipeName());
					newMQCJobPosition.setmqcCountUp(mqcTemplatePosition.getmqcCountUp());
					newMQCJobPosition.setLastEventUser(eventInfo.getEventUser());
					newMQCJobPosition.setLastEventComment(eventInfo.getEventComment());
					newMQCJobPosition.setLastEventTime(eventInfo.getEventTime());
					newMQCJobPosition.setLastEventTimeKey(eventInfo.getEventTimeKey());
					newMQCJobPosition.setLastEventName(eventInfo.getEventName());
					
					ExtendedObjectProxy.getMQCJobPositionService().create(eventInfo, newMQCJobPosition);
				}
				
			}
		}
		
		lotData.setProductionType("MQCA");
		lotData.setProductRequestName("");
		lotData.setProcessOperationName(firstProcessOperationName);
		lotData.setProcessOperationVersion("");
		lotData.setLotState("Released");
		lotData.setLotHoldState("N");
		lotData.setLotProcessState("WAIT");
		lotData.setProcessFlowName(mqcTemplate.getprocessFlowName());
		lotData.getUdfs().put("ENDBANK", "");
		lotData.getUdfs().put("ECCODE", mqcTemplate.geteccode());
		lotData.getUdfs().put("NOTE", "MQC JOB Name : "+mqcJobName);
		LotServiceProxy.getLotService().update(lotData);
		
		for(Product product: productList){
			product.setProductType("MQCA");
			product.setProductRequestName("");
			product.setProcessOperationName("-");
			product.setProcessOperationVersion("");
			
			product.getUdfs().put("ECCODE", mqcTemplate.geteccode());
			
			product.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
			product.setProductState("InProduction");
			product.setProductHoldState("N");

			product.setProductRequestName("");
			product.setProcessFlowName(mqcTemplate.getprocessFlowName());
			ProductServiceProxy.getProductService().update(product);
		}
		
		String TempProductSpec = mqcTemplate.getproductspecname();
		
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());

		productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
		
		//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
				lotData.getProductionType(), TempProductSpec, lotData.getProductSpecVersion(),
				"", "",
				"",
				lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(),
				lotData.getFactoryName(), lotData.getAreaName(), "Released", "WAIT", "N",
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), firstProcessOperationName, 
				GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, "", "", "", "", "", lotData.getUdfs(), productUdfs, false);

		lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		
		String condition = " WHERE MQCJOBNAME = ? AND PROCESSOPERATIONNAME = ?  ";
		Object[] bindSet = new Object[]{ mqcJobName ,firstProcessOperationName };
		List<MQCJobPosition> mqcJobPositionList = null;
		try {
			mqcJobPositionList = ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
			if(mqcJobPositionList == null || mqcJobPositionList.size()==0){
				throw new CustomException("COMMON-0001", "MQCCountUp = 1 Product not Exist!");
			}
		} catch (Exception e) {
			throw new CustomException("COMMON-0001", "MQCCountUp = 1 Product not Exist!");
		}
		

		for (Product product : productList) 
		{		
			String productName = product.getKey().getProductName();
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			productData.getUdfs().put("PROCESSFLAG", "N");
			for(MQCJobPosition mqcJobPosition : mqcJobPositionList){
				if(StringUtils.equals(mqcJobPosition.getproductName(), productName)){
					productData.getUdfs().put("PROCESSFLAG", "Y");
					break;
				}
			}
            productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
            productData.setProductState("InProduction");
            productData.setProductHoldState("N");
            ProductServiceProxy.getProductService().update(productData);
            
            String pCondition = " where productname=? and timekey= ? " ;
            Object[] pBindSet = new Object[]{productData.getKey().getProductName(),eventInfo.getEventTimeKey()};
            
            ProductHistoryKey keyInfo = new ProductHistoryKey();
            keyInfo.setProductName(productData.getKey().getProductName());
            keyInfo.setTimeKey(eventInfo.getEventTimeKey());
            
            // Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(keyInfo);
            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(keyInfo);
            
            producthistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
            producthistory.setProductState("InProduction");
            producthistory.setProductHoldState("N");
            ProductServiceProxy.getProductHistoryService().update(producthistory);
		}
		autoMQCSetting.setCarrierName(lotData.getCarrierName());
		ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
		
		// 2019.06.05_hsryu_Add Logic. Check BHold. Requested by CIM.
		lotData = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lotData.getKey().getLotName(), eventInfo);
		
		// Note Column Clear
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		lotData.getUdfs().put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
	}
}