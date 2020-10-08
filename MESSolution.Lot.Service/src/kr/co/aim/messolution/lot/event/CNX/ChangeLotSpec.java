package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeLotSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeWorkOrder", getEventUser(), getEventComment(), "", "");
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);		
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);		
				
		//get release qty
		for (Element eLotData : lotList) 
		{
			String lotName = eLotData.getChild("LOTNAME").getText();
			String befProductRequestName = eLotData.getChild("BEFPRODUCTREQUESTNAME").getText();
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			ProductRequest requestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(productRequestName);
			ProductRequest requestData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(new ProductRequestKey(productRequestName));

			ProductSpec produtSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
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
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(lotName);
			
			//2017.5.6 zhongsl changePriority for DSP
			long defaultPriority = lotData.getPriority();
			if(requestData.getProductRequestType().equals("T") || requestData.getProductRequestType().equals("E"))
			{
				defaultPriority = 5;
			}
			
			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotName,
					requestData.getProductRequestType(), produtSpecData.getKey().getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					productRequestName,
					lotData.getSubProductUnitQuantity1(), lotData.getSubProductQuantity2(), lotData.getDueDate(), defaultPriority,
					factoryName, lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
					processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, processOperationName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION,
					"", "", "", "", "",
					lotData.getUdfs(), productUdfs,
					false);
			
			lotData = MESLotServiceProxy.getLotServiceImpl().changeProductSpec(eventInfo, lotData, changeSpecInfo);
			
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			ProductRequest befRequestData = MESWorkOrderServiceProxy.getProductRequestInfoUtil().getProductRequest(befProductRequestName);
			ProductRequest befRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(new ProductRequestKey(befProductRequestName));

			Map<String, String> udfs1 = new HashMap<String, String>();
            udfs1.put("destinationWOName", productRequestName);
            udfs1.put("sourceWOName", "");
			befRequestData.setUdfs(udfs1);
			ProductRequestServiceProxy.getProductRequestService().update(befRequestData);
			
			Map<String, String> udfs2 = new HashMap<String, String>();
			udfs2.put("sourceWOName", befProductRequestName);
			udfs2.put("destinationWOName", "");
			requestData.setUdfs(udfs2);
			ProductRequestServiceProxy.getProductRequestService().update(requestData);
			
			//MESLotServiceProxy.getLotServiceUtil().decrementWorkOrderReleaseQty(eventInfo, befProductRequestName, (int)lotData.getProductQuantity());
			
			//MESLotServiceProxy.getLotServiceUtil().incrementWorkOrderReleaseQty(eventInfo, productRequestName, (int)lotData.getProductQuantity());
			
		}
		
		return doc;
	}

}
