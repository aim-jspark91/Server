package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.UserGroupTableAccess;
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

public class SettingUserGroupTableAccess extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		
		String userGroupName = SMessageUtil.getBodyItemValue(doc,"NAME", true);
		List<Element> addTableList = SMessageUtil.getBodySequenceItemList(doc, "ADDLIST", false);
		List<Element> delTableList = SMessageUtil.getBodySequenceItemList(doc, "DELLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SetUserGroupTableAccess", getEventUser(), getEventComment(), "", "");
		
		for (Element ele : addTableList)
		{
			String TableName = SMessageUtil.getChildText(ele, "TABLENAME", true);
			UserGroupTableAccess dataInfo = new UserGroupTableAccess();
			dataInfo.setUSERGROUPNAME(userGroupName);
			dataInfo.setTABLENAME(TableName);
			ExtendedObjectProxy.getUserGroupTableAccessService().create(eventInfo, dataInfo);
		}
		
		for (Element ele : delTableList)
		{
			String TableName = SMessageUtil.getChildText(ele, "TABLENAME", true);
			UserGroupTableAccess dataInfo = new UserGroupTableAccess();
			dataInfo.setUSERGROUPNAME(userGroupName);
			dataInfo.setTABLENAME(TableName);
			ExtendedObjectProxy.getUserGroupTableAccessService().delete(dataInfo);
		}
			
		return doc;
	}

}
