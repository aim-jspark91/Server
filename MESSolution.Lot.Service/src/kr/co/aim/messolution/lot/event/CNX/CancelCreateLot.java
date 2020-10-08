package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreate", getEventUser(), getEventComment(), null, null);
		
		for (Element eleLot : lotList)
		{
			try
			{
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				//to gain initiation
				lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotData.getKey());
							
				if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotState());
				
				try
				{
					List<ReserveLot> result = ExtendedObjectProxy.getReserveLotService().select("lotName = ? ", new Object[] {lotName});
					
					throw new CustomException("LOT-0018", lotData.getKey().getLotName());
				}
				catch (Exception ex)
				{
					//bingo
				}
				
				lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, new SetEventInfo());
				
//				kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update("DELETE LOT WHERE lotName = ?", new Object[] {lotData.getKey().getLotName()});
				GenericServiceProxy.getSqlMesTemplate().update("DELETE LOT WHERE lotName = ?", new Object[] {lotData.getKey().getLotName()});
			}
			catch (Exception ex)
			{
				eventLog.warn(ex);
			}
		}
		
		return doc;
	}

}
