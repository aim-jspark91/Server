package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetCarrierDataReply extends AsyncHandler {
	
	@Override
	// Modified by smkang on 2018.05.02 - According to EDO Hong Wei's request, after OPI receives GetCarrierDataReply, database will be updated directly.
	//									  When TEXsvr receives GetCarrierDataReply, database will be updated immediately without additional action.
//	public void doWorks(Document doc) throws CustomException 
//	{
//		// Modified by smkang on 2018.04.04 - Because GetCarrierDataRequest is only requested by OPI, TEXsvr doesn't need to update carrier data.
////		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetCarrierDataReply", getEventUser(), getEventComment(), "", "");
////		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
////		
////		try
////		{
////			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
////			String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
////			String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
////			String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
////			String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
////			String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
////			
////			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
////			
////			durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(
////							durableData, currentMachineName, currentPositionType, currentPositionName, 
////							currentZoneName, transferState, "", eventInfo);
////		}
////		catch(Exception e)
////		{
////		}
////		
////		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
////		doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
////		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
//		
//		// Modified by smkang on 2018.04.13 - Reply message should be sent using sendReplyBySender method.
////		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
//		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
//	}
	public void doWorks(Document doc) throws CustomException
	{
		try {
			EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
			

			String returnCode = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, false);

			String ErrorMessage = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false);
            
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String machineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
			String positionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
			String positionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
			String zoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
			String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
			
			String carrierRestrictedStatus = SMessageUtil.getBodyItemValue(doc, "CARRIERRESTRICTEDSTATUS", false);//MODIFY BY JHIYING ON20191126 MANTIS:5257
			
			// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
			// modify by jhiying on20191119  mantis :4945 start
		    if(!StringUtil.isEmpty(ErrorMessage) ){
				SetEventInfo setEventInfo = new SetEventInfo();
				DurableServiceProxy.getDurableService().setEvent(carrierData.getKey(), eventInfo,setEventInfo);
		    }
		    	
		    else {
		    	// modify by jhiying on20191119  mantis :4945 end
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
				  //START MODIFY BY JHIYING ON20191126 MANTIS:5257
			/*	MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, machineName, 
											positionType, positionName,	zoneName, transferState, "", eventInfo); */
			MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationV2(carrierData, machineName, positionType, positionName,	zoneName, transferState, "", eventInfo,carrierRestrictedStatus);						
			 //END MODIFY BY JHIYING ON20191126 MANTIS:5257
			    }else {
				// Modified by smkang on 2018.12.01 - If an exception throws in AsyncHandler class, OPI can't receive the exception and timeout occurs.
//				throw new CustomException("CST-0032", carrierData.getKey().getDurableName(), carrierData.getUdfs().get("MACHINENAME") + "/" + carrierData.getUdfs().get("POSITIONNAME"));
				eventLog.info("Carrier[" + carrierData.getKey().getDurableName() + "] is on same location[" + carrierData.getUdfs().get("MACHINENAME") + "/" + carrierData.getUdfs().get("POSITIONNAME") + "]");
			  }
		    }
		    
			// Added by smkang on 2018.07.03 - Need to forward a message to linked factory.
			// Modified by smkang on 2018.10.23 - EDO CIM team doesn't want to register CT_SHIPPINGSTOCKER.
//			MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, machineName);
			//MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc, carrierName);
			
			// Added by smkang on 2018.10.12 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.10.16 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		
		}catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			// Added by smkang on 2018.10.12 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.10.16 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
}