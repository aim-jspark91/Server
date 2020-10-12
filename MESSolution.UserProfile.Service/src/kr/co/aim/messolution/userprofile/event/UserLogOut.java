package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.jdom.Document;

public class UserLogOut extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String uiName = SMessageUtil.getBodyItemValue(doc, "UINAME", true);
		String userId = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String workStationName = SMessageUtil.getBodyItemValue(doc, "WORKSTATIONNAME", true);
		
		MESUserServiceProxy.getUserProfileServiceImpl().logOut(userId, uiName, workStationName);
	}
}
