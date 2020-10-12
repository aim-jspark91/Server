package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspConnectedStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ModifyConnectedStocker extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String direction = SMessageUtil.getBodyItemValue(doc, "DIRECTION", true);
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "ZONENAME", true);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		String newStockerName = SMessageUtil.getBodyItemValue(doc, "NEWSTOCKERNAME", true);
		String newZoneName = SMessageUtil.getBodyItemValue(doc, "NEWZONENAME", true);
		String newPosition = SMessageUtil.getBodyItemValue(doc, "NEWPOSITION", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "");
			
		DspConnectedStocker connectedStockerData = null;
		
//		try
//		{
//			connectedStockerData = ExtendedObjectProxy.getDspConnectedStockerService().selectByKey(false, new Object[] {machineName, portName, direction, stockerName, zoneName});
//		}
//		catch (Exception ex)
//		{
//			connectedStockerData = null;
//		}
//		
//		if(connectedStockerData == null)
//		{
//			throw new CustomException("IDLE-0006", "");
//		}
//		
		
		connectedStockerData = new DspConnectedStocker(machineName, portName, direction, stockerName, zoneName);
		connectedStockerData.setStockerName(newStockerName);
		connectedStockerData.setZoneName(newZoneName);
		connectedStockerData.setPosition(Long.parseLong(newPosition));
		connectedStockerData.setLastEventUser(eventInfo.getEventUser());
		connectedStockerData.setLastEventComment(eventInfo.getEventComment());
		connectedStockerData.setLastEventTime(eventInfo.getEventTime());
		connectedStockerData.setLastEventTimekey(eventInfo.getEventTimeKey());
		connectedStockerData.setLastEventName(eventInfo.getEventName());
		
		ExtendedObjectProxy.getDspConnectedStockerService().modify(eventInfo, connectedStockerData);
		
		return doc;
	}
}
