package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author smkang
 * @since 2018.04.14
 * @see When TEX server receives GetUnitStateReply, TEX server forwards the message to OPI.
 */
public class GetUnitStateReply extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		// Added by smkang on 2018.12.02 - Need to update unit state.
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			
			List<Element> unitElementList = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", true);
			
			for(Element unitElement : unitElementList) {
				try {
					String unitName = unitElement.getChildText("UNITNAME");
					String unitState = unitElement.getChildText("UNITSTATE");
				
					Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
				
					// Compare Previous State and Current State
					if(!StringUtils.equals(unitData.getMachineStateName(), unitState)) {
						// According to Wangli's request, OldStateReasonCode is added.
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//						unitData.getUdfs().put("OLDSTATEREASONCODE", unitData.getUdfs().get("STATEREASONCODE"));
//						
//						MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(unitData, unitState);
//						makeMachineStateByStateInfo.setUdfs(unitData.getUdfs());
						MakeMachineStateByStateInfo makeMachineStateByStateInfo = new MakeMachineStateByStateInfo();
						makeMachineStateByStateInfo.setMachineStateName(unitState);
						makeMachineStateByStateInfo.setValidateEventFlag("Y");
						makeMachineStateByStateInfo.getUdfs().put("OLDSTATEREASONCODE", unitData.getUdfs().get("STATEREASONCODE"));
						
						MachineServiceProxy.getMachineService().makeMachineStateByState(unitData.getKey(), eventInfo, makeMachineStateByStateInfo);
					} else {
						eventLog.info("Unit[" + machineName + "/" + unitName + "] is already same state[" + unitState + "]");
					}
				} catch (Exception e) {
					eventLog.error(e);
				}
			}
			
			// sendSubjectName of infra configuration will be used instead of ESBservice.
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");
			
			// Need to forward reply to OPI.
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		} catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			// Need to forward reply to OPI.
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}