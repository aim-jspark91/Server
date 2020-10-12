package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MailingUser;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class RemoveMailingUser extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		
		String system =  SMessageUtil.getBodyItemValue(doc, "SYSTEM", true);
		String alarmCode =  SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String userID =  SMessageUtil.getBodyItemValue(doc, "USERID", false);
		String userGroupName =  SMessageUtil.getBodyItemValue(doc, "USERGROUPNAME", false);
		
		MailingUser mailingUserData = null;
		try
		{
			mailingUserData = ExtendedObjectProxy.getMailingUserService().selectByKey(false, new Object[]{system,alarmCode,userID,userGroupName});
		}
		catch(Exception ex)
		{
			throw new CustomException("EMAIL-0002","");
		}
		
		ExtendedObjectProxy.getMailingUserService().remove(eventInfo, mailingUserData);
		
		return doc;
	}

}
