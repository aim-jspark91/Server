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

public class PhotoMaskStateRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		try {
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME" , false);		
					
			//get line machine
			Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
					
			SMessageUtil.setBodyItemValue(doc, "MACHINENAME", machineName, true);
			SMessageUtil.setBodyItemValue(doc, "UNITNAME", unitName, true);
					
			String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
					
			 if(StringUtils.isEmpty(targetSubjectName))
			    	throw new CustomException("LOT-9006", machineName);
			 
			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		} catch (Exception e) {
			eventLog.error(e);
			
			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");

			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
		
	}
}
