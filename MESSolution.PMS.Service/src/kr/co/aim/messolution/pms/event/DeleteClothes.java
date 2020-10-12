package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteClothes extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// TODO eventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventName("DeleteClothes");
		// get message from xml
		List<Element> clothesList = SMessageUtil.getBodySequenceItemList(doc, "CLOTHESLIST", true);
		for (Element element : clothesList)
		{
			String clothesID = SMessageUtil.getChildText(element, "CLOTHESID", true);

			Object[] keySet = new Object[] { clothesID };
			UserClothes clothesInfo = PMSServiceProxy.getCreateClothesService().selectByKey(true, keySet);
			PMSServiceProxy.getCreateClothesService().delete(clothesInfo);
		}

		return doc;
	}

}
