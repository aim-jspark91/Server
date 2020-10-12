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
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;

public class MaskOutIndexer extends AsyncHandler {
	
	@Override
    public void doWorks(Document doc) throws CustomException {
		
		EventInfo maskEventInfo = EventInfoUtil.makeEventInfo("Deassign", this.getEventUser(), this.getEventComment(), null, null);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String maskCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		
		Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskCarrierName);
		Durable maskCSTData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskCarrierName));
		
		//Mask
		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
						
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);
		/* hhlee, 20181020, modify, Change subunitname update column ==>> */
        //udfs.put("POSITIONNAME", subUnitName);
        udfs.put("SUBUNITNAME", subUnitName);
        /* <<== hhlee, 20181020, modify, Change subunitname update column */
		udfs.put("POSITIONTYPE", "");
		udfs.put("PORTNAME", "");
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
				
		maskCarrierName = "";
	    String maskPosition = "";
			    
	    //deassign mask
		SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setDeassignEVAMaskCSTInfo(udfs, maskCarrierName, maskPosition);
		setEventInfo.setUdfs(udfs);
				
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, maskEventInfo);
		
		if (portData.getUdfs().get("PORTTYPE").equals("PL"))
		{				
			if (maskCSTData.getLotQuantity() > 0)
				maskCSTData.setLotQuantity(maskCSTData.getLotQuantity() - 1);
			
			if(maskCSTData.getLotQuantity() < 1)
			{
				maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				maskCSTData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
				maskCSTData.getUdfs().put("MACHINENAME", "");
				maskCSTData.getUdfs().put("UNITNAME", "");
				maskCSTData.getUdfs().put("POSITIONTYPE", "");
				maskCSTData.getUdfs().put("PORTNAME", "");
				maskCSTData.getUdfs().put("POSITIONNAME", "");
			}
			
			if(StringUtil.equals(machineName, "O1MSK200"))
			{
				maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
				setEventInfo = new SetEventInfo();
				
				maskData.getUdfs().put("TRANSPORTSTATE", "INLINE");
				setEventInfo.setUdfs(maskData.getUdfs());
				
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
				
				ReserveMaskList rsvMAsk = ReserveMaskListService.getReserveMaskInfoByPL(maskName);
				
				if(rsvMAsk != null)
					ExtendedObjectProxy.getReserveMaskService().remove(eventInfo, rsvMAsk);
			}			
		}
		else if (portData.getUdfs().get("PORTTYPE").equals("PB"))
		{	
			if (maskCSTData.getLotQuantity() > 0)
			{
				maskCSTData.setLotQuantity(maskCSTData.getLotQuantity() - 1);
			}
			if(maskCSTData.getLotQuantity() < 1)
			{
				maskCSTData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			}
			
			//Clear ReserveData MaskCST
			if(StringUtil.equals(machineName, "O1EVA100"))
			{
				ReserveMaskList rsvMAsk = ReserveMaskListService.getReserveMaskInfoByPL(maskName);				
				if(rsvMAsk != null)
				{
					rsvMAsk.setCarrierName("");					
					EventInfo evenMaskInfo = EventInfoUtil.makeEventInfo("ClearMaskCST", this.getEventUser(), this.getEventComment(), null, null);
					ExtendedObjectProxy.getReserveMaskService().modify(evenMaskInfo, rsvMAsk);
				}
												
			}
		}
		DurableServiceProxy.getDurableService().update(maskCSTData);
		
		SetEventInfo setCSTEvent = new SetEventInfo();
		setCSTEvent.setUdfs(maskCSTData.getUdfs()); 
		
		eventInfo.setEventName("Deassign");
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskCSTData, setCSTEvent, eventInfo);
				
	}
}
