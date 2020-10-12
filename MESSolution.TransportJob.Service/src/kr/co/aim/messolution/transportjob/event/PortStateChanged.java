package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.04.28
 * @see Processing TEX receives PortStateChanged from MCS.
 */
public class PortStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portState  = SMessageUtil.getBodyItemValue(doc, "PORTSTATE", true);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		try {
			// Compare Previous State and Current State
			if(!StringUtils.equals(portData.getPortStateName(), portState)) {
				MakePortStateByStateInfo makePortStateByStateInfo = new MakePortStateByStateInfo();
				makePortStateByStateInfo.setPortStateName(portState);
				makePortStateByStateInfo.setValidateEventFlag("Y");
				
				PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);
			} else {
				throw new CustomException("PORT-0007", portData.getKey().getMachineName(), portData.getKey().getPortName(), portData.getPortStateName());
			}
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("PORT-9003", machineName, portName);
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("PORT-9999", machineName, portName);
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