package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleReserve;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SampleReserveService extends CTORMService<SampleReserve> {
	
	public static Log logger = LogFactory.getLog(SampleReserveService.class);
	
	private final String historyEntity = "SampleReserveHist";
	
	public List<SampleReserve> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<SampleReserve> result = super.select(condition, bindSet, SampleReserve.class);
		
		return result;
	}
	
	public SampleReserve selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SampleReserve.class, isLock, keySet);
	}
	
	public SampleReserve create(EventInfo eventInfo, SampleReserve dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SampleReserve dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SampleReserve modify(EventInfo eventInfo, SampleReserve dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void deleteSampleLotState(SampleReserve sampleReserveInfo)
			throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleReserveService().delete(sampleReserveInfo);
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}
}
