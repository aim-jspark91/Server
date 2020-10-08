package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnitStateReport extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_UnitStateCheckReply");
			
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
        
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", true);
		
		for (Element unitElement : unitList)
		{
			String unitName = SMessageUtil.getChildText(unitElement, "UNITNAME", true);
			String unitStateName = SMessageUtil.getChildText(unitElement, "UNITSTATENAME", true);//[ IDLE | RUN | DOWN]
			
			//add by wghuang 20181224, check machineStateName by BC Reported.[RUN,IDLE,DOWN]
			if(MESMachineServiceProxy.getMachineInfoUtil().checkMachineStateName(unitStateName) == true)
			{
				throw new CustomException("MACHINE-0027", unitStateName);
			}
			
			//Abnormal case:EQP Reports NONE
			if(StringUtil.equals(unitStateName, "NONE"))
				throw new CustomException("MACHINE-0019", machineName);
			
			Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
			
			/* 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold ==>> */
	        String preMachineStateName = machineData.getMachineStateName();
	        /* <<== 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold */
	        
	        /* 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) ==>> */
	        MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
	        boolean isStateModelEventReasonCode =  MESMachineServiceProxy.getMachineServiceUtil().machineStateModelEventReasonCode
	                (eventInfo, machineData, machineSpecData.getMachineStateModelName(), preMachineStateName, unitStateName);        
	        /* <<== 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) */
	        
			// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
			if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) 
			{
				try
				{
					//20190703 wsw If EnumDefValue Mac_OffLine RunStateReasonCode = Machine.STATEREASONCODE then only change state
					String stateReasonCode = machineData.getUdfs().get("STATEREASONCODE");
					
					// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
					machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
					
					// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
	//				MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
					eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
					machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
					
					//20190703 wsw If EnumDefValue Mac_OffLine RunStateReasonCode = Machine.STATEREASONCODE then only change state
					String runReaonCode = MESMachineServiceProxy.getMachineServiceUtil().getRunStateReasonCode("CommunicationState", "Mac_OffLine");
					if(StringUtil.equals(runReaonCode, stateReasonCode))
					{
						MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, unitStateName);
						
						MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
					}
				}
				catch(Exception ex)
				{
					eventLog.warn("Not Registerd State ReasonCode!");
				}
			}
			
			// Added by smkang on 2019.05.20 - According to Liu Hongwei's request, StateReasonCode is necessary.
			unitElement.addContent(new Element("STATEREASONCODE").setText(machineData.getUdfs().get("STATEREASONCODE")));
		}
		
		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
		return doc;
	}
}