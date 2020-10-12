package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.ESDTestSet;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateESDTestSet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String UserID 		     = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String AlarmLimit 	 = SMessageUtil.getBodyItemValue(doc, "ALARMLIMIT", true);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("ESDTestSet", getEventUser(), getEventComment(), null, null);
		
		ESDTestSet ESDTestSetData = null;

		try
		{
			ESDTestSetData=PMSServiceProxy.getESDTestSetService().selectByKey(true, new Object[] {UserID});
			//SET
			ESDTestSetData.setUserID(UserID);
			ESDTestSetData.setAlarmLimit(AlarmLimit);
			ESDTestSetData=PMSServiceProxy.getESDTestSetService().modify(eventInfo, ESDTestSetData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0064", UserID);
		}	

		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		return rtnDoc;
	}
	
}
