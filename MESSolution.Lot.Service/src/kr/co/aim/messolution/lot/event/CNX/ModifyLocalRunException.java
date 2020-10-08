package kr.co.aim.messolution.lot.event.CNX;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.LocalRunException;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyLocalRunException extends SyncHandler {
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

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		
		LocalRunException localRunExceptionData = null;
		
		try
		{			
			localRunExceptionData = ExtendedObjectProxy.getLocalRunExceptionService().selectByKey(false, new Object[]{lotName,processFlowName,processOperationName,machineName});
		}
		catch(Exception ex)
		{
			throw new CustomException("INHIBIT-0001", ""); 
		}
		
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

		
		ExtendedObjectProxy.getLocalRunExceptionService().modify(eventInfo, localRunExceptionData);
		
		return doc;
	}
	
	
}
