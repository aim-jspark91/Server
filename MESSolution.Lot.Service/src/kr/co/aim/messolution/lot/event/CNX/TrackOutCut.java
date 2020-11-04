package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MergeInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;

public class TrackOutCut extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		List<Lot> lotList = new ArrayList<Lot>();
		
		for (Element product : productList)
		{   
			String sLotName = product.getChild("LOTNAME").getText();
			
			if(!lotList.contains(MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName)))
			{
				lotList.add(MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName));
			}
		}
		
		Lot newLotData = null;
		
		for(Lot lotData : lotList)
		{			
			Durable assignCST = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			List<ProductP> productPList = new ArrayList<ProductP>();
			for (Element product : productList)
			{   
				String sLotName = product.getChild("LOTNAME").getText();
				
				if(lotData.getKey().getLotName().equals(sLotName))
				{
					String sProductName = product.getChild("PRODUCTNAME").getText();
					//String sProductGrade = product.getChild("PRODUCTGRADE").getText();
					
					int position = Integer.parseInt(SMessageUtil.getChildText(product, "POSITION", true));
					
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(sProductName);
					
					ProductP p = new ProductP();
					p.setPosition(position);
					p.setProductName(sProductName);
					p.setUdfs(productData.getUdfs());
					
					productPList.add(p);
				}
			}
			
			if(newLotData == null)
			{
				if(lotData.getProductQuantity() == productPList.size())
				{
					newLotData = lotData;
				}
				else
				{
					//Get Child Lot Name
				    String lotName = StringUtil.substring(lotData.getKey().getLotName(), 0, 8);
					List<String> argSeq = new ArrayList<String>();
					argSeq.add(lotName);
					List<String> lstName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("GlassSplitLotNaming", argSeq, 1);
					
					int i = 0;
					String childLotName = lstName.get(i++);
					
					SplitInfo splitInfo = new SplitInfo();
					splitInfo.setAssignCarrierUdfs(assignCST.getUdfs());
					splitInfo.setChildLotName(childLotName);
					splitInfo.setChildLotUdfs(lotData.getUdfs());
					splitInfo.setProductPSequence(productPList);
					splitInfo.setProductQuantity(productPList.size());
					splitInfo.setUdfs(lotData.getUdfs());
					
					//EventInfo
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", this.getEventUser(), this.getEventComment(), "", "");
					
					LotServiceProxy.getLotService().split(lotData.getKey(), eventInfo, splitInfo);
					
					newLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(childLotName);
				}
			}
			else if(!lotData.getKey().getLotName().equals(newLotData.getKey().getLotName()))
			{
				TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();
				transferProductsToLotInfo.setDestinationLotName(newLotData.getKey().getLotName());
				transferProductsToLotInfo.setDestinationLotUdfs(newLotData.getUdfs());
				transferProductsToLotInfo.setEmptyFlag("Y");
				transferProductsToLotInfo.setValidationFlag("Y");
				transferProductsToLotInfo.setProductPSequence(productPList);
				transferProductsToLotInfo.setProductQuantity(productPList.size());
				transferProductsToLotInfo.setUdfs(lotData.getUdfs());
				
				
				//EventInfo
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Merge", this.getEventUser(), this.getEventComment(), "", "");
				
				
				LotServiceProxy.getLotService().transferProductsToLot(lotData.getKey(), eventInfo, transferProductsToLotInfo);	
			}
		}
		
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment(), "", "");
		
		//TrackOut
 		//Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, newLotData, portData, 
			//									carrierName, lotJudge, machineName, "",
				//								productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, aHoldFlag, null);
		
		return doc;
	}

}
