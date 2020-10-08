package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ModifyMaxQTimeForDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String relationId = SMessageUtil.getBodyItemValue(doc, "RELATIONID", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fromProductSpecName = SMessageUtil.getBodyItemValue(doc, "FROMPRODUCTSPECNAME", true);
		String fromProcessFlowName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSFLOWNAME", true);
		String fromProcessOperationName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSOPERTIONNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERTIONNAME", true);
		String toMachineName = SMessageUtil.getBodyItemValue(doc, "TOMACHINENAME", true);
		String machineCapacity = SMessageUtil.getBodyItemValue(doc, "MACHINECAPACITY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		MaxQTime maxQTimeListData = null;
		
		String condition = " WHERE 1=1 AND factoryName =? AND fromProductSpecName = ? AND fromProcessFlowName = ? AND fromProcessOperationName = ?"
				+ " AND toProcessOperationName = ? AND toMachineName = ? ";
		Object[] bindSet = new Object[]{relationId, factoryName, fromProductSpecName, fromProcessFlowName, fromProcessOperationName, toProcessOperationName,
				toMachineName };
		maxQTimeListData = MESDSPServiceProxy.getMaxQTimeService().selectByKey(false, bindSet);
		
		maxQTimeListData.setMachineCapacity(Long.parseLong(machineCapacity));
		
		MESDSPServiceProxy.getMaxQTimeService().update(maxQTimeListData);
		
		return doc;
	}

}
