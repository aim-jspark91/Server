package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class CancelConfirmShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceItemNo        = SMessageUtil.getBodyItemValue(doc, "INVOICEDETAILNO", false);
		List<Element> cancelPalletList = SMessageUtil.getBodySequenceItemList(doc, "CANCELCONFIRMEDLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelConfirm", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
		if( !StringUtil.equals(shipRequestData.getShipRequestState(), "Confirmed") )
		{
			throw new CustomException("SYS-9999", "Check ShipRequestState");
		}
		
//		for(Element eleCancelPalletData : cancelPalletList)
//		{
//			String processGroupName = SMessageUtil.getChildText(eleCancelPalletData, "PROCESSGROUPNAME", true);
//			
//			ProcessGroupKey newProcessGroupKey = new ProcessGroupKey(processGroupName);
//			ProcessGroup newProcessGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(newProcessGroupKey);
//			
//			newProcessGroupData.setLastEventName(eventInfo.getEventName());
//			newProcessGroupData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
//			newProcessGroupData.setLastEventTime(eventInfo.getEventTime());
//			newProcessGroupData.setLastEventUser(eventInfo.getEventUser());
//			newProcessGroupData.setLastEventComment(eventInfo.getEventComment());
//			
//			ProcessGroupServiceProxy.getProcessGroupService().update(newProcessGroupData);
//		}
		
		try
		{
			shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
			
			shipRequestData.setLastEventName(eventInfo.getEventName());
			shipRequestData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
			shipRequestData.setLastEventTime(eventInfo.getEventTime());
			shipRequestData.setLastEventUser(eventInfo.getEventUser());
			shipRequestData.setLastEventComment(eventInfo.getEventComment());
			
			shipRequestData.setConfirmTime(null);
			shipRequestData.setConfirmUser("");
			
			shipRequestData.setShipRequestState("Reserved");
			
			FGMSServiceProxy.getShipRequestService().update(shipRequestData);
		}
		catch(Exception ex)
		{
//			eventLog.info("Not Found ProcessGroupList");
		}
		
		return doc;
	}
}
