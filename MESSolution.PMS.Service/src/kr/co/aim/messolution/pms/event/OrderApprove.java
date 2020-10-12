package kr.co.aim.messolution.pms.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.RequestSparePart;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.messolution.pms.management.data.SparePartInOut;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class OrderApprove extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
			
		String approveUser  = SMessageUtil.getBodyItemValue(doc, "APPROVEUSER", true);
		String approveDate  = SMessageUtil.getBodyItemValue(doc, "APPROVEDATE", true);
		String orderStatus = SMessageUtil.getBodyItemValue(doc, "ORDERSTATUS", true);
		
		List<Element> OrderList = SMessageUtil.getBodySequenceItemList(doc, "ORDERLIST", true);
		
		for(Element order:OrderList)
		{
			EventInfo eventInfo  = EventInfoUtil.makeEventInfo("OrderApprove", getEventUser(), getEventComment(), null, null);
			
			String orderNo = SMessageUtil.getChildText(order, "ORDERNO", true);
			String UseType = SMessageUtil.getChildText(order, "USETYPE", true);		
			String PartID = SMessageUtil.getChildText(order, "PARTID", true);
			String orderQty = SMessageUtil.getChildText(order, "ORDERQUANTITY", true);
					
			if(UseType.equals("Provide")) //Provide
			{
				//////////////////////////////////////////////////////////////////////////////////////////////////////
				String requestId    = SMessageUtil.getChildText(order, "REQUESTID", true);
				String requestState = SMessageUtil.getChildText(order, "REQUESTSTATE", true);

				String AvailableOrderQty = "0";
				int ResultAvailableQty   = 0;
				
				//Get AvailableOrderQty
				List<SparePartInOut> SparePartInOutList = null;
				try
				{
					SparePartInOutList = PMSServiceProxy.getSparePartInOutService().select("PARTID = ? AND AVAILABLEORDERQTY IS NOT NULL", new Object[] {PartID});
				}
				catch (Exception ex)
				{
					eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
					SparePartInOutList = new ArrayList<SparePartInOut>();
				}
				for (SparePartInOut sparePartInOut : SparePartInOutList)
				{
					AvailableOrderQty = sparePartInOut.getAvailableOrderQty().toString();
		            break;
				}
				
				// Step 1
				// SparePartInOut modify
				try
				{
					//Get
					SparePartInOut orderData = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[] {orderNo,PartID});
					
					String orderType    = orderData.getOrderType();
					String useType      = orderData.getUseType();
					String orderReason  = orderData.getOrderReason();
					String orderUser    = orderData.getOrderUser();
					String orderDate    = orderData.getOrderDate().toString();
					String partID       = orderData.getPartID();
		
					int OrderQty = Integer.parseInt(orderQty);
					int AvailableQty = Integer.parseInt(AvailableOrderQty);
					
					ResultAvailableQty = AvailableQty + OrderQty;  //This Quantity can be used in the Execute BM.
					
					//Set
					orderData = new SparePartInOut(orderNo,partID);
					orderData.setOrderNo(orderNo);
					orderData.setOrderType(orderType);
					orderData.setUseType(useType);
					orderData.setOrderStatus(orderStatus);
					orderData.setOrderQuantity(OrderQty);
					orderData.setOrderReason(orderReason);
					orderData.setRequestID(requestId);
					orderData.setOrderUser(orderUser);
					orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
					orderData.setPartID(partID);
					orderData.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
					orderData.setApproveUser(approveUser);
					orderData.setAvailableOrderQty(ResultAvailableQty);
					
					orderData = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, orderData);
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePartInOut modify  >>>>>>>>>>>>>>>>>>>>>>"));
					
					//Sync Available Quantity
					for (SparePartInOut sparePartInOut : SparePartInOutList)
					{
						String orderId = "";
						try
						{
							//Get
							orderId          = sparePartInOut.getOrderNo();
							String requestID = sparePartInOut.getRequestID();
							orderType        = sparePartInOut.getOrderType();
							orderReason      = sparePartInOut.getOrderReason();
							orderStatus      = sparePartInOut.getOrderStatus();
							orderUser        = sparePartInOut.getOrderUser();
							orderDate        = sparePartInOut.getOrderDate().toString();
							approveUser      = sparePartInOut.getApproveUser();
							approveDate      = sparePartInOut.getApproveDate().toString();
							partID           = sparePartInOut.getPartID();
							String sOrderQty = sparePartInOut.getOrderQuantity().toString();
							useType          = sparePartInOut.getUseType();
							
							
							//Set 
							sparePartInOut = new SparePartInOut(orderNo,partID);
							
							sparePartInOut.setOrderNo(orderId);
							sparePartInOut.setOrderType(orderType);
							sparePartInOut.setOrderReason(orderReason);
							sparePartInOut.setOrderStatus(orderStatus);
							sparePartInOut.setOrderUser(orderUser);
							sparePartInOut.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
							sparePartInOut.setApproveUser(approveUser);
							sparePartInOut.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
							sparePartInOut.setPartID(partID);
							sparePartInOut.setOrderQuantity(Integer.parseInt(sOrderQty));
							sparePartInOut.setAvailableOrderQty(ResultAvailableQty);
							sparePartInOut.setRequestID(requestID);
							sparePartInOut.setUseType(useType);
							
							eventInfo.setEventName("IncreaseByUnstoring"); 
							
							sparePartInOut = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, sparePartInOut);
							eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePartInOut modify (%s, %s, %d) >>>>>>>>>>>>>>>>>>>>>>",orderId, partID, ResultAvailableQty));
						}
						catch(Exception ex)
						{
							throw new CustomException("PMS-0055", orderId);
						}
					}
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0055", orderNo);
				}
				
				// Step 2
				// RequestSparePart modify
				try
				{
					//Get
					RequestSparePart requestData = PMSServiceProxy.getRequestSparePartService().selectByKey(true, new Object[] {requestId, PartID});
					String partId       = requestData.getPartId();
					String RequestState = requestState;
					String RequestQty   = requestData.getRequestQuantity();
					String Desc         = requestData.getDescription();
					String orderName    = requestData.getOrderNo();
					
					//Set
					requestData = new RequestSparePart(requestId,partId);
					requestData.setRequestId(requestId);
					requestData.setPartId(partId);
					requestData.setRequestState(RequestState);
					requestData.setRequestQuantity(RequestQty);
					requestData.setDescription(Desc);
					requestData.setOrderNo(orderName);
					
					eventInfo.setEventName("orderApprove");
		
					requestData = PMSServiceProxy.getRequestSparePartService().modify(eventInfo, requestData);
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success RequestSparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
					
				}
				catch (Exception ex)
				{
					throw new CustomException("PMS-0056", requestId);
				}
				
				// Step 3
				// SparePart modify
				try
				{
					//Get
					SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
				
					String partName            = sparePartData.getPartName();
					String groupName           = sparePartData.getGroupName();
					String partSpec            = sparePartData.getPartSpec(); 
					String location            = sparePartData.getLocation(); 
					String quantity            = sparePartData.getQuantity().toString(); 
					Number safeQuantity        = sparePartData.getSafeQuantity(); 
					Number notInQuantity       = sparePartData.getNotInQuantity(); 
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
					Number warningQty          = sparePartData.getWarningQuantity();
					String materialCode        = sparePartData.getMaterialCode();
					
					int currentQty  = Integer.parseInt(quantity);
					int decreaseQty = currentQty - Integer.parseInt(orderQty);
					 
					//Set
					sparePartData = new SparePart(PartID);
					sparePartData.setPartID(PartID);
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
					sparePartData.setPartDescription(partDesc);
					sparePartData.setUseDescription(useDesc);
					sparePartData.setPartType(partType);
					sparePartData.setPartAttribute(partAttribute);
					sparePartData.setPartGroup(partGroup);
					sparePartData.setOrderQuantity(ResultAvailableQty);
					sparePartData.setWarningQuantity(warningQty);
					sparePartData.setMaterialCode(materialCode);
					
					eventInfo.setEventName("decreaseByUnstoring");
		
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
					
				}
				catch (Exception ex)
				{
					throw new CustomException("PMS-0057", PartID);
				}
			}
			else if(UseType.equals("Warehousing"))//Warehousing
			{
				//////////////////////////////////////////////////////////////////////////////////////////////////////
				// Step 1
				// SparePartInOut modify
				try
				{
					//Get
					SparePartInOut orderData = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[] {orderNo,PartID});
							
					String orderType    = orderData.getOrderType();
					String useType      = orderData.getUseType();
					String orderReason  = orderData.getOrderReason();
					String orderUser    = orderData.getOrderUser();
					String orderDate    = orderData.getOrderDate().toString();
					String partID       = orderData.getPartID();
					String requestID    = orderData.getRequestID();
					//String availableQty = orderData.getAvailableOrderQty().toString();
								
					int OrderQty     = Integer.parseInt(orderQty);
					//int AvailableQty = Integer.parseInt(availableQty);

					//Set 
					orderData = new SparePartInOut(orderNo,partID);
					orderData.setOrderNo(orderNo);
					orderData.setOrderType(orderType);
					orderData.setUseType(useType);
					orderData.setOrderStatus(orderStatus);
					orderData.setOrderQuantity(OrderQty);
					orderData.setOrderReason(orderReason);
					orderData.setRequestID(requestID);
					orderData.setOrderUser(orderUser);
					orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
					orderData.setPartID(partID);
					orderData.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
					orderData.setApproveUser(approveUser);
					//orderData.setAvailableOrderQty(AvailableQty);
											
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
					SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
								
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
					Number warningQty          = sparePartData.getWarningQuantity();
					String materialCode        = sparePartData.getMaterialCode();
					
					int currentQty  = Integer.parseInt(quantity);
					int increaseQty = currentQty + Integer.parseInt(orderQty);
											 
					//Set
					sparePartData = new SparePart(PartID);
					sparePartData.setPartID(PartID);
					sparePartData.setPartName(partName);
					sparePartData.setGroupName(groupName);
					sparePartData.setPartSpec(partSpec);
					sparePartData.setLocation(location);
					sparePartData.setQuantity(increaseQty);
					sparePartData.setSafeQuantity(safeQuantity);
					sparePartData.setNotInQuantity(notInQuantity);
					sparePartData.setPurchaseCompleteQty(purchaseCompleteQty);
					sparePartData.setVendorID(vendorId);
					sparePartData.setUnit(unit);
					sparePartData.setUnitPrice(unitPrice);
					sparePartData.setUseFrequency(useFrequency);
					sparePartData.setPurchaseCycle(purchaseCycle);
					sparePartData.setPartDescription(partDesc);
					sparePartData.setUseDescription(useDesc);
					sparePartData.setPartType(partType);
					sparePartData.setPartAttribute(partAttribute);
					sparePartData.setPartGroup(partGroup);
					sparePartData.setOrderQuantity(orderQuantity);
					sparePartData.setWarningQuantity(warningQty);
					sparePartData.setMaterialCode(materialCode);						
					
					eventInfo.setEventName("IncreaseByEtcWarehousing");
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
									
				}
				catch (Exception ex)
				{
					throw new CustomException("PMS-0057", PartID);
				}
			}
			else
			{
				//////////////////////////////////////////////////////////////////////////////////////////////////////
				// Step 1
				// SparePartInOut modify
				try
				{
					//Get
					SparePartInOut orderData = PMSServiceProxy.getSparePartInOutService().selectByKey(true, new Object[] {orderNo,PartID});
					
					String orderType    = orderData.getOrderType();
					String useType      = orderData.getUseType();
					String orderReason  = orderData.getOrderReason();
					String orderUser    = orderData.getOrderUser();
					String orderDate    = orderData.getOrderDate().toString();
					String partID       = orderData.getPartID();
					String requestID    = orderData.getRequestID();
					//String availableQty = orderData.getAvailableOrderQty().toString();
					
					int OrderQty     = Integer.parseInt(orderQty);
					//int AvailableQty = Integer.parseInt(availableQty);

					//Set 
					orderData = new SparePartInOut(orderNo,partID);
					orderData.setOrderNo(orderNo);
					orderData.setOrderType(orderType);
					orderData.setUseType(useType);
					orderData.setOrderStatus(orderStatus);
					orderData.setOrderQuantity(OrderQty);
					orderData.setOrderReason(orderReason);
					orderData.setRequestID(requestID);
					orderData.setOrderUser(orderUser);
					orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
					orderData.setPartID(partID);
					orderData.setApproveDate(TimeStampUtil.getTimestamp(approveDate));
					orderData.setApproveUser(approveUser);
					//orderData.setAvailableOrderQty(AvailableQty);
								
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
					SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
					
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
					Number warningQty          = sparePartData.getWarningQuantity();
					String materialCode        = sparePartData.getMaterialCode();			
					
					int currentQty  = Integer.parseInt(quantity);
					int decreaseQty = currentQty - Integer.parseInt(orderQty);
								 					
					//Set
					sparePartData = new SparePart(PartID);
					sparePartData.setPartID(PartID);
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
					sparePartData.setPartDescription(partDesc);
					sparePartData.setUseDescription(useDesc);
					sparePartData.setPartType(partType);
					sparePartData.setPartAttribute(partAttribute);
					sparePartData.setPartGroup(partGroup);
					sparePartData.setOrderQuantity(orderQuantity);
					sparePartData.setWarningQuantity(warningQty);
					sparePartData.setMaterialCode(materialCode);			
					
					eventInfo.setEventName("decreaseByEtcUnstoring");
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
								
				}
				catch (Exception ex)
				{
					throw new CustomException("PMS-0057", PartID);
				}
			}
			
		}	
		return doc;
	}
}
