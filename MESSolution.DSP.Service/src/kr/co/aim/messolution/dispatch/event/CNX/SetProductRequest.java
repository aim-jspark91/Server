package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

import org.jdom.Document;
import org.jdom.Element;

public class SetProductRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> productRequestList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTREQUESTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
		
		for(Element eleProductRequestData : productRequestList)
		{
			String factoryName = SMessageUtil.getChildText(eleProductRequestData, "FACTORYNAME", true);
			String productRequestName = SMessageUtil.getChildText(eleProductRequestData, "PRODUCTREQUESTNAME", true);
			String dspFlag = SMessageUtil.getChildText(eleProductRequestData, "DSPFLAG", false);

			ProductRequestKey pKey = new ProductRequestKey(productRequestName);
			ProductRequest pData = CommonUtil.getProductRequestData(productRequestName);
			
			//ChangeSpecInfo
			ChangeSpecInfo changespecInfo = new ChangeSpecInfo();
			changespecInfo.setProductRequestType(pData.getProductRequestType());
			changespecInfo.setFactoryName(pData.getFactoryName());
			changespecInfo.setProductSpecName(pData.getProductSpecName());
			changespecInfo.setProductRequestState(pData.getProductRequestState());
			changespecInfo.setProductSpecVersion(pData.getProductSpecVersion());
			changespecInfo.setPlanQuantity(Long.valueOf(pData.getPlanQuantity()));
			changespecInfo.setPlanReleasedTime(pData.getPlanReleasedTime());
			changespecInfo.setPlanFinishedTime(pData.getPlanFinishedTime());
			changespecInfo.setReleasedQuantity(pData.getReleasedQuantity());
			changespecInfo.setFinishedQuantity(pData.getFinishedQuantity());
			changespecInfo.setScrappedQuantity(pData.getScrappedQuantity());
			changespecInfo.setProductRequestHoldState(pData.getProductRequestHoldState());
			
			Map<String, String> productRequestUserColumns = pData.getUdfs();
			productRequestUserColumns.put("dspFlag", dspFlag);
			
			changespecInfo.setUdfs(productRequestUserColumns);
			
			ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().changeSpec(pKey, eventInfo, changespecInfo);
			
			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
		}
		return doc;
	}

}
