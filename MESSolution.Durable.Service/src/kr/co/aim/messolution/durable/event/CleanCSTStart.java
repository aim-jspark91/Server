package kr.co.aim.messolution.durable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CleanCSTStart extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String sCarrierName = SMessageUtil.getBodyItemValue(doc,"CARRIERNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc,"PORTNAME", false);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sCarrierName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CSTCleanStarted", getEventUser(), getEventComment(), "", "");

		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> durableUdfs = durableData.getUdfs();
//		durableUdfs.put("MACHINENAME", sMachineName);
//		//durableUdfs.put("PORTNAME", sPortName);
//
//		SetEventInfo setEventInfo = new SetEventInfo();
//		setEventInfo.setUdfs(durableUdfs);
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", sMachineName);

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		// Added by smkang on 2018.12.05 - According to Liu Hongwei's request, if a carrier is started to clean at another machine, reserved lot information is removed.
		ExtendedObjectProxy.getDspReserveLotService().ignoreReserveLot(eventInfo, "", sCarrierName, sMachineName);

		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
		//2019.02.14 dmlee 
        //MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sCarrierName);
	}
}