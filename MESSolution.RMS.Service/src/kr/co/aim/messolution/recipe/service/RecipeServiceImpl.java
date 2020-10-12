package kr.co.aim.messolution.recipe.service;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class RecipeServiceImpl {

	private Log logger = LogFactory.getLog(RecipeServiceImpl.class);
	
	
    //2018.08.30 dmlee : Create Recipe (CT_RECIPE)
    public Recipe createRecipe(EventInfo eventInfo, String machineName, String recipeName, String recipeType, String compareFlag, String compareResult, String seq, String relFlag, String activeResult, Document doc) throws CustomException
    {	
    	Recipe recipeData = null;
    	
    	try
    	{
    		recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(machineName, recipeName);
    		
    		if(recipeData != null)
    		{
    			logger.info("Recipe [" + machineName + ", "+ recipeName +"] is Already Exist");
    			
    			//Unit Recipe Case (First Receive 'Create Unit Recipe' Message Case)
				if(StringUtil.isEmpty(recipeData.getCompareResult()) && recipeType.equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT) && !recipeData.getCompareFlag().equals(compareFlag))
				{
					recipeData.setCompareFlag(compareFlag);
					recipeData.setCompareResult(compareResult);
					recipeData.setActiveResult(activeResult);
					
					eventInfo.setEventName("Change");
					
					recipeData.setLastEventName(eventInfo.getEventName());
					recipeData.setLastEventTime(eventInfo.getEventTime());
					recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					recipeData.setLastEventUser(eventInfo.getEventUser());
					recipeData.setLastEventComment(eventInfo.getEventComment());
					
					recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
				}
				
				//Change Seq Case (Main,Unit Recipe)
				if(!recipeData.getSequence().equals(seq) && !recipeType.equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT) && !StringUtil.isEmpty(seq))
				{
					recipeData.setSequence(seq);
					
					eventInfo.setEventName("Change");
					
					if(recipeData.getRecipeType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN))
					{
						recipeData.setActiveResult("NG");
					}
					
					recipeData.setLastEventName(eventInfo.getEventName());
					recipeData.setLastEventTime(eventInfo.getEventTime());
					recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					recipeData.setLastEventUser(eventInfo.getEventUser());
					recipeData.setLastEventComment(eventInfo.getEventComment());
					
					recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
					
					try
					{
						List<RecipeRelation> recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
						
						for(RecipeRelation recipeRelData : recipeRelList)
						{
							this.setRecipeActiveResult(eventInfo, recipeRelData.getParentMachine(), recipeRelData.getParentMachineRecipe(), "NG", doc);
						}
					}
					catch(Exception ex)
					{
						
					}
				}
    			
    			return recipeData;
    		}
    	}
    	catch(Exception ex)
    	{
    		logger.info("Recipe [" + machineName + ", "+ recipeName +"] is Non Exist, then Create Recipe Data..");
    		
    		recipeData = new Recipe(machineName, recipeName);
    		
			recipeData.setRecipeType(recipeType);
			recipeData.setCompareFlag(compareFlag);
			recipeData.setCompareResult(compareResult);
			recipeData.setRecipeState(GenericServiceProxy.getConstantMap().RECIPE_STATE_CREATED);
			recipeData.setCheckState("CheckedIn");
			recipeData.setRelationFlag(relFlag);
			recipeData.setActiveResult(activeResult);
			
			recipeData.setSequence(seq);
			
			eventInfo.setEventName("Create");
			
			recipeData.setLastEventName(eventInfo.getEventName());
			recipeData.setLastEventTime(eventInfo.getEventTime());
			recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			recipeData.setLastEventUser(eventInfo.getEventUser());
			recipeData.setLastEventComment(eventInfo.getEventComment());
			
			recipeData = ExtendedObjectProxy.getRecipeService().create(eventInfo, recipeData);
    	}
    	
    	return recipeData;
    }
    
    //2018.08.30 dmlee : Remove Recipe Relation (CT_RECIPERELATION)
    public void removeMachineRecipeRelation(EventInfo eventInfo, String machineName, String machineRecipeName, List<Element> elementUnitList, Document doc) throws CustomException
    {	
		//Remove Recipe Relation
		try
		{
			List<RecipeRelation> oldRecipeList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{machineName, machineRecipeName});
			
			List<String> changeRecipeList = new ArrayList<String>(); 
			
			for(RecipeRelation recipeRelation : oldRecipeList)
			{
				boolean removeFlag = false;
				for (Element removeEUnit : elementUnitList)
				{
					String removeUnitName = SMessageUtil.getChildText(removeEUnit, "UNITNAME", true);
					String removeRecipeName = SMessageUtil.getChildText(removeEUnit, "UNITRECIPENAME", true);
					
					if(recipeRelation.getChildMachine().equals(removeUnitName) && recipeRelation.getChildMachineRecipe().equals(removeRecipeName))
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
					this.setRecipeActiveResult(eventInfo, recipeRelation.getParentMachine(), recipeRelation.getParentMachineRecipe(), "NG", doc);
					
					eventInfo.setEventName("Remove");
					
					changeRecipeList.add("Remove Relation["+recipeRelation.getChildMachineRecipe()+"]");
					
					ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelation);
					
					
				}
			}
			
			if(!changeRecipeList.isEmpty())
			{
				MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-REL-CH", machineName, machineRecipeName, changeRecipeList, null);
			}
		}
		catch(Exception ex)
		{

		}
    }
    
    
    //2018.09.04 dmlee : Remove Unit Recipe Relation (CT_RECIPERELATION)
    public void removeUnitRecipeRelation(EventInfo eventInfo, String unitName, String unitRecipeName, List<Element> elementSubUnitList, Document doc) throws CustomException
    {	
		//Remove Unit Recipe Relation
		try
		{
			List<RecipeRelation> oldRecipeList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{unitName, unitRecipeName});
			
			for(RecipeRelation recipeRelation : oldRecipeList)
			{
				boolean removeFlag = false;
				for (Element removeESubUnit : elementSubUnitList)
				{
					String removeSubUnitName = SMessageUtil.getChildText(removeESubUnit, "SUBUNITNAME", true);
					String removeSubUnitRecipeName = SMessageUtil.getChildText(removeESubUnit, "SUBUNITRECIPENAME", true);
					
					if(recipeRelation.getChildMachine().equals(removeSubUnitName) && recipeRelation.getChildMachineRecipe().equals(removeSubUnitRecipeName))
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
					this.setRecipeActiveResult(eventInfo, recipeRelation.getParentMachine(), recipeRelation.getParentMachineRecipe(), "NG", doc);
					
					eventInfo.setEventName("Remove");			
					ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelation);
				}
			}
		}
		catch(Exception ex)
		{

		}
    }
    
    
    //2018.08.30 dmlee : Create Recipe Relation (CT_RECIPERELATION)
    public RecipeRelation createRecipeRelation(EventInfo eventInfo, String parentMachineName, String parentMachineRecipeName, String childMachineName, String childMachineRecipeName, Document doc) throws CustomException
    {	
    	RecipeRelation recipeRelationData = null;
    	
    	try
    	{
    		RecipeRelation recipeRelation = ExtendedObjectProxy.getRecipeRelationService().selectByKey(false, new Object[]{parentMachineName, parentMachineRecipeName, childMachineName, childMachineRecipeName});
    		
    		if(recipeRelation != null)
    		{
    			logger.info("Recipe Relation [" + parentMachineName + ", "+ parentMachineRecipeName +", "+ childMachineName +", "+ childMachineRecipeName +"] is Already Exist");
    			
    			return recipeRelation;
    		}
    	}
    	catch(Exception ex)
    	{
    		
    		boolean existMachine = true;
    		try
    		{
    			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(childMachineName);
    			
    			if(machineData == null)
    			{
    				existMachine = false;	
    			}
    		}
    		catch(Exception ex2)
    		{
    			existMachine = false;
    		}
    		
    		if(existMachine)
    		{
        		logger.info("Recipe Relation [" + parentMachineName + ", "+ parentMachineRecipeName +", "+ childMachineName +", "+ childMachineRecipeName +"] is Non Exist, then Create Recipe Data..");
        		
        		recipeRelationData = new RecipeRelation(parentMachineName, parentMachineRecipeName, childMachineName, childMachineRecipeName);
        		
    			eventInfo.setEventName("Create");
    			
    			recipeRelationData.setLastEventName(eventInfo.getEventName());
    			recipeRelationData.setLastEventTime(eventInfo.getEventTime());
    			recipeRelationData.setLastEventTimeKey(eventInfo.getEventTimeKey());
    			recipeRelationData.setLastEventUser(eventInfo.getEventUser());
    			recipeRelationData.setLastEventComment(eventInfo.getEventComment());
    			
    			
    			ExtendedObjectProxy.getRecipeRelationService().create(eventInfo, recipeRelationData);
    			
    			this.setRecipeActiveResult(eventInfo, parentMachineName, parentMachineRecipeName, "NG", doc);

    		}
    	}
    	
    	return recipeRelationData;
    }
    
    
    
    public void removeMachineRecipe(EventInfo eventInfo, String machineName, List<Element> elementRecipeList) throws CustomException
    {  
		//Remove Machine Recipe
		try
		{
			List<Recipe> oldRecipeDataList = ExtendedObjectProxy.getRecipeService().select("WHERE MACHINENAME = ? ", new Object[]{machineName});
			
			for(Recipe oldRecipeData : oldRecipeDataList)
			{
				Boolean removeFlag = true;
		        for(Element recipe2 : elementRecipeList)
		        {
					String machineRecipeName = SMessageUtil.getChildText(recipe2, "MACHINERECIPENAME", true);
					
					if(oldRecipeData.getRecipeName().equals(machineRecipeName))
					{
						removeFlag = false;
						break;
					}
		        }
		        
		        if(removeFlag)
		        {
		        	eventInfo.setEventName("Remove");
		        	
		        	try
		        	{
			        	//Machine Recipe Relation Remove
			        	List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{machineName, oldRecipeData.getRecipeName()});
			        	
						if(recipeRelationList != null)
						{
							for(RecipeRelation recipeRelData : recipeRelationList)
							{
								ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelData);
							}
						}
		        	}
		        	catch(Exception ex)
		        	{
		        		
		        	}
		        	
		        	//Machine Recipe Remove
		        	ExtendedObjectProxy.getRecipeService().remove(eventInfo, oldRecipeData);
		        	
		        }
			}
		}
		catch(Exception ex)
		{

		}
        
    }
    
    
    public void removeRecipeParameter(EventInfo eventInfo, String machineName, String recipeName, List<Element> eleRecipeParamList, Document doc) throws CustomException
    {  
		try
		{
			List<RecipeParameter> recipeParameterList = ExtendedObjectProxy.getRecipeParamService().getRecipeParameterList(machineName, recipeName);
			
			boolean changeActive = false;
			for(RecipeParameter recipeParam : recipeParameterList)
			{
				boolean removeFlag = false;
				for (Element removeERecipeParam : eleRecipeParamList)
				{
					String recipeParaName = SMessageUtil.getChildText(removeERecipeParam, "RECIPEPARANAME", true);
					
					if(recipeParam.getMachineName().equals(machineName) && recipeParam.getRecipeName().equals(recipeName) && recipeParam.getRecipeParameterName().equals(recipeParaName))
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
					changeActive = true;
					
					eventInfo.setEventName("Remove");
					
					recipeParam.setParameterState("Removed");
					ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParam);
					
					//2019.10.11 dmlee
					//MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, machineName, recipeName, "NG", doc);
				}
			}
			
			//2019.10.11 dmlee
			if(changeActive)
			{
				MESRecipeServiceProxy.getRecipeServiceImpl().setRecipeActiveResult(eventInfo, machineName, recipeName, "NG", doc);
			}
		}
		catch(Exception ex)
		{

		}
    }
    
    public void setRecipeActiveResult(EventInfo eventInfo, String machineName, String recipeName, String changeActiveResult, Document doc) throws CustomException
    {
    	try
    	{
    		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{machineName, recipeName});
    		
    		logger.info("Set Recipe Active Result ["+recipeData.getActiveResult()+"] to ["+changeActiveResult+"] Machine["+machineName+"] Recipe["+recipeName+"]");
    		
    		//Main Case
    		if(recipeData.getRecipeType().equals("MAIN"))
    		{
    			if(recipeData.getRelationFlag().equals("Y") && !recipeData.getActiveResult().equals(changeActiveResult))
    			{
        			if(changeActiveResult.equals("OK"))
        			{
            			eventInfo.setEventName("Active");
            			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
            			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
        			}
        			else
        			{
            			eventInfo.setEventName("InActive");
            			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
            			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            			
            			MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", recipeData.getMachineName(), recipeData.getRecipeName(), null, null); //modify by jhy on20200501 mantis:6099
            			
        			}
        			
        			recipeData.setActiveResult(changeActiveResult);
        			
        			recipeData.setLastEventName(eventInfo.getEventName());
        			recipeData.setLastEventTime(eventInfo.getEventTime());
        			recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        			recipeData.setLastEventUser(eventInfo.getEventUser());
        			recipeData.setLastEventComment(eventInfo.getEventComment());
        			
        			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
    			}

    		}
    		//Unit Case
    		else if(recipeData.getRecipeType().equals("UNIT"))
    		{
    			
    			List<RecipeRelation> relDataList = null;
    			
    			try
    			{
    				relDataList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()}); 
    				
    			}
    			catch(Exception ex)
    			{

    			}
    			
    			//Non Exist Main Recipe Case
    			//2019.10.30 modify by donmin in jhiying computer, because unit only machine when changed subunit's recipe ,active result should be modified OK-->NG
    			if(relDataList == null || relDataList.get(0).getParentMachineRecipe().equals("-"))
    			{
    				//SubUnit Exist Unit Recipe
    				if(recipeData.getCompareFlag().equals("-") || StringUtil.isEmpty(recipeData.getCompareFlag()) )
    				{
    					if(recipeData.getRelationFlag().equals("Y") && !recipeData.getActiveResult().equals(changeActiveResult))
        				{
        					if(changeActiveResult.equals("OK"))
                			{
                    			eventInfo.setEventName("Active");
                    			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                    			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                			}
                			else
                			{
                    			eventInfo.setEventName("InActive");
                    			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                    			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                    			
                    			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", recipeData.getMachineName(), recipeData.getRecipeName(), null, null);
                			}
                			
                			recipeData.setActiveResult(changeActiveResult);
                			
                			recipeData.setLastEventName(eventInfo.getEventName());
                			recipeData.setLastEventTime(eventInfo.getEventTime());
                			recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
                			recipeData.setLastEventUser(eventInfo.getEventUser());
                			recipeData.setLastEventComment(eventInfo.getEventComment());
                			
                			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
        				}
    				}
    				//Param Exist Unit Recipe
	    			else
	    			{
	    				if(recipeData.getCompareFlag().equals("Y"))
	    				{
	    					if(changeActiveResult.equals("OK"))
	            			{
	                			eventInfo.setEventName("Active");
	                			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
	                			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
	            			}
	            			else
	            			{
	                			eventInfo.setEventName("InActive");
	                			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
	                			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
	                			
	                			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", recipeData.getMachineName(), recipeData.getRecipeName(), null, null);
	            			}
	            			
	            			recipeData.setActiveResult(changeActiveResult);
	            			
	            			recipeData.setLastEventName(eventInfo.getEventName());
	            			recipeData.setLastEventTime(eventInfo.getEventTime());
	            			recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
	            			recipeData.setLastEventUser(eventInfo.getEventUser());
	            			recipeData.setLastEventComment(eventInfo.getEventComment());
	            			
	            			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
	    				}
	    			}
    			}
    			//Main Recipe Exist Case
    			else
    			{
    				if(recipeData.getCompareFlag().equals("Y") && !recipeData.getActiveResult().equals(changeActiveResult))   
    				
    				{
    					if(changeActiveResult.equals("OK"))
            			{
                			eventInfo.setEventName("Active");
                			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
            			}
            			else
            			{
                			eventInfo.setEventName("InActive");
                			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                			
                			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", recipeData.getMachineName(), recipeData.getRecipeName(), null, null);
            			}
            			
            			recipeData.setActiveResult(changeActiveResult);
            			
            			recipeData.setLastEventName(eventInfo.getEventName());
            			recipeData.setLastEventTime(eventInfo.getEventTime());
            			recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
            			recipeData.setLastEventUser(eventInfo.getEventUser());
            			recipeData.setLastEventComment(eventInfo.getEventComment());
            			
            			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
    				}
    				// 删除SubUnit层Recipe时，修复Machine层Active不会变NG的Bug,只看Machine层Relation Flag和Active ResultBug Modified By JHY
        			//if(!StringUtil.isEmpty(recipeData.getCompareFlag()) && recipeData.getCompareFlag().equals("Y"))
        			//{
        				List<RecipeRelation> recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
            			
            			for(RecipeRelation recipeRel : recipeRelList)
            			{
            				Recipe mainRecipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(recipeRel.getParentMachine(), recipeRel.getParentMachineRecipe());
            				
            				if(mainRecipeData.getRelationFlag().equals("Y") && !mainRecipeData.getActiveResult().equals(changeActiveResult))
            				{
                    			if(changeActiveResult.equals("OK"))
                    			{
                        			eventInfo.setEventName("Active");
                        			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                        			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                    			}
                    			else
                    			{
                        			eventInfo.setEventName("InActive");
                        			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                        			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                        			
                        			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", mainRecipeData.getMachineName(), mainRecipeData.getRecipeName(), null, null);
                    			}
                    			
                    			mainRecipeData.setActiveResult(changeActiveResult);
                    			
                    			mainRecipeData.setLastEventName(eventInfo.getEventName());
                    			mainRecipeData.setLastEventTime(eventInfo.getEventTime());
                    			mainRecipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
                    			mainRecipeData.setLastEventUser(eventInfo.getEventUser());
                    			mainRecipeData.setLastEventComment(eventInfo.getEventComment());
                    			
                    			ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeData);
            				}
            			}
        		//	}
    				
    			}
    		}
    		//SubUnit Case
    		else
    		{
    			try
    			{
    				if(!recipeData.getActiveResult().equals(changeActiveResult))
    				{
        				recipeData.setActiveResult(changeActiveResult);
            			
        				recipeData.setLastEventName(eventInfo.getEventName());
        				recipeData.setLastEventTime(eventInfo.getEventTime());
        				recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        				recipeData.setLastEventUser(eventInfo.getEventUser());
        				recipeData.setLastEventComment(eventInfo.getEventComment());
            			
            			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
    				}
    			}
    			catch(Exception ex)
    			{
    				
    			}
    			
    			if(!StringUtil.isEmpty(recipeData.getCompareFlag()) && recipeData.getCompareFlag().equals("Y"))
    			{
    				List<RecipeRelation> recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
        			
        			for(RecipeRelation recipeRel : recipeRelList)
        			{
        				Recipe unitRecipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(recipeRel.getParentMachine(), recipeRel.getParentMachineRecipe());	
        				
            			List<RecipeRelation> relDataList = null;
            			
            			try
            			{
            				relDataList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{unitRecipeData.getMachineName(), unitRecipeData.getRecipeName()});
            			}
            			catch(Exception ex)
            			{

            			}
        				
        				//Unit Only Case
            			if(relDataList == null || relDataList.get(0).getParentMachineRecipe().equals("-"))
            			{
            				if(unitRecipeData.getRelationFlag().equals("Y") && !unitRecipeData.getActiveResult().equals(changeActiveResult))
            				{
            					if(changeActiveResult.equals("OK"))
                    			{
                        			eventInfo.setEventName("Active");
                        			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                        			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                    			}
                    			else
                    			{
                        			eventInfo.setEventName("InActive");
                        			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                        			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                        			
                        			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName(), null, null);
                    			}
                    			
                				unitRecipeData.setActiveResult(changeActiveResult);
                    			
                				unitRecipeData.setLastEventName(eventInfo.getEventName());
                				unitRecipeData.setLastEventTime(eventInfo.getEventTime());
                				unitRecipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
                				unitRecipeData.setLastEventUser(eventInfo.getEventUser());
                				unitRecipeData.setLastEventComment(eventInfo.getEventComment());
                    			
                    			ExtendedObjectProxy.getRecipeService().modify(eventInfo, unitRecipeData);
            				}
            			}
            			//Main Recipe Exist Case
            			else
            			{
            				List<RecipeRelation> recipeRelList2 = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{unitRecipeData.getMachineName(), unitRecipeData.getRecipeName()});
                			
                			for(RecipeRelation recipeRel2 : recipeRelList2)
                			{
                				Recipe mainRecipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(recipeRel2.getParentMachine(), recipeRel2.getParentMachineRecipe());
                				
                				if(mainRecipeData.getRelationFlag().equals("Y") && !mainRecipeData.getActiveResult().equals(changeActiveResult))
                				{
                        			if(changeActiveResult.equals("OK"))
                        			{
                            			eventInfo.setEventName("Active");
                            			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                            			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                        			}
                        			else
                        			{
                            			eventInfo.setEventName("InActive");
                            			//eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
                            			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
                            			
                            			//MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-COM-NG", mainRecipeData.getMachineName(), mainRecipeData.getRecipeName(), null, null);
                        			}
                        			
                        			mainRecipeData.setActiveResult(changeActiveResult);
                        			
                        			mainRecipeData.setLastEventName(eventInfo.getEventName());
                        			mainRecipeData.setLastEventTime(eventInfo.getEventTime());
                        			mainRecipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
                        			mainRecipeData.setLastEventUser(eventInfo.getEventUser());
                        			mainRecipeData.setLastEventComment(eventInfo.getEventComment());
                        			
                        			ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeData);
                				}
                			}
            			}	
        			}
    			}
    			
    		}

    	}
    	catch(Exception ex)
    	{
    		logger.error("*** Recipe Active Result Set Fail ! Machine["+machineName+"] Recipe["+recipeName+"] *** ");
    	}
    }
}
