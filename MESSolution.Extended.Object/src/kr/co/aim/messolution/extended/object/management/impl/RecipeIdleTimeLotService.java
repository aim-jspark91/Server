package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTimeLot;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeIdleTimeLotService extends CTORMService<RecipeIdleTimeLot> {
	
	public static Log logger = LogFactory.getLog(RecipeIdleTimeLotService.class);
	
	private final String historyEntity = "RecipeIdleTimeLotHist";
	
	public List<RecipeIdleTimeLot> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<RecipeIdleTimeLot> result = new ArrayList<RecipeIdleTimeLot>();
		
		try
		{
			result = super.select(condition, bindSet, RecipeIdleTimeLot.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public RecipeIdleTimeLot selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RecipeIdleTimeLot.class, isLock, keySet);
	}
	
	public RecipeIdleTimeLot create(EventInfo eventInfo, RecipeIdleTimeLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeIdleTimeLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public RecipeIdleTimeLot modify(EventInfo eventInfo, RecipeIdleTimeLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
