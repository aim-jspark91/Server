package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ResolveQueueTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> eleQTimeList = SMessageUtil.getBodySequenceItemList(doc, "QUEUETIMELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		for (Element eleQTime : eleQTimeList)
		{
			String lotName = SMessageUtil.getChildText(eleQTime, "LOTNAME", true);
			String queueTimeState = SMessageUtil.getChildText(eleQTime, "QUEUETIMESTATE", true);
			String fromOperationName = SMessageUtil.getChildText(eleQTime, "PROCESSOPERATIONNAME", true);
			String toOperationName = SMessageUtil.getChildText(eleQTime, "TOPROCESSOPERATIONNAME", true);
			String toProcessFlowName = SMessageUtil.getChildText(eleQTime, "TOPROCESSFLOWNAME", true);
			String queueType = SMessageUtil.getChildText(eleQTime, "QUEUETYPE", true);
			
			if(!queueTimeState.equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
			{
				throw new CustomException("LOT-0206", queueTimeState);
			}
			
			ExtendedObjectProxy.getQTimeService().resolveQTime(eventInfo, lotName, fromOperationName, toOperationName, toProcessFlowName, queueType);
		}
		
		return doc;
	}

}
