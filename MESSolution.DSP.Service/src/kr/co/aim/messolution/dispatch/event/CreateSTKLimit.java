package kr.co.aim.messolution.dispatch.event;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.dispatch.management.data.STKLimit;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateSTKLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
 
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		
		STKLimit stkLimitData = new STKLimit();
		stkLimitData.setMachineName(machineName);
		stkLimitData.setPortName(portName);
		stkLimitData.setStockerName(stockerName);
		
		MESDSPServiceProxy.getSTKLimitService().create(eventInfo, stkLimitData);
		
		return doc;
	}

}
