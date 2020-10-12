package kr.co.aim.messolution.port.event;


import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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

import org.jdom.Document;

public class ChangePortState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		String sPortStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATENAME", true);
		
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
		
		// Modified by smkang on 2018.05.10 - Exception handling is added for error tracing.
		//									  Before makePortStateByStateInfo is invoked, old state and new state should be compared.
//		MakePortStateByStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);
//		
//		MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, transitionInfo, eventInfo);
		try {
			// Compare Previous State and Current State
//			if(!StringUtils.equals(portData.getPortStateName(), sPortStateName)) {
//				MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);				
//				PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);
//			} else {
//				throw new CustomException("PORT-0007", portData.getKey().getMachineName(), portData.getKey().getPortName(), portData.getPortStateName());
//			}
			MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);				
			PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("PORT-9003", sMachineName, sPortName);
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("PORT-9999", sMachineName, sPortName);
		} catch (NotFoundSignal ne) {
			// Modified by smkang on 2018.05.10 - Need to throw exception.
//					eventLog.error(ne);
//					return;
			throw new CustomException(ne);
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
		return doc;
	}

}
