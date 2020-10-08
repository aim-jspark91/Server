package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.04.14
 * @see When TEX server receives GetUnitStateRequest, TEX server forwards the message to MCS.
 */
public class GetUnitStateRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetUnitStateRequest", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			
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