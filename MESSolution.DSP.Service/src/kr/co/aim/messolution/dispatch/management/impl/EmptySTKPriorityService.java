package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.EmptySTKPriority;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class EmptySTKPriorityService extends CTORMService<EmptySTKPriority> {
	
	public static Log logger = LogFactory.getLog(EmptySTKPriorityService.class);
	
	private final String historyEntity = "EMPTYSTKPRIORITYHISTORY";
	
	public List<EmptySTKPriority> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<EmptySTKPriority> result = new ArrayList<EmptySTKPriority>();
		
		try
		{
			result = super.select(condition, bindSet, EmptySTKPriority.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public EmptySTKPriority selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(EmptySTKPriority.class, isLock, keySet);
	}
	
	public EmptySTKPriority create(EventInfo eventInfo, EmptySTKPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, EmptySTKPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public EmptySTKPriority modify(EventInfo eventInfo, EmptySTKPriority dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
