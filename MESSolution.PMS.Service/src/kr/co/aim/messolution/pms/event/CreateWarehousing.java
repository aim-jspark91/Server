package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;
import java.util.ArrayList;
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

public class CreateWarehousing extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String orderNo 	   = SMessageUtil.getBodyItemValue(doc, "ORDERNO", true);
		String orderStatus = SMessageUtil.getBodyItemValue(doc, "ORDERSTATUS", true);
		String requestId   = SMessageUtil.getBodyItemValue(doc, "REQUESTID", false); 
		String orderUser   = SMessageUtil.getBodyItemValue(doc, "ORDERUSER", true);
		String orderDate   = SMessageUtil.getBodyItemValue(doc, "ORDERDATE", true);
		//String PartID      = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		//String orderQty    = SMessageUtil.getBodyItemValue(doc, "ORDERQTY", true);
		String orderType   = SMessageUtil.getBodyItemValue(doc, "ORDERTYPE", true);
		String useType     = SMessageUtil.getBodyItemValue(doc, "USETYPE", true);
		String orderReason = SMessageUtil.getBodyItemValue(doc, "ORDERREASON", false);
		
		List<Element> partInfoList = SMessageUtil.getBodySequenceItemList(doc, "PARTINFOLIST", true);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("Created", getEventUser(), getEventComment(), null, null);

		String orderNo = this.createOrderNo();
		
		List<SparePartInOut> SparePartInOutList = null;
		
		for(Element partInfo : partInfoList)
		{
			String PartID      = SMessageUtil.getChildText(partInfo, "PARTID", true);
			String orderQty    = SMessageUtil.getChildText(partInfo, "ORDERQUANTITY", true);	
			
			if(useType.equals("SendBack"))
			{
				// Step 1
				// SparePartInOut(ORDER) Create 
				SparePartInOut orderData = new SparePartInOut(orderNo,PartID);
				orderData.setOrderNo(orderNo);
				orderData.setOrderType(orderType);
				orderData.setUseType(useType);
				orderData.setOrderStatus(orderStatus);
				orderData.setOrderQuantity(Integer.parseInt(orderQty));
				orderData.setOrderReason(orderReason);
				orderData.setRequestID(requestId);
				orderData.setOrderUser(orderUser);
				orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
				orderData.setPartID(PartID);
		
				try
				{
					orderData = PMSServiceProxy.getSparePartInOutService().create(eventInfo, orderData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0058", orderNo);
				}
				

				String AvailableOrderQty = "0";
				int ResultAvailableQty = 0;
				
				//Get AvailableOrderQty
				try
				{
					SparePartInOutList = PMSServiceProxy.getSparePartInOutService().select
							("PARTID = ? AND AVAILABLEORDERQTY IS NOT NULL ", new Object[] {PartID});
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
		
				//Sync Available Quantity
				for (SparePartInOut sparePartInOut : SparePartInOutList)
				{
					String orderId = "";
					try
					{
						orderId            = sparePartInOut.getOrderNo();
						orderType          = sparePartInOut.getOrderType();
						orderReason        = sparePartInOut.getOrderReason();
						orderStatus        = sparePartInOut.getOrderStatus();
						orderUser          = sparePartInOut.getOrderUser();
						orderDate          = sparePartInOut.getOrderDate().toString();
						String partID      = sparePartInOut.getPartID();
						String requestID   = sparePartInOut.getRequestID();
						String approveUser = sparePartInOut.getApproveUser();
						Timestamp approveDate = sparePartInOut.getApproveDate();
						//String approveDate = sparePartInOut.getApproveDate().toString();
						String sOrderQty   = sparePartInOut.getOrderQuantity().toString();
						String sUseType    = sparePartInOut.getUseType();
			
						//Set 
						sparePartInOut = new SparePartInOut(orderNo,partID);
						
						sparePartInOut.setOrderNo(orderId);
						sparePartInOut.setOrderType(orderType);
						sparePartInOut.setOrderReason(orderReason);
						sparePartInOut.setOrderStatus(orderStatus);
						sparePartInOut.setOrderUser(orderUser);
						sparePartInOut.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
						sparePartInOut.setApproveUser(approveUser);
						sparePartInOut.setApproveDate(approveDate);
						sparePartInOut.setPartID(partID);
						sparePartInOut.setOrderQuantity(Integer.parseInt(sOrderQty));
						sparePartInOut.setRequestID(requestID);
						sparePartInOut.setUseType(sUseType);
						
						int OrderQty = Integer.parseInt(orderQty);
						int AvailableQty = Integer.parseInt(AvailableOrderQty);
						ResultAvailableQty = AvailableQty - OrderQty;
						sparePartInOut.setAvailableOrderQty(ResultAvailableQty);
						
						eventInfo.setEventName("DecreaseQtyBySendBack"); 
						
						sparePartInOut = PMSServiceProxy.getSparePartInOutService().modify(eventInfo, sparePartInOut);
						eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePartInOut modify (%s, %s, %d) >>>>>>>>>>>>>>>>>>>>>>",orderId, partID, ResultAvailableQty));
					}
					catch(Exception ex)
					{
						throw new CustomException("PMS-0055", orderId);
					}
				}
				
				// Step 2
				// RequestSparePart modify
				try
				{
					//Get
					RequestSparePart requestData = 
							PMSServiceProxy.getRequestSparePartService().selectByKey(true, new Object[] {requestId,PartID});
					
					String partId       = requestData.getPartId();
					String RequestState = "Completed";
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
							
					eventInfo.setEventName("Warehousing");
		
					requestData = PMSServiceProxy.getRequestSparePartService().modify(eventInfo, requestData);
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success RequestSparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
							
				}catch (Exception ex)
				{
					throw new CustomException("PMS-0056", requestId);
				}
							
				// Step 3
				// SparePart modify
				try
				{
					//Get
					SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
					
					String partName = sparePartData.getPartName();
					String groupName = sparePartData.getGroupName();
					String partSpec = sparePartData.getPartSpec(); 
					String location = sparePartData.getLocation(); 
					String quantity = sparePartData.getQuantity().toString(); 
					Number safeQuantity = sparePartData.getSafeQuantity(); 
					Number notInQuantity = sparePartData.getNotInQuantity(); 
					Number purchaseCompleteQty = sparePartData.getPurchaseCompleteQty(); 
					String vendorId = sparePartData.getVendorID(); 
					String unit = sparePartData.getUnit(); 
					Number unitPrice = sparePartData.getUnitPrice(); 
					Number useFrequency = sparePartData.getUseFrequency(); 
					String purchaseCycle = sparePartData.getPurchaseCycle(); 
					String partDesc = sparePartData.getPartDescription(); 
					String useDesc = sparePartData.getUseDescription(); 
					String partType = sparePartData.getPartType(); 
					String partAttribute = sparePartData.getPartAttribute(); 
					String partGroup = sparePartData.getPartGroup(); 
					Number warningQty = sparePartData.getWarningQuantity();
					String materialCode = sparePartData.getMaterialCode();
					
					int currentQty = Integer.parseInt(quantity);
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
					sparePartData.setOrderQuantity(ResultAvailableQty);
					sparePartData.setWarningQuantity(warningQty);
					sparePartData.setMaterialCode(materialCode);		
					
					eventInfo.setEventName("increaseBySendBack");
		
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));	
				}
				catch (Exception ex)
				{
					throw new CustomException("PMS-0057", PartID);
				}
			}
			else if( useType.equals("Warehousing"))
			{
				// Step 1
				// SparePartInOut(ORDER) Create 
				SparePartInOut orderData = new SparePartInOut(orderNo,PartID);
				orderData.setOrderNo(orderNo);
				orderData.setOrderType(orderType);
				orderData.setUseType(useType);
				orderData.setOrderStatus(orderStatus);
				orderData.setOrderQuantity(Integer.parseInt(orderQty));
				orderData.setOrderReason(orderReason);
				orderData.setOrderUser(orderUser); 
				orderData.setOrderDate(TimeStampUtil.getTimestamp(orderDate));
				orderData.setPartID(PartID);
					
				eventInfo.setEventName("Created");
				
				try
				{
					orderData = PMSServiceProxy.getSparePartInOutService().create(eventInfo, orderData);
				}
				catch(Exception ex)
				{
					throw new CustomException("PMS-0058", orderNo);
				}		
				
				/*
				// Step 2 next stage is approve and efect sparepartQty
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
					String safeQuantity        = sparePartData.getSafeQuantity().toString(); 
					String notInQuantity       = sparePartData.getNotInQuantity().toString(); 
					String purchaseCompleteQty = sparePartData.getPurchaseCompleteQty().toString(); 
					String vendorId            = sparePartData.getVendorID(); 
					String unit                = sparePartData.getUnit(); 
					String unitPrice           = sparePartData.getUnitPrice().toString(); 
					String useFrequency        = sparePartData.getUseFrequency().toString(); 
					String purchaseCycle       = sparePartData.getPurchaseCycle().toString(); 
					String partDesc            = sparePartData.getPartDescription(); 
					String useDesc             = sparePartData.getUseDescription(); 
					String partType            = sparePartData.getPartType(); 
					String partAttribute       = sparePartData.getPartAttribute(); 
					String partGroup           = sparePartData.getPartGroup(); 
					Number warningQty          = sparePartData.getWarningQuantity();
					Number sOrderQty           = sparePartData.getOrderQuantity();
					
					int currentQty = Integer.parseInt(quantity);
					int increaseQty = currentQty + Integer.parseInt(orderQty);
										 
					//Set
					sparePartData = new SparePart(PartID);
					sparePartData.setPartID(PartID);
					sparePartData.setPartName(partName);
					sparePartData.setGroupName(groupName);
					sparePartData.setPartSpec(partSpec);
					sparePartData.setLocation(location);
					sparePartData.setQuantity(increaseQty);
					sparePartData.setSafeQuantity(Integer.parseInt(safeQuantity));
					sparePartData.setNotInQuantity(Integer.parseInt(notInQuantity));
					sparePartData.setPurchaseCompleteQty(Integer.parseInt(purchaseCompleteQty));
					sparePartData.setVendorID(vendorId);
					sparePartData.setUnit(unit);
					sparePartData.setUnitPrice(Integer.parseInt(unitPrice));
					sparePartData.setUseFrequency(Integer.parseInt(useFrequency));
					sparePartData.setPurchaseCycle(Integer.parseInt(purchaseCycle));
					sparePartData.setPartDescription(partDesc);
					sparePartData.setUseDescription(useDesc);
					sparePartData.setPartType(partType);
					sparePartData.setPartAttribute(partAttribute);
					sparePartData.setPartGroup(partGroup);
					sparePartData.setOrderQuantity(sOrderQty);
					sparePartData.setWarningQuantity(warningQty);
										
					eventInfo.setEventName("increaseByWarehousing");
					
					sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));
									
				}
				catch (Exception ex)
				{
					eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Modify SparePart Fail [ PartID = %s] ", PartID));
				}
				*/
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
