package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

/**
 * @author smkang
 * @since 2018.08.11
 * @see Reset Machine Idle Time For MQC Condition.
 */
public class ResetMachineIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), null, null);
		
		try {
			MESMachineServiceProxy.getMachineServiceImpl().forceResetMachineIdleTime(machineName, unitName, eventInfo);
		} catch (Exception e) {
			eventLog.warn(e);
		}
		
		return doc;
	}
}