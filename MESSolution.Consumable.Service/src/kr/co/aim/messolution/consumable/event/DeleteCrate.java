package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteCrate extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		try
		{
			String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
			
			Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
			
			if (!crateData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
				throw new CustomException("CONS-0001", crateData.getKey().getConsumableName(), crateData.getConsumableState());
			
			// 2019.09.06 Modify By Park Jeong Su Mantis 4706
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteCrate", getEventUser(), getEventComment(), null, null);
			DecrementQuantityInfo decrementQuantityInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "", eventInfo.getEventTimeKey(), crateData.getQuantity(), crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData, decrementQuantityInfo, eventInfo);

			// 2019.09.06 Modify By Park Jeong Su Mantis 4706
			eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);

 		}
		catch (Exception ex)
		{
			eventLog.warn(ex);
		}
		
		return doc;
	}
}
