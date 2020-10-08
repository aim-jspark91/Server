package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;



import org.jdom.Document;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

/**
 * @author smkang
 * @since 2018.08.11
 * @see Reset Machine Idle Time For MQC Condition.
 */
public class ResetProductSpecIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String productspecname = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String idleTime = SMessageUtil.getBodyItemValue(doc, "IDLETIME", false);
		String validFlag = SMessageUtil.getBodyItemValue(doc, "VALIDFLAG", false);
		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		try {
			ExtendedObjectProxy.getProductSpecIdleTimeService().forceResetProductSpecIdleTime(productspecname,eventInfo);
			
			
			
			
			
			
			
		} catch (Exception e) {
			eventLog.warn(e);
		}
		
		return doc;
	}
}