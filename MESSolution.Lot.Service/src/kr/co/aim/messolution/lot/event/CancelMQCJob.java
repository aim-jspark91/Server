package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
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

public class CancelMQCJob extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element mqcJobList = SMessageUtil.getBodySequenceItem(doc, "MQCJOBLIST", true);

		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		for ( @SuppressWarnings("rawtypes")
//		Iterator iteratorLotList = mqcJobList.getChildren().iterator(); iteratorLotList.hasNext();)
//		{
//			Element mqcJobE = (Element) iteratorLotList.next();
//
//			String factoryName = SMessageUtil.getChildText(mqcJobE, "FACTORYNAME", true);
//			String mqcJobName = SMessageUtil.getChildText(mqcJobE, "MQCJOBNAME", true);
//
//			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelMQCJob", this.getEventUser(), this.getEventComment(), "", "");
//
//			MQCJob mqcJob = null;
//			try
//			{
//				mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
//			}
//			catch (Exception ex)
//			{
//			}
//
//			if(mqcJob == null)
//			{
//				throw new CustomException("MQC-0031", mqcJobName);
//			}
//
//			if(!StringUtils.equals(mqcJob.getmqcState(), "Executing"))
//			{
//				throw new CustomException("MQC-0043", mqcJobName);
//			}
//
//			mqcJob.setmqcState("Wait");
//			mqcJob.setLastEventComment(eventInfo.getEventComment());
//			mqcJob.setLastEventTime(eventInfo.getEventTime());
//			mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
//			mqcJob.setLastEventName(eventInfo.getEventName());
//
//			ExtendedObjectProxy.getMQCJobService().modify(eventInfo, mqcJob);
//
//			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(mqcJob.getlotName());
//
//			if(lotData != null)
//			{
//	            // Added by smkang on 2019.06.05 - According to Cui Yu's request, after StartCSTInfoCheckRequest is succeeded, 
//				//								   CancelMQCJob should be rejected.
//				if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
//					throw new CustomException("LOT-9050", mqcJob.getlotName());
//				
//				if(StringUtils.equals(lotData.getLotHoldState(), "Y"))
//				{
//					throw new CustomException("MQC-0042", lotData.getKey().getLotName());
//				}
//
//				String strSql = "SELECT QL.FACTORYNAME,   " +
//						"         QL.PROCESSOPERATIONNAME,   " +
//						"         PO.DESCRIPTION PROCESSOPERATIONDESC,   " +
//						"         QL.PROCESSFLOWNAME,   " +
//						"         PO.PROCESSOPERATIONTYPE,   " +
//						"         PO.DETAILPROCESSOPERATIONTYPE,   " +
//						"         PO.PROCESSOPERATIONGROUP,   " +
//						"         QL.NODEID   " +
//						"    FROM PROCESSOPERATIONSPEC PO,   " +
//						"         ( SELECT LEVEL LV,   " +
//						"                  FACTORYNAME,   " +
//						"                  PROCESSOPERATIONNAME,   " +
//						"                  PROCESSFLOWNAME,   " +
//						"                  PROCESSFLOWVERSION,   " +
//						"                  NODEID   " +
//						"         FROM (   SELECT    " +
//						"                     N.FACTORYNAME,   " +
//						"                     N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,   " +
//						"                     N.PROCESSFLOWNAME,   " +
//						"                     N.PROCESSFLOWVERSION,   " +
//						"                     N.NODEID,   " +
//						"                     N.NODETYPE,   " +
//						"                     A.FROMNODEID,   " +
//						"                     A.TONODEID   " +
//						"                FROM ARC A,   " +
//						"                     NODE N,   " +
//						"                     PROCESSFLOW PF   " +
//						"               WHERE 1 = 1   " +
//						"                 AND 1=1    " +
//						"                 AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME   " +
//						"                 AND N.FACTORYNAME = :FACTORYNAME   " +
//						"                 AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME   " +
//						"                 AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION   " +
//						"                 AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME   " +
//						"                 AND N.FACTORYNAME = PF.FACTORYNAME   " +
//						"                 AND A.FROMNODEID = N.NODEID)   " +
//						"          START WITH NODETYPE = :NODETYPE   " +
//						"          CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) QL   " +
//						"   WHERE 1 = 1   " +
//						"     AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME   " +
//						"     AND PO.FACTORYNAME = :FACTORYNAME   " +
//						"ORDER BY QL.LV ASC ";
//
//				Map<String, Object> bindMap = new HashMap<String, Object>();
//				bindMap.put("FACTORYNAME", factoryName);
//				bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
//				bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
//
//				List<Map<String, Object>> mqcFirstOperationInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
//
//				if(mqcFirstOperationInfo != null && mqcFirstOperationInfo.size() > 0)
//				{
//					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());
//
//					productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
//
//					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
//					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
//							lotData.getProductionType(), "M-COMMON", lotData.getProductSpecVersion(),
//							"", "",
//							"",
//							lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(),
//							factoryName, lotData.getAreaName(), GenericServiceProxy.getConstantMap().Lot_Completed, "", lotData.getLotHoldState(),
//							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), mqcFirstOperationInfo.get(0).get("PROCESSOPERATIONNAME").toString(), 
//							GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, "", "", "", "", "", lotData.getUdfs(), productUdfs, false);
//
//					eventInfo.setBehaviorName("ARRAY");
//					lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
//					LotServiceProxy.getLotService().update(lotData);
//
//					List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//
//					for (Product product : pProductList) 
//					{			
//						product.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
//						product.setProductProcessState("");
//						//2019.02.18_hsryu_Insert Logic. Mantis 0002606.
//						product.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
//						ProductServiceProxy.getProductService().update(product);
//
//						ProductHistoryKey productHistoryKey = new ProductHistoryKey();
//			            productHistoryKey.setProductName(product.getKey().getProductName());
//			            productHistoryKey.setTimeKey(product.getLastEventTimeKey());	            
//			            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
//
//			            producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
//			            producthistory.setProductProcessState("");
//						//2019.02.18_hsryu_Insert Logic. Mantis 0002606.
//						producthistory.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
//						ProductServiceProxy.getProductHistoryService().update(producthistory);
//					}
//				}
//				
//				// 2018.10.18 Mentis 682
//				String sql = "SELECT EDV.ENUMVALUE FROM ENUMDEF ED JOIN ENUMDEFVALUE EDV ON ED.ENUMNAME = EDV.ENUMNAME WHERE ED.ENUMNAME = :ENUMNAME AND TAG = :TAG";
//				bindMap.clear();
//				bindMap.put("ENUMNAME", "EndBank");
//				bindMap.put("TAG", "MQCA");
//				
//				@SuppressWarnings("unchecked")
//				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//				String NewEndBank = "";
//				if(sqlResult.size() > 0)
//				{
//					NewEndBank = sqlResult.get(0).get("ENUMVALUE").toString();
//				}
//				
//				eventInfo.setEventName("MoveToEndBank");
//				lotData.setProcessOperationName("-");
//				lotData.setLotProcessState("");
//				lotData.setLotHoldState("");
//				Map<String, String> udfs = lotData.getUdfs();
//				udfs.put("ENDBANK", NewEndBank);
//				
//				LotServiceProxy.getLotService().update(lotData);
//				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//				setEventInfo.setUdfs(udfs);
//				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
//				
//				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//				
//				for (Product product : pProductList) 
//				{
//					kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//					product.setProcessOperationName("-");
//                    product.setProcessOperationVersion("");
//                    product.setProductSpecName("M-COMMON");
//                    ProductServiceProxy.getProductService().update(product);
//                    ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
//				}
//				
//				/* 2018-11-20 Add By ParkJeongSu For AutoMQCSetting*/
//				setEmptyCarrerName(lotData.getCarrierName(), eventInfo);
//				/* 2018-11-20 Add By ParkJeongSu For AutoMQCSetting*/
//			}
//		}
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

			if (!StringUtils.equals(mqcJob.getmqcState(), "Executing"))
				throw new CustomException("MQC-0043", mqcJobName);
			
			lotDataMap.put(mqcJob.getlotName(), LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(mqcJob.getlotName())));
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		for (Iterator iteratorLotList = mqcJobList.getChildren().iterator(); iteratorLotList.hasNext();)
		{
			Element mqcJobE = (Element) iteratorLotList.next();
			
			String factoryName = SMessageUtil.getChildText(mqcJobE, "FACTORYNAME", true);
			String mqcJobName = SMessageUtil.getChildText(mqcJobE, "MQCJOBNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelMQCJob", this.getEventUser(), this.getEventComment(), "", "");

			MQCJob mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
			mqcJob.setmqcState("Wait");
			mqcJob.setLastEventComment(eventInfo.getEventComment());
			mqcJob.setLastEventTime(eventInfo.getEventTime());
			mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mqcJob.setLastEventName(eventInfo.getEventName());

			ExtendedObjectProxy.getMQCJobService().modify(eventInfo, mqcJob);

			Lot lotData = lotDataMap.get(mqcJob.getlotName());

            // Added by smkang on 2019.06.05 - According to Cui Yu's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   CancelMQCJob should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", mqcJob.getlotName());
			
			if(StringUtils.equals(lotData.getLotHoldState(), "Y"))
			{
				throw new CustomException("MQC-0042", lotData.getKey().getLotName());
			}

			String strSql = "SELECT QL.FACTORYNAME,   " +
					"         QL.PROCESSOPERATIONNAME,   " +
					"         PO.DESCRIPTION PROCESSOPERATIONDESC,   " +
					"         QL.PROCESSFLOWNAME,   " +
					"         PO.PROCESSOPERATIONTYPE,   " +
					"         PO.DETAILPROCESSOPERATIONTYPE,   " +
					"         PO.PROCESSOPERATIONGROUP,   " +
					"         QL.NODEID   " +
					"    FROM PROCESSOPERATIONSPEC PO,   " +
					"         ( SELECT LEVEL LV,   " +
					"                  FACTORYNAME,   " +
					"                  PROCESSOPERATIONNAME,   " +
					"                  PROCESSFLOWNAME,   " +
					"                  PROCESSFLOWVERSION,   " +
					"                  NODEID   " +
					"         FROM (   SELECT    " +
					"                     N.FACTORYNAME,   " +
					"                     N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,   " +
					"                     N.PROCESSFLOWNAME,   " +
					"                     N.PROCESSFLOWVERSION,   " +
					"                     N.NODEID,   " +
					"                     N.NODETYPE,   " +
					"                     A.FROMNODEID,   " +
					"                     A.TONODEID   " +
					"                FROM ARC A,   " +
					"                     NODE N,   " +
					"                     PROCESSFLOW PF   " +
					"               WHERE 1 = 1   " +
					"                 AND 1=1    " +
					"                 AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME   " +
					"                 AND N.FACTORYNAME = :FACTORYNAME   " +
					"                 AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME   " +
					"                 AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION   " +
					"                 AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME   " +
					"                 AND N.FACTORYNAME = PF.FACTORYNAME   " +
					"                 AND A.FROMNODEID = N.NODEID)   " +
					"          START WITH NODETYPE = :NODETYPE   " +
					"          CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) QL   " +
					"   WHERE 1 = 1   " +
					"     AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME   " +
					"     AND PO.FACTORYNAME = :FACTORYNAME   " +
					"ORDER BY QL.LV ASC ";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);

			List<Map<String, Object>> mqcFirstOperationInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

			if(mqcFirstOperationInfo != null && mqcFirstOperationInfo.size() > 0)
			{
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());

				productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");

				//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
						lotData.getProductionType(), "M-COMMON", lotData.getProductSpecVersion(),
						"", "",
						"",
						lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(),
						factoryName, lotData.getAreaName(), GenericServiceProxy.getConstantMap().Lot_Completed, "", lotData.getLotHoldState(),
						lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), mqcFirstOperationInfo.get(0).get("PROCESSOPERATIONNAME").toString(), 
						GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, "", "", "", "", "", lotData.getUdfs(), productUdfs, false);

				eventInfo.setBehaviorName("ARRAY");
				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
				// 2019-07-03 Park Jeong Su 아래 Update 로직 제거 필요없다고 판단.
				//LotServiceProxy.getLotService().update(lotData);

				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

				for (Product product : pProductList) 
				{			
					product.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
					product.setProductProcessState("");
					//2019.02.18_hsryu_Insert Logic. Mantis 0002606.
					product.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
					ProductServiceProxy.getProductService().update(product);

					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
		            productHistoryKey.setProductName(product.getKey().getProductName());
		            productHistoryKey.setTimeKey(product.getLastEventTimeKey());

		            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
		            ProductHistory producthistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);

		            producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
		            producthistory.setProductProcessState("");
					//2019.02.18_hsryu_Insert Logic. Mantis 0002606.
					producthistory.getUdfs().put("COMPLETEDATE", eventInfo.getEventTime().toString());
					ProductServiceProxy.getProductHistoryService().update(producthistory);
				}
			}
			
			// 2018.10.18 Mentis 682
			String sql = "SELECT EDV.ENUMVALUE FROM ENUMDEF ED JOIN ENUMDEFVALUE EDV ON ED.ENUMNAME = EDV.ENUMNAME WHERE ED.ENUMNAME = :ENUMNAME AND TAG = :TAG";
			bindMap.clear();
			bindMap.put("ENUMNAME", "EndBank");
			bindMap.put("TAG", "MQCA");
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			String NewEndBank = "";
			if(sqlResult.size() > 0)
			{
				NewEndBank = sqlResult.get(0).get("ENUMVALUE").toString();
			}
			
			eventInfo.setEventName("MoveToEndBank");
			lotData.setProcessOperationName("-");
			//2019.07.31 Park Jeong Su 
			//lotData.setNodeStack(StringUtil.EMPTY);
			
			// 2019.08.27 Park Jeong Su Mantis 4302
			lotData.setNodeStack("111111111111111");
			// 2019.08.27 Park Jeong Su Mantis 4302
			lotData.setLotProcessState("");
			lotData.setLotHoldState("");
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> udfs = lotData.getUdfs();
//			udfs.put("ENDBANK", NewEndBank);
			
			LotServiceProxy.getLotService().update(lotData);
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//			setEventInfo.setUdfs(udfs);
			setEventInfo.getUdfs().put("ENDBANK", NewEndBank);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
			
			for (Product product : pProductList) 
			{
				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo1 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				product.setProcessOperationName("-");
                product.setProcessOperationVersion("");
                //2019.07.31 Park Jeong Su 
                //product.setNodeStack(StringUtil.EMPTY);
                // 2019.08.27 Park Jeong Su Mantis 4302
                product.setNodeStack("111111111111111");
                // 2019.08.27 Park Jeong Su Mantis 4302
                product.setProductSpecName("M-COMMON");
                ProductServiceProxy.getProductService().update(product);
                ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo1);
			}
			
			/* 2018-11-20 Add By ParkJeongSu For AutoMQCSetting*/
			setEmptyCarrerName(lotData.getCarrierName(), eventInfo);
			/* 2018-11-20 Add By ParkJeongSu For AutoMQCSetting*/
		}
		
		return doc;
	}
		
	private void setEmptyCarrerName(String carrierName,EventInfo eventInfo)
	{
		try {
			String condition = " WHERE CARRIERNAME = ? ";
			Object[] bindSet = new Object[] {carrierName};
			List<AutoMQCSetting> autoMQCSettingList = ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
			
			for(AutoMQCSetting autoMQCSetting : autoMQCSettingList )
			{
				autoMQCSetting.setCarrierName("");
				ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
			}
		} catch (Exception e) {
		}
	}
}