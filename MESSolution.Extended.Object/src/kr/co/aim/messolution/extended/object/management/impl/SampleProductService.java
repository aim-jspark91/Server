package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SampleProductService extends CTORMService<SampleProduct> {
	
	public static Log logger = LogFactory.getLog(SampleProductService.class);
	
	private final String historyEntity = "SampleProductHist";
	
	public List<SampleProduct> select(String condition, Object[] bindSet)
			throws CustomException
	{
		List<SampleProduct> result = super.select(condition, bindSet, SampleProduct.class);
		
		return result;
	}
	
	public SampleProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(SampleProduct.class, isLock, keySet);
	}
	
	public SampleProduct create(EventInfo eventInfo, SampleProduct dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SampleProduct dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SampleProduct modify(EventInfo eventInfo, SampleProduct dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void insertSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, 
									String processFlowName, String processOperationName, String machineName, 
									String toProcessOperationName, String productSampleFlag, String productSampleCount, 
									String productSamplePosition, String actualSamplePosition, String manualSampleFlag)
										throws CustomException 
	{
		try
		{
			/* 2018.02.22 dmlee : arrange For EDO
			SampleProduct sampleproductInfo = new SampleProduct(productName, lotName, factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName);
			
			sampleproductInfo.setPRODUCTSAMPLEFLAG(productSampleFlag);
			sampleproductInfo.setPRODUCTSAMPLECOUNT(productSampleCount);
			sampleproductInfo.setPRODUCTSAMPLEPOSITION(productSamplePosition);
			sampleproductInfo.setACTUALSAMPLEPOSITION(actualSamplePosition);
			sampleproductInfo.setMANUALSAMPLEFLAG(manualSampleFlag);
			sampleproductInfo.setLASTEVENTUSER(eventInfo.getEventUser());
			sampleproductInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
			
			ExtendedObjectProxy.getSampleProductService().create(eventInfo, sampleproductInfo);
			
			*/
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
		}
	}
	
	public void insertSampleProductAddMachineRecipe(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, 
			String processFlowName, String processOperationName, String machineName, 
			String toProcessOperationName, String productSampleFlag, String productSampleCount, 
			String productSamplePosition, String actualSamplePosition, String manualSampleFlag, String machineRecipeName)
				throws CustomException 
	{
		try
		{
			/*			
			 2018.02.22 dmlee : arrange For EDO
			SampleProduct sampleproductInfo = new SampleProduct(productName, lotName, factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName);
			
			sampleproductInfo.setPRODUCTSAMPLEFLAG(productSampleFlag);
			sampleproductInfo.setPRODUCTSAMPLECOUNT(productSampleCount);
			sampleproductInfo.setPRODUCTSAMPLEPOSITION(productSamplePosition);
			sampleproductInfo.setACTUALSAMPLEPOSITION(actualSamplePosition);
			sampleproductInfo.setMANUALSAMPLEFLAG(manualSampleFlag);
			sampleproductInfo.setLASTEVENTUSER(eventInfo.getEventUser());
			sampleproductInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
			2018.02.07 hsryu - remove
			sampleproductInfo.setMACHINERECIPENAME(machineRecipeName);
			
			
			ExtendedObjectProxy.getSampleProductService().create(eventInfo, sampleproductInfo);
			
			//dmlee 
			*/
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void deleteSampleProduct(String productName, String lotName, String factoryName, String productSpecName, 
			String processFlowName, String processOperationName, String machineName, 
			String toProcessOperationName)throws CustomException 
	{
		try
		{
			/* 2018.02.22 dmlee : arrange For EDO
			SampleProduct sampleProductInfo = new SampleProduct(productName, lotName, factoryName, productSpecName, processFlowName, "", "", toProcessOperationName);
			
			ExtendedObjectProxy.getSampleProductService().delete(sampleProductInfo);
			//dmlee
			*/
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}
	
	public void deleteSampleProduct(SampleProduct sampleProductInfo)
			throws CustomException 
	{
		try
		{			
			ExtendedObjectProxy.getSampleProductService().delete(sampleProductInfo);
		}
		catch(Exception e)
		{	
			logger.info(e.getMessage());
		}
	}
	
	

	/**
	 * getSampleProductData
	 * @author hwlee
	 * @since 2015.10.27
	 * @param 
	 * @return List<Map<String, Object>>
	 * @throws CustomException
	 */
	/*
	public List<Map<String, Object>> getSampleProductData(
			String productName, String lotName, String factoryName, String productSpecName, String processFlowName, 
			String processOperationName, String machineName)
		throws CustomException
	{
		String sql = "" +
		 " SELECT PRODUCTNAME, LOTNAME, FACTORYNAME, PRODUCTSPECNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, MACHINENAME, " +
		 "        TOPROCESSOPERATIONNAME, PRODUCTSAMPLEFLAG, PRODUCTSAMPLECOUNT, PRODUCTSAMPLEPOSITION, ACTUALSAMPLEPOSITION " +
		 " FROM CT_SAMPLEPRODUCT " +
		 " WHERE PRODUCTNAME = :productName " +
		 "    AND LOTNAME = :lotName " +
		 "    AND FACTORYNAME = :factoryName " +
		 "    AND PRODUCTSPECNAME = :productSpecName " +
		 "    AND PROCESSFLOWNAME = :processFlowName " ;

		Map bindMap = new HashMap<String, Object>();
		bindMap.put("productName", productName);
		bindMap.put("lotName", lotName);
		bindMap.put("factoryName", factoryName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("processFlowName", processFlowName);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	*/
	
	/**
	 * getPOSSampleData
	 * @author hykim
	 * @since 2014.07.31
	 * @param 
	 * @return List<Map<String, Object>>
	 * @throws CustomException
	 */
	/*
	public List<Map<String, Object>> getSampleProductData(
			String productName, String lotName, String factoryName, String productSpecName, String processFlowName, 
			String processOperationName, String machineName, String toProcessOperationName)
		throws CustomException
	{
		Map bindMap = new HashMap<String, Object>();
		
		String sqlAddProductName = "";
		if(!productName.isEmpty())
		{
			bindMap.put("productName", productName);
			sqlAddProductName = "    AND PRODUCTNAME = :productName ";
		}
		
		String sql = "" +
		 " SELECT PRODUCTNAME, LOTNAME, FACTORYNAME, PRODUCTSPECNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, MACHINENAME, " +
		 "        TOPROCESSOPERATIONNAME, PRODUCTSAMPLEFLAG, PRODUCTSAMPLECOUNT, PRODUCTSAMPLEPOSITION, ACTUALSAMPLEPOSITION " +
		 " FROM CT_SAMPLEPRODUCT " +
		 " WHERE 1=1 " +
		 sqlAddProductName +
		 "    AND LOTNAME = :lotName " +
		 "    AND FACTORYNAME = :factoryName " +
		 "    AND PRODUCTSPECNAME = :productSpecName " +
		 "    AND PROCESSFLOWNAME = :processFlowName " +
		 //"    AND PROCESSOPERATIONNAME = :processOperationName " +
		 //"    AND MACHINENAME = :machineName " +
		 "    AND TOPROCESSOPERATIONNAME = :toProcessOperationName ";

		bindMap.put("productName", productName);
		bindMap.put("lotName", lotName);
		bindMap.put("factoryName", factoryName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("processFlowName", processFlowName);
		//bindMap.put("processOperationName", processOperationName);
		//bindMap.put("machineName", machineName);
		bindMap.put("toProcessOperationName", toProcessOperationName);
		
//		List<Map<String, Object>> sqlResult = 
//			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	*/

}
