package kr.co.aim.messolution.port.event;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class UnloadRequest extends AsyncHandler {

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
		
		if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
        }
        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
        {
            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
        }
        else
        {
        }
		Port portData = CommonUtil.getPortInfo(machineName, portName);
		
		/* 20190218, hhlee, add, add ReadyToUnLoad Validation */
        if(StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReservedToUnload))
        {
            throw new CustomException("SYS-9999", "UnLoadRequest", "TransferState is ReservedToUnload");        
        }
		
		//port handling
		if( !StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToUnload) )
		{
			eventInfo.setEventName("ChangeTransferState");
			
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
			makeTransferStateInfo.setValidateEventFlag("N");
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			//	2019.02.14	AIM.YUNJM	USE dsp
//            portData.getUdfs().put("UNLOADTIME", eventInfo.getEventTime().toString());
//			makeTransferStateInfo.setUdfs(portData.getUdfs());
			makeTransferStateInfo.getUdfs().put("UNLOADTIME", eventInfo.getEventTime().toString());

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}
		
		//20.11.06 DMLee (Update CST Setting Code)
		if(!StringUtils.isEmpty(carrierSettingCode) && !StringUtils.equals(portData.getUdfs().get("CSTSETTINGCODE"), carrierSettingCode))
		{
			eventInfo.setEventName("ChangeCSTSettingCode");
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CSTSETTINGCODE", carrierSettingCode);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		
		//carrier handling
		//MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);
		
		/* 20181116, hhlee, add, add FACTORYNAME Element (Requsted by DSP) ==>> */
		SMessageUtil.setBodyItemValue(doc, "FACTORYNAME", GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, true);
		/* <<== 20181116, hhlee, add, add FACTORYNAME Element (Requsted by DSP) */
		
		sendToDSP(doc);

		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
		
//		//2019.09.02 Add By Park Jeong Su Mantis 4691
//		try {
//			
//			List<Lot> lotList = LotServiceProxy.getLotService().select("WHERE carrierName = ?", new Object[] {carrierName});
//			eventLog.info("LotList size : " + lotList.size());
//			if(lotList!=null && lotList.size()>0){
//				String lotName = lotList.get(0).getKey().getLotName();
//		        try
//		        {
//		        	Lot lotData =  LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
//		            lotData.getUdfs().put("FIRSTCHECKRESULT", "N"); 
//		            LotServiceProxy.getLotService().update(lotData);
//		        }
//		        catch (Exception ex)
//		        {
//		        	eventLog.error("[ modifyLotFirstCheckResultY ] Update Fail Lot Data.");
//		            throw new CustomException("SYS-9999", "Lot", "No Lot to process");
//		        }
//			}
//		} catch (CustomException ce) {
//			eventLog.info("Change First Check Result Error.");
//		}
	}
	
	private void sendToDSP(Document doc)
	{
		// send to DSPsvr
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
