package kr.co.aim.greenframe.template.formatter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.event.AbstractBundleEventTemplate;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.tibco.tibrv.TibrvMsg;

public class MessageFormatter extends AbstractBundleEventTemplate {
   	private static Log log = LogFactory.getLog(MessageFormatter.class);
   	//test
	/**
	 * @uml.property  name="dataField_TAG"
	 */
	private String dataField_TAG = "xmlData";

	public MessageFormatter() {
	}

	public void onBundleMessage(String beanName, Object data) {
	   Document document = null;
	   
	   //set default for log4j
	   // Deleted by smkang on 2019.03.27 - MES.MSGNAME is set twice in this method, so transaction id of logging has side-effect. 
//	   try {
//		   MDC.put("MES.MSGNAME", this.getClass().getSimpleName());
//	   } catch (Exception e2) {
//		   log.error(e2.getMessage());
//	   }
	   
	   if (data instanceof TibrvMsg) {
		  document = SMessageUtil.getDocumentFromTibrvMsg((TibrvMsg)data, dataField_TAG);
	   }
	   
	   //set log header
	   try {
		   MDC.put("MES.MSGNAME", JdomUtils.getNodeText(document, "//Message/Header/MESSAGENAME"));
	   } catch (Exception e) {
		   log.error(e.getMessage());
	   }
	   
	   try {
		   MDC.put("MES.TRXID", JdomUtils.getNodeText(document, "//Message/Header/TRANSACTIONID"));
	   } catch (Exception e) {
		   log.error(e.getMessage());
	   }
	   
	   log.info("["+ beanName + "] execute");
	   
	   GenericServiceProxy.getMessageTraceService().recordMessageLog(document, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_RECEIVE);
	   
	   try {
		   // Modified by smkang on 2019.03.27 - StringBuilder can't guarantee thread-safe.
//		   StringBuilder strBuilder = new StringBuilder("RCRQ : ");		   
//		   strBuilder.append(MDC.get("MES.MSGNAME")).append(" ").append(MDC.get("MES.TRXID")).append(" ");
//		
//		   log.info(strBuilder.toString());
		   StringBuffer strBuffer = new StringBuffer("RCRQ : ");		   
		   strBuffer.append(MDC.get("MES.MSGNAME")).append(" ").append(MDC.get("MES.TRXID")).append(" ");
		
		   log.info(strBuffer.toString());
	   } catch (Exception e) {
		   log.error(e.getMessage());
	   }
	   	   
	   GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("RCRQ : ").append(JdomUtils.toString(document)).toString());
	   
	   // One Thread
	   //WorkflowServiceProxy.getBpelExecuter().executeProcess(arguments, false, null);
	   
	   // Multi Thread
	   execute(new Object[]{document});
    }
	
	public String toCompactString(Document document) {
		XMLOutputter out = new XMLOutputter();
		Format format = Format.getCompactFormat();
		out.setFormat(format);

		return out.outputString(document.getRootElement());
	}
}