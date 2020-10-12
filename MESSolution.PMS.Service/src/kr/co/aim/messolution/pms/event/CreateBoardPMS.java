package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateBoardPMS extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		String userGroup=SMessageUtil.getBodyItemValue(doc, "USERGROUP", true);
		//String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String title = SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String comments = SMessageUtil.getBodyItemValue(doc, "COMMENTS", false);
		List<Element> scopeShopList = SMessageUtil.getBodySequenceItemList(doc, "SCOPESHOPLIST", false);
		
		String no = null;
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateBoardPMS", getEventUser(), getEventComment(), null, null);
			
			//Create No
			no = generateBoardNo();
			
			//Create
			BulletinBoard boardData = new BulletinBoard(userGroup, no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			//boardData.setDepartment(department);
			
			boardData = PMSServiceProxy.getBulletinBoardService().create(eventInfo, boardData);
			
			//CT_BULLETINBOARDAREA			
			if(scopeShopList != null)
			{
				for(Element eleshop : scopeShopList)
				{
					String scopeShopName = eleshop.getChildText("SCOPESHOPNAME");
					
					BulletinBoardArea boardAreaData = new BulletinBoardArea(scopeShopName, no);
					boardAreaData = PMSServiceProxy.getBulletinBoardAreaService().create(eventInfo, boardAreaData);
				}
			}
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

