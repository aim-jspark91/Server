package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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

public class CancelTransportJobReply extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
						
			String eventName = StringUtils.equals(returnCode, "0") ? "CancelAccept" : "CancelReject";
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), "", "");
			
			// Added by smkang on 2018.05.04 - Need to check the carrier is existed in MES DB.
			// Deleted by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
			//Validation : Exist Carrier
//			MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			// Modified by smkang on 2019.01.30 - If MCS replies CancelTransportJobReply with CARRIER_NOT_FOUND(035) or COMMAND_NOT_FOUND(041), the transport job will be removed.
			TransportJobCommand transportJobCommandInfo = null;			
			if (StringUtils.equals(returnCode, "035") || StringUtils.equals(returnCode, "041")) {
				transportJobCommandInfo = ExtendedObjectProxy.getTransportJobCommandService().selectByKey(false, new Object[] {transportJobName});
				
				List<TransportJobCommand> transportJobList = new ArrayList<TransportJobCommand>();
				transportJobList.add(transportJobCommandInfo);
				
				MESTransportServiceProxy.getTransportJobServiceImpl().terminateTransportJob(doc, transportJobList, eventInfo);
			} else {
				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
			}
			
			MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
			MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
			
			// Deleted by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
			//									 Although MCS reports CancelTransportJobReply with ReturnCode of '0', TransportJobCancelFailed can be occurred.
//			if(StringUtils.equals(returnCode, "0"))
//			{
//				try
//				{
//					Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//					
//					Map<String, String> udfs = carrierData.getUdfs();
//					
//					if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "N")) {
//						udfs.put("TRANSPORTLOCKFLAG", "N");
//						
//						SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
//						MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
//						
//						// Added by smkang on 2018.11.03 - For synchronization of a carrier information, common method will be invoked.
//			            try {
//			            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
//								Element bodyElement = new Element(SMessageUtil.Body_Tag);
//								bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
//								bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
//								
//								// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
////								Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
//								Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
//								
//								MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
//			            	}
//			            } catch (Exception e) {
//			            	eventLog.warn(e);
//			            }
//					}
//				}
//				catch (Exception ex)
//				{
//					eventLog.error("Unlock failed");
//				}
//			}
			
			/*// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.		
			if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
				// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//				if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//				else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
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