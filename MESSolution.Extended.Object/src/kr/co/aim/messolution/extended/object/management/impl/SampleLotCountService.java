package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SampleLotCountService extends CTORMService<SampleLotCount> {

	public static Log logger = LogFactory.getLog(SampleLotCount.class);
	
	private final String historyEntity = "SampleLotCountHist";
	
	public List<SampleLotCount> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<SampleLotCount> result = super.select(condition, bindSet, SampleLotCount.class);
		
		return result;
	}
	
	public SampleLotCount selectByKey(boolean isLock, Object[] keySet)
			throws CustomException
	{
		return super.selectByKey(SampleLotCount.class, isLock, keySet);
	}
	
	public SampleLotCount create(EventInfo eventInfo, SampleLotCount dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SampleLotCount dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SampleLotCount modify(EventInfo eventInfo, SampleLotCount dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	
	//2018.02.22 dmlee : arrange For EDO 
	/*	
	public SampleLotCount insertSampleLotCount(EventInfo eventInfo, String factoryName, String productSpecName,
									 String processFlowName, String processOperationName, String machineName, String toProcessOperationName, 
									 String lotSampleCount, String currentLotCount, String totalLotCount)
		throws CustomException
	{
		try
		{			
			SampleLotCount countInfo = new SampleLotCount(factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName);
			countInfo.setLOTSAMPLECOUNT(lotSampleCount);
			countInfo.setCURRENTLOTCOUNT(currentLotCount);
			countInfo.setTOTALLOTCOUNT(totalLotCount);
			
			countInfo = ExtendedObjectProxy.getSampleLotCountService().create(eventInfo, countInfo);
			
			logger.info("Sampling count begins");
			
			return countInfo;
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-9999", "Sampling", "Count base generation failed");
		}
	}
	
	public SampleLotCount updateSampleLotCountData(EventInfo eventInfo, SampleLotCount countInfo,
													String lotSampleCount, String currentLotCount, String totalLotCount)
		throws CustomException 
	{
		try
		{
			countInfo.setLOTSAMPLECOUNT(lotSampleCount);
			countInfo.setCURRENTLOTCOUNT(currentLotCount);
			countInfo.setTOTALLOTCOUNT(totalLotCount);
			
			countInfo = ExtendedObjectProxy.getSampleLotCountService().modify(eventInfo, countInfo);
			
			logger.info("Sampling count update");
			
			return countInfo;
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-9999", "Sampling", "Count update failed");
		}
	}
	*/
	//dmlee
	
}
