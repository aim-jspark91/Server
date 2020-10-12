package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;

import org.jdom.Document;

public class CratePortAccessModeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		
		/* Additional data as information */
		//String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		//String porttype = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		//String portusetype = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);
		
		if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
        {
		    portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
        }
        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
        }
        else
        {
        }
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", getEventUser(), getEventComment(), null, null); 
		
		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);
		
		MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}
}
