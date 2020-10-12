package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserGrade;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateUserGrade extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String stateType   = SMessageUtil.getBodyItemValue(doc, "FORMNAME", true);
		String stateCode   = SMessageUtil.getBodyItemValue(doc, "STATECODE", true);
		String rank        = SMessageUtil.getBodyItemValue(doc, "RANK", true);
		String userGroup   = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);	
			
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreateUserGrade", getEventUser(), getEventComment(), null, null);
		
		UserGrade userGrade = new UserGrade(stateType,stateCode,rank,userGroup);

		try
		{
			userGrade = PMSServiceProxy.getUserGradeService().create(eventInfo, userGrade);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSUserGrade-0001", stateType,stateCode,rank,userGroup);
		}		
		return doc;
	}
}
