package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.HelpDesk;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HelpDeskService extends CTORMService<HelpDesk> {
	
	public static Log logger = LogFactory.getLog(HelpDeskService.class);
	
	private final String historyEntity = "";
	
	public List<HelpDesk> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<HelpDesk> result = super.select(condition, bindSet, HelpDesk.class);
		
		return result;
	}
	
	public HelpDesk selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(HelpDesk.class, isLock, keySet);
	}
	
	public HelpDesk create(EventInfo eventInfo, HelpDesk dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, HelpDesk dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public HelpDesk modify(EventInfo eventInfo, HelpDesk dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
