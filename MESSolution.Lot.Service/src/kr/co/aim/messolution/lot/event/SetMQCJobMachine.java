package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SetMQCJobMachine extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcJobName = SMessageUtil.getBodyItemValue(doc, "MQCJOBNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUPNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMQCJobMachine", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCJobOper mqcJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[] {mqcJobName, processOperationName, processOperationVersion});
			
		if(mqcJobOper != null)
		{
			MQCJob mqcJob = null;
			try
			{
				mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
			}
			catch (Exception ex)
			{
				mqcJob = null;
			}
			
			if(mqcJob == null)
			{
				throw new CustomException("MQC-0031", mqcJobName);
			}
			
			// 2019.07.09 Park Jeong Su 최위 요구사항으로 JobState 가 Executing 도 SetMQCJobMachine 가능하게 수정 
			/*
			if(StringUtils.equals(mqcJob.getmqcState(), "Executing"))
			{
				throw new CustomException("MQC-0041", mqcJobName);
			}
			*/
			
			if(StringUtils.isEmpty(mqcJobOper.getmachineGroupName()) && StringUtils.isEmpty(mqcJobOper.getmachineName()))
			{
				if(StringUtils.isEmpty(machineGroupName) && StringUtils.isEmpty(machineName))
				{
					throw new CustomException("MQC-0035", "");
				}
			}
			else if(!StringUtils.isEmpty(mqcJobOper.getmachineGroupName()) && !StringUtils.isEmpty(mqcJobOper.getmachineName()))
			{
				if(StringUtils.equals(mqcJobOper.getmachineGroupName(), machineGroupName) && StringUtils.equals(mqcJobOper.getmachineName(), machineName))
				{
					throw new CustomException("MQC-0035", "");
				}
			}
			
			mqcJobOper.setmachineGroupName(machineGroupName);
			mqcJobOper.setmachineName(machineName);
			mqcJobOper.setLastEventUser(eventInfo.getEventUser());
			mqcJobOper.setLastEventComment(eventInfo.getEventComment());
			mqcJobOper.setLastEventTime(eventInfo.getEventTime());
			mqcJobOper.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mqcJobOper.setLastEventName(eventInfo.getEventName());
			ExtendedObjectProxy.getMQCJobOperService().modify(eventInfo, mqcJobOper);
		}
		
		return doc;
	}
}
