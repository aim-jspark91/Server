package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateRequestCancel extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String RequestID    = SMessageUtil.getBodyItemValue(doc, "REQUESTID", true);
		String PartID       = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String RequestState = SMessageUtil.getBodyItemValue(doc, "REQUESTSTATE", true);//RequestCancel
		String RequestQty   = SMessageUtil.getBodyItemValue(doc, "REQUESTQUANTITY", true);
		String RequestType  = SMessageUtil.getBodyItemValue(doc, "REQUESTTYPE", false);
		String RequestDesc  = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("RequestCancel", getEventUser(), getEventComment(), null, null);		
		
		if(!RequestState.equals("RequestCancel"))
			eventLog.error(String.format( "Request Fail State is [%s] ", RequestState));

		
		RequestSparePart ReqSparePartInfo = new RequestSparePart(RequestID,PartID);
		ReqSparePartInfo.setRequestId(RequestID);
		ReqSparePartInfo.setPartId(PartID);
		ReqSparePartInfo.setRequestState(RequestState);
		ReqSparePartInfo.setRequestQuantity(RequestQty);
		ReqSparePartInfo.setRequestType(RequestType);
		ReqSparePartInfo.setDescription(RequestDesc);
		ReqSparePartInfo.setOrderNo("");
		
		try
		{
			ReqSparePartInfo = PMSServiceProxy.getRequestSparePartService().modify(eventInfo, ReqSparePartInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0056", RequestID);
		}
		
		return doc;
	}
}
