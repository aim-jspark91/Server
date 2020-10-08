package kr.co.aim.messolution.generic.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;
import org.jdom.Element;

public class RequestUnixProcessState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		Document returnMsg = (Document)doc.clone();
		
		SMessageUtil.setHeaderItemValue(returnMsg, "MESSAGENAME", "UnixProcessReport");
		SMessageUtil.setHeaderItemValue(returnMsg, "KIND", "UnixProcessReport");

		//Element bodyElement = SMessageUtil.getBodyElement(returnMsg);

		//1. Remove Body Element
		boolean result = returnMsg.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		//2. Create Body Element
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);     
		
		Element unixprocess_reportElement = new Element("UNIXPROCESS_REPORT");
		unixprocess_reportElement.setAttribute("ID", System.getProperty("Seq"));

		Element report_keyElement = new Element("REPORT_KEY");
		report_keyElement.setText(SMessageUtil.getHeaderItemValue(returnMsg, "VALUE", false));

		unixprocess_reportElement.addContent(report_keyElement);

		eleBodyTemp.addContent(unixprocess_reportElement);
		
		//3. Overwrite
		returnMsg.getRootElement().addContent(eleBodyTemp);
		
		return returnMsg;
		
		//Source Backup
//		Document returnMsg = (Document)xml.clone();
//
//		Element headerElement = JdomUtils.getNode(returnMsg, "//Message/Header");
//
//		headerElement.getChild("KIND").setText("UnixProcessReport");
//		headerElement.getChild("MESSAGENAME").setText("UnixProcessReport");
//
//		Element bodyElement = JdomUtils.getNode(returnMsg, "//Message/Body");
//
//		if(bodyElement == null){
//			Element messageElement = JdomUtils.getNode(returnMsg, "//Message");
//			bodyElement = new Element("Body");
//
//			messageElement.addContent(bodyElement);
//		}else{
//			bodyElement.removeContent();
//		}
//
//		String selfServerName = System.getProperty("Seq");
//
//		Element unixprocess_reportElement = new Element("UNIXPROCESS_REPORT");
//		unixprocess_reportElement.setAttribute("ID", selfServerName);
//
//		Element report_keyElement = new Element("REPORT_KEY");
//		report_keyElement.setText(value);
//
//		unixprocess_reportElement.addContent(report_keyElement);
//
//		bodyElement.addContent(unixprocess_reportElement);
	}

}
