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
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
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
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;

import org.jdom.Document;
import org.jdom.Element;

public class ReleaseLotByStressTest extends SyncHandler {
	
	/**
	 * 141103 by swcho : ReserveLotList to ReserveLot
	 */
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		Element eProductList = SMessageUtil.getBodySequenceItem(doc, "PRODUCTLIST", true);
		
		//search input
		//ReserveLot planData = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] {sMachineName, sLotName});
		
		//Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(planData.getLotName());
		
		LotKey lotKey = new LotKey();
		lotKey.setLotName(sLotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		
		//event information
		String sTimeKey = TimeUtils.getCurrentEventTimeKey();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(sTimeKey);
		
		List<ProductPGS> productPGSSequence = this.setProductPGSSequence(sFactoryName, eProductList, (long)lotData.getSubProductUnitQuantity1());
		{//release Lot
			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(
								lotData, machineData.getAreaName(), lotData.getNodeStack(),
								lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
								lotData.getProductionType(),
								lotData.getUdfs(), "",
								lotData.getDueDate(), lotData.getPriority());
			
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);
		}
		
		//150330 by swcho : Production generation is involved in Release
		/*{//create Product
			eventInfo.setEventName("Release");
			
			AssignNewProductsInfo assignInfo = MESLotServiceProxy.getLotInfoUtil().assignNewProductsInfo(
												lotData, productPGSSequence.size(), productPGSSequence);
			
			lotData = MESLotServiceProxy.getLotServiceImpl().assignNewProducts(eventInfo, lotData, assignInfo);
		}*/
		
		//refresh CT_ReserveLotList
		//updateInputPlan(eventInfo, planData);
		
		//consume DP box
		decreaseCrateQuantity(eventInfo, lotData, productPGSSequence);	
		
		return doc;
	}
	
	/**
	 * generate Produce to create and assign
	 * @author swcho
	 * @since 2014.04.28
	 * @param factoryName
	 * @param productList
	 * @param createSubProductQuantity
	 * @return
	 */
	private List<ProductPGS> setProductPGSSequence(String factoryName, Element productList, long createSubProductQuantity)
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		
		int idx = 0;
		
		for (Iterator itProduct = productList.getChildren().iterator(); itProduct.hasNext();)
		{
			idx++;
			
			Element eProduct = (Element) itProduct.next();
			
			try
			{
				String sProductName = SMessageUtil.getChildText(eProduct, "PRODUCTNAME", true);
				String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
				String sCrateName = SMessageUtil.getChildText(eProduct, "CRATENAME", true);
				
				ProductPGS productInfo = new ProductPGS();
				productInfo.setPosition(Long.parseLong(sPosition));
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
																	GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(sProductName);
				//productInfo.setSubProductGrades1(GradeDefUtil.generateGradeSequence(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY,
				//																	GenericServiceProxy.getConstantMap().GradeType_SubProduct, true, createSubProductQuantity));
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(createSubProductQuantity);
				
				productInfo.getUdfs().put("CRATENAME", sCrateName);
				
				productPGSSequence.add(productInfo);
			}
			catch (Exception ex)
			{
				eventLog.info(String.format("[%d]th Product not set", idx));
				//go to next
			}
		}
		
		return productPGSSequence;
	}
	
	/**
	 * to post input plan reservation
	 * 141103 by swcho : service object changed
	 * @author swcho
	 * @since 2014.05.08
	 * @param eventInfo
	 * @param planData
	 * @throws CustomException
	 */
	private void updateInputPlan(EventInfo eventInfo, ReserveLot planData)
		throws CustomException
	{
		//planData.setInputFlag(GenericServiceProxy.getConstantMap().Flag_Y);
		planData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
		planData.setInputTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		planData.setCompleteTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{
			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, planData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("CRATE-8001", ne.getMessage());
		}
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
