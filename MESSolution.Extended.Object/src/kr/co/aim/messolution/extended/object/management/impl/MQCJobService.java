package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCJobService extends CTORMService<MQCJob> {
	
	public static Log logger = LogFactory.getLog(MQCJobService.class);
	
	private final String historyEntity = "MQCJobHist";
	
	public List<MQCJob> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MQCJob> result = new ArrayList<MQCJob>();
		
		try
		{
			result = super.select(condition, bindSet, MQCJob.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MQCJob selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCJob.class, isLock, keySet);
	}
	
	public MQCJob create(EventInfo eventInfo, MQCJob dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MQCJob dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MQCJob modify(EventInfo eventInfo, MQCJob dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public List<MQCJob> selectMQCJobListByCarrier(String carrierName){
		String condition = "WHERE CARRIERNAME=?";
		Object[] bindSet = new Object[] {carrierName};
		List<MQCJob> MQCJobList = this.select(condition,bindSet);
		return MQCJobList;
	}
	
	public MQCJob selectMQCJobWhereJobStateisExecuting(String factoryName,String processFlowName,String lotName){
		String condition = " WHERE 1=1 AND LOTNAME = ? AND FACTORYNAME  = ? AND PROCESSFLOWNAME = ? AND MQCSTATE = ? ";
		Object[] bindSet = new Object[]{lotName,factoryName,processFlowName,"Executing"};
		return this.select(condition, bindSet).get(0);
	}
}
