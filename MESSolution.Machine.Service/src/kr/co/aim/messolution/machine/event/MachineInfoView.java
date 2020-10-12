package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

import org.jdom.Document;

public class MachineInfoView extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// Deleted by smkang on 2017.04.17 - Because Note column is not existed in Machine table, this transaction will be ignored.
//		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		String eqpNote = SMessageUtil.getBodyItemValue(doc, "MACHINENOTE", false);
//		
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Note", getEventUser(), "Note", null, null);
//		
//		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
//		
//		Map<String, String> udfs = new HashMap<String, String>();
//		
//		udfs = machineData.getUdfs();
//		udfs.put("NOTE", eqpNote);
//		
//		SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);
//		
//		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		
		return doc;
	}
}