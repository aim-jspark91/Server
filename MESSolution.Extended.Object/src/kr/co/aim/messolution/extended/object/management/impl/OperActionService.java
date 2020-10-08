package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.OperAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperActionService extends CTORMService<OperAction> {
	
	public static Log logger = LogFactory.getLog(OperActionService.class);
	
	private final String historyEntity = "OperActionHist";
	
	public List<OperAction> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<OperAction> result = super.select(condition, bindSet, OperAction.class);
		
		return result;
	}
	
	public OperAction selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(OperAction.class, isLock, keySet);
	}
	
	public OperAction create(EventInfo eventInfo, OperAction dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, OperAction dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public OperAction modify(EventInfo eventInfo, OperAction dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
