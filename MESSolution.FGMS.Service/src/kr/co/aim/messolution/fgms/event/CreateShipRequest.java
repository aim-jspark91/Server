package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo          = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceType        = SMessageUtil.getBodyItemValue(doc, "INVOICETYPE", true);
		String planShipDate       = SMessageUtil.getBodyItemValue(doc, "PLANSHIPDATE", true);
		String domesticExport	  = SMessageUtil.getBodyItemValue(doc, "DOMESTICEXPORT", true);
		String customerNo		  = SMessageUtil.getBodyItemValue(doc, "CUSTOMERNO", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShipRequest", getEventUser(), getEventComment(), "", "");
		
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		ShipRequest shipRequestData = null;
		try
		{
			shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new String[]{invoiceNo});
		}
		catch(Exception ex)
		{
			
		}
		
		if(shipRequestData != null)
		{
			throw new CustomException("SYS-9999", "Duplicate Error");
		}
		
		FGMSServiceProxy.getShipRequestService().insertShipRequest(eventInfo, invoiceNo, invoiceType, "Created", eventInfo.getEventTime(),
				eventInfo.getEventUser(), null, "", null, "", null, "", null, "",
				customerNo, TimeStampUtil.getTimestamp(planShipDate), domesticExport);
		
		// Add New Work Order to Body Message
		SMessageUtil.addItemToBody(doc, "NEWINVOICENO", invoiceNo);
		
		return doc;
	}
}
