package kr.co.aim.messolution.dispatch.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.dispatch.MESDispachServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DSPServiceImpl implements ApplicationContextAware{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DSPServiceImpl.class);
			
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

	public void insertReserveProductSpec(EventInfo eventInfo,
			String machineName, String processOperationGroupName, String processOperationName, 
			String productSpecName, String position, String reserveState, String reservedQuantity, 
			String completeQuantity ) 
	{	
		String sql = "INSERT INTO CT_RESERVEPRODUCT "
				+ " (MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY) "
				+ "VALUES "
				+ " (:machineName, :processOperationGroupName, :processOperationName, :productSpecName, :position, " 
				+ "  :reserveState, :reservedQuantity, :completeQuantity ) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);

//		long rv = greenFrameServiceProxy.getSqlTemplate()
//				.getSimpleJdbcTemplate().update(sql, bindMap);
		
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		insertReserveProductSpecHist(
				eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, position, 
				reserveState, reservedQuantity, completeQuantity);
	}
	
	public void insertReserveProductSpecHist(EventInfo eventInfo,
			String machineName, String processOperationGroupName, String processOperationName, 
			String productSpecName, String position, String reserveState, String reservedQuantity, 
			String completeQuantity) 
	{	
		String sql = "INSERT INTO CT_RESERVEPRODUCTHIST "
				+ " (MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, " +
				  " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY, EVENTTIMEKEY, EVENTNAME, EVENTUSER, EVENTCOMMENT) "
				+ "VALUES "
				+ " (:machineName, :processOperationGroupName, :processOperationName, :productSpecName, :position, " 
				+ "  :reserveState, :reservedQuantity, :completeQuantity, :eventTimeKey, :eventName, :eventUser, :eventComment) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());

//		long rv = greenFrameServiceProxy.getSqlTemplate()
//				.getSimpleJdbcTemplate().update(sql, bindMap);
		
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}
	
	public void updateReserveProductSpec(EventInfo eventInfo,
			String machineName, String processOperationGroupName, String processOperationName, 
			String productSpecName, String position, String reserveState, String reservedQuantity, 
			String completeQuantity) throws CustomException
	{
		List<Map<String, Object>> reserveProductSpecInfo = 
			MESDispachServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
					machineName, processOperationGroupName, processOperationName, productSpecName);
		
		if(StringUtil.equals(position, "SAME"))
			position = (String)reserveProductSpecInfo.get(0).get("POSITION");
		
		if(StringUtil.equals(reserveState, "SAME"))
			reserveState = (String)reserveProductSpecInfo.get(0).get("RESERVESTATE");
		
		if(StringUtil.equals(reservedQuantity, "SAME"))
			reservedQuantity = (String)reserveProductSpecInfo.get(0).get("RESERVEDQUANTITY");
		
		if(StringUtil.equals(completeQuantity, "SAME"))
			completeQuantity = (String)reserveProductSpecInfo.get(0).get("COMPLETEQUANTITY");
		
		String sql = "UPDATE CT_RESERVEPRODUCT SET " +
				 " POSITION = :position, RESERVESTATE = :reserveState, " +
				 " RESERVEDQUANTITY = :reservedQuantity, COMPLETEQUANTITY = :completeQuantity " +
				 " WHERE MACHINENAME = :machineName" +
				 " AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName " +
				 " AND PROCESSOPERATIONNAME = :processOperationName " +
				 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);

//		long rv = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		//insert SortJobHist
		insertReserveProductSpecHist(
				eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, 
				position, reserveState, reservedQuantity, completeQuantity);
	}
	
	public void deleteReserveProductSpec(EventInfo eventInfo,
			String machineName, String processOperationGroupName, 
			String processOperationName, String productSpecName) throws CustomException
	{
		List<Map<String, Object>> reserveProductSpecInfo = 
			MESDispachServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
					machineName, processOperationGroupName, processOperationName, productSpecName);
		
		String sql = "DELETE CT_RESERVEPRODUCT " +
				 " WHERE MACHINENAME = :machineName" +
				 " AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName " +
				 " AND PROCESSOPERATIONNAME = :processOperationName " +
				 " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);

//		long rv = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		//insert SortJobHist
		insertReserveProductSpecHist(
				eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, 
				(String)reserveProductSpecInfo.get(0).get("POSITION"), 
				(String)reserveProductSpecInfo.get(0).get("RESERVESTATE"), 
				(String)reserveProductSpecInfo.get(0).get("RESERVEDQUANTITY"), 
				(String)reserveProductSpecInfo.get(0).get("COMPLETEQUANTITY"));
	}
}
