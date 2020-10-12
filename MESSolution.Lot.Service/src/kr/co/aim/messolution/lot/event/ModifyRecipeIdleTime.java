package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class ModifyRecipeIdleTime extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String newIdleTime = SMessageUtil.getBodyItemValue(doc, "NEWIDLETIME", false);
		String newValidFlag = SMessageUtil.getBodyItemValue(doc, "NEWVALIDFLAG", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		
			
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
			throw new CustomException("RECIPE-0009", "");
		}
		
/*		if(recipeIdleTimeData.getidleTime() ==  Integer.valueOf(newIdleTime) && StringUtil.equals(recipeIdleTimeData.getvalidFlag(), newValidFlag))
		{
			throw new CustomException("MQC-0006", "");
		}
*/		
		if(!StringUtil.isEmpty(newIdleTime))
		{
			recipeIdleTimeData.setidleTime(Long.valueOf(newIdleTime));
		}
		
		if(!StringUtil.isEmpty(newValidFlag))
		{
			recipeIdleTimeData.setvalidFlag(newValidFlag);
		}
		recipeIdleTimeData.setlastEventUser(eventInfo.getEventUser());
		recipeIdleTimeData.setlastEventComment(eventInfo.getEventComment());
		recipeIdleTimeData.setlastEventTime(eventInfo.getEventTime());
		recipeIdleTimeData.setlastEventTimekey(eventInfo.getEventTimeKey());
		recipeIdleTimeData.setlastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getRecipeIdleTimeService().modify(eventInfo, recipeIdleTimeData);
		
		return doc;
	}
}
