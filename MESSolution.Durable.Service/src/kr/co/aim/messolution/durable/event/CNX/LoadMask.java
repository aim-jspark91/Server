package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class LoadMask extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mount",this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc,"DURABLELIST", false)) 
		{
			String sDurableName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
			String sMachineName = SMessageUtil.getChildText(eledur,"MACHINENAME", true);
			String sMaskPosition = SMessageUtil.getChildText(eledur,"POSITION", false);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
			Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));
			
			//2017.7.31 zhongsl MaskPosition validation
			MESDurableServiceProxy.getDurableServiceUtil().checkExistPosition(sMachineName, sMaskPosition, "PhotoMask");
			
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Mount);
			DurableServiceProxy.getDurableService().update(maskData);
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
//			Map<String, String> udfs = new HashMap<String, String>();
//			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE);
//			udfs.put("MACHINENAME", sMachineName);
//			//udfs.put("UNITNAME", sUnitName);
//			udfs.put("MASKSUBLOCATION", GenericServiceProxy.getConstantMap().PHTMASKLOCATION_INLIB);
//			udfs.put("MASKPOSITION", sMaskPosition);
//			SetEventInfo setEventInfo = new SetEventInfo();
//			setEventInfo.setUdfs(udfs);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INLINE);
			setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
			setEventInfo.getUdfs().put("MASKSUBLOCATION", GenericServiceProxy.getConstantMap().PHTMASKLOCATION_INLIB);
			setEventInfo.getUdfs().put("MASKPOSITION", sMaskPosition);

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		}
		
		return doc;
	}
}