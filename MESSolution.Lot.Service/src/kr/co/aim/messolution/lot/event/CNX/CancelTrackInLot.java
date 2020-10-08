package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.jdom.Document;
import org.jdom.Element;

public class CancelTrackInLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		//for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
				
		//Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().composeLot(eventInfo, carrierName, productList);
		Lot cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().getTrackOutLot(eventInfo, carrierName, lotName, productList);
		
		// Mentis 2868 2019.02.27
//		if(MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(cancelTrackInLot).getProcessFlowType().equals("Sort"))
//		{
//			if(!MESLotServiceProxy.getLotServiceUtil().checkSortCarrier(carrierName))
//			{
//				throw new CustomException("SORT-0007");
//			}
//		}
		
		if (cancelTrackInLot == null)
		{
			throw new CustomException("LOT-XXXX", carrierName);
		}
		
		//PORTNAME UPDATE YJYU Ω√¿€
		for (Element productEle : productList) 
		{
			String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
			
			// Modified by smkang on 2019.05.28 - ProductServiceProxy.getProductService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			Map<String, String> productUdfs = productData.getUdfs();
//			productUdfs.put("PORTNAME", "");//YJYU
//			
//			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//			setEventInfo.setUdfs(productUdfs);
//			ProductServiceProxy.getProductService().update(productData);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("PORTNAME", "");
			
			//start add by jhying on20200330 mantis:5924
//			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Product.class.getSimpleName());
//			SetMaterialLocationInfo setMaterialLocationInfo = MESProductServiceProxy.getProductInfoUtil().setMaterialLocationInfo(productData, StringUtil.EMPTY, udfs);
//			
//			MESProductServiceProxy.getProductServiceImpl().setMaterialLocation(eventInfo, productData, setMaterialLocationInfo);
			updateUdfs.put("MATERIALLOCATIONNAME", "");
			
			//end add by jhying on20200330 mantis:5924
			
			MESProductServiceProxy.getProductServiceImpl().updateProductWithoutHistory(productData, updateUdfs);
		}
		//YJYU ≥°
		
		//20180504, kyjung, QTime
		for (Element productEle : productList )
		{   
			String productName = SMessageUtil.getChildText(productEle, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy .getProductServiceUtil().getProductData(productName);
			
			MESProductServiceProxy.getProductServiceImpl().ExitedCancelQTime(eventInfo, productData, "TrackIn");
		}	
				
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList, machineName);

		//String recipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProductSpecName(),
				//cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessOperationName(), machineName, cancelTrackInLot.getUdfs().get("ECCODE"));
		
		//Added by jjyoo on 2018.09.19
		//When Track in Cancel, if there is inhibit data applied before, procss lot count should be decreased
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		String recipeName = lotData.getMachineRecipeName();
		
		if(StringUtil.isNotEmpty(recipeName))
		{
			MESProductServiceProxy.getProductServiceImpl().cancelTIRecipeIdleTimeLot(machineName, recipeName, eventInfo);
		}
		
		//MESLotServiceProxy.getLotServiceImpl().decreaseInhibitProcessLotCount(eventInfo, lotData, machineName);
		
		eventInfo.setEventName("CancelTrackIn");
		cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().cancelTrackIn(eventInfo, cancelTrackInLot, portData, "", carrierName, "", productPGSRCSequence, new HashMap<String, String>(), new HashMap<String,String>());
		
		//20180521, kyjung, make New MQC
		//20170516 Add by yudan	for PL/PU MQC CancelTrackIn	
		/*Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		List<MQCPlan> mqcPlanList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanList(lotData);
		
		if(mqcPlanList != null && portData.getUdfs().get("PORTTYPE").equals("PL") && !lotName.equals(cancelTrackInLot.getKey().getLotName()))
		{
			try
			{						
				String condition = " WHERE PRODUCTREQUESTNAME = ? ";
				Object[] bindSet = new Object[]{lotData.getProductRequestName()};
				List<ProductRequestHistory> productRequestList = ExtendedObjectProxy.getProductRequestHistoryService().select(condition, bindSet);
				
				String productSpecName = productRequestList.get(0).getProductSpecName();
				ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), productSpecName, "00001");
				
				List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUdfs(cancelTrackInLot.getKey().getLotName());

				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(cancelTrackInLot.getKey().getLotName(),
						productSpecData.getProductionType(), productSpecData.getKey().getProductSpecName(), cancelTrackInLot.getProductSpecVersion(), cancelTrackInLot.getProductSpec2Name(), cancelTrackInLot.getProductSpec2Version(),
						cancelTrackInLot.getProductRequestName(), cancelTrackInLot.getSubProductUnitQuantity1(), cancelTrackInLot.getSubProductQuantity2(), cancelTrackInLot.getDueDate(), cancelTrackInLot.getPriority(),
						cancelTrackInLot.getFactoryName(), cancelTrackInLot.getAreaName(), cancelTrackInLot.getLotState(), cancelTrackInLot.getLotProcessState(), cancelTrackInLot.getLotHoldState(),
						cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion(),cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(),
						CommonUtil.getValue(cancelTrackInLot.getUdfs(), "RETURNFLOWNAME"), CommonUtil.getValue(cancelTrackInLot.getUdfs(), "RETURNOPERATIONNAME"), "", "","",
						cancelTrackInLot.getUdfs(), productUdfs,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
						true);
				
				eventInfo.setEventName("PauseMQCJob");
				
				cancelTrackInLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, cancelTrackInLot, changeSpecInfo);
		    }
			catch(Exception ex)
			{
				
			}
		}*/

		//Port clear - YJYU
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		Lot lotData_Port = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Port.getUdfs();
//		udfs_note.put("PORTNAME", "");
//		LotServiceProxy.getLotService().update(lotData_Port);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("PORTNAME", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		// Mentis 2868 2019.02.27
//		if(MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(cancelTrackInLot).getProcessFlowType().equals("Sort"))
//		{
//			MESLotServiceProxy.getLotServiceUtil().CancelTrackInSort(carrierName, eventInfo);
//		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
		
		return doc;
	}	
}
