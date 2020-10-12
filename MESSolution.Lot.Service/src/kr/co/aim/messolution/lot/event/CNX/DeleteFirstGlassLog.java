package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogDetail;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassLogM;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;

public class DeleteFirstGlassLog extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String dataType = SMessageUtil.getBodyItemValue(doc, "DATATYPE", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String dataID = SMessageUtil.getBodyItemValue(doc, "DATAID", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		try
		{
			String condition = " WHERE dataType = ? AND dataId = ? ";
			Object[] bindSet = new Object[]{dataType, dataID};
			List<FirstGlassLogDetail> firstGlassLogDetailList = ExtendedObjectProxy.getFirstGlassLogDetailService().select(condition, bindSet);
			
			eventInfo.setEventName("Delete");
			
			for(FirstGlassLogDetail firstGlassLogDetailData : firstGlassLogDetailList)
			{
				ExtendedObjectProxy.getFirstGlassLogDetailService().delete(firstGlassLogDetailData);
			}
		}
		catch(Exception ex)
		{
			
		}
		
		String condition = " WHERE dataType = ? AND dataId = ? AND lotName = ? ";
		Object[] bindSet = new Object[]{dataType, dataID, lotName};
		List<FirstGlassLogM> firstGlassLogMList = ExtendedObjectProxy.getFirstGlassLogMService().select(condition, bindSet);
		
		eventInfo.setEventName("Delete");
		
		ExtendedObjectProxy.getFirstGlassLogMService().delete(firstGlassLogMList.get(0));
		
		return doc;
	}

}
