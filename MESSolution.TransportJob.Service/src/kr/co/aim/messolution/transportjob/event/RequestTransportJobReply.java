package kr.co.aim.messolution.transportjob.event;

import java.util.List;

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
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
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

public class RequestTransportJobReply extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
			String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
			String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
			String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
			String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
			
			// Deleted by smkang on 2018.05.03 - If CARRIERNAME is empty, CustomException would be already occurred when SMessageUtil.getBodyItemValue was invoked.
//			//Check Exist Carrier
//			if(StringUtils.isEmpty(carrierName))
//			{
//				carrierName = tData.getCarrierName();
//			}
			
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			//Event Info
			String eventName = StringUtils.equals(returnCode, "0") ? "TransportAccept" : "TransportReject";
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), "", "");
			
			// Update CT_TRANSPORTJOBCOMMAND		
			TransportJobCommand tData = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);

			// 2019.04.28_yunjm_Move to Logic. TransportJobCompleted -> RequestTransportJobReply. Mantis 0003396.
			// 2019.04.09_hsryu_Move to Logic. RequestTransportJobRequest -> TransportJobCompleted. Mantis 0003396.
			if(StringUtils.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD, tData.getTransportJobType().toString())
					&& tData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Accepted)
					&& !tData.getReserveProductName().isEmpty()) {
				this.reserveProductCount(tData, eventInfo);
			}
			
			MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
			MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destinationMachineName));
			
			String jobState = MESTransportServiceProxy.getTransportJobServiceUtil().getJobState(SMessageUtil.getMessageName(doc), doc);
			
			// ------------------------------------------------------------------------------------------------
			// Modified by smkang on 2018.05.28 - Need to set TRANSPORTLOCKFLAG anyway.
			// Deleted by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'. 
//			Map<String, String> udfs = durableData.getUdfs();
//			
//			if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "N")) {
//				udfs.put("TRANSPORTLOCKFLAG", "N");
//				
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
//				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
//				
//				// Added by smkang on 2018.11.03 - For synchronization of a carrier information, common method will be invoked.
//	            try {
//	            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
//						Element bodyElement = new Element(SMessageUtil.Body_Tag);
//						bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
//						bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
//						
//						// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
////						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
//						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
//						
//						MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
//	            	}
//	            } catch (Exception e) {
//	            	eventLog.warn(e);
//	            }
//			}
			// ------------------------------------------------------------------------------------------------
						
			if(StringUtils.equals(jobState, GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected))
			{
				// ------------------------------------------------------------------------------------------------
				//1. Update Carrier TransportLockFlag
				// Modified by smkang on 2018.05.28 - Need to set TRANSPORTLOCKFLAG anyway.
//				Map<String, String> udfs = durableData.getUdfs();
//				udfs.put("TRANSPORTLOCKFLAG", "N");
//				
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
//				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
				// ------------------------------------------------------------------------------------------------
				
				try
				{
					// ------------------------------------------------------------------------------------------------
					// Modified by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
					// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//					Map<String, String> udfs = durableData.getUdfs();

					// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//					if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "N")) {
					if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "N")) {
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");
						
						DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
						
/*						// For synchronization of a carrier information, common method will be invoked.
			            try {
			            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
								Element bodyElement = new Element(SMessageUtil.Body_Tag);
								bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
								bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("N"));
								
								// EventName will be recorded triggered EventName.
								Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
								
								MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
			            	}
			            } catch (Exception e) {
			            	eventLog.warn(e);
			            }*/
					}
					// ------------------------------------------------------------------------------------------------
					
					// --------------------------------------------------------------------------------------------------------------------
					//2. Update Port TransferState
					//Update Source TransferState
					// Modified by smkang on 2018.05.08 - Need to check MachineType is ProductionMachine or not.
//					if(StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//					{
//						MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sourceMachineName);
//						Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sourceMachineName, sourcePositionName);
//						
//						MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//						makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
//						makeTranferStateInfo.setValidateEventFlag("N");
//						
//						PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTranferStateInfo);
//					}
//					
//					//Update Dest TransferState
//					if(StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//					{
//						MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);
//						Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);
//						
//						MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//						makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//						makeTranferStateInfo.setValidateEventFlag("N");
//						
//						PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTranferStateInfo);
//					}
					if(StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
						Port sourcePortData = MESPortServiceProxy.getPortServiceUtil().getPortData(sourceMachineName, sourcePositionName);
						
						if (!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToUnload)) {
							MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
							makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
							makeTranferStateInfo.setValidateEventFlag("N");
							
							PortServiceProxy.getPortService().makeTransferState(sourcePortData.getKey(), eventInfo, makeTranferStateInfo);
						}
					}
					
					if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
						Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destinationMachineName, destinationPositionName);
						
						if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad)) {
							MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
							makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
							makeTranferStateInfo.setValidateEventFlag("N");
							
							PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
						}
					}
					// --------------------------------------------------------------------------------------------------------------------
				}
				catch (Exception ex)
				{
					eventLog.error("Port transfer state change failed");
					eventLog.error(ex.getMessage());
				}
			} else {
				// Added by smkang on 2018.12.01 - According to Feng Huanyan's request, if a user executes transport manually, reserved lot information is removed.
				if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
					try {
						List<Lot> lotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ?", new Object[] {carrierName});
						
						if (lotDataList != null && lotDataList.size() > 0) {
							for (Lot lotData : lotDataList) {
								ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, lotData.getKey().getLotName(), carrierName, destinationMachineName);
							}
						}
					} catch (Exception e) {
						ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, "", carrierName, destinationMachineName);
					}
				}
			}
			
/*			// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
			if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
				// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//				if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, sourceMachineName);
//				else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, destinationMachineName);
				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
			}*/
		} catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
	
	// 2019.04.28_yunjm_Move to Logic. TransportJobCompleted -> RequestTransportJobReply. Mantis 0003396.
	// 2019.04.09_hsryu_Move to Logic. RequestTransportJobRequest -> TransportJobCompleted. Mantis 0003396.
	private void reserveProductCount(TransportJobCommand transportJobCommandInfo, EventInfo eventinfo){
		try {
			String reserveProduct = transportJobCommandInfo.getReserveProductName();

			String[] list = reserveProduct.split("-");
			
			if(list.length != 4 || StringUtils.isEmpty(list[0])){
				eventLog.error("◈Not Exist ReserveProduct");
				return ;
			}
		
			String Machine = list[0];
			String reserveUnit = list[1];
			String reservePort = list[2];
			String reserveProductName = list[3];
		
			String condition = " WHERE 1=1 "
					+ "    AND MACHINENAME = :MACHINENAME "
					+ "    AND UNITNAME = :UNITNAME "
					+ "    AND PORTNAME = :PORTNAME "
					+ "    AND RESERVESTATE = :RESERVESTATE "
					+ "    AND USEFLAG = :USEFLAG ";
			Object[] bindSet = new Object[]{Machine, reserveUnit, reservePort, "Executing", "Y"};
			try{
				List<DspReserveProduct> dspReserveProductList = ExtendedObjectProxy.getDspReserveProductService().select(condition, bindSet);
				
				DspReserveProduct oldReserveProduct = dspReserveProductList.get(0);
				
				if(StringUtils.equals(reserveProductName, oldReserveProduct.getReserveName())){
					eventLog.info("▶old Data is same New Data");
				}else{
					oldReserveProduct.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_RESERVED);
					
//					ExtendedObjectProxy.getDspReserveProductService().update(oldReserveProduct, eventinfo);
					ExtendedObjectProxy.getDspReserveProductService().modify(eventinfo, oldReserveProduct);
					eventLog.info("▶old_ReserveProduct Update Success");
				}
			}catch(Exception e){
				eventLog.error("◈old_ReserveProduct Update Failed");
			}
			
			eventinfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			Object[] keySet = new Object[]{Machine, reserveUnit, reservePort, reserveProductName};
			
			DspReserveProduct newReserveProduct = ExtendedObjectProxy.getDspReserveProductService().selectByKey(false, keySet);
			
			// SetCount가 0인경우 Count하지 않음
			if(newReserveProduct.getSetCount() > 0){
				newReserveProduct.setCurrentCount(newReserveProduct.getCurrentCount() + 1);
			}
			
			if(newReserveProduct.getSetCount() != 0 && newReserveProduct.getSetCount() <= newReserveProduct.getCurrentCount()){
				newReserveProduct.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_COMPLETED);
			}else{
				newReserveProduct.setReserveState(GenericServiceProxy.getConstantMap().DSPSTATUS_EXECUTING);
			}
			
//			ExtendedObjectProxy.getDspReserveProductService().update(newReserveProduct, eventinfo);
			ExtendedObjectProxy.getDspReserveProductService().modify(eventinfo, newReserveProduct);
			eventLog.info("▶new_ReserveProduct Update Success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			eventLog.error("◈ReserveProduct Update Failed");
		}
	}
}