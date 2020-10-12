package kr.co.aim.messolution.machine.event;

import java.util.HashMap;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class OperationModeReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationMode", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);
		String operationModeDescription = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODEDESCRIPTION", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String currentCommunicationName = machineData.getCommunicationState();

		if (CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE").equals(operationMode))
		{
			eventLog.warn(String.format("OperationMode already in State[%s],Please Check.", operationMode));
		}
		else
		{
			//checkOperationMode
			MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, operationMode);

			eventInfo.setEventName("ChangeOperationMode");

			HashMap<String, String> udfs = new HashMap<String, String>();
			udfs.put("OPERATIONMODE", operationMode);

			SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);

			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
	}
}
