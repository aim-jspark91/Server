package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OperAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class FutureActionByOperation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		//String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACITIONNAME", true);
		String departmentName = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);

		String holdCode = "";
		//String holdType = "";
		String changeProductSpecName = "";
		String changeProductRequestName = "";
		String changeProductSpecVersion = "";
		String changeEcCode = "";
		String changeProcessFlowName = "";
		String changeProcessFlowVersion = "";
		String changeProcessOperationName = "";
		String changeProcessOperationVersion = "";
		
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		for(Element eleLot : eleLotList)
		{
			String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
			
			// 2019.05.17_hsryu_Add pass 'department'. Mantis 0003920.
			CommonValidation.checkSameOperAction(productSpecName, ecCode, processFlowName, processFlowVersion, processOperationName, departmentName);
			
			if(StringUtil.equals(actionName, "Hold"))
			{
				holdCode = SMessageUtil.getBodyItemValue(doc, "HOLDCODE", true);
				//holdType = SMessageUtil.getBodyItemValue(doc, "HOLDTYPE", true);
			}
			else
			{
				changeProductSpecName = SMessageUtil.getBodyItemValue(doc, "CHANGEPRODUCTSPECNAME", true);
				changeProductRequestName = SMessageUtil.getBodyItemValue(doc, "CHANGEPRODUCTREQUESTNAME", true);
				changeProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "CHANGEPRODUCTSPECVERSION", true);
				changeEcCode = SMessageUtil.getBodyItemValue(doc, "CHANGEECCODE", true);
				changeProcessFlowName = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSFLOWNAME", true);
				changeProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSFLOWVERSION", true);
				changeProcessOperationName = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSOPERATIONNAME", true);
				changeProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSOPERATIONVERSION", true);
				
				this.validation_Flow(processOperationName, processFlowName, changeProcessOperationName, changeProcessFlowName, factoryName);
			}
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("FutureActionByOperation", this.getEventUser(), this.getEventComment(), "", "");
			
			long lastPosition = Integer.parseInt(this.getLastPosition(factoryName, productSpecName, processFlowName, processOperationName,ecCode));
				
			//Create CT_OPERACTION
			OperAction operAction = new OperAction(factoryName, productSpecName,productSpecVersion, ecCode, processFlowName,processFlowVersion, processOperationName,processOperationVersion, lastPosition+1);
			
			operAction.setActionName(actionName);
			
			if(StringUtil.equals(actionName, "Hold"))
			{
				operAction.setHoldCode(holdCode);
				operAction.setHoldType(GenericServiceProxy.getConstantMap().HOLDTYPE_BHOLD);
			}
			else
			{
				operAction.setChangeProductSpecName(changeProductSpecName);
				operAction.setChangeProductSpecVersion(changeProductSpecVersion);
				operAction.setChangeECCode(changeEcCode);
				operAction.setChangeProcessFlowName(changeProcessFlowName);
				operAction.setChangeProcessFlowVersion(changeProcessFlowVersion);
				operAction.setChangeProcessOperationName(changeProcessOperationName);
				operAction.setChangeProcessOperationVersion(changeProcessOperationVersion);
				operAction.setChangeProductRequestName(changeProductRequestName);
				
			}

			operAction.setLastEventName(eventInfo.getEventName());
			operAction.setLastEventTime(eventInfo.getEventTime());
			operAction.setLastEventTimeKey(eventInfo.getEventTimeKey());
			operAction.setLastEventUser(eventInfo.getEventUser());
			operAction.setLastEventComment(note);
			operAction.setDepartmentName(departmentName);

			ExtendedObjectProxy.getOperActionService().create(eventInfo, operAction);
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
	
	
	public String getLastPosition(String factoryName, String specName,String flowName, String operationName,String ecCode)
	{
		String getPositionSql = "SELECT POSITION"
				+ " FROM CT_OPERACTION OA "
				+ " WHERE 1 = 1 "
				+ " AND OA.FACTORYNAME = :FACTORYNAME "
				+ " AND OA.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND OA.PRODUCTSPECVERSION = :PRODUCTSPECVERSION "
				+ " AND OA.ECCODE = :ECCODE "
				+ " AND OA.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND OA.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ " AND OA.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND OA.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " ORDER BY POSITION DESC";
		
		Map<String, Object> getPositionBind = new HashMap<String, Object>();
		getPositionBind.put("FACTORYNAME", factoryName);
		getPositionBind.put("PRODUCTSPECNAME", specName);
		getPositionBind.put("PRODUCTSPECVERSION", "00001");
		getPositionBind.put("PROCESSFLOWNAME", flowName);
		getPositionBind.put("PROCESSFLOWVERSION", "00001");
		getPositionBind.put("PROCESSOPERATIONNAME", operationName);
		getPositionBind.put("PROCESSOPERATIONVERSION", "00001");
		getPositionBind.put("ECCODE", ecCode);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> positionSqlBindSet = GenericServiceProxy.getSqlMesTemplate().queryForList(getPositionSql, getPositionBind); 
		
		if(positionSqlBindSet.size() == 0)
		{
			return "0";
		}
		
		return positionSqlBindSet.get(0).get("POSITION").toString();
	}

}
