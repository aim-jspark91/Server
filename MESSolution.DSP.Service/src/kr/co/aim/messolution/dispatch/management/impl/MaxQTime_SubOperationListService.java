package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_SubOperationList;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaxQTime_SubOperationListService extends CTORMService<MaxQTime_SubOperationList> {
	public static Log logger = LogFactory.getLog(MaxQTime_SubOperationListService.class);
	
	private final String historyEntity = "MQTIME_SUBOPERATIONHIST";
	
	public List<MaxQTime_SubOperationList> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaxQTime_SubOperationList> result = new ArrayList<MaxQTime_SubOperationList>();
		
		try
		{
			result = super.select(condition, bindSet, MaxQTime_SubOperationList.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MaxQTime_SubOperationList selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaxQTime_SubOperationList.class, isLock, keySet);
	}
	
	public MaxQTime_SubOperationList create(EventInfo eventInfo, MaxQTime_SubOperationList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaxQTime_SubOperationList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MaxQTime_SubOperationList modify(EventInfo eventInfo, MaxQTime_SubOperationList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public MaxQTime_SubOperationList insetmaxQTime_SubOperationListData(EventInfo eventInfo, String relationId, String processOperationName)
		throws CustomException
	{
		eventInfo.setEventName("Create");
		MaxQTime_SubOperationList maxQTime_SubOperationListData = new MaxQTime_SubOperationList();
		maxQTime_SubOperationListData.setRelationId(relationId);
		maxQTime_SubOperationListData.setProcessOperationName(processOperationName);
		
		maxQTime_SubOperationListData = MESDSPServiceProxy.getMaxQTime_SubOperationListService().create(eventInfo, maxQTime_SubOperationListData);
		
		return maxQTime_SubOperationListData;
	}
}
