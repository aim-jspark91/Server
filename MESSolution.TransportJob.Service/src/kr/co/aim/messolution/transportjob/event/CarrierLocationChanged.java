package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CarrierLocationChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");

		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

		// 1. Check Exist Carrier
		// Modified by smkang on 2019.03.19 - Thread.Sleep is not perfect solution, SelectForUpdate is absolute.
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
		
		// Deleted by smkang on 2018.05.06 - I don't know why TransportJobCommand is created or modified.
		//									 Furthermore we can't know source or destination.
//		// 2. If Transport Job is Null, Change Carrier Location Only
//		if(StringUtils.isEmpty(transportJobName))
//		{
//			// Change Carrier Location
//			durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(durableData,
//					currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "",
//					eventInfo);
//			
//			return;
//		}
//
//		TransportJobCommand sqlRow = new TransportJobCommand();
//		try {
//			sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });
//
//		} catch (Exception e) {
//			eventInfo.setEventComment("Requested");
//			TransportJobCommand dataInfo = new TransportJobCommand();
//			dataInfo.setTransportJobName(transportJobName);
//			dataInfo.setTransportJobType("N/A");
//			dataInfo.setCarrierName(carrierName);
//
//			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, dataInfo);
//
//			eventInfo.setEventComment("ChangeLoc");
//		}
//
//		sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });
//
//		List<TransportJobCommand> sqlResult = new ArrayList<TransportJobCommand>();
//		sqlResult.add(sqlRow);
//
//		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult,
//				transportJobName);
//
//		// Update CT_TRANSPORTJOBCOMMAND
//		MESTransportServiceProxy.getTransportJobServiceImpl().updateTransportJobCommand(transportJobName, doc,
//				eventInfo);
		
		/*
		//2019.03.07 dmlee : Set TransportLockFlag
		TransportJobCommand transportJobData = new TransportJobCommand();
		String transportLockFlag = "";
		try
		{
			transportJobData = ExtendedObjectProxy.getTransportJobCommandService().selectByKey(false, new Object[]{transportJobName});
			
			if(transportJobData != null)
			{
				if(transportJobData.getDestinationMachineName().equals(currentMachineName) && transportJobData.getDestinationPositionName().equals(currentPositionName))
				{
					transportLockFlag = "N";
				}
			}

		}
		catch (Exception e)
		{
			eventLog.warn("-----------Non Exist TrasnPortJob Data ["+transportJobName+"]!!! -------------");
		}
		*/
		
		//2019.03.07 dmlee : Set TransportLocFlag End -----------------------
		
		
		// Change Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(durableData,
												currentMachineName, currentPositionType, currentPositionName, 
												currentZoneName, transferState, "", eventInfo);
		
/*		// Added by smkang on 2018.07.03 - Need to forward a message to linked factory.
		// Modified by smkang on 2018.10.18 - Carrier synchronization is necessary using SharedFactory or Owner data instead of CT_SHIPPINGSTOCKER data.
//		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, currentMachineName);
		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);*/
	}
}