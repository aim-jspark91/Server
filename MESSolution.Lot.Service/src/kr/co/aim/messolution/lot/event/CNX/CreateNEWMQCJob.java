package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCJob;
import kr.co.aim.messolution.extended.object.management.data.MQCJobOper;
import kr.co.aim.messolution.extended.object.management.data.MQCJobPosition;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplate;
import kr.co.aim.messolution.extended.object.management.data.MQCTemplatePosition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateNEWMQCJob extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mqcTemplateName = SMessageUtil.getBodyItemValue(doc, "MQCTEMPLATENAME", true);
		String OriginalmqcTemplateName = SMessageUtil.getBodyItemValue(doc, "ORIGINALMQCTEMPLATENAME", false);
		Element lotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);
		
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String mqcProductSpec = SMessageUtil.getBodyItemValue(doc, "MQCPRODUCTSPEC", true);
		
		String mqcECCode = SMessageUtil.getBodyItemValue(doc, "MQCECCODE", true);
		String productQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQUANTITY", true);
		String mqcCountLimit = SMessageUtil.getBodyItemValue(doc, "MQCCOUNTLIMIT", true);
		
		String checkFlag = SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", false);
		
		List<Element> MachineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMqcJob", this.getEventUser(), "CreateMqcJob", "", "");
		
		MQCTemplate mqcTemplateData = null;
		MQCJob mqcJob = null;
		MQCJobOper mqcJobOper = null;

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
			//throw new CustomException("MQC-0009", mqcTemplateName);
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
					
					// 2019.03.06 No mentis list
					// Add validation. Request by Hongwei and End-user;
					if(!StringUtil.equals(lotData.getLotState(),"Completed"))
					{
						throw new CustomException("MQC-0057");
					}
					
					// mqcJobName = lotData.getKey().getLotName() + "." + lotData.getCarrierName();
					Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
					nameRuleAttrMap.put("LOTNAME", lotData.getKey().getLotName());
					nameRuleAttrMap.put("CARRIERNAME", lotData.getCarrierName());
					
					// List<String> argSeq = new ArrayList<String>();
					// List<String> TempmqcJobName = NameServiceProxy.getNameGeneratorRuleDefService().generateName("MQCJobNaming", argSeq, 1);
					// mqcJobName = TempmqcJobName.get(0);
					List<String> TempmqcJobName = CommonUtil.generateNameByNamingRule("MQCJobNaming", nameRuleAttrMap, 1);
					mqcJobName = TempmqcJobName.get(0);
					
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
					mqcJob.setprocessFlowName(processFlowName);
					mqcJob.setprocessFlowVersion("00001");
					// mqcJob.setproductQuantity(Long.valueOf(productquantity));
					// mqcJob.setmqcCountLimit(Long.valueOf(mqccountlimit));
					// mqcJob.setmqcCount(0);
					
					if(!StringUtil.equals(OriginalmqcTemplateName,""))
					{
						mqcJob.setmqcTemplateName(OriginalmqcTemplateName);
					}
					else {
						mqcJob.setmqcTemplateName(mqcTemplateName);
					}
					
					mqcJob.setmqcState("Wait");
					mqcJob.setLastEventUser(eventInfo.getEventUser());
					mqcJob.setLastEventComment(eventInfo.getEventComment());
					mqcJob.setLastEventTime(eventInfo.getEventTime());
					mqcJob.setLastEventTimeKey(eventInfo.getEventTimeKey());
					mqcJob.setLastEventName(eventInfo.getEventName());
					mqcJob.setmqcuseproductspec(mqcProductSpec); 	// 2018.12.25
					
					ExtendedObjectProxy.getMQCJobService().create(eventInfo, mqcJob);
					
					for(int i = 0; i < mqcTemplatePositionInfo.size(); i++)
					{
						// MachineList
						String Opername = "";
						String GroupName = "";
						String MachineName = "";
						
						for( Element Machine : MachineList)
						{
							Opername = Machine.getChild("OPERNAME").getText();
							GroupName = Machine.getChild("MACHINEGROUP").getText();
							MachineName = Machine.getChild("MACHINE").getText();
													
							if(StringUtil.equals((String)mqcTemplatePositionInfo.get(i).get("PROCESSOPERATIONNAME"),Opername))
							{
								mqcJobOper = new MQCJobOper(mqcJobName, (String)mqcTemplatePositionInfo.get(i).get("PROCESSOPERATIONNAME"), (String)mqcTemplatePositionInfo.get(i).get("PROCESSOPERATIONVERSION"));
								mqcJobOper.setmachineGroupName(GroupName);
								mqcJobOper.setmachineName(MachineName);
								mqcJobOper.setLastEventUser(eventInfo.getEventUser());
								mqcJobOper.setLastEventComment(eventInfo.getEventComment());
								mqcJobOper.setLastEventTime(eventInfo.getEventTime());
								mqcJobOper.setLastEventTimeKey(eventInfo.getEventTimeKey());
								mqcJobOper.setLastEventName(eventInfo.getEventName());
								
								ExtendedObjectProxy.getMQCJobOperService().create(eventInfo, mqcJobOper);
							}
						}						
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
						
						Product product = ProductServiceProxy.getProductService().selectByKey(new ProductKey((String)productData.get(0).get("PRODUCTNAME")));
						if(productData == null || productData.size()==0)
						{
							continue;
						}
						
						// Start 2019.09.29 Add By Park Jeong Su Mantis 4922
						
						if(!StringUtils.equals(mqcProductSpec, (String)product.getUdfs().get("MQCUSEPRODUCTSPEC")  )){
							throw new CustomException("PRODUCT-0036");
						}
						
						if(!StringUtils.equals(processFlowName, (String)product.getUdfs().get("MQCUSEPROCESSFLOW")  )){
							throw new CustomException("PRODUCT-0037");
						}

						// End 2019.09.29 Add By Park Jeong Su Mantis 4922

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
		
		// after
		if(StringUtil.equals(checkFlag, "TRUE"))
		{
			for(int j = 0; j < mqcTemplatePositionList.size(); j++)
			{
				MQCTemplatePosition mqcTemplatePosition = ExtendedObjectProxy.getMQCTemplatePositionService().selectByKey(false, new Object[] {mqcTemplateName, mqcTemplatePositionList.get(j).get("PROCESSOPERATIONNAME"), mqcTemplatePositionList.get(j).get("PROCESSOPERATIONVERSION"), mqcTemplatePositionList.get(j).get("POSITION")});
				ExtendedObjectProxy.getMQCTemplatePositionService().remove(eventInfo, mqcTemplatePosition);
			}
		}
				
		return doc;
	}
	
}
