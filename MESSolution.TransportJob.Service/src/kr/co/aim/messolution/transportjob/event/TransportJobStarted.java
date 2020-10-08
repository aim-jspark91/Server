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

public class TransportJobStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
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
		
		try {
			ExtendedObjectProxy.getTransportJobCommandService().selectByKey(true, new Object[] {transportJobName});
		} catch (Exception e) {
			eventInfo.setEventComment("TransportRequest");
			
			TransportJobCommand transportJobCommand = new TransportJobCommand();
			transportJobCommand.setTransportJobName(transportJobName);
			
			// Modified by smkang on 2018.05.06 - Judge TransportJobType according to the prefix of sTransportJobName.
//			transportJobCommand.setTransportJobType("N/A");
			transportJobCommand.setTransportJobType(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportJobTypeByTransportJobName(transportJobName));
			
			// Added by smkang on 2018.05.10 - Need to set JobState
			transportJobCommand.setJobState(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested);
			
			transportJobCommand.setCarrierName(carrierName);
			transportJobCommand.setSourceMachineName(currentMachineName);
			transportJobCommand.setSourcePositionType(currentPositionType);
			transportJobCommand.setSourcePositionName(currentPositionName);
			transportJobCommand.setSourceZoneName(currentZoneName);
			
			// Added by smkang on 2018.05.07 - Fill information as many as possible.
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
			
			// Modified by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
//			transportJobCommand.setTransferState(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(currentMachineName, currentPositionType));
			transportJobCommand.setTransferState(transferState);
			
			transportJobCommand.setAlternateFlag("N");

			// Modified by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//			transportJobCommand.setCarrierType(durableData.getDurableType());
//			transportJobCommand.setCleanState(durableData.getDurableCleanState());
			// Deleted by smkang on 2018.05.21 - MCS and MES don't use CARRIERTYPE.
//			transportJobCommand.setCarrierType(durableData != null ? durableData.getDurableType() : "N/A");
			transportJobCommand.setCleanState(durableData != null ? durableData.getDurableCleanState() : "N/A");
			
			// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
//			// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
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
			// Added by smkang on 2018.05.08 - Because TransportJob is created at this time, so it is necessary to check MachineType and update TransferState of source port.
			MachineSpec currentMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(currentMachineName));
			
			if(StringUtils.equals(currentMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Port currentPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(currentMachineName, currentPositionName);
				
				if (!currentPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToUnload);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(currentPortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
			// --------------------------------------------------------------------------------------------------------------------------------------------------------
			
			// Modified by smkang on 2018.06.20 - Need to change TimeKey.
//			eventInfo.setEventComment("TransportStart");
			eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		}

		// Modified by smkang on 2018.04.14 - Because updateTransportJobCommand method will return TransportJobCommand instance, getTransportJobInfo doesn't need to be invoked. 
//		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
//		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
		
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
			
/*			// For synchronization of a carrier information, common method will be invoked.
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