package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

import org.jdom.Document;

public class ChangeShop extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String messageName = SMessageUtil.getMessageName(doc);
		//String sourceSubjectName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "ORIGINALSOURCESUBJECTNAME");
		//String transactionId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TRANSACTIONID");
		
		String factoryName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "FACTORYNAME");
		String areaName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "AREANAME");
		
		MESUserServiceProxy.getUserProfileServiceUtil().validateFactoryAccessible(factoryName, getEventUser(), ",");
		
		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().changeShop(factoryName, areaName, getEventUser(), messageName);
			
		}
		catch (Exception e) {
			//reference on non-standard error handling
			throw new CustomException(e);
		}
		
		//Element returnElement = MESUserServiceProxy.getUserProfileServiceUtil().createUserProfileElement(userId, password);
		
		//replace for reply
		//doc.getRootElement().getChild(SMessageUtil.Body_Tag).setContent(returnElement);
		
		return doc;
	}

}
