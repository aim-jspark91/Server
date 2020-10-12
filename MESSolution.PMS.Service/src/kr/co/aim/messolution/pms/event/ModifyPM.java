package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class ModifyPM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintenanceID  = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		String pmCode         = SMessageUtil.getBodyItemValue(doc, "PMCODE", true);
		String groupName      = SMessageUtil.getBodyItemValue(doc, "GROUPNAME", false);
		String className 	  = SMessageUtil.getBodyItemValue(doc, "CLASSNAME", false);
		String maintStatus 	  = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);
		String machineName 	  = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		//String machineType    = SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", true);
		//String unitName 	  = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String maintName      = SMessageUtil.getBodyItemValue(doc, "MAINTNAME", false);
		String maintType      = SMessageUtil.getBodyItemValue(doc, "MAINTTYPE", false);
		
		String maintPlanDate = SMessageUtil.getBodyItemValue(doc, "MAINTPLANDATE", true);
		String maintEarlyDate = SMessageUtil.getBodyItemValue(doc, "MAINTEARLYDATE", true);
		String maintLimitDate = SMessageUtil.getBodyItemValue(doc, "MAINTLIMITDATE", true);
		//2016-12-30 wzm add
		String maintStartDate = SMessageUtil.getBodyItemValue(doc, "MAINTSTARTDATE", true);
		
		//String maintDesc = SMessageUtil.getBodyItemValue(doc, "MAINTDESC", false);
		String maintDoDesc = SMessageUtil.getBodyItemValue(doc, "MAINTDODESC", false);
		//String maintControlDesc = SMessageUtil.getBodyItemValue(doc, "MAINTCONTROLDESC", false);
		//String maintPurposelDesc = SMessageUtil.getBodyItemValue(doc, "MAINTPURPOSEDESC", false);
			
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ExecutePM", getEventUser(), getEventComment(), null, null);
		
		//Get
	    Maintenance maintenanceData = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[] {maintenanceID});
		//String PMCode 		      = maintenanceData.getPmCode();
		//String GroupName          = maintenanceData.getGroupName();
		//String ClassName          = maintenanceData.getClassName();
		//String MaintName          = maintenanceData.getMaintName();
		//String MachineName        = maintenanceData.getMachineName();
		//String UnitName 	      = maintenanceData.getUnitName();
		//String MachineType        = maintenanceData.getMachineType();
		//String MaintType          = maintenanceData.getMaintType();
		//String MaintDesc          = maintenanceData.getMaintDesc();
		//String MaintControlDesc   = maintenanceData.getMaintControlDesc();
		//String MaintPurposelDesc  = maintenanceData.getMaintPurposeDesc();
		//String MaintPlanDate      = maintenanceData.getMaintPlanDate().toString();
		Timestamp MaintStartDate      = maintenanceData.getMaintStartDate();
		Timestamp MaintEndDate        = maintenanceData.getMaintEndDate();
		//String MaintEarlyDate     = maintenanceData.getMaintEarlyDate().toString();
		//String MaintLimitDate     = maintenanceData.getMaintLimitDate().toString();		
		String MaintElapsedDate   = maintenanceData.getMaintElapsedDate();
		//String MaintDoDesc        = maintenanceData.getMaintDoDesc();
		String ExecuteUser        = maintenanceData.getExecuteUser();
		String MaintHelper        = maintenanceData.getMaintHelper();
		String CheckUser          = maintenanceData.getCheckUser();
		String CheckResult        = maintenanceData.getCheckResult();
		String CheckAction        = maintenanceData.getCheckAction();
		String EvaluationUser     = maintenanceData.getEvaluationUser();
		String EvaluationTime     = maintenanceData.getEvaluationTime();
		String EvaluationDesc     = maintenanceData.getEvaluationDesc();
		String CancelFlag         = maintenanceData.getCancelFlag();
		String CancelUser         = maintenanceData.getCancelUser();
		String CancelTime         = maintenanceData.getCancelTime();
		String Remark            = maintenanceData.getRemark();
				
		//Set
		maintenanceData.setMaintenanceID(maintenanceID);
		maintenanceData.setPmCode(pmCode);
		maintenanceData.setMaintStatus(maintStatus);				
		maintenanceData.setMaintStartDate(MaintStartDate);	
		maintenanceData.setMaintEndDate(MaintEndDate);
		maintenanceData.setExecuteUser(ExecuteUser);
		maintenanceData.setMaintHelper(MaintHelper);
		maintenanceData.setCheckUser(CheckUser);
		maintenanceData.setCheckResult(CheckResult);
		maintenanceData.setCheckAction(CheckAction);
		maintenanceData.setEvaluationUser(EvaluationUser);
		maintenanceData.setEvaluationTime(EvaluationTime);
		maintenanceData.setEvaluationDesc(EvaluationDesc);
		maintenanceData.setCancelFlag(CancelFlag);
		maintenanceData.setCancelUser(CancelUser);
		maintenanceData.setCancelTime(CancelTime);
		maintenanceData.setRemark(Remark);
		maintenanceData.setMaintElapsedDate(MaintElapsedDate);			
		
		if(StringUtil.isNotEmpty(groupName))
			maintenanceData.setGroupName(groupName);
		
		if(StringUtil.isNotEmpty(className))
			maintenanceData.setClassName(className);
		
		if(StringUtil.isNotEmpty(machineName))
			maintenanceData.setMachineName(machineName);
				
		//if(StringUtil.isNotEmpty(machineType))
		//    maintenanceData.setMachineType(machineType);
		
		//if(StringUtil.isNotEmpty(unitName))
		//    maintenanceData.setUnitName(unitName);	
		
		if(StringUtil.isNotEmpty(maintName))
		    maintenanceData.setMaintName(maintName);
		
		if(StringUtil.isNotEmpty(maintType))
		    maintenanceData.setMaintType(maintType);
		
		//if(StringUtil.isNotEmpty(maintDesc))
		//    maintenanceData.setMaintDesc(maintDesc);
		
		//if(StringUtil.isNotEmpty(maintControlDesc))
		//    maintenanceData.setMaintControlDesc(maintControlDesc);
		
		//if(StringUtil.isNotEmpty(maintPurposelDesc))
		//    maintenanceData.setMaintPurposeDesc(maintPurposelDesc);
		
		if(StringUtil.isNotEmpty(maintPlanDate))
		    maintenanceData.setMaintPlanDate(TimeStampUtil.getTimestamp(maintPlanDate));
					
		if(StringUtil.isNotEmpty(maintEarlyDate))
		    maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(maintEarlyDate));
		
		if(StringUtil.isNotEmpty(maintLimitDate))
		    maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(maintLimitDate));
		
		//wzm add 2016-12-30
		if(StringUtil.isNotEmpty(maintStartDate))
		    maintenanceData.setMaintStartDate(TimeStampUtil.getTimestamp(maintStartDate));
										
		if(StringUtil.isNotEmpty(maintDoDesc))
		    maintenanceData.setMaintDoDesc(maintDoDesc);
					
		try
		{	
			maintenanceData = PMSServiceProxy.getMaintenanceService().modify(eventInfo, maintenanceData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0088", maintenanceData,pmCode);
		}	
		return doc;
	}
}
