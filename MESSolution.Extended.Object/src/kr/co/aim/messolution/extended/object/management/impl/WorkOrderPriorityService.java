package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.WorkOrderPriority;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WorkOrderPriorityService extends CTORMService<WorkOrderPriority> {
public static Log logger = LogFactory.getLog(WorkOrderPriority.class);
	
	private final String historyEntity = "WorkOrderPriorityHist";
	
	public List<WorkOrderPriority> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<WorkOrderPriority> result = super.select(condition, bindSet, WorkOrderPriority.class);
		
		return result;
	}
	
	public WorkOrderPriority selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(WorkOrderPriority.class, isLock, keySet);
	}
	
	public WorkOrderPriority create(EventInfo eventInfo, WorkOrderPriority dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, WorkOrderPriority dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public WorkOrderPriority modify(EventInfo eventInfo, WorkOrderPriority dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
