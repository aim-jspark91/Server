package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.jdom.Document;

public class SubUnitStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String unitStateName = SMessageUtil.getBodyItemValue(doc, "UNITSTATENAME", false);
		String subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String subunitStateName = SMessageUtil.getBodyItemValue(doc, "SUBUNITSTATENAME", false); //[ DOWN| IDLE| RUN ]
			
		//add by wghuang 20181224, check machineStateName by BC Reported.[RUN,IDLE,DOWN]
		if(MESMachineServiceProxy.getMachineInfoUtil().checkMachineStateName(subunitStateName) == true)
		{
			throw new CustomException("MACHINE-0027", subunitStateName);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);
				
		/* 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold ==>> */
        String preMachineStateName = machineData.getMachineStateName();
        /* <<== 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold */
        
        /* 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) ==>> */
        MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
        boolean isStateModelEventReasonCode =  MESMachineServiceProxy.getMachineServiceUtil().machineStateModelEventReasonCode
                (eventInfo, machineData, machineSpecData.getMachineStateModelName(), preMachineStateName, subunitStateName);        
        /* <<== 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) */
        
		// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
		if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
			// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
			machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
			
			// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
//			MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subunitStateName);
			eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subunitStateName);
			machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
		
			MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, subunitStateName);
			
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
			
			//success then report to FMC
			try
			{
				// Added by smkang on 2019.05.20 - According to Liu Hongwei's request, StateReasonCode is necessary.
				SMessageUtil.setBodyItemValue(doc, "STATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"), true);
				
				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			}
			catch(Exception ex)
			{
				eventLog.warn("FMC Report Failed!");
			}
		}
	}
}