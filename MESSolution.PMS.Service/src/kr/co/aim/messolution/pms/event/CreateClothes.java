package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateClothes extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// TODO EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		
		// TODO Get message from xml
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String userID = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String clothesID = SMessageUtil.getBodyItemValue(doc, "CLOTHESID", true);
		
		int clothesCountLimit;
		//  TODO check
		// check user clothes limit
		//UserClothes userClothesData = null;
		// check clothesLength
		
		// check clothe if exist
		
		
		// TODO Create New Clothes
		UserClothes clothesInfo = new UserClothes();
		eventInfo.setEventName("CreateClothes");
		clothesInfo.setUserID(userID);
		clothesInfo.setClothesID(clothesID);
		
		
		clothesInfo.setCurrentCleanTime(eventInfo.getEventTime());
		clothesInfo.setLasteventName(eventInfo.getEventName());
		clothesInfo.setLasteventComment(eventInfo.getEventComment());
		clothesInfo.setLasteventTime(eventInfo.getEventTime());
		clothesInfo.setLasteventTimeKey(eventInfo.getEventTimeKey());
		clothesInfo.setCreateUserID(eventInfo.getEventUser());
		clothesInfo.setCleanState("dirty");
		
		UserClothes userClothes = PMSServiceProxy.getCreateClothesService().create(eventInfo, clothesInfo);
		
		SMessageUtil.addItemToBody(doc, "CLOTHESID", clothesID);
		
		
		return doc;
	}

}
