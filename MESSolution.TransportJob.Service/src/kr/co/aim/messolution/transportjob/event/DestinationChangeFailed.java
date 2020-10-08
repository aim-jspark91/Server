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

public class DestinationChangeFailed  extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.05.09 - Design is changed for DestinationChangeFailed action.
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFail", getEventUser(), getEventComment(), "", "");
//		
//		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
//		
//		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFail", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String oldDestMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", true);
		String oldDestPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);
		String newDestMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", true);
		String newDestPositionName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
		
		// Deleted by smkang on 2018.05.19 - Although the carrier is not existed in MES DB, the transport job should be updated.
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
//		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		// -----------------------------------------------------------------------------------------------------------------------------------------
		// Commented by smkang on 2018.05.09 - If destination is not changed, old destination should be reserved and new destination should be released.
		
		// Modify By Park Jeong Su on 2019.08.13  Mantis 4571 
//		MachineSpec oldDestMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(oldDestMachineName));
//		
//		if(StringUtils.equals(oldDestMachineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine)) {
//			Port oldDestPortData = MESPortServiceProxy.getPortServiceUtil().getPortData(oldDestMachineName, oldDestPositionName);
//			
//			if (!oldDestPortData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReservedToLoad)) {
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
//				makeTranferStateInfo.setValidateEventFlag("N");
//				
//				PortServiceProxy.getPortService().makeTransferState(oldDestPortData.getKey(), eventInfo, makeTranferStateInfo);
//			}
//		}
		
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
		// -----------------------------------------------------------------------------------------------------------------------------------------
		
	/*	// Added by smkang on 2018.10.07 - Need to forward a message to linked factory.
		MachineSpec sourceMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getSourceMachineName()));
		MachineSpec destMachineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(transportJobCommandInfo.getDestinationMachineName()));
		if (!sourceMachineSpecData.getFactoryName().equals(destMachineSpecData.getFactoryName())) {
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			if (sourceMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getSourceMachineName());
//			else if (destMachineSpecData.getFactoryName().equals(System.getProperty("shop")))
//				MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, transportJobCommandInfo.getDestinationMachineName());
			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, transportJobCommandInfo.getCarrierName());
		}*/
	}
}