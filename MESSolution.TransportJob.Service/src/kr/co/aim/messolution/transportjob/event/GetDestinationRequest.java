package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetDestinationRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
			sendToDSP(doc);
			
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
		private void sendToDSP(Document doc)
		{
			// send to DSPsvr
			try
			{
				String replySubject = GenericServiceProxy.getESBServive().getSendSubject("DSPsvr");
				GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "DSPSender");
			}
			catch (Exception e)
			{
				eventLog.error("sending to DSPsvr is failed");
			}
		}
		
}