package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelationLastActiveVer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeRelationLastActiveVerService extends CTORMService<RecipeRelationLastActiveVer> {
	
	public static Log logger = LogFactory.getLog(RecipeRelationLastActiveVerService.class);
	
	private final String historyEntity = "";
	
	public List<RecipeRelationLastActiveVer> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RecipeRelationLastActiveVer> result = super.select(condition, bindSet, RecipeRelationLastActiveVer.class);
		
		return result;
	}
	
	public RecipeRelationLastActiveVer selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(RecipeRelationLastActiveVer.class, isLock, keySet);
	}
	
	public RecipeRelationLastActiveVer create(EventInfo eventInfo, RecipeRelationLastActiveVer dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeRelationLastActiveVer dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RecipeRelationLastActiveVer modify(EventInfo eventInfo, RecipeRelationLastActiveVer dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
