package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeIdleTimeService extends CTORMService<RecipeIdleTime> {
	
	public static Log logger = LogFactory.getLog(RecipeIdleTimeService.class);
	
	private final String historyEntity = "RecipeIdleTimeHist";
	
	public List<RecipeIdleTime> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<RecipeIdleTime> result = new ArrayList<RecipeIdleTime>();
		
		try
		{
			result = super.select(condition, bindSet, RecipeIdleTime.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public RecipeIdleTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RecipeIdleTime.class, isLock, keySet);
	}
	
	public RecipeIdleTime create(EventInfo eventInfo, RecipeIdleTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeIdleTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public RecipeIdleTime modify(EventInfo eventInfo, RecipeIdleTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
