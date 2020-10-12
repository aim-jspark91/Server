package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BulletinBoard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyBoard extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String title = SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String comments = SMessageUtil.getBodyItemValue(doc, "COMMENTS", true);
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBoard", getEventUser(), getEventComment(), null, null);
			
			//Modify
			BulletinBoard boardData = new BulletinBoard(factoryName, no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			ExtendedObjectProxy.getBulletinBoardService().modify(eventInfo, boardData);
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}

		return doc;
	}
}