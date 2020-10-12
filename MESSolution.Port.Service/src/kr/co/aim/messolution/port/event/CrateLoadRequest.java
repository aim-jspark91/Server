package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CrateLoadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		
		/* Additional data as information */
		//String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", false);
		//String porttype = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		//String portusetype = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		//String portaccessmode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		/* 20190218, hhlee, add, add ReadyToLoad Validation */
        if(StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReservedToLoad))
        {
            throw new CustomException("SYS-9999", "CrateLoadRequest", "TransferState is ReservedToLoad");        
        }
        
		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
		{
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
			makeTransferStateInfo.setValidateEventFlag("N");
			// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
			//makeTransferStateInfo.setUdfs(portData.getUdfs());
			
			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}
		
		if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
		{
			eventInfo.setEventName("ChangeFullState");
			
			SetEventInfo setEventInfo = new SetEventInfo();
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			portData.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
//			setEventInfo.setUdfs(portData.getUdfs());
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}		
		
		sendToMCS(doc);
		sendToFMC(doc);
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
	
	private void sendToFMC(Document doc)
	{
		try
		{
			//159512 by swcho : success then report to FMC
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		}
		catch (Exception ex)
		{
			eventLog.error("FMC Report Failed!");
		}
	}
}