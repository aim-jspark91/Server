package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class CutGlass extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cut", this.getEventUser(), this.getEventComment(), "", "");
		
		if(productElement.size() > 0)
		{
			int afterCutProductquantity = 0; 
			for (Element productInfo : productElement)
			{
				
				String sProductName = productInfo.getChild("PRODUCTNAME").getText();
				
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				
				List<ProductPGQS> productPGQSList = new ArrayList<ProductPGQS>();
				
				for(int i=0; i<2; i++)//2는 수정해야됨 (Cutting Modeling 기능 개발)
				{
					ProductPGQS productPGQS = new ProductPGQS();
					productPGQS.setProductName(productData.getKey().getProductName()+(i+1));
					productPGQS.setPosition(productData.getPosition());
					productPGQS.setProductGrade(productData.getProductGrade());
					productPGQS.setSubProductUnitQuantity1(productData.getSubProductUnitQuantity1());
					productPGQS.setSubProductQuantity1(productData.getSubProductQuantity1());
					productPGQS.setUdfs(productData.getUdfs());
					
					productPGQSList.add(productPGQS);
					
					afterCutProductquantity++;
				}
				
				SeparateInfo separateProductInfo = new SeparateInfo();
				separateProductInfo.setProductType("Panel");
				separateProductInfo.setSubProductType("");
				separateProductInfo.setSubProductPGQSSequence(productPGQSList);
				
				ProductServiceProxy.getProductService().separate(productData.getKey(), eventInfo, separateProductInfo);

			}
			
			afterCutProductquantity = afterCutProductquantity-productElement.size();
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			lotData.setProductQuantity(lotData.getProductQuantity() + afterCutProductquantity);
			
			LotServiceProxy.getLotService().update(lotData);
			SetEventInfo setEventInfo = new SetEventInfo();
			
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		
		SMessageUtil.addItemToBody(doc, "LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		
		return doc;
	}

}
