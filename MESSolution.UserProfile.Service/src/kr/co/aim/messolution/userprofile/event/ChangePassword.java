package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class ChangePassword extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String messageName = SMessageUtil.getMessageName(doc);
		//String sourceSubjectName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "ORIGINALSOURCESUBJECTNAME");
		//String transactionId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TRANSACTIONID");
		
		String userId = SMessageUtil.getBodyItemValue(doc,"USERID", true);
		String oldPassword = SMessageUtil.getBodyItemValue(doc,"OLDPASSWORD", true);
		String newPassword = SMessageUtil.getBodyItemValue(doc,"NEWPASSWORD", true);
		
		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().changePassword(userId, oldPassword, newPassword);
			
		}
		catch (Exception e) {
			//reference on non-standard error handling
			throw new CustomException(e);
		}
		
		Element returnElement = MESUserServiceProxy.getUserProfileServiceUtil().createUserProfileElement(userId,newPassword);
		
		//replace for reply
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).setContent(returnElement);
		
		return doc;
	}

}
