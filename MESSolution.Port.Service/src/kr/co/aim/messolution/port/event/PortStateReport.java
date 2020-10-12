package kr.co.aim.messolution.port.event;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class PortStateReport extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		/* Copy to FMCSender. Add, hhlee, 20180327 */
		Document fmcsenddoc = (Document)doc.clone();

		/* Send message only in " Online Initial ". Add, hhlee, 20180327 */
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_PortStateCheckReply");

		/* 20180326 by hhlee : Send NG/OK results only in " Online Initial ". */
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK",true);
		SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "",true);

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> portList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);

		/* Check Machine Data(Online/OffLine) Add, hhlee, 20180327 */
        Machine machineData = null;
		String currentCommunicationName = StringUtil.EMPTY;

		/* Check Machine Data(Online/OffLine) Add, hhlee, 20180327 */
        machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);
		currentCommunicationName = machineData.getCommunicationState();

		for (Element elePort : portList)
		{
			String sPortName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
			String sPortStateName = SMessageUtil.getChildText(elePort, "PORTSTATENAME", true);
			String sPortAccessMode = SMessageUtil.getChildText(elePort, "PORTACCESSMODE", true);
			String sPortType = SMessageUtil.getChildText(elePort, "PORTTYPE", true);
			String sPortUseType = SMessageUtil.getChildText(elePort, "PORTUSETYPE", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			Map<String, String> udfs = new HashMap<String, String>();

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
			
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);
			
			PortSpec portSpecData = CommonUtil.getPortSpecInfo(sMachineName, sPortName);
			
			//add by wghuang 20181116
			MESPortServiceProxy.getPortServiceUtil().checkPortStateModelName(portSpecData, sPortName);

			//change port state
			eventInfo.setEventName("ChangeState");
			MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);
			//makePortStateByStateInfo.setValidateEventFlag("N");
			MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, makePortStateByStateInfo, eventInfo);

			/* 20190115, hhlee, modify, change logic ==>> */
	        if(StringUtil.isEmpty(CommonUtil.getValue(portData.getUdfs(), "DOUBLEMAINMACHINENAME")))
	        {
	            /* 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. ==>> */
	            /* 20190115, hhlee, modify, if DOUBLEMAINMACHINENAME is null, update accessMode ==>> */
	            eventInfo.setEventName("ChangeAccessMode");
	            MakeAccessModeInfo makeAccessModeInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sPortAccessMode);
	            MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);
	            /* <<== 20190115, hhlee, modify, if DOUBLEMAINMACHINENAME is null, update accessMode */
	            /* <<== 20180531, Do not process PORTACCESSMODE. ARRAY EAP reported only as AUTO. */
	        }
	        /* <<== 20190115, hhlee, modify, change logic */
			
			//change port type
			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(sPortType))
			{
				eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", sPortType));
			}
			else
			{
				eventInfo.setEventName("ChangeType");
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
				eventInfo.setEventName("ChangeUseType");
				udfs.put("PORTUSETYPE", sPortUseType);
				SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
				MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
			}
		}

		//success then report to FMC
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), fmcsenddoc, "FMCSender");
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}

		return doc;
	}

}
