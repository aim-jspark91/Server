package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DeleteMQCJob extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element mqcJobList = SMessageUtil.getBodySequenceItem(doc, "MQCJOBLIST", true);
		
		if (mqcJobList != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorLotList = mqcJobList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element mqcJobE = (Element) iteratorLotList.next();
				
				String factoryName = SMessageUtil.getChildText(mqcJobE, "FACTORYNAME", true);
				String mqcJobName = SMessageUtil.getChildText(mqcJobE, "MQCJOBNAME", true);
				String MQCLotName = StringUtil.EMPTY;
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveMqcJob", this.getEventUser(), this.getEventComment(), "", "");
				
				MQCJob mqcJob = null;
				MQCJobOper mqcJobOper = null;
				MQCJobPosition mqcJobPosition = null;

				try
				{
					mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
					MQCLotName = mqcJob.getlotName();
				}
				catch (Exception ex)
				{
					mqcJob = null;
				}
				
				if(mqcJob == null)
				{
					throw new CustomException("MQC-0031", mqcJobName);
				}
				
				if(StringUtils.equals(mqcJob.getmqcState(), "Executing"))
				{
					throw new CustomException("MQC-0041", mqcJobName);
				}
				
				//Delete MQCJob
				ExtendedObjectProxy.getMQCJobService().remove(eventInfo, mqcJob);
				
				//Delete MQCJobOper
				String strSql = "SELECT MQCJOBNAME, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION " +
						"  FROM CT_MQCJOBOPER " +
						" WHERE MQCJOBNAME = :MQCJOBNAME ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MQCJOBNAME", mqcJobName);

				List<Map<String, Object>> mqcJobOperList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
				
				for( int i = 0; i < mqcJobOperList.size(); i++)
				{
					mqcJobOper = ExtendedObjectProxy.getMQCJobOperService().selectByKey(false, new Object[] {mqcJobName, 
																												(String)mqcJobOperList.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)mqcJobOperList.get(i).get("PROCESSOPERATIONVERSION")});
					
					if(mqcJobOper != null)
					{
						ExtendedObjectProxy.getMQCJobOperService().remove(eventInfo, mqcJobOper);
					}
				}
				
				//Delete MQCJobPosition
				String strPositionSql = "SELECT MQCJOBNAME, " +
						"       PROCESSOPERATIONNAME, " +
						"       PROCESSOPERATIONVERSION, " +
						"       POSITION " +
						"  FROM CT_MQCJOBPOSITION " +
						" WHERE MQCJOBNAME = :MQCJOBNAME ";

				Map<String, Object> bindMapPosition = new HashMap<String, Object>();
				bindMapPosition.put("MQCJOBNAME", mqcJobName);

				List<Map<String, Object>> mqcJobPositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strPositionSql, bindMapPosition);
				
				for( int i = 0; i < mqcJobPositionList.size(); i++)
				{
					mqcJobPosition = ExtendedObjectProxy.getMQCJobPositionService().selectByKey(false, new Object[] {mqcJobName, 
																												(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONNAME"), 
																												(String)mqcJobPositionList.get(i).get("PROCESSOPERATIONVERSION"),
																												mqcJobPositionList.get(i).get("POSITION").toString()});
					
					if(mqcJobPosition != null)
					{
						ExtendedObjectProxy.getMQCJobPositionService().remove(eventInfo, mqcJobPosition);
					}
				}
				
				// Mentis 2607 add below logic
				// Mentis 2922 remove below logic
//				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(MQCLotName);
//				Map<String, String> udfs = lotData.getUdfs();
//				udfs.put("NOTE", "MQC JOB Name : " + mqcJobName);
//				LotServiceProxy.getLotService().update(lotData);
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
//				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				
//				Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(MQCLotName);
//				Map<String, String> udfs_note = lotData_Note.getUdfs();
//				udfs_note.put("NOTE", "");
//				LotServiceProxy.getLotService().update(lotData_Note);				
			}
		} 
		return doc;
	}
}
