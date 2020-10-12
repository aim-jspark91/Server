package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class SubUnitRecipeRemoved extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
        String unitName = StringUtil.EMPTY;
        String subunitName = StringUtil.EMPTY;
		String subunitRecipeName = StringUtil.EMPTY;
		String currentCommunicationName = StringUtil.EMPTY;
		Recipe recipeInfo = null;
		Machine machineData = null;
		Machine unitData = null;
		Machine subunitData = null;
		
		String factoryCode = StringUtil.EMPTY;

		String onLineInitialCommState = StringUtil.EMPTY;
		
		try
		{
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
            
			/* Unit Recipe Delete */
			machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			/* Machine Validation */
            machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            onLineInitialCommState = CommonUtil.getValue(machineData.getUdfs(), "ONLINEINITIALCOMMSTATE");
            currentCommunicationName = machineData.getCommunicationState();
            
			unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
			subunitRecipeName = SMessageUtil.getBodyItemValue(doc, "SUBUNITRECIPENAME", true);

			/* 20181101, hhlee, delete, When SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true) is Error, send messagename is A_RecipePermissionVerifyRequest ==>> */
            //if(machineData.getFactoryName().equals("MOD"))
            //{
            //  factoryCode = "M_";
            //}
            //else if(machineData.getFactoryName().equals("OLED"))
            //{
            //  factoryCode = "E_";
            //}
            //else
            //{
            //  factoryCode = "A_";
            //}
            /* <<== 20181101, hhlee, delete, When SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true) is Error, send messagename is A_RecipePermissionVerifyRequest */
			
			
			/* Unit Validation */
			unitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

			/* SubUnit Validation */
			subunitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
			
			//Remove RecipeData
			try
			{
				Recipe recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(subunitName, subunitRecipeName);
				ExtendedObjectProxy.getRecipeService().remove(eventInfo, recipeData);
			}
			catch(Exception ex)
			{
				
			}
			
			//Remove Recipe Relation Data
			try
			{
				List<RecipeRelation> recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{subunitName, subunitRecipeName});
				
				for(RecipeRelation recipeRelData : recipeRelList)
				{
					MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, recipeRelData.getParentMachine(), recipeRelData.getParentMachineRecipe(), "NG", doc);
		     //Because OLED ISSUE:5846 RMS BC上报RecipeRemoved，MES需要不删除Recipe的Relation关系 Modified By JHY On 2020.05.11
			//		ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelData);
				}
			}
			catch(Exception ex)
			{
				
			}
			
			try
			{
				List<RecipeParameter> recipeParamList = ExtendedObjectProxy.getRecipeParamService().getRecipeParameterList(subunitName, subunitRecipeName);
				
				if(recipeParamList.size() > 0)
				{
					for(RecipeParameter recipeParamRelData : recipeParamList)
					{
						recipeParamRelData.setParameterState("Removed");
						ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParamRelData);
					}
				}
			}
			catch(Exception ex)
			{
				
			}

			/**
			 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
			 * ====================================================================
			 */
			if(StringUtil.isNotEmpty(onLineInitialCommState))
			{
				try
				{
					SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"SubUnitRecipeRemovedCheckReply");

					/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
					SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

					//GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "EISSender");
                    GenericServiceProxy.getESBServive().recordMessagelogAftersendBySender(getOriginalSourceSubjectName(), doc, "EISSender");
				}
				catch(Exception ex)
				{
					eventLog.warn(String.format("[%s]%s", getOriginalSourceSubjectName(), "EISSender Send Failed!"));
				}
			}
		}
		catch(Exception ex)
		{
		    if(StringUtil.isNotEmpty(onLineInitialCommState))
			{
				SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"SubUnitRecipeRemovedCheckReply");

				/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG",true);
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

				handleSyncAsyncFault(doc, getOriginalSourceSubjectName(), ex);
			}
		    else
            {
                handleFault(doc, getReplySubjectName(), ex);
            }
		}
	}
}
