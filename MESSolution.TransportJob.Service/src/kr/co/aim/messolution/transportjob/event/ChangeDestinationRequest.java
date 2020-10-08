package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeDestinationRequest  extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRequest", getEventUser(), getEventComment(), "", "");
			
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			String newDestMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", true);
			String newDestPositionName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
			
			//1. Check Exist Carrier
//			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//			MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			//2. Check Transport Job Completed
			TransportJobCommand objJobData = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);

			//add by jhy on20200414 mantis:6020			
			if(StringUtils.equals(newDestMachineName,objJobData.getDestinationMachineName() ) && (StringUtils.equals(newDestPositionName, objJobData.getDestinationPositionName())))
			{
				throw new CustomException("TRANSPORT-2000", "");
			}//add by jhy on20200414 mantis:6020
			
			
			// Modified by smkang on 2018.05.04 - Terminated state should be also considered. 
//			if(objJobData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed)) {
//				throw new CustomException("TRANSPORT-0003", transportJobName);
//			}
			if(objJobData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed) || 
				objJobData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated)) {
				throw new CustomException("TRANSPORT-0003", transportJobName);
			}
			
			// Added by smkang on 2018.04.13 - Need to check ChangeState of CT_TRANSPORTJOBCOMMAND.
			if (StringUtils.isNotEmpty(objJobData.getChangeState()) && 
				(objJobData.getChangeState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested) ||
				objJobData.getChangeState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted) ||
				objJobData.getChangeState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started))) {
				throw new CustomException("TRANSPORT-0003", objJobData.getChangeState());	// Need to define error code.
			}
			
			//Update CT_TRANSPORTJOBCOMMAND
			TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
			
			// -------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.05.09 - First of all, new destination should be reserved.
			//								   But if ChangeDestinationRequest is failed, new destination would be released.
			MachineSpec newDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(newDestMachineName));
			
			if(StringUtils.equals(newDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Port newDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(newDestMachineName, newDestPositionName);
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// Added by smkang on 2019.07.05 - According to Cui Yu's request, validation of RequestTransportJobRequest is necessary too.
				Machine newDestMachineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(newDestMachineName);
				
				if (!newDestMachineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OffLine)) {
					String transportJobType = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportJobTypeByTransportJobName(transportJobCommandInfo.getTransportJobName());
					
					if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC)) {
						String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
						Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
						
						Lot lotData = null;
						try {
							List<Lot> lotDataList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ? AND ROWNUM = 1", new Object[] {carrierName});
							
							if(lotDataList != null) {
								lotData = lotDataList.get(0);
							}
						} catch(Exception ex1) {
						}
						
						MESTransportServiceProxy.getTransportJobServiceUtil().validateCassetteInfoDownloadRequest(durableData, lotData, newDestMachineSpecData, newDestMachineData, newDestPortData, eventInfo);
					}
					
					if(!StringUtils.equals(newDestPortData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
						throw new CustomException("TRANSPORT-0005", newDestMachineName + "/" + newDestPositionName, newDestPortData.getTransferState());
					else if(StringUtils.equals(newDestPortData.getPortStateName(), GenericServiceProxy.getConstantMap().Port_DOWN))
						throw new CustomException("TRANSPORT-0008", newDestMachineName + "/" + newDestPositionName, newDestPortData.getPortStateName());
				}
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
				if (!newDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(newDestPortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
			// -------------------------------------------------------------------------------------------------------------------------------------------

			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
			// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
			/*MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
			MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
			if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
				// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//				if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//				else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
			}*/
			
			if (!System.getProperty("svr").equals(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false)))
				GenericServiceProxy.getESBServive().sendBySender(doc, "HIFSender");
		} catch (Exception e) {
			eventLog.error(e);
			
			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");

			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}