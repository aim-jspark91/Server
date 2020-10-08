package kr.co.aim.messolution.consumable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class MaterialStateReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaterialStateReport", getEventUser(), getEventComment(), null, null);

        Machine machineData = null;
		Machine unitData = null;
		String currentCommunicationName = StringUtil.EMPTY;
		String unitName = StringUtil.EMPTY;

		String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);

		/* Machine Validation */
		machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		currentCommunicationName = machineData.getCommunicationState();

		List<Element> unitlist = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", true);

		if (unitlist != null)
		{
			for(Element uintE : unitlist)
			{
				/* Unitname Validation */
				unitName = SMessageUtil.getChildText(uintE, "UNITNAME", false);
				unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

				List<Element> materialList = SMessageUtil.getSubSequenceItemList(uintE, "MATERIALLIST", true);

				if (materialList != null)
				{
					for(Element materialE : materialList)
					{
						String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
						String materialPosition = SMessageUtil.getChildText(materialE, "MATERIALPOSITION", false);
						String materialState = SMessageUtil.getChildText(materialE, "MATERIALSTATE", false);
						String materialType = SMessageUtil.getChildText(materialE, "MATERIALTYPE", false);
						String materialUsedcount = SMessageUtil.getChildText(materialE, "MATERIALUSEDCOUNT", false);

						/*  Not used at Array, Only use at OLDE/Module */
						//String materialLotName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOTNAME", false);
						//String materialRemaincount = SMessageUtil.getBodyItemValue(doc, "MATERIALREMAINCOUNT", false);

						/* Consumable Validation */
						// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//						Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
						Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(materialName));
						
						Map<String, String> udfs = consumableData.getUdfs();

						/* Material Mount */
						if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Mount)))
						{
							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Mount);

							if (!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
							{
								throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
							}

							consumableData.setMaterialLocationName(materialPosition);
							udfs.put("UNITNAME", unitName);
							//kit event
							kitConsumableData(eventInfo, consumableData, machineName);

						}
						/* Material InUse */
						else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_InUse)))
						{
							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_InUse);

							//Check Validation
							if(!udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
							{
								throw new CustomException("MATERIAL-0005", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
							}

							consumableData.setMaterialLocationName(materialPosition);
							udfs.put("UNITNAME", unitName);
							//kit event
							kitConsumableData(eventInfo, consumableData, machineName);
						}
						/* Material Unmount */
						else if(StringUtil.upperCase(materialState).equals(StringUtil.upperCase(GenericServiceProxy.getConstantMap().Cons_Unmount)))
						{
							eventInfo.setEventName(GenericServiceProxy.getConstantMap().Cons_Unmount);

							//Check Validation
							if(!consumableData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_InUse) || !udfs.get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
							{
								new CustomException("MATERIAL-2002", GenericServiceProxy.getConstantMap().Cons_InUse, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);
							}

							//Unkit Material
							consumableData.setMaterialLocationName(materialPosition);
							this.unkitConsumableData(eventInfo, consumableData, materialUsedcount);
						}
						/* Material State is not exist */
						else
						{
							//eventLog.warn("Material State is not exist");
							new CustomException("MATERIAL-0021", "exist");
						}
					}
				}
			}
		}
	}

	/**
	* Name : kitConsumableData
	* Desc : Execute Kit event
	* Author : aim system
	* Date : 2016.07.29
	*/
	private void kitConsumableData(EventInfo eventInfo, Consumable materialData, String MachineName)
			throws CustomException
	{
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("MACHINENAME", MachineName);
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);

		materialData.setUdfs(udfs);

		materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
		materialData.setMaterialLocationName(MachineName);

		ConsumableServiceProxy.getConsumableService().update(materialData);

		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo =
				MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
	}

	private void unkitConsumableData(EventInfo eventInfo, Consumable materialData, String usedQty) throws CustomException
	{
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("MACHINENAME", "");
		udfs.put("UNITNAME", "");
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

		materialData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
		materialData.setMaterialLocationName("");
		materialData.setUdfs(udfs);

		kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo(materialData, materialData.getAreaName());
		ConsumableServiceProxy.getConsumableService().update(materialData);

		if(Double.valueOf(usedQty) != 0)
		{
			//decrement
			TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
														eventInfo.getEventTimeKey(), Double.parseDouble(usedQty), udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
										(DecrementQuantityInfo) transitionInfo, eventInfo);
			/*double quantity=materialData.getQuantity();
			quantity=quantity-Double.valueOf(usedQty);
			materialData.setQuantity(quantity);
			ConsumableServiceProxy.getConsumableService().update(materialData);*/
			//makeNotAvailable
			materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
			if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
			{
				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventName("ChangeState");
				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
				makeNotAvailableInfo.setUdfs(materialData.getUdfs());
				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
			}
		}
		else
		{
		   MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialData.getKey().getConsumableName(), setEventInfo, eventInfo);
		}
	}

}
