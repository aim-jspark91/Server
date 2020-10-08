package kr.co.aim.messolution.generic.util;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.factory.FactoryServiceProxy;
import kr.co.aim.greentrack.factory.management.data.MESFactory;
import kr.co.aim.greentrack.factory.management.data.MESFactoryKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpecUtil implements ApplicationContextAware {

	Log logger = LogFactory.getLog(SpecUtil.class);
	
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException
	{
		if (logger.isInfoEnabled())
			logger.info("MES Specipication Utility is loaded");
	}
	
	/**
	 * search product spec for common
	 * @author swcho
	 * @since 2014.04.14
	 * @param factoryName
	 * @param productSpecName
	 * @param productSpecVersion
	 * @return
	 * @throws CustomException
	 */
	public ProductSpec getProductSpec(String factoryName, String productSpecName, String productSpecVersion)
		throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(productSpecVersion))
				productSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
			
			ProductSpecKey productSpecKey = new ProductSpecKey(factoryName, productSpecName, productSpecVersion);
			
			ProductSpec productSpec = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
			
			return productSpec;
		}
		catch (NotFoundSignal ne) 
		{
			throw new CustomException("PRODUCTSPEC-9001", productSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCT-9999", fe.getMessage());
		}
	}
	
	/**
	 * search process flow for common
	 * @author swcho
	 * @since 2016.12.26
	 * @param factoryName
	 * @param processFlowName
	 * @param processFlowVersion
	 * @return
	 * @throws CustomException
	 */
	public ProcessFlow getProcessFlow(String factoryName, String processFlowName, String processFlowVersion)
		throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(processFlowVersion))
				processFlowVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
			
			ProcessFlowKey processFlowKey = new ProcessFlowKey(factoryName, processFlowName, processFlowVersion);
			
			ProcessFlow flowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			
			return flowData;
		}
		catch (NotFoundSignal ne) 
		{
			throw new CustomException("SYS-9001", "ProcessFlow");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "ProcessFlow", fe.getMessage());
		}
	}
	
	/**
	 * search consumable spec for common
	 * @author swcho
	 * @since 2014.05.13
	 * @param factoryName
	 * @param consumableSpecName
	 * @param consumableSpecVersion
	 * @return
	 * @throws CustomException
	 */
	public ConsumableSpec getConsumableSpec(String factoryName, String consumableSpecName, String consumableSpecVersion)
		throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(consumableSpecVersion))
				consumableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
			
			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setFactoryName(factoryName);
			consumableSpecKey.setConsumableSpecName(consumableSpecName);
			consumableSpecKey.setConsumableSpecVersion(consumableSpecVersion);
			
			ConsumableSpec consumableSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
			
			return consumableSpec;
		}
		catch (NotFoundSignal ne) 
		{
			throw new CustomException("CRATE-9001", consumableSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
	}
	
	/**
	 * query MachineSpec data by EQP ID
	 * derived from MachineInfoUtil.java
	 * @author swcho
	 * @since 2015.02.26
	 * @param machineName
	 * @return
	 * @throws CustomException
	 */
	public MachineSpec getMachineSpec(String machineName) throws CustomException
	{
		MachineSpecKey machineSpecKey = new MachineSpecKey();
		machineSpecKey.setMachineName(machineName);
		
		MachineSpec machineSpecData;
		try
		{
			machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(machineSpecKey);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}
		
		return machineSpecData;
	}
	
	/**
	 * query getDCSpecItem
	 * derived from PostCellGetGrade.java
	 * @author Lhkim
	 * @since 2015.04.07
	 * @return sqlResult
	 * @throws CustomException
	 */
	public List<DCSpecItem> getDCSpecItem(String itemName) throws CustomException
	{
		String condition = "ITEMNAME = ? ORDER BY ITEMNAME";
		
		Object[] bindSet = new Object[] {itemName};
		
		List<DCSpecItem> sqlResult = DataCollectionServiceProxy.getDCSpecItemService().select(condition, bindSet);

		
		return sqlResult;
	}
	
	/**
	 * get factory definition
	 * @author swcho
	 * @since 2016.03.12
	 * @param factoryName
	 * @return
	 * @throws CustomException
	 */
	public MESFactory getFactory(String factoryName) throws CustomException
	{
		try
		{
			MESFactoryKey keyInfo = new MESFactoryKey();
			keyInfo.setFactoryName(factoryName);
			
			MESFactory factoryData = FactoryServiceProxy.getMESFactoryService().selectByKey(keyInfo);
			
			return factoryData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9001", "MESFactory");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "MESFactory", fe.getMessage());
		}
	}
	
	/**
	 * obtain factory code
	 * @author swcho
	 * @since 2016.03.12
	 * @param factoryName
	 * @return
	 * @throws CustomException
	 */
	public String getFactoryCode(String factoryName) throws CustomException
	{
		try
		{
			MESFactory factoryData = getFactory(factoryName);
			
			return CommonUtil.getValue(factoryData.getUdfs(), "FACTORYCODE");
		}
		catch (CustomException ce)
		{
			return "";
		}
	}
	
	/**
	 * obtain default area
	 * @author swcho
	 * @since 2016.03.12
	 * @param factoryName
	 * @return
	 * @throws CustomException
	 */
	public String getDefaultArea(String factoryName) throws CustomException
	{
		try
		{
			MESFactory factoryData = getFactory(factoryName);
			
			return CommonUtil.getValue(factoryData.getUdfs(), "DEFAULTAREANAME");
		}
		catch (CustomException ce)
		{
			return "";
		}
	}
}
