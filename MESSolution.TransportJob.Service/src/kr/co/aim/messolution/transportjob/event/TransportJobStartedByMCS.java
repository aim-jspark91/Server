package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TransportJobStartedByMCS extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.05.07 - Design is changed for TransportJobStartedByMCS.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		
//		//Validation : Exist Carrier
//		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
//		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
//		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
//		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
//		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
//		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
//		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
//		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
//		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
//		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
//		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
//		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
//		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
//		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
//		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
//		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
//		
//		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
//		
//		MESTransportServiceProxy.getTransportJobServiceUtil();
//		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);	
//		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
//		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
//		
//		String alternateFlag = "N";
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
//		MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);
//		//MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);
//		
//		//if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
//		//	throw new CustomException("CST-0005", carrierName);
//		
//		//String requestSubjectName = "";
//		String transportJobType = "";
//		if(StringUtils.equals(messageName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS))
//		{
//			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
//			//requestSubjectName = SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false);
//		}
//		else if(StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
//		{
//			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
//			//requestSubjectName = SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false);
//		}
//		else
//		{
//			//transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
//		}
//		
//		String lotName = "";
//		Lot lotData = new Lot();
//		try
//		{
//			String condition = "Where carrierName = ? and RowNum = 1 ";
//			Object[] bindSet = new Object[] {carrierName};
//			
//			List<Lot> lotList = LotServiceProxy.getLotService().select(condition, bindSet);
//			lotName = lotList.get(0).getKey().getLotName();
//			LotKey lotKey = new LotKey();
//			lotKey.setLotName(lotName);
//			
//			lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
//		}
//		catch(Exception e){}
//		
//		//String processOperationName = lotData.getProcessOperationName();
//		double productQuantity = lotData.getProductQuantity();
//		
//		String carrierState = "";
//		if(lotName == null || lotName.isEmpty())
//			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
//		else
//			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
//		
//		TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
//		transportJobCommandInfo.setTransportJobName(transportJobName);
//		transportJobCommandInfo.setCarrierName(carrierName);
//		transportJobCommandInfo.setTransportJobType(transportJobType);
//		transportJobCommandInfo.setJobState(jobState);
//		transportJobCommandInfo.setCancelState(cancelState);
//		transportJobCommandInfo.setChangeState(changeState);
//		transportJobCommandInfo.setAlternateFlag(alternateFlag);
//		transportJobCommandInfo.setTransferState(transferState);
//		transportJobCommandInfo.setPriority(priority);
//		transportJobCommandInfo.setSourceMachineName(sourceMachineName);
//		transportJobCommandInfo.setSourcePositionType(sourcePositionType);
//		transportJobCommandInfo.setSourcePositionName(sourcePositionName);
//		transportJobCommandInfo.setSourceZoneName(sourceZoneName);
//		transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
//		transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
//		transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
//		transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
//		transportJobCommandInfo.setCurrentMachineName(currentMachineName);
//		transportJobCommandInfo.setCurrentPositionType(currentPositionType);
//		transportJobCommandInfo.setCurrentPositionName(currentPositionName);
//		transportJobCommandInfo.setCurrentZoneName(currentZoneName);
//		transportJobCommandInfo.setCarrierState(carrierState);
//		transportJobCommandInfo.setLotName(lotName);
//		//transportJobCommandInfo.setProcessOperationName(processOperationName);
//		transportJobCommandInfo.setProductQuantity((long)productQuantity);
//		//transportJobCommandInfo.setReasonCode("");
//		//transportJobCommandInfo.setReasonCodeType("");
//		//transportJobCommandInfo.setRequestSubjectName(requestSubjectName);
//		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
//		transportJobCommandInfo.setLastEventResultCode(returnCode);
//		transportJobCommandInfo.setLastEventResultText(returnMessage);
//		
//		try
//		{
//			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
//			
//			doc = SMessageUtil.addItemToBody(doc, "TRANSPORTJOBNAME", transportJobName);
//		}
//		catch(Exception e)
//		{
//			throw new CustomException("JOB-8011", e.getMessage());
//		}
//		
//		//update Current Carrier Location
//		durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(
//						durableData, 
//						currentMachineName, currentPositionType, currentPositionName, currentZoneName, 
//						transferState, "Y", eventInfo);
//		
//		//update Port TransferState (Source Port Already Unloaded)
//		if(StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//		{
//			PortKey portKey = new PortKey();
//			portKey.setMachineName(destinationMachineName);
//			portKey.setPortName(destinationPositionName);
//			
//			MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//			makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
//			makeTranferStateInfo.setValidateEventFlag("N");
//			PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//		}
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String alternateFlag = SMessageUtil.getBodyItemValue(doc, "ALTERNATEFLAG", false);
		
		// Added by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

		// -------------------------------------------------------------------------------------------------------------------------------
		// Modified by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//		
//		//update Current Carrier Location
//		durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(durableData, currentMachineName, 
//												currentPositionType, currentPositionName, currentZoneName, transferState, "", eventInfo);
		Durable durableData = null;
		try {
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// -------------------------------------------------------------------------------------------------------------------------------
		
		MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destinationMachineName));
		
		try {
			ExtendedObjectProxy.getTransportJobCommandService().selectByKey(true, new Object[] {transportJobName});
		} catch (Exception e) {
			eventInfo.setEventComment("TransportRequest");
			
			TransportJobCommand transportJobCommand = new TransportJobCommand();
			transportJobCommand.setTransportJobName(transportJobName);
			
			// Commented by smkang on 2018.05.07 - Judge TransportJobType according to the prefix of sTransportJobName.
			transportJobCommand.setTransportJobType(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportJobTypeByTransportJobName(transportJobName));
			
			// Added by smkang on 2018.05.10 - Need to set JobState
			transportJobCommand.setJobState(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested);
			
			transportJobCommand.setCarrierName(carrierName);
			transportJobCommand.setSourceMachineName(sourceMachineName);
			transportJobCommand.setSourcePositionType(sourcePositionType);
			transportJobCommand.setSourcePositionName(sourcePositionName);
			transportJobCommand.setSourceZoneName(sourceZoneName);
			transportJobCommand.setDestinationMachineName(destinationMachineName);
			transportJobCommand.setDestinationPositionType(destinationPositionType);
			transportJobCommand.setDestinationPositionName(destinationPositionName);
			transportJobCommand.setDestinationZoneName(destinationZoneName);
			transportJobCommand.setPriority(priority);

			Lot lotData = null;
			try {
				List<Lot> lotDataList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ? " + "AND ROWNUM = 1", new Object[] {carrierName});
				
				if(lotDataList != null)
					lotData = lotDataList.get(0);
			} catch(Exception ex1) {
			}
			
			transportJobCommand.setCarrierState(lotData != null ? GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL : GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY);
			transportJobCommand.setLotName(lotData != null ? lotData.getKey().getLotName() : "");
			transportJobCommand.setProductQuantity(lotData != null ? (long) lotData.getProductQuantity() : 0);
			transportJobCommand.setCurrentMachineName(currentMachineName);
			transportJobCommand.setCurrentPositionType(currentPositionType);
			transportJobCommand.setCurrentPositionName(currentPositionName);
			transportJobCommand.setCurrentZoneName(currentZoneName);
			
			// Added by smkang on 2018.05.10 - Need to set TransferState.
			// Modified by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
//			transportJobCommand.setTransferState(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(currentMachineName, currentPositionType));
			transportJobCommand.setTransferState(transferState);
			
			transportJobCommand.setAlternateFlag(alternateFlag);

			// Modified by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//			transportJobCommand.setCarrierType(durableData.getDurableType());
//			transportJobCommand.setCleanState(durableData.getDurableCleanState());
			// Deleted by smkang on 2018.05.21 - MCS and MES don't use CARRIERTYPE.
//			transportJobCommand.setCarrierType(durableData != null ? durableData.getDurableType() : "N/A");
			transportJobCommand.setCleanState(durableData != null ? durableData.getDurableCleanState() : "N/A");
			
			// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
			// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
			if (lotData != null) {
				transportJobCommand.setProcessOperationName(lotData.getProcessOperationName());
				transportJobCommand.setProductSpecName(lotData.getProductSpecName());
				transportJobCommand.setEcCode(lotData.getUdfs().get("ECCODE"));
				transportJobCommand.setProcessFlowName(lotData.getProcessFlowName());
				// 2019.03.28 by shkim. Mentis 3257 & 2019.04.23
				transportJobCommand.setProductRequestType(CommonUtil.getWorkOrderType(lotData));
			}
			
			ExtendedObjectProxy.getTransportJobCommandService().create(eventInfo, transportJobCommand);
			
			// --------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.05.08 - Because TransportJob is created at this time, so it is necessary to check MachineType and update TransferState of ports.
			if(StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Port sourcePortData = MESPortServiceProxy.getPortServiceUtil().getPortData(sourceMachineName, sourcePositionName);
				
				if (!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToUnload);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(sourcePortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
			
			if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destinationMachineName, destinationPositionName);
				
				if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
			// --------------------------------------------------------------------------------------------------------------------------------------------------------
			
			// Modified by smkang on 2018.06.20 - Need to change TimeKey.
//			eventInfo.setEventComment("TransportStart");
			eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		}

		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
		// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//		Map<String, String> udfs = durableData.getUdfs();
		
		// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//		if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "Y")) {
		if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "Y")) {
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "Y");
			
			// Added by smkang on 2019.01.23 - According to Liu Hongwei's request, requester of transport job should be recorded in Durable and DurableHistory.
			setEventInfo.getUdfs().put("TRANSPORTREQUESTER", eventInfo.getEventUser());
			
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			
			/*// For synchronization of a carrier information, common method will be invoked.
            try {
            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
					Element bodyElement = new Element(SMessageUtil.Body_Tag);
					bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
					bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("Y"));
					
					// EventName will be recorded triggered EventName.
					Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
					
					MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
            	}
            } catch (Exception e) {
            	eventLog.warn(e);
            }*/
		}
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		
/*		// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
		if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//			else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
		}*/
	}
}