package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetMachineStateReply extends AsyncHandler 
{ 
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String machineState  = SMessageUtil.getBodyItemValue(doc, "MACHINESTATE", true);
			String fullState  = SMessageUtil.getBodyItemValue(doc, "FULLSTATE", true);
			
			// Deleted by smkang on 2018.05.01 - StateModel of TransportMachine is not same with ProcessMachine.
//			if(machineState.equals("UP"))
//			{
//				machineState = "RUN";
//			}
//			else if(machineState.equals("DOWN"))
//			{
//				machineState = "IDLE";
//			}
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			// Deleted by smkang on 2018.05.01 - If this machine is not existed in DB, CustomException would be already thrown in getMachineData method.
//			if(machineData == null)
//			{
//				throw new CustomException("MACHINE-9001", machineName);
//			}

			// Modified by smkang on 2018.05.01 - MCS reports FULLSTATE as FULL or NOTFULL, but MES records FULLSTATE as Full or Empty.
			if (StringUtils.equals(fullState, "FULL"))
				fullState = "Full";
			else if (StringUtils.equals(fullState, "NOTFULL"))
				fullState = "Empty";
						
			// Compare Previous State and Current State
			if(!StringUtils.equals(machineData.getMachineStateName(), machineState) || !StringUtils.equals(machineData.getUdfs().get("FULLSTATE"), fullState)) {
				// Modified by smkang on 2018.05.01 - If MachineState is same with previous state and FullState is not same with previous state, CustomException would be thrown in makeMachineStateByState method.
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("FULLSTATE", fullState);
//				makeMachineStateByStateInfo.setUdfs(udfs);
//				
//				MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
				if (!StringUtils.equals(machineData.getMachineStateName(), machineState)) {
					// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//					MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineState);
//					makeMachineStateByStateInfo.setUdfs(udfs);
					MakeMachineStateByStateInfo makeMachineStateByStateInfo = new MakeMachineStateByStateInfo();
					makeMachineStateByStateInfo.setMachineStateName(machineState);
					makeMachineStateByStateInfo.setValidateEventFlag("Y");
					makeMachineStateByStateInfo.getUdfs().put("FULLSTATE", fullState);
					// Added by smkang on 2018.11.20 - According to Wangli's request, OldStateReasonCode is added.
					makeMachineStateByStateInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
					
					MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
				} else {
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("FULLSTATE", fullState);
					
					MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
				}
			} else {
				eventLog.info("Machine[" + machineName + "] is already same state[" + machineState + "/" + fullState + "]");
			}
			
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