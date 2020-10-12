package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class GetAlarmList extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME" , true);
		//String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE" , true);
		
		//from CNX to PEX
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEXSender");
	}
}
