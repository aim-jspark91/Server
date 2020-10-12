package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_ToProductSpecList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveMaxQTimeProdSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String relationId = SMessageUtil.getBodyItemValue(doc, "RELATIONID", true);
		
		List<Element> eleProductSpecList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEPRODUCTSPECLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<MaxQTime_ToProductSpecList> maxQTimeDataList = null;
		String condition = " WHERE relationId = ? ";
		Object[] bindSet = new Object[]{relationId};
		maxQTimeDataList = MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().select(condition, bindSet);
		
		for(MaxQTime_ToProductSpecList maxQTimeData : maxQTimeDataList)
		{
			MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().delete(maxQTimeData);
		}
		
		for(Element eleProductSpecData : eleProductSpecList)
		{
			String productSpecName = SMessageUtil.getChildText(eleProductSpecData, "PRODUCTSPECNAME", true);
			
			MaxQTime_ToProductSpecList maxQTimeData = null;
			try
			{
				maxQTimeData = MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().selectByKey(false,
						new Object[]{relationId, productSpecName});
			}
			catch(Exception ex)
			{
				
			}
			
			if(maxQTimeData == null)
			{
				MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().insertMaxQTime_ToProductSpecList(eventInfo, relationId, productSpecName);
			}
			
		}
		
		return doc;
	}

}
