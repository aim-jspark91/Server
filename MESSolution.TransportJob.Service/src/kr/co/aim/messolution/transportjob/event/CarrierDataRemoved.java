package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.05.02
 * @see Processing TEX receives CarrierDataRemoved from MCS.
 */
public class CarrierDataRemoved extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
		
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
		
		try {
			// Compare Previous Location and Current Location
			if(StringUtils.equals(carrierData.getUdfs().get("MACHINENAME"), machineName)) {
				// Modified by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("MACHINENAME", "");
//				udfs.put("POSITIONTYPE", "");
//				udfs.put("POSITIONNAME", "");
//				udfs.put("ZONENAME", "");
//				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_OUTSTK);
//				
//				SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
//				
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
				String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState("", "");
				
				// Modified by smkang on 2018.11.28 - TransportJobCompleted and CarrierDataRemoved were reported too short interval, so a problem of TransportLockFlag was occurred.
//				MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, "", "", "",	"", transportState, "", eventInfo);
				MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, "", "", "",	"", transportState, "N", eventInfo);
			} else {
				throw new CustomException("CST-0033", carrierData.getKey().getDurableName(), machineName, carrierData.getUdfs().get("MACHINENAME"));
			}
		} catch (InvalidStateTransitionSignal ie) {
			eventLog.error(ie);
			return;
		} catch (FrameworkErrorSignal fe) {
			eventLog.error(fe);
			return;
		} catch (NotFoundSignal ne) {
			eventLog.error(ne);
			return;
		}
		
		// Added by smkang on 2018.11.03 - Need to forward a message to linked factory.
		//MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
	}
}