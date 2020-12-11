package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.SetEventInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateProductRequest extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException
	{	
		String factroyName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName	= SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		String planReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String planFinishedTime	= SMessageUtil.getBodyItemValue(doc, "PLANFINISHEDTIME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
		String crateSpecName = SMessageUtil.getBodyItemValue(doc, "CRATESPECNAME", true);
		String workOrderType = SMessageUtil.getBodyItemValue(doc, "WORKORDERTYPE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String endBank = SMessageUtil.getBodyItemValue(doc, "ENDBANK", true);
		
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		//1. Finished Time must latter than Finished Time
		Long lReleasedTime = TimeStampUtil.getTimestamp(planReleasedTime).getTime();
		Long lFinishedTime = TimeStampUtil.getTimestamp(planFinishedTime).getTime();
		
		if(lReleasedTime > lFinishedTime)
		{
			throw new CustomException("PRODUCTREQUEST-0004", planReleasedTime, planFinishedTime);
		}
		
		//2. Plan Quantity must bigger than 1
		if(Integer.valueOf(planQuantity) <= 0)
		{
			throw new CustomException("PRODUCTREQUEST-0008", planQuantity);
		}
		
		//Create Work Order Name
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("WORKORDERTYPE", workOrderType);

		String workOrderName;
		
		if(productRequestName != null && StringUtil.isNotEmpty(productRequestName))
		{
			workOrderName = productRequestName;
		}
		else
		{
			workOrderName = CommonUtil.generateNameByNamingRule("WorkOrderNaming", nameRuleAttrMap, Integer.parseInt("1"), workOrderType).get(0);
		}
		
		//Check Exist Work Order
		ProductRequest checkData = new ProductRequest();
		try
		{
			ProductRequestKey pKey = new ProductRequestKey();
			pKey.setProductRequestName(workOrderName);
			checkData = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKey);
		}
		catch (Exception ex) 
		{
		}
		
		if(!StringUtils.isEmpty(checkData.getKey().getProductRequestName()))
		{
			throw new CustomException("PRODUCTREQUEST-0029", workOrderName);
		}
		
		//CreateInfo
		CreateInfo createInfo = new CreateInfo();
		createInfo.setProductRequestName(workOrderName);
		createInfo.setProductRequestType(productRequestType);
		createInfo.setPlanQuantity(Integer.parseInt(planQuantity));
		createInfo.setFactoryName(factroyName);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setPlanReleasedTime(TimeStampUtil.getTimestamp(planReleasedTime));
		createInfo.setPlanFinishedTime(TimeStampUtil.getTimestamp(planFinishedTime));
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfo);
				
		Map<String,String> udfs = resultData.getUdfs();
		udfs.put("crateSpecName", crateSpecName);
		udfs.put("CHANGEINQUANTITY", "0");
		udfs.put("CHANGEOUTQUANTITY", "0");
		udfs.put("DESCRIPTION", description);
		udfs.put("ENDBANK", endBank);
		resultData.setUdfs(udfs);
		
		resultData.setProductRequestHoldState(GenericServiceProxy.getConstantMap().Prq_OnHold);
		ProductRequestServiceProxy.getProductRequestService().update(resultData);
		
		SetEventInfo aa = new SetEventInfo();
		ProductRequestServiceProxy.getProductRequestService().setEvent(resultData.getKey(), eventInfo, aa);
		
		

		//Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);				
		
		//2018.01.29 dmlee : remove
		/*		
		//Update ERP
		//1) Update ERPProductRequest Receive Flag
		if(!StringUtils.isEmpty(seq))
		{
			ProductRequestServiceUtil.SetERPReceiveFlag(seq, productRequestName);
		}
		
		//2) Insert MESProductRequest WO Data
		if(ProductRequestServiceUtil.CheckERPWorkOrder(resultData.getKey().getProductRequestName()))
		{
		    ProductRequestServiceUtil.WriteMESProductRequest(resultData.getKey().getProductRequestName());
		}
		*/
		
		// Add New Work Order to Body Message
        SMessageUtil.addItemToBody(doc, "NEWPRODUCTREQUESTNAME", workOrderName);
		
		return doc;
	}
}
