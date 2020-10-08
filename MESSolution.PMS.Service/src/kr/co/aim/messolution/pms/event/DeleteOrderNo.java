package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteOrderNo extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
			
		List<Element> OrderList = SMessageUtil.getBodySequenceItemList(doc, "ORDERNOLIST", true);

		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteOrderInfo", getEventUser(), getEventComment(), null, null);
				
		for(Element order:OrderList)
		{
			String orderNo    = SMessageUtil.getChildText(order, "ORDERNO", true);
			String partID = SMessageUtil.getChildText(order, "PARTID", true);
			
			try
			{						
				SparePartInOut sparePartInOut = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[] {orderNo,partID});
				PMSServiceProxy.getSparePartInOutService().remove(eventInfo, sparePartInOut);		
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0061", orderNo);
			}			
		}
		return doc;												
	}
}
