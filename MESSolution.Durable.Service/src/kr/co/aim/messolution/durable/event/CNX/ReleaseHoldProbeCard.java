package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;
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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ReleaseHoldProbeCard  extends SyncHandler{
	public Object doWorks(Document doc) throws CustomException
	{
		// get durable list
		List<Element> eleDurableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);
		
		for (Element eleDurable : eleDurableList)
		{
			String sDurableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);
			String sDurableHoldState = SMessageUtil.getChildText(eleDurable,"DURABLEHOLDSTATE", true);
			
			DurableKey durableKey = new DurableKey();
			
			durableKey.setDurableName(sDurableName);
			
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			
			if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "N"))
				throw new CustomException("PROBE-0005", sDurableName);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> durableUdfs = durableData.getUdfs();
			durableUdfs.put("DURABLEHOLDSTATE", sDurableHoldState);
			setEventInfo.setUdfs(durableUdfs);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", getEventUser(), getEventComment(), "", "");
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}
		return doc;
	}	

}
