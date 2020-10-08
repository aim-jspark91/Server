package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeIdleTimeLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ResetRecipeIdleTime extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element recipeIdleTimeList = SMessageUtil.getBodySequenceItem(doc, "RECIPEIDLETIMELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reset", this.getEventUser(), this.getEventComment(), "", "");
		
		if(recipeIdleTimeList != null)
		{
			for(Object obj : recipeIdleTimeList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				String recipeName = SMessageUtil.getChildText(element, "RECIPENAME", true);
				String productSpecName = SMessageUtil.getChildText(element, "PRODUCTSPECNAME", true);
				String processOperationName = SMessageUtil.getChildText(element, "PROCESSOPERATIONNAME", true);
				
				RecipeIdleTimeLot recipeIdleTimeLotData = null;
				
				try
				{
					recipeIdleTimeLotData = ExtendedObjectProxy.getRecipeIdleTimeLotService().selectByKey(false, new Object[] {machineName, recipeName});
				}
				catch (Exception ex)
				{
					recipeIdleTimeLotData = null;
				}
				
				if(recipeIdleTimeLotData == null)
				{
					throw new CustomException("RECIPE-0010", "");
				}
				
				recipeIdleTimeLotData.setlastRunTime(eventInfo.getEventTime());
				recipeIdleTimeLotData.setfirstLotFlag("");
				recipeIdleTimeLotData.setfirstCstID("");
				recipeIdleTimeLotData.setfirstLotID("");
				recipeIdleTimeLotData.setlastEventUser(eventInfo.getEventUser());
				recipeIdleTimeLotData.setlastEventTime(eventInfo.getEventTime());
				recipeIdleTimeLotData.setlastEventTimekey(eventInfo.getEventTimeKey());
				recipeIdleTimeLotData.setlastEventName(eventInfo.getEventName());
				recipeIdleTimeLotData.setlastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getRecipeIdleTimeLotService().modify(eventInfo, recipeIdleTimeLotData);
			}
		}
		
		return doc;
	}
}
