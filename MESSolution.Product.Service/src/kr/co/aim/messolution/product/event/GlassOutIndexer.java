package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;

import org.jdom.Document;

public class GlassOutIndexer extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		MACHINENAME 
		UNITNAME
		SUBUNITNAME
		LOTNAME
		PRODUCTNAME
		PRODUCTJUDGE
		PRODUCTGRADE
		FROMSLOTID
		TOSLOTID
		PORTNAME
		CARRIERNAME
		*/
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlot = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlot = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("IndexerOut", getEventUser(), getEventComment(), null, null);
		
		String materialLocationInfo = StringUtil.EMPTY;

        if(StringUtil.isNotEmpty(subUnitName))
        {
            materialLocationInfo = subUnitName;
        }
        else if(StringUtil.isNotEmpty(unitName))
        {
            materialLocationInfo = unitName;
        }
        else
        {
            materialLocationInfo = machineName;
        }
        
		try
		{
		    /* 20181227, hhlee, add, Machine validation ==>> */
           // Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(materialLocationInfo);//add by GJJ 20200406, mantis:5968 add rowlockstart
            /* <<== 20181227, hhlee, add, Machine validation */
            
    		//add by GJJ 20200406, mantis:5968 add rowlockstart
    		MachineKey machineKey = new MachineKey();
    		machineKey.setMachineName(materialLocationInfo);
    		Machine machineData = MachineServiceProxy.getMachineService().selectByKeyForUpdate(machineKey);
    		//add by GJJ 20200406, mantis:5968 add rowlock end
            
            
            
			/* 2018-11-20 jspark Add For AutoMQCSetting update LastRuntime */
			MESMachineServiceProxy.getMachineServiceImpl().updateAutoMQCSettingTimeRunInfo(machineName);
			/* 2018-11-20 jspark Add For AutoMQCSetting update LastRuntime */
			
			Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
			
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
			/* 20190116, hhlee, add, ProcessingUintName Column ==>> */
            udfs.put("PROCESSINGUNITNAME", StringUtil.EMPTY);           
            /* <<== 20190116, hhlee, add, ProcessingUintName Column */
            
			/* 20181128, hhlee, modify, materialLocationInfo is fixed as MachineName ==>> */
			////SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, StringUtil.isEmpty(unitName)? machineName:unitName, udfs);
			//SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, materialLocationInfo, udfs);
			SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, machineName, udfs);
			/* <<== 20181128, hhlee, modify, materialLocationInfo is fixed as MachineName */
			
			MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
						
			/* 20180912, hhlee, Add Product Location History Insert ==>> */
			// Deleted by smkang on 2018.10.08 - Because MaterialLocationName is recorded in Product table, CT_MATERIALLOCATIONHIST is unnecessary.
//			MESProductServiceProxy.getProductInfoUtil().setMaterialLocationHistory(eventInfo, lotName, productName, productType, productJudge, 
//			                                                                       productGrade, machineName, unitName, subUnitName, portName, 
//			                                                                       carrierName, fromSlot, toSlot);

			/* <<== 20180912, hhlee, Add Product Location History Insert */
			
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.11.15 - Update MachineIdleTime or MQCCondition.
			//								   According to Honewei's request, LastRunTime will be updated at GlassOutIndexer and GlassInIndexer time.
			try {
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				//String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
				//Object[] bindSet = new Object[] {machineName, "UNIT"};
				//List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());
				//for (MachineSpec unitSpec : unitSpecList) {
					MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimeRunTime(machineName, lotData, portName, (String)machineData.getUdfs().get("OPERATIONMODE"), eventInfo);
				//}
			} catch (Exception e) {
				eventLog.warn(e);
				
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				// Added by smkang on 2018.11.17 - Although a machine has no unit, updateMachineIdleTimeRunInfo should be invoked.
				//Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());
				//MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimeRunTime(machineName, "", lotData, eventInfo);
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		}
		catch(NotFoundSignal ns)
		{
			MachineSpec macSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
			
			if(StringUtil.equals(macSpecData.getUdfs().get("CONSTRUCTTYPE"), "UNPK"))
			{	
				VirtualGlass vGlassData = new VirtualGlass();
				
				try
				{
					eventInfo.setEventName("Create");
					
					//ProductRequestPlan pPlan = CommonUtil.getFirstPlanByMachine(machineName, false);
					ConsumableKey cKey = new ConsumableKey(carrierName);					
			        Consumable cData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);				
			        
			        //ProductRequestPlan pPlan = CommonUtil.getFirstPlanStartByCrateSpecName(cData.getConsumableSpecName().toString());
			        ProductRequestPlan pPlan = CommonUtil.getFirstPlanByCrateSpecName(cData.getConsumableSpecName().toString(), GenericServiceProxy.getConstantMap().RESV_LOT_STATE_START);
					
					String productRequestName = pPlan.getKey().getProductRequestName();
					
					vGlassData.setCrateName(carrierName);
					vGlassData.setProductRequestName(productRequestName);
					vGlassData.setVirtualGlassName(productName);
					/* 20181023, hhlee, add Virglass LotName ==>> */
                    vGlassData.setLotName(lotName);
                    /* <<== 20181023, hhlee, add Virglass LotName */
					vGlassData = ExtendedObjectProxy.getVirtualGlassService().create(eventInfo, vGlassData);
					
					//decrementCrate(carrierName, doc);
					
					eventInfo.setCheckTimekeyValidation(false);
                    eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
                    eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
				}
				catch(Exception ex)
				{
					throw ex;
				}
				finally
				{
				    vGlassData = ExtendedObjectProxy.getVirtualGlassService().selectByKey(false, new Object[] {productName});
					
					eventInfo.setEventName("IndexerOut");
					
					/* 20181023, hhlee, delete ==>> */
					//if(CommonUtil.isBpkType(machineName))
					//{
					//	vGlassData.setCarrier("");
					//}
					/* <<== 20181023, hhlee, delete */
					
					vGlassData.setLocation(unitName);
					vGlassData.setMachineName(machineName);
					
					ExtendedObjectProxy.getVirtualGlassService().modify(eventInfo, vGlassData);
					
					//160506 by swcho : modified
					try
					{
						decrementCrate(carrierName, doc);
					}
					catch (CustomException ce)
					{
						eventLog.error(ce.getMessage());
					}
				}				
			}
			else
			{
				throw ns;
			}
		}
		catch(Exception e)
		{			
			throw e;
		}
		
	}
	
	private void decrementCrate(String crateName, Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Consume", getEventUser(), getEventComment(), null, null);
		
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
		
		//Get Crate Data
		ConsumableKey cKey = new ConsumableKey(crateName);
		Consumable crateData = ConsumableServiceProxy.getConsumableService().selectByKey(cKey);
		
		//decrement
		TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "",
															eventInfo.getEventTimeKey(), 1, udfs);
				
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData,
											(DecrementQuantityInfo) transitionInfo, eventInfo);
				
		//makeNotAvailable
		crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
		if(crateData.getQuantity() <= 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
		{
			eventInfo.setEventName("ChangeState");
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			makeNotAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
		}
		else if(crateData.getQuantity() > 0 && !StringUtil.equals(crateData.getConsumableState(), "Available"))
		{
			eventInfo.setEventName("ChangeState");
			MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
			makeAvailableInfo.setUdfs(crateData.getUdfs());
			MESConsumableServiceProxy.getConsumableServiceImpl().makeAvailable(crateData, makeAvailableInfo, eventInfo);
		}
	}
	
	private void splitGlass(EventInfo eventInfo, Product productData) throws CustomException
	{
		eventLog.info(String.format("Product[%s] would be separated", productData.getKey().getProductName()));
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		
		//only for 1 Glass 1 Lot relation
		if (StringUtil.equals(productData.getKey().getProductName(), productData.getLotName())) return;
		
		eventInfo.setEventName("Create");
		Lot garbageLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, productData.getKey().getProductName(), lotData, "", false, new HashMap<String, String>(), lotData.getUdfs());
		
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		ProductP productP = new ProductP();
		productP.setProductName(productData.getKey().getProductName());
		productP.setPosition(productData.getPosition());
		productP.setUdfs(productData.getUdfs());
		productPSequence.add(productP);
		
		TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
														garbageLot.getKey().getLotName(), 1, productPSequence, lotData.getUdfs(), new HashMap<String, String>());
		
		//do split
		eventInfo.setEventName("Split");
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
		
		garbageLot = MESLotServiceProxy.getLotInfoUtil().getLotData(garbageLot.getKey().getLotName());
		
		eventLog.info(String.format("Lot[%s] is separated", productData.getKey().getProductName()));
	}
}
