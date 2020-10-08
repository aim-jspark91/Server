package kr.co.aim.messolution.pms.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Maintenance;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CompletePM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintenanceID  = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEID", true);
		String maintStatus 	  = SMessageUtil.getBodyItemValue(doc, "MAINTSTATUS", true);
		String maintEndDate = SMessageUtil.getBodyItemValue(doc, "MAINTENDDATE", true);
		List<Element> userList = SMessageUtil.getBodySequenceItemList(doc, "USERLIST", true);
		String maintHelper = "";
				
		for(Element userInfo : userList)
		{
			//int a = userList.lastIndexOf(userInfo); //get last value
			String userName = SMessageUtil.getChildText(userInfo, "USERNAME", true);
			maintHelper += userName + ",";
		}		
		maintHelper = maintHelper.substring(0,(maintHelper.length() - 1));

	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompletePM", getEventUser(), getEventComment(), null, null);
		
		Maintenance maintenanceData = null;
		try
		{
			//Get
			maintenanceData = PMSServiceProxy.getMaintenanceService().selectByKey(true, new Object[] {maintenanceID});
			String pmCode            = maintenanceData.getPmCode();
			String groupName         = maintenanceData.getGroupName();
			String className         = maintenanceData.getClassName();
			String machineName       = maintenanceData.getMachineName();
			String machineType       = maintenanceData.getMachineType();
			String unitName          = maintenanceData.getUnitName();
			String maintName         = maintenanceData.getMaintName();		
			String maintType         = maintenanceData.getMaintType();
			String maintDesc         = maintenanceData.getMaintDesc();
			String maintControlDesc  = maintenanceData.getMaintControlDesc();
			String maintPurposeDesc  = maintenanceData.getMaintPurposeDesc();
			String maintPlandate     = maintenanceData.getMaintPlanDate().toString();
			//maintStatus
			String maintStartDate    = maintenanceData.getMaintStartDate().toString();
			//maintEndDate			
			String maintEarlydate    = maintenanceData.getMaintEarlyDate().toString();
			String maintLimitDate    = maintenanceData.getMaintLimitDate().toString();
			String maintElapsedDate  = maintenanceData.getMaintElapsedDate();
			String maintDoDesc       = maintenanceData.getMaintDoDesc();
			String executeUser       = maintenanceData.getExecuteUser();
			//maintHelper
			String checkUser         = maintenanceData.getCheckUser();
			String checkResult       = maintenanceData.getCheckResult();
			String checkAction       = maintenanceData.getCheckAction();
			String evaluationUser    = maintenanceData.getEvaluationUser();
			String evaluationTime    = maintenanceData.getEvaluationTime();
			String evaluationDesc    = maintenanceData.getEvaluationDesc();
			String cancelFlag        = maintenanceData.getCancelFlag();
			String cancelUser        = maintenanceData.getCancelUser();
			String cancelTime        = maintenanceData.getCancelTime();
			String remark            = maintenanceData.getRemark();
					
			//Set
			maintenanceData = new Maintenance(maintenanceID);
			maintenanceData.setPmCode(pmCode);
			maintenanceData.setGroupName(groupName);
			maintenanceData.setClassName(className);
			maintenanceData.setMachineName(machineName);
			maintenanceData.setMachineType(machineType);
			maintenanceData.setUnitName(unitName);
			maintenanceData.setMaintName(maintName);		
			maintenanceData.setMaintType(maintType);
			maintenanceData.setMaintDesc(maintDesc);
			maintenanceData.setMaintControlDesc(maintControlDesc);
			maintenanceData.setMaintPurposeDesc(maintPurposeDesc);
			maintenanceData.setMaintPlanDate(TimeStampUtil.getTimestamp(maintPlandate));
			maintenanceData.setMaintStatus(maintStatus);
			maintenanceData.setMaintStartDate(TimeStampUtil.getTimestamp(maintStartDate));
			maintenanceData.setMaintEndDate(TimeStampUtil.getTimestamp(maintEndDate));
			maintenanceData.setMaintEarlyDate(TimeStampUtil.getTimestamp(maintEarlydate));
			maintenanceData.setMaintLimitDate(TimeStampUtil.getTimestamp(maintLimitDate));
			maintenanceData.setMaintElapsedDate(maintElapsedDate);
			maintenanceData.setMaintDoDesc(maintDoDesc);
			maintenanceData.setExecuteUser(executeUser);
			maintenanceData.setMaintHelper(maintHelper);
			maintenanceData.setCheckUser(checkUser);
			maintenanceData.setCheckResult(checkResult);
			maintenanceData.setCheckAction(checkAction);
			maintenanceData.setEvaluationUser(evaluationUser);
			maintenanceData.setEvaluationTime(evaluationTime);
			maintenanceData.setEvaluationDesc(evaluationDesc);
			maintenanceData.setCancelFlag(cancelFlag);
			maintenanceData.setCancelUser(cancelUser);
			maintenanceData.setCancelTime(cancelTime);
			maintenanceData.setRemark(remark);

			maintenanceData = PMSServiceProxy.getMaintenanceService().modify(eventInfo, maintenanceData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0082", maintenanceData);
		}	
		return doc;
	}
}
