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
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateMultiProductRequest extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{	
		//COMMON
		String factroyName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String workOrderType = SMessageUtil.getBodyItemValue(doc, "WORKORDERTYPE", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String releasedDate = SMessageUtil.getBodyItemValue(doc, "RELEASEDDATE", true);
		String finishedDate = SMessageUtil.getBodyItemValue(doc, "FINISHEDDATE", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		
		//TFT WO Info
		String productSpecNameT = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAMET", true);
		String ecCodeT = SMessageUtil.getBodyItemValue(doc, "ECCODET", false);
		String processFlowT = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWT", false);
		String planQtyT = SMessageUtil.getBodyItemValue(doc, "PLANQTYT", true);
		
		//CF WO Info
		String productSpecNameC = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAMEC", true);
		String ecCodeC = SMessageUtil.getBodyItemValue(doc, "ECCODEC", false);
		String processFlowC = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWC", false);
		String planQtyC = SMessageUtil.getBodyItemValue(doc, "PLANQTYC", true);
		
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		//Finished Time must latter than Finished Time
		Long lReleasedTime = TimeStampUtil.getTimestamp(releasedDate).getTime();
		Long lFinishedTime = TimeStampUtil.getTimestamp(finishedDate).getTime();
		
		if(lReleasedTime > lFinishedTime)
		{
			throw new CustomException("PRODUCTREQUEST-0004", releasedDate, finishedDate);
		}
		
		//Plan Quantity must bigger than 1
		if(Integer.valueOf(planQtyT) <= 0 || Integer.valueOf(planQtyC) <= 0)
		{
			throw new CustomException("PRODUCTREQUEST-0008", planQtyT+","+planQtyC);
		}
		
		
		//TFT WO
		//Create Work Order Name
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("WORKORDERTYPE", workOrderType);

		String workOrderName = CommonUtil.generateNameByNamingRule("WorkOrderNaming", nameRuleAttrMap, Integer.parseInt("1"), workOrderType).get(0);
		
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
		createInfo.setProductRequestType(workOrderType);
		createInfo.setPlanQuantity(Integer.parseInt(planQtyT));
		createInfo.setFactoryName(factroyName);
		createInfo.setProductSpecName(productSpecNameT);
		createInfo.setPlanReleasedTime(TimeStampUtil.getTimestamp(releasedDate));
		createInfo.setPlanFinishedTime(TimeStampUtil.getTimestamp(finishedDate));
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfo);
				
		Map<String,String> udfs = resultData.getUdfs();
		udfs.put("CHANGEINQUANTITY", "0");
		udfs.put("CHANGEOUTQUANTITY", "0");
		udfs.put("DESCRIPTION", eventInfo.getEventComment());
		resultData.setUdfs(udfs);
		ProductRequestServiceProxy.getProductRequestService().update(resultData);	
		
		
		
		
		//CF WO
		//Create Work Order Name
		Map<String, Object> nameRuleAttrMapCF = new HashMap<String, Object>();
		nameRuleAttrMapCF.put("WORKORDERTYPE", workOrderType);
	
		String workOrderNameCF = CommonUtil.generateNameByNamingRule("WorkOrderNaming", nameRuleAttrMapCF, Integer.parseInt("1"), workOrderType).get(0);
		
		//Check Exist Work Order
		ProductRequest checkDataCF = new ProductRequest();
		try
		{
			ProductRequestKey pKeyCF = new ProductRequestKey();
			pKeyCF.setProductRequestName(workOrderNameCF);
			checkDataCF = ProductRequestServiceProxy.getProductRequestService().selectByKey(pKeyCF);
		}
		catch (Exception ex) 
		{
		}
		
		if(!StringUtils.isEmpty(checkDataCF.getKey().getProductRequestName()))
		{
			throw new CustomException("PRODUCTREQUEST-0029", workOrderNameCF);
		}
		
		//CreateInfo
		CreateInfo createInfoCF = new CreateInfo();
		createInfoCF.setProductRequestName(workOrderNameCF);
		createInfoCF.setProductRequestType(workOrderType);
		createInfoCF.setPlanQuantity(Integer.parseInt(planQtyC));
		createInfoCF.setFactoryName(factroyName);
		createInfoCF.setProductSpecName(productSpecNameC);
		createInfoCF.setPlanReleasedTime(TimeStampUtil.getTimestamp(releasedDate));
		createInfoCF.setPlanFinishedTime(TimeStampUtil.getTimestamp(finishedDate));
		
		ProductRequest resultDataCF = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfoCF);
				
		Map<String,String> udfsCF = resultDataCF.getUdfs();
		udfsCF.put("CHANGEINQUANTITY", "0");
		udfsCF.put("CHANGEOUTQUANTITY", "0");
		udfsCF.put("MAINPRODUCTREQUESTNAME", workOrderName);
		udfsCF.put("DESCRIPTION", eventInfo.getEventComment());
		resultDataCF.setUdfs(udfsCF);
		ProductRequestServiceProxy.getProductRequestService().update(resultDataCF);
		
		
		
		// Add New Work Order to Body Message
        SMessageUtil.addItemToBody(doc, "NEWPRODUCTREQUESTNAME", workOrderName);

		return doc;
	}
}
