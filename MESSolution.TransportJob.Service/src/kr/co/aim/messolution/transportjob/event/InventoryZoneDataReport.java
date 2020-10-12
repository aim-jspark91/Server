package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DspStockerZone;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class InventoryZoneDataReport extends AsyncHandler {

	// Modified by smkang on 2018.09.26 - CT_DSPSTOCKERZONE is used instead of CT_STOCKERZONEINFO.
//	@Override
//	public void doWorks(Document doc) throws CustomException {
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
//		
//		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		List<Element> zoneElementList = SMessageUtil.getBodySequenceItemList(doc, "ZONELIST", true);
//		
//		for(Element zoneElement : zoneElementList) {
//			String zoneName = zoneElement.getChildText("ZONENAME");
//			String totalCapacity = zoneElement.getChildText("TOTALCAPACITY");
//			String prohibitedShelfCount = zoneElement.getChildText("PROHIBITEDSHELFCOUNT");
//			String usedShelfCount = zoneElement.getChildText("USEDSHELFCOUNT");
//			String emptyShelfCount = zoneElement.getChildText("EMPTYSHELFCOUNT");
//			
//			List<StockerZoneInfo> stockerZoneInfoList = ExtendedObjectProxy.getStockerZoneInfo().select("MACHINENAME = ? AND ZONENAME = ?", new Object[] {machineName, zoneName});
//			
//			if(stockerZoneInfoList.size() > 0) {
//				StockerZoneInfo stockerZoneInfo = stockerZoneInfoList.get(0);
//				stockerZoneInfo.setTotalCapacity(totalCapacity);
//				stockerZoneInfo.setUsedShelfCount(usedShelfCount);
//				stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
//				stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
//				stockerZoneInfo.setEventName(eventInfo.getEventName());
//				stockerZoneInfo.setTimeKey(eventInfo.getEventTimeKey());
//				stockerZoneInfo.setEventUser(eventInfo.getEventUser());
//				stockerZoneInfo.setEventComment(eventInfo.getEventComment());
//				
//				try {
//					ExtendedObjectProxy.getStockerZoneInfo().modify(eventInfo, stockerZoneInfo);
//				} catch(Exception e) {
//					// Modified by smkang on 2018.05.06 - Although any zone has problem in for loop, another zones should be updated.
//					//									  So logging is added instead of throwing CustomException.
////					throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
//					eventLog.error(e);
//				}
//			} else {
//				StockerZoneInfo stockerZoneInfo = new StockerZoneInfo();
//				stockerZoneInfo.setMachineName(machineName);
//				stockerZoneInfo.setZoneName(zoneName);
//				stockerZoneInfo.setTotalCapacity(totalCapacity);
//				stockerZoneInfo.setUsedShelfCount(usedShelfCount);
//				stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
//				stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
//				stockerZoneInfo.setEventName(eventInfo.getEventName());
//				stockerZoneInfo.setTimeKey(eventInfo.getEventTimeKey());
//				stockerZoneInfo.setEventUser(eventInfo.getEventUser());
//				stockerZoneInfo.setEventComment(eventInfo.getEventComment());
//				
//				try {
//					ExtendedObjectProxy.getStockerZoneInfo().create(eventInfo, stockerZoneInfo);
//				} catch(Exception e) {
//					// Modified by smkang on 2018.05.06 - Although any zone has problem in for loop, another zones should be updated.
//					//									  So logging is added instead of throwing CustomException.
////					throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
//					eventLog.error(e);
//				}
//			}
//		}
//		
//		// Added by smkang on 2018.07.03 - Need to forward a message to linked factory.
//		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, machineName);
//	}
	@Override
	public void doWorks(Document doc) throws CustomException {
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(SMessageUtil.getMessageName(doc), getEventUser(), getEventComment(), "", "");
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		// Added by smkang on 2018.12.07 - For check this machine is existed in database.
		MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		List<Element> zoneElementList = SMessageUtil.getBodySequenceItemList(doc, "ZONELIST", true);
		
		for(Element zoneElement : zoneElementList) {
			String zoneName = zoneElement.getChildText("ZONENAME");
			long totalCapacity = StringUtils.isNumeric(zoneElement.getChildText("TOTALCAPACITY")) ? Long.parseLong(zoneElement.getChildText("TOTALCAPACITY")) : 0;

			// Added by smkang on 2019.05.20 - Missed logic.
			long highWaterMark = StringUtils.isNumeric(zoneElement.getChildText("HIGHWATERMARK")) ? Long.parseLong(zoneElement.getChildText("HIGHWATERMARK")) : 0;
			
			long prohibitedShelfCount = StringUtils.isNumeric(zoneElement.getChildText("PROHIBITEDSHELFCOUNT")) ? Long.parseLong(zoneElement.getChildText("PROHIBITEDSHELFCOUNT")) : 0;
			long usedShelfCount = StringUtils.isNumeric(zoneElement.getChildText("USEDSHELFCOUNT")) ? Long.parseLong(zoneElement.getChildText("USEDSHELFCOUNT")) : 0;
			long emptyShelfCount = StringUtils.isNumeric(zoneElement.getChildText("EMPTYSHELFCOUNT")) ? Long.parseLong(zoneElement.getChildText("EMPTYSHELFCOUNT")) : 0;
			
			// Added by smkang on 2019.05.20 - If MCS reports wrong values, this information wouldn't be updated.
			//if (totalCapacity > 0 && (usedShelfCount + emptyShelfCount) == totalCapacity) {   //modfiy by GJJ mantis 5208
			//if (totalCapacity > 0 && (usedShelfCount + emptyShelfCount+ prohibitedShelfCount) == totalCapacity) {
			if (totalCapacity > 0 ) { //modify byjhying on20200311 mantis :5758,5769
				try {
					List<DspStockerZone> stockerZoneInfoList = ExtendedObjectProxy.getDspStockerZoneService().select("STOCKERNAME = ? AND ZONENAME = ?", new Object[] {machineName, zoneName});
					
					if(stockerZoneInfoList.size() > 0) {
						DspStockerZone stockerZoneInfo = stockerZoneInfoList.get(0);
						stockerZoneInfo.setTotalCapacity(totalCapacity);
						stockerZoneInfo.setHighWaterMark(highWaterMark);	// Added by smkang on 2019.05.20 - Missed logic.						
						stockerZoneInfo.setUsedShelfCount(usedShelfCount);
						stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
						stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
						stockerZoneInfo.setLastEventName(eventInfo.getEventName());
						stockerZoneInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
						stockerZoneInfo.setLastEventTime(eventInfo.getEventTime());
						stockerZoneInfo.setLastEventUser(eventInfo.getEventUser());
						stockerZoneInfo.setLastEventComment(eventInfo.getEventComment());
						
						try {
							ExtendedObjectProxy.getDspStockerZoneService().modify(eventInfo, stockerZoneInfo);
						} catch(Exception e) {
							// Modified by smkang on 2018.05.06 - Although any zone has problem in for loop, another zones should be updated.
							//									  So logging is added instead of throwing CustomException.
//							throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
							eventLog.error(e);
						}
					}
				} catch (Exception e) {
					DspStockerZone stockerZoneInfo = new DspStockerZone();
					stockerZoneInfo.setStockerName(machineName);
					stockerZoneInfo.setZoneName(zoneName);
					stockerZoneInfo.setTotalCapacity(totalCapacity);
					stockerZoneInfo.setHighWaterMark(highWaterMark);	// Added by smkang on 2019.05.20 - Missed logic.						
					stockerZoneInfo.setUsedShelfCount(usedShelfCount);
					stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
					stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
					stockerZoneInfo.setLastEventName(eventInfo.getEventName());
					stockerZoneInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					stockerZoneInfo.setLastEventTime(eventInfo.getEventTime());
					stockerZoneInfo.setLastEventUser(eventInfo.getEventUser());
					stockerZoneInfo.setLastEventComment(eventInfo.getEventComment());
					
					try {
						ExtendedObjectProxy.getDspStockerZoneService().create(eventInfo, stockerZoneInfo);
					} catch(Exception e2) {
						// Modified by smkang on 2018.05.06 - Although any zone has problem in for loop, another zones should be updated.
						//									  So logging is added instead of throwing CustomException.
//						throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
						eventLog.error(e2);
					}
				}
			}
		}
		
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		// Added by smkang on 2018.09.26 - Need to delete a zone information which is not existed.
		try {
			Object[] bindSet = new Object[zoneElementList.size() + 1];
			bindSet[0] = machineName;
			
			for (int index = 0; index < zoneElementList.size(); index++) {
				bindSet[index + 1] = zoneElementList.get(index).getChildText("ZONENAME");;
			}
			
			String condition = "STOCKERNAME = ? AND ZONENAME NOT IN (" + StringUtils.removeEnd(StringUtils.repeat("?,", zoneElementList.size()), ",") + ")";
			List<DspStockerZone> removedStockerZoneList = ExtendedObjectProxy.getDspStockerZoneService().select(condition, bindSet);
			
			for (DspStockerZone stockerZoneInfo : removedStockerZoneList) {
				ExtendedObjectProxy.getDspStockerZoneService().remove(eventInfo, stockerZoneInfo);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		// --------------------------------------------------------------------------------------------------------------------------------------------------------
		
		// Added by smkang on 2018.07.03 - Need to forward a message to linked factory.
		// Modified by smkang on 2018.10.23 - According to EDO's request, inventory data and machine control state should be synchronized with shared factory without CT_SHIPPINGSTOCKER.
//		MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToShippingShop(doc, machineName);
	//	MESTransportServiceProxy.getTransportJobServiceUtil().publishMessageToSharedShop(doc);
	}
}