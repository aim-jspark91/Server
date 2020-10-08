package kr.co.aim.messolution.machine.event;

import java.util.Map;

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
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class OperationModeReport extends SyncHandler {

	/**====================================================================
	 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
	 * ====================================================================
	 */
	//private SyncHandler synchandler;

	@Override
	public Object doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationMode", getEventUser(), getEventComment(), "", "");

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_OperationModeCheckReply");

		/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);
		String operationModeDescription = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODEDESCRIPTION", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);

		String currentCommunicationName = machineData.getCommunicationState();

		if (CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE").equals(operationMode))
		{
			//checkOperationMode by ToPolicy
			MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, operationMode);

			if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL) ||
			   StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
			{
				//pbCardCheck
			    if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
					MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(machineName,operationMode,true);
			}

			eventLog.warn(String.format("OperationMode already in State[%s],Please Check.", operationMode));
		}
		else
		{
			//checkOperationMode by ToPolicy
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
			
			if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL) ||
			   StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
			{
				//pbCardCheck
			    if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
					MESDurableServiceProxy.getDurableServiceUtil().checkProbeCardCondition(machineName,operationMode,true);
			}

			
			eventInfo.setEventName("ChangeOperationMode");

			Map<String, String> udfs = machineData.getUdfs();
			udfs.put("OPERATIONMODE", operationMode);
			machineData.setUdfs(udfs);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}

		try
		{
					
			eventInfo.setEventName("ChangeCommState");

			MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, CommonUtil.getValue(machineData.getUdfs(),"ONLINEINITIALCOMMSTATE"));

			String CommunicationState = "";

			Map<String, String> machineUdfs = machineData.getUdfs();
			CommunicationState = CommonUtil.getValue(machineUdfs, "ONLINEINITIALCOMMSTATE");

			//add by wghuang 20180615
			//Same value Check		
			if(StringUtils.equals(machineData.getCommunicationState(), CommunicationState))
			{	
				eventLog.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
				
				//just left log
				CommonUtil.CustomExceptionLog("MACHINE-0001", machineData.getKey().getMachineName(),machineData.getCommunicationState(), CommunicationState);
				
				//put empty value to ONLINEINITIALCOMMSTATE
				Map<String, String> Udfs = machineData.getUdfs();
				Udfs.put("ONLINEINITIALCOMMSTATE", "");
				machineData.setUdfs(Udfs);

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(Udfs);
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
			}
			else
			{
				machineUdfs.put("ONLINEINITIALCOMMSTATE", "");
				machineData.setUdfs(machineUdfs);

				transitionInfo.setUdfs(machineUdfs);
				//transitionInfo.setValidateEventFlag("N");
				transitionInfo.setCommunicationState(CommunicationState);
				MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);
			}		
		}
		catch(Exception ex)
		{
			eventLog.warn(String.format("[%s]%s", getOriginalSourceSubjectName(), "EISSender Send Failed!"));
		}

		return doc;
	}
}
