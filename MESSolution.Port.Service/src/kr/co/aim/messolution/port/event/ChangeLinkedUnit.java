package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeLinkedUnit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String linkedUnitName = SMessageUtil.getBodyItemValue(doc, "LINKEDUNITNAME", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLinkedUnit", this.getEventUser(), this.getEventComment(), null, null);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> udfs = portData.getUdfs();
//		udfs.put("LINKEDUNITNAME", linkedUnitName);
//		portData.setUdfs(udfs);
		
		SetEventInfo transitionInfo = new SetEventInfo();
		transitionInfo.getUdfs().put("LINKEDUNITNAME", linkedUnitName);

		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);		
		return doc;
	
	}
}
