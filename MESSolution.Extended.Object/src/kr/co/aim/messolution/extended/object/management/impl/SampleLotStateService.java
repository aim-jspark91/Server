package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleLotState;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SampleLotStateService extends CTORMService<SampleLotState> {
	
	public static Log logger = LogFactory.getLog(SampleLotStateService.class);
	
	private final String historyEntity = "SampleLotStateHist";
	
	public List<SampleLotState> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<SampleLotState> result = super.select(condition, bindSet, SampleLotState.class);
		
		return result;
	}
	
	public SampleLotState selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SampleLotState.class, isLock, keySet);
	}
	
	public SampleLotState create(EventInfo eventInfo, SampleLotState dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SampleLotState dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SampleLotState modify(EventInfo eventInfo, SampleLotState dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void deleteSampleLotState(SampleLotState sampleLotStateInfo)
			throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleLotStateService().delete(sampleLotStateInfo);
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}
}
