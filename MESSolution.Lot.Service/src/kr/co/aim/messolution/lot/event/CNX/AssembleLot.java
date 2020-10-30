package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
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
			
			Lot ClotData = new Lot();
			Lot CpLotData = new Lot();
			
			List<ProductGSC> productGSCSequence = new ArrayList<ProductGSC>();
			
			for (Element productInfo : productElement)
			{

				String sProductName = productInfo.getChild("PRODUCTNAME").getText();
				String sPairProductName = productInfo.getChild("PAIRPRODUCTNAME").getText();
				String sLotName = productInfo.getChild("LOTNAME").getText();
				String sPairLotName = productInfo.getChild("PAIRLOTNAME").getText();
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
				Lot pairLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sPairLotName);
				
				ClotData = lotData;
				CpLotData = pairLotData;
				
				Product sProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(sProductName);
				Product pairProductData = MESProductServiceProxy.getProductServiceUtil().getProductData(sPairProductName);
				
				List<ConsumedMaterial> cms = new ArrayList<ConsumedMaterial>();
				
				ConsumedMaterial cm = new ConsumedMaterial();
				cm.setMaterialName(pairProductData.getKey().getProductName());
				cm.setMaterialType("Product");
				cm.setQuantity(1);
				cm.setUdfs(pairProductData.getUdfs());

				cms.add(cm);

				ProductGSC productGSC = new ProductGSC();
				productGSC.setConsumedMaterialSequence(cms);
				productGSC.setProductGrade(sProductData.getProductGrade());
				productGSC.setProductName(sProductData.getKey().getProductName());
				productGSC.setSubProductQuantity1(sProductData.getSubProductQuantity1());
				productGSC.setUdfs(sProductData.getUdfs());
				
				productGSCSequence.add(productGSC);		
			}
			
			List<kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial> consumedMaterialSequence = new ArrayList<kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial>();
			
			kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial cmL = new kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial();
			cmL.setMaterialName(CpLotData.getKey().getLotName());
			cmL.setMaterialType("Lot");
			cmL.setQuantity(0);
			cmL.setUdfs(CpLotData.getUdfs());
			
			consumedMaterialSequence.add(cmL);
			
			ConsumeMaterialsInfo consumeMaterialsInfo = new ConsumeMaterialsInfo();
			consumeMaterialsInfo.setConsumedMaterialSequence(consumedMaterialSequence);
			consumeMaterialsInfo.setLotGrade(CpLotData.getLotGrade());
			consumeMaterialsInfo.setProductGSCSequence(productGSCSequence);
			consumeMaterialsInfo.setUdfs(CpLotData.getUdfs());
			
			LotServiceProxy.getLotService().consumeMaterials(ClotData.getKey(), eventInfo, consumeMaterialsInfo);
		}
			
		
		
		return doc;
	}

}
