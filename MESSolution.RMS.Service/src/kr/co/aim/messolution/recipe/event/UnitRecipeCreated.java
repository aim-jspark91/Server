package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
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
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnitRecipeCreated extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

        String machineName = null;
        String unitName = null;
		String unitRecipeName = null;
		String seq = null;
		
		String onLineInitialCommState = StringUtil.EMPTY;
		
		String factoryCode = StringUtil.EMPTY;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		List<String> changeParamList = new ArrayList<String>();
		
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
            
			List<Element> eSubUnitList = SMessageUtil.getBodySequenceItemList(doc, "SUBUNITLIST", false);
			
			machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
            Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);            
            onLineInitialCommState = CommonUtil.getValue(machineData.getUdfs(), "ONLINEINITIALCOMMSTATE");
            
			unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", true);
			seq = SMessageUtil.getBodyItemValue(doc, "SEQUENCE", false);
			
			
			
			Machine unitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
			
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
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
			
			
			/* Unit Recipe Insert */
			String newCompareFlag = "N";
			String newCompareResult = "";
			String newActiveResult = "NG";
			
			
			if(eSubUnitList.size() > 0)
			{
				newCompareFlag = "-";
				newCompareResult = "-";
				newActiveResult = "-";
				
				//2019.05.22 dmlee : if Unit Recipe Only 'Y' Case Active Result NG
				try
				{
					
					if(machineSpecData.getUdfs().get("RMSUNITRECIPEONLY").equals("Y") || machineSpecData.getUdfs().get("CONSTRUCTTYPE").equals("TRACK"))
					{
						newCompareFlag = "-";
						newCompareResult = "-";
						newActiveResult = "NG";
						
					}
				}
				catch(Exception ex)
				{
					
				}
			}
			
			Recipe recipeData = MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, unitName, unitRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, newCompareFlag, newCompareResult, seq, "-", newActiveResult, doc);
			
			String oldCompareResultR = recipeData.getCompareResult();
			
			
			//2018.08.20 dmlee : If Track Machine OR Unit Recipe Only Flag = 'Y'
			if(machineSpecData.getUdfs().get("CONSTRUCTTYPE").equals("TRACK") || machineSpecData.getUdfs().get("RMSUNITRECIPEONLY").equals("Y"))
			{
				String machineRecipeName = "-";
				
				//Remove Recipe Relation
				try
				{
					
					List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{machineName, machineRecipeName});
					
					for(RecipeRelation recipeRelation : recipeRelationList)
					{
						boolean removeFlag = false;
						
						if(recipeRelation.getChildMachine().equals(unitName) && recipeRelation.getChildMachineRecipe().equals(unitRecipeName))
						{
							removeFlag = false;
						}
						else
						{
							removeFlag = true;
						}
						
						
						if(removeFlag)
						{
							//eventInfo.setEventName("Remove");
							//ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelation);
						}
					}
				}
				catch(Exception ex)
				{

				}
				
				try
				{
					RecipeRelation recipeRelation = ExtendedObjectProxy.getRecipeRelationService().selectByKey(false, new Object[]{machineName, machineRecipeName, unitName, unitRecipeName});
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
					
					List<String> changeRecipeList = new ArrayList<String>();
					changeRecipeList.add("Create Relation["+recipeRelation.getChildMachineRecipe()+"]");
					
					//2019.11.14 dmlee : This Logic is not necessary
					//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-REL-CH", machineName, machineRecipeName, changeRecipeList, null);
				}
			}
			//2018.08.20 dmlee : ---------------
			
			//Remove Unit Recipe Relation
			//Exist SubUnit Recipe
			if(eSubUnitList.size() > 0)
			{		
				//2019.02.14 dmlee : Remove RecipeParameter
				try
				{
					List<RecipeParameter> recipeParameterList = ExtendedObjectProxy.getRecipeParamService().getRecipeParameterList(unitName, unitRecipeName);
					
					for(RecipeParameter recipeParam : recipeParameterList)
					{
						eventInfo.setEventName("Remove");
						ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, recipeParam);
					}
				}
				catch(Exception ex)
				{

				}
				//2019.02.14 dmlee : -----------------------
				
				MESRecipeServiceProxy.getRecipeServiceImpl().removeUnitRecipeRelation(eventInfo, unitName, unitRecipeName, eSubUnitList, doc);
			
				for (Element eSubUnit : eSubUnitList)
				{
					String subUnitName = SMessageUtil.getChildText(eSubUnit, "SUBUNITNAME", true);
					String subUnitRecipeName = SMessageUtil.getChildText(eSubUnit, "SUBUNITRECIPENAME", true);
					
					MESRecipeServiceProxy.getRecipeServiceImpl().createRecipeRelation(eventInfo, unitName, unitRecipeName, subUnitName, subUnitRecipeName, doc);
				}
			}
			
			
			/* Unit Recipe Param Insert (Unit Recipe Param) */
			List<Element> eRecipeParamList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARALIST", false);
			
			//Exist Recipe Param
			if(eRecipeParamList.size() > 0)
			{
				// 2019.02.14 dmlee : Remove Recipe Relation
				try
				{
					
					List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{unitName, unitRecipeName});
					
					for(RecipeRelation recipeRelation : recipeRelationList)
					{
						eventInfo.setEventName("Remove");
						ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelation);
					}
				}
				catch(Exception ex)
				{

				}
				// 2019.02.14 dmlee : --------------------------
				
				/* Remove Recipe Param */
				MESRecipeServiceProxy.getRecipeServiceImpl().removeRecipeParameter(eventInfo, unitName, unitRecipeName, eRecipeParamList, doc);
				
				String allCompareResult = "OK";
				boolean changeCompreResultFlag = false;
				boolean changeActiveResultFlag = false;
				
				for (Element eRecipeParam : eRecipeParamList)
				{
					String recipeParaName = SMessageUtil.getChildText(eRecipeParam, "RECIPEPARANAME", true);
					String recipeParaValue = SMessageUtil.getChildText(eRecipeParam, "RECIPEPARAVALUE", false);
					
					try
					{
						RecipeParameter recipeParamData = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[]{unitName, unitRecipeName, recipeParaName});
						
						if(recipeParamData.getParameterState().equals("Removed"))
						{
							recipeParamData.setParameterState("Created");
							
							eventInfo.setEventName("Create");
							
							recipeParamData.setLastEventName(eventInfo.getEventName());
							recipeParamData.setLastEventTime(eventInfo.getEventTime());
							recipeParamData.setLastEventTimeKey(eventInfo.getEventTimeKey());
							recipeParamData.setLastEventUser(eventInfo.getEventUser());
							recipeParamData.setLastEventComment(eventInfo.getEventComment());
							
							recipeParamData = ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParamData);
						}
						
						if(!recipeParamData.getValue().equals(recipeParaValue))
						{
							String oldCompareResult = recipeParamData.getCompareResult();
							String oldValue = recipeParamData.getValue();
							
							recipeParamData.setValue(recipeParaValue);
							recipeParamData.setCompareResult("");
							recipeParamData.setCurrentChangeValue("Y");
							
							eventInfo.setEventName("Change");
							
							recipeParamData.setLastEventName(eventInfo.getEventName());
							recipeParamData.setLastEventTime(eventInfo.getEventTime());
							recipeParamData.setLastEventTimeKey(eventInfo.getEventTimeKey());
							recipeParamData.setLastEventUser(eventInfo.getEventUser());
							recipeParamData.setLastEventComment(eventInfo.getEventComment());
							
							String compareResult = "";
							
							if(MESRecipeServiceProxy.getRecipeServiceUtil().compareCheckParameter(recipeParamData))
							{
								compareResult = "OK";
								
								changeParamList.add("ChangeParam[ParamName:("+recipeParamData.getRecipeParameterName()+") ParamValue:("+recipeParamData.getValue()+") CompareResult:(OK) OldParamValue:("+oldValue+") OldCompareResult:("+oldCompareResult+") ] ");
							}
							else
							{
								if(!recipeParamData.getValidationType().equals("None"))
								{
									compareResult = "NG";
									allCompareResult = "NG";
									
									changeParamList.add("ChangeParam[ParamName:("+recipeParamData.getRecipeParameterName()+") ParamValue:("+recipeParamData.getValue()+") CompareResult:(NG) OldParamValue:("+oldValue+") OldCompareResult:("+oldCompareResult+") ] ");
								}
								else //if None
								{
									compareResult = "";
									
									changeParamList.add("ChangeParam[ParamName:("+recipeParamData.getRecipeParameterName()+") ParamValue:("+recipeParamData.getValue()+") CompareResult:(-) OldParamValue:("+oldValue+") OldCompareResult:("+oldCompareResult+") ] ");
								}
							}
							
							if(!oldCompareResult.equals(compareResult) && !recipeParamData.getValidationType().equals("None"))
							{
								changeCompreResultFlag = true;
							}
							
							recipeParamData.setCompareResult(compareResult);
							
							ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParamData);
							
							if(!recipeParamData.getValidationType().equals("None") && (StringUtils.isEmpty(recipeParamData.getResetActiveFlag()) || compareResult.equals("NG")) )
							{
								if(changeActiveResultFlag == false)
								{
									MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, unitName, unitRecipeName, "NG", doc);
									changeActiveResultFlag = true;
								}
							}
							

						}
					}
					catch(Exception ex)
					{
						RecipeParameter recipeParamData = new RecipeParameter(unitName, unitRecipeName, recipeParaName);
						recipeParamData.setValue(recipeParaValue);
						recipeParamData.setParameterState("Created");
						
						recipeParamData.setValidationType("None");
						
						eventInfo.setEventName("Create");
						
						recipeParamData.setLastEventName(eventInfo.getEventName());
						recipeParamData.setLastEventTime(eventInfo.getEventTime());
						recipeParamData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						recipeParamData.setLastEventUser(eventInfo.getEventUser());
						recipeParamData.setLastEventComment(eventInfo.getEventComment());
						
						try
						{
							ExtendedObjectProxy.getRecipeParamService().create(eventInfo, recipeParamData);
							
							if(changeActiveResultFlag == false)
							{
								MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, unitName, unitRecipeName, "NG", doc);
								changeActiveResultFlag = true;
							}
							
							allCompareResult = "NG";
						}
						catch(Exception ex2)
						{
							
						}
					}
				}
				
				if(changeCompreResultFlag)
				{
					MESRecipeServiceProxy.getRecipeServiceUtil().compareUnitRecipeParameterAll(eventInfo, unitName, unitRecipeName, allCompareResult);
					
					//2019.01.18 dmlee : Send RMS Mail
					if(oldCompareResultR.equals("OK") && !allCompareResult.equals("OK"))
					{
						MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", unitName, unitRecipeName, null, changeParamList);
					}
					else if(oldCompareResultR.equals("NG") && allCompareResult.equals("OK"))
					{
						MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-OK", unitName, unitRecipeName, null, changeParamList);
					}
					//2019.01.18 dmlee : --------------
				}
			}
			
			
			/**
			 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
			 * ====================================================================
			 */
			if(StringUtil.isNotEmpty(onLineInitialCommState))
			{
				try
				{
					SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"UnitRecipeCreatedCheckReply");

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
				SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"UnitRecipeCreatedCheckReply");

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
