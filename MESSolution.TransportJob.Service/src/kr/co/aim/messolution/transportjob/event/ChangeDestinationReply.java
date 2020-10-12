package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeDestinationReply extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			String oldDestMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", true);
			String oldDestPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
			String newDestMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", true);
			String newDestPositionName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
			String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
			
			String eventName = StringUtils.equals(returnCode, "0") ? "ChangeAccept" : "ChangeReject";
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), "", "");

			// Deleted by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
			//Validation : Exist Carrier
//			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//			MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			//Update CT_TRANSPORTJOBCOMMAND
			TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
			
			// -------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.05.09 - If ChangeDestinationRequest is succeeded, old destination should be released.
			//								   But if ChangeDestinationRequest is failed, new destination should be released.
			if (StringUtils.equals(returnCode, "0")) {
				MachineSpec oldDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(oldDestMachineName));
				
				if(StringUtils.equals(oldDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
					Port oldDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(oldDestMachineName, oldDestPositionName);
					
					if (!oldDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) {
						MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
						makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
						makeTranferStateInfo.setValidateEventFlag("N");
						
						PortServiceProxy.getPortService().makeTransferState(oldDestPortData.getKey(), eventInfo, makeTranferStateInfo);
					}
				}
			} else {
				MachineSpec newDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(newDestMachineName));
				
				if(StringUtils.equals(newDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
					Port newDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(newDestMachineName, newDestPositionName);
					
					if (!newDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) {
						MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
						makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
						makeTranferStateInfo.setValidateEventFlag("N");
						
						PortServiceProxy.getPortService().makeTransferState(newDestPortData.getKey(), eventInfo, makeTranferStateInfo);
					}
				}
			}
			// -------------------------------------------------------------------------------------------------------------------------------------------
			
			// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
/*			MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
			MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
			if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
				// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//				if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//				else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
			}*/
			
			// Added by smkang on 2018.04.12 - Need to forward reply to OPI.
			// Added by smkang on 2018.10.12 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.10.16 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
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
			
			// Added by smkang on 2018.04.12 - Need to forward reply to OPI.
			// Added by smkang on 2018.10.12 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.10.16 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}