package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ReserveShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String shipRequestState = SMessageUtil.getBodyItemValue(doc, "SHIPREQUESTSTATE", true);	
		String allWeight        = SMessageUtil.getBodyItemValue(doc, "ALLWEIGHT", true);
		String pureWeight       = SMessageUtil.getBodyItemValue(doc, "PUREWEIGHT", true);
		String volumn           = SMessageUtil.getBodyItemValue(doc, "VOLUMN", true);	
		String confirmUser      = getEventUser();      //SMessageUtil.getBodyItemValue(doc, "CONFIRMUSER", true);
		String lastEventName    = "ReserveShipRequest";//SMessageUtil.getBodyItemValue(doc, "LASTEVENTNAME", true);
		String lastEventUser    = getEventUser();      //SMessageUtil.getBodyItemValue(doc, "LASTEVENTUSER", true);
		String lastEventComment = getEventComment();   //SMessageUtil.getBodyItemValue(doc, "LASTEVENTCOMMENT", true);
		
		Timestamp lastEventTime = TimeStampUtil.getCurrentTimestamp();
		String lastEventTimeKey = TimeStampUtil.toTimeString(lastEventTime);
		Timestamp confirmTime = lastEventTime;
	
		ShipRequest shipRequstData = new ShipRequest(invoiceNo);
		
		shipRequstData.setShipRequestState(shipRequestState);
		shipRequstData.setConfirmTime(confirmTime);
		
//		if(allWeight != null && !allWeight.isEmpty()) {
//			shipRequstData.setAllWeight(allWeight);
//		}
//		if(pureWeight != null && !pureWeight.isEmpty()) {
//			shipRequstData.setPureWeight(pureWeight);
//		}
//		if(volumn != null && !volumn.isEmpty()) {
//			shipRequstData.setVolumn(volumn);
//		}
		
		shipRequstData.setConfirmUser(confirmUser);
		shipRequstData.setLastEventName(lastEventName);
		shipRequstData.setLastEventTimeKey(lastEventTimeKey);
		shipRequstData.setLastEventTime(lastEventTime);
		shipRequstData.setLastEventUser(lastEventUser);
		shipRequstData.setLastEventComment(lastEventComment);
				
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveShipRequest", getEventUser(), getEventComment(), "", "");
		
		FGMSServiceProxy.getShipRequestService().modify(eventInfo, shipRequstData);
		return doc;
	}
}
