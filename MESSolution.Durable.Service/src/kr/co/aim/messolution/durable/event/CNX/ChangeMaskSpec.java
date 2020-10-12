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

public class ChangeMaskSpec extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSpec", this.getEventUser(), this.getEventComment(), "", "");
		
		String sQRCode = SMessageUtil.getBodyItemValue(doc, "QRCODE", true);
		String sMaskSpecName = SMessageUtil.getBodyItemValue(doc, "DURABLESPECNAME",true);

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sQRCode);
		Durable maskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sQRCode));
		
		//validate maskType
		if (!maskData.getDurableType().equals("EVAMask"))
			throw new CustomException("MASK-0037", sQRCode);
		
		//validate Scrapped state
		if (!maskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
			throw new CustomException("MASK-0076", sQRCode);
		
		maskData.setDurableSpecName(sMaskSpecName);
		DurableServiceProxy.getDurableService().update(maskData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		
		//change maskSpec event
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);
		
		return doc;
	}
}