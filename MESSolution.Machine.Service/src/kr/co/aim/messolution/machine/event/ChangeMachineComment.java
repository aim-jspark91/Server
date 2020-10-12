package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeMachineComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeEventComment", this.getEventUser(), this.getEventComment(), "", "");
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		
		return doc;
	}

}
