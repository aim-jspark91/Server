package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class UnitStateRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);	
				
		//get line machine
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
				
		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
				
		if(StringUtils.isEmpty(targetSubjectName))
		    	throw new CustomException("LOT-9006", machineName);
		
		/* 20181030, hhlee, modify, Query Message Send Log record ==>> */
		String originalSourceSubjectName = SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false);
        if(StringUtil.isEmpty(originalSourceSubjectName))
        {
            originalSourceSubjectName =GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");
            SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", originalSourceSubjectName);
        }
		//GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		GenericServiceProxy.getESBServive().recordMessagelogAftersendBySender(targetSubjectName, doc, "EISSender");
		/* <<== 20181030, hhlee, modify, Query Message Send Log record */
	}
}
