package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequest;
import kr.co.aim.messolution.fgms.management.data.ShipRequestPS;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateShipRequestPS extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo           = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceDetailNo     = SMessageUtil.getBodyItemValue(doc, "INVOICEDETAILNO", true);
		String requestPanelQuantity  = SMessageUtil.getBodyItemValue(doc, "REQUESTPANELQUANTITY", true);
		String productSpecName	   = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		
		List <ShipRequest> sqlResult1 = new ArrayList <ShipRequest>();
		try
		{
			sqlResult1 = FGMSServiceProxy.getShipRequestService().select("INVOICENO = ?", new Object[] {invoiceNo});
			
			if (sqlResult1.size() == 0)
				throw new CustomException("ShipRequest-9001", invoiceNo);
		}
		catch (Exception ex)
		{
			throw new CustomException("ShipRequest-9001", invoiceNo);			
		}
		
	
//		ShipRequestPS shipRequestPSData = null;	
//		
//		try
//		  {   
//		    shipRequestPSData = FGMSServiceProxy.getShipRequestPSService().selectByKey(false, new Object[]{invoiceDetailNo,invoiceNo});			
//		  }
//		catch(Exception ex)
//		{  			
//			eventLog.debug("");		
//		}
//		if(shipRequestPSData != null)
//		{
//			throw new CustomException("SYS-9999", "Can Not Created");
//		}	
		
		
		List <ShipRequestPS> sqlResult = new ArrayList <ShipRequestPS>();
		try
		{
			sqlResult = FGMSServiceProxy.getShipRequestPSService().select("INVOICEDETAILNO = ? AND INVOICENO = ? ", new Object[] { invoiceDetailNo,invoiceNo});
			
			if (sqlResult.size() > 0)
				throw new CustomException("ShipRequestPS-9001", invoiceDetailNo);
		}
		catch (NotFoundSignal ne)
		{
			sqlResult = new ArrayList <ShipRequestPS>();
		}
		catch(greenFrameDBErrorSignal de)
		{
			if(!de.getErrorCode().equals("NotFoundSignal"))
				throw new CustomException("SYS-8001",de.getSql()); 
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShipRequestPS", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		FGMSServiceProxy.getShipRequestPSService().insertShipRequestPS(eventInfo, invoiceDetailNo, invoiceNo, 0,
				0, productSpecName, 0, getEventUser(), Long.parseLong(requestPanelQuantity));
		
		// Add New Work Order to Body Message
		SMessageUtil.addItemToBody(doc, "NEWINVOICEDETAILNO", invoiceDetailNo);
		SMessageUtil.addItemToBody(doc, "NEWINVOICENO", invoiceNo);
	
		return doc;
	}
}
