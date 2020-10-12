package kr.co.aim.messolution.machine.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class SubjectReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sSubjectName = SMessageUtil.getBodyItemValue(doc, "SUBJECTNAME", true);
		
		SetEventInfo setInfo = new SetEventInfo();
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		Map<String, String> udfs = machineData.getUdfs();
		
		udfs.put("MCSUBJECTNAME", sSubjectName);
		
		setInfo.setUdfs(udfs);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignSubject", getEventUser(), getEventComment(), "", "");
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setInfo, eventInfo);
		
		////semi-auto open test
		//try
		//{
		//	GenericServiceProxy.getESBServive().send(sSubjectName, doc);
		//}
		//catch (Exception ex)
		//{
		//	eventLog.error(ex.getMessage());
		//}
	}
}
