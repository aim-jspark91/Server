package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeParameterService extends CTORMService<RecipeParameter> {
	
	public static Log logger = LogFactory.getLog(RecipeParameterService.class);
	
	private final String historyEntity = "RecipeParameterHist";
	
	public List<RecipeParameter> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RecipeParameter> result = super.select(condition, bindSet, RecipeParameter.class);
		
		return result;
	}
	
	public RecipeParameter selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(RecipeParameter.class, isLock, keySet);
	}
	
	public RecipeParameter create(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RecipeParameter modify(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	//2018.09.06 dmlee : Get Recipe Parameter
	public RecipeParameter getRecipeParameter(String machineName, String recipeName, String recipeParamName) throws CustomException
	{
		try
		{
			RecipeParameter recipeParamData = this.selectByKey(false, new Object[]{machineName, recipeName, recipeParamName});
			
			if(recipeParamData.getParameterState().equals("Removed"))
			{
				throw new CustomException();
			}
			
			return recipeParamData;
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
	
	//2018.09.06 dmlee : Get Recipe Parameter List
	public List<RecipeParameter> getRecipeParameterList(String machineName, String recipeName) throws CustomException
	{
		try
		{
			List<RecipeParameter> recipeParamData = this.select("WHERE MACHINENAME = ? AND RECIPENAME = ? AND PARAMETERSTATE <> ?", new Object[]{machineName, recipeName, "Removed"});
			
			return recipeParamData;
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
}
