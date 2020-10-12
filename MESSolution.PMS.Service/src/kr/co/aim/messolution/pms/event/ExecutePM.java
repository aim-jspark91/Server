package kr.co.aim.messolution.pms.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ExecutePM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintenanceID  = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		String maintStatus 	  = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);
		String maintStartDate = SMessageUtil.getBodyItemValue(doc, "MAINTSTARTDATE", true);
		String executeUser 	  = SMessageUtil.getBodyItemValue(doc, "EXECUTEUSER", true);
		String maintDoDesc 	  = SMessageUtil.getBodyItemValue(doc, "MAINTDODESC", false);
		String className 	  = SMessageUtil.getBodyItemValue(doc, "CLASSNAME", true);
	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ExecutePM", getEventUser(), getEventComment(), null, null);
		
		Maintenance maintenanceData = null;
		try
		{
			//Get
			maintenanceData = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[] {maintenanceID});
			String PMCode 		      = maintenanceData.getPmCode();
			String GroupName          = maintenanceData.getGroupName();
			//String ClassName          = maintenanceData.getClassName();
			String MaintName          = maintenanceData.getMaintName();
			String MachineName        = maintenanceData.getMachineName();
			String UnitName 	      = maintenanceData.getUnitName();
			String MachineType        = maintenanceData.getMachineType();
			String MaintType          = maintenanceData.getMaintType();
			String MaintDesc          = maintenanceData.getMaintDesc();
			String MaintControlDesc   = maintenanceData.getMaintControlDesc();
			String MaintPurposelDesc  = maintenanceData.getMaintPurposeDesc();
			String MaintPlanDate      = maintenanceData.getMaintPlanDate().toString();
			String MaintEarlyDate     = maintenanceData.getMaintEarlyDate().toString();
			String MaintLimitDate     = maintenanceData.getMaintLimitDate().toString();		
			String MaintElapsedDate   = maintenanceData.getMaintElapsedDate();
			//String MaintDoDesc        = maintenanceData.getMaintDoDesc();
			//String ExecuteUser        = maintenanceData.getExecuteUser();
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
			maintenanceData.setPmCode(PMCode);
			maintenanceData.setGroupName(GroupName);
			maintenanceData.setClassName(className);		
			maintenanceData.setMachineName(MachineName);
			maintenanceData.setMachineType(MachineType);
			maintenanceData.setUnitName(UnitName);	
			maintenanceData.setMaintName(MaintName);
			maintenanceData.setMaintType(MaintType);
			maintenanceData.setMaintDesc(MaintDesc);
			maintenanceData.setMaintControlDesc(MaintControlDesc);
			maintenanceData.setMaintPurposeDesc(MaintPurposelDesc);
			maintenanceData.setMaintPlanDate(TimeStampUtil.getTimestamp(MaintPlanDate));
			maintenanceData.setMaintStatus(maintStatus);
			maintenanceData.setMaintStartDate(TimeStampUtil.getTimestamp(maintStartDate));
			//maintenanceData.setMaintEndDate(TimeStampUtil.getTimestamp(MaintEndDate));
			maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(MaintEarlyDate));
			maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(MaintLimitDate));
			maintenanceData.setMaintElapsedDate(MaintElapsedDate);
			maintenanceData.setMaintDoDesc(maintDoDesc);
			maintenanceData.setExecuteUser(executeUser);
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

			maintenanceData = PMSServiceProxy.getMaintenanceService().modify(eventInfo, maintenanceData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0082", maintenanceData);
		}	
		return doc;
	}
}
