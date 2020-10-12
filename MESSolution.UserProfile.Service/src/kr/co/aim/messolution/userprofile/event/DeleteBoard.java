package kr.co.aim.messolution.userprofile.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BulletinBoard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class DeleteBoard extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String no = SMessageUtil.getBodyItemValue(doc, "NO", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String scopeShopList = SMessageUtil.getBodyItemValue(doc, "SCOPESHOPLIST", true);
		
		try
		{	
			//Delete
			BulletinBoard boardData = new BulletinBoard(factoryName, no);
			ExtendedObjectProxy.getBulletinBoardService().delete(boardData);
			
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}

		return doc;
	}
}