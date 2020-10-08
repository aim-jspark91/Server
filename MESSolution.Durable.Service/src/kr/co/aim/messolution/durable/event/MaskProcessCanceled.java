package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class MaskProcessCanceled extends AsyncHandler{
	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", getEventUser(), getEventComment(), null, null);
		
		//existence validation
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
	}
}
