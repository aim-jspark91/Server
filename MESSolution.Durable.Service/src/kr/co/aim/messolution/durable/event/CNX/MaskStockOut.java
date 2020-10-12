package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhtMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class MaskStockOut extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TakeOut", this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null) {
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,"MASKLIST", true)) {
				
				String sMaskName = SMessageUtil.getChildText(eledur,"MASKNAME", true);
				String sMachineName = SMessageUtil.getChildText(eledur,"MACHINENAME", true);
				String sUnitName = SMessageUtil.getChildText(eledur,"UNITNAME", true);
				String sStockerSlot = SMessageUtil.getChildText(eledur,"MASKPOSITION", false);

				Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
				
				//maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				//DurableServiceProxy.getDurableService().update(maskData);
				
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("MACHINENAME", "");
//				udfs.put("UNITNAME", "");
//				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//				udfs.put("MASKPOSITION", "");
//				
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("MACHINENAME", "");
				setEventInfo.getUdfs().put("UNITNAME", "");
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
				setEventInfo.getUdfs().put("MASKPOSITION", "0");

				// Execution
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
				
				/************************************* PhotoMaskStocker Management ************************************/
				
				PhtMaskStocker phtMaskStockerData = null;
				
				try{
				    phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().selectByKey(false, new Object[] {sMachineName, sUnitName, sStockerSlot});
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
			            
			            phtMaskStockerData.setMachineName(sMachineName);
			            phtMaskStockerData.setUnitName(sUnitName);
			            phtMaskStockerData.setLocation(Long.parseLong(sStockerSlot));
			            
			            phtMaskStockerData.setLastOutMaskName(sMaskName);
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
				
				/******************************************************************************************************/

			}
		}
		
		return doc;
	}
}
