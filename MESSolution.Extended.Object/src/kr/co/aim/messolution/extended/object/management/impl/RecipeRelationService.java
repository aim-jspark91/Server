package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeRelationService extends CTORMService<RecipeRelation> {
	
	public static Log logger = LogFactory.getLog(RecipeRelationService.class);
	
	private final String historyEntity = "RecipeRelationHist";
	
	public List<RecipeRelation> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RecipeRelation> result = super.select(condition, bindSet, RecipeRelation.class);
		
		return result;
	}
	
	public RecipeRelation selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(RecipeRelation.class, isLock, keySet);
	}
	
	public RecipeRelation create(EventInfo eventInfo, RecipeRelation dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeRelation dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RecipeRelation modify(EventInfo eventInfo, RecipeRelation dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
