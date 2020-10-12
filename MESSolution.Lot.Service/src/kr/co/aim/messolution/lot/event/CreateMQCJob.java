package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateMQCJob extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		
		MQCTemplate mqcTemplateData = null;
		MQCJob mqcJob = null;
		MQCJobOper mqcJobOper = null;
		MQCJobPosition mqcJobPosition = null;
		
		try
		{
			mqcTemplateData = ExtendedObjectProxy.getMQCTemplateService().selectByKey(false, new Object[] {mqcTemplateName});
		}
		catch (Exception ex)
		{
			mqcTemplateData = null;
		}
		
		if(mqcTemplateData == null)
		{
			throw new CustomException("MQC-0009", mqcTemplateName);
		}
		
		
		String strSql = "SELECT DISTINCT MQCTEMPLATENAME,  " +
				"       PROCESSOPERATIONNAME,  " +
				"       PROCESSOPERATIONVERSION " +
				"  FROM CT_MQCTEMPLATEPOSITION  " +
				" WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME  ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MQCTEMPLATENAME", mqcTemplateName);

		List<Map<String, Object>> mqcTemplatePositionInfo = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSql, bindMap);
		
		if(mqcTemplatePositionInfo == null || mqcTemplatePositionInfo.size() == 0)
		{
			throw new CustomException("MQC-0027", "");
		}
		
		
		
		
		String strSqlPosition = "SELECT MQCTEMPLATENAME,   " +
				"       PROCESSOPERATIONNAME,   " +
				"       PROCESSOPERATIONVERSION,   " +
				"       POSITION,  " +
				"       RECIPENAME, " +
				"       MQCCOUNTUP  " +
				"  FROM CT_MQCTEMPLATEPOSITION   " +
				" WHERE MQCTEMPLATENAME = :MQCTEMPLATENAME   ";

		List<Map<String, Object>> mqcTemplatePositionList = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlPosition, bindMap);
		
		if(lotList != null)
		{
			String mqcJobName = "";
			for(Object obj : lotList.getChildren())
			{
				Element element = (Element)obj;
				String lotName = SMessageUtil.getChildText(element, "LOTNAME", false);
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
				
				if(lotData != null)
				{
					if(StringUtils.isEmpty(lotData.getCarrierName()))
					{
						throw new CustomException("MQC-0028", "");
					}
					mqcJobName = lotData.getKey().getLotName() + "." + lotData.getCarrierName();
					
					try
					{
						mqcJob = ExtendedObjectProxy.getMQCJobService().selectByKey(false, new Object[] {mqcJobName});
					}
					catch (Exception ex)
					{
						mqcJob = null;
					}
					
					if(mqcJob != null)
					{
						throw new CustomException("MQC-0029", mqcJobName);
					}
					
					mqcJob = new MQCJob(mqcJobName);
					mqcJob.setlotName(lotData.getKey().getLotName());
					mqcJob.setcarrierName(lotData.getCarrierName());
					mqcJob.setfactoryName(factoryName);
					mqcJob.setprocessFlowName(mqcTemplateData.getprocessFlowName());
					mqcJob.setprocessFlowVersion(mqcTemplateData.getprocessFlowVersion());
					//mqcJob.setproductQuantity(mqcTemplateData.getproductQuantity());
					//mqcJob.setmqcCountLimit(mqcTemplateData.getmqcCountLimit());
					//mqcJob.setmqcCount(0);
					mqcJob.setmqcTemplateName(mqcTemplateData.getmqcTemplateName());
					mqcJob.setmqcState("Wait");
					mqcJob.setLastEventUser(eventInfo.getEventUser());
					mqcJob.setLastEventComment(eventInfo.getEventComment());
					mqcJob.setLastEventTime(eventInfo.getEventTime());
					mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
					mqcJob.setLastEventName(eventInfo.getEventName());
					
					ExtendedObjectProxy.getMQCJobService().create(eventInfo, mqcJob);
					
					for(int i = 0; i < mqcTemplatePositionInfo.size(); i++)
					{
						mqcJobOper = new MQCJobOper(mqcJobName, (String)mqcTemplatePositionInfo.get(i).get("PROCESSOPERATIONNAME"), (String)mqcTemplatePositionInfo.get(i).get("PROCESSOPERATIONVERSION"));
						mqcJobOper.setmachineGroupName("");
						mqcJobOper.setmachineName("");
						mqcJobOper.setLastEventUser(eventInfo.getEventUser());
						mqcJobOper.setLastEventComment(eventInfo.getEventComment());
						mqcJobOper.setLastEventTime(eventInfo.getEventTime());
						mqcJobOper.setLastEventTimeKey(eventInfo.getEventTimeKey());
						mqcJobOper.setLastEventName(eventInfo.getEventName());
						
						ExtendedObjectProxy.getMQCJobOperService().create(eventInfo, mqcJobOper);
					}

					for(int j = 0; j < mqcTemplatePositionList.size(); j++)
					{
						String strSqlProduct = "SELECT PRODUCTNAME " +
								"  FROM PRODUCT " +
								" WHERE     LOTNAME = :LOTNAME " +
								"       AND POSITION = :POSITION " +
								"       AND FACTORYNAME = :FACTORYNAME ";
						
						Map<String, Object> bindMapProduct = new HashMap<String, Object>();
						bindMapProduct.put("LOTNAME", lotData.getKey().getLotName());
						bindMapProduct.put("FACTORYNAME", factoryName);
						bindMapProduct.put("POSITION", mqcTemplatePositionList.get(j).get("POSITION").toString());

						List<Map<String, Object>> productData = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(strSqlProduct, bindMapProduct);
						
						if(productData == null)
						{
							continue;
						}
						
						MQCJobPosition newMQCJobPosition = new MQCJobPosition(mqcJobName, 
								(String)mqcTemplatePositionList.get(j).get("PROCESSOPERATIONNAME"), 
								(String)mqcTemplatePositionList.get(j).get("PROCESSOPERATIONVERSION"), 
								Long.valueOf(mqcTemplatePositionList.get(j).get("POSITION").toString()));
						
						newMQCJobPosition.setproductName(productData.get(0).get("PRODUCTNAME").toString());
						newMQCJobPosition.setrecipeName((String)mqcTemplatePositionList.get(j).get("RECIPENAME"));
						newMQCJobPosition.setmqcCountUp(Long.valueOf(mqcTemplatePositionList.get(j).get("MQCCOUNTUP").toString()));
						newMQCJobPosition.setLastEventUser(eventInfo.getEventUser());
						newMQCJobPosition.setLastEventComment(eventInfo.getEventComment());
						newMQCJobPosition.setLastEventTime(eventInfo.getEventTime());
						newMQCJobPosition.setLastEventTimeKey(eventInfo.getEventTimeKey());
						newMQCJobPosition.setLastEventName(eventInfo.getEventName());
						
						ExtendedObjectProxy.getMQCJobPositionService().create(eventInfo, newMQCJobPosition);
					}
				}
				else
				{
					throw new CustomException("MQC-0030", lotName);
				}
			}
		}
		
		return doc;
	}
}
