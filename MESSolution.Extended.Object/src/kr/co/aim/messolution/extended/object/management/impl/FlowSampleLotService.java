package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLot;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class FlowSampleLotService extends CTORMService<FlowSampleLot> {

	public static Log logger = LogFactory.getLog(FlowSampleLotService.class);
	
	private final String historyEntity = "FlowSampleLotHist";
	
	/*try
	{
		
	}
	catch(greenFrameDBErrorSignal ne)
	{
		if (ne.getErrorCode().equals("NotFoundSignal"))
			throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
		else
			throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
	}*/
	
	public List<FlowSampleLot> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FlowSampleLot> result = super.select(condition, bindSet, FlowSampleLot.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
		}
	}
	
	public FlowSampleLot selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FlowSampleLot.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
		}
	}
	
	public FlowSampleLot create(EventInfo eventInfo, FlowSampleLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FlowSampleLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
		}
	}
	
	public FlowSampleLot modify(EventInfo eventInfo, FlowSampleLot dataInfo)
		throws CustomException, NotFoundSignal
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleLot", ne.getMessage());
		}
	}
	
	/**
	 * create single FlowSampleLot
	 * @author hwlee89
	 * @since 2016.12.12
	 * @param eventInfo
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @param toProcessFlowName
	 * @param toProcessOperationName
	 * @param lotSampleFlag
	 * @param lotSampleCount
	 * @param currentLotCount
	 * @param totalLotCount
	 * @param productSampleCount
	 * @param productSamplePosition
	 * @param actualProductCount
	 * @param actualSamplePosition
	 * @param manualSampleFlag
	 * @throws CustomException
	 * @throws NotFoundSignal
	 * @return FlowSampleLot
	 */
	public FlowSampleLot createFlowSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, 
								String processOperationName, String machineName, String toProcessFlowName, String toProcessOperationName, 
								String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount,
								String productSampleCount, String productSamplePosition, 
								String actualProductCount, String actualSamplePosition, String manualSampleFlag)
		throws CustomException, NotFoundSignal
	{
		FlowSampleLot sampleLotInfo = new FlowSampleLot(lotName, factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessFlowName, toProcessOperationName);
		
		sampleLotInfo.setLOTSAMPLEFLAG(lotSampleFlag);
		sampleLotInfo.setLOTSAMPLECOUNT(lotSampleCount);
		sampleLotInfo.setCURRENTLOTCOUNT(currentLotCount);
		sampleLotInfo.setTOTALLOTCOUNT(totalLotCount);
		sampleLotInfo.setPRODUCTSAMPLECOUNT(productSampleCount);
		sampleLotInfo.setPRODUCTSAMPLEPOSITION(productSamplePosition);
		sampleLotInfo.setACTUALPRODUCTCOUNT(actualProductCount);
		sampleLotInfo.setACTUALSAMPLEPOSITION(actualSamplePosition);
		sampleLotInfo.setMANUALSAMPLEFLAG(manualSampleFlag);
		
		sampleLotInfo.setLASTEVENTNAME(eventInfo.getEventName());
		sampleLotInfo.setLASTEVENTTIME(eventInfo.getEventTime());
		sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
		sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
		
		sampleLotInfo = ExtendedObjectProxy.getFlowSampleLotService().create(eventInfo, sampleLotInfo);
		
		return sampleLotInfo;
	}
	
	/**
	 * reserve sampling
	 * @author swcho
	 * @since 2016.12.19
	 * @param EventInfo
	 * @param Lot
	 * @return 
	 * @throws CustomException
	 */
	public void reserveSampling(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		//170215 by swcho : pilot Lot is not applicable
		if (!ExtendedObjectProxy.getFirstGlassLotService().getActiveJobNameByLotName(lotData.getKey().getLotName()).isEmpty())
		{
			logger.warn("Lot on pilot is throughout from sampling");
			return;
		}
		
		List<ListOrderedMap> ruleList = PolicyUtil.getFlowSamplingRule(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), lotData.getMachineName());
		
		for (ListOrderedMap rule : ruleList)
		{
			try
			{
				ExtendedObjectProxy.getFlowSampleLotService().setFlowSampling(eventInfo, lotData, rule);
			}
			catch (Exception ex)
			{
				logger.error("Flow sampling reservation occurs error");
			}
		}
	}
	
	/**
	 * rule find out and reserve sampling without duplication
	 * @author swcho
	 * @since 2016.12.20
	 * @param eventInfo
	 * @param lotData
	 * @param rule
	 * @throws CustomException
	 */
	public void setFlowSampling(EventInfo eventInfo, Lot lotData, ListOrderedMap rule)
		throws CustomException
	{
		//counting avobe all
		FlowSampleLotCount countInfo = ExtendedObjectProxy.getFlowSampleLotCountService().calculateSamplingCount(eventInfo, lotData, rule);
		
		//validate sampling reservation
		if (ExtendedObjectProxy.getFlowSampleLotService().isDuplicated(lotData, CommonUtil.getValue(rule, "FACTORYNAME"), CommonUtil.getValue(rule, "TOPROCESSFLOWNAME"), CommonUtil.getValue(rule, "TOPROCESSOPERATIONNAME")))
		{
			throw new CustomException("SYS-9999", "Sampling", "Sampling already reserved by this rule");
		}
		else if (!countInfo.getCURRENTLOTCOUNT().equals("1"))
		{
			throw new CustomException("SYS-9999", "Sampling", "No first Lot in this rule");
		}
		
		//reserve sampling block
		//FlowSampleLot flowSampleLot;
		
		String sampleFlag = "Y";
		String manualFlag = "N";
		
		//parcing the Product position
		String productSamplePositions = CommonUtil.getValue(rule, "PRODUCTSAMPLINGPOSITION");
		int samplingQTY = Integer.valueOf(CommonUtil.getValue(rule, "PRODUCTSAMPLINGCOUNT"));
		
		//get current Product list for target Lot
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
		//selected Product list for target Lot
		List<Product> actualProductList = new ArrayList<Product>();
		
		//random selection
		if(productSamplePositions.equals("Random"))
		{
			if(productList.size() <= samplingQTY)
			{
				productSamplePositions = "All";
			}
			else
			{
				Collections.shuffle(productList);
				
				for (int i = 0; i < samplingQTY; i++)
				{
					String position = String.valueOf(productList.get(i).getPosition());
					
					if (productSamplePositions.length() > 0) productSamplePositions += ",";
					
					productSamplePositions += position;
				}
			}
		}
		//total selection
		if(StringUtil.equalsIgnoreCase(productSamplePositions, "All"))
		{
			productSamplePositions = "";
			
			for (Product productData : productList)
			{
				String position = String.valueOf(productData.getPosition());
				
				if (productSamplePositions.length() > 0) productSamplePositions += ",";
				
				productSamplePositions += position;
			}
		}
		//void charge
		{
			//remove blank
			productSamplePositions = productSamplePositions.trim();
			
			//delimeter is ','
			String[] arrSelection = StringUtil.split(productSamplePositions, ",");
			
			List<String> assignedPositions = new ArrayList<String>();
			List<String> unassignedPositions = new ArrayList<String>();
			//find missing
			for (String selection : arrSelection)
			{
				boolean found = false;
				
				for (Product productData : productList)
				{
					long position = productData.getPosition();
					String sPosition = String.valueOf(position);
					
					if (sPosition.equals(selection))
					{//existing position
						found = true;
						assignedPositions.add(selection);
						actualProductList.add(productData);
						break;
					}
				}
				
				if (!found)
				{//missing position
					unassignedPositions.add(selection);
				}
			}
			
			List<Product> unselectedProductList = new ArrayList<Product>(); 
			
			for (Product productData : productList)
			{
				long position = productData.getPosition();
				String sPosition = String.valueOf(position);
				
				boolean isOrphan = true;
				
				for (String assignedPosition : assignedPositions)
				{
					if (sPosition.equals(assignedPosition))
					{
						isOrphan = false;
						break;
					}
				}
				
				if (isOrphan) unselectedProductList.add(productData);
			}
		
			//minimum first
			
			int insufficientQTY = unassignedPositions.size()<unselectedProductList.size()?unassignedPositions.size():unselectedProductList.size();
			
			//20170522 Add by yudan
			for (String unAssignPosition : unassignedPositions)
			{
				if(unAssignPosition.equals("Random"))
				{
					insufficientQTY = insufficientQTY - 1;
					break;
				}
			}			
			
			//randomize once
			Collections.shuffle(unselectedProductList);
			
			for (int cnt = 1; cnt <= insufficientQTY; cnt++)
			{
				//Collections.shuffle(unselectedProductList);
				
				String sPosition = String.valueOf(unselectedProductList.get(cnt-1).getPosition());
				
				assignedPositions.add(sPosition);
				actualProductList.add(unselectedProductList.get(cnt-1));
			}
			
			//final selection
			productSamplePositions = "";
			for (String sPosition : assignedPositions)
			{
				if (productSamplePositions.length() > 0) productSamplePositions += ",";
				
				productSamplePositions += sPosition;
			}
		}
		
		logger.info(String.format("final slot selection : [%s] of [%d] for Lot[%s]", productSamplePositions, samplingQTY, lotData.getKey().getLotName()));
		
		for (Product productData : actualProductList)
		{
			eventInfo.setEventName("Sampling");
			ExtendedObjectProxy.getFlowSampleProductService().createFlowSampleProduct(eventInfo, productData.getKey().getProductName(), productData.getLotName(),
																						productData.getFactoryName(), productData.getProductSpecName(), productData.getProcessFlowName(),
																						productData.getProcessOperationName(), productData.getMachineName(),
																						CommonUtil.getValue(rule, "TOPROCESSFLOWNAME"), CommonUtil.getValue(rule, "TOPROCESSOPERATIONNAME"),
																						sampleFlag,
																						CommonUtil.getValue(rule, "PRODUCTSAMPLINGCOUNT"), CommonUtil.getValue(rule, "PRODUCTSAMPLINGPOSITION"),
																						//161228 by swcho : only single slot would be assigned
																						//productSamplePositions,
																						String.valueOf(productData.getPosition()),
																						manualFlag, "");
		}
		
		eventInfo.setEventName("Sampling");
		ExtendedObjectProxy.getFlowSampleLotService().createFlowSampleLot(eventInfo, lotData.getKey().getLotName(),
																			lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
																			lotData.getProcessOperationName(), lotData.getMachineName(),
																			CommonUtil.getValue(rule, "TOPROCESSFLOWNAME"), CommonUtil.getValue(rule, "TOPROCESSOPERATIONNAME"),
																			sampleFlag,
																			countInfo.getLOTSAMPLECOUNT(), countInfo.getCURRENTLOTCOUNT(), countInfo.getTOTALLOTCOUNT(),
																			CommonUtil.getValue(rule, "PRODUCTSAMPLINGCOUNT"), CommonUtil.getValue(rule, "PRODUCTSAMPLINGPOSITION"),
																			String.valueOf(actualProductList.size()), productSamplePositions,
																			manualFlag);
	}

	/**
	 * sampling destination duplicated check
	 * @since 2016.12.19
	 * @author swcho
	 * @param lotData
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public boolean isDuplicated(Lot lotData, String factoryName, String processFlowName, String processOperationName) throws CustomException
	{
		List<FlowSampleLot> result;
		
		try
		{
			//String factoryName = CommonUtil.getValue(rule, "FACTORYNAME");
			//String productSpecName = CommonUtil.getValue(rule, "PRODUCTSPECNAME");
			//String fromFlowName = CommonUtil.getValue(rule, "PROCESSFLOWNAME");
			//String fromOperationName = CommonUtil.getValue(rule, "PROCESSOPERATIONNAME");
			//String fromMachineName = CommonUtil.getValue(rule, "MACHINENAME");
			//String toFlowName = CommonUtil.getValue(rule, "TOPROCESSFLOWNAME");
			//String toOperationName = CommonUtil.getValue(rule, "TOPROCESSOPERATIONNAME");
			//String lotSamplingCount = CommonUtil.getValue(rule, "LOTSAMPLINGCOUNT");
			
			String condition = " lotName = ? AND factoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND lotSampleFlag = ? ";
			
			Object[] bindSet = new Object[] {lotData.getKey().getLotName(), factoryName, processFlowName, processOperationName, "Y"};
			
			result = ExtendedObjectProxy.getFlowSampleLotService().select(condition, bindSet);
			
			return true;
		}
		catch (NotFoundSignal ne)
		{
			//not found means available
			return false;
		}
	}
	
	/**
	 * purge sampling at specific location on Lot
	 * @author swcho
	 * @since 2016.12.21
	 * @param eventInfo
	 * @param lotData
	 * @param processFlowName
	 * @param processOperationName
	 * @throws CustomException
	 */
	public void purgeFlowSampleLot(EventInfo eventInfo, Lot lotData, String processFlowName, String processOperationName)
		throws CustomException
	{
		eventInfo.setEventName("Remove");
		
		List<FlowSampleLot> sampleLotList;
		
		try
		{
			sampleLotList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND lotSampleFlag = ? ",
									new Object [] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
													processFlowName, processOperationName, "Y"});
		}
		catch (NotFoundSignal ne)
		{
			sampleLotList = new ArrayList<FlowSampleLot>();
		}
		
		for (FlowSampleLot samplelotData : sampleLotList)
		{
			try
			{
				this.remove(eventInfo, samplelotData);
				//cascade
				ExtendedObjectProxy.getFlowSampleProductService().purgeFlowSampleProduct(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), processFlowName, processOperationName);
			}
			catch (Exception ex)
			{
				//ignore
				logger.error(ex);
			}
		}
	}
	
	/**
	 * purge PartSel on Lot
	 * @author yudan
	 * @since 2017.08.17
	 * @param eventInfo
	 * @param lotData
	 * @param processFlowName
	 * @param processOperationName
	 * @throws CustomException
	 */
	public void pausePartSelLot(EventInfo eventInfo, Lot lotData, String processFlowName, String processOperationName)
			throws CustomException
		{
			eventInfo.setEventName("Remove");
			
			List<FlowSampleLot> sampleLotList;			
			
			try
			{
				sampleLotList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND lotSampleFlag = ? ",
											new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), processFlowName, processOperationName,"PARTSEL"});
			}
			catch (NotFoundSignal ne)
			{
				sampleLotList = new ArrayList<FlowSampleLot>();
			}
			
			for (FlowSampleLot samplelotData : sampleLotList)
			{
				try
				{
					this.remove(eventInfo, samplelotData);
					//cascade
					ExtendedObjectProxy.getFlowSampleProductService().pausePartSelProduct(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), processFlowName, processOperationName);
				}
				catch (Exception ex)
				{
					//ignore
					logger.error(ex);
				}
			}						
		}
	
	/**
	 * look for sampling at current locations with Lot
	 * @author swcho
	 * @since 2016.12.21
	 * @param lotData
	 * @return
	 * @throws CustomException
	 */
	public boolean isSamplingReserved(Lot lotData) throws CustomException
	{
		List<FlowSampleLot> sampleLotList;
		
		try
		{
			sampleLotList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND lotSampleFlag = ? ",
										new Object [] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
														lotData.getProcessFlowName(), lotData.getProcessOperationName(),
														"Y"});
		}
		catch (NotFoundSignal ne)
		{
			sampleLotList = new ArrayList<FlowSampleLot>();
		}
		
		if (sampleLotList.size() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * 161227 by swcho : advanced
	 * reserve sampling by flow manual
	 * @author swcho
	 * @since 2016.12.22
	 * @param eventInfo
	 * @param lotData
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @param productList
	 * @parma processOperationList
	 * @throws CustomException
	 */
	public void reserveSampling(EventInfo eventInfo, Lot lotData,
								String factoryName, String processFlowName, String processOperationName,
								String toProcessFlowName,
								List<Product> productList, List processOperationList)
		throws CustomException
	{
		String samplingFlag = "Y";
		String manualFlag = "Y";
		
		ProcessOperationSpec operationData = CommonUtil.getFirstOperation(factoryName, toProcessFlowName);
		
		//List<ListOrderedMap> operationList = ExtendedObjectProxy.getSampleLotService().getInspectionOperationList(factoryName, processFlowName, operationData.getKey().getProcessOperationName(), "");
		
		for (Object processOperation : processOperationList)
		{
			String toFlowName = "";
			String toOperationName = "";
			
			if (processOperation instanceof ListOrderedMap)
			{
				ListOrderedMap cOperationData = (ListOrderedMap) processOperation;
				
				toFlowName = CommonUtil.getValue(cOperationData, "PROCESSFLOWNAME");
				toOperationName = CommonUtil.getValue(cOperationData, "PROCESSOPERATIONNAME");
			}
			else if (processOperation instanceof Element)
			{
				Element eOperationData = (Element) processOperation;
				
				toFlowName = SMessageUtil.getChildText(eOperationData, "PROCESSFLOWNAME", false);
				toOperationName = SMessageUtil.getChildText(eOperationData, "PROCESSOPERATIONNAME", false);
			}
			
			if (toFlowName.isEmpty() || toOperationName.isEmpty())
			{
				continue;
			}
			
			try
			{
				//destination duplication check
				if (ExtendedObjectProxy.getFlowSampleLotService().isDuplicated(lotData, factoryName, toFlowName, toOperationName))
				{
					//throw new CustomException("SYS-9999", "Sampling", "sampling duplication occurs");
					//purge then overwrite
					eventInfo.setEventName("Remove");
					ExtendedObjectProxy.getFlowSampleProductService().purgeFlowSampleProduct(eventInfo, lotData.getKey().getLotName(), factoryName, lotData.getProductSpecName(), toFlowName, toOperationName);
					this.purgeFlowSampleLot(eventInfo, lotData, toFlowName, toOperationName);
				}
				
				eventInfo.setEventName("Reserve");
				
				String positions = "";
				
				for (Product productData : productList)
				{
					ExtendedObjectProxy.getFlowSampleProductService().createFlowSampleProduct(eventInfo, productData.getKey().getProductName(), productData.getLotName(),
																								productData.getFactoryName(), productData.getProductSpecName(),
																								processFlowName, processOperationName, "NA",
																								toFlowName, toOperationName,
																								samplingFlag,
																								String.valueOf(productList.size()), String.valueOf(productData.getPosition()), String.valueOf(productData.getPosition()),
																								manualFlag,
																								"");
					
					if (!positions.isEmpty()) positions += ",";
					positions += String.valueOf(productData.getPosition());
				}
				
				ExtendedObjectProxy.getFlowSampleLotService().createFlowSampleLot(eventInfo, lotData.getKey().getLotName(),
																					lotData.getFactoryName(), lotData.getProductSpecName(),
																					processFlowName, processOperationName, "NA",
																					toFlowName, toOperationName,
																					samplingFlag,
																					"1", "1", "1",
																					String.valueOf(productList.size()), positions,
																					String.valueOf(productList.size()), positions,
																					manualFlag);
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-9999", "Sampling", "failed to reserve sampling");
			}
		}
	}
	
	/**
	 * @author yudan
	 * @since 2017.08.15
	 * @param eventInfo
	 * @param lotData
	 * @param factoryName
	 * @param processFlowName
	 * @param processOperationName
	 * @param productList
	 * @throws CustomException
	 */
	public void partSel(EventInfo eventInfo, Lot lotData,
								String factoryName, String processFlowName, String processOperationName,
								List<Product> productList)
		throws CustomException
	{
		String samplingFlag = "PARTSEL";
		String manualFlag = "Y";			
								
		try
		{				
			eventInfo.setEventName("PartSel");
				
			String positions = "";
				
			for (Product productData : productList)
			{
				ExtendedObjectProxy.getFlowSampleProductService().createFlowSampleProduct(eventInfo, productData.getKey().getProductName(), productData.getLotName(),
																								productData.getFactoryName(), productData.getProductSpecName(),
																								processFlowName, processOperationName, "NA",
																								"NA", "NA",
																								samplingFlag,
																								String.valueOf(productList.size()), String.valueOf(productData.getPosition()), String.valueOf(productData.getPosition()),
																								manualFlag,
																								"");
					
				if (!positions.isEmpty()) positions += ",";
				positions += String.valueOf(productData.getPosition());
			}
				
			ExtendedObjectProxy.getFlowSampleLotService().createFlowSampleLot(eventInfo, lotData.getKey().getLotName(),
																					lotData.getFactoryName(), lotData.getProductSpecName(),
																					processFlowName, processOperationName, "NA",
																					"NA", "NA",
																					samplingFlag,
																					"1", "1", "1",
																					String.valueOf(productList.size()), positions,
																					String.valueOf(productList.size()), positions,
																					manualFlag);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "PartSel", "failed to PartSel");
		}
	}
	
	/**
	 * search Lot sampling
	 * @author swcho
	 * @since 2016.12.28
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public List<FlowSampleLot> getSampling(String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		List<FlowSampleLot> sampleLotList;
		
		try
		{
			sampleLotList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND lotSampleFlag = ? ",
										new Object [] {lotName, factoryName, productSpecName, processFlowName, processOperationName, "Y"});
		}
		catch (NotFoundSignal ne)
		{
			sampleLotList = new ArrayList<FlowSampleLot>();
		}
		
		return sampleLotList;
	}
	
	/**
	 * search Lot PartSel Info
	 * @author yudan
	 * @since 2017.08.16
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @return
	 * @throws CustomException
	 */
	public List<FlowSampleLot> getPartSelLot(String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		List<FlowSampleLot> sampleLotList;
		
		try
		{
			sampleLotList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND lotSampleFlag = ? ",
										new Object [] {lotName, factoryName, productSpecName, processFlowName, processOperationName, "PARTSEL"});
		}
		catch (NotFoundSignal ne)
		{
			sampleLotList = new ArrayList<FlowSampleLot>();
		}
		
		return sampleLotList;
	}
}
