package kr.co.aim.messolution.durable.event.CNX;

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

public class HoldMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if (eleBody != null) {
			for (Element eleDurable : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false)){
				// parsing
				String sDurableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);
				String sDurableHoldState = SMessageUtil.getChildText(eleDurable,"DURABLEHOLDSTATE", true);
				String sReasonCode = SMessageUtil.getChildText(eleDurable,"REASONCODE", true);
				String sReasonCodeType = SMessageUtil.getChildText(eleDurable,"REASONCODETYPE", true);
				
				DurableKey durableKey = new DurableKey();
				
				durableKey.setDurableName(sDurableName);
				
				Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
				
				// validation
				if(StringUtil.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
					throw new CustomException("MASK-0013", sDurableName);
				
				//if (durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
				//	throw new CustomException("MASK-0012", sDurableName, durableData.getDurableState());
				
				durableData.setReasonCode(sReasonCode);
				durableData.setReasonCodeType(sReasonCodeType);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				
				Map<String, String> udfs = durableData.getUdfs();
				
				udfs.put("DURABLEHOLDSTATE", sDurableHoldState);
				
				setEventInfo.setUdfs(udfs);
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
				
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
		return doc;
	}
}