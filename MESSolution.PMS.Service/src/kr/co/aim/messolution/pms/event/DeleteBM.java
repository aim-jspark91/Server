package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String BMName 		 = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteBM", getEventUser(), getEventComment(), null, null);
		
		try
		{						
			BM BMData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
			PMSServiceProxy.getBMService().remove(eventInfo, BMData);		
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0091", BMName);
		}	
		
		return doc;
	}
}
