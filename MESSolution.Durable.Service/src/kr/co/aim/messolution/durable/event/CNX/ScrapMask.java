package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ScrapMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if (eleBody != null) {
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,"DURABLELIST", false)) 
			{
				String sDurableName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
				String sReasonCode= SMessageUtil.getChildText(eledur,"REASONCODE", true);
				String sReasonCodeType= SMessageUtil.getChildText(eledur,"REASONCODETYPE", true);

				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Durable durableData = CommonUtil.getDurableInfo(sDurableName);
				Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
				
				if (StringUtil.equals(durableData.getDurableState(), "Scrapped"))
					throw new CustomException("MASK-0012", sDurableName);
				
				if (StringUtil.equals(durableData.getDurableState(), "InUse"))
					throw new CustomException("MASK-0030", sDurableName);
			
				durableData.setReasonCode(sReasonCode);
				durableData.setReasonCodeType(sReasonCodeType);
				
				DurableServiceProxy.getDurableService().update(durableData);
				durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
				
				// Make Scrapped Info
				MakeScrappedInfo makeScrappedInfo = MESDurableServiceProxy.getDurableInfoUtil().makeScrappedInfo(durableData);
				
				// Execute
				MESDurableServiceProxy.getDurableServiceImpl().makeScrapped(durableData, makeScrappedInfo, eventInfo);
				
			}
		}
		
		return doc;
	}
}