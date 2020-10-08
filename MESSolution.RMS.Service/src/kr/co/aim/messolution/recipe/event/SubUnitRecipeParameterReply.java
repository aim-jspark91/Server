package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SubUnitRecipeParameterReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);	
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String subUnitRecipeName = SMessageUtil.getBodyItemValue(doc, "SUBUNITRECIPENAME", true);
		
		List<Element> recipeParaList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARALIST", false);
		
		List<String> changeParamList = new ArrayList<String>();
		
		Recipe recipeData = null;
		if(recipeParaList.size() > 0)
		{
			/* SubUnit Recipe Insert */
			recipeData = MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, subUnitName, subUnitRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT, "N", "", "", "-", "NG", doc);
			
			/* Remove Recipe Param */
			MESRecipeServiceProxy.getRecipeServiceImpl().removeRecipeParameter(eventInfo, subUnitName, subUnitRecipeName, recipeParaList, doc);
			
		}
		
		String oldCompareResultR = recipeData.getCompareResult();
		
		/* Insert Recipe Param */
		if(recipeParaList.size() > 0)
		{
			String allCompareResult = "OK";
			boolean changeCompreResultFlag = false;
			boolean changeActiveResultFlag = false;
			
			for (Element eRecipeParam : recipeParaList)
			{
				String recipeParaName = SMessageUtil.getChildText(eRecipeParam, "RECIPEPARANAME", true);
				String recipeParaValue = SMessageUtil.getChildText(eRecipeParam, "RECIPEPARAVALUE", false);
				
				try
				{
					RecipeParameter recipeParamData = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[]{subUnitName, subUnitRecipeName, recipeParaName});
					
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
								MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, subUnitName, subUnitRecipeName, "NG", doc);
								changeActiveResultFlag = true;
							}
						}
						
						
					}
				}
				catch(Exception ex)
				{
					RecipeParameter recipeParamData = new RecipeParameter(subUnitName, subUnitRecipeName, recipeParaName);
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
							MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, subUnitName, subUnitRecipeName, "NG", doc);
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
				MESRecipeServiceProxy.getRecipeServiceUtil().compareRecipeParameterAll(eventInfo, subUnitName, subUnitRecipeName, allCompareResult);
				
				//2019.01.18 dmlee : Send RMS Mail
				if(oldCompareResultR.equals("OK") && !allCompareResult.equals("OK"))
				{
					MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", subUnitName, subUnitRecipeName, null, changeParamList);
				}
				else if(oldCompareResultR.equals("NG") && allCompareResult.equals("OK"))
				{
					MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-OK", subUnitName, subUnitRecipeName, null, changeParamList);
				}
				//2019.01.18 dmlee : --------------
			}
			
		}
		
		GenericServiceProxy.getESBServive().sendReplyBySenderByRMS(getOriginalSourceSubjectName(), doc, "OICSender");
	}
}

