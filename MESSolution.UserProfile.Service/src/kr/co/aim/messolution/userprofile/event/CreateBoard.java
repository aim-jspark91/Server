package kr.co.aim.messolution.userprofile.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BulletinBoard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateBoard extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String title = SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String comments = SMessageUtil.getBodyItemValue(doc, "COMMENTS", false);
		List<Element> scopeShopList = SMessageUtil.getBodySequenceItemList(doc, "SCOPESHOPLIST", false);
		
		String no = null;
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateBoard", getEventUser(), getEventComment(), null, null);
			
			//Create No
			no = generateBoardNo();
			
			//Create
			BulletinBoard boardData = new BulletinBoard(factoryName, no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			ExtendedObjectProxy.getBulletinBoardService().create(eventInfo, boardData);
			
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}
		
		//return No
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "NO", no);

		return rtnDoc;
	}
	
	public static String generateBoardNo() throws CustomException
	{
		try
		{
			String currentTime = TimeUtils.getCurrentEventTimeKey();
			String no = currentTime.substring(2, 14) + currentTime.substring(17);
			
			return no;
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}
	}
}