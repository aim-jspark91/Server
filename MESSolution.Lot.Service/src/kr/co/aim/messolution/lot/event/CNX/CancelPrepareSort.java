package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
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
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelPrepareSort extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {		
		
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		// Added by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		List<Lot> lotDataList = new ArrayList<Lot>();
		for (Element eleLot : eleLotList) {
			
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
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelPrepareSort",getEventUser(), getEventComment(), "", "");
		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		for(Element eleLot : eleLotList)
//		{
//			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);				
//			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
//			
//			// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
//			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
//			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
//			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
//				throw new CustomException("LOT-9050", lotName);
//				
//			if(StringUtil.equals(lotData.getLotProcessState().trim().toUpperCase(),"RUN"))
//			{
//				throw new CustomException("LOT-0124");
//			}
//				
//			try
//			{
//				Map<String, String> udfs = lotData.getUdfs();
//				
//				String beforeProcessFlowName = lotData.getProcessFlowName(); // sort flow
//				String beforeProcessOperationName = lotData.getProcessOperationName(); // sort operation
//				
//				String NewProcessFlow = udfs.get("BEFOREFLOWNAME");
//				String NewProcessOperation = udfs.get("BEFOREOPERATIONNAME");
//				
//				if(StringUtil.equals(NewProcessOperation,"-"))
//				{
//					String[] arrayNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
//					String TempNodeStack = "";
//					if (arrayNodeStack.length > 1) 
//					{
//						TempNodeStack = arrayNodeStack[0];
//					}
//					String temptimekey = eventInfo.getEventTimeKey();			
//					lotData.setProcessFlowName(NewProcessFlow);
//					lotData.setLotHoldState("");
//					lotData.setProcessOperationName("-");
//					lotData.setLotProcessState("");
//					lotData.setLotState("Completed");
//					lotData.setNodeStack(TempNodeStack);
//					lotData.setProcessOperationVersion("");
//					Map<String, String> tudfs = lotData.getUdfs();
//    				tudfs.put("BEFOREOPERATIONNAME", beforeProcessOperationName);
//    				tudfs.put("BEFOREFLOWNAME", beforeProcessFlowName);
//    				LotServiceProxy.getLotService().update(lotData);
//    				SetEventInfo setEventInfo = new SetEventInfo();
//    				setEventInfo.setUdfs(tudfs);
//    				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
//    				
//    				LotHistoryKey LotHistoryKey = new LotHistoryKey();
//    		        LotHistoryKey.setLotName(lotData.getKey().getLotName());
//    		        LotHistoryKey.setTimeKey(temptimekey);
//    		        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
//    		        LotHistory.setOldProcessFlowName(beforeProcessFlowName);
//    		        LotHistory.setOldProcessOperationName(beforeProcessOperationName);
//    				LotServiceProxy.getLotHistoryService().update(LotHistory);
//    				  				
//    				List<Product> productList3 = null;
//    				productList3 = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//    	            for (Product productData : productList3)
//    	            {   
//    	                productData.setProductProcessState("");
//    	                productData.setProductState("Completed");
//    	                productData.setProductHoldState("");
//    	                productData.setProcessFlowName(NewProcessFlow);
//    	                productData.setProcessOperationName(NewProcessOperation);
//    	                productData.setProcessOperationVersion("");
//    	                ProductServiceProxy.getProductService().update(productData);
//    	                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//    					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
//    					
//    					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
//    		            productHistoryKey.setProductName(productData.getKey().getProductName());
//    		            productHistoryKey.setTimeKey(temptimekey);	            
//    		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
//    					productHistory.setOldProcessFlowName(beforeProcessFlowName);
//    					productHistory.setOldProcessOperationName(beforeProcessOperationName);
//    					ProductServiceProxy.getProductHistoryService().update(productHistory);
//    	            } 
//				}
//				
//				else {
//					String[] arrayNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
//					String TempNodeStack = "";
//					
//					if (arrayNodeStack.length > 1) 
//					{
//						for(int i=0; i<arrayNodeStack.length -1; i++)
//						{
//							TempNodeStack = TempNodeStack + arrayNodeStack[i] + ".";
//						}
//					}
//					
//					if(!StringUtil.isEmpty(TempNodeStack))
//					{
//						TempNodeStack = TempNodeStack.substring(0,TempNodeStack.length() -1);
//					}
//					
//					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
//					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());			
//					productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");			
//					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
//							lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
//							"", lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
//							lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
//							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
//							CommonUtil.getValue(lotData.getUdfs(), "BEFOREFLOWNAME"), CommonUtil.getValue(lotData.getUdfs(), "BEFOREOPERATIONNAME"),
//							"", "", lotData.getNodeStack(),
//							lotData.getUdfs(), productUdfs,
//							true,true);
//					
//					eventInfo.setBehaviorName("ARRAY");
//    				
//					if(!StringUtil.isEmpty(TempNodeStack))
//    				{
//    					changeSpecInfo.setNodeStack(TempNodeStack);
//    				}
//					
//					lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
//
//					try {
//                     	LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
//					} catch (Exception ex) {
//						eventLog.error("[SYS-9999] No Product to process");
//					}
//				}
//							
//				String MultiHoldString = "SELECT LOTNAME, EVENTNAME, EVENTUSER, EVENTCOMMENT FROM CT_LOTMULTIHOLD WHERE LOTNAME = :LOTNAME";
//  				Map<String, String> bindMap = new HashMap<String, String>();
//  				bindMap.put("LOTNAME", lotData.getKey().getLotName());
//  				@SuppressWarnings("unchecked")
//  				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(MultiHoldString, bindMap);
//
//                if(sqlResult != null && sqlResult.size() > 0)
//                {
//                	for(int i = 0; i < sqlResult.size(); i++)
//                	{
//                		/********2019.04.04_hsryu_when Hold, put EventUser & EventComment. Same OLED! Mantis 0003394. **********/
//                    	eventInfo.setEventUser((String)sqlResult.get(i).get("EVENTUSER"));
//                    	eventInfo.setEventComment((String)sqlResult.get(i).get("EVENTCOMMENT"));
//                    	/************************************************************************************************************/
//                    	eventInfo.setCheckTimekeyValidation(false);
//                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
//                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
//                        
//                    	lotData.setLotHoldState("Y");
//                      	Map<String, String> udfsForSort = lotData.getUdfs();
//        				udfsForSort.put("NOTE", sqlResult.get(i).get("EVENTCOMMENT").toString());
//                      	LotServiceProxy.getLotService().update(lotData);
//                      	
//                      	SetEventInfo setEventInfo = new SetEventInfo();
//                      	eventInfo.setEventName((String)sqlResult.get(i).get("EVENTNAME"));
//          				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
//                	}
//                }	
//                
//                // 2019.05.21_hsryu_Add Logic. if PermanentInfo, Delete.
//        		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);
//
//			}
//			catch (Exception ex)
//			{
//				throw new CustomException("SYS-9999", "CancelPrepareSort", ex.getMessage());
//			}
//		}
		for (Lot lotData : lotDataList) {
			// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
			//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
			//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
			if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
				throw new CustomException("LOT-9050", lotData.getKey().getLotName());
				
			if(StringUtil.equals(lotData.getLotProcessState().trim().toUpperCase(),"RUN"))
			{
				throw new CustomException("LOT-0124");
			}
			
			eventInfo.setEventName("CancelPrepareSort");//add GJJ 20200204  event name error mantis 5666
			eventInfo.setEventUser(getEventUser());//add GJJ 20200204  event name error mantis 5666
			eventInfo.setEventComment(getEventComment());//add GJJ 20200204  event name error mantis 5666
			
			
			
			try
			{
				Map<String, String> udfs = lotData.getUdfs();
				
				String beforeProcessFlowName = lotData.getProcessFlowName(); // sort flow
				String beforeProcessOperationName = lotData.getProcessOperationName(); // sort operation
				
				String NewProcessFlow = udfs.get("BEFOREFLOWNAME");
				String NewProcessOperation = udfs.get("BEFOREOPERATIONNAME");
				
				if(StringUtil.equals(NewProcessOperation,"-"))
				{
					String[] arrayNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
					String TempNodeStack = "";
					if (arrayNodeStack.length > 1) 
					{
						TempNodeStack = arrayNodeStack[0];
					}
					String temptimekey = eventInfo.getEventTimeKey();			
					lotData.setProcessFlowName(NewProcessFlow);
					lotData.setLotHoldState("");
					lotData.setProcessOperationName("-");
					lotData.setLotProcessState("");
					lotData.setLotState("Completed");
					lotData.setNodeStack(TempNodeStack);
					lotData.setProcessOperationVersion("");
					// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//					Map<String, String> tudfs = lotData.getUdfs();
//    				tudfs.put("BEFOREOPERATIONNAME", beforeProcessOperationName);
//    				tudfs.put("BEFOREFLOWNAME", beforeProcessFlowName);
//    				LotServiceProxy.getLotService().update(lotData);
//    				SetEventInfo setEventInfo = new SetEventInfo();
//    				setEventInfo.setUdfs(tudfs);
					LotServiceProxy.getLotService().update(lotData);
					SetEventInfo setEventInfo = new SetEventInfo();
    				setEventInfo.getUdfs().put("BEFOREOPERATIONNAME", beforeProcessOperationName);
    				setEventInfo.getUdfs().put("BEFOREFLOWNAME", beforeProcessFlowName);
    				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
    				
    				LotHistoryKey LotHistoryKey = new LotHistoryKey();
    		        LotHistoryKey.setLotName(lotData.getKey().getLotName());
    		        LotHistoryKey.setTimeKey(temptimekey);
    		        
    		        // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    		        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
    		        LotHistory LotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);
    		        
    		        LotHistory.setOldProcessFlowName(beforeProcessFlowName);
    		        LotHistory.setOldProcessOperationName(beforeProcessOperationName);
    				LotServiceProxy.getLotHistoryService().update(LotHistory);

    				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    				List<Product> productList3 = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
    				List<Product> productList3 = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

    				for (Product productData : productList3)
    	            {   
    	                productData.setProductProcessState("");
    	                productData.setProductState("Completed");
    	                productData.setProductHoldState("");
    	                productData.setProcessFlowName(NewProcessFlow);
    	                productData.setProcessOperationName(NewProcessOperation);
    	                productData.setProcessOperationVersion("");
    	                ProductServiceProxy.getProductService().update(productData);
    	                kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo2 = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
    					ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo2);
    					
    					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
    		            productHistoryKey.setProductName(productData.getKey().getProductName());
    		            productHistoryKey.setTimeKey(temptimekey);

    		            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//    		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
    		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);

    		            productHistory.setOldProcessFlowName(beforeProcessFlowName);
    					productHistory.setOldProcessOperationName(beforeProcessOperationName);
    					ProductServiceProxy.getProductHistoryService().update(productHistory);
    	            } 
				}
				
				else {
					String[] arrayNodeStack = StringUtil.split(lotData.getNodeStack(), ".");
					String TempNodeStack = "";
					
					if (arrayNodeStack.length > 1) 
					{
						for(int i=0; i<arrayNodeStack.length -1; i++)
						{
							TempNodeStack = TempNodeStack + arrayNodeStack[i] + ".";
						}
					}
					
					if(!StringUtil.isEmpty(TempNodeStack))
					{
						TempNodeStack = TempNodeStack.substring(0,TempNodeStack.length() -1);
					}
					
					//2018.11.01 Modify, hsryu, lotData.getProductRequestName -> "", Because WorkOrder of Product must not be changed.
					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotData.getKey().getLotName());			
					productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");			
					ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData.getKey().getLotName(),
							lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
							"", lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), lotData.getPriority(),
							lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
							CommonUtil.getValue(lotData.getUdfs(), "BEFOREFLOWNAME"), CommonUtil.getValue(lotData.getUdfs(), "BEFOREOPERATIONNAME"),
							"", "", lotData.getNodeStack(),
							lotData.getUdfs(), productUdfs,
							true,true);
					
					eventInfo.setBehaviorName("ARRAY");
    				
					if(!StringUtil.isEmpty(TempNodeStack))
    				{
    					changeSpecInfo.setNodeStack(TempNodeStack);
    				}

					lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

					try {
                     	LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
					} catch (Exception ex) {
						eventLog.error("[SYS-9999] No Product to process");
					}
				}
							
				String MultiHoldString = "SELECT LOTNAME, EVENTNAME, EVENTUSER, EVENTCOMMENT FROM CT_LOTMULTIHOLD WHERE LOTNAME = :LOTNAME";
  				Map<String, String> bindMap = new HashMap<String, String>();
  				bindMap.put("LOTNAME", lotData.getKey().getLotName());
  				@SuppressWarnings("unchecked")
  				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(MultiHoldString, bindMap);

                if(sqlResult != null && sqlResult.size() > 0)
                {
                	for(int i = 0; i < sqlResult.size(); i++)
                	{
                		/********2019.04.04_hsryu_when Hold, put EventUser & EventComment. Same OLED! Mantis 0003394. **********/
                    	eventInfo.setEventUser((String)sqlResult.get(i).get("EVENTUSER"));
                    	eventInfo.setEventComment((String)sqlResult.get(i).get("EVENTCOMMENT"));
                    	/************************************************************************************************************/
                    	eventInfo.setCheckTimekeyValidation(false);
                        eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
                        
                    	lotData.setLotHoldState("Y");
                      	Map<String, String> udfsForSort = lotData.getUdfs();
        				udfsForSort.put("NOTE", sqlResult.get(i).get("EVENTCOMMENT").toString());
                      	LotServiceProxy.getLotService().update(lotData);
                      	
                      	SetEventInfo setEventInfo = new SetEventInfo();
                      	eventInfo.setEventName((String)sqlResult.get(i).get("EVENTNAME"));
          				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
                	}
                }	
                
                // 2019.05.21_hsryu_Add Logic. if PermanentInfo, Delete.
        		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), beforeProcessFlowName, beforeProcessOperationName, eventInfo);

			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "CancelPrepareSort", ex.getMessage());
			}
		}

		return doc;
	}
}