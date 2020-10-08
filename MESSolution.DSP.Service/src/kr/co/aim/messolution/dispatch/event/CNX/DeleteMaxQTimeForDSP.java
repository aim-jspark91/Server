package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_SubOperationList;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_ToProductSpecList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class DeleteMaxQTimeForDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String relationId = SMessageUtil.getBodyItemValue(doc, "RELATIONID", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fromProductSpecName = SMessageUtil.getBodyItemValue(doc, "FROMPRODUCTSPECNAME", true);
		String fromProcessFlowName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSFLOWNAME", true);
		String fromProcessOperationName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSOPERATIONNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		String toMachineName = SMessageUtil.getBodyItemValue(doc, "TOMACHINENAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		MaxQTime maxQTimeListData = null;
		
		String condition = " WHERE 1=1 AND factoryName =? AND fromProductSpecName = ? AND fromProcessFlowName = ? AND fromProcessOperationName = ?"
				+ " AND toProcessOperationName = ? AND toMachineName = ? ";
		Object[] bindSet = new Object[]{relationId, factoryName, fromProductSpecName, fromProcessFlowName, fromProcessOperationName, toProcessOperationName,
				toMachineName };
		maxQTimeListData = MESDSPServiceProxy.getMaxQTimeService().selectByKey(false, bindSet);
		
		MESDSPServiceProxy.getMaxQTimeService().delete(maxQTimeListData);
		
		List<MaxQTime> maxQTimeList = null;
		try
		{
			condition = " WHERE relationId = ? ";
			bindSet = new Object[]{relationId};
			maxQTimeList = MESDSPServiceProxy.getMaxQTimeService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			
		}
		
		try
		{
			if(maxQTimeList == null || maxQTimeList.size() <= 0 )
			{
				condition = " WHERE relationId = ? ";
				bindSet = new Object[]{relationId};
				List<MaxQTime_ToProductSpecList> maxQTime_ToProductSpecList = MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().select(condition, bindSet);
				
				for(MaxQTime_ToProductSpecList maxQTime_ToProductSpecListData : maxQTime_ToProductSpecList)
				{
					MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().delete(maxQTime_ToProductSpecListData);
				}
			}
		}
		catch(Exception ex)
		{
			
		}
		
		try
		{
			if(maxQTimeList == null || maxQTimeList.size() <= 0 )
			{
				condition = " WHERE relationId = ? ";
				bindSet = new Object[]{relationId};
				List<MaxQTime_SubOperationList> maxQTime_SubOperationList = MESDSPServiceProxy.getMaxQTime_SubOperationListService().select(condition, bindSet);
				
				for(MaxQTime_SubOperationList maxQTime_SubOperationListData : maxQTime_SubOperationList)
				{
					MESDSPServiceProxy.getMaxQTime_SubOperationListService().delete(maxQTime_SubOperationListData);
				}
			}
		}
		catch(Exception ex)
		{
			
		}
		
		
		return doc;
	}

}
