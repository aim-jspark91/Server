package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveSamplingBatch extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveSample", getEventUser(), getEventComment(), null, null);
		
		//report
		StringBuilder strComment = new StringBuilder();
		
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "SAMPLELIST", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);

		for (Element eleLot : eleLotList)
		{
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String sMainFlowName = SMessageUtil.getChildText(eleLot, "PROCESSFLOWNAME", true);
			String sMainFlowVersion = SMessageUtil.getChildText(eleLot, "PROCESSFLOWVERSION", true);
			String sProcessOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", false);
			String sProcessOperationVersion = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONVERSION", false);
			String sPosition= SMessageUtil.getChildText(eleLot, "POSITION", false);
			String sSampleOutHoldFlag= SMessageUtil.getChildText(eleLot, "SAMPLEOUTHOLDFLAG", false);
			String sSampleFlowName= SMessageUtil.getChildText(eleLot, "SAMPLEPROCESSFLOWNAME", false);
			String sSampleFlowVersion= SMessageUtil.getChildText(eleLot, "SAMPLEPROCESSFLOWVERSION", false);
			
			//2019.03.05_hsryu_Mantis 0002944.
			String departmentName = SMessageUtil.getChildText(eleLot, "DEPARTMENTNAME", false);
			
			//modify by JHIYING on2019.07.12 0004289 start
			
			String ReasonCode= SMessageUtil.getChildText(eleLot, "REASONCODE", false);
			String sampleDepartmentName= SMessageUtil.getChildText(eleLot, "SAMPLEDEPARTMENTNAME", false);
			//modify by JHIYING on2019.07.12 0004289 end

			//2019.03.05_hsryu_Note.Same ReserveSampling.
			eventInfo.setEventComment(note);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			if(StringUtil.isEmpty(sProcessOperationName))
			{
				throw new CustomException("SAMPLE-0004",lotData.getKey().getLotName());
			}
			
			if(StringUtil.isEmpty(sPosition))
			{
				throw new CustomException("SAMPLE-0005",lotData.getKey().getLotName(), sMainFlowName, sProcessOperationName, sSampleFlowName);
			}
			
			//satrt by jhying on20200420 mantis:5973
			 String sMoveOperationNodeID = NodeStack.getNodeID(lotData.getFactoryName(), sMainFlowName, sProcessOperationName);
			 boolean endFlag = MESLotServiceProxy.getLotServiceUtil().checkEndOperation(sMainFlowName, sMainFlowVersion, sMoveOperationNodeID);
			 ProcessFlow processFlow=null;
				
			 ProcessFlowKey keyInfo = new ProcessFlowKey(lotData.getFactoryName(),sMainFlowName,sMainFlowVersion);
			 processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(keyInfo);
			  if(endFlag)
			  {
				  if(StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MAIN) || 
						  StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_MQC) ||
						  StringUtils.equals(processFlow.getProcessFlowType(), GenericServiceProxy.getConstantMap().PROCESSFLOW_TYPE_BRANCH))
				  throw new CustomException("SAMPLE-2000",sProcessOperationName,lotName);
			  }
			//end by jhying on20200420 mantis:5973
			  
			long lastPosition= 0;
			
			LotAction lotAction = new LotAction();
			
			//Get futureAction LastPosition by LotData
			lastPosition = Integer.parseInt(MESLotServiceProxy.getLotServiceUtil().getLastPositionOfLotAction(lotData,sMainFlowName,sProcessOperationName));
			
			String lotActionSql = "SELECT FACTORYNAME, LOTNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION , PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, "
					+ " POSITION, SAMPLEPROCESSFLOWNAME, SAMPLEPROCESSFLOWVERSION , ACTIONNAME"
					+ " FROM CT_LOTACTION "
					+ " WHERE LOTNAME = :LOTNAME "
					+ " AND FACTORYNAME = :FACTORYNAME "
					+ " AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ " AND PROCESSFLOWVERSION = :PROCESSFLOWVERION "
					+ " AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
					+ " AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
					+ " ORDER BY POSITION DESC";
			
			Map<String, Object> lotActionBindSet = new HashMap<String, Object>();
			lotActionBindSet.put("LOTNAME", lotData.getKey().getLotName());
			lotActionBindSet.put("FACTORYNAME", lotData.getFactoryName());
			lotActionBindSet.put("PROCESSFLOWNAME", sMainFlowName);
			lotActionBindSet.put("PROCESSFLOWVERION", sMainFlowVersion);
			lotActionBindSet.put("PROCESSOPERATIONNAME", sProcessOperationName);
			lotActionBindSet.put("PROCESSOPERATIONVERSION", sProcessOperationVersion);
			
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
						
						if(sampleFlow.equals(sSampleFlowName)&&sampleFlowVer.equals(sSampleFlowVersion))
						{
							throw new CustomException("SAMPLE-0002",lotData.getKey().getLotName(), sMainFlowName, sProcessOperationName, sSampleFlowName);
						}
					}
				}

				//insert Sampling
				lotAction = ExtendedObjectProxy.getLotActionService().setSampleActioninfo(eventInfo, lotData, sMainFlowName, sProcessOperationName, lastPosition + 1, sSampleFlowName, sPosition, sSampleOutHoldFlag, departmentName,ReasonCode,sampleDepartmentName);
				ExtendedObjectProxy.getLotActionService().create(eventInfo, lotAction);			
			}
			else
			{
				if(sampleSqlResult.size() == 0)
				{
					lotAction = ExtendedObjectProxy.getLotActionService().setSampleActioninfo(eventInfo, lotData, sMainFlowName, sProcessOperationName, lastPosition + 1, sSampleFlowName, sPosition, sSampleOutHoldFlag, departmentName,ReasonCode,sampleDepartmentName);
					ExtendedObjectProxy.getLotActionService().create(eventInfo, lotAction);			
				}
			}
		}
		
		return doc;
	}
}
