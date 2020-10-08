package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.jdom.Document;

public class DeleteUserProfile extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userID = SMessageUtil.getBodyItemValue(doc,"USERID", true);
				
		try
		{
			UserProfileKey userProfileKey = new UserProfileKey(userID);
			UserServiceProxy.getUserProfileService().delete(userProfileKey); //delete
			
		}
		catch(NotFoundSignal ds)
		{
			throw new CustomException("USER-0009");	//no data
		}
		catch(FrameworkErrorSignal ds)
		{
			 throw new CustomException("USER-9999", ds.getMessage());
		}
		
		return doc;
	}

}
