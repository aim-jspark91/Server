package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class PortoutInhibit extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> PortList      = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);
		String machineName =  SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		//modify byJHIYING ON20190819 start mantis:4568
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		//modify byJHIYING ON20190819 end mantis:4568
		//2019.03.15_hsryu_Change EventName. Mantis 0003082.
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change PortOutInhibit", this.getEventUser(), this.getEventComment(), "", "");
		for (Element elePort : PortList) 
		{
			String portName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
			String portInInhibitFlag = SMessageUtil.getChildText(elePort, "PORTININHIBITFLAG", false);
			String portOutInhibitFlag = SMessageUtil.getChildText(elePort, "PORTOUTINHIBITFLAG", false);
			
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(portData.getUdfs());
			if(!StringUtil.equals(portData.getUdfs().get("PORTOUTINHIBITFLAG"), "Y"))
			{
				setEventInfo.getUdfs().put("PORTOUTINHIBITFLAG", "Y");
			}
			else
			{
				setEventInfo.getUdfs().put("PORTOUTINHIBITFLAG", "N");
			}
			
			//modify byJHIYING ON20190819 start mantis:4568
//			eventInfo.setEventComment(this.getEventComment()+" : "+ note);
			eventInfo.setEventComment(note); // modify jhying on20200403 mantis:5608
			//modify byJHIYING ON20190819 end mantis:4568

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
							
		}
		return doc;
	}
}
