package kr.co.aim.messolution.alarm.event.ALM;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MachineAlarmStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmState = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", true);
		String alarmText = SMessageUtil.getBodyItemValue(doc, "ALARMTEXT", false);
		String subUnitName = "";
		
		
		EventInfo eventInfo = checkSameAlarmJob(machineName, unitName, alarmCode, subUnitName);
		
		if (StringUtils.equals(alarmState, "SET"))
			alarmState = GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE;
		

		if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE))
			eventInfo.setEventName("Issue");
		else if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_CLEAR))
			eventInfo.setEventName("Clear");
		else
		{
			try
			{
			  	GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
				
			    String sql = " DELETE CT_MACHINEALARMJOB WHERE MACHINENAME=:MACHINENAME and UNITNAME=:UNITNAME and SUBUNITNAME=:SUBUNITNAME AND ALARMCODE=:ALARMCODE ";
			
			    Map<String,Object> bindMap = new HashMap<String,Object>();
			    bindMap.put("MACHINENAME" , machineName);
			    bindMap.put("UNITNAME" , unitName);
			    bindMap.put("SUBUNITNAME" , StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ");
			    bindMap.put("ALARMCODE" , alarmCode);
			
			    GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}
			catch(Exception e)
			{   
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			}
			finally
			{
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			}
			
			return;
		}
			
		
		MachineAlarmList machineAlarmData = new MachineAlarmList(machineName, unitName, subUnitName, alarmCode);
		machineAlarmData.setAlarmState(alarmState);
		machineAlarmData.setAlarmSeverity("");
		machineAlarmData.setAlarmText(alarmText);
		machineAlarmData.setProductList("");
		
		MESAlarmServiceProxy.getAlarmServiceImpl().setMachineAlarm(machineAlarmData, machineName, unitName, subUnitName, alarmCode, alarmState, eventInfo);
		
		try {
          AlarmDefinition alarmDefinitionData = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(true, new Object[] {alarmCode});
          
          try {
        	  MESAlarmServiceProxy.getAlarmServiceImpl().createAlarm(alarmCode, alarmDefinitionData.getAlarmSeverity(), alarmState,
        			  alarmDefinitionData.getAlarmType(), alarmDefinitionData.getDescription(), alarmDefinitionData.getFactoryName(), machineName, unitName, subUnitName, machineAlarmData.getProductList(), eventInfo);
        	  
        	  if (StringUtils.equals(alarmState, GenericServiceProxy.getConstantMap().ALARMSTATE_ISSUE))
        		  MESAlarmServiceProxy.getAlarmServiceImpl().doAlarmAction(eventInfo, doc, alarmDefinitionData);
          } catch (Exception ex) {
        	  eventLog.error(ex);
          }
      } catch(Exception ex) {
          eventLog.warn(ex.getLocalizedMessage());
      }
      
      //success then report to FMC
      try {
          /* 20181207, hhlee, Add, add item "ALARMTYPE" ==>> */
          SMessageUtil.setBodyItemValue(doc, "ALARMTYPE", "EQP", true);
          /* <<== 20181207, hhlee, Add, add item "ALARMTYPE" */
          
          GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
      } catch(Exception ex) {
    	  eventLog.warn("FMC Report Failed!");
      }
      
      
		try
		{
		  	GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			
		    String sql = " DELETE CT_MACHINEALARMJOB WHERE MACHINENAME=:MACHINENAME and UNITNAME=:UNITNAME and SUBUNITNAME=:SUBUNITNAME AND ALARMCODE=:ALARMCODE ";
		
		    Map<String,Object> bindMap = new HashMap<String,Object>();
		    bindMap.put("MACHINENAME" , machineName);
		    bindMap.put("UNITNAME" , unitName);
		    bindMap.put("SUBUNITNAME" , StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ");
		    bindMap.put("ALARMCODE" , alarmCode);
		
		    GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{   
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
		finally
		{
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
      
      
      
	}

	private EventInfo checkSameAlarmJob(String machineName, String unitName, String alarmCode, String subUnitName)
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		try
        {
        	try
        	{
        		String sqlSelect =  " SELECT MACHINENAME, " +
       				 "       UNITNAME, " +
       				 "       SUBUNITNAME, " +
       				 "       ALARMCODE, " +
       				 "       EVENTNAME, " +
       				 "       TRANSACTIONID, " +
       				 "       TIMEKEY " +
       				 "FROM CT_MACHINEALARMJOB " +
       				 "WHERE MACHINENAME = :MACHINENAME " +
       				 "      AND UNITNAME = :UNITNAME " +
       				 "      AND SUBUNITNAME = :SUBUNITNAME " +
       				 "      AND ALARMCODE = :ALARMCODE " ;

            
                Map<String, String> bindMapS = new HashMap<String, String>();
                bindMapS.put("MACHINENAME" , machineName);
                bindMapS.put("UNITNAME" , unitName);
                bindMapS.put("SUBUNITNAME" , StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ");
                bindMapS.put("ALARMCODE" , alarmCode);

                List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSelect, bindMapS);
                
                
                if(sqlResult.size() > 0)
                {
                	int limit = 1;
   					while(limit < 50)
   					{	
   						eventLog.info("-----WAIT SAME MACHINE ALARM JOB . . . .  ------");
   						
   		        		String sqlSelect2 =  " SELECT MACHINENAME, " +
   		       				 "       UNITNAME, " +
   		       				 "       SUBUNITNAME, " +
   		       				 "       ALARMCODE, " +
   		       				 "       EVENTNAME, " +
   		       				 "       TRANSACTIONID, " +
   		       				 "       TIMEKEY " +
   		       				 "FROM CT_MACHINEALARMJOB " +
   		       				 "WHERE MACHINENAME = :MACHINENAME " +
   		       				 "      AND UNITNAME = :UNITNAME " +
   		       				 "      AND SUBUNITNAME = :SUBUNITNAME " +
   		       				 "      AND ALARMCODE = :ALARMCODE " ;

   		            
   		                Map<String, String> bindMapS2 = new HashMap<String, String>();
   		                bindMapS2.put("MACHINENAME" , machineName);
	   		            bindMapS2.put("UNITNAME" , unitName);
	   		            bindMapS2.put("SUBUNITNAME" , StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ");
	   		            bindMapS2.put("ALARMCODE" , alarmCode);

   		                List<Map<String, Object>> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSelect2, bindMapS2);
   		                
   		                if(sqlResult2.size() == 0)
   		                {
   		                	break;
   		                }
   		                else
   		                {
	   		  				String timeKey = sqlResult2.get(0).get("TIMEKEY").toString();
	   		  				String nowTimeKey = eventInfo.getEventTimeKey();
	   		  				
	   		  				timeKey = timeKey.substring(0, 14);
	   		  			
	   		  				nowTimeKey = nowTimeKey.substring(0, 14);
	   		  				
	   		  				if(!timeKey.substring(0, 14).equals(nowTimeKey.substring(0, 14)))
	   		  				{
	   		  					break;
	   		  				}
   		                }
   		                
   		                limit++;
   					}
                }
        	}
        	catch(Exception ex)
        	{
        		return eventInfo;
        	}

        	
        	GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
        	
            String sql = " MERGE INTO CT_MACHINEALARMJOB A USING " +
            		 " (SELECT " +
            		 "  :MACHINENAME as MACHINENAME, " +
            		 "  :UNITNAME as UNITNAME, " +
            		 "  :SUBUNITNAME as SUBUNITNAME, " +
            		 "  :ALARMCODE as ALARMCODE, " +
            		 "  :EVENTNAME as EVENTNAME, " +
            		 "  :TRANSACTIONID as TRANSACTIONID, " +
            		 "  :TIMEKEY as TIMEKEY " +
            		 "  FROM DUAL) B " +
            		 "ON (A.MACHINENAME = B.MACHINENAME and A.UNITNAME = B.UNITNAME and A.SUBUNITNAME = B.SUBUNITNAME and A.ALARMCODE = B.ALARMCODE) " +
            		 "WHEN NOT MATCHED THEN  " +
            		 "INSERT ( " +
            		 "  MACHINENAME, UNITNAME, SUBUNITNAME, ALARMCODE, EVENTNAME,  " +
            		 "  TRANSACTIONID, TIMEKEY) " +
            		 "VALUES ( " +
            		 "  B.MACHINENAME, B.UNITNAME, B.SUBUNITNAME, B.ALARMCODE, B.EVENTNAME,  " +
            		 "  B.TRANSACTIONID, B.TIMEKEY) " +
            		 "WHEN MATCHED THEN " +
            		 "UPDATE SET  " +
            		 "  A.EVENTNAME = B.EVENTNAME, " +
            		 "  A.TRANSACTIONID = B.TRANSACTIONID, " +
            		 "  A.TIMEKEY = B.TIMEKEY " ;
    
            Map<String,Object> bindMap = new HashMap<String,Object>();
            bindMap.put("MACHINENAME" , machineName);
            bindMap.put("UNITNAME" , unitName);
            bindMap.put("SUBUNITNAME" , StringUtils.isNotEmpty(subUnitName) ? subUnitName : " ");
            bindMap.put("ALARMCODE" , alarmCode);
            bindMap.put("EVENTNAME" , getMessageName());
            bindMap.put("TRANSACTIONID" ,getTransactionId());
            bindMap.put("TIMEKEY" , eventInfo.getEventTimeKey());

            GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
        }
        catch(Exception e)
        {   
        	GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
        	
        	return eventInfo;
        }
        finally
        {
        	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
        	
        }
		
		return eventInfo;
	}
}