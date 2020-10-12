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
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeCrateQuantity extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("ChangeQuantity");
		
		String sConsumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String sQuantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String expirationDate = SMessageUtil.getBodyItemValue(doc, "EXPIRATIONDATE", true);
				
		
		Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sConsumableName);
		
		double oldQuantity = crateData.getQuantity();
		double newQuantity = Double.parseDouble(sQuantity);
	
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
		udfs.put("EXPIRATIONDATE", expirationDate);
		
		
		if (oldQuantity > newQuantity)
		{
			//eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
			//decrement
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
											eventInfo.getEventTimeKey(), oldQuantity-newQuantity, udfs);
			
			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData,
										(DecrementQuantityInfo) transitionInfo, eventInfo);
			
			//makeNotAvailable
			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
			
			if(crateData.getQuantity() == 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
			{
				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventName("ChangeState");
				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
				makeNotAvailableInfo.setUdfs(crateData.getUdfs());
				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
			}
		}
		else if (oldQuantity < newQuantity)
		{
			//makeAvailable
			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
			
			if(crateData.getQuantity() == 0 && !StringUtil.equals(crateData.getConsumableState(), "Available"))
			{
				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventName("ChangeState");
				MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
				makeAvailableInfo.setUdfs(crateData.getUdfs());
				MESConsumableServiceProxy.getConsumableServiceImpl().makeAvailable(crateData, makeAvailableInfo, eventInfo);
			}
			eventInfo.setEventName("ChangeQuantity");
			//eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
			//increment
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().incrementQuantityInfo(newQuantity-oldQuantity, udfs);
			
			MESConsumableServiceProxy.getConsumableServiceImpl().incrementQuantity(crateData, (IncrementQuantityInfo) transitionInfo, eventInfo);
		}
		else
		{
			//20170121 Add by yudan
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
					eventInfo.getEventTimeKey(), oldQuantity-newQuantity, udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData,
					(DecrementQuantityInfo) transitionInfo, eventInfo);
			
			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
		}
		
		return doc;
	}

}
