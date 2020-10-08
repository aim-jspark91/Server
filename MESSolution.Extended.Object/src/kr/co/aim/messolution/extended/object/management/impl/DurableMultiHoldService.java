package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DurableMultiHold;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DurableMultiHoldService extends CTORMService<DurableMultiHold> {
	
	public static Log logger = LogFactory.getLog(DurableMultiHoldService.class);
	
	private final String historyEntity = "";
	
	public List<DurableMultiHold> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<DurableMultiHold> result = super.select(condition, bindSet, DurableMultiHold.class);
		
		return result;
	}
	
	public DurableMultiHold selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(DurableMultiHold.class, isLock, keySet);
	}
	
	public DurableMultiHold create(EventInfo eventInfo, DurableMultiHold dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, DurableMultiHold dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public DurableMultiHold modify(EventInfo eventInfo, DurableMultiHold dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
