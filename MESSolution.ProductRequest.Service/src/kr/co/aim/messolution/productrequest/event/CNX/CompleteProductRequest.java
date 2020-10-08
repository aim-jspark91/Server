package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CompleteProductRequest extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		// get Message Item
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Complete", this.getEventUser(), this.getEventComment(), "", "");
		
		//Set ProductRequest Key
		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);
		
		// Get ProductRequest DB Data
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		ProductRequest workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		ProductRequest workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);
		
		// Validation 
		if( StringUtils.equals(workOrderData.getProductRequestState(), "Finished") || 
			StringUtils.equals(workOrderData.getProductRequestState(), "Completed") )
		{
			throw new CustomException("PRODUCTREQUEST-0009", productRequestName, workOrderData.getProductRequestState());
		}
		
		// Validation
		/*if(StringUtils.equals(workOrderData.getProductRequestHoldState(), "Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
		}*/
		
		eventLog.info(String.format("WorkOrder[%s] : PlanQty=[%d], ReleasedQty=[%d]", 
				workOrderData.getKey().getProductRequestName(), workOrderData.getPlanQuantity(), workOrderData.getReleasedQuantity()));
		
		if (workOrderData.getPlanQuantity() >= workOrderData.getReleasedQuantity())
		{
			String existLotSql = "SELECT LOTNAME FROM LOT WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME"
					+ " AND LOTSTATE = :LOTSTATE ";
			
			Map<String, Object> bindSet = new HashMap<String, Object>();
			bindSet.put("PRODUCTREQUESTNAME", workOrderData.getKey().getProductRequestName());
			bindSet.put("LOTSTATE", "Created");
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(existLotSql, bindSet);
			
		
			if ( sqlResult.size() > 0 )
			{
				throw new CustomException("PRODUCTREQUEST-0045", productRequestName);
			}
		}
		else
		{
			throw new CustomException("PRODUCTREQUEST-0049", productRequestName);
		}
				
		if ( StringUtil.equals(workOrderData.getProductRequestState(), "Created") ||
			 StringUtil.equals(workOrderData.getProductRequestState(), "Planned") )
		{
			workOrderData.setProductRequestState("Released");
			ProductRequestServiceProxy.getProductRequestService().update(workOrderData);
		}
		
		// API - MakeCompleted execute
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
		ProductRequestServiceProxy.getProductRequestService().makeCompleted(productRequestKey, eventInfo, makeCompletedInfo);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKeyForUpdate(productRequestKey);

		workOrderData.setProductRequestHoldState("N");
		ProductRequestServiceProxy.getProductRequestService().update(workOrderData);
		
		workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(productRequestKey);
		
		// Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(workOrderData, eventInfo);
		
		return doc;
	}
}
