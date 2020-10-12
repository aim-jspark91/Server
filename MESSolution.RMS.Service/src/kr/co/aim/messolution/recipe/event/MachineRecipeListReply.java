package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class MachineRecipeListReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);	
		
        String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
        
        List<Element> eRecipeList = SMessageUtil.getBodySequenceItemList(doc, "MACHINERECIPELIST", true);
        
        //Remove Machine Recipe
        MESRecipeServiceProxy.getRecipeServiceImpl().removeMachineRecipe(eventInfo, machineName, eRecipeList);
        
        //Insert machine Recipe
        for(Element recipe : eRecipeList)
        {
			String machineRecipeName = SMessageUtil.getChildText(recipe, "MACHINERECIPENAME", true);
			
			MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, machineName, machineRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN, "-", "-", "","N", "NG", doc);
        }
		
		GenericServiceProxy.getESBServive().sendReplyBySenderByRMS(getOriginalSourceSubjectName(), doc, "OICSender");
		
	}	
}
