package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnLoadProbeCard extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unmount", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
		{
			String sProbeName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sProbeName);
			Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sProbeName));
			
            CommonUtil.setMaskPositionUpdate(maskData.getKey().getDurableName());
            
            maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sProbeName);
			
			CommonValidation.checkMaskUseState(sProbeName);
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", "");
			udfs.put("UNITNAME", "");
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
			
			maskData.setUdfs(udfs);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			// Modified by smkang on 2018.09.24 - Available state is used instead of UnMount state.
//			maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
			
			DurableServiceProxy.getDurableService().update(maskData);
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		}
		
		return doc;	
	}
}