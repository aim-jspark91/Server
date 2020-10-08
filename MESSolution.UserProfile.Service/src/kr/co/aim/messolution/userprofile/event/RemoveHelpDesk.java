package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.HelpDesk;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.user.management.data.UserProfile;

import org.jdom.Document;

public class RemoveHelpDesk extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		
		String dept = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String workDate = SMessageUtil.getBodyItemValue(doc, "WORKDATE", true);
	    String dayNight = SMessageUtil.getBodyItemValue(doc, "DAYNIGHT", true);
	    String workUser = SMessageUtil.getBodyItemValue(doc, "WORKUSER", true);
	    
	    //check user group authority
	  	UserProfile userProfile = MESUserServiceProxy.getUserProfileServiceUtil().getUser(getEventUser());
	  	
	  	String userGroupName = userProfile.getUserGroupName();
	  	
	  	if (!userGroupName.equals("CIM") && !userGroupName.equals("Administrator"))
	  	{
	  		throw new CustomException("USER-0005", getEventUser(), userGroupName);
	  	}
	  	
	  	//prepare data
	  	HelpDesk helpDeskInfo = ExtendedObjectProxy.getHelpDeskService().selectByKey(false, new Object[] {dept, workDate, dayNight, workUser});
	    
	    //remove
	    try
	    {
	    	ExtendedObjectProxy.getHelpDeskService().remove(eventInfo, helpDeskInfo);
	    }
	  	catch (Exception ex)
	  	{
	  		throw new CustomException("SYS-9999", "System", ex.getMessage());
	  	}
	  	return doc;
	}

}
