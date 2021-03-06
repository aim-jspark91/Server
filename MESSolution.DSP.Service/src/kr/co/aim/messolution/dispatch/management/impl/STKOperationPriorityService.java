package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.STKOperationPriority;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class STKOperationPriorityService extends CTORMService<STKOperationPriority> {
	
	public static Log logger = LogFactory.getLog(STKOperationPriorityService.class);
	
	private final String historyEntity = "STKOPERATIONPRIORITYHIST";
	
	public List<STKOperationPriority> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<STKOperationPriority> result = new ArrayList<STKOperationPriority>();
		
		try
		{
			result = super.select(condition, bindSet, STKOperationPriority.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public STKOperationPriority selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(STKOperationPriority.class, isLock, keySet);
	}
	
	public STKOperationPriority create(EventInfo eventInfo, STKOperationPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, STKOperationPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public STKOperationPriority modify(EventInfo eventInfo, STKOperationPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo,this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
