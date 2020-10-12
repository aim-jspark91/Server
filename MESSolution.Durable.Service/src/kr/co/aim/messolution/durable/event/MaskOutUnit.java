package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class MaskOutUnit extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitOut", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String vcrMaskName = SMessageUtil.getBodyItemValue(doc, "VCRMASKNAME", false);
		String maskJudge = SMessageUtil.getBodyItemValue(doc, "MASKJUDGE", false);
		String fromSlotID = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotID = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		//mask
		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		/* hhlee, 20181020, modify, Change subunitname update column ==>> */
        //udfs.put("POSITIONNAME", subUnitName);
        udfs.put("SUBUNITNAME", subUnitName);
        /* <<== hhlee, 20181020, modify, Change subunitname update column */
		udfs.put("MASKPOSITION", toSlotID);
		udfs.put("TRANSPORTSTATE", "MOVING");
		udfs.put("JUDGE", maskJudge);
		udfs.put("VCRMASKNAME", vcrMaskName);
		maskData.setUdfs(udfs);
		
		//validate
		//this.validateByCleaner(maskData, udfs, unitName);
		
		//set event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
	}
}