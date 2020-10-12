package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
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
import org.jdom.Element;

public class RequestTransportJobRequest extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportRequest", getEventUser(), getEventComment(), "", "");
			
			// Modified by smkang on 2018.04.12 - Although TRANSPORTJOBNAME is null, OPI adds empty element to this message for matching of message format.
			//									  So required parameter of getBodyItemValue should be false.
//			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", false);
			
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			
			// Modified by smkang on 2018.05.03 - SOURCEMACHINENAME, SOURCEPOSITIONTYPE, SOURCEPOSITIONNAME should be essential.
//			String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
//			String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
//			String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
			String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", true);
			String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", true);
			// Modified by smkang on 2018.05.28 - SOURCEPOSITIONNAME is not essential.
//			String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", true);
			String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
			
			String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
			
			// Modified by smkang on 2018.05.03 - DESTINATIONMACHINENAME, DESTINATIONPOSITIONTYPE, DESTINATIONPOSITIONNAME should be essential.
//			String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
//			String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
//			String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
			String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", true);
			String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", true);
			// Modified by smkang on 2018.05.28 - DESTINATIONPOSITIONNAME is not essential.
//			String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", true);
			String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
			
			String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
			String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
			String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
			String cleanState = SMessageUtil.getBodyItemValue(doc, "CLEANSTATE", false);
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			
			// Deleted by smkang on 2018.05.21 - MCS and MES don't use CARRIERTYPE.
//			String carrierType = SMessageUtil.getBodyItemValue(doc, "CARRIERTYPE", false);
			
			String kanban = SMessageUtil.getBodyItemValue(doc, "KANBAN", false);
			String region = SMessageUtil.getBodyItemValue(doc, "REGION", false);
			
			// Added by aim.yunjm on 2019.01.17
			String reserveProductName = SMessageUtil.getBodyItemValue(doc, "RESERVEPRODUCTNAME", false);
			
			// 2019.04.09_hsryu_Move To TransportJobCompleted Logic. Mantis 0003396.
			// Added by aim.yunjm on 2019.01.30	Mantis : 0002626
			//reserveProductCount(reserveProductName, eventInfo);
			
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			// Deleted by smkang on 2018.05.24 - If destination is not a process machine, although this carrier is hold, transport should be enabled.
			//									 So this validation should be executed after find a MachineSpec of destination. 
//			CommonValidation.CheckDurableHoldState(carrierName);
			
			if(StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "Y"))
				throw new CustomException("TRANSPORT-0002", carrierName);
			
			// Modified by smkang on 2019.01.28 - According to Cui Yu's request, DryFlag doesn't be checked for CassetteCleaner or Stocker.
//			if(StringUtils.equals(durableData.getUdfs().get("DRYFLAG"), "N"))
//				throw new CustomException("TRANSPORT-0007", carrierName);
			
			String transportJobType = "";
			if(StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
				transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
			else{
				transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
				// Add	by 2019.01.04	aim.yunjm	if EventUser is DSP, change to RuleName
				eventInfo.setEventUser(SMessageUtil.getBodyItemValue(doc, "EVENTUSER", false));
			}

			Lot lotData = null;
			try {
				List<Lot> lotDataList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ? AND ROWNUM = 1", new Object[] {carrierName});
				
				if(lotDataList != null) {
					lotData = lotDataList.get(0);
				}
			} catch(Exception ex1) {
			}
			
			// Added by smkang on 2019.03.01 - According to Liu Hongwei's request, validation is added.
			//								   1. If LotState is 'Received', reject a transport.
			//								   2. If LotState is null and DurableState is 'InUse' and FactoryName is 'OLED', reject a transport.
			// Modified by smkang on 2019.04.16 - When a Lot is assigned in OLED shop, Lot data can't be checked in ARRAY shop.
			//									  So although a Lot data is not assigned in ARRAY shop, it should be checked with DurableState.
//			if (lotData != null && !System.getProperty("svr").equals(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false))) {
//				if (StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Received)) {
//					throw new CustomException("TRANSPORT-0011", lotData.getKey().getLotName());
//				} else if (StringUtils.isEmpty(lotData.getLotState()) && !StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop"))) {
//					throw new CustomException("TRANSPORT-0012", lotData.getKey().getLotName(), carrierName);
//				}
//			}
			// Modified by smkang on 2019.04.22 - According to Cui Yu's request, this validation is applied to manual transport only.
			if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC)) {
				if (!System.getProperty("svr").equals(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false))) {
					if (lotData != null) {
						if (StringUtils.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Received))
							throw new CustomException("TRANSPORT-0011", lotData.getKey().getLotName());
						else if (StringUtils.isEmpty(lotData.getLotState()) && !StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
							throw new CustomException("TRANSPORT-0012", lotData.getKey().getLotName(), carrierName);					
					} else {
						if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse) && !StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
							throw new CustomException("TRANSPORT-0012", "NULL", carrierName);
					}
				}
			}
			
			// --------------------------------------------------------------------------------------------------------------------------------------------
			// Check Destination Port Transfer State
			MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
			
			if(StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Machine sourceMachineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(sourceMachineName);
				Port sourcePortData = MESPortServiceProxy.getPortServiceUtil().getPortData(sourceMachineName, sourcePositionName);
				
				// Deleted by smkang on 2018.11.15 - Because ARRAY machine has a problem, Hongwei wants to remove this validation.
//				if(!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToUnload))
//					throw new CustomException("TRANSPORT-0005", sourceMachineName + "/" + sourcePositionName, sourcePortData.getTransferState());
//				else if(sourcePortData.getPortStateName().equals(GenericServiceProxy.getConstantMap().Port_DOWN))
//					throw new CustomException("TRANSPORT-0008", sourceMachineName + "/" + sourcePositionName, sourcePortData.getPortStateName());
				// Added by smkang on 2019.01.29 - According to Liu Hongwei and Feng Huanyan's request, validation of port is necessary at Offline state.
				if (!sourceMachineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OffLine)) {
					// Added by smkang on 2019.01.25 - According to Liu Hongwei's request, PEX validation is only needed to be checked for OPI transport.
					if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC)) {
						CommonValidation.checkMachineState(sourceMachineData);
					}
					
					if(!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToUnload))
						throw new CustomException("TRANSPORT-0005", sourceMachineName + "/" + sourcePositionName, sourcePortData.getTransferState());
					else if(sourcePortData.getPortStateName().equals(GenericServiceProxy.getConstantMap().Port_DOWN))
						throw new CustomException("TRANSPORT-0008", sourceMachineName + "/" + sourcePositionName, sourcePortData.getPortStateName());
				}
				
				if (!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToUnload);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(sourcePortData.getKey(), eventInfo, makeTranferStateInfo);
				}
			}
			
			MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destinationMachineName));
			
			if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
				Machine destMachineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(destinationMachineName);
				Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destinationMachineName, destinationPositionName);
				
				// ------------------------------------------------------------------------------------------------------------------------------------------
				// Modified by smkang on 2018.11.21 - According to Hongwei's request, validation will be executed when a destination machine is online only.
				// Modified by smkang on 2019.01.09 - According to Feng Huanyan and Liu Hongwei's request, TEX should validate like PEX.
				if (!destMachineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OffLine)) {
					// Added by smkang on 2019.01.25 - According to Liu Hongwei's request, PEX validation is only needed to be checked for OPI transport.
					if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC))
						MESTransportServiceProxy.getTransportJobServiceUtil().validateCassetteInfoDownloadRequest(durableData, lotData, destMachineSpecData, destMachineData, destPortData, eventInfo);
					
					// Added by smkang on 2019.01.29 - According to Liu Hongwei and Feng Huanyan's request, validation of port is necessary at Offline state.
					if(!StringUtils.equals(destPortData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
						throw new CustomException("TRANSPORT-0005", destinationMachineName + "/" + destinationPositionName, destPortData.getTransferState());
					else if(StringUtils.equals(destPortData.getPortStateName(), GenericServiceProxy.getConstantMap().Port_DOWN))
						throw new CustomException("TRANSPORT-0008", destinationMachineName + "/" + destinationPositionName, destPortData.getPortStateName());
				}
				// ------------------------------------------------------------------------------------------------------------------------------------------
				
				if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
					MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
					makeTranferStateInfo.setValidateEventFlag("N");
					
					PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
				}
				
				// 2018.11.20_hsryu_move Logic and make new Logic.. 
				// Added by smkang on 2018.08.06 - If this machine is necessary to run MQC, transport should be failed.
				// Modified by smkang on 2018.08.11 - According to Hongwei's request, transport rule is changed.
				//									  1. MachineIdleTime condition is over - MQC(O), Production(X)
				//									     but MachineIdleTime condition is over and (MQCRunCount+MQCPreRunCount >= MQCPlanCount) - MQC(O), Production(O)
				//									  2. MachineState is MQC - MQC(O), Production(X)
				//									     but MachineState is MQC and (MQCRunCount+MQCPreRunCount >= MQCPlanCount) - MQC(O), Production(O)
				//									  When MQCCondition is necessary to be checked, DestUnitName is also necessary to be considered.
//				Machine destMachineData = MESMachineServiceProxy.getMachineServiceUtil().getMachineData(destinationMachineName);
//				if (MESMachineServiceProxy.getMachineServiceUtil().needToRunMQC(destinationMachineName))
//					throw new CustomException("TRANSPORT-0006", destinationMachineName);
//				if (lotData != null && !lotData.getProductionType().equals("MQCA")) {
//					if (MESMachineServiceProxy.getMachineServiceUtil().isOverMachineIdleTime(lotName, destinationMachineName) || destMachineData.getMachineStateName().equals("MQC")) {
//						List<MQCCondition> mqcConditionList = null;
//						try {
//							mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select("MACHINENAME = ? AND MQCPRODUCTSPECNAME = ?", new Object[] {destinationMachineName, lotData.getProductSpecName()});
//						} catch (Exception e) {
//							eventLog.info(e);
//						}
	//
//						if ((mqcConditionList == null || mqcConditionList.size() == 0) ||
//							(mqcConditionList.get(0).getMqcRunCount() + mqcConditionList.get(0).getMqcPreRunCount() < mqcConditionList.get(0).getMqcPlanCount())) {
//							throw new CustomException("TRANSPORT-0006", destinationMachineName);
//						}
//					}
//				}
				// Deleted by smkang on 2018.11.15 - Because ARRAY machine has a problem, Hongwei wants to remove this validation.
//				if(!StringUtils.equals(destPortData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
//					throw new CustomException("TRANSPORT-0005", destinationMachineName + "/" + destinationPositionName, destPortData.getTransferState());
//				else if(StringUtils.equals(destPortData.getPortStateName(), GenericServiceProxy.getConstantMap().Port_DOWN))
//					throw new CustomException("TRANSPORT-0008", destinationMachineName + "/" + destinationPositionName, destPortData.getPortStateName());
			}
			// --------------------------------------------------------------------------------------------------------------------------------------------
			
			// Added by smkang on 2019.03.18 - According to Liu Hongwei's request, cross shop transport can be accepted STK to STK only.
			if (!StringUtils.equals(sourceMachineSpecData.getFactoryName(), destMachineSpecData.getFactoryName()) &&
			   (!StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine) ||
			    !StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine))) {
				
				throw new CustomException("TRANSPORT-0013");
			}
			
			// Set Transport Job Command Info
			TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
			transportJobCommandInfo.setTransportJobName(StringUtils.isNotEmpty(transportJobName) ? transportJobName : MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(carrierName, transportJobType));
			transportJobCommandInfo.setTransportJobType(transportJobType);
			transportJobCommandInfo.setJobState(MESTransportServiceProxy.getTransportJobServiceUtil().getJobState(messageName, doc));
			transportJobCommandInfo.setCancelState(MESTransportServiceProxy.getTransportJobServiceUtil().getCancelState(messageName, doc));
			transportJobCommandInfo.setChangeState(MESTransportServiceProxy.getTransportJobServiceUtil().getChangeState(messageName, doc));
			transportJobCommandInfo.setCarrierName(carrierName);
			transportJobCommandInfo.setSourceMachineName(sourceMachineName);
			transportJobCommandInfo.setSourcePositionType(sourcePositionType);
			transportJobCommandInfo.setSourcePositionName(sourcePositionName);
			transportJobCommandInfo.setSourceZoneName(sourceZoneName);
			transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
			transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
			transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
			transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
			transportJobCommandInfo.setPriority(priority);
			transportJobCommandInfo.setCarrierState(lotData != null ? GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL : GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY);
			transportJobCommandInfo.setLotName(lotData != null ? lotData.getKey().getLotName() : "");
			transportJobCommandInfo.setProductQuantity(lotData != null ? (long) lotData.getProductQuantity() : 0);
			transportJobCommandInfo.setCurrentMachineName(sourceMachineName);
			transportJobCommandInfo.setCurrentPositionType(sourcePositionType);
			transportJobCommandInfo.setCurrentPositionName(sourcePositionName);			
			transportJobCommandInfo.setCurrentZoneName(sourceZoneName);
			
			// Modified by smkang on 2019.04.25 - When current position is STK port, DSP can't set POSITIONNAME.
			// Modified by smkang on 2019.04.29 - When current position is VEHICLE, DSP can't set POSITIONNAME too.
//			if (StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT) && StringUtils.isEmpty(sourcePositionName)) {
//				transportJobCommandInfo.setSourcePositionName(durableData.getUdfs().get("POSITIONNAME"));
//				transportJobCommandInfo.setCurrentPositionName(durableData.getUdfs().get("POSITIONNAME"));
//			}
			if (StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT) || 
				StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_VEHICLE)) {
				
				if (StringUtils.isEmpty(sourcePositionName)) {
					transportJobCommandInfo.setSourcePositionName(durableData.getUdfs().get("POSITIONNAME"));
					transportJobCommandInfo.setCurrentPositionName(durableData.getUdfs().get("POSITIONNAME"));
				
					SMessageUtil.setBodyItemValue(doc, "SOURCEPOSITIONNAME", StringUtils.isNotEmpty(durableData.getUdfs().get("POSITIONNAME")) ? durableData.getUdfs().get("POSITIONNAME") : "");
				}
			}
			
			transportJobCommandInfo.setTransferState(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(sourceMachineName, sourcePositionType));
			transportJobCommandInfo.setAlternateFlag("N");
			transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
			transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));
			transportJobCommandInfo.setCleanState(durableData.getDurableCleanState());
			
			// Added by smkang on 2018.08.31 - According to Feng Huanyan's requirement, reason code should be added.
			transportJobCommandInfo.setReasonCode(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false));

			transportJobCommandInfo.setRegion(region);
			transportJobCommandInfo.setKanban(kanban);
			
			// Added by aim.yunjm on 2019.01.17
			transportJobCommandInfo.setReserveProductName(reserveProductName);
			
			// Added by smkang on 2019.01.07 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
			// Added by smkang on 2019.02.27 - According to Cui Yu's request, ProductSpecName, ECCode and ProcessFlowName are added.
			if (lotData != null) {
				transportJobCommandInfo.setProcessOperationName(lotData.getProcessOperationName());
				transportJobCommandInfo.setProductSpecName(lotData.getProductSpecName());
				transportJobCommandInfo.setEcCode(lotData.getUdfs().get("ECCODE"));
				transportJobCommandInfo.setProcessFlowName(lotData.getProcessFlowName());
				// 2019.04.23 by shkim. Mentis 3257
				transportJobCommandInfo.setProductRequestType(CommonUtil.getWorkOrderType(lotData));
			}
			
			transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
						
			doc.getRootElement().getChild(SMessageUtil.Body_Tag).removeChild("REASONCODE");
			
			try
			{
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).removeChild("REGION");
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).removeChild("KANBAN");
			}
			catch(Exception ex)
			{
				eventLog.warn("Durable Update Failed!");
			}
			
			try
			{
				ExtendedObjectProxy.getTransportJobCommandService().create(eventInfo, transportJobCommandInfo);
				
				// Modified by smkang on 2018.04.12 - Although TRANSPORTJOBNAME is null, OPI adds empty element to this message for matching of message format.
				//									  So this element doesn't be needed to add.
//				doc = SMessageUtil.addItemToBody(doc, "TRANSPORTJOBNAME", transportJobName);
				SMessageUtil.setBodyItemValue(doc, "TRANSPORTJOBNAME", transportJobCommandInfo.getTransportJobName());
			}
			catch(Exception e)
			{
				throw new CustomException("JOB-8011", e.getMessage());
			}
			
			//Update Carrier TransportLockFlag
			// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//			Map<String, String> udfs = durableData.getUdfs();
			
			// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//			if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "Y")) {
			if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "Y")) {
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "Y");
				
				// Added by smkang on 2019.01.23 - According to Liu Hongwei's request, requester of transport job should be recorded in Durable and DurableHistory.
				setEventInfo.getUdfs().put("TRANSPORTREQUESTER", eventInfo.getEventUser());
				
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
				
				// Added by smkang on 2018.11.03 - For synchronization of a carrier information, common method will be invoked.
/*	            try {
	            	if (sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
						Element bodyElement = new Element(SMessageUtil.Body_Tag);
						bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
						bodyElement.addContent(new Element("TRANSPORTLOCKFLAG").setText("Y"));
						
						// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//						Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
						//Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
						
						//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
	            	}
	            } catch (Exception e) {
	            	eventLog.warn(e);
	            }*/
			}
			
			// Added by smkang on 2019.01.10 - According to Liu Hongwei's request, requester of transport job should be recorded in Lot and LotHistory.
			// Deleted by smkang on 2019.01.23 - According to Liu Hongwei's request, requester of transport job should be recorded in Durable and DurableHistory.
//			if (lotData != null) {
//				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
//				setEventInfo.getUdfs().put("TRANSPORTREQUESTER", eventInfo.getEventUser());
//				
//				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
//			}
			
			// Added by smkang on 2018.04.12 - CarrierState and CleanState should be changed to upper case for MCS.
			carrierState = transportJobCommandInfo.getCarrierState();
			cleanState = transportJobCommandInfo.getCleanState();
			SMessageUtil.setBodyItemValue(doc, "CARRIERSTATE", StringUtils.upperCase(carrierState));
			SMessageUtil.setBodyItemValue(doc, "CLEANSTATE", StringUtils.isNotEmpty(cleanState) ? StringUtils.upperCase(cleanState) : "");
			
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
			// Modified by smkang on 2019.03.13 - Need to check EventUser.
//			if (StringUtils.isNotEmpty(getOriginalSourceSubjectName()))
			if (!System.getProperty("svr").equals(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false)) && StringUtils.isNotEmpty(getOriginalSourceSubjectName()))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
/*			// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
			if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
				// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//				if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, sourceMachineName);
//				else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//					MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, destinationMachineName);
				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
			}
			*/
			// Added by smkang on 2019.03.20 - Reply message is received too fast, so TEX will commit transaction before request message is sent.
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			
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
			
			// Modified by smkang on 2019.03.13 - Need to check EventUser.
//			if (StringUtils.isNotEmpty(getOriginalSourceSubjectName()))
			if (!System.getProperty("svr").equals(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false)) && StringUtils.isNotEmpty(getOriginalSourceSubjectName()))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");

			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}