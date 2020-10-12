package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetInventoryCarrierDataReply  extends AsyncHandler {
	
	@Override
	// Modified by smkang on 2018.05.02 - According to EDO Hong Wei's request, after OPI receives GetInventoryCarrierDataReply, database will be updated directly.
	//									  When TEXsvr receives GetInventoryCarrierDataReply, database will be updated immediately without additional action.
//	public void doWorks(Document doc) throws CustomException 
//	{
//		// Modified by smkang on 2018.04.13 - Reply message should be sent using sendReplyBySender method.
////		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
//		GenericServiceProxy.getESBServive().sendReplyBySender(getOriginalSourceSubjectName(), doc, "OICSender");
//	}
	public void doWorks(Document doc) throws CustomException
	{
		try {
//			Case 1. MCS reports a carrier data and the carrier is existed in MES database, but location of the carrier is different.
//			        - MES updates location with MCS data.
//			Case 2. MCS reports a carrier data, but it isn't existed in MES database.
//			        - MES can't create the carrier because durable information is not enough to create a carrier.
//			Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
//			        - MES deletes location.
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		
			List<Element> carrierElementList = SMessageUtil.getBodySequenceItem(doc, "CARRIERLIST", true).getChildren();
			
			if (carrierElementList != null && carrierElementList.size() > 0) {
				EventInfo eventInfo = null;
				
				List<String> mcsCarrierNames = new ArrayList<String>();
				for (Element carrierElement : carrierElementList) {
					try {
						eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
						
						String carrierName = carrierElement.getChildText("CARRIERNAME");
						String positionType = carrierElement.getChildText("CURRENTPOSITIONTYPE");
						String positionName = carrierElement.getChildText("CURRENTPOSITIONNAME");
						String zoneName = carrierElement.getChildText("CURRENTZONENAME");
						String carrierRestrictedStatus = carrierElement.getChildText("CARRIERRESTRICTEDSTATUS"); //ADD BYJHIYING ON20191125 MANTIS:5257
						
						mcsCarrierNames.add(carrierName);
						
						// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//						Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
						Durable carrierData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
						
						// Case 1. MCS reports a carrier data and the carrier is existed in MES database, but location of the carrier is different.
						if(!StringUtils.equals(carrierData.getUdfs().get("MACHINENAME"), machineName) || 
							!StringUtils.equals(carrierData.getUdfs().get("POSITIONTYPE"), positionType) ||
							!StringUtils.equals(carrierData.getUdfs().get("POSITIONNAME"), positionName) ||
							!StringUtils.equals(carrierData.getUdfs().get("ZONENAME"), zoneName)||
							!StringUtils.equals(carrierData.getUdfs().get("CARRIERRESTRICTEDSTATUS"), carrierRestrictedStatus)) //MODIFY BY JHIYING ON20191203 
							{
							// Modified by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
//							Map<String, String> udfs = new HashMap<String, String>();
//							udfs.put("MACHINENAME", machineName);
//							udfs.put("POSITIONTYPE", positionType);
//							udfs.put("POSITIONNAME", positionName);
//							udfs.put("ZONENAME", zoneName);
//							
//							String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(machineName, positionType);
//							if (StringUtils.isNotEmpty(transportState))
//								udfs.put("TRANSPORTSTATE", transportState);
//							
//							SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
//							
//							MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
							String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState(machineName, positionType);
					         //START MODIFY BY JHIYING ON20191126 MANTIS:5257 
						/*	MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, machineName, 
														positionType, positionName,	zoneName, transportState, "", eventInfo);*/
							MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocationV2(carrierData, machineName, 
									positionType, positionName,	zoneName, transportState, "", eventInfo,carrierRestrictedStatus);
							//END MODIFY BY JHIYING ON20191126 MANTIS:5257 		
						}
					} catch (Exception e) {
						// TODO: handle exception
						// Commented by smkang on 2018.05.02 - Although any port has problem in for loop, another ports should be updated.
						//									   So CustomException handler is added here.
						// Case 2. MCS reports a carrier data, but it isn't existed in MES database.
						eventLog.error(e);
					}
				}
				
				// Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
				List<Durable> carrierList = MESDurableServiceProxy.getDurableServiceUtil().getCarrierListByEQP(machineName, mcsCarrierNames);
				removeLocationInfo(doc, carrierList);
			} else {
				// Case 3. MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
				List<Durable> carrierList = MESDurableServiceProxy.getDurableServiceUtil().getCarrierListByEQP(machineName, null);
				removeLocationInfo(doc, carrierList);
			}
			
			// Added by smkang on 2018.11.03 - Need to forward a message to linked factory.
			// 								   According to EDO's request, inventory data and machine control state should be synchronized with shared factory without CT_SHIPPINGSTOCKER.
		//	MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc);
			
			// Added by smkang on 2018.11.03 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.11.03 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		} catch (Exception e) {
			eventLog.error(e);

			if (e instanceof CustomException) {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, ((CustomException) e).errorDef.getErrorCode());
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, ((CustomException) e).errorDef.getLoc_errorMessage());
			} else {
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, (e != null) ? e.getClass().getName() : "SYS-0000");
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, (e != null && StringUtils.isNotEmpty(e.getMessage())) ? e.getMessage() : "Unknown exception is occurred.");
			}
			
			// Added by smkang on 2018.11.03 - To avoid send a message twice.
			String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);	// Added by smkang on 2018.11.03 - eventUser variable is not same with EVENTUSER of a document.
			if (!System.getProperty("svr").equals(eventUser))
				GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
			
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	}
	
	// Added by smkang on 2018.05.02 - MCS doesn't report a carrier but the carrier isn't existed in the machine of MES database.
	//								   MES deletes location.
	private void removeLocationInfo(Document doc, List<Durable> carrierList) {
		if (carrierList != null && carrierList.size() > 0) {
			EventInfo eventInfo = null;
			
			for (Durable carrierData : carrierList) {
				try {
					eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), this.getEventUser(), this.getEventComment(), "", "");
					
					// Modified by smkang on 2018.05.06 - Need to update FactoryName and AreaName and using common method.
//					Map<String, String> udfs = new HashMap<String, String>();
//					udfs.put("MACHINENAME", "");
//					udfs.put("POSITIONTYPE", "");
//					udfs.put("POSITIONNAME", "");
//					udfs.put("ZONENAME", "");
//					
//					String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState("", "");
//					if (StringUtils.isNotEmpty(transportState))
//						udfs.put("TRANSPORTSTATE", transportState);
//					
//					SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
//					
//					MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
					String transportState = MESTransportServiceProxy.getTransportJobServiceUtil().judgeTransportState("", "");
					MESTransportServiceProxy.getTransportJobServiceImpl().changeCurrentCarrierLocation(carrierData, "", 
												"", "",	"", transportState, "", eventInfo);
				} catch (Exception e) {
					// TODO: handle exception
					eventLog.error(e);
				}
			}
		}
	}
}