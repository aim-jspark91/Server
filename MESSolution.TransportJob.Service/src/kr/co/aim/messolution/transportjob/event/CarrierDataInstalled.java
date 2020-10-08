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
 * @see Processing TEX receives CarrierDataInstalled from MCS.
 */
public class CarrierDataInstalled extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
		
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", true);
		String positionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", true);
		String positionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String zoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", true);
		
		String carrierRestrictedStatus = SMessageUtil.getBodyItemValue(doc, "CARRIERRESTRICTEDSTATUS", false); //ADD BYJHIYING ON20191125 MANTIS:5257

		// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
		
		// Commented by smkang on 2018.05.02 - If MES needs to manage CRANE, VEHICLE or SHELF, more condition should be added.
		// Deleted by smkang on 2018.12.30 - Useless code.
//		if (StringUtils.equals(positionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT)) {
//			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, positionName);
//		}
		
		try {
			// Compare Previous Location and Current Location
			if(!StringUtils.equals(carrierData.getUdfs().get("MACHINENAME"), machineName) || 
				!StringUtils.equals(carrierData.getUdfs().get("POSITIONTYPE"), positionType) ||
				!StringUtils.equals(carrierData.getUdfs().get("POSITIONNAME"), positionName) ||
				!StringUtils.equals(carrierData.getUdfs().get("ZONENAME"), zoneName) ||
				!StringUtils.equals(carrierData.getUdfs().get("TRANSPORTSTATE"), transferState)||
				!StringUtils.equals(carrierData.getUdfs().get("CARRIERRESTRICTEDSTATUS"), carrierRestrictedStatus)) //MODIFY BY JHIYING ON20191203  
			{
				// Modified by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
//				Map<String, String> udfs = new HashMap<String, String>();
//				udfs.put("MACHINENAME", machineName);
//				udfs.put("POSITIONTYPE", positionType);
//				udfs.put("POSITIONNAME", positionName);
//				udfs.put("ZONENAME", zoneName);
//				udfs.put("TRANSPORTSTATE", transferState);
//				
//				SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
//				
//				MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
			// START MODIFY BY JHIYING ON20191126 MANTIS:5257
			/*	MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, machineName, 
											positionType, positionName,	zoneName, transferState, "", eventInfo);*/
				
				MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationV2(carrierData, machineName, positionType, positionName,	zoneName, transferState, "", eventInfo,carrierRestrictedStatus);
			// END MODIFY BY JHIYING ON20191126 MANTIS:5257
			} else {
				throw new CustomException("CST-0032", carrierData.getKey().getDurableName(), carrierData.getUdfs().get("MACHINENAME") + "/" + carrierData.getUdfs().get("POSITIONNAME"));
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