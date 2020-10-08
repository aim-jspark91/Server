package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
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
public class AddMQCCondition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String mqcProductSpecName = SMessageUtil.getBodyItemValue(doc, "MQCPRODUCTSPECNAME", true);
		String mqcPlanCount = SMessageUtil.getBodyItemValue(doc, "MQCPLANCOUNT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		List<MQCCondition> mqcConditionList = null;
		
		try {
			String condition = "MACHINENAME = ? AND UNITNAME = ? AND MQCPRODUCTSPECNAME = ?";
			Object[] bindSet = new Object[] {machineName, unitName, mqcProductSpecName};
			mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select(condition, bindSet);
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
		} finally {
			if (mqcConditionList != null && mqcConditionList.size() > 0)
				throw new CustomException("MACHINE-0202", mqcConditionList.get(0).getMachineName(), mqcConditionList.get(0).getUnitName(), mqcConditionList.get(0).getMqcProductSpecName());
		}
		
		return doc;
	}
}