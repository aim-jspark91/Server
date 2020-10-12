
package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeDepartAndPriority extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String falg = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", true);
		
		// Modified by smkang on 2019.06.20 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
		
		if(StringUtils.equals(falg, "DEPARTMENT"))
		{
			String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENTNAME", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDepartment", getEventUser(), getEventComment(), "", "");
			
			if(StringUtils.isEmpty(department))
				throw new CustomException("LOT-0219",department);
			
			// Modified by smkang on 2019.05.24 - If UDF is needed to be updated only, update method is unnecessary to be invoked.
//			lotData.getUdfs().put("DEPARTMENTNAME", department);
//			lotData.getUdfs().put("NOTE", note);
//			
//			LotServiceProxy.getLotService().update(lotData);
//
//			//Set Event
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(lotData.getUdfs());
//
//			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("DEPARTMENTNAME", department);
			setEventInfo.getUdfs().put("NOTE", note);
			
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		else
		{
			String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePriority", getEventUser(), getEventComment(), "", "");
			
			if(StringUtils.isEmpty(priority))
				throw new CustomException("LOT-0219",priority);
			
			lotData.setPriority(Long.parseLong(priority));
			lotData.getUdfs().put("NOTE", note);
			
			LotServiceProxy.getLotService().update(lotData);

			//Set Event
			SetEventInfo setEventInfo = new SetEventInfo();		
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}

		//Note clear - YJYU
		// Modified by smkang on 2019.05.24 - LotServiceProxy.getLotService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		Lot lotData_Note = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
//		Map<String, String> udfs_note = lotData_Note.getUdfs();
//		udfs_note.put("NOTE", "");
//		LotServiceProxy.getLotService().update(lotData_Note);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESLotServiceProxy.getLotServiceImpl().updateLotWithoutHistory(lotData, updateUdfs);
		
		return doc;
	}
}