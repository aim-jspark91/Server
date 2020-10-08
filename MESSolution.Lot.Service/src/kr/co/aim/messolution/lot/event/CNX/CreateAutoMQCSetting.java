package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AutoMQCSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateAutoMQCSetting extends SyncHandler
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
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateAutoMQCSetting", this.getEventUser(), this.getEventComment(), "", "");

		
		AutoMQCSetting autoMQCSetting = null;
		
		List<AutoMQCSetting> autoMQCSettingList=null;
		try {
			String condition = " WHERE MACHINENAME = ? ";

			Object[] bindSet = new Object[] {machineName};
			
			autoMQCSettingList =  ExtendedObjectProxy.getAutoMQCSettingService().select(condition, bindSet);
		} catch (Exception e) {
			autoMQCSettingList=null;
		}
		
		if(autoMQCSettingList != null && autoMQCSettingList.size()!=0)
		{
			throw new CustomException("COMMON-0001", "Already Exists Machine!");
		}
		
		try {
			autoMQCSetting = ExtendedObjectProxy.getAutoMQCSettingService().selectByKey(false, new Object[] {productSpecName,ecCode,processOperationName,machineName,mqcTemplateName,mqcType});
		} catch (Exception e) {
			autoMQCSetting=null;
		}
		
		if(autoMQCSetting != null)
		{
			throw new CustomException("COMMON-0001", "Already Exists");
		}
		
		autoMQCSetting=  new AutoMQCSetting(productSpecName,ecCode,processOperationName,machineName,mqcTemplateName,mqcType);
		
		autoMQCSetting.setMqcValidFlag(mqcValidFlag);
		try {
			autoMQCSetting.setMqcValue(Double.parseDouble(mqcValue));
		} catch (Exception e) {
			throw new CustomException("COMMON-0001", "Please input the Numbers");
		}
		autoMQCSetting.setLastRunTime(eventInfo.getEventTime());
		autoMQCSetting.setLastEventUser(eventInfo.getEventUser());
		autoMQCSetting.setLastEventComment(eventInfo.getEventComment());
		autoMQCSetting.setLastEventTime(eventInfo.getEventTime());
		autoMQCSetting.setLastEventName(eventInfo.getEventName());
		autoMQCSetting.setLastEventTimeKey(eventInfo.getEventTimeKey());
		
		ExtendedObjectProxy.getAutoMQCSettingService().create(eventInfo, autoMQCSetting);

		return doc;
	}
}
