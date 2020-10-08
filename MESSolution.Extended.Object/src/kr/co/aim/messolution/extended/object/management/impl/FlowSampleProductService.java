package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FlowSampleProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlowSampleProductService extends CTORMService<FlowSampleProduct> {
	
	public static Log logger = LogFactory.getLog(FlowSampleProductService.class);
	
	private final String historyEntity = "FlowSampleProductHist";
	
	/*try
	{
		
	}
	catch(greenFrameDBErrorSignal ne)
	{
		if (ne.getErrorCode().equals("NotFoundSignal"))
			throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
		else
			throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
	}*/
	
	public List<FlowSampleProduct> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<FlowSampleProduct> result = super.select(condition, bindSet, FlowSampleProduct.class);
			
			return result;
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
		}
	}
	
	public FlowSampleProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(FlowSampleProduct.class, isLock, keySet);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (ne.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(ne.getDataKey(), ne.getSql());
			else
				throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
		}
	}
	
	public FlowSampleProduct create(EventInfo eventInfo, FlowSampleProduct dataInfo)
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
				throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, FlowSampleProduct dataInfo)
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
				throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
		}
	}
	
	public FlowSampleProduct modify(EventInfo eventInfo, FlowSampleProduct dataInfo)
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
				throw new CustomException("SYS-9999", "FlowSampleProducts", ne.getMessage());
		}
	}
	
	/**
	 * create single FlowSampleProduct
	 * @author swcho
	 * @since 2016.12.17
	 * @param eventInfo
	 * @param productName
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param processFlowName
	 * @param processOperationName
	 * @param machineName
	 * @param toProcessFlowName
	 * @param toProcessOperationName
	 * @param productSampleFlag
	 * @param productSampleCount
	 * @param productSamplePosition
	 * @param actualSamplePosition
	 * @param manualSampleFlag
	 * @param machineRecipeName
	 * @throws CustomException
	 * @throws NotFoundSignal
	 * @return FlowSampleProduct
	 */
	public FlowSampleProduct createFlowSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, 
										String processFlowName, String processOperationName, String machineName, String toProcessFlowName,
										String toProcessOperationName, String productSampleFlag, String productSampleCount, 
										String productSamplePosition, String actualSamplePosition, String manualSampleFlag, String machineRecipeName)
		throws CustomException, NotFoundSignal
	{
		FlowSampleProduct sampleProductInfo = new FlowSampleProduct(productName, lotName, factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessFlowName, toProcessOperationName);
		
		sampleProductInfo.setPRODUCTSAMPLEFLAG(productSampleFlag);
		sampleProductInfo.setPRODUCTSAMPLECOUNT(productSampleCount);
		sampleProductInfo.setPRODUCTSAMPLEPOSITION(productSamplePosition);
		sampleProductInfo.setACTUALSAMPLEPOSITION(actualSamplePosition);
		sampleProductInfo.setMANUALSAMPLEFLAG(manualSampleFlag);
		sampleProductInfo.setMACHINERECIPENAME(machineRecipeName);
		
		sampleProductInfo.setLASTEVENTTIME(eventInfo.getEventTime());
		sampleProductInfo.setLASTEVENTNAME(eventInfo.getEventName());
		sampleProductInfo.setLASTEVENTUSER(eventInfo.getEventUser());
		sampleProductInfo.setLASTEVENTCOMMENT(eventInfo.getEventComment());
		
		sampleProductInfo = ExtendedObjectProxy.getFlowSampleProductService().create(eventInfo, sampleProductInfo);
		
		return sampleProductInfo;
	}
	
	/**
	 * 
	 * @author swcho
	 * @since 2016.12.21
	 * @param eventInfo
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param toFlowName
	 * @param toOperationName
	 * @throws CustomException
	 */
	public void purgeFlowSampleProduct(EventInfo eventInfo, String lotName, String factoryName, String productSpecName,
										String toFlowName, String toOperationName)
		throws CustomException
	{
		List<FlowSampleProduct> productList;
		
		try
		{
			productList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND productSampleFlag = ? ",
										new Object [] {lotName, factoryName, productSpecName, toFlowName, toOperationName, "Y"});
		}
		catch (NotFoundSignal ne)
		{
			productList = new ArrayList<FlowSampleProduct>();
		}
		
		for (FlowSampleProduct productData : productList)
		{
			try
			{
				this.remove(eventInfo, productData);
			}
			catch (NotFoundSignal ne)
			{
				//ignore
			}
		}
	}
	
	/**
	 * purge PartSel on Product
	 * @author yudan
	 * @since 2017.08.17
	 * @param eventInfo
	 * @param lotName
	 * @param factoryName
	 * @param productSpecName
	 * @param flowName
	 * @param operationName
	 * @throws CustomException
	 */
	public void pausePartSelProduct(EventInfo eventInfo, String lotName,String factoryName,String productSpecName,String flowName,String operationName)
			throws CustomException
	{
		List<FlowSampleProduct> productList;
		
		try
		{
			productList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND productSampleFlag = ? ",
									new Object[] {lotName, factoryName, productSpecName, flowName, operationName,"PARTSEL"});
		}
		catch (NotFoundSignal ne)
		{
			productList = new ArrayList<FlowSampleProduct>();
		}
		
		for (FlowSampleProduct productData : productList)
		{
			try
			{
				this.remove(eventInfo, productData);
			}
			catch (NotFoundSignal ne)
			{
				//ignore
			}
		}
	}
	
	/**
	 * search Product sampling
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
	public List<FlowSampleProduct> getSamplingProduct(String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		List<FlowSampleProduct> sampleProductList;
			
		try
		{
			//170213 by swcho : bug-fix
			sampleProductList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND productSampleFlag = ? ",
												new Object [] {lotName, factoryName, productSpecName, processFlowName, processOperationName, "Y"});
		}
		catch (NotFoundSignal ne)
		{
			sampleProductList = new ArrayList<FlowSampleProduct>();
		}
		
		return sampleProductList;
	}
	
	/**
	 * select Product position
	 * @author swcho
	 * @since 2016.12.28
	 * @param productData
	 * @param productSampleList
	 * @return
	 * @throws CustomException
	 */
	public String getSamplingFlag(Product productData, List<FlowSampleProduct> productSampleList)
		throws CustomException
	{
		String result = GenericServiceProxy.getConstantMap().Flag_N;
		
		long position = productData.getPosition();
		
		String sPosition;
		try
		{
			sPosition = String.valueOf(position);
		}
		catch (Exception ex)
		{
			sPosition = "";
		}
		
		if (!sPosition.isEmpty())
		{
			for (FlowSampleProduct productSample : productSampleList)
			{
				//only single position reserved
				if (productSample.getACTUALSAMPLEPOSITION().equals(sPosition))
				{
					result = GenericServiceProxy.getConstantMap().Flag_Y;
					break;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * search Product PartSel Info
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
	public List<FlowSampleProduct> getPartSelProduct(String lotName, String factoryName, String productSpecName, String processFlowName, String processOperationName)
		throws CustomException
	{
		List<FlowSampleProduct> sampleProductList;
			
		try
		{
			//170213 by swcho : bug-fix
			sampleProductList = this.select("lotName = ? AND factoryName = ? AND productSpecName = ? AND processFlowName = ? AND processOperationName = ? AND productSampleFlag = ? ",
												new Object [] {lotName, factoryName, productSpecName, processFlowName, processOperationName, "PARTSEL"});
		}
		catch (NotFoundSignal ne)
		{
			sampleProductList = new ArrayList<FlowSampleProduct>();
		}
		
		return sampleProductList;
	}
}
