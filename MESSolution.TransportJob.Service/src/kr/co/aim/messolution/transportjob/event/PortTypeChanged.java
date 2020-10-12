package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.04.28
 * @see Processing TEX receives PortTypeChanged from MCS.
 */
public class PortTypeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portAccessMode  = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);
		String portType  = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
		
		try {
			// Modified by smkang on 2018.05.01 - MCS reports FULLSTATE as FULL or NOTFULL, but MES records FULLSTATE as Full or Empty.
			if (StringUtils.equals(portAccessMode, "AUTO"))
				portAccessMode = "Auto";
			else if (StringUtils.equals(portAccessMode, "MANUAL"))
				portAccessMode = "Manual";
			
			// Compare Previous State and Current State
			if(!portData.getUdfs().get("PORTTYPE").equals(portType) || !StringUtils.equals(portData.getAccessMode(), portAccessMode)) {
				if (!StringUtils.equals(portData.getAccessMode(), portAccessMode)) {
					MakeAccessModeInfo makeAccessModeInfo = new MakeAccessModeInfo();
					makeAccessModeInfo.setAccessMode(portAccessMode);
					makeAccessModeInfo.setValidateEventFlag("N");
					
					MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);
				}
				
				if(!portData.getUdfs().get("PORTTYPE").equals(portType)) {
					eventInfo = EventInfoUtil.makeEventInfo("ChangeType", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");
					
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("PORTTYPE", portType);
					
					MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
				}
			} else {
				throw new CustomException("PORT-0008", portData.getKey().getMachineName(), portData.getKey().getPortName(), portData.getUdfs().get("PORTTYPE"), portData.getAccessMode());
			}
		} catch (InvalidStateTransitionSignal ie) {
			throw new CustomException("PORT-9003", machineName, portName);
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("PORT-9999", machineName, portName);
		} catch (NotFoundSignal ne) {
			// Modified by smkang on 2018.05.10 - Need to throw exception.
//			eventLog.error(ne);
//			return;
			throw new CustomException(ne);
		}
		
		try {
			// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
			GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
		} catch(Exception ex) {
			eventLog.warn("FMC Report Failed!");
		}
	}
}