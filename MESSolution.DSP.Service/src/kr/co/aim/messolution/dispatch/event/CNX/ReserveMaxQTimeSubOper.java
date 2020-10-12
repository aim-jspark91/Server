package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_SubOperationList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveMaxQTimeSubOper extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String relationId = SMessageUtil.getBodyItemValue(doc, "RELATIONID", true);
		String toProOperName = SMessageUtil.getBodyItemValue(doc, "TOOPERATION", true);
		
		List<Element> eleSubOperList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEPROOPERLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{
			List<MaxQTime_SubOperationList> maxQTimeList = null;
			String condition = " WHERE relationId = ? AND processOperationName NOT IN (?) ";
			Object[] bindSet = new Object[]{relationId, toProOperName};
			
			maxQTimeList = MESDSPServiceProxy.getMaxQTime_SubOperationListService().select(condition, bindSet);
			
			for(MaxQTime_SubOperationList maxQTimeData : maxQTimeList)
			{
				MESDSPServiceProxy.getMaxQTime_SubOperationListService().delete(maxQTimeData);
			}
		}
		catch(Exception ex)
		{
			
		}
		
		for(Element eleSubOperData : eleSubOperList)
		{
			String processOperationName = SMessageUtil.getChildText(eleSubOperData, "PROCESSOPERATIONNAME", true);
			
			MaxQTime_SubOperationList maxQTimeData = null;
			try
			{
				maxQTimeData = MESDSPServiceProxy.getMaxQTime_SubOperationListService().selectByKey(false,
						new Object[]{relationId, processOperationName});
			}
			catch(Exception ex)
			{
				
			}
			
			if(maxQTimeData == null)
			{
				MESDSPServiceProxy.getMaxQTime_SubOperationListService().insetmaxQTime_SubOperationListData(eventInfo, relationId, processOperationName);
			}
			
		}
		
		return doc;
	}

}
