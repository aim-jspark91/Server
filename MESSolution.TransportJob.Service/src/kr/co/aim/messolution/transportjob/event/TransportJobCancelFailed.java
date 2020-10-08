package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;

import org.jdom.Document;

public class TransportJobCancelFailed extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFail", getEventUser(), getEventComment(), "", "");
		
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);

		TransportJobCommand transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
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