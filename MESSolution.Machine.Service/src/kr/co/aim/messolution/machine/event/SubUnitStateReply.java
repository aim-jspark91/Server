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

public class SubUnitStateReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String communicationState = mainMachineData.getCommunicationState();

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);

		for (Element unitElement : unitList)
		{
			List<Element> subUnitList = SMessageUtil.getSubSequenceItemList(unitElement, "SUBUNITLIST", false);

			for (Element subUnitElement : subUnitList)
			{
				String subUnitName = SMessageUtil.getChildText(subUnitElement, "SUBUNITNAME", true);
				String subUnitStateName = SMessageUtil.getChildText(subUnitElement, "SUBUNITSTATENAME", true); //[ DOWN | ENGINEER | IDLE | PM | RUN ]

				//add by wghuang 20181224, check machineStateName by BC Reported.[RUN,IDLE,DOWN]
				if(MESMachineServiceProxy.getMachineInfoUtil().checkMachineStateName(subUnitStateName) == true)
				{
					throw new CustomException("MACHINE-0027", subUnitStateName);
				}
				
				try
				{
					Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);
					
					/* 20181027, hhlee, modify, GlassOutIndexer, GlassInUnit, GlassInSubUnit Message is not checked a BCASECSSFlag ==>> */
					//// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
					//// Modified by smkang on 2018.10.27 - If previous state is PM, NONSCHEDULEDTIME or MQC, state of unit and subunits should be changed by BC's reply.
					////if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
					//if (machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) || 
					//	machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME) ||
					//	machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC) ||
					//	MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
					//	// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
					//	//MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
					//	eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
					//	machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());
                    //
					//	MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, subUnitStateName);
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
                        //MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
                        eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
                        machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());

                        MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, subUnitStateName);
    
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
				catch (CustomException ce)
				{
					eventLog.warn("SubUnitStateChangeFail : " + ce.getLocalizedMessage());
				}				
			}
		}
	}
}