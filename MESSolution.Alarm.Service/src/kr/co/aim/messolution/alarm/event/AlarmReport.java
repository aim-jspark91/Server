package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class AlarmReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// Modified by smkang on 2018.12.13 - PEX will forward AlarmReport to ALMsvr.
		GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
	}
}