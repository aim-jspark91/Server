package kr.co.aim.messolution.port.event;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.jdom.Document;

public class ChangePortTransferState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		String sTransferStateName = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", true);
		
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
		
		MakeTransferStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeTransferStateInfo(portData, sTransferStateName);
		
		// Added by smkang on 2019.02.22 - If a user changes TransferState to 'ReadyToLoad', CarrierName of the port will be deleted.
		transitionInfo.getUdfs().put("CARRIERNAME", "");
		
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, transitionInfo, eventInfo);
		
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
		return doc;
	}

}
