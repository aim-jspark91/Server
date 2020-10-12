package kr.co.aim.messolution.pms.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BulletinBoardAreaService extends CTORMService<BulletinBoardArea> {
	
	public static Log logger = LogFactory.getLog(BulletinBoardAreaService.class);
	
	private final String historyEntity = "";
	
	public List<BulletinBoardArea> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<BulletinBoardArea> result = super.select(condition, bindSet, BulletinBoardArea.class);
		
		return result;
	}
	
	public BulletinBoardArea selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(BulletinBoardArea.class, isLock, keySet);
	}
	
	public BulletinBoardArea create(EventInfo eventInfo, BulletinBoardArea dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BulletinBoardArea dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public BulletinBoardArea modify(EventInfo eventInfo, BulletinBoardArea dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
