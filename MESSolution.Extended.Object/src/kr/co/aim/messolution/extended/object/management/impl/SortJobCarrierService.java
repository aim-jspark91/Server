package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SortJobCarrierService extends CTORMService<SortJobCarrier> {
	
	public static Log logger = LogFactory.getLog(SortJobCarrierService.class);
	
	private final String historyEntity = "SortJobCarrierHist";
	
	public List<SortJobCarrier> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SortJobCarrier> result = super.select(condition, bindSet, SortJobCarrier.class);
		
		return result;
	}
	
	public SortJobCarrier selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SortJobCarrier.class, isLock, keySet);
	}
	
	public SortJobCarrier create(EventInfo eventInfo, SortJobCarrier dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SortJobCarrier dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SortJobCarrier modify(EventInfo eventInfo, SortJobCarrier dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
}
