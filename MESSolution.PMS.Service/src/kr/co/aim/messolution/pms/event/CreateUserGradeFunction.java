package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.UserGradeFunction;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateUserGradeFunction extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String stateType        = SMessageUtil.getBodyItemValue(doc, "FORMNAME", true);
		String stateCode        = SMessageUtil.getBodyItemValue(doc, "STATECODE", true);
		String rank             = SMessageUtil.getBodyItemValue(doc, "RANK", true);
		String userGroup        = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);				
		String userID           = SMessageUtil.getBodyItemValue(doc, "USERID", true);	
		String formButtonName   = SMessageUtil.getBodyItemValue(doc, "FORMBUTTONNAME", true);
			
		EventInfo eventInfo     = EventInfoUtil.makeEventInfo("CreateUserGradeFunction", getEventUser(), getEventComment(), null, null);
				
		List<UserGradeFunction> userGradeFunctionCheck = null ;
			
		//Check Duplication
		try
		{			
			userGradeFunctionCheck = PMSServiceProxy.getUserGradeFunctionService().select("STATETYPE = ? AND STATECODE = ? AND RANK = ? AND DEPT = ? AND USERID = ? AND FUNCTIONNAME = ? ", 
					                                                        new Object[] {stateType,stateCode,rank,userGroup,userID,formButtonName});
		}
		catch(Exception ex)
		{
			throw new CustomException(ex);
		}
		
		if(userGradeFunctionCheck.size()>0)
		{
			throw new CustomException("PMSUserGradeFunction-0001", stateType, stateCode,rank,userGroup,userID,formButtonName);
		}
		
		//Create UserGradeFunction
		UserGradeFunction userGradeFunction = new UserGradeFunction(stateType,stateCode,rank,userGroup,userID,formButtonName);

		try
		{
			userGradeFunction = PMSServiceProxy.getUserGradeFunctionService().create(eventInfo, userGradeFunction);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMSUserGradeFunction-0002", stateType,stateCode,rank,userGroup,userID,formButtonName);
		}		
		return doc;
	}
}
