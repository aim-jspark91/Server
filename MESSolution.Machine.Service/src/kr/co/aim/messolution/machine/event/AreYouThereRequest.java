package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class AreYouThereRequest  extends SyncHandler{
	private static Log log = LogFactory.getLog(AreYouThereRequest.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "A_AreYouThereReply");
			
		String smachineName= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		//check Machine Data			
		Machine MachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(smachineName);
		
		return doc;
	}
}
