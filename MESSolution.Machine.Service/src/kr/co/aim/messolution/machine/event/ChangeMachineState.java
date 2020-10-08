package kr.co.aim.messolution.machine.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeMachineState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);

		// Modified by smkang on 2019.03.28 - According to Xu Lifeng's request, ReasonCode is changed to optional condition.
//		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		String sMachineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);
		
		// Modified by smkang on 2018.05.10 - Exception handling is added for error tracing.
		//									  Before makeMachineStateByStateInfo is invoked, old state and new state should be compared.
//		MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, sMachineStateName);
//		
//		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
		try {
			// Compare Previous State and Current State
//			if(!StringUtils.equals(machineData.getMachineStateName(), sMachineStateName)) {
//				if (!StringUtils.equals(machineData.getMachineStateName(), sMachineStateName)) {
//					MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, sMachineStateName);				
//					MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
//				}
//			} else {
//				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getMachineStateName() + "/" + machineData.getUdfs().get("FULLSTATE"), sMachineStateName);
//			}
			
			// Modified by smkang on 2018.10.08 - According to EDO's request, if a user change machine state to PM or NONSCHEDULEDTIME, state of all units and sub-units should be also changed.
//			MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, sMachineStateName);				
//			MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
			if (sMachineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) 
				|| sMachineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME)
				// 2019.05.13_hsryu_Add 'DOWN' State. if new MachineState is 'DOWN', Unit and SubUnit have to changed. Mantis 0003832.
				|| sMachineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN)
				// add by GJJ mantis 5092 start 20191029
				|| sMachineStateName.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC)) {
				// add by GJJ mantis 5092 end 20191029
				// Modified by smkang on 2018.10.27 - According to EDO's request, if unit is changed to PM or NONSCHEDULEDTIME, subunits should be changed too.
//				List<String> allUnitAndSubUnitNameList = MESMachineServiceProxy.getMachineServiceUtil().getAllUnitAndSubUnitNameList(sMachineName);
				MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(sMachineName);
				
				List<String> allUnitAndSubUnitNameList = null;
				if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN))
					allUnitAndSubUnitNameList = MESMachineServiceProxy.getMachineServiceUtil().getAllUnitAndSubUnitNameList(sMachineName);
				else if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT))
					allUnitAndSubUnitNameList = MESMachineServiceProxy.getMachineServiceUtil().getAllSubUnitNameList(sMachineName);
				else if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT)) {
					allUnitAndSubUnitNameList = new ArrayList<String>();
					allUnitAndSubUnitNameList.add(sMachineName);
				}
				
				for (String unitName : allUnitAndSubUnitNameList) {
					Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
					
					MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, sMachineStateName);
					
					// Modified by smkang on 2019.03.28 - Update selected udfs only.
//					// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
//					machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
//					
//					// Added by smkang on 2018.10.26 - ReasonCode column is used commonly, so StateReasonCode is added.
//					machineData.getUdfs().put("STATEREASONCODE", sReasonCode);
					makeMachineStateByStateInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
					makeMachineStateByStateInfo.getUdfs().put("STATEREASONCODE", sReasonCode);
					
					MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
				}
			} else {
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
				
				// Added by smkang on 2018.10.22 - If previous state is PM or NONSCHEDULEDTIME, state of unit and subunits should be changed by BC's reply.
				String previousState = machineData.getMachineStateName();
				
				MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, sMachineStateName);

				// Modified by smkang on 2019.03.28 - Update selected udfs only.
//				// Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
//				machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
//				
//				// Added by smkang on 2018.10.26 - ReasonCode column is used commonly, so StateReasonCode is added.
//				machineData.getUdfs().put("STATEREASONCODE", sReasonCode);
				makeMachineStateByStateInfo.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
				makeMachineStateByStateInfo.getUdfs().put("STATEREASONCODE", sReasonCode);
				
				MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
				
				// 2019.05.16_hsryu_Add 'DOWN' State. Requested by CIM. Mantis 0003832.
				// Added by smkang on 2018.10.22 - If previous state is PM or NONSCHEDULEDTIME, state of unit and subunits should be changed by BC's reply.
				// Modified by smkang on 2018.10.27 - According to EDO's request, MQC state will be contained this rule.
//				if (previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) || previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME)) {
				if (previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) || previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME) || 
					previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC)|| previousState.equals(GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN)) {
					
					if (!machineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OffLine)) {
						try {
							// Modified by smkang on 2018.10.27 - According to EDO's request, if unit is changed to PM or NONSCHEDULEDTIME, subunits should be changed too.
//							Element bodyElement = new Element(SMessageUtil.Body_Tag);
//							bodyElement.addContent(new Element("MACHINENAME").setText(sMachineName));
//							Document stateRequestMessage = SMessageUtil.createXmlDocument(bodyElement, "A_UnitStateRequest", "", "", System.getProperty("svr"), SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", false));
//							
//							if (stateRequestMessage != null) {
//								GenericServiceProxy.getESBServive().sendBySender(stateRequestMessage, "PEXSender");
//								
//								SMessageUtil.setHeaderItemValue(stateRequestMessage, "MESSAGENAME", "A_SubUnitStateRequest");
//								GenericServiceProxy.getESBServive().sendBySender(stateRequestMessage, "PEXSender");
//							}
							MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(sMachineName);
							
							if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN)) {
								Element bodyElement = new Element(SMessageUtil.Body_Tag);
								bodyElement.addContent(new Element("MACHINENAME").setText(sMachineName));
								Document stateRequestMessage = SMessageUtil.createXmlDocument(bodyElement, "A_UnitStateRequest", "", "", System.getProperty("svr"), SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", false));
								
								if (stateRequestMessage != null) {
									GenericServiceProxy.getESBServive().sendBySender(stateRequestMessage, "PEXSender");
									//2019.03.06_hsryu_add UnitName.
									bodyElement.addContent(new Element("UNITNAME").setText(""));
									SMessageUtil.setHeaderItemValue(stateRequestMessage, "MESSAGENAME", "A_SubUnitStateRequest");
									GenericServiceProxy.getESBServive().sendBySender(stateRequestMessage, "PEXSender");
								}
							} else if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT)) {
								Element bodyElement = new Element(SMessageUtil.Body_Tag);
								bodyElement.addContent(new Element("MACHINENAME").setText(machineSpecData.getSuperMachineName()));
								//2019.03.06_hsryu_add UnitName.
								bodyElement.addContent(new Element("UNITNAME").setText(sMachineName));
								Document stateRequestMessage = SMessageUtil.createXmlDocument(bodyElement, "A_SubUnitStateRequest", "", "", System.getProperty("svr"), SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", false));
								
								if (stateRequestMessage != null) {
									GenericServiceProxy.getESBServive().sendBySender(stateRequestMessage, "PEXSender");
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							eventLog.warn(e);
						}
					} else {
						// OffLine Display
						eventLog.warn("CommunicationState is OffLine, so UnitState and SubUnitState are not changed");
					}
				}
			}
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("MACHINE-9003", sMachineName);
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("MACHINE-9999", sMachineName);
		} catch (NotFoundSignal ne) {
			throw new CustomException(ne);
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			// Added by smkang on 2019.05.20 - According to Liu Hongwei's request, StateReasonCode is necessary.
			SMessageUtil.setBodyItemValue(doc, "STATEREASONCODE", sReasonCode, true);
			
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
		
		return doc;
	}
}