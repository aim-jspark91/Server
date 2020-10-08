package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateRecipeIdleTime extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String areaName = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String idleTime = SMessageUtil.getBodyItemValue(doc, "IDLETIME", true);
		String validFlag = SMessageUtil.getBodyItemValue(doc, "VALIDFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
			
		RecipeIdleTime recipeIdleTimeData = null;
		
		try
		{
			recipeIdleTimeData = ExtendedObjectProxy.getRecipeIdleTimeService().selectByKey(false, new Object[] {machineName, recipeName});
		}
		catch (Exception ex)
		{
			recipeIdleTimeData = null;
		}
		
		if(recipeIdleTimeData != null)
		{
			throw new CustomException("RECIPE-0009", "");
		}
		
		recipeIdleTimeData = new RecipeIdleTime(machineName, recipeName);
		recipeIdleTimeData.setfactoryName(factoryName);
		recipeIdleTimeData.setareaName(areaName);
		recipeIdleTimeData.setidleTime(Long.valueOf(idleTime));
		recipeIdleTimeData.setvalidFlag(validFlag);
		recipeIdleTimeData.setlastEventUser(eventInfo.getEventUser());
		recipeIdleTimeData.setlastEventComment(eventInfo.getEventComment());
		recipeIdleTimeData.setlastEventTime(eventInfo.getEventTime());
		recipeIdleTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
		recipeIdleTimeData.setlastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getRecipeIdleTimeService().create(eventInfo, recipeIdleTimeData);
		
		return doc;
	}
}
