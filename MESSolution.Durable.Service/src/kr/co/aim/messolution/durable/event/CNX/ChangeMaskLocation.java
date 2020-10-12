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
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeMaskLocation extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME",true );
		String location = SMessageUtil.getBodyItemValue(doc, "LOCATION",true);
		String event= SMessageUtil.getBodyItemValue(doc, "EVENT",true);
		
		if(event.equals("IN"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TakeIn", this.getEventUser(), this.getEventComment(), "", "");
			String newMaskName = SMessageUtil.getBodyItemValue(doc, "NEWMASKNAME", true);
			
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(newMaskName);
			
			CommonValidation.checkMaskUseState(newMaskName);
			
			ExtendedObjectProxy.getMaskStockerService().checkStockInByMaskName(newMaskName);
						
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> udfs = new HashMap<String, String>();
//			udfs.put("MACHINENAME", machineName);
//			udfs.put("UNITNAME", unitName);
//			udfs.put("MASKPOSITION", location);
//			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
//			
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(udfs);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MACHINENAME", machineName);
			setEventInfo.getUdfs().put("UNITNAME", unitName);
			setEventInfo.getUdfs().put("MASKPOSITION", location);
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);

			// Execution
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			
			PhtMaskStocker phtMaskStk = new PhtMaskStocker();
			phtMaskStk = ExtendedObjectProxy.getMaskStockerService().selectByKey(false, new Object[]{machineName,unitName,location});
				
			if(phtMaskStk!=null)
			{
				if(!phtMaskStk.getCurrentMaskName().equals(""))
				{
					throw new CustomException("STK-0001",machineName,unitName,location);
				}
					
				phtMaskStk.setCurrentMaskName(newMaskName);
				phtMaskStk.setCurrentInTime(eventInfo.getEventTime());
				phtMaskStk.setLastEventName(eventInfo.getEventName());
				phtMaskStk.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				phtMaskStk.setLastEventTime(eventInfo.getEventTime());
				phtMaskStk.setLastEventUser(eventInfo.getEventUser());
				phtMaskStk.setLastEventComment(eventInfo.getEventComment());
					
				phtMaskStk = ExtendedObjectProxy.getMaskStockerService().modify(eventInfo, phtMaskStk);
			}
			else
			{
				new CustomException("MASK-0083",machineName,unitName,location);
			}
		}
		else if(event.equals("OUT"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TakeOut", this.getEventUser(), this.getEventComment(), "", "");
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			
			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> udfs = new HashMap<String, String>();
//			udfs.put("MACHINENAME", "");
//			udfs.put("UNITNAME", "");
//			udfs.put("MASKPOSITION", "0");
//			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(udfs);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MACHINENAME", "");
			setEventInfo.getUdfs().put("UNITNAME", "");
			setEventInfo.getUdfs().put("MASKPOSITION", "0");
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

			// Execution
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
			
			PhtMaskStocker phtMaskStk = new PhtMaskStocker();
			phtMaskStk = ExtendedObjectProxy.getMaskStockerService().selectByKey(false, new Object[]{machineName,unitName,location});
							
			if(phtMaskStk!=null)
			{
				if(phtMaskStk.getCurrentMaskName().equals(""))
				{
					throw new CustomException("STK-0002", maskName,machineName,unitName,location);
				}
										
				phtMaskStk.setCurrentMaskName("");
				phtMaskStk.setCurrentInTime(null);
				phtMaskStk.setLastOutMaskName(maskName);
				phtMaskStk.setLastOutTime(eventInfo.getEventTime());
				phtMaskStk.setLastEventName(eventInfo.getEventName());
				phtMaskStk.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				phtMaskStk.setLastEventTime(eventInfo.getEventTime());
				phtMaskStk.setLastEventUser(eventInfo.getEventUser());
				phtMaskStk.setLastEventComment(eventInfo.getEventComment());
					 
				phtMaskStk = ExtendedObjectProxy.getMaskStockerService().modify(eventInfo, phtMaskStk);
			}
			else
			{
				throw new CustomException("MASK-0083",machineName,unitName,location);
			}
		}
		
		return doc;
	}
}
