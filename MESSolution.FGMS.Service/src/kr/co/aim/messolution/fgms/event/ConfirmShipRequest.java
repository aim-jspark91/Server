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
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;

import org.jdom.Document;

public class ConfirmShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Confirmed", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		//Check Ship Request State
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
		if( !StringUtil.equals(shipRequestData.getShipRequestState(), "Reserved") )
		{
			throw new CustomException("SYS-9999", "Check ShipRequestState");
		}
		
		List<ProcessGroup> reservedProcessGroupList = null;
		try
		{
			String condition = " WHERE invoiceNo = ?";
			Object[] bindSet = new Object[]{ invoiceNo};
			reservedProcessGroupList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
			
			if( shipRequestData.getShipRequestState().equals("Reserved") && reservedProcessGroupList != null || reservedProcessGroupList.size() > 0 )
			{
				shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
				
				shipRequestData.setLastEventName(eventInfo.getEventName());
				shipRequestData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
				shipRequestData.setLastEventTime(eventInfo.getEventTime());
				shipRequestData.setLastEventUser(eventInfo.getEventUser());
				shipRequestData.setLastEventComment(eventInfo.getEventComment());
				
				shipRequestData.setConfirmTime(eventInfo.getEventTime());
				shipRequestData.setConfirmUser(eventInfo.getEventUser());
				
				shipRequestData.setShipRequestState("Confirmed");
				
				FGMSServiceProxy.getShipRequestService().update(shipRequestData);
			}
		}
		catch(Exception ex)
		{
			eventLog.info("Not Found ProcessGroupList");
		}
		
		return doc;
	}
}
