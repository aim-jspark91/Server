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
 * @since 2018.08.11
 * @see Delete Machine Idle Time For MQC Condition.
 */
public class DeleteMachineIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		String condition = "MACHINENAME = ? AND UNITNAME = ?";
		Object[] bindSet = new Object[] {machineName, unitName};
		
		try {
			List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			
			for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
				ExtendedObjectProxy.getMachineIdleTimeService().remove(eventInfo, machineIdleTime);
			}
		} catch (Exception e) {
			eventLog.warn(e);
		}

		try {
			List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
			
			for (MQCCondition mqcCondition : mqcConditionList) {
				ExtendedObjectProxy.getMQCConditionService().remove(eventInfo, mqcCondition);
			}
		} catch (Exception e) {
			eventLog.warn(e);
		}
		
		return doc;
	}
}