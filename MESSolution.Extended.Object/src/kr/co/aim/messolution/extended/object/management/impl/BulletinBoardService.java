package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BulletinBoard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BulletinBoardService extends CTORMService<BulletinBoard> {
	
	public static Log logger = LogFactory.getLog(BulletinBoardService.class);
	
	private final String historyEntity = "";
	
	public List<BulletinBoard> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<BulletinBoard> result = super.select(condition, bindSet, BulletinBoard.class);
		
		return result;
	}
	
	public BulletinBoard selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(BulletinBoard.class, isLock, keySet);
	}
	
	public BulletinBoard create(EventInfo eventInfo, BulletinBoard dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BulletinBoard dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public BulletinBoard modify(EventInfo eventInfo, BulletinBoard dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
