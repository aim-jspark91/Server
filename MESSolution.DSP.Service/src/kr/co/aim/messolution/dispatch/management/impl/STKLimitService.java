package kr.co.aim.messolution.dispatch.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.dispatch.management.data.STKLimit;
import kr.co.aim.messolution.extended.object.CTORMHeader;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CTORMHeader(tag = "CT",divider = "_")
public class STKLimitService extends CTORMService<STKLimit> {
	public static Log logger = LogFactory.getLog(STKLimitService.class);
	
	private final String historyEntity = "STKLimitHistory";
	
	public List<STKLimit> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<STKLimit> result = new ArrayList<STKLimit>();
		
		try
		{
			result = super.select(condition, bindSet, STKLimit.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public STKLimit selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(STKLimit.class, isLock, keySet);
	}
	
	public STKLimit create(EventInfo eventInfo, STKLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, STKLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public STKLimit modify(EventInfo eventInfo, STKLimit dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
