package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveSample", getEventUser(), getEventComment(), null, null);

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sMainFlowName = SMessageUtil.getBodyItemValue(doc, "MAINFLOWNAME", true);
		String sMainFlowVersion = SMessageUtil.getBodyItemValue(doc, "MAINFLOWVERSION", true);
		String sDecideOperationName = SMessageUtil.getBodyItemValue(doc, "DECIDEOPERATIONNAME", true);
		String sDecideOperationVersion = SMessageUtil.getBodyItemValue(doc, "DECIDEOPERATIONVERSION", true);
		String sMoveOperationName = SMessageUtil.getBodyItemValue(doc, "MOVEOPERATIONNAME", true);
		String sMoveOperationVersion = SMessageUtil.getBodyItemValue(doc, "MOVEOPERATIONVERSION", true);
		String sSampleFlowName = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLOWNAME", true);
		String sSampleFlowVersion = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLOWVERSION", true);
		String sSampleOutHoldFlag = SMessageUtil.getBodyItemValue(doc, "SAMPLEOUTHOLDFLAG", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		String departmentName = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", false);
		
		//  MODIFY BY JHIYING START 0004289
		
		String  ReasonCode= SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sampleDepartmentName = SMessageUtil.getBodyItemValue(doc, "SAMPLEDEPARTMENTNAME", true);
		
		// MODIFY BY JHIYING END  0004289
		

		eventInfo.setEventComment(note);
		//String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String position = "";
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		String[] positionArr = null;
		// String[] posPositionArr = null;
		ArrayList<String> sumPosition = new ArrayList<String>();
		long lastPosition= 0;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		// 2019.05.09_hsryu_Add Validation. if FirstOper is 'Repair', Can't ReserveSampling. 
		if(this.checkFirstOperRepair(lotData.getFactoryName(), sSampleFlowName))	
			throw new CustomException("SAMPLE-0010",sSampleFlowName);
		
		//satrt by jhying on20200420 mantis:5973
		 String sMoveOperationNodeID = NodeStack.getNodeID(lotData.getFactoryName(), sMainFlowName, sMoveOperationName);
		 boolean endFlag = MESLotServiceProxy.getLotServiceUtil().checkEndOperation(sMainFlowName, sMainFlowVersion, sMoveOperationNodeID);
		 ProcessFlow processFlow=null;
			
		 ProcessFlowKey keyInfo = new ProcessFlowKey(lotData.getFactoryName(),sMainFlowName,sMainFlowVersion);
		 processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(keyInfo);
		  if(endFlag)
		  {
			  if(StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN) || 
					  StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC) ||
					  StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
			  throw new CustomException("SAMPLE-2000",sMoveOperationName,lotName);
		  }
		//end by jhying on20200420 mantis:5973
		for (Element eleProduct : productList) 
		{
			// Modified by smkang on 2018.11.24 - Filtering empty slot.
//			sumPosition.add(SMessageUtil.getChildText(eleProduct, "POSITION", true));
			if (StringUtils.isNotEmpty(SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", false)))
				sumPosition.add(SMessageUtil.getChildText(eleProduct, "POSITION", true));
		}
		
		position = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(sumPosition);
		
		LotAction lotAction = new LotAction();
		
		//Get futureAction LastPosition by LotData
		lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(lotData,sMainFlowName,sMoveOperationName));
		
		String lotActionSql = "SELECT FACTORYNAME, LOTNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION , PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, "
				+ " POSITION, SAMPLEPROCESSFLOWNAME, SAMPLEPROCESSFLOWVERSION , ACTIONNAME"
				+ " FROM CT_LOTACTION "
				+ " WHERE LOTNAME = :LOTNAME "
				+ " AND FACTORYNAME = :FACTORYNAME "
				+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERION "
				+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				+ " AND ACTIONSTATE = :ACTIONSTATE "
				+ " ORDER BY POSITION DESC";
		
		Map<String, Object> lotActionBindSet = new HashMap<String, Object>();
		lotActionBindSet.put("LOTNAME", lotData.getKey().getLotName());
		lotActionBindSet.put("FACTORYNAME", lotData.getFactoryName());
		lotActionBindSet.put("PROCESSFLOWNAME", sMainFlowName);
		lotActionBindSet.put("PROCESSFLOWVERION", sMainFlowVersion);
		lotActionBindSet.put("PROCESSOPERATIONNAME", sMoveOperationName);
		lotActionBindSet.put("PROCESSOPERATIONVERSION", sMoveOperationVersion);
		lotActionBindSet.put("ACTIONSTATE", "Created");

		List<Map<String, Object>> sampleSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(lotActionSql, lotActionBindSet);

		if (sampleSqlResult.size() != 0) 
		{
			//check already reserve.
			for(int i=0;i<sampleSqlResult.size(); i++)
			{
				String actionName = sampleSqlResult.get(i).get("ACTIONNAME").toString();
				
				if(actionName.equals("Sampling"))
				{
					String sampleFlow = sampleSqlResult.get(i).get("SAMPLEPROCESSFLOWNAME").toString();
					String sampleFlowVer = sampleSqlResult.get(i).get("SAMPLEPROCESSFLOWVERSION").toString();
					
					if(StringUtils.equals(sampleFlow, sSampleFlowName)&&StringUtils.equals(sampleFlowVer, sSampleFlowVersion))
					{
						throw new CustomException("SAMPLE-0002",lotData.getKey().getLotName(), sMainFlowName, sMoveOperationName, sSampleFlowName);
					}
				}
			}

			//insert Sampling
			lotAction = ExtendedObjectProxy.getLotActionService().setSampleActioninfo(eventInfo, lotData, sMainFlowName, sMoveOperationName, lastPosition + 1, sSampleFlowName, position, sSampleOutHoldFlag, departmentName,ReasonCode,sampleDepartmentName);
			ExtendedObjectProxy.getLotActionService().create(eventInfo, lotAction);			
		}
		else
		{
			if(sampleSqlResult.size() == 0)
			{
				lotAction = ExtendedObjectProxy.getLotActionService().setSampleActioninfo(eventInfo, lotData, sMainFlowName, sMoveOperationName, lastPosition + 1, sSampleFlowName, position, sSampleOutHoldFlag, departmentName,ReasonCode,sampleDepartmentName);
				ExtendedObjectProxy.getLotActionService().create(eventInfo, lotAction);			
			}
		}
		
		
		//2019.08.16 dmlee : SUM Actual SamplePosition [Reserve Sampling] - [System Sampling]
		try
		{
			
			//CT_SAMPLELOT Merge
			List<SampleLot> sampleLotList = null;
			try
			{
				sampleLotList = ExtendedObjectProxy.getSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
						, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
						lotAction.getSampleProcessFlowName(), lotAction.getSampleProcessFlowVersion(), lotAction.getProcessOperationName(), lotAction.getProcessOperationVersion() });	
				
			}
			catch(greenFrameDBErrorSignal ne)
			{
				eventLog.info("Already Decide Sampling Data Non Exist (CT_SAMPLELOT)");
			}
			
			if(sampleLotList != null)
			{
				eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_SAMPLELOT)");
				
				eventInfo.setEventName("MergeActualPosition");
				sumActualSamplePosition(eventInfo, lotAction, sampleLotList);
				
				EventInfo eventInfoLotAction = EventInfoUtil.makeEventInfo("MergeActualPosition", getEventUser(), getEventComment(), null, null);
	
				lotAction.setActionState("Merged");
				ExtendedObjectProxy.getLotActionService().modify(eventInfoLotAction, lotAction);	
			}
		}
		catch(Exception ex)
		{
			eventLog.error("SUM Actual Sample Position Fail ! (CT_SAMPLELOT)");
		}
			
		try
		{
			//CT_CORRESSAMPLELOT
			List<CorresSampleLot> corresSampleLotList = null;
			try
			{
				corresSampleLotList = ExtendedObjectProxy.getCorresSampleLotService().select(" lotName = ? and factoryName = ? and productSpecName = ? and ecCode = ? and sampleProcessFlowName = ? and sampleProcessFlowVersion = ? and fromProcessOperationName = ? and fromProcessOperationVersion = ? "
						, new Object[] {lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getUdfs().get("ECCODE"),
						lotAction.getSampleProcessFlowName(), lotAction.getSampleProcessFlowVersion(), lotAction.getProcessOperationName(), lotAction.getProcessOperationVersion() });	
				
			}
			catch(greenFrameDBErrorSignal ne)
			{
				eventLog.info("Already Decide Sampling Data Non Exist (CT_CORRESSAMPLELOT)");
			}
			
			if(corresSampleLotList != null)
			{
				eventLog.info("Already Decide Sampling Data Exist, SUM Actual Sample Position ... (CT_CORRESSAMPLELOT)");
				
				eventInfo.setEventName("MergeActualPosition");
				sumActualSamplePositionCorres(eventInfo, lotAction, corresSampleLotList);
				
				EventInfo eventInfoLotAction = EventInfoUtil.makeEventInfo("MergeActualPosition", getEventUser(), getEventComment(), null, null);

				lotAction.setActionState("Merged");
				ExtendedObjectProxy.getLotActionService().modify(eventInfoLotAction, lotAction);	
			}
		}
		catch(Exception ex)
		{
			eventLog.error("SUM Actual Sample Position Fail ! (CT_CORRESSAMPLELOT)");
		}
		//2019.08.16 dmlee : SUM Actual SamplePosition End-----------------------------------
		
		
		// Modified by smkang on 2019.05.28 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		//Note clear - YJYU
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);

		return doc;
	}








	private void sumActualSamplePosition(EventInfo eventInfo,
			LotAction lotAction, List<SampleLot> sampleLotList)
			throws CustomException {
		for(SampleLot sampleLotData : sampleLotList)
		{		
			String positionSUM = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(sampleLotData.getActualSamplePosition(), lotAction.getSamplePosition());
			
			ArrayList<String> manualPositionArr = new ArrayList<String>();
			ArrayList<String> actualPositionArr = new ArrayList<String>();
			ArrayList<String> resultPositionArr = new ArrayList<String>();
			
			if(lotAction.getSamplePosition().contains(","))
			{
				String[] fromPositionArr = lotAction.getSamplePosition().split(",");

				for(int i=0;i<fromPositionArr.length;i++)
				{
					manualPositionArr.add(fromPositionArr[i]);
				}
			}
			else
			{
				manualPositionArr.add(lotAction.getSamplePosition());
			}
			
			
			if(sampleLotData.getActualSamplePosition().contains(","))
			{
				String[] toPositionArr = sampleLotData.getActualSamplePosition().split(",");

				for(int i=0; i<toPositionArr.length;i++)
				{
					actualPositionArr.add(toPositionArr[i]);
				}
			}
			else
			{
				actualPositionArr.add(sampleLotData.getActualSamplePosition());
			}
			
			for(String maPosition : manualPositionArr)
			{
				resultPositionArr.add(maPosition);

				for(String acPosition : actualPositionArr)
				{
					if(StringUtils.equals(acPosition, maPosition))
					{
						resultPositionArr.remove(maPosition);
						break;
					}
				}
			}
			
			String strManualPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(resultPositionArr);

			sampleLotData.setActualSamplePosition(positionSUM);
			sampleLotData.setManualSamplePosition(strManualPosition);
			
			sampleLotData.setLastEventComment(eventInfo.getEventComment());
			sampleLotData.setLastEventName(eventInfo.getEventName());
			sampleLotData.setLastEventTime(eventInfo.getEventTime());
			sampleLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
			sampleLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotData);
		}
	}
	
	
	private void sumActualSamplePositionCorres(EventInfo eventInfo,
			LotAction lotAction, List<CorresSampleLot> corresSampleLotList)
			throws CustomException {
		for(CorresSampleLot corresSampleLotData : corresSampleLotList)
		{		
			String positionSUM = MESLotServiceProxy.getLotServiceUtil().sumPositionAndSort(corresSampleLotData.getActualSamplePosition(), lotAction.getSamplePosition());
			
			ArrayList<String> manualPositionArr = new ArrayList<String>();
			ArrayList<String> actualPositionArr = new ArrayList<String>();
			ArrayList<String> resultPositionArr = new ArrayList<String>();
			
			if(lotAction.getSamplePosition().contains(","))
			{
				String[] fromPositionArr = lotAction.getSamplePosition().split(",");

				for(int i=0;i<fromPositionArr.length;i++)
				{
					manualPositionArr.add(fromPositionArr[i]);
				}
			}
			else
			{
				manualPositionArr.add(lotAction.getSamplePosition());
			}
			
			
			if(corresSampleLotData.getActualSamplePosition().contains(","))
			{
				String[] toPositionArr = corresSampleLotData.getActualSamplePosition().split(",");

				for(int i=0; i<toPositionArr.length;i++)
				{
					actualPositionArr.add(toPositionArr[i]);
				}
			}
			else
			{
				actualPositionArr.add(corresSampleLotData.getActualSamplePosition());
			}
			
			for(String maPosition : manualPositionArr)
			{
				resultPositionArr.add(maPosition);

				for(String acPosition : actualPositionArr)
				{
					if(StringUtils.equals(acPosition, maPosition))
					{
						resultPositionArr.remove(maPosition);
						break;
					}
				}
			}
			
			String strManualPosition = MESLotServiceProxy.getLotServiceUtil().ConvertArrayPositionToString(resultPositionArr);

			corresSampleLotData.setActualSamplePosition(positionSUM);
			corresSampleLotData.setManualSamplePosition(strManualPosition);
			
			corresSampleLotData.setLastEventComment(eventInfo.getEventComment());
			corresSampleLotData.setLastEventName(eventInfo.getEventName());
			corresSampleLotData.setLastEventTime(eventInfo.getEventTime());
			corresSampleLotData.setLastEventTimekey(eventInfo.getEventTimeKey());
			corresSampleLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, corresSampleLotData);
		}
	}
	
	
	
	
	
	
	
	private boolean checkFirstOperRepair(String factoryName, String toProcessFlowName) throws CustomException
	{
		try {
			ProcessOperationSpec operationData = CommonUtil.getFirstOperation(factoryName, toProcessFlowName);
			if(StringUtils.equals(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().DETAIL_PROCESSOPERATION_TYPE_REP)){
				return true;
			}
			
			return false;
		}
		catch(Throwable e){
			eventLog.warn("CheckFirstOperRepair Error!!! ");
			return false;
		}
	}
}
