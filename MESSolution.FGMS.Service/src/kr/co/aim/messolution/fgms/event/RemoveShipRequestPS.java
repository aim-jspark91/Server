package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import kr.co.aim.messolution.fgms.FGMSServiceProxy;
import kr.co.aim.messolution.fgms.management.data.ShipRequestPS;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;

import org.jdom.Document;
import org.jdom.Element;

public class RemoveShipRequestPS extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String invoiceNo           = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		List<Element> invoiceDetailNoList = SMessageUtil.getBodySequenceItemList(doc, "INVOCEITEMNOLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveShipRequestPS", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		for(Element eleinvoiceDetailNo : invoiceDetailNoList)
		{
			String invoiceDetailNo = SMessageUtil.getChildText(eleinvoiceDetailNo, "INVOICEDETAILNO", true);
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
				throw new CustomException("SYS-9999","Can Not Remove. Exist Pallet");
			}
		}
		
		{}
		
		for(Element eleinvoiceDetailNo : invoiceDetailNoList)
		{
			String invoiceDetailNo = SMessageUtil.getChildText(eleinvoiceDetailNo, "INVOICEDETAILNO", true);
			
			ShipRequestPS shipRequstPSData = FGMSServiceProxy.getShipRequestPSService().selectByKey(false, new Object[]{invoiceDetailNo, invoiceNo});
			
			FGMSServiceProxy.getShipRequestPSService().remove(eventInfo, shipRequstPSData);
		}
		
		return doc;
	}
}
