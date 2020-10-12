package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeReserveState extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeReserveState", getEventUser(), getEventComment(), "", "");
		Element eLotList = SMessageUtil.getBodySequenceItem(doc, "LOTLIST", true);

		for (@SuppressWarnings("rawtypes")
		Iterator iLot = eLotList.getChildren().iterator(); iLot.hasNext();)
		{
			Element eLot = (Element) iLot.next();
			String lotName = SMessageUtil.getChildText(eLot, "LOTNAME", true);
			String reserveState = SMessageUtil.getChildText(eLot, "RESERVESTATE", true);
			String machineName = SMessageUtil.getChildText(eLot, "MACHINENAME", true);
			String factoryName = SMessageUtil.getChildText(eLot, "FACTORYNAME", true);

			ReserveLot reserveLot = new ReserveLot();
			String condition = "WHERE machineName = ? AND lotName = ? ";
			Object[] bindSet = new Object[] {machineName,lotName};

			List<ReserveLot> lotList = new ArrayList<ReserveLot>();

			lotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			if(lotList.size()>0)
			{
				for(ReserveLot rl : lotList)
				{
					if(!StringUtils.equals(rl.getReserveState(), GenericServiceProxy.getConstantMap().RESV_STATE_START)
							||!StringUtils.equals(reserveState, GenericServiceProxy.getConstantMap().RESV_LOT_STATE_RESV))
					{
						throw new CustomException("COMMON-0001", "'Started' State can change to 'Reserved' state only! current ReserveState : " + 
								rl.getReserveState() + " & Change to State : " + reserveState);
					}	
					
					rl.setReserveState(reserveState);
					ExtendedObjectProxy.getReserveLotService().modify(eventInfo, rl);
				}
			}
			else
			{
				throw new CustomException("COMMON-0001","Not exist ReserveLot Information. LotName : " + lotName);
			}
		}
			
		return doc;
	}
}
