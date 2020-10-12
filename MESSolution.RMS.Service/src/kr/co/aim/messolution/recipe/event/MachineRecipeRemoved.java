package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeRelation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class MachineRecipeRemoved extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
		String machineRecipeName = StringUtil.EMPTY;
		String currentCommunicationName = StringUtil.EMPTY;
		Recipe recipeInfo = null;
		Machine machineData = null;
		
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
            
			machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			/* Machine Validation */
            machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            currentCommunicationName = machineData.getCommunicationState();
            onLineInitialCommState = CommonUtil.getValue(machineData.getUdfs(), "ONLINEINITIALCOMMSTATE");
            
			machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);			
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
			
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
			
			
			//Remove Recipe Data
			try
			{
				Recipe recipeData = ExtendedObjectProxy.getRecipeService().getRecipeInfo(machineName, machineRecipeName);
				ExtendedObjectProxy.getRecipeService().remove(eventInfo, recipeData);
			}
			catch(Exception ex)
			{
				
			}
			
			
			List<RecipeRelation> recipeRelationList = null;
			try
			{
				recipeRelationList = ExtendedObjectProxy.getRecipeRelationService().select("WHERE PARENTMACHINE = ? AND PARENTMACHINERECIPE = ? ", new Object[]{machineName, machineRecipeName});
			}
			catch(Exception ex)
			{
				
			}
			
			//Remove Recipe Relation
			if(recipeRelationList != null)
			{
				for(RecipeRelation recipeRelData : recipeRelationList)
				{
					try
					{
						ExtendedObjectProxy.getRecipeRelationService().remove(eventInfo, recipeRelData);
					}
					catch(Exception ex)
					{
						
					}
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
					SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"MachineRecipeRemovedCheckReply");

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
                SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"MachineRecipeCreatedCheckReply");

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
