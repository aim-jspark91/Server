package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackInLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME",false);
		String autoFlag = SMessageUtil.getBodyItemValue(doc, "AUTOFLAG", false);
		String maskBarcodeIDList = SMessageUtil.getBodyItemValue(doc,"MASKBARCODEIDLIST", false);
		String maskSpec = SMessageUtil.getBodyItemValue(doc, "MASKSPEC", false);
		List<Element> slotMapList = SMessageUtil.getBodySequenceItemList(doc, "SLOTMAPLIST", true);
		

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(),getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		ProcessOperationSpec processOperationSpec =  getProcessOperationSpec(lotData);
		// Added by smkang on 2018.11.26 - According to Liu Hongwei's request, check possible to run again.
		if (!MESLotServiceProxy.getLotServiceUtil().possibleToOPIRun(lotName, machineName)&&!StringUtils.equals("DUMMY", processOperationSpec.getDetailProcessOperationType()))
			throw new CustomException("COMMON-0001", "Impossible to run, MachineName[" + machineName + "] and ProcessOperationName[" + lotData.getProcessOperationName() + "]");
		
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		
		String machineGroupName = null;

		// check Track Flag 
		boolean checkTrackFlag = false;
		
		List<Product> sProductListForTRCKFlag = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		for (Product sProductInfo : sProductListForTRCKFlag) 
		{
			ProductFlag productFlag = new ProductFlag();
			
			try
			{
				productFlag = ExtendedObjectProxy.getProductFlagService().selectByKey(false, new Object[] {sProductInfo.getKey().getProductName()});
			}
			catch(Throwable e)
			{
				eventLog.error(sProductInfo.getKey().getProductName() + " is not exist ProductFlag Info.");
			}
			
			if(productFlag!=null)
			{
				String trackFlag = productFlag.getTrackFlag();
				
				if (StringUtil.equals(trackFlag, GenericServiceProxy.getConstantMap().Flag_Y))
				{
					checkTrackFlag = true;
					break;
				}
				else
				{
					continue;
				}
			}
		}

		if ( checkTrackFlag == true )
		{
			//2018.12.13_hsryu_Modify Same PEX Logic.
//			if(StringUtil.equals(eqpData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH) || 
//					StringUtil.equals(eqpData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
//			{
				if ( StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
						StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) )
				{
					// Nothing : Normal
				}
				else
				{
					// Abnormal
					throw new CustomException("MACHINE-1001", machineSpecData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG"));
				}
//			}
		}

		/* <<== 20180731, hhlee, Modify, Change to PEX Logic */
		
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkProductGradeN(lotData);
		
		//check MachineOperationMode
		CommonValidation.checkMachineOperationModeExistence(eqpData);

		//2019.01.31_hsryu_Check PanelJudge Definition. Mantis 0002640.
		MESLotServiceProxy.getLotServiceUtil().checkPanelJudgeInDummyOperation(lotData);
		
		// Validation MachineState
		//CommonValidation.ChekcMachinState(eqpData);	
		//CommonValidation.CheckSortOperaion(lotData, lotName);
		// Validation LotGrade
		// 2016.03.18 by hwlee89
		//MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

		// 2017.01.13 zhongsl validate POSMask DurableSpec(UVMask)
//		if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ASY") && lotData.getFactoryName().equals("CELL")) {
//			MESDurableServiceProxy.getDurableServiceUtil().validateMaskMapping(machineName, lotData, lotData.getCarrierName());
//		}

		if (!autoFlag.equals("Y")) {
			if (!lotData.getCarrierName().isEmpty()) {
				// Update 2016.03.22 by hwlee89
				// CommonValidation.checkDurableDirtyState(lotData.gefortCarrierName());
				CommonValidation.CheckDurableHoldState(lotData.getCarrierName());
			}
		}

		Map<String, String> lotUdfs = lotData.getUdfs();

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		/**
		 * 20180208 Not Use Table about First Job : NJPARK
		 * 
		 * //2017.8.22 zhongsl Check current Operation mapping targetOper when
		 * firstGlass case
		 * if(MESLotServiceProxy.getLotServiceUtil().checkFirstGlassTargetOper
		 * (lotData)) { if
		 * (lotData.getLotHoldState().equals(GenericServiceProxy.
		 * getConstantMap().Flag_N)) { lotData =
		 * MESLotServiceProxy.getLotServiceUtil().executeHold(eventInfo,
		 * lotData, "HoldLot",
		 * GenericServiceProxy.getConstantMap().Pilot_ReasonCode_Generic); }
		 * 
		 * throw new
		 * CustomException("LOT-0102",lotData.getKey().getLotName().toString());
		 * }
		 * 
		 * //Pilot validation //ExtendedObjectProxy.getFirstGlassJobService().
		 * vaildateLotProcessing(lotData, machineName);
		 * ExtendedObjectProxy.getFirstGlassJobService
		 * ().validateLotProcessingV2(lotData, machineName);
		 */
		
		// 2018.10.25
//		String checkMQCMachine = "";
//		if (MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData).getProcessFlowType().equals("MQC")) {
//			
//			checkMQCMachine = MESProductServiceProxy.getProductServiceImpl().checkMQCMachine(lotData,machineName );
//			if(StringUtil.isEmpty(checkMQCMachine))
//			{
//				throw new CustomException("MQC-0048");
//			}
//		}
//		
//		// Recipe
//		if (MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData).getProcessFlowType().equals("MQC")) {
//			recipeName = checkMQCMachine;
//		}
//		else {
//			if(StringUtil.isEmpty(recipeName))
//			{
//				recipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeByTPEFOMPolicy(lotData.getFactoryName(),
//								lotData.getProductSpecName(),
//								lotData.getProcessFlowName(),
//								lotData.getProcessOperationName(), machineName,
//								lotData.getUdfs().get("ECCODE"));
//			}	
//		}

		
		// Recipe Validation (check if POSMachine recipe matches any recipes of
		// Mask in TRK machine)
		if (!MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData).getProcessFlowType().equals("MQC")) {
			MESDurableServiceProxy.getDurableServiceUtil().validateRecipeMapping(machineName, recipeName,lotName);
		} else { 		// 2019.01.19 Check MQC Job Operation and Machine
			if(!MESProductServiceProxy.getProductServiceImpl().checkMQCMachineforTrackin(lotData,machineName))
			{
				throw new CustomException("MQC-0048");
			}
		}
		
		//20180525, kyjung, MQC
		MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotName);

		// 20180504, kyjung, QTime
		MESProductServiceProxy.getProductServiceImpl().checkQTime(lotName);
		
		if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
		{
			eventLog.info("ProbeCard check Started.");
			
			PolicyUtil.checkDefinePhotoMaskAndProbeCardByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), recipeName, "PROBECARD");

//			String pbCardType = "";
//
//			if(StringUtil.equals(CommonUtil.getValue(eqpData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
//			{
//				//get one PBCardType by mahcine
//				 pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(machineName).get(0).getDurableSpecName();
//
//				 PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), recipeName, pbCardType);
//			}
//			else
//			{
//				//INDP
//				List<String>unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(machineName);
//
//				for(String unit : unitList)
//				{
//					pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(unit).get(0).getDurableSpecName();
//					PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), recipeName, pbCardType);
//				}
//			}

			eventLog.info("ProbeCard check Ended.");
		}

		//PhotoMask
		if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK"))
		{
			eventLog.info("PhotoMask check Started.");
			
			PolicyUtil.checkDefinePhotoMaskAndProbeCardByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), recipeName, "PHOTOMASK");

//			//also check mask state
//			List<Durable> PhotoMaskList = MESDurableServiceProxy.getDurableServiceUtil().getPhotoMaskNameByMachineName(machineName);
//
//            if(PhotoMaskList.size() <= 0)
//            {
//                throw new CustomException("MASK-0098", machineName);                
//            }
//
//			PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), recipeName, PhotoMaskList);
			eventLog.info("PhotoMask check Ended .");
			
			//check Product ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.
			eventLog.info("Check ProcessTurnFlag or TurnOverFlag and 'TRACK' Machine.");
			
			List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			List<ProductFlag> productFlagList = new ArrayList<ProductFlag>();
			
			for(Product productData : productList)
			{
                String condition = "WHERE productName = ? ";
                Object[] bindSet = new Object[]{ productData.getKey().getProductName()};
                
                try
                {
        			productFlagList = ExtendedObjectProxy.getProductFlagService().select(condition, bindSet);
                }
                catch(Throwable e)
                {
                	eventLog.error("Product [" + productData.getKey().getProductName() + " [ProductFlag is not exist");
                }
                
                if(productFlagList.size()>0)
                {
                	for(ProductFlag productFlag : productFlagList)
                	{
                    	if(StringUtils.equals(productFlag.getTurnOverFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                    	{
                            throw new CustomException("PRODUCT-0034", productData.getKey().getProductName());
                    	}
                    	
                    	if(StringUtils.equals(productFlag.getProcessTurnFlag(), GenericServiceProxy.getConstantMap().Flag_Y))
                    	{
                            throw new CustomException("PRODUCT-0035", productData.getKey().getProductName());
                    	}
                	}
                }
			}
			eventLog.info("PhotoMask check Ended .");
		}
		

		try
		{
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());
			machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
		}
		catch (Exception ex)
		{
		}
		
		String bending = StringUtil.EMPTY;
		SMessageUtil.setBodyItemValue(doc, "BENDING", bending);
		
		List<Element> productListElement = new ArrayList<Element>();
		
		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		
		for(Product productData : productList)
		{
			Element productElement = new Element("PRODUCT");
			
			Element productNameE = new Element("PRODUCTNAME");
			productNameE.setText(productData.getKey().getProductName());
			productElement.addContent(productNameE);
			
			productListElement.add(productElement);
		}

		if(ExtendedObjectProxy.getProductFlagService().checkProductFlagElaQtime(productListElement))
		{
			if(!CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ELA"))
			{
				throw new CustomException("PRODUCT-9004", lotData.getKey().getLotName()); 
			}      
		}            
		
		//2018.11.20_hsryu_add.
		MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOverForOPI(machineName, lotData, portName);
        
		// 20180612, kyjung, Recipe Idle Time
		//MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(machineName, recipeName, lotData.getProductSpecName(), lotData.getProcessOperationName());

		// Deleted by smkang on 2018.09.03 - It is unnecessary to update product with update method.
		//									 UDF will be set to ProductSequence.
//		//PORTNAME UPDATE YJYU 시작
//		List<Product> sProductList = LotServiceProxy.getLotService().allProducts(lotData.getKey().getLotName());
//
//		for (Product sProductInfo : sProductList) 
//		{
//			Map<String, String> productUdfs = sProductInfo.getUdfs();
//			productUdfs.put("PORTNAME", portName);//YJYU
//			
//			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
//			
//			setEventInfo.setUdfs(productUdfs);
//			
//			ProductServiceProxy.getProductService().update(sProductInfo);
//		    /*
//			EventInfo producteventInfo = EventInfoUtil.makeEventInfo(
//					"CreateMaskInfo", getEventUser(), getEventComment(),
//					null, null);
//			ProductServiceProxy.getProductService().setEvent(
//					sProductInfo.getKey(), producteventInfo, setEventInfo);
//			*/
//		}
//		//YJYU 끝
		
		List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		// 20180504, kyjung, QTime
		for (Product productData : productDataList) {
			MESProductServiceProxy.getProductServiceImpl().ExitedQTime(eventInfo, productData, "TrackIn");
		}

		// TrackIn
		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);
		
		Lot trackInLot =null;
		String carrierName =null;
		if(productCSequence.size() == slotMapList.size()){
			// Added by smkang on 2018.09.03 - It is unnecessary to update product with update method.
			//								   UDF will be set to ProductSequence.
			for (ProductC productC : productCSequence) {
				productC.getUdfs().put("PORTNAME", portName);
				//2018.12.21_hsryu_after TrackIn, ProcessingInfo is null. Requested by CIM.
				productC.getUdfs().put("PROCESSINGINFO", "");
			}
		}
		else{
			Lot targetLot= null;
			productCSequence = new ArrayList<ProductC>();
			List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
			String splitProductList = StringUtil.EMPTY;
			
		    for (Element product : slotMapList )   
	        {
                String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);
                Product productData =  ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
                
				ProductC productC = new ProductC();
				productC.setProductName(productData.getKey().getProductName());
				//productC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
				productC.getUdfs().put("PORTNAME", portName);
				productC.getUdfs().put("PROCESSINGINFO", "");
				
				ProductPGSRC productPGSRC = new ProductPGSRC();
				productPGSRC.setProductName(productName);
				productPGSRC.setPosition(productData.getPosition());
				productPGSRC.setProductGrade(productData.getProductGrade());
				productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
				productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
				productPGSRC.setReworkFlag("N");

				//productPGSRC.setCms(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());
				productData.getUdfs().put("PROCESSINGINFO", GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P);
				productPGSRC.setUdfs(productData.getUdfs());
				
				productPGSRCSequence.add(productPGSRC);

				productCSequence.add(productC);

	        }
		    eventInfo.setEventName("Create");
		    targetLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLot(eventInfo, lotData, "", true, new HashMap<String, String>(), lotData.getUdfs());
		    

	        List<ProductP> productPSequence = new ArrayList<ProductP>();
	        for (ProductPGSRC productPGSRC : productPGSRCSequence)
	        {
	            ProductP productP = new ProductP();
	            productP.setProductName(productPGSRC.getProductName());
	            productP.setPosition(productPGSRC.getPosition());
	            productP.setUdfs(productPGSRC.getUdfs());
	            productPSequence.add(productP);
	            
	            if(StringUtil.isEmpty(splitProductList))
	            {
	                splitProductList = productPGSRC.getProductName();
	            }
	            else
	            {
	                splitProductList = splitProductList + " , " +  productPGSRC.getProductName() ;
	            }
	        }
	        
	        splitProductList = "[Change Product] - " + splitProductList;
	        
	        
	        lotUdfs.put("NOTE", splitProductList);                 
	        lotData.setUdfs(lotUdfs);      
	        LotServiceProxy.getLotService().update(lotData);
	        /* <<== 20180928, hhlee, add, Lot Note */

	        TransferProductsToLotInfo  transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(targetLot.getKey().getLotName(), productPSequence.size(), productPSequence, lotData.getUdfs(), new HashMap<String, String>());

	        eventInfo.setEventName(GenericServiceProxy.getConstantMap().SORT_JOBTYPE_SPLIT);
	        eventInfo.setCheckTimekeyValidation(false);
	        /* 20181128, hhlee, EventTime Sync */
	        //eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
	        eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());

	        lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);
	        lotData = targetLot;
		}
		
		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
		
		//2018.08.22 - YJYU NOTE가 리셋되지 않아 HISTORY에 계속 NOTE 값이 복제 되어 초기화 
		lotUdfs.put("NOTE", "");
		
		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence,lotUdfs);

		carrierName = lotData.getCarrierName();

		eventInfo.setEventName("TrackIn");
		trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLotForOPI(eventInfo, lotData, makeLoggedInInfo);

		
		// Deleted by smkang on 2019.05.16 - According to Liu Hongwei's request, PEX should be executed only.
//		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
//		// Added by smkang on 2018.08.31 - Update MQCPreRunCount of MQCCondition.
//		try {
//			/********** 2019.02.01_hsryu_Delete Logic ***********/
////			String condition = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
////			Object[] bindSet = new Object[] {machineName, "UNIT"};
////			List<MachineSpec> unitSpecList = MachineServiceProxy.getMachineSpecService().select(condition, bindSet);
//			
////			for (MachineSpec unitSpec : unitSpecList) {
//				MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, trackInLot, portName, (String)eqpData.getUdfs().get("OPERATIONMODE"), eventInfo);
////			}
//		} catch (Exception e) {
//			eventLog.warn(e);
//			/********** 2019.02.01_hsryu_Delete Logic ***********/
//			// Added by smkang on 2018.11.17 - Although a machine has no unit, updateMachineIdleTimePreRunInfo should be invoked.
//			//MESMachineServiceProxy.getMachineServiceImpl().updateMachineIdleTimePreRunInfo(machineName, "", trackInLot, eventInfo);
//		}
//		// -----------------------------------------------------------------------------------------------------------------------------------------------------------

		//Added by jjyoo on 2018.09.19 - check inhibit condition
		//MESLotServiceProxy.getLotServiceImpl().checkInhibitCondition(eventInfo, lotData, machineName, recipeName);
		
		trackInLot = MESLotServiceProxy.getLotServiceUtil().executeSampleLot(trackInLot);
		
		//2018.11.17_hsryu_check FirstLotFlag.
		//MESLotServiceProxy.getLotServiceUtil().checkFirstLotFlagByRecipeIdleTime(machineName, recipeName);
		
		if(!StringUtil.equals(trackInLot.getProductionType(), "MQCA"))
		{
			if(!StringUtil.isEmpty(carrierName))
			{
				MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(
						(Document) doc.clone(),
						trackInLot.getFactoryName(),
						eqpData.getAreaName(),
						trackInLot.getKey().getLotName(), 
						carrierName, 
						machineName, 
						portData.getKey().getPortName(),
						recipeName, 
						eventInfo);
			}
		}

		if (!lotData.getCarrierName().isEmpty()) {
			// IncrementTimeUsed For Carrier by hwlee89
			// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trackInLot.getCarrierName());
			Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(trackInLot.getCarrierName()));

			//YJYU 시작
			//이부분에서 durable limit 정보를 durable spec 정보로 업데이트
			StringBuilder sqlStatement = new StringBuilder();
			sqlStatement.append("SELECT  DS.TIMEUSEDLIMIT, D.TIMEUSED \n")
						.append("  FROM DURABLE D, DURABLESPEC DS \n")
						.append(" WHERE D.DURABLENAME = ?\n")
						.append("   AND D.FACTORYNAME = ?\n")
						.append("   AND D.FACTORYNAME = DS.FACTORYNAME AND D.DURABLESPECNAME = DS.DURABLESPECNAME \n");
						
			List<ListOrderedMap> queryResultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), new Object[] { lotData.getCarrierName(), trackInLot.getFactoryName() });
			
			String TIMEUSEDLIMIT = "";
			String TIMEUSED = "";
	
			if (queryResultList != null && queryResultList.size() > 0)
			{
				for (ListOrderedMap queryResult : queryResultList) 
				{
					TIMEUSEDLIMIT = queryResult.get("TIMEUSEDLIMIT").toString();
					TIMEUSED = queryResult.get("TIMEUSED").toString();
				}
			}
			
			if(StringUtil.isNotEmpty(TIMEUSEDLIMIT))
			{
				long TIMEUSEDLIMIT_INT = Long.parseLong(TIMEUSEDLIMIT);
				long TIMEUSED_INT = StringUtil.isNotEmpty(TIMEUSED)? Long.parseLong(TIMEUSED) : 0;
				
				eventInfo.setEventName("Use");
				
				incrementTimeUsed(durableData,TIMEUSEDLIMIT_INT,TIMEUSED_INT,eventInfo,lotData); 
			}			
			//YJYU 끝
		}

		if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL") &&
			StringUtils.isNotEmpty(trackInLot.getCarrierName())) {
			try {
				deassignCarrier(eventInfo, trackInLot);
			} catch (CustomException ce) {
				eventLog.error("Deassign failed");
			}
		}
		
		if(!StringUtil.isEmpty(recipeName))
		{
			eventInfo.setEventName("TrackIn");
			Map<String, String> machineUdfs = eqpData.getUdfs();
			machineUdfs.put("MACHINERECIPENAME", recipeName);
			SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineUdfs);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(eqpData, setEventInfo, eventInfo);
		}	
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		MESLotServiceProxy.getLotServiceUtil().deletePermanentHoldInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),lotData.getProcessOperationName(), eventInfo);

		// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//	    lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//
//		// For Clear Note, Add By Park Jeong Su
//		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
//		lotData.getUdfs().put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);

		/**
		 * 20180208 Not Use Table about First Job : NJPARK //Deassign Carrier
		 * //170210 by swcho : pilot case for 4F to 2F if
		 * (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL") &&
		 * CommonUtil.getValue(machineSpecData.getUdfs(),
		 * "CONSTRUCTTYPE").equals("SRT") &&
		 * (CommonUtil.getValue(eqpData.getUdfs(),
		 * "OPERATIONMODE").equals("LTPS2ARRAY") ||
		 * CommonUtil.getValue(eqpData.getUdfs(),
		 * "OPERATIONMODE").equals("ARRAY2LTPS")) ) {
		 * deassignCarrierAtSortOnPilot(eventInfo, trackInLot); } else
		 * if(StringUtil.equals(CommonUtil.getValue(portData.getUdfs(),
		 * "PORTTYPE"), "PL") &&
		 * StringUtils.isNotEmpty(trackInLot.getCarrierName())) { try {
		 * deassignCarrier(eventInfo, trackInLot); } catch (CustomException ce)
		 * { eventLog.error("Deassign failed"); } }
		 */

		/**
		 * 20180208 Material Management Check (Need to modify) : NJPARK
		 * //20170303 by zhanghao ConsumMaterials Record List<ListOrderedMap>
		 * MaterialPositionNameList
		 * =MESConsumableServiceProxy.getConsumableServiceUtil
		 * ().getTPOMMaterialPositionNameList(lotData.getFactoryName(),
		 * lotData.getProductSpecName(), lotData.getProcessOperationName(),
		 * machineName); List<Product> pProductList =
		 * LotServiceProxy.getLotService
		 * ().allProducts(lotData.getKey().getLotName());
		 * if(MaterialPositionNameList!=null) { for(ListOrderedMap
		 * MaterialPositionName :MaterialPositionNameList) {
		 * if((!MaterialPositionName
		 * .isEmpty())||MaterialPositionName.getValue(0)=="null") { String
		 * condition =
		 * "MATERIALPOSITIONNAME=?  AND MACHINENAME=? AND EVENTNAME='Load' " +
		 * "AND TIMEKEY=(SELECT MIN(TIMEKEY) FROM CONSUMABLEHISTORY  WHERE MATERIALPOSITIONNAME=?  AND MACHINENAME=? AND EVENTNAME='Load'"
		 * +
		 * "AND CONSUMABLENAME IN (SELECT CONSUMABLENAME FROM CONSUMABLE WHERE MATERIALPOSITIONNAME=? AND MACHINENAME=? ) )"
		 * ; Object bindSet[] = new
		 * Object[]{MaterialPositionName.getValue(0).toString
		 * (),machineName,MaterialPositionName
		 * .getValue(0).toString(),machineName
		 * ,MaterialPositionName.getValue(0).toString(),machineName};
		 * List<ConsumableHistory>
		 * ConsumableList=ConsumableServiceProxy.getConsumableHistoryService
		 * ().select(condition, bindSet); ConsumableHistory
		 * Consumablehistory=ConsumableList.get(0); Consumable Consumable =
		 * MESConsumableServiceProxy
		 * .getConsumableInfoUtil().getConsumableData(Consumablehistory
		 * .getKey().getConsumableName());
		 * 
		 * Consumable.setConsumableState("Available");
		 * ConsumableServiceProxy.getConsumableService().update(Consumable);
		 * List<ListOrderedMap>consumeQuantity=MESConsumableServiceProxy.
		 * getConsumableServiceUtil
		 * ().getPosbomConsumeQuantity(lotData.getFactoryName(),
		 * lotData.getProductSpecName(), lotData.getProcessOperationName(),
		 * machineName, Consumable.getConsumableSpecName()); Double
		 * productConsumeQuantity
		 * =Double.parseDouble(consumeQuantity.get(0).getValue(0).toString());
		 * 
		 * List<ConsumedMaterial> consumedMaterialLotList= new
		 * ArrayList<ConsumedMaterial>(); ConsumedMaterial
		 * consumedMaterialLot=new ConsumedMaterial();
		 * consumedMaterialLot.setMaterialName
		 * (Consumable.getKey().getConsumableName());
		 * consumedMaterialLot.setMaterialType("consumable");
		 * consumedMaterialLot
		 * .setQuantity(productConsumeQuantity*pProductList.size());
		 * consumedMaterialLotList.add(consumedMaterialLot);
		 * 
		 * List<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial
		 * > consumedMaterialProductList = new
		 * ArrayList<kr.co.aim.greentrack.product
		 * .management.info.ext.ConsumedMaterial>();
		 * kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial
		 * consumedMaterialProduct = new
		 * kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial();
		 * consumedMaterialProduct
		 * .setMaterialName(Consumable.getKey().getConsumableName());
		 * consumedMaterialProduct.setMaterialType("consumable");
		 * consumedMaterialProduct.setQuantity(productConsumeQuantity);
		 * consumedMaterialProductList.add(consumedMaterialProduct);
		 * 
		 * List<ProductGSC> productGSCSequence=new ArrayList<ProductGSC>();
		 * for(Product productInfo:pProductList) { ProductGSC productGSC = new
		 * ProductGSC(); String sProductName =
		 * productInfo.getKey().getProductName();
		 * productGSC.setProductName(sProductName);
		 * productGSC.setCms(consumedMaterialProductList);
		 * productGSCSequence.add(productGSC); }
		 * 
		 * EventInfo consumEventInfo =
		 * EventInfoUtil.makeEventInfo("consumableMaterial", getEventUser(),
		 * getEventComment(), null, null); ConsumeMaterialsInfo
		 * consumeMaterialsInfo
		 * =MESLotServiceProxy.getLotInfoUtil().consumeMaterialsInfo(lotData,
		 * consumedMaterialLotList, productGSCSequence);
		 * LotServiceProxy.getLotService().consumeMaterials(lotData.getKey(),
		 * consumEventInfo, consumeMaterialsInfo);
		 * 
		 * String
		 * consumableName=ConsumableList.get(0).getKey().getConsumableName();
		 * Consumable
		 * ConsumableDate=MESConsumableServiceProxy.getConsumableInfoUtil
		 * ().getConsumableData(consumableName);
		 * ConsumableDate.setConsumableState
		 * (GenericServiceProxy.getConstantMap().Cons_InUse);
		 * ConsumableServiceProxy.getConsumableService().update(ConsumableDate);
		 * 
		 * if(ConsumableDate.getQuantity()<=0) { ConsumableDate.setQuantity(0);
		 * EventInfo autoUnKitEventInfo =
		 * EventInfoUtil.makeEventInfo("autoUnKitMaterial", getEventUser(),
		 * getEventComment(), null, null);
		 * //kr.co.aim.messolution.consumable.event
		 * .UnKitMaterial.unkitConsumableData
		 * (autoUnKitEventInfo,ConsumableDate,"0"); Map<String, String> udfs =
		 * ConsumableDate.getUdfs(); udfs.put("MACHINENAME", "");
		 * udfs.put("TRANSPORTSTATE",
		 * GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		 * 
		 * ConsumableDate.setConsumableState(GenericServiceProxy.getConstantMap(
		 * ).Cons_Available); ConsumableDate.setMaterialLocationName("");
		 * ConsumableDate.setUdfs(udfs);
		 * 
		 * kr.co.aim.greentrack.consumable.management.info.SetEventInfo
		 * setEventInfo =
		 * MESConsumableServiceProxy.getConsumableInfoUtil().setEventInfo
		 * (ConsumableDate, ConsumableDate.getAreaName());
		 * ConsumableServiceProxy.getConsumableService().update(ConsumableDate);
		 * 
		 * MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(
		 * ConsumableDate.getKey().getConsumableName(), setEventInfo,
		 * autoUnKitEventInfo); } } } }
		 */

		// 150117 by swcho : success then report to FMC
		try {
			GenericServiceProxy.getESBServive().sendBySender(
					GenericServiceProxy.getESBServive()
					.getSendSubject("FMCsvr"), doc, "FMCSender");
		} catch (Exception ex) {
			eventLog.warn("FMC Report Failed!");
		}
	
		return doc;
	}

	private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException {
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		if (StringUtils.equals(carrierData.getDurableState(), "InUse")) {
			eventInfo.setEventName("DeassignCarrier");

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData,productUSequence);

			// Modified by smkang on 2018.10.19 - For synchronization of a carrier state, common method will be invoked.
//			LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
			MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
		}
	}
	private ProcessOperationSpec getProcessOperationSpec(Lot lotData){
		
		ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();

		processOperationKey.setFactoryName(lotData.getFactoryName());
		processOperationKey.setProcessOperationName(lotData.getProcessOperationName());
		processOperationKey.setProcessOperationVersion("00001");
		
		ProcessOperationSpec processOperationSpec =  ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);
		
		return processOperationSpec;
	}
	
	//YJYU - 2018.08.27
	public void incrementTimeUsed (Durable DurableData, long TIMEUSEDLIMIT, long TIMEUSED, EventInfo eventInfo,Lot lotData ) 
	{
		try{
		/* VALIDATION 
		CurrentTimeUsedOutOfRange
		TimeUsedOutOfRange
		CanNotDoAtDurableState */
		
		//Process Operation Used Value
		String ProcessOperationName = lotData.getProcessOperationName();
		
		String strSql = 
				" 	SELECT NVL(TIMEUSED,1) TIMEUSED "+    
				" 	 FROM PROCESSOPERATIONSPEC "+    
				" 	WHERE PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
		List<Map<String, Object>> TimeUsed = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		long TIMEUSED_OPER = 0;
		long TIMEUSED_TOTAL = 0;	
		
		if(TimeUsed != null && TimeUsed.size() > 0)
		{	
			TIMEUSED_OPER = Long.parseLong(TimeUsed.get(0).get("TIMEUSED").toString());
		}
		else
		{
			TIMEUSED_OPER = 1;	
		}
		
		TIMEUSED_TOTAL = Long.parseLong(DurableData.getUdfs().get("TOTALUSEDCOUNT").toString() ) + TIMEUSED_OPER;
		TIMEUSED_TOTAL = TIMEUSED_TOTAL -1;				
		
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> durableUdfs = DurableData.getUdfs();
//		durableUdfs.put("TOTALUSEDCOUNT", Long.toString(TIMEUSED_TOTAL)); 
//		DurableData.setUdfs(durableUdfs);
		
		DurableData.setTimeUsedLimit(TIMEUSEDLIMIT);
		DurableData.setLastEventComment("TrackInLot");
		//DurableData.setTimeUsed(TIMEUSED+1);
		DurableData.setTimeUsed(TIMEUSED + TIMEUSED_OPER);
		
		if (TIMEUSED + TIMEUSED_OPER + 1 >= TIMEUSEDLIMIT)
		{
			DurableData.setDurableCleanState("Dirty");
		}
		
		eventInfo.setEventName("Use");
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
		setEventInfo.getUdfs().put("TOTALUSEDCOUNT", Long.toString(TIMEUSED_TOTAL));
		
		DurableServiceProxy.getDurableService().update(DurableData);
		DurableServiceProxy.getDurableService().setEvent(DurableData.getKey(), eventInfo, setEventInfo);
		}
		catch (Exception e) 
		{
			eventLog.error("TimeUsed Error");
		}
	}

	private void deassignCarrierAtSortOnPilot(EventInfo eventInfo, Lot lotData) throws CustomException {
		String pilotJobName = ExtendedObjectProxy.getFirstGlassLotService().getActiveJobNameByLotName(lotData.getKey().getLotName());

		// Carrier not yet deassigned
		if (lotData.getCarrierName().isEmpty()) {
			return;
		}

		if (!pilotJobName.isEmpty()) {
			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

			for (Lot subLotData : CommonUtil.getLotListByCarrier(carrierData.getKey().getDurableName(), false)) {
				if (StringUtils.equals(carrierData.getDurableState(), "InUse")) {
					// release hold temporary during pilot
					if (subLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_Y)) {
						eventInfo.setEventName("ReleaseHold");
						subLotData = MESLotServiceProxy.getLotServiceUtil().releaseHold(
										eventInfo,
										subLotData,
										"HoldLot",
										GenericServiceProxy.getConstantMap().Pilot_ReasonCode_Generic);
					}

					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(subLotData);
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(subLotData,carrierData, productUSequence);

					eventInfo.setEventName("DeassignCarrier");
					subLotData = LotServiceProxy.getLotService().deassignCarrier(subLotData.getKey(), eventInfo,deassignCarrierInfo);

					ExtendedObjectProxy.getFirstGlassLotService().adjustCarrier(eventInfo, pilotJobName,subLotData.getKey().getLotName(), "");
				}
			}
		} else {
			try {
				deassignCarrier(eventInfo, lotData);
			} catch (CustomException ce) {
				eventLog.error("Deassign failed");
			}
		}
	}

}
