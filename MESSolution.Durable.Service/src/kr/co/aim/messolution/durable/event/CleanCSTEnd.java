package kr.co.aim.messolution.durable.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CleanCSTEnd extends AsyncHandler {

	@Override
	public void doWorks(Document doc)
		throws CustomException
	{
		String sCarrierName = SMessageUtil.getBodyItemValue(doc,"CARRIERNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc,"PORTNAME", false);
		//Commit

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sCarrierName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CSTCleanEnd", getEventUser(), getEventComment(), "", "");

		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> durableUdfs = durableData.getUdfs();
//		durableUdfs.put("MACHINENAME", sMachineName);
//		durableUdfs.put("PORTNAME", sPortName);
//		durableUdfs.put("POSITIONTYPE", "PORT");
		
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
		/* 20190319, hhlee, modify, TimeSync DurableHistory EventTime and LastCleanTime */
		//durableUdfs.put("LASTCLEANTIME", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		durableUdfs.put("LASTCLEANTIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
		/* 20181022, hhlee, add, DryFlag ==>> */
//		durableUdfs.put("DRYFLAG", GenericServiceProxy.getConstantMap().FLAG_N);
		/* <<== 20181022, hhlee, add, DryFlag */

		CleanInfo cleanInfo = new CleanInfo();
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		cleanInfo.setUdfs(durableUdfs);
		cleanInfo.getUdfs().put("MACHINENAME", sMachineName);
		cleanInfo.getUdfs().put("PORTNAME", sPortName);
		cleanInfo.getUdfs().put("POSITIONTYPE", "PORT");
		cleanInfo.getUdfs().put("LASTCLEANTIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		cleanInfo.getUdfs().put("DRYFLAG", GenericServiceProxy.getConstantMap().FLAG_N);

		durableData = DurableServiceProxy.getDurableService().clean(durableData.getKey(), eventInfo, cleanInfo);
		
		/*//2019.02.14 dmlee : No Exist OLED 'A_CleanCSTEnd' so send 'SynchronizeCarrierState' To OLED
        try {
        	String carrierName = durableData.getKey().getDurableName();

			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
			// 2019.04.09_hsryu_Delete Logic.
			//bodyElement.addContent(new Element("DURABLESTATE").setText(durableData.getDurableState()));
			bodyElement.addContent(new Element("DURABLECLEANSTATE").setText(durableData.getDurableCleanState()));
			bodyElement.addContent(new Element("DURABLEDRYFLAG").setText(GenericServiceProxy.getConstantMap().FLAG_N));
			
			// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//			Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
			Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
			
			MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
        } catch (Exception e) {
        	eventLog.warn(e);
        }*/
		

		// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
        //2019.02.14 dmlee : No Exist OLED 'A_CleanCSTEnd' so send 'SynchronizeCarrierState' To OLED
		//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sCarrierName);
	}
}