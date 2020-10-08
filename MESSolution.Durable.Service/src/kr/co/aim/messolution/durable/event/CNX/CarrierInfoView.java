package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CarrierInfoView extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String cstName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String note = SMessageUtil.getBodyItemValue(doc, "NOTE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Note", getEventUser(), "Note", null, null);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		
		Durable cstData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cstName);
		
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> udfs = new HashMap<String, String>();
//		udfs = cstData.getUdfs();
//		udfs.put("NOTE", note);
//		setEventInfo.setUdfs(udfs);
		
		setEventInfo.getUdfs().put("NOTE", note);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(cstData, setEventInfo, eventInfo);
		
		// Modified by smkang on 2019.05.28 - DurableServiceProxy.getDurableService().update(DATA dataInfo) sometimes occurs a problem which is updated with old data.
//		cstData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cstData.getKey().getDurableName());
//		
//		// For Clear Note, Add By Park Jeong Su
//		cstData.getUdfs().put("NOTE", "");
//		DurableServiceProxy.getDurableService().update(cstData);
		Map<String, String> updateUdfs = new HashMap<String, String>();
		updateUdfs.put("NOTE", "");
		MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(cstData, updateUdfs);
		
		return doc;	
	}
}
