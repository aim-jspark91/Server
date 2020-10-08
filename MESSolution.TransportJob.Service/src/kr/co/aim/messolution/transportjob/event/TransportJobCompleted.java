package kr.co.aim.messolution.transportjob.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspReserveProduct;
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
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TransportJobCompleted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Added by smkang on 2019.01.04 - CarrierLocationChanged and TransportJobCompleted are reported within too short times.
		//								   So Durable data can be updated wrong.
		try {
			eventLog.debug("Thread is started to wait because of CarrierLocationChanged.");
			Thread.sleep(500);
			eventLog.debug("Thread is completed to wait because of CarrierLocationChanged.");
		} catch (Exception e) {
			// TODO: handle exception
			eventLog.debug(e);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportComplete", getEventUser(), getEventComment(), "", "");
		
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
		
		// Modified by smkang on 2018.05.07 - Because updateTransportJobCommand method will return TransportJobCommand instance, it is unnecessary to find it.
//		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		//MODIFY BY JHIYING ON20191028  mantis:5079 start 
        TransportJobCommand transportJobCommandInfo1 = ExtendedObjectProxy.getTransportJobCommandService().selectByKey(false, new Object[] {transportJobName});
		
		String changestate = transportJobCommandInfo1.getChangeState();
		String destinationMachineName = transportJobCommandInfo1.getDestinationMachineName();
		String destinationPositionName = transportJobCommandInfo1.getDestinationPositionName();
		
		if(StringUtil.equals(changestate, "Requested"))
		 {
			MachineSpec destinationMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destinationMachineName));
			if (StringUtils.isNotEmpty(destinationMachineName) && StringUtils.isNotEmpty(destinationPositionName))
		     {
				
				if (!StringUtils.equals(currentMachineName, destinationMachineName) || !StringUtils.equals(currentPositionName, destinationPositionName)) 
				{
					if(StringUtils.equals(destinationMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) 
					{
						Port destinationPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destinationMachineName, destinationPositionName);
						
						if (destinationPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) 		
						{
							MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
							makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
							makeTranferStateInfo.setValidateEventFlag("N");
							
							String eventName1 = "ChangeReject";
							String eventUser1 = transportJobCommandInfo1.getDestinationMachineName();
							String eventComment1= "TransportJobCompleted";
							EventInfo eventInfo1 = EventInfoUtil.makeEventInfo(eventName1, eventUser1, eventComment1, "", "");
							PortServiceProxy.getPortService().makeTransferState(destinationPortData.getKey(), eventInfo1, makeTranferStateInfo);
						}
					}
				}
		     }
		 }
		//MODIFY BY JHIYING ON20191028  mantis:5079 end
		
		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		// Add	aim.yunjm	2019.04.28	if CST's Currnet location <> RESERVEPRODUCTNAME Column��s Location, CurrentCount-1	/ Mantis : 0003396
		if(StringUtils.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD, transportJobCommandInfo.getTransportJobType().toString())
				&& !transportJobCommandInfo.getReserveProductName().isEmpty()) {
			this.reserveProductCount(transportJobCommandInfo, eventInfo);
		}
		// Deleted by smkang on 2018.05.07 - After update TransportJobCommand, it is unnecessary to find it.
//		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] {transportJobName});
//		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
		
		// ---------------------------------------------------------------------------------------------------------------
		// Modified by smkang on 2018.05.08 - Logic is wrong. If current location and source location are different, 
		//									  TransferState of the destination port shouldn't be changed to ReadyToLoad.
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
		String sourceMachineName = transportJobCommandInfo.getSourceMachineName();
		String sourcePositionName = transportJobCommandInfo.getSourcePositionName();
		
		String sKanban = transportJobCommandInfo.getKanban();
		String sRegion = transportJobCommandInfo.getRegion();
				
		if (StringUtils.isNotEmpty(sourceMachineName) && StringUtils.isNotEmpty(sourcePositionName)) {
			if (StringUtils.equals(currentMachineName, sourceMachineName) && StringUtils.equals(currentPositionName, sourcePositionName)) {
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
				
				// Deleted by smkang on 2018.09.12 - If current position and destination is different, destination should be changed to ReadyToLoad. 
//				String destMachineName = transportJobCommandInfo.getDestinationMachineName();
//				String destPositionName = transportJobCommandInfo.getDestinationPositionName();
//				
//				if (StringUtils.isNotEmpty(destMachineName) && StringUtils.isNotEmpty(destPositionName)) {
//					MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destMachineName));
//					
//					if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
//						Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destMachineName, destPositionName);
//						
//						if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) {
//							MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//							makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//							makeTranferStateInfo.setValidateEventFlag("N");
//							
//							PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
//						}
//					}
//				}
			}
		}
		
		// Added by smkang on 2018.09.12 - If current position and destination is different, destination should be changed to ReadyToLoad. 
		String destMachineName = transportJobCommandInfo.getDestinationMachineName();
		String destPositionName = transportJobCommandInfo.getDestinationPositionName();
		 		
		boolean normalflag = true;
		
		MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destMachineName));
		
		if (StringUtils.isNotEmpty(destMachineName) && StringUtils.isNotEmpty(destPositionName)) 
		{
			if (!StringUtils.equals(currentMachineName, destMachineName) || !StringUtils.equals(currentPositionName, destPositionName)) 
			{
				if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) 
				{
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
		}
		// ADD by aim.yunjm on	2019.01.02	
		String currentPositionType = transportJobCommandInfo.getCurrentPositionType();
		String currentZonename = transportJobCommandInfo.getCurrentZoneName();
		String destZonename = transportJobCommandInfo.getDestinationZoneName();
		
		if (!(StringUtils.equals(currentMachineName, destMachineName) && StringUtils.equals(currentZonename, destZonename))
				&& StringUtils.equals(currentPositionType, "SHELF")) {
			sRegion = GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK;
		}
		
		if(!StringUtils.equals(currentPositionType, "SHELF")){
			normalflag = false;
		}
		// ADD End
		// ---------------------------------------------------------------------------------------------------------------
		try
		{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(transportJobCommandInfo.getCarrierName());
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
			// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//			Map<String, String> durableUdfs = durableData.getUdfs();
			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> durableUdfs = setEventInfo.getUdfs();
			
			if(normalflag)
			{
	 			String stockerinTime = eventInfo.getEventTime().toString();
				String kanban = sKanban;
				String region = StringUtils.isNotEmpty(sRegion) ? sRegion : GenericServiceProxy.getConstantMap().STOCKERREGION_STOCK;
	 				
				durableUdfs.put("STOCKERINTIME", stockerinTime);
				durableUdfs.put("KANBAN", kanban);
				durableUdfs.put("REGION", region);
				
				// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
				// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//				if (!StringUtils.equals(durableUdfs.get("TRANSPORTLOCKFLAG"), "N"))
				if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "N"))
					durableUdfs.put("TRANSPORTLOCKFLAG", "N");
				
				// Deleted by smkang on 2018.12.30 - If standard columns are unnecessary to be updated, update method is unnecessary to be invoked.
//				DurableServiceProxy.getDurableService().update(durableData);

				// Modified by smkang on 2018.12.30 - Need to invoke setEvent of DurableService.
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, SetEventInfo, eventInfo);
				DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

			/*	// Added by smkang on 2018.11.03 - For synchronization of a carrier information, common method will be invoked.
	            try {
	            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
						Element bodyElement = new Element(SMessageUtil.Body_Tag);
						bodyElement.addContent(new Element("DURABLENAME").setText(transportJobCommandInfo.getCarrierName()));
						bodyElement.addContent(new Element("STOCKERINTIME").setText(stockerinTime));
						bodyElement.addContent(new Element("KANBAN").setText(kanban));
						bodyElement.addContent(new Element("REGION").setText(region));
						
						// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
						bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
						
						// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
						
						MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, transportJobCommandInfo.getCarrierName());
	            	}
	            } catch (Exception e) {
	            	eventLog.warn(e);
	            }*/
			} else {
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
				// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
				// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//				if (!StringUtils.equals(durableUdfs.get("TRANSPORTLOCKFLAG"), "N")) {
				
				//2019.03.09 dmlee : Already Change TransportLocFlag N (because Change Loc (CurrentMachine = DestMachine) Need SetEvent
				//if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "N"))
				if(true)
				{
					durableUdfs.put("TRANSPORTLOCKFLAG", "N");
					//	Add 2019.03.13	aim.yunjm	Kanban update
					durableUdfs.put("KANBAN", sKanban);
					durableUdfs.put("REGION", "");
					
					DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
					
					// For synchronization of a carrier information, common method will be invoked.
		            try {
		            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
		            		/*Element bodyElement = new Element(SMessageUtil.Body_Tag);
							bodyElement.addContent(new Element("DURABLENAME").setText(transportJobCommandInfo.getCarrierName()));
							bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
							
							// EventName will be recorded triggered EventName.
							Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
							
							MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, transportJobCommandInfo.getCarrierName());*/
		            	}
		            } catch (Exception e) {
		            	eventLog.warn(e);
		            }
				}
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			}
		}
		catch(Exception ex) {
			eventLog.warn("Durable Update Failed!");
		}
		
		// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.		
/*		if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, sourceMachineName);
//			else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, destMachineName);
			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
		}*/
	}

	// Add	aim.yunjm	2019.04.28	if CST's Currnet location <> RESERVEPRODUCTNAME Column��s Location, CurrentCount-1	/ Mantis : 0003396	
	private void reserveProductCount(TransportJobCommand transportJobCommandInfo, EventInfo eventinfo){
		try{
			String reserveProduct = transportJobCommandInfo.getReserveProductName();

			String[] list = reserveProduct.split("-");
			
			if(list.length != 4 || StringUtils.isEmpty(list[0])){
				eventLog.error("��Not Exist ReserveProduct");
				return ;
			}
		
			String Machine = list[0];
			String reserveUnit = list[1];
			String reservePort = list[2];
			String reserveProductName = list[3];
			
			if(!(transportJobCommandInfo.getCurrentMachineName().equals(Machine)
					&& (reservePort.equals("*") || transportJobCommandInfo.getCurrentPositionName().equals(reservePort)))){
				Object[] keySet = new Object[]{Machine, reserveUnit, reservePort, reserveProductName};
				
				DspReserveProduct reserveProductData = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, keySet);
				if(reserveProductData.getSetCount() > 0){
					reserveProductData.setCurrentCount(reserveProductData.getCurrentCount() - 1);
					if(reserveProductData.getReserveState().equals(GenericServiceProxy.getConstantMap().DSPSTATUS_COMPLETED)){
						reserveProductData.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED);
					}
					ExtendedObjectProxy.getDspReserveProductService().modify(eventinfo, reserveProductData);
					eventLog.info("�� ReserveProduct : CurrentCount-1 Success");
				}
			}else{
				eventLog.info("�� ReserveProduct : Currnet location = ReserveProduct location");
			}
			
		}catch(Exception e){
			eventLog.error("��ReserveProduct : Update Failed");
		}
	}
}