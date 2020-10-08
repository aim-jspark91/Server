package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CrateUnloadComplete extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		
		/* Additional data as information */
		//String porttype = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		//String portusetype = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		//String portaccessmode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		Port portData = CommonUtil.getPortInfo(machineName, portName);
				
		/* 20181204, hhlee, add, add EventName ==>> */
        eventInfo.setEventName("Unload");
        /* <<== 20181204, hhlee, add, add EventName */
		MESConsumableServiceProxy.getConsumableServiceUtil().changeConsumableLocation(
				eventInfo, crateName, portData.getAreaName(), machineName, portName);

		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
		{
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
			makeTransferStateInfo.setValidateEventFlag("N");
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String,String> udfs = portData.getUdfs();
//            udfs.put("CARRIERNAME", "");
//            makeTransferStateInfo.setUdfs(udfs);
			//makeTransferStateInfo.setUdfs(portData.getUdfs());
            makeTransferStateInfo.getUdfs().put("CARRIERNAME", "");
			
			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}
		
		if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
		{
			eventInfo.setEventName("ChangeFullState");
			
			SetEventInfo setEventInfo = new SetEventInfo();
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			portData.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
//			portData.getUdfs().put("CARRIERNAME", "");
//			setEventInfo.setUdfs(portData.getUdfs());
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
			setEventInfo.getUdfs().put("CARRIERNAME", "");

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}
}