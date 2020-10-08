package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class StartMQCJob extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element mqcJobList = SMessageUtil.getBodySequenceItem(doc, "MQCJOBLIST", true);
		// modify by JHIYING on 20190813 mantis:4442 start
	    String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
	    String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		// modify by JHIYING on 20190813 mantis:4442 end
	    
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		
		Map<String, Lot> lotDataMap = new ConcurrentHashMap<String, Lot>();
		for (Iterator iteratorLotList = mqcJobList.getChildren().iterator(); iteratorLotList.hasNext();)
		{
			Element mqcJobE = (Element) iteratorLotList.next();
			String mqcJobName = SMessageUtil.getChildText(mqcJobE, "MQCJOBNAME", true);
			
			MQCJob mqcJob = null;
			try {
				mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
			} catch (Exception ex) {
			}

			if (mqcJob == null)
				throw new CustomException("MQC-0031", mqcJobName);

			if (StringUtils.equals(mqcJob.getmqcState(), "Executing"))
				throw new CustomException("MQC-0041", mqcJobName);
			
			lotDataMap.put(mqcJob.getlotName(), LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(mqcJob.getlotName())));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		
		for (Iterator iteratorLotList = mqcJobList.getChildren().iterator(); iteratorLotList.hasNext();)
		{
			Element mqcJobE = (Element) iteratorLotList.next();

			String factoryName = SMessageUtil.getChildText(mqcJobE, "FACTORYNAME", true);
			String mqcJobName = SMessageUtil.getChildText(mqcJobE, "MQCJOBNAME", true);
			// modify by jhiying on20190814 mantis:4442 start
			// EventInfo eventInfo = EventInfoUtil.makeEventInfo("MqcStart", this.getEventUser(), this.getEventComment(), "", "");
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("MqcStart", this.getEventUser(), this.getEventComment(), "MQCSTART", reasonCode);
			eventInfo.setBehaviorName("ARRAY");

			MQCJob mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
			
			// 각각의 공정에서 모든 Product 가 Scrap 되어있다면, 에러 발생
			MESLotServiceProxy.getLotServiceImpl().checkMQCJobOfStartMQCJob(mqcJob);

			mqcJob.setmqcState("Executing");
			mqcJob.setLastEventComment(eventInfo.getEventComment());
			mqcJob.setLastEventTime(eventInfo.getEventTime());
			mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mqcJob.setLastEventName(eventInfo.getEventName());
		

			ExtendedObjectProxy.getMQCJobService().modify(eventInfo, mqcJob);

			Lot lotData = lotDataMap.get(mqcJob.getlotName());
			
			// 2019.02.28
			if(!StringUtil.equals(lotData.getProcessOperationName(),"-"))
			{
				throw new CustomException("MQC-0054");
			}
			if(!StringUtil.equals(lotData.getLotState(),"Completed"))
			{
				throw new CustomException("MQC-0054");
			}
			
			String FirstOperationName = this.getFirstOperationName(mqcJob.getprocessFlowName(), factoryName);
			if(StringUtils.isEmpty(FirstOperationName))
			{
				throw new CustomException("MQC-0058"); 
			}
			
			String MQCJobEccode = this.getECCode(mqcJob.getmqcuseproductspec(),mqcJob.getprocessFlowName(),factoryName);
			if(StringUtils.isEmpty(MQCJobEccode))
			{
				throw new CustomException("MQC-0058"); 
			}
			
			String MQCProductName = "";
			String sql = "SELECT DISTINCT PRODUCTNAME FROM CT_MQCJOBPOSITION WHERE MQCJOBNAME = :MQCJOBNAME AND MQCCOUNTUP = 1 ORDER BY PRODUCTNAME";
			Map<String, String> newbindMap = new HashMap<String, String>();
			newbindMap.put("MQCJOBNAME", mqcJobName);				
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
			if(newsqlResult != null && newsqlResult.size() > 0)
			{
				for(int i=0; i< newsqlResult.size(); i++)
				{
					if(StringUtils.isEmpty(MQCProductName))
					{
						MQCProductName = newsqlResult.get(i).get("PRODUCTNAME").toString();
					}
					else
					{
						MQCProductName = MQCProductName + " , " + newsqlResult.get(i).get("PRODUCTNAME").toString();
					}
				}
			}
							
			Map<String, String> udfs = lotData.getUdfs();
			udfs.put("ENDBANK", "");
			// modify by jhiying on20190813 mantis:4442 End
			udfs.put("NOTE",   note +",  MQC JOB Name : " + mqcJobName + " ,  MQCProductName : " + MQCProductName);
			udfs.put("ECCODE", MQCJobEccode);
			
			LotServiceProxy.getLotService().update(lotData);
		
			if(StringUtils.equals(lotData.getLotHoldState(), "Y"))
			{
				throw new CustomException("MQC-0042", lotData.getKey().getLotName());
			}
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());

			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			for (ProductU product : productUdfs) {
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();
				productsUdfs.put("ECCODE", MQCJobEccode);
			}
			//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
					"MQCA", mqcJob.getmqcuseproductspec(), lotData.getProductSpecVersion(),
					"", "",
					"",
					lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(),
					factoryName, lotData.getAreaName(), "Released", "WAIT", "N",
					mqcJob.getprocessFlowName(), "", FirstOperationName, 
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, "", "", "", "", "", lotData.getUdfs(), productUdfs, false);

			lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			
			/*
			 * checkMQCJobOperationListAndProcessFlowOperationList
			 * MQCJob 의 ProcessFlow 와 ProcessFlow 의 공정이 다르면 Start 불가
			 * */
			MESLotServiceProxy.getLotServiceImpl().checkMQCJobOperationListAndProcessFlowOperationList(mqcJobName, factoryName, lotData.getProcessFlowName());
			
			String condition = " WHERE MQCJOBNAME = ? AND PROCESSOPERATIONNAME = ?  ";
			Object[] bindSet = new Object[]{ mqcJobName ,FirstOperationName };
			List<MQCJobPosition> mqcJobPositionList = null;
			try {
				mqcJobPositionList = ExtendedObjectProxy.getMQCJobPositionService().select(condition, bindSet);
				if(mqcJobPositionList == null || mqcJobPositionList.size()==0){
					throw new CustomException("COMMON-0001", "Product not Exist!");
				}
			} catch (Exception e) {
				throw new CustomException("COMMON-0001", "Product not Exist!");
			}
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
			
			for (Product productData : pProductList) 
			{
				boolean processFlag =false;
				productData.getUdfs().put("PROCESSFLAG", "N");
				for(MQCJobPosition mqcJobPosition : mqcJobPositionList){
					if(StringUtils.equals(mqcJobPosition.getproductName(), productData.getKey().getProductName())){
						productData.getUdfs().put("PROCESSFLAG", "Y");
						processFlag=true;
						break;
					}
				}
                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                productData.setProductState("InProduction");
                productData.setProductHoldState("N");
                ProductServiceProxy.getProductService().update(productData);
                
                ProductHistoryKey productHistoryKey = new ProductHistoryKey();
                productHistoryKey.setProductName(productData.getKey().getProductName());
                productHistoryKey.setTimeKey(productData.getLastEventTimeKey());

                // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
                // ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
                ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);

                productHistory.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                productHistory.setProductState("InProduction");
                productHistory.setProductHoldState("N");
                if(processFlag){
                	productHistory.getUdfs().put("PROCESSFLAG", "Y");
                }else{
                	productHistory.getUdfs().put("PROCESSFLAG", "N");
                }
                
                ProductServiceProxy.getProductHistoryService().update(productHistory);
			}
			
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(mqcJob.getlotName());
			
			// 2019.06.05_hsryu_Add Logic. Check BHold. Requested by CIM. Mantis 0004122.
			lotData = MESLotServiceProxy.getLotServiceUtil().checkBHoldAndOperHold(lotData.getKey().getLotName(), eventInfo);
			
			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
			// Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			// Map<String, String> udfs_note = lotData_Note.getUdfs();
			// udfs_note.put("NOTE", "");
			// LotServiceProxy.getLotService().update(lotData_Note);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		}
		
		return doc;
	}
	
	public String getFirstOperationName(String FlowName, String factoryName) throws CustomException
	{
		String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
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
		Map<String, String> newbindMap = new HashMap<String, String>();
		newbindMap.put("PROCESSFLOWNAME", FlowName);
		newbindMap.put("FACTORYNAME", factoryName);
		newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
		
		if (newsqlResult.size() > 0) 
		{
			for(int k=0; k< newsqlResult.size(); k++)
			{
				if(!StringUtil.equals(newsqlResult.get(k).get("NODETYPE").toString(), "Start"))
				{
					return newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString();
				}
			}
		}

		return "";
	}
	
	public String getECCode(String Spec, String Flow, String factoryName)
	{
		String sql = "SELECT PP.ECCODE FROM CT_PRODUCTSPECPOSSIBLEPF PP WHERE PP.FACTORYNAME = :FACTORYNAME AND PP.PRODUCTSPECNAME = :PRODUCTSPECNAME AND PP.PROCESSFLOWNAME = :PROCESSFLOWNAME";
		Map<String, String> newbindMap = new HashMap<String, String>();
		newbindMap.put("PRODUCTSPECNAME", Spec);
		newbindMap.put("PROCESSFLOWNAME", Flow);
		newbindMap.put("FACTORYNAME", factoryName);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
		
		if (newsqlResult.size() > 0) 
		{
			if(newsqlResult.get(0).get("ECCODE") != null)
			{
				return newsqlResult.get(0).get("ECCODE").toString();
			}
		}
		
		return "";
	}
}