package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author smkang
 * @since 2018.05.01
 * @see Processing TEX receives AllUnitReport from MCS.
 */
public class AllUnitReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> unitElementList = SMessageUtil.getBodySequenceItem(doc, "UNITLIST", true).getChildren();
		
		if (unitElementList != null && unitElementList.size() > 0) {
			EventInfo eventInfo = null;
			
			for (Element unitElement : unitElementList) {
				try {
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
					
					String unitName = unitElement.getChildText("UNITNAME");
					String unitState  = unitElement.getChildText("UNITSTATE");
					
					Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
					
					try {
						// Compare Previous State and Current State
						if(!StringUtils.equals(unitData.getMachineStateName(), unitState)) {
							// Added by smkang on 2018.11.20 - According to Wangli's request, OldStateReasonCode is added.
							// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//							unitData.getUdfs().put("OLDSTATEREASONCODE", unitData.getUdfs().get("STATEREASONCODE"));
//							
//							MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(unitData, unitState);
							MakeMachineStateByStateInfo makeMachineStateByStateInfo = new MakeMachineStateByStateInfo();
							makeMachineStateByStateInfo.setMachineStateName(unitState);
							makeMachineStateByStateInfo.setValidateEventFlag("Y");
							makeMachineStateByStateInfo.getUdfs().put("OLDSTATEREASONCODE", unitData.getUdfs().get("STATEREASONCODE"));
							
							MachineServiceProxy.getMachineService().makeMachineStateByState(unitData.getKey(), eventInfo, makeMachineStateByStateInfo);
						} else {
							throw new CustomException("MACHINE-0001", unitData.getKey().getMachineName(), unitData.getMachineStateName(), unitState);
						}
					} catch (InvalidStateTransitionSignal ie) {
						throw new CustomException("MACHINE-9003", unitName);
					} catch (FrameworkErrorSignal fe) {
						throw new CustomException("MACHINE-9999", unitName);
					} catch (NotFoundSignal ne) {
						eventLog.error(ne);
					}
				} catch (CustomException e) {
					// TODO: handle exception
					// Commented by smkang on 2018.05.01 - Although any unit has problem in for loop, another units should be updated.
					//									   So CustomException handler is added here.
				}
			}
			
			try {
				// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
				GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
			} catch(Exception ex) {
				eventLog.warn("FMC Report Failed!");
			}
		}
	}
}