package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserGroup;
import kr.co.aim.greentrack.user.management.data.UserGroupKey;

import org.jdom.Document;

public class ModifyUserGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userGroupName = SMessageUtil.getBodyItemValue(doc,"USERGROUPNAME", true);
		String accessFactoryName = SMessageUtil.getBodyItemValue(doc,"ACCESSFACTORYNAME", false);
		String description = SMessageUtil.getBodyItemValue(doc,"DESCRIPTION", true);
		
		
		try
		{
			UserGroupKey userGroupKey = new UserGroupKey(userGroupName);
			UserServiceProxy.getUserGroupService().delete(userGroupKey);
			
			
			UserGroup userGroup = new UserGroup();
			userGroup.setKey(userGroupKey);
			userGroup.setDescription(description);
			
			if(accessFactoryName != null)
			{
			// Put data into UDF
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("ACCESSFACTORY", accessFactoryName);
			userGroup.setUdfs(udfs);
			}
			
			UserServiceProxy.getUserGroupService().addGroup(userGroupName, userGroup);	
			
		}
	
		catch(NotFoundSignal ex)
		{
			//no data
			throw new CustomException("USER-0009"); 
		}
			
		return doc;
	}

}
