package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutCut extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		List<String> lotList = new ArrayList<String>();
		
		for (Element product : productList)
		{   
			String sLotName = product.getChild("LOTNAME").getText();
			
			if(!lotList.contains(sLotName))
			{
				lotList.add(sLotName);
			}
		}
		
		Lot newLotData = null;
		
		for(String lotName : lotList)
		{		
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			
			Durable assignCST = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			boolean isNoChangeLotFlag = true;
			String firstLotName = null;
			
			List<ProductP> productPList = new ArrayList<ProductP>();
			for (Element product : productList)
			{   
				String sLotName = product.getChild("LOTNAME").getText();
				
				if(firstLotName == null)
				{
					firstLotName = sLotName;
				}
				else if(!firstLotName.equals(sLotName))
				{
					isNoChangeLotFlag = false;
				}
				
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
				if(isNoChangeLotFlag == true && lotData.getProductQuantity() == productList.size())
				{
					newLotData = lotData;
				}
				else
				{
					//Get Child Lot Name
				    String newLotName = StringUtil.substring(lotData.getKey().getLotName(), 0, 8);
					List<String> argSeq = new ArrayList<String>();
					argSeq.add(newLotName);
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
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCForTrackOutForOPI(newLotData, productList);
		
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
		String beforeProductSpecName = newLotData.getProductSpecName();
		String beforeECCode = newLotData.getUdfs().get("ECCODE");
		String beforeProcessFlowName = newLotData.getProcessFlowName();
		String beforeProcessFlowVersion = newLotData.getProcessFlowVersion();
		String beforeProcessOperationName = newLotData.getProcessOperationName();
		String beforeProcessOperationVersion = newLotData.getProcessOperationVersion();
		
   		String decideSampleNodeStack = "";
		
   		boolean aHoldFlag = false;
   		
   		try {
   	   		// MQC Job에 생성된 공정들과 Lot 의 ProcessFlow 의 공정들이 다르다면 aHoldFlag 를 true 로 설정
   			aHoldFlag = MESLotServiceProxy.getLotServiceImpl().getAholdFlagMQCOperationListAndProcessFlowOperationList(newLotData.getKey().getLotName(),beforeProcessFlowName);
		} catch (Exception e) {
			
		}
   		if(!aHoldFlag){
   			aHoldFlag = MESLotServiceProxy.getLotServiceUtil().isExistAhold(newLotData.getKey().getLotName(), beforeProcessFlowName, beforeProcessOperationName);
   		}
   		
   		
   		String superLotFlag = newLotData.getUdfs().get("SUPERLOTFLAG");
   		if (StringUtils.isEmpty(superLotFlag) || !superLotFlag.equals("Y"))
   		{
			EventInfo eventInfoForDecideSample = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			eventInfoForDecideSample.setEventTime(eventInfo.getEventTime());
			eventInfoForDecideSample.setEventTimeKey(eventInfo.getEventTimeKey());
			
   			MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfoForDecideSample, newLotData);
   		}
   		
   		// aHold is exist, not go to SampleFlow.
   		if(!aHoldFlag)
   		{
   			String sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkReservedSamplingInfo(newLotData.getKey().getLotName(), beforeProcessFlowName, beforeProcessFlowVersion, beforeProcessOperationName, eventInfo);
   			
   	   		if (StringUtils.isEmpty(sampleFlowName) && (StringUtils.isEmpty(superLotFlag) || superLotFlag.equals("N")))
   	   			sampleFlowName = MESLotServiceProxy.getLotServiceUtil().checkNormalSamplingInfo(newLotData.getKey().getLotName(), beforeProcessOperationName, eventInfo);
   			
   			if (StringUtil.isNotEmpty(sampleFlowName))
   				decideSampleNodeStack = MESLotServiceProxy.getLotServiceUtil().getOperFirstNodeStack(sampleFlowName);
   		}
		
		//TrackOut
 		Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, newLotData, portData, 
												carrierName, "G", machineName, "",
												productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, aHoldFlag, null);
		
		return doc;
	}

}
