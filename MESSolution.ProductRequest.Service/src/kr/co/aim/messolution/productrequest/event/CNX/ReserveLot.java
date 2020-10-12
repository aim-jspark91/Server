package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.collections.map.HashedMap;
import org.jdom.Document;
import org.jdom.Element;


public class ReserveLot extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {	 
		
		List<Element> eLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		//String ProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveLot", getEventUser(), getEventComment(), null, null);
		
		double sumOfProductQTY = 0 ;
		
		//TODO check QTY  sum <= productPlan-releasedQTY - assignQTY
		
		for (Element element : eLotList) {
			
			String sLotName = SMessageUtil.getChildText(element, "LOTNAME", true);
			String assignProductRequestName = SMessageUtil.getChildText(element, "ASSIGNEDWONAME", false);;
			
			LotKey lotKey = new LotKey(sLotName);
			//Lot LotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			SetEventInfo setEventInfo = new SetEventInfo();
			
			Lot LotData = LotServiceProxy.getLotService().setEvent(lotKey, eventInfo, setEventInfo);
			sumOfProductQTY  +=	LotData.getProductQuantity();
			
			
			Lot newLotData = (Lot) ObjectUtil.copyTo(LotData);
			
			Map<String, String> udfs = new HashedMap(2);
			udfs.put("ASSIGNEDWONAME", assignProductRequestName);
			newLotData.setUdfs(udfs);
			LotServiceProxy.getLotService().update(newLotData);
			String timeKey = newLotData.getLastEventTimeKey();
/*			LotHistory lotHistoryInfo = new LotHistory();
			LotServiceProxy.getLotHistoryDataAdaptor().setHV(LotData, newLotData, lotHistoryInfo);
			lotHistoryInfo.getUdfs().put("ASSIGNEDWONAME", assignProductRequestName);
			lotHistoryInfo.getKey().setTimeKey(TimeUtils.getCurrentEventTimeKey());
			LotServiceProxy.getLotHistoryService().insert(lotHistoryInfo );*/
			LotHistoryKey lotHistoryKey = new LotHistoryKey();
			lotHistoryKey.setLotName(sLotName);
			lotHistoryKey.setTimeKey(timeKey);
			LotHistory lotHistoryInfo = LotServiceProxy.getLotHistoryService().selectByKey(lotHistoryKey );
			lotHistoryInfo.getUdfs().put("ASSIGNEDWONAME", assignProductRequestName);
			LotServiceProxy.getLotHistoryService().update(lotHistoryInfo);
		}
		
		
		return doc;
	}

}
