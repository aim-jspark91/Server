package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogM;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;

public class ConfirmFirstGlassLog extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
		String dataType = SMessageUtil.getBodyItemValue(doc, "DATATYPE", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String dataID = SMessageUtil.getBodyItemValue(doc, "DATAID", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		String condition = " WHERE dataType = ? AND dataId = ? AND lotName = ? ";
		Object[] bindSet = new Object[]{dataType, dataID, lotData.getKey().getLotName()};
		List<FirstGlassLogM> firstGlassLogMList = ExtendedObjectProxy.getFirstGlassLogMService().select(condition, bindSet);
		
		if(StringUtil.equals(eventName, "Create"))
		{
			eventInfo.setEventName("Confirmed");
			FirstGlassLogM firstGlassLogMData = firstGlassLogMList.get(0);
			firstGlassLogMData.setConfirmor(eventInfo.getEventUser());
			firstGlassLogMData.setLastEventComment(eventInfo.getEventComment());
			firstGlassLogMData.setLastEventTime(eventInfo.getEventTime());
			firstGlassLogMData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			firstGlassLogMData.setLastEventName("Confirmed");
			
			ExtendedObjectProxy.getFirstGlassLogMService().modify(eventInfo, firstGlassLogMData);
		}
		else if(StringUtil.equals(eventName, "Delete"))
		{
			eventInfo.setEventName("Canceled");
			FirstGlassLogM firstGlassLogMData = firstGlassLogMList.get(0);
			firstGlassLogMData.setConfirmor("");
			firstGlassLogMData.setLastEventComment(eventInfo.getEventComment());
			firstGlassLogMData.setLastEventTime(eventInfo.getEventTime());
			firstGlassLogMData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			firstGlassLogMData.setLastEventName("Canceled");
			
			ExtendedObjectProxy.getFirstGlassLogMService().modify(eventInfo, firstGlassLogMData);
		}
		
		return doc;
	}

}
