package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

import org.jdom.Document;

public class MachineAlarmStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		GenericServiceProxy.getESBServive().sendBySender(doc, "ALMSender");
	}
}