package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZone;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class ModifyEmptyCassetteCount extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "ZONENAME", true);
		String minEmptyCarrierCount = SMessageUtil.getBodyItemValue(doc, "MINEMPTYCARRIERCOUNT", true);
		
        Object[] keySet = new Object[]{stockerName, zoneName};
	    DspStockerZone stockerData = ExtendedObjectProxy.getDspStockerZoneService().selectByKey(false, keySet);
			
		stockerData.setMinEmptyCarrierCount(Long.parseLong(minEmptyCarrierCount.trim()));
		ExtendedObjectProxy.getDspStockerZoneService().update(stockerData);
		
		return doc;
	}
}
