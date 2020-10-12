package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhtMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MaskStockIn extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TakeIn", this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);	
		
		String sMachineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
				
		if (eleBody != null) 
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true)) {
				
				String sMaskName = SMessageUtil.getChildText(eledur,"MASKNAME", true);
				String sStockerSlot = SMessageUtil.getChildText(eledur,"STOCKERSLOT", true);
				
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
				
				CommonValidation.checkMaskUseState(sMaskName);
				
				//durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
				//DurableServiceProxy.getDurableService().update(durableData);
				
				// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("MACHINENAME", sMachineName);
//				udfs.put("MASKPOSITION", sStockerSlot);
//				udfs.put("UNITNAME", sUnitName);
//				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
//				
//				SetEventInfo setEventInfo = new SetEventInfo();
//				setEventInfo.setUdfs(udfs);
				
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
				setEventInfo.getUdfs().put("MASKPOSITION", sStockerSlot);
				setEventInfo.getUdfs().put("UNITNAME", sUnitName);
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);

				// Execution
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
							
				// 2019.05.15_hsryu_Delete Logic. 
//				List<PhtMaskStocker> phtMaskStkList = new ArrayList<PhtMaskStocker>();
//								
//				phtMaskStkList = ExtendedObjectProxy.getMaskStockerService().select("where machineName = ? and unitName = ? and location = ? ", new Object[]{sMachineName,sUnitName,sStockerSlot});
//									
//				if(phtMaskStkList.size()!=0)
//				{
//					PhtMaskStocker phtMaskStocker = new PhtMaskStocker();
//					
//					phtMaskStocker = phtMaskStkList.get(0);
//					
//					if(!phtMaskStocker.getCurrentMaskName().equals(""))
//					{
//						throw new CustomException("STK-0001", sMaskName,sMachineName,sUnitName,sStockerSlot);
//					}
//						
//					phtMaskStocker.setCurrentMaskName(sMaskName);
//					phtMaskStocker.setCurrentInTime(eventInfo.getEventTime());
//					phtMaskStocker.setLastEventName(eventInfo.getEventName());
//					phtMaskStocker.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//					phtMaskStocker.setLastEventTime(eventInfo.getEventTime());
//					phtMaskStocker.setLastEventUser(eventInfo.getEventUser());
//					phtMaskStocker.setLastEventComment(eventInfo.getEventComment());
//						
//					phtMaskStocker = ExtendedObjectProxy.getMaskStockerService().modify(eventInfo, phtMaskStocker);
//				}
//				else
//				{
//					new CustomException("MASK-0083",sMachineName,sUnitName,sStockerSlot);
//				}
				
				/************************************* PhotoMaskStocker Management ************************************/
				
				// 2019.05.15_hsryu_if other Location Stock In Mask, Take Out For PhotoMaskStocker Management Table. 
				try{
					ExtendedObjectProxy.getPhtMaskStockerService().takeOutCurrentMask(sMachineName, sUnitName, sStockerSlot, durableData, eventInfo);
				}
				catch(Throwable e){
					eventLog.warn("Fail takeOutCurrentMask!");
				}
				
				PhtMaskStocker phtMaskStockerData = null;
				
				try{
		            phtMaskStockerData = ExtendedObjectProxy.getPhtMaskStockerService().selectByKey(false, new Object[] {sMachineName, sUnitName, sStockerSlot});
				}
				catch(Throwable e){
					eventLog.info("PhtMaskStocker Data is not exist");
				}
				
				try{
					if(phtMaskStockerData != null) {
			            if(!StringUtils.equals(phtMaskStockerData.getCurrentMaskName(), sMaskName)){
			            	
							// Take Out ! 
							phtMaskStockerData.setLastOutMaskName(StringUtils.isEmpty(phtMaskStockerData.getCurrentMaskName())?phtMaskStockerData.getLastOutMaskName():phtMaskStockerData.getCurrentMaskName());
							phtMaskStockerData.setLastOutTime(StringUtils.isEmpty(phtMaskStockerData.getCurrentMaskName())?phtMaskStockerData.getLastOutTime():eventInfo.getEventTime());

				            phtMaskStockerData.setCurrentMaskName(sMaskName);
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
			            phtMaskStockerData.setMachineName(sMachineName);
			            phtMaskStockerData.setUnitName(sUnitName);
			            phtMaskStockerData.setLocation(Long.parseLong(sStockerSlot));
			            
			            phtMaskStockerData.setCurrentMaskName(sMaskName);
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
		return doc;
	}
}
