package kr.co.aim.messolution.query.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.query.MESQueryServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.jdom.Document;
import org.jdom.Element;

public class Query extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		eventLog.info(String.format("[%s] started", getClass().getSimpleName()));
		
		String messageName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "MESSAGENAME");
		String sourceSubjectName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "ORIGINALSOURCESUBJECTNAME");
		String transactionId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/", doc, "TRANSACTIONID");
		
		String queryId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "QUERYID");
		String version = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "VERSION");
		
		Element bindElement = null;
		try {
			bindElement = JdomUtils.getNode(doc, "//Message/Body/BINDV");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Element conditionElement = null;
		try {
			conditionElement = JdomUtils.getNode(doc, "//Message/Body/BINDP");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String result = "";
		try {
			//result = MESQueryServiceProxy.getQueryServiceImpl().getQueryResult(messageName, sourceSubjectName, sourceSubjectName, transactionId, queryId, version, bindElement);
			result = MESQueryServiceProxy.getQueryServiceImpl().getQueryResult(messageName, sourceSubjectName, sourceSubjectName, transactionId, queryId, version, conditionElement, bindElement);
		} catch (Exception e) {
			
			if (e instanceof CustomException)
				throw (CustomException) e;
			else
				throw new CustomException("SYS-0000", e.getCause().getMessage());
		}
		
		eventLog.info(String.format("[%s] finished", getClass().getSimpleName()));
		
		return result;
	}
}
