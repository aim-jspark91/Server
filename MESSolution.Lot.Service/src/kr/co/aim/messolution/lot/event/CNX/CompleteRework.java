package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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

public class CompleteRework extends SyncHandler {

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
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteRework", getEventUser(), getEventComment(), "", "");
		eventInfo.setBehaviorName("ARRAY");
		
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
		
   		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		List<Product> ProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		List<Product> ProductList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
		
		if(!StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion()).toUpperCase(), "REWORK"))
		{
			throw new CustomException("REWORK-0003", lotData.getKey().getLotName()); 
		}
		
		// 2018.02.19 hsryu - Check First Operation in ReworkFlow
		if(!CommonValidation.checkFirstOperation(lotData, "REWORK"))
		{
			throw new CustomException("LOT-9013", lotData.getProcessOperationName()); 
		}
		
		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		
		//Get udfs of Lot Object 
		Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);
		
		List<ProductU> productU = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(doc);
		
		//2017.7.20 zhongsl  when Operation Changed, Update Product ProcessingInfo to N
		productU = MESLotServiceProxy.getLotInfoUtil().setProductUdfsProcessingInfo(productU, "");
		
		beforeNodeId = MESLotServiceProxy.getLotServiceUtil().getBeforeNodeStack(lotData);
		beforeflowType = MESLotServiceProxy.getLotServiceUtil().getFlowTypebyNodeID(beforeNodeId);
		
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		udfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
//		udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		
		if(((String)beforeflowType.get(0).get("PROCESSFLOWTYPE")).equals(GenericServiceProxy.getConstantMap().Arc_Rework))
		{
			List<String> allProductListNames = new ArrayList<String>();
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("BEFOREFLOWNAME",  lotData.getProcessFlowName());
			setEventInfo.getUdfs().put("BEFOREOPERATIONNAME",  lotData.getProcessOperationName());

			for(Product product : ProductList)
			{
				allProductListNames.add(product.getKey().getProductName());
			}
			
			String nodeStack = GetRemoveCurrentNodeIdNodeStack(lotData);
			
			lotData.setProcessFlowName(returnFlowName);
			lotData.setProcessOperationName(returnOperationName);
			lotData.setNodeStack(nodeStack);
			lotData.setReworkNodeId(beforeNodeId);
			
			LotServiceProxy.getLotService().update(lotData);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
						
			for(Product product : ProductList)
			{
				product.setProcessFlowName(returnFlowName);
				product.setProcessOperationName(returnOperationName);
				product.setNodeStack(nodeStack);
				product.setReworkNodeId(beforeNodeId);
				
				ProductServiceProxy.getProductService().update(product);
				
				kr.co.aim.greentrack.product.management.info.SetEventInfo productSetEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, productSetEventInfo);
			}
		}
		else
		{
			if(!StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_S))
			{
				lotData.setLotGrade(GenericServiceProxy.getConstantMap().LotGrade_G);
				LotServiceProxy.getLotService().update(lotData);
				
				for (Product product : ProductList)
				{					
					if(!StringUtils.equals(product.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S))
					{
						product.setProductGrade(GenericServiceProxy.getConstantMap().ProductGrade_G);
						ProductServiceProxy.getProductService().update(product);
					}
				}
			}
			
			MakeNotInReworkInfo makeNotInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfo(lotData, eventInfo, lotName, returnFlowName, returnOperationName, udfs, productU);

			MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo, lotData, makeNotInReworkInfo);
		}

		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		Lot afterLotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
		
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
		boolean isCheckSampling = MESLotServiceProxy.getLotServiceUtil().checkSampling(lotName,eventInfo);
		
		if(!MESLotServiceProxy.getLotServiceUtil().isExistBholdorOperHold(lotName, eventInfo) && !isCheckSampling ){
			afterLotData = MESLotServiceProxy.getLotServiceUtil().executeLotFutureActionChange(afterLotData.getKey().getLotName(), eventInfo);
		}
		
		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(afterLotData.getKey().getLotName(), afterLotData.getFactoryName(), beforeProcessFlowName,beforeProcessOperationName, eventInfo);

		return doc;
	}
	
	private String GetRemoveCurrentNodeIdNodeStack(Lot lotData)
	{
		NodeStack nodeStack = NodeStack.stringToNodeStack(lotData.getNodeStack());

		int lastIndex = nodeStack.size();
		nodeStack.remove(lastIndex - 1);

		return NodeStack.nodeStackToString(nodeStack);
	}
}