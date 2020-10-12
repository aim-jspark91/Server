package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;
import org.jdom.Element;

public class OpCallSend extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
				
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OpCallMessageSend", getEventUser(), getEventComment(), "", "");
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if(eleBody != null)
		{
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String subunitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
						
			//get machineData
			/* Send OPCALL messages in machines, units and SubUnit */
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Machine unitData = null;
			Machine subunitData = null;
			
			//MES-EAP protocol (check use or not ?) 
			/* Send OPCALL messages in SubUnit */
			String targetSubjectName = StringUtil.EMPTY;
			SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
            targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
            
//			if(StringUtil.isNotEmpty(subunitName))
//			{
//				subunitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subunitName);
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", subunitData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(subunitData.getUdfs(), "MCSUBJECTNAME");
//			}
//			/* Send OPCALL messages in Unit */
//			else if(StringUtil.isNotEmpty(unitName))
//			{
//				unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", unitData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(unitData.getUdfs(), "MCSUBJECTNAME");
//			}
//			/* Send OPCALL messages in Machine */
//			else
//			{
//				SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());
//				targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
//			}
			
			SMessageUtil.setHeaderItemValue(doc, "EVENTCOMMENT", "OpCallMessageSend");
								
			String sendMsg = JdomUtils.toString(doc);
								
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
			
		}
		
		//success then report to FMC (FMS?)
		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMCsvr"), doc, "FMCSender");	
		}
		catch(Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}	
	}	
}
