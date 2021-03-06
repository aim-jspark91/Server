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

public class ModifyHelpDesk extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), null, null);
		
		String dept = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String workDate = SMessageUtil.getBodyItemValue(doc, "WORKDATE", true);
	    String dayNight = SMessageUtil.getBodyItemValue(doc, "DAYNIGHT", true);
	    String workUser = SMessageUtil.getBodyItemValue(doc, "WORKUSER", true);
	    String telephone = SMessageUtil.getBodyItemValue(doc, "TELEPHONE", false);
	    String email = SMessageUtil.getBodyItemValue(doc, "EMAIL", false);
	    String cellphone = SMessageUtil.getBodyItemValue(doc, "CELLPHONE", false);
	    
	    
	    //check user group privilege
	  	UserProfile userProfile = MESUserServiceProxy.getUserProfileServiceUtil().getUser(getEventUser());
	  	String userGroupName = userProfile.getUserGroupName();
	  	
//	  	if (!userGroupName.equals("CIM") && !userGroupName.equals("Administrator"))
//	  		throw new CustomException("USER-0005", getEventUser(), userGroupName);
	  	
	  	
	  	try
		{
	  		//helpDesk data
	  		HelpDesk helpDeskInfo = ExtendedObjectProxy.getHelpDeskService().selectByKey(false, new Object[] {dept, workDate, dayNight, workUser});
		  	helpDeskInfo.setTelePhone(telephone);
		  	helpDeskInfo.setEmail(email);
		  	helpDeskInfo.setCellphone(cellphone);
		  	
		  	try
		  	{
		  		//modify
				ExtendedObjectProxy.getHelpDeskService().modify(eventInfo, helpDeskInfo);
		  	}
		  	catch (Exception ex)
		  	{
		  		throw new CustomException("SYS-9999", "System", ex.getMessage());
		  	}
		}
		catch(Exception e)
		{
			throw new CustomException("HELPDESK-0002", workUser);
		}
	  	
		return doc;
	}
}
