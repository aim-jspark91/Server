package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductGSC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class AssembleLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//COMMON
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String mainWorkOrder = SMessageUtil.getBodyItemValue(doc, "MAINWORKORDER", false);
		String subWorkOrder = SMessageUtil.getBodyItemValue(doc, "SUBWORKORDER", false);
		
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assemble", this.getEventUser(), this.getEventComment(), "", "");
				
		
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		if(productElement.size() > 0)
		{
			for (Element productInfo : productElement)
			{

				String sProductName = productInfo.getChild("PRODUCTNAME").getText();
				String sPairProductName = productInfo.getChild("PAIRPRODUCTNAME").getText();
				String sLotName = productInfo.getChild("LOTNAME").getText();
				String sPairLotName = productInfo.getChild("PAIRLOTNAME").getText();
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				Lot pairLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sPairLotName);
				
				Product sProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(sProductName);
				Product pairProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(sPairProductName);
				
				// ProductP List
				List<ProductP> productPList = new ArrayList<ProductP>();
				ProductP productP = new ProductP();
				productP.setPosition(pairProductData.getPosition());
				productP.setProductName(pairProductData.getKey().getProductName());
				productP.setUdfs(pairProductData.getUdfs());
				productPList.add(productP);
				
				// Comsumed Material List (Lot)
				List<kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial> cmLots = new ArrayList<kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial>();
				kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial cmLot = new kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial();
				cmLot.setMaterialName(sPairLotName);
				cmLot.setMaterialType("Lot"); 
				cmLot.setQuantity(1);
				cmLot.setUdfs(pairLotData.getUdfs());  
				cmLot.setProductPSequence(productPList);
				cmLots.add(cmLot);
				
				// Comsumed Material List (Product)
				List<ConsumedMaterial> consumedMaterialList = new ArrayList<ConsumedMaterial>();
				ConsumedMaterial cms = new ConsumedMaterial();
				cms.setMaterialName(sPairProductName);
				cms.setMaterialType("Product");
				cms.setQuantity(1); 
				consumedMaterialList.add(cms);
				
				
				// ProductGSC List
				List<ProductGSC> productGSCList = new ArrayList<ProductGSC>();
				ProductGSC productGSC = new ProductGSC();
				
				productGSC.setConsumedMaterialSequence(consumedMaterialList);
				productGSC.setProductGrade(sProductData.getProductGrade());
				productGSC.setProductName(sProductName); 
				productGSC.setSubProductGrades1(sProductData.getSubProductGrades1());
				productGSC.setSubProductGrades2(sProductData.getSubProductGrades2());
				productGSC.setSubProductQuantity1(Double.valueOf(sProductData.getSubProductQuantity1()).doubleValue());
				productGSC.setSubProductQuantity2(Double.valueOf(sProductData.getSubProductQuantity2()).doubleValue());
				productGSC.setUdfs(sProductData.getUdfs());
				productGSCList.add(productGSC);
				
				ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();
				consumeMaterialsInfo.setConsumedMaterialSequence(cmLots);
				consumeMaterialsInfo.setLotGrade(pairLotData.getLotGrade());
				consumeMaterialsInfo.setProductGSCSequence(productGSCList); 
				consumeMaterialsInfo.setUdfs(lotData.getUdfs());   
				
				LotServiceProxy.getLotService().consumeMaterials(lotData.getKey(), eventInfo, consumeMaterialsInfo);
			}
		}
			
		
		
		return doc;
	}

}
