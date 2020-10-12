package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class UnitRecipeListReply extends AsyncHandler {

    @Override
    public void doWorks(Document doc) throws CustomException {
        
    	String machineName = null;
        String unitName = null;
		
		
		String factoryCode = StringUtil.EMPTY;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);

	    /* 20181101, hhlee, add, Set Reply MessageName by DEFAULT_FACTORY ==>> */
        if(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY.equals("MOD"))
        {
          factoryCode = "M_";
        }
        else if(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY.equals("OLED"))
        {
          factoryCode = "E_";
        }
        else
        {
          factoryCode = "A_";
        }
        /* <<== 20181101, hhlee, add, Set Reply MessageName by DEFAULT_FACTORY */
        
		machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);             
		unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
        
		List<Element> eUnitRecipeList = SMessageUtil.getBodySequenceItemList(doc, "UNITRECIPELIST", false);
		if(eUnitRecipeList.size() > 0)
		{		
			//Remove Recipe Relation
			try
			{
				
				List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? AND CHILDMACHINE = ? ", new Object[]{machineName, "-", unitName});
				
				for(RecipeRelation recipeRelation : recipeRelationList)
				{
					boolean removeFlag = false;
					
					for (Element eSubUnit : eUnitRecipeList)
					{
						String unitRecipeName = SMessageUtil.getChildText(eSubUnit, "UNITRECIPENAME", true);
						
						if(recipeRelation.getChildMachine().equals(unitName) && recipeRelation.getChildMachineRecipe().equals(unitRecipeName))
						{
							removeFlag = false;
							break;
						}
						else
						{
							removeFlag = true;
						}
						
					}
					
					if(removeFlag)
					{
						eventInfo.setEventName("Remove");
						ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelation);
					}
					
					
				}
			}
			catch(Exception ex)
			{

			}
			
			
			List<String> changeRecipeList = new ArrayList<String>(); 
			for (Element eSubUnit : eUnitRecipeList)
			{
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				String unitRecipeName = SMessageUtil.getChildText(eSubUnit, "UNITRECIPENAME", true);
				
				String machineRecipeName = "-";
				
				/* Unit Recipe Insert */
				String newCompareFlag = "N";
				String newCompareResult = "";
				String newActiveResult = "NG";
				
				MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, unitName, unitRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, newCompareFlag, newCompareResult, "", "-", newActiveResult, doc);
				
				
				try
				{
					RecipeRelation recipeRelation = ExtendedObjectProxy.getRecipeRelationService().selectByKey(false, new Object[]{machineName, machineRecipeName, unitName, unitRecipeName});
					
					if(recipeRelation == null)
					{
						RecipeRelation recipeRelation2 = new RecipeRelation(machineName, machineRecipeName, unitName, unitRecipeName);
						
						eventInfo.setEventName("Create");
						
						recipeRelation2.setLastEventName(eventInfo.getEventName());
						recipeRelation2.setLastEventTime(eventInfo.getEventTime());
						recipeRelation2.setLastEventTimeKey(eventInfo.getEventTimeKey());
						recipeRelation2.setLastEventUser(eventInfo.getEventUser());
						recipeRelation2.setLastEventComment(eventInfo.getEventComment());
						
						ExtendedObjectProxy.getRecipeRelationService().create(eventInfo, recipeRelation2);
						
						changeRecipeList.add("Create Relation["+recipeRelation2.getChildMachineRecipe()+"]");
					}
				}
				catch(Exception ex)
				{
					RecipeRelation recipeRelation = new RecipeRelation(machineName, machineRecipeName, unitName, unitRecipeName);
					
					eventInfo.setEventName("Create");
					
					recipeRelation.setLastEventName(eventInfo.getEventName());
					recipeRelation.setLastEventTime(eventInfo.getEventTime());
					recipeRelation.setLastEventTimeKey(eventInfo.getEventTimeKey());
					recipeRelation.setLastEventUser(eventInfo.getEventUser());
					recipeRelation.setLastEventComment(eventInfo.getEventComment());
					
					ExtendedObjectProxy.getRecipeRelationService().create(eventInfo, recipeRelation);
					
					changeRecipeList.add("Create Relation["+recipeRelation.getChildMachineRecipe()+"]");
				}
			}
			
			if(!changeRecipeList.isEmpty())
			{
				MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-REL-CH", machineName, "-", changeRecipeList, null);
			}
		}

		
		GenericServiceProxy.getESBServive().sendReplyBySenderByRMS(getOriginalSourceSubjectName(), doc, "OICSender");
    }
}

