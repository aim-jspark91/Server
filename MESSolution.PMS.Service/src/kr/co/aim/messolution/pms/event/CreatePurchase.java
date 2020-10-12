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
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.jdom.Document;


public class CreatePurchase extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		//String PoCode 		   = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PartID 		   = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String AvailablePeriod = SMessageUtil.getBodyItemValue(doc, "AVAILABLEPERIOD", false);
		String InRequestDate   = SMessageUtil.getBodyItemValue(doc, "INREQUESTDATE", true);	
		String PurchaseQty     = SMessageUtil.getBodyItemValue(doc, "PURCHASEQUANTITY", false);
		String PurchaseUnit    = SMessageUtil.getBodyItemValue(doc, "PURCHASEUNIT", false);
		String PurchaseReason  = SMessageUtil.getBodyItemValue(doc, "PURCHASEREASON", false);
		String PurchaseType    = SMessageUtil.getBodyItemValue(doc, "PURCHASETYPE", false);	
		String PhoneNumber     = SMessageUtil.getBodyItemValue(doc, "PHONENUMBER", false);	
		String PurchaseStatus  = "Created";	
		int purchaseQty = Integer.parseInt(PurchaseQty);
		String CancelFlag      = SMessageUtil.getBodyItemValue(doc, "CANCELFLAG", true);	
		String CancelComment   = "";
				
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("CreatePurchase", getEventUser(), getEventComment(), null, null);
		
		
		String PoCode = this.createPOCode();
		
		//Purchase Part
		Purchase purchaseDatainfo = new Purchase(PoCode);
		
		purchaseDatainfo.setPoCode(PoCode);
		purchaseDatainfo.setPartID(PartID);
		purchaseDatainfo.setAvailablePeriod(AvailablePeriod);
		purchaseDatainfo.setInRequestDate(TimeStampUtil.getTimestamp(InRequestDate));
		purchaseDatainfo.setPurchaseQuantity(purchaseQty);	
		purchaseDatainfo.setPurchaseType(PurchaseType);
		purchaseDatainfo.setPurchaseUnit(PurchaseUnit);
		purchaseDatainfo.setPurchaseReason(PurchaseReason);
		purchaseDatainfo.setPurchaseStatus(PurchaseStatus);
		purchaseDatainfo.setPhoneNumber(PhoneNumber);
		purchaseDatainfo.setInExpectdate(TimeStampUtil.getTimestamp(""));
		purchaseDatainfo.setCreateUser(eventInfo.getEventUser());
		purchaseDatainfo.setCreateTime(eventInfo.getEventTime());
		purchaseDatainfo.setCancelFlag(CancelFlag);
		purchaseDatainfo.setCancelComment(CancelComment);

		try
		{
			purchaseDatainfo = PMSServiceProxy.getPurchaseService().create(eventInfo, purchaseDatainfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0061", PoCode);
		}	
		
		//SparePart	
		try
		{
			//Get
			SparePart sparePartData = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
		
			String partName            = sparePartData.getPartName();
			String groupName           = sparePartData.getGroupName();
			String partSpec            = sparePartData.getPartSpec(); 
			String location            = sparePartData.getLocation(); 
			Number quantity            = sparePartData.getQuantity(); 
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
			Number orderQty            = sparePartData.getOrderQuantity();
			Number warningQty          = sparePartData.getWarningQuantity();
			String materialCode        = sparePartData.getMaterialCode();
			
			int RealnotInQty = notInQuantity.intValue() +  purchaseQty;
			 
			//Set
			sparePartData = new SparePart(PartID);
			sparePartData.setPartID(PartID);
			sparePartData.setPartName(partName);
			sparePartData.setGroupName(groupName);
			sparePartData.setPartSpec(partSpec);
			sparePartData.setLocation(location);
			sparePartData.setQuantity(quantity);
			sparePartData.setSafeQuantity(safeQuantity);
			sparePartData.setNotInQuantity(RealnotInQty);
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
			sparePartData.setOrderQuantity(orderQty);
			sparePartData.setWarningQuantity(warningQty);
			sparePartData.setMaterialCode(materialCode);
			
			eventInfo.setEventName("");
			
			sparePartData = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartData); 
			eventLog.error(String.format( "<<<<<<<<<<<<<<<<< Success SparePart modify >>>>>>>>>>>>>>>>>>>>>>"));			
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0057", PartID);
		}
		
		//Set User PhoneNumber
		UserProfileKey keyInfo = new UserProfileKey(eventInfo.getEventUser());
		UserProfile userData = UserServiceProxy.getUserProfileService().selectByKey(keyInfo);
		Map<String, String> udfs = userData.getUdfs();
		
		udfs.put("PHONENUMBER", PhoneNumber);
		
		userData.setUdfs(udfs);
		UserServiceProxy.getUserProfileService().update(userData);
		
		
		//return 
		Document rtnDoc = new Document();
		rtnDoc = (Document)doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "POCODE", PoCode);
		return rtnDoc;
	}
	
	public String createPOCode()  throws CustomException
	{
		String newPoCode = "";
		String currentDate = TimeUtils.getCurrentEventTimeKey();
		String poDate = currentDate.substring(0, 8);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PO", "PJStock");
		nameRuleAttrMap.put("PODATE", poDate);
		nameRuleAttrMap.put("HYPHEN", "-");
		
		//LotID Generate
		try
		{
			int createQty = 1;
			List<String> lstName = CommonUtil.generateNameByNamingRule("POCodeNaming", nameRuleAttrMap, createQty);
			newPoCode = lstName.get(0);
		}
		catch(Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}
		
		return newPoCode;
	}
}
