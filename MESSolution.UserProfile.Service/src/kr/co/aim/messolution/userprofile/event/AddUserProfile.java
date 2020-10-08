package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.jdom.Document;

public class AddUserProfile extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userID = SMessageUtil.getBodyItemValue(doc,"USERID", true);
		String password = SMessageUtil.getBodyItemValue(doc,"PASSWORD", true);
		String userGroupName = SMessageUtil.getBodyItemValue(doc,"USERGROUPNAME", true);
		String userName = SMessageUtil.getBodyItemValue(doc,"USERNAME", true);
		String email = SMessageUtil.getBodyItemValue(doc,"EMAIL", true);
		String email2 = SMessageUtil.getBodyItemValue(doc,"EMAIL2", true);
		String department = SMessageUtil.getBodyItemValue(doc,"DEPARTMENT", true);
		String phoneNumber = SMessageUtil.getBodyItemValue(doc,"PHONENUMBER", true);

		UserProfile userProfile = new UserProfile();
		UserProfileKey userProfileKey = new UserProfileKey(userID);
		
		userProfile.setKey(userProfileKey);
		userProfile.setUserName(userName);
		userProfile.setUserGroupName(userGroupName);
		userProfile.setPassword(password);
		//userProfile.setEmail(email);		
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("DEPARTMENT", department);
		udfs.put("PHONENUMBER", phoneNumber);
		udfs.put("EMAIL",email+'@'+email2);
		userProfile.setUdfs(udfs);
		
		try
		{
			UserServiceProxy.getUserProfileService().addUserProfile(userID, userProfile);
		}
		catch(DuplicateNameSignal ds) // same userID check
		{
			throw new CustomException("USER-0008");
		}
		
		return doc;
	}

}
