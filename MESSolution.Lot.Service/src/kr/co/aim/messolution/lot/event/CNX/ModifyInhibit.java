package kr.co.aim.messolution.lot.event.CNX;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyInhibit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{	
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String ecCode = SMessageUtil.getBodyItemValue(doc, "ECCODE", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineGroupName = SMessageUtil.getBodyItemValue(doc, "MACHINEGROUP", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", false);
		String exceptionLotCount = SMessageUtil.getBodyItemValue(doc, "EXCEPTIONLOTCOUNT", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String inhibitID = SMessageUtil.getBodyItemValue(doc, "INHIBITID", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
		Inhibit inhibitData = null;
		try
		{
			inhibitData = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitID});
		}
		catch(Exception ex)
		{
			throw new CustomException("INHIBIT-0001", ""); 
		}
		
		inhibitData.setProductSpecName(productSpecName); 
		inhibitData.setEcCode(ecCode);
		inhibitData.setProcessFlowName(processFlowName);
		inhibitData.setProcessFlowVersion(processFlowVersion);
		inhibitData.setProcessOperationName(processOperationName);
		inhibitData.setMachineGroupName(machineGroupName);
		inhibitData.setMachineName(machineName);
		inhibitData.setUnitName(unitName);
		inhibitData.setSubUnitName(subUnitName);
		inhibitData.setRecipeName(recipeName);
		
		if(exceptionLotCount != "")
		{
			inhibitData.setExceptionLotCount(Long.parseLong(exceptionLotCount));
		}
		else
		{
			inhibitData.setExceptionLotCount(0);
		}
		inhibitData.setProcessLotCount(0);
		inhibitData.setDescription(description);
		inhibitData.setDepartment(department);
		
		inhibitData.setLastEventName(eventInfo.getEventName());
		inhibitData.setLastEventTimekey(eventInfo.getEventTimeKey());
		inhibitData.setLastEventTime(eventInfo.getEventTime());
		inhibitData.setLastEventUser(eventInfo.getEventUser());
		inhibitData.setLastEventComment(eventInfo.getEventComment());
		inhibitData.setCreateTime(eventInfo.getEventTime());
		inhibitData.setCreateUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getInhibitService().modify(eventInfo, inhibitData);
		//SMessageUtil.addItemToBody(doc, "INHIBITID", inhibitID);
		//setNextInfo(doc, inhibitID);
		return doc;
	}
	
	private void setNextInfo(Document doc, String inhibitID)
	{
		try
		{	
			StringBuilder strComment = new StringBuilder();
			strComment.append("InhibitID").append("[").append(inhibitID).append("]").append("\n");
			//setEventComment(strComment.toString());
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			//eventLog.warn("Note after Crate is nothing");
		}
	}
}
