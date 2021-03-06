package kr.co.aim.messolution.lot.event;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;

public class GlassLineIn extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc) throws CustomException  
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String reasonCode  = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);
		 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitIn", getEventUser(), getEventComment(), null, reasonCode);
		 
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		
		//ChangeLocationInfo
		Map<String,String>udfs = productData.getUdfs();
		
		SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, unitName, udfs);
		
		MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
		
		/* 20181203, hhlee, delete, LotGrade is Only ReWork, Scrap */
		////ChangeGrade
		//this.changeGrade(eventInfo, productData, "G");
	}
	
	private void changeGrade(EventInfo eventInfo, Product productData, String productGrade) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		
		List<ProductPGSRC> productPGSRCSequence = MESProductServiceProxy.getProductInfoUtil().getProductPGSRCSequence(lotData);
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
		
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{			
			if (productPGSRC.getProductName().equals(productData.getKey().getProductName()))
			{
				productPGSRC.setProductGrade(productGrade);				
			}			
		}
		
		String lotGrade = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotData, "", productPGSRCSequence);		
		
		ProductPGS productPGS = new ProductPGS();
		productPGS.setProductName(productData.getKey().getProductName());	
		productPGS.setProductGrade(productGrade);
		productPGS.setPosition(productData.getPosition());
		productPGS.setSubProductQuantity1(productData.getSubProductQuantity1());
		
		productPGSSequence.add(productPGS);
		
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);
		
		eventInfo.setEventName("ChangeGrade");
		
		Lot result = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
	}
}
