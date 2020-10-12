package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetMachineStateRequest extends AsyncHandler 
{ 
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		try {
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			
			// Modified by smkang on 2018.05.02 - If this machine is not existed in DB, CustomException would be already thrown in getMachineData method.
//			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
//			
//			if(machineData == null)
//			{
//				throw new CustomException("MACHINE-9001", machineName);
//			}
			MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
			// Modified by smkang on 2018.05.02 - Request message should be sent using sendBySender method.
//			String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("MCS");
//			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");
			// Modified by smkang on 2018.06.12 - sendSubjectName of infra configuration will be used instead of ESBservice.
//			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("MCS");
//			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "HIFSender");
			GenericServiceProxy.getESBServive().sendBySender(doc, "HIFSender");
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