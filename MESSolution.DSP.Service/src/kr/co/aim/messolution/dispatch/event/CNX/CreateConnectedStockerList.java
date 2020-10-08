package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspConnectedStocker;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CreateConnectedStockerList extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		try
		{	
			List<Element> eledataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", true);
						
			for(Element eleData : eledataList)
			{
				String machineName = SMessageUtil.getChildText(eleData, "MACHINENAME", true);
				String portName = SMessageUtil.getChildText(eleData, "PORTNAME", true);
				String direction = SMessageUtil.getChildText(eleData, "DIRECTION", true);
				String stockerName = SMessageUtil.getChildText(eleData, "STOCKERNAME", true);
				String zoneName = SMessageUtil.getChildText(eleData, "ZONENAME", true);
				String postition = ""; 
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");

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
					//throw new CustomException("RECIPE-0009", "");

					List<Map<String, Object>>  connectedStockerPositionInfo = MESDSPServiceProxy.getDSPServiceUtil().getConnectedStockerPositionInfo(machineName,portName,direction);
					
					if(connectedStockerPositionInfo.size() > 0 )
					{
						postition = connectedStockerPositionInfo.get(0).get("POSITION").toString();
					}
										
					connectedStockerData = new DspConnectedStocker(machineName, portName, direction, stockerName, zoneName);
					connectedStockerData.setPosition(Long.parseLong(postition));
					connectedStockerData.setLastEventUser(eventInfo.getEventUser());
					connectedStockerData.setLastEventComment(eventInfo.getEventComment());
					connectedStockerData.setLastEventTime(eventInfo.getEventTime());
					connectedStockerData.setLastEventTimekey(eventInfo.getEventTimeKey());
					connectedStockerData.setLastEventName(eventInfo.getEventName());
					
					ExtendedObjectProxy.getDspConnectedStockerService().create(eventInfo, connectedStockerData);
				}
//				else
//				{
//					throw new CustomException("RECIPE-0009", "");
//				}
			}	
		}
		catch(Exception ex)
		{
			throw new CustomException("InsertFail!");
		}
		
		return doc;
	}
}
