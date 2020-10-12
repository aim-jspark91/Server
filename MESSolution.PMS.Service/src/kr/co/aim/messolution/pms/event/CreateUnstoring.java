package kr.co.aim.messolution.pms.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateUnstoring extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String orderNo 	   = SMessageUtil.getBodyItemValue(doc, "ORDERNO", true);
		String orderType   = SMessageUtil.getBodyItemValue(doc, "ORDERTYPE", true);
		String useType     = SMessageUtil.getBodyItemValue(doc, "USETYPE", true);
		String orderStatus = SMessageUtil.getBodyItemValue(doc, "ORDERSTATUS", true);		
		String orderReason = SMessageUtil.getBodyItemValue(doc, "ORDERREASON", false);
		String requestId   = SMessageUtil.getBodyItemValue(doc, "REQUESTID", false);
		String orderUser   = SMessageUtil.getBodyItemValue(doc, "ORDERUSER", true);
		String orderDate   = SMessageUtil.getBodyItemValue(doc, "ORDERDATE", true);	
		String desc        = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
	
		List<Element> OrderList = SMessageUtil.getBodySequenceItemList(doc, "ORDERLIST", true);
				
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("Created", getEventUser(), getEventComment(), null, null);
		
		String orderNo = this.createOrderNo();
		
		for(Element order : OrderList)
		{
			String partID      = SMessageUtil.getChildText(order, "PARTID", true);
			String orderQty    = SMessageUtil.getChildText(order, "ORDERQUANTITY", true);	
			
			SparePartInOut orderData = new SparePartInOut(orderNo,partID);
			orderData.setOrderNo(orderNo);
			orderData.setOrderType(orderType);
			orderData.setUseType(useType);
			orderData.setOrderStatus(orderStatus);
			orderData.setOrderQuantity(Integer.parseInt(orderQty));
			orderData.setOrderReason(orderReason);
			orderData.setRequestID(requestId);
			orderData.setOrderUser(orderUser);
			orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
			orderData.setPartID(partID);
			orderData.setDescription(desc);
			
			try
			{
				orderData = PMSServiceProxy.getSparePartInOutService().create(eventInfo, orderData);
				eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePartInOut create  >>>>>>>>>>>>>>>>>>>>>>"));
			}
			catch(Exception ex)
			{
				throw new CustomException("PMS-0058", orderNo);
			}
						
			if(useType.equals("Sample")) //Sample
			{
				// Step 1
				// SparePartInOut modify
				try
				{
					//Get
					orderData = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[] {orderNo,partID});
					
					orderType    = orderData.getOrderType();
					useType      = orderData.getUseType();
					orderReason  = orderData.getOrderReason();
					orderUser    = orderData.getOrderUser();
					orderDate    = orderData.getOrderDate().toString();
					partID       = orderData.getPartID();
					requestId    = orderData.getRequestID();
					orderStatus  = "Completed";
					desc         = orderData.getDescription();
					//String availableQty = orderData.getAvailableOrderQty().toString();
								
					int OrderQty = Integer.parseInt(orderQty);
					//int AvailableQty = Integer.parseInt(availableQty);
					//Set 
					orderData = new SparePartInOut(orderNo,partID);
					orderData.setOrderNo(orderNo);
					orderData.setOrderType(orderType);
					orderData.setUseType(useType);
					orderData.setOrderStatus(orderStatus);
					orderData.setOrderQuantity(OrderQty);
					orderData.setOrderReason(orderReason);
					//orderData.setRequestID(requestID);
					orderData.setOrderUser(orderUser);
					orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
					orderData.setPartID(partID);
					orderData.setDescription(desc);
									
					eventInfo.setEventName("Sample");
					orderData = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, orderData);
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePartInOut modify  >>>>>>>>>>>>>>>>>>>>>>"));
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0055", orderNo);
				}
				// Step 2
				// SparePart modify
				try
				{
					//Get
					SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {partID});
						
					String partName            = sparePartData.getPartName();
					String groupName           = sparePartData.getGroupName();
					String partSpec            = sparePartData.getPartSpec(); 
					String location            = sparePartData.getLocation(); 
					String quantity            = sparePartData.getQuantity().toString(); 
					Number safeQuantity        = sparePartData.getSafeQuantity(); 
					Number notInQuantity 	   = sparePartData.getNotInQuantity(); 
					Number purchaseCompleteQty = sparePartData.getPurchaseCompleteQty(); 
					String vendorId            = sparePartData.getVendorID(); 
					String unit                = sparePartData.getUnit(); 
					Number unitPrice           = sparePartData.getUnitPrice(); 
					Number useFrequency        = sparePartData.getUseFrequency(); 
					String purchaseCycle       = sparePartData.getPurchaseCycle(); 
					String partDesc            = sparePartData.getPartDescription(); 
					String useDesc             = sparePartData.getUseDescription(); 
					String partType            = sparePartData.getPartType(); 
					String partAttribute       = sparePartData.getPartAttribute(); 
					String partGroup           = sparePartData.getPartGroup(); 
					Number orderQuantity       = sparePartData.getOrderQuantity();
					Number warningQuantity     = sparePartData.getWarningQuantity();
					String materialCode        = sparePartData.getMaterialCode();
											
					int currentQty  = Integer.parseInt(quantity);
					int decreaseQty = currentQty - Integer.parseInt(orderQty);
											 
					//Set
					sparePartData = new SparePart(partID);
					sparePartData.setPartID(partID);
					sparePartData.setPartName(partName);
					sparePartData.setGroupName(groupName);
					sparePartData.setPartSpec(partSpec);
					sparePartData.setLocation(location);
					sparePartData.setQuantity(decreaseQty);
					sparePartData.setSafeQuantity(safeQuantity);
					sparePartData.setNotInQuantity(notInQuantity);
					sparePartData.setPurchaseCompleteQty(purchaseCompleteQty);
					sparePartData.setVendorID(vendorId);
					sparePartData.setUnit(unit);
					sparePartData.setUnitPrice(unitPrice);
					sparePartData.setUseFrequency(useFrequency);
					sparePartData.setPurchaseCycle(purchaseCycle);
					//sparePartData.setPurchaseCycle(Integer.parseInt(purchaseCycle));
					sparePartData.setPartDescription(partDesc);
					sparePartData.setUseDescription(useDesc);
					sparePartData.setPartType(partType);
					sparePartData.setPartAttribute(partAttribute);
					sparePartData.setPartGroup(partGroup);
					sparePartData.setOrderQuantity(orderQuantity);
					sparePartData.setWarningQuantity(warningQuantity);
					sparePartData.setMaterialCode(materialCode);
											
					eventInfo.setEventName("decreaseByOrderEtcUnstoring");
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
											
				}	
				catch (Exception ex)
				{
					throw new CustomException("PMS-0057", partID);
				}
			}
			
			if( requestId != null && !requestId.equals(""))
			{
				try
				{
					//Get
					RequestSparePart requestData = PMSServiceProxy.getRequestSparePartService().selectByKey(true, new Object[] {requestId,partID});
					String partId       = requestData.getPartId();
					String RequestState = "Order";
					String RequestQty   = requestData.getRequestQuantity();
					String Desc         = requestData.getDescription();
					String RequestType  = requestData.getRequestType();
					
					//Set
					requestData = new RequestSparePart(requestId,partId);
					requestData.setRequestId(requestId);
					requestData.setPartId(partId);
					requestData.setRequestState(RequestState);
					requestData.setRequestQuantity(RequestQty);
					requestData.setDescription(Desc);
					requestData.setOrderNo(orderNo);
					requestData.setRequestType(RequestType);
					
					eventInfo.setEventName("Unstoring");
		
					requestData = PMSServiceProxy.getRequestSparePartService().modify(eventInfo, requestData);
					
				}catch (Exception ex)
				{
					throw new CustomException("PMS-0056", requestId);
				}
			}
		}		


		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "ORDERID", orderNo);
		return rtnDoc;
	}
	
	
	public String createOrderNo()  throws CustomException
	{
		String newOrderNo = "";
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String OrderDate = currentDate.substring(0, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("ORDER", "PMS");
		nameRuleAttrMap.put("ORDERDATE", OrderDate);
		nameRuleAttrMap.put("HYPHEN", "-");
		
		//LotID Generate
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("OrderNaming", nameRuleAttrMap, createQty);
			newOrderNo = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}
		
		return newOrderNo;
	}
}