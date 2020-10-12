package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class MachineRecipeCheckRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
		String machineRecipeName = StringUtil.EMPTY;
		Recipe recipeInfo = null;
		Machine machineData = null;
		
		String factoryCode = StringUtil.EMPTY;

		try
		{

			/* Machine Recipe Check */
			machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
			
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
			
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"MachineRecipeCheckReply");

			SMessageUtil.setBodyItemValue(doc, "RECIPECHECKRESULT", "NG",true);
			SMessageUtil.setBodyItemValue(doc, "RECIPECHECKCOMMENT", "",true);
			
			try
			{
				
				Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{machineName, machineRecipeName});
				
				MESRecipeServiceProxy.getRecipeServiceUtil().checkRMSCompareResult(machineName, machineRecipeName, false);
				
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
