package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaskInIndexer extends AsyncHandler{
	
	private static Log log = LogFactory.getLog(MaskInIndexer.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", true);
		
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName); 
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskCarrierName);
		Durable maskCSTData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskCarrierName));
		
		if (portData.getUdfs().get("PORTTYPE").equals("PU") || portData.getUdfs().get("PORTTYPE").equals("PB"))
		{
			if (maskCSTData.getLotQuantity() < maskCSTData.getCapacity())
			{
				maskCSTData.setLotQuantity(maskCSTData.getLotQuantity() + 1);
				
				maskCSTData.setDurableState("InUse");
				
				DurableServiceProxy.getDurableService().update(maskCSTData);
			}
		}
		//set cassette event
		SetEventInfo setCarrierEvent = new SetEventInfo();
		EventInfo eventCarrierInfo = EventInfoUtil.makeEventInfo("Assign", this.getEventUser(), eventInfo.getEventComment(), "", "");
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskCSTData, setCarrierEvent, eventCarrierInfo);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		Durable durMaskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));
		
		durMaskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		DurableServiceProxy.getDurableService().update(durMaskData);
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		udfs.put("POSITIONTYPE", "PORT");
		/* hhlee, 20181020, modify, Change subunitname update column ==>> */
        //udfs.put("POSITIONNAME", subUnitName);
        udfs.put("SUBUNITNAME", subUnitName);
        /* <<== hhlee, 20181020, modify, Change subunitname update column */
		udfs.put("PORTNAME", portName);
		udfs.put("TRANSPORTSTATE", "INPORT");
		udfs.put("MASKCARRIERNAME", maskCarrierName);
		udfs.put("MASKPOSITION", toSlotId);
		
		//set mask event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
	}
}