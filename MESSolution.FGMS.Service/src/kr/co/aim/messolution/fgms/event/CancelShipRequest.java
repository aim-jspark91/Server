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
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.UndoInfo;

import org.jdom.Document;

public class CancelShipRequest extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String inVoiceNo        = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String shipRequestState = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);
		String cancelUser       = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);
		String lastEventName    = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);
		String lastEventUser    = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);
		String lastEventComment = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);

		Timestamp lastEventTime = ConvertUtil.getCurrTimeStampSQL();
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_TIMEKEY);
		String lastEventTimeKey = formatter.format(lastEventTime);
		Timestamp cancelTime = lastEventTime;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLocation", getEventUser(), getEventComment(), "", "");
		
		ShipRequest ShipRequestData = null;
		try{
			//Get
			ShipRequestData = FGMSServiceProxy.getShipRequestService().selectByKey(true, new Object[] {inVoiceNo});
			
			ShipRequestData = new ShipRequest(inVoiceNo);
			ShipRequestData.setShipRequestState(shipRequestState);
			ShipRequestData.setCancelTime(cancelTime);
			ShipRequestData.setCancelUser(cancelUser);
			ShipRequestData.setLastEventName(lastEventName);
			ShipRequestData.setLastEventTimeKey(lastEventTimeKey);
			ShipRequestData.setLastEventTime(lastEventTime);
			ShipRequestData.setLastEventUser(lastEventUser);
			ShipRequestData.setLastEventComment(lastEventComment);
		
			FGMSServiceProxy.getShipRequestService().modify(eventInfo, ShipRequestData);
		}catch (Exception ex)
		{
			eventLog.error(String.format( "Modify CT_SHIPREQUEST Fail [ inVoiceNo = %s] ", inVoiceNo));
		}
		
		List<String> palletList = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getPalletNamesFromInvoiceNo(inVoiceNo);
		
		for(String palletName : palletList)
		{
			ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(palletName);

			UndoInfo undoInfo = MESProcessGroupServiceProxy.getProcessGroupInfoUtil().
					undoInfo(processGroupData, eventInfo.getEventName(), eventInfo.getEventTime(), eventInfo.getEventTimeKey(), eventInfo.getEventUser(), lastEventTimeKey);
			
			MESProcessGroupServiceProxy.getProcessGroupServiceImpl().undo(processGroupData, undoInfo, eventInfo);
			
		}

		return doc;
	}
}
