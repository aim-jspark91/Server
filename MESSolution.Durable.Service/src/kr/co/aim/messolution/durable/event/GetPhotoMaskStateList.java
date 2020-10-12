package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class GetPhotoMaskStateList extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		//from CNX to PEX
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEXSender");
	}
}
