package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MachineControlStateChanged extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineControlState  = SMessageUtil.getBodyItemValue(doc, "MACHINECONTROLSTATE", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		try {
			// Commented by smkang on 2018.05.05 - MCS reports MACHINECONTROLSTATE as OFFLINE, LOCAL or REMOTE, 
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
				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getCommunicationState(), machineControlState);
			}
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		} catch (DuplicateNameSignal de) {
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		} catch (NotFoundSignal ne) {
			// Commented by smkang on 2018.05.05 - If this machine is not existed in DB, CustomException would be already thrown in getMachineData method.
			// Modified by smkang on 2018.05.10 - Need to throw exception.
//			eventLog.error(ne);
//			return;
			throw new CustomException(ne);
		}
		
		// Deleted by smkang on 2018.05.28 - It is unnecessary to reply to OIC.
//		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		
		//success then report to FMC
		try  {
			// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
		} catch(Exception ex) {
			eventLog.warn("FMC Report Failed!");
		}
		
		// Added by smkang on 2018.10.12 - Need to forward a message to linked factory.
		// Modified by smkang on 2018.10.23 - According to EDO's request, inventory data and machine control state should be synchronized with shared factory without CT_SHIPPINGSTOCKER.
//		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, machineName);
		//MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc);
	}
}