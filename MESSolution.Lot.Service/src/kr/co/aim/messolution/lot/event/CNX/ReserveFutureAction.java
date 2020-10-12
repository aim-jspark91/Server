package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveFutureAction extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> futureActionElementList = SMessageUtil.getBodySequenceItemList(doc, "FUTUREACTIONLIST", true);
		
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveFutureAction", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : futureActionElementList)
		{
			String slotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			String sFactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sProcessFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String sProcessOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String sHoldCode = SMessageUtil.getChildText(eledur, "HOLDCODE", false);
			String sHoldType = SMessageUtil.getChildText(eledur, "HOLDTYPE", false);
			String sHoldPermanentFlag = SMessageUtil.getChildText(eledur, "HOLDPERMANENTFLAG", false);
			String sChangeProductRequestName = SMessageUtil.getChildText(eledur, "TOWORKORDERNAME", false);
			String sChangeProductSpecName = SMessageUtil.getChildText(eledur, "TOPRODUCTSPECNAME", false);
			String sChangeECCode = SMessageUtil.getChildText(eledur, "TOECCODE", false);
			String sChangeProcessFlowName = SMessageUtil.getChildText(eledur, "TOPROCESSFLOWNAME", false);
			String sChangeProcessOperation = SMessageUtil.getChildText(eledur, "TOPROCESSOPERATIONNAME", false);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(slotName);
			
			// if Lot In Sort ProcessFlow Throw Error Add By Park Jeong su
			ProcessFlowKey processFlowKey = new ProcessFlowKey(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			if (StringUtils.equals("Sort", processFlow.getProcessFlowType()))
			{
				throw new CustomException("COMMON-0001", "it is not possible to do lot hold or reserve lot hold in sorter flow.");
			}
			
			//2018.12.13_hsryu_Delete Logic. request by CIM.
//			CommonValidation.checkExistHoldAction(slotName,sFactoryName,sProcessFlowName,"00001",sProcessOperationName,"00001",sHoldCode,sHoldType,
//					sHoldPermanentFlag,department);
			
			//Get Last Position	
			long lastPosition = Integer.parseInt(this.getLastPosition(slotName, sFactoryName, sProcessFlowName, sProcessOperationName));
				
			//Create CT_LOTACTION
			LotAction lotActionData = new LotAction(slotName, sFactoryName, sProcessFlowName,"00001", sProcessOperationName, "00001", lastPosition + 1);
			
			String actionName = "";
			if(!sHoldCode.isEmpty()) //Action : Hold
			{
				eventInfo.setEventName("FutureBHold");
				actionName = GenericServiceProxy.getConstantMap().ACTIONNAME_HOLD;
			}
			else if(sHoldCode.isEmpty()) //Action : Change
			{
				eventInfo.setEventName("FutureChangeSpec");
				actionName = GenericServiceProxy.getConstantMap().ACTIONNAME_CHANGE;
				this.validation_Flow(sProcessOperationName, sProcessFlowName, sChangeProcessOperation, sChangeProcessFlowName, sFactoryName);
			}
			
			lotActionData.setActionName(actionName);
			lotActionData.setActionState(GenericServiceProxy.getConstantMap().ACTIONSTATE_CREATED);
			lotActionData.setFactoryName(sFactoryName);
			lotActionData.setHoldCode(sHoldCode);
			lotActionData.setHoldPermanentFlag(sHoldPermanentFlag);
			lotActionData.setHoldType(sHoldType);
			lotActionData.setChangeProductRequestName(sChangeProductRequestName);
			lotActionData.setChangeProductSpecName(sChangeProductSpecName);
			lotActionData.setChangeECCode(sChangeECCode);
			lotActionData.setChangeProcessFlowName(sChangeProcessFlowName);
			lotActionData.setChangeProcessOperationName(sChangeProcessOperation);
			lotActionData.setLastEventName(eventInfo.getEventName());
			lotActionData.setLastEventTime(eventInfo.getEventTime());
			lotActionData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotActionData.setLastEventUser(eventInfo.getEventUser());
			//lotActionData.setLastEventComment(eventInfo.getEventComment());
			lotActionData.setLastEventComment(note);
			lotActionData.setDepartment(department);
			lotActionData.setHoldCount(0);

			ExtendedObjectProxy.getLotActionService().create(eventInfo, lotActionData);
			
		}
		

		return doc;
	}
	
	private void validation_Flow(String oldoperationData, String oldprocessflowData, String newProcessOperationName, String newProcessFlowName, String factoryName)
			throws CustomException
		{
			int count = 0; 
			
			if(oldprocessflowData.equals(newProcessFlowName))
			{
				return;
			}
		
			String sql = "SELECT LEVEL LV,FACTORYNAME,PROCESSOPERATIONNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,NODEID,NODETYPE";
			sql += " FROM (SELECT N.FACTORYNAME,N.NODEATTRIBUTE1 PROCESSOPERATIONNAME,N.PROCESSFLOWNAME,N.PROCESSFLOWVERSION,N.NODEID,N.NODETYPE,A.FROMNODEID,A.TONODEID";
			sql += " FROM ARC A,";
			sql += " NODE N,";
			sql += " PROCESSFLOW PF";
			sql += " WHERE 1 = 1";
			sql += " AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = :FACTORYNAME";
			sql += " AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME";
			sql += " AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION";
			sql += " AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME";
			sql += " AND N.FACTORYNAME = PF.FACTORYNAME";
			sql += " AND A.FROMNODEID = N.NODEID)";
			sql += " START WITH NODETYPE = :NODETYPE";
			sql += " CONNECT BY NOCYCLE FROMNODEID = PRIOR TONODEID";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("PROCESSFLOWNAME", oldprocessflowData);
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSOPERATIONNAME", oldoperationData);
			bindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> OldsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if (OldsqlResult.size() > 0) 
			{
				Map<String, String> newbindMap = new HashMap<String, String>();
				newbindMap.put("PROCESSFLOWNAME", newProcessFlowName);
				newbindMap.put("FACTORYNAME", factoryName);
				newbindMap.put("PROCESSOPERATIONNAME", newProcessOperationName);
				newbindMap.put("NODETYPE", GenericServiceProxy.getConstantMap().Node_Start);
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> newsqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, newbindMap);
				
				if (newsqlResult.size() > 0) 
				{
					// Check new Operation
					boolean checkOperation = false;
					
					for( int k=1; k< newsqlResult.size(); k++)
					{
						count ++;
						
						if(newProcessOperationName.equals(newsqlResult.get(k).get("PROCESSOPERATIONNAME").toString()))
						{
							checkOperation = !checkOperation;
							break;
						}
					}
					
					if(!checkOperation)
					{
						throw new CustomException("PROCESSOPERATION-0003", newProcessOperationName);
					}
					
					// Compare between old flow and new flow before new operation
					String oldname = "";
					String Newname = "";
									
					for( int i=1; i<count+1; i++)
					{
						boolean check = false;
						
						oldname = OldsqlResult.get(i).get("processoperationname").toString();
											
						for( int j=1; j<count+1; j++)
						{
							Newname = newsqlResult.get(j).get("processoperationname").toString();
							
							if(oldname.equals(Newname))
							{
								check = !check;
							}
						}
						
						if(!check)
						{
							throw new CustomException("PROCESSOPERATION-0002");
						}
					}			
				}
				else {
					throw new CustomException("PROCESSOPERATION-0002");
				}			
			}
			else {
				throw new CustomException("PROCESSOPERATION-0002");
			}		
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
