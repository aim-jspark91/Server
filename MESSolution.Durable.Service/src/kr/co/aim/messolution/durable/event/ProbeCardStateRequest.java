package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ProbeCardStateRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
					
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);	
				
		//get line machine
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
				
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
				
		if(StringUtils.isEmpty(targetSubjectName))
	    	throw new CustomException("LOT-9006", machineName);
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}
}
