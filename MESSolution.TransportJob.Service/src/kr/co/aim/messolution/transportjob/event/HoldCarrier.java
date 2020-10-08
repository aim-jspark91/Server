package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class HoldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{

		Element eDurableList = SMessageUtil.getBodySequenceItem(doc, "DURABLELIST", true);
		
		for (@SuppressWarnings("rawtypes")
		Iterator iDurable = eDurableList.getChildren().iterator(); iDurable.hasNext();)
		{
			Element eDurable = (Element) iDurable.next();
			String sDurableName = SMessageUtil.getChildText(eDurable, "DURABLENAME", true);
			String sReasonCodeType = SMessageUtil.getChildText(eDurable,"REASONCODETYPE", false);
			String sReasonCode = SMessageUtil.getChildText(eDurable,"REASONCODE", false);
			String sDurableHoldState = SMessageUtil.getChildText(eDurable,"DURABLEHOLDSTATE", true);
			String sEventCommnet = SMessageUtil.getChildText(eDurable,"EVENTCOMMENT", false);

			DurableKey durableKey = new DurableKey();
	
			durableKey.setDurableName(sDurableName);

			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

			if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
				throw new CustomException("CST-0005", sDurableName);
		
			SetEventInfo setEventInfo = new SetEventInfo();
			
			Map<String, String> durableUdfs = setEventInfo.getUdfs();

			durableUdfs.put("DURABLEHOLDSTATE", sDurableHoldState);

			setEventInfo.setUdfs(durableUdfs);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(),"", sReasonCodeType, sReasonCode);
			eventInfo.setReasonCodeType(sReasonCodeType);
			eventInfo.setReasonCode(sReasonCode);
			eventInfo.setEventComment(sEventCommnet);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
	
			}
		return doc;
	}

}
