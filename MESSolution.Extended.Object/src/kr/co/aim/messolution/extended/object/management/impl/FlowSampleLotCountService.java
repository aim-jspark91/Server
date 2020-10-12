package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlowSampleLotCountService extends CTORMService<FlowSampleLotCount> {

	public static Log logger = LogFactory.getLog(FlowSampleLotCount.class);
	
	private final String historyEntity = "";
	
	/*try
	{
		
	}
	catch(greenFrameDBErrorSignal ne)
	{
		if (ne.getErrorCode().equals("NotFoundSignal"))
			throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
		else
			throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
	}*/
	
	public List<FlowSampleLotCount> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FlowSampleLotCount> result = super.select(condition, bindSet, FlowSampleLotCount.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
		}
	}
	
	public FlowSampleLotCount selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FlowSampleLotCount.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
		}
	}
	
	public FlowSampleLotCount create(EventInfo eventInfo, FlowSampleLotCount dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FlowSampleLotCount dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
		}
	}
	
	public FlowSampleLotCount modify(EventInfo eventInfo, FlowSampleLotCount dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLotCount", ne.getMessage());
		}
	}
	
	/**
	 * create initial sampling count
	 * @author swcho
	 * @since 2016.12.17
	 * @param eventInfo
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @param toProcessFlowName
	 * @param toProcessOperationName
	 * @param lotSampleCount
	 * @param currentLotCount
	 * @param totalLotCount
	 * @return
	 * @throws CustomException
	 * @throws NotFoundSignal
	 */
	public FlowSampleLotCount insertCount(EventInfo eventInfo, String factoryName, String productSpecName,
										String processFlowName, String processOperationName, String machineName, String toProcessFlowName, String toProcessOperationName, 
										long lotSampleCount, long currentLotCount, long totalLotCount)
		throws CustomException, NotFoundSignal
	{
		FlowSampleLotCount countInfo = new FlowSampleLotCount(factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessFlowName, toProcessOperationName);
		
		countInfo.setLOTSAMPLECOUNT(String.valueOf(lotSampleCount));
		countInfo.setCURRENTLOTCOUNT(String.valueOf(currentLotCount));
		countInfo.setTOTALLOTCOUNT(String.valueOf(totalLotCount));
		
		countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().create(eventInfo, countInfo);
		
		logger.info("Sampling count begins");
		
		return countInfo;
	}
	
	/**
	 * update sampling count
	 * @author swcho
	 * @since 2016.12.17
	 * @param eventInfo
	 * @param countInfo
	 * @param lotSampleCount
	 * @param currentLotCount
	 * @param totalLotCount
	 * @return
	 * @throws CustomException
	 */
	public FlowSampleLotCount updateCount(EventInfo eventInfo, FlowSampleLotCount countInfo,
										long lotSampleCount, long currentLotCount, long totalLotCount)
		throws CustomException 
	{
		countInfo.setLOTSAMPLECOUNT(String.valueOf(lotSampleCount));
		countInfo.setCURRENTLOTCOUNT(String.valueOf(currentLotCount));
		countInfo.setTOTALLOTCOUNT(String.valueOf(totalLotCount));
		
		countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().modify(eventInfo, countInfo);
		
		logger.info("Sampling count update");
		
		return countInfo;
	}
	
	/**
	 * sampling rule counting
	 * @since 2016.12.19
	 * @author swcho
	 * @param eventInfo
	 * @param lotData
	 * @param rule
	 * @return
	 * @throws CustomException
	 */
	public FlowSampleLotCount calculateSamplingCount(EventInfo eventInfo, Lot lotData, ListOrderedMap rule) throws CustomException
	{
		String factoryName = CommonUtil.getValue(rule, "FACTORYNAME");
		String productSpecName = CommonUtil.getValue(rule, "PRODUCTSPECNAME");
		String fromFlowName = CommonUtil.getValue(rule, "PROCESSFLOWNAME");
		String fromOperationName = CommonUtil.getValue(rule, "PROCESSOPERATIONNAME");
		String fromMachineName = CommonUtil.getValue(rule, "MACHINENAME");
		String toFlowName = CommonUtil.getValue(rule, "TOPROCESSFLOWNAME");
		String toOperationName = CommonUtil.getValue(rule, "TOPROCESSOPERATIONNAME");
		String lotSamplingCount = CommonUtil.getValue(rule, "LOTSAMPLINGCOUNT");
		
		FlowSampleLotCount countInfo;
		
		//find existing sampling count by rule
		try
		{
			countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().selectByKey(true,
														new Object[]{factoryName, productSpecName,
																	 fromFlowName, fromOperationName, fromMachineName,
																	 toFlowName, toOperationName});
		}
		catch (NotFoundSignal ne)
		{
			logger.debug("Not found flow sampling count");
			
			//generate base count if not exists
			eventInfo.setEventName("Create");
			countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().insertCount(eventInfo, factoryName, productSpecName,
																						fromFlowName, fromOperationName, fromMachineName, toFlowName, toOperationName,
																						0, 0, 0);
		}
		
		long lotSampleCount = 0;
		long currentLotCount = 0;
		long totalLotCount = 0;
		
		try
		{
			lotSampleCount = Long.parseLong(lotSamplingCount);
			currentLotCount = Long.parseLong(countInfo.getCURRENTLOTCOUNT());
			totalLotCount = Long.parseLong(countInfo.getTOTALLOTCOUNT());
			
			if(currentLotCount % lotSampleCount == 0)
			{
				currentLotCount = 1;
				totalLotCount++;
			}
			else
			{
				currentLotCount = currentLotCount + 1;
				totalLotCount++;
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage());
			throw new CustomException("SYS-9999", "Sampling", "Sampling counting failed");
		}
		
		eventInfo.setEventName("Increase");
		countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().updateCount(eventInfo, countInfo, lotSampleCount, currentLotCount, totalLotCount);
		
		return countInfo;
	}
}
