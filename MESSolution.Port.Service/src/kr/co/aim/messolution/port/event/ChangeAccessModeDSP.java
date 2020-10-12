package kr.co.aim.messolution.port.event;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeAccessModeDSP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPLAccessMode = SMessageUtil.getBodyItemValue(doc, "PLACCESSMODE", false);
		String sPUAccessMode = SMessageUtil.getBodyItemValue(doc, "PUACCESSMODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessModeDSP", this.getEventUser(), this.getEventComment(), null, null);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
		PortKey portKey = new PortKey();
		portKey.setMachineName(sMachineName);
		portKey.setPortName(sPortName);
		Port portData = PortServiceProxy.getPortService().selectByKeyForUpdate(portKey);
		
		String defaultAccessMode = "Auto";
		
		Map<String, String> portUdfs = portData.getUdfs();
		
		//Validation 
		if(sPLAccessMode.isEmpty() && sPUAccessMode.isEmpty())
		{
			throw new CustomException("PORT-1003");
		}
		
		if(sPLAccessMode.isEmpty() && !sPUAccessMode.isEmpty())
		{
			sPLAccessMode = CommonUtil.getValue(portUdfs, "PLACCESSMODE");
			if(sPLAccessMode.isEmpty())
			{
				sPLAccessMode = defaultAccessMode;
			}
		}
		
		if(!sPLAccessMode.isEmpty() && sPUAccessMode.isEmpty())
		{	
			sPUAccessMode =  CommonUtil.getValue(portUdfs, "PUACCESSMODE");
			
			if(sPUAccessMode.isEmpty())
			{
				sPUAccessMode = defaultAccessMode;
			}
		}		
		
		portUdfs.put("PLACCESSMODE", sPLAccessMode);
		portUdfs.put("PUACCESSMODE", sPUAccessMode);
		PortServiceProxy.getPortService().update(portData);
		
		SetEventInfo changeAccessEvent = new SetEventInfo();
		changeAccessEvent.setUdfs(portUdfs);		
		
		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, changeAccessEvent, eventInfo);
		
		return doc;
	}
}