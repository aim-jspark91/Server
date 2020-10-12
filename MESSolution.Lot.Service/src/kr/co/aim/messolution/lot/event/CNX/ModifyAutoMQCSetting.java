package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyAutoMQCSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		//String mqcType = SMessageUtil.getBodyItemValue(doc, "MQCTYPE", true);
		String mqcType = "IDLETIME";
		String mqcValue = SMessageUtil.getBodyItemValue(doc, "MQCVALUE", true);
		String mqcValidFlag = SMessageUtil.getBodyItemValue(doc, "MQCVALIDFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyAutoMQCSetting", this.getEventUser(), this.getEventComment(), "", "");
			
		AutoMQCSetting autoMQCSetting = null;
		
		try
		{
			autoMQCSetting = ExtendedObjectProxy.getAutoMQCSettingService().selectByKey(false, new Object[] {productSpecName,ecCode,processOperationName,machineName,mqcTemplateName,mqcType});
		}
		catch (Exception ex)
		{
			autoMQCSetting = null;
		}
		
		if(autoMQCSetting == null)
		{
			throw new CustomException("IDLE-0006", "");
		}
		

		autoMQCSetting.setMqcValidFlag(mqcValidFlag);
		try {
			autoMQCSetting.setMqcValue(Double.parseDouble(mqcValue));
		} catch (Exception e) {
			throw new CustomException("IDLE-0005", "");
		}

		autoMQCSetting.setLastEventUser(eventInfo.getEventUser());
		autoMQCSetting.setLastEventComment(eventInfo.getEventComment());
		autoMQCSetting.setLastEventTime(eventInfo.getEventTime());
		autoMQCSetting.setLastEventName(eventInfo.getEventName());
		autoMQCSetting.setLastEventTimeKey(eventInfo.getEventTimeKey());
		
		
		ExtendedObjectProxy.getAutoMQCSettingService().modify(eventInfo, autoMQCSetting);
		
		return doc;
	}
}
