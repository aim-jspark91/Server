package kr.co.aim.messolution.port.event;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
/* 2018.04.02, hhlee, Not used Port State Update ==>> */
//import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CratePortStateReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/* Copy to FMCSender. Add, hhlee, 20180327 */
		Document fmcsenddoc = (Document)doc.clone();
				
		/* Send message only in " Online Initial ". Add, hhlee, 20180327 */
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_CratePortStateCheckReply");
			
		/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);
		
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> portList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);
		
		/* Check Machine Data(Online/OffLine) Add, hhlee, 20180327 */
        Machine machineData = null;
		String currentCommunicationName = StringUtil.EMPTY;
		
		try
		{
			/* Check Machine Data(Online/OffLine) Add, hhlee, 20180327 */
	        machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
			currentCommunicationName = machineData.getCommunicationState();
			
			for (Element elePort : portList)
			{				
				String sPortName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
				String sPortType = SMessageUtil.getChildText(elePort, "PORTTYPE", true);
				String sPortUseType = SMessageUtil.getChildText(elePort, "PORTUSETYPE", true);
				String sPortAccessMode = SMessageUtil.getChildText(elePort, "PORTACCESSMODE", true);
				
				if(StringUtil.equals(StringUtil.upperCase(sPortAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL)))
		        {
				    sPortAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_MANUAL;
		        }
		        else if(StringUtil.equals(StringUtil.upperCase(sPortAccessMode), StringUtil.upperCase(GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO)))
		        {
		            sPortAccessMode = GenericServiceProxy.getConstantMap().PORT_ACCESSMODE_AUTO;
		        }
		        else
		        {
		        }
				
				/* Additional data as information */
				//String crateName = SMessageUtil.getChildText(elePort, "CRATENAME", false);
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				Map<String, String> udfs = new HashMap<String, String>();
				
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
				
				/* 2018.04.02, hhlee, Not used Port State Update ==>> */
				////change port state
				////eventInfo.setEventName("ChangeState");
				//eventInfo = EventInfoUtil.makeEventInfo("ChangeState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
				//MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);
				//MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, makePortStateByStateInfo, eventInfo);
				/* <<== 2018.04.02, hhlee, Not used Port State Update */
				
				//change port access mode
				//eventInfo.setEventName("ChangeAccessMode");
				eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
				MakeAccessModeInfo makeAccessModeInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sPortAccessMode);
				MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);
				
				//change port type
				if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(sPortType))
				{
					eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", sPortType));
				}
				else
				{
					//eventInfo.setEventName("ChangeType");
					eventInfo = EventInfoUtil.makeEventInfo("ChangeType", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
					udfs.put("PORTTYPE", sPortType);
					SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
					MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
				}
				
				//change port use type
				if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals(sPortUseType))
				{
					eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTUSETYPE", sPortUseType));
				}
				else
				{
					//eventInfo.setEventName("ChangeUseType");
					eventInfo = EventInfoUtil.makeEventInfo("ChangeUseType", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
					udfs.put("PORTUSETYPE", sPortUseType);
					SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
					MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
				}				
			}
			
			/**
			 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
			 * ====================================================================
			 */		
			if(StringUtil.isNotEmpty(CommonUtil.getValue(machineData.getUdfs(), "ONLINEINITIALCOMMSTATE")))
			{
				handleSyncAsync(doc, getOriginalSourceSubjectName());					
			}
		}
		catch (CustomException ce)
		{
			eventLog.warn(ce.getLocalizedMessage());
			
			/**
			 * 20180326 by hhlee : Send NG/OK results only in " Online Initial ".
			 * ====================================================================
			 */
			//if(StringUtil.isNotEmpty(currentCommunicationName)&& 
			//		StringUtil.upperCase(currentCommunicationName).equals("OFFLINE"))
			{
				handleSyncAsyncFault(doc, getOriginalSourceSubjectName(), ce);
			}
		}
		
		//150117 by swcho : success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), fmcsenddoc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}			
	}

}
