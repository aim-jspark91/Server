package kr.co.aim.messolution.machine.event;

import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class OperationModeChangeCommandReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException  {
		// TODO Auto-generated method stub
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sOperationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
		String sCommState    = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
		String Result    = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperMode", this.getEventUser(), this.getEventComment(), "", "");
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		if(StringUtil.equals(sCommState, GenericServiceProxy.getConstantMap().Mac_OffLine))
			throw new CustomException("MACHINE-0009", sMachineName);
		
		if(StringUtil.equals(Result, "OK"))
		{
			Map<String, String> udfs = machineData.getUdfs();
			udfs.put("OPERATIONMODE", sOperationMode);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);	
		}
		
		//return doc;
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}

}
