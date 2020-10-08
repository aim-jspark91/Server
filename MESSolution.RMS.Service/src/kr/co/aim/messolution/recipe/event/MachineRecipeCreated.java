package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

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
import org.jdom.Element;

public class MachineRecipeCreated extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

        String machineName = StringUtil.EMPTY;
		String machineRecipeName = StringUtil.EMPTY;
		String seq = StringUtil.EMPTY;

		String onLineInitialCommState = StringUtil.EMPTY;
		
		String factoryCode = StringUtil.EMPTY;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
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
			Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
            onLineInitialCommState = CommonUtil.getValue(machineData.getUdfs(), "ONLINEINITIALCOMMSTATE");
            
			machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
			seq = SMessageUtil.getBodyItemValue(doc, "SEQUENCE", false);
			
			List<Element> eUnitList = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", true);			
			
			
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
			
			//Recipe Insert
			MESRecipeServiceProxy.getRecipeServiceImpl().createRecipe(eventInfo, machineName, machineRecipeName, GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN, "-", "-", seq, "N", "NG", doc);
			
			//Remove Recipe Relation
			MESRecipeServiceProxy.getRecipeServiceImpl().removeMachineRecipeRelation(eventInfo, machineName, machineRecipeName, eUnitList, doc);
			
			//Create Recipe Relation
			List<String> changeRecipeList = new ArrayList<String>(); 
			for (Element eUnit : eUnitList)
			{
				
				String unitName = SMessageUtil.getChildText(eUnit, "UNITNAME", true);
				String unitRecipeName = SMessageUtil.getChildText(eUnit, "UNITRECIPENAME", true);
				
				RecipeRelation newRelationData = MESRecipeServiceProxy.getRecipeServiceImpl().createRecipeRelation(eventInfo, machineName, machineRecipeName, unitName, unitRecipeName, doc);
				
				if(newRelationData != null && newRelationData.getLastEventTimeKey().equals(eventInfo.getEventTimeKey()))
				{
					changeRecipeList.add("Create Relation["+newRelationData.getChildMachineRecipe()+"]");
				}
			}
			
			if(!changeRecipeList.isEmpty())
			{
				MESRecipeServiceProxy.getRecipeServiceUtil().sendByRMSCreateAlarm(eventInfo, doc, "RMS-REL-CH", machineName, machineRecipeName, changeRecipeList, null);
			}
			
			
			
			
			
			/**
			 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
			 * ====================================================================
			 */
			if(StringUtil.isNotEmpty(onLineInitialCommState))
			{
				try
				{
					SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", factoryCode+"MachineRecipeCreatedCheckReply");

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
