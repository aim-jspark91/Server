package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class UpdateClothesInfo extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// TODO EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventName("updateClothesInfo");
		
		// get message from xml
		String userID = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String clothesID = SMessageUtil.getBodyItemValue(doc, "CLOTHESID", true);
		String cleanState = SMessageUtil.getBodyItemValue(doc, "CLEANSTATE", true);
		
		
		Object[] keySet = new Object[]{clothesID};
		UserClothes clothesInfo  = PMSServiceProxy.getCreateClothesService().selectByKey(true, keySet );
		clothesInfo.setUserID(userID);
		clothesInfo.setCleanState(cleanState);
		
		PMSServiceProxy.getCreateClothesService().modify(eventInfo, clothesInfo);
		
		return doc;
	}

}
