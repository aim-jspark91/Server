package kr.co.aim.messolution.pms.event;

import java.util.Iterator;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.messolution.pms.management.data.BMUser;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class VerifyBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		String BMName 		 = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String ExecuteResult = SMessageUtil.getBodyItemValue(doc, "EXECUTERESULT", true);
		String BMGrade 		 = SMessageUtil.getBodyItemValue(doc, "BMGRADE", true);
		String BMState 	 	 = SMessageUtil.getBodyItemValue(doc, "BMSTATE", false);
		String NGReason      = SMessageUtil.getBodyItemValue(doc, "NGREASON", false);
		Element userE = SMessageUtil.getBodySequenceItem(doc, "USERLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("VerifyBM", getEventUser(), getEventComment(), null, null);		
		BM bmData = null;
		try
		{
			bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
			bmData.setExecuteResult(ExecuteResult);
			bmData.setBmGrade(BMGrade);
			if(ExecuteResult.equals("NG"))
				BMState = "Completed";
			else
				BMState = "Verified";
			bmData.setBmState(BMState);
			bmData.setNGReason(NGReason);
			bmData.setLastEventName(eventInfo.getEventName());
			bmData.setLastEventTime(eventInfo.getEventTime());
			bmData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0064", BMName);
		}
		//---…Ë÷√PMS_BMUser±Ì---
		BMUser userData = null;
		if (userE != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorUserList = userE.getChildren().iterator(); iteratorUserList.hasNext();)
			{
				Element userInfo = (Element) iteratorUserList.next();
				
				String BMUserName = SMessageUtil.getChildText(userInfo, "USERNAME", true);
				String BMUserType = "Verified"; 
				
				userData = new BMUser(BMUserName, BMName, BMUserType);
				userData.setBmID(BMName); 
				userData.setBmUser(BMUserName);
				userData.setUserType(BMUserType);
				userData.setTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeUtils.getCurrentEventTimeKey():eventInfo.getEventTimeKey());
				
				eventInfo.setEventName("");

				try
				{
					userData = PMSServiceProxy.getBMUserService().create(eventInfo, userData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0065", BMName);
				}
			}
		}
		
		return doc;
	}
}
