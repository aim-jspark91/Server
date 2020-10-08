package kr.co.aim.messolution.durable.event.CNX;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Reserve", this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null) {
			// parsing
			String sSTKMachineName = SMessageUtil.getChildText(eleBody, "STKNAME", true);
			String sSTKPortName = SMessageUtil.getChildText(eleBody, "STKPORT", true);
			String sMaskCarrierName = SMessageUtil.getChildText(eleBody, "DURABLENAME", true);
			String sEVAUnitName = SMessageUtil.getChildText(eleBody, "UNITNAME", true);

			// reserve mask list
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false)) {
				// parsing
				String sMaskName = SMessageUtil.getChildText(eledur, "MASKNAME", true);
				String sSSlotNo = SMessageUtil.getChildText(eledur, "POSITION", false);
				String sEVASubUnitName = SMessageUtil.getChildText(eledur, "POSITIONNAME", false);
				String sMaskCleanRecipe = SMessageUtil.getChildText(eledur, "MASKCLEANRECIPENAME", false);
				String sOffSetMark = SMessageUtil.getChildText(eledur, "OFFSETMARK", false);
				String sOffSetPre = SMessageUtil.getChildText(eledur, "OFFSETPRE", false);
				String sWorkFlow = SMessageUtil.getChildText(eledur, "WORKFLOW", true);
				String sState = "Created";
				String TimeUsedLimit = SMessageUtil.getChildText(eledur, "TIMEUSEDLIMIT", false);
				ReserveMaskList reserveMaskdataInfo = new ReserveMaskList();
				Durable durMaskData = new Durable();
				SetEventInfo setEventInfo = new SetEventInfo();
				Map<String, String> udfs = durMaskData.getUdfs();

				reserveMaskdataInfo = new ReserveMaskList(TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DETAIL));
				reserveMaskdataInfo.setMaskName(sMaskName);
				reserveMaskdataInfo.setCarrierName(sMaskCarrierName);
				reserveMaskdataInfo.setMachineName(sSTKMachineName);
				reserveMaskdataInfo.setUnitName(sEVAUnitName);
				reserveMaskdataInfo.setSubUnitName(sEVASubUnitName);
				reserveMaskdataInfo.setPortname(sSTKPortName);
				reserveMaskdataInfo.setPosition(sSSlotNo);
				reserveMaskdataInfo.setMaskCleanRecipe(sMaskCleanRecipe);
				reserveMaskdataInfo.setOffSetPre(sOffSetPre);
				reserveMaskdataInfo.setOffSetMark(sOffSetMark);
				reserveMaskdataInfo.setWorkFlow(sWorkFlow);
				reserveMaskdataInfo.setState(sState);

				// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//				durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
				durMaskData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(sMaskName));

				ExtendedObjectProxy.getReserveMaskService().create(eventInfo, reserveMaskdataInfo);

				// Put data into UDF
				udfs = durMaskData.getUdfs();
				udfs.put("OFFSETPRE", sOffSetPre);
				udfs.put("OFFSET", sOffSetPre);
				
				double timeUsedLimit = Double.parseDouble(TimeUsedLimit);
				durMaskData.setTimeUsedLimit(timeUsedLimit);
				durMaskData.setUdfs(udfs);

				setEventInfo = new SetEventInfo();
				setEventInfo.setUdfs(udfs);
				DurableServiceProxy.getDurableService().update(durMaskData);
				
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
			}
		}
		
		return doc;
	}
}