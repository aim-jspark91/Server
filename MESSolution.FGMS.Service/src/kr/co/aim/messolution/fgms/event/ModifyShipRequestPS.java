package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;

import org.jdom.Document;

public class ModifyShipRequestPS extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo           = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceDetailNo     = SMessageUtil.getBodyItemValue(doc, "INVOICEDETAILNO", true);
		String requestPanelQuantity  = SMessageUtil.getBodyItemValue(doc, "REQUESTPANELQUANTITY", true);
		String productSpecName	   = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyShipRequestPS", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		List<ProcessGroup> processGroupList = null;
		try
		{
			String condition = " WHERE invoiceNo = ? AND invoiceDetailNo = ? ";
			Object[] bindSet = new Object[]{invoiceNo, invoiceDetailNo};
			processGroupList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			eventLog.info("No ProcessGroup.");
		}
		
		if(processGroupList != null)
		{
			throw new CustomException("SYS-9999", "Can Not Modify. Exist Pallet");
		}
		
		FGMSServiceProxy.getShipRequestPSService().updateShipRequestPS(eventInfo, invoiceDetailNo, invoiceNo, 0,
				0, productSpecName, 0, getEventUser(), Long.parseLong(requestPanelQuantity));
		
		return doc;
	}
}
