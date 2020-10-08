package kr.co.aim.messolution.productrequest.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class FinishProductRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		// get Message Item
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Finish", this.getEventUser(), this.getEventComment(), "", "");
		
		//Set ProductRequest Key
		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);
		
		// Get ProductRequest DB Data
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		ProductRequest workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
		
		// Validation 
		if(!StringUtils.equals(workOrderData.getProductRequestState(), "Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0042", productRequestName);
		}
		
		// Validation
		if(StringUtils.equals(workOrderData.getProductRequestHoldState(), "Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
		}
		
		
		// API - makeReleased execute - Don't have makeFinished API
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
		ProductRequestServiceProxy.getProductRequestService().makeReleased(productRequestKey, eventInfo, makeReleasedInfo);
		
		workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		workOrderData.setProductRequestState("Finished");
		workOrderData.setProductRequestHoldState("");
		workOrderData.setReleaseTime(null);
		workOrderData.setReleaseUser("");
		
		ProductRequestServiceProxy.getProductRequestService().update(workOrderData);
		
		workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		
		// Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(workOrderData, eventInfo);
		

		return doc;
	}
}
