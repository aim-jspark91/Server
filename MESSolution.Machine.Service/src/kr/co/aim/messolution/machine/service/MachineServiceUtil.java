package kr.co.aim.messolution.machine.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCCondition;
import kr.co.aim.messolution.extended.object.management.data.MachineGroupMachine;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author gksong
 * @date   2009.02.25
 */

public class MachineServiceUtil implements ApplicationContextAware
{
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext		applicationContext;
	private static Log 				log = LogFactory.getLog(MachineServiceImpl.class);
	
	
	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	@Override
    public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	/*
	* Name : getMachineData
	* Desc : This function is getMachineData
	* Author : AIM Systems, Inc
	* Date : 2011.01.07
	*/
	public Machine getMachineData(String machineName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try{
			if(log.isInfoEnabled()){
				log.info("machineName = " + machineName);
			}
			MachineKey machineKey = new MachineKey();
			machineKey.setMachineName(machineName);
			Machine machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
			return machineData;
		}
		catch(Exception e)
		{
			throw new CustomException("MACHINE-9000", machineName);
		}
	}
	
	 public List<Map<String, Object>> getMachineDataByHierarchy(String machineName, String machineLevel, 
	         String unitName, String subunitName, String machineType) throws CustomException
	 {
	     List<Map<String, Object>> machineDatabyHierarchy = null;
	     String strSql = StringUtil.EMPTY;
	     try
	     {
	         strSql = " SELECT MQ.*                                                                         \n"
	                 + "  FROM (                                                                                    \n"
	                 + "        SELECT SQ.LV                                                                        \n"
	                 + "              ,SQ.COMBINEMACHINE                                                            \n"
	                 + "              ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 1) AS MACHINENAME               \n"
	                 + "              ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 2) AS UNITNAME                  \n"
	                 + "              ,REGEXP_SUBSTR(SQ.COMBINEMACHINE, '[^-]+', 1, 3) AS SUBUNITNAME               \n"
	                 + "              ,SQ.FACTORYNAME,SQ.AREANAME,SQ.SUPERMACHINENAME                               \n"
	                 + "              ,SQ.MACHINEGROUPNAME,SQ.PROCESSCOUNT,SQ.RESOURCESTATE                         \n"
	                 + "              ,SQ.E10STATE,SQ.COMMUNICATIONSTATE,SQ.MACHINESTATENAME                        \n"
	                 + "              ,SQ.LASTEVENTNAME,SQ.LASTEVENTTIMEKEY,SQ.LASTEVENTTIME                        \n"
	                 + "              ,SQ.LASTEVENTUSER,SQ.LASTEVENTCOMMENT,SQ.LASTEVENTFLAG                        \n"
	                 + "              ,SQ.REASONCODETYPE,SQ.REASONCODE,SQ.MCSUBJECTNAME                             \n"
	                 + "              ,SQ.OPERATIONMODE,SQ.DSPFLAG,SQ.FULLSTATE                                     \n"
	                 + "              ,SQ.ONLINEINITIALCOMMSTATE                                                    \n"
	                 + "              ,SQ.VENDOR,SQ.MODEL,SQ.SERIALNO                                               \n"
	                 + "              ,SQ.PROCESSUNIT,SQ.PROCESSCAPACITY,SQ.PROCESSGROUPSIZEMIN                     \n"
	                 + "              ,SQ.PROCESSGROUPSIZEMAX,SQ.DEFAULTRECIPENAMESPACENAME                         \n"
	                 + "              ,SQ.MACHINESTATEMODELNAME,NVL(SQ.CONSTRUCTTYPE, ' '),NVL(SQ.RMSFLAG, ' ')     \n"
	                 + "              ,SQ.DEPARTMENT,SQ.TRACKFLAG                                                   \n"
	                 + "          FROM (                                                                            \n"
	                 + "                SELECT LEVEL LV                                                             \n"
	                 + "                      ,SUBSTR(SYS_CONNECT_BY_PATH(M.MACHINENAME, '-'), 2) AS COMBINEMACHINE \n"
	                 + "                      ,M.*                                                                  \n"
	                 + "                      ,MS.VENDOR,MS.MODEL,MS.SERIALNO                                       \n"
	                 + "                      ,MS.PROCESSUNIT,MS.PROCESSCAPACITY,MS.PROCESSGROUPSIZEMIN             \n"
	                 + "                      ,MS.PROCESSGROUPSIZEMAX,MS.DEFAULTRECIPENAMESPACENAME                 \n"
	                 + "                      ,MS.MACHINESTATEMODELNAME,MS.CONSTRUCTTYPE                            \n"
	                 + "                      ,MS.RMSFLAG,MS.DEPARTMENT,MS.TRACKFLAG                                \n"
	                 + "                      FROM MACHINE M, MACHINESPEC MS                                        \n"
	                 + "                     WHERE M.MACHINENAME = MS.MACHINENAME                                   \n"
	                 + "                START WITH M.SUPERMACHINENAME IS NULL                                       \n";
	                 Map<String, Object> bindMap = new HashMap<String, Object>();        
	                 if(!machineType.isEmpty())
                     {
	                     strSql = strSql  + "                       AND MS.MACHINETYPE = :MACHINETYPE               \n";
	                     bindMap.put("MACHINETYPE", machineType);
                     }
	                 strSql = strSql  +  "          CONNECT BY PRIOR M.MACHINENAME = M.SUPERMACHINENAME             \n"
	                 + "                ORDER BY M.MACHINENAME                                                      \n"
	                 + "                ) SQ                                                                        \n"
	                 + "        ORDER BY SQ.LV                                                                      \n"
	                 + "        ) MQ                                                                                \n"
	                 + "  WHERE 1=1                                                                                 \n"
	                 + "    AND MQ.LV = :MACHINELEVEL                                                               \n";
	                
	                
	                 bindMap.put("MACHINENAME", machineName);
	                 bindMap.put("MACHINELEVEL", machineLevel);
	                 
	                 if(!unitName.isEmpty())
	                 {
	                     strSql = strSql  + "    AND MQ.UNITNAME = :UNITNAME                                        \n";
	                     bindMap.put("UNITNAME", unitName);
	                 }
	                 if(!subunitName.isEmpty())
	                 {
	                     strSql = strSql  + "    AND MQ.SUBUNITNAME = :SUBUNITNAME                                  \n";
	                     bindMap.put("SUBUNITNAME", subunitName);
	                 }
	                 strSql = strSql  + "       ORDER BY MQ.LV, MQ.COMBINEMACHINE                                   \n";
	     
	                 machineDatabyHierarchy = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
	     }
	     catch (Exception ex)
	     {
	         //throw new CustomException();
	     }
	     
	     return machineDatabyHierarchy;
	 }
	 
	 /**
	  * @author smkang
	  * @since 2018.08.11
	  * @param machineName
	  * @param unitName
	  * @return boolean
	  * @see This machine is over machine idle time or not.
	  */
	 public boolean isOverMachineIdleTime(String lotName, String machineName, String portName) throws CustomException{
		 	
		 Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		 
		 //2018.11.19_hsryu_Add
		 String unitList = MESMachineServiceProxy.getMachineServiceUtil().getUnitList(lotData, machineName, portName);
		 List<MQCCondition> mqcConditionList = null;
		 
		 try {
			 //unitName = StringUtils.isNotEmpty(unitName) ? unitName : "";
			// Modified by smkang on 2018.09.25 - Accroding to EDO's request, asterisk is permitted at UnitName.
//			 List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select("MACHINENAME = ? AND (UNITNAME IS NULL OR UNITNAME = ?) AND ISIDLETIMEOVER = ?", new Object[] {machineName, StringUtils.isNotEmpty(unitName) ? unitName : "", "Y"});
			 //List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select("MACHINENAME = ? AND (? IS NULL OR UNITNAME = ? OR UNITNAME = ?) AND ISIDLETIMEOVER = ?", new Object[] {machineName, unitName, unitName, "*", "Y"});
			 //2018.11.19_hsryu_Modify..
			 List<MachineIdleTime> machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select("MACHINENAME = ? AND (" + unitList + ") IS NULL OR UNITNAME IN (" + unitList + ") OR UNITNAME = ?) AND ISIDLETIMEOVER = ?", new Object[] {machineName, "*", "Y"});

			 return (machineIdleTimeList != null && machineIdleTimeList.size() > 0);
		 } catch (Exception e) {
			 return false;
		 }
	 }
	 
	 public String getUnitList(Lot lotData, String machineName, String portName) throws CustomException
	 {
		 String unitName = "";
		 Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		 Port portData = null;

		 // Modified by smkang on 2018.11.22 - SORTING mode is also checked as NORMAL mode. 
		 if(StringUtil.equals(CommonUtil.getValue(machineData.getUdfs(), "OPERATIONMODE"), GenericServiceProxy.getConstantMap().OPERATIONMODE_INDP))
		 {
    		 //2018.11.19_hsryu_For PEX&RequestTransportJob..
    		 if(StringUtils.isNotEmpty(portName))
    		 {
    			 try
    			 {
    				 portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
    			 }
    			 catch(Throwable e)
    			 {
    				 portData = null;
    				 log.error("PortData is not exist!");
    				 throw new CustomException("COMMON-0001", "PortData is not exist!");
    			 }
    			 
    			 if(portData!=null)
    			 {
    				unitName = portData.getUdfs().get("LINKEDUNITNAME");
    				
    				if(StringUtils.isEmpty(unitName))
       				 	throw new CustomException("COMMON-0001", "current OperationMode is 'INDP'. not define LinkedUnitName in PortData For checkIsIdleTimeOver");
    				
    				if(!checkProcessingUnit(machineName, unitName, lotData.getProcessOperationName(), "INDP"))
       				 	throw new CustomException("COMMON-0001", "can't Proceed.. Not Defined operationMode&TOPolicy.");
    				
    				unitName = "'" + portData.getUdfs().get("LINKEDUNITNAME") + "'";

    			 }
    		 }
    		 //For OPI..
    		 else
    		 {
    			 StringBuilder sql = new StringBuilder();

    			 sql.append(" SELECT TP.PROCESSOPERATIONNAME, PO.UNITNAME ");
    			 sql.append("   FROM TOPOLICY TP, POSOPERATIONMODE PO ");
    			 sql.append("  WHERE TP.FACTORYNAME = :FACTORYNAME ");
    			 sql.append("        AND TP.CONDITIONID = PO.CONDITIONID ");
    			 sql.append("        AND PO.MACHINENAME = :MACHINENAME ");
    			 sql.append("        AND PO.OPERATIONMODE = :OPERATIONMODE ");
    			 sql.append("        AND (TP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR TP.PROCESSOPERATIONNAME= :STAR) ");

    			 Map<String, Object> bindMap = new HashMap<String, Object>();
    			 bindMap.put("FACTORYNAME", lotData.getFactoryName());
    			 bindMap.put("MACHINENAME", machineName);
    			 bindMap.put("OPERATIONMODE", "INDP");
    			 bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
    			 bindMap.put("STAR", "*");

    			 @SuppressWarnings("unchecked")
    			 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

    			 if(sqlResult.size()>0)
    			 {
    				 for(int i=0; i<sqlResult.size(); i++)
    				 {
    					 if(i==sqlResult.size()-1)
    						 unitName += "'" + (String)sqlResult.get(i).get("UNITNAME") + "'";

    					 else
    						 unitName += "'" + (String)sqlResult.get(i).get("UNITNAME")+"',";
    				 }

    			 }
    			 else
    			 {
    				 throw new CustomException("COMMON-0001", "can't Proceed.. Not Defined operationMode&TOPolicy.");
    			 }
    		 }

    		 return unitName;
         }
         else
         {
        	 return "";
         }
	 }
	 
	 public boolean checkProcessingUnit(String machineName, String unitName, String processOperation, String operationMode)
	 {
		 boolean checkExistData = false;
		 
		 StringBuilder sql = new StringBuilder();

		 sql.append(" SELECT TP.PROCESSOPERATIONNAME, PO.UNITNAME ");
		 sql.append("   FROM TOPOLICY TP, POSOPERATIONMODE PO ");
		 sql.append("  WHERE 1=1 ");
		 sql.append("    AND TP.CONDITIONID = PO.CONDITIONID ");
		 sql.append("    AND PO.MACHINENAME = :MACHINENAME ");
		 sql.append("    AND PO.OPERATIONMODE = :OPERATIONMODE ");
		 sql.append("    AND (TP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME OR TP.PROCESSOPERATIONNAME = :STAR) ");
		 sql.append("    AND (PO.UNITNAME = :UNITNAME OR PO.UNITNAME = :STAR) ");

		 Map<String, Object> bindMap = new HashMap<String, Object>();
		 bindMap.put("MACHINENAME", machineName);
		 bindMap.put("OPERATIONMODE", "INDP");
		 bindMap.put("PROCESSOPERATIONNAME", processOperation);
		 bindMap.put("UNITNAME", unitName);
		 bindMap.put("STAR", "*");

		 @SuppressWarnings("unchecked")
		 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		 
		 if(sqlResult.size()>0)
		 {
			 checkExistData = true;
		 }
		 
		 return checkExistData;
	 }
	 
	 /**
	  * @author smkang
	  * @since 2018.10.07
	  * @param machineData
	  * @return boolean
	  * @see When a BC reports machine state, check it should be updated or not.
	  */	 
	public boolean needToUpdateBCMachineState(Machine machineData) {
		// Modified by smkang on 2018.10.13 - BC가 보고한 상태를 변경할 지 체크할 때 상태별 AccessFlag를 먼저 체크한 후 Y인 경우 ReasonCode별 AccessFlag를 체크하여 변경 여부 결정
//		if (machineData != null) {
//			try {
//				String sql = "SELECT REASONCODE" +
//				 	 	  	  "  FROM REASONCODE" +
//				 	 	  	  " WHERE REASONCODETYPE = :REASONCODETYPE" +
//				 	 	  	  "   AND REASONCODE = :REASONCODE" +
//				 	 	  	  "   AND BCACCESSFLAG = :BCACCESSFLAG";
//			 
//				 Map<String, String> bindMap = new HashMap<String, String>();
//				 bindMap.put("REASONCODETYPE", machineData.getReasonCodeType());
//				 bindMap.put("REASONCODE", machineData.getReasonCode());
//				 bindMap.put("BCACCESSFLAG", "Y");
//					
//				 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//				 
//				 if (sqlResult != null && sqlResult.size() > 0) {
//					 try {
//							MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
//							String previousMachineState = machineData.getMachineStateName();
//							
//							if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN))
//								return machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT);
//							else if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE))
//								return true;
//							else if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN))
//								return true;
//							else if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC))
//								return machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT);
//							else if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_PM))
//								return false;
//							else if (StringUtils.equals(previousMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME))
//								return false;
//						} catch (Exception e) {
//							log.warn(e);
//						}
//				 }
//			} catch (Exception e) {
//				log.info(e);
//			}
//		}
//		
//		return false;
		// Modified by smkang on 2018.10.18 - If ReasonCode of a machine is null, machine state should be updated.
		// Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
//		if (StringUtils.isNotEmpty(machineData.getReasonCode())) {
		if (StringUtils.isNotEmpty(machineData.getUdfs().get("STATEREASONCODE"))) {
			try {
				String sql = "SELECT REASONCODE, BCMACHINEACCESSFLAG, BCUNITACCESSFLAG, BCSUBUNITACCESSFLAG" +
				 	 	  	  "  FROM REASONCODE" +
				 	 	  	  " WHERE REASONCODETYPE = :REASONCODETYPE" +
				 	 	  	  "   AND REASONCODE = :REASONCODE";
			 
				 Map<String, String> bindMap = new HashMap<String, String>();
				 bindMap.put("REASONCODETYPE", "ChangeMachineState");
				 bindMap.put("REASONCODE", machineData.getMachineStateName());
					
				 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				 
				 if (sqlResult != null && sqlResult.size() > 0) {
					 try {
						 MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineData.getKey().getMachineName()));
						 
						 bindMap.clear();
						 bindMap.put("REASONCODETYPE", "ChangeMachineState");
						 
						 // Modified by smkang on 2018.10.25 - ReasonCode column is used commonly, so StateReasonCode is added.
//						 bindMap.put("REASONCODE", machineData.getReasonCode());
						 bindMap.put("REASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
						 
						 if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_MAIN)) {
							 if (sqlResult.get(0).get("BCMACHINEACCESSFLAG").toString().equals("Y")) {
								 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
								 
								 if (sqlResult != null && sqlResult.size() > 0)
									 return (sqlResult.get(0).get("BCMACHINEACCESSFLAG").toString().equals("Y"));
								 /* 20181113, hhlee, add, I set to the default reasoncode in case there is no reasoncode. */
                                 /* Only Array (requested by Guishi, HongWei) */
								 else
								     return true;								 
							 }
						 } else if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT)) {
							 if (sqlResult.get(0).get("BCUNITACCESSFLAG").toString().equals("Y")) {
								 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
								 
								 if (sqlResult != null && sqlResult.size() > 0)
									 return (sqlResult.get(0).get("BCUNITACCESSFLAG").toString().equals("Y"));
								 /* 20181113, hhlee, add, I set to the default reasoncode in case there is no reasoncode. */
                                 /* Only Array (requested by Guishi, HongWei) */
                                 else
                                     return true;                                 
							 }
						 } else if (machineSpecData.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT)) {
							 if (sqlResult.get(0).get("BCSUBUNITACCESSFLAG").toString().equals("Y")) {
								 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
								 
								 if (sqlResult != null && sqlResult.size() > 0)
									 return (sqlResult.get(0).get("BCSUBUNITACCESSFLAG").toString().equals("Y"));
								 /* 20181113, hhlee, add, I set to the default reasoncode in case there is no reasoncode. */
								 /* Only Array (requested by Guishi, HongWei) */
                                 else
                                     return true;                                 
							 }
						 }
					 } catch (Exception e) {
						 log.warn(e);
					 }
				 } 
			} catch (Exception e) {
				log.info(e);
			}
			
			return false;
		} else {
			return true;
		}
	}
	 
	 /**
	  * @author smkang
	  * @since 2018.10.08
	  * @param eventInfo
	  * @param machineName
	  * @param newMachineState
	  * @return EventInfo
	  * @see When a BC reports machine state, find detail ReasonCode and set it to EventInfo.
	  */
	 public EventInfo adjustMachineStateReasonCode(EventInfo eventInfo, String machineName, String newMachineState) {
		 // Modified by smkang on 2018.10.13 - RUN 상태일 경우는 WorkOrderType을 비교하고, 만약 WorkOrderType이 존재하지 않는다면 ProductionType으로 ReasonCode 결정
		 //									   CassetteCleaner는 P-Run으로 변경
//		 if (eventInfo != null) {
//			 try {
//				 if (StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_DOWN) ||
//					 StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_IDLE) ||
//					 StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC) ||
//					 StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_PM) ||
//					 StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_NONSCHEDULEDTIME)) {
//					 
//					 String sql = "SELECT REASONCODE" +
//						 	 	  "  FROM REASONCODE" +
//						 	      " WHERE REASONCODETYPE = :REASONCODETYPE" +
//						 	      "   AND SUPERREASONCODE = :SUPERREASONCODE";
//					 
//					 Map<String, String> bindMap = new HashMap<String, String>();
//					 bindMap.put("REASONCODETYPE", "ChangeMachineState");
//					 bindMap.put("SUPERREASONCODE", newMachineState);
//						
//					 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//					 
//					 if (sqlResult != null && sqlResult.size() > 0) {
//						 eventInfo.setReasonCodeType("ChangeMachineState");
//						 eventInfo.setReasonCode(sqlResult.get(0).get("REASONCODE").toString());
//					 }
//				 }
//				 else if (StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN)) {
//					 
//					 String sql = "SELECT PRODUCTREQUESTTYPE" +
//					 	 	  	  "  FROM PRODUCTREQUEST PR, PRODUCT P" +
//					 	 	  	  " WHERE PR.PRODUCTREQUESTNAME = P.PRODUCTREQUESTNAME" +
//					 	 	  	  "   AND P.MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME" + 
//					 	 	  	  " ORDER BY P.LASTEVENTTIMEKEY DESC";
//					 
//					 Map<String, String> bindMap = new HashMap<String, String>();
//					 bindMap.put("MATERIALLOCATIONNAME", machineName);
//					
//					 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//					 
//					 if (sqlResult != null && sqlResult.size() > 0) {
//						 sql = "SELECT REASONCODE" +
//							   "  FROM REASONCODE" +
//							   " WHERE REASONCODETYPE = :REASONCODETYPE" +
//							   "   AND SUPERREASONCODE = :SUPERREASONCODE" +
//							   "   AND DESCRIPTION = :DESCRIPTION";
//						 
//						 bindMap.put("REASONCODETYPE", "ChangeMachineState");
//						 bindMap.put("SUPERREASONCODE", newMachineState);
//						 
//						 String workOrderType = sqlResult.get(0).get("PRODUCTREQUESTTYPE").toString();
//						 if (StringUtils.isNotEmpty(workOrderType)) {
//							 bindMap.put("DESCRIPTION", (workOrderType.equals("MP") || workOrderType.equals("ME") || workOrderType.equals("RM")) ? "P-Run" : "E-Run");
//						 } else {
//							 bindMap.put("DESCRIPTION", "MQC-Run");
//						 }
//						 
//						 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
//						 
//						 if (sqlResult != null && sqlResult.size() > 0) {
//							 eventInfo.setReasonCodeType("ChangeMachineState");
//							 eventInfo.setReasonCode(sqlResult.get(0).get("REASONCODE").toString());
//						 }
//					 }
//				 }
//			} catch (Exception e) {
//				log.warn(e);
//			}
//		 }
//		 
//		 return eventInfo;
		 if (eventInfo != null) {
			 try {
			     /* 20181027, hhlee, modify, ARRAY는 MACHINE_STATE_RUN, MACHINE_STATE_MQC 일 경우 ReasonCode를 변경 가능하고, OLED는 MACHINE_STATE_RUN 일때 ReasonCode를 변경이 가능하다.*/
				 //if (StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN)) {
			     if (StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_RUN) || 
			             StringUtils.equals(newMachineState, GenericServiceProxy.getConstantMap().MACHINE_STATE_MQC)) {
					 // 현재 위치에 가장 최근 투입된 Product 조회
					 String sql = "SELECT P.PRODUCTNAME, P.PRODUCTREQUESTNAME, P.PRODUCTIONTYPE" +
					 	 	  	  "  FROM PRODUCT P" +
					 	 	  	  " WHERE P.MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME" + 
					 	 	  	  "   AND P.PRODUCTSTATE = :PRODUCTSTATE" + 
					 	 	  	  " ORDER BY P.LASTEVENTTIMEKEY DESC";
					 
					 Map<String, String> bindMap = new HashMap<String, String>();
					 bindMap.put("MATERIALLOCATIONNAME", machineName);
					 /* 20190625, hhlee, modify, added validation PRODUCTSTATE = 'InProduction' */
					 bindMap.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_InProduction);
					
					 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
					 
					 if (sqlResult != null && sqlResult.size() > 0) {
						 // Modified by smkang on 2018.10.26 - ProductRequestName of MQC is null.
//						 String productRequestName = sqlResult.get(0).get("PRODUCTREQUESTNAME").toString();
						 String productRequestName = (sqlResult.get(0).get("PRODUCTREQUESTNAME") != null) ? sqlResult.get(0).get("PRODUCTREQUESTNAME").toString() : "";
						 
						 String productionType = sqlResult.get(0).get("PRODUCTIONTYPE").toString();
						 
						 // WorkOrder가 존재하는 Product라면 WorkOrderType에 따라 ReasonCode 결정
						 sql = "SELECT EV.RUNSTATEREASONCODE" +
							   "  FROM PRODUCTREQUEST PR, ENUMDEFVALUE EV" +
							   " WHERE PR.PRODUCTREQUESTTYPE = EV.ENUMNAME" +
							   "   AND PR.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME";
						 
						 bindMap.clear();
						 bindMap.put("PRODUCTREQUESTNAME", productRequestName);
						 
						 if (productionType.equals("DMQC")) {
							 sql += "   AND EV.TAG = :ENUMGROUP";
							 bindMap.put("ENUMGROUP", productionType);
						 } else {
							 sql += "   AND EV.TAG IS NULL";							 
						 }
						 
						 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
						 
						 if (sqlResult != null && sqlResult.size() > 0) {
							 eventInfo.setReasonCodeType("ChangeMachineState");
							 eventInfo.setReasonCode(sqlResult.get(0).get("RUNSTATEREASONCODE").toString());
							 
							 return eventInfo;
						 } else {
							// WorkOrder가 존재하지 않는 Product라면 ProductionType에 따라 ReasonCode 결정
							 sql = "SELECT EV.RUNSTATEREASONCODE" +
								   "  FROM ENUMDEFVALUE EV" +
								   " WHERE EV.ENUMNAME = :ENUMNAME" +
								   "   AND EV.ENUMVALUE = :ENUMVALUE";
							 
							 bindMap.clear();
							 bindMap.put("ENUMNAME", "ProductionType");
							 bindMap.put("ENUMVALUE", productionType);
							 
							 sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
							 
							 if (sqlResult != null && sqlResult.size() > 0) {
								 eventInfo.setReasonCodeType("ChangeMachineState");
								 eventInfo.setReasonCode(sqlResult.get(0).get("RUNSTATEREASONCODE").toString());
								 
								 return eventInfo;
							 }
						 }
					 }
				 }
				 
				 MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));
				 
				 // RUN 상태로 변경하는 것이 아니거나 Product가 존재하지 않는다면 StateModelState의 DefaultReasonCode로 결정
				 String sql = "SELECT SM.DEFAULTREASONCODE" +
					 	 	  "  FROM STATEMODELSTATE SM" +
					 	      " WHERE SM.STATEMODELNAME = :STATEMODELNAME" +
					 	      "   AND SM.STATENAME = :STATENAME";
				 
				 Map<String, String> bindMap = new HashMap<String, String>();
				 bindMap.put("STATEMODELNAME", machineSpecData.getMachineStateModelName());
				 bindMap.put("STATENAME", newMachineState);
					
				 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				 
				 if (sqlResult != null && sqlResult.size() > 0) {
					 eventInfo.setReasonCodeType("ChangeMachineState");
					 eventInfo.setReasonCode(sqlResult.get(0).get("DEFAULTREASONCODE").toString());
					 
					 return eventInfo;
				 }
			 } catch (Exception e) {
				 log.warn(e);
			 }
		 }
		 
		 return eventInfo;
	 }
	 
	 public String getRunStateReasonCode(String enumName, String enumValue) 
	 {
		 String sql = "SELECT EV.RUNSTATEREASONCODE" +
			   "  FROM ENUMDEFVALUE EV" +
			   " WHERE EV.ENUMNAME = :ENUMNAME" +
			   "   AND EV.ENUMVALUE = :ENUMVALUE";
		 
		 Map<String, String> bindMap = new HashMap<String, String>();
		 bindMap.clear();
		 bindMap.put("ENUMNAME", enumName);
		 bindMap.put("ENUMVALUE", enumValue);
							 
		 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		 String runReaonCode = "";
		 if (sqlResult != null && sqlResult.size() > 0) 
		 {
			 runReaonCode = sqlResult.get(0).get("RUNSTATEREASONCODE").toString();
		 }
		 return runReaonCode;
	 }
	 
	 public String getReasonCode(String reasonCode) 
	 {
		 String sql = " SELECT A.SUPERREASONCODE FROM REASONCODE A WHERE A.REASONCODE = :REASONCODE" ;
		 
		 Map<String, String> bindMap = new HashMap<String, String>();
		 bindMap.clear();
		 bindMap.put("REASONCODE", reasonCode);
							 
		 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		 String runReaonCode = "";
		 if (sqlResult != null && sqlResult.size() > 0) 
		 {
			 runReaonCode = sqlResult.get(0).get("SUPERREASONCODE").toString();
		 }
		 return runReaonCode;
	 }
	 
	 public String getINDPMode(String unitName) 
	 {
		 String sql = " SELECT DISTINCT PO.OPERATIONMODE FROM TOPOLICY TP, POSOPERATIONMODE PO WHERE 1=1 AND TP.CONDITIONID = PO.CONDITIONID AND PO.UNITNAME = :UNITNAME " ;
		 Map<String, String> bindMap = new HashMap<String, String>();
		 bindMap.clear();
		 bindMap.put("UNITNAME", unitName);
		 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		 String indpMode = "";
		 if (sqlResult != null && sqlResult.size() > 0) 
		 {
			 indpMode = sqlResult.get(0).get("OPERATIONMODE").toString();
		 }
		 return indpMode;
	 }
	 
	 
	 /**
	  * 
	  * @Name     machineStateModelEventReasonCode
	  * @since    2018. 10. 27.
	  * @author   hhlee
	  * @contents 
	  *           
	  * @param eventInfo
	  * @param machineData
	  * @param stateModelName
	  * @param oldMachineState
	  * @param newMachineState
	  */
	 public boolean machineStateModelEventReasonCode(EventInfo eventInfo, Machine machineData, String stateModelName, String oldMachineState, String newMachineState) 
	 {
	     try 
	     {
	         String strSql = " SELECT NVL(SME.STATEREASONCODE, :STATEREASONCODE) AS STATEREASONCODE \n" 
                           + "   FROM STATEMODELEVENT SME                              \n" 
                           + "  WHERE 1=1                                              \n" 
                           + "    AND SME.STATEMODELNAME = :STATEMODELNAME             \n" 
                           + "    AND SME.OLDSTATENAME = :OLDSTATENAME                 \n" 
                           + "    AND SME.NEWSTATENAME = :NEWSTATENAME                   ";
             
             Map<String, String> bindMap = new HashMap<String, String>();
             bindMap.put("STATEMODELNAME", stateModelName);
             bindMap.put("OLDSTATENAME", oldMachineState);
             bindMap.put("NEWSTATENAME", newMachineState);
             bindMap.put("STATEREASONCODE", " ");
                
             List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindMap);
             
             if (sqlResult != null && sqlResult.size() > 0) 
             {
                 String stateReasonCode = StringUtil.trim(sqlResult.get(0).get("STATEREASONCODE").toString());
                                  
                 if(StringUtil.isNotEmpty(stateReasonCode))
                 {
                     if (!stateReasonCode.equals(machineData.getUdfs().get("STATEREASONCODE"))) 
                     {
                         eventInfo.setReasonCodeType("ChangeMachineState");
                         eventInfo.setReasonCode(stateReasonCode);
                         
                         // Added by smkang on 2018.11.15 - According to Wangli's request, OldStateReasonCode is added.
                         machineData.getUdfs().put("OLDSTATEREASONCODE", machineData.getUdfs().get("STATEREASONCODE"));
     					
                         machineData.getUdfs().put("STATEREASONCODE", stateReasonCode);                     
                         kr.co.aim.greentrack.machine.management.info.SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
                         //MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);//modfiy by GJJ  mantis 5870 뫘劤샙憩榴檄嫩끽，돔鈴old宅new돨status宮谿 
             			 MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);//add by GJJ  mantis 5870 뫘劤샙憩榴檄嫩끽，돔鈴old宅new돨status宮谿 

                         return true;
                     }
                 }
             }
	     } 
	     catch (Exception e) {
             log.warn(e);
         }
	     
	     return false;
	 }
	 
	 /**
	  * @author smkang
	  * @since 2018.10.08
	  * @param machineName
	  * @return allUnitAndSubUnitNameList
	  * @see According to EDO's request, if a user change machine state to PM or NONSCHEDULEDTIME, state of all units and sub-units should be also changed.
	  *      So this method will return all unit and sub-unit names.
	  */
	 public List<String> getAllUnitAndSubUnitNameList(String machineName) {
		 List<String> allUnitAndSubUnitNameList = new ArrayList<String>();
		 
		 if (StringUtils.isNotEmpty(machineName)) {
			 try {
				 String sql = "WITH UNITNAMES AS (SELECT DISTINCT(MS.UNITNAME)" +
						 	  "                     FROM (SELECT UNIT.SUPERMACHINENAME AS MACHINENAME, UNIT.MACHINENAME AS UNITNAME" +
						 	  "                             FROM MACHINESPEC UNIT, MACHINESPEC SUBUNIT" +
						 	  "                            WHERE UNIT.MACHINENAME = SUBUNIT.SUPERMACHINENAME (+)" +
						 	  "                              AND UNIT.DETAILMACHINETYPE = :UNITTYPE) MS" +
						 	  "                    WHERE MS.MACHINENAME = :MACHINENAME) " +
						 	  "SELECT UNITNAME" +
						 	  "  FROM UNITNAMES" +
						 	  " UNION " +
						 	  "SELECT DISTINCT(MS.UNITNAME)" +
						 	  "  FROM (SELECT UNIT.SUPERMACHINENAME AS MACHINENAME, UNIT.MACHINENAME AS UNITNAME" +
						 	  "          FROM MACHINESPEC UNIT, MACHINESPEC SUBUNIT" +
						 	  "         WHERE UNIT.MACHINENAME = SUBUNIT.SUPERMACHINENAME (+)" +
						 	  "           AND UNIT.DETAILMACHINETYPE = :SUBUNITTYPE) MS" +
						 	  " WHERE MS.MACHINENAME IN (SELECT UNITNAME FROM UNITNAMES)";
				 
				 Map<String, String> bindMap = new HashMap<String, String>();
				 bindMap.put("UNITTYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_UNIT);
				 bindMap.put("MACHINENAME", machineName);
				 bindMap.put("SUBUNITTYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT);
				
				 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				 
				 if (sqlResult != null && sqlResult.size() > 0) {
					 for (Map<String, Object> map : sqlResult) {
						 allUnitAndSubUnitNameList.add(map.get("UNITNAME").toString());
					 }
				 }
				 
				 allUnitAndSubUnitNameList.add(machineName);
			} catch (Exception e) {
				log.warn(e);
			}
		 }

		 return allUnitAndSubUnitNameList;
	 }
	 
	 /**
	  * @author smkang
	  * @since 2018.10.27
	  * @param unitName
	  * @return allSubUnitNameList
	  * @see According to EDO's request, if a user change unit state to PM or NONSCHEDULEDTIME, state of all subunits should be also changed.
	  *      So this method will return all subunit names.
	  */
	 public List<String> getAllSubUnitNameList(String unitName) {
		 List<String> allSubUnitNameList = new ArrayList<String>();
		 
		 if (StringUtils.isNotEmpty(unitName)) {
			 try {
				 String sql = "SELECT DISTINCT(MACHINENAME)" +
						 	  "  FROM MACHINESPEC" +
						 	  " WHERE DETAILMACHINETYPE = :UNITTYPE" +
						 	  "   AND SUPERMACHINENAME = :UNITNAME";
				 
				 Map<String, String> bindMap = new HashMap<String, String>();
				 bindMap.put("UNITTYPE", GenericServiceProxy.getConstantMap().RECIPE_TYPE_SUBUNIT);
				 bindMap.put("UNITNAME", unitName);
				
				 List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				 
				 if (sqlResult != null && sqlResult.size() > 0) {
					 for (Map<String, Object> map : sqlResult) {
						 allSubUnitNameList.add(map.get("MACHINENAME").toString());
					 }
				 }
				 
				 allSubUnitNameList.add(unitName);
			} catch (Exception e) {
				log.warn(e);
			}
		 }

		 return allSubUnitNameList;
	 }
	 
	 /**
      * 
      * @Name     getMachineOperationGroup
      * @since    2018. 9. 30.
      * @author   hhlee
      * @contents 
      *           
      * @param machineName
      * @return
      * @throws CustomException
      */
     public String getMachineOperationGroup( String machineName, String machineGroupName) throws CustomException
     {
         String machineOperationGroup = StringUtil.EMPTY;
         
         try
         {               
             List<MachineGroupMachine> machinegroupmachineList = ExtendedObjectProxy.getMachineGroupMachineService().select(" WHERE MACHINENAME = ? AND MACHINEGROUPNAME = ? ", new Object[] {machineName, machineGroupName});
             machineOperationGroup =  machinegroupmachineList.get(0).getMachineGroupName();
             
         } 
         catch( Exception ex )
         {
             //log.warn(ex.getStackTrace());
             throw new CustomException("LOT-9027", machineName, machineGroupName);    
             
         }
         
         return machineOperationGroup;
     }
     
     /**
      * 
      * @Name     checkExistenceLinkedUnitName
      * @since    2018. 10. 13.
      * @author   Administrator
      * @contents 
      *           
      * @param machineName
      * @param machineType
      * @throws CustomException
      */
     public void checkExistenceLinkedUnitName(String machineName, String machineType ) throws CustomException
     {
       
         String strSql = " SELECT MACHINENAME          \n"
                       + "   FROM MACHINESPEC          \n"
                       + "  WHERE SUPERMACHINENAME = ? \n"
                       + "    AND MACHINETYPE = ?      \n" ;
     
         Object[] bindSet = new String[]{machineName, machineType};
     
         List<ListOrderedMap> unitList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSql, bindSet);
         
         if(unitList == null || unitList.size() <= 0)
         {
             throw new CustomException("MACHINE-9006", machineName);
         }
         
         String unitlistName = "";
         
         for(ListOrderedMap unit : unitList)
         {
             if(StringUtil.isEmpty(unitlistName))
             {
                 unitlistName = "'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
             }
             else
             {
                 unitlistName += ",'" + CommonUtil.getValue(unit, "MACHINENAME") + "'";
             }
         }
         
         strSql = " SELECT P.LINKEDUNITNAME                           \n"
                + "  FROM PORT P                                      \n"
                + " WHERE 1=1                                         \n"
                + "   AND P.LINKEDUNITNAME IN ( " + unitlistName + ") \n"
                + " GROUP BY P.LINKEDUNITNAME                           ";
                 
         Map<String, Object> bindMap = new HashMap<String, Object>();
                  
         List<Map<String, Object>> linkedUnitData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
         
         if (linkedUnitData == null || linkedUnitData.size() <= 0 ||
                 unitList.size() != linkedUnitData.size())
         {
             throw new CustomException("PORT-9005",machineName, unitList.size(), linkedUnitData.size());
         }         
     }
     
 	/**
 	 * @author hsryu
 	 * @since 2018.11.17
 	 * @param machineName
 	 * @param unitName
 	 * @param lotData
 	 * @see check MachineIdleTimeOver.
 	 */
 	public void checkMachineIdleTimeOver(String machineName, Lot lotData, String portName) throws CustomException {
 		
 		String unitList = MESMachineServiceProxy.getMachineServiceUtil().getUnitList(lotData, machineName, portName);
 		
 		if (StringUtils.isNotEmpty(machineName) && lotData != null) {
 			List<MachineIdleTime> machineIdleTimeList = null;
 			String condition = "";
 			Object[] bindSet = null;
 			if (StringUtils.isNotEmpty(unitList.replace("'", ""))) {
 				condition = "MACHINENAME = ? AND (UNITNAME IN (" + unitList + "))" ;
 				bindSet = new Object[] {machineName};

 				try
 				{
 					machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
 				}
 				catch(Throwable e)
 				{
 					//not exist machineIdleTimeList Data.
 				}

 			} 
 			else {
 				//condition = "MACHINENAME = ? AND UNITNAME IN (" + unitList + ")" ;
 				condition = "MACHINENAME = ? ";
 				bindSet = new Object[] {machineName};

 				try
 				{
 					machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
 				}
 				catch(Throwable e)
 				{
 					//not exist machineIdleTimeList Data.
 				}
 			}

 			if(machineIdleTimeList!=null)
 			{
 				for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
 					if (machineIdleTime.getIsIdleTimeOver().equals("Y")) 
 					{

 						if (!lotData.getProductionType().equals("MQCA"))
 						{
 							String resetType = machineIdleTime.getResetType();
 							boolean checkSingleMQCCondition = false;
 							
 							List<MQCCondition> mqcConditionList = null;
 							
 							try {
 								mqcConditionList = ExtendedObjectProxy.getMQCConditionService().select("MACHINENAME = ? AND UNITNAME = ? ", new Object[] {machineName, machineIdleTime.getUnitName()});
 							} catch (Exception e) {
 								log.info(e);
 							}
 							
 							if(mqcConditionList != null && mqcConditionList.size() > 0){
 	 							for( MQCCondition mqcCondition : mqcConditionList ){
 	 	 							if(StringUtils.equals(resetType, "MULTI")){
 	 	 								if(mqcCondition.getMqcRunCount() + mqcCondition.getMqcPreRunCount() < mqcCondition.getMqcPlanCount()){
 	 	 									log.info("ResetType is Multi");
 	 	 									log.info("CountInfo. MQCRunCount : " + mqcCondition.getMqcRunCount() + ", MQCPreRunCount : " + mqcCondition.getMqcPreRunCount());
 	 	 									throw new CustomException("MACHINE-0204");
 	 	 								}
 	 	 							}
 	 	 							else{
 	 	 								log.info("ResetType is Single");
 	 	 								if(mqcCondition.getMqcRunCount() + mqcCondition.getMqcPreRunCount() >= mqcCondition.getMqcPlanCount()){
 	 	 									log.info("CountInfo. MQCRunCount : " + mqcCondition.getMqcRunCount() + ", MQCPreRunCount : " + mqcCondition.getMqcPreRunCount());
 	 	 									checkSingleMQCCondition = true;
 	 	 									break;
 	 	 								}
 	 	 							}
 	 							}
 	 							
 	 							if(StringUtils.equals(resetType, "SINGLE") && !checkSingleMQCCondition){
 	 								throw new CustomException("MACHINE-0204");
 	 							}
 							}
 						}
 					}
 					
 				}
 			}
 		}
 	}
 	
	
	public void checkMachineIdleTimeOverForOPI(String machineName, Lot lotData, String portName) throws CustomException {
		
		String unitList = MESMachineServiceProxy.getMachineServiceUtil().getUnitList(lotData, machineName, portName);
		
		if (StringUtils.isNotEmpty(machineName) && lotData != null) {
			List<MachineIdleTime> machineIdleTimeList = null;
			String condition = "";
			Object[] bindSet = null;
			
			if (StringUtils.isNotEmpty(unitList.replace("'", ""))) {
				condition = "MACHINENAME = ? AND (UNITNAME IN (" + unitList + "))" ;
				bindSet = new Object[] {machineName};

				try
				{
					machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
				}
				catch(Throwable e)
				{
					//not exist machineIdleTimeList Data.
				}

			} 
			else {
				condition = "MACHINENAME = ? ";
				bindSet = new Object[] {machineName};

				try
				{
					machineIdleTimeList = ExtendedObjectProxy.getMachineIdleTimeService().select(condition, bindSet);
				}
				catch(Throwable e)
				{
					//not exist machineIdleTimeList Data.
				}
			}

			if(machineIdleTimeList!=null)
			{
				for (MachineIdleTime machineIdleTime : machineIdleTimeList) {
					if (machineIdleTime.getIsIdleTimeOver().equals("Y")) 
					{
						if (!lotData.getProductionType().equals("MQCA")) 
						{	
							throw new CustomException("MACHINE-0204");
						}
					}

				}
			}
		}
	}
}