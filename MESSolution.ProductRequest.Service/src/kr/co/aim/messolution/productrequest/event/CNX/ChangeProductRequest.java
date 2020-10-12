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
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

import org.jdom.Document;

public class ChangeProductRequest extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{	
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
		String productSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		String planReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String planFinishedTime	= SMessageUtil.getBodyItemValue(doc, "PLANFINISHEDTIME", true);

		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		ProductRequest pData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		
		//Velidation
		//Can't Change if Product Request is OnHold
		/*if(pData.getProductRequestHoldState().equals("Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0001", productRequestName);
		}*/
		
		//1. Product Request State must be Created
		if(pData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Completed) || 
				pData.getProductRequestState().equals(GenericServiceProxy.getConstantMap().Prq_Finished))
		{
			throw new CustomException("PRODUCTREQUEST-0012", productRequestName, pData.getProductRequestState());
		}
		
		//2. Plan Quantity must bigger than 1
		if(Integer.valueOf(planQuantity) <= 0)
		{
			throw new CustomException("PRODUCTREQUEST-0008", planQuantity);
		}
		
		//3. Finished Time must latter than Finished Time
		Long lReleasedTime = TimeStampUtil.getTimestamp(planReleasedTime).getTime();
		Long lFinishedTime = TimeStampUtil.getTimestamp(planFinishedTime).getTime();
		if(lReleasedTime > lFinishedTime)
		{
			throw new CustomException("PRODUCTREQUEST-0004", planReleasedTime, planFinishedTime);
		}
		
		String planSql = "SELECT NVL(SUM(PLANQUANTITY),0) S_PLANQUANTITY FROM PRODUCTREQUESTPLAN WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME";
		
		Map<String, Object> planBindSet = new HashMap<String, Object>();
		planBindSet.put("PRODUCTREQUESTNAME", productRequestName);

		List<Map<String, Object>> planSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(planSql, planBindSet);
		
		if( planSqlResult.size() > 0 )
		{
			int sumPlanQty = Integer.parseInt(planSqlResult.get(0).get("S_PLANQUANTITY").toString());
			
			if (Integer.valueOf(planQuantity) < sumPlanQty) 
			{
				throw new CustomException("PRODUCTREQUEST-0051", Integer.valueOf(planQuantity), sumPlanQty);
			}
		}

		/**
		//4. Work Order none Work Order Plan
		boolean chkProductRequest = ProductRequestServiceUtil.CheckProductRequestReserved(productRequestName);
		
		if(chkProductRequest == true) {
			throw new CustomException("PRODUCTREQUEST-0020", productRequestName);
		}
		*/
		
		//5. Work Order none Lot
//		String condition = "WHERE productRequestName = ?";
//		Object[] bindSet = new Object[] {productRequestName};
//		List<Lot> lotList = new ArrayList<Lot>();
//		
//		try{
//			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
//		}
//		catch(NotFoundSignal ne){
//		}
//		
//		if(lotList.size() > 0)
//		{
//			throw new CustomException("PRODUCTREQUEST-0020", productRequestName);
//		}

		//ChangeSpecInfo
		ChangeSpecInfo changespecInfo = new ChangeSpecInfo();
		changespecInfo.setFactoryName(pData.getFactoryName());
		changespecInfo.setProductSpecName(productSpec);
		changespecInfo.setProductRequestType(productRequestType);
		
		changespecInfo.setPlanQuantity(Long.valueOf(planQuantity));
		changespecInfo.setProductRequestState(pData.getProductRequestState());
		
		changespecInfo.setPlanReleasedTime(TimeStampUtil.getTimestamp(planReleasedTime));
		changespecInfo.setPlanFinishedTime(TimeStampUtil.getTimestamp(planFinishedTime));
		
		changespecInfo.setReleasedQuantity(pData.getReleasedQuantity());
		
		
		changespecInfo.setUdfs(pData.getUdfs());
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductRequest", this.getEventUser(), this.getEventComment(), "", "");
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().changeSpec(pKey, eventInfo, changespecInfo);
		
		//Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
		
		/*
		//1) Update ERPProductRequest Receive Flag
		if(!StringUtils.isEmpty(seq))
		{
			ProductRequestServiceUtil.SetERPReceiveFlag(seq, productRequestName);
		}
		*/
		
		//2) Insert MESProductRequest WO Data
//		if(ProductRequestServiceUtil.CheckERPWorkOrder(productRequestName))
//		{
//		    ProductRequestServiceUtil.WriteMESProductRequest(productRequestName);
//		}
		//Sync Plan By Product Request
		//MESWorkOrderServiceProxy.getProductRequestServiceUtil().syncPlanByProductRequest(eventInfo, productRequestName);
		
		
		return doc;
	}
}
