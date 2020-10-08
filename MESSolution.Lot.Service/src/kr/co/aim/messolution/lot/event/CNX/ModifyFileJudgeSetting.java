package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;

public class ModifyFileJudgeSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String gradeDataFlag = SMessageUtil.getBodyItemValue(doc, "GRADEDATAFLAG", true);
		String repairGradeFlag = SMessageUtil.getBodyItemValue(doc, "REPAIRGRADEFLAG", true);
		String forceRepairFlag = SMessageUtil.getBodyItemValue(doc, "FORCEREPAIRFLAG", true);
		String reJudgeFlag = SMessageUtil.getBodyItemValue(doc, "REJUDGEFLAG", true);
		String autoReviewFlag = SMessageUtil.getBodyItemValue(doc, "AUTOREVIEWFLAG", true);
		String pepLevel = SMessageUtil.getBodyItemValue(doc, "PEPLEVEL", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyFileJudgeSetting", this.getEventUser(), this.getEventComment(), "", "");
		
			
		FileJudgeSetting fileJudgeSetting = null;
		
		try
		{
			fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {processFlowName, processFlowVersion, processOperationName  , processOperationVersion  , repairGradeFlag });
		}
		catch (Exception ex)
		{
			fileJudgeSetting = null;
		}
		
		if(fileJudgeSetting == null)
		{
			throw new CustomException("IDLE-0006", "");
		}
		
		
		//fileJudgeSetting.setRepairGradeFlag(repairGradeFlag);
		fileJudgeSetting.setGradeDataFlag(gradeDataFlag);
		//fileJudgeSetting.setForceRepairFlag(forceRepairFlag);
		fileJudgeSetting.setReJudgeFlag(reJudgeFlag);
		fileJudgeSetting.setAutoReviewFlag(autoReviewFlag);
		double pep;
		try{
			pep = Double.parseDouble(pepLevel);
		}catch(Exception e){
			throw new CustomException("IDLE-0005", "");
		}

		fileJudgeSetting.setLastEventComment(eventInfo.getEventComment());
		fileJudgeSetting.setLastEventName(eventInfo.getEventName());
		fileJudgeSetting.setLastEventTime(eventInfo.getEventTime());
		fileJudgeSetting.setLastEventTimeKey(eventInfo.getEventTimeKey());
		fileJudgeSetting.setLastEventUser(eventInfo.getEventUser());
		
		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, processOperationName);
		
		ExtendedObjectProxy.getFileJudgeSettingService().modify(eventInfo, fileJudgeSetting);
		
		return doc;
	}
}
