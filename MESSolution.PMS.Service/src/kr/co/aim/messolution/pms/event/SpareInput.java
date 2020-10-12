package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class SpareInput extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String InputResult         = SMessageUtil.getBodyItemValue(doc, "INPUTRESULT", true);	
		String PoCode              = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PartID              = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String Quantity            = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String PurchaseCompleteQty = SMessageUtil.getBodyItemValue(doc, "PURCHASECOMPLETEQTY", true);
		String PurchaseQty         = SMessageUtil.getBodyItemValue(doc, "PURCHASEQUANTITY", true);
	    int nPurchaseQty = Integer.parseInt(PurchaseQty);
		
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("SpareInput", getEventUser(), getEventComment(), null, null);
	
		SparePart sparePartDataInfo = null;
				
		//get
		sparePartDataInfo    = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
		String PartName      = sparePartDataInfo.getPartName();
		String GroupName     = sparePartDataInfo.getGroupName();
		String PartSpec      = sparePartDataInfo.getPartSpec();
		String Location      = sparePartDataInfo.getLocation();
		Number nQuantity     = Integer.parseInt(Quantity);
		Number SafeQuantity  = sparePartDataInfo.getSafeQuantity();
		Number NotInQuantity = sparePartDataInfo.getNotInQuantity();
		String Unit          = sparePartDataInfo.getUnit();
		Number UnitPrice     = sparePartDataInfo.getUnitPrice();
		Number UseFrequency  = sparePartDataInfo.getUseFrequency();
		String PurchaseCycle = sparePartDataInfo.getPurchaseCycle();
		String PartDesc      = sparePartDataInfo.getPartDescription();
		String UseDesc       = sparePartDataInfo.getUseDescription();
		String PartType      = sparePartDataInfo.getPartType();
		String PartAttribute = sparePartDataInfo.getPartAttribute();		
		String PartGroup     = sparePartDataInfo.getPartGroup();
		String VendorID      = sparePartDataInfo.getVendorID();	
		Number OrderQty      = sparePartDataInfo.getOrderQuantity();
		Number WarningQty    = sparePartDataInfo.getWarningQuantity();
		String materialCode    = sparePartDataInfo.getMaterialCode();
		
		int RealNotInQuantity = NotInQuantity.intValue() - nPurchaseQty;
		
		//set
		sparePartDataInfo = new SparePart(PartID);
		sparePartDataInfo.setPartName(PartName);
		sparePartDataInfo.setGroupName(GroupName);
		sparePartDataInfo.setPartSpec(PartSpec);
		sparePartDataInfo.setLocation(Location);
		sparePartDataInfo.setQuantity(nQuantity);
		sparePartDataInfo.setSafeQuantity(SafeQuantity);
		sparePartDataInfo.setNotInQuantity(RealNotInQuantity);
		sparePartDataInfo.setUnit(Unit);
		sparePartDataInfo.setUnitPrice(UnitPrice);
		sparePartDataInfo.setUseFrequency(UseFrequency);
		sparePartDataInfo.setPurchaseCycle(PurchaseCycle);
		sparePartDataInfo.setPartDescription(PartDesc);
		sparePartDataInfo.setUseDescription(UseDesc);
		sparePartDataInfo.setPartType(PartType);
		sparePartDataInfo.setPartAttribute(PartAttribute);	
		sparePartDataInfo.setPartGroup(PartGroup);
		sparePartDataInfo.setVendorID(VendorID);
		sparePartDataInfo.setOrderQuantity(OrderQty);
		sparePartDataInfo.setWarningQuantity(WarningQty);
		sparePartDataInfo.setPurchaseCompleteQty(Integer.parseInt(PurchaseCompleteQty));
		sparePartDataInfo.setMaterialCode(materialCode);

		try
		{
			sparePartDataInfo = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartDataInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0057", PartID);
		}
		
		//Purchase
		Purchase purchase = null;
		purchase = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[]{PoCode});
		
		//get
		String PurchaseType     = purchase.getPurchaseType();
		String AvailablePeriod  = purchase.getAvailablePeriod();
		Timestamp InRequestDate = purchase.getInRequestDate();
		Number PurchaseQtyBefore      = purchase.getPurchaseQuantity();
		String PurchaseUnit     = purchase.getPurchaseUnit();
		String PurchaseReason   = purchase.getPurchaseReason();
		String PurchaseStatus   ="";
		Timestamp InExpectDate  = purchase.getInExpectdate();
		String CreateUser       = purchase.getCreateUser();
		Timestamp CreateTime    = purchase.getCreateTime();
		String CancelComment    = purchase.getCancelComment();
		String CancelFlag       = purchase.getCancelFlag();
		
		if(InputResult.equals("Y"))
		    PurchaseStatus   = "Completed";//Only change Status
		else
			PurchaseStatus   = "Purchasing";
		
		purchase = new Purchase(PoCode);
		//set
		purchase.setPoCode(PoCode);
		purchase.setPurchaseType(PurchaseType);
		purchase.setPartID(PartID);
		purchase.setAvailablePeriod(AvailablePeriod);
		purchase.setInRequestDate(InRequestDate);
		
		if(InputResult.equals("Y"))
			purchase.setPurchaseQuantity(nPurchaseQty);
		else
			purchase.setPurchaseQuantity(PurchaseQtyBefore.intValue() - nPurchaseQty);
			
		
		purchase.setPurchaseUnit(PurchaseUnit);
		
		purchase.setPurchaseReason(PurchaseReason);		
		purchase.setPurchaseStatus(PurchaseStatus);			
		purchase.setInExpectdate(InExpectDate);
		purchase.setCreateUser(CreateUser);
		purchase.setCreateTime(CreateTime);
		purchase.setCancelComment(CancelComment);
		purchase.setCancelFlag(CancelFlag);

		try
		{
			purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0059", PoCode);
		}
		
		return doc;
	}
}
