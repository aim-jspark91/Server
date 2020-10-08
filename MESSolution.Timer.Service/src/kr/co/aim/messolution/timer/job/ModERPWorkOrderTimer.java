package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;

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

public class ModERPWorkOrderTimer implements Job, InitializingBean, ApplicationContextAware {
	private static Log log = LogFactory.getLog(ModERPWorkOrderTimer.class);
	
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
//	        	ERPWorkOrderTimer();
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
				
	        	ERPWorkOrderTimer();
				
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

	public void ERPWorkOrderTimer() throws CustomException
	{
		//1. Get Not Received from ERP Interface table.
		List<ListOrderedMap> ERPWorkOrderList = this.getERPWorkOrderList();
		
		if(ERPWorkOrderList.size() <= 0) return;
		
		//2. Make WO
		for(ListOrderedMap workOrder : ERPWorkOrderList )
		{
			//Get WO info
			String productRequestName	= workOrder.get("PRODUCTREQUESTNAME").toString();
			String productSpecName = workOrder.get("PRODUCTSPECNAME").toString();
			String factroyName = workOrder.get("FACTORYNAME").toString();
			String productRequestType = "P";
			String planReleasedTime = workOrder.get("PLANRELEASEDTIME").toString();
			String planFinishedTime	= workOrder.get("PLANFINISHEDTIME").toString();
			String planQuantity = workOrder.get("PLANQUANTITY").toString();
			//String seq	= workOrder.get("SEQ").toString();
			
			String tradeType	=  getMapValue(workOrder, "TRADETYPE");
			String mto =  getMapValue(workOrder, "MTO");
			String rohs =  getMapValue(workOrder, "ROHS");
			String saleOrder =  getMapValue(workOrder, "SALEORDER");
			
			String seq =  getMapValue(workOrder, "SEQ");
			
			String infoResultStatus =  getMapValue(workOrder, "INFORESULTSTATUS");
			
			if(infoResultStatus.equals("C"))
			{
				//Send Create ProductRequest to CNM
				Document doc = writeCreateProductRequestMessage(factroyName, productRequestName, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime,seq);
				
				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
				
				GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
			}
			else if(infoResultStatus.equals("M"))
			{
				//Send Chane ProductRequest to CNM
				Document doc = writeChangeProductRequestMessage(factroyName, productRequestName, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime,seq);
				
				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
				GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
			}
		}
	}

	private String getMapValue(ListOrderedMap map, String key)
	{
		String text = "";
		
		try
		{
			text = map.get(key).toString();
		}
		catch (Exception ex) {
			log.error(ex);
		}
		
		return text;
	}

	private Document writeCreateProductRequestMessage(String factoryName, String productRequestName,
			String productSpecName, String productRequestType, String planQuantity, String tradeType, String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime,String seq) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "CreateWorkOrderByMOD", "", "", "MES",
					"Auto Create Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("PRODUCTSPECNAME");
			element3.setText(productSpecName);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTREQUESTTYPE");
			element4.setText(productRequestType);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PLANQUANTITY");
			element5.setText(planQuantity);
			eleBodyTemp.addContent(element5);

			Element element6 = new Element("DOMESTICEXPORT");
			element6.setText(tradeType);
			eleBodyTemp.addContent(element6);

			Element element7 = new Element("MTO");
			element7.setText(mto);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("ROHS");
			element8.setText(rohs);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("PLANFINISHEDTIME");
			element9.setText(planFinishedTime);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("PLANRELEASEDTIME");
			element10.setText(planReleasedTime);
			eleBodyTemp.addContent(element10);
			
			Element element11 = new Element("SALEORDER");
			element11.setText(saleOrder);
			eleBodyTemp.addContent(element11);
			
			Element element12 = new Element("SEQ");
			element12.setText(seq);
			eleBodyTemp.addContent(element12);

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}
	
	
	private Document writeChangeProductRequestMessage(String factoryName, String productRequestName,
			String productSpecName, String productRequestType, String planQuantity, String tradeType,String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime,String seq) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "ChangeWorkOrderByMOD", "", "", "MES",
					"Auto Change Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("PRODUCTSPECNAME");
			element3.setText(productSpecName);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTREQUESTTYPE");
			element4.setText(productRequestType);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PLANQUANTITY");
			element5.setText(planQuantity);
			eleBodyTemp.addContent(element5);

			Element element6 = new Element("DOMESTICEXPORT");
			element6.setText(tradeType);
			eleBodyTemp.addContent(element6);

			Element element7 = new Element("MTO");
			element7.setText(mto);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("ROHS");
			element8.setText(rohs);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("PLANFINISHEDTIME");
			element9.setText(planFinishedTime);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("PLANRELEASEDTIME");
			element10.setText(planReleasedTime);
			eleBodyTemp.addContent(element10);
			
			Element element11 = new Element("CONFIRMSTATE");
			element11.setText("");
			eleBodyTemp.addContent(element11);
			
			Element element12 = new Element("PROCESSFLOWNAME");
			element12.setText("");
			eleBodyTemp.addContent(element12);
			
			Element element13 = new Element("SALEORDER");
			element13.setText(saleOrder);
			eleBodyTemp.addContent(element13);
			
			Element element14 = new Element("SEQ");
			element14.setText(seq);
			eleBodyTemp.addContent(element14);
			

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}
	
	public List<ListOrderedMap> getERPWorkOrderList() throws CustomException
	{                                                     
		StringBuffer sql = new StringBuffer();
		sql.append("    SELECT E.PRODUCTREQUESTNAME,  ");
		sql.append("           E.PRODUCTSPECNAME,  ");
		sql.append("           E.FACTORYNAME,  ");
		sql.append("           E.PRODUCTTYPE,  ");
		sql.append("           E.PRODUCTREQUESTSTATE,  ");
		sql.append("           E.PLANRELEASEDTIME,  ");
		sql.append("           E.PLANFINISHEDTIME,  ");
		sql.append("           E.TRADETYPE,  ");
		sql.append("           E.PRIORITY,  ");
		sql.append("           E.PLANQUANTITY,  ");
		sql.append("           E.PLANLIMITQUANTITY,  ");
		sql.append("           E.MTO,  ");
		sql.append("           E.ROHS,  ");
		sql.append("           E.SALEORDER,  ");
		sql.append("           E.SEQ, ");
		sql.append("           E.INFORESULTSTATUS, ");
		sql.append("           E.INFORESULTMESSAGE ");
		sql.append("      FROM CT_ERPPRODUCTREQUEST@FABMESDEV E  ");
		sql.append("     WHERE 1 = 1   ");
		sql.append("           AND (E.RECEIVEFLAG IS NULL OR E.RECEIVEFLAG <> :RECEIVEFLAG)  ");
		sql.append("           AND E.FACTORYNAME = :FACTORYNAME  ");
		sql.append("  ORDER BY E.SEQ  ");

		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("RECEIVEFLAG", "Y");
		bindMap.put("FACTORYNAME", "MOD");
		
		try
		{
			@SuppressWarnings("unchecked")
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ERPWorkOrder", fe.getMessage());
		}
	}

}
