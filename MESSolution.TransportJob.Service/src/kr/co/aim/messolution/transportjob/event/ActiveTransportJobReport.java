package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
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
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ActiveTransportJobReport  extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		// Modified by smkang on 2018.05.19 - Design is changed for ActiveTransportJobReport action.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		
//		//String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
//		//String requestSubjectName = SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false);
//		
//		List<Element> transportJobList = SMessageUtil.getBodySequenceItemList(doc, "TRANSPORTJOBLIST", true);
//		
//		for(Element transportJobE : transportJobList)
//		{
//			String transportJobName = transportJobE.getChildText("TRANSPORTJOBNAME");
//			List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select(
//					"TRANSPORTJOBNAME = ?", new Object[] {transportJobName});
//			if(sqlResult.size() == 0)
//			{
//				String carrierName = transportJobE.getChildText("CARRIERNAME");
//				String sourceMachineName = transportJobE.getChildText("SOURCEMACHINENAME");
//				String sourcePositionType = transportJobE.getChildText("SOURCEPOSITIONTYPE");
//				String sourcePositionName = transportJobE.getChildText("SOURCEPOSITIONNAME");
//				String sourceZoneName = transportJobE.getChildText("SOURCEZONENAME");
//				String currentMachineName = transportJobE.getChildText("CURRENTMACHINENAME");
//				String currentPositionType = transportJobE.getChildText("CURRENTPOSITIONTYPE");
//				String currentPositionName = transportJobE.getChildText("CURRENTPOSITIONNAME");
//				String currentZoneName = transportJobE.getChildText("CURRENTZONENAME");
//				String destinationMachineName = transportJobE.getChildText("DESTINATIONMACHINENAME");
//				String destinationPositionType = transportJobE.getChildText("DESTINATIONPOSITIONTYPE");
//				String destinationPositionName = transportJobE.getChildText("DESTINATIONPOSITIONNAME");
//				String destinationZoneName = transportJobE.getChildText("DESTINATIONZONENAME");
//				String priority = transportJobE.getChildText("PRIORITY");
//				String carrierState = transportJobE.getChildText("CARRIERSTATE");
//				String lotName = transportJobE.getChildText("LOTNAME");
//				String productQuantity = transportJobE.getChildText("PRODUCTQUANTITY");
//				String transferState = transportJobE.getChildText("TRANSFERSTATE");
//				String alternateFlag = transportJobE.getChildText("ALTERNATEFLAG");
//				
//				TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
//				transportJobCommandInfo.setTransportJobName(transportJobName);
//				transportJobCommandInfo.setCarrierName(carrierName);
//				transportJobCommandInfo.setSourceMachineName(sourceMachineName);
//				transportJobCommandInfo.setSourcePositionType(sourcePositionType);
//				transportJobCommandInfo.setSourcePositionName(sourcePositionName);
//				transportJobCommandInfo.setSourceZoneName(sourceZoneName);
//				transportJobCommandInfo.setCurrentMachineName(currentMachineName);
//				transportJobCommandInfo.setCurrentPositionType(currentPositionType);
//				transportJobCommandInfo.setCurrentPositionName(currentPositionName);
//				transportJobCommandInfo.setCurrentZoneName(currentZoneName);
//				transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
//				transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
//				transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
//				transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
//				transportJobCommandInfo.setPriority(priority);
//				transportJobCommandInfo.setCarrierState(carrierState);
//				transportJobCommandInfo.setLotName(lotName);
//				//transportJobCommandInfo.setRequestSubjectName(requestSubjectName);
//				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
//				
//				try
//				{
//					transportJobCommandInfo.setProductQuantity(Long.valueOf(productQuantity));
//				}
//				catch(Exception e)
//				{
//					transportJobCommandInfo.setProductQuantity(0);
//				}
//				transportJobCommandInfo.setTransferState(transferState);
//				transportJobCommandInfo.setAlternateFlag(alternateFlag);
//				
//				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
//			}
//		}
//		Case 1. MCS reports a transport job data and the job is existed in MES database, but information of the job is different.
//				- MES updates job with MCS data.
//		Case 2. MCS reports a transport job data, but it isn't existed in MES database.
//				- MES creates the job.
//		Case 3. MCS doesn't report a transport job but the job is existed in the job of MES database.
//				- MES terminates the job.
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
		
		List<Element> transportJobElementList = SMessageUtil.getBodySequenceItem(doc, "TRANSPORTJOBLIST", true).getChildren();
		
		if (transportJobElementList != null && transportJobElementList.size() > 0) {
			List<String> mcsTransportJobNames = new ArrayList<String>();
			for (Element transportJobElement : transportJobElementList) {
				String transportJobName = transportJobElement.getChildText("TRANSPORTJOBNAME");
				String carrierName = transportJobElement.getChildText("CARRIERNAME");
				String currentMachineName = transportJobElement.getChildText("CURRENTMACHINENAME");
				String currentPositionType = transportJobElement.getChildText("CURRENTPOSITIONTYPE");
				String currentPositionName = transportJobElement.getChildText("CURRENTPOSITIONNAME");
				String currentZoneName = transportJobElement.getChildText("CURRENTZONENAME");
				String destMachineName = transportJobElement.getChildText("DESTINATIONMACHINENAME");
				String destPositionType = transportJobElement.getChildText("DESTINATIONPOSITIONTYPE");
				String destPositionName = transportJobElement.getChildText("DESTINATIONPOSITIONNAME");
				String destZoneName = transportJobElement.getChildText("DESTINATIONZONENAME");
				String priority = transportJobElement.getChildText("PRIORITY");
				String alternateFlag = transportJobElement.getChildText("ALTERNATEFLAG");
				
				// Added by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
				String transferState = transportJobElement.getChildText("TRANSFERSTATE");
				
				try {
					mcsTransportJobNames.add(transportJobName);
					
					TransportJobCommand transportJobData = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
					
					try {
//						Case 1. MCS reports a transport job data and the job is existed in MES database, but information of the job is different.
						if(!StringUtils.equals(transportJobData.getCurrentMachineName(), currentMachineName) || 
							!StringUtils.equals(transportJobData.getCurrentPositionType(), currentPositionType) || 
							!StringUtils.equals(transportJobData.getCurrentPositionName(), currentPositionName) || 
							!StringUtils.equals(transportJobData.getCurrentZoneName(), currentZoneName) || 
							!StringUtils.equals(transportJobData.getDestinationMachineName(), destMachineName) || 
							!StringUtils.equals(transportJobData.getDestinationPositionType(), destPositionType) || 
							!StringUtils.equals(transportJobData.getDestinationPositionName(), destPositionName) || 
							!StringUtils.equals(transportJobData.getDestinationZoneName(), destZoneName) || 
							!StringUtils.equals(transportJobData.getPriority(), priority) || 
							!StringUtils.equals(transportJobData.getAlternateFlag(), alternateFlag)) {

							transportJobData.setCurrentMachineName(currentMachineName);
							transportJobData.setCurrentPositionType(currentPositionType);
							transportJobData.setCurrentPositionName(currentPositionName);
							transportJobData.setCurrentZoneName(currentZoneName);
							transportJobData.setDestinationMachineName(destMachineName);
							transportJobData.setDestinationPositionType(destPositionType);
							transportJobData.setDestinationPositionName(destPositionName);
							transportJobData.setDestinationZoneName(destZoneName);
							transportJobData.setPriority(priority);
							transportJobData.setAlternateFlag(alternateFlag);
							
							// Modified by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
//							transportJobData.setTransferState(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(currentMachineName, currentPositionType));
							transportJobData.setTransferState(transferState);
							
							ExtendedObjectProxy.getTransportJobCommandService().modify(eventInfo, transportJobData);
						}
					} catch (InvalidStateTransitionSignal ie) {
						eventLog.error(ie);
					} catch (FrameworkErrorSignal fe) {
						eventLog.error(fe);
					} catch (NotFoundSignal ne) {
						eventLog.error(ne);
					}
				} catch (CustomException e) {
					// TODO: handle exception
					// Commented by smkang on 2018.05.02 - Although any transport job has problem in for loop, another transport jobs should be updated.
					//									   So CustomException handler is added here.
					// Case 2. MCS reports a transport job data, but it isn't existed in MES database.
					Durable durableData = null;
					try {
						durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
					} catch (Exception e1) {
						// TODO: handle exception
					}
					
					try {
						MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
					} catch (Exception e2) {
						eventInfo.setEventComment("TransportRequest");
						
						String sourceMachineName = transportJobElement.getChildText("SOURCEMACHINENAME");
						String sourcePositionType = transportJobElement.getChildText("SOURCEPOSITIONTYPE");
						String sourcePositionName = transportJobElement.getChildText("SOURCEPOSITIONNAME");
						String sourceZoneName = transportJobElement.getChildText("SOURCEZONENAME");
						
						TransportJobCommand transportJobCommand = new TransportJobCommand();
						transportJobCommand.setTransportJobName(transportJobName);
						transportJobCommand.setTransportJobType(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportJobTypeByTransportJobName(transportJobName));
						transportJobCommand.setJobState(MESTransportServiceProxy.getTransportJobServiceUtil().getJobState(TransportJobStartedByMCS.class.getSimpleName(), doc));
						transportJobCommand.setCarrierName(carrierName);
						transportJobCommand.setSourceMachineName(sourceMachineName);
						transportJobCommand.setSourcePositionType(sourcePositionType);
						transportJobCommand.setSourcePositionName(sourcePositionName);
						transportJobCommand.setSourceZoneName(sourceZoneName);
						transportJobCommand.setDestinationMachineName(destMachineName);
						transportJobCommand.setDestinationPositionType(destPositionType);
						transportJobCommand.setDestinationPositionName(destPositionName);
						transportJobCommand.setDestinationZoneName(destZoneName);
						transportJobCommand.setPriority(priority);
						
						String lotName = "";
						try {
							Object[] bindSet = new Object[] { carrierName };
							
							List<Lot> arrayList = LotServiceProxy.getLotService().select("WHERE CARRIERNAME = ? " + "AND ROWNUM = 1", bindSet);
							
							if(arrayList != null)
								lotName = arrayList.get(0).getKey().getLotName();
						} catch(Exception ex1) {
						}
						
						double productQuantity = 0;
						Lot lotData = null;
						try {
							LotKey lotKey = new LotKey();
							lotKey.setLotName(lotName);
							lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
							
							productQuantity = lotData.getProductQuantity();
						} catch(Exception ex2) {
						}
						
						String carrierState = "";
						if(StringUtils.isEmpty(lotName))
							carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
						else
							carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
						
						transportJobCommand.setCarrierState(carrierState);
						transportJobCommand.setProductQuantity((long) productQuantity);
						transportJobCommand.setCurrentMachineName(currentMachineName);
						transportJobCommand.setCurrentPositionType(currentPositionType);
						transportJobCommand.setCurrentPositionName(currentPositionName);
						transportJobCommand.setCurrentZoneName(currentZoneName);
						
						// Modified by smkang on 2018.05.29 - Trust TRANSFERSTATE which is reported by MCS.
//						transportJobCommand.setTransferState(MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(currentMachineName, currentPositionType));
						transportJobCommand.setTransferState(transferState);
						
						transportJobCommand.setAlternateFlag(alternateFlag);
						transportJobCommand.setCleanState(durableData != null ? durableData.getDurableCleanState() : "N/A");
						
						// Added by smkang on 2019.02.27 - According to Park Hyojoon's request, report system needs to be added ProcessOperationName.
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
						
						MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(sourceMachineName));
						
						if(StringUtils.equals(sourceMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
							Port sourcePortData = MESPortServiceProxy.getPortServiceUtil().getPortData(sourceMachineName, sourcePositionName);
							
							if (!sourcePortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToUnload)) {
								MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
								makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToUnload);
								makeTranferStateInfo.setValidateEventFlag("N");
								
								PortServiceProxy.getPortService().makeTransferState(sourcePortData.getKey(), eventInfo, makeTranferStateInfo);
							}
						}
						
						MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(destMachineName));
						
						if(StringUtils.equals(destMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
							Port destPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(destMachineName, destPositionName);
							
							if (!destPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
								MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
								makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
								makeTranferStateInfo.setValidateEventFlag("N");
								
								PortServiceProxy.getPortService().makeTransferState(destPortData.getKey(), eventInfo, makeTranferStateInfo);
							}
						}
						
						eventInfo.setEventComment("TransportStart");
						
						// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
						// Added by smkang on 2018.11.15 - Management rule of TransportLockFlag is changed, so until a transport job is finished, TransportLockFlag will be remained 'Y'.
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//						Map<String, String> udfs = durableData.getUdfs();
						
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//						if (!StringUtils.equals(udfs.get("TRANSPORTLOCKFLAG"), "Y")) {
						if (!StringUtils.equals(durableData.getUdfs().get("TRANSPORTLOCKFLAG"), "Y")) {
							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "Y");
							
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
				            } catch (Exception e1) {
				            	eventLog.warn(e1);
				            }*/
						}
						// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
					}

					MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
				}
			}
			
			// Case 3. MCS doesn't report a transport job but the job is existed in the job of MES database.
			List<TransportJobCommand> transportJobList = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobList(mcsTransportJobNames);
			MESTransportServiceProxy.getTransportJobServiceImpl().terminateTransportJob(doc, transportJobList, eventInfo);
		} else {
			// Case 3. MCS doesn't report a transport job but the job is existed in the job of MES database.
			List<TransportJobCommand> transportJobList = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobList(null);
			MESTransportServiceProxy.getTransportJobServiceImpl().terminateTransportJob(doc, transportJobList, eventInfo);
		}
	}
}