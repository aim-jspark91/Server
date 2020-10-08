package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;

import org.jdom.Document;



public class DeleteBoardPMS extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String showUserGroup = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);
		//String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String userGroupList = SMessageUtil.getBodyItemValue(doc, "USERGROUPLIST", true);
		
		//EventInfo eventInfo  = EventInfoUtil.makeEventInfo("DeleteBoardPMS", getEventUser(), getEventComment(), null, null);
		try
		{	
			//Delete
			BulletinBoard boardData = new BulletinBoard(showUserGroup, no);
			PMSServiceProxy.getBulletinBoardService().delete(boardData);
			
			//PMS_BULLETINBOARDAREA DELETE
			String[] userGroups = userGroupList.split(",");
			for (String userGroup : userGroups)
			{
				BulletinBoardArea boardAreaData = new BulletinBoardArea(userGroup, no);		
				PMSServiceProxy.getBulletinBoardAreaService().delete(boardAreaData);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}
	
		return doc;		
	}
}
