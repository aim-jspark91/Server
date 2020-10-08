package kr.co.aim.messolution.generic.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeButtonRight extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String menuName = SMessageUtil.getBodyItemValue(doc, "MENUNAME", true);
		String buttonName = SMessageUtil.getBodyItemValue(doc, "BUTTONNAME", true);
	    List<Element> groupList = SMessageUtil.getBodySequenceItemList(doc, "USERGROUPNAMELIST", false);
	    List<Element> userList = SMessageUtil.getBodySequenceItemList(doc, "USERIDLIST", false);
	    
	    
	    Map<String, Object> bindMap = new HashMap<String, Object>();
	    bindMap.put("MENUNAME", menuName);
	    bindMap.put("BUTTONTAG", buttonName);
	    // Delete CT_USERGROUPBUTTON
	    String deleteGroupSql ="DELETE FROM CT_USERGROUPBUTTON A WHERE A.MENUNAME=:MENUNAME AND A.BUTTONTAG=:BUTTONTAG ";
	    GenericServiceProxy.getSqlMesTemplate().update(deleteGroupSql, bindMap);
	    // Delete CT_USERPROFILEBUTTON
	    String deleteUserSql ="DELETE FROM CT_USERPROFILEBUTTON A WHERE A.MENUNAME=:MENUNAME AND A.BUTTONTAG=:BUTTONTAG ";
	    GenericServiceProxy.getSqlMesTemplate().update(deleteUserSql, bindMap);
	    
	    // Insert CT_USERGROUPBUTTON
    	String insertGroupSql ="INSERT INTO CT_USERGROUPBUTTON (USERGROUPNAME,MENUNAME,BUTTONTAG) VALUES (:USERGROUPNAME,:MENUNAME,:BUTTONTAG) ";
		for (Element group : groupList)
		{
			String groupName = SMessageUtil.getChildText(group, "USERGROUPNAME", true);
			bindMap.put("USERGROUPNAME", groupName);
			GenericServiceProxy.getSqlMesTemplate().update(insertGroupSql, bindMap);
		}
	    
	    // Insert CT_USERPROFILEBUTTON
    	String insertUserSql ="INSERT INTO CT_USERPROFILEBUTTON A (A.MENUNAME,A.BUTTONTAG,A.USERID) VALUES (:MENUNAME,:BUTTONTAG,:USERID) ";
		for (Element user : userList)
		{
			String userId = SMessageUtil.getChildText(user, "USERID", true);
			bindMap.put("USERID", userId);
			GenericServiceProxy.getSqlMesTemplate().update(insertUserSql, bindMap);
		}
	    
		return doc;
	}

}
