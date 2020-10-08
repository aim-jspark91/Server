package kr.co.aim.messolution.productrequest.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.DecrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.27
 */

public class ProductRequestInfoUtil implements ApplicationContextAware  {
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext     	applicationContext;
	private static Log				log = LogFactory.getLog("ProductRequestServiceImpl");
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
		throws BeansException
	{
		applicationContext = arg0;
	}
	 
	/*
	* Name : createInfo
	* Desc : This function is createInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public CreateInfo createInfo( String factoryName, Timestamp planFinishedTime, 
										 long planQuantity, Timestamp planReleasedTime, String orlPlanReleasedTime,
										 String productRequestType, String productSpecName, String productSpecVersion, 
										 String productionType, String phase)
	{
		String productRequestName = null;
		
		
		try {
			//productRequestName = ProductRequestServiceImpl.createProductRequestNameGenerated(productSpecName, productionType, phase, orlPlanReleasedTime);
			productRequestName = "";
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		CreateInfo createInfo = new CreateInfo();	
		createInfo.setFactoryName(factoryName);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setPlanFinishedTime(planFinishedTime);
		createInfo.setPlanQuantity(planQuantity);
		createInfo.setPlanReleasedTime(planReleasedTime);
		createInfo.setProductRequestType(productRequestType);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);
		
		
		Map<String,String> productRequestUdfs = new HashMap<String, String>();
		productRequestUdfs.put("PRODUCTIONTYPE", productionType);
		productRequestUdfs.put("PHASE", phase);
			
		createInfo.setUdfs( productRequestUdfs );
		
		return createInfo;
	}

	/*
	* Name : decrementScrappedQuantityByInfo
	* Desc : This function is decrementScrappedQuantityByInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo( ProductRequest productRequestData, long quantity)
	{
		DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo = new DecrementScrappedQuantityByInfo();
		decrementScrappedQuantityByInfo.setQuantity(quantity);
	
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		decrementScrappedQuantityByInfo.setUdfs( productRequestUdfs );
		
		return decrementScrappedQuantityByInfo;
	}

	/*
	* Name : incrementFinishedQuantityByInfo
	* Desc : This function is incrementFinishedQuantityByInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo( ProductRequest productRequestData, long quantity)
	{
		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(quantity);
		
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		incrementFinishedQuantityByInfo.setUdfs( productRequestUdfs );
		
		return incrementFinishedQuantityByInfo;
	}

	/*
	* Name : incrementReleasedQuantityByInfo
	* Desc : This function is incrementReleasedQuantityByInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo( ProductRequest productRequestData, long quantity)
	{
		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(quantity);
				
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		incrementReleasedQuantityByInfo.setUdfs( productRequestUdfs );
		
		return incrementReleasedQuantityByInfo;
	}

	/*
	* Name : incrementScrappedQuantityByInfo
	* Desc : This function is incrementScrappedQuantityByInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo( ProductRequest productRequestData, long quantity)
	{
		IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo = new IncrementScrappedQuantityByInfo();
		incrementScrappedQuantityByInfo.setQuantity(quantity);
					
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		incrementScrappedQuantityByInfo.setUdfs( productRequestUdfs );
		
		return incrementScrappedQuantityByInfo;
	}

	/*
	* Name : makeCompletedInfo
	* Desc : This function is makeCompletedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeCompletedInfo makeCompletedInfo( ProductRequest productRequestData )
	{
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
							
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		makeCompletedInfo.setUdfs( productRequestUdfs );
		
		return makeCompletedInfo;
	}

	/*
	* Name : makeNotOnHoldInfo
	* Desc : This function is makeNotOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeNotOnHoldInfo makeNotOnHoldInfo( ProductRequest productRequestData )
	{
		MakeNotOnHoldInfo makeNotOnHoldInfo = new MakeNotOnHoldInfo();
				
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		makeNotOnHoldInfo.setUdfs( productRequestUdfs );
		
		return makeNotOnHoldInfo;
	}

	/*
	* Name : makeOnHoldInfo
	* Desc : This function is makeOnHoldInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeOnHoldInfo makeOnHoldInfo( ProductRequest productRequestData )
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();
							
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		makeOnHoldInfo.setUdfs( productRequestUdfs );
		
		return makeOnHoldInfo;
	}

	/*
	* Name : makeReleasedInfo
	* Desc : This function is makeReleasedInfo
	* Author : AIM Systems, Inc
	* Date : 2011.01.18
	*/
	public MakeReleasedInfo makeReleasedInfo( ProductRequest productRequestData )
	{
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();
							
		Map<String,String> productRequestUdfs = productRequestData.getUdfs();
		makeReleasedInfo.setUdfs( productRequestUdfs );
		
		return makeReleasedInfo;
	}
	
	/**
	 * search ProductRequest
	 * @author swcho
	 * @since 2016.04.11
	 * @param productRequestName
	 * @return
	 * @throws CustomException
	 */
	public ProductRequest getProductRequest(String productRequestName) throws CustomException
	{
		try
		{
			ProductRequestKey keyInfo = new ProductRequestKey(productRequestName);
			
			ProductRequest result = ProductRequestServiceProxy.getProductRequestService().selectByKey(keyInfo);
			
			return result;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9999", "ProductRequest", "Not found work order");
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "ProductRequest", ex.getMessage());
		}
	}
	
	
	// -COMMENT- DT
	// 2016.04.29 Aim System
	public static void insertECCode(String factoryName, String productSpecName, String productSpecVersion, 
			String processFlowName, String processFlowVersion, String ecCode) 
	{
		StringBuilder sql = null;
		Map<String, Object> bindMap = new HashMap<String, Object>();
				
		sql = new StringBuilder();
		sql.append(" INSERT INTO PRODUCTSPECPOSSIBLEPF(FACTORYNAME, PRODUCTSPECNAME, PRODUCTSPECVERSION, PROCESSFLOWNAME, PROCESSFLOWVERSION, ECCODE)")
		   .append("      VALUES(:factoryName, :productSpecName, :productSpecVersion, :processFlowName, :processFlowVersion, :ecCode)");
				
		bindMap.put("factoryName", factoryName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("productSpecVersion", productSpecVersion);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		bindMap.put("ecCode", ecCode);
			
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
				
	}
	
	// -COMMENT- DT
	// 2016.04.29 Aim System
	public static void modifyECCode(String factoryName, String productSpecName, String productSpecVersion, 
			String processFlowName, String processFlowVersion, String ecCode) 
	{
		StringBuilder sql = null;
		Map<String, Object> bindMap = new HashMap<String, Object>();
					
		sql = new StringBuilder();
		sql.append(" UPDATE PRODUCTSPECPOSSIBLEPF SET ECCODE = :ecCode")
		   .append("   WHERE 1=1")
		   .append("     AND FACTORYNAME=:factoryName")
		   .append("     AND PRODUCTSPECNAME=:productSpecName")
		   .append("     AND PRODUCTSPECVERSION=:productSpecVersion")
		   .append("     AND PROCESSFLOWNAME=:processFlowName")
		   .append("     AND PROCESSFLOWVERSION=:processFlowVersion ");
					
		bindMap.put("factoryName", factoryName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("productSpecVersion", productSpecVersion);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		bindMap.put("ecCode", ecCode);
				
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
					
	}
		
	// -COMMENT- DT
	// 2016.04.29 Aim System
	public static void removeECCode(String factoryName, String productSpecName, String productSpecVersion, 
			String processFlowName, String processFlowVersion) 
	{
		StringBuilder sql = null;
		Map<String, Object> bindMap = new HashMap<String, Object>();
					
		sql = new StringBuilder();
		sql.append(" DELETE FROM PRODUCTSPECPOSSIBLEPF")
		   .append("   WHERE 1=1")
		   .append("     AND FACTORYNAME=:factoryName")
		   .append("     AND PRODUCTSPECNAME=:productSpecName")
		   .append("     AND PRODUCTSPECVERSION=:productSpecVersion")
		   .append("     AND PROCESSFLOWNAME=:processFlowName")
		   .append("     AND PROCESSFLOWVERSION=:processFlowVersion ");
					
		bindMap.put("factoryName", factoryName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("productSpecVersion", productSpecVersion);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
				
		greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
					
	}
}
