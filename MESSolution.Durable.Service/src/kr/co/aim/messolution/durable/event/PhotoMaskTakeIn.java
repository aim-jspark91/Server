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
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class PhotoMaskTakeIn extends AsyncHandler{
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PhotoMaskTakeIn", this.getEventUser(), this.getEventComment(), null, null);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName  = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String fromSlotID = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotID = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", true);
		String receiveTime = SMessageUtil.getBodyItemValue(doc, "RECEIVETIME", false);
		
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Machine unitData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		
		MachineSpec unitSpec  = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
		
		if(Integer.parseInt(toSlotID) > unitSpec.getProcessCapacity())
		{
			eventLog.error("toSlotID is bigger than ProcessCapacity!!");
		}
		else
		{
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);			
			Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(maskName));			
			
			MESDurableServiceProxy.getDurableServiceUtil().checkExistenceByPhotoMaskPosition(eventInfo, machineName, unitName, toSlotID);
						
			if(!maskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
			{
				maskData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				DurableServiceProxy.getDurableService().update(maskData);
			}
			
			eventInfo.setEventName("PhotoMaskTakeIn");
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", machineName);
			udfs.put("UNITNAME", unitName);
			udfs.put("POSITIONNAME", subUnitName);
			//udfs.put("PORTNAME", "");         ?
			//udfs.put("POSITIONTYPE", "");     ?
			//udfs.put("MASKCARRIERNAME", "");  ?
			udfs.put("MASKPOSITION", toSlotID);
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);	
			udfs.put("RECEIVETIME", TimeStampUtil.getTimestamp(receiveTime).toString());
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			//set event
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			
			/************************************* PhotoMaskStocker Management ************************************/
			
			// 2019.05.15_hsryu_if other Location Stock In Mask, Take Out For PhotoMaskStocker Management Table. 
			try{
				ExtendedObjectProxy.getPhtMaskStockerService().takeOutCurrentMask(machineName, unitName, toSlotID, maskData, eventInfo);
			}
			catch(Throwable e){
				eventLog.warn("Fail takeOutCurrentMask!");
			}
			
			PhtMaskStocker phtMaskStockerData = null;
			
			try{
	            phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().selectByKey(false, new Object[] {machineName,unitName,toSlotID});
			}
			catch(Throwable e){
				eventLog.info("PhtMaskStocker Data is not exist");
			}
			
			try{
				if(phtMaskStockerData != null){
		            if(!StringUtils.equals(phtMaskStockerData.getCurrentMaskName(), maskName)){
		            	
						// Take Out ! 
						phtMaskStockerData.setLastOutMaskName(StringUtils.isEmpty(phtMaskStockerData.getCurrentMaskName())?phtMaskStockerData.getLastOutMaskName():phtMaskStockerData.getCurrentMaskName());
						phtMaskStockerData.setLastOutTime(StringUtils.isEmpty(phtMaskStockerData.getCurrentMaskName())?phtMaskStockerData.getLastOutTime():eventInfo.getEventTime());

			            phtMaskStockerData.setCurrentMaskName(maskName);
			            phtMaskStockerData.setCurrentInTime(eventInfo.getEventTime());
			            
			            // 2019.05.14_hsryu_Set LastEventInfo. Missing Logic.
						phtMaskStockerData.setLastEventName(eventInfo.getEventName());
						phtMaskStockerData.setLastEventTime(eventInfo.getEventTime());
						phtMaskStockerData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						phtMaskStockerData.setLastEventComment(eventInfo.getEventComment());
						phtMaskStockerData.setLastEventUser(eventInfo.getEventUser());

						phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().modify(eventInfo, phtMaskStockerData);
		            }
				}
				else{
					eventLog.info("PhtMaskStocker Data is not exist. Start Create !  ");  
		            
		            phtMaskStockerData = new PhtMaskStocker();
		            phtMaskStockerData.setMachineName(machineName);
		            phtMaskStockerData.setUnitName(unitName);
		            phtMaskStockerData.setLocation(Long.parseLong(toSlotID));
		            
		            phtMaskStockerData.setCurrentMaskName(maskName);
	                phtMaskStockerData.setCurrentInTime(eventInfo.getEventTime());     
	                
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
}
