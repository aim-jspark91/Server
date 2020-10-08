package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CSTInUnit extends AsyncHandler{

	private static Log log = LogFactory.getLog(CSTInUnit.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitIn", this.getEventUser(), this.getEventComment(), null, null);
			
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//		Map<String, String> udfs = new HashMap<String, String>();
//		udfs.put("MACHINENAME", machineName);
//		udfs.put("UNITNAME", unitName);
//		// Carrier UnitIn CSTCleaner  Set Carrier POSITIONTYPE and PORTNAME is null
//		udfs.put("POSITIONTYPE", "");
//		udfs.put("PORTNAME", "");
//		SetEventInfo setEventInfo = new SetEventInfo();
//		setEventInfo.setUdfs(udfs);
		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("POSITIONTYPE", "");
		setEventInfo.getUdfs().put("PORTNAME", "");

		durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}

}
