package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.fgms.management.data.ShipRequestPS;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemoveShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo          = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceType        = SMessageUtil.getBodyItemValue(doc, "INVOICETYPE", false);
		String planShipDate       = SMessageUtil.getBodyItemValue(doc, "PLANSHIPDATE", false);
		String domesticExport	  = SMessageUtil.getBodyItemValue(doc, "DOMESTICEXPORT", false);
		String customerNo		  = SMessageUtil.getBodyItemValue(doc, "CUSTOMERNO", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveShipRequest", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		List<ShipRequestPS> shipRequestPSList = null;
		try
		{
			String condition = " WHERE invoiceNo = ? ";
			Object[] bindSet = new Object[]{invoiceNo};
			shipRequestPSList = FGMSServiceProxy.getShipRequestPSService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			eventLog.info("No InvoiceItem");
		}
		
		if(shipRequestPSList != null)
		{
			throw new CustomException("SYS-9999", "Can not Modify. Exist Invoice Item");
		}
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().getShipRequestData(invoiceNo);
		
		FGMSServiceProxy.getShipRequestService().removeShipRequest(eventInfo, shipRequestData);
		
		return doc;
	}
}
