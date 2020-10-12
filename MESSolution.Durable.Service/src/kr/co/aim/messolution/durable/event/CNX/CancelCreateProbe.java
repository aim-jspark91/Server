package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateProbe extends SyncHandler 
{
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreate", this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null) 
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,"DURABLELIST", false)) 
			{
				//parsing
				String sDurableName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
				
				DurableKey durableKey = new DurableKey();
				durableKey.setDurableName(sDurableName);
				
				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
				//validation
				if (!StringUtils.equals(durableData.getDurableState(), "Available"))
					throw new CustomException("PROBE-0002");

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, new SetEventInfo(), eventInfo);
				
				//Execute
				DurableServiceProxy.getDurableService().remove(durableKey);
			}
		}
		return doc;
	}
}
