package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Mailing;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailingService extends CTORMService<Mailing> {
	
	public static Log logger = LogFactory.getLog(MailingService.class);
	
	private final String historyEntity = "";
	
	public List<Mailing> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<Mailing> result = super.select(condition, bindSet, Mailing.class);
		
		return result;
	}
	
	public Mailing selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(Mailing.class, isLock, keySet);
	}
	
	public Mailing create(EventInfo eventInfo, Mailing dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Mailing dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Mailing modify(EventInfo eventInfo, Mailing dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
