package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RegisterSuperLot extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String toSuperLotFlag=SMessageUtil.getBodyItemValue(doc, "TOSUPERLOTFLAG", true);
		String note=SMessageUtil.getBodyItemValue(doc, "NOTE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RegisterSuperLot", getEventUser(), getEventComment(), "", "");
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		if (StringUtil.equals(lotData.getLotProcessState(), "RUN"))
			throw new CustomException("LOT-0008", lotName);
		
		Map<String,String> udfs = lotData.getUdfs();
		String superLotFlag = CommonUtil.getValue(udfs, "SUPERLOTFLAG");
		
		if (!StringUtils.equals(superLotFlag, toSuperLotFlag)) {
			if(StringUtils.equals(toSuperLotFlag, GenericServiceProxy.getConstantMap().Flag_Y))
				eventInfo.setEventName("RegisterSuperLot");
			else
				eventInfo.setEventName("CancelSuperLot");
			
			// Modified by smkang on 2019.05.24 - If UDF is needed to be updated only, update method is unnecessary to be invoked.
//			udfs.put("SUPERLOTFLAG", toSuperLotFlag);
//			udfs.put("NOTE", note);
//			lotData.setUdfs(udfs);
//			LotServiceProxy.getLotService().update(lotData);
//			
//			//Set Event
//			SetEventInfo setEventInfo = new SetEventInfo();
//			
//			//execute						
//			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("SUPERLOTFLAG", toSuperLotFlag);
			setEventInfo.getUdfs().put("NOTE", note);
			
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//			//Note clear - YJYU
//			Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
//			Map<String, String> udfs_note = lotData_Note.getUdfs();
//			udfs_note.put("NOTE", "");
//			LotServiceProxy.getLotService().update(lotData_Note);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		} else {
			throw new CustomException("COMMON-0001", "current SuperLotFlag & SuperLotFlag to be chaned is same!");
		}
		
		return doc;
	}
}