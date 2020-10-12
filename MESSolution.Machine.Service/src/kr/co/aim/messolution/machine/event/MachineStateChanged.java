package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.jdom.Document;

public class MachineStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		
		//add by wghuang 20181224, check machineStateName by BC Reported.[RUN,IDLE,DOWN]
		if(MESMachineServiceProxy.getMachineInfoUtil().checkMachineStateName(machineStateName) == true)
		{
			throw new CustomException("MACHINE-0027", machineStateName);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		
		//Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);//add by GJJ 20200406, mantis:5968 add rowlock
		//add by GJJ 20200406, mantis:5968 add rowlockstart
		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);
		Machine machineData = MachineServiceProxy.getMachineService().selectByKeyForUpdate(machineKey);
		//add by GJJ 20200406, mantis:5968 add rowlock end
		
		/* 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold ==>> */
		String preMachineStateName = machineData.getMachineStateName();
		/* <<== 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold */
		
		/* 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) ==>> */
		MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
		
		/* 20181116, hhlee, add, add Check MachineStateModel ==>> */
        if(StringUtil.isEmpty(machineSpecData.getMachineStateModelName()))
        {
            throw new CustomException("MACHINE-9005", machineData.getKey().getMachineName());
        }
        /* 20181116, hhlee, add, add Check MachineStateModel ==>> */
		
		boolean isStateModelEventReasonCode =  MESMachineServiceProxy.getMachineServiceUtil().machineStateModelEventReasonCode
		        (eventInfo, machineData, machineSpecData.getMachineStateModelName(), preMachineStateName, machineStateName);		
		/* <<== 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) */
		
		// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
		if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
			// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
			machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
			
			// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
			//MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, machineStateName);
			eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, machineStateName);
			machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
		
			MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
			
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
			
			//159512 by swcho : success then report to FMC
			try
			{
			    
			    /* 20181227, hhlee, Add, add item "STATEREASONCODE" ==>> */
				// Modified by smkang on 2019.05.16 - If an element is not existed in a document, the last argument should be true.
//	            SMessageUtil.setBodyItemValue(doc, "STATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"), false);
	            SMessageUtil.setBodyItemValue(doc, "STATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"), true);
	            /* <<== 20181227, hhlee, Add, add item "STATEREASONCODE" */
	            
				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			}
			catch(Exception ex)
			{
				eventLog.warn("FMC Report Failed!");
			}
		}
		
		/* 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold ==>> */
		// Modified by smkang on 2019.03.12 - According to Feng Huanyan's request, previous state is unnecessary to be checked.
		//if(StringUtil.equals(preMachineStateName, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN) &&
		//	StringUtil.equals(machineStateName, GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN))
		if(StringUtil.equals(machineStateName, GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN))
		{
		    if(!CommonUtil.isInitialInput(machineName))
	        {
		        lotFutureHoldByRunDown(eventInfo, machineName, CommonUtil.getValue(machineSpecData.getUdfs(), "DEPARTMENT"));
		    }
		}
		/* <<== 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold */
	}
	
	private void lotFutureHoldByRunDown(EventInfo eventInfo, String machineName)
	{
	    String condition = " WHERE MACHINENAME = ? AND LOTSTATE = ? AND LOTPROCESSSTATE = ? AND LOTHOLDSTATE <> ? ";

        Object[] bindSet = new Object[] {machineName, GenericServiceProxy.getConstantMap().Lot_Released, 
                                                      GenericServiceProxy.getConstantMap().Lot_Run,
                                                      GenericServiceProxy.getConstantMap().Prq_OnHold};

        try
        {
            List<Lot> lotList = LotServiceProxy.getLotService().select(condition, bindSet);
            for(Lot lotData : lotList)
            {   
                eventInfo.setEventComment(String.format("DOWN EQP [MachineName: %s], Hold Lot [LotName: %s].", machineName, lotData.getKey().getLotName())); 
                MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotData, machineName, "HoldLot", "EQDH");
            }
        }
        catch (Exception ex)
        {
            eventLog.warn(String.format("There is no Lot in progress on the machine[%s]", machineName));
        }	    
	}
	
	private void lotFutureHoldByRunDown(EventInfo eventInfo, String machineName, String machineDepartment)
    {
        String condition = " WHERE MACHINENAME = ? AND LOTSTATE = ? AND LOTPROCESSSTATE = ? AND LOTHOLDSTATE <> ? ";

        Object[] bindSet = new Object[] {machineName, GenericServiceProxy.getConstantMap().Lot_Released, 
                                                      GenericServiceProxy.getConstantMap().Lot_Run,
                                                      GenericServiceProxy.getConstantMap().Prq_OnHold};

        try
        {
            List<Lot> lotList = LotServiceProxy.getLotService().select(condition, bindSet);
            for(Lot lotData : lotList)
            {   
                eventInfo.setEventComment(String.format("DOWN EQP [MachineName: %s], Hold Lot [LotName: %s].", machineName, lotData.getKey().getLotName())); 
                MESLotServiceProxy.getLotServiceUtil().futureHoldLot(eventInfo, lotData, machineName, "HoldLot", "EQDH", machineDepartment);
            }
        }
        catch (Exception ex)
        {
            eventLog.warn(String.format("There is no Lot in progress on the machine[%s]", machineName));
        }       
    }
}
