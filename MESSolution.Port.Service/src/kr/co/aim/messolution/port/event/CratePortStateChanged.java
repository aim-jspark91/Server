package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;

import org.jdom.Document;

public class CratePortStateChanged extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {

        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
        String portStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATENAME", true);
        
        EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
        
        Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

        //change port state
        eventInfo.setEventName("ChangeState");
        MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, portStateName);
        MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, makePortStateByStateInfo, eventInfo);
        

        //159512 by swcho : success then report to FMC
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
