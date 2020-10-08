package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelRework extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String lotName 			   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName 	   = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String xNodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", false);// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致

		
		// Modified by smkang on 2019.06.17 - According to Liu Hongwei's request, Lot should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		eventLog.debug("Lot will be locked to be prevented concurrent executing.");
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		eventLog.debug("Lot is locked to be prevented concurrent executing.");
		
		String beforeNodeId = "";
		List<Map<String, Object>> beforeflowType = null;
		
		Element element = doc.getDocument().getRootElement();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelRework", getEventUser(), getEventComment(), "", "");
		
		// Added by smkang on 2019.05.21 - According to Jiang Haiying's request, after StartCSTInfoCheckRequest is succeeded, 
		//								   HoldLot/ForceSampling/StartBranch/StartRework/ChangeProductSpec/ChangeProcessFlow/ChangeProcessOperation
		//								   PrepareSort/CancelPrepareSort/ForceOutSampling/CancelBranch/CancelRework/CompleteRework should be rejected.
		if (StringUtils.equals(lotData.getUdfs().get("STARTCHECKRESULT"), "Y"))
			throw new CustomException("LOT-9050", lotName);
		
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		if( !xNodeStack.isEmpty() &&  !StringUtils.equals(xNodeStack, lotData.getNodeStack())){
			setLanguage("Chinese");
			throw new CustomException("LOT-8002");
		}
		// 20200323 add by GJJ mantis :5852 卡控nodestack与Server一致
		
		
   		String beforeProcessFlowName = lotData.getProcessFlowName();
   		String beforeProcessOperationName = lotData.getProcessOperationName();
		
		Map<String, String> returnBeforeInfo = MESLotServiceProxy.getLotServiceUtil().getBeforeOperName(lotData.getFactoryName(), returnFlowName, "00001", returnOperationName, "00001");
		String beforeOper = returnBeforeInfo.get("PROCESSOPERATIONNAME");

		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		List<Product> ProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		List<Product> ProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
		
		if(!StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion()).toUpperCase(), "REWORK"))
		{
			throw new CustomException("REWORK-0003", lotData.getKey().getLotName()); 
		}
		
		// 2018.02.19 hsryu - Check First Operation in ReworkFlow
		if(CommonValidation.checkFirstOperation(lotData, "REWORK"))
		{
			throw new CustomException("LOT-9012", lotData.getProcessOperationName()); 
		}
		
		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		
		//Get udfs of Lot Object 
		Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);
		
		List<ProductU> productU = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		
		beforeNodeId = CommonUtil.getNodeStack(lotData.getFactoryName(),returnFlowName, returnOperationName);		

		beforeflowType = MESLotServiceProxy.getLotServiceUtil().getFlowTypebyNodeID(beforeNodeId);
		
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("CancelRework", getEventUser(), getEventComment(), "", "");
		eventInfo1.setBehaviorName("ARRAY");

		if(((String)beforeflowType.get(0).get("PROCESSFLOWTYPE")).equals(GenericServiceProxy.getConstantMap().Arc_Rework))
		{
			List<String> allProductListNames = new ArrayList<String>();
			
			for(Product product : ProductList)
			{
				allProductListNames.add(product.getKey().getProductName());
			}
			
			String nodeStack = GetRemoveCurrentNodeIdNodeStack(lotData);
			
			lotData.setProcessFlowName(returnFlowName);
			lotData.setProcessOperationName(returnOperationName);
			lotData.setNodeStack(nodeStack);
			lotData.setReworkCount(lotData.getReworkCount()-1);
			lotData.setReworkNodeId(beforeNodeId);
			
			LotServiceProxy.getLotService().update(lotData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("RETURNNODESTACK", "");
			setEventInfo.setUdfs(udfs);
			
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo1, setEventInfo);
			
			for(Product allProduct : ProductList)
			{
				allProduct.setProcessFlowName(returnFlowName);
				allProduct.setProcessOperationName(returnOperationName);
				allProduct.setNodeStack(nodeStack);
				allProduct.setReworkCount(lotData.getReworkCount());
				allProduct.setReworkNodeId(beforeNodeId);
				
//				String originalProductGrade = MESLotServiceProxy.getLotServiceUtil().getOriginalProductGradeByReworkProduct
//						(allProduct, lotName, lotData.getFactoryName(), returnFlowName, returnOperationName, lotData.getProcessFlowName(), lotData.getProcessOperationName());
//				
//				if(currentReworkProducts.contains(allProduct.getKey().getProductName()))
//				{
//					allProduct.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
//				}
				
				ProductServiceProxy.getProductService().update(allProduct);
				
				kr.co.aim.greentrack.product.management.info.SetEventInfo productSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(allProduct.getKey(), eventInfo1, productSetEventInfo);
			}
		}
		else
		{
			String originalLotGrade = MESLotServiceProxy.getLotServiceUtil().getOriginalLotGradeByReworkLot
					(lotData, lotData.getFactoryName(), returnFlowName, beforeOper, lotData.getProcessFlowName(), lotData.getProcessOperationName());

			// if changeGrade S->G Rework, ReworkCount not increase.
			if(lotData.getReworkCount()>0)
			lotData.setReworkCount(lotData.getReworkCount()-1);
			
			//2018.11.12_hsryu_return OriginalLotGrade
			if(!StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
			{
				if(StringUtils.isNotEmpty(originalLotGrade))
				{
					if(!StringUtils.equals(originalLotGrade, GenericServiceProxy.getConstantMap().LotGrade_R))
					{
						lotData.setLotGrade(originalLotGrade);
					}
					else
					{
						lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_G);
					}
				}
				else
				{
					lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_G);
				}
			}

			LotServiceProxy.getLotService().update(lotData);
			
			for (Product product : ProductList)
			{
				String originalProductGrade = MESLotServiceProxy.getLotServiceUtil().getOriginalProductGradeByReworkProduct
						(product, lotName, lotData.getFactoryName(), returnFlowName, beforeOper, lotData.getProcessFlowName(), lotData.getProcessOperationName());
				
				//2018.11.12_hsryu_return OriginalProductGrade
//				if(currentReworkProducts.contains(allProduct.getKey().getProductName()))
//				{
//					allProduct.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
//				}
				
				if(!StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S))
				{
					if(StringUtils.isNotEmpty(originalProductGrade))
					{
						if(!StringUtils.equals(originalProductGrade, GenericServiceProxy.getConstantMap().ProductGrade_R))
						{
							product.setProductGrade(originalProductGrade);
						}
						else
						{
							product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
						}
					}
					else
					{
						product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
					}
				}
				
				product.setReworkCount(lotData.getReworkCount());
				ProductServiceProxy.getProductService().update(product);
			}
			
			eventInfo1.setBehaviorName("ARRAY");
			
			MakeNotInReworkInfo makeNotInReworkInfo = 
					MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfo(lotData, 
																			eventInfo1, 
																			lotName, 
																			returnFlowName, 
																			returnOperationName,   
																			udfs, 
																			productU);

			Lot aLot = MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo1, lotData, makeNotInReworkInfo);
		}
		
		ExtendedObjectProxy.getreworkLotService().decreaseReworkCount(ProductList, lotData, lotData.getProcessFlowName(), lotData.getProcessOperationName(), returnFlowName, beforeOper, eventInfo);

		//delete ReturnInformation
		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		// 2019.05.30_Delete Logic. 
//		if(!MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo))
//		{
//			if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo))
//			{
//				//Reserve Change
//				afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
//			}
//		}
		
		// 2019.05.30_hsryu_Modify Logic. if TrackOut and Sampling, Not Execute BHold. So, Check Sampling and BHold at the same time.
		boolean isCheckSampling = false;
		
		isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo1);
		
		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo1) && !isCheckSampling ){
			afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo1);
		}
		
		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo1);

		return doc;
	}
	
	private String GetRemoveCurrentNodeIdNodeStack(Lot lotData)
	{
		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData
				.getNodeStack());

		int lastIndex = nodeStack.size();
		nodeStack.remove(lastIndex - 1);

		String strNodeStack = NodeStack.nodeStackToString(nodeStack);
	
		return strNodeStack;
	}
}
