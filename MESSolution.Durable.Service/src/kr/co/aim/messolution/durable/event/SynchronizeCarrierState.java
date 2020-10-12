package kr.co.aim.messolution.durable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

/**
 * @author smkang
 * @since 2018.10.23
 * @see For synchronization of a carrier state and lot quantity, common method will be invoked.
 *
 */
public class SynchronizeCarrierState extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Modified by smkang on 2018.11.03 - TransportLockFlag, StockerInTime, Kanban or Region can be also updated using SynchronizeCarrierState.
		// 									  EventName will be recorded triggered EventName.
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
//		String carrierState = SMessageUtil.getBodyItemValue(doc, "DURABLESTATE", true);
//		
//		EventInfo eventInfo = null;
//		if (carrierState.equals(GenericServiceProxy.getConstantMap().Dur_Available))
//			eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", this.getEventUser(), this.getEventComment(), "", "");
//		else if (carrierState.equals(GenericServiceProxy.getConstantMap().Dur_InUse))
//			eventInfo = EventInfoUtil.makeEventInfo("AssignCarrier", this.getEventUser(), this.getEventComment(), "", "");
//		
//		if (eventInfo != null) {
//			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
//			durableData.setDurableState(carrierState);
//			durableData.setLotQuantity(carrierState.equals(GenericServiceProxy.getConstantMap().Dur_Available) ? 0 : (durableData.getLotQuantity() + 1));
//			
//			DurableServiceProxy.getDurableService().update(durableData);
//			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, new SetEventInfo());
//			
//			// Added by smkang on 2018.10.25 - According to EDO's request, when a carrier is changed to Available state, the carrier name of lot and product will be also deleted.
//			if (carrierState.equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
//				try {
//					List<Lot> lotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ?", new Object[] {carrierName});
//					
//					for (Lot lotData : lotDataList) {
//						lotData.setCarrierName("");
//						LotServiceProxy.getLotService().update(lotData);
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//					eventLog.warn(e);
//				}
//				
//				try {
//					List<Product> productDataList = ProductServiceProxy.getProductService().select("CARRIERNAME = ?", new Object[] {carrierName});
//					
//					for (Product productData : productDataList) {
//						productData.setCarrierName("");
//						ProductServiceProxy.getProductService().update(productData);
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//					eventLog.warn(e);
//				}
//			}
//		} else {
//			eventLog.warn("CarrierState is " + carrierState + ", so this information will not be updated.");
//		}
		String carrierName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		
		// Modified by smkang on 2019.04.09 - To guarantee synchronization. 
//		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		Durable durableData = DurableServiceProxy.getDurableService().selectByKeyForUpdate(new DurableKey(carrierName));
		
		String messageName = SMessageUtil.getMessageName(doc);
		String eventComment = SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(StringUtils.isNotEmpty(eventComment) ? eventComment : messageName, getEventUser(), StringUtils.isNotEmpty(eventComment) ? eventComment : messageName, "", "");
		
		String carrierState = SMessageUtil.getBodyItemValue(doc, "DURABLESTATE", false);
		String transportLockFlag = SMessageUtil.getBodyItemValue(doc, "TRANSPORTLOCKFLAG", false);
		String stockerInTime = SMessageUtil.getBodyItemValue(doc, "STOCKERINTIME", false);
		String kanban = SMessageUtil.getBodyItemValue(doc, "KANBAN", false);
		String region = SMessageUtil.getBodyItemValue(doc, "REGION", false);
		
		// Added by smkang on 2018.11.23 - DurableCleanState and DryFlag are also necessary to be updated.
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String durableCleanState = SMessageUtil.getBodyItemValue(doc, "DURABLECLEANSTATE", false);
		String dryFlag = SMessageUtil.getBodyItemValue(doc,"DURABLEDRYFLAG", false);
		
		if (StringUtils.isNotEmpty(carrierState)) {
			durableData.setDurableState(carrierState);
			durableData.setLotQuantity(carrierState.equals(GenericServiceProxy.getConstantMap().Dur_Available) ? 0 : (durableData.getLotQuantity() + 1));
			
			DurableServiceProxy.getDurableService().update(durableData);
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, new SetEventInfo());
			
			// Added by smkang on 2018.10.25 - According to EDO's request, when a carrier is changed to Available state, the carrier name of lot and product will be also deleted.
			if (carrierState.equals(GenericServiceProxy.getConstantMap().Dur_Available)) {
				try {
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					List<Lot> lotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ?", new Object[] {carrierName});
					List<Lot> lotDataList = LotServiceProxy.getLotService().select("CARRIERNAME = ? FOR UPDATE", new Object[] {carrierName});
					
					for (Lot lotData : lotDataList) {
						lotData.setCarrierName("");
						LotServiceProxy.getLotService().update(lotData);
						
						// Added by smkang on 2018.11.29 - Need to record LotHistory for Report.
						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
					}
				} catch (Exception e) {
					// TODO: handle exception
					eventLog.warn(e);
				}
				
				try {
					// Modified by smkang on 2019.06.21 - According to Liu Hongwei's request, a row data should be locked to be prevented concurrent executing.
//					List<Product> productDataList = ProductServiceProxy.getProductService().select("CARRIERNAME = ?", new Object[] {carrierName});
					List<Product> productDataList = ProductServiceProxy.getProductService().select("CARRIERNAME = ? FOR UPDATE", new Object[] {carrierName});
					
					for (Product productData : productDataList) {
						productData.setCarrierName("");
						ProductServiceProxy.getProductService().update(productData);
						
						// Added by smkang on 2018.11.29 - Need to record ProductHistory for Report.
						kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
						ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
					}
				} catch (Exception e) {
					// TODO: handle exception
					eventLog.warn(e);
				}
				
				// Added by smkang on 2018.12.31 - After a carrier is changed to Available, ReceiveFlag of the carrier needs to be changed to N.
				try {
					if (StringUtils.equals(durableData.getUdfs().get("RECEIVEFLAG"), "Y")) {
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("RECEIVEFLAG", "N");
						
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
					}
				} catch (Exception e) {
					// TODO: handle exception
					eventLog.warn(e);
				}
			}
		} else {
			if (StringUtils.isNotEmpty(transportLockFlag) || StringUtils.isNotEmpty(stockerInTime) ||
				StringUtils.isNotEmpty(kanban) || StringUtils.isNotEmpty(region)) {
				
				SetEventInfo setEventInfo = new SetEventInfo();
				
				// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//				if (StringUtils.isNotEmpty(transportLockFlag))
//					durableData.getUdfs().put("TRANSPORTLOCKFLAG", transportLockFlag);
//
//				if (StringUtils.isNotEmpty(stockerInTime))
//					durableData.getUdfs().put("STOCKERINTIME", stockerInTime);
//				
//				if (StringUtils.isNotEmpty(kanban))
//					durableData.getUdfs().put("KANBAN", kanban);
//				
//				if (StringUtils.isNotEmpty(region))
//					durableData.getUdfs().put("REGION", region);
//				
//				setEventInfo.setUdfs(durableData.getUdfs());
				Map<String, String> durableUdfs = setEventInfo.getUdfs();
				
				if (StringUtils.isNotEmpty(transportLockFlag))
					durableUdfs.put("TRANSPORTLOCKFLAG", transportLockFlag);

				if (StringUtils.isNotEmpty(stockerInTime))
					durableUdfs.put("STOCKERINTIME", stockerInTime);
				
				if (StringUtils.isNotEmpty(kanban))
					durableUdfs.put("KANBAN", kanban);
				
				if (StringUtils.isNotEmpty(region))
					durableUdfs.put("REGION", region);
				
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			} else if (StringUtils.isNotEmpty(durableCleanState) || StringUtils.isNotEmpty(dryFlag)) {
				// Added by smkang on 2018.11.23 - DurableCleanState and DryFlag are also necessary to be updated.
				if (durableData.getDurableCleanState().equals(durableCleanState)) {
					// Added by smkang on 2019.01.02 - If DryFlag of a message is empty, it is unnecessary to be updated.
					if (StringUtils.isNotEmpty(dryFlag)) {
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
	//					Map<String, String> udfs = durableData.getUdfs();
						SetEventInfo setEventInfo = new SetEventInfo();
						Map<String, String> udfs = setEventInfo.getUdfs();
						
						udfs.put("DRYFLAG", dryFlag);
						
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
					}
				} else {
					if (durableCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Clean)) {
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//						Map<String, String> udfs = durableData.getUdfs();
						CleanInfo cleanInfo = new CleanInfo();
						Map<String, String> udfs = cleanInfo.getUdfs();
						
						udfs.put("MACHINENAME", machineName);
						udfs.put("LASTCLEANTIME", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						
						// Added by smkang on 2019.01.02 - If DryFlag of a message is empty, it is unnecessary to be updated.
						if (StringUtils.isNotEmpty(dryFlag))
							udfs.put("DRYFLAG", dryFlag);
						
						MESDurableServiceProxy.getDurableServiceImpl().clean(durableData, cleanInfo, eventInfo);
					} else if (durableCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Dirty)) {
						// Modified by smkang on 2018.12.30 - For avoid update of udfs is wrong.
//						Map<String, String> udfs = durableData.getUdfs();
						DirtyInfo dirtyInfo = new DirtyInfo();
						Map<String, String> udfs = dirtyInfo.getUdfs();
						
						// Added by smkang on 2019.01.02 - If DryFlag of a message is empty, it is unnecessary to be updated.
						if (StringUtils.isNotEmpty(dryFlag))
							udfs.put("DRYFLAG", dryFlag);
						
						MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
					}
				}
			} else {
				eventLog.info("Any UDFS aren't changed, so durable isn't updated.");
			}			
		}
	}
}