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

public class CreateMaterialNew extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		/*============= Set event =============*/
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<Element> eleMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		for (Element eleMaterial : eleMaterialList)
		{
			String factoryName = SMessageUtil.getChildText(eleMaterial, "FACTORYNAME", true);
			String materialSpecName = SMessageUtil.getChildText(eleMaterial, "MATERIALSPECNAME", true);
			String materialName = SMessageUtil.getChildText(eleMaterial, "MATERIALNAME", true);
			String materialQty = SMessageUtil.getChildText(eleMaterial, "MATERIALQUANTITY", true);
			String materialType = SMessageUtil.getChildText(eleMaterial, "MATERIALTYPE", true);
			String expirationDate= SMessageUtil.getChildText(eleMaterial, "EXPIRATIONDATE", true);
			
			
			/*============= Validation consumableName =============*/
			checkExistConsumable(materialName);
		 
			
			/*============= Create Material =============*/
			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(materialSpecName);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(factoryName);
			ConsumableSpec materialSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			Map<String, String> materialUdf = new HashMap<String, String>();
			materialUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
		    materialUdf.put("EXPIRATIONDATE", expirationDate);
			materialUdf.put("DURATIONUSED", "0");
			materialUdf.put("DURATIONUSEDLIMIT", materialSpecData.getUdfs().get("DURATIONUSEDLIMIT"));
			materialUdf.put("MANUFACTUREDATE", String.valueOf(eventInfo.getEventTime()));

			CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", materialName, materialSpecName, "00001", materialType, Double.valueOf(materialQty), materialUdf);
			MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, materialName, createInfo);
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
