package kr.co.aim.messolution.pms.event;

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

public class EvaluationBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String BMName 	         = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String EvaluationUser    = SMessageUtil.getBodyItemValue(doc, "EVALUATIONUSER", true);
		String EvaluationComment = SMessageUtil.getBodyItemValue(doc, "EVALUATIONCOMMENT", true);
		String QAEvaluationComment=SMessageUtil.getBodyItemValue(doc, "QAEVALUATIONCOMMENT", true);
		String CumulativeTime 	 = SMessageUtil.getBodyItemValue(doc, "CUMULATIVETIME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("EvaluationBM", getEventUser(), getEventComment(), null, null);
		
		BM bmData = null;

		bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
		bmData.setBmState("Completed");
		bmData.setLastEventName(eventInfo.getEventName());
		bmData.setLastEventTime(eventInfo.getEventTime()); 
		bmData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		bmData.setQAEvaluationComment(QAEvaluationComment);
		bmData.setEvaluationComment(EvaluationComment);
		bmData.setCumulativeTime(CumulativeTime);

		try
		{
			bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0064", BMName);
		}
		
		//Set User Info 
		BMUser userData = null;
		String BMUserType = "Evaluation";
				
		userData = new BMUser(EvaluationUser, BMName, BMUserType);
		userData.setBmID(BMName);
		userData.setBmUser(EvaluationUser);
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
		
		return doc;
	}
}
