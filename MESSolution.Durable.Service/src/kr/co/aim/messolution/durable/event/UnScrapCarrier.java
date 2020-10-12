package kr.co.aim.messolution.durable.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnScrapCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
				
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc,"REASONCODETYPE", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc,"REASONCODE", false);
		
		EventInfo eventInfo = 
				EventInfoUtil.makeEventInfo("UnScrap", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
		
		List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
		
		if(eleList!=null)
		{
			for(Element eleCarrier : eleList)
			{
				String sDurableName = SMessageUtil.getChildText(eleCarrier, "DURABLENAME", true);
				
				DurableKey durableKey = new DurableKey();

				durableKey.setDurableName(sDurableName);

				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
/*				if(!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")))
				{
					if(!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
					{
						throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
					}
				}*/
				
				MESDurableServiceProxy.getDurableServiceImpl().makeUnScrap(durableData, eventInfo);
			}
			
		/*	// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
			if (eleList.size() > 0)
				MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, eleList.get(0).getChildText("DURABLENAME"));*/
		}
		
		return doc;
	}

}
