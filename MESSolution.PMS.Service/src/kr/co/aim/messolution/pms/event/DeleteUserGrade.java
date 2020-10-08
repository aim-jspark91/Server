package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserGrade;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteUserGrade extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String stateType   = SMessageUtil.getBodyItemValue(doc, "FORMNAME", true);
		String stateCode   = SMessageUtil.getBodyItemValue(doc, "STATECODE", true);
		String rank        = SMessageUtil.getBodyItemValue(doc, "RANK", true);
		String userGroup   = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);	
			
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteUserGrade", getEventUser(), getEventComment(), null, null);
		
		//Check existance
		List<UserGrade> userGradeCheck = null ;
		
		try
		{			
			userGradeCheck = PMSServiceProxy.getUserGradeService().select("STATETYPE = ? AND STATECODE = ? AND RANK = ? AND DEPT = ? ", 
					                                                        new Object[] {stateType,stateCode,rank,userGroup});
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}
		
		if(userGradeCheck.size() == 0)
		{
			throw new CustomException("PMSUserGrade-0005", stateType, stateCode,rank,userGroup);
		}
		
		//Delete UserGrade
		UserGrade userGrade = new UserGrade(stateType,stateCode,rank,userGroup);

		try
		{					
		    PMSServiceProxy.getUserGradeService().remove(eventInfo, userGrade);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSUserGrade-0003", stateType,stateCode,rank,userGroup);
		}		
		return doc;
	}
}
