package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DSPFlagSetUp extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
		
		for(Element eleMachineData : machineList)
		{
			String factoryName = SMessageUtil.getChildText(eleMachineData, "FACTORYNAME", true);
			String areaName = SMessageUtil.getChildText(eleMachineData, "AREANAME", true);
			String machineName = SMessageUtil.getChildText(eleMachineData, "MACHINENAME", true);
			String dspFlag = SMessageUtil.getChildText(eleMachineData, "DSPFLAG", false);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			Map<String, String> udfs = machineData.getUdfs();
			udfs.put("DSPFLAG", dspFlag);
			machineData.setUdfs(udfs);
						
			SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
		
		return doc;
	}

}
