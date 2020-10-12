package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeService extends CTORMService<Recipe> {
	
	public static Log logger = LogFactory.getLog(RecipeService.class);
	
	private final String historyEntity = "RecipeHist";
	
	public List<Recipe> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<Recipe> result = super.select(condition, bindSet, Recipe.class);
		
		return result;
	}
	
	public Recipe selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(Recipe.class, isLock, keySet);
	}
	
	public Recipe create(EventInfo eventInfo, Recipe dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Recipe dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Recipe modify(EventInfo eventInfo, Recipe dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * unavailable recipe becomes disable
	 * @author swcho
	 * @since 2015.06.29
	 * @param eventInfo
	 * @param machineName
	 * @param recipeName
	 * @param recipeType
	 * @return
	 * @throws CustomException
	 */
	public Recipe makeNotAvailable(EventInfo eventInfo, String machineName, String recipeName, String recipeType)
		throws CustomException
	{
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] {machineName, recipeName});
		
		if (StringUtil.isEmpty(recipeType))
		{//without message
			if (recipeData.getRecipeType().equalsIgnoreCase("MAIN"))
				recipeType = "E";
			else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT"))
				recipeType = "U";
			else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT"))
				recipeType = "S";
		}
				
		//history trace
		this.setHistory(recipeData, eventInfo);
		
		if (recipeData.getRecipeType().equalsIgnoreCase("MAIN") && recipeType.equalsIgnoreCase("E"))
		{//sequence re-arranged
			recipeData.setRecipeState("UnApproved");
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT") && recipeType.equalsIgnoreCase("U"))
		{//independent program parameter modified
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT") && recipeType.equalsIgnoreCase("S"))
		{//independent program parameter modified
		}
		else
		{
			throw new CustomException("MACHINE-8001", String.format("PPID[%s] has something wrong, please check it up", recipeName));
		}
		
		//manipulated recipe is invalid until verified
		
		recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		
		return recipeData;
	}
	
	/**
	 * get Recipe by key condition
	 * @author swcho
	 * @since 2016.11.23
	 * @param machineName
	 * @param recipeName
	 * @return
	 * @throws CustomException
	 * @throws NotFoundSignal
	 */
	public Recipe getRecipeInfo(String machineName, String recipeName) throws CustomException, NotFoundSignal
	{
		try
		{
			Recipe recipeData = this.selectByKey(false, new Object[] {machineName, recipeName});
			
			return recipeData;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "Recipe", ne.getMessage());
		}
	}
	
	public Recipe setHistory(Recipe recipeInfo, EventInfo eventInfo)
	{
		//history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		recipeInfo.setLastEventTime(eventInfo.getEventTime());
		
		return recipeInfo;
	}
}
