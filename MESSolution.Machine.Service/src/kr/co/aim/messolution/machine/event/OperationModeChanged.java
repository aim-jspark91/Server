package kr.co.aim.messolution.machine.event;

import java.util.HashMap;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class OperationModeChanged extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_OperationModeChangedCheckReply");
		
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);
		String operationModeDescription = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODEDESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationMode", getEventUser(), getEventComment(), "", "");
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
		
		if (CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE").equals(operationMode))
		{
			//checkOperationMode
			MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, operationMode);
			
			//pbCardCheck
			if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
				MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(machineName,operationMode,true);
			
			eventLog.warn(String.format("OperationMode already in State[%s],Please Check.", operationMode));
		}
		else
		{
			//checkOperationMode
			MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, operationMode);
			
			/* 20181013, hhlee, add, Check When an equipment is the INDP mode, assign the UNIT to the port. ==>> */
            //if (StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
			if (StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
            {
                MESMachineServiceProxy.getMachineServiceUtil().checkExistenceLinkedUnitName(machineName, GenericServiceProxy.getConstantMap().MACHINETYPE_PRODUCTION);
            }
            /* <<== 20181013, hhlee, add, Check When an equipment is the INDP mode, assign the UNIT to the port. */
            
			/* 20190110, hhlee, add, Update AccessMode ByValidate DoubleMainMachine ==>> */
            MESPortServiceProxy.getPortServiceUtil().updateAccessModeByOperationModeChange(eventInfo, machineName, operationMode);
            /* <<== 20190110, hhlee, add, Update AccessMode ByValidate DoubleMainMachine */
			
			/* 20181107, hhlee, modify , Area => CONSTRUCTTYPE ==>> */
			//pbCardCheck
			//if(StringUtil.equals(machineData.getAreaName(), "TEST"))
			if (CommonUtil.getValue(macSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
				MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(machineName,operationMode,true);
			/* <<== 20181107, hhlee, modify , Area => CONSTRUCTTYPE */
			
			HashMap<String, String> udfs = new HashMap<String, String>();
			udfs.put("OPERATIONMODE", operationMode);
			
			SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
		
		return doc;
	}
}
