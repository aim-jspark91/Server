package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GlassProcessStarted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		//additional info initialized
		setEventComment("");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String productRecipe = SMessageUtil.getBodyItemValue(doc, "PRODUCTRECIPE", false);
		
		Machine MachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		//Q-time
		/*ExtendedObjectProxy.getQTimeService().monitorQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().validateQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().exitQTime(eventInfo, lotName, lotData.getProcessOperationName());*/
				
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		Map<String, String> lotUdfs = lotData.getUdfs();
		
		// Added by smkang on 2019.01.23 - According to Liu Hongwei's request, requester of transport job should be recorded in Lot and LotHistory.
		try {
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			lotUdfs.put("TRANSPORTREQUESTER", durableData.getUdfs().get("TRANSPORTREQUESTER"));
		} catch (Exception e) {
			// TODO: handle exception
			eventLog.info(e);
		}
		
		MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

		//TrackIn
		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
		
		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
									lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));
		
		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, machineRecipeName, productCSequence, lotUdfs);
		
		
		eventInfo.setEventName("TrackIn");
		Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);
		
		//splitGlass(eventInfo, trackInLot);
		
		sendToFMC(doc);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, getEventComment().toString());
	}
	
	private void splitGlass(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		
		if (productList.size() > 1)
		{
			eventLog.info("Split Glass as Lot for inline");
			
			for (Product productData : productList)
			{
				eventLog.info(String.format("Product[%s] would be separated", productData.getKey().getProductName()));
				
				eventInfo.setEventName("Create");
				Lot garbageLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, productData.getKey().getProductName(), lotData, lotData.getCarrierName(), false, new HashMap<String, String>(), lotData.getUdfs());
				
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
				
				setNextInfo(garbageLot);
				
				eventLog.info(String.format("Lot[%s] is separated", productData.getKey().getProductName()));
			}
		}
	}
	
	private Lot deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		
		if(StringUtils.equals(carrierData.getDurableState(), "InUse"))
		{
			eventInfo.setEventName("DeassignCarrier");
			
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			
			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);
			
			// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//			lotData = LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
			return MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
		}
		
		return null;
	}
	
	private void setNextInfo(Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder(getEventComment());
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			setEventComment(strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK In is nothing");
		}
	}
	
	private void sendToFMC(Document doc)
	{
		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}
}
