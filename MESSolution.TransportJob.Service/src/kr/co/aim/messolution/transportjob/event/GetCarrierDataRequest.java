package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetCarrierDataRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		try {
			// Deleted by smkang on 2018.05.01 - If any event is not written, it would not be necessary to be made EventInfo. 
//			EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetCarrierDataRequest", getEventUser(), getEventComment(), "", "");
//			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
			// Modified by smkang on 2018.04.13 - Request message should be sent using sendBySender method.
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