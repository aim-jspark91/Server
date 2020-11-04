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
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
 
public class ChangeGrade extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		// Modified by smkang on 2018.08.16 - Business logic is wrong.
//		String lotName 	   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);		
//		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", false);		
//		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
//		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
//		 
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", getEventUser(), getEventComment(), "", "");
//		
//		for (Element eleLot : lotList) 
//		{
//			lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
//			lotGrade = SMessageUtil.getChildText(eleLot, "LOTGRADE", true);
//			
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//			
//			//validation.
//			CommonValidation.checkLotShippedState(lotData); 
//			
//			if(StringUtil.equals(lotData.getLotGrade(), "S"))
//				throw new CustomException("SYS-9999", "Lot", "Lot Judge is S, Not Available Change Grade");
//			
//			if(StringUtil.equals(lotData.getReworkState(),"NotInRework"))
//			{
//				SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotName);
//				SMessageUtil.setBodyItemValue(doc, "LOTGRADE", lotGrade);
//				
//				List<ProductPGS> productPGS = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence(doc);
//				
//				ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);
//				
//				Lot aLot = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
//			}
//			
//			if(StringUtil.equals(lotData.getReworkState(),"InRework"))
//			{
//				if(productList!=null)
//				{
//					for(Element productElement : productList)
//					{
//						String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
//						String position = SMessageUtil.getChildText(productElement, "POSITION", true);
//						String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTGRADE", false);
//						String reasonCode = SMessageUtil.getChildText(productElement, "REASONCODE", false);
//						String areaName = SMessageUtil.getChildText(productElement, "AREA", false);
//						String machineName = SMessageUtil.getChildText(productElement, "MAHINENAME", false);
//						String departmentName = SMessageUtil.getChildText(productElement, "DEPARTMENTNAME", false);
//
//						Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
//						if(StringUtil.equals(productGrade, "S"))
//						{
//							productData.setAreaName(areaName);
//							productData.setMachineName(machineName);
//							productData.setReasonCode(reasonCode);
//						}
//						
//						if(!productGrade.isEmpty() && StringUtil.equals(lotName, productData.getLotName()))
//						{
//							Map<String, String> productUdfs = productData.getUdfs();
//							productUdfs.put("REWORKGRADE", productGrade);
//							
//							if(StringUtil.equals(productGrade, "S"))
//								productUdfs.put("SCRAPDEPARTMENTNAME", departmentName);
//							
//							productData.setUdfs(productUdfs);
//							
//							SetEventInfo setEventInfo = new SetEventInfo();
//							setEventInfo.setUdfs(productUdfs);
//							MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);	
//						}
//					}
//					
//					kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//					Map<String, String> lotUdfs = lotData.getUdfs();
//					lotUdfs.put("REWORKGRADE", lotGrade);
//					setEventInfo.setUdfs(lotUdfs);
//					LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
//				}
//			}
//
//			if(productList!=null)
//			{
//				for(Element productElement : productList)
//				{
//					String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
//					String position = SMessageUtil.getChildText(productElement, "POSITION", true);
//					String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTGRADE", false);
//					String reasonCode = SMessageUtil.getChildText(productElement, "REASONCODE", false);
//					String areaName = SMessageUtil.getChildText(productElement, "AREANAME", false);
//					String machineName = SMessageUtil.getChildText(productElement, "MACHINENAME", false);
//					String departmentName = SMessageUtil.getChildText(productElement, "DEPARTMENTNAME", false);
//
//					Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
//					
//					if(StringUtil.equals(productGrade, "S"))
//					{
//						productData.setAreaName(areaName);
//						productData.setMachineName(machineName);
//						productData.setReasonCode(reasonCode);
//						Map<String, String> productUdfs = productData.getUdfs();
//						productUdfs.put("SCRAPDEPARTMENTNAME", departmentName);
//						
//						ProductServiceProxy.getProductService().update(productData);
//						
//						String pCondition = "where productname=?" + " and timekey=(select max(timekey) from lothistory where productname=? and eventname = ? ) " ;
//						Object[] pBindSet = new Object[]{productData.getKey().getProductName(),productData.getKey().getProductName(),"ChangeGrade"};
//						List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
//						ProductHistory producthistory = pArrayList.get(0);
//						producthistory.setReasonCode(reasonCode);
//						producthistory.setAreaName(areaName);
//						producthistory.setMachineName(machineName);
//						ProductServiceProxy.getProductHistoryService().update(producthistory);
//					}
//				}
//			}
//		}
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);		
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", false);		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String scrapMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String scrapDepartmentName = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", false);
		boolean moveFlag = false;
		String sNodeStack = "";
		ScrapProduct tempScrapProduct=null;
		
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String logicalSlotMap = "";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", getEventUser(), getEventComment(), "", "");
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		
		//start modify by jhiying on20191016 mantis:5016
		if(StringUtil.equals(lotData.getLotState(),"Received"))
		{
		throw new CustomException("LOT-4004", "");
		 }
		//end modify by jhiying on20191016 mantis:5016
		
		
		if (productElementList != null && productElementList.size() > 0) {
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
			List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotName);
			
			for (Element productElement : productElementList) {
				String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
				String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTGRADE", false);
				
				for (Product product : productList) {
					if (product.getKey().getProductName().equals(productName)) {
						if (StringUtils.isNotEmpty(productGrade) && !product.getProductGrade().equals(productGrade)) {
							if (productGrade.equals("S")) {
								CommonValidation.checkProductRequestStateClosed(product.getKey().getProductName());
				                product.setReasonCode(reasonCode);								
								ScrapProduct scrapProduct = new ScrapProduct();
												                
				                try
				                {
				                	scrapProduct = ExtendedObjectProxy.getScrapProductService().selectByKey(false, new Object[] {productName});
				                }
				                catch(Throwable e)
				                {
				                	scrapProduct = null;
				                }
				                
				                if(scrapProduct!=null)
				                	this.ModifyScrapProductInfo(product, scrapProduct, scrapMachineName,scrapDepartmentName,note,eventInfo);
				                else
				                	this.CreateScrapProductInfo(product,scrapDepartmentName,scrapMachineName,note, eventInfo);

				                eventInfo.setEventName("ChangeSGrade");
							} 
							else 
							{
								eventInfo.setEventName("ChangeGrade");
								CommonValidation.checkProductRequestStateClosed(product.getKey().getProductName());//add by jhying on20200331 mantis:5905
								if(StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
								{
									ScrapProduct scrapProduct = new ScrapProduct();
									String returnOperation = "";
									String returnFlowName = "";
									String returnProductionType = "";
									String returnProductSpec = "";
									String returnProductRequestName = "";
									String returnNodeStack = "";
					                
					                try
					                {
					                	scrapProduct = ExtendedObjectProxy.getScrapProductService().selectByKey(false, new Object[] {productName});
					                    returnProductionType = scrapProduct.getProductionType();
					                    returnProductSpec = scrapProduct.getProductSpecName();
					                    returnFlowName = scrapProduct.getProcessFlowName();
					                    returnOperation = scrapProduct.getProcessOperationName();
					                    returnNodeStack = scrapProduct.getNodeStack();
					                }
					                catch(Throwable e)
					                {
					                	scrapProduct = null;
					                	eventLog.warn("Not exist ScrapGlassInfo.. - GlassName [" + product.getKey().getProductName() + "]. Serch ProductHistory 'ChangeSGrade'");
					                }
					                
					                if(scrapProduct==null)
					                {
										//history에서 grade가 S이고 event가 changegrade인 것을 찾아 해당 히스토리의 operation으로 수정(돌아가려는  operetion이랑 현재 operation이 같아야 수정할수 있음)
										String pCondition = " where productname = ? and timekey = (select max(timekey) from producthistory where productname = ? and eventname = ? and productgrade = ?)" ;
					                    Object[] pBindSet = new Object[]{product.getKey().getProductName(),product.getKey().getProductName(),"ChangeSGrade","S"};
					                    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
					                    ProductHistory producthistory = pArrayList.get(0);
					                    returnOperation = producthistory.getProcessOperationName();
					                    returnNodeStack = scrapProduct.getNodeStack();
					                }
									
					                if(!StringUtils.equals(product.getProcessOperationName(), returnOperation)
					                 ||!StringUtils.equals(product.getProcessFlowName(), returnFlowName)
					                 ||!StringUtils.equals(product.getProductSpecName(), returnProductSpec)
					                 ||!StringUtils.equals(product.getProductionType(), returnProductionType)
					                 //2019.02.25_hsryu_Delete Condition. Mantis 0002757.
					                 //||!StringUtils.equals(product.getProductRequestName(), returnProductRequestName)
					                 )
									{
				                    	//check ReturnNodeStack all Glass. if not same, Error!
				                    	if(StringUtil.isNotEmpty(sNodeStack))
				                    	{
				                    		if(!StringUtil.equals(sNodeStack, returnNodeStack))
				                    		{
				                    			throw new CustomException("PRODUCT-0052");
				                    		}
				                    	}
				                    	
				                    	//check Glass Spec. all Glass same Spec... 
				                    	if(tempScrapProduct != null)
				                    	{
				                    		if(!StringUtil.equals(tempScrapProduct.getProductionType(), scrapProduct.getProductionType()))
				                    			throw new CustomException("COMMON-0001","Mixed another ProductionType");
				                    		if(!StringUtil.equals(tempScrapProduct.getProductSpecName(), scrapProduct.getProductSpecName()))
				                    			throw new CustomException("COMMON-0001","Mixed another ProductSpecName");
				                    		if(!StringUtil.equals(tempScrapProduct.getProcessFlowName(), scrapProduct.getProcessFlowName()))
				                    			throw new CustomException("COMMON-0001","Mixed another ProcessFlow");
				                    		if(!StringUtil.equals(tempScrapProduct.getProcessFlowVersion(), scrapProduct.getProcessFlowVersion()))
				                    			throw new CustomException("COMMON-0001","Mixed another ProcessFlowVersion");
				                    		if(!StringUtil.equals(tempScrapProduct.getProductState(), scrapProduct.getProductState()))
				                    			throw new CustomException("COMMON-0001","Mixed another ProductState");
				                    		if(!StringUtil.equals(tempScrapProduct.getUdfs().get("ECCODE"), scrapProduct.getUdfs().get("ECCODE")))
				                    			throw new CustomException("COMMON-0001","Mixed another ECCode");
				                    	}
				                    	
				                    	moveFlag = true;
				                    	sNodeStack = returnNodeStack;
				                    	tempScrapProduct = (ScrapProduct)ObjectUtil.copyTo(scrapProduct);
									}
				                    
			                    	this.DeleteSGradeInfo(scrapProduct, eventInfo);

				                    product.setProductGrade(productGrade);
									product.setReasonCode("");
								}
								// change operation 추가  끝
							}
							
							product.setProductGrade(productGrade);

							eventInfo.setReasonCode(product.getReasonCode());
							eventInfo.setEventComment(note);
							ProductServiceProxy.getProductService().update(product);							
							
							SetEventInfo setEventInfo = new SetEventInfo();
							// 2019.06.21_hsryu_delete setUdfs.  udfs must not be changed.
							//setEventInfo.setUdfs(product.getUdfs());
							
							ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo);
							
							// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//							product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(product.getKey().getProductName());
//							
//							//Note clear - JSPARK
//							product.getUdfs().put("NOTE", "");
//							ProductServiceProxy.getProductService().update(product);
							Map<String, String> updateUdfs = new HashMap<String, String>();
							updateUdfs.put("NOTE", "");
							MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(product, updateUdfs);
						}
						
						break;
					}
				}
			}
		}
		
		List<ProductPGS> productPGS = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence(doc);
		
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);
		
		// 2019.06.20_hsryu_Avoid update old udfs.
//		lotData.getUdfs().put("NOTE", note);
//		changeGradeInfo.setUdfs(lotData.getUdfs());
		changeGradeInfo.getUdfs().put("NOTE", note);
		
		if(!StringUtil.equals(lotGrade, "S"))
		{
			eventInfo.setReasonCode("");
			eventInfo.setReasonCodeType("");
		}
		
		lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		
		boolean isReworkFlowFlag = false;
		
		if(moveFlag)
		{
			if(StringUtil.isNotEmpty(sNodeStack))
			{
	            String[] arrNodeStack = StringUtil.split(sNodeStack, ".");
	            int count = arrNodeStack.length;
	            String lastNodeStack = arrNodeStack[arrNodeStack.length-1].toString();

	            Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(lastNodeStack);
	            String flowName = flowMap.get("PROCESSFLOWNAME");
	            String flowVersion = flowMap.get("PROCESSFLOWVERSION");
	            String operationName = flowMap.get("PROCESSOPERATIONNAME");

	            if(!StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), flowName, flowVersion).toUpperCase(), "SORT"))
	            {
	            	lotData = changeOperationForChangeGrade(sFactoryName, lotName, lastNodeStack, tempScrapProduct, eventInfo);
	            }
			}
		}

		
		if(StringUtil.isNotEmpty(lotData.getCarrierName()))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
	        logicalSlotMap = this.getSlotMapInfoForCompleteLot(durableData);
		}
		
        /* Array 20180829, Add [Process Flag Update] ==>> */        
		if(!StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Completed))
		{
			if(StringUtil.isNotEmpty(logicalSlotMap))
			{
		        MESProductServiceProxy.getProductServiceUtil().setProdutProcessFlag(eventInfo, lotData, logicalSlotMap, false);
			}
		}
        /* <<== Array 20180829, Add [Process Flag Update] */
		
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//	    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//		// Lot Note clear Jspark
//		lotData.getUdfs().put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		return doc;
	}

	private Lot StartRework(Lot lotData, EventInfo eventInfo) throws CustomException
	{
		String currentNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(currentNodeStack, ".");
		
		lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_R);	
		lotData.setReworkState(GenericServiceProxy.getConstantMap().Lot_InRework);
		lotData.setReworkNodeId(arrNodeStack[arrNodeStack.length-1]);
		LotServiceProxy.getLotService().update(lotData);

		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		String condition = "where lotname=?" + " and timekey= ? " ;
		String condition = "WHERE LOTNAME = ? AND TIMEKEY = ? FOR UPDATE";

		Object[] bindSet = new Object[]{lotData.getKey().getLotName(),eventInfo.getEventTimeKey()};
		List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
		LotHistory lotHistory = arrayList.get(0);
		lotHistory.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_R);	
		lotHistory.setReworkState(GenericServiceProxy.getConstantMap().Lot_InRework);
		lotHistory.setReworkNodeId(arrNodeStack[arrNodeStack.length-1]);
		LotServiceProxy.getLotHistoryService().update(lotHistory);

		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());

		for(Product productData : pProductList)
		{
			productData.setProductGrade(GenericServiceProxy.getConstantMap().LotGrade_R);
			productData.setReworkState(GenericServiceProxy.getConstantMap().Lot_InRework);
			productData.setReworkNodeId(arrNodeStack[arrNodeStack.length-1]);

			ProductServiceProxy.getProductService().update(productData);

			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			String pCondition = " where productname=?" + " and timekey= ? " ;
			String pCondition = "WHERE PRODUCTNAME = ? AND TIMEKEY = ? FOR UPDATE";

			Object[] pBindSet = new Object[]{productData.getKey().getProductName(), eventInfo.getEventTimeKey()};
			List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
			ProductHistory producthistory = pArrayList.get(0);
			Map<String, String> productHistUdfs = producthistory.getUdfs();
			producthistory.setProductGrade(GenericServiceProxy.getConstantMap().LotGrade_R);
			producthistory.setReworkState(GenericServiceProxy.getConstantMap().Lot_InRework);
			producthistory.setReworkNodeId(arrNodeStack[arrNodeStack.length-1]);
			ProductServiceProxy.getProductHistoryService().update(producthistory);                        
		}
		
		return MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
	}

	private void DeleteSGradeInfo(ScrapProduct scrapProduct, EventInfo eventInfo) throws CustomException
	{
		EventInfo eventInfoForScrapProduct = EventInfoUtil.makeEventInfo("DeleteScrapProduct", getEventUser(), getEventComment(), "", "");
		eventInfoForScrapProduct.setEventTime(eventInfo.getEventTime());
		eventInfoForScrapProduct.setEventTimeKey(eventInfo.getEventTimeKey());

		try
		{
			ExtendedObjectProxy.getScrapProductService().remove(eventInfoForScrapProduct, scrapProduct);
		}
		catch(Throwable e)
		{
			eventLog.error("Fail delete ScrapProductInfo..ProductName : " + scrapProduct.getProductName()); 
		}
	}
	
	private void deleteScrapProduct(ArrayList<ScrapProduct> productList, EventInfo eventInfo) throws CustomException
	{
		for(ScrapProduct scrapProductData : productList)
		{
			eventInfo.setEventName("DeleteScrapProduct");
			
			try
			{
				ExtendedObjectProxy.getScrapProductService().remove(eventInfo, scrapProductData);
			}
			catch(Throwable e)
			{
				eventLog.warn("Product[" + scrapProductData.getProductName() + "] Data is not exist in ScrapProduct.");
			}
		}
	}
	
	private void ModifyScrapProductInfo(Product product, ScrapProduct scrapProduct, String scrapDepartmentName, String scrapMachineName, String note, EventInfo eventInfo) throws CustomException
	{
    	eventLog.warn("Already Exist ScrapGlass Info. Start Modify OriginalInfo");
		
    	eventInfo.setEventName("ModifyScrapProduct");

		boolean isSampleFlow = false;
		
		String processFlowName = product.getProcessFlowName();
		String processFlowVersion = product.getProcessFlowVersion();
		String processOperationName = product.getProcessOperationName();
		String processOperationVersion = product.getProcessOperationVersion();
		String nodeStack = product.getNodeStack();

		if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(product.getFactoryName(), product.getProcessFlowName(), product.getProcessOperationName()).toUpperCase(), "SAMPLING"))
		{
			eventLog.info("Start Insert ScrapProudct.if product in SampleFlow, register MainFlow,MainOper at CT_SCRAPPRODUCT.");
			isSampleFlow = true;

			String currentNodeStack = product.getNodeStack();
			String[] arrNodeStack = StringUtil.split(currentNodeStack, ".");
			
			if(arrNodeStack.length>1)
			{
				nodeStack = arrNodeStack[arrNodeStack.length-2];
	        	//String nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(product.getFactoryName(), nodeStack);
	        	
	        	if(StringUtils.isNotEmpty(nodeStack))
	        	{
					Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(nodeStack);

					String mainFlowName = flowMap.get("PROCESSFLOWNAME");
					String mainFlowNameFlowVersion = flowMap.get("PROCESSFLOWVERSION");
					String mainFlowNameOperationName = flowMap.get("PROCESSOPERATIONNAME");
					String mainFlowNameOperationVersion = flowMap.get("PROCESSOPERATIONVERSION");
					
					if(StringUtils.isNotEmpty(mainFlowNameOperationName))
					{
						processFlowName = mainFlowName;
						processFlowVersion = mainFlowNameFlowVersion;
						processOperationName = mainFlowNameOperationName;
						processOperationVersion = mainFlowNameOperationVersion;
					}
	        	}
			}
			else
			{
				throw new CustomException("COMMON-0001", "currentFlow is SampleFlow. But NodeStack is lack." );
			}
		}
		else
		{
			eventLog.info("Start Insert ScrapProudct.");
		}
    	
		scrapProduct.setFactoryName(product.getFactoryName());
		scrapProduct.setProductionType(product.getProductionType());
		scrapProduct.setProductSpecName(product.getProductSpecName());
		scrapProduct.setProductSpecVersion(product.getProductSpecVersion());
		scrapProduct.setProductRequestName(product.getProductRequestName());
		scrapProduct.setLotName(product.getLotName());
		scrapProduct.setCarrierName(product.getCarrierName());
		scrapProduct.setPosition(product.getPosition());
		scrapProduct.setProductType(product.getProductType());
		scrapProduct.setProductGrade(product.getProductGrade());
		scrapProduct.setPriority(product.getPriority());
		scrapProduct.setDueDate(product.getDueDate());
		scrapProduct.setAreaName(product.getAreaName());
		scrapProduct.setProductState(product.getProductState());
		scrapProduct.setProductProcessState(product.getProductProcessState());
		scrapProduct.setProductHoldState(product.getProductHoldState());
		scrapProduct.setProcessFlowName(processFlowName);
		scrapProduct.setProcessFlowVersion(processFlowVersion);
		scrapProduct.setProcessOperationName(processOperationName);
		scrapProduct.setProcessOperationVersion(processOperationVersion);
		scrapProduct.setNodeStack(nodeStack);
		scrapProduct.setMachineName(product.getMachineName());
		scrapProduct.setEcCode(product.getUdfs().get("ECCODE"));
		scrapProduct.setDummyUsedCount(StringUtils.isEmpty(product.getUdfs().get("DUMMYUSEDCOUNT"))?0:Long.parseLong(product.getUdfs().get("DUMMYUSEDCOUNT")));
		scrapProduct.setMqcCount(StringUtils.isEmpty(product.getUdfs().get("MQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("MQCCOUNT")));
		scrapProduct.setTotalMQCCount(StringUtils.isEmpty(product.getUdfs().get("TOTALMQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("TOTALMQCCOUNT")));
		scrapProduct.setMqcUSEProductSpec(product.getUdfs().get("MQCUSEPRODUCTSPEC"));
		scrapProduct.setMqcUSEECCode(product.getUdfs().get("MQCUSEECCODE"));
		scrapProduct.setMqcUSEProcessFlow(product.getUdfs().get("MQCUSEPROCESSFLOW"));
		scrapProduct.setScrapDepartmentName(scrapMachineName);
		scrapProduct.setScrapMachine(scrapMachineName);
		scrapProduct.setNote(note);
		scrapProduct.setLastEventTime(eventInfo.getEventTime());
		scrapProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
		scrapProduct.setLastEventUser(eventInfo.getEventUser());
		scrapProduct.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getScrapProductService().modify(eventInfo, scrapProduct);


	}
	
	private void CreateScrapProductInfo(Product product, String scrapDepartmentName, String scrapMachineName, String note, EventInfo eventInfo) throws CustomException
	{
		eventInfo.setEventName("CreateScrapProduct");

		boolean isSampleFlow = false;
		
		String processFlowName = product.getProcessFlowName();
		String processFlowVersion = product.getProcessFlowVersion();
		String processOperationName = product.getProcessOperationName();
		String processOperationVersion = product.getProcessOperationVersion();
		String nodeStack = product.getNodeStack();

		if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(product.getFactoryName(), product.getProcessFlowName(), product.getProcessFlowVersion()).toUpperCase(), "SAMPLING"))
		{
			eventLog.info("Start Insert ScrapProudct.if product in SampleFlow, register MainFlow, MainOper at CT_SCRAPPRODUCT.");
			isSampleFlow = true;

			String currentNodeStack = product.getNodeStack();
			String[] arrNodeStack = StringUtil.split(currentNodeStack, ".");
			
			if(arrNodeStack.length>1)
			{
				// 2019.05.06_hsryu_Modify Logic. arrNodeStack.length-2 -> 0.
				//nodeStack = arrNodeStack[arrNodeStack.length-2];
				nodeStack = arrNodeStack[0];
	        	//String nextNodeID = MESLotServiceProxy.getLotServiceUtil().GetReturnAfterNodeStackForSampling(product.getFactoryName(), nodeStack);
	        	
	        	if(StringUtils.isNotEmpty(nodeStack))
	        	{
					Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(nodeStack);

					String FlowName = flowMap.get("PROCESSFLOWNAME");
					String FlowVersion = flowMap.get("PROCESSFLOWVERSION");
					String OperationName = flowMap.get("PROCESSOPERATIONNAME");
					String OperationVersion = flowMap.get("PROCESSOPERATIONVERSION");
					
					ProcessOperationSpec nextOperationData = null;
					
					// 2019.05.06_hsryu_Insert Logic. record NextOperation at 'ScrapProduct' //
			    	try {
			    		nextOperationData = CommonUtil.getNextOperation(product.getFactoryName(), FlowName, OperationName);
					} catch (Exception e) {
						eventLog.warn("fail search NextOperationData ! ");
					}
			    	
			    	if(nextOperationData != null) {
			    		processFlowName = FlowName;
			    		processFlowVersion = FlowVersion;
			    		processOperationName = nextOperationData.getKey().getProcessOperationName();
			    		processOperationVersion = nextOperationData.getKey().getProcessOperationVersion();
			    	}
			    	else {
						processFlowName = FlowName;
						processFlowVersion = FlowVersion;
						processOperationName = OperationName;
						processOperationVersion = OperationVersion;
			    	}
	        	}
			}
			else
			{
				throw new CustomException("COMMON-0001", "currentFlow is SampleFlow. But NodeStack is lack." );
			}
		}
		else
		{
			eventLog.info("Start Insert ScrapProudct.");
		}

		ScrapProduct scrapProduct = new ScrapProduct();

		scrapProduct.setProductName(product.getKey().getProductName());
		scrapProduct.setFactoryName(product.getFactoryName());
		scrapProduct.setProductionType(product.getProductionType());
		scrapProduct.setProductSpecName(product.getProductSpecName());
		scrapProduct.setProductSpecVersion(product.getProductSpecVersion());
		scrapProduct.setProductRequestName(product.getProductRequestName());
		scrapProduct.setLotName(product.getLotName());
		scrapProduct.setCarrierName(product.getCarrierName());
		scrapProduct.setPosition(product.getPosition());
		scrapProduct.setProductType(product.getProductType());
		scrapProduct.setProductGrade(product.getProductGrade());
		scrapProduct.setPriority(product.getPriority());
		scrapProduct.setDueDate(product.getDueDate());
		scrapProduct.setAreaName(product.getAreaName());
		scrapProduct.setProductState(product.getProductState());
		scrapProduct.setProductProcessState(product.getProductProcessState());
		scrapProduct.setProductHoldState(product.getProductHoldState());
		scrapProduct.setProcessFlowName(processFlowName);
		scrapProduct.setProcessFlowVersion(processFlowVersion);
		scrapProduct.setProcessOperationName(processOperationName);
		scrapProduct.setProcessOperationVersion(processOperationVersion);
		scrapProduct.setNodeStack(product.getNodeStack());
		scrapProduct.setMachineName(product.getMachineName());
		scrapProduct.setEcCode(product.getUdfs().get("ECCODE"));
		scrapProduct.setDummyUsedCount(StringUtils.isEmpty(product.getUdfs().get("DUMMYUSEDCOUNT"))?0:Long.parseLong(product.getUdfs().get("DUMMYUSEDCOUNT")));
		scrapProduct.setMqcCount(StringUtils.isEmpty(product.getUdfs().get("MQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("MQCCOUNT")));
		scrapProduct.setTotalMQCCount(StringUtils.isEmpty(product.getUdfs().get("TOTALMQCCOUNT"))?0:Long.parseLong(product.getUdfs().get("TOTALMQCCOUNT")));
		scrapProduct.setMqcUSEProductSpec(product.getUdfs().get("MQCUSEPRODUCTSPEC"));
		scrapProduct.setMqcUSEECCode(product.getUdfs().get("MQCUSEECCODE"));
		scrapProduct.setMqcUSEProcessFlow(product.getUdfs().get("MQCUSEPROCESSFLOW"));
		scrapProduct.setScrapDepartmentName(scrapDepartmentName);
		scrapProduct.setScrapMachine(scrapMachineName);
		scrapProduct.setNote(note);
		scrapProduct.setReasonCode(product.getReasonCode());
		scrapProduct.setReasonCodeType(product.getReasonCodeType());
		scrapProduct.setLastEventName(eventInfo.getEventName());
		scrapProduct.setLastEventTime(eventInfo.getEventTime());
		scrapProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
		scrapProduct.setLastEventUser(eventInfo.getEventUser());
		scrapProduct.setLastEventComment(eventInfo.getEventComment());
		scrapProduct.setLastEventComment(note);

		ExtendedObjectProxy.getScrapProductService().create(eventInfo, scrapProduct);
	}

	private Lot changeOperationForChangeGrade(String sfactoryName, String lotName,String lastNodeStack, ScrapProduct scrapProduct, EventInfo eventInfo) throws CustomException
	{
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		try {
			Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(lastNodeStack);
	
			String flowName = flowMap.get("PROCESSFLOWNAME");
			String flowVersion = flowMap.get("PROCESSFLOWVERSION");
			String operationName = flowMap.get("PROCESSOPERATIONNAME");
			
			eventInfo.setEventName("ChangeOperByChangeGrade");
	
			if (StringUtil.equals(lotData.getLotProcessState(), "RUN"))
				throw new CustomException("LOT-0008", lotName);
	
			Map<String, String> lotUdfs = lotData.getUdfs();
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
	
			//Operation Changed, Update Product ProcessingInfo to N
			productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productUdfs, "");
			
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
			
			if(scrapProduct!=null)
			{
				changeSpecInfo.setAreaName(scrapProduct.getAreaName());
				changeSpecInfo.setFactoryName(scrapProduct.getFactoryName());
				changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
				changeSpecInfo.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
				changeSpecInfo.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
				changeSpecInfo.setPriority(scrapProduct.getPriority());
				changeSpecInfo.setProcessFlowName(scrapProduct.getProcessFlowName());
				changeSpecInfo.setProcessFlowVersion(scrapProduct.getProcessFlowVersion());
				changeSpecInfo.setProcessOperationName(scrapProduct.getProcessOperationName());
				changeSpecInfo.setProcessOperationVersion(scrapProduct.getProcessOperationVersion());
				changeSpecInfo.setProductionType(scrapProduct.getProductionType());
				//2019.02.22_hsryu_Delete Logic. Mantis 0002757.
				//changeSpecInfo.setProductRequestName(scrapProduct.getProductRequestName());
				changeSpecInfo.setProductSpecName(scrapProduct.getProductSpecName());
				changeSpecInfo.setProductSpecVersion(scrapProduct.getProductSpecVersion());
				//changeSpecInfo.setProductUdfs(productUdfs);
				changeSpecInfo.setNodeStack(scrapProduct.getNodeStack());
				changeSpecInfo.setDueDate(scrapProduct.getDueDate());
				changeSpecInfo.setProductionType(scrapProduct.getProductionType());
	
				eventInfo.setBehaviorName("ARRAY");
	
				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
	
				eventInfo.setBehaviorName("greenTrack");
				
				if(StringUtils.equals(scrapProduct.getProductState(), GenericServiceProxy.getConstantMap().Prod_Completed))
				{
					lotData.setProcessOperationName("-");
					lotData.setProcessOperationVersion("");
					lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
					lotData.setLotProcessState("");
					lotData.setLotHoldState("");
					LotServiceProxy.getLotService().update(lotData);
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
	//				String condition = "where lotname=?" + " and timekey= ? " ;
					String condition = "WHERE LOTNAME = ? AND TIMEKEY = ? FOR UPDATE";
	
					Object[] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getLastEventTimeKey()};
					List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
					LotHistory lotHistory = arrayList.get(0);
					lotHistory.setProcessOperationName("-");
					lotHistory.setProcessOperationVersion("");
					LotServiceProxy.getLotHistoryService().update(lotHistory);
					
					// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
					List<Product> pProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
					
					for(Product productData : pProductList)
					{
						productData.setProcessOperationName("-");
						productData.setProcessOperationVersion("");
						productData.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
						productData.setProductProcessState("");
						productData.setProductHoldState("");
						
						ProductServiceProxy.getProductService().update(productData);
						
						// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
	//                    String pCondition = " where productname=?" + " and timekey= ? " ;
	                    String pCondition = "WHERE PRODUCTNAME = ? AND TIMEKEY = ? FOR UPDATE";
	
	                    Object[] pBindSet = new Object[]{productData.getKey().getProductName(), productData.getLastEventTimeKey()};
	                    List<ProductHistory> pArrayList = ProductServiceProxy.getProductHistoryService().select(pCondition, pBindSet);
	                    ProductHistory producthistory = pArrayList.get(0);
	                    Map<String, String> productHistUdfs = producthistory.getUdfs();
	                    producthistory.setProcessOperationName("-");
	                    producthistory.setProcessOperationVersion("");
	                    producthistory.setProductState(GenericServiceProxy.getConstantMap().Prod_Completed);
	                    producthistory.setProductProcessState("");
	                    producthistory.setProductHoldState("");
	                    ProductServiceProxy.getProductHistoryService().update(producthistory);                        
					}
				}
			}
			else
			{
				changeSpecInfo.setAreaName(lotData.getAreaName());
				changeSpecInfo.setFactoryName(lotData.getFactoryName());
				changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
				changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
				changeSpecInfo.setLotState(lotData.getLotState());
				changeSpecInfo.setPriority(lotData.getPriority());
				changeSpecInfo.setProcessFlowName(flowName);
				changeSpecInfo.setProcessFlowVersion("00001");
				changeSpecInfo.setProcessOperationName(operationName);
				changeSpecInfo.setProcessOperationVersion("00001");
				changeSpecInfo.setProductionType(lotData.getProductionType());
				//changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
				changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
				changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
				changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
				changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				//changeSpecInfo.setProductUdfs(productUdfs);
				changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
				changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
				changeSpecInfo.setNodeStack(lastNodeStack);
				changeSpecInfo.setDueDate(lotData.getDueDate());
	
				eventInfo.setBehaviorName("ARRAY");
	
				lotData = MESLotServiceProxy.getLotServiceUtil().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
	
				eventInfo.setBehaviorName("greenTrack");
			}
			
			//2018.11.13_hsryu_add Check Rework Logic.
			/********************CHECK REWORK*********************/
			boolean isReworkFlowFlag = false;
			
	        if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), flowName, flowVersion).toUpperCase(), "REWORK"))
	        	isReworkFlowFlag = true;
	        
			if(isReworkFlowFlag)
			{
				lotData = this.StartRework(lotData, eventInfo);
			}
			/****************************************************/
		}
		catch(Throwable e)
		{
			eventLog.error(e.getMessage());
		}
		
		return MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
	}

	private String getSlotMapInfoForCompleteLot(Durable durableData)
			throws CustomException
	{
		StringBuffer normalSlotInfoBuffer = new StringBuffer();

			// Get Durable's Capacity
			long iCapacity = durableData.getCapacity(); 
			
			// Get Product's Slot , These are not Scrapped Product.
			List<Product> productList = new ArrayList<Product>();
			
			try
			{
				productList = ProductServiceProxy.getProductService().select("carrierName = ?",
								new Object[] {durableData.getKey().getDurableName()});
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "Product", "No avaliable Product");
			}
			
			// Make Durable Normal SlotMapInfo
			for(int i = 0; i < iCapacity; i++)
			{
				normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
			
			eventLog.debug("Default Slot Map : " + normalSlotInfoBuffer);
			
			for(int i = 0; i < productList.size(); i++)
			{
				try
				{
					int index = (int)productList.get(i).getPosition() - 1;
					
					normalSlotInfoBuffer.replace(index, index+1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
				}
				catch (Exception ex)
				{
					eventLog.error("Position conversion failed");
					normalSlotInfoBuffer.replace(i, i+1, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
				}
			}
			
			eventLog.info("Current Slot Map : " + normalSlotInfoBuffer);
			
			return normalSlotInfoBuffer.toString();
		}
	
	//js

	

}