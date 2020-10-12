package kr.co.aim.messolution.pms.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.BulletinBoard;
import kr.co.aim.messolution.pms.management.data.BulletinBoardArea;
import kr.co.aim.messolution.pms.management.data.Purchase;
import kr.co.aim.messolution.pms.management.data.SparePart;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class PurchaseModify extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		//Purchase Part
		String PurchaseCode  = SMessageUtil.getBodyItemValue(doc, "POCODE", true);
		String PurchaseQty   = SMessageUtil.getBodyItemValue(doc, "PURCHASEQUANTITY", false);
		String PurchaseType  = SMessageUtil.getBodyItemValue(doc, "PURCHASETYPE", false);
		String AvailablePeriod  = SMessageUtil.getBodyItemValue(doc, "AVAILABLEPERIOD", false);
		String InRequestDate  = SMessageUtil.getBodyItemValue(doc, "INREQUESTDATE", false);
		String InExpectDate  = SMessageUtil.getBodyItemValue(doc, "INEXPECTDATE", false);
		//String PurchaseStatus  = SMessageUtil.getBodyItemValue(doc, "PURCHASESTATUS", true);
		String PurchaseReason  = SMessageUtil.getBodyItemValue(doc, "PURCHASEREASON", false);
		
		//Spare Part
		String PartID 		 = SMessageUtil.getBodyItemValue(doc, "PARTID", true);
		String PartName 	 = SMessageUtil.getBodyItemValue(doc, "PARTNAME", false);
		String PartSpec		 = SMessageUtil.getBodyItemValue(doc, "PARTSPEC", false);
		String CurrentQty		 = SMessageUtil.getBodyItemValue(doc, "CURRENTQTY", false);
		String SafeQty		 = SMessageUtil.getBodyItemValue(doc, "SAFEQUANTITY", false);
		String WarningQty		 = SMessageUtil.getBodyItemValue(doc, "WARNINGQUANTITY", false);	
		String Unit      = SMessageUtil.getBodyItemValue(doc, "UNIT", false);
		String PartType      = SMessageUtil.getBodyItemValue(doc, "PARTTYPE", false);
		String MaterialCode  = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", false);
		String PartAttribute = SMessageUtil.getBodyItemValue(doc, "PARTATTRIBUTE", false);	
		String PartGroup      = SMessageUtil.getBodyItemValue(doc, "PARTGROUP", false);
		String VendorName      = SMessageUtil.getBodyItemValue(doc, "VENDORNAME", false);
		String Location      = SMessageUtil.getBodyItemValue(doc, "LOCATION", false);	
		String PurchaseCycle      = SMessageUtil.getBodyItemValue(doc, "PURCHASECYCLE", false);
		//GroupName		
		String UseDesc      = SMessageUtil.getBodyItemValue(doc, "USEDESCRIPTION", false);
		String PartDesc      = SMessageUtil.getBodyItemValue(doc, "PARTDESCRIPTION", false);
				
		EventInfo eventInfo  = EventInfoUtil.makeEventInfo("PurchaseModify", getEventUser(), getEventComment(), null, null);

		//Modify Purchase
		Purchase purchase = PMSServiceProxy.getPurchaseService().selectByKey(true, new Object[] {PurchaseCode});
		
		//get
		String PhoneNumber = purchase.getPhoneNumber();
		String CreateUser  = purchase.getCreateUser();
		Timestamp CreateTime = purchase.getCreateTime();
		String PurchaseUnit = purchase.getPurchaseUnit();
		String partid = purchase.getPartID();
		String PurchaseStatus  = purchase.getPurchaseStatus();
		String CancelComment    = purchase.getCancelComment();
		String CancelFlag       = purchase.getCancelFlag();
		Number PurchaseQtyold = purchase.getPurchaseQuantity();
		
		//set		
		purchase = new Purchase(PurchaseCode);
		purchase.setPhoneNumber(PhoneNumber);
		purchase.setCreateUser(CreateUser);
		purchase.setCreateTime(CreateTime);
		purchase.setPurchaseUnit(PurchaseUnit);
		purchase.setPartID(partid);
		purchase.setPurchaseStatus(PurchaseStatus);
		purchase.setCancelComment(CancelComment);
		purchase.setCancelFlag(CancelFlag);
		
		if(StringUtil.isNotEmpty(PurchaseQty))
			purchase.setPurchaseQuantity(Integer.parseInt(PurchaseQty));
		
		if(StringUtil.isNotEmpty(PurchaseType))
			purchase.setPurchaseType(PurchaseType);
		
		if(StringUtil.isNotEmpty(AvailablePeriod))
			purchase.setAvailablePeriod(AvailablePeriod);
		
		if(StringUtil.isNotEmpty(InRequestDate))
			purchase.setInRequestDate(TimeStampUtil.getTimestamp(InRequestDate));
		
		if(StringUtil.isNotEmpty(InExpectDate))
			purchase.setInExpectdate(TimeStampUtil.getTimestamp(InExpectDate));
		
		if(StringUtil.isNotEmpty(PurchaseReason))
			purchase.setPurchaseReason(PurchaseReason);
		
		try
		{
			purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0059", PurchaseCode);
		}	
		
		
		//Spare Part
		
		//get
		SparePart sparePartDataInfo = PMSServiceProxy.getSparePartService().selectByKey(true, new Object[] {PartID});
				
		Number NotInQuantity       = sparePartDataInfo.getNotInQuantity();
		Number PurchaseCompleteQty = sparePartDataInfo.getPurchaseCompleteQty();
		String GroupName           = sparePartDataInfo.getGroupName();
		int UseFrequency            = 0;
		int UnitPrice               = 0;	
		
		int realNotInQty =Integer.parseInt(PurchaseQty)- PurchaseQtyold.intValue();
		if(realNotInQty>=0)
		{
			NotInQuantity=NotInQuantity.intValue()+realNotInQty;
		};
		
		if((realNotInQty)<0)
		{
			NotInQuantity=NotInQuantity.intValue()-Math.abs(realNotInQty);
		};
		
		//set
		sparePartDataInfo = new SparePart(PartID);
		
		sparePartDataInfo.setNotInQuantity(NotInQuantity);
		sparePartDataInfo.setPurchaseCompleteQty(PurchaseCompleteQty);	
		sparePartDataInfo.setUnitPrice(UnitPrice);	
		sparePartDataInfo.setUseFrequency(UseFrequency);
		sparePartDataInfo.setGroupName(GroupName);
		
		if(StringUtil.isNotEmpty(PartGroup))
		    sparePartDataInfo.setPartGroup(PartGroup);
		
		if(StringUtil.isNotEmpty(PartName))
		    sparePartDataInfo.setPartName(PartName);
		
		if(StringUtil.isNotEmpty(PartSpec))
		    sparePartDataInfo.setPartSpec(PartSpec);
		
		if(StringUtil.isNotEmpty(Location))
		sparePartDataInfo.setLocation(Location);
		
		if(StringUtil.isNotEmpty(CurrentQty))
		    sparePartDataInfo.setQuantity(Integer.parseInt(CurrentQty));
		
		if(StringUtil.isNotEmpty(WarningQty))
		    sparePartDataInfo.setWarningQuantity(Integer.parseInt(WarningQty));
		
		if(StringUtil.isNotEmpty(SafeQty))
		    sparePartDataInfo.setSafeQuantity(Integer.parseInt(SafeQty));
			
		if(StringUtil.isNotEmpty(Unit))
		    sparePartDataInfo.setUnit(Unit);
			
		if(StringUtil.isNotEmpty(PartType))
		    sparePartDataInfo.setPartType(PartType);
		
		if(StringUtil.isNotEmpty(MaterialCode))
		    sparePartDataInfo.setMaterialCode(MaterialCode);
		
		if(StringUtil.isNotEmpty(PurchaseCycle))
		    sparePartDataInfo.setPurchaseCycle(PurchaseCycle);	
		
		if(StringUtil.isNotEmpty(PartDesc))
		    sparePartDataInfo.setPartDescription(PartDesc);
		
		if(StringUtil.isNotEmpty(UseDesc))
		    sparePartDataInfo.setUseDescription(UseDesc);
				
		if(StringUtil.isNotEmpty(PartAttribute))
		    sparePartDataInfo.setPartAttribute(PartAttribute);	
		
		if(StringUtil.isNotEmpty(VendorName))
		    sparePartDataInfo.setVendorID(VendorName);
		
		try
		{
			sparePartDataInfo = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePartDataInfo);
		}
		catch(Exception ex)
		{
			throw new CustomException("PMS-0057", PartID);
		}	
		
		InsertBoard(PurchaseCode,getEventUser()); // Inser Modify data to Board.
		
		return doc;
		
		
		/*
		List<Purchase> PurchaseList = null;
		try
		{
			PurchaseList = PMSServiceProxy.getPurchaseService().select("PARTID = ?", new Object[] {PartID});
		}
		catch (Exception ex)
		{
			eventLog.error(String.format("<<<<<<<<<<<<<<< No Data this PARTID = %s", PartID));
			PurchaseList = new ArrayList<Purchase>();
		}
		
		for(Purchase purchase : PurchaseList)
		{			
		    try
		    {
		    	//get
		    	TempPOCODE              = purchase.getPoCode();
		    	String PoCode           = purchase.getPoCode();
		    	String PurchaseType     = purchase.getPurchaseType();
		    	String PartId           = purchase.getPartID();
		    	String AvailablePeriod  = purchase.getAvailablePeriod();
		    	Timestamp InrequestDate = purchase.getInRequestDate();
		    	Number PurchaseQty      = purchase.getPurchaseQuantity();
		    	String PurchaseUnit     = purchase.getPurchaseUnit();
		    	String PurchaseReason   = purchase.getPurchaseReason();
		    	String PurchaseStatus   = purchase.getPurchaseStatus();
		    	Timestamp InExpectDate  = purchase.getInExpectdate();
		    	String PhoneNumber      = purchase.getPhoneNumber();
		    	String CreateUser       = purchase.getCreateUser();
				Timestamp CreateTime    = purchase.getCreateTime();
				
		    	//set
		    	purchase = new Purchase(PoCode);
		    	purchase.setPoCode(PoCode);
		    	purchase.setPurchaseType(PurchaseType);
		    	purchase.setPartID(PartId);
		    	purchase.setAvailablePeriod(AvailablePeriod);
		    	purchase.setInRequestDate(InrequestDate);
		    	purchase.setCreateUser(CreateUser);
				purchase.setCreateTime(CreateTime);
				
		    	if(PoCode.equals(PurchaseCode))
		    	{
		    		purchase.setPurchaseQuantity(Integer.parseInt(OrderQty));
		    		purchase.setPurchaseStatus(PoStatus);
		    	}
		    	else
		    	{
		    		purchase.setPurchaseQuantity(PurchaseQty);
		    		purchase.setPurchaseStatus(PurchaseStatus);
		    	}
		    	
		    	purchase.setPurchaseUnit(PurchaseUnit);
		    	purchase.setPurchaseReason(PurchaseReason);	    	
		    	purchase.setInExpectdate(InExpectDate);	
		    	purchase.setPhoneNumber(PhoneNumber);
		    				
		    	purchase = PMSServiceProxy.getPurchaseService().modify(eventInfo, purchase);
		    	
		    }catch (Exception ex)
		    {
		    	throw new CustomException("PMS-0059", TempPOCODE);
		    }
		}
		*/
		/*
	    //Modify SparePart
		SparePart sparePart = null;
		try
		{
			sparePart = PMSServiceProxy.getSparePartService().selectByKey(true,new Object[]{PartID});
			
			//get
			String GroupName      = sparePart.getGroupName();
			String Location       = sparePart.getLocation();
			Number Qty            = sparePart.getQuantity();
			Number SafeQty        = sparePart.getSafeQuantity();
			Number NotInQty       = sparePart.getNotInQuantity();
			Number PurchaseCmpQty = sparePart.getPurchaseCompleteQty();
			String VendorId       = sparePart.getVendorID();
			String Unit           = sparePart.getUnit();
			Number UnitPrice      = sparePart.getUnitPrice();
			Number UseFre         = sparePart.getUseFrequency();
			Number PurchaseCycl   = sparePart.getPurchaseCycle();
			String PartDesc       = sparePart.getPartDescription();
			String UseDesc        = sparePart.getUseDescription();
			String PartType       = sparePart.getPartType();
			String PartAttribute  = sparePart.getPartAttribute();
			String PartGroup      = sparePart.getPartGroup();
			Number Orderqty       = sparePart.getOrderQuantity();
			Number WarningQty     = sparePart.getWarningQuantity();
			
			//set
			sparePart = new SparePart(PartID);
			sparePart.setPartName(PartName);
			sparePart.setGroupName(GroupName);
			sparePart.setPartSpec(PartSpec);
			sparePart.setLocation(Location);
			sparePart.setQuantity(Qty);
			sparePart.setSafeQuantity(SafeQty);
			sparePart.setNotInQuantity(NotInQty);
			sparePart.setPurchaseCompleteQty(PurchaseCmpQty);
			sparePart.setVendorID(VendorId);
			sparePart.setUnit(Unit);
			sparePart.setUnitPrice(UnitPrice);
			sparePart.setUseFrequency(UseFre);
			sparePart.setPurchaseCycle(PurchaseCycl);
			sparePart.setPartDescription(PartDesc);
			sparePart.setUseDescription(UseDesc);
			sparePart.setPartType(PartType);
			sparePart.setPartAttribute(PartAttribute);
			sparePart.setPartGroup(PartGroup);
			sparePart.setOrderQuantity(Orderqty);
			sparePart.setWarningQuantity(WarningQty);	
			
			sparePart = PMSServiceProxy.getSparePartService().modify(eventInfo, sparePart);
		}
		catch (Exception ex)
		{
			throw new CustomException("PMS-0057", PartID);
		}
*/
		/*
		Vendor VendorData = null;
		try
		{
			VendorData = new Vendor(VendorID);
			VendorData.setVendorID(VendorID);
			VendorData.setVendorName(VendorName);
			VendorData.setTelephone(TelePhone);
			VendorData.setMobile(Mobile);
			eventInfo.setEventName("");
			
			VendorData = PMSServiceProxy.getVendorService().modify(eventInfo,VendorData);
			
		}
		catch(Exception ex)
		{
			eventLog.error(String.format( "<<<<<<<<<<<<<<<<<<<<<<< Create Vendor Info Fail [%s] >>>>>>>>>>>>>>>>>>>>>>>>>>", VendorID));
		}*/
		
		
	}
	
	public void InsertBoard(String POCode, String User) throws CustomException
	{
		String [] scopeShopList = new String[]{"Administrator","AMOLED General Manager","AM EQP","AMOLED Design Department","AMOLED EN Production","AMOLED EV Production","AMOLED LTPS Production","MaterialControl","Purchase"
                ,"Facility Department","TFT General Manager","TFT EQP","TFT Array Production","TFT CELL Production","TFT CF Production","TFT 1st technical Department","TFT 2st technical Department","AMOLED 1st technical Department","AMOLED 2st technical Department"};
		
		String no;
		try
		{
			String currentTime = TimeUtils.getCurrentEventTimeKey();		
		    no = currentTime.substring(2, 14) + currentTime.substring(17);			
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}	
		
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBoardPMS", getEventUser(), getEventComment(), null, null);
			
			String title = "PoCode Modify";
			String comments = "Pocode: " + " [" + POCode + "] " + " was modified by " + " [ " + User + " ] " +"." + "\n" + "You can check detail info with PurchaseHistory Function.";
			
			//Create
			BulletinBoard boardData = new BulletinBoard("Administrator", no);
			boardData.setTitle(title);
			boardData.setCreateTime(eventInfo.getEventTime());
			boardData.setCreateUser(eventInfo.getEventUser());
			boardData.setComments(comments);
			
			boardData = PMSServiceProxy.getBulletinBoardService().create(eventInfo, boardData);
			
			for(String eleshop : scopeShopList)
			{
				String scopeShopName = eleshop.toString();
				
				BulletinBoardArea boardAreaData = new BulletinBoardArea(scopeShopName, no);
				boardAreaData = PMSServiceProxy.getBulletinBoardAreaService().create(eventInfo, boardAreaData);
			}
		}
		catch(Exception ex)
		{
			throw new CustomException("MOD-0002");
		}		
	}
}
