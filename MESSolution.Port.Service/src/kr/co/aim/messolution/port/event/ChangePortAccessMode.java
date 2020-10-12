package kr.co.aim.messolution.port.event;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;

import org.jdom.Document;

public class ChangePortAccessMode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		String sAccessMode = SMessageUtil.getBodyItemValue(doc, "ACCESSMODE", true);

		if(StringUtil.equals(StringUtil.upperCase(sAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
        {
		    sAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
        }
        else if(StringUtil.equals(StringUtil.upperCase(sAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
        {
            sAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
        }
        else
        {
        }
		
		/* 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. ==>> */
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", this.getEventUser(), this.getEventComment(), null, null);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sAccessMode);

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
		/* <<== 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. */
		return doc;
	}

}
