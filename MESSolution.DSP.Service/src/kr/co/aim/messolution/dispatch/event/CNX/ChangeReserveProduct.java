package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeReserveProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> eleProductList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", getEventUser(), getEventComment(), "", "");
		
		for(Element eleProduct : eleProductList)
		{
			String machineName = SMessageUtil.getChildText(eleProduct, "MACHINENAME", true);
			String processOperationGroupName = SMessageUtil.getChildText(eleProduct, "PROCESSOPERATIONGROUPNAME", true);
			String processOperationName = SMessageUtil.getChildText(eleProduct, "PROCESSOPERATIONNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleProduct, "PRODUCTSPECNAME", true);
			String position = SMessageUtil.getChildText(eleProduct, "POSITION", true);
			String reserveState = SMessageUtil.getChildText(eleProduct, "RESERVESTATE", true);
			String reservedQuantity = SMessageUtil.getChildText(eleProduct, "RESERVEDQUANTITY", true);
			String completeQuantity = SMessageUtil.getChildText(eleProduct, "COMPLETEQUANTITY", true);
			
			ReserveProduct reserveProductData = ExtendedObjectProxy.getReserveProductService().selectByKey(false, 
					new Object[]{machineName, processOperationGroupName, processOperationName, productSpecName});
			
			ExtendedObjectProxy.getReserveProductService().modify(eventInfo, reserveProductData);
			
		}
		return doc;
	}

}
