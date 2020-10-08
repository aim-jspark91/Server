package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotFutureAction;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.lot.management.sql.SqlStatement;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;
import org.jdom.Element;

public class GlassProcessEnd extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		List<Element> productElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String lotJudge = "";
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		String lotName = MESLotServiceProxy.getLotServiceUtil().getLotNamefromProductElements(productElement);
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		List<Element> newProductList = new ArrayList<Element>();
		
		for (Element product : productElement )
		{
			String processingInfo = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
			
			if(StringUtil.equals(processingInfo, "P"))
			{
				newProductList.add(product);
			}
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), null, null);
		Lot trackOutLot = null;
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(lotData.getKey().getLotName(), newProductList);
		
		if (lotData.getProductQuantity() > productPGSRCSequence.size() && productPGSRCSequence.size() > 0)
		{
			trackOutLot = this.splitGlass(eventInfo, lotData, productPGSRCSequence);
			
			copyFutureAction(lotData, trackOutLot);
			
		}
		else
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
			trackOutLot = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
		}
		
		eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		
		productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(newProductList, machineName);
		productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequenceProcessingInfo(productPGSRCSequence, "");
		
		// Auto-judge lotGrade
		//lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(trackOutLot, lotJudge, productPGSRCSequence);
		
		// Decide Sampling
		String decideSampleNodeStack = "";
		//boolean decidedsampleFlag = false;
		MESLotServiceProxy.getLotServiceUtil().getDecideSample(doc, eventInfo, trackOutLot);
		
		//20180504, kyjung, QTime
		eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		List<Map<String, Object>> queuePolicyData = null;
		
		Map<String, Object> qTimeTPEFOPolicyData = MESProductServiceProxy.getProductServiceImpl().checkPriorityPolicy(trackOutLot);
		
		if(qTimeTPEFOPolicyData != null)
		{
			queuePolicyData = MESProductServiceProxy.getProductServiceImpl().checkQTimePolicy(qTimeTPEFOPolicyData);
			
			if(queuePolicyData != null)
			{
				for (Element product : newProductList )
				{   
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
					
					MESProductServiceProxy.getProductServiceImpl().EnteredQTime(eventInfo, eventInfo1, productName, queuePolicyData);
				}
			}
		}
		
		Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, trackOutLot, portData, carrierName, lotJudge, "", "",
				productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack, false,null);
		
		setNextInfo(doc, afterTrackOutLot);
		
		// Success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
		//for common
		/*EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		HashMap<String, String> assignCarrierUdfs = new HashMap<String, String>();
		HashMap<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		
		List<Lot> trackOutLotList = this.getLotListByProductList(productList);
		
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PL") ||
				(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PP") && productList.size() < 1))
		{//PL end
			trackOutLotList = CommonUtil.getLotListByCarrier(carrierName, false);
		}
		
		for (Lot trackOutLot : trackOutLotList)
		{
			String lotJudge = "";			
			String reworkFlag = "";
			
			List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(trackOutLot.getKey().getLotName(), productList);
			
			if (trackOutLot.getProductQuantity() > productPGSRCSequence.size() && productPGSRCSequence.size() > 0)
			{
				trackOutLot = this.splitGlass(eventInfo, trackOutLot, productPGSRCSequence);
			}
			
			//refined Lot logged in
			Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);
			
			//151224 by swcho : Port scenario handling
			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PL") ||
					(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") .equals("PP") && productList.size() < 1))
			{
				eventLog.warn(String.format("Job end with any Product at Machine[%s] Port[%s] PortType[%s] CST[%s]",
											machineName, portName, CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), carrierName));
				
				if (productPGSRCSequence.size() > 0)
				{
					cancelTrackIn(eventInfo, trackOutLot, carrierName, productPGSRCSequence);
				}
				else
				{
					deassignCarrier(eventInfo, trackOutLot);
				}
				
				continue;
			}
			
			// Auto-Rework Case : OLED NG
			//reworkFlag = this.prepareRework(reworkFlag, productPGSRCSequence, portData);
			
			// Auto-judge
			lotJudge = MESLotServiceProxy.getLotServiceUtil().decideLotJudge(trackOutLot, lotJudge, productPGSRCSequence);
			
			// Auto-Rewok Case#1 : Q-time
			//ExtendedObjectProxy.getQTimeService().monitorQTime(eventInfo, trackOutLot.getKey().getLotName());
			//MESLotServiceProxy.getLotServiceUtil().doQTimeAction(doc, trackOutLot.getKey().getLotName());
			//ExtendedObjectProxy.getQTimeService().moveInQTime(eventInfo, lotName, trackOutLot.getFactoryName(), trackOutLot.getProductSpecName(),
			//		trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName());
			
			// Auto-Rework Case#2 : NG
			//reworkFlag = StringUtil.equals(trackOutLot.getReworkState(), "NotInRework") && lotJudge.equalsIgnoreCase("R")?"Y":reworkFlag;
						
			// Sampling
			//MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(trackOutLot, new ArrayList<Element>() , true);
			//MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, trackOutLot);
			
			//udfs
			trackOutLot.getUdfs().put("UNITNAME", "");
			
			String decideSampleNodeStack = "";
			
			//TK OUT
			Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLot(eventInfo, trackOutLot, portData, 
													carrierName, lotJudge, "", "",
													productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, decideSampleNodeStack);

			// Start Rework
			//afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startRework(eventInfo, afterTrackOutLot, reworkFlag, true);
			
			// Repair
			//afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().startAlteration(eventInfo, afterTrackOutLot, afterTrackOutLot.getLotGrade());
			
			// Skip
			//afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, beforeTrackOutLot, afterTrackOutLot);
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}*/
	}
	
	/**
	 * must be in here at last point of event
	 * @since 2015.08.21
	 * @author swcho
	 * @param doc
	 * @param lotData
	 */
	private void setNextInfo(Document doc, Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder();
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]").append("\n")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]").append("\n")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]").append("\n")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK OUT is nothing");
		}
	}
	
	private void copyFutureAction(Lot parent, Lot child)
	{
		//Copy  FutureAction from parent to child
		// Find future Action
		Object[] bindSet =
				new Object[] {
				parent.getKey().getLotName(),
				parent.getFactoryName(),
				parent.getProcessFlowName(),
				parent.getProcessFlowVersion(),
				parent.getProcessOperationName(),
				parent.getProcessOperationVersion() };

		try
		{
			List<LotFutureAction> lotFutureActionList = LotServiceProxy.getLotFutureActionService().select(SqlStatement.LotFutureActionKey, bindSet);
			if(lotFutureActionList.size() > 0)
			{
				Object[] bindSetChild =
						new Object[] {
						child.getKey().getLotName(),
						child.getFactoryName(),
						child.getProcessFlowName(),
						child.getProcessFlowVersion(),
						child.getProcessOperationName(),
						child.getProcessOperationVersion() };
				
				String condition = "";
				
				for(LotFutureAction action : lotFutureActionList)
				{
					LotServiceProxy.getLotFutureActionService().update(action, condition, bindSetChild);
				}
			}
		}
		catch (NotFoundSignal ne)
		{
			return;
		}
		catch(Exception e)
		{
			return;
		}
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
	
	/*private void sendToFMC(Document doc)
	{
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}*/
	
	/*private String prepareRework(String reworkFlag, List<ProductPGSRC> productPGSRCSequence, Port portData)
	{
		if (portData != null && StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"), "NG")
				|| StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"), "RW"))
		{
			reworkFlag = "Y";
			
			for (ProductPGSRC productPGSRC : productPGSRCSequence)
			{
				productPGSRC.setProductGrade("R");
				productPGSRC.setReworkFlag("Y");
			}
		}
		
		return reworkFlag;
	}*/
	
	
	
	/**
	 * temp cancel track-In logic
	 * @author swcho
	 * @since 2015-04-26
	 * @param eventInfo
	 * @param lotData
	 * @param productList
	 * @throws CustomException
	 */
	/*private void cancelTrackIn(EventInfo eventInfo, Lot lotData, String carrierName, List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		eventInfo.setEventName("CancelTrackIn");
		lotData = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, lotData, null, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
	}
	*/
	/*private List<Lot> getLotListByProductList(List<Element> productList) throws CustomException
	{
		List<String> lotNameList = new ArrayList<String>();
		
		for (Element productElement : productList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			String evaSamplingFlag = SMessageUtil.getChildText(productElement, "EVASAMPLINGFLAG", false);
			//String lotName = SMessageUtil.getChildText(productElement, "LOTNAME", true);
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			Map<String, String> udfs = productData.getUdfs();
			
			udfs.put("FURNACEFLAG", evaSamplingFlag);
			
			productData.setUdfs(udfs);
			
			ProductServiceProxy.getProductService().update(productData);
			
			if (!lotNameList.contains(productData.getLotName()))
			{
				lotNameList.add(productData.getLotName());
			}
		}
		
		List<Lot> lotList = new ArrayList<Lot>();
		
		for (String lotName : lotNameList)
		{
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			lotList.add(lotData);
		}
		
		return lotList;
	}
	
	private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		try
		{
			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			
			if(StringUtils.equals(carrierData.getDurableState(), "InUse"))
			{
				eventInfo.setEventName("Deassign");
				
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				
				DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);
				
				lotData = MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
			}
		}
		catch (Exception ex)
		{
			eventLog.error(ex.getMessage());
		}
	}*/
}
