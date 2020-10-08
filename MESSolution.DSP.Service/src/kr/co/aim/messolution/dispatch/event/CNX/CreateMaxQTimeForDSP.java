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
import org.jdom.Element;

public class CreateMaxQTimeForDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fromProductSpecName = SMessageUtil.getBodyItemValue(doc, "FROMPRODUCTSPECNAME", true);
		String fromProcessFlowName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSFLOWNAME", true);
		String fromProcessOperationName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSOPERTIONNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERTIONNAME", true);
		String toMachineName = SMessageUtil.getBodyItemValue(doc, "TOMACHINENAME", true);
		String machineCapacity = SMessageUtil.getBodyItemValue(doc, "MACHINECAPACITY", true);
		
		List<Element> eleSubOperationList = SMessageUtil.getBodySequenceItemList(doc, "SUBOPERATIONLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String newRelationId = fromProcessOperationName + TimeUtils.getCurrentTime(TimeUtils.FORMAT_SIMPLE_DEFAULT);
		
		List<MaxQTime> maxQTimeList = null;
		try
		{
			String condition = " WHERE factoryName =? AND fromProductSpecName = ? AND fromProcessFlowName = ? AND fromProcessOperationName = ?"
					+ " AND toProcessOperationName = ? AND toMachineName = ? ";
			Object[] bindSet = new Object[]{factoryName, fromProductSpecName, fromProcessFlowName, fromProcessOperationName, toProcessOperationName,
					toMachineName };
			maxQTimeList = MESDSPServiceProxy.getMaxQTimeService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			
		}
		
		if(maxQTimeList != null && maxQTimeList.size() > 0)
		{
			throw new CustomException("DSP-0001", maxQTimeList.get(0).getRelationId());
		}
		
		maxQTimeList = null;
		String condition = " WHERE factoryName =? AND fromProductSpecName = ? AND fromProcessFlowName = ? AND fromProcessOperationName = ?"
				+ " AND toProcessOperationName = ?";
		Object[] bindSet = new Object[]{factoryName, fromProductSpecName, fromProcessFlowName, fromProcessOperationName, toProcessOperationName};
		maxQTimeList = MESDSPServiceProxy.getMaxQTimeService().select(condition, bindSet);
		
		if(maxQTimeList != null && maxQTimeList.size() > 0)
		{
			newRelationId = maxQTimeList.get(0).getRelationId();
			
			MaxQTime maxQTimeData = new MaxQTime();
			
			maxQTimeData = MESDSPServiceProxy.getMaxQTimeService().insertMaxQTime(eventInfo, newRelationId, factoryName, fromProductSpecName,
					fromProcessFlowName, fromProcessOperationName, toProcessOperationName, toMachineName, machineCapacity);
		}
		
		if(maxQTimeList == null || maxQTimeList.size() <= 0)
		{
			MaxQTime maxQTimeData = new MaxQTime();
			
			maxQTimeData = MESDSPServiceProxy.getMaxQTimeService().insertMaxQTime(eventInfo, newRelationId, factoryName, fromProductSpecName,
					fromProcessFlowName, fromProcessOperationName, toProcessOperationName, toMachineName, machineCapacity);
			
			MaxQTime_ToProductSpecList maxQTime_ToProductSpecListData = new MaxQTime_ToProductSpecList();
			
			maxQTime_ToProductSpecListData = MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().insertMaxQTime_ToProductSpecList(eventInfo,
					newRelationId, fromProductSpecName);
			
			MaxQTime_SubOperationList maxQTime_SubOperationListData = new MaxQTime_SubOperationList();
			
			maxQTime_SubOperationListData = MESDSPServiceProxy.getMaxQTime_SubOperationListService().insetmaxQTime_SubOperationListData(eventInfo,
					newRelationId, toProcessOperationName);
			
			for(Element eleSubOperationData : eleSubOperationList)
			{
				String subOperationName = SMessageUtil.getChildText(eleSubOperationData, "SUBOPERATIONNAME", true);
				
				MESDSPServiceProxy.getMaxQTime_SubOperationListService().insetmaxQTime_SubOperationListData(eventInfo,
						newRelationId, subOperationName);
			}
		}
		
		return doc;
	}

}
