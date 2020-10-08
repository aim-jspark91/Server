package kr.co.aim.messolution.lot.event.CNX;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ExposureFeedBack;
import kr.co.aim.messolution.extended.object.management.data.ProductFlag;
import kr.co.aim.messolution.extended.object.management.data.VirtualGlass;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequestplan.ProductRequestPlanServiceProxy;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlan;
import kr.co.aim.greentrack.productrequestplan.management.data.ProductRequestPlanKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

//import com.sun.xml.internal.ws.policy.jaxws.PolicyUtil;

public class CheckLotValidation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		//String slotMap     = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);

		//existence validation
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		boolean isInitialInput = CommonUtil.isInitialInput(machineName);
		Lot lotData = null;

		if(!isInitialInput)
		{
    		//lotData = this.getLotData(carrierName);
		    //lotData = MESLotServiceProxy.getLotInfoUtil().getLotValidateData(carrierName);
		}
		
		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		List<ProductU> allProductList = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		
		//DurableSpec durableSpecData
		//check Durable Hold State
		CommonValidation.CheckDurableHoldState(durableData);

		//check MachineState
		//CommonValidation.ChekcMachinState(machineData);

		ProcessFlow processFlow = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		//PB,PL Port

		//check LotState/ProcessState/HoldLotState
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkProductGradeN(lotData);

		//2019.01.31_hsryu_Check PanelJudge Definition. Mantis 0002640.
		MESLotServiceProxy.getLotServiceUtil().checkPanelJudgeInDummyOperation(lotData);
		
		eventLog.info("CommonValidation.checkMachineOperationModeExistence is invoked");
		//check MachineOperationMode
		CommonValidation.checkMachineOperationModeExistence(machineData);
		
		eventLog.info("MESProductServiceProxy.getProductServiceImpl().checkQTime is invoked");
		//20180504, kyjung, QTime
		MESProductServiceProxy.getProductServiceImpl().checkQTime(lotData.getKey().getLotName());

		eventLog.info("MESProductServiceProxy.getProductServiceImpl().checkMQCLot is invoked");
		//20180525, kyjung, MQC
		MESProductServiceProxy.getProductServiceImpl().checkMQCLot(lotData.getKey().getLotName());

		eventLog.info("MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeByTPEFOMPolicy is invoked");
		//Recipe
		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, lotData.getUdfs().get("ECCODE"));

		//eventLog.info("MESLotServiceProxy.getLotServiceImpl().checkInhibitConditionForCheckLotValidation is invoked");

		//MESLotServiceProxy.getLotServiceImpl().checkInhibitConditionForCheckLotValidation(lotData,  machineName,  machineRecipeName);

		//2018.11.17_hsryu_add checkMachineIdleTimeOver
//		List<MachineSpec> unitSpecList = null;
//		String conditionForMachineIdleTime = "SUPERMACHINENAME = ? AND DETAILMACHINETYPE = ?";
//		Object[] bindSetForMachineIdleTime = new Object[] {machineName, "UNIT"};
//		try
//		{
//			unitSpecList = MachineServiceProxy.getMachineSpecService().select(conditionForMachineIdleTime, bindSetForMachineIdleTime);
//		}
//		catch(Throwable e)
//		{}
//
//		if(unitSpecList!=null)
//		{
//			for (MachineSpec unitSpec : unitSpecList) {
//				MESMachineServiceProxy.getMachineServiceImpl().checkMachineIdleTimeOver(machineName, unitSpec.getKey().getMachineName(), lotData);
//			}
		//		}
		//		else

		MESMachineServiceProxy.getMachineServiceUtil().checkMachineIdleTimeOverForOPI(machineName, lotData, "");
		// -----------------------------------------------------------------------------------------------------------------------------------------------------------

//		if (lotData != null && !lotData.getProductionType().equals("MQCA")) {
//			if (MESMachineServiceProxy.getMachineServiceUtil().isOverMachineIdleTime(machineName, "") || machineData.getMachineStateName().equals("MQC")) {
//				List<MQCCondition> mqcConditionList = null;
//				try {
//					mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select("MACHINENAME = ? AND MQCPRODUCTSPECNAME = ?", new Object[] {machineName, lotData.getProductSpecName()});
//				} catch (Exception e) {
//					eventLog.info(e);
//				}
//
//				if ((mqcConditionList == null || mqcConditionList.size() == 0) ||
//					(mqcConditionList.get(0).getMqcRunCount() + mqcConditionList.get(0).getMqcPreRunCount() < mqcConditionList.get(0).getMqcPlanCount())) {
//					throw new CustomException("TRANSPORT-0006", machineName);
//				}
//			}
//		}
//		
		//2018.11.17_hsryu_check FirstLotFlag.
		//MESLotServiceProxy.getLotServiceUtil().checkFirstLotFlagByRecipeIdleTime(machineName, machineRecipeName);
		// 20180612, kyjung, Recipe Idle Time
		//MESProductServiceProxy.getProductServiceImpl().checkRecipeIdleTime(machineName, machineRecipeName, lotData.getProductSpecName(), lotData.getProcessOperationName());
		/* <<== 20180616, hhlee, Modify */			

		//slot map validation
		boolean isValidationCheck = true;
		
		//2018.09.13 DMLEE
		/*
		 usage check 
		String flag = CommonUtil.getValue(machineSpecData.getUdfs(), "RMSFLAG");                
		if (flag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
		{
			 RMS Check            
			if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK"))
			{
				List<Map<String, Object>> unitDatabyHierarchy = 
						MESMachineServiceProxy.getMachineServiceUtil().getMachineDataByHierarchy(machineName, "2", 
								StringUtil.EMPTY, StringUtil.EMPTY, GenericServiceProxy.getConstantMap().Mac_ProductionMachine);

				int machineIndex = 999;
				int machineRecipeIndex = 999;
				String constructType = StringUtil.EMPTY;
                 [Regular expression]Cut by 4 characters to create an array 
                 where the 4 dots after the G indicates every nth position to split. In this case, the 4 dots indicate every 4 positions 
                 * "(?<=\\G....)" 
                 * "(?<=\\G.4)" 
                 * String[] splitStr = str.split("(?<=\\G.{" + 4 + "})");
                 * Between elements of an array | add delimiters and join them in a single string
                 * str = StringUtils.join(splitStr, "|"); 
                String[] machinerecipeSeperate = machineRecipeName.split("(?<=\\G.{" + 4 + "})");
                
                for(int i = 0; i < machinerecipeSeperate.length; i++)
                { 
                    machineIndex = 999;
                    machineRecipeIndex = 999;
                    constructType = StringUtil.EMPTY;
                    
                    for(int j = 0; j < unitDatabyHierarchy.size(); j++)
                    {
                        constructType = StringUtil.EMPTY;
                        
                        machineIndex = j;
                        machineRecipeIndex = i;
                        
                        machineName = unitDatabyHierarchy.get(machineIndex).get("UNITNAME").toString();
                        machineRecipeName = machinerecipeSeperate[machineRecipeIndex].toString();
                        
                        if(unitDatabyHierarchy.get(j).get("CONSTRUCTTYPE") != null)
                        {   
                            constructType = unitDatabyHierarchy.get(j).get("CONSTRUCTTYPE").toString();
                        }
                                
                        if(i == 0 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_SCNN))
                        {                            
                            break;
                        }
                        else if(i == 1 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_EGEY))
                        {
                            break;
                        }
                        else if(i == 2 && StringUtil.equals(constructType, GenericServiceProxy.getConstantMap().ConstructType_TRACK_TRKD))
                        {
                            break;
                        }
                        else
                        {
                            machineIndex = 999;
                            machineRecipeIndex = 999;
                            break;
                        }
                    }
                    
                    if(machineIndex != 999 && machineRecipeIndex != 999)
                    {
                        if(!MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeInfo(machineName, machineRecipeName, "UNIT", "Approved"))
                        {
                            isValidationCheck = false;
                            break;
                        }
                    } 
                    else
                    {
                        isValidationCheck = false;  
                        break;
                    }
                }                
            }
            else
            {
                if(!MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeInfo(machineName, machineRecipeName, "MAIN", "Approved"))
                {
                    isValidationCheck = false;
                }                       
            }
            if(!isValidationCheck)
            {
            	throw new CustomException("RMS-0006", machineRecipeName); 
            }
        }*/
       
		//ProbeCard
		//ProbeCard
		if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("ATST"))
		{
			eventLog.info("ProbeCard check Started.");
			
			PolicyUtil.checkDefinePhotoMaskAndProbeCardByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, "PROBECARD");

//			String pbCardType= "";
//
//			if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
//			{
//				//get one PBCardType by mahcine
//				List<Durable> pbCardList = MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(machineName);
//				
//				if(pbCardList!=null)
//				{
//					 pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByMachine(machineName).get(0).getDurableSpecName();
//					 PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
//				}
//			}
//			else
//			{
//				//INDP
//				List<String>unitList = MESDurableServiceProxy.getDurableServiceUtil().getUnitListByMahcineName(machineName);
//
//				for(String unit : unitList)
//				{
//					pbCardType = MESDurableServiceProxy.getDurableServiceUtil().getPBListByUnitName(unit).get(0).getDurableSpecName();
//					PolicyUtil.checkMachineProbeCardTypeByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, pbCardType);
//				}
//			}

			eventLog.info("ProbeCard check Ended.");
		}

		//PhotoMask
		if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("TRACK"))
		{
			eventLog.info("PhotoMask check Started.");
			
			PolicyUtil.checkDefinePhotoMaskAndProbeCardByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, "PHOTOMASK");

//			//also check mask state
//			List<Durable> PhotoMaskList = MESDurableServiceProxy.getDurableServiceUtil().getPhotoMaskNameByMachineName(machineName);
//            if(PhotoMaskList.size() <= 0)
//            {
//                throw new CustomException("MASK-0098", machineName);
//            }
//
//			PolicyUtil.checkMachinePhotoMaskByTPEFOMPolicy(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, CommonUtil.getValue(lotData.getUdfs(), "ECCODE"), machineRecipeName, PhotoMaskList);
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

		String toMachineName = StringUtil.EMPTY;
		//Only INDP has toMachineName
		//List<Map<String,Object>>toMachineAndOperationName = MESLotServiceProxy.getLotServiceUtil().getToMachineNamegetToMachineNameAndOperationNameAndOperationName(lotList,machineName,CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
		/*if(toMachineAndOperationName.size() > 0)
		{
			toMachineName = CommonUtil.getValue(toMachineAndOperationName.get(0), "MACHINENAME");
		}*/
		/* <<== hhlee, 20180616, Added TOMACHINE specified logic. */

		if(!StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
		{
			//checkOperationMode
			MESLotServiceProxy.getLotServiceUtil().checkOperationModeByCT_OperationMode(machineName, CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
		}

		
		
		// PHOTO TRCKFLAG MANAGEMENT
		boolean checkTrackFlag = false;
		
		List<Product> sProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		
		for (Product sProductInfo : sProductList) 
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
//			if(StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_ETCH) || 
//					StringUtil.equals(machineData.getAreaName(), GenericServiceProxy.getConstantMap().MACHINE_AREA_PHOTO))
//			{
				if ( StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_DRYF) || 
						StringUtil.equals(machineSpecData.getUdfs().get("TRACKFLAG"), GenericServiceProxy.getConstantMap().PHOTO_STRP) )
				{
					// Nothing : Normal
				}
				else
				{
					// Abnormal
					throw new CustomException("MACHINE-1001", 
							machineSpecData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG"));
				}
//			}
		}
		else
		{
			//// Abnormal
			//throw new CustomException("MACHINE-1001", 
			//        machineSpecData.getKey().getMachineName(), machineSpecData.getUdfs().get("TRACKFLAG"));
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
		/* <<== 20180619, Add ELA Q-Time Flag Check */

		return doc;
	}



	private String createSlotMapforUPK(Durable durableData) throws CustomException
	{
		String SlotMap = "";

		long durableCapacity = durableData.getCapacity();

		for(int i = 0; i < durableCapacity; i++)
			SlotMap +=SlotMap;

		return SlotMap;
	}

	/**
	 * probeCardCheck
	 * @author wghuang
	 * @since 2018.05.08
	 * @param machineName
	 * @param operationMode
	 * @return
	 * @throws CustomException
	 */
	private void probeCardCheck(String machineName, String operationMode) throws CustomException
	{
		int unitCount = 0 ;
		int pUnitCoutn = 0 ;

		if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_NORMAL))
		{
			if(getUnitListByMachine(machineName).size()<=0)
				throw new CustomException("MACHINE-9001", machineName);

			unitCount = getUnitListByMachine(machineName).size();

			pUnitCoutn = getPUnitListByUnitList(getUnitListByMachine(machineName)).size();

			if(unitCount != pUnitCoutn)
				throw new CustomException("PROBECARD-0001", machineName);
			else
			{
				String probeCardType ="";
				for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
				{
					String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");

					if(StringUtil.isEmpty(probeCardType))
					{
						probeCardType = tempprobeCardType;
					}
					else
					{
						if(!StringUtil.equals(probeCardType,tempprobeCardType))
							throw new CustomException("PROBECARD-0002", machineName, probeCardType, tempprobeCardType);
					}
				}
			}
		}
		else if(StringUtil.equals(operationMode, GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
		{
			if(pUnitCoutn < 0)
				throw new CustomException("PROBECARD-0005", machineName);
			else if(pUnitCoutn > unitCount)
				throw new CustomException("PROBECARD-0004", machineName);

			/*if(unitCount == pUnitCoutn)
			{
				String probeCardType ="";
				for(Durable probeCard : getPUnitListByUnitList(getUnitListByMachine(machineName)))
				{
					String tempprobeCardType = CommonUtil.getValue(probeCard.getUdfs(), "PROBECARDTYPE");

					if(StringUtil.isEmpty(probeCardType))
					{
						probeCardType = tempprobeCardType;
					}
					else
					{
						if(StringUtil.equals(probeCardType,tempprobeCardType))
							throw new CustomException("PROBECARD-0003", machineName);
					}
				}
			}
			else if(unitCount < pUnitCoutn)
				throw new CustomException("PROBECARD-0004", machineName);*/
		}
	}

	private List<Machine> getUnitListByMachine(String machineName) throws CustomException
	{
		String condition = "WHERE SUPERMACHINENAME = ? AND MACHINENAME <> ?";

		Object[] bindSet = new Object[]{machineName,"A2TST011"};

		List<Machine>unitList ;
		try
		{
			unitList = MachineServiceProxy.getMachineService().select(condition, bindSet);
		}
		catch(NotFoundSignal ne)
		{
			unitList = new ArrayList<Machine>();
		}

		return unitList;
	}

	private List<Durable> getPUnitListByUnitList(List<Machine> machineList)
	{
		String unitlistName = "";
		List<Durable> probeCardList;

		for(Machine unit : machineList)
		{
			if(StringUtil.isEmpty(unitlistName))
				unitlistName = "'" + unit.getKey().getMachineName() + "'";

			unitlistName += ",'" + unit.getKey().getMachineName() + "'";
		}

		try
		{
			probeCardList = DurableServiceProxy.getDurableService().select(" WHERE UNITNAME IN (" + unitlistName + ")", null);
		}
		catch(NotFoundSignal ne)
		{
			probeCardList = new ArrayList<Durable>();
		}

		return probeCardList;
	}

	/**
	 * append one by one to LotInfoDownloadRequest
	 * @author swcho
	 * @since 2015.03.09
	 * @param bodyElement
	 * @param lotName
	 * @param carrierName
	 * @param productionType
	 * @param productSpecName
	 * @param processOperationName
	 * @param durableState
	 * @param durableType
	 * @return
	 * @throws CustomException
	 */
	private Element generateBodyTemplate(Element bodyElement,
			String lotName, String carrierName,
			String productionType, String productSpecName, String processOperationName, String lotGrade,
			String durableState, String durableType)
					throws CustomException
	{

		if (bodyElement.getChild("CARRIERNAME") == null)
		{
			Element carrierIDElement = new Element("CARRIERNAME");
			carrierIDElement.setText(carrierName);
			bodyElement.addContent(carrierIDElement);
		}

		if (bodyElement.getChild("LOTNAME") == null)
		{
			Element lotIDElement = new Element("LOTNAME");
			lotIDElement.setText(lotName);
			bodyElement.addContent(lotIDElement);
		}

		Element carrierStateElement = new Element("CARRIERSTATE");
		carrierStateElement.setText(durableState);
		bodyElement.addContent(carrierStateElement);

		Element CarrierTypeElement = new Element("CARRIERTYPE");
		CarrierTypeElement.setText(durableType);
		bodyElement.addContent(CarrierTypeElement);

		Element firstProductionTypeElement = new Element("PRODUCTIONTYPE");
		firstProductionTypeElement.setText(productionType);
		bodyElement.addContent(firstProductionTypeElement);

		Element firstProductSpecNameElement = new Element("PRODUCTSPECNAME");
		firstProductSpecNameElement.setText(productSpecName);
		bodyElement.addContent(firstProductSpecNameElement);

		Element firstWorkorderNameElement = new Element("WORKORDERNAME");
		firstWorkorderNameElement.setText("");
		bodyElement.addContent(firstWorkorderNameElement);

		Element partid = new Element("PARTID");
		partid.setText("");
		bodyElement.addContent(partid);

		Element firstProcessOperationNameElement = new Element("PROCESSOPERATIONNAME");
		firstProcessOperationNameElement.setText(processOperationName);
		bodyElement.addContent(firstProcessOperationNameElement);

		Element lotJudgeElement = new Element("LOTJUDGE");
		lotJudgeElement.setText(lotGrade);
		bodyElement.addContent(lotJudgeElement);

		//need to fill outside
		Element machineRecipeNameElement = new Element("MACHINERECIPENAME");
		machineRecipeNameElement.setText("");
		bodyElement.addContent(machineRecipeNameElement);

		Element slotSelElement = new Element("SLOTSEL");
		slotSelElement.setText("");
		bodyElement.addContent(slotSelElement);

		Element productQuantityElement = new Element("PRODUCTQUANTITY");
		productQuantityElement.setText("");
		bodyElement.addContent(productQuantityElement);

		Element productListElement = new Element("PRODUCTLIST");
		productListElement.setText("");
		bodyElement.addContent(productListElement);

		return bodyElement;
	}

	/**
	 * append one by one to LotInfoDownloadRequest
	 * @author wghuang
	 * @since 2018.03.29
	 * @param bodyElement
	 * @param machineName
	 * @param carrierName
	 * @param durableState
	 * @param durableType
	 * @param portName
	 * @param portType
	 * @param portUseType
	 * @param slotMap
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	private Element generateEDOBodyTemplate(Element bodyElement,
			Machine machineData, String carrierName,String durableState,String durableType,String portName,
			String portType,String portUseType,String slotMap, String processOperationName)
					throws CustomException
	{
		//remove Body Content
		bodyElement.removeContent();

		Element machineNameE = new Element("MACHINENAME");
		machineNameE.setText(machineData.getKey().getMachineName());
		bodyElement.addContent(machineNameE);

		Element carrierNameE = new Element("CARRIERNAME");
		carrierNameE.setText(carrierName);
		bodyElement.addContent(carrierNameE);

		Element carrierStateE = new Element("CARRIERSTATE");
		carrierStateE.setText(durableState);
		bodyElement.addContent(carrierStateE);

		Element carrierTypeE = new Element("CARRIERTYPE");
		carrierTypeE.setText(durableType);
		bodyElement.addContent(carrierTypeE);

		Element portNameE = new Element("PORTNAME");
		portNameE.setText(portName);
		bodyElement.addContent(portNameE);

		Element portTypeE = new Element("PORTTYPE");
		portTypeE.setText(portType);
		bodyElement.addContent(portTypeE);

		Element portUseTypeE = new Element("PORTUSETYPE");
		portUseTypeE.setText(portUseType);
		bodyElement.addContent(portUseTypeE);

		Element slotMapE = new Element("SLOTMAP");
		slotMapE.setText(slotMap);
		bodyElement.addContent(slotMapE);


		//need fill out side
		Element slotSelE = new Element("SLOTSEL");
		slotSelE.setText("");
		bodyElement.addContent(slotSelE);

		Element machineRecipeName = new Element("MACHINERECIPE");
		machineRecipeName.setText("");
		bodyElement.addContent(machineRecipeName);

		Element probeCardNameE = new Element("PROBECARDNAME");
		probeCardNameE.setText("");
		bodyElement.addContent(probeCardNameE);

		Element operationModeNameE = new Element("OPERATIONMODENAME");
		operationModeNameE.setText(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"));
		bodyElement.addContent(operationModeNameE);

		//need fill out side
		Element toMachineNameE = new Element("TOMACHINENAME");
		toMachineNameE.setText("");
		bodyElement.addContent(toMachineNameE);

		Element bendingE = new Element("BENDING");
		bendingE.setText("");
		bodyElement.addContent(bendingE);

		Element priorityE = new Element("PRIORITY");
		priorityE.setText("");
		bodyElement.addContent(priorityE);

		//DCRReadFlag
		Element DCRReadFlagE = new Element("DCRREADFLAG");
		DCRReadFlagE.setText("");
		bodyElement.addContent(DCRReadFlagE);

		Element productListE = new Element("PRODUCTLIST");
		productListE.setText("");
		bodyElement.addContent(productListE);

		return bodyElement;
	}



	/**
	 * write down processing material info
	 * @author swcho
	 * @since 2015.03.09
	 * @param lotList
	 * @param machineData
	 * @param portData
	 * @param durableData
	 * @param machineRecipeName
	 * @return
	 * @throws CustomException
	 */
	private List<Element> generateProductListElement(List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
			String machineRecipeName, String machineConstructType)
					throws CustomException
					{
		//final return
		List<Element> productListElement = new ArrayList<Element>();		

		String chamberGroupName = StringUtil.EMPTY;
		String machineGroupName = StringUtil.EMPTY;

		for (Lot lotData : lotList)
		{
			//avail Product list
			List<Product> productList;
			try
			{
				productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "Product", "No Product to process");
			}

			//base Product info
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

			//base flow info
			ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			//waiting step
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

			//get Sorter job List
			List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
			if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
			{
				sortJobList = this.getSortJobList(durableData, lotData, machineData, portData);
			}

			//make sure TK cancel
			/*
			boolean abortFlag = false;
			try
			{
				ProductServiceProxy.getProductService().select("lotName = ? AND processingInfo = ? AND productState = ?",
						new Object[] {lotData.getKey().getLotName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });

				abortFlag = true;
			}
			catch (NotFoundSignal ne)
			{
				eventLog.info("retry after TK In Canceled");

				abortFlag = false;
			}
			 */

			//need re-engineering
			/*
			//160801 by swcho : sampling enhanced
			List<ListOrderedMap> sampleLotListOld = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByLot(lotData.getKey().getLotName(),
													lotData.getFactoryName(), lotData.getProductSpecName(),
													lotData.getProcessFlowName(), lotData.getProcessOperationName());
			List<SampleProduct> sampleProductListOld = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(
														lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
														lotData.getProcessFlowName(), lotData.getProcessOperationName());

			//161228 by swcho :flow sampling is lower prioirty
			List<FlowSampleLot> sampleLotListNew = new ArrayList<FlowSampleLot>();
			List<FlowSampleProduct> sampleProductListNew = new ArrayList<FlowSampleProduct>();
			if (sampleLotListOld.size() < 1)
			{
				sampleLotListNew = ExtendedObjectProxy.getFlowSampleLotService().getSampling(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
																								lotData.getProcessFlowName(), lotData.getProcessOperationName());
				sampleProductListNew = ExtendedObjectProxy.getFlowSampleProductService().getSamplingProduct(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
																											lotData.getProcessFlowName(), lotData.getProcessOperationName());
			}

			//store sampling parameters
			int targetCnt = 0;
			//String targetPositions = "";
			int selCnt = 0;

			if (sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0 ) //|| partSelLot.size() > 0
			{
				//String sTargetCount = CommonUtil.getValue(sampleLotList.get(0), "ACTUALPRODUCTCOUNT");
				String sTargetCount = "0";

				if (sampleLotListOld.size() > 0)
				{
					sTargetCount = CommonUtil.getValue(sampleLotListOld.get(0), "ACTUALPRODUCTCOUNT");
				}
				else if (sampleLotListNew.size() > 0)
				{
					sTargetCount = sampleLotListNew.get(0).getACTUALPRODUCTCOUNT();
				}
				else if (partSelLot.size() > 0)
				{
					sTargetCount = partSelLot.get(0).getACTUALPRODUCTCOUNT();
				}

				try
				{
					targetCnt = Integer.parseInt(sTargetCount);
				}
				catch (Exception ex)
				{
					eventLog.error("Sampling target count convert failed");
				}
			}
			 */

			/* 20180620, hhlee, Add Set ChamberGroup ==>> */
			machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
			chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(machineData.getKey().getMachineName(), machineGroupName, "3");
			/* <<== 20180620, hhlee, Add Set ChamberGroup */

			for (Product productData : productList)
			{
				String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");

				//DP box data
				ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
				String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
				//String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
				String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
				String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
				String samplingFlag = "";

				//Glass selection
				//161228 by swcho : additional sampling
				 /* 20190312, hhlee, add Check Validation */
                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
				/* 20190523, hhlee, modify, add check validation SamplingFlow */
                //if (operationData.getProcessOperationType().equals("Inspection") && 
                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
                //            && !flowData.getProcessFlowType().equals("MQC"))
                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                        !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
				{
					/* 20180602, Need Sampling Logic ==>> */
					/* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
				    Set SlotSel to " Y " for the entire operation of Glass. */
					samplingFlag = "Y";
					/* <<== 20180602, Need Sampling Logic */

					/*if (sampleProductListOld != null && sampleProductListOld.size() > 0)
					{
						samplingFlag = ExtendedObjectProxy.getSampleLotService().getSamplingFlag(productData, sampleProductListOld);

						machineRecipeName = "";

						if(StringUtil.equals(samplingFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
						{
					 *//** 20180208 NJPARK
							for (SampleProduct sampleProduct : sampleProductListOld)
							{
								if(!StringUtil.isEmpty(sampleProduct.getMACHINERECIPENAME()) &&
										StringUtil.equals(productData.getKey().getProductName(), sampleProduct.getPRODUCTNAME()))
								{
									machineRecipeName = sampleProduct.getMACHINERECIPENAME();

									break;
								}
							}
					  *//*
							selCnt++;
						}
					}
					else if (sampleProductListNew.size() > 0)
					{
						samplingFlag = ExtendedObjectProxy.getFlowSampleProductService().getSamplingFlag(productData, sampleProductListNew);

						if (samplingFlag.equals(GenericServiceProxy.getConstantMap().FLAG_Y))
							selCnt++;
					}*/
				}
				else
				{
					//sort case
					if (StringUtil.equals(operationData.getDetailProcessOperationType(), "SORT"))
					{
						samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
					}
					//repair case
					/* 20190523, hhlee, modify, add check validation SamplingFlow */
                    //else if (StringUtil.equals(operationData.getDetailProcessOperationType(), "REP"))
                    else if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) && 
                            StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
					{
						if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					//rework case
					else if (StringUtil.equals(flowData.getProcessFlowType(), "Rework"))
					{
						if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					//MQC SlotMap
					else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
					{
						String flag = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
						if(!StringUtil.isEmpty(flag))
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						}
						else
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}
					}
					else
					{
						if(CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals(GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;

						//samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
					}
				}

				//machineData
				Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
						productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);

				productListElement.add(productElement);
			}

			//160801 by swcho : sampling filling
			//161228 by swcho : additional sampling
			/*if ((sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0)
					&&  targetCnt > 0 && targetCnt > selCnt)
			{
				eventLog.info("Glass selection filling");

				for (Element productElement : productListElement)
				{
					if (SMessageUtil.getChildText(productElement, "SAMPLINGFLAG", false).equals("Y"))
					{
						continue;
					}
					else
					{
						try
						{
							productElement.getChild("SAMPLINGFLAG").setText("Y");
							selCnt++;
						}
						catch (Exception ex)
						{
							eventLog.error(ex.getMessage());
						}
					}

					if (selCnt >= targetCnt) break;
				}
			}*/
		}

		return productListElement;
					}

	/**
	 * write down processing material info
	 * @author swcho
	 * @since 2015.03.09
	 * @param lotList
	 * @param machineData
	 * @param portData
	 * @param durableData
	 * @param machineRecipeName
	 * @return
	 * @throws CustomException
	 */
	private List<Element> generateProductListElement(List<Lot> lotList, Machine machineData, Port portData, Durable durableData,
			String machineRecipeName, String machineConstructType, String logicalSlotMap)
					throws CustomException
					{
		//final return
		List<Element> productListElement = new ArrayList<Element>();

		String chamberGroupName = StringUtil.EMPTY;
		String machineGroupName = StringUtil.EMPTY;

		for (Lot lotData : lotList)
		{
			//avail Product list
			List<Product> productList;
			try
			{
				productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "Product", "No Product to process");
			}

			//base Product info
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

			//base flow info
			ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			//waiting step
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

			//get Sorter job List
			List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();
			if (operationData.getDetailProcessOperationType().equals(GenericServiceProxy.getConstantMap().ConstructType_Sort))
			{
				sortJobList = this.getSortJobList(durableData, lotData, machineData, portData);
			}

			//make sure TK cancel
			/*
            boolean abortFlag = false;
            try
            {
                ProductServiceProxy.getProductService().select("lotName = ? AND processingInfo = ? AND productState = ?",
                        new Object[] {lotData.getKey().getLotName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });

                abortFlag = true;
            }
            catch (NotFoundSignal ne)
            {
                eventLog.info("retry after TK In Canceled");

                abortFlag = false;
            }
			 */

			//need re-engineering
			/*
            //160801 by swcho : sampling enhanced
            List<ListOrderedMap> sampleLotListOld = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByLot(lotData.getKey().getLotName(),
                                                    lotData.getFactoryName(), lotData.getProductSpecName(),
                                                    lotData.getProcessFlowName(), lotData.getProcessOperationName());
            List<SampleProduct> sampleProductListOld = ExtendedObjectProxy.getSampleLotService().getActualSamplePosition(
                                                        lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
                                                        lotData.getProcessFlowName(), lotData.getProcessOperationName());

            //161228 by swcho :flow sampling is lower prioirty
            List<FlowSampleLot> sampleLotListNew = new ArrayList<FlowSampleLot>();
            List<FlowSampleProduct> sampleProductListNew = new ArrayList<FlowSampleProduct>();
            if (sampleLotListOld.size() < 1)
            {
                sampleLotListNew = ExtendedObjectProxy.getFlowSampleLotService().getSampling(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
                                                                                                lotData.getProcessFlowName(), lotData.getProcessOperationName());
                sampleProductListNew = ExtendedObjectProxy.getFlowSampleProductService().getSamplingProduct(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
                                                                                                            lotData.getProcessFlowName(), lotData.getProcessOperationName());
            }

            //store sampling parameters
            int targetCnt = 0;
            //String targetPositions = "";
            int selCnt = 0;

            if (sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0 ) //|| partSelLot.size() > 0
            {
                //String sTargetCount = CommonUtil.getValue(sampleLotList.get(0), "ACTUALPRODUCTCOUNT");
                String sTargetCount = "0";

                if (sampleLotListOld.size() > 0)
                {
                    sTargetCount = CommonUtil.getValue(sampleLotListOld.get(0), "ACTUALPRODUCTCOUNT");
                }
                else if (sampleLotListNew.size() > 0)
                {
                    sTargetCount = sampleLotListNew.get(0).getACTUALPRODUCTCOUNT();
                }
                else if (partSelLot.size() > 0)
                {
                    sTargetCount = partSelLot.get(0).getACTUALPRODUCTCOUNT();
                }

                try
                {
                    targetCnt = Integer.parseInt(sTargetCount);
                }
                catch (Exception ex)
                {
                    eventLog.error("Sampling target count convert failed");
                }
            }
			 */

			/* 20180620, hhlee, Add Set ChamberGroup ==>> */
			machineGroupName = CommonUtil.getValue(operationData.getUdfs(), "MACHINEGROUPNAME");
			chamberGroupName = ExtendedObjectProxy.getChamberGroupInfoService().getValidateChamberGroupName(machineData.getKey().getMachineName(), machineGroupName, "3");
			/* <<== 20180620, hhlee, Add Set ChamberGroup */

			for (Product productData : productList)
			{
				String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");

				//DP box data
				ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
				String productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
				//String productThickness = getExactProductThickness(productData, consumableSpecData, machineData.getKey().getMachineName());
				String productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
				String bending = CommonUtil.getValue(consumableSpecData.getUdfs(), "BENDING");
				String samplingFlag = "";

				//Glass selection
				//161228 by swcho : additional sampling
				 /* 20190312, hhlee, add Check Validation */
                //if (operationData.getProcessOperationType().equals("Inspection") && !flowData.getProcessFlowType().equals("MQC"))
				/* 20190523, hhlee, modify, add check validation SamplingFlow */
                //if (operationData.getProcessOperationType().equals("Inspection") && 
                //            !StringUtil.equals(operationData.getDetailProcessOperationType(), "REP")
                //            && !flowData.getProcessFlowType().equals("MQC"))
                if (operationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Pos_Inspection) && 
                        flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) &&
                        !StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
				{
					/* 20180602, Need Sampling Logic ==>> */
					/* If 'ProcessUperationType' is 'Inspection' then additional Sampling Logic is required.
                    Set SlotSel to " Y " for the entire operation of Glass. */
					samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
					/* <<== 20180602, Need Sampling Logic */

					/*if (sampleProductListOld != null && sampleProductListOld.size() > 0)
                    {
                        samplingFlag = ExtendedObjectProxy.getSampleLotService().getSamplingFlag(productData, sampleProductListOld);

                        machineRecipeName = "";

                        if(StringUtil.equals(samplingFlag, GenericServiceProxy.getConstantMap().FLAG_Y))
                        {
					 *//** 20180208 NJPARK
                            for (SampleProduct sampleProduct : sampleProductListOld)
                            {
                                if(!StringUtil.isEmpty(sampleProduct.getMACHINERECIPENAME()) &&
                                        StringUtil.equals(productData.getKey().getProductName(), sampleProduct.getPRODUCTNAME()))
                                {
                                    machineRecipeName = sampleProduct.getMACHINERECIPENAME();

                                    break;
                                }
                            }
					  *//*
                            selCnt++;
                        }
                    }
                    else if (sampleProductListNew.size() > 0)
                    {
                        samplingFlag = ExtendedObjectProxy.getFlowSampleProductService().getSamplingFlag(productData, sampleProductListNew);

                        if (samplingFlag.equals(GenericServiceProxy.getConstantMap().FLAG_Y))
                            selCnt++;
                    }*/
				}
				else
				{
					//sort case
					if (StringUtil.equals(operationData.getDetailProcessOperationType(), "SORT"))
					{
						samplingFlag = ExtendedObjectProxy.getSortJobService().getSortJobFlag(productData, sortJobList);
					}
					//repair case
					/* 20190523, hhlee, modify, add check validation SamplingFlow */
                    //else if (StringUtil.equals(operationData.getDetailProcessOperationType(), "REP"))
                    else if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_SAMPLING) && 
                            StringUtil.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP))
					{
						if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					//rework case
					else if (StringUtil.equals(flowData.getProcessFlowType(), "Rework"))
					{
						if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					//MQC SlotMap
					else if (StringUtil.equals(flowData.getProcessFlowType(), "MQC"))
					{
						String flag = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
						if(!StringUtil.isEmpty(flag))
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						}
						else
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}
					}
					else
					{
						if(CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals(GenericServiceProxy.getConstantMap().PRODUCT_PROCESSINGINFO_P))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						else
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;

						//samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(false, productData);
					}
				}

				//machineData
				Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness,
						productSize, machineRecipeName, samplingFlag, bending, machineConstructType,productSpecData, chamberGroupName);

				productListElement.add(productElement);
			}

			//160801 by swcho : sampling filling
			//161228 by swcho : additional sampling
			/*if ((sampleLotListOld.size() > 0 || sampleLotListNew.size() > 0)
                    &&  targetCnt > 0 && targetCnt > selCnt)
            {
                eventLog.info("Glass selection filling");

                for (Element productElement : productListElement)
                {
                    if (SMessageUtil.getChildText(productElement, "SAMPLINGFLAG", false).equals("Y"))
                    {
                        continue;
                    }
                    else
                    {
                        try
                        {
                            productElement.getChild("SAMPLINGFLAG").setText("Y");
                            selCnt++;
                        }
                        catch (Exception ex)
                        {
                            eventLog.error(ex.getMessage());
                        }
                    }

                    if (selCnt >= targetCnt) break;
                }
            }*/
		}

		return productListElement;
					}

	/**
	 * @author wghuang
	 * @param durableData
	 * @param lotData
	 * @param machineData
	 * @param portData
	 * @return SortJobList
	 * @throws CustomException
	 */
	private List<ListOrderedMap>getSortJobList(Durable durableData, Lot lotData, Machine machineData, Port portData) throws CustomException
	{
		List<ListOrderedMap> sortJobList = new ArrayList<ListOrderedMap>();

		try
		{
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("SELECT J.jobName, J.jobState, C.machineName, C.portName, C.carrierName, P.fromLotName, P.productName, P.fromPosition").append("\n")
			.append("	    FROM CT_SortJob J, CT_SortJobCarrier C, CT_SortJobProduct P").append("\n")
			.append("	WHERE J.jobName = C.jobName").append("\n")
			.append("    AND C.jobName = P.jobName").append("\n")
			.append("    AND C.machineName = P.machineName").append("\n")
			.append("    AND C.carrierName = P.fromCarrierName").append("\n")
			.append("    AND C.carrierName = ?").append("\n")
			.append("    AND C.lotName = ?").append("\n")
			.append("    AND C.machineName = ?").append("\n")
			.append("    AND C.portName = ?").append("\n")
			.append("    AND J.jobState IN (?,?)");

			Object[] bindList = new Object[] { durableData.getKey().getDurableName(), lotData.getKey().getLotName(),
					machineData.getKey().getMachineName(), portData.getKey().getPortName(),
					GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED};

			sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
		}
		catch (Exception ex)
		{
			eventLog.debug("No sorter job");
			throw new CustomException("SYS-9999", "SortJob", "No job for Product");
		}
		return sortJobList;
	}


	/**
	 * @author wuzhiming
	 * @param productData
	 * @param consumableSpecData
	 * @return 除了O1PPK设备外，其他设备返回实际厚度
	 * @throws CustomException
	 */
	private String getExactProductThickness(Product productData, ConsumableSpec consumableSpecData, String machineName) throws CustomException
	{
		MachineSpecKey machinespecKey = new MachineSpecKey();
		machinespecKey.setMachineName(machineName);
		MachineSpec machineSpec = MachineServiceProxy.getMachineSpecService().selectByKey(machinespecKey);

		String construcType = CommonUtil.getValue(machineSpec.getUdfs(), "CONSTRUCTTYPE");

		String productThickness = "";
		if(StringUtil.equalsIgnoreCase(construcType, "BPK"))
		{
			productThickness = getProductThickness(productData, consumableSpecData);
		}
		else
		{
			productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
		}
		return productThickness;
	}

	/**
	 * @author wuzhiming
	 * @param productData
	 * @param consumableSpecData
	 * @return 玻璃的厚度
	 * @throws CustomException
	 */
	private String getProductThickness(Product productData, ConsumableSpec consumableSpecData) throws CustomException
	{
		String productThickness = "";
		String thicknessAfterAssemble = getThicknessAfterAssemble(productData);
		if (!StringUtil.isEmpty(thicknessAfterAssemble))
		{
			productThickness = thicknessAfterAssemble;
		}else {
			//2017.04.10 wuzhiming add: 得到pairProduct的glassThickNess
			double pairProductThickness = this.getPairProductThickness(productData) ;
			productThickness = String.valueOf(this.addTwoDouble(Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS")), pairProductThickness));

		}
		return productThickness;
	}

	/**
	 * @author wuzhiming
	 * @param productData
	 * @return modeler中设定的和productspec绑定的贴合后玻璃的厚度
	 */
	private String getThicknessAfterAssemble(Product productData)
	{
		ProductSpecKey productspecKey = new ProductSpecKey();
		productspecKey.setProductSpecName(productData.getProductSpecName());
		productspecKey.setFactoryName(productData.getFactoryName());
		productspecKey.setProductSpecVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		ProductSpec ProductSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productspecKey);

		String thicknessAfterAssembleString = CommonUtil.getValue(ProductSpecData.getUdfs(), "THICKNESSAFTERASSEMBLE");
		return thicknessAfterAssembleString;
	}

	/**
	 * @Author wuzhiming
	 * @since 2017.03.10 16:46
	 * @param productData 主玻璃Data
	 * @return pairProductThickness ：贴合玻璃的厚度，若没有贴合玻璃，返回0
	 * @throws  CustomException ：consumableSpec 没找到时将抛出异常
	 */
	private double getPairProductThickness(Product productData) throws CustomException
	{
		String pairProductName = CommonUtil.getValue(productData.getUdfs(),"PAIRPRODUCTNAME");

		if (StringUtil.isEmpty(pairProductName))
		{
			return 0;
		}else {
			Product pairProductData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(pairProductName));
			String crateName = CommonUtil.getValue(pairProductData.getUdfs(), "CRATENAME");
			ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
			double pairProductThickness = Double.valueOf(CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS"));

			return pairProductThickness;
		}
	}

	/**
	 * @author wuzhiming
	 * @since 2017.03.10 16:55
	 * @param v1
	 * @param v2
	 * @return 返回精确的两个double值相加
	 */
	public double addTwoDouble(double v1, double v2)
	{
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * write down virtual material info
	 * @author swcho
	 * @since 2015.04.11
	 * @param requestData
	 * @param productList
	 * @param machineData
	 * @param portData
	 * @param durableData
	 * @param machineRecipeName
	 * @return
	 * @throws CustomException
	 */
	private List<Element> generateProductListElement(ProductRequest requestData, List<VirtualGlass> productList,
			Machine machineData, Port portData, Durable durableData,
			String productionType, String productType, String productSpecName, 
			String processFlowName, String processOperationName,
			String machineRecipeName)
					throws CustomException
					{
		String factoryName = requestData.getFactoryName();

		String productVendor = "";
		String productThickness = "";
		String productSize = "";
		String degree = "";

		if (productList.size() > 0)
		{
			String crateName = productList.get(0).getCrateName();

			//DP box data
			ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
			productVendor = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSVENDOR");
			productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
			productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
		}

		List<Element> productListElement = new ArrayList<Element>();
		for (VirtualGlass productData : productList)
		{
			Element productElementOld = this.generateProductElement(productData, productionType, productType,
					productSpecName, requestData.getKey().getProductRequestName(), processFlowName, processOperationName,
					productThickness, productSize, productVendor, machineRecipeName, "Y", degree);


			//Element productElement = this.generateEDOProductElement(productData, productData.getProductionType(), productThickness, productSize, machineRecipeName,"");

			productListElement.add(productElementOld);
		}

		return productListElement;
					}

	/**
	 * write down Product item
	 * @author swcho
	 * @since 2014.03.09
	 * @param productData
	 * @param productionType
	 * @param productThickness
	 * @param productSize
	 * @param productVendor
	 * @param machineRecipeName
	 * @param samplingFlag
	 * @return
	 * @throws CustomException
	 */
	private Element generateProductElement(Product productData, String productionType, String productThickness, 
			String productSize, String productVendor, String machineRecipeName, 
			String samplingFlag, String degree, String TmsFlag, String CsgFlag, 
			String furnaceFlag, String dummyType)
					throws CustomException
	{
		Element productElement = new Element("PRODUCT");

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(productData.getLotName());
		productElement.addContent(lotNameElement);

		Element productNameElement = new Element("PRODUCTNAME");
		productNameElement.setText(productData.getKey().getProductName());
		productElement.addContent(productNameElement);

		Element productTypeElement = new Element("PRODUCTTYPE");
		productTypeElement.setText(productData.getProductType());
		productElement.addContent(productTypeElement);

		Element processflowElement = new Element("PROCESSFLOWNAME");
		processflowElement.setText(productData.getProcessFlowName());
		productElement.addContent(processflowElement);

		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText(productData.getProcessOperationName());
		productElement.addContent(processOperationNameElement);

		Element positionElement = new Element("POSITION");
		positionElement.setText(String.valueOf(productData.getPosition()));
		productElement.addContent(positionElement);

		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(productData.getProductSpecName());
		productElement.addContent(productSpecNameElement);

		Element productionTypeElement = new Element("PRODUCTIONTYPE");
		//productionTypeElement.setText(productData.getProductionType());
		productionTypeElement.setText(productionType);
		productElement.addContent(productionTypeElement);

		Element productJudgeElement = new Element("PRODUCTJUDGE");
		productJudgeElement.setText(productData.getProductGrade());
		productElement.addContent(productJudgeElement);

		Element productGradeElement = new Element("PRODUCTGRADE");
		productGradeElement.setText(productData.getProductGrade());
		productElement.addContent(productGradeElement);

		Element dummyTypeElement = new Element("DUMMYTYPE");
		dummyTypeElement.setText(dummyType);
		productElement.addContent(dummyTypeElement);

		Element tmsFlagElement = new Element("TMSFLAG");
		tmsFlagElement.setText(TmsFlag);
		productElement.addContent(tmsFlagElement);

		Element csgFlagElement = new Element("CSGFLAG");
		csgFlagElement.setText(CsgFlag);
		productElement.addContent(csgFlagElement);

		Element turnFlagElement = new Element("TURNFLAG");
		turnFlagElement.setText(degree);
		productElement.addContent(turnFlagElement);

		Element furnaceFlagElement = new Element("FURNACEFLAG");
		furnaceFlagElement.setText(furnaceFlag);
		productElement.addContent(furnaceFlagElement);

		Element subProductJudgesElement = new Element("SUBPRODUCTJUDGES");
		subProductJudgesElement.setText("");
		productElement.addContent(subProductJudgesElement);

		Element subProductGradesElement = new Element("SUBPRODUCTGRADES");
		subProductGradesElement.setText("");
		productElement.addContent(subProductGradesElement);

		Element productThicknessElement = new Element("PRODUCTTHICKNESS");
		productThicknessElement.setText(productThickness);
		productElement.addContent(productThicknessElement);

		Element workOrderElement = new Element("WORKORDER");
		workOrderElement.setText(productData.getProductRequestName());
		productElement.addContent(workOrderElement);

		String ProductRequestName=productData.getProductRequestName();
		if(ProductRequestName.length()<=12)
		{
			Element partid = new Element("PARTID");
			partid.setText(ProductRequestName);
			productElement.addContent(partid);
		}
		else
		{
			ProductRequestName=ProductRequestName.substring(0,12);
			Element partid = new Element("PARTID");
			partid.setText(ProductRequestName);
			productElement.addContent(partid);
		}

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		productElement.addContent(crateNameElement);

		Element productSizeElement = new Element("PRODUCTSIZE");
		productSizeElement.setText(productSize);
		productElement.addContent(productSizeElement);

		Element glassMakerElement = new Element("GLASSMAKER");
		glassMakerElement.setText(productVendor);
		productElement.addContent(glassMakerElement);

		Element productRecipeElement = new Element("PRODUCTRECIPE");

		productRecipeElement.setText(machineRecipeName);
		productElement.addContent(productRecipeElement);

		Element exeposureUnitId = new Element("EXPOSUREUNITID");
		exeposureUnitId.setText("");
		productElement.addContent(exeposureUnitId);

		Element exeposureRecipeName = new Element("EXPOSURERECIPENAME");
		exeposureRecipeName.setText("");
		productElement.addContent(exeposureRecipeName);

		Element reworkCountElement = new Element("REWORKCOUNT");
		reworkCountElement.setText(String.valueOf(productData.getReworkCount()));
		productElement.addContent(reworkCountElement);

		Element dummyCountElement = new Element("DUMMYUSEDCOUNT");
		dummyCountElement.setText(CommonUtil.getValue(productData.getUdfs(), "DUMMYUSEDCOUNT"));
		productElement.addContent(dummyCountElement);

		Element maskNameElement = new Element("MASKNAME");
		maskNameElement.setText("");
		productElement.addContent(maskNameElement);

		Element proberNameElement = new Element("PROBERNAME");
		proberNameElement.setText("");
		productElement.addContent(proberNameElement);

		Element processingInfoElement = new Element("PROCESSINGINFO");
		processingInfoElement.setText("");
		productElement.addContent(processingInfoElement);

		Element samplingFlagElement = new Element("SAMPLINGFLAG");
		samplingFlagElement.setText(samplingFlag);
		productElement.addContent(samplingFlagElement);

		Element arrayCutRepairType = new Element("ARRAYCUTREPAIRTYPE");
		arrayCutRepairType.setText("");
		productElement.addContent(arrayCutRepairType);

		Element lcvdRepairType = new Element("LCVDREPAIRTYPE");
		lcvdRepairType.setText("");
		productElement.addContent(lcvdRepairType);

		Element lastProcessEndTime = new Element("LASTPROCESSENDTIME");
		lastProcessEndTime.setText(CommonUtil.getValue(productData.getUdfs(), "LASTPROCESSENDTIME"));
		productElement.addContent(lastProcessEndTime);

		Element ELARecipeName = new Element("ELARECIPENAME");
		ELARecipeName.setText(CommonUtil.getValue(productData.getUdfs(), "ELARECIPENAME"));
		productElement.addContent(ELARecipeName);

		Element ELAEnergyUsage = new Element("ELAENERGYUSAGE");
		ELAEnergyUsage.setText(CommonUtil.getValue(productData.getUdfs(), "ELAENERGYUSAGE"));
		productElement.addContent(ELAEnergyUsage);

		return productElement;
	}

	/**
	 * write down Product item
	 * @author wghuang
	 * @since 2014.03.31
	 * @param productData
	 * @param productionType
	 * @param productThickness
	 * @param productSize
	 * @param machineRecipeName
	 * @return
	 * @throws CustomException
	 */
	private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
			String productSize, String machineRecipeName, String samplingFlag,
			String Bending, String machineConstructType, ProductSpec productSpecData)
					throws CustomException
	{
		Element productElement = new Element("PRODUCT");

		//productName
		Element productNameE = new Element("PRODUCTNAME");
		productNameE.setText(productData.getKey().getProductName());
		productElement.addContent(productNameE);

		//position
		Element positionE = new Element("POSITION");
		positionE.setText(String.valueOf(productData.getPosition()));
		productElement.addContent(positionE);

		//lotName
		Element lotNameE = new Element("LOTNAME");
		lotNameE.setText(productData.getLotName());
		productElement.addContent(lotNameE);

		//productSpecName
		Element productSpecNameE = new Element("PRODUCTSPECNAME");
		productSpecNameE.setText(productData.getProductSpecName());
		productElement.addContent(productSpecNameE);

		//productionType
		Element productionTypeE = new Element("PRODUCTIONTYPE");
		productionTypeE.setText(productionType);
		productElement.addContent(productionTypeE);

		//productType
		Element productTypeE = new Element("PRODUCTTYPE");
		productTypeE.setText(productData.getProductType());
		productElement.addContent(productTypeE);

		//processflow
		Element processflowE = new Element("PROCESSFLOWNAME");
		processflowE.setText(productData.getProcessFlowName());
		productElement.addContent(processflowE);

		//processOperationName
		Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
		processOperationNameE.setText(productData.getProcessOperationName());
		productElement.addContent(processOperationNameE);

		//productSize
		Element productSizeE = new Element("PRODUCTSIZE");
		productSizeE.setText(productSize);
		productElement.addContent(productSizeE);

		//workOrder
		Element workOrderE = new Element("WORKORDER");
		workOrderE.setText(productData.getProductRequestName());
		productElement.addContent(workOrderE);

		//reworkCount
		Element reworkCountE = new Element("REWORKCOUNT");
		reworkCountE.setText(String.valueOf(productData.getReworkCount()));
		productElement.addContent(reworkCountE);

		//productJudge
		Element productJudgeE = new Element("PRODUCTJUDGE");
		productJudgeE.setText(productData.getProductGrade());
		productElement.addContent(productJudgeE);

		//PanelProdcutJudges
		Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
		panelProductJudgesE.setText("");
		productElement.addContent(panelProductJudgesE);

		//productRecipe
		Element productRecipeE = new Element("PRODUCTRECIPE");
		productRecipeE.setText(machineRecipeName);
		productElement.addContent(productRecipeE);

		//crateName
		Element crateNameE = new Element("CRATENAME");
		crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		productElement.addContent(crateNameE);

		//productThickness
		Element productThicknessE = new Element("PRODUCTTHICKNESS");
		productThicknessE.setText(productThickness);
		productElement.addContent(productThicknessE);

		/* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
		String exposureUnitId = StringUtil.EMPTY;
		String exposureRecipeName = StringUtil.EMPTY;
		String maskName = StringUtil.EMPTY;
		if (machineConstructType.equals("CDME"))
		{
			ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
					productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
			if(pExposureFeedBackData != null)
			{
				exposureUnitId = pExposureFeedBackData.getUnitName();
				exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
				maskName = pExposureFeedBackData.getMaskName();
			}
		}
		//exeposureUnitId
		Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
		exeposureUnitIdE.setText(exposureUnitId);
		productElement.addContent(exeposureUnitIdE);

		//exeposureRecipeName
		Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
		exeposureRecipeNameE.setText(exposureRecipeName);
		productElement.addContent(exeposureRecipeNameE);

		//MaskName
		Element maskNameE = new Element("MASKNAME");
		maskNameE.setText(maskName);
		productElement.addContent(maskNameE);
		/* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */

		//ChamberID
		Element chamberIDE = new Element("CHAMBERID");
		chamberIDE.setText("");
		productElement.addContent(chamberIDE);

		//slotPriority
		Element slotPriorityE = new Element("SLOTPRIORITY");
		slotPriorityE.setText("");
		productElement.addContent(slotPriorityE);

		//slotBending
		Element slotBendingE = new Element("SLOTBENDING");
		slotBendingE.setText("");
		productElement.addContent(slotBendingE);

		/* 20180531, Get Product Process Data(Last 5 processes) ==>> */
		//ProcessList
		Element processListElement = new Element("PROCESSLIST");
		//Element processElement = new Element("PROCESS");
		//
		//Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
		//processOperationNameLElement.setText("");
		//processElement.addContent(processOperationNameLElement);
		//
		//Element processMachineNameElement = new Element("PROCESSMACHINENAME");
		//processMachineNameElement.setText("");
		//processElement.addContent(processMachineNameElement);
		//
		//processListElement.addContent(processElement);
		if (machineConstructType.equals("AOI"))
		{
			processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
		}

		/* <<== 20180531, Get Product Process Data(Last 5 processes) */
		productElement.addContent(processListElement);


		/* Array Product Flag Setting ==>> */
		productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
		/* <== Array Product Flag Setting */

		////TurnOverFlag
		//Element turnOverFlagElement = new Element("TURNOVERFLAG");
		//turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
		//productElement.addContent(turnOverFlagElement);

		////TurnOverFlag
		//Element turnSideFlagElement = new Element("TURNSIDEFLAG");
		//turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
		//productElement.addContent(turnSideFlagElement);

		//SamplingFlag
		Element samplingFlagElement = new Element("SAMPLINGFLAG");
		samplingFlagElement.setText(samplingFlag);
		productElement.addContent(samplingFlagElement);

		return productElement;
	}

	/**
	 * @Name     generateEDOProductElement
	 * @since    2018. 6. 21.
	 * @author   hhlee
	 * @contents 
	 * @param productData
	 * @param productionType
	 * @param productThickness
	 * @param productSize
	 * @param machineRecipeName
	 * @param samplingFlag
	 * @param Bending
	 * @param machineConstructType
	 * @param productSpecData
	 * @param chamberGroupName
	 * @return
	 * @throws CustomException
	 */
	private Element generateEDOProductElement(Product productData, String productionType, String productThickness, 
			String productSize, String machineRecipeName, String samplingFlag,
			String Bending, String machineConstructType, ProductSpec productSpecData,
			String chamberGroupName)
					throws CustomException
	{
		Element productElement = new Element("PRODUCT");

		//productName
		Element productNameE = new Element("PRODUCTNAME");
		productNameE.setText(productData.getKey().getProductName());
		productElement.addContent(productNameE);

		//position
		Element positionE = new Element("POSITION");
		positionE.setText(String.valueOf(productData.getPosition()));
		productElement.addContent(positionE);

		//lotName
		Element lotNameE = new Element("LOTNAME");
		lotNameE.setText(productData.getLotName());
		productElement.addContent(lotNameE);

		//productSpecName
		Element productSpecNameE = new Element("PRODUCTSPECNAME");
		productSpecNameE.setText(productData.getProductSpecName());
		productElement.addContent(productSpecNameE);

		//productionType
		Element productionTypeE = new Element("PRODUCTIONTYPE");
		productionTypeE.setText(productionType);
		productElement.addContent(productionTypeE);

		//productType
		Element productTypeE = new Element("PRODUCTTYPE");
		productTypeE.setText(productData.getProductType());
		productElement.addContent(productTypeE);

		//processflow
		Element processflowE = new Element("PROCESSFLOWNAME");
		processflowE.setText(productData.getProcessFlowName());
		productElement.addContent(processflowE);

		//processOperationName
		Element processOperationNameE = new Element("PROCESSOPERATIONNAME");
		processOperationNameE.setText(productData.getProcessOperationName());
		productElement.addContent(processOperationNameE);

		//productSize
		Element productSizeE = new Element("PRODUCTSIZE");
		productSizeE.setText(productSize);
		productElement.addContent(productSizeE);

		//workOrder
		Element workOrderE = new Element("WORKORDER");
		workOrderE.setText(productData.getProductRequestName());
		productElement.addContent(workOrderE);

		//reworkCount
		Element reworkCountE = new Element("REWORKCOUNT");
		reworkCountE.setText(String.valueOf(productData.getReworkCount()));
		productElement.addContent(reworkCountE);

		//productJudge
		Element productJudgeE = new Element("PRODUCTJUDGE");
		productJudgeE.setText(productData.getProductGrade());
		productElement.addContent(productJudgeE);

		//PanelProdcutJudges
		Element panelProductJudgesE = new Element("PANELPRODUCTJUDGES");
		panelProductJudgesE.setText("");
		productElement.addContent(panelProductJudgesE);

		//productRecipe
		Element productRecipeE = new Element("PRODUCTRECIPE");
		String recipeName = MESProductServiceProxy.getProductServiceImpl().slotMQCProduct(productData);
		if(!StringUtil.isEmpty(recipeName))
		{
			productRecipeE.setText(recipeName);
		}
		else
		{
			productRecipeE.setText(machineRecipeName);
		}

		productElement.addContent(productRecipeE);

		//crateName
		Element crateNameE = new Element("CRATENAME");
		crateNameE.setText(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		productElement.addContent(crateNameE);

		//productThickness
		Element productThicknessE = new Element("PRODUCTTHICKNESS");
		productThicknessE.setText(productThickness);
		productElement.addContent(productThicknessE);

		/* 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. ==>> */
		String exposureUnitId = StringUtil.EMPTY;
		String exposureRecipeName = StringUtil.EMPTY;
		String maskName = StringUtil.EMPTY;
		if (machineConstructType.equals("CDME"))
		{
			ExposureFeedBack pExposureFeedBackData = ExtendedObjectProxy.getExposureFeedBackService().getLastExposureFeedBackDataOfPhoto(
					productData.getKey().getProductName(), productData.getLotName(), productData.getProductSpecName());
			if(pExposureFeedBackData != null)
			{
				exposureUnitId = pExposureFeedBackData.getUnitName();
				exposureRecipeName = pExposureFeedBackData.getExposureRecipeName();
				maskName = pExposureFeedBackData.getMaskName();
			}
		}
		//exeposureUnitId
		Element exeposureUnitIdE = new Element("EXPOSUREUNITID");
		exeposureUnitIdE.setText(exposureUnitId);
		productElement.addContent(exeposureUnitIdE);

		//exeposureRecipeName
		Element exeposureRecipeNameE = new Element("EXPOSURERECIPENAME");
		exeposureRecipeNameE.setText(exposureRecipeName);
		productElement.addContent(exeposureRecipeNameE);

		//MaskName
		Element maskNameE = new Element("MASKNAME");
		maskNameE.setText(maskName);
		productElement.addContent(maskNameE);
		/* <<== 20180528, Add, Used in ARRAY CDME. The CDME process provides photo Exposure data. */

		//ChamberID
		Element chamberIDE = new Element("CHAMBERID");
		chamberIDE.setText(chamberGroupName);
		productElement.addContent(chamberIDE);

		//slotPriority
		Element slotPriorityE = new Element("SLOTPRIORITY");
		slotPriorityE.setText("");
		productElement.addContent(slotPriorityE);

		//slotBending
		Element slotBendingE = new Element("SLOTBENDING");
		slotBendingE.setText("");
		productElement.addContent(slotBendingE);

		/* 20180531, Get Product Process Data(Last 5 processes) ==>> */
		//ProcessList
		Element processListElement = new Element("PROCESSLIST");
		//Element processElement = new Element("PROCESS");
		//
		//Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
		//processOperationNameLElement.setText("");
		//processElement.addContent(processOperationNameLElement);
		//
		//Element processMachineNameElement = new Element("PROCESSMACHINENAME");
		//processMachineNameElement.setText("");
		//processElement.addContent(processMachineNameElement);
		//
		//processListElement.addContent(processElement);
		if (machineConstructType.equals("AOI"))
		{
			processListElement = this.setLotInfoDownLoadSendProcessList(productData.getFactoryName(), productData.getKey().getProductName());
		}

		/* <<== 20180531, Get Product Process Data(Last 5 processes) */
		productElement.addContent(processListElement);


		/* Array Product Flag Setting ==>> */
		productElement = ExtendedObjectProxy.getProductFlagService().setLotInfoDownLoadSendProductFlag(productElement,  productData.getFactoryName(), productData.getKey().getProductName());
		/* <== Array Product Flag Setting */

		////TurnOverFlag
		//Element turnOverFlagElement = new Element("TURNOVERFLAG");
		//turnOverFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNOVERFLAG"));
		//productElement.addContent(turnOverFlagElement);

		////TurnOverFlag
		//Element turnSideFlagElement = new Element("TURNSIDEFLAG");
		//turnSideFlagElement.setText(CommonUtil.getValue(productSpecData.getUdfs(), "TURNSIDEFLAG"));
		//productElement.addContent(turnSideFlagElement);

		//SamplingFlag
		Element samplingFlagElement = new Element("SAMPLINGFLAG");
		samplingFlagElement.setText(samplingFlag);
		productElement.addContent(samplingFlagElement);

		return productElement;
	}

	/**
	 * write down virtual Product item
	 * @since 2016.04.11
	 * @author swcho
	 * @param productData
	 * @param productionType
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param productThickness
	 * @param productSize
	 * @param productVendor
	 * @param machineRecipeName
	 * @param samplingFlag
	 * @return
	 * @throws CustomException
	 */
	private Element generateProductElement(VirtualGlass productData, String productionType, String productType,
			String productSpecName, String productRequestName, String processFlowName, String processOperationName,
			String productThickness, String productSize, String productVendor,
			String machineRecipeName, String samplingFlag, String degree)
					throws CustomException
	{
		Element productElement = new Element("PRODUCT");

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText("");
		productElement.addContent(lotNameElement);

		Element productNameElement = new Element("PRODUCTNAME");
		productNameElement.setText(productData.getVirtualGlassName());
		productElement.addContent(productNameElement);

		Element productTypeElement = new Element("PRODUCTTYPE");
		productTypeElement.setText(productType);
		productElement.addContent(productTypeElement);

		Element processflowElement = new Element("PROCESSFLOWNAME");
		processflowElement.setText(processFlowName);
		productElement.addContent(processflowElement);

		Element processOperationNameElement = new Element("PROCESSOPERATIONNAME");
		processOperationNameElement.setText(processOperationName);
		productElement.addContent(processOperationNameElement);

		Element positionElement = new Element("POSITION");
		positionElement.setText(String.valueOf(productData.getPosition()));
		productElement.addContent(positionElement);

		Element productSpecNameElement = new Element("PRODUCTSPECNAME");
		productSpecNameElement.setText(productSpecName);
		productElement.addContent(productSpecNameElement);

		Element productionTypeElement = new Element("PRODUCTIONTYPE");
		productionTypeElement.setText(productionType);
		productElement.addContent(productionTypeElement);

		Element productJudgeElement = new Element("PRODUCTJUDGE");
		productJudgeElement.setText(productData.getGrade());
		productElement.addContent(productJudgeElement);

		Element productGradeElement = new Element("PRODUCTGRADE");
		productGradeElement.setText(productData.getGrade());
		productElement.addContent(productGradeElement);

		Element dummyTypeElement = new Element("DUMMYTYPE");
		dummyTypeElement.setText("");
		productElement.addContent(dummyTypeElement);

		Element tmsFlagElement = new Element("TMSFLAG");
		tmsFlagElement.setText("");
		productElement.addContent(tmsFlagElement);

		Element turnFlagElement = new Element("TURNFLAG");
		turnFlagElement.setText(degree);
		productElement.addContent(turnFlagElement);

		Element furnaceFlagElement = new Element("FURNACEFLAG");
		furnaceFlagElement.setText("");
		productElement.addContent(furnaceFlagElement);

		Element subProductJudgesElement = new Element("SUBPRODUCTJUDGES");
		subProductJudgesElement.setText("");
		productElement.addContent(subProductJudgesElement);

		Element subProductGradesElement = new Element("SUBPRODUCTGRADES");
		subProductGradesElement.setText("");
		productElement.addContent(subProductGradesElement);

		Element productThicknessElement = new Element("PRODUCTTHICKNESS");
		productThicknessElement.setText(productThickness);
		productElement.addContent(productThicknessElement);

		Element workOrderElement = new Element("WORKORDER");
		workOrderElement.setText(productRequestName);
		productElement.addContent(workOrderElement);

		Element crateNameElement = new Element("CRATENAME");
		crateNameElement.setText(productData.getCrateName());
		productElement.addContent(crateNameElement);

		Element productSizeElement = new Element("PRODUCTSIZE");
		productSizeElement.setText(productSize);
		productElement.addContent(productSizeElement);

		Element glassMakerElement = new Element("GLASSMAKER");
		glassMakerElement.setText(productVendor);
		productElement.addContent(glassMakerElement);

		Element productRecipeElement = new Element("PRODUCTRECIPE");

		productRecipeElement.setText(machineRecipeName);
		productElement.addContent(productRecipeElement);

		Element exeposureUnitId = new Element("EXPOSUREUNITID");
		exeposureUnitId.setText("");
		productElement.addContent(exeposureUnitId);

		Element exeposureRecipeName = new Element("EXPOSURERECIPENAME");
		exeposureRecipeName.setText("");
		productElement.addContent(exeposureRecipeName);

		Element reworkCountElement = new Element("REWORKCOUNT");
		reworkCountElement.setText("0");
		productElement.addContent(reworkCountElement);

		Element dummyCountElement = new Element("DUMMYUSEDCOUNT");
		dummyCountElement.setText("0");
		productElement.addContent(dummyCountElement);

		Element maskNameElement = new Element("MASKNAME");
		maskNameElement.setText("");
		productElement.addContent(maskNameElement);

		Element proberNameElement = new Element("PROBERNAME");
		proberNameElement.setText("");
		productElement.addContent(proberNameElement);

		Element processingInfoElement = new Element("PROCESSINGINFO");
		processingInfoElement.setText("");
		productElement.addContent(processingInfoElement);

		Element samplingFlagElement = new Element("SAMPLINGFLAG");
		samplingFlagElement.setText(samplingFlag);
		productElement.addContent(samplingFlagElement);

		Element arrayCutRepairType = new Element("ARRAYCUTREPAIRTYPE");
		arrayCutRepairType.setText("");
		productElement.addContent(arrayCutRepairType);

		Element lcvdRepairType = new Element("LCVDREPAIRTYPE");
		lcvdRepairType.setText("");
		productElement.addContent(lcvdRepairType);

		Element lastProcessEndTime = new Element("LASTPROCESSENDTIME");
		lastProcessEndTime.setText("");
		productElement.addContent(lastProcessEndTime);

		Element ELARecipeName = new Element("ELARECIPENAME");
		ELARecipeName.setText("");
		productElement.addContent(ELARecipeName);

		Element ELAEnergyUsage = new Element("ELAENERGYUSAGE");
		ELAEnergyUsage.setText("");
		productElement.addContent(ELAEnergyUsage);

		return productElement;
	}

	/**
	 * Glass determine going to proceed
	 * @author swcho
	 * @since 2015.03.10
	 * @param doc
	 * @param lotData
	 * @param durableData
	 * @param slotMap
	 * @throws CustomException
	 */
	private void doGlassSelection(Document doc, Lot lotData, Durable durableData, String slotMap)
			throws CustomException
	{
		StringBuffer slotMapTemp = new StringBuffer();

		for (long i=0; i<durableData.getCapacity(); i++)
		{
			slotMapTemp.append(GenericServiceProxy.getConstantMap().E_PRODUCT_NOT_IN_SLOT);
		}

		if (!slotMap.isEmpty() && (slotMap.length() != slotMapTemp.length()))
			throw new CustomException("CST-2001", durableData.getKey().getDurableName());

	}

	private Document downloadCarrierJob(Document doc, String slotMap, Durable durableData, Machine machineData, String portName, String portType, String portUseType, String defaultRecipeNameSpace)
			throws CustomException
	{
		eventLog.info("CST Cleaner job download");

		try
		{
			//validation
			CommonValidation.checkEmptyCst(durableData.getKey().getDurableName());
		}
		catch (CustomException ce)
		{
			/* 20180531, Add CST Hold Logic ==>> */
			MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "Hold CST","Hold CST");
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-006");
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
			return doc;
			//throw new CustomException("CST-0006", durableData.getKey().getDurableName());
			/* <<== 20180531, Add CST Hold Logic */
		}

		/* 20180531, Add CST Hold Logic ==>> */
		try
		{
			//slot map validation
			if (StringUtil.isNotEmpty(slotMap))
			{
				CommonValidation.checkCstSlot(slotMap);
			}
		}
		catch (CustomException ce)
		{
			MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "Hold CST","Hold CST");
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, "CST-0026");
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, durableData.getKey().getDurableName() + " is not empty");
			return doc;
			//throw new CustomException("CST-0026");
		}
		/* <<== 20180531, Add CST Hold Logic */

		//New
		this.generateEDOBodyTemplate(SMessageUtil.getBodyElement(doc), machineData, durableData.getKey().getDurableName(),
				durableData.getDurableState(),durableData.getDurableType(), portName,portType, portUseType, slotMap, "");

		//Recipe how to control?
		//String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(durableData.getFactoryName(), durableData.getDurableSpecName(), durableData.getKey().getDurableName(), machineData.getKey().getMachineName(), false);

		DurableSpecKey keyInfo = new DurableSpecKey();
		keyInfo.setDurableSpecName(durableData.getDurableSpecName());
		keyInfo.setFactoryName(durableData.getFactoryName());
		keyInfo.setDurableSpecVersion("00001");

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(keyInfo);

		SMessageUtil.setBodyItemValue(doc, "MACHINERECIPE", CommonUtil.getValue(durableSpecData.getUdfs(), "CSTCLEANRECIPE"));
		SMessageUtil.setBodyItemValue(doc, "SLOTSEL", this.slotMapTransfer(slotMap));

		return doc;
	}

	private void validateSorterJob(String machineName, String portName, String carrierName) throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer()
				.append("SELECT J.jobName, J.jobState, J.processFlowName, J.processOperationName,").append("\n")
				.append("       C.carrierName, C.lotName, C.transferDirection").append("\n")
				.append("  FROM CT_SortJob J, CT_SortJobCarrier C").append("\n")
				.append(" WHERE J.jobName = C.jobName").append("\n")
				.append("   AND C.machineName = ?").append("\n")
				.append("   AND C.carrierName = ?").append("\n")
				.append("   AND C.portName = ?").append("\n")
				.append("   AND J.jobState IN (?, ?)");

		List<ListOrderedMap> result;

		try
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), 
															new Object[] {machineName, carrierName, portName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_STARTED});
		}
		catch (Exception ex)
		{
			result = new ArrayList<ListOrderedMap>();
		}

		if (result.size() < 1)
		{
			throw new CustomException("SYS-9999", "SortJob", "CST is not enable to Sorter job");
		}
	}

	private Lot getLotData(String carrierName) throws CustomException
	{
		String condition = "WHERE carrierName = ?";

		Object[] bindSet = new Object[] {carrierName};

		List<Lot> lotList;

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);

			//check MultiLot
			if(lotList.size() != 1)
			{
				throw new CustomException("LOT-0222", "");
			}
		}
		catch(NotFoundSignal ne)
		{
			//empty CST
			lotList = new ArrayList<Lot>();

			return null;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "Lot", "Lot binding CST incorrect");
		}

		return lotList.get(0);
	}

	private Lot assignReleaseLot(Document doc, String factoryName, String lotName, String machineName, String carrierName, String assignedWOName) throws CustomException
	{

		List<ListOrderedMap> planList = CommonUtil.getProductRequestPlanList(machineName);
		ProductRequestPlan planData = null;
		Lot lot = CommonUtil.getLotInfoByLotName(lotName);
		List<String> toProductSpecNameList = this.getToProductSpecNameList(lot);
		List<String> firstPlanList = new ArrayList<String>();
		Boolean bool = true;
		for (ListOrderedMap prPlanListMap : planList)
		{
			firstPlanList.add(CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTNAME"));

			/*ProductRequest pRequest = CommonUtil.getProductRequestData(prPlan.getKey().getProductRequestName());
			if (toProductSpecNameList.contains(pRequest.getProductSpecName()))
			{
				planData = prPlan;
				break;
			}	*/

			if (CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTNAME").equalsIgnoreCase(assignedWOName)) {
				if (CommonUtil.getValue(prPlanListMap, "PRODUCTREQUESTHOLDSTATE").equalsIgnoreCase(GenericServiceProxy.getConstantMap().Prq_OnHold)) {
					throw new CustomException("PRODUCTREQUEST-0041");
				}
				bool = Long.parseLong(CommonUtil.getValue(prPlanListMap, "PLANQUANTITY")) - Long.parseLong(CommonUtil.getValue(prPlanListMap, "RELEASEDQUANTITY")) >= lot.getProductQuantity();

				ProductRequestPlanKey planKey = new ProductRequestPlanKey();
				planKey.setAssignedMachineName(machineName);
				planKey.setProductRequestName(assignedWOName);
				Timestamp planReleasedTime = (Timestamp) prPlanListMap.get("PLANRELEASEDTIME");
				planKey.setPlanReleasedTime(planReleasedTime);
				planData = ProductRequestPlanServiceProxy.getProductRequestPlanService().selectByKey(planKey );

			}
		}
		if (!firstPlanList.contains(assignedWOName)) {
			throw new CustomException("PRODUCTREQUEST-0041");
		}



		/*if (planData == null || planData.getProductRequestHoldState().equals(GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PRODUCTREQUEST-0041");
		}
		bool = planData.getPlanQuantity() - planData.getReleasedQuantity() > lot.getProductQuantity();*/

		StringBuffer sqlBuffer =  new StringBuffer().append("	SELECT L.LOTNAME, L.PRODUCTSPECNAME, L.PRODUCTIONTYPE, L.PRODUCTREQUESTNAME,	").append("\n")
				.append("	         L.LOTSTATE, L.PRODUCTQUANTITY,	").append("\n")
				.append("	         L.FACTORYNAME, L.LASTFACTORYNAME,	").append("\n")
				.append("	         L.PROCESSFLOWNAME, L.PROCESSOPERATIONNAME	").append("\n")
				.append("	    FROM TPPOLICY T, POSFACTORYRELATION P, PRODUCTREQUEST W, LOT L, ENUMDEFVALUE E	").append("\n")
				.append("	   WHERE T.CONDITIONID = P.CONDITIONID	").append("\n")
				.append("	     AND W.PRODUCTSPECNAME = P.TOPRODUCTSPECNAME	").append("\n")
				.append("	     AND T.PRODUCTSPECNAME = L.PRODUCTSPECNAME	").append("\n")
				.append("	     AND L.factoryName = ?	").append("\n")
				.append("	     AND W.productRequestName = ?	").append("\n")
				.append("	     AND L.lotState = ?	").append("\n")
				.append("	     AND L.lotName = ?	").append("\n");
		if (!factoryName.equals("CELL"))
		{
			sqlBuffer.append("	     AND E.ENUMVALUE = W.PRODUCTREQUESTTYPE ").append("\n")
			.append("	     AND L.PRODUCTIONTYPE IN (E.DESCRIPTION) ");
		}

		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(),
					new Object[] {factoryName, assignedWOName, "Released", lotName});

			if (result.size() < 1)
				throw new CustomException("SYS-9999", "ProductRequestPlan", "Lot is not available for first reserved Work Order at EQP");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProductRequestPlan", fe.getMessage());
		}


		Document cloneDoc = (Document) doc.clone();

		try
		{
			if (bool)
			{
				SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "AssignReleaseLot");
			}
			else
			{
				SMessageUtil.setHeaderItemValue(cloneDoc, "MESSAGENAME", "AssignWorkOrderV2");
			}

			SMessageUtil.setHeaderItemValue(cloneDoc, "ORIGINALSOURCESUBJECTNAME", "");
			cloneDoc.getRootElement().removeChild(SMessageUtil.Body_Tag);

			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

			Element element1 = new Element("FACTORYNAME");
			element1.setText(factoryName);
			eleBodyTemp.addContent(element1);

			Element element2 = new Element("ASSIGNEDMACHINENAME");
			element2.setText(machineName);
			eleBodyTemp.addContent(element2);

			Element element21 = new Element("PRODUCTREQUESTNAME");
			element21.setText(assignedWOName);
			eleBodyTemp.addContent(element21);

			Element element3 = new Element("PLANRELEASEDTIME");
			element3.setText(TimeStampUtil.toTimeString(planData.getKey().getPlanReleasedTime()));
			eleBodyTemp.addContent(element3);

			Element element5 = new Element("PORTNAME");
			element5.setText("N");
			eleBodyTemp.addContent(element5);

			Element element6 = new Element("CHKAUTO");
			element6.setText("N");
			eleBodyTemp.addContent(element6);

			if (bool)
			{
				Element element7 = new Element("LOTLIST");
				{
					Element eleLot = new Element("LOT");
					{
						Element eleLotName = new Element("LOTNAME");
						eleLotName.setText(lotName);
						eleLot.addContent(eleLotName);
					}
					element7.addContent(eleLot);
				}
				eleBodyTemp.addContent(element7);
			}
			else
			{
				Map<String, Long> productNamePositionMap = this.getProductNameAndPosition(lotName,planData);

				Element element8 = new Element("LOTNAME");
				element8.setText(lotName);
				eleBodyTemp.addContent(element8);

				Element element9 = new Element("PRODUCTLIST");
				{
					for (String productName : productNamePositionMap.keySet())
					{
						Element eProduct = new Element("PRODUCT");
						{
							Element eleProductName = new Element("PRODUCTNAME");
							eleProductName.setText(productName);
							eProduct.addContent(eleProductName);

							Element elePosition = new Element("POSITION");
							elePosition.setText(String.valueOf(productNamePositionMap.get(productName)));
							eProduct.addContent(elePosition);
						}
						element9.addContent(eProduct);
					}
				}
				eleBodyTemp.addContent(element9);
			}


			// overwrite
			cloneDoc.getRootElement().addContent(eleBodyTemp);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "Lot", "Message generating failed");
		}

		try
		{
			if (bool)
			{
				InvokeUtils.invokeMethod(InvokeUtils.newInstance(AssignWorkOrder.class.getName(), null, null), "execute", new Object[] {cloneDoc});
			}
			else
			{
				InvokeUtils.invokeMethod(InvokeUtils.newInstance(AssignWorkOrderV2.class.getName(), null, null), "execute", new Object[] {cloneDoc});
			}

			//return released Lot
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);

			return lotData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "System", "Invocation failed");
		}
	}

	private Map<String, Long> getProductNameAndPosition(String lotName, ProductRequestPlan planData)
	{
		Long planCount = planData.getPlanQuantity() - planData.getReleasedQuantity();
		String condition = "WHERE lotName = ? AND ROWNUM <= ? ORDER BY position desc";
		Object[] bindSet = new Object[]{lotName, planCount};

		List<Product> products = ProductServiceProxy.getProductService().select(condition, bindSet);
		LinkedHashMap<String,Long> productNameMap = new LinkedHashMap<String, Long>();

		for (Product product : products)
		{
			productNameMap.put(product.getKey().getProductName(), product.getPosition());
		}
		return productNameMap;
	}

	private List<String> getToProductSpecNameList(Lot lotData) throws CustomException
	{
		String sql = "SELECT F.TOPRODUCTSPECNAME FROM TPPOLICY T, POSFACTORYRELATION F WHERE T.CONDITIONID = F.CONDITIONID AND T.PRODUCTSPECNAME = ?";
		Object[] bindSet = new Object[] {lotData.getProductSpecName()};
		List<String> toProductSpecNameList = new ArrayList<String>();

		try
		{
			List<Map<String, String>> sqlList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
			if (sqlList.size() > 0)
			{
				for (int i=0; i<sqlList.size(); i++)
				{
					toProductSpecNameList.add(sqlList.get(i).get("TOPRODUCTSPECNAME"));
				}
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ToProductSpecName", fe.getMessage());
		}
		return toProductSpecNameList;
	}

	private String slotMapTransfer(String slotMap) throws CustomException
	{
		StringBuffer slotMapTemp = new StringBuffer();

		for(int i = 0; i<slotMap.length(); i++)
		{
			slotMapTemp.append("N");
		}

		for(int i = 0; i<slotMap.length(); i++)
		{
			if(String.valueOf(slotMap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
				slotMapTemp.append("Y");
		}

		return slotMapTemp.toString();
	}

	//Get realOperationName at OLED
	private String getRealOperationName(String factoryName, String productSpecName, String ecCode, String processFlowName, String machineName, String carrierName )throws CustomException
	{
		String processOperationName = "" ;

		//String strSql = "SELECT DISTINCT PROCESSOPERATIONNAME " +
		//		"  FROM TPEFOMPOLICY " +
		//		" WHERE     FACTORYNAME = :FACTORYNAME " +
		//		"       AND PRODUCTSPECNAME = :PRODUCTSPECNAME " +
		//		"       AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
		//		"       AND ECCODE = :ECCODE " +
		//		"       AND PROCESSFLOWNAME = :PROCESSFLOWNAME " +
		//		"       AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
		//		"       AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
		//		"       AND MACHINENAME = :MACHINENAME " ;

		String strSql= StringUtil.EMPTY;
		strSql = strSql + "  SELECT DISTINCT PROCESSOPERATIONNAME                                                                 \n";
		strSql = strSql + "    FROM TPEFOMPOLICY T                                                                                \n";
		strSql = strSql + "    INNER JOIN POSRECIPE P ON T.CONDITIONID  = P.CONDITIONID                                           \n";
		strSql = strSql + "   WHERE 1 = 1                                                                                         \n";
		strSql = strSql + "     AND ((T.FACTORYNAME = :FACTORYNAME) OR (T.FACTORYNAME = '*'))                                     \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECNAME = :PRODUCTSPECNAME) OR (T.PRODUCTSPECNAME = '*'))                         \n";
		strSql = strSql + "     AND ((T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION) OR (T.PRODUCTSPECVERSION = '*'))                \n";
		strSql = strSql + "     AND ((T.ECCODE = :ECCODE) OR (T.ECCODE = '*'))                                                    \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWNAME = :PROCESSFLOWNAME) OR (T.PROCESSFLOWNAME = '*'))                         \n";
		strSql = strSql + "     AND ((T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) OR (T.PROCESSFLOWVERSION = '*'))                \n";
		//strSql = strSql + "     AND ((T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME) OR (T.PROCESSOPERATIONNAME = '*'))          \n";
		strSql = strSql + "     AND ((T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION) OR (T.PROCESSOPERATIONVERSION = '*')) \n";
		strSql = strSql + "     AND ((T.MACHINENAME = :MACHINENAME) OR (T.MACHINENAME = '*'))                                     \n";
		strSql = strSql + " ORDER BY DECODE (FACTORYNAME, '*', 9999, 0),                                                          \n";
		strSql = strSql + "          DECODE (PRODUCTSPECNAME, '*', 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (PRODUCTSPECVERSION, '*', 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (ECCODE, '*', 9999, 0),                                                               \n";
		strSql = strSql + "          DECODE (PROCESSFLOWNAME, '*', 9999, 0),                                                      \n";
		strSql = strSql + "          DECODE (PROCESSFLOWVERSION, '*', 9999, 0),                                                   \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONNAME, '*', 9999, 0),                                                 \n";
		strSql = strSql + "          DECODE (PROCESSOPERATIONVERSION, '*', 9999, 0),                                              \n";
		strSql = strSql + "          DECODE (MACHINENAME, '*', 9999, 0)                                                           \n";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", "00001");
		bindMap.put("ECCODE", ecCode);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("PROCESSOPERATIONVERSION", "00001");
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> tpefomPolicyData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

		if ( tpefomPolicyData.size() != 1 )
		{
			throw new CustomException("POLICY-0001", carrierName);
		}
		else
		{
			processOperationName = (String) tpefomPolicyData.get(0).get("PROCESSOPERATIONNAME");
		}

		return processOperationName ;
	}

	/**
	 * @Name     setLotInfoDownLoadSendProcessList
	 * @since    2018. 6. 1.
	 * @author   hhlee
	 * @contents Set LotInfoDownLoadSend Process List
	 * @param processlistElement
	 * @param factoryName
	 * @param productName
	 * @return
	 * @throws CustomException
	 */
	private Element setLotInfoDownLoadSendProcessList(String factoryName, String productName) throws CustomException
	{
		Element processListElement = new Element("PROCESSLIST");
		try
		{
			//String strSql = StringUtil.EMPTY;
			//strSql = strSql + " SELECT SQ.TIMEKEY AS TIMEKEY,                                   \n";
			//strSql = strSql + "        SQ.PRODUCTNAME AS PRODUCTNAME,                           \n";
			//strSql = strSql + "        SQ.PROCESSFLOWNAME AS PROCESSFLOWNAME,                   \n";
			//strSql = strSql + "        SQ.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME,         \n";
			//strSql = strSql + "        SQ.MACHINENAME AS MACHINENAME                            \n";
			//strSql = strSql + "   FROM (                                                        \n";
			//strSql = strSql + "         SELECT /*+ INDEX_DESC(PRODUCTHISTORY_PK) */             \n";
			//strSql = strSql + "                PH.TIMEKEY AS TIMEKEY,                           \n";
			//strSql = strSql + "                PH.PRODUCTNAME AS PRODUCTNAME,                   \n";
			//strSql = strSql + "                PF.PROCESSFLOWNAME AS PROCESSFLOWNAME,           \n";
			//strSql = strSql + "                PH.PROCESSOPERATIONNAME AS PROCESSOPERATIONNAME, \n";
			//strSql = strSql + "                PH.MACHINENAME AS MACHINENAME                    \n";
			//strSql = strSql + "           FROM PRODUCTHISTORY PH, PROCESSFLOW PF                \n";
			//strSql = strSql + "          WHERE 1=1                                              \n";
			//strSql = strSql + "            AND PH.PRODUCTNAME = :PRODUCTNAME                    \n";
			//strSql = strSql + "            AND PH.MACHINENAME IS NOT NULL                       \n";
			//strSql = strSql + "            AND PH.PROCESSFLOWNAME = PF.PROCESSFLOWNAME          \n";
			//strSql = strSql + "            AND PF.PROCESSFLOWTYPE = :PROCESSFLOWTYPE            \n";
			//strSql = strSql + "            AND PF.FACTORYNAME = :FACTORYNAME                    \n";
			//strSql = strSql + "           ORDER BY PH.TIMEKEY DESC                              \n";
			//strSql = strSql + "         )SQ                                                     \n";
			//strSql = strSql + " WHERE 1=1                                                       \n";
			//strSql = strSql + "   AND ROWNUM <= 5                                               \n";

			String strSql = StringUtil.EMPTY;
			strSql = strSql + " SELECT MQ.PRODUCTNAME,                                                                 \n";
			strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,1), ' ') AS PROCESSMACHINENAME,  \n";
			strSql = strSql + "        NVL(REGEXP_SUBSTR(MQ.PROCESSOPERATION,'[^,]+',1,2), ' ') AS PROCESSOPERATIONNAME \n";
			strSql = strSql + "   FROM (                                                                               \n";
			strSql = strSql + "         SELECT T.NO, SQ.PRODUCTNAME,                                                   \n";
			strSql = strSql + "                (CASE WHEN T.NO = 1 THEN SQ.AT1                                         \n";
			strSql = strSql + "                      WHEN T.NO = 2 THEN SQ.AT2                                         \n";
			strSql = strSql + "                      WHEN T.NO = 3 THEN SQ.AT3                                         \n";
			strSql = strSql + "                      WHEN T.NO = 4 THEN SQ.AT4                                         \n";
			strSql = strSql + "                      WHEN T.NO = 5 THEN SQ.AT5                                         \n";
			strSql = strSql + "                      END) AS PROCESSOPERATION                                          \n";
			strSql = strSql + "           FROM (                                                                       \n";
			strSql = strSql + "                 SELECT PO.PRODUCTNAME AS PRODUCTNAME,                                  \n";
			strSql = strSql + "                        PO.ATTRIBUTE1 AS AT1,                                           \n";
			strSql = strSql + "                        PO.ATTRIBUTE2 AS AT2,                                           \n";
			strSql = strSql + "                        PO.ATTRIBUTE3 AS AT3,                                           \n";
			strSql = strSql + "                        PO.ATTRIBUTE4 AS AT4,                                           \n";
			strSql = strSql + "                        PO.ATTRIBUTE5 AS AT5                                            \n";
			strSql = strSql + "                   FROM CT_PROCESSEDOPERATION PO                                        \n";
			strSql = strSql + "                  WHERE 1=1                                                             \n";
			strSql = strSql + "                    AND PO.PRODUCTNAME = :PRODUCTNAME                                   \n";
			strSql = strSql + "                 )SQ,                                                                   \n";
			strSql = strSql + "                 (SELECT LEVEL NO                                                       \n";
			strSql = strSql + "                    FROM DUAL CONNECT BY LEVEL <= 5) T                                  \n";
			strSql = strSql + "         )MQ                                                                            \n";
			strSql = strSql + "  ORDER BY MQ.NO                                                                        \n";

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);
			//bindMap.put("FACTORYNAME", factoryName);
			//bindMap.put("PROCESSFLOWTYPE", GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN);

			List<Map<String, Object>> ProcessOperationData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);

			if ( ProcessOperationData.size() > 0 )
			{
				for(int i = 0; i < ProcessOperationData.size(); i++ )
				{
					/* If you have to send only what has a value, loosen the IF statement below. */
					//if (!ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim().isEmpty())
					{
						Element processElement = new Element("PROCESS");

						Element processOperationNameLElement = new Element("PROCESSOPERATIONNAME");
						processOperationNameLElement.setText(ProcessOperationData.get(i).get("PROCESSOPERATIONNAME").toString().trim());
						processElement.addContent(processOperationNameLElement);

						Element processMachineNameElement = new Element("PROCESSMACHINENAME");
						processMachineNameElement.setText(ProcessOperationData.get(i).get("PROCESSMACHINENAME").toString().trim());
						processElement.addContent(processMachineNameElement);

						processListElement.addContent(processElement);
					}
				}
			}
			else
			{
			}
		}
		catch (Exception ex)
		{
			eventLog.warn("[setLotInfoDownLoadSendProcessList] Data Query Failed");;
		}

		return processListElement;
	}

	/**
	 * @Name     getNewActualSamplePosition
	 * @since    2018. 6. 16.
	 * @author   hhlee
	 * @contents 
	 * @param indexcnt
	 * @param logicalslotmap
	 * @param actualsamplepostiondata
	 * @return
	 */
	private String getNewActualSamplePosition(int indexcnt, String logicalslotmap , String[] actualsamplepostiondata)
	{
		String newpostion = StringUtil.EMPTY;
		boolean newposition = false;
		int position = 0;

		for(int i = indexcnt + 1; i<logicalslotmap.length(); i++) 
		{
			position = i + 1;	        
			if(String.valueOf(logicalslotmap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
			{
				newposition = false;
				newpostion = String.valueOf(position);
				for(int j = 0; j < actualsamplepostiondata.length; j++ )
				{
					if (position ==  Integer.parseInt(actualsamplepostiondata[j]))
					{           
						newposition = true;
						break;
					}
				}

				if (!newposition)
				{
					break;
				}	            
			}
			newpostion = StringUtil.EMPTY;
		}

		return newpostion;
	}

	/**
	 * @Name     actualSamplePositionSorting
	 * @since    2018. 6. 16.
	 * @author   hhlee
	 * @contents 
	 * @param actualSamplePosition
	 * @return
	 */
	private String actualSamplePositionSorting(String actualSamplePosition)
	{
		String[] actualsamplepostion = actualSamplePosition.trim().split(",");
		String actualsamplepostionNew = StringUtil.EMPTY;
		String positiontemp = StringUtil.EMPTY;

		for(int i = 0; i < actualsamplepostion.length - 1; i++ )
		{
			for(int j = 0; j < actualsamplepostion.length - 1; j++ )
			{
				if(Long.parseLong(actualsamplepostion[j]) > Long.parseLong(actualsamplepostion[j+1]))
				{
					positiontemp = actualsamplepostion[j];
					actualsamplepostion[j] = actualsamplepostion[j+1];
					actualsamplepostion[j+1] = positiontemp;
				}
			}
		}
		for(int i = 0; i < actualsamplepostion.length; i++ )
		{
			actualsamplepostionNew = actualsamplepostionNew + "," + actualsamplepostion[i] ;
		}
		actualsamplepostionNew = actualsamplepostionNew.substring(1);

		return actualsamplepostionNew;	            
	}

	private String getSlotMap(Lot lotData, Durable durableData)
	{
		String slotMap= "";
		
		for(int i=1; i<=durableData.getCapacity();i++)
		{
			String condition = "WHERE LOTNAME = ? AND POSITION = ? AND PRODUCTSTATE <> ?";
			Object[] bindSet = new Object[]{lotData.getKey().getLotName(), i, GenericServiceProxy.getConstantMap().Prod_Scrapped};

			try
			{
				List<Product> products = ProductServiceProxy.getProductService().select(condition, bindSet);
				
				if(products.size()>0)
				{
					slotMap += "O";
				}
			}
			catch(Throwable e)
			{
				slotMap += "X";
			}
			
		}
		return slotMap;
	}
}
