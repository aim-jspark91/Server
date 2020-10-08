package kr.co.aim.messolution.transportjob.event;

import java.text.MessageFormat;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author smkang
 * @since 2018.05.01
 * @see Processing TEX receives AllPortReport from MCS.
 */
public class AllPortReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> portElementList = SMessageUtil.getBodySequenceItem(doc, "PORTLIST", true).getChildren();
		
		if (portElementList != null && portElementList.size() > 0) {
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			
			EventInfo eventInfo = null;
			
			for (Element portElement : portElementList) {
				try {
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
					
					String portName = portElement.getChildText("PORTNAME");
					String portState  = portElement.getChildText("PORTSTATE");
					String portAccessMode  = portElement.getChildText("PORTACCESSMODE");
					String portType  = portElement.getChildText("PORTTYPE");
					
					Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
					
					// PORTSTATE Update
					try {
						// Compare Previous State and Current State
						if(!StringUtils.equals(portData.getPortStateName(), portState)) {
							// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//							MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, portState);
							MakePortStateByStateInfo makePortStateByStateInfo = new MakePortStateByStateInfo();
							makePortStateByStateInfo.setPortStateName(portState);
							makePortStateByStateInfo.setValidateEventFlag("Y");
							
							PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);
						} else {
							ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-0007");
							
							if (errorDef != null)
								eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), portData.getKey().getMachineName(), portData.getKey().getPortName(), portData.getPortStateName()));
						}
					} catch (InvalidStateTransitionSignal ie) {
						ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-9003");
						
						if (errorDef != null)
							eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), machineName, portName));
					} catch (FrameworkErrorSignal fe) {
						ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-9999");
						
						if (errorDef != null)
							eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), machineName, portName));
					} catch (NotFoundSignal ne) {
						eventLog.error(ne);
					}
					
					// PORTACCESSMODE and PORTTYPE Update
					try {
						// Modified by smkang on 2018.05.01 - MCS reports FULLSTATE as FULL or NOTFULL, but MES records FULLSTATE as Full or Empty.
						if (StringUtils.equals(portAccessMode, "AUTO"))
							portAccessMode = "Auto";
						else if (StringUtils.equals(portAccessMode, "MANUAL"))
							portAccessMode = "Manual";
						
						// Compare Previous State and Current State
						if(!portData.getUdfs().get("PORTTYPE").equals(portType) || !StringUtils.equals(portData.getAccessMode(), portAccessMode)) {
							if (!StringUtils.equals(portData.getAccessMode(), portAccessMode)) {
								eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");
								
								// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//								MakeAccessModeInfo makeAccessModeInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);
								MakeAccessModeInfo makeAccessModeInfo = new MakeAccessModeInfo();
								makeAccessModeInfo.setAccessMode(portAccessMode);
								makeAccessModeInfo.setValidateEventFlag("N");
								
								MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);
							}
							
							if(!portData.getUdfs().get("PORTTYPE").equals(portType)) {
								eventInfo = EventInfoUtil.makeEventInfo("ChangeType", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");
								
								// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//								Map<String, String> udfs = new HashMap<String, String>();
//								udfs.put("PORTTYPE", portType);
//								
//								SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
								SetEventInfo setEventInfo = new SetEventInfo();
								setEventInfo.getUdfs().put("PORTTYPE", portType);
								
								MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
							}
						} else {
							ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-0008");
							
							if (errorDef != null)
								eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), portData.getKey().getMachineName(), portData.getKey().getPortName(), portData.getUdfs().get("PORTTYPE"), portData.getAccessMode()));
						}
					} catch (InvalidStateTransitionSignal ie) {
						ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-9003");
						
						if (errorDef != null)
							eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), machineName, portName));
					} catch (FrameworkErrorSignal fe) {
						ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("PORT-9999");
						
						if (errorDef != null)
							eventLog.error(MessageFormat.format(errorDef.getLoc_errorMessage(), machineName, portName));
					} catch (NotFoundSignal ne) {
						eventLog.error(ne);
					}
				} catch (CustomException e) {
					// TODO: handle exception
					// Commented by smkang on 2018.05.01 - Although any port has problem in for loop, another ports should be updated.
					//									   So CustomException handler is added here.
				}
			}
			
			try {
				// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
				GenericServiceProxy.getESBServive().sendBySender(doc, "FMCSender");	
			} catch(Exception ex) {
				eventLog.warn("FMC Report Failed!");
			}
		}
	}
}