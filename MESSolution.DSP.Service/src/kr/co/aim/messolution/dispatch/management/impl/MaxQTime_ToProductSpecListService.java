package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.MaxQTime_ToProductSpecList;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaxQTime_ToProductSpecListService extends CTORMService<MaxQTime_ToProductSpecList> {
	public static Log logger = LogFactory.getLog(MaxQTime_ToProductSpecListService.class);
	
	private final String historyEntity = "MQTIME_TOPRODUCTSPECHIST";
	
	public List<MaxQTime_ToProductSpecList> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaxQTime_ToProductSpecList> result = new ArrayList<MaxQTime_ToProductSpecList>();
		
		try
		{
			result = super.select(condition, bindSet, MaxQTime_ToProductSpecList.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public MaxQTime_ToProductSpecList selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaxQTime_ToProductSpecList.class, isLock, keySet);
	}
	
	public MaxQTime_ToProductSpecList create(EventInfo eventInfo, MaxQTime_ToProductSpecList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaxQTime_ToProductSpecList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public MaxQTime_ToProductSpecList modify(EventInfo eventInfo, MaxQTime_ToProductSpecList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public MaxQTime_ToProductSpecList insertMaxQTime_ToProductSpecList(EventInfo eventInfo, String relationId, String productSpecName)
			throws CustomException
	{
		eventInfo.setEventName("Create");
		MaxQTime_ToProductSpecList maxQTime_ToProductSpecListData = new MaxQTime_ToProductSpecList();
		maxQTime_ToProductSpecListData.setRelationId(relationId);
		maxQTime_ToProductSpecListData.setProductSpecName(productSpecName);
		
		maxQTime_ToProductSpecListData = MESDSPServiceProxy.getMaxQTime_ToProductSpecListService().create(eventInfo, maxQTime_ToProductSpecListData);
		
		return maxQTime_ToProductSpecListData;
	}
}
