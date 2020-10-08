package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateDummyProductRequest extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{	
		String factroyName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECTYPE", true);
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		String planReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String planFinishedTime	= SMessageUtil.getBodyItemValue(doc, "PLANFINISHEDTIME", true);
		
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
		
		if(!StringUtils.equals(productRequestType, "D"))
		{
			throw new CustomException("PRODUCTREQUEST-0023", productRequestType);
		}

		//CreateBoxName
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTSPECNAME", productSpecName);
		String workOrderName = CommonUtil.generateNameByNamingRule("BoxNaming", nameRuleAttrMap, Integer.parseInt("1")).get(0);
		
		HashMap<String, String> productRequestUserColumns = new HashMap<String, String>();
		
		//CreateInfo
		CreateInfo createInfo = new CreateInfo();
		createInfo.setProductRequestName(workOrderName);
		createInfo.setProductRequestType(productRequestType);
		createInfo.setPlanQuantity(Integer.parseInt(planQuantity));
		createInfo.setFactoryName(factroyName);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setPlanReleasedTime(TimeStampUtil.getTimestamp(planReleasedTime));
		createInfo.setPlanFinishedTime(TimeStampUtil.getTimestamp(planFinishedTime));
		createInfo.setUdfs(productRequestUserColumns);
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		ProductRequest resultData = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfo);
		//  Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(resultData, eventInfo);
		
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, workOrderName);
		return doc;
	}
}
