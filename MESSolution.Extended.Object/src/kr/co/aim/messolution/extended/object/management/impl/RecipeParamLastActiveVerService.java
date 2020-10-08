package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParamLastActiveVer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeParamLastActiveVerService extends CTORMService<RecipeParamLastActiveVer> {
	
	public static Log logger = LogFactory.getLog(RecipeParamLastActiveVerService.class);
	
	private final String historyEntity = "";
	
	public List<RecipeParamLastActiveVer> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RecipeParamLastActiveVer> result = super.select(condition, bindSet, RecipeParamLastActiveVer.class);
		
		return result;
	}
	
	public RecipeParamLastActiveVer selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(RecipeParamLastActiveVer.class, isLock, keySet);
	}
	
	public RecipeParamLastActiveVer create(EventInfo eventInfo, RecipeParamLastActiveVer dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeParamLastActiveVer dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RecipeParamLastActiveVer modify(EventInfo eventInfo, RecipeParamLastActiveVer dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	


	
}
