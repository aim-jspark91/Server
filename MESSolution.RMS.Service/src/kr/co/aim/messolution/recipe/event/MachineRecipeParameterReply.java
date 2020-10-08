package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class MachineRecipeParameterReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);	
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQUENCE", false);
		
		List<Element> unitList = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", false);
		
		
		if (unitList != null)
		{
			//Insert Main Recipe
			MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, machineName, machineRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN, "-", "-", seq, "Y", "NG", doc);
			
			//Remove Recipe Relation
			MESRecipeServiceProxy.getRecipeServiceImpl().removeMachineRecipeRelation(eventInfo, machineName, machineRecipeName, unitList, doc);
			
			//Create Unit Recipe
			for (Element eUnit : unitList)
			{
				String unitName = SMessageUtil.getChildText(eUnit, "UNITNAME", true);
				String unitRecipeName = SMessageUtil.getChildText(eUnit, "UNITRECIPENAME", true);
				
				//Create Unit Recipe
				MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, unitName, unitRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, "", "", "", "-", "", doc);
			}
			
			
			//Create Recipe Relation
			List<String> changeRecipeList = new ArrayList<String>(); 
			for (Element eUnit : unitList)
			{
				String unitName = SMessageUtil.getChildText(eUnit, "UNITNAME", true);
				String unitRecipeName = SMessageUtil.getChildText(eUnit, "UNITRECIPENAME", true);
				
				RecipeRelation newRelationData = MESRecipeServiceProxy.getRecipeServiceImpl().createRecipeRelation(eventInfo, machineName, machineRecipeName, unitName, unitRecipeName, doc);
				
				if(newRelationData != null && newRelationData.getLastEventTimeKey().equals(eventInfo.getEventTimeKey()))
				{
					changeRecipeList.add("Create Relation["+newRelationData.getChildMachineRecipe()+"]");
				}
			}
			
			if(!changeRecipeList.isEmpty())
			{
				MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-REL-CH", machineName, machineRecipeName, changeRecipeList, null);
			}
		}
		
		GenericServiceProxy.getESBServive().sendReplyBySenderByRMS(getOriginalSourceSubjectName(), doc, "OICSender");
		
	}
}
