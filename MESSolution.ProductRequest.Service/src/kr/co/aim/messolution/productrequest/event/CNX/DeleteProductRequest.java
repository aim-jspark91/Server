package kr.co.aim.messolution.productrequest.event.CNX;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.jdom.Document;

public class DeleteProductRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
		
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Validation
		//Can't Delete if Product Request is OnHold
		/*
		if(pData.getProductRequestHoldState().equals("Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
		}
		*/
		
		//1. Product Request State must be Created
		if(!pData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Created))
		{
			throw new CustomException("PRODUCTREQUEST-0012", productRequestName, GenericServiceProxy.getConstantMap().Prq_Created);
		}
		//2. Work Order none Work Order Plan
		boolean chkProductRequest = ProductRequestServiceUtil.CheckProductRequestReserved(productRequestName);
		
		if(chkProductRequest == true) {
			throw new CustomException("PRODUCTREQUEST-0020", productRequestName);
		}
		
		// Delete Work Order
		ProductRequestServiceProxy.getProductRequestService().remove(pKey);
		
		// Add History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), "", "");
		
		pData.setLastEventName(eventInfo.getEventName());
		pData.setLastEventUser(eventInfo.getEventUser());
		pData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		pData.setLastEventTime(eventInfo.getEventTime());
		pData.setLastEventComment(eventInfo.getEventComment());

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(pData, eventInfo);
		
		return doc;
	}
}
