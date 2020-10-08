package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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
import org.jdom.Element;

public class SubUnitStateReport extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_SubUnitStateCheckReply");

		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

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
								
				/* 20190118, hhlee, delete, temporarily */
				/* 20190130, hhlee, applied validation  */
				Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);
				
				try
                {	
					/* 20190118, hhlee, delete, temporarily         */
				    /* 20190130, hhlee, delete, applied validation  */
					//Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);
					
					/* 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold ==>> */
		            String preMachineStateName = machineData.getMachineStateName();
		            /* <<== 20180920, hhlee, Add, when the equipment is 'Down', Lot Future Hold */
		            
		            /* 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) ==>> */
		            MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
		            boolean isStateModelEventReasonCode =  MESMachineServiceProxy.getMachineServiceUtil().machineStateModelEventReasonCode
		                    (eventInfo, machineData, machineSpecData.getMachineStateModelName(), preMachineStateName, subUnitStateName);        
		            /* <<== 20181027, hhlee, Add, Check StateModelEvent STATEREASONCODE(For MQC) */
		            
					// Added by smkang on 2018.10.08 - According to EDO's request, MES decides and finds ReasonCode for machine state.
					if (MESMachineServiceProxy.getMachineServiceUtil().needToUpdateBCMachineState(machineData)) {
						// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
						machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
						
						// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
//						MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
						eventInfo = MESMachineServiceProxy.getMachineServiceUtil().adjustMachineStateReasonCode(eventInfo, machineName, subUnitStateName);
						machineData.getUdfs().put("STATEREASONCODE", eventInfo.getReasonCode());

						MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, subUnitStateName);
	
						MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
					}
				}
				catch (CustomException ce)
				{
					eventLog.warn("SubUnitStateChangeFail : " + ce.getLocalizedMessage());
				}
				
				// Added by smkang on 2019.05.20 - According to Liu Hongwei's request, StateReasonCode is necessary.
				subUnitElement.addContent(new Element("STATEREASONCODE").setText(machineData.getUdfs().get("STATEREASONCODE")));
			}
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
