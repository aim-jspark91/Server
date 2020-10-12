package kr.co.aim.messolution.product.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class ScrapProduct extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String messageName = SMessageUtil.getMessageName(doc);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody!=null)
		{
			String lotName = "";
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);
				String sLotName = SMessageUtil.getChildText(eledur, "LOTNAME", false);
				String sReasonCode = SMessageUtil.getChildText(eledur, "REASONCODE", true);
				double productQuantity = 1.0;
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(sProductName));
				
				if(StringUtil.equals(productData.getProductState(), "Scrapped"))
				{
					throw new CustomException("PRODUCT-0005", sProductName);
				}
				
				productData.setProductGrade("S");
				ProductServiceProxy.getProductService().update(productData);
				
				eventInfo.setReasonCode(sReasonCode);
				
				Lot sLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				LotKey lotKey = sLotData.getKey();
				
				List<ProductU> productUSequence = new ArrayList<ProductU>();
				ProductU productU = new ProductU();
				productU.setProductName(sProductName);
				productUSequence.add(productU);
				
				MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(sLotData, productQuantity, productUSequence);
				
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
    			//LotServiceProxy.getLotService().makeScrapped(lotKey, eventInfo, makeLotScrappedInfo);
                MESLotServiceProxy.getLotServiceImpl().makeScrapped(eventInfo,sLotData,makeLotScrappedInfo);
    			// Start 2019.09.09 Modify By Park Jeong Su Mantis 4721
                
			    

			    lotName = sLotData.getKey().getLotName();
			}
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			if(StringUtil.equals(lotData.getLotState(), "Scrapped") && StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				
				//List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);	
				DeassignCarrierInfo createInfo =  
					MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, new ArrayList<ProductU>()); 
				
				eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");	
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
			}
		}	
		return doc;
	}
}
