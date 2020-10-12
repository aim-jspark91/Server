package kr.co.aim.messolution.machine.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeMachineHoldState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		String sMachineHoldState = SMessageUtil.getBodyItemValue(doc, "MACHINEHOLDSTATE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Machine.class.getSimpleName());
		
		SetEventInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, transitionInfo, eventInfo);
		
		return doc;
	}

}
