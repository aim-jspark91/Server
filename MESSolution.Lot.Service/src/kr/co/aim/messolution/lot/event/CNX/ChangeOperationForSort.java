package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeOperationForSort extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String toProcessFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		 
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		// Added by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		List<Lot> lotDataList = new ArrayList<Lot>();
		for (Element eleLot : lotList) {
			// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
			Lot lotData =  LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true)));
			String xNodeStack = SMessageUtil.getChildText(eleLot, "NODESTACK", false);
			if( xNodeStack.isEmpty() || xNodeStack.equals(lotData.getNodeStack()))
			{
				lotDataList.add(lotData);
			}
			else {
				setLanguage("Chinese");
				throw new CustomException("LOT-8002");
			}
			// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致	
			
			//lotDataList.add(LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(SMessageUtil.getChildText(eleLot, "LOTNAME", true))));modefiy by GJJ 20200323
		}
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationForSort", getEventUser(), getEventComment(), null, null);
		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		for ( Element lotE : lotList)
//		{
//			String lotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			
//			// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
//			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
//			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
//			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
//				throw new CustomException("LOT-9050", lotName);
//			
//			//List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
//			if (MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData).getProcessFlowType().equals("Sort")) {
//				throw new CustomException("SORT-0006");
//			}
//			
//			if(!CheckTransPortState(lotData.getCarrierName()))
//			{
//				throw new CustomException("LOT-0125");
//			}
//			
//			String CurrentFlowName = lotData.getProcessFlowName();
//			String CurrentOperationName = lotData.getProcessOperationName();
//			String CurrentHoldState = lotData.getLotHoldState();
//			
//			if(StringUtils.equals(CurrentHoldState.trim().toUpperCase(),"Y"))
//			{
//				eventInfo.setEventName("ChangeHoldState");
//				eventInfo.setEventUser("System");				
//				lotData.setLotHoldState("N");
//				Map<String, String> Ludfs = lotData.getUdfs();
//				Ludfs.put("NOTE", "For sorter auto release and it will be hold after complete sorter job");
//				LotServiceProxy.getLotService().update(lotData);
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(Ludfs);
//				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);   
//				
//				List<Product> productList = null;
//				productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//				for (Product productData : productList)
//				{
//					productData.setProductHoldState("N");
//	                ProductServiceProxy.getProductService().update(productData);
//	                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
//				}		
//			}
//			
//			eventInfo.setEventName("ChangeOperationForSort");
//			eventInfo.setEventUser(getEventUser());	
//			eventInfo.setCheckTimekeyValidation(false);
//            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//			
//			String sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(toProcessFlowName);
//			sampleNodeStack = lotData.getNodeStack() + "." + sampleNodeStack;
//			
//			String temptimekey = eventInfo.getEventTimeKey();
//			
//			lotData.setLotProcessState("WAIT");
//			lotData.setLotState("Released");	
//			lotData.setLotHoldState("N");
//			lotData.setProcessFlowName(toProcessFlowName);
//			lotData.setProcessOperationName(toProcessOperationName);
//			lotData.setProcessOperationVersion("00001");
//			lotData.setNodeStack(sampleNodeStack);
//			Map<String, String> tudfs = lotData.getUdfs();
//			tudfs.put("BEFOREOPERATIONNAME", CurrentOperationName);
//			tudfs.put("BEFOREFLOWNAME", CurrentFlowName);
//			tudfs.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(tudfs);
//			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);    
//			
//	        LotHistoryKey LotHistoryKey = new LotHistoryKey();
//	        LotHistoryKey.setLotName(lotData.getKey().getLotName());
//	        LotHistoryKey.setTimeKey(temptimekey);
//	        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
//	        LotHistory.setOldProcessFlowName(CurrentFlowName);
//	        LotHistory.setOldProcessOperationName(CurrentOperationName);
//			LotServiceProxy.getLotHistoryService().update(LotHistory);
//			
//			List<Product> productList = null;
//			productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//            for (Product productData : productList)
//            {   
//                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
//                productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);   
//                productData.setProductHoldState("N"); 
//                productData.setProcessFlowName(toProcessFlowName);
//                productData.setProcessOperationName(toProcessOperationName);
//                productData.setProcessOperationVersion("00001");
//                productData.setNodeStack(sampleNodeStack);
//                ProductServiceProxy.getProductService().update(productData);
//                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//				ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
//				
//	            ProductHistoryKey productHistoryKey = new ProductHistoryKey();
//	            productHistoryKey.setProductName(productData.getKey().getProductName());
//	            productHistoryKey.setTimeKey(temptimekey);	            
//	            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
//				productHistory.setOldProcessFlowName(CurrentFlowName);
//				productHistory.setOldProcessOperationName(CurrentOperationName);
//				ProductServiceProxy.getProductHistoryService().update(productHistory);
//            }   
//            
//			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//            
//			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), CurrentFlowName,CurrentOperationName, eventInfo);
//		}
		for (Lot lotData : lotDataList) {
			// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotData.getKey().getLotName());
			
			//List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			if (MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData).getProcessFlowType().equals("Sort")) {
				throw new CustomException("SORT-0006");
			}
			
			if(!CheckTransPortState(lotData.getCarrierName()))
			{
				throw new CustomException("LOT-0125");
			}
			
			String CurrentFlowName = lotData.getProcessFlowName();
			String CurrentOperationName = lotData.getProcessOperationName();
			String CurrentHoldState = lotData.getLotHoldState();
			
			if(StringUtils.equals(CurrentHoldState.trim().toUpperCase(),"Y"))
			{
				eventInfo.setEventName("ChangeHoldState");
				eventInfo.setEventUser("System");				
				lotData.setLotHoldState("N");
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				Map<String, String> Ludfs = lotData.getUdfs();
//				Ludfs.put("NOTE", "For sorter auto release and it will be hold after complete sorter job");
//				LotServiceProxy.getLotService().update(lotData);
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(Ludfs);
				LotServiceProxy.getLotService().update(lotData);
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("NOTE", "For sorter auto release and it will be hold after complete sorter job");
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);   
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

				for (Product productData : productList)
				{
					productData.setProductHoldState("N");
	                ProductServiceProxy.getProductService().update(productData);
	                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
				}		
			}
			
			eventInfo.setEventName("ChangeOperationForSort");
			eventInfo.setEventUser(getEventUser());	
			eventInfo.setCheckTimekeyValidation(false);
            eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
            eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
			
			String sampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(toProcessFlowName);
			sampleNodeStack = lotData.getNodeStack() + "." + sampleNodeStack;
			
			String temptimekey = eventInfo.getEventTimeKey();
			
			lotData.setLotProcessState("WAIT");
			lotData.setLotState("Released");	
			lotData.setLotHoldState("N");
			lotData.setProcessFlowName(toProcessFlowName);
			lotData.setProcessOperationName(toProcessOperationName);
			lotData.setProcessOperationVersion("00001");
			lotData.setNodeStack(sampleNodeStack);
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> tudfs = lotData.getUdfs();
//			tudfs.put("BEFOREOPERATIONNAME", CurrentOperationName);
//			tudfs.put("BEFOREFLOWNAME", CurrentFlowName);
//			tudfs.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData);
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(tudfs);
			LotServiceProxy.getLotService().update(lotData);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("BEFOREOPERATIONNAME", CurrentOperationName);
			setEventInfo.getUdfs().put("BEFOREFLOWNAME", CurrentFlowName);
			setEventInfo.getUdfs().put("NOTE", "");

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);    
			
	        LotHistoryKey LotHistoryKey = new LotHistoryKey();
	        LotHistoryKey.setLotName(lotData.getKey().getLotName());
	        LotHistoryKey.setTimeKey(temptimekey);
	        
	        // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
	        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);

	        LotHistory.setOldProcessFlowName(CurrentFlowName);
	        LotHistory.setOldProcessOperationName(CurrentOperationName);
			LotServiceProxy.getLotHistoryService().update(LotHistory);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

			for (Product productData : productList)
            {   
                productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
                productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);   
                productData.setProductHoldState("N"); 
                productData.setProcessFlowName(toProcessFlowName);
                productData.setProcessOperationName(toProcessOperationName);
                productData.setProcessOperationVersion("00001");
                productData.setNodeStack(sampleNodeStack);
                ProductServiceProxy.getProductService().update(productData);
                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
				
	            ProductHistoryKey productHistoryKey = new ProductHistoryKey();
	            productHistoryKey.setProductName(productData.getKey().getProductName());
	            productHistoryKey.setTimeKey(temptimekey);

	            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//	            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
	            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);

	            productHistory.setOldProcessFlowName(CurrentFlowName);
				productHistory.setOldProcessOperationName(CurrentOperationName);
				ProductServiceProxy.getProductHistoryService().update(productHistory);
            }
            
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
            
			MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), CurrentFlowName,CurrentOperationName, eventInfo);
		}
		
		return doc;
	}
	
	// Mentis 2515
	public boolean CheckTransPortState(String CarrierName)
	{
		String Query = "SELECT COUNT(TRANSPORTJOBNAME) AS CNT FROM CT_TRANSPORTJOBCOMMAND WHERE CARRIERNAME = :CARRIERNAME AND JOBSTATE IN (:JOBSTATE1, :JOBSTATE2, :JOBSTATE3)";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CARRIERNAME", CarrierName);
		bindMap.put("JOBSTATE1", GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested);
		bindMap.put("JOBSTATE2", GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted);
		bindMap.put("JOBSTATE3", GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		if( sqlResult != null && sqlResult.size() > 0)
		{
			String CNT = sqlResult.get(0).get("CNT").toString().trim();
			if(Integer.parseInt(CNT) > 0)
			{
				return false;
			}
		}
		
		Query = "SELECT PORTNAME, MACHINENAME FROM DURABLE WHERE DURABLENAME = :DURABLENAME";
		bindMap.clear();
		bindMap.put("DURABLENAME", CarrierName);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> DurableResult = GenericServiceProxy.getSqlMesTemplate().queryForList(Query, bindMap);
		
		if( DurableResult != null && DurableResult.size() > 0)
		{
			if(DurableResult.get(0).get("PORTNAME") != null)
			{
				if(!StringUtil.isEmpty(DurableResult.get(0).get("PORTNAME").toString().trim()))
				{
					return false;
				}
			}
		}

		return true;
	}
}