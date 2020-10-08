package kr.co.aim.messolution.recipe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class RecipeServiceUtil {

    private Log logger = LogFactory.getLog(RecipeServiceUtil.class);

    private final String CR = "\n";
    private final String[] groupCode = new String[] {"$", "&"};

    /**
     * to acquire Recipe
     * @author swcho
     * @since 2014.09.03
     * @param factoryName
     * @param productSpecName
     * @param processFlowName
     * @param processOperationName
     * @param machineName
     * @param isVerified
     * @return
     * @throws CustomException
     */
    
    // start modify by jhiying on20191123 
    public String getINDPMachineRecipe (String factoryName, String productSpecName, String processFlowName, String processOperationName,
                                    String machineName, String ecCode,String portName)
        throws CustomException 
      {
    	logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        //finally return
        String result = "";

        // Modified by dmlee on 2018.07.11 - Accept asterisk in TPEFOMPolicy conditions.
//        ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicy(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);
        ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);

        String machineGroupName = CommonUtil.getValue(instruction, "MACHINEGROUPNAME");
        String designatedRecipeName = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));
        
        
        Port portData =  MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName); 
        Machine  machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
        String linkedUnittName = portData.getUdfs().get("LINKEDUNITNAME");
        {//mandatorOy validation
        	//if (StringUtil.isEmpty(machineGroupName))
        	//  throw new CustomException("MACHINE-0101", machineGroupName);

        	if (StringUtil.isEmpty(designatedRecipeName))
        		throw new CustomException("MACHINE-0102", designatedRecipeName);
        }

        //2018.08.20 dmlee : RMS Check
        MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
       
  
        try
        {
        	if(macSpecData.getUdfs().get("RMSFLAG").toString().equals("Y"))
        	{
        		logger.info("*** RMS System : Machine["+machineName+"] Recipe["+designatedRecipeName+"] Check .... ***");
     		   Recipe recipeData = null;
     	    	try
     	    	{
     	    		recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(machineName, designatedRecipeName);
     	    	}
     			catch(Exception ex)
     			{
     				throw new CustomException("RMS-0003", machineName, designatedRecipeName);
     			}
        	 //  if(machineData.getUdfs().get("OPERATIONMODE").equals("INDP"))
        		   
        		if (recipeData.getRelationFlag().equals("Y")) 
        		{
        		   if(machineData.getUdfs().get("OPERATIONMODE").toString().equals("INDP"))
        		   {
        			   List<RecipeRelation> recipeRelList = null;
           			   try
         	    		 {
         	    			recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
         	    		 }
         	    		 catch(Exception ex)
         	    		 {
         	    			throw new CustomException("RMS-0004", recipeData.getMachineName(), recipeData.getRecipeName());
         	    		 }
           			   
           			  for(RecipeRelation recipeRelData : recipeRelList)
      	    		   {
      	    		   
	      	    		      Recipe unitRecipeData = null;
	      	    		       try
	      	                  {
	      	    		        unitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe()});
	      	                  }
	      	    		      catch(Exception ex)
	      	                  {
	      	    		        throw new CustomException("RMS-0003", recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe());
	      	                  }
      	    		       
	      	    		     List<RecipeRelation> recipeRelListUnit = null;
	     	    		    
		     		    		try
		     		    		{
		     		    			recipeRelListUnit = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{unitRecipeData.getMachineName(), unitRecipeData.getRecipeName()});
		     		    		}
		     		    		catch(Exception ex)
		     		    		{
		     		    			//throw new CustomException("RMS-0004", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		     		    		}
		     		    		
		     		    		//SubUnit Recipe Exist
		     		    		if(recipeRelListUnit != null)
		     		    		{
		     			    		for(RecipeRelation recipeRelSubUnitData : recipeRelListUnit)
		     			    		{
		     			    		    Recipe subUnitRecipeData = null;
		     			    		    try
		     			                {
		     			    		    	subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe()});
		     			                }
		     			    		    catch(Exception ex)
		     			                {
		     			    		        throw new CustomException("RMS-0003", recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe());
		     			                }
		     			    		    
		     		    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getCompareResult().equals("NG"))
		     		    		    	{
		     		    		    		throw new CustomException("RMS-0002", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
		     		    		    	}
		     		    		    	
		     		    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getActiveResult().equals("NG"))
		     		    		    	{
		     		    		    		throw new CustomException("RMS-0007", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
		     		    		    	}
		     			    		}
		     		    		}
		     		    		//SubUnit Recipe Non Exist
		     		    		else
		     		    		{
		     		    			
		     		    			
		     		    			
		     		    			if(unitRecipeData.getMachineName().equals(linkedUnittName)
		     		    					&& unitRecipeData.getCompareFlag().equals("Y") && unitRecipeData.getCompareResult().equals("NG"))
		     		    			{
		     		    				throw new CustomException("RMS-0002", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		     		    			}
		     		    			
		     		    			if(unitRecipeData.getMachineName().equals(linkedUnittName)
		     		    					&& unitRecipeData.getCompareFlag().equals("Y") && unitRecipeData.getActiveResult().equals("NG"))
		     		    			{
		     		    				throw new CustomException("RMS-0007", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		     		    			}
		     		    		}
      	    		       
      	    		       }   
           			  
        		       }
        		   else
        		   {
        			   //If Track Machine
               		  if(macSpecData.getUdfs().get("CONSTRUCTTYPE").equals("TRACK"))
               		  {
               			designatedRecipeName = this.checkRMSCompareResultForTrackMachine(machineName, designatedRecipeName);
               		  }
               		 //Unit Recipe Only Flag = 'Y'
               		   else if(macSpecData.getUdfs().get("RMSUNITRECIPEONLY").equals("Y"))
               		   {
               		    /* 20181128, hhlee, modify, add MACHINETYPE condition */
               			List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(
               					" WHERE DETAILMACHINETYPE = ? AND SUPERMACHINENAME = ? AND MACHINETYPE = ? ORDER BY MACHINENAME ASC", 
               					new Object[]{GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine});
               			
               			logger.info("*** RMS System : Machine["+unitSpecList.get(0).getKey().getMachineName()+"] Recipe["+designatedRecipeName+"] Check .... ***");
               			
               			this.checkRMSCompareResult(unitSpecList.get(0).getKey().getMachineName(), designatedRecipeName, true);
               		   }
               		 else
               		 {
               			logger.info("*** RMS System : Machine["+machineName+"] Recipe["+designatedRecipeName+"] Check .... ***");
               			
               			this.checkRMSCompareResult(machineName, designatedRecipeName, false);
               		 }
        		   }
        			        					
        		}

            }
         }

        catch(Exception ex)
        {
        	throw ex;
        }
        //2018.08.20 dmlee : ------------


        result = designatedRecipeName;

        logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        return result;
      }
    // end modify by jhiying on20191123 
    public String getMachineRecipe(String factoryName, String productSpecName, String processFlowName, String processOperationName,
                                    String machineName, String ecCode)
        throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        //finally return
        String result = "";

        // Modified by dmlee on 2018.07.11 - Accept asterisk in TPEFOMPolicy conditions.
//        ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicy(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);
        ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);

        String machineGroupName = CommonUtil.getValue(instruction, "MACHINEGROUPNAME");
        String designatedRecipeName = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));

        {//mandatorOy validation
        	//if (StringUtil.isEmpty(machineGroupName))
        	//  throw new CustomException("MACHINE-0101", machineGroupName);

        	if (StringUtil.isEmpty(designatedRecipeName))
        		throw new CustomException("MACHINE-0102", designatedRecipeName);
        }

        //2018.08.20 dmlee : RMS Check
        MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        try
        {
        	if(macSpecData.getUdfs().get("RMSFLAG").toString().equals("Y"))
        	{
        		//If Track Machine
        		if(macSpecData.getUdfs().get("CONSTRUCTTYPE").equals("TRACK"))
        		{
        			designatedRecipeName = this.checkRMSCompareResultForTrackMachine(machineName, designatedRecipeName);
        		}
        		//Unit Recipe Only Flag = 'Y'
        		else if(macSpecData.getUdfs().get("RMSUNITRECIPEONLY").equals("Y"))
        		{
        		    /* 20181128, hhlee, modify, add MACHINETYPE condition */
        			List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(
        					" WHERE DETAILMACHINETYPE = ? AND SUPERMACHINENAME = ? AND MACHINETYPE = ? ORDER BY MACHINENAME ASC", 
        					new Object[]{GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine});
        			
        			logger.info("*** RMS System : Machine["+unitSpecList.get(0).getKey().getMachineName()+"] Recipe["+designatedRecipeName+"] Check .... ***");
        			
        			this.checkRMSCompareResult(unitSpecList.get(0).getKey().getMachineName(), designatedRecipeName, true);
        		}
        		else
        		{
        			logger.info("*** RMS System : Machine["+machineName+"] Recipe["+designatedRecipeName+"] Check .... ***");
        			
        			this.checkRMSCompareResult(machineName, designatedRecipeName, false);
        		}
        	}
        }
        catch(Exception ex)
        {
        	throw ex;
        }
        //2018.08.20 dmlee : ------------


        result = designatedRecipeName;

        logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        return result;
    }

    public String getMachineRecipeByTPEFOMPolicy(String factoryName, String productSpecName, String processFlowName, String processOperationName,
    		String machineName, String ecCode)
    				throws CustomException
    {
    	logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

    	//finally return
    	String result = "";

    	// Modified by dmlee on 2018.07.11 - Accept asterisk in TPEFOMPolicy conditions.
    	//ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicy(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);
    	ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameByTPEFOMPolicyV2(factoryName, productSpecName, processFlowName, processOperationName, machineName, ecCode);

    	String designatedRecipeName = StringUtil.trim(CommonUtil.getValue(instruction, "MACHINERECIPENAME"));

    		if (StringUtil.isEmpty(designatedRecipeName))
    			throw new CustomException("MACHINE-0102", designatedRecipeName);

    	result = designatedRecipeName;

    	logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

    	return result;
    }



    /**
     * Recipe acquisition for Durable
     * @author swcho
     * @since 2015.03.02
     * @param factoryName
     * @param durableSpecName
     * @param durableName
     * @param machineName
     * @param isVerified
     * @return
     * @throws CustomException
     */
    public String getMachineRecipe(String factoryName, String durableSpecName, String durableName, String machineName, boolean isVerified)
        throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        //finally return
        String result = "";

        DurableSpecKey duralbeSpecKey = new DurableSpecKey();
        duralbeSpecKey.setFactoryName(factoryName);
        duralbeSpecKey.setDurableSpecName(durableSpecName);
        duralbeSpecKey.setDurableSpecVersion("00001");

        DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(duralbeSpecKey);

        String cleanRecipeName = CommonUtil.getValue(durableSpec.getUdfs(), "MASKRECIPENAME");

        String designatedRecipeName = cleanRecipeName;

        {//mandatory validation
            //if (StringUtil.isEmpty(machineGroupName))
            //  throw new CustomException("MACHINE-0101", machineGroupName);

            if (StringUtil.isEmpty(designatedRecipeName))
                throw new CustomException("MACHINE-0102", designatedRecipeName);
        }

        //if (isVerified)
            //verifyMachineRecipe(machineGroupName, machineName, designatedRecipeName);

        result = designatedRecipeName;

        logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));

        return result;
    }


    /**
     * Recipe acquisition for Consumable UPK Load
     * @author wghuang
     * @since 2018.04.24
     * @param factoryName
     * @param consumableSpecName
     * @param consumableName
     * @param machineName
     * @return
     * @throws CustomException
     */
    public String getMachineRecipe(String consumableName)throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipeFromCrate", System.currentTimeMillis()));

        //finally return
        String result = "";

        ConsumableKey consumableKey = new ConsumableKey(consumableName);
        Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
        String consumableSpec = con.getConsumableSpecName();

        ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
        consumableSpecKey.setConsumableSpecName(consumableSpec);
        consumableSpecKey.setConsumableSpecVersion("00001");
        consumableSpecKey.setFactoryName(con.getFactoryName());
        ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

        String machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");

        if (StringUtil.isEmpty(machineRecipeName))
            throw new CustomException("MACHINE-0106", consumableName);

        result = machineRecipeName;

        logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipeFromCrate", System.currentTimeMillis()));

        return result;
    }
    
    //2018.08.13 dmlee : Check RMS (Check Recipe Table CompareResult)
    public void checkRMSCompareResult(String machineName, String recipeName, boolean isUnitRecipeOnly) throws CustomException, NotFoundSignal
    {
    	Recipe recipeData = null;
    	try
    	{
    		recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(machineName, recipeName);
    	}
		catch(Exception ex)
		{
			throw new CustomException("RMS-0003", machineName, recipeName);
		}
    	
    	
		boolean checkChildRecipe = false;
		
		if(recipeData.getRelationFlag().equals("Y"))
		{
			if(recipeData.getActiveResult().equals("NG"))
			{
				throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
			}
			
			checkChildRecipe = true;
			
			logger.info("Machine["+recipeData.getMachineName()+"], Recipe["+recipeData.getRecipeName()+"] Relation Flag is 'Y' Search Child Recipe .. ");
			
		}
		else
		{
			if(recipeData.getRecipeType().equals("UNIT") && recipeData.getCompareFlag().equals("Y"))
			{
				if(!recipeData.getActiveResult().equals("OK"))
				{
					throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
				}
			}
			
			logger.info("Machine["+recipeData.getMachineName()+"], Recipe["+recipeData.getRecipeName()+"] Relation Flag is 'N' No Search Child Recipe .. ");
		}
    	
		if(checkChildRecipe)
		{
			//Normal Case
			if(recipeData.getRecipeType().equals("MAIN"))
			{
				
				List<RecipeRelation> recipeRelList = null;
				
	    		try
	    		{
	    			recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
	    		}
	    		catch(Exception ex)
	    		{
	    			throw new CustomException("RMS-0004", recipeData.getMachineName(), recipeData.getRecipeName());
	    		}

	    		for(RecipeRelation recipeRelData : recipeRelList)
	    		{
	    		    Recipe unitRecipeData = null;
	    		    try
	                {
	    		        unitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe()});
	                }
	    		    catch(Exception ex)
	                {
	    		        throw new CustomException("RMS-0003", recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe());
	                }
	    		    
	    		    List<RecipeRelation> recipeRelListUnit = null;
	    		    
		    		try
		    		{
		    			recipeRelListUnit = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{unitRecipeData.getMachineName(), unitRecipeData.getRecipeName()});
		    		}
		    		catch(Exception ex)
		    		{
		    			//throw new CustomException("RMS-0004", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		    		}
		    		
		    		//SubUnit Recipe Exist
		    		if(recipeRelListUnit != null)
		    		{
			    		for(RecipeRelation recipeRelSubUnitData : recipeRelListUnit)
			    		{
			    		    Recipe subUnitRecipeData = null;
			    		    try
			                {
			    		    	subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe()});
			                }
			    		    catch(Exception ex)
			                {
			    		        throw new CustomException("RMS-0003", recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe());
			                }
			    		    
		    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getCompareResult().equals("NG"))
		    		    	{
		    		    		throw new CustomException("RMS-0002", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
		    		    	}
		    		    	
		    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getActiveResult().equals("NG"))
		    		    	{
		    		    		throw new CustomException("RMS-0007", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
		    		    	}
			    		}
		    		}
		    		//SubUnit Recipe Non Exist
		    		else
		    		{
		    			if(unitRecipeData.getCompareFlag().equals("Y") && unitRecipeData.getCompareResult().equals("NG"))
		    			{
		    				throw new CustomException("RMS-0002", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		    			}
		    			
		    			if(unitRecipeData.getCompareFlag().equals("Y") && unitRecipeData.getActiveResult().equals("NG"))
		    			{
		    				throw new CustomException("RMS-0007", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
		    			}
		    		}
	    		}
			}
			//Unit Only Case
			else if(recipeData.getRecipeType().equals("UNIT"))
			{
				List<RecipeRelation> recipeRelListUnit = null;
    		    
	    		try
	    		{
	    			recipeRelListUnit = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});
	    		}
	    		catch(Exception ex)
	    		{
	    			//throw new CustomException("RMS-0004", unitRecipeData.getMachineName(), unitRecipeData.getRecipeName());
	    		}
	    		
	    		//SubUnit Recipe Exist
	    		if(recipeRelListUnit != null)
	    		{
		    		for(RecipeRelation recipeRelSubUnitData : recipeRelListUnit)
		    		{
		    		    Recipe subUnitRecipeData = null;
		    		    try
		                {
		    		    	subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe()});
		                }
		    		    catch(Exception ex)
		                {
		    		        throw new CustomException("RMS-0003", recipeRelSubUnitData.getChildMachine(), recipeRelSubUnitData.getChildMachineRecipe());
		                }
		    		    
	    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getCompareResult().equals("NG"))
	    		    	{
	    		    		throw new CustomException("RMS-0002", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
	    		    	}
	    		    	
	    		    	if(subUnitRecipeData.getCompareFlag().equals("Y") && subUnitRecipeData.getActiveResult().equals("NG"))
	    		    	{
	    		    		throw new CustomException("RMS-0007", subUnitRecipeData.getMachineName(), subUnitRecipeData.getRecipeName());
	    		    	}
		    		}
	    		}
	    		//SubUnit Recipe Non Exist
	    		else
	    		{
	    			if(recipeData.getCompareFlag().equals("Y") && recipeData.getCompareResult().equals("NG"))
	    			{
	    				throw new CustomException("RMS-0002", recipeData.getMachineName(), recipeData.getRecipeName());
	    			}
	    			
	    			if(recipeData.getCompareFlag().equals("Y") && recipeData.getActiveResult().equals("NG"))
	    			{
	    				throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
	    			}
	    		}
			}
		}
    }
    
    //2018.08.20 dmlee : Check RMS Track Machine (Check Recipe Table CompareResult)
    public String checkRMSCompareResultForTrackMachine(String machineName, String recipeName) throws CustomException, NotFoundSignal
    {
    	if(recipeName.length() != 12)
    	{
    		throw new CustomException("RMS-0005", machineName);
    	}
    	
    	try
    	{
    		List<MachineSpec> trackUnitSpecList = MachineServiceProxy.getMachineSpecService().select(
    				"WHERE DETAILMACHINETYPE = ? AND SUPERMACHINENAME = ? AND MACHINETYPE = ? ORDER BY CONSTRUCTTYPE ASC", 
    				new Object[]{GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine});
    		
    		if(trackUnitSpecList.size() != 3)
    		{
    			throw new CustomException("RMS-0005", machineName);
    		}
    		
    		String checkUnit1 = trackUnitSpecList.get(0).getKey().getMachineName();
    		String checkUnit2 = trackUnitSpecList.get(1).getKey().getMachineName();
    		String checkUnit3 = trackUnitSpecList.get(2).getKey().getMachineName();
    		
    		
    		String checkUnit1Recipe1 = recipeName.substring(0, 4);
    		String checkUnit1Recipe2 = recipeName.substring(4, 8);
    		String checkUnit1Recipe3 = recipeName.substring(8, 12);
    		
    		for(int i=0; i<3; i++)
    		{
    			String unitName = "";
    			String unitRecipe = "";
    			
    			if(i==0)
    			{
    				unitName = checkUnit1;
    				unitRecipe = checkUnit1Recipe1;
    			}
    			else if(i == 1)
    			{
    				unitName = checkUnit2;
    				unitRecipe = checkUnit1Recipe2;
    			}
    			else if(i == 2)
    			{
    				unitName = checkUnit3;
    				unitRecipe = checkUnit1Recipe3;
    			}
    			
    			/* 20190520, hhlee, delete try ~ catch ~ ==>> */    			
    			/* 20190520, hhlee, add inquery unit recipe data try ~ catch ~ ==>> */
    			Recipe recipeData = null;
                try
                {
                    recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(unitName, unitRecipe);
                }
                catch(Exception ex)
                {
                    throw new CustomException("RMS-0003", unitName, unitRecipe);
                }
                /* <<== 20190520, hhlee, add inquery unit recipe data try ~ catch ~ */
                                                
                //Unit Only Case
                if(recipeData.getCompareFlag().equals("Y"))
                {
                    if(recipeData.getCompareResult().equals("NG"))
                    {
                        throw new CustomException("RMS-0006", unitName, unitRecipe);
                    }
                    
                    if(recipeData.getActiveResult().equals("NG"))
                    {
                        throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
                    }
                }
                //Sub Unit Exist Case
                else if(recipeData.getCompareFlag().equals("-"))
                {   
                	if(recipeData.getRelationFlag().equals("Y"))
                	{
                		List<RecipeRelation> recipeRelList = null;
                        
                        try
                        {
                            recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});       
                        }
                        catch(Exception ex)
                        {
                            throw new CustomException("RMS-0004", recipeData.getMachineName(), recipeData.getRecipeName());
                        }
                        
                        if(recipeData.getActiveResult().equals("NG"))
                        {
                        	throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
                        }
                        

                        for(RecipeRelation recipeRelData : recipeRelList)
                        {
                            /* 20190520, hhlee, add inquery child recipe data try ~ catch ~ ==>> */
                            Recipe childRecipeData = null;
                            try
                            {
                                childRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe()});
                            }
                            catch(Exception ex)
                            {
                                throw new CustomException("RMS-0003", recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe());
                            }
                            /* <<== 20190520, hhlee, add inquery child recipe data try ~ catch ~ ==>> */
                            
                            if(childRecipeData.getCompareFlag().equals("Y") && childRecipeData.getCompareResult().equals("NG"))
                            {
                                throw new CustomException("RMS-0002", childRecipeData.getMachineName(), childRecipeData.getRecipeName());
                            }
                            
                            if(childRecipeData.getCompareFlag().equals("Y") && childRecipeData.getActiveResult().equals("NG"))
                            {
                                throw new CustomException("RMS-0007", childRecipeData.getMachineName(), childRecipeData.getRecipeName());
                            }
                        }
                	}
                	
                    
                }                
                /* <<== 20190520, hhlee, delete try ~ catch ~ ==>> */
                
    			//try
    			//{
    		    //	Recipe recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(unitName, unitRecipe);
    		    //	
    		    //	//Unit Only Case
    		    //	if(recipeData.getCompareFlag().equals("Y"))
    		    //	{
        		//    	if(recipeData.getCompareResult().equals("NG"))
        		//    	{
        		//    		throw new CustomException("RMS-0006", unitName, unitRecipe);
        		//    	}
        		//    	
    			//		if(recipeData.getActiveResult().equals("NG"))
    			//		{
    			//			throw new CustomException("RMS-0007", recipeData.getMachineName(), recipeData.getRecipeName());
    			//		}
    		    //	}
    		    //	//Sub Unit Exist Case
    		    //	else if(recipeData.getCompareFlag().equals("-"))
    		    //	{	
	    	    //		List<RecipeRelation> recipeRelList = null;
	    	    //		
	    	    //		try
	    	    //		{
	    	    //			recipeRelList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{recipeData.getMachineName(), recipeData.getRecipeName()});		
	    	    //		}
	    	    //		catch(Exception ex)
	    	    //		{
	    	    //			throw new CustomException("RMS-0004", recipeData.getMachineName(), recipeData.getRecipeName());
	    	    //		}
	    	    //		
                //
	    	    //		for(RecipeRelation recipeRelData : recipeRelList)
	    	    //		{
	    	    //			Recipe childRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{recipeRelData.getChildMachine(), recipeRelData.getChildMachineRecipe()});
	    	    //			
    	    	//			if(childRecipeData.getCompareFlag().equals("Y") && childRecipeData.getCompareResult().equals("NG"))
    	    	//			{
    	    	//				throw new CustomException("RMS-0002", childRecipeData.getMachineName(), childRecipeData.getRecipeName());
    	    	//			}
    	    	//			
    			//			if(childRecipeData.getCompareFlag().equals("Y") && childRecipeData.getActiveResult().equals("NG"))
    			//			{
    			//				throw new CustomException("RMS-0007", childRecipeData.getMachineName(), childRecipeData.getRecipeName());
    			//			}
	    	    //		}
    		    //	}
    		    //	
    			//}
    			//catch(Exception ex)
    			//{
    			//	throw new CustomException("RMS-0002", unitName, unitRecipe);
    			//}
    		}
    		
    		return recipeName;
    	}
    	catch(Exception ex)
    	{
    		throw ex;
    	}
    }
    	
	//2018.08.15 dmlee : Validation Parameter
	public Boolean compareCheckParameter(RecipeParameter paraData) throws CustomException
	{
		try
		{
			if(paraData.getValidationType().equals("Exact"))
			{
				try
				{
					//Number
					if(Double.parseDouble(paraData.getValue()) == Double.parseDouble(paraData.getTarget()))
					{
						return true;
					}
				}
				catch(Exception ex)
				{
					//No Number
					if(paraData.getValue().equals(paraData.getTarget()))
					{
						return true;
					}
				}
			}
			else if(paraData.getValidationType().equals("Range"))
			{
				if(Double.parseDouble(paraData.getValue()) >= Double.parseDouble(paraData.getLowerLimit()) && Double.parseDouble(paraData.getValue()) <= Double.parseDouble(paraData.getUpperLimit()))
				{
					return true;
				}
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("RMS-0006", paraData.getRecipeParameterName());
		}
		
		return false;
	}
	
	
	//2018.08.16 dmlee : All SubUnit, Unit, Machine Compare
	public void compareRecipeParameterAll(EventInfo eventInfo, String subUnitName, String subUnitRecipeName, String allParamCompareResult) throws CustomException
	{
		Recipe subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{subUnitName, subUnitRecipeName});
		
		//Validate CheckOut
		if(subUnitRecipeData.getCheckState().equals("CheckedOut"))
		{
			if(!subUnitRecipeData.getCheckOutUser().equals(eventInfo.getEventUser()))
			{
				throw new CustomException("RECIPE-0100");
			}
		}
		
		if(subUnitRecipeData.getCompareFlag().equals("Y"))
		{
			subUnitRecipeData.setCompareResult(allParamCompareResult);
			
			if(subUnitRecipeData.getCompareResult().equals("NG"))
			{
				subUnitRecipeData.setRecipeState("UnApproved");
				subUnitRecipeData.setActiveResult("NG");
			}
			else
			{
				subUnitRecipeData.setRecipeState("Approved");
				subUnitRecipeData.setApproveTime(eventInfo.getEventTime());
			}
		}
		
		eventInfo.setEventName("Compare");
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		ExtendedObjectProxy.getRecipeService().modify(eventInfo, subUnitRecipeData);
		
		try
		{
			List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{subUnitName, subUnitRecipeName});
			
			for(RecipeRelation recipeRelationData : recipeRelationList)
			{
				String parentMachineName = recipeRelationData.getParentMachine();
				String parentMachineRecipeName = recipeRelationData.getParentMachineRecipe();
				
				
				String sql = "SELECT RC.MACHINENAME, " +
						"       RC.RECIPENAME, " +
						"       RC.COMPARERESULT " +
						"FROM   CT_RECIPE RC, " +
						"       CT_RECIPERELATION R " +
						"WHERE R.PARENTMACHINE = :MACHINENAME " +
						"      AND R.PARENTMACHINERECIPE = :RECIPENAME " +
						"      AND RC.MACHINENAME = R.CHILDMACHINE " +
						"      AND RC.RECIPENAME = R.CHILDMACHINERECIPE " +
						"      AND RC.RECIPETYPE = :RECIPETYPE " ;
						//"      AND RC.COMPAREFLAG = 'Y' " ;

				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MACHINENAME", parentMachineName);
				bindMap.put("RECIPENAME", parentMachineRecipeName);
				bindMap.put("RECIPETYPE", "SUBUNIT");
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if ( sqlResult.size() > 0)
				{	
					boolean allCompareFlag = true;
				
					for(Map<String, Object> map : sqlResult)
					{
						try
						{
							if(((String)map.get("COMPARERESULT")).equals("NG"))
							{
								allCompareFlag = false;
								break;
							}
						}
						catch(Exception ex)
						{
							
						}
					}
				}
				
				try
				{
					List<RecipeRelation> recipeRelationList2 = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{parentMachineName, parentMachineRecipeName});
					
					for(RecipeRelation recipeRelationData2 : recipeRelationList2)
					{
						String mainMachineName = recipeRelationData2.getParentMachine();
						String mainMachineRecipeName = recipeRelationData2.getParentMachineRecipe();
						
						
						String sql2 = "SELECT RC.MACHINENAME, " +
								"       RC.RECIPENAME, " +
								"       RC.COMPARERESULT " +
								"FROM   CT_RECIPE RC, " +
								"       CT_RECIPERELATION R " +
								"WHERE R.PARENTMACHINE = :MACHINENAME " +
								"      AND R.PARENTMACHINERECIPE = :RECIPENAME " +
								"      AND RC.MACHINENAME = R.CHILDMACHINE " +
								"      AND RC.RECIPENAME = R.CHILDMACHINERECIPE " +
								"      AND RC.RECIPETYPE = :RECIPETYPE " ;
						
						Map<String, Object> bindMap2 = new HashMap<String, Object>();
						bindMap2.put("MACHINENAME", mainMachineName);
						bindMap2.put("RECIPENAME", mainMachineRecipeName);
						bindMap2.put("RECIPETYPE", "UNIT");
						
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
						
						if ( sqlResult2.size() > 0)
						{	
							boolean allCompareFlag = true;
						
							for(Map<String, Object> map : sqlResult2)
							{
								try
								{
									if(((String)map.get("COMPARERESULT")).equals("NG"))
									{
										allCompareFlag = false;
										break;
									}
								}
								catch(Exception ex)
								{
									
								}
							}
							
						}
					}
				}
				catch(Exception ex)
				{
					logger.warn("Recipe Auto Compare Fail (Non Exsit Parent Recipe !)");
				}
			}
		}
		catch(Exception ex)
		{
			logger.warn("Recipe Auto Compare Fail !");
		}
		
	}
	
	
	//2018.08.16 dmlee : All Unut, Machine Compare
	public void compareUnitRecipeParameterAll(EventInfo eventInfo, String unitName, String unitRecipeName, String allParamCompareResult) throws CustomException
	{
		Recipe unitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{unitName, unitRecipeName});
		
		//Validate CheckOut
		if(unitRecipeData.getCheckState().equals("CheckedOut"))
		{
			if(!unitRecipeData.getCheckOutUser().equals(eventInfo.getEventUser()))
			{
				throw new CustomException("RECIPE-0100");
			}
		}
		
		if(unitRecipeData.getCompareFlag().equals("N"))
		{
			if(unitRecipeData.getCompareResult().equals("OK"))
			{
				unitRecipeData.setCompareResult("NG");
				unitRecipeData.setRecipeState("UnApproved");
				eventInfo.setEventName("Compare");
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				ExtendedObjectProxy.getRecipeService().modify(eventInfo, unitRecipeData);
				
				return;
			}
			else
			{
				return;
			}
		}
		
		unitRecipeData.setCompareResult(allParamCompareResult);
		
		if(unitRecipeData.getCompareResult().equals("NG"))
		{
			unitRecipeData.setRecipeState("UnApproved");
			unitRecipeData.setActiveResult("NG");
		}
		else
		{
			unitRecipeData.setRecipeState("Approved");
			unitRecipeData.setApproveTime(eventInfo.getEventTime());
		}
		
		eventInfo.setEventName("Compare");
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		unitRecipeData.setLastEventName(eventInfo.getEventName());
		unitRecipeData.setLastEventTime(eventInfo.getEventTime());
		unitRecipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		unitRecipeData.setLastEventUser(eventInfo.getEventUser());
		unitRecipeData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getRecipeService().modify(eventInfo, unitRecipeData);
		
		

		try
		{
			List<RecipeRelation> recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE CHILDMACHINE = ? AND CHILDMACHINERECIPE = ? ", new Object[]{unitName, unitRecipeName});
			
			for(RecipeRelation recipeRelationData : recipeRelationList)
			{
				String parentMachineName = recipeRelationData.getParentMachine();
				String parentMachineRecipeName = recipeRelationData.getParentMachineRecipe();
				
				//All Unit Check
				String sql = "SELECT RC.MACHINENAME, " +
						"       RC.RECIPENAME, " +
						"       RC.COMPARERESULT " +
						"FROM   CT_RECIPE RC, " +
						"       CT_RECIPERELATION R " +
						"WHERE R.PARENTMACHINE = :MACHINENAME " +
						"      AND R.PARENTMACHINERECIPE = :RECIPENAME " +
						"      AND RC.MACHINENAME = R.CHILDMACHINE " +
						"      AND RC.RECIPENAME = R.CHILDMACHINERECIPE " +
						"      AND RC.RECIPETYPE = :RECIPETYPE " +
						"      AND RC.COMPAREFLAG = :COMPAREFLAG " ;
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MACHINENAME", parentMachineName);
				bindMap.put("RECIPENAME", parentMachineRecipeName);
				bindMap.put("RECIPETYPE", "UNIT");
				bindMap.put("COMPAREFLAG", "Y");
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if ( sqlResult.size() > 0)
				{	
					boolean allCompareFlag = true;
				
					for(Map<String, Object> map : sqlResult)
					{	
						try
						{
							if(((String)map.get("COMPARERESULT")).equals("NG"))
							{
								allCompareFlag = false;
								break;
							}
						}
						catch(Exception ex)
						{
							
						}
					}

					
				}
			}
		}
		catch(Exception ex)
		{
			logger.warn("Recipe Auto Compare Fail !");
		}
		
	}
	
	/**
	 * 
	 * @Name     getUnitRecipeList
	 * @since    2018. 9. 25.
	 * @author   hhlee
	 * @contents 
	 *           
	 * @param machineName
	 * @param machineRecipeName
	 * @return
	 * @throws CustomException
	 */
    public String getUnitRecipeList(String machineName, String machineRecipeName) throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        String unitRecipeList = "";
        String strSql = StringUtil.EMPTY;
        
        /* 20181210, hhlee, modify, Get Unit Recipe List ==>> */
        //strSql =  " SELECT SQ.MACHINENAME                                              AS MACHINENAME,                     \n"
        //+ "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITNAME, '|')), 2)       AS UNITNAME,                        \n"
        //+ "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITRECIPENAME, '|')), 2) AS UNITRECIPENAME                   \n"
        //+ "   FROM (                                                                                               \n"
        //+ "         SELECT rr.PARENTMACHINE AS MACHINENAME,                                                        \n"
        //+ "                rr.PARENTMACHINERECIPE AS MACHINERECIPENAME,                                            \n"
        //+ "                rr.CHILDMACHINE AS UNITNAME,                                                            \n"
        //+ "                rr.CHILDMACHINERECIPE AS UNITRECIPENAME,                                                \n"
        //+ "                ROW_NUMBER () OVER (PARTITION BY rr.PARENTMACHINE ORDER BY rr.PARENTMACHINERECIPE) RNUM \n"
        //+ "           FROM CT_RECIPERELATION rr,                                                                   \n"
        //+ "                MACHINE m,                                                                              \n"
        //+ "                MACHINESPEC ms                                                                          \n"
        //+ "          WHERE 1=1                                                                                     \n"
        //+ "            AND rr.PARENTMACHINE = :PARENTMACHINE                                                       \n"
        //+ "            AND rr.PARENTMACHINE = ms.MACHINENAME                                                       \n"
        //+ "            AND ms.MACHINENAME = m.MACHINENAME                                                          \n"
        //+ "            AND rr.PARENTMACHINERECIPE = (CASE WHEN ms.CONSTRUCTTYPE = 'TRACK' OR                       \n"
        //+ "                                                    ms.RMSUNITRECIPEONLY = 'Y'                          \n"
        //+ "                                               THEN '-' ELSE :PARENTMACHINERECIPE END)                  \n"
        //+ "         GROUP BY rr.PARENTMACHINE,                                                                     \n"
        //+ "                  rr.PARENTMACHINERECIPE,                                                               \n"
        //+ "                  rr.CHILDMACHINE,                                                                      \n"
        //+ "                  rr.CHILDMACHINERECIPE                                                                 \n"
        //+ "         ) SQ                                                                                           \n"
        //+ "  START WITH SQ.RNUM = 1                                                                                \n"
        //+ "  CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                \n"
        //+ "    GROUP BY SQ.MACHINENAME                                                                             \n";
        
        strSql =  " SELECT SQ.MACHINENAME                                              AS MACHINENAME,                     \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITNAME, '|')), 2)       AS UNITNAME,                        \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITRECIPENAME, '|')), 2) AS UNITRECIPENAME                   \n"
                + "   FROM (                                                                                               \n"
                + "         SELECT rr.PARENTMACHINE AS MACHINENAME,                                                        \n"
                + "                rr.PARENTMACHINERECIPE AS MACHINERECIPENAME,                                            \n"
                + "                rr.CHILDMACHINE AS UNITNAME,                                                            \n"
                + "                rr.CHILDMACHINERECIPE AS UNITRECIPENAME,                                                \n"
                + "                ROW_NUMBER () OVER (PARTITION BY rr.PARENTMACHINE ORDER BY rr.PARENTMACHINERECIPE) RNUM \n"
                + "           FROM CT_RECIPERELATION rr,                                                                   \n"
                + "                MACHINE m,                                                                              \n"
                + "                MACHINESPEC ms                                                                          \n"
                + "          WHERE 1=1                                                                                     \n"
                + "            AND rr.PARENTMACHINE = :PARENTMACHINE                                                       \n"
                + "            AND rr.PARENTMACHINE = ms.MACHINENAME                                                       \n"
                + "            AND ms.MACHINENAME = m.MACHINENAME                                                          \n"
                + "            AND rr.PARENTMACHINERECIPE = :PARENTMACHINERECIPE                                           \n"
                + "         GROUP BY rr.PARENTMACHINE,                                                                     \n"
                + "                  rr.PARENTMACHINERECIPE,                                                               \n"
                + "                  rr.CHILDMACHINE,                                                                      \n"
                + "                  rr.CHILDMACHINERECIPE                                                                 \n"
                + "         ) SQ                                                                                           \n"
                + "  START WITH SQ.RNUM = 1                                                                                \n"
                + "  CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                \n"
                + "    GROUP BY SQ.MACHINENAME                                                                             \n";
        /* <<== 20181210, hhlee, modify, Get Unit Recipe List */
        
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("PARENTMACHINE", machineName);
        bindMap.put("PARENTMACHINERECIPE", machineRecipeName);
        
        try
        {
            List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

            /* 20190124, hhlee, modify, change Type casting ==>> */
            if(sqlResult != null && sqlResult.size() > 0)
            {
                unitRecipeList = sqlResult.get(0).get("UNITRECIPENAME") != null ? sqlResult.get(0).get("UNITRECIPENAME").toString() : StringUtil.EMPTY;
            }
            /* <<== 20190124, hhlee, modify, change Type casting */
        }
        catch (Exception de)
        {            
        }        

        logger.info(String.format("quit from [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        return unitRecipeList;
    }
    
    /**
     * 
     * @Name     getUnitRecipeList
     * @since    2019. 2. 20.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param machineRecipeName
     * @param procUnitNameList
     * @return
     * @throws CustomException
     */
    public String getUnitRecipeList(String machineName, String machineRecipeName, String procUnitNameList) throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        String unitRecipeList = StringUtil.EMPTY;
        String strSql = StringUtil.EMPTY;
        
        strSql =  " SELECT SQ.MACHINENAME                                              AS MACHINENAME,                     \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITNAME, '|')), 2)       AS UNITNAME,                        \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITRECIPENAME, '|')), 2) AS UNITRECIPENAME                   \n"
                + "   FROM (                                                                                               \n"
                + "         SELECT rr.PARENTMACHINE AS MACHINENAME,                                                        \n"
                + "                rr.PARENTMACHINERECIPE AS MACHINERECIPENAME,                                            \n"
                + "                rr.CHILDMACHINE AS UNITNAME,                                                            \n"
                + "                rr.CHILDMACHINERECIPE AS UNITRECIPENAME,                                                \n"
                + "                ROW_NUMBER () OVER (PARTITION BY rr.PARENTMACHINE ORDER BY rr.PARENTMACHINERECIPE) RNUM \n"
                + "           FROM CT_RECIPERELATION rr,                                                                   \n"
                + "                MACHINE m,                                                                              \n"
                + "                MACHINESPEC ms                                                                          \n"
                + "          WHERE 1=1                                                                                     \n"
                + "            AND rr.PARENTMACHINE = :PARENTMACHINE                                                       \n"
                + "            AND rr.PARENTMACHINE = ms.MACHINENAME                                                       \n"
                + "            AND ms.MACHINENAME = m.MACHINENAME                                                          \n"
                + "            AND rr.PARENTMACHINERECIPE = :PARENTMACHINERECIPE                                           \n"
                + "         GROUP BY rr.PARENTMACHINE,                                                                     \n"
                + "                  rr.PARENTMACHINERECIPE,                                                               \n"
                + "                  rr.CHILDMACHINE,                                                                      \n"
                + "                  rr.CHILDMACHINERECIPE                                                                 \n"
                + "         ) SQ                                                                                           \n"
                + "  START WITH SQ.RNUM = 1                                                                                \n"
                + "  CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                \n"
                + "    GROUP BY SQ.MACHINENAME                                                                             \n";
                
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("PARENTMACHINE", machineName);
        bindMap.put("PARENTMACHINERECIPE", machineRecipeName);
        
        try
        {
            List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

            /* 20190124, hhlee, modify, change Type casting ==>> */
            if(sqlResult != null && sqlResult.size() > 0)
            {
                String unitNameList = sqlResult.get(0).get("UNITNAME") != null ? sqlResult.get(0).get("UNITNAME").toString() : StringUtil.EMPTY;
                String unitRecipeNameList = sqlResult.get(0).get("UNITRECIPENAME") != null ? sqlResult.get(0).get("UNITRECIPENAME").toString() : StringUtil.EMPTY;
                
                String[] splitUnitName = StringUtil.split(unitNameList , "|");
                String[] splitUnitRecipeName = StringUtil.split(unitRecipeNameList , "|");
                
                String[] splitProcUnitName = StringUtil.split(procUnitNameList , "|");                
                
                for(int i = 0; i < splitProcUnitName.length; i++ )
                {
                    try
                    {
                        for(int j = 0; j < splitUnitName.length; j++ )
                        {
                            if(StringUtil.equals(splitProcUnitName[i], splitUnitName[j]))
                            {
                                unitRecipeList += splitUnitRecipeName[j] + "|";
                                break;
                            }                        
                        }
                    }
                    catch (Exception ex)
                    {
                        logger.error(ex.getMessage());            
                    }
                }        
                
                unitRecipeList = StringUtil.substring(unitRecipeList, 0, unitRecipeList.length() -1);
            }
            /* <<== 20190124, hhlee, modify, change Type casting */
        }
        catch (Exception de)
        {            
        }        

        logger.info(String.format("quit from [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        return unitRecipeList;
    }
    
    /**
     * 
     * @Name     getUnitRecipeListByUnitName
     * @since    2019. 2. 25.
     * @author   hhlee
     * @contents 
     *           
     * @param machineName
     * @param machineRecipeName
     * @param unitName
     * @return
     * @throws CustomException
     */
    public String getUnitRecipeListByUnitName(String machineName, String machineRecipeName, String unitName) throws CustomException
    {
        logger.info(String.format("entry to [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        String unitNameList = StringUtil.EMPTY;
        String unitRecipeNameList = StringUtil.EMPTY;
        String strSql = StringUtil.EMPTY;
        
        strSql =  " SELECT SQ.MACHINENAME                                              AS MACHINENAME,                     \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITNAME, ',')), 2)       AS UNITNAME,                        \n"
                + "        SUBSTR(MAX(SYS_CONNECT_BY_PATH(SQ.UNITRECIPENAME, ',')), 2) AS UNITRECIPENAME                   \n"
                + "   FROM (                                                                                               \n"
                + "         SELECT rr.PARENTMACHINE AS MACHINENAME,                                                        \n"
                + "                rr.PARENTMACHINERECIPE AS MACHINERECIPENAME,                                            \n"
                + "                rr.CHILDMACHINE AS UNITNAME,                                                            \n"
                + "                rr.CHILDMACHINERECIPE AS UNITRECIPENAME,                                                \n"
                + "                ROW_NUMBER () OVER (PARTITION BY rr.PARENTMACHINE ORDER BY rr.PARENTMACHINERECIPE) RNUM \n"
                + "           FROM CT_RECIPERELATION rr,                                                                   \n"
                + "                MACHINE m,                                                                              \n"
                + "                MACHINESPEC ms                                                                          \n"
                + "          WHERE 1=1                                                                                     \n"
                + "            AND rr.PARENTMACHINE = :PARENTMACHINE                                                       \n"
                + "            AND rr.PARENTMACHINE = ms.MACHINENAME                                                       \n"
                + "            AND ms.MACHINENAME = m.MACHINENAME                                                          \n"
                + "            AND rr.PARENTMACHINERECIPE = :PARENTMACHINERECIPE                                           \n"
                + "            AND rr.CHILDMACHINE = :CHILDMACHINE                                                         \n"
                + "         GROUP BY rr.PARENTMACHINE,                                                                     \n"
                + "                  rr.PARENTMACHINERECIPE,                                                               \n"
                + "                  rr.CHILDMACHINE,                                                                      \n"
                + "                  rr.CHILDMACHINERECIPE                                                                 \n"
                + "         ) SQ                                                                                           \n"
                + "  START WITH SQ.RNUM = 1                                                                                \n"
                + "  CONNECT BY PRIOR SQ.RNUM = SQ.RNUM - 1                                                                \n"
                + "    GROUP BY SQ.MACHINENAME                                                                             \n";
                
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("PARENTMACHINE", machineName);
        bindMap.put("PARENTMACHINERECIPE", machineRecipeName);
        bindMap.put("CHILDMACHINE", unitName);
        
        try
        {
            List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

            if(sqlResult != null && sqlResult.size() > 0)
            {
                unitNameList = sqlResult.get(0).get("UNITNAME") != null ? sqlResult.get(0).get("UNITNAME").toString() : StringUtil.EMPTY;
                unitRecipeNameList = sqlResult.get(0).get("UNITRECIPENAME") != null ? sqlResult.get(0).get("UNITRECIPENAME").toString() : StringUtil.EMPTY;
                
            }
        }
        catch (Exception de)
        {            
        }        

        logger.info(String.format("quit from [%s] at [%d]", "getUnitRecipeList", System.currentTimeMillis()));

        return unitRecipeNameList;
    }
    
    //18.12.19 dmlee : Send RMS Alarm
    public void sendByRMSCreateAlarm(EventInfo eventInfo, Document doc, String alarmCode, String machineName, String recipeName, List<String> changeRecipeList, List<String> changeParamList) throws CustomException
    {
    	try
    	{
    		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{machineName, recipeName});
    		
    		AlarmDefinition alarmDefData = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[]{alarmCode});
    		
    		boolean sendFlag = true;
    		
    		if(recipeData.getRecipeType().equals("MAIN") && !recipeData.getRelationFlag().equals("Y") && alarmDefData.getAlarmCode().equals("RMS-REL-CH"))
    		{
    			sendFlag = false;
    		}
    		
    		if(!recipeData.getRecipeType().equals("MAIN") && !recipeData.getCompareFlag().equals("Y") && alarmDefData.getAlarmCode().equals("RMS-COM-NG"))
    		{
    			sendFlag = false;
    		}
    		
    		if(sendFlag)
    		{
        		
        		//2018.12.26 dmlee : Case 1. Code [RMS-COM-NG] (Compare NG, Please Check)
        		if(alarmDefData.getAlarmCode().equals("RMS-COM-NG"))
        		{
        			logger.info(String.format("Machine [%s] Recipe [%s] Compare NG, Please Check, Send E-mail", "machineName", "recipeName"));
        		}
        		//2018.12.26 dmlee : Case 2. Code [RMS-COM-OK] (Compae OK, But Active Result NG, Please Active Recipe)
        		else if(alarmDefData.getAlarmCode().equals("RMS-COM-OK"))
        		{
        			logger.info(String.format("Machine [%s] Recipe [%s] Compae OK, But Active Result NG, Please Active Recipe, Send E-mail", "machineName", "recipeName"));
        		}
        		//2018.12.26 dmlee : Case 1. Code [RMS-REL-CH] (Relation Changed, Please Active Recipe)
        		else if(alarmDefData.getAlarmCode().equals("RMS-REL-CH"))
        		{
        			logger.info(String.format("Machine [%s] Recipe [%s] Relation Changed, Please Active Recipe, Send E-mail", "machineName", "recipeName"));
        		}
        		else
        		{
        			logger.error(String.format("E-Mail Send Fail !, Please Check Alarm Definition"));
        		}

        		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CreateAlarm");
    			
        		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
    			
    			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
    			
    			Element element1 = new Element("ALARMCODE");
    			element1.setText(alarmDefData.getAlarmCode());
    			eleBodyTemp.addContent(element1);
    			
    			Element element2 = new Element("MACHINENAME");
    			element2.setText(machineName);
    			eleBodyTemp.addContent(element2);
    			
    			Element element3 = new Element("RECIPENAME");
    			element3.setText(recipeName); //
    			eleBodyTemp.addContent(element3);
    			
    			
    			String changeRecipeListStr = "";
    			if(changeRecipeList != null)
    			{
    				changeRecipeListStr = changeRecipeList.toString();
    			}
    			
    			Element element4 = new Element("CHANGERECIPELIST");
    			element4.setText(changeRecipeListStr);
    			eleBodyTemp.addContent(element4);
    			
    			
    			
    			String changeParamListStr = "";
    			if(changeParamList != null)
    			{
    				changeParamListStr = changeParamList.toString();
    			}
    			
    			Element element5 = new Element("CHANGEPARAMLIST");
    			element5.setText(changeParamListStr);
    			eleBodyTemp.addContent(element5);
    			
    			
    			
    			SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", eventInfo.getEventComment());
    			
    			//overwrite
    			doc.getRootElement().addContent(eleBodyTemp);
    			
    			//Send ALM Server : Create Alarm
    			GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
    		}

    	}
    	catch(Exception ex)
    	{
    		logger.error(String.format("E-Mail Send Fail !"));
    	}
    }
    
    /**
     * 
     * @Name     validateRecipeParameterPhotoMaskName
     * @since    2019. 5. 14.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineName
     * @param machineRecipeName
     * @param photoMaskName
     * @throws CustomException
     */
    public void validateRecipeParameterPhotoMaskName(EventInfo eventInfo, String machineName, String machineRecipeName, String photoMaskName) throws CustomException
    {
        String recipeParameterMaskName = StringUtil.EMPTY; 
        List<Map<String, Object>> photoMaskNameList = null;
        
        if(StringUtil.isNotEmpty(photoMaskName))
        {
            logger.info("Event Name = validateRecipeParameterPhotoMaskName , EventTimeKey" + eventInfo.getEventTimeKey());
            
            String unit1RecipeName = machineRecipeName.substring(0, 4);
            String unit2RecipeName = machineRecipeName.substring(4, 8);
            String unit3RecipeName = machineRecipeName.substring(8, 12);
            
            try
            {
                String strSql = " SELECT RR.PARENTMACHINE, R.MACHINENAME, R.RECIPETYPE, RP.RECIPENAME,               \n"
                              + "        RP.RECIPEPARAMETERNAME, RP.VALUE AS PARAMETERVALUE,                         \n"
                              + "        R.COMPAREFLAG AS RECIPECOMPAREFLAG, R.COMPARERESULT AS RECIPECOMPARERESULT, \n"
                              + "        R.RELATIONFLAG AS RECIPERELATIONFLAG, R.ACTIVERESULT AS RECIPEACTIVERESULT, \n"
                              + "        RP.COMPARERESULT AS PARAMETERCOMPARERESULT                                  \n"
                              + "   FROM CT_RECIPE R, CT_RECIPERELATION RR, CT_RECIPEPARAMETER RP                    \n"
                              + "  WHERE 1=1                                                                         \n"
                              + "    AND RR.PARENTMACHINE = :PARENTMACHINE                                           \n"
                              + "    AND RR.CHILDMACHINE = R.MACHINENAME                                             \n"
                              + "    AND RR.CHILDMACHINERECIPE = R.RECIPENAME                                        \n"
                              + "    AND R.MACHINENAME = RP.MACHINENAME                                              \n"
                              + "    AND R.RECIPENAME = RP.RECIPENAME                                                \n"
                              + "    AND R.RECIPETYPE = :RECIPETYPE                                                  \n"
                              + "    AND R.RECIPENAME = :RECIPENAME                                                  \n"
                              + "    AND RP.RECIPEPARAMETERNAME = :RECIPEPARAMETERNAME                               \n"
                              //+ "    AND RP.VALUE = :MASKNAME                                                        \n"
                              + " ORDER BY RR.PARENTMACHINE, R.MACHINENAME, R.RECIPETYPE, RP.RECIPENAME                ";
                
                Map<String, Object> bindMap = new HashMap<String, Object>();
                bindMap.put("PARENTMACHINE", machineName);
                bindMap.put("RECIPETYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT);
                bindMap.put("RECIPENAME", unit2RecipeName);
                bindMap.put("RECIPEPARAMETERNAME", GenericServiceProxy.getConstantMap().RECIPE_PARAMETERNAME_PHOTOMASKNAME);
                //bindMap.put("MASKNAME", photoMaskName);            
                
                photoMaskNameList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
                
            }
            catch(Exception ex)
            {
                //logger.error("[validateRecipeParameterPhotoMaskName] " + ex);
                throw new CustomException("RMS-0009", machineName, machineRecipeName);
            }  
            
            if(photoMaskNameList != null && photoMaskNameList.size() > 0)
            {
                recipeParameterMaskName = (photoMaskNameList.get(0).get("PARAMETERVALUE") != null ? 
                        photoMaskNameList.get(0).get("PARAMETERVALUE").toString() : StringUtil.EMPTY);
                
                if(!StringUtil.equals(photoMaskName, recipeParameterMaskName))
                {
                    throw new CustomException("MASK-0100",photoMaskName, machineName, recipeParameterMaskName, machineRecipeName);
                }                
            }
            else
            {
                throw new CustomException("RMS-0009", machineName, machineRecipeName);
            }   
        }
    }
    
    /**
     * 
     * @Name     validateReceivedMachineRecipeByOnLineLocal
     * @since    2019. 5. 20.
     * @author   hhlee
     * @contents 
     *           
     * @param eventInfo
     * @param machineSpecData
     * @param machineName
     * @param machineRecipeName
     * @throws CustomException
     */
    public void validateReceivedMachineRecipeByOnLineLocal(EventInfo eventInfo, 
            MachineSpec machineSpecData, String machineName,String machineRecipeName) throws CustomException
    {        
        logger.info("Event Name = validateReceivedMachineRecipeFromMachine , EventTimeKey" + eventInfo.getEventTimeKey());
        
        if(machineSpecData == null)
        {
            machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        }
        
        if(StringUtil.equals(machineSpecData.getUdfs().get("RMSFLAG").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
        {
            try
            {   
                //If Track Machine 
                if(StringUtil.equals(machineSpecData.getUdfs().get("CONSTRUCTTYPE") , GenericServiceProxy.getConstantMap().ConstructType_PHOTO))
                {
                    machineRecipeName = this.checkRMSCompareResultForTrackMachine(machineName, machineRecipeName);
                }
                //Unit Recipe Only Flag = 'Y'
                else if(StringUtil.equals(machineSpecData.getUdfs().get("RMSUNITRECIPEONLY").toString(), GenericServiceProxy.getConstantMap().FLAG_Y))
                {
                    List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(
                            " WHERE DETAILMACHINETYPE = ? AND SUPERMACHINENAME = ? AND MACHINETYPE = ? ORDER BY MACHINENAME ASC", 
                            new Object[]{GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT, machineName, GenericServiceProxy.getConstantMap().Mac_ProductionMachine});

                    this.checkRMSCompareResult(unitSpecList.get(0).getKey().getMachineName(), machineRecipeName, true);
                }
                else
                {
                    this.checkRMSCompareResult(machineName, machineRecipeName, false);
                }                    
            }
            catch(Exception ex)
            {
                throw ex;
            }
        }
        
    }
}
