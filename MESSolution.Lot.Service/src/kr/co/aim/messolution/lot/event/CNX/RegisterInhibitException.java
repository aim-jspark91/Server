package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Inhibit;
import kr.co.aim.messolution.extended.object.management.data.InhibitException;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RegisterInhibitException extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String inhibitID = SMessageUtil.getBodyItemValue(doc, "INHIBITID", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		Lot lotData = null;

		try
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		}
		catch(NotFoundSignal ex)
		{
			throw new CustomException("INHIBIT-0004", "");
		}
		
		if(StringUtils.isEmpty(lotData.getCarrierName())) {
			throw new CustomException("LOT-0118", lotData.getKey().getLotName());
		}
		
		try {
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
		}
		catch (Throwable e) {
			throw new CustomException("CARRIER-9000", lotData.getCarrierName());
		}
		
		Inhibit inhibitData = ExtendedObjectProxy.getInhibitService().selectByKey(false, new Object[]{inhibitID});
		long exceptionLotCount = inhibitData.getExceptionLotCount();
		
		if(exceptionLotCount > 0)
		{
			throw new CustomException("INHIBIT-0003", "");
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Register", this.getEventUser(), this.getEventComment(), "", "");
			
			InhibitException inhibitExceptionData = new InhibitException(lotName, inhibitID);
			
			inhibitExceptionData.setLastEventName(eventInfo.getEventName());
			inhibitExceptionData.setLastEventTimekey(eventInfo.getEventTimeKey());
			inhibitExceptionData.setLastEventTime(eventInfo.getEventTime());
			inhibitExceptionData.setLastEventUser(eventInfo.getEventUser());
			inhibitExceptionData.setLastEventComment(eventInfo.getEventComment());
			inhibitExceptionData.setCreateTime(eventInfo.getEventTime());
			inhibitExceptionData.setCreateUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getInhibitExceptionService().create(eventInfo, inhibitExceptionData);
			
			setNextInfo(doc, lotName, inhibitID);
		}
		return doc;
	}
	
	private void setNextInfo(Document doc, String lotName, String inhibitID)
	{
		try
		{	
			StringBuilder strComment = new StringBuilder();
			strComment.append("Inhibit Exception Info").append("\n");
			strComment.append("InhibitID").append("[").append(inhibitID).append("]").append("\n");
			strComment.append("LotName").append("[").append(lotName).append("]").append("\n");
			//setEventComment(strComment.toString());
			
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, strComment.toString());
		}
		catch (Exception ex)
		{
			//eventLog.warn("Note after Crate is nothing");
		}
	}
	
	

}
