package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TransportJobCancelCompleted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.05.07 - Design is changed for TransportJobCancelCompleted.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelComplete", getEventUser(), getEventComment(), "", "");
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		
//		//Validation : Exist Carrier
//		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
//		String carrierName        = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		String currentMachineName  = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
//		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
//		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
//		String currentZoneName     = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
//		//String carrierState        = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
//		String transferState       = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
//		//String alternateFlag       = SMessageUtil.getBodyItemValue(doc, "ALTERNATEFLAG", false);
//		
//		//Update CT_TRANSPORTJOBCOMMAND
//		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
//		
//		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select(
//				"TRANSPORTJOBNAME = ?", new Object[] {transportJobName});
//		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
//		
//		Durable durableData = new Durable();
//		try
//		{
//			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//		}
//		catch(Exception e){
//			throw new CustomException("CST-0017", carrierName);
//		}
//		
//		//update Current Carrier Location
//		durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(
//						durableData, currentMachineName, currentPositionType, currentPositionName, 
//						currentZoneName, transferState, "N", eventInfo);
//		
//		//update Port TransferState
//		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
//		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
//		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
//		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
//		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
//		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();
//		
//		try
//		{
//			if(StringUtil.equals(currentMachineName, sourceMachineName) && StringUtil.equals(currentPositionName, sourcePositionName)
//					&& StringUtil.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//			{
//				CommonValidation.checkExistMachine(sourceMachineName);
//				CommonValidation.checkExistPort(sourceMachineName, sourcePositionName);
//				
//				PortKey portKey = new PortKey();
//				portKey.setMachineName(sourceMachineName);
//				portKey.setPortName(sourcePositionName);
//				
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
//				makeTranferStateInfo.setValidateEventFlag("N");
//				
//				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//			}
//			
//			if(StringUtil.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//			{
//				CommonValidation.checkExistMachine(destinationMachineName);
//				
//				CommonValidation.checkExistPort(destinationMachineName, destinationPositionName);
//				
//				PortKey portKey = new PortKey();
//				portKey.setMachineName(destinationMachineName);
//				portKey.setPortName(destinationPositionName);
//				
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//				makeTranferStateInfo.setValidateEventFlag("N");
//				
//				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//			}
//		}
//		catch (Exception ex)
//		{
//			eventLog.error("Port transfer state change failed");
//			eventLog.error(ex.getMessage());
//		}
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelComplete", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		
		// -------------------------------------------------------------------------------------------------------------------------------
		// Deleted by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//		
//		//update Current Carrier Location
//		durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(durableData, currentMachineName, 
//												currentPositionType, currentPositionName, currentZoneName, transferState, "N", eventInfo);
		// -------------------------------------------------------------------------------------------------------------------------------
		
		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		// ---------------------------------------------------------------------------------------------------------------
		// Modified by smkang on 2018.05.08 - Need to check MachineType is ProductionMachine or not.
//		//update Port TransferState
//		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
//		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
//		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
//		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
//		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
//		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();
//		
//		try {
//			if(StringUtil.equals(currentMachineName, sourceMachineName) && 
//				StringUtil.equals(currentPositionName, sourcePositionName) && 
//				StringUtil.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT)) {
//				
//				CommonValidation.checkExistMachine(sourceMachineName);
//				CommonValidation.checkExistPort(sourceMachineName, sourcePositionName);
//				
//				PortKey portKey = new PortKey();
//				portKey.setMachineName(sourceMachineName);
//				portKey.setPortName(sourcePositionName);
//				
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
//				makeTranferStateInfo.setValidateEventFlag("N");
//				
//				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//			}
//			
//			if(StringUtil.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT)) {
//				CommonValidation.checkExistMachine(destinationMachineName);				
//				CommonValidation.checkExistPort(destinationMachineName, destinationPositionName);
//				
//				PortKey portKey = new PortKey();
//				portKey.setMachineName(destinationMachineName);
//				portKey.setPortName(destinationPositionName);
//				
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//				makeTranferStateInfo.setValidateEventFlag("N");
//				
//				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//			}
//		} catch (Exception ex) {
//			eventLog.error("Port transfer state change failed");
//			eventLog.error(ex.getMessage());
//		}
		MachineSpec currentMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(currentMachineName));
		
		if(StringUtils.equals(currentMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
			Port currentPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(currentMachineName, currentPositionName);
			// modify by jhiying on20190906 start mantis:4741
			//if (!currentPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToUnload)) 
			if (currentPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload))
			// modify by jhiying on20190906 end mantis:4741	
			{
				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
				makeTranferStateInfo.setValidateEventFlag("N");
				
				PortServiceProxy.getPortService().makeTransferState(currentPortData.getKey(), eventInfo, makeTranferStateInfo);
			}
		}
		
		String destMachineName = transportJobCommandInfo.getDestinationMachineName();
		String destPositionName = transportJobCommandInfo.getDestinationPositionName();
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destMachineName));
		
		if (StringUtils.isNotEmpty(destMachineName) && StringUtils.isNotEmpty(destPositionName)) {
			if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destMachineName, destPositionName);
				//modify by jhiying on20190919 cimissue:4846 start
				//if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) 
				if (destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) 
				//modify by jhiying on20190919 cimissue:4846 end
				
				{
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
		}
		// ---------------------------------------------------------------------------------------------------------------
		
		// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
		try	{
			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(transportJobCommandInfo.getCarrierName());
			
			// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//			Map<String, String> udfs = carrierData.getUdfs();
			
			// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//			if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "N")) {
			if (!StringUtils.equals(carrierData.getUdfs().get("TRANSPORTLOCKFLAG"), "N")) {
				// Modified by smkang on 2018.12.30 - It is unnecessary to be invoked setEventInfo of DurableInfoUtil.
//				SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");
				
				// Modified by smkang on 2018.12.30 - Need to invoke setEvent of DurableService.
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
				DurableServiceProxy.getDurableService().setEvent(carrierData.getKey(), eventInfo, setEventInfo);
			/*	
				// For synchronization of a carrier information, common method will be invoked.
	            try {
	            	if (currentMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
						Element bodyElement = new Element(SMessageUtil.Body_Tag);
						bodyElement.addContent(new Element("DURABLENAME").setText(transportJobCommandInfo.getCarrierName()));
						bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
						
						// EventName will be recorded triggered EventName.
						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
						
						MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, transportJobCommandInfo.getCarrierName());
	            	}
	            } catch (Exception e) {
	            	eventLog.warn(e);
	            }*/
			}
		} catch (Exception ex) {
			eventLog.error("Unlock failed");
		}

		/*// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
		if (!currentMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			if (currentMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, currentMachineName);
//			else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, destMachineName);
			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
		}*/
	}
}