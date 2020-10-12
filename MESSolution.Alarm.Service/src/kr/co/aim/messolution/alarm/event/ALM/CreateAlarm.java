package kr.co.aim.messolution.alarm.event.ALM;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.12.13
 * @see Processing OPI alarm by ALMsvr.
 */
public class CreateAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), null, null);
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", false);
		String machineName =  SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);		
		String OPIIssue = SMessageUtil.getBodyItemValue(doc, "OPIISSUE", false);
	
//		try {
			AlarmDefinition alarmDefinitionData = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode});
			
			if(StringUtils.equals("EQP", alarmType)){
				
				if(StringUtils.isEmpty(machineName)){
					throw new CustomException("COMMON-0001", "machineName is Empty");
				}
				MESAlarmServiceProxy.getAlarmServiceImpl().createMachineAlarm(alarmDefinitionData,machineName, "", "", alarmCode, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE, eventInfo);
			}
			else
			{
						
				try {
					MESAlarmServiceProxy.getAlarmServiceImpl().createAlarm(alarmCode, alarmDefinitionData.getAlarmSeverity(), GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE,
							alarmDefinitionData.getAlarmType(), alarmDefinitionData.getDescription(), alarmDefinitionData.getFactoryName(), machineName, "", "", "", eventInfo);	
				} catch (Exception ex) {
					eventLog.error("Create Alarm Data Fail !");
					eventLog.error(ex);
				}
				
				try {
					MESAlarmServiceProxy.getAlarmServiceImpl().doAlarmAction(eventInfo, doc, alarmDefinitionData);
				} catch (Exception ex) {	
					eventLog.error(ex);
				}
				
		        try {
					SMessageUtil.setBodyItemValue(doc, "UNITNAME", "", false);
					SMessageUtil.setBodyItemValue(doc, "SUBUNITNAME", "", false);
					SMessageUtil.setBodyItemValue(doc, "ALARMSTATE", GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE, false);
					SMessageUtil.setBodyItemValue(doc, "ALARMTEXT", alarmDefinitionData.getDescription(), false);
		        	
		            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");   
		        } catch(Exception ex) {
		            eventLog.warn("FMC Report Failed!");
		        }
		        
			}

//		}
//		catch(Exception ex)
//		{
//			eventLog.warn("Non Exist Alarm Definition Data !");
//		}
				
		if (StringUtils.equals(OPIIssue, "Y"))
			GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");

		return doc;
	}
}