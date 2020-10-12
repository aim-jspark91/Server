package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaterial", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<Element> eleMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		for (Element eleMaterial : eleMaterialList)
		{
			String factoryName = SMessageUtil.getChildText(eleMaterial, "FACTORYNAME", true);
			String consumableSpecName = SMessageUtil.getChildText(eleMaterial, "CONSUMABLESPECNAME", true);
			String consumableName = SMessageUtil.getChildText(eleMaterial, "CONSUMABLENAME", true);
			String quantity = SMessageUtil.getChildText(eleMaterial, "QUANTITY", true);
			String consumableType = SMessageUtil.getChildText(eleMaterial, "CONSUMABLETYPE", true);
			String dueDate= SMessageUtil.getChildText(eleMaterial, "EXPIRATIONDATE", true);
			String provider = SMessageUtil.getChildText(eleMaterial, "PROVIDER", true);
			String materialState = SMessageUtil.getChildText(eleMaterial, "MATERIALSTATE", true);
			String thickness = SMessageUtil.getChildText(eleMaterial, "THICKNESS", true);
			String productionInputType = SMessageUtil.getChildText(eleMaterial, "PRODUCTIONINPUTTYPE", true);
			/*============= Validation consumableName =============*/
			checkExistConsumable(consumableName);		 
			
			/*============= Create Material =============*/
			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(consumableSpecName);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(factoryName);
			ConsumableSpec materialSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			Map<String, String> materialUdf = new HashMap<String, String>();
			materialUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
		    materialUdf.put("EXPIRATIONDATE", dueDate);
			materialUdf.put("DURATIONUSED", "0");
			materialUdf.put("DURATIONUSEDLIMIT", materialSpecData.getUdfs().get("DURATIONUSEDLIMIT"));
			materialUdf.put("PROVIDER", provider);
			materialUdf.put("MATERIALSTATE", materialState);
			materialUdf.put("THICKNESS", thickness);
			materialUdf.put("PRODUCTIONINPUTTYPE", productionInputType);

			CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", consumableName, consumableSpecName, "00001", consumableType, Double.valueOf(quantity), materialUdf);
			MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, consumableName, createInfo);
		}
		
		return doc;
	}
	
	
	/**
	* Name : checkExistConsumable
	* Desc : check existence
	* Author : AIM-jhlee
	* Date : 2016.07.29
	*/
	private void checkExistConsumable(String consumableName) throws CustomException
	{
		String condition = "CONSUMABLENAME = ?";
		
		Object[] bindSet = new Object[] {consumableName};
		try
		{
			List <Consumable> sqlResult = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
			
			if(sqlResult.size() > 0)
			{
				throw new CustomException("MATERIAL-0003", consumableName);
			}
		}
		catch (NotFoundSignal ex)
		{
			 return;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-0001", fe.getMessage());
		}
	}
	
	/**
	* Name : checkExistDurable
	* Desc : check existence
	* Author : AIM-jhlee
	* Date : 2016.07.29
	*/
	
}
