package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RegisterLocalRunException extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{	
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", false);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", false);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);		
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		
		
		StringBuilder sqlStatement = new StringBuilder();
		Map<String, String> bindMap = new HashMap<String, String>();
		
	
		
		sqlStatement.append("SELECT * FROM CT_LOCALRUNEXCEPTION WHERE 1=1 ");
		
		if(lotName == "")
		{
			sqlStatement.append("AND LOTNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND LOTNAME = :LOTNAME ");
			bindMap.put("LOTNAME", lotName);
		}
		if(processFlowName == "")
		{
			sqlStatement.append("AND PROCESSFLOWNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			bindMap.put("PROCESSFLOWNAME", processFlowName);
		}
		if(processOperationName == "")
		{
			sqlStatement.append("AND PROCESSOPERATIONNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		}
		
		if(machineName == "")
		{
			sqlStatement.append("AND MACHINENAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND MACHINENAME = :MACHINENAME ");
			bindMap.put("MACHINENAME", machineName);
		}
		if(unitName == "")
		{
			sqlStatement.append("AND UNITNAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND UNITNAME = :UNITNAME ");
			bindMap.put("UNITNAME", unitName);
		}
		
		if(recipeName == "")
		{
			sqlStatement.append("AND RECIPENAME IS NULL ");
		}
		else
		{
			sqlStatement.append("AND RECIPENAME = :RECIPENAME ");
			bindMap.put("RECIPENAME", recipeName);
		}
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStatement.toString(), bindMap);
		
		if(sqlResult.size() > 0)
		{
			throw new CustomException("LocalRunException-0001", ""); 
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("RegisterLocalRunException", this.getEventUser(), this.getEventComment(), "", "");
			
	        Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			// if Lot In Sort ProcessFlow Throw Error Add By Park Jeong su
			ProcessFlowKey processFlowKey = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			if (StringUtils.equals("Sort", processFlow.getProcessFlowType()))
			{
				throw new CustomException("COMMON-0001", "it is not possible to do lot hold or reserve lot hold in sorter flow.");
			}

			LocalRunException  localRunExceptionData = new LocalRunException(lotName,processFlowName,processOperationName,machineName);
			
			//localRunExceptionData.setLotName(lotName);
			localRunExceptionData.setCarrierName(carrierName);		
			localRunExceptionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
			localRunExceptionData.setProductSpecName(productSpecName);
			localRunExceptionData.setEcCode(ecCode);
			localRunExceptionData.setProductionType(productionType);
			localRunExceptionData.setProductQuantity(productQuantity);
		//	localRunExceptionData.setProcessFlowName(processFlowName);
			localRunExceptionData.setProcessFlowVersion(processFlowVersion);
		//	localRunExceptionData.setProcessOperationName(processOperationName);
			localRunExceptionData.setMachineGroupName(machineGroupName);
		//	localRunExceptionData.setMachineName(machineName);
			localRunExceptionData.setUnitName(unitName);
			localRunExceptionData.setRecipeName(recipeName);
			localRunExceptionData.setDepartment(department);
			
			localRunExceptionData.setLastEventName(eventInfo.getEventName());
			localRunExceptionData.setLastEventTimekey(eventInfo.getEventTimeKey());
			localRunExceptionData.setLastEventTime(eventInfo.getEventTime());
			localRunExceptionData.setLastEventUser(eventInfo.getEventUser());
			localRunExceptionData.setLastEventComment(eventInfo.getEventComment());
			localRunExceptionData.setCreateTime(eventInfo.getEventTime());
			localRunExceptionData.setCreateUser(eventInfo.getEventUser());
			
		   ExtendedObjectProxy.getLocalRunExceptionService().create(eventInfo, localRunExceptionData);
		   
		 //Get Last Position
		   
			long lastPosition = Integer.parseInt(this.getLastPosition(lotName, GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, processFlowName, processOperationName));
		  //Create CT_LOTACTION
			LotAction lotActionData = new LotAction(lotName, "", processFlowName,"00001", processOperationName, "00001", lastPosition + 1);
			
			String actionName = "";
			eventInfo.setEventName("FutureBHold");
			actionName = GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD;
			
			lotActionData.setPosition(lastPosition+1);
			lotActionData.setActionName(actionName);
			lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
			lotActionData.setFactoryName(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY);
			lotActionData.setHoldCode("RLRE");
			lotActionData.setHoldPermanentFlag("N");
			lotActionData.setHoldType("BHOLD");
			lotActionData.setLastEventName(eventInfo.getEventName());
			lotActionData.setLastEventTime(eventInfo.getEventTime());
			lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotActionData.setLastEventUser(eventInfo.getEventUser());
			lotActionData.setLastEventComment(eventInfo.getEventComment());
			lotActionData.setDepartment(department);
			lotActionData.setHoldCount(0);

			ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);

		}
		return doc;
	}

	public String getLastPosition(String lotName, String factoryName, String flowName, String operationName)
	{
		String getPositionSql = "SELECT POSITION "
				+ " FROM CT_LOTACTION "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " ORDER BY POSITION DESC";
		
		Map<String, Object> getPositionBind = new HashMap<String, Object>();
		getPositionBind.put("LOTNAME", lotName);
		getPositionBind.put("FACTORYNAME", factoryName);
		getPositionBind.put("PROCESSFLOWNAME", flowName);
		getPositionBind.put("PROCESSFLOWVERSION", "00001");
		getPositionBind.put("PROCESSOPERATIONNAME", operationName);
		getPositionBind.put("PROCESSOPERATIONVERSION", "00001");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind); 
		
		if(positionSqlBindSet.size() == 0)
		{
			return "0";
		}
		
		return positionSqlBindSet.get(0).get("POSITION").toString();
	}
	
}
