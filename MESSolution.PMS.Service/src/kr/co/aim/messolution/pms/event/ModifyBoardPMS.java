package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyBoardPMS extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String userGroup = SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);
		//String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String title = SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String comments = SMessageUtil.getBodyItemValue(doc, "COMMENTS", true);
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBoardPMS", getEventUser(), getEventComment(), null, null);
			
			//Modify
			BulletinBoard boardData = new BulletinBoard(userGroup, no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			PMSServiceProxy.getBulletinBoardService().modify(eventInfo, boardData);
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}

		return doc;
	}
}