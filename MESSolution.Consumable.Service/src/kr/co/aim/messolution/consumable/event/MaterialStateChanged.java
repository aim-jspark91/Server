package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class MaterialStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String materialPosition = SMessageUtil.getBodyItemValue(doc, "MATERIALPOSITION", false);
		String materialState = SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", false);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);
		String materialUsedcount = SMessageUtil.getBodyItemValue(doc, "MATERIALUSEDCOUNT", false);
		
		/* 20181119, hhlee, add materialUsedcount empty ==>> */
        materialUsedcount = StringUtil.isEmpty(materialUsedcount) ? "0" : materialUsedcount;
        /* <<== 20181119, hhlee, add materialUsedcount empty */
        
		/*  Not used at Array, Only use at OLDE/Module */
		//String materialLotName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOTNAME", false);
		//String materialRemaincount = SMessageUtil.getBodyItemValue(doc, "MATERIALREMAINCOUNT", false);

		/*String[] splitMaterialName = StringUtil.split(materialName, "_");

		if ( splitMaterialName.length != 3 )
		{
			throw new CustomException("CONS-0003", materialName);
		}

		String prSpecName = splitMaterialName[0];
		String materialName = splitMaterialName[1];
		String expireDateById = splitMaterialName[2];*/
		
		
		if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Mount)))
        {
		    materialState = GenericServiceProxy.getConstantMap().Cons_Mount;
        }
		else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_InUse)))
        {
            materialState = GenericServiceProxy.getConstantMap().Cons_InUse;
        }
		else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Unmount)))
        {
            materialState = GenericServiceProxy.getConstantMap().Cons_Unmount;
        }
		
		//Consumable
		// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
		
		Map<String, String> udfs = consumableData.getUdfs();
		
		if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Mount))
		{
			eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
			
			if (CommonUtil.getValue(consumableData.getUdfs(), "CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().FLAG_Y))
            {
                throw new CustomException("MATERIAL-0010", materialName);
            }
			
			if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
			{
				throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
			}
			
//			MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, consumableData.getKey().toString(), unitName, 
//                    materialPosition, materialState); //  20191010, GJJ, Mantis 4974 
			//add  20191010, GJJ, Mantis 4974 start  ==>> 
			MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, machineName, consumableData.getKey().getConsumableName(), unitName, 
                    materialPosition, materialState);
			
			eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);
			
			consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Mount);
			 
			consumableData.setMaterialLocationName(materialPosition);
			udfs.put("UNITNAME", unitName);
			udfs.put("MATERIALPOSITION", materialPosition);
			udfs.put("MANUFACTUREDATE", "");			
			//2018.12.07_hsryu_Modify Column Attribute VARCHAR2(40) -> Date
			//udfs.put("LASTMOUNTTIME", StringUtil.right(eventInfo.getEventTimeKey(), 14)); //Last Mount Time Update by eventtimekey
			udfs.put("LASTMOUNTTIME", eventInfo.getEventTimeKey());

			//kit event
			MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);
		}
		else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_InUse))
		{
//			eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
//						
//			//Check Validation
//			if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
//			{
//				throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//			}
//			
//			MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().toString(), machineName, unitName, 
//                    materialPosition, materialState);
			
			

			//Check Validation 20191010, GJJ, Mantis 4974 start  ==>> 
			if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
			{
				throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
			}
			
			MESConsumableServiceProxy.getConsumableServiceUtil().checkExistenceByMachineNamePosition(eventInfo, consumableData.getKey().getConsumableName(), machineName, unitName, 
			        materialPosition, materialState);
			
			eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);
			// 20191010, GJJ, Mantis 4974 END  ==>>
			
			consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
			
			consumableData.setMaterialLocationName(materialPosition);
			udfs.put("UNITNAME", unitName);
			udfs.put("MATERIALPOSITION", materialPosition);
			
			//kit event
			MESConsumableServiceProxy.getConsumableServiceUtil().kitConsumableData(eventInfo, consumableData, machineName, materialState, materialPosition);			
		}
		else if(materialState.equals(GenericServiceProxy.getConstantMap().Cons_Unmount))
		{
			eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);
									
//			//Check Validation
//			if(!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse) || !udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
//			{
//			    /* 20181119, hhlee, add materialUsedcount empty ==>> */
//			    throw new CustomException("MATERIAL-2002", GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//			}
			
			//Check Validation 20191010, GJJ, Mantis 4974 start  ==>> 
			if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
			{    							   
				throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
			}
			// 20191010, GJJ, Mantis 4974 END  ==>> 
			
			
			consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
			
			//Unkit Material
			consumableData.setMaterialLocationName(materialPosition);
			//this.unkitConsumableData(eventInfo, consumableData, materialUsedcount, materialState);
			MESConsumableServiceProxy.getConsumableServiceUtil().unkitConsumableData(eventInfo, consumableData, materialUsedcount, materialState);
		}
		else
		{
			eventLog.warn("Material State is not exist");
		}	
		
	}
		
//	/**
//	* Name : kitConsumableData
//	* Desc : Execute Kit event
//	* Author : aim system
//	* Date : 2016.07.29
//	*/
//	private void kitConsumableData(EventInfo eventInfo, Consumable materialData, String MachineName, String materialState, String materialPosition) 
//			throws CustomException
//	{
//		Map<String, String> udfs = materialData.getUdfs();
//		udfs.put("MACHINENAME", MachineName);
//		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
//		
//		materialData.setUdfs(udfs);
//		
//		//materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
//		//materialData.setMaterialLocationName(MachineName);
//		materialData.setConsumableState(materialState);
//        materialData.setMaterialLocationName(materialPosition);
//        
//		ConsumableServiceProxy.getConsumableService().update(materialData);
//		
//		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = 
//				MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
//		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
//	}
//	
//	private void unkitConsumableData(EventInfo eventInfo, Consumable materialData, String usedQty, String materialState) throws CustomException
//	{
//		Map<String, String> udfs = materialData.getUdfs();
//		udfs.put("MACHINENAME", "");
//		udfs.put("UNITNAME", "");
//		udfs.put("MATERIALPOSITION", "");
//		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//		
//		//materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
//		materialData.setConsumableState(materialState);
//		materialData.setMaterialLocationName("");
//		materialData.setUdfs(udfs);
//		
//		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
//		ConsumableServiceProxy.getConsumableService().update(materialData);
//		
//		if(Double.valueOf(usedQty) != 0 && 
//		        Double.valueOf(materialData.getQuantity()) != 0)
//		{
//			//decrement
//			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
//														eventInfo.getEventTimeKey(), Double.parseDouble(usedQty), udfs);
//			
//			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
//										(DecrementQuantityInfo) transitionInfo, eventInfo);
//			/*double quantity=materialData.getQuantity();
//			quantity=quantity-Double.valueOf(usedQty);
//			materialData.setQuantity(quantity);
//			ConsumableServiceProxy.getConsumableService().update(materialData);*/
//			//makeNotAvailable
//			materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
//			if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
//			{
//				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
//				eventInfo.setEventName("ChangeState");
//				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
//				makeNotAvailableInfo.setUdfs(materialData.getUdfs());
//				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
//			}
//		}
//		else
//		{
//		   MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
//		}
//	}
	
}
