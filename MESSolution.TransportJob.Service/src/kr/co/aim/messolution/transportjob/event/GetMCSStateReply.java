package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.04.14
 * @see After TEX server starts up, TEX server requests state of MCS.
 */
public class GetMCSStateReply  extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			// Commented by smkang on 2018.04.14 - When TEX server receives this message, TEX server should do something?
			// Added by smkang on 2018.05.04 - If MCS changes to INACTIVE state, all transport machines would be supposed OFFLINE state.
			String mcsState = SMessageUtil.getBodyItemValue(doc, "MCSSTATE", true);
			if (mcsState.equals("INACTIVE")) {
				String condition = "DETAILMACHINETYPE = ? AND MACHINETYPE IN (?, ?)";
				
				Object[] bindSet = new Object[] {GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN, 
												GenericServiceProxy.getConstantMap().Mac_StorageMachine, 
												GenericServiceProxy.getConstantMap().Mac_TransportMachine};
				
				List<MachineSpec> machineSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
				if (machineSpecList != null && machineSpecList.size() > 0) {
					for (MachineSpec machineSpecData : machineSpecList) {
						try {
							EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), "", "");
							
							Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineSpecData.getKey().getMachineName());
							
							String machineControlState = GenericServiceProxy.getConstantMap().Mac_OffLine;
							
							// Compare Previous State and Current State
							if(!StringUtils.equals(machineData.getCommunicationState(), machineControlState)) {
								MakeCommunicationStateInfo makeCommunicationStateInfo = new MakeCommunicationStateInfo();
								makeCommunicationStateInfo.setCommunicationState(machineControlState);
								makeCommunicationStateInfo.setValidateEventFlag("Y");
								
								MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);
							}
						} catch (Exception e) {
							// TODO: handle exception
							// Commented by smkang on 2018.05.04 - Although any machine has problem in for loop, another machines should be updated.
							//									   So CustomException handler is added here.
							eventLog.info(e);
						}
					}
					
					// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//					GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
					GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");
				}			
			}
			
			// Added by smkang on 2018.05.04 - Suppose that GetMCSStateRequest will be requested by OPI. 
			//								   Need to forward reply to OPI.
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
			
			// Added by smkang on 2018.05.04 - Suppose that GetMCSStateRequest will be requested by OPI. 
			//								   Need to forward reply to OPI.
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}