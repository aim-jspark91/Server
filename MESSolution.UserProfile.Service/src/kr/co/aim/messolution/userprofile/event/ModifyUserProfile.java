package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.impl.EncryptUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyUserProfile extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userID = SMessageUtil.getBodyItemValue(doc,"USERID", true);
		String password = SMessageUtil.getBodyItemValue(doc,"PASSWORD", false);
		String userGroupName = SMessageUtil.getBodyItemValue(doc,"USERGROUPNAME", true);
		String userName = SMessageUtil.getBodyItemValue(doc,"USERNAME", true);
		String email = SMessageUtil.getBodyItemValue(doc,"EMAIL", true);
		String email2 = SMessageUtil.getBodyItemValue(doc,"EMAIL2", true);
		String department = SMessageUtil.getBodyItemValue(doc,"DEPARTMENT", true);
		String phoneNumber = SMessageUtil.getBodyItemValue(doc,"PHONENUMBER", true);
				
		// 2019.06.17_hsryu_Delete Logic. Mantis 0004179.
//		try
//		{
//			UserProfileKey userProfileKey = new UserProfileKey(userID);
//			UserServiceProxy.getUserProfileService().delete(userProfileKey); //delete
//			
//			UserProfile userProfile = new UserProfile();
//			
//			userProfile.setKey(userProfileKey);
//			userProfile.setUserName(userName);
//			userProfile.setUserGroupName(userGroupName);
//			userProfile.setPassword(password);
//			
//			
//			Map<String, String> udfs = new HashMap<String, String>();
//			udfs.put("DEPARTMENT", department);
//			udfs.put("PHONENUMBER", phoneNumber);
//			udfs.put("EMAIL",email+'@'+email2);
//			userProfile.setUdfs(udfs);
//			
//			UserServiceProxy.getUserProfileService().addUserProfile(userID, userProfile); //insert
//		}
//		catch(NotFoundSignal ds)
//		{
//			throw new CustomException("USER-0009");	//no data
//		}
//		catch(FrameworkErrorSignal ds)
//		{
//			 throw new CustomException("USER-9999", ds.getMessage());
//		}
		
		// 2019.06.17_hsryu_New Logic. Mantis 0004179.
		try
		{
			UserProfile userProfileData = MESUserServiceProxy.getUserProfileServiceUtil().getUser(userID);

			userProfileData.setUserGroupName(userGroupName);

			// 2019.06.17_hsryu_Mantis 0004179. if password is empty, not change password. 
			if(StringUtils.isNotEmpty(password)) {
				userProfileData.setPassword(EncryptUtil.encrypt(password));
			}
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("DEPARTMENT", department);
			udfs.put("PHONENUMBER", phoneNumber);
			udfs.put("EMAIL",email+'@'+email2);
			userProfileData.setUdfs(udfs);
			
			UserServiceProxy.getUserProfileService().update(userProfileData);
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
