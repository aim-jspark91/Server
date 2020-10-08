package kr.co.aim.messolution.dispatch.service;

import java.util.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;


import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Element;
import org.jdom.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DSPServiceUtil implements ApplicationContextAware{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DSPServiceUtil.class);
	
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
	* Name : timeDelay
	* Desc : This function is timeDelay
	* Author : AIM Systems, Inc
	* Date : 2011.01.20
	*/
	public void timeDelay(long delay)
	{
		Timer timer = new Timer();
		timer.schedule(null, delay);	
	}
	
	/*
	* Name : getNextPositionForReserveProductSpec
	* Desc : get NextPosition For ReserveProductSpec
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public String getNextPositionForReserveProductSpec(String machineName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MAX(POSITION) MAXPOSITION FROM " +
		 " (SELECT POSITION FROM CT_RESERVEPRODUCT WHERE MACHINENAME = :machineName " +
		 " UNION " +
		 " SELECT '0' POSITION FROM  DUAL) ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap); 
		
		int nextPositionI = Integer.valueOf((String)sqlResult.get(0).get("MAXPOSITION")) + 1;
		
		String nextPositionS = String.valueOf(nextPositionI);
		
		return nextPositionS;
	}
	
	/*
	* Name : getReserveProductSpecData
	* Desc : getReserveProductSpecData
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public List<Map<String, Object>> getReserveProductSpecData(
			String machineName, String processOperationGroupName, String processOperationName, String productSpecName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 " FROM CT_RESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName " +
		 " AND PROCESSOPERATIONNAME = :processOperationName " +
		 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/*
	* Name : getReserveProductSpecList
	* Desc : getReserveProductSpecList
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public List<Map<String, Object>> getReserveProductSpecList(String machineName) throws CustomException													
	{
		String sql = "" +
		 " SELECT ROWNUM -1 SEQ, MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
		 "        RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 "  FROM CT_RESERVEPRODUCT " +
		 " WHERE MACHINENAME = :MACHINENAME " +
		 " ORDER BY POSITION ASC ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	/*
	* Name : getReserveProductSpecList
	* Desc : getReserveProductSpecList
	* Author : hykim
	* Date : 2015.02.14 
	*/
	public List<Map<String, Object>> getReserveProductSpecList(
			String productSpecName, String processOperationName, String machineName) throws CustomException													
	{
		String sql = "" +
		 " SELECT ROWNUM-1 SEQ, MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
		 "        RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 "  FROM CT_RESERVEPRODUCT " +
		 " WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME " +
		 " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
		 " AND MACHINENAME = :MACHINENAME " +
		 " ORDER BY POSITION ASC ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("MACHINENAME", machineName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
}
