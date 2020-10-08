package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.InhibitException;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CancelInhibitException extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException{
		
		String inhibitID = SMessageUtil.getBodyItemValue(doc, "INHIBITID", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", this.getEventUser(), this.getEventComment(), "", "");
		InhibitException inhibitExceptionData = null;
		
		try{
			inhibitExceptionData = ExtendedObjectProxy.getInhibitExceptionService().selectByKey(false, new Object[]{lotName,inhibitID});
		}
		catch(NotFoundSignal ex)
		{
			throw new CustomException("INHIBIT-0001", "");
		}
		
		ExtendedObjectProxy.getInhibitExceptionService().remove(eventInfo, inhibitExceptionData);
		
		return doc;
	}
}
