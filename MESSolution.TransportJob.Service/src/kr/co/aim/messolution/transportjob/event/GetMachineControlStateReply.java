package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetMachineControlStateReply extends AsyncHandler 
{ 
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), "", "");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String machineControlState  = SMessageUtil.getBodyItemValue(doc, "MACHINECONTROLSTATE", true);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			// Deleted by smkang on 2018.05.01 - If this machine is not existed in DB, CustomException would be already thrown in getMachineData method.
//			if(machineData == null)
//			{
//				throw new CustomException("MACHINE-9001", machineName);
//			}
			
			// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			// Modified by smkang on 2018.05.02 - Need to compare previous state and current state.
//			MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, machineControlState);
//			
//			MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);
			
			// Commented by smkang on 2018.05.02 - MCS reports MACHINECONTROLSTATE as OFFLINE, LOCAL or REMOTE, 
			//									   but MES records COMMUNICATIONSTATE as OffLine, OnLineLocal or OnLineRemote.
			if (StringUtils.equals(machineControlState, "OFFLINE"))
				machineControlState = GenericServiceProxy.getConstantMap().Mac_OffLine;
			else if (StringUtils.equals(machineControlState, "LOCAL"))
				machineControlState = GenericServiceProxy.getConstantMap().Mac_OnLineLocal;
			else if (StringUtils.equals(machineControlState, "REMOTE"))
				machineControlState = GenericServiceProxy.getConstantMap().Mac_OnLineRemote;
						
			// Compare Previous State and Current State
			if(!StringUtils.equals(machineData.getCommunicationState(), machineControlState)) {
				MakeCommunicationStateInfo makeCommunicationStateInfo = new MakeCommunicationStateInfo();
				makeCommunicationStateInfo.setCommunicationState(machineControlState);
				makeCommunicationStateInfo.setValidateEventFlag("Y");
				
				MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);
			} else {
				// Modified by smkang on 2018.12.02 - If an exception throws in AsyncHandler class, OPI can't receive the exception and timeout occurs.
//				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getCommunicationState(), machineControlState);
				eventLog.info("Machine[" + machineName + "] is already " + machineControlState);
			}
			// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			
			// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");
			
			// Added by smkang on 2018.05.02 - Need to forward reply to OPI.
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		} catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			// Added by smkang on 2018.05.02 - Need to forward reply to OPI.
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}