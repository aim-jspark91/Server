package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnitStateReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String communicationState = mainMachineData.getCommunicationState();

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", true);

		for (Element unitElement : unitList)
		{
			String unitName = SMessageUtil.getChildText(unitElement, "UNITNAME", true);
			String unitStateName = SMessageUtil.getChildText(unitElement, "UNITSTATENAME", true);//[ IDLE | RUN | DOWN | MAINT ]

			//add by wghuang 20181224, check machineStateName by BC Reported.[RUN,IDLE,DOWN]
			if(MESMachineServiceProxy.getMachineInfoUtil().checkMachineStateName(unitStateName) == true)
			{
				throw new CustomException("MACHINE-0027", unitStateName);
			}
			
			Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
			
			/* 20181027, hhlee, modify, GlassOutIndexer, GlassInUnit, GlassInSubUnit Message is not checked a BCASECSSFlag ==>> */
			//// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
			//// Modified by smkang on 2018.10.27 - If previous state is PM, NONSCHEDULEDTIME or MQC, state of unit and subunits should be changed by BC's reply.
			////if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
			//if (machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) || 
			//	machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME) ||
			//	machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC) ||
			//	MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
			//	// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
			//	//MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
			//	eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
			//	machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
            //
			//	MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, unitStateName);
            //	
			//	MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
			//	
			//	//success then report to FMC
			//	try
			//	{
			//		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
			//	}
			//	catch(Exception ex)
			//	{
			//		eventLog.warn("FMC Report Failed!");
			//	}
			//}
			// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
            // Modified by smkang on 2018.10.27 - If previous state is PM, NONSCHEDULEDTIME or MQC, state of unit and subunits should be changed by BC's reply.
            //if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
            if (machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) || 
                machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME) ||
                machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC) ||
                // 2019.05.16_hsryu_Add 'DOWN' State. Requested by CIM. Mantis 0003832.
                machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN))
                {
					// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
					machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
				
                    // Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
                    //MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
                    eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, unitStateName);
                    machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
    
                    MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, unitStateName);
        
                    MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
                    
                    //success then report to FMC
                    try
                    {
                        GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
                    }
                    catch(Exception ex)
                    {
                        eventLog.warn("FMC Report Failed!");
                    }
            }
			/* <<== 20181027, hhlee, modify, GlassOutIndexer, GlassInUnit, GlassInSubUnit Message is not checked a BCASECSSFlag */
		}
	}
}