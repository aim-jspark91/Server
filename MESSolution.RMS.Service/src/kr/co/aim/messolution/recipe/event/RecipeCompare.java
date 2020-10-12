package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RecipeCompare extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Compare", getEventUser(), getEventComment(), null, null);
		
		String eqMachineName = SMessageUtil.getBodyItemValue(doc, "EQMACHINENAME", false);
		String eqRecipeName = SMessageUtil.getBodyItemValue(doc, "EQRECIPENAME", false);
		String unitMachineName = SMessageUtil.getBodyItemValue(doc, "UNITMACHINENAME", true);
		String unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", true);
		String subUnitMachineName = SMessageUtil.getBodyItemValue(doc, "SUNITMACHINENAME", false);
		String subUnitRecipeName = SMessageUtil.getBodyItemValue(doc, "SUNITRECIPENAME", false);
		
		//MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(eqMachineName);
		
		Recipe recipeData = null;
		//Unit Recipe
		if(StringUtil.isEmpty(subUnitMachineName))
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{unitMachineName, unitRecipeName});
		}
		//SubUnit Recipe
		else
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{subUnitMachineName, subUnitRecipeName});
		}
		
		String oldComparesult = recipeData.getCompareResult();
		
		//Validation CheckOut
		//Check In Case
		if(recipeData.getCheckState().equals("CheckedIn"))
		{
			throw new CustomException("RMS-0009", recipeData.getMachineName(),recipeData.getRecipeName());
		}
		//Check Out Case
		else
		{
			if(!recipeData.getCheckOutUser().equals(eventInfo.getEventUser()))
			{
				UserProfile userData = UserServiceProxy.getUserProfileService().selectByKey(new UserProfileKey(eventInfo.getEventUser()));
				
				if(!userData.getUserGroupName().equals("Administrator"))
				{
					throw new CustomException("RMS-0010", recipeData.getCheckOutUser());
				}
			}
		}
		
		//Check In
		recipeData.setCheckState("CheckedIn");
		recipeData.setCheckOutTime(null);
		recipeData.setCheckOutUser("");
		
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTime(eventInfo.getEventTime());
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);

		
		List<Element> eParaList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARAMETERLIST", true);
		
		String allCompareResult = "OK";
		boolean allNoneFlag = false;
		
		eventLog.info("Check Param List Start...");
		for (Element ePara : eParaList)
		{
			String sRecipeParameterName = SMessageUtil.getChildText(ePara, "RECIPEPARAMETERNAME", true);
			String sValue = SMessageUtil.getChildText(ePara, "VALUE", true);
			String sValidationType = SMessageUtil.getChildText(ePara, "VALIDATIONTYPE", true);
			String sTarget = SMessageUtil.getChildText(ePara, "TARGET", false);
			String sLowerLimit = SMessageUtil.getChildText(ePara, "LOWERLIMIT", false);
			String sUpperLimit = SMessageUtil.getChildText(ePara, "UPPERLIMIT", false);
			String sResetActiveFlag = SMessageUtil.getChildText(ePara, "RESETACTIVEFLAG", false);
			
			RecipeParameter paraData = null;
			
			if(StringUtil.isEmpty(subUnitMachineName)) //Unit Param
			{
				paraData = ExtendedObjectProxy.getRecipeParamService().getRecipeParameter(unitMachineName, unitRecipeName, sRecipeParameterName);
			}
			else //SubUnit Param
			{
				paraData = ExtendedObjectProxy.getRecipeParamService().getRecipeParameter(subUnitMachineName, subUnitRecipeName, sRecipeParameterName);
			}
			
			String oldTarget = paraData.getTarget();
			String oldLower = paraData.getLowerLimit();
			String oldUpper = paraData.getUpperLimit();
			String oldValidationType = paraData.getValidationType();
			String oldCompareResult = paraData.getCompareResult();
			
			eventInfo.setEventName("Compare");
			
			paraData.setValidationType(sValidationType);
			paraData.setTarget(sTarget);
			paraData.setLowerLimit(sLowerLimit);
			paraData.setUpperLimit(sUpperLimit);
			
			if(sValidationType.equals("Range"))
			{
				paraData.setResetActiveFlag(sResetActiveFlag);
			}
			else
			{
				paraData.setResetActiveFlag("");
			}
			
			if(paraData.getValidationType().equals("None"))
			{
				if(!StringUtil.equals(oldValidationType,"None") || !StringUtil.isEmpty(oldCompareResult))
				{
					paraData.setCompareResult("");
					paraData = ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, paraData);
				}
				
				continue;
			}
			
			allNoneFlag = true;
			
			String compareResult = "";
			
			if(MESRecipeServiceProxy.getRecipeServiceUtil().compareCheckParameter(paraData))
			{
				compareResult = "OK";
			}
			else if(sValidationType.equals("None"))
			{
				compareResult = "";
			}
			else
			{
				compareResult = "NG";
				allCompareResult = "NG";
			}
			
			paraData.setCompareResult(compareResult);
			
			ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, paraData);
			
			//2019.03.04 dmlee : mantis 2912 If Change Target, Low, Upper ActiveResult 'NG'
			if((!paraData.getTarget().equals(oldTarget)) || (!paraData.getLowerLimit().equals(oldLower)) || (!paraData.getUpperLimit().equals(oldUpper)))
			{
				if(!sValidationType.equals("None") && (StringUtils.isEmpty(sResetActiveFlag) || compareResult.equals("NG")) )
				{
					if(!recipeData.getActiveResult().equals("NG"))
					{
	        			MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, recipeData.getMachineName(), recipeData.getRecipeName(), "NG", doc);
					}
				}
			}
		}
		eventLog.info("Check Param List End...");
		

		if(StringUtil.isEmpty(subUnitMachineName)) //Unit Param
		{
			MESRecipeServiceProxy.getRecipeServiceUtil().compareUnitRecipeParameterAll(eventInfo, unitMachineName, unitRecipeName, allCompareResult);
		}
		else //SubUnit Param
		{
			MESRecipeServiceProxy.getRecipeServiceUtil().compareRecipeParameterAll(eventInfo, subUnitMachineName, subUnitRecipeName, allCompareResult);
		}
		
		if(oldComparesult.equals("OK"))
		{
			//Unit Recipe
			if(StringUtil.isEmpty(subUnitMachineName))
			{
				recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{unitMachineName, unitRecipeName});
				
				//2019.01.18 dmlee : Send RMS Mail
				if(recipeData.getCompareResult().equals("NG"))
				{
					//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", unitMachineName, unitRecipeName, null, null);
				}
				else
				{
					//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-OK", unitMachineName, unitRecipeName, null, null);
				}
				//2019.01.18 dmlee : --------------
			}
			//SubUnit Recipe
			else
			{
				recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{subUnitMachineName, subUnitRecipeName});
				
				//2019.01.18 dmlee : Send RMS Mail
				if(recipeData.getCompareResult().equals("NG"))
				{
					//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", subUnitMachineName, subUnitRecipeName, null, null);
				}
				else
				{
					//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-OK", subUnitMachineName, subUnitRecipeName, null, null);
				}
				//2019.01.18 dmlee : --------------
			}
		}
		
		
		
		
	
		
		return doc;
	}
}
