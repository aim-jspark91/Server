package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTimeLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DeleteRecipeIdleTime extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element recipeIdleTimeList = SMessageUtil.getBodySequenceItem(doc, "RECIPEIDLETIMELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		if(recipeIdleTimeList != null)
		{
			for(Object obj : recipeIdleTimeList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				String recipeName = SMessageUtil.getChildText(element, "RECIPENAME", true);
				
				RecipeIdleTime recipeIdleTimeData = null;
				
				try
				{
					recipeIdleTimeData = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
				}
				catch (Exception ex)
				{
					recipeIdleTimeData = null;
				}
				
				if(recipeIdleTimeData == null)
				{
					throw new CustomException("RECIPE-0010", "");
				}
				
				if(StringUtils.equals(recipeName, "*"))
				{
					List<RecipeIdleTimeLot> allRecipeIdleTimeLot = null;
					
					String condition = " WHERE machineName = ? ";
					Object[] bindSet = new Object[]{machineName};
					
					try
					{
						allRecipeIdleTimeLot = ExtendedObjectProxy.getRecipeIdleTimeLotService().select(condition, bindSet);
					}
					catch(Throwable e)
					{
						allRecipeIdleTimeLot = null;
						eventLog.info("Not exist RecipeIdleTimeLotData except RecipeName '*'.");
					}
					
					if(allRecipeIdleTimeLot!=null)
					{
						for(RecipeIdleTimeLot recipeIdleTimeLot : allRecipeIdleTimeLot)
						{
							try
							{
								RecipeIdleTime IdleTimeData = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {recipeIdleTimeLot.getmachineName(), recipeIdleTimeLot.getrecipeName()});
							}
							catch(Throwable e)
							{
								ExtendedObjectProxy.getRecipeIdleTimeLotService().remove(eventInfo, recipeIdleTimeLot);
							}
						}
					}
				}
				else
				{
					List<RecipeIdleTimeLot> allRecipeIdleTimeLot = null;
					
					String condition = " WHERE machineName = ? AND recipeName = ? ";
					Object[] bindSet = new Object[]{machineName, recipeName};

					try
					{
						allRecipeIdleTimeLot = ExtendedObjectProxy.getRecipeIdleTimeLotService().select(condition, bindSet);
					}
					catch(Throwable e)
					{
						allRecipeIdleTimeLot = null;
						eventLog.info("Not exist RecipeIdleTimeLotData except RecipeName '*'.");
					}
					
					if(allRecipeIdleTimeLot!=null)
					{
						for(RecipeIdleTimeLot recipeIdleTimeLot : allRecipeIdleTimeLot)
						{
							ExtendedObjectProxy.getRecipeIdleTimeLotService().remove(eventInfo, recipeIdleTimeLot);
						}
					}
				}
					
				ExtendedObjectProxy.getRecipeIdleTimeService().remove(eventInfo, recipeIdleTimeData);
			}
		}
		
		return doc;
	}
}
