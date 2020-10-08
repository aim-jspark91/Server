package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;

public class PortTypeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);

		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);

		if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
        {
		    portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
        }
        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
        }
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortKey portKey = new PortKey();
		portKey.setMachineName(machineName);
		portKey.setPortName(portName);
		Port portData = PortServiceProxy.getPortService().selectByKeyForUpdate(portKey);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", getEventUser(), getEventComment(), null, null);

		// Port Type Change
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(portType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", portType));
		}
		else
		{
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTTYPE", portType);

			PortServiceProxy.getPortService().update(portData);

			SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);

			// Send to FMC
            try
            {
                GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
            }
            catch(Exception ex)
            {
                eventLog.warn("FMC Report Failed!");
            }
		}

		/* 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. ==>> */
		// Port Access Mode Change
		//if (portData.getAccessMode().equals(portAccessMode))
		//{
		//	eventLog.warn(String.format("Attribute[%s] is still [%s]", "ACCESSMODE", portAccessMode));
		//}
		//else
		//{
        //
		//    eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
        //
		//	portData.getUdfs().put("PORTTYPE", portType);
        //
		//	PortServiceProxy.getPortService().update(portData);
        //
		//	MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);
        //
		//	MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);
        //
		//	// Send to FMC
		//	try
		//	{
		//		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		//	}
		//	catch(Exception ex)
		//	{
		//		eventLog.warn("FMC Report Failed!");
		//	}
		//}
		/* <<== 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. */
	}
}
