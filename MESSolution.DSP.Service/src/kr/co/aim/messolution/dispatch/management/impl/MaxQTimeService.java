package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaxQTimeService extends CTORMService<MaxQTime> {
	public static Log logger = LogFactory.getLog(EmptySTKBalanceService.class);
	
	private final String historyEntity = "MaxQTimeHistory";
	
	public List<MaxQTime> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaxQTime> result = new ArrayList<MaxQTime>();
		
		try
		{
			result = super.select(condition, bindSet, MaxQTime.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MaxQTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaxQTime.class, isLock, keySet);
	}
	
	public MaxQTime create(EventInfo eventInfo, MaxQTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaxQTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MaxQTime modify(EventInfo eventInfo, MaxQTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public MaxQTime insertMaxQTime(EventInfo eventInfo, String relationId, String factoryName, String fromProductSpecName, String fromProcessFlowName,
			String fromProcessOperationName, String toProcessOperationName, String toMachineName, String machineCapacity)
					throws CustomException
	{
		eventInfo.setEventName("Create");
		MaxQTime maxQTimeData = new MaxQTime();
		maxQTimeData.setRelationId(relationId);
		maxQTimeData.setFactoryName(factoryName);
		maxQTimeData.setFromProductSpecName(fromProductSpecName);
		maxQTimeData.setFromProcessFlowName(fromProcessFlowName);
		maxQTimeData.setFromProcessOperationName(fromProcessOperationName);
		maxQTimeData.setToProcessOperationName(toProcessOperationName);
		maxQTimeData.setToMachineName(toMachineName);
		maxQTimeData.setMachineCapacity(Long.parseLong(machineCapacity));
		
		maxQTimeData = MESDSPServiceProxy.getMaxQTimeService().create(eventInfo, maxQTimeData);
		
		return maxQTimeData;
	}
}
