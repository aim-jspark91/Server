package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParamLastActiveVer;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelationLastActiveVer;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeRecipeSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeSpec", getEventUser(), getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String compareFlag = SMessageUtil.getBodyItemValue(doc, "COMPAREFLAG", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String checkInFlag = SMessageUtil.getBodyItemValue(doc, "CHECKINFLAG", false);
		String checkOutFlag = SMessageUtil.getBodyItemValue(doc, "CHECKOUTFLAG", false);
		
		String relationFlag = SMessageUtil.getBodyItemValue(doc, "RELATIONFLAG", false);
		String activeResult = SMessageUtil.getBodyItemValue(doc, "ACTIVERESULT", false);
		String unitRelationFlag = SMessageUtil.getBodyItemValue(doc, "URELATIONFLAG", false);

		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{machineName, recipeName});
		
		String oldRelationFlag = recipeData.getRelationFlag();
		String oldCompareFlag = recipeData.getCompareFlag();
		String oldActiveResult = recipeData.getActiveResult();
		
		recipeData.setCompareFlag(compareFlag);
		recipeData.setDescription(description);
		recipeData.setRelationFlag(relationFlag);
		recipeData.setActiveResult(activeResult);
		
		//Validate CheckOut
		if(recipeData.getCheckState().equals("CheckedOut"))
		{
			if(!recipeData.getCheckOutUser().equals(eventInfo.getEventUser()))
			{
				throw new CustomException("RECIPE-0100", recipeData.getRecipeName());
			}
		}
		
		//2019.05.22 dmlee : add Unit Relation Flag mantis (3978)
		if(!StringUtil.isEmpty(unitRelationFlag))
		{
			recipeData.setRelationFlag(unitRelationFlag);
		}
		
		if(recipeData.getCompareResult().equals("NG") && activeResult.equals("OK") && recipeData.getCompareFlag().equals("Y"))
		{
			throw new CustomException("RMS-0002", recipeData.getMachineName(), recipeData.getRecipeName());
		}
		
		if(StringUtil.equals(compareFlag, "Y") && StringUtil.equals(activeResult, "-"))
		{
			throw new CustomException("RMS-0011");
		}
		
		if(compareFlag.equals("N"))
		{
			recipeData.setCompareResult("");
			recipeData.setRecipeState("UnApproved");
		}
		
		if(compareFlag.equals("Y") && recipeData.getCompareResult().equals(""))
		{
			recipeData.setCompareResult("NG");
			recipeData.setRecipeState("UnApproved");
		}
		
		if(!oldRelationFlag.equals(relationFlag) && relationFlag.equals("Y"))
		{
			recipeData.setActiveResult("NG");
		}
		
		//Check In
		if(Boolean.parseBoolean(checkInFlag))
		{
			recipeData.setCheckState("CheckedIn");
			recipeData.setCheckOutTime(null);
			recipeData.setCheckOutUser("");
			
			eventInfo.setEventName("CheckedIn");
		}
		//Check Out
		else if(Boolean.parseBoolean(checkOutFlag))
		{
			recipeData.setCheckState("CheckedOut");
			recipeData.setCheckOutTime(eventInfo.getEventTime());
			recipeData.setCheckOutUser(eventInfo.getEventUser());
			
			eventInfo.setEventName("CheckedOut");
		}
		
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTime(eventInfo.getEventTime());
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		
		
		//2018.12.28 dmlee : Reset CurrentChangeValueFlag
		if(!oldActiveResult.equals("OK") && activeResult.equals("OK"))
		{	
			List<RecipeParameter> recipeParamList = null;
			try
			{
				recipeParamList = ExtendedObjectProxy.getRecipeParamService().select("WHERE MACHINENAME = ? AND RECIPENAME = ? AND PARAMETERSTATE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName(), "Created"});
				
				for(RecipeParameter recipeParamData : recipeParamList)
				{
					recipeParamData.setCurrentChangeValue("");
					
					ExtendedObjectProxy.getRecipeParamService().update(recipeParamData);
				}
			}
			catch(Exception ex)
			{
				
			}
			
			EventInfo lastActiveEventInfo = EventInfoUtil.makeEventInfo("LastActive", getEventUser(), getEventComment(), null, null);
			
			//Last Active Relation Insert
			if(recipeParamList == null)
			{
				
				List<RecipeRelation> recipeRelationList = null;
				
				try
				{
					recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
					
					for(RecipeRelation recipeRel : recipeRelationList)
					{
						RecipeRelationLastActiveVer recipeRelLastActive = new RecipeRelationLastActiveVer(recipeRel.getParentMachine(), recipeRel.getParentMachineRecipe(), recipeRel.getChildMachine(), recipeRel.getChildMachineRecipe());
						
						recipeRelLastActive.setLastActiveTimeKey(lastActiveEventInfo.getEventTimeKey());
						
						try
						{
							ExtendedObjectProxy.getRecipeRelationLastActiveVerService().create(lastActiveEventInfo, recipeRelLastActive);
						}
						catch(Exception ex)
						{
							ExtendedObjectProxy.getRecipeRelationLastActiveVerService().modify(lastActiveEventInfo, recipeRelLastActive);
						}
						
						
						
						List<RecipeRelation> recipeRelationList2 = null;
						
						try
						{
							recipeRelationList2 = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeRel.getChildMachine(), recipeRel.getChildMachineRecipe()});
							
							for(RecipeRelation recipeRel2 : recipeRelationList2)
							{
								RecipeRelationLastActiveVer recipeRelLastActive2 = new RecipeRelationLastActiveVer(recipeRel2.getParentMachine(), recipeRel2.getParentMachineRecipe(), recipeRel2.getChildMachine(), recipeRel2.getChildMachineRecipe());
								
								recipeRelLastActive2.setLastActiveTimeKey(lastActiveEventInfo.getEventTimeKey());
								
								try
								{
									ExtendedObjectProxy.getRecipeRelationLastActiveVerService().create(lastActiveEventInfo, recipeRelLastActive2);
								}
								catch(Exception ex)
								{
									ExtendedObjectProxy.getRecipeRelationLastActiveVerService().modify(lastActiveEventInfo, recipeRelLastActive2);
								}
								
							}
						}
						catch(Exception ex)
						{
							
						}
					}
					
					recipeData.setLastActiveTimeKey(lastActiveEventInfo.getEventTimeKey());
					ExtendedObjectProxy.getRecipeService().modify(lastActiveEventInfo, recipeData);
				}
				catch(Exception ex)
				{
					
				}
				
			}
			//Last Active Param Insert
			else
			{
				for(RecipeParameter recipeParamData : recipeParamList)
				{
					RecipeParamLastActiveVer recipeParamLastActive = new RecipeParamLastActiveVer(recipeParamData.getMachineName(), recipeParamData.getRecipeName(), recipeParamData.getRecipeParameterName());
					
					recipeParamLastActive.setValue(recipeParamData.getValue());
					recipeParamLastActive.setValidationType(recipeParamData.getValidationType());
					recipeParamLastActive.setTarget(recipeParamData.getTarget());
					recipeParamLastActive.setLowerLimit(recipeParamData.getLowerLimit());
					recipeParamLastActive.setUpperLimit(recipeParamData.getUpperLimit());
					recipeParamLastActive.setLastActiveTimeKey(lastActiveEventInfo.getEventTimeKey());
					
					try
					{				
						ExtendedObjectProxy.getRecipeParamLastActiveVerService().create(lastActiveEventInfo, recipeParamLastActive);
					}
					catch(Exception ex)
					{
						ExtendedObjectProxy.getRecipeParamLastActiveVerService().modify(lastActiveEventInfo, recipeParamLastActive);
					}	
				}
				
				recipeData.setLastActiveTimeKey(lastActiveEventInfo.getEventTimeKey());
				ExtendedObjectProxy.getRecipeService().modify(lastActiveEventInfo, recipeData);
			}
		}
		//2018.12.28 dmlee : -----------------------------
		
		
		
		
		//18.10.15 dmlee : Auto Compare if CompareFlag N->Y
		try
		{
			if(!oldCompareFlag.equals("Y") && recipeData.getCompareFlag().equals("Y"))
			{
				
				List<RecipeParameter> recipeParamList = null;
				try
				{
					recipeParamList = ExtendedObjectProxy.getRecipeParamService().select("WHERE MACHINENAME = ? AND RECIPENAME = ? AND PARAMETERSTATE <> ?", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName(), "Removed"});
				}
				catch(Exception ex)
				{
					return doc;
				}
				
				String allCompareResult = "OK";
				boolean allNoneFlag = false;
				
				String unitMachineName = "";
				String unitRecipeName = "";
				String subUnitMachineName = "";
				String subUnitRecipeName = "";
				
				for(RecipeParameter recipeParamData : recipeParamList)
				{
					String sRecipeParameterName = recipeParamData.getRecipeParameterName();
					String sValue = recipeParamData.getValue();
					String sValidationType = recipeParamData.getValidationType();
					String sTarget = recipeParamData.getTarget();
					String sLowerLimit = recipeParamData.getLowerLimit();
					String sUpperLimit = recipeParamData.getUpperLimit();
					
					RecipeParameter paraData = null;
					
					
					if(recipeData.getRecipeType().equals("UNIT"))
					{
						unitMachineName = recipeData.getMachineName();
						unitRecipeName = recipeData.getRecipeName();
					}
					else if(recipeData.getRecipeType().equals("SUBUNIT"))
					{
						subUnitMachineName = recipeData.getMachineName();
						subUnitRecipeName = recipeData.getRecipeName();
					}
					else
					{
						break;
					}
					
					if(StringUtil.isEmpty(subUnitMachineName)) //Unit Param
					{
						paraData = ExtendedObjectProxy.getRecipeParamService().getRecipeParameter(unitMachineName, unitRecipeName, sRecipeParameterName);
					}
					else //SubUnit Param
					{
						paraData = ExtendedObjectProxy.getRecipeParamService().getRecipeParameter(subUnitMachineName, subUnitRecipeName, sRecipeParameterName);
					}
					
					paraData.setValidationType(sValidationType);
					paraData.setTarget(sTarget);
					paraData.setLowerLimit(sLowerLimit);
					paraData.setUpperLimit(sUpperLimit);
					
					if(paraData.getValidationType().equals("None"))
					{
						paraData.setCompareResult("");
						paraData = ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, paraData);
						continue;
					}
					
					allNoneFlag = true;
					
					String compareResult = "";
					String oldCompareResult = paraData.getCompareResult();
					
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
					if(!oldCompareResult.equals(compareResult))
					{
						if(!sValidationType.equals("None") && StringUtil.isEmpty(recipeParamData.getResetActiveFlag()) || recipeParamData.getCompareResult().equals("NG") )
						{
							if(!recipeData.getActiveResult().equals("NG"))
							{
			        			MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, recipeData.getMachineName(), recipeData.getRecipeName(), "NG", doc);
							}
						}
					}
				}
				

				if(StringUtil.isEmpty(subUnitMachineName)) //Unit Param
				{
					MESRecipeServiceProxy.getRecipeServiceUtil().compareUnitRecipeParameterAll(eventInfo, unitMachineName, unitRecipeName, allCompareResult);
				}
				else //SubUnit Param
				{
					MESRecipeServiceProxy.getRecipeServiceUtil().compareRecipeParameterAll(eventInfo, subUnitMachineName, subUnitRecipeName, allCompareResult);
				}
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("Auto Compare Fail !");
		}
		
		return doc;
	}
}
