package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class H4ReleaseLot extends SyncHandler {
	
	/**
	 * 151106 by xzquan : Create ReleaseProductRequest
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sLoadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
		String sUnLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
		String sPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String sDueDate = SMessageUtil.getBodyItemValue(doc, "DUEDATE", true);
		String sCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		Element eProductList = SMessageUtil.getBodySequenceItem(doc, "PRODUCTLIST", true);
		
		List<Lot> successLotList = new ArrayList<Lot>();

		//Product Spec Data
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, sProductSpecName,
									GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		//Machine Data
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		//1. Velidation
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), "", "");
			
		//1. Release Lot
		//1.1) get lot Data by Lot Name
		LotKey lotKey = new LotKey(sLotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotKey);
			
		//1.2) get PGSSequance
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		List<Object[]> insertArgList = new ArrayList<Object[]>();
		List<Object[]> insertArgListForHQGlass = new ArrayList<Object[]>();
			
		for (@SuppressWarnings("rawtypes")
		Iterator iProduct = eProductList.getChildren().iterator(); iProduct.hasNext();)
		{
			Element eProduct = (Element) iProduct.next();
			String sProductName = SMessageUtil.getChildText(eProduct, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
			String sCrateName = SMessageUtil.getChildText(eProduct, "CONSUMABLENAME", true);
				
			Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sCrateName);
			if (crateData.getUdfs().get("CONSUMABLEHOLDSTATE").equals("Y"))
				{
					throw new CustomException("CRATE-0008", sCrateName);
				}
										
				ProductPGS productInfo = new ProductPGS();
				productInfo.setPosition(Long.parseLong(sPosition));
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
																	GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(sProductName);
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(specData.getSubProductUnitQuantity1());
				productInfo.getUdfs().put("CRATENAME", sCrateName);
    			// Start 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
    			try {
    				crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sCrateName);
    				productInfo.getUdfs().put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
				} catch (Exception e) {
					eventLog.info("crateData is Not Found");
				}
    			// End 2019.09.11 Modfiy By Park Jeong Su Mantis 4706
				productPGSSequence.add(productInfo);
		}
			//1.3)Release Lot
			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(
					lotData, machineData.getAreaName(), lotData.getNodeStack(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getProductionType(),
					lotData.getUdfs(), "",
					lotData.getDueDate(), lotData.getPriority());			
			double originalCreateQty = 0;
			originalCreateQty = lotData.getCreateProductQuantity();
			lotData.setCreateProductQuantity(productPGSSequence.size());
			lotData.setCreateSubProductQuantity(lotData.getSubProductUnitQuantity1() * productPGSSequence.size());
			lotData.setCreateSubProductQuantity1(lotData.getSubProductUnitQuantity1() * productPGSSequence.size());
			LotServiceProxy.getLotService().update(lotData);
			
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);
			
			List<Product> productList = MESLotServiceProxy.getLotServiceUtil().allUnScrappedProductsByLotForUpdate(lotData.getKey().getLotName());
			
			if(productList != null)
			{
				ExtendedObjectProxy.getProductFlagService().setCreateProductFlagForUPK(eventInfo, productList);
			}
			
			//5. consume DP box
			try
			{
				decreaseCrateQuantity(eventInfo, lotData, productPGSSequence);				
			}
			catch(Exception e)
			{
				eventLog.error("decreaseCrateQuantity Failed");
			}			
			
			//6. Matching Lot Name & W/O Plan(CT_ReserveLot) to Released
			try
			{	
				//Reserve Lot Data
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {sMachineName, sLotName});
				reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
				
				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Lot[%s] is failed to Macthing Plan", sLotName));
			}
			
			//7. Auto Track In / Out Unpacker
			//Document trackInOutDoc = writeTrackInOutRequest(doc, sLotName, sMachineName, sCarrierName, sLoadPort, sUnLoadPort, crateName);
			
			Port loader = MESLotServiceProxy.getLotServiceUtil().searchLoaderPort(sMachineName);
			
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProcessFlowName(), lotData.getProcessOperationName(), sMachineName, lotData.getUdfs().get("ECCODE"));

/*			ConsumableKey consumableKey = new ConsumableKey(crateData.);
			Consumable con = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
			String consumableSpec = con.getConsumableSpecName();

			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setConsumableSpecName(consumableSpec);
			consumableSpecKey.setConsumableSpecVersion("00001");
			consumableSpecKey.setFactoryName(con.getFactoryName());
			ConsumableSpec conSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			String machineRecipeName = conSpec.getUdfs().get("MACHINERECIPENAME");*/
			
			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), sLoadPort, machineRecipeName);

			MESLotServiceProxy.getLotServiceUtil().TrackInLotForUPK(doc, eventInfo);
			
			Port unloader = MESLotServiceProxy.getLotServiceUtil().searchUnloaderPort(loader);

			doc = MESLotServiceProxy.getLotServiceUtil().writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), sUnLoadPort, sCarrierName);

			MESLotServiceProxy.getLotServiceUtil().TrackOutLotForUPK(doc, eventInfo);
			
		successLotList.add(0, lotData);
		
		MESLotServiceProxy.getLotServiceUtil().setNextInfoForUPK(doc, successLotList);

		return doc;
	}
	
	/**
	 * logic for decrement with different DP box
	 * @author swcho
	 * @since 2014.05.08
	 * @param eventInfo
	 * @param lotData
	 * @param productPGSSequence
	 * @throws CustomException
	 */
	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, List<ProductPGS> productPGSSequence)
		throws CustomException
	{
		Map<String, Object> crateMap = new HashMap<String, Object>();
		
		String oldCrateName = "";
		
		int count = 0;
		
		for (ProductPGS productPGS : productPGSSequence)
		{
			if (!oldCrateName.equals(CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME")))
			{
				//initialize
				count = 0;
				oldCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");
			}
			
			count++;
			crateMap.put(oldCrateName, count);
		}
		
		for (String crateName : crateMap.keySet())
		{
			if (crateMap.get(crateName) != null)
			{
				int quantity = Integer.parseInt(crateMap.get(crateName).toString());
				
				eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
				decreaseCrateQuantity(eventInfo, lotData, crateName, quantity);
				
				//makeNotAvailable
				Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				if(crateData.getQuantity() == 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
					
					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
					makeNotAvailableInfo.setUdfs(crateData.getUdfs());
					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
				}
			}
		}
	}
	
	/**
	 * to consume Crate
	 * @author swcho
	 * @since 2014.05.08
	 * @param eventInfo
	 * @param lotData
	 * @param consumableName
	 * @throws CustomException
	 */
	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, String consumableName, double quantity)
		throws CustomException
	{
		eventInfo.setEventName("Consume");
		
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		
		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotData.getKey().getLotName(),
												lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
												"", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()),
												quantity, lotData.getUdfs());
		
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}
}