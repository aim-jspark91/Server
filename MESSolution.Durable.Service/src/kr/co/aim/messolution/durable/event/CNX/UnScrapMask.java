package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnScrapMask extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String sReasonCodeType = "";
			String sReasonCode = "";
			
			DurableKey durableKey = new DurableKey();
			
			durableKey.setDurableName(sDurableName);
			
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
			
			MESDurableServiceProxy.getDurableServiceImpl().makeUnScrap(durableData, eventInfo);
		}
		return doc;
	}
}
