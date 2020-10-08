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

public class ShipReservation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String invoiceNo        = SMessageUtil.getBodyItemValue(doc, "INVOICENO", true);
		String invoiceItemNo        = SMessageUtil.getBodyItemValue(doc, "INVOICEDETAILNO", true);
		List<Element> newPalletList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reserved", getEventUser(), getEventComment(), "", "");
		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		eventInfo.setEventTimeKey(lastEventTimeKey);
		
		ShipRequest shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
		if(StringUtil.equals(shipRequestData.getShipRequestState(), "Confirmed") || StringUtil.equals(shipRequestData.getShipRequestState(), "Shipped") )
		{
			throw new CustomException("SYS-9999", "Check ShipRequestState");
		}
		
		for(Element elenewPalletData : newPalletList)
		{
			String processGroupName = SMessageUtil.getChildText(elenewPalletData, "PROCESSGROUPNAME", true);
			
			List<ProcessGroup> reservedProcessGroupData = null;
			try
			{
				String condition = " WHERE processGroupName = ? AND invoiceNo = ? AND invoiceDetailNo = ? ";
				Object[] bindSet = new Object[]{ processGroupName, invoiceNo, invoiceItemNo };
				reservedProcessGroupData = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
				eventLog.info("Not Found ProcessGroupList");
			}
			
			if(reservedProcessGroupData == null)
			{
				ProcessGroupKey newProcessGroupKey = new ProcessGroupKey(processGroupName);
				ProcessGroup newProcessGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(newProcessGroupKey);
				
				if( !newProcessGroupData.getUdfs().get("INVOICENO").isEmpty() || !newProcessGroupData.getUdfs().get("INVOICEDETAILNO").isEmpty() )
				{
					break;
				}
				
				newProcessGroupData.setLastEventName(eventInfo.getEventName());
				newProcessGroupData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
				newProcessGroupData.setLastEventTime(eventInfo.getEventTime());
				newProcessGroupData.setLastEventUser(eventInfo.getEventUser());
				newProcessGroupData.setLastEventComment(eventInfo.getEventComment());
				
				Map<String, String> udfs = newProcessGroupData.getUdfs();
				udfs.put("INVOICENO", invoiceNo);
				udfs.put("INVOICEDETAILNO", invoiceItemNo);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udfs);
				
//				ProcessGroupServiceProxy.getProcessGroupService().setEvent(newProcessGroupKey, eventInfo, setEventInfo);
//				MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(newProcessGroupData, setEventInfo, eventInfo);
				ProcessGroupServiceProxy.getProcessGroupService().update(newProcessGroupData);
				
				//Add history
			}
		}
		
		List<ProcessGroup> reservedProcessGroupList = null;
		try
		{
			String condition = " WHERE invoiceNo = ? AND invoiceDetailNo = ? ";
			Object[] bindSet = new Object[]{ invoiceNo, invoiceItemNo };
			reservedProcessGroupList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);
			
			if( shipRequestData.getShipRequestState().equals("Created")
					&& reservedProcessGroupList != null && reservedProcessGroupList.size() > 0 )
			{
//				shipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(false, new Object[]{invoiceNo});
				
				shipRequestData.setLastEventName(eventInfo.getEventName());
				shipRequestData.setLastEventTimeKey(TimeStampUtil.toTimeString(TimeStampUtil.getCurrentTimestamp()));
				shipRequestData.setLastEventTime(eventInfo.getEventTime());
				shipRequestData.setLastEventUser(eventInfo.getEventUser());
				shipRequestData.setLastEventComment(eventInfo.getEventComment());
				
				shipRequestData.setReserveTime(eventInfo.getEventTime());
				shipRequestData.setReserveUser(eventInfo.getEventUser());
				
				shipRequestData.setShipRequestState("Reserved");
				
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
