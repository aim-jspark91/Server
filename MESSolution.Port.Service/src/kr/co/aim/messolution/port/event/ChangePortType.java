package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangePortType extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.05.10 - PortType of PortSpec doesn't need to be changed.
//		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
//		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		
//		//String sAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
//		
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", this.getEventUser(), this.getEventComment(), null, null);
//		
//		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
//		
//		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Port.class.getSimpleName());
//		
//		SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
//		
//		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
//
//		PortSpec portSpec = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(sMachineName, sPortName); 
//		String sPortType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
//		// Port Type Change.
//		if( !StringUtil.equals(sPortType, portSpec.getPortType()) )
//		{
//			eventLog.info(String.format("MES PortType=[%s], Current PortSpec.PortType=[%s]", sPortType, portSpec.getPortType()));
//			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
//			changeSpecInfo.setPortType(sPortType);
//			PortServiceProxy.getPortSpecService().changeSpec(portSpec.getKey(), changeSpecInfo, "", "");	
//		}
//		
//		return doc;
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", this.getEventUser(), this.getEventComment(), null, null);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		// Commented by smkang on 2018.05.10 - Need to compare old PortType with new PortType.
		if (!StringUtils.equals(portData.getUdfs().get("PORTTYPE"), portType)) {
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Port.class.getSimpleName());
			
			SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
		}
		
		return doc;
	}
}