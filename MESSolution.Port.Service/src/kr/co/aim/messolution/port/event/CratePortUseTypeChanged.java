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

public class CratePortUseTypeChanged extends AsyncHandler{

	@Override
	public void doWorks(Document doc) throws CustomException {
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
		
		/* Additional data as information */
		//String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
		//String porttype = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		//String portaccessmode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals(portUseType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTUSETYPE", portUseType));
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeUseType", getEventUser(), getEventComment(), null, null);
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTUSETYPE", portUseType);
			
			SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
		}
	}
}
