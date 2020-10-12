package kr.co.aim.messolution.port.event;

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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CrateUnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
		
		/* Additional data as information */
		//String porttype = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		//String portusetype = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		//String portaccessmode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		
		/* 20190218, hhlee, add, add ReadyToUnLoad Validation */
        if(StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReservedToUnload))
        {
            throw new CustomException("SYS-9999", "CrateUnloadRequest", "TransferState is ReservedToUnload");        
        }
        
		//port handling
		if( !StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToUnload) )
		{
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
			makeTransferStateInfo.setValidateEventFlag("N");
			// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
			//makeTransferStateInfo.setUdfs(portData.getUdfs());

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}
		
		sendToMCS(doc);
		
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
	
	private void sendToMCS(Document doc)
	{
		// send to TEXsvr
		try
		{
			String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEXsvr");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEXSender");
		}
		catch (Exception e)
		{
			eventLog.error("sending to TEXsvr is failed");
		}
	}	
}
