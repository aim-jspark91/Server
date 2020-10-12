package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class CratePermissionRequest extends SyncHandler { 
	
	@Override 
	public Object doWorks(Document doc) throws CustomException {
		
		//pre-processing for sync
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CratePermissionReply");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType    = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType    = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
		String barcodeData     = SMessageUtil.getBodyItemValue(doc, "BARCODEDATA", false);
		
		//existence validation
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName); 
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
		
		Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(barcodeData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		return doc;
	}
}
