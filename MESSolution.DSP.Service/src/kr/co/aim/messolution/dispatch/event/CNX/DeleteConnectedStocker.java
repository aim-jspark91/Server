package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspConnectedStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteConnectedStocker extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		Element connList = SMessageUtil.getBodySequenceItem(doc, "CONNLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "");
		
		if(connList != null)
		{
			for(Object obj : connList.getChildren())
			{
				Element element = (Element)obj;
				String machineName = SMessageUtil.getChildText(element, "MACHINENAME", true);
				String portName = SMessageUtil.getChildText(element, "PORTNAME", true);
				String direction = SMessageUtil.getChildText(element, "DIRECTION", true);
				String stockerName = SMessageUtil.getChildText(element, "STOCKERNAME", true);
				String zoneName = SMessageUtil.getChildText(element, "ZONENAME", true);
				String position = SMessageUtil.getChildText(element, "POSITION", true);
				
				DspConnectedStocker connectedStockerData = null;
				
				try
				{
					connectedStockerData = ExtendedObjectProxy.getDspConnectedStockerService().selectByKey(false, new Object[] {machineName, portName, direction, stockerName, zoneName});
				}
				catch (Exception ex)
				{
					connectedStockerData = null;
				}
				
				if(connectedStockerData == null)
				{
					throw new CustomException("IDLE-0006", "");
				}
				
				ExtendedObjectProxy.getDspConnectedStockerService().remove(eventInfo, connectedStockerData);
			}
		}
		
		return doc;
	}
}
