package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CancelTransportJobRequest extends AsyncHandler
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelRequest", getEventUser(), getEventComment(), "", "");
			
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			
			TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
			
			// Modified by smkang on 2018.05.04 - Started state should be removed and Completed state should be added.
//			if(!StringUtils.equals(transprotJobCommandInfo.getJobState(), "Started") || 
//				StringUtils.equals(transprotJobCommandInfo.getJobState(), "Terminated") ||
//				StringUtils.equals(transprotJobCommandInfo.getJobState(), "Rejected")) {
			// Deleted by smkang on 2018.10.20 - According to EDO Cuiyu's request, Canceling is accepted.
//			if(StringUtils.isNotEmpty(transportJobCommandInfo.getJobState()) && 
//				(StringUtils.equals(transportJobCommandInfo.getJobState(), GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Terminated) ||
//				StringUtils.equals(transportJobCommandInfo.getJobState(), GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected) ||
//				StringUtils.equals(transportJobCommandInfo.getJobState(), GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed))) {
//				throw new CustomException("JOB-2011", transportJobCommandInfo.getJobState());
//			}
			
			// Added by smkang on 2018.04.13 - Need to check CancelState of CT_TRANSPORTJOBCOMMAND.
			if (StringUtils.isNotEmpty(transportJobCommandInfo.getCancelState()) && 
				(transportJobCommandInfo.getCancelState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested) || 
				transportJobCommandInfo.getCancelState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted) || 
				transportJobCommandInfo.getCancelState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started))) {
				throw new CustomException("JOB-2011", transportJobCommandInfo.getCancelState());	// Need to define error code.
			}
			
			//Update CT_TRANSPORTJOBCOMMAND
			MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
			
			//setReplySubjectName("");
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
			// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
			//MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
			//MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
		/*	if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
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