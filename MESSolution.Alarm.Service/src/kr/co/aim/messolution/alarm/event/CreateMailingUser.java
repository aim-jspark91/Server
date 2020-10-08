package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateMailingUser extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		String system =  SMessageUtil.getBodyItemValue(doc, "SYSTEM", true);
		String alarmCode =  SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String userID =  SMessageUtil.getBodyItemValue(doc, "USERID", false);
		String email =  SMessageUtil.getBodyItemValue(doc, "EMAIL", false);
		String userGroupName =  SMessageUtil.getBodyItemValue(doc, "USERGROUPNAME", false);
		String machineName =  SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		
		MailingUser mailingUserData = null;
		try
		{
			if(userGroupName == "")
			{
				mailingUserData = new MailingUser(system, alarmCode, userID, "-");
				mailingUserData.setMailAddr(email);
			}
			
			if(userID == "")
			{
				mailingUserData = new MailingUser(system, alarmCode, "-", userGroupName);
				mailingUserData.setUserGroupName(userGroupName);
			}
			if(machineName != "")
			{
				mailingUserData.setMachineName(machineName);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("EMAIL-0001","");
		}
		ExtendedObjectProxy.getMailingUserService().create(eventInfo, mailingUserData);
		
		return doc;
	}

}
