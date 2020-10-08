package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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

public class UnLoadMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnMount", this.getEventUser(), this.getEventComment(), "", "");
		
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
		{
			String sMaskName = SMessageUtil.getChildText(eledur,"DURABLENAME", true);
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
			Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sMaskName));
			
			//2018.07.25 add hsryu
			CommonValidation.checkMaskUseState(sMaskName);
			
			// 2019.06.21_hsryu_Change setUdfs.  other udfs must not be changed.
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MACHINENAME", "");
			setEventInfo.getUdfs().put("MASKPOSITION", "");
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);

			// Modified by smkang on 2018.09.24 - Available state is used instead of UnMount state.
//			maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Unmount);
			maskData.setDurableState(GenericServiceProxy.getConstantMap().Cons_Available);
			
			DurableServiceProxy.getDurableService().update(maskData);
			
			//Execute
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		}
		
		return doc;	
	}
}