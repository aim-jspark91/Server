package kr.co.aim.messolution.userprofile.event;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserGroupKey;

import org.jdom.Document;

public class DeleteUserGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userGroupName = SMessageUtil.getBodyItemValue(doc,"USERGROUPNAME", true);
		UserGroupKey userGroupKey = new UserGroupKey(userGroupName);
		
		try
		{
			UserServiceProxy.getUserGroupService().delete(userGroupKey);
		}
		catch(Exception ex)
		{
			throw new CustomException("USER-0009");
		}
			
		return doc;
	}

}
