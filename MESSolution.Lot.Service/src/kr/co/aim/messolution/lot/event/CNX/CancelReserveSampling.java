package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CorresSampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveSampling extends SyncHandler {
	private static Log log = LogFactory.getLog(LotServiceImpl.class);
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//This is Cancel System Sampling
		
		//for common 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSystemSampling", getEventUser(),getEventComment(), null, null);
		//eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		if(StringUtils.equals(eventInfo.getEventComment(), "CancelReserveSampling"))
			eventInfo.setEventComment("CancelSystemSampling");		
		
		List<Element> samplingList = SMessageUtil.getBodySequenceItemList(doc, "SAMPLINGLIST", true);
		
		for (Element eledur : samplingList)
		{
			String lotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(eledur, "PRODUCTSPECNAME", true);
			String ecCode = SMessageUtil.getChildText(eledur, "ECCODE", true);
			String processFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
			String fromProcessOperationName = SMessageUtil.getChildText(eledur, "FROMPROCESSOPERATIONNAME", true);
			String sampleProcessFlowName = SMessageUtil.getChildText(eledur, "SAMPLEPROCESSFLOWNAME", true);
			String description = SMessageUtil.getChildText(eledur, "DESCRIPTION", true);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			if(StringUtils.equals(lotData.getKey().getLotName(), lotName)
					&&StringUtils.equals(lotData.getFactoryName(), factoryName)
					&&StringUtils.equals(lotData.getProductSpecName(), productSpecName)
					&&StringUtils.equals(lotData.getUdfs().get("ECCODE"), ecCode)
					&&StringUtils.equals(lotData.getProcessFlowName(),sampleProcessFlowName))
			{
				throw new CustomException("COMMON-0001", "Can't CancelSystemSampling! already Lot located this SampleFlow[" + sampleProcessFlowName + "]"); 
			}

			if(StringUtils.equals(description.toUpperCase(), "SYSTEMSAMPLING"))
			{
				SampleLot sampleLotInfo = null;

				try
				{
					sampleLotInfo = ExtendedObjectProxy.getSampleLotService().selectByKey(false, new Object[] {lotName, factoryName, productSpecName, ecCode, processFlowName,
							"00001", processOperationName, "00001", machineName, sampleProcessFlowName, "00001", fromProcessOperationName, "00001" });
				}
				catch(Throwable e){
					throw new CustomException("SAMPLE-0009"); 
				}
				
				if(sampleLotInfo != null)
				{
					if(!StringUtils.equals(sampleLotInfo.getSampleState(), "Decided"))
						throw new CustomException("COMMON-0001", "SampleState is not Decided !"); 
					
					sampleLotInfo.setSampleState("Canceled");
					sampleLotInfo.setLastEventName(eventInfo.getEventName());
					sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
					sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
					sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
					sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					sampleLotInfo = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLotInfo);
					
					ExtendedObjectProxy.getSampleLotService().delete(sampleLotInfo);
				}
			}
			else if(StringUtils.equals(description.toUpperCase(), "CORRESSAMPLING"))
			{
				CorresSampleLot sampleLotInfo = null;
				
				try
				{
					sampleLotInfo = ExtendedObjectProxy.getCorresSampleLotService().selectByKey(false, new Object[] {lotName, factoryName, productSpecName, ecCode, processFlowName,
							"00001", processOperationName, "00001", machineName, sampleProcessFlowName, "00001"});
				}
				catch(Throwable e){
					throw new CustomException("SAMPLE-0009"); 
				}
				
				if(sampleLotInfo != null)
				{
					if(!StringUtils.equals(sampleLotInfo.getSampleState(), "Decided"))
						throw new CustomException("COMMON-0001", "SampleState is not Decided !"); 
					
					sampleLotInfo.setSampleState("Canceled");
					sampleLotInfo.setLastEventName(eventInfo.getEventName());
					sampleLotInfo.setLastEventUser(eventInfo.getEventUser());
					sampleLotInfo.setLastEventComment(eventInfo.getEventComment());
					sampleLotInfo.setLastEventTime(eventInfo.getEventTime());
					sampleLotInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					sampleLotInfo = ExtendedObjectProxy.getCorresSampleLotService().modify(eventInfo, sampleLotInfo);
					
					ExtendedObjectProxy.getCorresSampleLotService().delete(sampleLotInfo);
				}

			}
			else{
				throw new CustomException("COMMON-0001", "Description [" + description + "] is unknown Sampling!"); 
			}
		}
		
		return doc;
	}
}
