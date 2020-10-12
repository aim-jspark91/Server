package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class SampleLotService extends CTORMService<SampleLot> {

	public static Log logger = LogFactory.getLog(SampleLotService.class);
	
	private final String historyEntity = "SampleLotHist";
	
	public List<SampleLot> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<SampleLot> result = super.select(condition, bindSet, SampleLot.class);
		
		return result;
	}
	
	public SampleLot selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SampleLot.class, isLock, keySet);
	}
	
	public SampleLot create(EventInfo eventInfo, SampleLot dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SampleLot dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SampleLot modify(EventInfo eventInfo, SampleLot dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	/**
	 * insertSampleLot
	 * @author hykim
	 * @since 2014.07.31
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public void insertSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, 
								String processOperationName, String machineName, String toProcessOperationName, 
								String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount,
								String productSampleCount, String productSamplePosition, 
								String actualProductCount, String actualSamplePosition, String manualSampleFlag)
									throws CustomException 
	{
		try
		{
			//2018.02.22 dmlee arrange For EDO
			/*
			SampleLot sampleLotInfo = new SampleLot(lotName, factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName);
			sampleLotInfo.setLOTSAMPLEFLAG(lotSampleFlag);
			sampleLotInfo.setLOTSAMPLECOUNT(lotSampleCount);
			sampleLotInfo.setCURRENTLOTCOUNT(currentLotCount);
			sampleLotInfo.setTOTALLOTCOUNT(totalLotCount);
			sampleLotInfo.setPRODUCTSAMPLECOUNT(productSampleCount);
			sampleLotInfo.setPRODUCTSAMPLEPOSITION(productSamplePosition);
			sampleLotInfo.setACTUALPRODUCTCOUNT(actualProductCount);
			sampleLotInfo.setACTUALSAMPLEPOSITION(actualSamplePosition);
			sampleLotInfo.setMANUALSAMPLEFLAG(manualSampleFlag);
			sampleLotInfo.setLASTEVENTUSER(eventInfo.getEventUser());
			sampleLotInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
			
			ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLotInfo);
			*/
			//dmlee
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
		}
	}
	
	/**
	 * insertSampleLot
	 * @author hwlee89
	 * @since 2015.10.21
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public void insertSampleLot(EventInfo eventInfo, SampleLot sampleLotInfo)
									throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLotInfo);
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
		}
	}
	
	/**
	 * insertSampleLotForPostHold
	 * @author hwlee89
	 * @since 2016.01.27
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public void insertSampleLotForPostHold(EventInfo eventInfo, SampleLot sampleLotInfo)
									throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleLotService().create(eventInfo, sampleLotInfo);
		}
		catch(Exception e)
		{
			throw new CustomException("LOT-0015", sampleLotInfo.getLotName());
		}
	}
	
	public void deleteSampleLot(String lotName, String factoryName, String productSpecName, String processFlowName, 
			String processOperationName, String machineName, String toProcessOperationName)
				throws CustomException 
	{
		try
		{
			//2018.02.22 dmlee arrange For EDO
			/*
			SampleLot sampleInfo = new SampleLot(lotName, factoryName, productSpecName, processFlowName, "", "", toProcessOperationName);
			
			ExtendedObjectProxy.getSampleLotService().delete(sampleInfo);
			*/
			//dmlee
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}
	
	public void deleteSampleLot(SampleLot sampleInfo)
				throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleLotService().delete(sampleInfo);
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}

	/**
	 * setSamplingData
	 * 160611 by swcho : enhanced readability
	 * counting-duplication check-generate sampling
	 * @author hykim
	 * @since 2014.07.31
	 * @param EventInfo
	 * @param Lot
	 * @return 
	 * @throws CustomException
	 */
	public SampleLot setSamplingData(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy)
		throws CustomException
	{
		//counting avobe all
		SampleLotCount countInfo = ExtendedObjectProxy.getSampleLotService().calculateSamplingCount(eventInfo, lotData, samplePolicy);
		
		//validate sampling reservation
		if (ExtendedObjectProxy.getSampleLotService().isDuplicated(lotData, samplePolicy))
		{
			logger.info("Sampling already reserved");
			return null;
		}
		else if (countInfo.getCurrentCount() != 1)
		{
			logger.info("No first Lot in sampling rule");
			return null;
		}
		
		//reserve sampling block
		SampleLot sampleLot;
		try
		{
			String sampleFlag = "Y";
			
			// parcing the PRODUCTSAMPLEPOSITION
			String productSamplePositions = (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION");
			
			//Random Sampling Position by hwlee89
			if(productSamplePositions.equals("Random"))
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				
				if(StringUtil.equals(lotData.getReworkState(), "InRework"))
				{
					for(int i=0;i<productList.size();i++)
					{
						if( !StringUtil.equals(productList.get(i).getProductGrade(), "R") )
						{
							productList.remove(i);
							i--;
						}
					}
				}
				
				if( productList.size() < Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")) )
				{
					productSamplePositions = "All";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for(int i=0;i<Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() +",";
						temp += position;
					}
					productSamplePositions = temp.substring( 0, (temp.length() - 1) );
				}
			}
			
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			if(StringUtil.equals(productSamplePositions, "All") || StringUtil.equals(productSamplePositions, "all"))
			{
				productSamplePositions = 
					CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();
			
			for(String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position AND PRODUCTSTATE NOT IN (:Scrapped, :Consumed)";
				Map bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);
				bindMap.put("Scrapped", GenericServiceProxy.getConstantMap().Prod_Scrapped);
				bindMap.put("Consumed", GenericServiceProxy.getConstantMap().Prod_Consumed);
				
				List<Map<String, Object>> sampleProductResult = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sampleProductResult.size() > 0) //Exist the Product for productSamplePosition
				{
					//set SamplingProduct Data(CT_SAMPLEPRODUCT)					
					ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
							(String)sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(), 
							lotData.getFactoryName(), lotData.getProductSpecName(), 
							lotData.getProcessFlowName(), lotData.getProcessOperationName(), 
							(String)samplePolicy.get("MACHINENAME"), (String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag, 
							(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
							productSamplePosition, "", (String)samplePolicy.get("MACHINERECIPENAME"));
					
					actualSamplePositionList.add(productSamplePosition);
				}
				else //No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
				{
					List<Product> backupProductList = 
						ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
					
					Collections.shuffle(backupProductList); //Random For No Exist Product
					
					for(Product backupProduct : backupProductList)
					{
						if(!exceptBackpPositionList.contains(CommonUtil.StringValueOf(backupProduct.getPosition()))) // find new Product
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
									backupProduct.getKey().getProductName(), lotData.getKey().getLotName(), 
									lotData.getFactoryName(), lotData.getProductSpecName(), 
									lotData.getProcessFlowName(), lotData.getProcessOperationName(), 
									(String)samplePolicy.get("MACHINENAME"), (String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag, 
									(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
									CommonUtil.StringValueOf(backupProduct.getPosition()), "", (String)samplePolicy.get("MACHINERECIPENAME"));
							
							//except for new Product(backupProduct)
							exceptBackpPositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							actualSamplePositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							
							break;
						}
					}
				}
			}
			
			//2018.02.22 dmlee : arrange For EDO
			/* 
			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo,
					lotData.getKey().getLotName(), lotData.getFactoryName(), 
					lotData.getProductSpecName(), lotData.getProcessFlowName(), 
					lotData.getProcessOperationName(), (String)samplePolicy.get("MACHINENAME"), 
					(String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag,
					countInfo.getSampleCount(),
					countInfo.getCurrentCount(), countInfo.getTotalCount(), 
					(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), 
					(String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
					String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "");
			
			sampleLot = ExtendedObjectProxy.getSampleLotService().selectByKey(true,
					new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), 
					lotData.getProcessFlowName(), lotData.getProcessOperationName(), (String)samplePolicy.get("MACHINENAME"), 
					(String)samplePolicy.get("TOPROCESSOPERATIONNAME")});
			*/
			//dmlee
			 		
			 
			
			
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage());
			sampleLot = null;
		}
		
		//2018.02.22 dmlee : arrange For EDO
		//return sampleLot;
		return null;
	}
	
	/**
	 * setSamplingData
	 * 160815 by hwlee89 : Add
	 * counting-duplication check-generate sampling
	 * @author hykim
	 * @since 2014.07.31
	 * @param EventInfo
	 * @param Lot
	 * @return 
	 * @throws CustomException
	 */
	public SampleLot setSamplingDataForOperation(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy)
		throws CustomException
	{
		//counting avobe all
		SampleLotCount countInfo = ExtendedObjectProxy.getSampleLotService().calculateSamplingCount(eventInfo, lotData, samplePolicy);
		
		//validate sampling reservation
		if (ExtendedObjectProxy.getSampleLotService().isDuplicated(lotData, samplePolicy))
		{
			logger.info("Sampling already reserved");
			return null;
		}
//		else if (!countInfo.getCURRENTLOTCOUNT().equals("1"))
//		{
//			logger.info("No first Lot in sampling rule");
//			return null;
//		}
		
		//reserve sampling block
		SampleLot sampleLot;
		try
		{
			String sampleFlag = "Y";
			
			// parcing the PRODUCTSAMPLEPOSITION
			String productSamplePositions = (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION");
			
			//Random Sampling Position by hwlee89
			if(productSamplePositions.equals("Random"))
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				
				if( productList.size() < Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")) )
				{
					productSamplePositions = "All";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for(int i=0;i<Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() +",";
						temp += position;
					}
					productSamplePositions = temp.substring( 0, (temp.length() - 1) );
				}
			}
			
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			if(StringUtil.equals(productSamplePositions, "All") || StringUtil.equals(productSamplePositions, "all"))
			{
				productSamplePositions = 
					CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();
			
			for(String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position ";
				Map bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);
				
				List<Map<String, Object>> sampleProductResult = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sampleProductResult.size() > 0) //Exist the Product for productSamplePosition
				{
					//set SamplingProduct Data(CT_SAMPLEPRODUCT)					
					ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
							(String)sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(), 
							lotData.getFactoryName(), lotData.getProductSpecName(), 
							lotData.getProcessFlowName(), (String)samplePolicy.get("PROCESSOPERATIONNAME"), 
							(String)samplePolicy.get("MACHINENAME"), (String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag, 
							(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
							productSamplePosition, "", (String)samplePolicy.get("MACHINERECIPENAME"));
					
					actualSamplePositionList.add(productSamplePosition);
				}
				else //No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
				{
					List<Product> backupProductList = 
						ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
					
					Collections.shuffle(backupProductList); //Random For No Exist Product
					
					for(Product backupProduct : backupProductList)
					{
						if(!exceptBackpPositionList.contains(CommonUtil.StringValueOf(backupProduct.getPosition()))) // find new Product
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
									backupProduct.getKey().getProductName(), lotData.getKey().getLotName(), 
									lotData.getFactoryName(), lotData.getProductSpecName(), 
									lotData.getProcessFlowName(), (String)samplePolicy.get("PROCESSOPERATIONNAME"), 
									(String)samplePolicy.get("MACHINENAME"), (String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag, 
									(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
									CommonUtil.StringValueOf(backupProduct.getPosition()), "", (String)samplePolicy.get("MACHINERECIPENAME"));
							
							//except for new Product(backupProduct)
							exceptBackpPositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							actualSamplePositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							
							break;
						}
					}
				}
			}
			
			//2018.02.22 dmlee : arrange For EDO
			/*
			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo,
					lotData.getKey().getLotName(), lotData.getFactoryName(), 
					lotData.getProductSpecName(), lotData.getProcessFlowName(), 
					(String)samplePolicy.get("PROCESSOPERATIONNAME"), (String)samplePolicy.get("MACHINENAME"), 
					(String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag,
					countInfo.getSampleCount(),
					countInfo.getCurrentCount(), countInfo.getTotalCount(), 
					(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), 
					(String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
					String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "");
			
			sampleLot = ExtendedObjectProxy.getSampleLotService().selectByKey(true,
					new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), 
					lotData.getProcessFlowName(), (String)samplePolicy.get("PROCESSOPERATIONNAME"), (String)samplePolicy.get("MACHINENAME"), 
					(String)samplePolicy.get("TOPROCESSOPERATIONNAME")});
			*/
			//dmlee
			
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage());
			sampleLot = null;
		}
		
		//2018.02.22 dmlee : arrange For EDO
		//return sampleLot;
		//dmlee
		return null;
	}
	
	
	/**
	 * 150227 by swcho : modified
	 * @author hykim
	 * @since 2014.11.26
	 * @param productData
	 * @param sampleLot
	 * @return
	 * @throws CustomException
	 */
	public String getSamplingFlag( Product productData, SampleLot sampleLot )  
	 throws CustomException
	{  
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		
		if(sampleLot != null) 
		{
			// Get PRODUCTSAMPLEPOSITION
			String productSamplePositions = sampleLot.getActualSamplePosition();
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			for(String productSamplePosition : productSamplePositionList)
			{
				int k = Integer.valueOf(productSamplePosition);
				
				if(k == productData.getPosition())
				{
					samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
					break;
				}
			}
			
			//if ( productData.getUdfs().get("PROCESSINGINFO").toString().equals("F"))  
			//{
			//	samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
			//}
		}
	
		//just determine sampling
		
		return samplingFlag;
	}
	
	/**
	 * deleteSamplingDataForRework
	 * @author jhyeom
	 * @since 2015.08.21
	 * @param EventInfo
	 * @param Lot
	 * @return 
	 * @throws CustomException
	 */
	public void deleteSamplingDataForRework(Lot reworkLotData, List<Element> productList)
	throws CustomException
	{
		if(StringUtil.equals(reworkLotData.getReworkState(), "InRework"))
		{
			try
			{
				/*
				List<Map<String, Object>> sampleLotList = MESLotServiceProxy.getLotServiceUtil().getSampleLotList(
						reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(), reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName());
				*/
				
				String condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? ";
				Object[] bindSet = new Object[]{reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(), reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName()}; 
				List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
				
				if(sampleLotList == null)
					return ;
			
				if(sampleLotList != null)
				{
					for (int i = 0; i < sampleLotList.size(); i++) 
					{
						//String toProcessOper = (String)sampleLotList.get(i).getTOPROCESSOPERATIONNAME();
						
						/*
						ExtendedObjectProxy.getSampleLotService().deleteSampleLot(
								reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(), 
								reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName(), 
								"", "NA", toProcessOper);
						*/
						/*
						SampleLot sampleInfo = new SampleLot();
						sampleInfo.setLOTNAME(reworkLotData.getKey().getLotName());
						sampleInfo.setFACTORYNAME(reworkLotData.getFactoryName());
						sampleInfo.setPRODUCTSPECNAME(reworkLotData.getProductSpecName());
						sampleInfo.setPROCESSFLOWNAME(reworkLotData.getProcessFlowName());
						//sampleInfo.setPROCESSOPERATIONNAME("");
						//sampleInfo.setMACHINENAME("NA");
						sampleInfo.setTOPROCESSOPERATIONNAME(toProcessOper);
						*/
						ExtendedObjectProxy.getSampleLotService().deleteSampleLot(sampleLotList.get(i));
			
						/*
						List<Map<String, Object>> sampleProductList = new ArrayList<Map<String, Object>>();
				
						sampleProductList = MESLotServiceProxy.getLotServiceUtil().getSampleProductData("", reworkLotData.getKey().getLotName(),
								reworkLotData.getFactoryName(), reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName(),
								"", "", toProcessOper);
						*/
						/*
						condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? ";
						bindSet = new Object[]{reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(),
								reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName()};
						
						List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
							
						if(sampleProductList!=null)
						{	*/						
							/*
							ExtendedObjectProxy.getSampleProductService().deleteSampleProduct("",
									reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(), 
									reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName(), 
									"", "", toProcessOper);
							*/
							/*
							SampleProduct sampleProductInfo = new SampleProduct();
							sampleProductInfo.setPRODUCTNAME("");
							sampleProductInfo.setLOTNAME(reworkLotData.getKey().getLotName());
							sampleProductInfo.setFACTORYNAME(reworkLotData.getFactoryName());
							sampleProductInfo.setPRODUCTSPECNAME(reworkLotData.getProductSpecName());
							sampleProductInfo.setPROCESSFLOWNAME(reworkLotData.getProcessFlowName());
							sampleProductInfo.setPROCESSOPERATIONNAME("");
							sampleProductInfo.setMACHINENAME("");
							sampleProductInfo.setTOPROCESSOPERATIONNAME(toProcessOper);
							*/
							
							//ExtendedObjectProxy.getSampleProductService().deleteSampleProduct(sampleProductInfo);
						/*}*/
					}
					
					List<SampleProduct> sampleProductList = null;
					try
					{
						condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? ";
						bindSet = new Object[]{reworkLotData.getKey().getLotName(), reworkLotData.getFactoryName(),
								reworkLotData.getProductSpecName(), reworkLotData.getProcessFlowName()};
						
						sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
					}
					catch(Exception ex)
					{
						
					}
					
					if(sampleProductList != null)
					{
						for(int i=0;i<sampleProductList.size();i++)
						{
							ExtendedObjectProxy.getSampleProductService().deleteSampleProduct(sampleProductList.get(i));
						}
					}
					
				}
			} 
			catch (Exception ex)
			{ 
				logger.error(ex);
				logger.warn("Lot[%s] deleteSamplingDataForRework Failed");
			}
		}
	}
	
	/**
	 * 150205 by hykim
	 * deleteForceSampling
	 * @param eventInfo
	 * @param beforeTrackOutLot
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public void deleteForceSampling(EventInfo eventInfo, 
			String lotName, String factoryName, String productSpecName, String processFlowName)
		throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("DeleteSampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		
		String condition = "lotname = ? AND factoryname = ? AND productSpecName = ? AND processflowname = ? ";
		Object[] bindSet = new Object[]{ lotName, factoryName, productSpecName, processFlowName };
		
		List<SampleLot> sampleLotList =  ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		
		if(sampleLotList == null)
			return ;
		
		//delete FutureSkip
		for(SampleLot sampleLotM : sampleLotList)
		{
			//2018.02.22 dmlee : arrange For EDO
			/*
			ExtendedObjectProxy.getSampleLotService().deleteSampleLot(sampleLotM);
			
			condition = "lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? AND toprocessoperationname = ?";
			bindSet = new Object[]{sampleLotM.getLOTNAME(), sampleLotM.getFACTORYNAME(), sampleLotM.getPRODUCTSPECNAME(), sampleLotM.getPROCESSFLOWNAME(),
					sampleLotM.getTOPROCESSOPERATIONNAME()};
			List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
			
			if(sampleProductList != null)
			{			
				SampleProduct sampleProductInfo = new SampleProduct();
				sampleProductInfo.setPRODUCTNAME("");
				sampleProductInfo.setLOTNAME(sampleLotM.getLOTNAME());
				sampleProductInfo.setFACTORYNAME(sampleLotM.getFACTORYNAME());
				sampleProductInfo.setPRODUCTSPECNAME(sampleLotM.getPRODUCTSPECNAME());
				sampleProductInfo.setPROCESSFLOWNAME(sampleLotM.getPROCESSFLOWNAME());
				sampleProductInfo.setPROCESSOPERATIONNAME(sampleLotM.getPROCESSOPERATIONNAME());
				sampleProductInfo.setMACHINENAME(sampleLotM.getMACHINENAME());
				sampleProductInfo.setTOPROCESSOPERATIONNAME(sampleLotM.getTOPROCESSOPERATIONNAME());
				
				ExtendedObjectProxy.getSampleProductService().delete(sampleProductInfo);
			}
			*/
			//dmlee
		}

		return;
	}
	
	/**
	 * @author hwlee89
	 * @since 2016.02.02
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public String getSamplingFlag( Product productData, List<SampleProduct> productSampleList )  
	 throws CustomException
	{  
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		String productSamplePositions = "";
		
		/* 2018.02.22 dmlee : arrange For EDO
		if(productSampleList != null)
		{
			for(SampleProduct sampleProduct : productSampleList)
			{
				productSamplePositions += sampleProduct.getACTUALSAMPLEPOSITION() + ",";
			}
			productSamplePositions = productSamplePositions.substring( 0, (productSamplePositions.length() - 1) );
		}
		//dmlee */
		
		if(!productSamplePositions.isEmpty()) 
		{
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			for(String productSamplePosition : productSamplePositionList)
			{
				if (productSamplePosition.isEmpty()) continue;
				
				int k = Integer.valueOf(productSamplePosition);
				
				if(k == productData.getPosition())
				{
					samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
					break;
				}
			}
		}
		
		return samplingFlag;
	}
	
	/**
	 * 160312 by swcho : modified
	 * @author hwlee89
	 * @since 2016.02.02
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public String getSamplePosition( List<Product> productList, SampleLot sampleLot )  
	 throws CustomException
	{  
		String samplePostions = "";
		
		/* 2018.02.22 dmlee : arrange For EDO
		long productSampleCount = sampleLot.getSAMPLECOUNT();
		String productSamplePositions = sampleLot.getSamplePosition();
		
		int samplingCount = 0;
		
		try
		{
			samplingCount = (int)productSampleCount;
		}
		catch (Exception ex)
		{
			samplingCount = Integer.valueOf(sampleLot.getACTUALPRODUCTCOUNT());
		}
		
		//All에 대한 부분이 추후 수정이 필요할 수 있다.
		if(productList.size() < samplingCount)
		{
			for( int i = 0; i < productList.size() ; i++ )
			{
				samplePostions = samplePostions + productList.get(i).getPosition();
			}
		}
		else
		{
			if(productSamplePositions.equals("Random"))
			{
				Collections.shuffle(productList);
				String temp = "";
				for(int i=0;i<samplingCount; i++)
				{
					String position = productList.get(i).getPosition() +",";
					temp = temp + position;
				}
				samplePostions = temp.substring( 0, (temp.length() - 1) );
			}
			else
			{
				samplePostions = sampleLot.getACTUALSAMPLEPOSITION();
			}
		}
		dmlee */ 
		
		return samplePostions;
	}
	
	/**
	 * 160312 by swcho : modified
	 * @author hwlee89
	 * @since 2016.02.02
	 * @param 
	 * @param 
	 * @return
	 * @throws CustomException
	 */
	public List<SampleProduct> getActualSamplePosition(String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName )  
	 throws CustomException
	{  
		String actualSamplePostions = "";
		
		/* 2018.02.22 dmlee : arrange For EDO
		//Get ManualSampleLot
		List<SampleLot> manualSampleLotList = null;
		try{
			String condition = " lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? "
                    + " AND toprocessoperationname = ? AND manualsampleflag = 'Y' ";
			Object[] bindSet = new Object[]{lotName, factoryName, productSpecName, processFlowName, processOperationName};
		     
		     manualSampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			logger.debug("Not Found SamplingData");
		}
		
		//Get AutoSampleLot
		List<SampleLot> autoSampleLotList = null;
		try{
			String condition = " lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? "
                    + " AND toprocessoperationname = ? AND manualsampleflag is null";
		     Object[] bindSet = new Object[]{lotName, factoryName, productSpecName, processFlowName, processOperationName};
		     
		     autoSampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch(Exception ex)
		{
			logger.debug("Not Found SamplingData");
		}
		
		List<SampleProduct> sampleProductList = null;
		//Get manual SampleProductList
		if(manualSampleLotList != null)
		{
			try
			{
				String condition = " lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? "
	                    + "AND toprocessoperationname = ? AND manualsampleflag = 'Y' ";
			     Object[] bindSet = new Object[]{manualSampleLotList.get(0).getLOTNAME(), manualSampleLotList.get(0).getFACTORYNAME(),
			    		 manualSampleLotList.get(0).getPRODUCTSPECNAME(), manualSampleLotList.get(0).getPROCESSFLOWNAME(), manualSampleLotList.get(0).getTOPROCESSOPERATIONNAME()};
			     
			     sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
				logger.debug("Not Found SampleProductData");
			}
			
		}
		//Get Auto SampleProductList
		if((manualSampleLotList == null) && (autoSampleLotList != null))
		{
			try
			{
				String condition = " lotname = ? AND factoryname = ? AND productspecname = ? AND processflowname = ? "
	                    + "AND toprocessoperationname = ? AND manualsampleflag is null";
			     Object[] bindSet = new Object[]{autoSampleLotList.get(0).getLOTNAME(), autoSampleLotList.get(0).getFACTORYNAME(),
			    		 autoSampleLotList.get(0).getPRODUCTSPECNAME(), autoSampleLotList.get(0).getPROCESSFLOWNAME(), autoSampleLotList.get(0).getTOPROCESSOPERATIONNAME()};
			     
			     sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
			}
			catch(Exception ex)
			{
				logger.debug("Not Found SampleProductData");
			}
		}
		
//		if(sampleProductList != null)
//		{
//			for(SampleProduct sampleProduct : sampleProductList)
//			{
//				actualSamplePostions += sampleProduct.getACTUALSAMPLEPOSITION() + ",";
//			}
//			actualSamplePostions = actualSamplePostions.substring( 0, (actualSamplePostions.length() - 1) );
//		}
 		
 		return sampleProductList;
 
 		dmlee */ 
		
		return null;
	}
	
	/**
	 * sampling rule counting
	 * @since 2016-06-10
	 * @author swcho
	 * @param eventInfo
	 * @param lotData
	 * @param samplePolicy
	 * @return
	 * @throws CustomException
	 */
	public SampleLotCount calculateSamplingCount(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		SampleLotCount countInfo;
		
		String fromFlowName = CommonUtil.getValue(samplePolicy, "PROCESSFLOWNAME");
		String fromOperationName = CommonUtil.getValue(samplePolicy, "PROCESSOPERATIONNAME");
		String fromMachineName = CommonUtil.getValue(samplePolicy, "MACHINENAME");
		String toFlowName = CommonUtil.getValue(samplePolicy, "PROCESSFLOWNAME");
		String toOperationName = CommonUtil.getValue(samplePolicy, "TOPROCESSOPERATIONNAME");
		String lotSamplingCount = CommonUtil.getValue(samplePolicy, "LOTSAMPLINGCOUNT");
		
		//find existing sampling count by rule
		try
		{
			countInfo = ExtendedObjectProxy.getSampleLotCountService().selectByKey(true,
								new Object[]{lotData.getFactoryName(), lotData.getProductSpecName(),
											 fromFlowName, fromOperationName, fromMachineName, toOperationName});
		}
		catch (Exception ex)
		{
			logger.debug("Not found sampling count");
			countInfo = null;
		}
		
		//generate base count if not exists
		if(countInfo == null)
		{	
			eventInfo.setEventName("Create");
			
			//2018.02.22 dmlee : arrange For EDO
			/*
			countInfo = ExtendedObjectProxy.getSampleLotCountService().insertSampleLotCount(eventInfo, lotData.getFactoryName(), lotData.getProductSpecName(),
																							fromFlowName, fromOperationName, fromMachineName, toOperationName,
																							lotSamplingCount, "0", "0");
			*/
			//dmlee
		}
		
		double lotSampleCount = 0;
		double currentLotCount = 0;
		double totalLotCount = 0;
		
		try
		{
			//lotSampleCount = Double.parseDouble(countInfo.getLOTSAMPLECOUNT());
			lotSampleCount = Double.parseDouble(lotSamplingCount);
			currentLotCount = countInfo.getCurrentCount();
			totalLotCount = countInfo.getTotalCount();
			
			if(currentLotCount % lotSampleCount == 0)
			{
				currentLotCount = 1;
				totalLotCount++;
			}
			else
			{
				currentLotCount = currentLotCount + 1;
				totalLotCount++;
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage());
			throw new CustomException("SYS-9999", "Sampling", "Sampling counting failed");
		}
		
		eventInfo.setEventName("Increase");
		
		//2018.02.22 dmlee : arrange For EDO
		/*
		countInfo = ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountData(eventInfo, countInfo, 
				 CommonUtil.StringValueOf(lotSampleCount), CommonUtil.StringValueOf(currentLotCount), CommonUtil.StringValueOf(totalLotCount));
		*/
		//dmlee
		
		return countInfo;
	}
	
	/**
	 * 
	 * @since 2016-06-13
	 * @author swcho
	 * @param lotData
	 * @param samplePolicy
	 * @return
	 */
	public boolean isDuplicated(Lot lotData, Map<String, Object> samplePolicy)
	{
		try
		{
			String condition = " lotName = ? AND factoryName = ? AND processFlowName = ? AND toProcessOperationName = ? and lotSampleFlag = ?";
			
			Object[] bindSet = new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), CommonUtil.getValue(samplePolicy, "PROCESSFLOWNAME"), CommonUtil.getValue(samplePolicy, "TOPROCESSOPERATIONNAME"), "Y"};
			
			ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
			
			return true;
		}
		catch (Exception ex)
		{
			//not found means available
			logger.debug("No sampling data");
			return false;
		}
	}
	
	/**
	 * generate sampling reservation for multi-Lot in single CST
	 * @author swcho
	 * @since 2016.07.30
	 * @param eventInfo
	 * @param lotList
	 * @param samplePolicy
	 * @return
	 * @throws CustomException
	 */
	public SampleLot setSamplingDataV2(EventInfo eventInfo, List<Lot> lotList, Map<String, Object> samplePolicy) throws CustomException
	{
		if (lotList.size() < 1)
		{
			logger.error("No Lot to sample");
			return null;
		}
		
		Lot repLotData = lotList.get(0);
		
		//counting avobe all
		SampleLotCount countInfo = ExtendedObjectProxy.getSampleLotService().calculateSamplingCount(eventInfo, repLotData, samplePolicy);
		
		//validate ratio
		if (countInfo.getCurrentCount() != 1)
		{
			logger.error("No first Lot in sampling rule");
			return null;
		}
		
		String carrierName = repLotData.getCarrierName();
		
		if (carrierName.isEmpty())
		{
			logger.error("No CST to sample");
			return null;
		}
		
		//reserve sampling block
		SampleLot sampleLot;
		try
		{
			String sampleFlag = "Y";
			
			// parcing the PRODUCTSAMPLEPOSITION
			String productSamplePositions = (String)samplePolicy.get("PRODUCTSAMPLINGPOSITION");
			
			//Random Sampling Position by hwlee89
			if(productSamplePositions.equals("Random"))
			{
				String condition = " WHERE carrierName = ? " + " AND productState != ?" + " AND productState != ?" + " ORDER BY position";
				
				Object bindSet[] = new Object[3];
				bindSet[0] = carrierName;
				bindSet[1] = GenericServiceProxy.getConstantMap().Prod_Scrapped;
				bindSet[2] = GenericServiceProxy.getConstantMap().Prod_Consumed;
				
				List<Product> productList = ProductServiceProxy.getProductService().select(condition, bindSet);
				
				if( productList.size() < Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")) )
				{
					productSamplePositions = "All";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for(int i=0;i<Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() +",";
						temp += position;
					}
					productSamplePositions = temp.substring( 0, (temp.length() - 1) );
				}
			}
			
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			
			if(StringUtil.equals(productSamplePositions, "All") || StringUtil.equals(productSamplePositions, "all"))
			{
				productSamplePositions = 
					CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String)samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();
			
			for(String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME, LOTNAME, FACTORYNAME, PRODUCTSPECNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME FROM PRODUCT WHERE carrierName = :carrierName AND POSITION = :position ";
				Map bindMap = new HashMap<String, Object>();
				bindMap.put("carrierName", carrierName);
				bindMap.put("position", productSamplePosition);
				
				List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				String fromFactoryName = CommonUtil.getValue(samplePolicy, "FACTORYNAME");
				String fromFlowName = CommonUtil.getValue(samplePolicy, "PROCESSFLOWNAME");
				String fromOperationName = CommonUtil.getValue(samplePolicy, "PROCESSOPERATIONNAME");
				String fromMachineName = CommonUtil.getValue(samplePolicy, "MACHINENAME");
				String toOperationName = CommonUtil.getValue(samplePolicy, "TOPROCESSOPERATIONNAME");
				String reservedRecipeName = "";
				
				if(sampleProductResult.size() > 0) //Exist the Product for productSamplePosition
				{
					try
					{
						ListOrderedMap slotData = PolicyUtil.getSlotReservationTFOM(fromFactoryName, fromFlowName, fromOperationName, fromMachineName, toOperationName, productSamplePosition);
						reservedRecipeName = CommonUtil.getValue(slotData, "MACHINERECIPENAME");
					}
					catch (Exception ex)
					{
						logger.info("No slot reservation");
					}
					
					//set SamplingProduct Data(CT_SAMPLEPRODUCT)					
					ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
							(String)sampleProductResult.get(0).get("PRODUCTNAME"), CommonUtil.getValue(sampleProductResult.get(0), "LOTNAME"), 
							CommonUtil.getValue(sampleProductResult.get(0), "FACTORYNAME"), CommonUtil.getValue(sampleProductResult.get(0), "PRODUCTSPECNAME"), 
							CommonUtil.getValue(sampleProductResult.get(0), "PROCESSFLOWNAME"), CommonUtil.getValue(sampleProductResult.get(0), "PROCESSOPERATIONNAME"), 
							CommonUtil.getValue(samplePolicy, "MACHINENAME"), CommonUtil.getValue(samplePolicy, "TOPROCESSOPERATIONNAME"), sampleFlag, 
							CommonUtil.getValue(samplePolicy, "PRODUCTSAMPLINGCOUNT"), CommonUtil.getValue(samplePolicy, "PRODUCTSAMPLINGPOSITION"), 
							productSamplePosition, "",
							reservedRecipeName);
					
					actualSamplePositionList.add(productSamplePosition);
				}
				else //No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
				{
					String condition = " WHERE carrierName = ? " + " AND productState != ?" + " AND productState != ?" + " ORDER BY position";
					
					Object bindSet[] = new Object[3];
					bindSet[0] = carrierName;
					bindSet[1] = GenericServiceProxy.getConstantMap().Prod_Scrapped;
					bindSet[2] = GenericServiceProxy.getConstantMap().Prod_Consumed;
					
					List<Product> backupProductList = ProductServiceProxy.getProductService().select(condition, bindSet);
					
					Collections.shuffle(backupProductList); //Random For No Exist Product
					
					for(Product backupProduct : backupProductList)
					{
						if(!exceptBackpPositionList.contains(CommonUtil.StringValueOf(backupProduct.getPosition()))) // find new Product
						{
							try
							{
								ListOrderedMap slotData = PolicyUtil.getSlotReservationTFOM(fromFactoryName, fromFlowName, fromOperationName, fromMachineName, toOperationName, CommonUtil.StringValueOf(backupProduct.getPosition()));
								reservedRecipeName = CommonUtil.getValue(slotData, "MACHINERECIPENAME");
							}
							catch (Exception ex)
							{
								logger.info("No slot reservation");
							}
							
							ExtendedObjectProxy.getSampleProductService().insertSampleProductAddMachineRecipe(eventInfo,
									backupProduct.getKey().getProductName(), backupProduct.getLotName(), 
									backupProduct.getFactoryName(), backupProduct.getProductSpecName(), 
									backupProduct.getProcessFlowName(), backupProduct.getProcessOperationName(), 
									CommonUtil.getValue(samplePolicy, "MACHINENAME"), CommonUtil.getValue(samplePolicy, "TOPROCESSOPERATIONNAME"), sampleFlag, 
									CommonUtil.getValue(samplePolicy, "PRODUCTSAMPLINGCOUNT"), CommonUtil.getValue(samplePolicy, "PRODUCTSAMPLINGPOSITION"), 
									CommonUtil.StringValueOf(backupProduct.getPosition()), "",
									reservedRecipeName);
							
							//except for new Product(backupProduct)
							exceptBackpPositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							actualSamplePositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							
							break;
						}
					}
				}
			}
			
			//validate sampling reservation
			for (Lot lotData : lotList)
			{
				if (ExtendedObjectProxy.getSampleLotService().isDuplicated(lotData, samplePolicy))
				{
					logger.info("Sampling already reserved");
					//return null;
				}
				/* 2018.02.22 dmlee : arrange For EDO
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo,
						lotData.getKey().getLotName(), lotData.getFactoryName(), 
						lotData.getProductSpecName(), lotData.getProcessFlowName(), 
						lotData.getProcessOperationName(), lotData.getMachineName(), 
						(String)samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleFlag,
						countInfo.getSampleCount(),
						countInfo.getCurrentCount(), countInfo.getTotalCount(), 
						(String)samplePolicy.get("PRODUCTSAMPLINGCOUNT"), 
						(String)samplePolicy.get("PRODUCTSAMPLINGPOSITION"), 
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "");
				
				sampleLot = ExtendedObjectProxy.getSampleLotService().selectByKey(true,
						new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), 
						lotData.getProcessFlowName(), lotData.getProcessOperationName(), lotData.getMachineName(), 
						(String)samplePolicy.get("TOPROCESSOPERATIONNAME")});
				dmlee */
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage());
			sampleLot = null;
		}
		
		return null;
	}

	/**
	 * target step list by interval
	 * @author swcho
	 * @since 2016.09.13
	 * @param factoryName
	 * @param processFlowName
	 * @param fromOperationName
	 * @param toOperationName
	 * @return
	 * @throws CustomException
	 */
	public List<ListOrderedMap> getInspectionOperationList(String factoryName, String processFlowName, String fromOperationName, String toOperationName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
		.append("/* GetOperationList [00021] */                                                                                    ").append("\n")
		.append(" SELECT QL.FACTORYNAME,                                                                                           ").append("\n")
		.append("         QL.PROCESSOPERATIONNAME,                                                                                 ").append("\n")
		.append("         PO.DESCRIPTION PROCESSOPERATIONDESC,                                                                     ").append("\n")
		.append("         QL.PROCESSFLOWNAME,                                                                                      ").append("\n")
		.append("         PO.PROCESSOPERATIONTYPE,                                                                                 ").append("\n")
		.append("         PO.DETAILPROCESSOPERATIONTYPE,                                                                           ").append("\n")
		.append("         PO.PROCESSOPERATIONGROUP,                                                                                ").append("\n")
		.append("         QL.NODEID                                                                                                ").append("\n")
		.append("    FROM PROCESSOPERATIONSPEC PO,                                                                                 ").append("\n")
		.append("         ( SELECT LEVEL LV,                                                                                       ").append("\n")
		.append("                  FACTORYNAME,                                                                                    ").append("\n")
		.append("                  PROCESSOPERATIONNAME,                                                                           ").append("\n")
		.append("                  PROCESSFLOWNAME,                                                                                ").append("\n")
		.append("                  PROCESSFLOWVERSION,                                                                             ").append("\n")
		.append("                  NODEID                                                                                          ").append("\n")
		.append("         FROM (   SELECT                                                                                          ").append("\n")
		.append("                     N.FACTORYNAME,                                                                               ").append("\n")
		.append("                     N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,                                                       ").append("\n")
		.append("                     N.PROCESSFLOWNAME,                                                                           ").append("\n")
		.append("                     N.PROCESSFLOWVERSION,                                                                        ").append("\n")
		.append("                     N.NODEID,                                                                                    ").append("\n")
		.append("                     N.NODETYPE,                                                                                  ").append("\n")
		.append("                     A.FROMNODEID,                                                                                ").append("\n")
		.append("                     A.TONODEID,                                                                                  ").append("\n")
		.append("                     DECODE(N.NODEATTRIBUTE1, :PROCESSOPERATIONNAME, :Y, :N) FROMOPERATIONNAME                  ").append("\n")
		.append("                FROM ARC A,                                                                                       ").append("\n")
		.append("                     NODE N,                                                                                      ").append("\n")
		.append("                     PROCESSFLOW PF                                                                               ").append("\n")
		.append("               WHERE 1 = 1                                                                                        ").append("\n")
		.append("                 AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME                                                        ").append("\n")
		.append("                 AND N.FACTORYNAME = :FACTORYNAME                                                                 ").append("\n")
		.append("                 AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME                                                       ").append("\n")
		.append("                 AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION                                                 ").append("\n")
		.append("                 AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME                                                        ").append("\n")
		.append("                 AND N.FACTORYNAME = PF.FACTORYNAME                                                               ").append("\n")
		.append("                 AND A.FROMNODEID = N.NODEID)                                                                     ").append("\n")
		.append("          START WITH FROMOPERATIONNAME = :Y                                                                      ").append("\n")
		.append("          CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) QL                                                      ").append("\n")
		.append("   WHERE 1 = 1                                                                                                    ").append("\n")
		.append("     AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME                                                        ").append("\n")
		.append("     AND PO.FACTORYNAME = :FACTORYNAME                                                                            ").append("\n")
		.append("     AND PO.PROCESSOPERATIONTYPE = :INSPECTIONTYPE                                                                   ").append("\n")
		.append("     AND LV <= (SELECT MIN(LV)                                                                                    ").append("\n")
		.append("                FROM ( SELECT LEVEL LV,                                                                           ").append("\n")
		.append("                              PROCESSOPERATIONTYPE                                                                ").append("\n")
		.append("                         FROM (   SELECT                                                                          ").append("\n")
		.append("                                     N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,                                       ").append("\n")
		.append("                                     N.PROCESSFLOWNAME,                                                           ").append("\n")
		.append("                                     N.PROCESSFLOWVERSION,                                                        ").append("\n")
		.append("                                     N.NODEID,                                                                    ").append("\n")
		.append("                                     N.NODETYPE,                                                                  ").append("\n")
		.append("                                     A.FROMNODEID,                                                                ").append("\n")
		.append("                                     A.TONODEID, PS.PROCESSOPERATIONTYPE,                                         ").append("\n")
		.append("                                     DECODE(N.NODEATTRIBUTE1, :PROCESSOPERATIONNAME, :Y, :N) FROMOPERATIONNAME  ").append("\n")
		.append("                                FROM ARC A,                                                                       ").append("\n")
		.append("                                     NODE N,                                                                      ").append("\n")
		.append("                                     PROCESSFLOW PF,                                                              ").append("\n")
		.append("                                     PROCESSOPERATIONSPEC PS                                                      ").append("\n")
		.append("                               WHERE 1 = 1                                                                        ").append("\n")
		.append("                                 AND N.FACTORYNAME = PS.FACTORYNAME                                               ").append("\n")
		.append("                                 AND N.NODEATTRIBUTE1 = PS.PROCESSOPERATIONNAME                                   ").append("\n")
		.append("                                 AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME                                        ").append("\n")
		.append("                                 AND N.FACTORYNAME = :FACTORYNAME                                                 ").append("\n")
		.append("                                 AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME                                       ").append("\n")
		.append("                                 AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION                                 ").append("\n")
		.append("                                 AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME                                        ").append("\n")
		.append("                                 AND N.FACTORYNAME = PF.FACTORYNAME                                               ").append("\n")
		.append("                                 AND A.FROMNODEID = N.NODEID)                                                     ").append("\n")
		.append("                          START WITH FROMOPERATIONNAME = :Y                                                      ").append("\n")
		.append("                          CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID) QL                                      ").append("\n")
		.append("                WHERE 1=1                                                                                         ").append("\n")
		.append("                  AND LV > 1                                                                                      ").append("\n")
		.append("                  AND QL.PROCESSOPERATIONTYPE=:PRODUCTIONTYPE)                                                       ");
	
	HashMap<String, Object> bindMap = new HashMap<String, Object>();
	bindMap.put("FACTORYNAME", factoryName);
	bindMap.put("PROCESSFLOWNAME", processFlowName);
	bindMap.put("PROCESSOPERATIONNAME", fromOperationName);
	bindMap.put("Y", "Y");
	bindMap.put("N", "N");
	bindMap.put("PRODUCTIONTYPE", GenericServiceProxy.getConstantMap().Pos_Production);
	bindMap.put("INSPECTIONTYPE", GenericServiceProxy.getConstantMap().Pos_Inspection);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "SampleLotService", fe.getMessage());
		}
	}
	

	/**
	 * setSamplingData
	 * @author hykim
	 * @since 2014.07.31
	 * @param EventInfo
	 * @param Lot
	 * @return 
	 * @throws CustomException
	 */
	public void setReworkSamplingListData(EventInfo eventInfo, Lot lotData)
	throws CustomException
	{
		List<String> reworkFlowOperList = CommonUtil.getOperList(lotData.getFactoryName(), lotData.getProcessFlowName(), "00001");
		
		for(String ProcessOperation : reworkFlowOperList)
		{			
			// 1. set Sampling Lot Data
			// get SampleLotData(CT_SAMPLELOT)
			List<SampleLot> sampleLotList = null;
			SampleLot sampleLot = null;
			try
			{
				/*sampleLot = ExtendedObjectProxy.getSampleLotService().selectByKey(true,
						new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),  
						lotData.getProcessFlowName(), lotData.getProcessOperationName(), "NA", ProcessOperation});*/
				String condition = "lotName=? and factoryName=? and productspecname=? and processflowname=? and toprocessoperationname=?";
				Object [] bindSet = new Object[]{lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),ProcessOperation};
				
				sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
				sampleLot = sampleLotList.get(0);
			}
			catch(Exception ex)
			{
				
			}
						
			//if(sampleLot.size() < 1)
			if(sampleLot == null)
			{
				List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				List<Product> allUnScrappedProductSList = new ArrayList<Product>();
							
				for(Product unScrappedProductE : allUnScrappedProductList)
				{
					if( StringUtil.equals(unScrappedProductE.getProductGrade(), "R") )
					{
						allUnScrappedProductSList.add(unScrappedProductE);
					}
				}
						
				List<String> actualSamplePositionList = new ArrayList<String>();
						
				eventInfo.setEventName("Reserve");
					
				// insert SampleProduct & make actualSamplePositionList
				for(int i=0;i<allUnScrappedProductSList.size();i++)
				{
					try
					{
						Product productData = allUnScrappedProductSList.get(i);
									
						//Validation
						if(!productData.getLotName().equals(lotData.getKey().getLotName()))
							throw new CustomException("LOT-0014", lotData.getKey().getLotName(), productData.getLotName(), productData.getKey().getProductName());
							
						if(!allUnScrappedProductSList.contains(productData))
							throw new CustomException("PRODUCT-0018", productData.getKey().getProductName());

						//set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo,
								productData.getKey().getProductName(), lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), 
								lotData.getProcessFlowName() , lotData.getProcessOperationName(),"NA", 
								ProcessOperation, 
								"Y", "", "",String.valueOf(productData.getPosition()), "Y");
									
						actualSamplePositionList.add(String.valueOf(productData.getPosition()));
					}
					catch(Exception ex)
					{
						//log.error(ex.getMessage());
					}
				}
							
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo,
						lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), 
						lotData.getProcessFlowName() , lotData.getProcessOperationName() , "NA", 
						ProcessOperation, 
						"Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()), 
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y");
			}
			else 
			{ 
				//throw new CustomException("LOT-0018", sampleLot.get(0).get("LOTNAME"), sampleLot.get(0).get("TOPROCESSOPERATIONNAME"));
				//throw new CustomException("LOT-0018", sampleLot.getLOTNAME(), sampleLot.getTOPROCESSOPERATIONNAME());
			}
		  }				
	}
	
	public String exceptSGrade(String lotName , String logicalSlotMap)
	{
		String logicalSlotMapExceptSGrade = "";
		
		int index = 0;
		
		for(int i=0; i<logicalSlotMap.length(); i++)
		{
			index = i;

			if(String.valueOf(logicalSlotMap.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
			{
				try
				{
					List<Product> prdList = ProductServiceProxy.getProductService().select(" LOTNAME = ? AND POSITION = ? ", new Object[] {lotName, i+1});
					
					if(prdList.size()>0)
					{
						Product productData = prdList.get(0);
						if(StringUtil.equals(productData.getProductGrade(), "S"))
						{
							logicalSlotMapExceptSGrade += "X";
						}
						else
						{
							logicalSlotMapExceptSGrade += logicalSlotMap.charAt(index);
						}
					}
				}
				catch(Throwable e)
				{
					//Not Exist productList..
				}
			}
			else
			{
				logicalSlotMapExceptSGrade += "X";
			}
			
		}
		
		return logicalSlotMapExceptSGrade;
	}
	
	/**
	 * 
	 * @Name     getActualSamplePosition
	 * @since    2018. 11. 8.
	 * @author   hhlee
	 * @contents Get Actual Sample Positon
	 *           LotInfoDownLoadRequestNew, LotProcessEnd, LotProcessAbort, 
	 * @param eventInfo
	 * @param lotData
	 * @param logicalSlotMap
	 * @param isActualSamplePositionUpdate
	 * @return
	 */
	public String getActualSamplePosition(EventInfo eventInfo, Lot lotData, String logicalSlotMap, boolean isActualSamplePositionUpdate)
	{
		logger.info("getActualSamplePosition Start");
		
		String actualSamplePosition = StringUtil.EMPTY;
		String strSql = StringUtil.EMPTY;
		String originallogicalSlotMap = logicalSlotMap;
		String logicalSlotMapExceptSGrade = this.exceptSGrade(lotData.getKey().getLotName(), logicalSlotMap);

		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;

		if(count > 1)
		{
			Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-2]);

			String beforeFlowName = flowMap.get("PROCESSFLOWNAME");
			String beforeFlowVersion = flowMap.get("PROCESSFLOWVERSION");
			String beforeOperationName = flowMap.get("PROCESSOPERATIONNAME");
			String beforeOperationVersion = flowMap.get("PROCESSOPERATIONVERSION");
			
			try
			{
				
				strSql = strSql + " SELECT SAMPLEPOSITION, SAMPLEOUTHOLDFLAG, NOTE,  MANUALSAMPLEFLAG,TYPE \n";
				strSql = strSql + "   FROM (SELECT LA.SAMPLEPOSITION AS SAMPLEPOSITION, NVL(LA.SAMPLEOUTHOLDFLAG,:N) SAMPLEOUTHOLDFLAG, LA.LASTEVENTCOMMENT NOTE, :RESERVESAMPLING MANUALSAMPLEFLAG, :RESERVE TYPE \n";
				strSql = strSql + "           FROM CT_LOTACTION LA, LOT L \n";
				strSql = strSql + "         WHERE 1=1 \n";
				strSql = strSql + "           AND LA.FACTORYNAME = L.FACTORYNAME \n";
				strSql = strSql + "           AND LA.LOTNAME = L.LOTNAME \n";
				strSql = strSql + "           AND LA.LOTNAME = :LOTNAME \n";
				strSql = strSql + "           AND LA.PROCESSFLOWNAME = :FROMPROCESSFLOWNAME \n";
				strSql = strSql + "           AND LA.PROCESSFLOWVERSION = :FROMPROCESSFLOWVERSION \n";
				strSql = strSql + "           AND LA.PROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME \n";
				strSql = strSql + "           AND LA.PROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION \n";
				strSql = strSql + "           AND LA.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME \n";
				strSql = strSql + "           AND (LA.SAMPLEPROCESSFLOWVERSION = L.PROCESSFLOWVERSION OR LA.SAMPLEPROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "           AND LA.ACTIONSTATE <> :SAMPLESTATE \n";
				strSql = strSql + "           AND LA.ACTIONNAME = :ACTIONNAME \n";
				strSql = strSql + "           UNION \n";
				strSql = strSql + "         SELECT SL.ACTUALSAMPLEPOSITION AS SAMPLEPOSITION, NVL(SL.SAMPLEOUTHOLDFLAG,:N) SAMPLEOUTHOLDFLAG, :EMPTY NOTE, SL.MANUALSAMPLEFLAG, :AUTO TYPE \n";
				strSql = strSql + "           FROM CT_SAMPLELOT SL, LOT L \n";
				strSql = strSql + "          WHERE 1 = 1 \n";
				strSql = strSql + "            AND SL.LOTNAME = L.LOTNAME \n";
				strSql = strSql + "            AND SL.ECCODE = L.ECCODE \n";
				strSql = strSql + "            AND SL.PRODUCTSPECNAME = L.PRODUCTSPECNAME \n";
				strSql = strSql + "            AND SL.FACTORYNAME = L.FACTORYNAME \n";
				strSql = strSql + "            AND SL.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME \n";
				strSql = strSql + "            AND (SL.SAMPLEPROCESSFLOWVERSION = L.PROCESSFLOWVERSION OR SL.SAMPLEPROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "            AND L.LOTNAME = :LOTNAME \n";
				strSql = strSql + "            AND SL.PROCESSFLOWNAME = :FROMPROCESSFLOWNAME \n";
				strSql = strSql + "            AND (SL.PROCESSFLOWVERSION = :FROMPROCESSFLOWVERSION OR SL.PROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "            AND SL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME \n";
				strSql = strSql + "            AND (SL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR SL.FROMPROCESSOPERATIONVERSION = :STAR) \n";
				strSql = strSql + "            AND SL.SAMPLEFLAG = :SAMPLEFLAG \n";
				strSql = strSql + "            AND SL.SAMPLESTATE <> :SAMPLESTATE \n";
				strSql = strSql + "         UNION \n";
				strSql = strSql + "         SELECT CSL.ACTUALSAMPLEPOSITION AS SAMPLEPOSITION, NVL(CSL.SAMPLEOUTHOLDFLAG,:N) SAMPLEOUTHOLDFLAG, :EMPTY NOTE, :N MANUALSAMPLEFLAG, :CORRES TYPE \n";
				strSql = strSql + "           FROM CT_SAMPLELOTCOUNT SLC, CT_CORRESSAMPLELOT CSL, LOT L \n";
				strSql = strSql + "          WHERE 1 = 1 \n";
				strSql = strSql + "            AND SLC.FACTORYNAME = CSL.FACTORYNAME \n";
				strSql = strSql + "            AND SLC.PRODUCTSPECNAME = CSL.PRODUCTSPECNAME \n";
				strSql = strSql + "            AND SLC.ECCODE = CSL.ECCODE \n";
				strSql = strSql + "            AND SLC.PROCESSFLOWNAME = CSL.PROCESSFLOWNAME \n";
				strSql = strSql + "            AND SLC.PROCESSFLOWVERSION = CSL.PROCESSFLOWVERSION \n";
				strSql = strSql + "            AND SLC.CORRESSAMPLEPROCESSFLOWNAME = CSL.SAMPLEPROCESSFLOWNAME \n";
				strSql = strSql + "            AND (SLC.CORRESSAMPLEPROCESSFLOWVERSION = CSL.SAMPLEPROCESSFLOWVERSION OR SLC.CORRESSAMPLEPROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "            AND SLC.CORRESPROCESSOPERATIONNAME = CSL.FROMPROCESSOPERATIONNAME \n";
				strSql = strSql + "            AND (SLC.CORRESPROCESSOPERATIONVERSION = CSL.FROMPROCESSOPERATIONVERSION OR SLC.CORRESPROCESSOPERATIONVERSION = :STAR) \n";
				strSql = strSql + "            AND CSL.LOTNAME = L.LOTNAME \n";
				strSql = strSql + "            AND CSL.FACTORYNAME = L.FACTORYNAME \n";
				strSql = strSql + "            AND CSL.SAMPLEPROCESSFLOWNAME = L.PROCESSFLOWNAME \n";
				strSql = strSql + "            AND (CSL.SAMPLEPROCESSFLOWVERSION = L.PROCESSFLOWVERSION OR CSL.SAMPLEPROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "            AND L.LOTNAME = :LOTNAME \n";
				strSql = strSql + "            AND CSL.PROCESSFLOWNAME = :FROMPROCESSFLOWNAME \n";
				strSql = strSql + "            AND (CSL.PROCESSFLOWVERSION = :FROMPROCESSFLOWVERSION OR CSL.PROCESSFLOWVERSION = :STAR) \n";
				strSql = strSql + "            AND CSL.FROMPROCESSOPERATIONNAME = :FROMPROCESSOPERATIONNAME \n";
				strSql = strSql + "            AND (CSL.FROMPROCESSOPERATIONVERSION = :FROMPROCESSOPERATIONVERSION OR CSL.FROMPROCESSOPERATIONVERSION = :STAR) \n";
				strSql = strSql + "            AND CSL.SAMPLEFLAG = :SAMPLEFLAG \n";
				strSql = strSql + "            AND CSL.SAMPLESTATE <> :SAMPLESTATE) A \n";
				strSql = strSql + " ORDER BY DECODE (TYPE,  :AUTO, 0,  :CORRES, 1,  2) ASC \n";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTNAME", lotData.getKey().getLotName());
				bindMap.put("FROMPROCESSFLOWNAME", beforeFlowName);
				bindMap.put("FROMPROCESSFLOWVERSION", beforeFlowVersion);
				bindMap.put("FROMPROCESSOPERATIONNAME", beforeOperationName);
				bindMap.put("FROMPROCESSOPERATIONVERSION", beforeOperationVersion);
				bindMap.put("N", "N");
				bindMap.put("SAMPLEFLAG", "Y");
				bindMap.put("SAMPLESTATE", "Completed");
				bindMap.put("ACTIONNAME", "Sampling");
				bindMap.put("RESERVESAMPLING", "ReserveSampling");
				bindMap.put("STAR", "*");
				bindMap.put("EMPTY", "");
				bindMap.put("RESERVE", "RESERVE");
				bindMap.put("AUTO", "AUTO");
				bindMap.put("CORRES", "CORRES");

				List<Map<String, Object>> sampleLotData = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);

				if ( sampleLotData.size() > 0 )
				{
					String[] actualsamplepostion = sampleLotData.get(0).get("SAMPLEPOSITION").toString().trim().split(",");
					
					int index = 0;
					
					for(int i = 0; i < actualsamplepostion.length; i++ )
					{
						index = Integer.parseInt(actualsamplepostion[i]) - 1;

						if(!String.valueOf(logicalSlotMapExceptSGrade.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
						{
							actualsamplepostion[i] = getNewActualSamplePosition(index, logicalSlotMapExceptSGrade, actualsamplepostion);
						}

						/* 20181229, hhlee, add actualsamplepostion is empty ==>> */
		                if(StringUtil.isNotEmpty(actualsamplepostion[i].trim()))
		                {		                
    						if(StringUtil.isEmpty(actualSamplePosition))
    						{
    							actualSamplePosition = actualsamplepostion[i];
    						}
    						else
    						{
    							actualSamplePosition = actualSamplePosition + "," + actualsamplepostion[i];
    						}
		                }
						/* <<== 20181229, hhlee, add actualsamplepostion is empty */

					} 

					actualSamplePosition = actualSamplePositionSorting(actualSamplePosition); 
					
					/* 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter ==>> */
					if(isActualSamplePositionUpdate)
					{
					    this.getSampleLotInfo(lotData, actualSamplePosition, eventInfo);
					}
					
					/* <<== 20181108, hhlee, modify, add ActualSamplePositionUpdate parameter */
					
					//	                if(!originallogicalSlotMap.equals(actualSamplePosition))
					//	                {
					//	                    eventInfo.setEventName("ChangeActualSamplePosition");
					//	                    
					//	                    SampleLot sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] 
					//	                            {sampleLotData.get(0).get("LOTNAME").toString(), 
					//	                            sampleLotData.get(0).get("FACTORYNAME").toString(),
					//	                            sampleLotData.get(0).get("PRODUCTSPECNAME").toString(),
					//	                            sampleLotData.get(0).get("ECCODE").toString(),
					//	                            sampleLotData.get(0).get("PROCESSFLOWNAME").toString(),
					//	                            sampleLotData.get(0).get("PROCESSFLOWVERSION").toString(),
					//	                            sampleLotData.get(0).get("PROCESSOPERATIONNAME").toString(),
					//	                            sampleLotData.get(0).get("PROCESSOPERATIONVERSION").toString(),
					//	                            sampleLotData.get(0).get("MACHINENAME").toString(),
					//	                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWNAME").toString(),
					//	                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWVERSION").toString()
					//	                            });
					//	        
					//	                    //default spec info
					//	                    sampleLotInfo.setActualSamplePosition(actualSamplePosition);                   
					//	        
					//	                    //history trace
					//	                    sampleLotInfo.setLastEventName(eventInfo.getEventName());
					//	                    sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					//	                    sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
					//	                    sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
					//	                    sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
					//	        
					//	                    //ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
					//	                }
					
					//2019.01.25_hsryu_Mantis 0002598.
					for(int j=0; j<sampleLotData.size();j++)
					{
						if(StringUtils.equals((String)sampleLotData.get(j).get("TYPE"), "RESERVE"))
						{
							if(StringUtils.isNotEmpty((String)sampleLotData.get(j).get("NOTE")))
							{
								eventInfo.setEventComment((String)sampleLotData.get(j).get("NOTE"));
							}
						}
					}
				}
				else
				{
				}    
				
				logger.info("getActualSamplePosition End");
			}
			catch (Exception ex)
			{
				logger.warn("[getActualSamplePosition] Data Query Failed");;
			}
		}

		return actualSamplePosition;
	}
	
	
	public String getSamplePositionOfLotAction(Lot lotData, String sampleProcessFlowName, String moveOperationName, EventInfo eventInfo)
	{
		try
		{
			List<LotAction> sampleActionList = new ArrayList<LotAction>();

			String condition = " WHERE 1=1 AND lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
					+ " AND processOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
			Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					moveOperationName, lotData.getProcessOperationVersion(), sampleProcessFlowName, "00001", "Sampling", "Created" };

			sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

			if(sampleActionList.size()>0)
			{
				LotAction lotAction = new LotAction();
				lotAction = sampleActionList.get(0);

/*				lotAction.setActionState("Executed");
				lotAction.setLastEventName("Executed");
				lotAction.setLastEventTime(eventInfo.getEventTime());
				lotAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotAction.setLastEventUser(eventInfo.getEventUser());
				lotAction.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getLotActionService().modify(eventInfo, lotAction);*/

				return lotAction.getSamplePosition().toString();
			}
		}
		catch(Exception ex)
		{
			return "";
		}

		return "";
	}


	//  /**
//     * @Name     getActualSamplePosition
//     * @since    2018. 6. 16.
//     * @author   hhlee
//     * @contents 
//     * @param lotData
//     * @param logicalSlotMap
//     * @return
//     */
//    private String getActualSamplePosition(EventInfo eventInfo, Lot lotData, String logicalSlotMap)
//    {
//        String actualSamplePosition = StringUtil.EMPTY;
//        String strSql = StringUtil.EMPTY;
//        String originallogicalSlotMap = logicalSlotMap;
//        try
//        {
////          strSql = strSql + " SELECT SA.LOTNAME, SA.SAMPLEPROCESSFLOWNAME, SA.SAMPLEPROCESSFLOWVERSION,                        \n";
////          strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
////          strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
////          strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
////          strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
////          strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
////          strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
////          strSql = strSql + "   FROM CT_SAMPLELOTSTATE SA,                                                                     \n";
////          strSql = strSql + "        CT_SAMPLELOT SL                                                                           \n";
////          strSql = strSql + "  WHERE 1=1                                                                                       \n";
////          strSql = strSql + "    AND SA.LOTNAME = :LOTNAME                                                                     \n";
////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
////          strSql = strSql + "    AND SA.LOTNAME = SL.LOTNAME                                                                   \n";
////          strSql = strSql + "    AND SA.FACTORYNAME = SL.FACTORYNAME                                                           \n";
////          strSql = strSql + "    AND SA.PRODUCTSPECNAME = SL.PRODUCTSPECNAME                                                   \n";
////          strSql = strSql + "    AND SA.PROCESSFLOWNAME = SL.PROCESSFLOWNAME                                                   \n";
////          strSql = strSql + "    AND SA.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION                                             \n";
////          strSql = strSql + "    AND SA.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME                                         \n";
////          strSql = strSql + "    AND SA.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION                                   \n";
////          strSql = strSql + "    AND SA.MACHINENAME = SL.MACHINENAME                                                           \n";
////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME                                       \n";
////          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION                                 \n";
////          strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
//            
//            strSql = strSql + " SELECT SL.LOTNAME, SL.SAMPLEPROCESSFLOWNAME, SL.SAMPLEPROCESSFLOWVERSION,                        \n";
//            strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
//            strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
//            strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
//            strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
//            strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
//            strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
//            strSql = strSql + "   FROM CT_SAMPLELOT SL                                                                           \n";
//            strSql = strSql + "  WHERE 1=1                                                                                       \n";
//            strSql = strSql + "    AND SL.LOTNAME = :LOTNAME                                                                     \n";
//            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
//            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
//            strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
//            
//            Map<String, Object> bindMap = new HashMap<String, Object>();
//            bindMap.put("LOTNAME", lotData.getKey().getLotName());
//            bindMap.put("SAMPLEPROCESSFLOWNAME", lotData.getProcessFlowName());
//            bindMap.put("SAMPLEPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
//            bindMap.put("ECCODE", CommonUtil.getValue(lotData.getUdfs(), "ECCODE"));
//    
//            List<Map<String, Object>> sampleLotData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
//            
//            if ( sampleLotData.size() > 0 )
//            {                
//                String[] actualsamplepostion = sampleLotData.get(0).get("ACTUALSAMPLEPOSITION").toString().trim().split(",");
//                int index = 0;
//                for(int i = 0; i < actualsamplepostion.length; i++ )
//                {
//                    index = Integer.parseInt(actualsamplepostion[i]) - 1;
//                    
//                    if(!String.valueOf(logicalSlotMap.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
//                    {
//                        actualsamplepostion[i] = getNewActualSamplePosition(index, logicalSlotMap, actualsamplepostion);
//                    }
//                    
//                    if(StringUtil.isEmpty(actualSamplePosition))
//                    {
//                        actualSamplePosition = actualsamplepostion[i];
//                    }
//                    else
//                    {
//                        actualSamplePosition = actualSamplePosition + "," + actualsamplepostion[i];
//                    }
//                   
//                } 
//                
//                actualSamplePosition = actualSamplePositionSorting(actualSamplePosition);                
//                
//                if(!originallogicalSlotMap.equals(actualSamplePosition))
//                {
//                    eventInfo.setEventName("ChangeActualSamplePosition");
//                    
//                    SampleLot sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] 
//                            {sampleLotData.get(0).get("LOTNAME").toString(), 
//                            sampleLotData.get(0).get("FACTORYNAME").toString(),
//                            sampleLotData.get(0).get("PRODUCTSPECNAME").toString(),
//                            sampleLotData.get(0).get("ECCODE").toString(),
//                            sampleLotData.get(0).get("PROCESSFLOWNAME").toString(),
//                            sampleLotData.get(0).get("PROCESSFLOWVERSION").toString(),
//                            sampleLotData.get(0).get("PROCESSOPERATIONNAME").toString(),
//                            sampleLotData.get(0).get("PROCESSOPERATIONVERSION").toString(),
//                            sampleLotData.get(0).get("MACHINENAME").toString(),
//                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWNAME").toString(),
//                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWVERSION").toString()
//                            });
//        
//                    //default spec info
//                    sampleLotInfo.setActualSamplePosition(actualSamplePosition);                   
//        
//                    //history trace
//                    sampleLotInfo.setLastEventName(eventInfo.getEventName());
//                    sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
//                    sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
//                    sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
//                    sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
//        
//                    //ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
//                }
//            }
//            else
//            {
//            }    
//        }
//        catch (Exception ex)
//        {
//            eventLog.warn("[getActualSamplePosition] Data Query Failed");;
//        }
//        
//        return actualSamplePosition;
//    }        
    
	/**
     * @Name     setActualSamplePosition
     * @since    2018. 6. 16.
     * @author   hhlee
     * @contents 
     * @param lotData
     * @param logicalSlotMap
     * @return
     */
    public String setActualSamplePosition(EventInfo eventInfo, Lot lotData, String logicalSlotMap)
    {
        String actualSamplePosition = StringUtil.EMPTY;
        String strSql = StringUtil.EMPTY;
        String originallogicalSlotMap = logicalSlotMap;
        try
        {
//          strSql = strSql + " SELECT SA.LOTNAME, SA.SAMPLEPROCESSFLOWNAME, SA.SAMPLEPROCESSFLOWVERSION,                        \n";
//          strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
//          strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
//          strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
//          strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
//          strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
//          strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
//          strSql = strSql + "   FROM CT_SAMPLELOTSTATE SA,                                                                     \n";
//          strSql = strSql + "        CT_SAMPLELOT SL                                                                           \n";
//          strSql = strSql + "  WHERE 1=1                                                                                       \n";
//          strSql = strSql + "    AND SA.LOTNAME = :LOTNAME                                                                     \n";
//          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
//          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
//          strSql = strSql + "    AND SA.LOTNAME = SL.LOTNAME                                                                   \n";
//          strSql = strSql + "    AND SA.FACTORYNAME = SL.FACTORYNAME                                                           \n";
//          strSql = strSql + "    AND SA.PRODUCTSPECNAME = SL.PRODUCTSPECNAME                                                   \n";
//          strSql = strSql + "    AND SA.PROCESSFLOWNAME = SL.PROCESSFLOWNAME                                                   \n";
//          strSql = strSql + "    AND SA.PROCESSFLOWVERSION = SL.PROCESSFLOWVERSION                                             \n";
//          strSql = strSql + "    AND SA.PROCESSOPERATIONNAME = SL.PROCESSOPERATIONNAME                                         \n";
//          strSql = strSql + "    AND SA.PROCESSOPERATIONVERSION = SL.PROCESSOPERATIONVERSION                                   \n";
//          strSql = strSql + "    AND SA.MACHINENAME = SL.MACHINENAME                                                           \n";
//          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWNAME = SL.SAMPLEPROCESSFLOWNAME                                       \n";
//          strSql = strSql + "    AND SA.SAMPLEPROCESSFLOWVERSION = SL.SAMPLEPROCESSFLOWVERSION                                 \n";
//          strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
            
            strSql = strSql + " SELECT SL.LOTNAME, SL.SAMPLEPROCESSFLOWNAME, SL.SAMPLEPROCESSFLOWVERSION,                        \n";
            strSql = strSql + "        SL.FACTORYNAME, SL.PRODUCTSPECNAME, SL.ECCODE,                                            \n";
            strSql = strSql + "        SL.PROCESSFLOWNAME, SL.PROCESSFLOWVERSION, SL.PROCESSOPERATIONNAME,                       \n";
            strSql = strSql + "        SL.PROCESSOPERATIONVERSION, SL.MACHINENAME, SL.SAMPLEPROCESSFLOWNAME,                     \n";
            strSql = strSql + "        SL.SAMPLEPROCESSFLOWVERSION, SL.FROMPROCESSOPERATIONNAME, SL.FROMPROCESSOPERATIONVERSION, \n";
            strSql = strSql + "        SL.SAMPLECOUNT, SL.CURRENTCOUNT, SL.TOTALCOUNT,                                           \n";
            strSql = strSql + "        SL.SYSTEMSAMPLEPOSITION, SL.MANUALSAMPLEPOSITION, SL.ACTUALSAMPLEPOSITION                 \n";
            strSql = strSql + "   FROM CT_SAMPLELOT SL                                                                           \n";
            strSql = strSql + "  WHERE 1=1                                                                                       \n";
            strSql = strSql + "    AND SL.LOTNAME = :LOTNAME                                                                     \n";
            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWNAME= :SAMPLEPROCESSFLOWNAME                                          \n";
            strSql = strSql + "    AND SL.SAMPLEPROCESSFLOWVERSION = :SAMPLEPROCESSFLOWVERSION                                   \n";
            strSql = strSql + "    AND SL.ECCODE = :ECCODE                                                                       \n";
            
            Map<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("LOTNAME", lotData.getKey().getLotName());
            bindMap.put("SAMPLEPROCESSFLOWNAME", lotData.getProcessFlowName());
            bindMap.put("SAMPLEPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
            bindMap.put("ECCODE", CommonUtil.getValue(lotData.getUdfs(), "ECCODE"));
    
            List<Map<String, Object>> sampleLotData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
            
            if ( sampleLotData.size() > 0 )
            {                
                /* 20180731 , Add ==>> */
                String originalactualsamplepostion = sampleLotData.get(0).get("ACTUALSAMPLEPOSITION").toString();
                /* <<== 20180731 , Add */
                
                String[] actualsamplepostion = sampleLotData.get(0).get("ACTUALSAMPLEPOSITION").toString().trim().split(",");
                int index = 0;
                for(int i = 0; i < actualsamplepostion.length; i++ )
                {
                    index = Integer.parseInt(actualsamplepostion[i]) - 1;
                    
                    if(!String.valueOf(logicalSlotMap.charAt(index)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
                    {
                        actualsamplepostion[i] = getNewActualSamplePosition(index, logicalSlotMap, actualsamplepostion);
                    }
                    
                    if(StringUtil.isEmpty(actualSamplePosition))
                    {
                        actualSamplePosition = actualsamplepostion[i];
                    }
                    else
                    {
                        actualSamplePosition = actualSamplePosition + "," + actualsamplepostion[i];
                    }
                   
                } 
                
                actualSamplePosition = actualSamplePositionSorting(actualSamplePosition);                
                
                /* 20180731 , Change ==>> */
                //if(!originallogicalSlotMap.equals(actualSamplePosition))
                if(!originalactualsamplepostion.equals(actualSamplePosition))
                {
                    eventInfo.setEventName("ChangeActualSamplePosition");
                    
                    SampleLot sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] 
                            {sampleLotData.get(0).get("LOTNAME").toString(), 
                            sampleLotData.get(0).get("FACTORYNAME").toString(),
                            sampleLotData.get(0).get("PRODUCTSPECNAME").toString(),
                            sampleLotData.get(0).get("ECCODE").toString(),
                            sampleLotData.get(0).get("PROCESSFLOWNAME").toString(),
                            sampleLotData.get(0).get("PROCESSFLOWVERSION").toString(),
                            sampleLotData.get(0).get("PROCESSOPERATIONNAME").toString(),
                            sampleLotData.get(0).get("PROCESSOPERATIONVERSION").toString(),
                            sampleLotData.get(0).get("MACHINENAME").toString(),
                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWNAME").toString(),
                            sampleLotData.get(0).get("SAMPLEPROCESSFLOWVERSION").toString()
                            });
        
                    //default spec info
                    sampleLotInfo.setActualSamplePosition(actualSamplePosition);                   
        
                    //history trace
                    sampleLotInfo.setLastEventName(eventInfo.getEventName());
                    sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
                    sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
                    sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
                    sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
        
                    ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
                }
            }
            else
            {
            }    
        }
        catch (Exception ex)
        {
            logger.warn("[getActualSamplePosition] Data Query Failed");;
        }
        
        return actualSamplePosition;
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
    public String getNewActualSamplePosition(int indexcnt, String logicalslotmap , String[] actualsamplepostiondata)
    {
        String newpostion = StringUtil.EMPTY;
        boolean newposition = false;
        int position = 0;
        
        /* 20180825, Modify, ==>> */
        //for(int i = indexcnt + 1; i<logicalslotmap.length(); i++) 
        for(int i = 0; i<logicalslotmap.length(); i++) /* <<== 20180825, Modify, */        
        {
            position = i + 1;           
            if(String.valueOf(logicalslotmap.charAt(i)).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
            {
                newposition = false;
                newpostion = String.valueOf(position);
                for(int j = 0; j < actualsamplepostiondata.length; j++ )
                {
                	if(StringUtils.isNotEmpty(actualsamplepostiondata[j]))
                	{
                        if (position ==  Integer.parseInt(actualsamplepostiondata[j]))
                        {           
                            newposition = true;
                            break;
                        }
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
    public String actualSamplePositionSorting(String actualSamplePosition)
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

	public Lot getSampleLotInfo(Lot lotData, String actualSampleSlot, EventInfo eventInfo)
	{
		//2019.01.28_hsryu_Change eventComment. eventInfo.getEventCommnet -> eventInfo.getEventName. if not ReserveSampling, eventInfo.getEventCommnet is null.
	    /* 20181128, hhlee, EventTime Sync */
	    EventInfo actualEventInfo = EventInfoUtil.makeEventInfo("ChangeActualSampleSlot", eventInfo.getEventUser(), eventInfo.getEventName(), "", "");
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		SampleLot sampleLot = new SampleLot();
		boolean systemFlag = false;

		List<CorresSampleLot> corresSampleLotList = new ArrayList<CorresSampleLot>();
		CorresSampleLot corresSampleLot = new CorresSampleLot();
 
		String tempNodeStack = lotData.getNodeStack();
		String[] arrNodeStack = StringUtil.split(tempNodeStack, ".");
		int count = arrNodeStack.length;

		Map<String, String> flowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-1]);

		String flowName = flowMap.get("PROCESSFLOWNAME");
		String flowVersion = flowMap.get("PROCESSFLOWVERSION");
		String operationName = flowMap.get("PROCESSOPERATIONNAME");

		//boolean systemFlag = false;
		String realActualSlot = "";
		
		List<Product> pProductList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

		if(StringUtil.equals(MESLotServiceProxy.getLotServiceUtil().returnFlowType(lotData.getFactoryName(), flowName, flowVersion), "Sampling"))
		{
			if(count>1)
			{
				Map<String, String> beforeFlowMap = MESLotServiceProxy.getLotServiceUtil().getProcessFlowInfo(arrNodeStack[count-2]);

				String beforeFlowName = beforeFlowMap.get("PROCESSFLOWNAME");
				String beforeFlowVersion = beforeFlowMap.get("PROCESSFLOWVERSION");
				String beforeOperationName = beforeFlowMap.get("PROCESSOPERATIONNAME");
				String beforeOperationVersion = beforeFlowMap.get("PROCESSOPERATIONVERSION");

				//SystemSample Completing..
				try
				{
					try {
						sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and processFlowName = ? and processFlowVersion = ? and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) "
								+ "and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) ", 
								new Object[] {lotData.getKey().getLotName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
								beforeOperationName, beforeOperationVersion, "*",
								flowName, flowVersion, "*"});
					} catch (Exception e) {
						logger.info(e);
					}

					if ( sampleLotList != null && sampleLotList.size() > 0 )
					{
						sampleLot = sampleLotList.get(0);
						//systemFlag = true;
						
						if(!StringUtil.equals(sampleLot.getActualSamplePosition(), actualSampleSlot))
						{
							systemFlag = true;
							sampleLot.setActualSamplePosition(actualSampleSlot);
							/* 20181128, hhlee, EventTime Sync */
							sampleLot.setLastEventName(actualEventInfo.getEventName());
							//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
							//sampleLot.setLastEventUser(actualEventInfo.getEventUser());
							sampleLot.setLastEventComment(actualEventInfo.getEventComment());
							sampleLot.setLastEventTime(actualEventInfo.getEventTime());
							sampleLot.setLastEventTimekey(actualEventInfo.getEventTimeKey());
							sampleLot = ExtendedObjectProxy.getSampleLotService().modify(actualEventInfo, sampleLot);
						}
					}
				}
				catch (Throwable e)
				{
					logger.error("Update SampleLot Failed");
				}

				//corresSample Executing..
				try
				{
					corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? "
							+ "and processFlowName = ? and processFlowVersion = ? and fromProcessOperationName = ? and (fromProcessOperationVersion = ? or fromProcessOperationVersion = ?) and sampleProcessFlowName = ? and (sampleProcessFlowVersion = ? or sampleProcessFlowVersion = ?) ",
							new Object[] {lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"), beforeFlowName, beforeFlowVersion,
							beforeOperationName, beforeOperationVersion, "*", flowName, flowVersion, "*"});


					if(corresSampleLotList.size()>0)
					{
						corresSampleLot = corresSampleLotList.get(0);
						
						if(!StringUtil.equals(corresSampleLot.getActualSamplePosition(), actualSampleSlot))
						{
							systemFlag = true;
							corresSampleLot.setActualSamplePosition(actualSampleSlot);
							/* 20181128, hhlee, EventTime Sync */
							corresSampleLot.setLastEventName(actualEventInfo.getEventName());
							//2019.02.27_hsryu_Mantis 0002723. if SampleOutHold, remain EventUser . 
							//corresSampleLot.setLastEventUser(actualEventInfo.getEventUser());
							corresSampleLot.setLastEventComment(actualEventInfo.getEventComment());
							corresSampleLot.setLastEventTime(actualEventInfo.getEventTime());
							corresSampleLot.setLastEventTimekey(actualEventInfo.getEventTimeKey());
							corresSampleLot = ExtendedObjectProxy.getCorresSampleLotService().modify(actualEventInfo, corresSampleLot);
						}
					}
				}
				catch(Throwable e)
				{
					//log.info("not corresSample currently..");
				}
				
				if(!systemFlag)
				{
					//ReserveSample Completing..
					try
					{
						List<LotAction> sampleActionList = new ArrayList<LotAction>();

						String condition = "WHERE lotName = ? AND factoryName = ? AND processFlowName = ? AND processFlowVersion = ? AND processOperationName = ?"
										+ " AND processOperationVersion = ? AND sampleProcessFlowName = ? AND sampleProcessFlowVersion = ? AND actionName = ? AND actionState = ? ";
						Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), lotData.getFactoryName(), beforeFlowName, beforeFlowVersion,
								beforeOperationName, "00001", flowName, flowVersion, "Sampling", "Created" };

						sampleActionList = ExtendedObjectProxy.getLotActionService().select(condition, bindSet);

						if(sampleActionList.size() == 1)
						{
							LotAction lotAction = new LotAction();
							lotAction = sampleActionList.get(0);

							lotAction.setSamplePosition(actualSampleSlot);
							/* 20181128, hhlee, EventTime Sync */
							lotAction.setLastEventTime(actualEventInfo.getEventTime());
							lotAction.setLastEventTimeKey(actualEventInfo.getEventTimeKey());
							//2018.12.20_hsryu_Not Change EventUser.
							//lotAction.setLastEventUser(actualEventInfo.getEventUser());
							//2019.01.28_hsryu_Not Change EventComment. 
							//lotAction.setLastEventComment(actualEventInfo.getEventComment());
							ExtendedObjectProxy.getLotActionService().modify(actualEventInfo, lotAction);
						}
					}
					catch(Throwable e)
					{
						//log.info("not ReserveSampling currently..");
					}
				}
			}
		}
		return lotData;
	}
}
