package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.jdom.Document;

public class ChangeCOMState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		String sCommunicationName = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, sCommunicationName);
		
		MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);
		
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
