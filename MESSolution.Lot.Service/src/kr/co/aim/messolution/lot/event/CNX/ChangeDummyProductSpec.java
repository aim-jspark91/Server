package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.jdom.Document;

public class ChangeDummyProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductSpec", getEventUser(), getEventComment(), "", "");
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		ProductRequest requestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestName);
		
		// Check LotProcessState
		if(StringUtil.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Run))
		{
			throw new CustomException("LOT-0008", lotData.getKey().getLotName());
		}
		
		// Check InRework
		if(StringUtil.equals(lotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
		{
			throw new CustomException("LOT-4003", lotData.getKey().getLotName(), lotData.getReworkState());
		}
		
		//20180521, kyjung, make New MQC
		// Check MQC Processing
		/*List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(lotData);
		if(mqcPlanList != null)
		{
			throw new CustomException("LOT-0212", lotData.getKey().getLotName());
		}*/
		
		//161206 by swcho : pilot job check
		String pilotJobName = ExtendedObjectProxy.getFirstGlassLotService().getActiveJobNameByLotName(lotData.getKey().getLotName());
		if (!pilotJobName.isEmpty())
		{
			throw new CustomException("LOT-9999", "Lot is on First Glass job processing");
		}
		
		//Check Remain Quantity
		CommonValidation.checkWORemainQty(productRequestName, (long)lotData.getProductQuantity());
		
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(lotData.getProductRequestName(), null, "R", -(int)lotData.getProductQuantity(), eventInfo);
		
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, "00001");
		
		String nodeId = CommonUtil.getNodeStack(factoryName, processFlowName, processOperationName);
		
//		if(StringUtils.equals(productSpecData.getProductionType(), "D"))
//		{
//			throw new CustomException("");
//		}
		
		eventInfo.setEventName("ChangeProductSpec");
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(lotData.getAreaName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
		changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
		changeSpecInfo.setLotState(lotData.getLotState());
		changeSpecInfo.setNodeStack(nodeId);
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion("00001");
		changeSpecInfo.setProcessOperationName(processOperationName);
		changeSpecInfo.setProcessOperationVersion("00001");
		changeSpecInfo.setProductionType(requestData.getProductRequestType());
		changeSpecInfo.setProductRequestName(productRequestName);
		changeSpecInfo.setProductSpec2Name(productSpecData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(productSpecData.getProductSpec2Version());
		changeSpecInfo.setProductSpecName(productSpecData.getKey().getProductSpecName());
		changeSpecInfo.setProductSpecVersion("00001");
		changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
		changeSpecInfo.setUdfs(lotData.getUdfs());
		
		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		
		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName); 
		
//		// Production Lot increaseReleaseQuantity
//		List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
//		int productQuantity = productList.size();
//		
//		MESLotServiceProxy.getLotServiceUtil().decrementWorkOrderReleaseQty(eventInfo, lotData.getProductRequestName(), productQuantity);
		
		//Dummy ProductRequest decreaseReleaseQuantity
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(lotData.getProductRequestName(), null, "R", (long)lotData.getProductQuantity(), eventInfo);

		
		return doc;
	}
}
