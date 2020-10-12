package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ScrapProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableHistoryKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.10.25 - Logic Change.
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String scrapProductNames = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		Map<String ,Integer> woHashMap = new HashMap<String, Integer>();
		// if ProductionType of LotData == MQCA And LotState of LotData ==Completed -> true
		boolean mqcLotAndCompleteFlag = false;
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
		// Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		// Start 2019.09.26 Add By Park Jeong Su Mantis 4896
		String beforeNodeStack = lotData.getNodeStack();
		if(StringUtils.equals(GenericServiceProxy.getConstantMap().PRODUCTION_TYPE_MQCA, lotData.getProductionType()) && StringUtils.equals(GenericServiceProxy.getConstantMap().Lot_Completed, lotData.getLotState())){
			eventLog.info("LotName : " +lotData.getKey().getLotName() + " NodeStackChange Start Before NodeStack "+lotData.getNodeStack());
			String endNodeId = CommonUtil.getLastNode(lotData.getFactoryName(), lotData.getProcessFlowName());
			lotData.setNodeStack(endNodeId);
			LotServiceProxy.getLotService().update(lotData);
			mqcLotAndCompleteFlag=true;
			eventLog.info("LotName : " +lotData.getKey().getLotName() + " NodeStackChange End");
		}
		// End 2019.09.26 Add By Park Jeong Su Mantis 4896
		
		//start modify by jhiying on20191016 mantis:5016
		if(StringUtil.equals(lotData.getLotState(),"Received"))
		{
		throw new CustomException("LOT-4004", "");
		 }
		//end modify by jhiying on20191016 mantis:5016
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", lotData.getReasonCode());
		
		//2019.01.08_hsryu_Check SortJob!
		CommonValidation.checkExistSortJob(lotData.getFactoryName(), lotData.getCarrierName());

		// Validation of LotProcessState.
		if (StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
			throw new CustomException("LOT-9003", lotData.getKey().getLotName() +". Current State is " + lotData.getLotProcessState());

		String note = String.format("AREA [%s], MACHINENAME [%s]", lotData.getAreaName(), lotData.getMachineName());
		note+=" "+scrapProductNames;
		lotData.getUdfs().put("NOTE", note);
		LotServiceProxy.getLotService().update(lotData);
		
		// Check all products will be scrapped or not.
		List<Product> unscrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		
		/********* 2018.11.07_hsryu_adjust WO Quantity **********/
		for (Element productElement : productElementList) {
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			ScrapProduct scrapProduct = new ScrapProduct();
			String productRequestName = "";
			
			try{
            	scrapProduct = ExtendedObjectProxy.getScrapProductService().selectByKey(false, new Object[] {productData.getKey().getProductName()});
			}
			catch(Throwable e){
				scrapProduct = null;
			}
			
			if(scrapProduct != null)
				productRequestName = scrapProduct.getProductRequestName();
			else
				productRequestName = productData.getProductRequestName();
			
			if(StringUtils.isNotEmpty(productRequestName)){
				if(!woHashMap.containsKey(productRequestName))
					woHashMap.put(productRequestName, 1);
				else
					woHashMap.put(productRequestName, (woHashMap.get(productRequestName))+1);
			}
		}
		
		for(String key : woHashMap.keySet()){
			// Adjust WorkOrder Quantity
			// 2019.01.21 exclude MQC Lot
			if (!lotData.getProductionType().equals("MQCA")){
				MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(key, null, "S", woHashMap.get(key), eventInfo);
			}
		}
		/********************************************************/

		// All Glass Scrap! 
		if (unscrappedProductList == null || unscrappedProductList.size() == productElementList.size()) 
		{
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			for (Element productElement : productElementList) {
				String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				
				if(!StringUtils.equals(productData.getProductGrade(), "S"))
					throw new CustomException("PRODUCT-0006", productName);
				
				ProductU productU = new ProductU(productName);				
				productUSequence.add(productU);
			}
			
			// 1. Deassign Carrier
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			
			eventInfo.setBehaviorName("greenTrack");
			eventInfo.setEventName("DeassignCarrierByScrap");
			
			DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
			deassignCarrierInfo.setProductUSequence(productUSequence);
			
			MESLotServiceProxy.getLotServiceImpl().deassignCarrierByScrap(lotData, deassignCarrierInfo, eventInfo);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// Lot afterDeassignCarrierLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
			Lot afterDeassignCarrierLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
			
			// 2. Scrap Lot
			eventInfo.setEventName("Scrap");
			eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
            eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			// To avoid validation of lot state in greenTrack API.
			if (afterDeassignCarrierLotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Completed)) {
				afterDeassignCarrierLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				afterDeassignCarrierLotData.setLotHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
				afterDeassignCarrierLotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
				
				LotServiceProxy.getLotService().update(afterDeassignCarrierLotData);
				
				for (Element productElement : productElementList) {
					String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
					Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
					
					productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
					productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
					productData.setProductHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
					
					ProductServiceProxy.getProductService().update(productData);
				}
			}
			
			MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
			makeScrappedInfo.setProductQuantity(productUSequence.size());
			makeScrappedInfo.setProductUSequence(productUSequence);
			
			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
			//LotServiceProxy.getLotService().makeScrapped(afterDeassignCarrierLotData.getKey(), eventInfo, makeScrappedInfo);
			MESLotServiceProxy.getLotServiceImpl().makeScrapped(eventInfo,afterDeassignCarrierLotData,makeScrappedInfo);
			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
			
			Lot afterScrappedLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(afterDeassignCarrierLotData.getKey().getLotName());
			
			// MESLotServiceProxy.getLotServiceUtil().checkMQCFlowbyScrap(lotData, lotData.getCarrierName(), eventInfo);

			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
			// Note clear - YJYU
			// afterScrappedLotData.getUdfs().put("NOTE", "");
			// LotServiceProxy.getLotService().update(afterScrappedLotData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(afterScrappedLotData, updateUdfs);
		}
		else 
		{
			// Modified by smkang on 2018.11.08 - According to EDO's request, partial lot should be executed split and de-assign carrier.
			// 1. Split Lot
			eventInfo.setBehaviorName("ARRAY");
			eventInfo.setEventName("SplitByScrap");
			
			List<ProductP> productPSequence = new ArrayList<ProductP>();
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			for (Element productElement : productElementList) {
				String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
				// Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
				
				if(!StringUtils.equals(productData.getProductGrade(), "S"))
					throw new CustomException("PRODUCT-0006", productName);
				
				ProductP productP = new ProductP();
				productP.setProductName(productName);
				productP.setPosition(productData.getPosition());
				
				productPSequence.add(productP);
				
				ProductU productU = new ProductU(productName);
				productUSequence.add(productU);
				
				// To avoid validation of product state in greenTrack API.
				if (productData.getProductState().equals(GenericServiceProxy.getConstantMap().Prod_Completed)) {				
					productData.setProductState(GenericServiceProxy.getConstantMap().Prod_InProduction);
					productData.setProductProcessState(GenericServiceProxy.getConstantMap().Prod_Idle);
					productData.setProductHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
					
					ProductServiceProxy.getProductService().update(productData);
				}
			}
			
			// Get Child Lot Name
			List<String> argSeq = new ArrayList<String>();
			argSeq.add(StringUtil.substring(lotData.getKey().getLotName(), 0, 8));
			List<String> childLotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
			
			SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitInfo(lotData, lotData.getCarrierName(), childLotNameList.get(0), productPSequence, String.valueOf(productPSequence.size()));
			MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);

			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// Lot splitLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(childLotNameList.get(0));
			Lot splitLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(childLotNameList.get(0)));
			
			// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.11.12 - If ProcessOperationName is null, ProcessOperationName and version would be set the last operation of this ProcessFlow automatically after split is executed.
			//								   So ProcessOperationName and ProcessOperationVersion should be updated with the information of source lot manually.
			//								   In additional, ConsumerPOName and ConsumerPOVersion of DurableHistory should be also updated.
			
			String desWOName = "";
			/**************************** Check Mixed WO Name *********************************/
			//2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.
			try{
				desWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(splitLotData);
			}
			catch(Throwable e){
				eventLog.warn("Fail Update WO!");
			}
			/*********************************************************************************/
			
			splitLotData.setProcessOperationName(lotData.getProcessOperationName());
			splitLotData.setProcessOperationVersion(lotData.getProcessOperationVersion());
			// 2019.04.25_hsryu_Insert Logic. 
			if(!StringUtils.isEmpty(desWOName)) {
				if(!StringUtils.equals(splitLotData.getProductRequestName(), desWOName)) {
					splitLotData.setProductRequestName(desWOName);
				}
			}
			LotServiceProxy.getLotService().update(splitLotData);
			
			LotHistoryKey LotHistoryKey = new LotHistoryKey();
		    LotHistoryKey.setLotName(splitLotData.getKey().getLotName());
		    LotHistoryKey.setTimeKey(splitLotData.getLastEventTimeKey());
		    
		    // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
		    // LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKey(LotHistoryKey);
		    LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(LotHistoryKey);

		    lotHistory.setProcessOperationName(splitLotData.getProcessOperationName());
			lotHistory.setProcessOperationVersion(splitLotData.getProcessOperationVersion());
			// 2019.04.25_hsryu_Insert Logic. 
			if(!StringUtils.isEmpty(desWOName)) {
				if(!StringUtils.equals(lotHistory.getProductRequestName(), desWOName)) {
					lotHistory.setProductRequestName(desWOName);
				}
			}
			LotServiceProxy.getLotHistoryService().update(lotHistory);
			
			DurableHistoryKey DurableHistory = new DurableHistoryKey();
			DurableHistory.setDurableName(splitLotData.getCarrierName());
			DurableHistory.setTimeKey(eventInfo.getEventTimeKey());
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(DurableHistory);
			DurableHistory durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKeyForUpdate(DurableHistory);

			durableHistory.setConsumerPOName(splitLotData.getProcessOperationName());
			durableHistory.setConsumerPOVersion(splitLotData.getProcessOperationVersion());
			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
			
			if (splitLotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Completed)) {
				for (Element productElement : productElementList) {
					String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);

					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
		            productHistoryKey.setProductName(productName);
		            productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());

		            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
		            // ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
		            
					productHistory.setProcessOperationName(splitLotData.getProcessOperationName());
					productHistory.setProcessOperationVersion(splitLotData.getProcessOperationVersion());
					productHistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
					productHistory.setProductProcessState("");
					productHistory.setProductHoldState("");
					ProductServiceProxy.getProductHistoryService().update(productHistory);
				}
			}
			// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		    lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
			
		    // 2019.04.23_hsryu_Insert Logic. Change call Common Fuction. Mantis 0002757.		
		    try {
				String sourceWOName = MESLotServiceProxy.getLotServiceUtil().isMixedWorkOrder(lotData);
				
				if(!StringUtils.isEmpty(sourceWOName)) {
					if(!StringUtils.equals(lotData.getProductRequestName(), sourceWOName)) {
						lotData.setProductRequestName(sourceWOName);
						LotServiceProxy.getLotService().update(lotData);
						
						// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
						// String condition = "where lotname=?" + " and timekey= ? ";
						String condition = "where lotname=?" + " and timekey= ? for update";
						
						Object[] bindSet = new Object[]{lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};
						List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
						LotHistory sourceLotHistory = arrayList.get(0);
						sourceLotHistory.setProductRequestName(sourceWOName);
						LotServiceProxy.getLotHistoryService().update(sourceLotHistory);
					}
				}
			}
			catch(Throwable e) {
				eventLog.warn("Fail Update WO!");
			}
		    
		    // Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
		    // Note clear - YJYU
		    // lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		    // lotData.getUdfs().put("NOTE", "");
		    // LotServiceProxy.getLotService().update(lotData);
		    Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
			
			// 2. Deassign Carrier
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(splitLotData.getCarrierName());
			
			eventInfo.setBehaviorName("greenTrack");
			eventInfo.setEventName("DeassignCarrierByScrap");
			
			DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo();
			deassignCarrierInfo.setProductUSequence(productUSequence);
			
			MESLotServiceProxy.getLotServiceImpl().deassignCarrierByScrap(splitLotData, deassignCarrierInfo, eventInfo);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
			// Lot afterDeassignCarrierLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(splitLotData.getKey().getLotName());
			Lot afterDeassignCarrierLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(splitLotData.getKey());
			
			if (afterDeassignCarrierLotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Completed)) {
				afterDeassignCarrierLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				afterDeassignCarrierLotData.setLotHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
				afterDeassignCarrierLotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
				
				LotServiceProxy.getLotService().update(afterDeassignCarrierLotData);
				
				for (Element productElement : productElementList) {
					String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
					ProductHistoryKey productHistoryKey = new ProductHistoryKey();
		            productHistoryKey.setProductName(productName);
		            productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());

		            // Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
		            // ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKey(productHistoryKey);
		            ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);

		            productHistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
					productHistory.setProductProcessState("");
					productHistory.setProductHoldState("");
					ProductServiceProxy.getProductHistoryService().update(productHistory);
				}
			}

			// 3. Scrap Child Lot
			eventInfo.setEventName("Scrap");
			
			MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();
			makeScrappedInfo.setProductQuantity(productUSequence.size());
			makeScrappedInfo.setProductUSequence(productUSequence);
			
			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
			//LotServiceProxy.getLotService().makeScrapped(afterDeassignCarrierLotData.getKey(), eventInfo, makeScrappedInfo);
			MESLotServiceProxy.getLotServiceImpl().makeScrapped(eventInfo,afterDeassignCarrierLotData,makeScrappedInfo);
			// End 2019.09.09 Modify By Park Jeong Su Mantis 4721
			
			
			Lot afterScrappedLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(afterDeassignCarrierLotData.getKey().getLotName());
			
			// 2019.04.30
			// if (unscrappedProductList == null || unscrappedProductList.size() == productElementList.size()) 
			// {
			// MESLotServiceProxy.getLotServiceUtil().checkMQCFlow(lotData, lotData.getCarrierName(), eventInfo, deassignCarrierInfo);			
			// } else {
			// MESLotServiceProxy.getLotServiceUtil().checkMQCFlowAfter(lotData, lotData.getCarrierName(), eventInfo);
			// }
			
			// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
			// Note clear - YJYU
			// afterScrappedLotData.getUdfs().put("NOTE", "");
			// LotServiceProxy.getLotService().update(afterScrappedLotData);
			Map<String, String> updateUdfs2 = new HashMap<String, String>();
			updateUdfs2.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(afterScrappedLotData, updateUdfs2);
		}
		
		// Start 2019.09.26 Add By Park Jeong Su Mantis 4896
		if(mqcLotAndCompleteFlag==true  )
		{
			lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
			if(!lotData.getLotState().equalsIgnoreCase(GenericServiceProxy.getConstantMap().Lot_Scrapped))// add by GJJ 20200501 mantis:6096
			{
				eventLog.info("LotName : " +lotData.getKey().getLotName() + " NodeStack ReChange Start Before NodeStack "+lotData.getNodeStack());
				lotData.setNodeStack(beforeNodeStack);
				LotServiceProxy.getLotService().update(lotData);
				eventLog.info("LotName : " +lotData.getKey().getLotName() + " NodeStack ReChange End NodeStack "+lotData.getNodeStack());
			}
		}
		// End 2019.09.26 Add By Park Jeong Su Mantis 4896
		
		MESLotServiceProxy.getLotServiceImpl().checkMQCJobOfScrapLot(lotName,lotData.getCarrierName(),eventInfo);
		
		return doc;
	}
}