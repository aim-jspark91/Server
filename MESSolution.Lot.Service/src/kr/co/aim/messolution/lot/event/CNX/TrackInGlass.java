package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.DeassignMaterialsInfo;
import kr.co.aim.greentrack.processgroup.management.info.ext.MaterialU;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackInGlass extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//additional info initialized
		setEventComment("");
		
		//String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		//optional
		Machine machineData;
		Port portData;
		if (!portName.isEmpty()) portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		else portData = null;
		
		//2019.02.01_hsryu_add Logic. get MachineData.
		machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		//Q-time
		/*ExtendedObjectProxy.getQTimeService().monitorQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().validateQTime(eventInfo, lotName);
		ExtendedObjectProxy.getQTimeService().exitQTime(eventInfo, lotName, lotData.getProcessOperationName());*/
		
		//transaction per Lot
		for (Element elementLot : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true))
		{
			String lotName = SMessageUtil.getChildText(elementLot, "LOTNAME", true);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			// Added by smkang on 2018.11.26 - According to Liu Hongwei's request, check possible to run again.
			if (!MESLotServiceProxy.getLotServiceUtil().possibleToOPIRun(lotName, machineName))
				throw new CustomException("COMMON-0001", "Impossible to run, MachineName[" + machineName + "] and ProcessOperationName[" + lotData.getProcessOperationName() + "]");
			
			//2007.9.29 zhongsl exit Q-time
			ExtendedObjectProxy.getQTimeService().exitQTime(eventInfo, lotName, lotData.getProcessOperationName());
			
			ProcessOperationSpec operationSpec = CommonUtil.getProcessOperationSpec(lotData, lotData.getProcessOperationName());
			
			Map<String, String> lotUdfs = lotData.getUdfs();
			
			MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);
			
			if (!lotData.getCarrierName().isEmpty() && portData == null)
				throw new CustomException("CST-1001");

			//TrackIn
			List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
			
			// Added by smkang on 2018.09.03 - According to EDO's request, PortName should be updated.
			for (ProductC productC : productCSequence) {
				productC.getUdfs().put("PORTNAME", portName);
			}
			
			if (portData != null)
			{
				lotUdfs.put("PORTNAME", portData.getKey().getPortName());
				lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
				lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
			}
			
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));

			MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, machineRecipeName, productCSequence, lotUdfs);
			
			//inline tracking special
			makeLoggedInInfo.getUdfs().put("UNITNAME", unitName);
			
			eventInfo.setEventName("TrackIn");
			Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);
			
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
			// Added by smkang on 2018.08.31 - Update MQCPreRunCount of MQCCondition.
			try {
				/********** 2019.02.01_hsryu_Delete Logic ***********/
//				String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
//				Object[] bindSet = new Object[] {machineName, "UNIT"};
//				List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
				
//				for (MachineSpec unitSpec : unitSpecList) {
					MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, trackInLot, portName, (String)machineData.getUdfs().get("OPERATIONMODE"), eventInfo);
//				}
			} catch (Exception e) {
				eventLog.warn(e);
				/********** 2019.02.01_hsryu_Delete Logic ***********/
				// Added by smkang on 2018.11.17 - Although a machine has no unit, updateMachineIdleTimePreRunInfo should be invoked.
//				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, "", trackInLot, eventInfo);
			}
			// -----------------------------------------------------------------------------------------------------------------------------------------------------------
			
			//Deassign Carrier
			//160319 by swhco : inline buffer port is PB
			if(StringUtils.isNotEmpty(trackInLot.getCarrierName())
					&& (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB")))
			{
				try
				{
					trackInLot = deassignCarrier(eventInfo, trackInLot);
				}
				catch (CustomException ce)
				{
					eventLog.error("Deassign failed");
				}
			}
			
			if (StringUtils.isNotEmpty(trackInLot.getProcessGroupName())) 
			{
				this.deassignBox(eventInfo, trackInLot);				
			}
			
			if (!StringUtils.equals(operationSpec.getDetailProcessOperationType(), "OQC") 
					&& !StringUtils.equals(operationSpec.getDetailProcessOperationType(), "PPK")) 
			{
				splitGlass(eventInfo, trackInLot);
			}
			
		}

		sendToFMC(doc);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, getEventComment().toString());
		
		return doc;
	}
	
	private void deassignBox(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("DeassignToBox");
		int prdqty = 0;
		String boxName = lotData.getProcessGroupName();	
		
		ProcessGroup boxData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(boxName);
		ProcessGroupKey processGroupKey = new ProcessGroupKey();
		processGroupKey.setProcessGroupName(boxName);
		
		Map<String, String> udfs = new HashMap<String, String>();
		List<MaterialU> materialUList = new ArrayList<MaterialU>();
		udfs = boxData.getUdfs();
		
		if(StringUtil.isNotEmpty(String.valueOf(lotData.getProductQuantity())))
			prdqty = prdqty + (int)lotData.getProductQuantity();
		
		prdqty = Integer.parseInt(boxData.getUdfs().get("productQuantity")) - prdqty;
		udfs.put("productQuantity", String.valueOf(prdqty));
		boxData.setUdfs(udfs);
		
		//1.update productQuantity
		ProcessGroupServiceProxy.getProcessGroupService().update(boxData);
		
		MaterialU materialU = new MaterialU();
		materialU.setMaterialName(lotData.getKey().getLotName());
		materialUList.add(materialU);
		
		DeassignMaterialsInfo deassignMaterialsInfo = new DeassignMaterialsInfo();
		deassignMaterialsInfo.setMaterialQuantity(materialUList.size());
		deassignMaterialsInfo.setMaterialUSequence(materialUList);
		
		//2.update materialQuantity and lot deassignProcessGroup
		ProcessGroupServiceProxy.getProcessGroupService().deassignMaterials(processGroupKey, eventInfo, deassignMaterialsInfo);		
	}

	private void splitGlass(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
				
		if (productList.size() > 1 
				|| (productList.size() == 1 && !productList.get(0).getKey().getProductName().equalsIgnoreCase(productList.get(0).getLotName())))
		{
			eventLog.info("Split Glass as Lot for inline");
			
			for (Product productData : productList)
			{
				eventLog.info(String.format("Product[%s] would be separated", productData.getKey().getProductName()));
				
				eventInfo.setEventName("Create");
				Lot garbageLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, productData.getKey().getProductName(), lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
				
				List<ProductP> productPSequence = new ArrayList<ProductP>();
				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(productData.getPosition());
				productP.setUdfs(productData.getUdfs());
				productPSequence.add(productP);
				
				TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(
																garbageLot.getKey().getLotName(), 1, productPSequence, lotData.getUdfs(), new HashMap<String, String>());
				
				//do split
				eventInfo.setEventName("Split");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
				
				garbageLot = MESLotServiceProxy.getLotInfoUtil().getLotData(garbageLot.getKey().getLotName());
				
				setNextInfo(garbageLot);
				
				eventLog.info(String.format("Lot[%s] is separated", productData.getKey().getProductName()));
			}
		}
	}
	
	private Lot deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		
		if(StringUtils.equals(carrierData.getDurableState(), "InUse"))
		{
			eventInfo.setEventName("DeassignCarrier");
			
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			
			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);
			
			// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//			lotData = LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
			return MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
		}
		
		return null;
	}
	
	private void setNextInfo(Lot lotData)
	{
		try
		{
			StringBuilder strComment = new StringBuilder(getEventComment());
			strComment.append("LotName").append("[").append(lotData.getKey().getLotName()).append("]")
						.append("LotGrade").append("[").append(lotData.getLotGrade()).append("]")
						.append("NextFlow").append("[").append(lotData.getProcessFlowName()).append("]")
						.append("NextOperation").append("[").append(lotData.getProcessOperationName()).append("]").append("\n");
			
			setEventComment(strComment.toString());
		}
		catch (Exception ex)
		{
			eventLog.warn("Note after TK In is nothing");
		}
	}
	
	private void sendToFMC(Document doc)
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
	}
}
