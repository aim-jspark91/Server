package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;


public class CreateRequestSparePart extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String RequestName 	= SMessageUtil.getBodyItemValue(doc, "REQUESTID", true);
		String RequestState = SMessageUtil.getBodyItemValue(doc, "REQUESTSTATE", true);
		String RequestDesc 	= SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false); 
		String RequestType 	= SMessageUtil.getBodyItemValue(doc, "REQUESTTYPE", false);
		
		List<Element> RequestList = SMessageUtil.getBodySequenceItemList(doc, "REQUESTLIST", true);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("Created", getEventUser(), getEventComment(), null, null);
		
		String RequestName = this.createRequestName();
		
		
		RequestSparePart requestData = null;
		
		for(Element request : RequestList)
		{
			String PartId 	  = SMessageUtil.getChildText(request, "PARTID", true);
			String RequestQty = SMessageUtil.getChildText(request, "REQUESTQUANTITY", true);
				
		    requestData = new RequestSparePart(RequestName,PartId);
		    requestData.setRequestId(RequestName);
		    //requestData.setPartId(PartId);
		    requestData.setRequestState(RequestState);
		    requestData.setRequestQuantity(RequestQty);
		    requestData.setDescription(RequestDesc);
		    requestData.setRequestType(RequestType); 
		    
			try
			{
				requestData = PMSServiceProxy.getRequestSparePartService().create(eventInfo, requestData);
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0062", RequestName);
			}		
		}
		
		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "REQEUSTID", RequestName);
		return rtnDoc;
	}
	
	public String createRequestName()  throws CustomException
	{
		String newRequestName = "";
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String requestDate = currentDate.substring(0, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("REQ", "Req");
		nameRuleAttrMap.put("REQDATE", requestDate);
		nameRuleAttrMap.put("HYPHEN", "-");
		
		//LotID Generate
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("RequestNaming", nameRuleAttrMap, createQty);
			newRequestName = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}
		
		return newRequestName;
	}
}