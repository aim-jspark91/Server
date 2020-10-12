package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
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
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
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

public class UnscrapLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.10.25 - Logic Change.
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		//start modify by jhiying on20191016 mantis:5016
		if(StringUtil.equals(lotData.getLotState(),"Received"))
		{
		throw new CustomException("LOT-4004", "");
		 }
		//end modify by jhiying on20191016 mantis:5016

		lotData.getUdfs().put("NOTE", note);
		
		List<Product> scrappedProductDataList = ProductServiceProxy.getProductService().select("LOTNAME = ?", new Object[] {lotName});
		List<Product> unScrapProductDataList = new ArrayList<Product>();
		List<ProductU> productUSequence = new ArrayList<ProductU>();
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		
		makeProductInfoList(productElementList, unScrapProductDataList, productUSequence, productPSequence);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		if (scrappedProductDataList.size() == productElementList.size()) {
			if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
				// 1. All unscrapped products assigned to a Available carrier -> UnScrapLot and AssignCarrier.
				// 1-1. UnScrapLot.
				Lot unScrappedLotData = unScrapLot(eventInfo, lotData, unScrapProductDataList, productUSequence);
				
				// 1-2. AssignCarrier.
				unScrappedLotData = assignCarrier(eventInfo, unScrappedLotData, durableData, productPSequence);
				
				// 1-3. Adjust ProductRequest Quantity.
				if (!lotData.getProductionType().equals("MQCA")){
					//2019.02.22_hsryu_Modify ProductElementList.Size() -> ProductElementList
					adjustProductRequest(eventInfo, unScrappedLotData, productElementList);
				}
				
				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				unScrappedLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(unScrappedLotData.getKey().getLotName());
//
//				// 1-4. Clear Lot Note.
//				unScrappedLotData.getUdfs().put("NOTE", "");
//				LotServiceProxy.getLotService().update(unScrappedLotData);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(unScrappedLotData, updateUdfs);
			} else {
				// 2. All unscrapped products assigned to a InUse carrier -> UnScrapLot and MergeLot.
				// 2-1. Validate Merge Condition.
				List<Lot> targetLotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ?", new Object[] {carrierName});
				Lot targetLotData = targetLotDataList.get(0);
				
				// 2-2. UnScrapLot.
				Lot unScrappedLotData = unScrapLot(eventInfo, lotData, unScrapProductDataList, productUSequence);

				String validationResult = compareMergeCondition(unScrappedLotData, targetLotData);
				if (StringUtils.isNotEmpty(validationResult))
					throw new CustomException("COMMON-0001", "Merge is failed because of different condition -" + validationResult);
				else {
					// 2-3. Merge.
					unScrappedLotData = mergeLot(eventInfo, unScrappedLotData, targetLotData, unScrapProductDataList, productPSequence);
					
					// 2-4. Adjust ProductRequest Quantity.
					if (!lotData.getProductionType().equals("MQCA")){
						//2019.02.22_hsryu_Modify ProductElementList.Size() -> ProductElementList
						adjustProductRequest(eventInfo, unScrappedLotData, productElementList);
					}
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//					unScrappedLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(unScrappedLotData.getKey().getLotName());
//
//					// 2-5. Clear Lot Note.
//					unScrappedLotData.getUdfs().put("NOTE", "");
//					LotServiceProxy.getLotService().update(unScrappedLotData);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(unScrappedLotData, updateUdfs);
				}
			}
		} else {
			if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
				// 3. Some unscrapped products assigned to a Available carrier -> SplitLot, UnScrapLot and AssignCarrier. 
				// 3-1. SplitLot.
				Lot splitLotData = splitLot(eventInfo, lotData, productPSequence);
				
				// 3-2. UnScrapLot.
				Lot unScrappedLotData = unScrapLot(eventInfo, splitLotData, unScrapProductDataList, productUSequence);
				
				// 3-3. AssignCarrier.
				unScrappedLotData = assignCarrier(eventInfo, unScrappedLotData, durableData, productPSequence);
				
				// 3-4. Adjust ProductRequest Quantity.
				if (!lotData.getProductionType().equals("MQCA")){
					//2019.02.22_hsryu_Modify ProductElementList.Size() -> ProductElementList
					adjustProductRequest(eventInfo, unScrappedLotData, productElementList);
				}
				
				// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//				unScrappedLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(unScrappedLotData.getKey().getLotName());
//
//				// 3-5. Clear Lot Note.
//				unScrappedLotData.getUdfs().put("NOTE", "");
//				LotServiceProxy.getLotService().update(unScrappedLotData);
				Map<String, String> updateUdfs = new HashMap<String, String>();
				updateUdfs.put("NOTE", "");
				MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(unScrappedLotData, updateUdfs);
			} else {
				// 4. Some unscrapped products assigned to a InUse carrier -> SplitLot, UnScrapLot and MergeLot.
				// 4-1. Validate Merge Condition.
				List<Lot> targetLotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ?", new Object[] {carrierName});
				Lot targetLotData = targetLotDataList.get(0);
				
				// 4-2. SplitLot.
				Lot splitLotData = splitLot(eventInfo, lotData, productPSequence);
				
				// 4-3. UnScrapLot.
				Lot unScrappedLotData = unScrapLot(eventInfo, splitLotData, unScrapProductDataList, productUSequence);
				
				String validationResult = compareMergeCondition(unScrappedLotData, targetLotData);
				if (StringUtils.isNotEmpty(validationResult))
					throw new CustomException("COMMON-0001", "Merge is failed because of different condition " + validationResult);
				else {
					// 4-4. Merge.
					unScrappedLotData = mergeLot(eventInfo, unScrappedLotData, targetLotData, unScrapProductDataList, productPSequence);
					
					// 4-5. Adjust ProductRequest Quantity.
					if (!lotData.getProductionType().equals("MQCA")){
						//2019.02.22_hsryu_Modify ProductElementList.Size() -> ProductElementList
						adjustProductRequest(eventInfo, unScrappedLotData, productElementList);
					}
					
					// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.					
//					unScrappedLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(unScrappedLotData.getKey().getLotName());
//
//					// 4-6. Clear Lot Note.
//					unScrappedLotData.getUdfs().put("NOTE", "");
//					LotServiceProxy.getLotService().update(unScrappedLotData);
					Map<String, String> updateUdfs = new HashMap<String, String>();
					updateUdfs.put("NOTE", "");
					MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(unScrappedLotData, updateUdfs);
				}
			}
		}
		
		return doc;
	}
	
	private void makeProductInfoList(List<Element> productElementList, List<Product> scrappedProductdataliList, List<ProductU> productUSequence, List<ProductP> productPSequence) throws CustomException {
		for (Element productElement : productElementList) {
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));

			scrappedProductdataliList.add(productData);
			
			ProductU productU = new ProductU(productName);
			productUSequence.add(productU);
			
			ProductP productP = new ProductP();
			productP.setProductName(productName);
			productP.setPosition(productData.getPosition());
			productPSequence.add(productP);			
		}
	}
	
	private Lot unScrapLot(EventInfo eventInfo, Lot lotData, List<Product> scrappedProductDataList, List<ProductU> productUSequence) throws CustomException {
		// After product is unscrapped, product grade will be changed initial state of GradeDefinition, so previous product grade will be updated the end of function.
		eventInfo.setBehaviorName("greenTrack");
		eventInfo.setEventName("UnScrap");
		
		//2019.09.23 dmlee : UnScrap Set Return Node Start
		String returnNode = lotData.getNodeStack();
		try
		{
			ScrapProduct scrapProductData = ExtendedObjectProxy.getScrapProductService().selectByKey(false, new Object[]{scrappedProductDataList.get(0).getKey().getProductName()});
			returnNode = scrapProductData.getNodeStack();
		}
		catch(Exception ex)
		{
			eventLog.error("*** Get Return Node Fail ! ***");
		}
		
		try
		{
			Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(returnNode);
		}
		catch(NotFoundSignal ns)
		{
			//2019.11.07 dmlee : if NodeStack Over 2count, just check last node --- Start
			if(returnNode.contains("."))
			{
				try
				{
					String[] arrNodeStack = StringUtil.split(returnNode, ".");
					
					Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(arrNodeStack[arrNodeStack.length-1]);
				}
				catch(NotFoundSignal ns2)
				{
					throw new CustomException("UnScrap-001",returnNode);
				}
			}
			else
			{
				throw new CustomException("UnScrap-001",returnNode);
			}
			//2019.11.07 dmlee : if NodeStack Over 2count, just check last node --- End
		}
		//2019.09.23 dmlee : UnScrap Set Return Node End
		
		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo();
		makeUnScrappedInfo.setProductQuantity(productUSequence.size());
		makeUnScrappedInfo.setProductUSequence(productUSequence);
		
		LotServiceProxy.getLotService().makeUnScrapped(lotData.getKey(), eventInfo, makeUnScrappedInfo);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot unScrappedLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
		Lot unScrappedLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
		
		// After lot is unscrapped, lot grade will be changed initial state of GradeDefinition, so previous lot grade will be updated the end of function.
		unScrappedLotData.setLotGrade(lotData.getLotGrade());
		unScrappedLotData.setNodeStack(returnNode);
		
		// If EndBank is not null, LotState should be changed to Completed.
		if (StringUtils.equals(lotData.getProcessOperationName(), "-"))
		{
			unScrappedLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
			unScrappedLotData.setLotProcessState("");
			unScrappedLotData.setLotHoldState("");
		}
		
		LotServiceProxy.getLotService().update(unScrappedLotData);
		
		// Commented by smkang on 2018.11.11 - After split lot is executed, lot state is changed to Emptied, so lot history will be updated manually.
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Object[] bindSet = new Object[] {unScrappedLotData.getKey().getLotName(), eventInfo.getEventTimeKey()};				
//		LotHistory lotHistory = LotServiceProxy.getLotHistoryService().select("WHERE LOTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
		LotHistoryKey lotHistoryKey = new LotHistoryKey();
		lotHistoryKey.setLotName(unScrappedLotData.getKey().getLotName());
		lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
		LotHistory lotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
		
		lotHistory.setLotGrade(unScrappedLotData.getLotGrade());
		lotHistory.setLotState(unScrappedLotData.getLotState());
		lotHistory.setLotProcessState(unScrappedLotData.getLotProcessState());
		lotHistory.setLotHoldState(unScrappedLotData.getLotHoldState());
		lotHistory.setNodeStack(returnNode);
		LotServiceProxy.getLotHistoryService().update(lotHistory);
		
		for (Product scrappedProductData : scrappedProductDataList) {
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product unscrappedProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(scrappedProductData.getKey().getProductName());
			Product unscrappedProductData = ProductServiceProxy.getProductService().selectByKeyForUpdate(scrappedProductData.getKey());
			
			// After product is unscrapped, product grade will be changed initial state of GradeDefinition, so previous product grade will be updated the end of function.
			unscrappedProductData.setProductGrade(scrappedProductData.getProductGrade());
			unscrappedProductData.setNodeStack(returnNode);
			
			// If EndBank is not null, LotState should be changed to Completed.
			if (StringUtils.equals(lotData.getProcessOperationName(), "-")) {
				unscrappedProductData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
				unscrappedProductData.setProductProcessState("");
				unscrappedProductData.setProductHoldState("");
			}

			ProductServiceProxy.getProductService().update(unscrappedProductData);
			
			// Commented by smkang on 2018.11.11 - ProductGrade of ProductHistory should be also updated manually.
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Object[] bindSet = new Object[] {unscrappedProductData.getKey().getProductName(), eventInfo.getEventTimeKey()};
//			ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().select("WHERE PRODUCTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
			ProductHistoryKey productHistoryKey = new ProductHistoryKey();
			productHistoryKey.setProductName(unscrappedProductData.getKey().getProductName());
			productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
			ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
			
			productHistory.setProductGrade(unscrappedProductData.getProductGrade());
			productHistory.setProductState(unscrappedProductData.getProductState());
			productHistory.setProductProcessState(unscrappedProductData.getProductProcessState());
			productHistory.setProductHoldState(unscrappedProductData.getProductHoldState());
			productHistory.setNodeStack(returnNode);
			ProductServiceProxy.getProductHistoryService().update(productHistory);
		}
		
		return unScrappedLotData;
	}
	
	private Lot splitLot(EventInfo eventInfo, Lot lotData, List<ProductP> productPSequence) throws CustomException {
		eventInfo.setBehaviorName("SPECIAL");
		eventInfo.setEventName("SplitByUnScrap");
		
		// Get Child Lot Name
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(StringUtil.substring(lotData.getKey().getLotName(), 0, 8));
		List<String> childLotNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
		
		SplitInfo splitInfo = new SplitInfo();

		splitInfo.setChildLotName(childLotNameList.get(0));
		splitInfo.setProductPSequence(productPSequence);
		splitInfo.setProductQuantity(productPSequence.size());

		Map<String, String> lotUdfs = lotData.getUdfs();
		splitInfo.setUdfs(lotUdfs);

		Map<String, String> childLotUdfs = lotData.getUdfs();
		splitInfo.setChildLotUdfs(childLotUdfs);
		
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// Commented by smkang on 2018.11.09 - Because a lot of Scrapped state can be split, the lot and the products will be changed to Released state temporarily.
		lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
		lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
		lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Prq_NotOnHold);
		LotServiceProxy.getLotService().update(lotData);
		// --------------------------------------------------------------------------------------------------------------------------------------------
		
		MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);
		
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// Commented by smkang on 2018.11.09 - Because a lot of Scrapped state can be split, the lot will be changed to Released state temporarily.
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
		lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
		
		// Added by smkang on 2018.11.13 - Because split is executed a lot is in scrapped state, decrementQuantity behavior is removed from SPECIAL behavior for avoid exception in greenTrack API.
		//								   But DestinationLotName is updated in decrementQuantity behavior.
		//								   So DestinationLotName of source lot should be updated manually.
		lotData.setDestinationLotName(childLotNameList.get(0));

		if (StringUtils.isNotEmpty(lotData.getDestinationLotName()))
			lotData.setLastEventFlag("S");
		
		lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
		lotData.setLotProcessState("");
		lotData.setLotHoldState("");
		lotData.getUdfs().put("NOTE", "");
		LotServiceProxy.getLotService().update(lotData);
		
		// Commented by smkang on 2018.11.11 - After split lot is executed, lot state is changed to Emptied, so lot history will be updated manually.
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Object[] bindSet = new Object[] {lotData.getKey().getLotName(), eventInfo.getEventTimeKey()};				
//		LotHistory sourceLotHistory = LotServiceProxy.getLotHistoryService().select("WHERE LOTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
		LotHistoryKey lotHistoryKey = new LotHistoryKey();
		lotHistoryKey.setLotName(lotData.getKey().getLotName());
		lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
		LotHistory sourceLotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
		
		// Added by smkang on 2018.11.13 - Because split is executed a lot is in scrapped state, decrementQuantity behavior is removed from SPECIAL behavior for avoid exception in greenTrack API.
		//								   But DestinationLotName is updated in decrementQuantity behavior.
		//								   So DestinationLotName of source lot should be updated manually.
		sourceLotHistory.setDestinationLotName(lotData.getDestinationLotName());
		sourceLotHistory.setEventFlag(lotData.getLastEventFlag());
		
		sourceLotHistory.setLotState(lotData.getLotState());
		sourceLotHistory.setLotProcessState(lotData.getLotProcessState());
		sourceLotHistory.setLotHoldState(lotData.getLotHoldState());
		LotServiceProxy.getLotHistoryService().update(sourceLotHistory);
		// --------------------------------------------------------------------------------------------------------------------------------------------
		
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// Commented by smkang on 2018.11.09 - Because a lot of Scrapped state can be split, the lot will be changed to Released state temporarily.
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot splitLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(childLotNameList.get(0));
		Lot splitLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(childLotNameList.get(0)));
		
		// Added by smkang on 2018.11.12 - If ProcessOperationName is null, ProcessOperationName and version would be set the last operation of this ProcessFlow automatically after split is executed.
		//								   So ProcessOperationName and ProcessOperationVersion should be updated with the information of source lot manually.
		splitLotData.setProcessOperationName(lotData.getProcessOperationName());
		splitLotData.setProcessOperationVersion(lotData.getProcessOperationVersion());
		
		splitLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
		splitLotData.setLotProcessState("");
		splitLotData.setLotHoldState("");
		LotServiceProxy.getLotService().update(splitLotData);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Object[] bindSet = new Object[] {splitLotData.getKey().getLotName(), eventInfo.getEventTimeKey()};				
//		LotHistory childLotHistory = LotServiceProxy.getLotHistoryService().select("WHERE LOTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
		LotHistoryKey childLotHistoryKey = new LotHistoryKey();
		childLotHistoryKey.setLotName(splitLotData.getKey().getLotName());
		childLotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
		LotHistory childLotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(childLotHistoryKey);
		
		childLotHistory.setProcessOperationName(splitLotData.getProcessOperationName());
		childLotHistory.setProcessOperationVersion(splitLotData.getProcessOperationVersion());
		childLotHistory.setLotState(splitLotData.getLotState());
		childLotHistory.setLotProcessState(splitLotData.getLotProcessState());
		childLotHistory.setLotHoldState(splitLotData.getLotHoldState());
		LotServiceProxy.getLotHistoryService().update(childLotHistory);
		
		if (splitLotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Completed)) {
			for (ProductP productP : productPSequence) {
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Object[] bindSet = new Object[] {productP.getProductName(), eventInfo.getEventTimeKey()};
//				ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().select("WHERE PRODUCTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
				ProductHistoryKey productHistoryKey = new ProductHistoryKey();
				productHistoryKey.setProductName(productP.getProductName());
				productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
				ProductHistory productHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
				
				productHistory.setProcessOperationName(splitLotData.getProcessOperationName());
				productHistory.setProcessOperationVersion(splitLotData.getProcessOperationVersion());
				productHistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
				productHistory.setProductProcessState("");
				productHistory.setProductHoldState("");
				ProductServiceProxy.getProductHistoryService().update(productHistory);
			}
		}
		// --------------------------------------------------------------------------------------------------------------------------------------------
		
		return splitLotData;
	}
	
	private Lot mergeLot(EventInfo eventInfo, Lot sourceLotData, Lot targetLotData, List<Product> unScrapProductDataList, List<ProductP> productPSequence) throws CustomException {
		eventInfo.setBehaviorName("SPECIAL");
		eventInfo.setEventName("MergeByUnScrap");
		
		TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();
		transferProductsToLotInfo.setDestinationLotName(targetLotData.getKey().getLotName());
		transferProductsToLotInfo.setProductQuantity(productPSequence.size());
		transferProductsToLotInfo.setEmptyFlag("Y");
		transferProductsToLotInfo.setValidationFlag("Y");
		transferProductsToLotInfo.setProductPSequence(productPSequence);
		
		// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721 
		//LotServiceProxy.getLotService().transferProductsToLot(sourceLotData.getKey(), eventInfo, transferProductsToLotInfo);
		MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, sourceLotData, transferProductsToLotInfo);
		// End 2019.09.09 Modify By Park Jeong Su Mantis 4721
		
		sourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotData.getKey().getLotName());
		targetLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(targetLotData.getKey().getLotName());
		
		// Added by smkang on 2018.11.13 - If LotState is not Released, although all products are moved to a target lot, LotState wouldn't be changed to Emptied state.
		//								   So it is necessary to be updated manually.
		try {
			ProductServiceProxy.getProductService().select("LOTNAME = ?", new Object[] {sourceLotData.getKey().getLotName()});
		} catch (Exception e) {
			sourceLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Emptied);
			sourceLotData.setLotProcessState("");
			sourceLotData.setLotHoldState("");
			sourceLotData.setProductQuantity(0);
			sourceLotData.setSubProductQuantity(0);
			sourceLotData.setSubProductQuantity1(0);
			sourceLotData.setSubProductQuantity2(0);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Object[] bindSet = new Object[] {sourceLotData.getKey().getLotName(), eventInfo.getEventTimeKey()};				
//			LotHistory sourceLotHistory = LotServiceProxy.getLotHistoryService().select("WHERE LOTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
			LotHistoryKey lotHistoryKey = new LotHistoryKey();
			lotHistoryKey.setLotName(sourceLotData.getKey().getLotName());
			lotHistoryKey.setTimeKey(eventInfo.getEventTimeKey());			
			LotHistory sourceLotHistory = LotServiceProxy.getLotHistoryService().selectByKeyForUpdate(lotHistoryKey);
			
			sourceLotHistory.setLotState(sourceLotData.getLotState());
			sourceLotHistory.setLotProcessState(sourceLotData.getLotProcessState());
			sourceLotHistory.setLotHoldState(sourceLotData.getLotHoldState());
			sourceLotHistory.setProductQuantity(sourceLotData.getProductQuantity());
			sourceLotHistory.setSubProductQuantity(sourceLotData.getSubProductQuantity());
			sourceLotHistory.setSubProductQuantity1(sourceLotData.getSubProductQuantity1());
			sourceLotHistory.setSubProductQuantity2(sourceLotData.getSubProductQuantity2());
			LotServiceProxy.getLotHistoryService().update(sourceLotHistory);
		}
		
		// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		sourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotData.getKey().getLotName());
//
//		sourceLotData.getUdfs().put("NOTE", "");
//		LotServiceProxy.getLotService().update(sourceLotData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(sourceLotData, updateUdfs);
		
		if (StringUtils.equals(targetLotData.getLotState(), "Completed")) {
			for(Product unScrapProductData : unScrapProductDataList) {
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product mergeProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(unScrapProductData.getKey().getProductName());
				Product mergeProductData = ProductServiceProxy.getProductService().selectByKeyForUpdate(unScrapProductData.getKey());
				
				mergeProductData.setProcessOperationName("-");
				mergeProductData.setProcessOperationVersion("");
				mergeProductData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
				mergeProductData.setProductProcessState("");
				mergeProductData.setProductHoldState("");
				
				ProductServiceProxy.getProductService().update(mergeProductData);
				
				// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//                Object[] bindSet = new Object[] {mergeProductData.getKey().getProductName(), eventInfo.getEventTimeKey()};				
//    			ProductHistory mergeProductHistory = ProductServiceProxy.getProductHistoryService().select("WHERE PRODUCTNAME = ? AND TIMEKEY = ?", bindSet).get(0);
				ProductHistoryKey productHistoryKey = new ProductHistoryKey();
				productHistoryKey.setProductName(mergeProductData.getKey().getProductName());
				productHistoryKey.setTimeKey(eventInfo.getEventTimeKey());
    			ProductHistory mergeProductHistory = ProductServiceProxy.getProductHistoryService().selectByKeyForUpdate(productHistoryKey);
    			
    			mergeProductHistory.setProcessOperationName(mergeProductData.getProcessOperationName());
    			mergeProductHistory.setProcessOperationVersion(mergeProductData.getProcessOperationVersion());
    			mergeProductHistory.setProductState(mergeProductData.getProductState());
    			mergeProductHistory.setProductProcessState(mergeProductData.getProductProcessState());
    			mergeProductHistory.setProductHoldState(mergeProductData.getProductHoldState());
    			ProductServiceProxy.getProductHistoryService().update(mergeProductHistory);
			}
		}

		// FutureAction
		copyFutureAction(sourceLotData, targetLotData, eventInfo);
		
		return targetLotData;
	}
	
	private Lot assignCarrier(EventInfo eventInfo, Lot unScrappedLotData, Durable durableData, List<ProductP> productPSequence) throws CustomException {
		eventInfo.setBehaviorName("greenTrack");
		eventInfo.setEventName("AssignCarrierByUnScrap");
		
		AssignCarrierInfo assignCarrierInfo = new AssignCarrierInfo();
		assignCarrierInfo.setCarrierName(durableData.getKey().getDurableName());
		assignCarrierInfo.setProductPSequence(productPSequence);
		
		MESLotServiceProxy.getLotServiceImpl().assignCarrier(unScrappedLotData, assignCarrierInfo, eventInfo);
		
		return MESLotServiceProxy.getLotServiceUtil().getLotData(unScrappedLotData.getKey().getLotName());
	}
	
	private void adjustProductRequest(EventInfo eventInfo, Lot unScrappedLotData, List<Element> productElementList) throws CustomException {
		eventInfo.setBehaviorName("greenTrack");
		eventInfo.setEventName("UnScrap");
		
        /**** 2019.02.22_hsryu_adjust WO Quantity. for Mixed WO Glass in a Lot.****/
		Map<String ,Integer> woHashMap = new HashMap<String, Integer>();
		
		for (Element productElement : productElementList) {
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			if(StringUtils.isNotEmpty(productData.getProductRequestName())){
    			if(!woHashMap.containsKey(productData.getProductRequestName()))
    				woHashMap.put(productData.getProductRequestName(), 1);
    			else
    				woHashMap.put(productData.getProductRequestName(), (woHashMap.get(productData.getProductRequestName()))+1);
        	}
		}
		
		for(String key : woHashMap.keySet()){	
			if (!unScrappedLotData.getProductionType().equals("MQCA"))
	            MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(key, null, "S", -(woHashMap.get(key)), eventInfo);
		}
		/***************************************************************************/

		//2019.02.22_hsryu_Delete Logic. 
		// Adjust ProductRequest Quantity
		//MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(unScrappedLotData.getProductRequestName(), null, "S", -(productElementList.size()), eventInfo);
	}
	
	private String compareMergeCondition(Lot sourceLotData, Lot targetLotData) throws CustomException {
		String validationResult = "";
		if (!sourceLotData.getProductSpecName().equals(targetLotData.getProductSpecName()))
			validationResult = validationResult.concat(" ProductSpec(".concat(sourceLotData.getProductSpecName()).concat("->").concat(targetLotData.getProductSpecName()).concat(")"));
		
		if (!sourceLotData.getUdfs().get("ECCODE").equals(targetLotData.getUdfs().get("ECCODE")))
			validationResult = validationResult.concat(" ECCode(".concat(sourceLotData.getUdfs().get("ECCODE")).concat("->").concat(targetLotData.getUdfs().get("ECCODE")).concat(")"));
		
		if (!sourceLotData.getProcessFlowName().equals(targetLotData.getProcessFlowName()))
			validationResult = validationResult.concat(" ProcessFlow(".concat(sourceLotData.getProcessFlowName()).concat("->").concat(targetLotData.getProcessFlowName()).concat(")"));
		
		if (!sourceLotData.getProcessOperationName().equals(targetLotData.getProcessOperationName()))
			validationResult = validationResult.concat(" ProcessOperation(".concat(sourceLotData.getProcessOperationName()).concat("->").concat(targetLotData.getProcessOperationName()).concat(")"));
		
		//2019.02.25_hsryu_Delete Logic. Mantis 0002757.
//		if (!sourceLotData.getProductRequestName().equals(targetLotData.getProductRequestName()))
//			validationResult = validationResult.concat(" WorkOrder(".concat(sourceLotData.getProductRequestName()).concat("->").concat(targetLotData.getProductRequestName()).concat(")"));

		//2019.02.25_hsryu_Insert Logic. Mantis 0002757.
		if(!StringUtils.equals(CommonUtil.getWorkOrderType(sourceLotData), CommonUtil.getWorkOrderType(targetLotData)))
			validationResult = validationResult.concat(" WorkOrderType(".concat(CommonUtil.getWorkOrderType(sourceLotData)).concat("->").concat(CommonUtil.getWorkOrderType(targetLotData)).concat(")"));

		if (!sourceLotData.getLotGrade().equals(targetLotData.getLotGrade()))
			validationResult = validationResult.concat(" LotGrade(".concat(sourceLotData.getLotGrade()).concat("->").concat(targetLotData.getLotGrade()).concat(")"));
		
		// Added by smkang on 2018.11.15 - Need to add validation of DEPARTMENTNAME.
		if (!sourceLotData.getUdfs().get("DEPARTMENTNAME").equals(targetLotData.getUdfs().get("DEPARTMENTNAME")))
			validationResult = validationResult.concat(" Department(".concat(sourceLotData.getUdfs().get("DEPARTMENTNAME")).concat("->").concat(targetLotData.getUdfs().get("DEPARTMENTNAME")).concat(")"));
		
		return validationResult;
	}
	
	// Added by smkang on 2018.11.11 - Copy from MergeLot.java
	private void copyFutureAction(Lot sourceLotData, Lot targetLotData, EventInfo eventInfo) {
		List<LotAction> lotActionList = new ArrayList<LotAction>();
		List<LotAction> lotActionList2 = new ArrayList<LotAction>();
		long lastPosition= 0;

		String condition = " WHERE lotName = ? AND factoryName = ? AND actionState = ? ";
		Object[] bindSet = new Object[]{ sourceLotData.getKey().getLotName(), sourceLotData.getFactoryName(), "Created" };

		try
		{
			lotActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);
			
			for(int i=0; i<lotActionList.size();i++)
			{
				LotAction lotaction = new LotAction();
				lotaction = lotActionList.get(i);

				String condition2 = "WHERE lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND "
						+ "processOperationName = ? AND processOperationVersion = ? AND actionState = ? ";
				
				Object[] bindSet2 = new Object[]{ targetLotData.getKey().getLotName(), targetLotData.getFactoryName() ,lotaction.getProcessFlowName(),
						lotaction.getProcessFlowVersion(), lotaction.getProcessOperationName(), lotaction.getProcessOperationVersion(), "Created" };
				
				try
				{
					lotActionList2 = ExtendedObjectProxy.getLotActionService().select(condition2, bindSet2);
					
					for(int j=0; j<lotActionList2.size(); j++)
					{
						LotAction lotAction2 = new LotAction();
						lotAction2 = lotActionList2.get(j);
						
						if(StringUtil.equals(lotAction2.getActionName(), lotaction.getActionName()))
						{
							lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(targetLotData,lotaction.getProcessFlowName(),lotaction.getProcessOperationName()));

							lotaction.setLotName(targetLotData.getKey().getLotName());
							lotaction.setPosition(lastPosition+1);
							lotaction.setLastEventTime(eventInfo.getEventTime());
							
							// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//							lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
							lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
							
							ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
						}
					}
				}
				catch(Throwable e)
				{
					lotaction.setLotName(targetLotData.getKey().getLotName());
					lotaction.setLastEventTime(eventInfo.getEventTime());
					
					// Modified by smkang on 2019.05.17 - EventInfo.LastEventTimekey is null.
//					lotaction.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					lotaction.setLastEventTimeKey(eventInfo.getEventTimeKey());
					
					ExtendedObjectProxy.getLotActionService().create(eventInfo, lotaction);
				}
			}
		}
		catch (Throwable e)
		{
			return;
		}
	}
}