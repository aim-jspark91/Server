package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.UserTableAccess;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserGroup;
import kr.co.aim.greentrack.user.management.data.UserGroupKey;

import org.jdom.Document;
import org.jdom.Element;

public class SettingUserTableAccess extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		
		String userName = SMessageUtil.getBodyItemValue(doc,"NAME", true);
		List<Element> addTableList = SMessageUtil.getBodySequenceItemList(doc, "ADDLIST", false);
		List<Element> delTableList = SMessageUtil.getBodySequenceItemList(doc, "DELLIST", false);
//		UserTableAccess dataInfo = new UserTableAccess();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SetUserTableAccess", getEventUser(), getEventComment(), "", "");
		
		for (Element ele : addTableList)
		{
			String TableName = SMessageUtil.getChildText(ele, "TABLENAME", true);
			UserTableAccess dataInfo = new UserTableAccess();
			dataInfo.setUSERNAME(userName);
			dataInfo.setTABLENAME(TableName);
			ExtendedObjectProxy.getUserTableAccessService().create(eventInfo, dataInfo);
		}
		
		for (Element ele : delTableList)
		{
			String TableName = SMessageUtil.getChildText(ele, "TABLENAME", true);
			UserTableAccess dataInfo = new UserTableAccess();
			dataInfo.setUSERNAME(userName);
			dataInfo.setTABLENAME(TableName);
			ExtendedObjectProxy.getUserTableAccessService().delete(dataInfo);
		}
			
		return doc;
	}

}
