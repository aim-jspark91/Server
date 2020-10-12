package kr.co.aim.messolution.fgms.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CancelShipReservation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String invoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceItemNo        = SMessageUtil.getBodyItemValue(doc, "INVOICEDETAILNO", true);
		List<Element> cancelPalletList = SMessageUtil.getBodySequenceItemList(doc, "CANCELRESERVELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserved", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
		if(!StringUtil.equals(shipRequestData.getShipRequestState(), "Reserved"))
		{
			throw new CustomException("SYS-9999", "Check ShipRequestState");
		}
		
		for(Element eleCancelPalletList : cancelPalletList)
		{
			String processGroupName = SMessageUtil.getChildText(eleCancelPalletList, "PROCESSGROUPNAME", true);
//			String invoiceNo = SMessageUtil.getChildText(eleCancelPalletList, "INVOICENO", true);
//			String invoiceItemNo = SMessageUtil.getChildText(eleCancelPalletList, "INVOICEDETAILNO", true);
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			
			if( processGroupData.getUdfs().get("INVOICENO").isEmpty() || processGroupData.getUdfs().get("INVOICEDETAILNO").isEmpty() )
			{
				continue;
			}

			processGroupData.setLastEventName(eventInfo.getEventName());
			processGroupData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
			processGroupData.setLastEventTime(eventInfo.getEventTime());
			processGroupData.setLastEventUser(eventInfo.getEventUser());
			processGroupData.setLastEventComment(eventInfo.getEventComment());
			
			Map<String, String> udfs = processGroupData.getUdfs();
			udfs.put("INVOICENO", "");
			udfs.put("INVOICEDETAILNO", "");
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
//			ProcessGroupServiceProxy.getProcessGroupService().setEvent(processGroupKey, eventInfo, setEventInfo);
			ProcessGroupServiceProxy.getProcessGroupService().update(processGroupData);	
		}
		
		List<ProcessGroup> reservedProcessGroupList = null;
		try
		{
			String condition = " WHERE invoiceNo = ? AND invoiceDetailNo = ? ";
			Object[] bindSet = new Object[]{ invoiceNo, invoiceItemNo };
			reservedProcessGroupList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			eventLog.info("Not Found ProcessGroupList");
		}
		
		if( shipRequestData.getShipRequestState().equals("Reserved") && reservedProcessGroupList == null || reservedProcessGroupList.size() <= 0 )
		{			
			shipRequestData.setLastEventName(eventInfo.getEventName());
			shipRequestData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
			shipRequestData.setLastEventTime(eventInfo.getEventTime());
			shipRequestData.setLastEventUser(eventInfo.getEventUser());
			shipRequestData.setLastEventComment(eventInfo.getEventComment());
			
			shipRequestData.setReserveTime(null);
			shipRequestData.setReserveUser("");
			
			shipRequestData.setShipRequestState("Created");
			
			FGMSServiceProxy.getShipRequestService().update(shipRequestData);
		}
		
		return doc;
	}

}
