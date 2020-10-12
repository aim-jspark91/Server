package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteReserveLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String targetmachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> eleLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		for(Element eleLot : eleLotList)
		{
			String machineName = SMessageUtil.getChildText(eleLot, "MACHINENAME", true);
			String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleLot, "PRODUCTSPECNAME", true);
			String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
			
			Object[] keySet = new Object[]{machineName, lotName};
			ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().selectByKey(false, keySet);
			
			ExtendedObjectProxy.getReserveLotService().remove(eventInfo, reserveLot);
		}
		//  Add By Huhaifeng  20170201		
		List<ReserveLot> reservedLotList = null ;
		
		try
		{		
			String condition = "WHERE machineName = ? AND reserveState = ? AND processOperationName IS NOT NULL ORDER BY POSITION  ";
			Object[] bindSet = new Object[]{targetmachineName, "Reserved"};
			reservedLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);	
			
			if(reservedLotList != null)
			{
				long position=1 ;
				for (ReserveLot reservedLotData : reservedLotList )
				{
				reservedLotData.setPosition(position);
				ExtendedObjectProxy.getReserveLotService().update(reservedLotData);
				position++;
				}
			}
		}
		catch(Exception ex)
		{
			
		}
//		
		return doc;
	}

}
