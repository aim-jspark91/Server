package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GlassProcessAbort extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		//String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		//for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		//Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, carrierName, productList);
		List<Lot> cancelTrackInLotList = this.getLotListByProductList(productList);
		
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PL") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PP"))
		{//PL end
			cancelTrackInLotList = CommonUtil.getLotListByCarrier(carrierName, false);
		}		

		for (Lot cancelTrackInLot : cancelTrackInLotList)
		{
			List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(cancelTrackInLot.getKey().getLotName(), productList);
			
			if (cancelTrackInLot.getProductQuantity() > productPGSRCSequence.size() && productPGSRCSequence.size() > 0)
			{
				cancelTrackInLot = this.splitGlass(eventInfo, cancelTrackInLot, productPGSRCSequence);
			}
			
			if (productPGSRCSequence.size() > 0)
			{
				cancelTrackIn(eventInfo, cancelTrackInLot, carrierName, productPGSRCSequence);
			}
			else
			{
				deassignCarrier(eventInfo, cancelTrackInLot);
			}
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}
	
	private List<Lot> getLotListByProductList(List<Element> productList) throws CustomException
	{
		List<String> lotNameList = new ArrayList<String>();
		
		for (Element productElement : productList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(productName));
			
			if (!lotNameList.contains(productData.getLotName()))
			{
				lotNameList.add(productData.getLotName());
			}
		}
		
		List<Lot> lotList = new ArrayList<Lot>();
		
		for (String lotName : lotNameList)
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
			
			lotList.add(lotData);
		}
		
		return lotList;
	}
	
	private Lot splitGlass(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		eventLog.info("Split Lot for TK out");
		
		eventInfo.setEventName("Create");
		Lot targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
		
		List<ProductP> productPSequence = new ArrayList<ProductP>();
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			ProductP productP = new ProductP();
			productP.setProductName(productPGSRC.getProductName());
			productP.setPosition(productPGSRC.getPosition());
			productP.setUdfs(productPGSRC.getUdfs());
			productPSequence.add(productP);
		}
		
		TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
														targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());
		
		//do splits
		eventInfo.setEventName("Split");
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
		
		targetLot = MESLotServiceProxy.getLotInfoUtil().getLotData(targetLot.getKey().getLotName());
		
		eventLog.info(String.format("Lot[%s] is separated", targetLot.getKey().getLotName()));
		
		return targetLot;
	}
	
	/**
	 * temp cancel track-In logic
	 * @author swcho
	 * @since 2015-04-26
	 * @param eventInfo
	 * @param lotData
	 * @param productList
	 * @throws CustomException
	 */
	private void cancelTrackIn(EventInfo eventInfo, Lot lotData, String carrierName, List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		eventInfo.setEventName("CancelTrackIn");
		lotData = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, lotData, null, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
	}
	
	private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		try
		{
			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			
			if(StringUtils.equals(carrierData.getDurableState(), "InUse"))
			{
				eventInfo.setEventName("DeassignCarrier");
				
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				
				DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);
				
				lotData = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);				
			}
		}
		catch (Exception ex)
		{
			eventLog.error(ex.getMessage());
		}
	}
}
