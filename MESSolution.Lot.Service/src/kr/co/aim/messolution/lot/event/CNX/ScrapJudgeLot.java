package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapJudgeLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		String desDurableName = SMessageUtil.getBodyItemValue(doc, "DESDURABLENAME", false);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		List<Element> scrapProductList = SMessageUtil.getBodySequenceItemList(doc, "SCRAPPRODUCTLIST", true);
			
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapJudgeGlass", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(desDurableName);
		
		Map<String, String> childInfo = new HashMap<String, String>();
		
		Map<String, Integer> qtyMap = new HashMap<String, Integer>();
		List<String> scrapList = new ArrayList<String>();	
		
		ProductRequestKey pKey = new ProductRequestKey();
		pKey.setProductRequestName(productRequestName);
		
		//validation
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		
		if (!durableName.isEmpty())
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			CommonValidation.CheckDurableHoldState(durableData);
		}
				
		for (Element eleProduct : scrapProductList) 
		{			
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);			
			Product prdData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
								
			ProductRequestKey key = new ProductRequestKey(prdData.getProductRequestName());
			
			try
			{
				ProductRequestServiceProxy.getProductRequestService().selectByKey(key);
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-9999", "Product", "Not Exist WorkOrder");
			}
						
			if(! StringUtil.equals(lotData.getProductRequestName(), prdData.getProductRequestName()))
			{
				throw new CustomException("PRODUCT-0032");
			}				
			
			//20170324 Add by yudan
			if(qtyMap.containsKey(key.getProductRequestName()))
			{
				qtyMap.put(key.getProductRequestName(), qtyMap.get(key.getProductRequestName()) + 1);
			}
			else
			{
				qtyMap.put(key.getProductRequestName(), 1);
			}
		
			scrapList.add(key.getProductRequestName());						
		}
		
		//WorkOrder Scrapped Quantity   *20170324 Modify by yudan*
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().calculateProductRequestQty(productRequestName, null, "S", scrapProductList.size(), eventInfo);
		
		//Split Lot
		Lot childLot = MESLotServiceProxy.getLotServiceUtil().splitLot(eventInfo, scrapProductList,lotData);	
				
		//Deassign box
		String boxName = lotData.getProcessGroupName(); 
		Lot lotData1 = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		if (StringUtils.isNotEmpty(boxName))
		{
			eventInfo.setEventName("DeassignToBox");
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(boxName);
			ProcessGroup boxData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(boxName);
		    
			//desDurableState is InUse
			int prdQty = scrapProductList.size();
			prdQty = Integer.parseInt(boxData.getUdfs().get("productQuantity")) - prdQty;
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("productQuantity", String.valueOf(prdQty));
			boxData.setUdfs(udfs);
			
			ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
			
			if (lotData1.getProductQuantity() == 0) 
			{
				List<MaterialU> materialUList = new ArrayList<MaterialU>();
			    MaterialU materialU = new MaterialU();
			    materialU.setMaterialName(lotData1.getKey().getLotName());
			    materialUList.add(materialU);
			
			    DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
			    deassignMaterialsInfo.setMaterialQuantity(materialUList.size());
			    deassignMaterialsInfo.setMaterialUSequence(materialUList);
			
		        ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials(processGroupKey, eventInfo, deassignMaterialsInfo);
			}
			
			//desDurableState is Available
			if (!StringUtils.isEmpty(childLot.getProcessGroupName())) 
			{
				if (!StringUtils.equals(lotData.getKey().getLotName(), childLot.getKey().getLotName())) 
				{
					boxData.setMaterialQuantity(boxData.getMaterialQuantity() + 1);
					
				    ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
				}				
			
			    List<MaterialU> materialUList = new ArrayList<MaterialU>();
			    MaterialU materialU = new MaterialU();
			    materialU.setMaterialName(childLot.getKey().getLotName());
			    materialUList.add(materialU);
			
			    DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
			    deassignMaterialsInfo.setMaterialQuantity(materialUList.size());
			    deassignMaterialsInfo.setMaterialUSequence(materialUList);
			
		        ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials(processGroupKey, eventInfo, deassignMaterialsInfo);
			}
		}
		
		try
		{							
			List<Element> assignList = new ArrayList<Element>();
			
			for (Element elePrd : scrapProductList) 
			{
				String prdState = SMessageUtil.getChildText(elePrd, "PRODUCTSTATE", true);
				
				if(!StringUtil.equals(prdState, "Scrapped"))
					assignList.add(elePrd);
			}
									
			childLot = MESLotServiceProxy.getLotServiceUtil().arrangeCarrier(eventInfo, childLot, desDurableData, assignList);
				
			childLot.setLotGrade("S");																	
			LotServiceProxy.getLotService().update(childLot);
			
			Map<String, String> udfs = childLot.getUdfs();
			
			SetEventInfo setEventInfo = new SetEventInfo();			
			setEventInfo.setUdfs(udfs);
			
			eventInfo.setEventName("ScrapJudgeGlass");
			LotServiceProxy.getLotService().setEvent(childLot.getKey(), eventInfo, setEventInfo);		
		}
		catch (Exception ex)
		{
			eventLog.error("CST arrange failed");
		}
		
		for (Element eleProduct : scrapProductList) 
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);			
			Product prdData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			//judge
			ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(prdData, 
					prdData.getPosition(), "S", prdData.getProductProcessState(),
					prdData.getSubProductGrades1(), prdData.getSubProductGrades2(), prdData.getSubProductQuantity1(), prdData.getSubProductQuantity2());
			
			prdData = MESProductServiceProxy.getProductServiceImpl().changeGrade(prdData, changeGradeInfo, eventInfo);			
		
			Map<String, String> prdUdfs = prdData.getUdfs();
			kr.co.aim.greentrack.product.management.info.SetEventInfo prdSetEvInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			prdSetEvInfo.setUdfs(prdUdfs);

		}
		
		childInfo.put(childLot.getCarrierName(), childLot.getKey().getLotName());
			
		try
		{
			if(!StringUtil.equals(childLot.getKey().getLotName(), lotName))
			{
				Lot changeGradeLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				
				this.changeGrade(eventInfo, changeGradeLotData);
			}
		}
		catch (Exception ex)
		{
			eventLog.error("Not Found Source Product List");
		}

		setNextInfo(doc, childInfo);
		
		return doc;
	}
	
	/**
	 * must be in here at last point of event
	 * @author swcho
	 * @since 2015-11-27
	 * @param doc
	 * @param childInfo
	 */
	private void setNextInfo(Document doc, Map<String, String> childInfo)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			
			for (String durableName : childInfo.keySet())
			{
				strComment.append(String.format("Lot[%s] is divided into CST[%s]", childInfo.get(durableName), durableName)).append("\n");
			}
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}		
	
	private void changeGrade(EventInfo eventInfo, Lot lotData) throws CustomException
	{		
		List<ProductPGSRC> productPGSRCSequence = MESProductServiceProxy.getProductInfoUtil().getProductPGSRCSequence(lotData);
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();
				
		String lotGrade = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(lotData, "", productPGSRCSequence);
		
		for (ProductPGSRC productPGSRC : productPGSRCSequence) 
		{
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productPGSRC.getProductName());
			
			ProductPGS productPGS = new ProductPGS();
			productPGS.setProductName(productPGSRC.getProductName());
			productPGS.setProductGrade(productPGSRC.getProductGrade());
			productPGS.setPosition(productPGSRC.getPosition());
			productPGS.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGS.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGS.setUdfs(productData.getUdfs());
			
			productPGSSequence.add(productPGS);
		}		
		
		kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);
		
		eventInfo.setEventName("ScrapJudgeGlass");
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		Lot result = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		
		if(!lotData.getLotGrade().equals(result.getLotGrade()))
		{
			Map<String, String> udfs = result.getUdfs();
			
			eventInfo.setEventName("ChangeGrade");
			
			SetEventInfo setEventInfo = new SetEventInfo();			
			setEventInfo.setUdfs(udfs);
			LotServiceProxy.getLotService().setEvent(result.getKey(), eventInfo, setEventInfo);
		}
		
	}
}
