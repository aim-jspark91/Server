package kr.co.aim.messolution.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
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

	public void insertDSPReserveProductSpec(EventInfo eventInfo,
			String machineName,String machineGroupName, String PortName, String ProductionType,
			String productSpecName, String ECCode, String RecycleFlag,
			String ReserveName, String ProcessFlowName, String processOperationName, 
			String UseFlag, String SetCount, String CurrentCount, String SkipFlag, String Position) 
	{	
		String sql = "INSERT INTO CT_DSPRESERVEPRODUCT "
				+ " (MACHINENAME,MACHINEGROUPNAME, PORTNAME, PRODUCTIONTYPE, PRODUCTSPECNAME, ECCODE, RECYCLEFLAG, RESERVESTATE, " 
				+ "  PROCESSFLOWNAME, PROCESSOPERATIONNAME,  USEFLAG, SETCOUNT, CURRENTCOUNT, SKIPFLAG, POSITION, RESERVENAME, "
				+ "  LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTUSER,LASTEVENTCOMMENT,LASTEVENTTIME) "
				+ " VALUES "
				+ " (:machineName, :machineGroupName ,:PortName, :ProductionType, :productSpecName, :ECCode,:RecycleFlag,'Reserve', " 
				+ "  :ProcessFlowName, :processOperationName, :UseFlag, :SetCount, :CurrentCount, :SkipFlag, :Position, :ReserveName, "
				+ "  :eventName, :eventTimeKey,  :eventUser, :eventComment , :eventTime) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("machineGroupName", machineGroupName);
		bindMap.put("PortName", PortName);
		bindMap.put("ProductionType", ProductionType);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("ECCode", ECCode);
		bindMap.put("RecycleFlag", RecycleFlag);
		bindMap.put("ReserveName", ReserveName);
		
		bindMap.put("ProcessFlowName", ProcessFlowName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("UseFlag", UseFlag);
		bindMap.put("SetCount", SetCount);
		bindMap.put("CurrentCount", CurrentCount);
		bindMap.put("SkipFlag", SkipFlag);
		bindMap.put("Position", Position);
		
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());
		bindMap.put("eventTime", eventInfo.getEventTime());
				
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		insertDSPReserveProductSpecHist( eventInfo, 
				machineName,machineGroupName , PortName, ProductionType,  productSpecName, ECCode,
				RecycleFlag, ReserveName, ProcessFlowName, processOperationName, 
				UseFlag, SetCount, CurrentCount, SkipFlag, Position );
	}
	
	public void insertMaxWipSpec(EventInfo eventInfo,
			String machineName,String productSpecName ,String opeID, String processFlowName, String validateFlag,String currentState,String currentMode,String maxWip ) 
	{	
		String sql = "INSERT INTO CT_DSPEQPMAXWIP "
				+ " (MACHINENAME, PRODUCTSPECNAME, PROCESSOPERATIONNAME, MAXWIPCOUNT, VALIDFLAG, CURRENTSTATE, " 
				+ "  CURRENTRUNMODE, CREATEUSER,  CREATETIME, "
				+ "  LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,LASTEVENTCOMMENT, PROCESSFLOWNAME) "
				+ " VALUES "
				+ " (:machineName, :productSpecName, :opeID, :maxWip, :validateFlag, :currentState" 
				+ " ,:currentMode, :eventUser, :eventTime, :eventName, :eventTimeKey, :eventTime, :eventUser, :eventComment, :processFlowName ) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("opeID", opeID);
		bindMap.put("maxWip", maxWip);
		bindMap.put("validateFlag", validateFlag);
		bindMap.put("currentState", currentState);
		
		bindMap.put("currentMode", currentMode);
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventTime", eventInfo.getEventTime());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());
		
		bindMap.put("processFlowName", processFlowName);

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		insertMaxWipSpecHist( eventInfo,machineName,productSpecName,opeID, processFlowName, validateFlag,currentState,currentMode,maxWip );

	}
	
	public void insertMaxWipSpecHist(EventInfo eventInfo,
			String machineName,String productSpecName ,String opeID, String processFlowName, String validateFlag,String currentState,String currentMode,String maxWip ) 
	{	
		String sql = "INSERT INTO CT_DSPEQPMAXWIPHIST "
				+ " (MACHINENAME, PRODUCTSPECNAME, PROCESSOPERATIONNAME, MAXWIPCOUNT, VALIDFLAG, CURRENTSTATE, " 
				+ "  CURRENTRUNMODE, CREATEUSER,  CREATETIME, "
				+ "  EVENTNAME,TIMEKEY,EVENTTIME,EVENTUSER,EVENTCOMMENT,PROCESSFLOWNAME) "
				+ " VALUES "
				+ " (:machineName, :productSpecName, :opeID, :maxWip, :validateFlag, :currentState" 
				+ " ,:currentMode, :eventUser, :eventTime, :eventName, :eventTimeKey, :eventTime, :eventUser, :eventComment, :processFlowName ) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("opeID", opeID);
		bindMap.put("maxWip", maxWip);
		bindMap.put("validateFlag", validateFlag);
		bindMap.put("currentState", currentState);
		
		bindMap.put("currentMode", currentMode);
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventTime", eventInfo.getEventTime());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());
		bindMap.put("processFlowName", processFlowName);

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
	}
	

	public void deleteMaxWipSpec(EventInfo eventInfo,
			String machineName,String productSpecName ,String opeID, String processFlowName, String validateFlag,String currentState,String currentMode,String maxWip) throws CustomException
	{
 
		String sql = "DELETE CT_DSPEQPMAXWIP " +
				 " WHERE MACHINENAME = :machineName" +
				 " AND PROCESSOPERATIONNAME = :opeid " +
				 " AND PRODUCTSPECNAME = :productSpecName " +
				 " AND PROCESSFLOWNAME = :processFlowName ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("machineName", machineName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("opeid", opeID);
		bindMap.put("processFlowName", processFlowName);
 
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		insertMaxWipSpecHist(eventInfo,machineName,productSpecName,opeID, processFlowName, validateFlag,currentState,currentMode,maxWip );

	}
	
	
	public void updateMaxWipSpec(EventInfo eventInfo,String machineName,String productSpecName ,String opeID, String processFlowName, String validateFlag,String currentState,String currentMode,String maxWip ) throws CustomException
	{
	 
		String sql = "UPDATE CT_DSPEQPMAXWIP SET " +
				 " MAXWIPCOUNT = :maxWip, VALIDFLAG = :validateFlag, " +
				 " CURRENTSTATE = :currentState, CURRENTRUNMODE = :currentMode, " +
				 " LASTEVENTTIME = :eventTime , LASTEVENTNAME = :eventName, " +
				 " LASTEVENTTIMEKEY = :eventTimeKey , LASTEVENTUSER = :eventUser, " +
				 " LASTEVENTCOMMENT = :eventComment "+				 
				 " WHERE MACHINENAME = :machineName " +
				 " AND PROCESSOPERATIONNAME = :opeID " +
				 " AND PRODUCTSPECNAME = :productSpecName " +
				 " AND PROCESSFLOWNAME = :processFlowName ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		
 		bindMap.put("machineName", machineName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("opeID", opeID);
		bindMap.put("maxWip", maxWip);
		bindMap.put("validateFlag", validateFlag);
		bindMap.put("currentState", currentState);
		
		bindMap.put("currentMode", currentMode);
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventTime", eventInfo.getEventTime());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());
		
		bindMap.put("processFlowName", processFlowName);
	 

//		long rv = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		insertMaxWipSpecHist( eventInfo,machineName,productSpecName,opeID, processFlowName, validateFlag,currentState,currentMode,maxWip );

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
	
	
	public void insertDSPReserveProductSpecHist( EventInfo eventInfo,
			String machineName, String machineGroupName , String PortName , String ProductionType, String productSpecName, String ECCode, 
			String RecycleFlag, String ReserveName, String ProcessFlowName, String processOperationName, 
			String UseFlag, String SetCount, String CurrentCount, String SkipFlag, String Position ) 
	{	
		String sql = "INSERT INTO CT_DSPRESERVEPRODUCTHIST " 
				+ " (MACHINENAME, MACHINEGROUPNAME, PORTNAME, PRODUCTIONTYPE, PRODUCTSPECNAME, ECCODE, RECYCLEFLAG, RESERVESTATE, " 
				+ "  PROCESSFLOWNAME, PROCESSOPERATIONNAME,  USEFLAG, SETCOUNT, CURRENTCOUNT, SKIPFLAG, POSITION,RESERVENAME  "
				+ "  ,TIMEKEY, EVENTNAME, EVENTUSER, EVENTCOMMENT, EVENTTIME) "

				+ "VALUES "
				+ " (:machineName, :machineGroupName ,:PortName, :ProductionType, :productSpecName, :ECCode,:RecycleFlag,'Reserve' , " 
				+ "  :ProcessFlowName, :processOperationName, :UseFlag, :SetCount, :CurrentCount, :SkipFlag, :Position, :ReserveName "
				+ "  ,:eventTimeKey, :eventName, :eventUser, :eventComment, :eventTime) ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		
		bindMap.put("machineName", machineName);
		bindMap.put("machineGroupName", machineGroupName);
		bindMap.put("PortName", PortName);
		bindMap.put("ProductionType", ProductionType);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("ECCode", ECCode);
		bindMap.put("RecycleFlag", RecycleFlag);
		bindMap.put("ReserveName", ReserveName);
		
		bindMap.put("ProcessFlowName", ProcessFlowName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("UseFlag", UseFlag);
		bindMap.put("SetCount", SetCount);
		bindMap.put("CurrentCount", CurrentCount);
		bindMap.put("SkipFlag", SkipFlag);
		bindMap.put("Position", Position);
 
		bindMap.put("eventTimeKey", eventInfo.getEventTimeKey());
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());
		bindMap.put("eventTime", eventInfo.getEventTime());
		
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
			MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
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
			MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
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
	
	
	public void deleteDSPReserveProductSpec(EventInfo eventInfo,
			String machineName, String portName, String reserveName) throws CustomException
	{
		List<Map<String, Object>> reserveProductSpecInfo = 
			MESDSPServiceProxy.getDSPServiceUtil().getDSPReserveProductSpecData(machineName, portName, reserveName);
		
		String sql = "DELETE CT_DSPRESERVEPRODUCT " +
				 " WHERE MACHINENAME = :machineName" +
				 " AND PORTNAME = :portName " +
				 " AND RESERVENAME = :reserveName ";

		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("machineName", machineName);
		bindMap.put("portName", portName);
		bindMap.put("reserveName", reserveName);

//		long rv = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		String MACHINEGROUPNAME = (String)reserveProductSpecInfo.get(0).get("MACHINEGROUPNAME");
		String PORTNAME = (String)reserveProductSpecInfo.get(0).get("PORTNAME");
		String PRODUCTIONTYPE = (String)reserveProductSpecInfo.get(0).get("PRODUCTIONTYPE");
		String PRODUCTSPECNAME = (String)reserveProductSpecInfo.get(0).get("PRODUCTSPECNAME");
		String ECCODE = (String)reserveProductSpecInfo.get(0).get("ECCODE");
		String RECYCLEFLAG = (String)reserveProductSpecInfo.get(0).get("RECYCLEFLAG");
		String RESERVENAME = (String)reserveProductSpecInfo.get(0).get("RESERVENAME");
		String PROCESSFLOWNAME = (String)reserveProductSpecInfo.get(0).get("PROCESSFLOWNAME");
		String PROCESSOPERATIONNAME = (String)reserveProductSpecInfo.get(0).get("PROCESSOPERATIONNAME");
		String USEFLAG = (String)reserveProductSpecInfo.get(0).get("USEFLAG");
		
		//String SETCOUNT = (String)reserveProductSpecInfo.get(0).get("SETCOUNT");
		String SETCOUNT = reserveProductSpecInfo.get(0).get("SETCOUNT").toString();
		
		String POSITION = reserveProductSpecInfo.get(0).get("POSITION").toString();
		String CURRENTCOUNT = reserveProductSpecInfo.get(0).get("CURRENTCOUNT").toString();
		String SKIPFLAG = reserveProductSpecInfo.get(0).get("SKIPFLAG").toString();
				
		//insert SortJobHist
//		insertReserveProductSpecHist(
//				eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, 
//				(String)reserveProductSpecInfo.get(0).get("POSITION"), 
//				(String)reserveProductSpecInfo.get(0).get("RESERVESTATE"), 
//				(String)reserveProductSpecInfo.get(0).get("RESERVEDQUANTITY"), 
//				(String)reserveProductSpecInfo.get(0).get("COMPLETEQUANTITY"));
		
		insertDSPReserveProductSpecHist(eventInfo
				 ,	machineName  
				 ,  MACHINEGROUPNAME 
				 ,  PORTNAME
				 ,  PRODUCTIONTYPE
				 ,  PRODUCTSPECNAME
				 ,  ECCODE
				 ,  RECYCLEFLAG
				 ,  RESERVENAME
				 ,  PROCESSFLOWNAME
				 ,  PROCESSOPERATIONNAME
				 ,  USEFLAG
				 ,  SETCOUNT
				 ,  CURRENTCOUNT
				 ,  SKIPFLAG
				 ,  POSITION				);
	}
}
