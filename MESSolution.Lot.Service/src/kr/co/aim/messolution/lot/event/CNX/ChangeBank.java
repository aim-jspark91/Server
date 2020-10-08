package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeBank extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String ToEndBank=SMessageUtil.getBodyItemValue(doc, "TOENDBANK", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeBank", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : LotList)
		{
			String slotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			Lot lotData =  LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(slotName));
			// Modified by smkang on 2019.05.24 - If UDF is needed to be updated only, update method is unnecessary to be invoked.
//			Lot lotData=MESLotServiceProxy.getLotInfoUtil().getLotData(slotName);
//			
//			Map<String, String> lotUdfs = lotData.getUdfs();	
//			lotUdfs.put("ENDBANK", ToEndBank); 
//
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(lotUdfs);
//
//			LotServiceProxy.getLotService().update(lotData);
//			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("ENDBANK", ToEndBank);
			
			setEventInfo.getUdfs().put("NOTE", "FromEndBank : "+lotData.getUdfs().get("ENDBANK") + " ToEndBank : "+ToEndBank);
			
			// MODIFY  BY JHY ON 2019.07.01 TEST
			//setEventInfo.getUdfs().put("SUPERLOTFLAG", "TTTTT");
			
			
			//lotData.setSourceLotName("JHY");
			
			LotServiceProxy.getLotService().setEvent(new LotKey(slotName), eventInfo, setEventInfo);
			
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		}
		
		return doc;
	}
}