package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MCSStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		// Modified by smkang on 2018.05.04 - If MCS changes to INACTIVE state, all transport machines would be supposed OFFLINE state.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MCSStateChanged", getEventUser(), getEventComment(), "", "");
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		String mcsState = SMessageUtil.getBodyItemValue(doc, "MCSSTATE", true);
		if (mcsState.equals("INACTIVE")) {
			String condition = "detailMachineType = ? AND machineType in (?, ?)";
			
			Object[] bindSet = new Object[3];
			bindSet[0] = "MAIN";
			bindSet[1] = GenericServiceProxy.getConstantMap().Mac_StorageMachine;
			bindSet[2] = GenericServiceProxy.getConstantMap().Mac_TransportMachine;
			
			List<MachineSpec> machineSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
			if (machineSpecList != null && machineSpecList.size() > 0) {
				for (MachineSpec machineSpecData : machineSpecList) {
					try {
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), "", "");
						
						Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineSpecData.getKey().getMachineName());
						
						try {
							String machineControlState = GenericServiceProxy.getConstantMap().Mac_OffLine;
							
							// Compare Previous State and Current State
							if(!StringUtils.equals(machineData.getCommunicationState(), machineControlState)) {
								MakeCommunicationStateInfo makeCommunicationStateInfo = new MakeCommunicationStateInfo();
								makeCommunicationStateInfo.setCommunicationState(machineControlState);
								makeCommunicationStateInfo.setValidateEventFlag("Y");
								
								MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);
							}
						} catch (InvalidStateTransitionSignal ie) {
							throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
						} catch (FrameworkErrorSignal fe) {
							throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
						} catch (DuplicateNameSignal de) {
							throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
						} catch (NotFoundSignal ne) {
							// Commented by smkang on 2018.05.04 - If this machine is not existed in DB, CustomException would be already thrown in getMachineData method.
							eventLog.error(ne);
						}
					} catch (CustomException e) {
						// TODO: handle exception
						// Commented by smkang on 2018.05.04 - Although any machine has problem in for loop, another machines should be updated.
						//									   So CustomException handler is added here.
					}
				}
				
				try {
					// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//					GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
					GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
				} catch(Exception ex) {
					eventLog.warn("FMC Report Failed!");
				}
			}			
		}
	}
}