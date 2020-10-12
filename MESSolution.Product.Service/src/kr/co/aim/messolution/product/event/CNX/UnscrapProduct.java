package kr.co.aim.messolution.product.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class UnscrapProduct extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");
		
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody!=null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);
				String sLotName = SMessageUtil.getChildText(eledur, "LOTNAME", false);
				double productQuantity = 1.0;
				
				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				Product productData = ProductServiceProxy.getProductService().selectByKeyForUpdate(new ProductKey(sProductName));
				
				productData.setProductGrade("G");
				ProductServiceProxy.getProductService().update(productData);
				eventInfo.setReasonCode("");
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				LotKey lotKey = lotData.getKey();
				
				List<ProductU> productUSequence = new ArrayList<ProductU>();
				ProductU productU = new ProductU();
				productU.setProductName(sProductName);
				productUSequence.add(productU);
				
				MakeUnScrappedInfo makeUnScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeUnScrappedInfo(lotData, lotData.getLotProcessState(), productQuantity, productUSequence);
					
			    LotServiceProxy.getLotService().makeUnScrapped(lotKey, eventInfo, makeUnScrappedInfo);
			}
		}	

		return doc;
	}
}