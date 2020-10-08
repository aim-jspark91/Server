package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.data.ScheduleJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.timer.ScheduleJobFactory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

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

public class ERPWorkOrderTimer implements Job, InitializingBean, ApplicationContextAware
{	
	private static Log log = LogFactory.getLog(ERPWorkOrderTimer.class);
	
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
//			try {
//				log.info(String.format("Job[%s] START", this.getClass().getName()));
//				
//				ERPWorkOrderTimer();
//				
//				log.info(String.format("Job[%s] END", this.getClass().getName()));
//			} catch (CustomException e) {
//				if (log.isDebugEnabled())
//					log.error(e.errorDef.getLoc_errorMessage());
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
	
	/**
	 * real-time ERP WorkOrder time monitor
	 * @author xzquan
	 * @since 2016.11.15
	 * @throws CustomException
	 */
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
			String seq	= workOrder.get("SEQ").toString();			
			String tradeType	=  getMapValue(workOrder, "TRADETYPE");
			String mto =  getMapValue(workOrder, "MTO");
			String rohs =  getMapValue(workOrder, "ROHS");
			String saleOrder =  getMapValue(workOrder, "SALEORDER");
			
			String infoResultStatus =  getMapValue(workOrder, "INFORESULTSTATUS");
			
			if(infoResultStatus.equals("C"))
			{
				if(factroyName.equals("ARRAY") || factroyName.equals("CF") || factroyName.equals("LTPS"))
				{
					//Send Create ProductRequest to CNM
					Document doc = writeCreateProductRequestMessage(factroyName, productRequestName, seq, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime);
					
					String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
					GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
				}				
			}
//			else if(infoResultStatus.equals("M"))
//			{
//				ProductRequestKey pKey = new ProductRequestKey();
//				pKey.setProductRequestName(productRequestName);
//								
//				ProductRequest  productRequestDate = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//				
//				ReleaseHoldWorkOrder(productRequestDate);
//				
//				//Send Change ProductRequest to CNM
//				Document doc = writeChangeProductRequestMessage(factroyName, productRequestName, seq, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime);
//				
//				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
//				GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
//			}
//			else if(infoResultStatus.equals("F"))
//			{
//				ProductRequestKey pKey = new ProductRequestKey();
//				pKey.setProductRequestName(productRequestName);
//								
//				ProductRequest  productRequestDate = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//				
//				ReleaseHoldWorkOrder(productRequestDate);
//				
//				//Send Delete ProductRequest to CNM
//				Document doc = writeFinishProductRequestMessage(factroyName, productRequestName, seq, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime);
//				
//				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
//				GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
//			}
//			else if(infoResultStatus.equals("D"))
//			{
//				ProductRequestKey pKey = new ProductRequestKey();
//				pKey.setProductRequestName(productRequestName);
//								
//				ProductRequest  productRequestDate = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//				
//				ReleaseHoldWorkOrder(productRequestDate);
//				
//				//Send Delete ProductRequest to CNM
//				Document doc = writeDeleteProductRequestMessage(factroyName, productRequestName, seq, productSpecName, productRequestType, planQuantity, tradeType, saleOrder, mto, rohs, planFinishedTime, planReleasedTime);
//				
//				String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
//				GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "CNXSender");
//			}
//			else if(infoResultStatus.equals("H"))
//			{		
//				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", "MES", "Auto Hold Product Request", null, null);
//				
//				//Hold ProductRequest
//				ProductRequestKey pKey = new ProductRequestKey();
//				pKey.setProductRequestName(productRequestName);
//								
//				ProductRequest  productRequestDate = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
//				
//				HashMap<String, String> udfs = new HashMap<String, String>();
//				
//				ChangeSpecInfo changespecInfo = new ChangeSpecInfo();
//				changespecInfo.setFactoryName(productRequestDate.getFactoryName());
//				changespecInfo.setProductSpecName(productRequestDate.getProductSpecName());
//				changespecInfo.setProductSpecVersion(productRequestDate.getProductSpecVersion());
//				changespecInfo.setProductRequestType(productRequestDate.getProductRequestType());
//				changespecInfo.setPlanQuantity(Long.valueOf(productRequestDate.getPlanQuantity()));
//				changespecInfo.setProductRequestState(productRequestDate.getProductRequestState());
//				changespecInfo.setPlanReleasedTime(productRequestDate.getPlanReleasedTime());
//				changespecInfo.setPlanFinishedTime(productRequestDate.getPlanFinishedTime());
//				changespecInfo.setProductRequestHoldState("Y");				
//				
//				changespecInfo.setUdfs(udfs);
//
//				ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().changeSpec(pKey, eventInfo, changespecInfo);
//				
//				// Add History				
//				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData);
//				
//				//1) Update ERPProductRequest Receive Flag
//				if(!StringUtils.isEmpty(seq))
//				{
//					ProductRequestServiceUtil.SetERPReceiveFlag(seq, productRequestName);
//				}
//				
//				//2) Insert MESProductRequest WO Data
//				ProductRequestServiceUtil.WriteMESProductRequest(productRequestName);
//			}			
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

	private Document writeCreateProductRequestMessage(String factoryName, String productRequestName, String seq,
			String productSpecName, String productRequestType, String planQuantity, String tradeType, String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "CreateProductRequest", "", "", "MES",
					"Auto Create Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("SEQ");
			element3.setText(seq);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTSPECNAME");
			element4.setText(productSpecName);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PRODUCTREQUESTTYPE");
			element5.setText(productRequestType);
			eleBodyTemp.addContent(element5);

			Element element7 = new Element("PLANQUANTITY");
			element7.setText(planQuantity);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("TRADETYPE");
			element8.setText(tradeType);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("SALEORDER");
			element9.setText(saleOrder);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("MTO");
			element10.setText(mto);
			eleBodyTemp.addContent(element10);

			Element element11 = new Element("ROHS");
			element11.setText(rohs);
			eleBodyTemp.addContent(element11);

			Element element12 = new Element("PLANFINISHEDTIME");
			element12.setText(planFinishedTime);
			eleBodyTemp.addContent(element12);

			Element element13 = new Element("PLANRELEASEDTIME");
			element13.setText(planReleasedTime);
			eleBodyTemp.addContent(element13);

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}	
	
	private Document writeChangeProductRequestMessage(String factoryName, String productRequestName, String seq,
			String productSpecName, String productRequestType, String planQuantity, String tradeType, String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "ChangeProductRequest", "", "", "MES",
					"Auto Change Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("SEQ");
			element3.setText(seq);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTSPECNAME");
			element4.setText(productSpecName);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PRODUCTREQUESTTYPE");
			element5.setText(productRequestType);
			eleBodyTemp.addContent(element5);

			Element element7 = new Element("PLANQUANTITY");
			element7.setText(planQuantity);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("TRADETYPE");
			element8.setText(tradeType);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("SALEORDER");
			element9.setText(saleOrder);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("MTO");
			element10.setText(mto);
			eleBodyTemp.addContent(element10);

			Element element11 = new Element("ROHS");
			element11.setText(rohs);
			eleBodyTemp.addContent(element11);

			Element element12 = new Element("PLANFINISHEDTIME");
			element12.setText(planFinishedTime);
			eleBodyTemp.addContent(element12);

			Element element13 = new Element("PLANRELEASEDTIME");
			element13.setText(planReleasedTime);
			eleBodyTemp.addContent(element13);
			
			Element element14 = new Element("OUTSOURCE");
			element14.setText("");
			eleBodyTemp.addContent(element14);
			
			Element element15 = new Element("CONFIRMSTATE");
			element15.setText("");
			eleBodyTemp.addContent(element15);
			
			Element element16 = new Element("PROCESSFLOWNAME");
			element16.setText("");
			eleBodyTemp.addContent(element16);
			
			Element element17 = new Element("AUTOSHIPFLAG");
			element17.setText("");
			eleBodyTemp.addContent(element17);

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}
	
	private Document writeDeleteProductRequestMessage(String factoryName, String productRequestName, String seq,
			String productSpecName, String productRequestType, String planQuantity, String tradeType, String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "DeleteProductRequest", "", "", "MES",
					"Auto Change Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("SEQ");
			element3.setText(seq);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTSPECNAME");
			element4.setText(productSpecName);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PRODUCTREQUESTTYPE");
			element5.setText(productRequestType);
			eleBodyTemp.addContent(element5);

			Element element7 = new Element("PLANQUANTITY");
			element7.setText(planQuantity);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("TRADETYPE");
			element8.setText(tradeType);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("SALEORDER");
			element9.setText(saleOrder);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("MTO");
			element10.setText(mto);
			eleBodyTemp.addContent(element10);

			Element element11 = new Element("ROHS");
			element11.setText(rohs);
			eleBodyTemp.addContent(element11);

			Element element12 = new Element("PLANFINISHEDTIME");
			element12.setText(planFinishedTime);
			eleBodyTemp.addContent(element12);

			Element element13 = new Element("PLANRELEASEDTIME");
			element13.setText(planReleasedTime);
			eleBodyTemp.addContent(element13);
			
			Element element14 = new Element("OUTSOURCE");
			element14.setText("");
			eleBodyTemp.addContent(element14);
			
			Element element15 = new Element("CONFIRMSTATE");
			element15.setText("");
			eleBodyTemp.addContent(element15);
			
			Element element16 = new Element("PROCESSFLOWNAME");
			element16.setText("");
			eleBodyTemp.addContent(element16);
			
			Element element17 = new Element("AUTOSHIPFLAG");
			element17.setText("");
			eleBodyTemp.addContent(element17);

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}
	
	private Document writeFinishProductRequestMessage(String factoryName, String productRequestName, String seq,
			String productSpecName, String productRequestType, String planQuantity, String tradeType, String saleOrder,
			String mto, String rohs, String planFinishedTime, String planReleasedTime) throws CustomException {
		
		Document doc = new Document();

		try {
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			doc = SMessageUtil.createXmlDocument(eleBodyTemp, "FinishProductRequest", "", "", "MES",
					"Auto Finish Product Request");
			
			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("PRODUCTREQUESTNAME");
			element2.setText(productRequestName);
			eleBodyTemp.addContent(element2);

			Element element3 = new Element("SEQ");
			element3.setText(seq);
			eleBodyTemp.addContent(element3);

			Element element4 = new Element("PRODUCTSPECNAME");
			element4.setText(productSpecName);
			eleBodyTemp.addContent(element4);

			Element element5 = new Element("PRODUCTREQUESTTYPE");
			element5.setText(productRequestType);
			eleBodyTemp.addContent(element5);

			Element element7 = new Element("PLANQUANTITY");
			element7.setText(planQuantity);
			eleBodyTemp.addContent(element7);

			Element element8 = new Element("TRADETYPE");
			element8.setText(tradeType);
			eleBodyTemp.addContent(element8);

			Element element9 = new Element("SALEORDER");
			element9.setText(saleOrder);
			eleBodyTemp.addContent(element9);

			Element element10 = new Element("MTO");
			element10.setText(mto);
			eleBodyTemp.addContent(element10);

			Element element11 = new Element("ROHS");
			element11.setText(rohs);
			eleBodyTemp.addContent(element11);

			Element element12 = new Element("PLANFINISHEDTIME");
			element12.setText(planFinishedTime);
			eleBodyTemp.addContent(element12);

			Element element13 = new Element("PLANRELEASEDTIME");
			element13.setText(planReleasedTime);
			eleBodyTemp.addContent(element13);
			
			Element element14 = new Element("OUTSOURCE");
			element14.setText("");
			eleBodyTemp.addContent(element14);
			
			Element element15 = new Element("CONFIRMSTATE");
			element15.setText("");
			eleBodyTemp.addContent(element15);
			
			Element element16 = new Element("PROCESSFLOWNAME");
			element16.setText("");
			eleBodyTemp.addContent(element16);
			
			Element element17 = new Element("AUTOSHIPFLAG");
			element17.setText("");
			eleBodyTemp.addContent(element17);

			// overwrite
			doc.getRootElement().addContent(eleBodyTemp);
		} catch (Exception ex) {
			log.error(ex);
		}

		return doc;
	}
	
	/**
	 * Get Work Order Info from ERP Interface table
	 * @author XZ
	 * @since 2016.11.16
	 * @param 
	 * @throws CustomException
	 */
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
		sql.append("      FROM CT_ERPPRODUCTREQUEST E  ");
		sql.append("     WHERE 1 = 1   ");
		sql.append("           AND (E.RECEIVEFLAG IS NULL OR E.RECEIVEFLAG <> :RECEIVEFLAG)  ");
		sql.append("  ORDER BY E.SEQ  ");

		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("RECEIVEFLAG", "Y");
		
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
	
	private void ReleaseHoldWorkOrder(ProductRequest productRequestDate)
			throws CustomException
	{		
		try 
		{
			if (productRequestDate.getProductRequestHoldState().equals("Y")) 
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", "MES", "Auto ReleaseHold Product Request", null, null);
				
				//ReleaseHold ProductRequest			
				HashMap<String, String> udfs = new HashMap<String, String>();						
				
				ChangeSpecInfo changespecInfo = new ChangeSpecInfo();
				changespecInfo.setFactoryName(productRequestDate.getFactoryName());
				changespecInfo.setProductSpecName(productRequestDate.getProductSpecName());
				changespecInfo.setProductSpecVersion(productRequestDate.getProductSpecVersion());
				changespecInfo.setProductRequestType(productRequestDate.getProductRequestType());
				changespecInfo.setPlanQuantity(Long.valueOf(productRequestDate.getPlanQuantity()));
				changespecInfo.setProductRequestState(productRequestDate.getProductRequestState());
				changespecInfo.setPlanReleasedTime(productRequestDate.getPlanReleasedTime());
				changespecInfo.setPlanFinishedTime(productRequestDate.getPlanFinishedTime());
				changespecInfo.setProductRequestHoldState("N");				
				
				changespecInfo.setUdfs(udfs);

				ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().changeSpec(productRequestDate.getKey(), eventInfo, changespecInfo);
				
				// Add History				
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);			
			}
			else 
			{
				return;
			}			
		} 
		catch (Exception e) {
			// TODO: handle exception
		}		
	}
}