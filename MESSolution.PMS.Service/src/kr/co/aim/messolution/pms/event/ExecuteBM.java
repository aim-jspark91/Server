package kr.co.aim.messolution.pms.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BM;
import kr.co.aim.messolution.pms.management.data.BMUsePart;
import kr.co.aim.messolution.pms.management.data.BMUser;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ExecuteBM extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String BMName 	  = SMessageUtil.getBodyItemValue(doc, "BMNAME", true);
		String BMCause    = SMessageUtil.getBodyItemValue(doc, "BMCAUSE", true);
		String BMSolution = SMessageUtil.getBodyItemValue(doc, "BMSOLUTION", true);
		String RepairTime = SMessageUtil.getBodyItemValue(doc, "REPAIRTIME", true);

		Element userE = SMessageUtil.getBodySequenceItem(doc, "USERLIST", true);
		Element partE = SMessageUtil.getBodySequenceItem(doc, "PARTLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ExecuteBM", getEventUser(), getEventComment(), null, null);
		
		BM bmData = null;
		try{
			
			bmData = PMSServiceProxy.getBMService().selectByKey(true, new Object[] {BMName});
			bmData.setBmCause(BMCause);
			bmData.setBmSolution(BMSolution);
			bmData.setBmState("Executed");
			bmData.setLastEventName(eventInfo.getEventName());
			bmData.setLastEventTime(eventInfo.getEventTime()); 
			bmData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			bmData.setRepairTime(TimeStampUtil.getTimestamp(RepairTime));

			bmData = PMSServiceProxy.getBMService().modify(eventInfo, bmData);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0064", BMName);
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////
		//Set User Info 
		BMUser userData = null;
		if (userE != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorUserList = userE.getChildren().iterator(); iteratorUserList.hasNext();)
			{
				Element userInfo = (Element) iteratorUserList.next();
				
				String BMUserName = SMessageUtil.getChildText(userInfo, "USERNAME", true);
				String BMUserType = "Executed";
				
				userData = new BMUser(BMUserName, BMName, BMUserType);
				userData.setBmID(BMName);
				userData.setBmUser(BMUserName);
				userData.setUserType(BMUserType);
				userData.setTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeUtils.getCurrentEventTimeKey():eventInfo.getEventTimeKey());
				eventInfo.setEventName("");
				
				try
				{
					userData = PMSServiceProxy.getBMUserService().create(eventInfo, userData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0065", BMName);
				}
			}
		}
		
		if (partE != null)
		{
			for ( @SuppressWarnings("rawtypes")
			Iterator iteratorPartList = partE.getChildren().iterator(); iteratorPartList.hasNext();)
			{
				Element partInfo = (Element) iteratorPartList.next();
				
				String PartID     = SMessageUtil.getChildText(partInfo, "PARTID", true);
				String UsePartQty = SMessageUtil.getChildText(partInfo, "USEQUANTITY", true);
				
				long useQuantity = Long.parseLong(UsePartQty);
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				//Set UsePart Info 
				BMUsePart usePart = null;
				try
				{
					usePart = PMSServiceProxy.getBMUsePartService().selectByKey(true, new Object[] {BMName, PartID});
				}catch (Exception ex)
				{
					eventLog.error(String.format( "Select PMS_BM Fail [ BMName = %s , PartID = %s] ", BMName, PartID));
				}
				
				if( usePart == null )
				{
					usePart = new BMUsePart(BMName, PartID);
					usePart.setBmID(BMName);
					usePart.setPartID(PartID);
					usePart.setUseQuantity(useQuantity);
					usePart.setEventTime(eventInfo.getEventTime());
					//usePart.setOrderID(orderName);
					eventInfo.setEventName("");
					
					usePart = PMSServiceProxy.getBMUsePartService().create(eventInfo, usePart);
				}
				else
				{
					usePart = new BMUsePart(BMName, PartID);
					usePart.setBmID(BMName);
					usePart.setPartID(PartID);
					usePart.setUseQuantity(useQuantity);
					usePart.setEventTime(eventInfo.getEventTime());
					//usePart.setOrderID(orderName);
					eventInfo.setEventName("");
					
					usePart = PMSServiceProxy.getBMUsePartService().modify(eventInfo, usePart);
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////
				//Modify SPAREPARTINOUT (AvailableOrderQuantity decrease)
				List<SparePartInOut> SparePartInOutList = null;
				try
				{
					SparePartInOutList = 
							PMSServiceProxy.getSparePartInOutService().
								select("PARTID = ? AND AVAILABLEORDERQTY IS NOT NULL", new Object[] {PartID});
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
					SparePartInOutList = new ArrayList<SparePartInOut>();
				}
		
				for (SparePartInOut sparePartInOut : SparePartInOutList)
				{
					try{
						//Get ResultQuantity
						String sAvailableOrderQty = sparePartInOut.getAvailableOrderQty().toString(); 
						String sOrderQuantity   = sparePartInOut.getOrderQuantity().toString();
						
						long availableOrderQty = Long.parseLong(sAvailableOrderQty);
						long orderQuantity   = Long.parseLong(sOrderQuantity);
						long decreaseResult  = availableOrderQty - useQuantity ;
		
						String orderNo   = sparePartInOut.getOrderNo();
						String orderType = sparePartInOut.getOrderType();
						String orderReason = sparePartInOut.getOrderReason();
						String orderStatus = sparePartInOut.getOrderStatus();
						String orderUser   = sparePartInOut.getOrderUser();
						String orderDate = sparePartInOut.getOrderDate().toString();
						String approveUser = sparePartInOut.getApproveUser();
						String approveDate = sparePartInOut.getApproveDate().toString();
						String partID = sparePartInOut.getPartID();
						String requestID = sparePartInOut.getRequestID();
		
						//Set 
						sparePartInOut = new SparePartInOut(orderNo,partID);
						
						sparePartInOut.setOrderNo(orderNo);
						sparePartInOut.setOrderType(orderType);
						sparePartInOut.setOrderReason(orderReason);
						sparePartInOut.setOrderStatus(orderStatus);
						sparePartInOut.setOrderUser(orderUser);
						sparePartInOut.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
						sparePartInOut.setApproveUser(approveUser);
						sparePartInOut.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
						sparePartInOut.setPartID(partID);
						sparePartInOut.setOrderQuantity(orderQuantity);
						sparePartInOut.setAvailableOrderQty(decreaseResult);
						sparePartInOut.setRequestID(requestID);
						
						eventInfo.setEventName("decreaseByBM"); 
						
						sparePartInOut = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, sparePartInOut);
						
					}catch (Exception ex)
					{
						throw new CustomException("PMS-0064", BMName);
					}
				}	
			}
		}
		else 
		{
			eventLog.error(String.format( "<<<<<<<<<<<<<<<<<<<<<<<  Part List is null >>>>>>>>>>>>>>>>>>>>>>"));
		}

		return doc;
	}
}