package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class UnloadComplete extends AsyncHandler {

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

        // Deleted by smkang on 2018.12.27 - Because DSP uses timer, so a transport job can be requested before MES receives LoadRequest event.
//        if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
//        {
//            eventInfo.setEventName("ChangeTransferState");
//
//            MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
//            makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//            makeTransferStateInfo.setValidateEventFlag("N");
//            Map<String,String> udfs = portData.getUdfs();
//            udfs.put("CARRIERNAME", "");
//            makeTransferStateInfo.setUdfs(udfs);
//
//            MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
//        }

        if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
        {
            eventInfo.setEventName("ChangeFullState");

            SetEventInfo setEventInfo = new SetEventInfo();
            // 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//            portData.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
//            portData.getUdfs().put("CARRIERNAME", "");
//			//	2019.02.28	AIM.YUNJM	USE dsp
//            portData.getUdfs().put("UNLOADTIME", "");
//            setEventInfo.setUdfs(portData.getUdfs());
            
            setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
            setEventInfo.getUdfs().put("CARRIERNAME", "");
            setEventInfo.getUdfs().put("UNLOADTIME", "");

            MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
        }

        /* 20180531, Carrier Hold processed if CleanCSTEnd is not reported. ==>> */
        //cleaning EQP
        MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
        
        // Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
        // if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CCLN"))
        if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
        {
            try
            {
                Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
                if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU") &&
                        StringUtil.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
                {
                    MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "HoldCST","VAHC");
                }
                
                /* 20181203, hhlee, delete, duplicate logic ==>> */
                eventInfo.setEventName("ChangePositionState");
                kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
                
             // 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//                Map<String,String> udfs = durableData.getUdfs();
                
                /* 20181203, hhlee, modify, durable Data Modify ==>> */
                /* 20181127, hhlee, modify , MachineName is not null at load port ==>> */
                if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
                {
                    //if(StringUtil.equals(CommonUtil.getValue(durableData.getUdfs(), "TRANSPORTSTATE"), 
                    //        GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP))
                    //{
                    //    udfs.put("MACHINENAME", "");                    
                    //    udfs.put("PORTNAME", "");
                    //    udfs.put("POSITIONNAME", "");
                    //}
                	
                	// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
                    //udfs.put("MACHINENAME", "");             
                    setEventInfo.getUdfs().put("MACHINENAME", "");
                    //udfs.put("PORTNAME", "");
                    //udfs.put("POSITIONNAME", "");
                }
                else
                {
                	// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
                    //udfs.put("MACHINENAME", machineName); 
                    setEventInfo.getUdfs().put("MACHINENAME", machineName);
                    //udfs.put("PORTNAME", "");
                    //udfs.put("POSITIONNAME", "");
                }
                /* <<== 20181127, hhlee, modify , MachineName is not null at load port */
                /* <<== 20181203, hhlee, modify, durable Data Modify */
                
            	// 2019.06.21_hsryu_Delete setUdfs.  other udfs must not be changed.
//                udfs.put("PORTNAME", "");
//                udfs.put("POSITIONNAME", "");
//                setEventInfo.setUdfs(udfs);
                setEventInfo.getUdfs().put("PORTNAME", "");
                setEventInfo.getUdfs().put("POSITIONNAME", "");
                
                MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
                /* <<== 20181203, hhlee, delete, duplicate logic */
            }
            catch(Exception ex)
            {
                eventLog.warn(String.format("DurableData Error CarrierName[%s] PortName[%s] ! - %s" , carrierName, portName, ex.getStackTrace()));
            }
        }
        /* <<== 20180531, Carrier Hold processed if CleanCSTEnd is not reported. */


        //carrier transportState handling OLD
        /*if (StringUtil.isNotEmpty(carrierName))
        MESDurableServiceProxy.getDurableServiceImpl().unload(eventInfo, carrierName);*/
      
        //add by wghuang 20181129
        if (StringUtil.isNotEmpty(carrierName))
        {
            /* 20190422, hhlee, modify, Changed PP, BL, BU, PG Type LoadComplete Update Logic ==>> */
            if(StringUtil.equals(portData.getAccessMode(), GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL))
            {
                /* 20181203, hhlee, modify, durable Data Modify ==>> */
                MESDurableServiceProxy.getDurableServiceImpl().unload(eventInfo, carrierName, portData, 
                        CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"), machineSpecData.getAreaName());
                /* <<== 20181203, hhlee, modify, durable Data Modify */
            }
            /* <<== 20190422, hhlee, modify, Changed PP, BL, BU, PG Type LoadComplete Update Logic */
        }
        /* 20181116, hhlee, delete, DSP send Message(LoadRequest, UnloadRequest) */
        //success then report to DSP
        //sendToDSP(doc);

        //success then report to FMC
        try
        {
            GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
        }
        catch(Exception ex)
        {
            eventLog.warn("FMC Report Failed!");
        }
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
}

//package kr.co.aim.messolution.port.event;
//
//import java.util.Map;
//
//import kr.co.aim.messolution.durable.MESDurableServiceProxy;
//import kr.co.aim.messolution.generic.GenericServiceProxy;
//import kr.co.aim.messolution.generic.errorHandler.CustomException;
//import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
//import kr.co.aim.messolution.generic.util.CommonUtil;
//import kr.co.aim.messolution.generic.util.EventInfoUtil;
//import kr.co.aim.messolution.generic.util.SMessageUtil;
//import kr.co.aim.messolution.port.MESPortServiceProxy;
//import kr.co.aim.greentrack.durable.management.data.Durable;
//import kr.co.aim.greentrack.generic.info.EventInfo;
//import kr.co.aim.greentrack.generic.util.StringUtil;
//import kr.co.aim.greentrack.machine.management.data.MachineSpec;
//import kr.co.aim.greentrack.port.management.data.Port;
//import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
//import kr.co.aim.greentrack.port.management.info.SetEventInfo;
//
//import org.apache.commons.lang.StringUtils;
//import org.jdom.Document;
//
//public class UnloadComplete extends AsyncHandler {
//
//	@Override
//	public void doWorks(Document doc) throws CustomException
//	{
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
//
//		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
//		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
//		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
//		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
//
//		if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
//        {
//            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
//        }
//        else if(StringUtil.equals(StringUtil.upperCase(portAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
//        {
//            portAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
//        }
//        else
//        {
//        }
//		
//		Port portData = CommonUtil.getPortInfo(machineName, portName);
//
//		if(!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
//		{
//			eventInfo.setEventName("ChangeTransferState");
//
//			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
//			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
//			makeTransferStateInfo.setValidateEventFlag("N");
//			Map<String,String> udfs = portData.getUdfs();
//			udfs.put("CARRIERNAME", "");
//			makeTransferStateInfo.setUdfs(udfs);
//
//			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
//		}
//
//		if(!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
//		{
//			eventInfo.setEventName("ChangeFullState");
//
//			SetEventInfo setEventInfo = new SetEventInfo();
//			portData.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
//			portData.getUdfs().put("CARRIERNAME", "");
//			setEventInfo.setUdfs(portData.getUdfs());
//
//			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
//		}
//
//		/* 20180531, Carrier Hold processed if CleanCSTEnd is not reported. ==>> */
//		//cleaning EQP
//		MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(machineName);
//		
//		// Modified by smkang on 2018.11.28 - ConstructType of cassette cleaner is changed to CCLN.
//        // if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals("CCLN"))
//       	if (CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE").equals(GenericServiceProxy.getConstantMap().ConstructType_CassetteClenaer))
//        {
//            Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//            if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU") &&
//                    StringUtil.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
//            {
//                MESDurableServiceProxy.getDurableServiceUtil().setCarrierHold(durableData,"Hold", getEventUser(), getEventComment(), "Hold CST","Hold CST");
//            }
//
//            eventInfo.setEventName("ChangePositionState");
//            kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
//
//            Map<String,String> udfs = durableData.getUdfs();
//
//            /* 20181127, hhlee, modify , MachineName is not null at load port ==>> */
//            if(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU"))
//            {
//                udfs.put("MACHINENAME", "");
//            }
//            else
//            {
//                udfs.put("MACHINENAME", machineName);
//            }
//            /* <<== 20181127, hhlee, modify , MachineName is not null at load port */
//            
//            udfs.put("PORTNAME", "");
//            udfs.put("POSITIONNAME", "");
//            setEventInfo.setUdfs(udfs);
//
//            MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
//        }
//		/* <<== 20180531, Carrier Hold processed if CleanCSTEnd is not reported. */
//
//
//        //carrier transportState handling OLD
//	    /*if (StringUtil.isNotEmpty(carrierName))
//		MESDurableServiceProxy.getDurableServiceImpl().unload(eventInfo, carrierName);*/
//      
//        //add by wghuang 20181129
//        if (StringUtil.isNotEmpty(carrierName))
//		MESDurableServiceProxy.getDurableServiceImpl().unloadDurable(eventInfo, carrierName, portData, CommonUtil.getValue(machineSpecData.getUdfs(), "CONSTRUCTTYPE"));
//
//		/* 20181116, hhlee, delete, DSP send Message(LoadRequest, UnloadRequest) */
//		//success then report to DSP
//		//sendToDSP(doc);
//
//		//success then report to FMC
//		try
//		{
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");
//		}
//		catch(Exception ex)
//		{
//			eventLog.warn("FMC Report Failed!");
//		}
//	}
//
//	private void sendToDSP(Document doc)
//	{
//		// send to DSPsvr
//		try
//		{
//			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("DSPsvr");
//			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "DSPSender");
//		}
//		catch (Exception e)
//		{
//			eventLog.error("sending to DSPsvr is failed");
//		}
//	}
//
//}