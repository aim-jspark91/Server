package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

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

public class CleanMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Clean", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			
			String sDurableCleanState = "Clean";
			String sTransportState = "INLINE";
			
			//latest Mask data
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName); 
			Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName)); 
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("LASTCLEANTIME", eventInfo.getEventTime().toString());
			udfs.put("TRANSPORTSTATE", sTransportState);
			
			// Object Attribute Type: Standard
			durableData.setDurableCleanState(sDurableCleanState);			
			
			DurableServiceProxy.getDurableService().update(durableData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			eventInfo = EventInfoUtil.makeEventInfo("Clean", getEventUser(), getEventComment(), null, null);
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}
		
		return doc;	
	}
}