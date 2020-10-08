package kr.co.aim.messolution.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

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
		 " SELECT :ZERO POSITION FROM  DUAL) ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("ZERO", "0");
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap); 
		
		int nextPositionI = Integer.valueOf((String)sqlResult.get(0).get("MAXPOSITION")) + 1;
		
		return String.valueOf(nextPositionI);
	}
	
	public String getNextPositionForDSPReserveProductSpec(String machineName) throws CustomException													
	{
 		String sql = "" +
				 " SELECT :RP_|| MAX(RESERVENAME) MAXPOSITION " +
			     " FROM " +
				 "	  ( " +
			     "   SELECT REPLACE(RESERVENAME,:RP_,:EMPTY) +1 RESERVENAME " +
			     "   FROM (  " +
			     "         SELECT RESERVENAME  " +
			     "           FROM CT_DSPRESERVEPRODUCT WHERE MACHINENAME = :machineName " +  
			     "         UNION  " +
			     "         SELECT :RP_0 RESERVENAME " + 
			     "          FROM  DUAL ) "+
			     " ) "; 

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("RP_", "RP_");
		bindMap.put("RP_0", "RP_0");
		bindMap.put("EMPTY", "");
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap); 
		
		return (String)sqlResult.get(0).get("MAXPOSITION");
	}
	
	public String getNextPositionForDSPReserveProductNon(String machineName) throws CustomException													
	{
 		String sql = "" +
				 " SELECT :RP_|| MAX(RESERVENAME) MAXPOSITION " +
			     " FROM " +
				 "	  ( " +
			     "   SELECT REPLACE(RESERVENAME,:RP_,:EMPTY) +1 RESERVENAME " +
			     "   FROM (  " +
			     "         SELECT RESERVENAME  " +
			     "           FROM CT_DSPRESERVEPRODUCTNON WHERE MACHINENAME = :machineName " +  
			     "         UNION  " +
			     "         SELECT :RP_0 RESERVENAME " + 
			     "          FROM  DUAL ) "+
			     " ) "; 

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("RP_", "RP_");
		bindMap.put("RP_0", "RP_0");
		bindMap.put("EMPTY", "");
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap); 
		
		return (String)sqlResult.get(0).get("MAXPOSITION");
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
	
	public List<Map<String, Object>> getDSPReserveProductSpecData(
			String machineName, String portName, String reserveName ) throws CustomException													
	{
		String sql = "" +
		 " SELECT  MACHINENAME,  MACHINEGROUPNAME ,  PORTNAME ,  PRODUCTIONTYPE,  PRODUCTSPECNAME,  ECCODE,  " +
				  "  RECYCLEFLAG,  RESERVENAME,  PROCESSFLOWNAME,  PROCESSOPERATIONNAME, " +
				  "   USEFLAG,  SETCOUNT,  CURRENTCOUNT,  SKIPFLAG,  POSITION " +
		 " FROM CT_DSPRESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PORTNAME = :portName " +
		 " AND RESERVENAME = :reserveName ";
		 
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("portName", portName);
		bindMap.put("reserveName", reserveName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	 	
	public List<Map<String, Object>> getReserveProductSpecData(
			String machineName, String processOperationName, String productSpecName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY " +
		 " FROM CT_RESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PROCESSOPERATIONNAME = :processOperationName " +
		 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>> getReserveProductData(
			String machineName, String processOperationName, String productSpecName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME,PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION " +
		 " FROM CT_DSPRESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName " +
		 " AND PROCESSOPERATIONNAME = :processOperationName " +
		 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		
		//List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	
	public List<Map<String, Object>> getReserveProductPosition(String machineName, String portName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME,PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION " +
		 " FROM CT_DSPRESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName AND PORTNAME = :portName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("portName", portName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>> getReserveProductPosition_Modify(String machineName, String portName, String reserveName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME,PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION " +
		 " FROM CT_DSPRESERVEPRODUCT " +
		 " WHERE MACHINENAME = :machineName AND PORTNAME = :portName AND RESERVENAME <> :reserveName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("portName", portName);
		bindMap.put("reserveName", reserveName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	public List<Map<String, Object>> getReserveProductPositionNon_Modify(String machineName, String portName, String reserveName,String unitName) throws CustomException													
	{
		String sql = "" +
		 " SELECT MACHINENAME,PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION " +
		 " FROM CT_DSPRESERVEPRODUCTNON " +
		 " WHERE MACHINENAME = :machineName AND PORTNAME = :portName AND UNITNAME =:UNITNAME AND RESERVENAME <> :reserveName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("machineName", machineName);
		bindMap.put("portName", portName);
		bindMap.put("reserveName", reserveName);
		bindMap.put("UNITNAME", unitName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>> getConnectedStockerPositionInfo(String machineName, String portName, String direction) throws CustomException													
	{
		String sql = "" +	
		"  SELECT NVL(( MAX(POSITION) + 1 ),0) POSITION "+
		"   FROM CT_DSPCONNECTEDSTOCKER "+
		"  WHERE MACHINENAME = :MACHINENAME "+
		"    AND PORTNAME = :PORTNAME "+
		"    AND LUDIRECTION = :LUDIRECTION ";
		 
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("PORTNAME", portName);
		bindMap.put("LUDIRECTION", direction);
		
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
