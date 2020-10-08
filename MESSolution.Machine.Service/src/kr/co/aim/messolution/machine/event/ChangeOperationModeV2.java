package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class ChangeOperationModeV2 extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sOperationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
				
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		if(StringUtil.equals(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OffLine))
			throw new CustomException("MACHINE-0009", sMachineName);
						
		//from CNX to PEX
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
				
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEXSender");
				
	}

}
