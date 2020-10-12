package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.09.25
 * @see According to EDO's request, Add/Update/Delete views are added.
 */
public class DeleteMQCCondition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String mqcProductSpecName = SMessageUtil.getBodyItemValue(doc, "MQCPRODUCTSPECNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		try {
			String condition = "MACHINENAME = ? AND UNITNAME = ?";
			Object[] bindSet = new Object[] {machineName, unitName};
			
			List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
			
			int removedMqcConditionCount = 0;
			for (MQCCondition mqcCondition : mqcConditionList) {
				if (mqcCondition.getMqcProductSpecName().equals(mqcProductSpecName)) {
					ExtendedObjectProxy.getMQCConditionService().remove(eventInfo, mqcCondition);
					removedMqcConditionCount++;
				}
			}
			
			// if all MQCConditions are deleted, MachineIdleTime should be also deleted. 
			if (mqcConditionList.size() == removedMqcConditionCount) {
				try {
					List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
					
					for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
						ExtendedObjectProxy.getMachineIdleTimeService().remove(eventInfo, machineIdleTime);
					}
				} catch (Exception e2) {
					// TODO: handle exception
					eventLog.warn(e2);
				}
			}
		} catch (Exception e) {
			eventLog.warn(e);
		}

		return doc;
	}
}