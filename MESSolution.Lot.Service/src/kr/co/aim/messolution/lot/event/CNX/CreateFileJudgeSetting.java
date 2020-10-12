package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FileJudgeSetting;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateFileJudgeSetting extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		
		List<Element> operationlist = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateFileJudgeSetting", this.getEventUser(), this.getEventComment(), "", "");
		
		String getRunLotListQuery =" WHERE PROCESSFLOWNAME = ? AND PROCESSOPERATIONNAME = ? AND LOTPROCESSSTATE = ? ";
		String getGholdLotListQuery =" WHERE PROCESSFLOWNAME = ? AND PROCESSOPERATIONNAME = ? AND LOTPROCESSSTATE = ? AND LOTNAME IN (SELECT LOTNAME FROM CT_LOTMULTIHOLD A WHERE A.REASONCODE = ?) ";

		for (Element eledur : operationlist)
		{
			Boolean createFlage=false;
			
			String processFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(eledur, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", true);
			
			String gradeDataFlag = SMessageUtil.getChildText(eledur, "GRADEDATAFLAG", false);
			String reJudgeFlag = SMessageUtil.getChildText(eledur, "REJUDGEFLAG", false);
			String autoReviewFlag = SMessageUtil.getChildText(eledur, "AUTOREVIEWFLAG", false);

			String forceRepairFlag = SMessageUtil.getChildText(eledur, "FORCEREPAIRFLAG", false);
			String repairGradeFlag = SMessageUtil.getChildText(eledur, "REPAIRGRADEFLAG", false);
			
			List<String> runLotListOfLotName = new ArrayList<String>();
			List<String> gHoldLotListOfLotName = new ArrayList<String>();
			// Start 2019.09.09 Add By Park Jeong Su Mantis 4780
			if(StringUtils.isEmpty(gradeDataFlag) && StringUtils.isEmpty(reJudgeFlag) && StringUtils.isEmpty(autoReviewFlag) && StringUtils.isEmpty(forceRepairFlag) && StringUtils.isEmpty(repairGradeFlag) ){
				continue;
			}
			// End 2019.09.09 Add By Park Jeong Su Mantis 4780
			FileJudgeSetting fileJudgeSetting = null;
			
			try
			{
				fileJudgeSetting = ExtendedObjectProxy.getFileJudgeSettingService().selectByKey(false, new Object[] {factoryName,processFlowName, processFlowVersion, processOperationName  , processOperationVersion });
						
			}
			catch (Exception ex)
			{
				fileJudgeSetting = null;
			}
			
			if(fileJudgeSetting != null)
			{
				// Start 2019.09.09 Add By Park Jeong Su Mantis 4780
				//기존 데이터가 존재
				try {
					if(StringUtils.equals("Y", fileJudgeSetting.getGradeDataFlag()) && StringUtils.equals("N", gradeDataFlag)){

						ProcessOperationSpec nextOperationData = CommonUtil.getNextOperation(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, processFlowName, processOperationName);
						
						List<Lot> runLotList= LotServiceProxy.getLotService().select(getRunLotListQuery, 
								new Object[]{
								processFlowName,
								processOperationName,
								GenericServiceProxy.getConstantMap().Lot_Run});
						
						List<Lot> gHoldLotList= LotServiceProxy.getLotService().select(getGholdLotListQuery, 
								new Object[]{
								processFlowName,
								nextOperationData.getKey().getProcessOperationName(),
								"WAIT",
								"GHLD"});
						
						for(Lot lotData : runLotList){
							runLotListOfLotName.add(lotData.getKey().getLotName());
						}
						for(Lot lotData : gHoldLotList){
							gHoldLotListOfLotName.add(lotData.getKey().getLotName());
						}
						
						if(runLotListOfLotName.size()>0 || gHoldLotListOfLotName.size()>0){
							throw new CustomException("LOT-0231",runLotListOfLotName.toString(),gHoldLotListOfLotName.toString());
						}
					}
				} catch (Exception e) {
					throw new CustomException("LOT-0231",runLotListOfLotName.toString(),gHoldLotListOfLotName.toString());
				}

				// End 2019.09.09 Add By Park Jeong Su Mantis 4780
			}
			else{
				// 새로운 데이터 생성
				fileJudgeSetting = new FileJudgeSetting(factoryName,processFlowName, processFlowVersion, processOperationName  , processOperationVersion);
				createFlage=true;
			}
			
			fileJudgeSetting.setGradeDataFlag(gradeDataFlag);
			fileJudgeSetting.setReJudgeFlag(reJudgeFlag);
			fileJudgeSetting.setAutoReviewFlag(autoReviewFlag);
			fileJudgeSetting.setForceRepairFlag(forceRepairFlag);
			fileJudgeSetting.setRepairGradeFlag(repairGradeFlag);
			
			fileJudgeSetting.setLastEventComment(eventInfo.getEventComment());
			fileJudgeSetting.setLastEventName(eventInfo.getEventName());
			fileJudgeSetting.setLastEventTime(eventInfo.getEventTime());
			fileJudgeSetting.setLastEventTimeKey(eventInfo.getEventTimeKey());
			fileJudgeSetting.setLastEventUser(eventInfo.getEventUser());
			
			
			if(createFlage==true){
				eventInfo.setEventName("CreateFileJudgeSetting");
				ExtendedObjectProxy.getFileJudgeSettingService().create(eventInfo, fileJudgeSetting);
			}else
			{
				eventInfo.setEventName("ModifyFileJudgeSetting");
				ExtendedObjectProxy.getFileJudgeSettingService().modify(eventInfo, fileJudgeSetting);
			}
			
		}

		return doc;
	}
}
