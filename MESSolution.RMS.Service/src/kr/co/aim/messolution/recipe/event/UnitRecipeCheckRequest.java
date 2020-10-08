package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class UnitRecipeCheckRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
        String unitName = StringUtil.EMPTY;
		String unitRecipeName = StringUtil.EMPTY;
		Recipe recipeInfo = null;
		Machine machineData = null;
		Machine unitData = null;
		

		String factoryCode = StringUtil.EMPTY;

		try
		{

			/* Machine Recipe Check */
			machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", true);

			/* Machine Validation */
			machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			if(machineData.getFactoryName().equals("MOD"))
			{
				factoryCode = "M_";
			}
			else if(machineData.getFactoryName().equals("OLED"))
			{
				factoryCode = "E_";
			}
			else
			{
				factoryCode = "A_";
			}
			
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"UnitRecipeCheckReply");

			SMessageUtil.setBodyItemValue(doc, "RECIPECHECKRESULT", "NG",true);
			SMessageUtil.setBodyItemValue(doc, "RECIPECHECKCOMMENT", "",true);

			/* Unit Validation */
			unitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

			try
			{
				Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{unitName, unitRecipeName});
				
				//Case 1. SubUnit Recipe Exist
				if(recipeData.getCompareResult().equals("-"))
				{
					List<RecipeRelation> recipeRelSubList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
					
					for(RecipeRelation recipeRelDataSub : recipeRelSubList)
					{
						Recipe subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelDataSub.getChildMachine(), recipeRelDataSub.getChildMachineRecipe()});
						
						if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getCompareResult().equals("NG"))
						{
							throw new CustomException("RMS-0002", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
						}
					}
				}
				//Case 2. Unit Recipe Only
				else
				{
					if(recipeData.getCompareFlag().equals("Y") && recipeData.getCompareResult().equals("NG"))
					{
						throw new CustomException("RMS-0002", recipeData.getMachineName(), recipeData.getRecipeName());
					}
				}
				
				SMessageUtil.setBodyItemValue(doc, "RECIPECHECKRESULT", "OK",true);

			}
			catch(Exception ex)
			{
				SMessageUtil.setBodyItemValue(doc, "RECIPECHECKRESULT", "NG",true);
				SMessageUtil.setBodyItemValue(doc, "RECIPECHECKCOMMENT", "Approved recipes do not exist.",true);
			}
		}
		catch(CustomException ce)
		{
			eventLog.warn(String.format("[%s]%s", ce.errorDef.getErrorCode(), ce.errorDef.getLoc_errorMessage()));
		}

		return doc;
	}
}
