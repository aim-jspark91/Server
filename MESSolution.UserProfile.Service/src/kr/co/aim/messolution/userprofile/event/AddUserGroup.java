package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.UserTableAccess;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


import org.jdom.Document;

public class AddUserGroup extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		
		String userName = SMessageUtil.getBodyItemValue(doc,"NAME", true);
		UserTableAccess dataInfo = new UserTableAccess();
		EventInfo eventInfo = new EventInfo();
		
		try
		{
			ExtendedObjectProxy.getUserTableAccessService().create(eventInfo, dataInfo);
		}
		catch(DuplicateNameSignal ex) //same data
		{
			throw new CustomException("USER-0008");
		}
		catch(FrameworkErrorSignal ex) //GreenTrack User Service error
		{
			throw new CustomException("USER-9999", ex.getMessage());
		}
			
		return doc;
	}

}
