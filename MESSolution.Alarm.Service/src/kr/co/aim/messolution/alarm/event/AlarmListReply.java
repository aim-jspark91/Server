package kr.co.aim.messolution.alarm.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.jdom.Document;
import org.jdom.Element;

public class AlarmListReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String sMachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		Element eleBody = SMessageUtil.getBodyElement(doc);

		List<Element> eleReplyAlarmList = new ArrayList<Element>();
		
		if(eleBody!=null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", false))
			{
				String unitName = SMessageUtil.getChildText(eledur, "UNITNAME", false);
				
				List<Element> seqItemList = SMessageUtil.getSubSequenceItemList(eledur, "ALARMLIST", false);

				for(Element elem: seqItemList)
				{
					String sAlarmCode = SMessageUtil.getChildText(elem, "ALARMCODE", false);
					String sAlarmSeverity = SMessageUtil.getChildText(elem, "ALARMSEVERITY", false);
					String sAlarmText = SMessageUtil.getChildText(elem, "ALARMTEXT", false);
					String sSubunitName = SMessageUtil.getChildText(elem, "SUBUNITNAME", false);
					
					eleReplyAlarmList.add(setSubElement(sMachineName, unitName, sAlarmCode, sAlarmSeverity, sAlarmText, sSubunitName));
				}
			}	
		}
			
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "REPLYALARMLIST", eleReplyAlarmList);	
		

		//return doc to OIC;
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}
	}
	
	
	//2019.04.17 dmlee
	private Element setSubElement(String machineName, String unitName, String alarmCode, String alarmSeverity, String alarmText, String subUnitName)
	{
		Element eleAlarm = new Element("ALARM");
		 
		try 
		{
			XmlUtil.addElement(eleAlarm, "MACHINENAME", machineName);
			XmlUtil.addElement(eleAlarm, "UNITNAME", unitName);
			XmlUtil.addElement(eleAlarm, "SUBUNITNAME", subUnitName);
			XmlUtil.addElement(eleAlarm, "ALARMCODE", alarmCode);
			XmlUtil.addElement(eleAlarm, "ALARMSEVERITY", alarmSeverity);
			XmlUtil.addElement(eleAlarm, "ALARMTEXT", alarmText);
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Set Alarm Element Fail"));
		}
		
		return eleAlarm;
	}
	
}
