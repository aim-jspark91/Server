package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class MaskInLine extends AsyncHandler{
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Load", this.getEventUser(), "LoadMask", null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String fromSlotID = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false); 
		String toSlotID = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
        
		//Prepare
		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		/* hhlee, 20181020, modify, Change subunitname update column ==>> */
        //udfs.put("POSITIONNAME", subUnitName);
        udfs.put("SUBUNITNAME", subUnitName);
        /* <<== hhlee, 20181020, modify, Change subunitname update column */
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE);
		udfs.put("MASKPOSITION", toSlotID);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		//Execute 
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
	}

}
