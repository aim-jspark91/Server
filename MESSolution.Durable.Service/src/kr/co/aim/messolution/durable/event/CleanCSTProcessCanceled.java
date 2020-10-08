package kr.co.aim.messolution.durable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CleanCSTProcessCanceled extends AsyncHandler {

    @Override
    public void doWorks(Document doc)
        throws CustomException
    {
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
        String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
        String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
        String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
        String reasonCodeDescription = SMessageUtil.getBodyItemValue(doc, "REASONCODEDESCRIPTION", false);

        DurableKey durableKey = new DurableKey();
        durableKey.setDurableName(carrierName);
        Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

        EventInfo eventInfo = EventInfoUtil.makeEventInfo("CleanCSTProcessCanceled", getEventUser(), getEventComment(), "CST-CANCEL", reasonCode);

        SetEventInfo setEventInfo = new SetEventInfo();
        setEventInfo.getUdfs().put("MACHINENAME", machineName);
        setEventInfo.getUdfs().put("MACHINERECIPE", machineRecipeName);

        MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

        /* 20190102, hhlee, delete, change logic ==>> */
        MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","ABHC");
        //MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST", reasonCode);
        /* <<== 20190102, hhlee, delete, change logic */
        
        // Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
        //MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, carrierName);
    }
}