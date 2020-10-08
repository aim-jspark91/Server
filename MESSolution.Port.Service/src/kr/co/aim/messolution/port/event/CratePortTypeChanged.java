package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;

public class CratePortTypeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		
		/* Additional data as information */
		//String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
		//String portusetype = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		//String portaccessmode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
				
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(portType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", portType));
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", getEventUser(), getEventComment(), null, null);
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTTYPE", portType);
			
			SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
		}
	}
}
