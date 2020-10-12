package kr.co.aim.messolution.durable.event;

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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeCSTAttributes extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sTimeUsed = SMessageUtil.getBodyItemValue(doc,"TIMEUSED", true);
		String sDurationUsed = SMessageUtil.getBodyItemValue(doc,"DURATIONUSED", true);
		String sTransportLockFlag = SMessageUtil.getBodyItemValue(doc,"TRANSPORTLOCKFLAG", true);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(new DurableKey(sDurableName));
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sDurableName));

		if (!StringUtils.equals(durableData.getFactoryName(), System.getProperty("shop")) &&
			!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available)) {
			
			throw new CustomException("CST-0039", sDurableName, durableData.getFactoryName());
		}

		durableData.setFactoryName(sFactoryName);
		
		if (StringUtils.isNotEmpty(sDurationUsed))
			durableData.setDurationUsed(Double.parseDouble(sDurationUsed));

		if (StringUtils.isNotEmpty(sTimeUsed))
			durableData.setTimeUsed(Double.parseDouble(sTimeUsed));
		
		DurableServiceProxy.getDurableService().update(durableData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCarrierInfo", this.getEventUser(), this.getEventComment(), "", "");
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", sTransportLockFlag);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
		// Added by smkang on 2018.10.23 - According to EDO's request, carrier data should be synchronized with shared factory.
		//MESDurableServiceProxy.getDurableServiceUtil().publishMessageToSharedShop(doc, sDurableName);
		
		return doc;
	}
}