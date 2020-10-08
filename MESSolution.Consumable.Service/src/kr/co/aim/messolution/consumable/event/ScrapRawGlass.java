package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ScrapRawGlass extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
	
		String sConsumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String sQuantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc,"REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc,"REASONCODE", false);
		String expirationDate = SMessageUtil.getBodyItemValue(doc, "EXPIRATIONDATE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("ScrapRawGlass");
		
		
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
		udfs.put("EXPIRATIONDATE", expirationDate);
		
		Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sConsumableName);
		
		double scrappedQuantity = Double.parseDouble(sQuantity);
		
		TransitionInfo transitionInfo;
		
		transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
													eventInfo.getEventTimeKey(),scrappedQuantity, udfs);
		
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData,
									(DecrementQuantityInfo) transitionInfo, eventInfo);
		
		//if existing raw glass quantity is the same with scrapped raw glass quantity, makeNotAvailable
		crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
		if(crateData.getQuantity() == 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
		{
			//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventName("ChangeState");
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
		}
		
		return doc;
	}

}
