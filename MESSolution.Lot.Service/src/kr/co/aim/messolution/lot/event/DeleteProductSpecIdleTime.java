package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductSpecIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;


public class DeleteProductSpecIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String productspecname = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		String condition = "PRODUCTSPECNAME = ?";
		Object[] bindSet = new Object[] {productspecname};
		
		try {
			List<ProductSpecIdleTime> ProductSpecIdleTimeList = ExtendedObjectProxy.getProductSpecIdleTimeService().select(condition, bindSet);
			
			for (ProductSpecIdleTime ProductSpecIdleTime : ProductSpecIdleTimeList) {
				ExtendedObjectProxy.getProductSpecIdleTimeService().remove(eventInfo, ProductSpecIdleTime);
			}
		} catch (Exception e) {
			eventLog.warn(e);
		}

		
		
		return doc;
	}
}