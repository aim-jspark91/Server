package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserClothes;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CleanClothes extends SyncHandler
{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// TODO eventinfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventName("cleanClothes");
		
		// TODO getClothesID from xml
		String clothesID = SMessageUtil.getBodyItemValue(doc, "CLOTHESID", true);
		
		// TODO check
		
		
		UserClothes clothesInfo =null;
		Object[] keySet = new Object[]{clothesID};
		try
		{
			clothesInfo = PMSServiceProxy.getCreateClothesService().selectByKey(true, keySet );
		}
		catch (greenFrameDBErrorSignal e)
		{
			// TODO: handle exception
			throw new CustomException("PMS-9000",clothesID);
		}
				
		//clothesInfo.setLastCleanTime(clothesInfo.getCurrentCleanTime());
		//2016-01-04 ADD
		if(StringUtils.equals(clothesInfo.getCleanState(), "cleaned"))
		{
			throw new CustomException("PMS-9001","cleaned");
		}
		
		clothesInfo.setCurrentCleanTime(eventInfo.getEventTime());
		clothesInfo.setCleanState("cleaned");
		clothesInfo.setLasteventComment(eventInfo.getEventComment());
		clothesInfo.setLasteventName(eventInfo.getEventName());
		
		
		// TODO update 
		PMSServiceProxy.getCreateClothesService().modify(eventInfo, clothesInfo);
		// TODO sendBack
		
		SMessageUtil.addItemToBody(doc, "CLOTHESID", clothesID);
		
		return doc;
	}

}
