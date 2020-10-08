package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author JHY
 * @since 2019.11.08
 * @see MCS会在CarrierRestrictedStatusChange中上报这个值，MES需要存储
 */
public class CarrierRestrictedStatusChange extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
		
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);	
		String carrierRestrictedStatus = SMessageUtil.getBodyItemValue(doc, "CARRIERRESTRICTEDSTATUS", false);	
		String carrierRestrictedStatusNote = StringUtil.EMPTY;
		if(StringUtil.equals(carrierRestrictedStatus, "Y"))
			
			carrierRestrictedStatusNote = "CarrierRestrictedStatusChange" + " - [When CARRIERRESTRICTEDSTATUS=Y，CST Can't Transport！]";
		
		try
		{
			DurableKey durableKey = new DurableKey();
			durableKey.setDurableName(carrierName);
			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> durableUdfs = setEventInfo.getUdfs();
			durableUdfs.put("CARRIERRESTRICTEDSTATUS", carrierRestrictedStatus);
			durableUdfs.put("NOTE", carrierRestrictedStatusNote);
			DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);
						
			 //ADD BY JHYING ON 20200404 MANTIS:5971
			Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
			Map<String, String> updateUdfs = new HashMap<String, String>();
			updateUdfs.put("NOTE", "");
			MESDurableServiceProxy.getDurableServiceImpl().updateDurableWithoutHistory(durableData, updateUdfs);
			
		}
		 catch (NotFoundSignal ne)
		{
			eventLog.error(ne);
			return;
		}
	}
}