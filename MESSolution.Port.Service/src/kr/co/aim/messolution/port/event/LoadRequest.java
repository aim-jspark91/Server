package kr.co.aim.messolution.port.event;

import java.util.Map;

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

public class LoadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		//20.11.06 DMLee (Add Item)
		String carrierSettingCode = SMessageUtil.getBodyItemValue(doc, "CARRIERSETTINGCODE", false);
		String portStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATENAME", false);
		String reserveProductID = SMessageUtil.getBodyItemValue(doc, "REVERSEPRODUCTID", false);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		/* 20190218, hhlee, add, add ReadyToLoad Validation */
		if(StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReservedToLoad))
		{
		    throw new CustomException("SYS-9999", "LoadRequest", "TransferState is ReservedToLoad");	    
		}
		
		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
		{
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
			makeTransferStateInfo.setValidateEventFlag("N");
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
			// Added by smkang on 2018.12.27 - Because DSP uses timer, so a transport job can be requested before MES receives LoadRequest event.
			//								   UnloadComplete logic is added here.
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("CARRIERNAME", "");
//			makeTransferStateInfo.setUdfs(udfs);
			makeTransferStateInfo.getUdfs().put("CARRIERNAME", "");
			
			//20.11.06 DMLee (Update CST Setting Code)
			if(!StringUtils.isEmpty(carrierSettingCode) && !StringUtils.equals(portData.getUdfs().get("CSTSETTINGCODE"), carrierSettingCode))
			{
				makeTransferStateInfo.getUdfs().put("CSTSETTINGCODE", carrierSettingCode);
			}
			
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
			
			//20.11.06 DMLee (Update CST Setting Code)
			if(!StringUtils.isEmpty(carrierSettingCode) && !StringUtils.equals(portData.getUdfs().get("CSTSETTINGCODE"), carrierSettingCode))
			{
				setEventInfo.getUdfs().put("CSTSETTINGCODE", carrierSettingCode);
			}
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		
		/* 20181116, hhlee, add, add FACTORYNAME Element (Requsted by DSP) ==>> */
        SMessageUtil.setBodyItemValue(doc, "FACTORYNAME", GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, true);
        /* <<== 20181116, hhlee, add, add FACTORYNAME Element (Requsted by DSP) */
        
        sendToDSP(doc); 
		sendToFMC(doc);
	}
	
	private void sendToDSP(Document doc)
	{
		//success then send to DSPsvr
		try
		{
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("DSPsvr");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "DSPSender");
		}
		catch (Exception e)
		{
			eventLog.error("sending to DSPsvr is failed");
		}
	}
	
	private void sendToMCS(Document doc)
	{
		//success then send to TEXsvr
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
			//success then report to FMC
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
		}
		catch (Exception ex)
		{
			eventLog.error("sending to FMC is failed");
		}
	}
}