package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.name.NameServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class MixingMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mix", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mixMaterialName = SMessageUtil.getBodyItemValue(doc, "MIXMATERIALNAME", true);
		String mixMaterialQty = SMessageUtil.getBodyItemValue(doc, "CREATEQUANTITY", true);
		String mixMaterialCode = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", false);
		String expirationDate = SMessageUtil.getBodyItemValue(doc, "EXPIRATIONDATE", true);
		
		List<Element> materialList = SMessageUtil.getBodySequenceItemList(doc, "MIXMATERIALLIST", true);
		
		String namingRule = this.createMaterialNaming();
		mixMaterialName = namingRule;
		String mixMaterialSpec = "";
		
		String sourceMaterialQty = "";
		
		//20170308 yanyan
		String MaterialRatio = "";
		
		if (materialList != null)
		{
			for(Element materialE : materialList)
			{
				String materialName = SMessageUtil.getChildText(materialE, "MATERIALNAME", true);
				mixMaterialSpec = SMessageUtil.getChildText(materialE, "MIXMATERIALSPEC", false);
				String requiredQty = SMessageUtil.getChildText(materialE, "REQUIREDQUANTITY", true);
				
				if(!mixMaterialSpec.isEmpty())
				{
					mixMaterialSpec = SMessageUtil.getChildText(materialE, "MIXMATERIALSPEC", false);
				}
				
				//Request By Longzhenghui
				String mr = materialName + "[" + requiredQty + "] ";
				sourceMaterialQty += mr;
				
				//Request By Yanyan
				String ma = "[" + requiredQty + "] ";
				MaterialRatio += ma;
				

				//consumable
				Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				
				//consumableSpec
				ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(materialName);
				Map<String, String> specUdfs = consumableSpecData.getUdfs();
				
				String requiredEvent = CommonUtil.getValue(specUdfs, "REQUIREDEVENT");
				
				//validate
				if(!materialData.getConsumableState().equals(GenericServiceProxy.getConstantMap().Cons_Available))
					throw new CustomException("MATERIAL-0014", materialName, GenericServiceProxy.getConstantMap().Cons_Available);
				
				if (!requiredEvent.equals("MIXING") && !requiredEvent.equals("UNFROZENAndMIXING"))
					throw new CustomException("MATERIAL-0008", materialName);
				
				if (requiredEvent.equals("UNFROZENAndMIXING") && !materialData.getUdfs().get("TRANSPORTSTATE").equals("UNFROZEN"))
					throw new CustomException("MATERIAL-0007", materialName);
					
					
				//Mix Material
				this.mixConsumableData(eventInfo, materialData, requiredQty, mixMaterialName);
			}
			
			//Create MixMaterial
			//validate
			this.checkExistConsumable(mixMaterialName);
			
			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			
			consumableSpecKey.setConsumableSpecName(mixMaterialSpec);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(factoryName);
						
			ConsumableSpec materialSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			Map<String, String> mSpecUdfs = materialSpecData.getUdfs();
			
			String mRequiredEvent = CommonUtil.getValue(mSpecUdfs, "REQUIREDEVENT");
			
			Map<String, String> materialUdf = new HashMap<String, String>();
			
			if(mRequiredEvent.equals("UNFROZEN"))
			{
				materialUdf.put("TRANSPORTSTATE", "UNFROZEN");
				materialUdf.put("UNFREEZECOUNT", String.valueOf(1));
				materialUdf.put("UNFREEZENTIME", String.valueOf(eventInfo.getEventTime()));
			}
			else
			{
				materialUdf.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
			}
			materialUdf.put("MANUFACTUREDATE", TimeStampUtil.getCurrentTime());
			materialUdf.put("EXPIRATIONDATE", expirationDate);
			materialUdf.put("MATERIALCODE", mixMaterialCode);
			materialUdf.put("DURATIONUSED", "0");
			materialUdf.put("DURATIONUSEDLIMIT", materialSpecData.getUdfs().get("DURATIONUSEDLIMIT"));
			materialUdf.put("UNFREEZETIMEUSED", materialSpecData.getUdfs().get("UNFREEZETIMEUSED"));
			//Request By Longzhenghui
			materialUdf.put("SOURCEMATERIALQTY", sourceMaterialQty);
			//Request By Yanyan
			materialUdf.put("MATERIALRATIO", MaterialRatio);
						
			CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", mixMaterialName, mixMaterialSpec, "00001", materialSpecData.getConsumableType(), Double.valueOf(mixMaterialQty), materialUdf);
			
			//create
			MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, mixMaterialName, createInfo);
		}
		 
		//20170425  MixingMaterial Set StockerOutCount=1
		// Modified by smkang on 2019.06.19 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Consumable mixConsumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(mixMaterialName);
		Consumable mixConsumableData = ConsumableServiceProxy.getConsumableService().selectByKeyForUpdate(new ConsumableKey(mixMaterialName));
		
		Map<String, String> udfs = mixConsumableData.getUdfs();
		udfs.put("STOCKEROUTCOUNT", "1");
		mixConsumableData.setUdfs(udfs);
		ConsumableServiceProxy.getConsumableService().update(mixConsumableData);
		
		//return New  Material
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "NEWMATERIALNAME", mixMaterialName);
		
		return rtnDoc;
	}
	
	
	
	private void mixConsumableData(EventInfo eventInfo, Consumable materialData, String requiredQty, String mixMaterialName) throws CustomException
	{
		//Use Quantity
		double requiredQtyD = Double.valueOf(requiredQty);
		
		Map<String, String> udfs = materialData.getUdfs();
		udfs.put("MIXMATERIALNAME", mixMaterialName);
		materialData.setUdfs(udfs);
		
		//decrement
		TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
													eventInfo.getEventTimeKey(), requiredQtyD, udfs);
		
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(materialData,
									(DecrementQuantityInfo) transitionInfo, eventInfo);
		
		//makeNotAvailable
		materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialData.getKey().getConsumableName());
		if(materialData.getQuantity() == 0 && StringUtil.equals(materialData.getConsumableState(), "Available"))
		{
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(materialData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(materialData, makeNotAvailableInfo, eventInfo);
		}
		
	}
	
	public static String createMaterialNaming() throws CustomException
	{
		//Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		//nameRuleAttrMap.put("M-MIX", "M-MIX");
		//List<String> materialList = CommonUtil.generateNameByNamingRule("MixMaterialNaming", nameRuleAttrMap, 1);
		
		List<String> argSeq = new ArrayList<String>();
		List<String> materialList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("MixMaterialNaming", argSeq, Integer.parseInt("1"));
		
		String materialName = materialList.get(0);
		
		return materialName;
	}
	
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

}
