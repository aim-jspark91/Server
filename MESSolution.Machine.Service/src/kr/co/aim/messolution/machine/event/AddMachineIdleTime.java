package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.09.25
 * @see According to EDO's request, Add/Update/Delete views are added.
 */
public class AddMachineIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String mqcType = SMessageUtil.getBodyItemValue(doc, "MQCTYPE", true);
		String resetType = SMessageUtil.getBodyItemValue(doc, "RESETTYPE", false);
		String autoResetFlag = SMessageUtil.getBodyItemValue(doc, "AUTORESETFLAG", false);
		String idleTime = SMessageUtil.getBodyItemValue(doc, "IDLETIME", false);
		String glassCount = SMessageUtil.getBodyItemValue(doc, "GLASSCOUNT", false);
		String mqcProductSpecName = SMessageUtil.getBodyItemValue(doc, "MQCPRODUCTSPECNAME", true);
		String mqcPlanCount = SMessageUtil.getBodyItemValue(doc, "MQCPLANCOUNT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		List<MachineIdleTime> machineIdleTimeList = null;
		
		try {
			// Modified by smkang on 2018.09.29 - Query is wrong.
//			String condition = "MACHINENAME = ? AND (UNITNAME = ? OR UNITNAME = ?)";
//			Object[] bindSet = new Object[] {machineName, unitName, "*"};
//			machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			if (unitName.equals("*")) {
				String condition = "MACHINENAME = ?";
				Object[] bindSet = new Object[] {machineName};
				machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			} else {
				String condition = "MACHINENAME = ? AND (UNITNAME = ? OR UNITNAME = ?)";
				Object[] bindSet = new Object[] {machineName, unitName, "*"};
				machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
			}
		} catch (Exception e) {
			// TODO: handle exception
			MachineIdleTime machineIdleTimeInfo = new MachineIdleTime();
    		
			machineIdleTimeInfo.setMachineName(machineName);
			machineIdleTimeInfo.setUnitName(unitName);
			machineIdleTimeInfo.setMqcType(mqcType);
			machineIdleTimeInfo.setResetType(resetType);
            machineIdleTimeInfo.setAutoResetFlag(autoResetFlag);
            machineIdleTimeInfo.setIdleTime(Long.parseLong(idleTime));
            machineIdleTimeInfo.setGlassCount(Long.parseLong(glassCount));
            machineIdleTimeInfo.setLastRunTime(eventInfo.getEventTime());
            machineIdleTimeInfo.setIsIdleTimeOver("N");
            machineIdleTimeInfo.setCreateUser(eventInfo.getEventUser());
            machineIdleTimeInfo.setLastEventName(eventInfo.getEventName());
            machineIdleTimeInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
            machineIdleTimeInfo.setLastEventTime(eventInfo.getEventTime());
            machineIdleTimeInfo.setLastEventUser(eventInfo.getEventUser());
            machineIdleTimeInfo.setLastEventComment(eventInfo.getEventComment());

            ExtendedObjectProxy.getMachineIdleTimeService().create(eventInfo, machineIdleTimeInfo);
		} finally {
			if (machineIdleTimeList != null && machineIdleTimeList.size() > 0)
				throw new CustomException("MACHINE-0201", machineIdleTimeList.get(0).getMachineName(), machineIdleTimeList.get(0).getUnitName());
		}

		try {
			String condition = "MACHINENAME = ? AND UNITNAME = ? AND MQCPRODUCTSPECNAME = ?";
			Object[] bindSet = new Object[] {machineName, unitName, mqcProductSpecName};
			List<MQCCondition> mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
			
			if (mqcConditionList != null && mqcConditionList.size() > 0) {
				mqcConditionList.get(0).setMqcPlanCount(Long.parseLong(mqcPlanCount));
				mqcConditionList.get(0).setLastEventName(eventInfo.getEventName());
				mqcConditionList.get(0).setLastEventTimekey(eventInfo.getEventTimeKey());
				mqcConditionList.get(0).setLastEventTime(eventInfo.getEventTime());
				mqcConditionList.get(0).setLastEventUser(eventInfo.getEventUser());
				mqcConditionList.get(0).setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getMQCConditionService().modify(eventInfo, mqcConditionList.get(0));
			}
		} catch (Exception e) {
			// TODO: handle exception
			MQCCondition mqcConditionInfo = new MQCCondition();
			
			mqcConditionInfo.setMachineName(machineName);
			mqcConditionInfo.setUnitName(unitName);
			mqcConditionInfo.setMqcProductSpecName(mqcProductSpecName);
			mqcConditionInfo.setMqcPlanCount(Long.parseLong(mqcPlanCount));
			mqcConditionInfo.setCreateUser(eventInfo.getEventUser());
			mqcConditionInfo.setLastEventName(eventInfo.getEventName());
			mqcConditionInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			mqcConditionInfo.setLastEventTime(eventInfo.getEventTime());
			mqcConditionInfo.setLastEventUser(eventInfo.getEventUser());
			mqcConditionInfo.setLastEventComment(eventInfo.getEventComment());
			
			ExtendedObjectProxy.getMQCConditionService().create(eventInfo, mqcConditionInfo);
		}
		
		//Send FMC
		MESLotServiceProxy.getSendServiceImpl().SendToFMCIsIdleTimeOver(doc);
		return doc;
	}
}