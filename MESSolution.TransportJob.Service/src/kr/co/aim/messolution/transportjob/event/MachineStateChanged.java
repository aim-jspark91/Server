package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MachineStateChanged extends AsyncHandler { 

	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineState  = SMessageUtil.getBodyItemValue(doc, "MACHINESTATE", true);
		String fullState  = SMessageUtil.getBodyItemValue(doc, "FULLSTATE", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		try {
			// Modified by smkang on 2018.05.01 - MCS reports FULLSTATE as FULL or NOTFULL, but MES records FULLSTATE as Full or Empty.
			if (StringUtils.equals(fullState, "FULL"))
				fullState = "Full";
			else if (StringUtils.equals(fullState, "NOTFULL"))
				fullState = "Empty";
			
			// Compare Previous State and Current State
			if(!StringUtils.equals(machineData.getMachineStateName(), machineState) || !StringUtils.equals(machineData.getUdfs().get("FULLSTATE"), fullState)) {
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
				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getMachineStateName() + "/" + machineData.getUdfs().get("FULLSTATE"), machineState + "/" + fullState);
			}
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("MACHINE-9003", machineName);
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("MACHINE-9999", machineName);
		} catch (NotFoundSignal ne) {
			// Modified by smkang on 2018.05.10 - Need to throw exception.
//			eventLog.error(ne);
//			return;
			throw new CustomException(ne);
		}
		
		try {
			// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
		} catch(Exception ex) {
			eventLog.warn("FMC Report Failed!");
		}
	}
}