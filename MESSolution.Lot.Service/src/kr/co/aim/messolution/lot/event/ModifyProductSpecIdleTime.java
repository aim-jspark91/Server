package kr.co.aim.messolution.lot.event;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductSpecIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.09.25
 * @see According to EDO's request, Add/Update/Delete views are added.
 */
public class ModifyProductSpecIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{   
		
		
		String productspecname = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String idleTime = SMessageUtil.getBodyItemValue(doc, "IDLETIME", false);
		String validFlag = SMessageUtil.getBodyItemValue(doc, "VALIDFLAG", false);
		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		try {
		
			//String condition = "PRODUCTSPECNAME = ?";
			Object[] bindSet = new Object[] {productspecname};

			ProductSpecIdleTime  ProductSpecIdleTimeInfo = new ProductSpecIdleTime();
           ProductSpecIdleTimeInfo  = ExtendedObjectProxy.getProductSpecIdleTimeService().selectByKey(false,bindSet);
			
			
			
			
			
		       
			
			if (ProductSpecIdleTimeInfo != null ) {
				
			
				

				ProductSpecIdleTimeInfo.setValidflag(validFlag);
				ProductSpecIdleTimeInfo.setIdletime(idleTime);
				ProductSpecIdleTimeInfo.setLasteventname(eventInfo.getEventName());
				
				
				ProductSpecIdleTimeInfo.setLasteventtime(eventInfo.getEventTime());
				ProductSpecIdleTimeInfo.setLasteventtimekey(eventInfo.getEventTimeKey());
				
				ProductSpecIdleTimeInfo.setLasteventuser(eventInfo.getEventUser());
				ProductSpecIdleTimeInfo.setLasteventcomment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getProductSpecIdleTimeService().modify(eventInfo, ProductSpecIdleTimeInfo);
			}
		} catch (Exception e) {
			// TODO: handle exception
		    
			
		}

		
		
		return doc;
	}
}