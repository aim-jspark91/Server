package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.04.14
 * @see After TEX server starts up, TEX server requests state of MCS.
 */
public class GetMCSStateRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.05.04 - Suppose that GetMCSStateRequest will be requested by OPI.
//		try {
//			// Commented by smkang on 2018.04.14 - When this class is necessary to be invoked?
//			Element bodyElement = new Element(SMessageUtil.Body_Tag);
//			
//			Element mcsNameElement = new Element("MACHINENAME");
//			mcsNameElement.setText("MCS");
//			bodyElement.addContent(mcsNameElement);
//			
//			String originalSourceSubjectName = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEXsvr");
//			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("MCS");
//			
//			doc = SMessageUtil.createXmlDocument(bodyElement, "GetMCSStateRequest", originalSourceSubjectName, targetSubject, "TEXsvr", "GetMCSStateRequest");
//			
//			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "HIFSender");
//		} catch (Exception e) {
//			eventLog.error(e);
//		}
//		
//		return doc;
		try {
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