package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhtMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class PhotoMaskTakeOut extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PhotoMaskTakeOut", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String fromSlotID = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", true);
		String receiveTime = SMessageUtil.getBodyItemValue(doc, "RECEIVETIME", false);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));
		
		if(!maskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
		{
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			DurableServiceProxy.getDurableService().update(maskData);
		}
		
		Map<String, String> udfs = new HashMap<String, String>();
		//udfs.put("MACHINENAME", machineName);
		//udfs.put("UNITNAME", unitName);
		//udfs.put("POSITIONNAME", subUnitName);
		//udfs.put("MASKPOSITION", toSlotID);
		
		udfs.put("MACHINENAME", StringUtil.EMPTY);
        udfs.put("UNITNAME",  StringUtil.EMPTY);
        udfs.put("POSITIONNAME",  StringUtil.EMPTY);
        udfs.put("MASKPOSITION",  StringUtil.EMPTY);
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
		udfs.put("RECEIVETIME", TimeStampUtil.getTimestamp(receiveTime).toString());
		
		maskData.setUdfs(udfs);
		
		//validate
		//this.validateByCleaner(maskData, udfs, unitName);
		
		//set event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		
		/************************************* PhotoMaskStocker Management ************************************/
		
		PhtMaskStocker phtMaskStockerData = null;
		
		try{
		    phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().selectByKey(false, new Object[] {machineName,unitName,fromSlotID});
		}
		catch(Throwable e){
			eventLog.info("PhtMaskStocker Data is not exist");
		}
		
		try{
			if(phtMaskStockerData != null) {
	            phtMaskStockerData.setCurrentMaskName(StringUtil.EMPTY);
	            phtMaskStockerData.setCurrentInTime(null);
	            
	            phtMaskStockerData.setLastOutMaskName(maskData.getKey().getDurableName());
	            phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());
	            
	            // 2019.05.14_hsryu_Set LastEventInfo. Missing Logic.
				phtMaskStockerData.setLastEventName(eventInfo.getEventName());
				phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
				phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
				phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
	            
	            phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().modify(eventInfo, phtMaskStockerData);
			}
			else {
	            phtMaskStockerData = new PhtMaskStocker();
	            
	            phtMaskStockerData.setMachineName(machineName);
	            phtMaskStockerData.setUnitName(unitName);
	            phtMaskStockerData.setLocation(Long.parseLong(fromSlotID));
	            
	            phtMaskStockerData.setLastOutMaskName(maskName);
	            phtMaskStockerData.setLastOutTime(eventInfo.getEventTime());  
	            
	            // 2019.05.14_hsryu_Set LastEventInfo. Missing Logic.
				phtMaskStockerData.setLastEventName(eventInfo.getEventName());
				phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
				phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
				phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());
	            
	            phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().create(eventInfo, phtMaskStockerData);
			}
		}
		catch(Throwable e){
			eventLog.error("Fail phtMaskStocker Data Management.");
		}
	}
}