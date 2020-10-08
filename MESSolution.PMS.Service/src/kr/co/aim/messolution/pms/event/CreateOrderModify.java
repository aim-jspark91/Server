package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateOrderModify extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String OrderNo 	 = SMessageUtil.getBodyItemValue(doc, "ORDERNO", true);
		String RequestID = SMessageUtil.getBodyItemValue(doc, "REQUESTID", false);
		String PartID    = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String OrderQty  = SMessageUtil.getBodyItemValue(doc, "ORDERQUANTITY", true);
		String OrderUser = SMessageUtil.getBodyItemValue(doc, "ORDERUSER", true);
		String UseType   = SMessageUtil.getBodyItemValue(doc, "USETYPE", true);
		
		int nOrderQty = Integer.parseInt(OrderQty);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreateOrderModify", getEventUser(), getEventComment(), null, null);
		
		SparePartInOut SparePartInOutDataInfo = null;
		try
		{
		  SparePartInOutDataInfo = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[]{OrderNo, PartID});
		}catch(Exception ex)
		{
			throw new CustomException("PMS-0055", OrderNo);
		}
		//get
		String OrderType         = SparePartInOutDataInfo.getOrderType();
		String OrderReason       = SparePartInOutDataInfo.getOrderReason();
		String OrderStatus       = SparePartInOutDataInfo.getOrderStatus();
		Timestamp OrderDate      = SparePartInOutDataInfo.getOrderDate();
		String ApproveUser       = SparePartInOutDataInfo.getApproveUser();
		Timestamp ApproveDate    = SparePartInOutDataInfo.getApproveDate();
		Number AvailableOrderQty = SparePartInOutDataInfo.getAvailableOrderQty();
		
		//set
		SparePartInOutDataInfo = new SparePartInOut(OrderNo,PartID);
		SparePartInOutDataInfo.setOrderNo(OrderNo);
		SparePartInOutDataInfo.setOrderType(OrderType);
		SparePartInOutDataInfo.setOrderReason(OrderReason);
		SparePartInOutDataInfo.setOrderStatus(OrderStatus);
		SparePartInOutDataInfo.setOrderUser(OrderUser);
		SparePartInOutDataInfo.setOrderDate(OrderDate);
		SparePartInOutDataInfo.setApproveUser(ApproveUser);
		SparePartInOutDataInfo.setApproveDate(ApproveDate);
		SparePartInOutDataInfo.setPartID(PartID);
		SparePartInOutDataInfo.setOrderQuantity(nOrderQty);
		SparePartInOutDataInfo.setRequestID(RequestID);
		SparePartInOutDataInfo.setUseType(UseType);
		SparePartInOutDataInfo.setAvailableOrderQty(AvailableOrderQty);
		
		try
		{
			SparePartInOutDataInfo = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, SparePartInOutDataInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0055", OrderNo);
		}
		
		return doc;
	}
}