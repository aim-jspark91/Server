package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.spccontrol.SPCControlServiceProxy;
import kr.co.aim.greentrack.spccontrol.management.data.SPCControlRule;
import kr.co.aim.greentrack.spccontrol.management.data.SPCControlRuleKey;
import kr.co.aim.greentrack.spccontrol.management.info.CollectDataByDCDataInfo;
import kr.co.aim.greentrack.spccontrol.management.info.CollectDataResult;
import kr.co.aim.greentrack.spccontrol.management.info.ext.ChartResult;
import kr.co.aim.greentrack.spccontrol.management.info.ext.ItemResult;
import kr.co.aim.greentrack.spccontrol.management.info.ext.SPCControlRuleResult;

import org.jdom.Document;
import org.jdom.Element;

public class CheckSPC extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		String machineName 			= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName 			= SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName		 		= SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName		 	= SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String sDataId              = SMessageUtil.getBodyItemValue(doc, "DCDATAID", true);
		
		long dataId = Long.valueOf(sDataId);
		
		CollectDataByDCDataInfo collectDataByDCDataInfo = new CollectDataByDCDataInfo();
		
		collectDataByDCDataInfo.setDCDataId(dataId);
		
		List<CollectDataResult> list = null;
		try 
		{
			list = SPCControlServiceProxy.getSPCControlDataResultService().collectDataByDCData(collectDataByDCDataInfo);
			eventLog.info("list = " + list);
		} 
		catch (Exception e)
		{
			throw new CustomException("DC-0001", dataId);
		}
		
		for(CollectDataResult collectDataResult : list)
		{
			try
			{
				String dcDataId = String.valueOf(dataId);
				String spcControlSpec = collectDataResult.getSPCControlSpecName();
				
				List<ItemResult> irslist = collectDataResult.getIrs();
				
				for(ItemResult itemResult : irslist)
				{
					String itemName = itemResult.getItemName();
					
					ArrayList<ChartResult> chartresultlist = itemResult.getCrs(); 
					
					for(ChartResult chartResult : chartresultlist)
					{
						String chartName = chartResult.getChartName();
						
						List<SPCControlRuleResult> spcControlRuleResultlist = chartResult.getCrrs(); 
						
						for(SPCControlRuleResult controlRuleResult :  spcControlRuleResultlist)
						{
							String controlRuleName = "";
							
							if(!controlRuleResult.isRuleIn())
							{							
								controlRuleName = controlRuleResult.getSPCControlRuleName();

								SPCControlRuleKey controlRuleKey = new SPCControlRuleKey();
								controlRuleKey.setSPCControlRuleName(controlRuleName);
								
								SPCControlRule controlRule = SPCControlServiceProxy.getSPCControlRuleService().selectByKey(controlRuleKey);
								
								String alarmid = controlRule.getUdfs().get("alarmId");
								String alarmType = controlRule.getUdfs().get("alarmType");
								
								//Set Alarm Comment
								StringBuilder sComment = new StringBuilder();
								sComment.append("Unit : " + unitName + "\n");
								sComment.append("DCDataID : " + dcDataId + "\n");
								sComment.append("SPCControlSec : " + spcControlSpec + "\n");
								sComment.append("ItemName : " + itemName + "\n");
								sComment.append("ChartName : " + chartName + "\n");
								sComment.append("ControlRuleName : " + controlRuleName + "\n");
								
								//Create Alarm Message
								Document alarmDoc = writeAlarmMessage(doc, alarmid, alarmType, machineName, lotName, carrierName, sComment.toString());
								
								//Send CNM Server : Create Alarm
								String replySubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
								GenericServiceProxy.getESBServive().sendBySender(replySubject, alarmDoc, "LocalSender");
								
								eventLog.info("RESULTs rSPCServiceImple Rule Out : " + controlRuleName);
								eventLog.info("RESULTs rSPCServiceImple Rule Out : " + alarmid);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				eventLog.info("ERROR SpcAlarmInformationList " + e.getMessage());
				throw new CustomException("DC-0002");
			}
		}
	}
	
	@SuppressWarnings("unused")
	private Document writeAlarmMessage(Document doc, String alarmCode, String alarmType, String machineName, String lotName, String carrierName, String comment)
			throws CustomException
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CreateAlarm");

			boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
			
			Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
			
			Element element1 = new Element("ALARMCODE");
			element1.setText(alarmCode);
			eleBodyTemp.addContent(element1);
			
			Element element2 = new Element("ALARMTYPE");
			element2.setText(alarmType);
			eleBodyTemp.addContent(element2);
			
			Element element3 = new Element("MACHINENAME");
			element3.setText(machineName);
			eleBodyTemp.addContent(element3);
			
			Element element4 = new Element("LOTNAME");
			element4.setText(lotName);
			eleBodyTemp.addContent(element4);
			
			Element element5 = new Element("CARRIERNAME");
			element5.setText(carrierName);
			eleBodyTemp.addContent(element5);
			
			SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", comment);
			
			//overwrite
			doc.getRootElement().addContent(eleBodyTemp);
			
			return doc;
		}
}
