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

public class CancelPM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintenanceID      = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		String maintStatus        = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);
		String cancelUser         = SMessageUtil.getBodyItemValue(doc, "CANCELUSER", true);
		String cancelTime         = SMessageUtil.getBodyItemValue(doc, "CANCELTIME", true);
		
		EventInfo eventInfo       = EventInfoUtil.makeEventInfo("CancelPM", getEventUser(), getEventComment(), null, null);
		
		Maintenance maintenanceData  = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[] {maintenanceID});
		
		//Get
		String PMcode 		     = maintenanceData.getPmCode();
		String GroupName         = maintenanceData.getGroupName();
		String ClassName         = maintenanceData.getClassName();
		String MaintName         = maintenanceData.getMaintName();
		String MaintType         = maintenanceData.getMaintType();
		String MachineName       = maintenanceData.getMachineName();		
		String MachineType       = maintenanceData.getMachineType();
		String UnitName 	     = maintenanceData.getUnitName();	
		String MaintDesc         = maintenanceData.getMaintDesc();
		String MaintControlDesc  = maintenanceData.getMaintControlDesc();
		String MaintPurposelDesc = maintenanceData.getMaintPurposeDesc();
		String MaintPlanDate     = maintenanceData.getMaintPlanDate().toString();
		String MaintStartDate    = maintenanceData.getMaintPlanDate().toString();
		String MaintEndDate      = maintenanceData.getMaintPlanDate().toString();
		String MaintEarlyDate    = maintenanceData.getMaintEarlyDate().toString();
		String MaintLimitDate    = maintenanceData.getMaintLimitDate().toString();		
		String MaintElapsedDate  = maintenanceData.getMaintElapsedDate();
		String MaintDoDesc       = maintenanceData.getMaintDoDesc();
		String ExecuteUser       = maintenanceData.getExecuteUser();
		String MaintHelper       = maintenanceData.getMaintHelper();
		String CheckUser         = maintenanceData.getCheckUser();
		String CheckResult       = maintenanceData.getCheckResult();
		String CheckAction       = maintenanceData.getCheckAction();
		String EvaluationUser    = maintenanceData.getEvaluationUser();
		String EvaluationTime    = maintenanceData.getEvaluationTime();
		String EvaluationDesc    = maintenanceData.getEvaluationDesc();
		String Remark            = maintenanceData.getRemark();
		
		//Set
		maintenanceData.setMaintenanceID(maintenanceID);
		maintenanceData.setPmCode(PMcode);
		maintenanceData.setGroupName(GroupName);
		maintenanceData.setClassName(ClassName);		
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
		maintenanceData.setMaintStartDate(TimeStampUtil.getTimestamp(MaintStartDate));
		maintenanceData.setMaintEndDate(TimeStampUtil.getTimestamp(MaintEndDate));
		maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(MaintEarlyDate));
		maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(MaintLimitDate));
		maintenanceData.setMaintElapsedDate(MaintElapsedDate);
		maintenanceData.setMaintDoDesc(MaintDoDesc);
		maintenanceData.setExecuteUser(ExecuteUser);
		maintenanceData.setMaintHelper(MaintHelper);
		maintenanceData.setCheckUser(CheckUser);
		maintenanceData.setCheckResult(CheckResult);
		maintenanceData.setCheckAction(CheckAction);
		maintenanceData.setEvaluationUser(EvaluationUser);
		maintenanceData.setEvaluationTime(EvaluationTime);
		maintenanceData.setEvaluationDesc(EvaluationDesc);
		maintenanceData.setCancelFlag("Y");
		maintenanceData.setCancelUser(cancelUser);
		maintenanceData.setCancelTime(cancelTime);
		maintenanceData.setRemark(Remark);
		
		try
		{
			maintenanceData = PMSServiceProxy.getMaintenanceService().modify(eventInfo, maintenanceData);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0085", maintenanceID,PMcode); 
		}	
		
		return doc;
	}
}


