package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CarrierRegionChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");

		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true);
		
		for (Element data : dataList)
		{	
			String carrierName = SMessageUtil.getChildText(data, "CARRIERNAME", true);
			String region = SMessageUtil.getChildText(data, "REGION", false);
			String kanban = SMessageUtil.getChildText(data, "KANBAN", false);
			
			// Check Exist Carrier
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			// Change Carrier Region
			durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierRegion(durableData, eventInfo, region, kanban);
			
			/*// Added by smkang on 2018.11.03 - For synchronization of a carrier information, common method will be invoked.
            try {
				Element bodyElement = new Element(SMessageUtil.Body_Tag);
				bodyElement.addContent(new Element("DURABLENAME").setText(carrierName));
				bodyElement.addContent(new Element("KANBAN").setText(kanban));
				bodyElement.addContent(new Element("REGION").setText(region));
				
				// Modified by smkang on 2018.11.03 - EventName will be recorded triggered EventName.
//				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), "SynchronizeCarrierState");
				Document synchronizeCarrierMessage = SMessageUtil.createXmlDocument(bodyElement, "SynchronizeCarrierState", "", "", eventInfo.getEventUser(), eventInfo.getEventName());
				
				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(synchronizeCarrierMessage, carrierName);
            } catch (Exception e) {
            	eventLog.warn(e);
            }*/
		}
	}
}