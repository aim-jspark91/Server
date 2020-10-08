package kr.co.aim.messolution.durable.event;

import java.util.List;

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

import org.jdom.Document;
import org.jdom.Element;

public class DeleteCST extends SyncHandler{
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteCST", getEventUser(), getEventComment(), "", "");
		List<Element> eleList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
		
		if (eleList != null && eleList.size() > 0)
		{
			// Added by smkang on 2018.10.02 - According to EDO's request, carrier data should be synchronized with shared factory.
			//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, eleList.get(0).getChildText("DURABLENAME"));
			
			for(Element eleCarrier : eleList)
			{
				String sDurableName = SMessageUtil.getChildText(eleCarrier, "DURABLENAME", true);				
				DurableKey durableKey = new DurableKey(sDurableName);
				
				// Modified by smkang on 2018.10.10 - setEvent and delete methods can be replaced with remove method.
//				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);			
//
//				String condition = "WHERE Durablename = ? and FactoryName = ?";
//				Object[] bindSet = new Object[] { sDurableName, sFactoryname };				
//				
//				SetEventInfo setEventInfo = new SetEventInfo();
//				Map<String, String> udfs = durableData.getUdfs();
//				setEventInfo.setUdfs(udfs);				
//				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);			
//				// Delete Durable
//				DurableServiceProxy.getDurableService().delete(condition, bindSet);
				// Modified by smkang on 2018.10.15 - remove method doesn't record a data in DurableHistory.
//				DurableServiceProxy.getDurableService().remove(durableKey);
				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
								
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(durableData.getUdfs());
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
				
				DurableServiceProxy.getDurableService().delete(durableKey);
			}
		}
				
		return doc;
	}
}