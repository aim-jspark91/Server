package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskListService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class MaskInSubUnit extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SubUnitIn", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));
		
		if(maskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Available))
		{
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			DurableServiceProxy.getDurableService().update(maskData);
		}
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		/* hhlee, 20181020, modify, Change subunitname update column ==>> */
        //udfs.put("POSITIONNAME", subUnitName);
        udfs.put("SUBUNITNAME", subUnitName);
        /* <<== hhlee, 20181020, modify, Change subunitname update column */
		udfs.put("MASKPOSITION", toSlotId);
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_PROCESSING);
		udfs.put("PORTNAME", "");
		udfs.put("POSITIONTYPE", "");
		udfs.put("MASKCARRIERNAME", "");
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		//Execute
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		
		if(machineName.equals("O1EVA100"))
		{
			ReserveMaskList reserveMaskData = ReserveMaskListService.getReserveMaskInfoByPL(maskName);
			if (reserveMaskData != null && !reserveMaskData.getCarrierName().isEmpty())
			{									
				reserveMaskData.setCarrierName("");				
				eventInfo = EventInfoUtil.makeEventInfo("ClearMaskCST", this.getEventUser(), this.getEventComment(), null, null);
				ExtendedObjectProxy.getReserveMaskService().modify(eventInfo, reserveMaskData);
			}
		}
	}
}