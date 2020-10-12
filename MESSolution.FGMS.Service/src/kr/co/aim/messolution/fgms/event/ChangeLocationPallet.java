package kr.co.aim.messolution.fgms.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.area.AreaServiceProxy;
import kr.co.aim.greentrack.area.management.data.Area;
import kr.co.aim.greentrack.area.management.data.AreaKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeLocationPallet extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {

		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String location   = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);

		//Timestamp lastEventTime = new Timestamp(System.currentTimeMillis());

		ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(palletName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLocation", getEventUser(), getEventComment(), "", "");
		
		Map<String, String> udfs = processGroupData.getUdfs();//new HashMap<String, String>();
		udfs.put("LOCATION", location);
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroupData, setEventInfo, eventInfo);
		
		//Update Location Info(Area)
		String areaName    = location;
				
		AreaKey areaKey = new AreaKey();
		areaKey.setAreaName(areaName);
				
		Area areaData = null;
		areaData = AreaServiceProxy.getAreaService().selectByKey(areaKey);
				
		Map<String, String> areaUdfs = new HashMap<String,String>();
		areaUdfs.put("LocationState", GenericServiceProxy.getConstantMap().FGMS_LOCATION_FULL);
		areaUdfs.put("PalletName", palletName);
		areaData.setUdfs(areaUdfs);
				
		AreaServiceProxy.getAreaService().update(areaData);
		
		return doc;
	}
}
